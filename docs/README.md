# LTUDM Backend Documentation

Tai lieu nay mo ta backend hien tai sau khi clean code. Tat ca endpoint REST deu co base URL:

```text
http://localhost:8080/api/v1
```

Mac dinh server chay port `8080`, context path `/api/v1`. Cac API ngoai tru `/auth/**` can JWT access token trong header:

```http
Authorization: Bearer <access_token>
```

Response chung:

```json
{
  "code": 200,
  "message": "optional message",
  "data": {}
}
```

Khi loi, `code` trong body la ma business error tu `ErrorCode`, HTTP status duoc map theo code: `400`, `401`, `403`, `404`, hoac `500`.

## Tai lieu

- [API Reference](./API_REFERENCE.md): danh sach API REST, request, response, validation va behavior.
- [Business Flows](./BUSINESS_FLOWS.md): luong xu ly auth, user, friendship, conversation, message, file upload, password reset.
- [WebSocket](./WEBSOCKET.md): ket noi STOMP, auth, destination, realtime events.
- [Operations](./OPERATIONS.md): cau hinh moi truong, Redis, MySQL, R2, mail, build va run.

## Kien truc package

```text
config       Spring Security, CORS, R2, WebSocket STOMP, WebSocket JWT auth
controller   REST API layer
dto          Request/response objects
entity       JPA entities and composite keys
exception    ErrorCode, AppException, global exception mapping
mapper       MapStruct mapper cho User va Message
repository   Spring Data JPA repositories
service      Business logic
websocket    STOMP message controllers and presence events
```

## Luong request co ban

1. Client goi `/auth/login` de lay access token va refresh token.
2. Client gui REST request co header `Authorization: Bearer <access_token>`.
3. Spring Security verify JWT bang `jwt.secret`.
4. Controller nhan request DTO va goi service.
5. Service kiem tra quyen, validate business rule, thao tac repository/storage/cache.
6. Controller tra `ApiResponse<T>`.
7. Neu co loi, service nem `AppException(ErrorCode.X)`, `GlobalExceptionHandler` tra HTTP status va body loi thong nhat.

## Luong realtime co ban

1. Client ket noi SockJS/STOMP den `/api/v1/ws`.
2. STOMP `CONNECT` gui native header `Authorization: Bearer <access_token>`.
3. `WebSocketAuthInterceptor` verify JWT, set principal la `userId`, mark online.
4. Client subscribe `/topic/conversation/{conversationId}` de nhan message realtime.
5. Client subscribe `/user/queue/conversations` de nhan conversation preview rieng theo user.
6. Client subscribe `/topic/presence` de nhan online/offline events.
