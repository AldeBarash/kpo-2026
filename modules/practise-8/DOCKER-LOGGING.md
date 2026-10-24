# Логи приложения в ELK

Запуск из корня репозитория:

```bash
./gradlew :modules:practise-8:bootJar
docker compose -f modules/practise-8/docker-compose.yml up -d --build
```

Перед сборкой Docker-образа нужно пересобрать JAR: Dockerfile использует
`build/libs/practise-8.jar`, созданный Gradle.
Контейнер приложения занимает порт 8080; остановите локальный запуск из IDE,
если он использует этот порт.

## Как проходят логи

Приложение пишет обычные логи в консоль и JSON в формате ECS в
`/var/log/application/application.json`. Это встроенная возможность Spring Boot:
отдельные зависимости для логирования не нужны. Логи аспектов попадают туда же.
Настройки JSON и файла включены только в Docker Compose.

Logstash читает JSON из общего volume `application_logs`, включая ротированные
файлы, и отправляет события в Elasticsearch. Позиция чтения и очередь Logstash
сохраняются в volume `logstash_data`, индексы — в `elasticsearch_data`.
Ротация ограничена 10 MB на файл, 7 днями и 100 MB суммарно.

## Просмотр

- Приложение: http://localhost:8080/health
- Метрики: http://localhost:8080/prometheus
- Kibana: http://localhost:5601
- Elasticsearch: http://localhost:9200
- Grafana: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9090

В Kibana откройте Discover. Если Data View ещё не создан, создайте его:

- шаблон индекса: `notification-delivery-*`;
- поле времени: `@timestamp`.

Для поиска используйте `service.name: "notification-delivery"`,
`log.level: "ERROR"` или `message: *demo.annotation*`.
После старта Main делает пять демонстрационных вызовов аннотированного метода;
их результаты появятся в логах. При первом старте ELK нужно время на загрузку
образов и инициализацию; если события не видны, расширьте диапазон времени.

Проверка доставки логов:

```bash
curl 'http://localhost:9200/notification-delivery-*/_count'
curl 'http://localhost:9200/notification-delivery-*/_search?size=5&pretty'
docker compose -f modules/practise-8/docker-compose.yml logs --tail=100 logstash
```

Остановка с сохранением данных:

```bash
docker compose -f modules/practise-8/docker-compose.yml down
```

Это локальная учебная конфигурация: Elasticsearch и Kibana доступны только
на loopback-интерфейсе, а аутентификация Elasticsearch отключена.
Все компоненты ELK закреплены на одной версии 9.2.3.
Prometheus собирает метрики контейнера `application:8080`.

Документация: [JSON-логи Spring Boot](https://docs.spring.io/spring-boot/reference/features/logging.html),
[Logstash в Docker](https://www.elastic.co/guide/en/logstash/current/docker.html).
