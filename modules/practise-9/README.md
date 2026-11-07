# Практика 9. Hibernate: биллинг AI Gateway для Ollama

## Цель

Разработать сервис платного доступа к локальным языковым моделям Ollama. Пользователь пополняет внутренний баланс, получает API-ключ и отправляет запросы к модели через наше Spring-приложение. Сервис измеряет фактический расход LLM-токенов и списывает соответствующее количество кредитов.

В этой работе нужно освоить:

- отображение связанных сущностей через Jakarta Persistence и Hibernate;
- миграции схемы PostgreSQL;
- транзакции и границы `@Transactional`;
- optimistic или pessimistic locking при конкурентном списании;
- идемпотентную обработку повторных HTTP-запросов;
- интеграцию Spring AI с Ollama;
- запуск приложения, PostgreSQL и Ollama через Docker Compose.

Результат работы: клиент с действующим API-ключом может выполнить AI-запрос только при достаточном балансе. После ответа Ollama приложение сохраняет фактическое число входных и выходных токенов, рассчитывает стоимость и создаёт неизменяемые записи в журнале кошелька.

## Предметная область

Не смешивайте три разных понятия:

- **API-ключ** — секрет для аутентификации клиента, например `sk-local-...`;
- **LLM-токены** — единицы текста, обработанные моделью;
- **кредиты** — внутренняя расчётная единица сервиса.

Ollama возвращает в результате запроса `prompt_eval_count` — число токенов входного контекста — и `eval_count` — число сгенерированных токенов. Spring AI предоставляет унифицированный объект `Usage` в метаданных `ChatResponse`.

Пример получения usage (точные имена методов могут зависеть от зафиксированной в проекте версии Spring AI):

```java
ChatResponse response = chatClient.prompt()
    .user(command.prompt())
    .options(OllamaChatOptions.builder()
        .model(command.model())
        .numPredict(command.maxTokens())
        .build())
    .call()
    .chatResponse();

Usage usage = response.getMetadata().getUsage();
int promptTokens = usage.getPromptTokens();
int completionTokens = usage.getCompletionTokens();
```

В старых версиях Spring AI вместо `getCompletionTokens()` может использоваться `getGenerationTokens()`. В проекте должна быть выбрана одна версия зависимости; писать адаптацию сразу под несколько версий не требуется.

## Основной сценарий

Клиент отправляет запрос:

```http
POST /v1/chat/completions
Authorization: Bearer sk-local-abc123
Idempotency-Key: 7d120544-63d4-4890-b2a2-26701ed2c915
Content-Type: application/json

{
  "model": "qwen2.5:0.5b",
  "prompt": "Объясни уровни изоляции транзакций",
  "maxTokens": 300
}
```

Приложение должно:

1. Проверить API-ключ и состояние пользователя.
2. Проверить существование и доступность выбранной модели.
3. Найти тариф, действующий на момент начала запроса.
4. Рассчитать и зарезервировать максимально возможную стоимость.
5. Зафиксировать резерв в БД и завершить транзакцию.
6. Вызвать Ollama через Spring AI.
7. Получить фактическое количество входных и выходных токенов.
8. Рассчитать итоговую стоимость по зафиксированному тарифу.
9. Списать итоговую стоимость и освободить остаток резерва.
10. Вернуть текст ответа и данные об использовании.

Пример успешного ответа:

```json
{
  "requestId": "974209bd-813a-429b-ae1e-291e732b5a93",
  "model": "qwen2.5:0.5b",
  "content": "Уровень изоляции определяет...",
  "usage": {
    "promptTokens": 18,
    "completionTokens": 126,
    "totalTokens": 144,
    "chargedCredits": 270
  },
  "balance": 9730
}
```

## Почему нужен резерв

Проверять баланс после вызова Ollama недостаточно. Пользователь с балансом 100 кредитов может одновременно запустить несколько запросов, каждый из которых стоит до 100 кредитов.

Нельзя также держать блокировку строки кошелька всё время генерации: Ollama может отвечать десятки секунд. Реализуйте две короткие транзакции и внешний вызов между ними:

```text
Транзакция 1
  проверить баланс
  зафиксировать тариф
  создать AI-запрос
  зарезервировать максимальную стоимость
  commit

Вне транзакции
  вызвать Ollama

Транзакция 2
  сохранить фактический usage
  списать стоимость из резерва
  вернуть неиспользованную часть
  завершить AI-запрос
  commit
```

При ошибке Ollama отдельная транзакция переводит запрос в `FAILED` и полностью освобождает резерв. Запланированная задача должна находить зависшие запросы и освобождать их резервы после заданного таймаута.

## Тарификация

Для каждой модели задаётся цена входных и выходных токенов. Например:

| Модель | Вход за 1000 токенов | Выход за 1000 токенов |
| --- | ---: | ---: |
| `qwen2.5:0.5b` | 100 кредитов | 200 кредитов |
| `llama3.2:1b` | 150 кредитов | 300 кредитов |

Формула:

```text
inputCost  = ceil(promptTokens     * inputPricePer1000  / 1000)
outputCost = ceil(completionTokens * outputPricePer1000 / 1000)
totalCost  = inputCost + outputCost
```

Все денежные значения храните целыми числами минимальных единиц. Не используйте `float` или `double`. Округление должно выполняться вверх отдельно для входа и выхода.

Для резерва разрешается приблизительно оценить входные токены по тексту либо заранее запросить токенизацию. Способ оценки нужно описать в README решения. Выходная часть резерва рассчитывается по `maxTokens`. При слишком низкой оценке фактическая стоимость всё равно не должна сделать доступный баланс отрицательным.

## Модель данных

Минимальная модель:

```text
User 1 ─── * ApiKey
User 1 ─── 1 Wallet 1 ─── * WalletTransaction
User 1 ─── * AiRequest * ─── 1 Model
Model 1 ─── * ModelPrice
AiRequest 1 ─── 0..1 UsageRecord
```

### `users`

```text
id            UUID, PK
email         VARCHAR, UNIQUE, NOT NULL
status        VARCHAR, NOT NULL
created_at    TIMESTAMPTZ, NOT NULL
```

Статусы: `ACTIVE`, `BLOCKED`.

### `api_keys`

```text
id            UUID, PK
user_id       UUID, FK, NOT NULL
key_prefix    VARCHAR, NOT NULL
key_hash      VARCHAR, UNIQUE, NOT NULL
status        VARCHAR, NOT NULL
created_at    TIMESTAMPTZ, NOT NULL
last_used_at  TIMESTAMPTZ
expires_at    TIMESTAMPTZ
```

Полное значение ключа показывается только один раз при создании. В БД хранится криптографический хеш и короткий безопасный префикс для отображения. Не логируйте полный API-ключ и текст пользовательского prompt.

### `wallets`

```text
id                UUID, PK
user_id           UUID, UNIQUE, FK, NOT NULL
balance           BIGINT, NOT NULL
reserved_balance  BIGINT, NOT NULL
version           BIGINT, NOT NULL
```

Инварианты:

```text
balance >= 0
reserved_balance >= 0
reserved_balance <= balance
available = balance - reserved_balance
```

Поле `version` отображается через `@Version`. Вместо optimistic locking разрешён `PESSIMISTIC_WRITE`, но выбор нужно обосновать и проверить конкурентным тестом.

### `wallet_transactions`

```text
id             UUID, PK
wallet_id      UUID, FK, NOT NULL
ai_request_id  UUID, FK, NULL
type           VARCHAR, NOT NULL
amount         BIGINT, NOT NULL
balance_after  BIGINT, NOT NULL
reserved_after BIGINT, NOT NULL
created_at     TIMESTAMPTZ, NOT NULL
```

Типы операций:

- `TOP_UP` — пополнение;
- `RESERVE` — резервирование;
- `CHARGE` — окончательное списание;
- `RELEASE` — освобождение резерва;
- `ADJUSTMENT` — ручная корректировка администратора.

Журнал операций является append-only: записи нельзя обновлять и удалять через публичный API.

### `models` и `model_prices`

`Model` описывает доступную в Ollama модель и её состояние. `ModelPrice` содержит `inputPricePer1000`, `outputPricePer1000`, `validFrom` и `validTo`.

Изменение цены создаёт новую запись тарифа. Старые строки не обновляются: начавшийся запрос должен завершиться по цене, действовавшей в момент резервирования.

### `ai_requests`

```text
id                  UUID, PK
user_id             UUID, FK, NOT NULL
api_key_id          UUID, FK, NOT NULL
model_id            UUID, FK, NOT NULL
model_price_id      UUID, FK, NOT NULL
idempotency_key     VARCHAR, NOT NULL
status              VARCHAR, NOT NULL
max_output_tokens   INTEGER, NOT NULL
reserved_credits    BIGINT, NOT NULL
actual_cost         BIGINT
created_at          TIMESTAMPTZ, NOT NULL
completed_at        TIMESTAMPTZ
error_code          VARCHAR
```

Статусы:

```text
RESERVED -> PROCESSING -> COMPLETED
                       -> FAILED
                       -> EXPIRED
```

Добавьте уникальное ограничение `(user_id, idempotency_key)`.

### `usage_records`

```text
id                  UUID, PK
ai_request_id       UUID, UNIQUE, FK, NOT NULL
prompt_tokens       INTEGER, NOT NULL
completion_tokens   INTEGER, NOT NULL
total_tokens        INTEGER, NOT NULL
model_name          VARCHAR, NOT NULL
created_at          TIMESTAMPTZ, NOT NULL
```

Не сохраняйте только итоговую сумму: отдельные значения нужны для проверки тарификации и аналитики.

## Требования к Hibernate

1. Сущности не должны напрямую сериализоваться в HTTP-ответы. Используйте request/response DTO.
2. Все коллекции по умолчанию загружайте лениво.
3. Не исправляйте `LazyInitializationException` массовым переходом на `EAGER`.
4. Для экранов списка используйте проекции, `join fetch` или `@EntityGraph`, когда это действительно нужно.
5. Продемонстрируйте и устраните N+1 хотя бы в одном тесте или описанном сценарии.
6. Внешний вызов Ollama не выполняется внутри транзакции, удерживающей блокировку кошелька.
7. Схема создаётся Liquibase. `ddl-auto=create` и `ddl-auto=update` для итогового решения запрещены.
8. Изменения Liquibase должны быть атомарно разнесены по файлам: одна новая таблица, индекс или другое изменение схемы — один файл. Учебные данные находятся в отдельном seed-файле; master changelog определяет только порядок подключения файлов.
9. Ограничения предметной области дублируются подходящими `NOT NULL`, `UNIQUE`, `CHECK` и внешними ключами БД.
10. Для времени используйте `Instant` и PostgreSQL `TIMESTAMPTZ`.
11. Удаление пользователя, кошелька или журнала транзакций через API не требуется.
12. Для JPA boilerplate используйте Lombok: `@Getter` и защищённый `@NoArgsConstructor`. Общие `@Setter` на сущностях запрещены; состояние меняется через предметные методы вроде `reserve`, `charge` и `release`. Конструкторы внедрения оформляйте через `@RequiredArgsConstructor`.

## HTTP API

Минимальный контракт:

| Метод и путь | Действие |
| --- | --- |
| `POST /api/users` | Создать пользователя и пустой кошелёк |
| `POST /api/users/{id}/api-keys` | Выпустить API-ключ |
| `DELETE /api/api-keys/{id}` | Отозвать API-ключ |
| `POST /api/users/{id}/top-ups` | Учебное пополнение баланса |
| `GET /api/users/{id}/wallet` | Получить баланс и резерв |
| `GET /api/users/{id}/wallet/transactions` | Получить страницу операций |
| `GET /api/models` | Получить доступные модели и тарифы |
| `POST /v1/chat/completions` | Выполнить запрос к Ollama |
| `GET /api/requests/{id}` | Получить состояние и usage запроса |

`POST /api/users/{id}/top-ups` заменяет реальную платёжную систему. Подключать банк или вводить данные банковских карт в учебной работе не нужно.

Для списков обязательны пагинация и стабильная сортировка. Не возвращайте все операции кошелька одним неограниченным массивом.

### Ошибки

Используйте единый формат:

```json
{
  "code": "INSUFFICIENT_CREDITS",
  "message": "Недостаточно кредитов",
  "requestId": "974209bd-813a-429b-ae1e-291e732b5a93"
}
```

Минимальные ситуации:

| Ситуация | HTTP-код |
| --- | ---: |
| Некорректное тело или параметры | `400` |
| Нет API-ключа или он недействителен | `401` |
| Недостаточно кредитов | `402` |
| Пользователь или модель не найдены | `404` |
| Повторный idempotency key с другим телом | `409` |
| Ollama временно недоступна | `503` |

Повтор запроса с тем же `Idempotency-Key` и тем же телом не создаёт новую операцию и не списывает баланс повторно. Можно вернуть сохранённый результат или текущее состояние исходного запроса.

## Docker Compose

В каталоге практики находятся:

- [`docker-compose.yml`](docker-compose.yml) — приложение, PostgreSQL, Ollama и загрузчик модели;
- [`Dockerfile`](Dockerfile) — multi-stage сборка Spring Boot приложения.
- [`db.changelog-master.yaml`](src/main/resources/db/changelog/db.changelog-master.yaml) — порядок атомарных Liquibase changeset-файлов.

Compose создаёт постоянные volumes для PostgreSQL и моделей Ollama. Контейнер `ollama-pull` дожидается готовности Ollama, скачивает выбранную модель и успешно завершается. Приложение запускается только после готовности БД и окончания загрузки модели.

### Что такое `ollama-pull`

`ollama-pull` — это одноразовый init-контейнер, а не второй сервер с моделью. Основной контейнер `ollama` запускает HTTP API на порту `11434`, но чистая установка ещё не содержит ни одной языковой модели. Поэтому Compose выполняет следующую последовательность:

```text
ollama запускается и становится healthy
              ↓
ollama-pull выполняет: ollama pull qwen2.5:0.5b
              ↓
модель сохраняется в общем volume ollama_data
              ↓
ollama-pull завершается с exit code 0
              ↓
запускается application
```

Оба Ollama-контейнера используют один volume `ollama_data`. Загрузчик помещает файлы модели в этот volume, а основной сервер сразу видит их и может обрабатывать запросы. После успешного скачивания состояние `ollama-pull` — `Exited (0)`; это ожидаемое успешное завершение, а не падение. При следующем запуске `ollama pull` проверит уже сохранённую модель и не станет заново скачивать все 398 МБ. Если загрузка завершится ошибкой, `application` не стартует благодаря условию `service_completed_successfully`.

По умолчанию используется компактная модель [`qwen2.5:0.5b`](https://ollama.com/library/qwen2.5:0.5b): образ модели занимает около 398 МБ, поддерживает русский язык и подходит для первого запуска на обычном ноутбуке. Её можно заменить переменной окружения:

```bash
OLLAMA_MODEL=llama3.2:1b docker compose \
  -f modules/practise-9/docker-compose.yml up --build
```

Первый запуск скачивает образ Ollama и модель, поэтому требует подключения к интернету и может занять несколько минут. Последующие запуски используют Docker volume.

Запуск из корня репозитория:

```bash
docker compose -f modules/practise-9/docker-compose.yml up --build
```

Проверка:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:11435/api/tags
```

Логи:

```bash
docker compose -f modules/practise-9/docker-compose.yml logs -f application
```

Остановка без удаления данных:

```bash
docker compose -f modules/practise-9/docker-compose.yml down
```

Удаление контейнеров вместе с учебной БД и скачанными моделями:

```bash
docker compose -f modules/practise-9/docker-compose.yml down -v
```

Последняя команда удаляет данные необратимо; используйте её только для полного сброса практики.

Внутри Docker приложение обращается к сервисам по именам контейнеров:

```text
PostgreSQL: jdbc:postgresql://postgres:5432/ai_gateway
Ollama:     http://ollama:11434
```

`localhost` внутри контейнера приложения указывает на само приложение и не подходит для соединения с PostgreSQL или Ollama.

### Требования к конфигурации приложения

Приложение должно читать как минимум следующие переменные:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
SPRING_AI_OLLAMA_BASE_URL
APP_OLLAMA_MODEL
```

Пароли и секреты не должны находиться в Java-коде. Значения в Compose являются локальными учебными значениями и не подходят для production.

### Быстрая проверка реализованного сервиса

После запуска зарегистрируйте пользователя:

```bash
curl -X POST http://localhost:8080/api/users \
  -H 'Content-Type: application/json' \
  -d '{"email":"student@example.com"}'
```

Скопируйте `id` из ответа и условно пополните баланс:

```bash
curl -X POST http://localhost:8080/api/users/<USER_ID>/top-ups \
  -H 'Content-Type: application/json' \
  -d '{"amount":10000}'
```

Выпустите API-ключ. Полное поле `key` возвращается только в этом ответе и не хранится в БД открытым текстом:

```bash
curl -X POST http://localhost:8080/api/users/<USER_ID>/api-keys
```

Отправьте prompt:

```bash
curl -X POST http://localhost:8080/v1/chat/completions \
  -H 'Authorization: Bearer <API_KEY>' \
  -H 'Idempotency-Key: first-request' \
  -H 'Content-Type: application/json' \
  -d '{"model":"qwen2.5:0.5b","prompt":"Зачем нужны транзакции?","maxTokens":80}'
```

Ответ содержит фактический usage, который Spring AI получил от Ollama:

```json
{
  "usage": {
    "promptTokens": 18,
    "completionTokens": 42,
    "totalTokens": 60,
    "chargedCredits": 11
  },
  "balance": 9989
}
```

Посмотреть журнал резервов и списаний:

```bash
curl 'http://localhost:8080/api/users/<USER_ID>/wallet/transactions?page=0&size=20'
```

Повторите chat-запрос с тем же телом и `Idempotency-Key`: сервис вернёт сохранённый ответ с тем же `requestId`, не вызовет Ollama повторно и не спишет кредиты второй раз.

### Postman Collection

Готовая коллекция находится в [`postman/practise-9.postman_collection.json`](postman/practise-9.postman_collection.json). Импортируйте файл в Postman и запустите всю коллекцию через **Run collection** в исходном порядке. Никакие environment-файлы не нужны: `baseUrl`, идентификаторы, временный API-ключ и `Idempotency-Key` хранятся в collection variables и заполняются test scripts автоматически.

Коллекция выполняет 12 запросов и проверяет:

- health приложения;
- регистрацию и нулевой начальный баланс;
- условное пополнение на 10 000 кредитов;
- выпуск API-ключа;
- реальный prompt/completion usage от Ollama;
- уменьшение баланса и освобождение резерва;
- отсутствие повторного списания при том же `Idempotency-Key`;
- наличие `TOP_UP`, `RESERVE`, `CHARGE` и `RELEASE` в журнале;
- `402 INSUFFICIENT_CREDITS` для пустого кошелька;
- `401 INVALID_API_KEY` для неизвестного ключа.

Ту же коллекцию можно запустить без UI через Newman:

```bash
npx --yes newman run \
  modules/practise-9/postman/practise-9.postman_collection.json
```

### Allure E2E-тесты

E2E-кейсы находятся в [`AiGatewayApiE2eTest`](src/test/java/studying/gateway/e2e/AiGatewayApiE2eTest.java). Они выключены при обычном unit-test запуске и активируются только при передаче адреса уже запущенного сервиса:

```bash
./gradlew :modules:practise-9:test \
  -De2e.base-url=http://localhost:8080
```

Проверяются три бизнес-сценария:

1. реальный запрос к Ollama, сохранение usage и списание кредитов;
2. идемпотентный повтор без второго списания;
3. отказ до вызова Ollama при недостаточном балансе.

Сформировать HTML-отчёт:

```bash
./gradlew :modules:practise-9:allureReport
```

Готовый отчёт появится в `modules/practise-9/build/reports/allure-report/allureReport/index.html`. Для интерактивного просмотра также можно выполнить `./gradlew :modules:practise-9:allureServe`.

## Что проверить тестами

### Репозитории

- сохранение связей и ограничений;
- поиск действующего тарифа на заданный момент;
- уникальность email, хеша API-ключа и idempotency key;
- пагинация журнала операций;
- отсутствие N+1 в выбранном сценарии.

Для интеграционных тестов БД рекомендуется Testcontainers с PostgreSQL, а не H2: поведение типов, блокировок и ограничений должно совпадать с рабочей БД.

### Биллинг

1. Успешный запрос создаёт резерв, usage и окончательное списание.
2. Неиспользованная часть резерва освобождается.
3. Ошибка Ollama освобождает весь резерв.
4. Изменение тарифа во время генерации не меняет цену начатого запроса.
5. Повторный `Idempotency-Key` не вызывает Ollama и не списывает кредиты второй раз.
6. Один ключ нельзя использовать после отзыва или истечения срока.

### Конкурентность

Пусть доступный баланс равен 100 кредитам. Два параллельных запроса пытаются зарезервировать по 80 кредитов. Ровно один запрос должен получить резерв; второй завершается ошибкой недостаточного баланса. Итоговые `balance` и `reserved_balance` не могут быть отрицательными.

Тест должен действительно запускать операции параллельно, например через `ExecutorService` и `CountDownLatch`. Последовательный вызов двух методов не проверяет блокировки.

### Сбои

- timeout при обращении к Ollama;
- ответ Ollama без usage или с некорректным usage;
- исключение между резервом и вызовом модели;
- исключение после ответа модели, но до финального списания;
- восстановление зависшего резерва scheduled-задачей.

## Дополнительные задания

- Реализовать streaming-ответ через Server-Sent Events. Резерв выполняется до начала потока, окончательное списание — после получения финального usage.
- Добавить дневной и месячный лимит для API-ключа.
- Добавить разные права ключей: `CHAT`, `EMBEDDINGS`, `ADMIN`.
- Реализовать Telegram-бота для проверки баланса и выпуска ключей.
- Добавить endpoint статистики расходов по пользователям, моделям и дням.
- Добавить outbox-событие `CreditsCharged` и асинхронную отправку уведомления о низком балансе.
- Поддержать tool calling и учесть суммарный usage всех обращений к модели внутри одного пользовательского запроса.

## Что сдавать

- исходный код Spring Boot приложения;
- сущности, репозитории, сервисы, контроллеры и DTO;
- атомарные миграции Liquibase и отдельный changelog с учебными данными;
- Dockerfile и Docker Compose, поднимающий приложение, PostgreSQL и Ollama;
- unit- и интеграционные тесты;
- краткое описание выбранной стратегии блокировок и оценки резерва;
- примеры HTTP-запросов для полного сценария: пользователь → пополнение → API-ключ → AI-запрос → история списаний.

## Критерии готовности

Работа готова, когда одной командой Docker Compose запускаются PostgreSQL, Ollama и реализованное приложение; модель автоматически загружается; запрос с действующим ключом и достаточным балансом завершается ответом; фактический usage сохраняется; баланс корректно списывается; повтор и конкурентный запрос не приводят к двойному или отрицательному списанию.

На защите нужно уметь объяснить:

- почему вызов Ollama находится между двумя транзакциями;
- чем `balance` отличается от `reserved_balance`;
- какую гонку предотвращает выбранная блокировка;
- почему журнал кошелька нельзя заменить одним полем баланса;
- зачем запрос хранит ссылку на конкретную версию тарифа;
- почему API-ключ нельзя хранить в БД открытым текстом;
- как Hibernate-запросы избегают N+1.

## Справочные материалы

- [Spring AI: Ollama Chat](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html)
- [Spring AI: Chat Response Metadata и Usage](https://docs.spring.io/spring-ai/reference/api/aimetadata.html)
- [Ollama Chat API](https://docs.ollama.com/api/chat)
- [Spring Data JPA: Locking](https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html)
- [Hibernate ORM User Guide](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html)

