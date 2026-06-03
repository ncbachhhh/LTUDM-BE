# Operations

## Runtime Requirements

- Java 21
- Maven wrapper included: `mvnw`, `mvnw.cmd`
- MySQL
- Redis
- SMTP account for password reset email
- Cloudflare R2 or S3-compatible storage

## Environment Variables

Datasource:

```env
MYSQL_URL=jdbc:mysql://localhost:3306/ltudm
MYSQL_USERNAME=root
MYSQL_PASSWORD=password
```

Redis:

```env
REDIS_HOST=localhost
REDIS_PORT=6379
```

JWT:

```env
JWT_SECRET=<long-hs256-secret>
```

Mail:

```env
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

R2:

```env
R2_ENDPOINT=https://<account-id>.r2.cloudflarestorage.com
R2_BUCKET=<bucket>
R2_ACCESS_KEY=<access-key>
R2_SECRET_KEY=<secret-key>
R2_PUBLIC_BASE_URL=https://cdn.example.com
```

CORS:

```env
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
```

## Application Config

Important defaults in `src/main/resources/application.yaml`:

```yaml
server:
  port: 8080
  servlet:
    context-path: /api/v1

jwt:
  access-token-expiration: 360000
  refresh-token-expiration: 6048000
```

Multipart max request/file size is 100MB. Service-level validation is stricter:

- Avatar: 5MB.
- Message image: 10MB.
- Message file: 100MB.

## Build and Verify

Compile:

```bash
./mvnw -q -DskipTests compile
```

Run tests:

```bash
./mvnw test
```

Tests use `src/test/resources/application.yaml` with an in-memory H2 datasource, so a local MySQL instance is not required for the current test suite.

Run app:

```bash
./mvnw spring-boot:run
```

## Storage Behavior

R2 upload object keys:

```text
avatars/{userId}/{uuid}.{ext}
messages/{conversationId}/{senderId}/{uuid}.{ext}
messages/{conversationId}/{senderId}/files/{uuid}.{ext}
```

Public URL:

1. If `r2.public-base-url` has text, URL is `{publicBaseUrl}/{objectKey}`.
2. Otherwise URL is `{endpoint}/{bucket}/{objectKey}`.

## Redis Keys

Password reset:

```text
forgot:otp:{email}
forgot:cooldown:{email}
forgot:attempts:{email}
forgot:reset-token:{sha256(resetToken)}
```

Redis is also required by Spring Data Redis dependency but current custom usage is password reset only.

## Security Notes

- REST security permits `/auth/**`, `/ws`, `/ws/**`, and OPTIONS.
- `/conversations/**`, `/users/**`, `/messages/**`, `/friendships/**` require authenticated JWT.
- Method security is enabled; user update checks owner with `@userSecurity.isOwner`.
- WebSocket auth is enforced at STOMP CONNECT using Bearer JWT.
