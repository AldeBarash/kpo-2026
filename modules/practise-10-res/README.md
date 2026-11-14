# Практика 10 — решение из двух микросервисов

AI Gateway и Telegram-бот работают как независимые Spring Boot приложения. У каждого свой PostgreSQL, пользователь БД, Liquibase changelog и Dockerfile. Общая связь — Kafka и защищённый внутренний HTTP API; чтения таблиц соседнего сервиса нет.

```text
Клиент → Gateway :8080 → Ollama
             │
       gateway-db
       биллинг + outbox
             │
             ↓
 Kafka: billing.credits-charged.v1
             │
             ↓
     Telegram-сервис :8081 ───→ Telegram Bot API
             │
        telegram-db
    привязки + inbox + очередь
             │
             └── HTTP → Gateway: погашение кода и баланс
```

## Структура и ответственность

- [`gateway`](gateway) — HTTP API практики 9, пользователи, API-ключи, кошельки, Ollama, одноразовые коды, квитанции погашения и publisher outbox. Telegram-токена и Kafka consumer здесь нет.
- [`telegram-service`](telegram-service) — long polling, команды, привязки, Kafka consumer, inbox и очередь уведомлений. Здесь нет сущностей кошелька, API-ключей, зависимости от Spring AI или доступа к `gateway-db`.
- [`docker-compose.yml`](docker-compose.yml) — оба приложения, две БД, Kafka в KRaft, создание топиков и загрузка модели Ollama.

Корневой модуль — агрегатор сборки, отдельного монолитного приложения больше нет. DTO события описан отдельно в каждом сервисе: runtime-зависимости сервисов друг от друга нет. Только интеграционные тесты Telegram-сервиса подключают Gateway как тестовую зависимость и запускают оба контекста с разными портами и БД.

Конфигурации: [`gateway.yaml`](gateway/src/main/resources/gateway.yaml) и [`telegram.yaml`](telegram-service/src/main/resources/telegram.yaml). Каждый main выбирает своё имя конфигурации. Пути Liquibase также разделены: `db/gateway` и `db/telegram`.

## Запуск

Нужны Docker Compose, свободные порты `8080`, `8081`, `5432`, `5433`, `9092`, `11435` и интернет при первой загрузке образов и модели.

Из корня репозитория:

```bash
cp modules/practise-10-res/.env.example modules/practise-10-res/.env
```

Заполните локальный `.env`:

```dotenv
TELEGRAM_BOT_ENABLED=true
TELEGRAM_BOT_TOKEN=токен_из_BotFather
INTERNAL_API_TOKEN=общий_случайный_секрет_не_короче_32_символов
OLLAMA_MODEL=qwen2.5:0.5b
```

`INTERNAL_API_TOKEN` должен совпадать у двух сервисов. Compose передаёт его в оба приложения. В примере предусмотрено локальное учебное значение; для собственного окружения задайте свой секрет. Настоящий `.env` исключён из Git и Docker build context.

```bash
docker compose --env-file modules/practise-10-res/.env \
  -f modules/practise-10-res/docker-compose.yml up --build -d
```

Без Telegram можно оставить `TELEGRAM_BOT_ENABLED=false`, токен тогда не нужен. Consumer всё равно читает Kafka и сохраняет задания; включение бота позволяет доставить накопившиеся уведомления с действующей привязкой.

Проверка приложений:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

Первый старт Ollama скачивает модель. Telegram-сервис может стартовать раньше Gateway: пока Gateway недоступен, `/help` и `/unlink` работают, а `/link` и `/balance` повторяются после восстановления HTTP-связи.

Compose использует **новые volumes** `gateway_db_data` и `telegram_db_data`. Это новый учебный стенд: автоматического переноса данных из прежней монолитной БД нет. Старый volume не удаляется и не подключается к новой схеме.

## Пользовательский сценарий

API биллинга сохранён из [практики 9](../practise-9/README.md). Зарегистрируйте пользователя:

```bash
curl -X POST http://localhost:8080/api/users \
  -H 'Content-Type: application/json' -d '{"email":"student@example.com"}'
```

Подставьте UUID в следующие запросы:

```bash
curl -X POST http://localhost:8080/api/users/<USER_ID>/top-ups \
  -H 'Content-Type: application/json' -d '{"amount":10000}'
curl -X POST http://localhost:8080/api/users/<USER_ID>/api-keys
```

Получите код привязки по возвращённому API-ключу:

```bash
curl -X POST http://localhost:8080/api/telegram/link-codes \
  -H 'Authorization: Bearer <API_KEY>'
```

В личном чате с ботом отправьте `/start`, `/link <code>`, затем `/balance`. Код действует пять минут; новый код отменяет предыдущий. Полный API-ключ в Telegram не передаётся. Telegram ID и chat ID хранятся как `BIGINT`.

Выполните AI-запрос:

```bash
curl -X POST http://localhost:8080/v1/chat/completions \
  -H 'Authorization: Bearer <API_KEY>' \
  -H 'Idempotency-Key: telegram-demo-1' \
  -H 'Content-Type: application/json' \
  -d '{"model":"qwen2.5:0.5b","prompt":"Что такое outbox?","maxTokens":80}'
```

После списания бот присылает модель, request ID, фактические токены, стоимость и баланс после операции. Повтор того же тела с тем же `Idempotency-Key` не создаёт второго списания или события. `/unlink` удаляет локальную привязку; ожидающие уведомления по прежнему binding ID пропускаются.

[Postman-коллекция](postman/practise-10-res.postman_collection.json) проверяет HTTP-биллинг и не содержит Telegram-токен.

## Внутренний HTTP API

Оба endpoint Gateway требуют `X-Service-Token`, сравниваемый с `INTERNAL_API_TOKEN`. Отсутствующий или неверный секрет возвращает `401`.

| Метод и путь | Назначение |
| --- | --- |
| `POST /internal/telegram/link-redemptions` | Погасить код, вернуть `userId` |
| `GET /internal/users/{userId}/wallet` | Получить баланс активного пользователя |

Тело погашения:

```json
{"updateId":123,"telegramUserId":456,"chatId":456,"code":"одноразовый-код"}
```

Gateway атомарно удаляет код и создаёт квитанцию погашения. Повтор с тем же `updateId`, кодом, Telegram ID и chat ID возвращает исходный результат, даже если первый HTTP-ответ потерян. Подмена тела при том же update ID возвращает `409`. Другое update ID не может использовать уже погашенный код.

Это устраняет окно: Gateway погасил код → Telegram-сервис упал до сохранения привязки. После перезапуска он повторяет HTTP-вызов и завершает свою локальную транзакцию. Общей транзакции между БД нет. Квитанции Gateway автоматически не удаляются.

Telegram-сервис выполняет HTTP **до** локальной транзакции. Затем атомарно фиксирует update ID, привязку и ответ в очереди. Если Gateway временно недоступен, update не помечается обработанным и повторяется позже. Проверка баланса также выполняется через HTTP; перед сохранением ответа проверяется, что привязка не изменилась во время запроса.

Одна локальная связь допускается на пользователя, Telegram ID и чат. Чужая связь не перезаписывается. Если код был погашен, но локальная связь конфликтует с существующей, пользователь получает сообщение об ошибке; после `/unlink` следует выпустить новый код.

## Outbox, inbox и доставка

Gateway сохраняет usage, окончательное списание, журнал и `outbox_events` **в одной транзакции PostgreSQL**. Ошибка Ollama или rollback не оставляют событие `CreditsCharged`. Payload не содержит prompt, ответа модели, API-ключей или Telegram-токена.

Publisher Gateway отправляет событие в `billing.credits-charged.v1` с ключом `userId`. Запись становится `SENT` только после подтверждения Kafka. Telegram-сервис фиксирует inbox и задание на уведомление в своей БД и лишь после commit подтверждает offset (`MANUAL_IMMEDIATE`). Уникальный event ID защищает от второго задания. При отсутствии привязки inbox фиксирует `NO_BINDING`; старые события автоматически после привязки не рассылаются.

Обе очереди забирают по одному заданию через `FOR UPDATE SKIP LOCKED`, освобождая блокировки до сетевого вызова. Lease — 60 секунд, ожидание Kafka и Telegram send — до 10 секунд. Истёкший lease позволяет восстановиться после падения; lock token не позволяет старому worker изменить запись нового.

Повторы очередей: до восьми попыток, задержки 2, 4, 8… секунд, максимум 300. Telegram `429` учитывает `retry_after`, постоянные 4xx, кроме 429, сразу дают `FAILED`. Ошибочные сообщения Kafka после двух повторов публикуются в `billing.credits-charged.dlt`. Offset подтверждается только после успешной записи в DLT. Ошибки БД повторяются без ограничения с интервалом пять секунд.

Границы гарантий:

- Outbox обеспечивает at-least-once публикацию: падение после Kafka ack и до `SENT` может повторить событие с прежним ID.
- Inbox устраняет повторные задания, но Telegram может получить повторное сообщение при падении после внешнего успеха и до сохранения `SENT`.
- Уже начавшийся Telegram HTTP-вызов невозможно отозвать при одновременном `/unlink`.
- Конкурентные publisher не гарантируют порядок операций пользователя; баланс в сообщении — снимок после конкретного списания. Текущий баланс даёт `/balance` через Gateway.
- Остановка одного сервиса не останавливает второй: Gateway продолжает биллинг без бота; Telegram-сервис обрабатывает Kafka без HTTP-доступности Gateway.

## Проверка сбоев и очередей

Посмотреть события Kafka:

```bash
docker compose --env-file modules/practise-10-res/.env \
  -f modules/practise-10-res/docker-compose.yml exec kafka \
  /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka:19092 \
  --topic billing.credits-charged.v1 --from-beginning --property print.key=true
```

Посмотреть outbox и уведомления в **разных БД**:

```bash
docker compose --env-file modules/practise-10-res/.env \
  -f modules/practise-10-res/docker-compose.yml exec gateway-db \
  psql -U ai_gateway -d ai_gateway -c \
  'SELECT id,status,attempts,last_error FROM outbox_events ORDER BY created_at DESC LIMIT 20;'

docker compose --env-file modules/practise-10-res/.env \
  -f modules/practise-10-res/docker-compose.yml exec telegram-db \
  psql -U telegram -d telegram -c \
  'SELECT id,event_id,status,attempts,last_error FROM telegram_notifications ORDER BY created_at DESC LIMIT 20;'
```

Остановите Kafka после старта приложений, выполните AI-запрос с новым idempotency key, затем восстановите broker:

```bash
docker compose --env-file modules/practise-10-res/.env \
  -f modules/practise-10-res/docker-compose.yml stop kafka
# Выполните AI-запрос: списание успешно, outbox ждёт retry.
docker compose --env-file modules/practise-10-res/.env \
  -f modules/practise-10-res/docker-compose.yml start kafka
```

Для выбранного `FAILED` после устранения причины повторите обработку без изменения ID и payload:

```sql
-- В gateway-db:
UPDATE outbox_events SET status='NEW', attempts=0, next_attempt_at=now(), last_error=NULL
WHERE id='<EVENT_ID>' AND status='FAILED';
-- В telegram-db:
UPDATE telegram_notifications SET status='NEW', attempts=0, next_attempt_at=now(), last_error=NULL
WHERE id='<JOB_ID>' AND status='FAILED';
```

Истёкшие `PROCESSING` восстанавливаются автоматически. Inbox для обычного retry не удаляется. DLT автоматически не переигрывается: исправленный payload поддерживаемой версии можно вернуть в основной топик с прежним event ID и ключом пользователя через `kafka-console-producer.sh --property parse.key=true --property 'key.separator=|'`.

## Настройки и разработка

Общие переменные очередей: `DELIVERY_MAX_ATTEMPTS=8`, `DELIVERY_LEASE_SECONDS=60` (минимум 30), `DELIVERY_RETRY_SECONDS=2`, `DELIVERY_MAX_RETRY_SECONDS=300`. Gateway также поддерживает `OUTBOX_POLL_MS=1000`; Telegram-сервис — `KAFKA_CONSUMER_ENABLED=true`, `TELEGRAM_BOT_ENABLED=false`, `TELEGRAM_BOT_TOKEN`, `GATEWAY_BASE_URL` и `TELEGRAM_API_BASE_URL`. Compose задаёт внутренние адреса автоматически.

Локальные адреса БД: Gateway `localhost:5432/ai_gateway`, Telegram `localhost:5433/telegram`; Kafka — `localhost:9092`, внутри Docker — `kafka:19092`.

Сборка обоих приложений и все тесты:

```bash
./gradlew :modules:practise-10-res:test :modules:practise-10-res:bootJar
```

Отдельный сервис:

```bash
./gradlew :modules:practise-10-res:gateway:bootRun
./gradlew :modules:practise-10-res:telegram-service:bootRun
```

Gradle сам не читает `.env`: для `bootRun` передайте соответствующие переменные окружения. Сборке и тестам Telegram-токен не нужен.

Интеграционные тесты запускают два приложения с двумя PostgreSQL и настоящей Kafka. Telegram и Ollama заменяются HTTP-заглушками. Проверяются изоляция БД и endpoint, внутренний секрет, HTTP-биллинг и повтор, rollback JPA/JDBC, межсервисная привязка и её replay, конкурентное погашение, получение баланса через HTTP, Kafka outage, DLT, lease, Telegram retry и окно возможного повторного внешнего сообщения.

Исходные E2E-тесты Gateway с реальной Ollama по умолчанию выключены:

```bash
./gradlew :modules:practise-10-res:gateway:test -De2e.base-url=http://localhost:8080
./gradlew :modules:practise-10-res:gateway:allureReport
```

## Где заменить Telegram-токен

1. В `@BotFather` выполните `/revoke`, выберите своего бота и получите новый токен.
2. Замените `TELEGRAM_BOT_TOKEN` в `modules/practise-10-res/.env`.
3. Пересоздайте только Telegram-сервис:

```bash
docker compose --env-file modules/practise-10-res/.env \
  -f modules/practise-10-res/docker-compose.yml up -d --no-deps --force-recreate telegram-service
```

Остановка стенда с сохранением данных:

```bash
docker compose --env-file modules/practise-10-res/.env \
  -f modules/practise-10-res/docker-compose.yml down
```
