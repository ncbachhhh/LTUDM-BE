# Code Quality Notes

## Current Status

Backend source code has been cleaned and is in a better state than before, but it is not fully optimized yet.

The current codebase is acceptable for continued development because:

- Main backend code compiles successfully.
- Full test suite can run without a local MySQL instance through the H2 test configuration.
- Controller code is cleaner and more consistent.
- Dead DTOs and unused mapper/repository methods were removed.
- Relationship, friendship, and block state logic has been centralized in `RelationshipService`.
- Exception messages and exception handling are more consistent.
- WebSocket authentication and message authorization are easier to follow.
- API and flow documentation has been rewritten from the current code.

## What Has Been Improved

### Config

- Simplified security configuration.
- Removed broken encoding comments.
- Split WebSocket authentication logic into smaller helper methods.
- Improved `UserSecurity.isOwner` by avoiding unsafe direct casting.

### Controller

- Removed broken encoding comments.
- Standardized `ApiResponse` creation.
- Centralized `ApiResponse` success and message factory methods.
- Kept endpoint paths and business behavior unchanged.

### DTO

- Removed unused DTOs:
  - `UserLoginRequest`
  - `UserLoginResponse`
- Removed the empty tracked `UserMapperDefaultsTest` file.
- Removed stale commented fields and noisy comments.

### Exception

- Rewrote `ErrorCode` messages in ASCII English to avoid encoding issues.
- Simplified `GlobalExceptionHandler`.
- Kept enum names and numeric error codes stable.

### Mapper and Repository

- Removed unused methods:
  - `MessageMapper.updateMessage`
  - `MessageMapper.toMessageResponseList`
  - `UserRepository.findByUsername`
  - `ConversationMemberRepository.findByIdConversationIdIn`
  - `ConversationMemberRepository.countMemberInConversation`
  - `ConversationMemberRepository.countByIdConversationId`

### Service

- Added `RelationshipService` as the single place to resolve friendship/block state.
- Reused relationship logic in:
  - `UserService`
  - `FriendshipService`
  - `ConversationService`
  - `MessageService`
- Refactored `AuthenticationService` for clearer token generation, verification, and revocation.
- Removed many redundant or obvious comments.

### Docs

Old docs were deleted and replaced with:

- `README.md`
- `API_REFERENCE.md`
- `BUSINESS_FLOWS.md`
- `WEBSOCKET.md`
- `OPERATIONS.md`

## Remaining Technical Debt

### 1. `ConversationService` Is Still Too Large

`ConversationService` still handles too many responsibilities:

- Creating direct conversations.
- Creating group conversations.
- Adding group members.
- Deleting group conversations.
- Building conversation preview data.
- Building conversation info data.
- Mapping members and latest messages.
- Computing stats.

Recommended next step:

- Extract conversation member management into a dedicated component/service.
- Extract conversation preview building into a separate service.
- Extract conversation info/stat building into a separate service.

### 2. Message Logic Can Be Split Further

`MessageService` still handles:

- Sending messages.
- Resolving text/file/image content.
- Creating attachments.
- Marking read receipts.
- Soft deleting messages.
- Mapping message responses.

Recommended next step:

- Extract attachment creation and file metadata mapping.
- Extract read receipt logic.
- Keep `MessageService` focused on message workflow orchestration.

### 3. Test Coverage Is Still Weak

Compile and the full test suite pass without a local MySQL database because test resources now use an in-memory H2 datasource.

Current verification results:

```bash
./mvnw -q -DskipTests compile
```

Pass.

```bash
./mvnw test
```

Pass.

Recommended next step:

- Add focused unit tests for:
  - `RelationshipService`
  - `AuthenticationService`
  - `FriendshipService`
  - `ConversationService`
  - `MessageService`

### 4. Potential N+1 Query Problems

Conversation preview and member mapping may cause many repository calls when the user has many conversations.

Potentially expensive areas:

- Loading members per conversation.
- Loading users for members.
- Counting unread messages per conversation.
- Loading latest visible message per conversation.
- Resolving presence per member.
- Resolving relationship state per direct conversation.

Recommended next step:

- Profile `GET /conversations/me`.
- Batch load conversation members by conversation ids.
- Batch load users by member ids.
- Consider custom query/projection for conversation preview.

### 5. Response Messages Were Normalized To English

Some response and error messages were changed to ASCII English to remove encoding corruption.

Risk:

- If frontend displays backend messages directly and expects Vietnamese text, UI copy may change.

Recommended next step:

- Frontend should ideally map error codes to localized UI messages.
- Backend should keep stable error codes and avoid relying on response text as a frontend contract.

## Priority Cleanup Plan

1. Add focused unit tests for relationship, friendship, conversation, and message rules.
2. Split `ConversationService`.
3. Split `MessageService`.
4. Optimize `GET /conversations/me` query pattern.
5. Review frontend usage of backend message strings.

## Conclusion

The backend is cleaner and safer to maintain than before, but it is not fully optimized yet.

The highest-impact remaining work is to split `ConversationService`, improve tests, and optimize conversation preview queries.
