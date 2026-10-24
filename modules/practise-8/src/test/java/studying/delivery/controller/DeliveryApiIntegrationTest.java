package studying.delivery.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import studying.delivery.model.AttemptResult;
import studying.delivery.observer.DeliveryJournal;
import studying.delivery.repository.DeliveryRepository;
import studying.delivery.sender.NotificationSender;
import studying.delivery.service.DeliveryService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Проверяет HTTP-контракт с реальными фабриками, сервисами и Spring AOP. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DeliveryApiIntegrationTest {
    private static final Instant READY = Instant.parse("2026-10-23T12:00:00Z");
    private static final String BASE = "/api/deliveries";
    private static final String PROCESS = "/api/delivery-processing";
    private static final String REQUEST = """
            {"channel":"EMAIL","recipient":"student@example.com",
             "text":"Практика 8","priority":"NORMAL",
             "readyAt":"2026-10-23T12:00:00Z",
             "retry":{"maxAttempts":3,"baseDelaySeconds":10}}
            """;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private DeliveryRepository repository;
    @Autowired
    private DeliveryService deliveryService;
    @Autowired
    private MeterRegistry metrics;
    @MockitoBean
    private Clock clock;
    @MockitoSpyBean
    private NotificationSender sender;
    @MockitoSpyBean
    private DeliveryJournal journal;

    @BeforeEach
    void setTime() {
        when(clock.instant()).thenReturn(READY);
    }

    @Test
    void emptyQueueIsSuccessful() throws Exception {
        mvc.perform(get(BASE)).andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        process(0);
    }

    @Test
    void createsAndReadsAllChannelsWithoutSending() throws Exception {
        List<String> ids = new ArrayList<>();
        for (String channel : List.of("EMAIL", "PUSH", "IN_APP")) {
            String location = create(REQUEST.replace("EMAIL", channel));
            ids.add(location.substring(location.lastIndexOf('/') + 1));
            mvc.perform(get(location)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.channel").value(channel))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.attemptsMade").value(0))
                    .andExpect(jsonPath("$.policy").doesNotExist());
            var saved = repository.find(UUID.fromString(ids.getLast()))
                    .orElseThrow();
            assertEquals("2026-10-23T12:00",
                    saved.notification().createdAt().toString());
        }
        var list = json.readTree(mvc.perform(get(BASE)).andReturn()
                .getResponse().getContentAsString());
        assertEquals(3, list.size());
        for (int i = 0; i < ids.size(); i++) {
            assertEquals(ids.get(i), list.get(i).get("id").asString());
        }
        assertTrue(journal.history(UUID.fromString(ids.getFirst())).isEmpty());
        assertEquals(0, metrics.find("notification.send").timers().size());
    }

    @Test
    void retriesAtBoundaryAndNeverResendsCompletedDelivery() throws Exception {
        String location = create(REQUEST);
        when(clock.instant()).thenReturn(READY.minusSeconds(1));
        process(0);
        assertState(location, "PENDING", 0, READY);
        when(clock.instant()).thenReturn(READY);
        process(0);
        assertState(location, "PENDING", 1, READY.plusSeconds(10));
        when(clock.instant()).thenReturn(READY.plusSeconds(9));
        process(0);
        assertState(location, "PENDING", 1, READY.plusSeconds(10));
        when(clock.instant()).thenReturn(READY.plusSeconds(10));
        process(0);
        assertState(location, "SUCCEEDED", 2, null);
        process(0);
        assertState(location, "SUCCEEDED", 2, null);
        assertTrue(AopUtils.isAopProxy(deliveryService));
        assertEquals(1, metrics.get("delivery.enqueue").timer().count());
        assertEquals(5, metrics.get("delivery.process").timer().count());
        assertEquals(2, metrics.get("notification.send").timer().count());
    }

    @Test
    void rejectsInvalidInputWithoutSaving() throws Exception {
        List<String> invalid = List.of("{", "null", "{}",
                REQUEST.replace("EMAIL", "SMS"),
                REQUEST.replace("NORMAL", "URGENT"),
                REQUEST.replace("student@example.com", "   "),
                REQUEST.replace("Практика 8", ""),
                REQUEST.replace("2026-10-23T12:00:00Z", "yesterday"),
                REQUEST.replace("2026-10-23T12:00:00Z", "2026-10-23T12:00:00"),
                REQUEST.replace("\"maxAttempts\":3", "\"maxAttempts\":0"),
                REQUEST.replace("\"maxAttempts\":3", "\"maxAttempts\":null"),
                REQUEST.replace("\"maxAttempts\":3,", ""),
                REQUEST.replace("\"baseDelaySeconds\":10",
                        "\"baseDelaySeconds\":-1"),
                REQUEST.replace("\"baseDelaySeconds\":10",
                        "\"baseDelaySeconds\":null"),
                REQUEST.replace("{\"maxAttempts\":3,\"baseDelaySeconds\":10}",
                        "null"),
                REQUEST.replace("\"maxAttempts\":3", "\"maxAttempts\":1.5"),
                REQUEST.replace("\"baseDelaySeconds\":10",
                        "\"baseDelaySeconds\":0.5"),
                REQUEST.replace("\"EMAIL\"", "0"));
        for (String body : invalid) {
            mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                    .content(body)).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        }
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void distinguishesBadIdMissingResourceAndHttpErrors() throws Exception {
        mvc.perform(get(BASE + "/invalid")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mvc.perform(get(BASE + "/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DELIVERY_NOT_FOUND"));
        mvc.perform(put(BASE)).andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("HTTP_405"));
        mvc.perform(post(BASE).contentType(MediaType.TEXT_PLAIN).content("hi"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("HTTP_415"));
    }

    @Test
    void stopsAtAttemptLimit() throws Exception {
        String location = create(REQUEST.replace("\"maxAttempts\":3",
                "\"maxAttempts\":1"));
        process(0);
        assertState(location, "FAILED", 1, null);
        when(clock.instant()).thenReturn(READY.plusSeconds(100));
        process(0);
        assertState(location, "FAILED", 1, null);
    }

    @Test
    void permanentFailureDoesNotBecomeHttp500() throws Exception {
        doReturn(new AttemptResult(AttemptResult.Kind.PERMANENT_FAILURE,
                "Unknown recipient")).when(sender).send(any());
        String location = create(REQUEST);
        process(0);
        assertState(location, "FAILED", 1, null);
        process(0);
        assertState(location, "FAILED", 1, null);
    }

    @Test
    void observerFailureDoesNotRollBackAttempt() throws Exception {
        doThrow(new IllegalStateException("private observer details"))
                .when(journal).onAttemptFinished(any());
        String location = create(REQUEST);
        process(1);
        assertState(location, "PENDING", 1, READY.plusSeconds(10));
    }

    @Test
    void exhaustionKeepsPreviouslyCompletedDelivery() throws Exception {
        String first = create(REQUEST);
        process(0);
        when(clock.instant()).thenReturn(READY.plusSeconds(10));
        process(0);
        String second = create(REQUEST);
        var response = mvc.perform(post(PROCESS))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Внутренняя ошибка сервера"))
                .andReturn().getResponse().getContentAsString();
        assertFalse(response.contains("SENDER_RESULTS_EXHAUSTED"));
        assertFalse(response.contains("stackTrace"));
        assertState(first, "SUCCEEDED", 2, null);
        assertState(second, "PENDING", 0, READY);
    }

    @Test
    void concurrentProcessingMakesOnlyOneDueAttempt() throws Exception {
        String location = create(REQUEST);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = executor.submit(() -> {
                start.await();
                return mvc.perform(post(PROCESS)).andReturn()
                        .getResponse().getStatus();
            });
            var second = executor.submit(() -> {
                start.await();
                return mvc.perform(post(PROCESS)).andReturn()
                        .getResponse().getStatus();
            });
            start.countDown();
            assertEquals(200, first.get());
            assertEquals(200, second.get());
        }
        assertState(location, "PENDING", 1, READY.plusSeconds(10));
        assertEquals(1, metrics.get("notification.send").timer().count());
    }

    private String create(String body) throws Exception {
        var response = mvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.attemptsMade").value(0))
                .andReturn().getResponse();
        String location = response.getHeader("Location");
        assertEquals(BASE + "/" + json.readTree(response.getContentAsString())
                .get("id").asString(), location);
        return location;
    }

    private void process(int failures) throws Exception {
        mvc.perform(post(PROCESS)).andExpect(status().isOk())
                .andExpect(jsonPath("$.observerFailuresCount").value(failures));
    }

    private void assertState(String location, String state, int attempts,
            Instant next) throws Exception {
        JsonNode response = json.readTree(mvc.perform(get(location))
                .andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString());
        assertEquals(state, response.get("status").asString());
        assertEquals(attempts, response.get("attemptsMade").asInt());
        if (next == null) {
            assertTrue(response.get("nextAttemptAt").isNull());
        } else {
            assertEquals(next.toString(),
                    response.get("nextAttemptAt").asString());
        }
    }
}
