# PROGRESS — Smart Order Notification System
> **Version:** 1.0.0 | **Cập nhật lần cuối:** 2026-05-25
> **Quy tắc:** Agent/developer PHẢI đọc file này trước khi bắt đầu làm việc. Sau khi hoàn thành task, cập nhật ngay trạng thái tương ứng.

---

## Trạng Thái Tổng Quan

| Hạng mục | Tiến độ | Ghi chú |
|---|---|---|
| **Tổng số task** | 12 phases / 42 tasks | — |
| **Hoàn thành** | 0 / 42 | Chưa bắt đầu |
| **Đang làm** | — | — |
| **Blocked** | — | — |
| **Phiên làm việc** | 0 | — |

---

## Ký Hiệu Trạng Thái

| Ký hiệu | Ý nghĩa |
|---|---|
| `[ ]` | Chưa làm |
| `[~]` | Đang làm (ghi rõ ai/session nào) |
| `[x]` | Hoàn thành |
| `[!]` | Blocked — cần unblock trước |
| `[-]` | Bỏ qua / Không áp dụng |

---

## Phase 1 — Khởi Tạo Dự Án & Maven Setup
> **Kiến thức luyện:** Spring Initializr, Maven, Project structure
> **Ước tính:** 30 phút

| ID | Task | Trạng thái | Ghi chú |
|---|---|---|---|
| P1-01 | Tạo project Spring Boot 3.x qua Spring Initializr | `[ ]` | Chọn Maven, Java 17, Jar |
| P1-02 | Cấu hình `pom.xml` — thêm đủ dependencies | `[ ]` | web, jpa, validation, aop, kafka, redis, postgresql, h2, lombok, test |
| P1-03 | Tạo đầy đủ cấu trúc package theo spec | `[ ]` | config, domain, dto, repository, service, controller, event, kafka, cache, aop, validation |
| P1-04 | Tạo `SmartOrderApplication.java` entry point | `[ ]` | `@SpringBootApplication` |
| P1-05 | Tạo `application.properties` base config | `[ ]` | DB, Kafka, Redis, app properties |
| P1-06 | Tạo `application-dev.properties` (H2 override) | `[ ]` | H2 in-memory |
| P1-07 | Tạo `application-prod.properties` (PostgreSQL) | `[ ]` | URL, pool config |
| P1-08 | Tạo `docker-compose.yml` (PostgreSQL + Redis + Kafka + Zookeeper) | `[ ]` | Xem spec section 5.3 |

**Điều kiện hoàn thành Phase 1:** `./mvnw compile` chạy thành công không lỗi.

---

## Phase 2 — Domain Layer
> **Kiến thức luyện:** `@Entity`, `@Table`, `@ManyToOne`, Bean Lifecycle, Enum
> **Ước tính:** 45 phút

| ID | Task | Trạng thái | Ghi chú |
|---|---|---|---|
| P2-01 | Tạo `OrderStatus.java` enum | `[ ]` | PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED |
| P2-02 | Tạo `User.java` entity | `[ ]` | `@Entity`, id, email (unique), name, `@OneToMany orders` |
| P2-03 | Tạo `Order.java` entity | `[ ]` | `@Entity @Table("orders")`, `@ManyToOne LAZY user`, `@CreationTimestamp`, `@UpdateTimestamp` |
| P2-04 | Tạo `CreateOrderRequest.java` DTO | `[ ]` | `@NotBlank`, `@Min`, `@Max`, `@DecimalMin`, `@Positive` |
| P2-05 | Tạo `OrderResponse.java` DTO | `[ ]` | Mapping từ Order entity |

**Điều kiện hoàn thành Phase 2:** JPA có thể tạo bảng `orders` và `users` khi khởi động với H2.

---

## Phase 3 — Configuration Layer
> **Kiến thức luyện:** `@Configuration`, `@Bean`, `@Profile`, `@EnableXxx`, `@Value`
> **Ước tính:** 60 phút

| ID | Task | Trạng thái | Ghi chú |
|---|---|---|---|
| P3-01 | Tạo `AppConfig.java` | `[ ]` | `@EnableAspectJAutoProxy`, `@EnableTransactionManagement`, `@EnableCaching`, `@Bean MessageSource` |
| P3-02 | Tạo `ProfileDataSourceConfig.java` | `[ ]` | `@Profile("dev")` H2, `@Profile("prod")` HikariCP maxPool=20 |
| P3-03 | Tạo `AsyncConfig.java` | `[ ]` | `@EnableAsync`, `ThreadPoolTaskExecutor` core=5 max=20 queue=100 |
| P3-04 | Tạo `KafkaConfig.java` | `[ ]` | `NewTopic` 3 partitions, `ProducerFactory` acks=all retries=3, `KafkaTemplate` |
| P3-05 | Tạo `RedisConfig.java` | `[ ]` | `RedisTemplate` JSON serializer, `CacheManager` TTL=10min |
| P3-06 | Tạo `messages_vi.properties` | `[ ]` | key `order.status.changed`, `order.productName.required` |
| P3-07 | Tạo `messages_en.properties` | `[ ]` | English equivalents |

**Điều kiện hoàn thành Phase 3:** Application context load thành công với tất cả beans.

---

## Phase 4 — Repository Layer
> **Kiến thức luyện:** `@Repository`, Spring Data JPA, `@Query`, JPQL
> **Ước tính:** 30 phút

| ID | Task | Trạng thái | Ghi chú |
|---|---|---|---|
| P4-01 | Tạo `UserRepository.java` | `[ ]` | `JpaRepository<User, Long>`, `existsById()` |
| P4-02 | Tạo `OrderRepository.java` | `[ ]` | Derived: `findByUserId`, `findByStatus`; JPQL: `findByUserIdAndStatus`, `countOrdersSince` |

**Điều kiện hoàn thành Phase 4:** Repository methods có thể gọi được (test bằng CommandLineRunner tạm).

---

## Phase 5 — Service Layer
> **Kiến thức luyện:** DI Constructor Injection, `@Transactional`, `@Service`, `ApplicationEventPublisher`
> **Ước tính:** 60 phút

| ID | Task | Trạng thái | Ghi chú |
|---|---|---|---|
| P5-01 | Tạo `OrderService.java` — skeleton + constructor DI | `[ ]` | Inject: OrderRepo, UserRepo, OrderCacheService, EventPublisher |
| P5-02 | Implement `createOrder()` `@Transactional` | `[ ]` | Validate user → build Order → save → cache → publishEvent |
| P5-03 | Implement `updateStatus()` `@Transactional` | `[ ]` | Find → update status → cache → publishEvent(oldStatus, newStatus) |
| P5-04 | Implement `getOrder()` `@Transactional(readOnly=true)` | `[ ]` | Cache-first: Redis → PostgreSQL fallback |
| P5-05 | Tạo `OrderCreatedEvent.java` | `[ ]` | `extends ApplicationEvent`, giữ `Order` |
| P5-06 | Tạo `OrderStatusChangedEvent.java` | `[ ]` | `extends ApplicationEvent`, giữ `order`, `oldStatus`, `newStatus` |

**Điều kiện hoàn thành Phase 5:** `OrderService.createOrder()` lưu được vào DB (test thủ công).

---

## Phase 6 — AOP Layer
> **Kiến thức luyện:** `@Aspect`, `@Pointcut`, `@Before`, `@Around`, `@AfterThrowing`
> **Ước tính:** 45 phút

| ID | Task | Trạng thái | Ghi chú |
|---|---|---|---|
| P6-01 | Tạo `LoggingAspect.java` | `[ ]` | Pointcut service layer, `@Before` log entry, `@Around` measure time, `@AfterThrowing` log exception |
| P6-02 | Tạo `PerformanceAspect.java` | `[ ]` | `@Around` riêng cho performance timing, log nếu > threshold (ví dụ 500ms) |
| P6-03 | Verify AOP hoạt động | `[ ]` | Gọi một service method, xem log output có hiện entry + time không |

**Điều kiện hoàn thành Phase 6:** Mỗi service call in ra log `→ Entering:` và `✓ completed in Xms`.

---

## Phase 7 — Spring Events & NotificationService
> **Kiến thức luyện:** Observer Pattern, `@EventListener`, `@Async`, `MessageSource`
> **Ước tính:** 45 phút

| ID | Task | Trạng thái | Ghi chú |
|---|---|---|---|
| P7-01 | Tạo `NotificationService.java` — constructor DI | `[ ]` | Inject: OrderEventProducer (stub), MessageSource |
| P7-02 | Implement `onOrderCreated()` `@EventListener` | `[ ]` | Nhận `OrderCreatedEvent` → gọi kafkaProducer (có thể stub trước) |
| P7-03 | Implement `onStatusChanged()` `@Async @EventListener` | `[ ]` | Dùng `MessageSource` lấy message → `simulateSendEmail()` |
| P7-04 | Verify async: log thread name phải là `async-notification-X` | `[ ]` | Không phải main thread |

**Điều kiện hoàn thành Phase 7:** Event flow hoạt động: `createOrder()` → event → `onOrderCreated()` log thấy.

---

## Phase 8 — Kafka Integration
> **Kiến thức luyện:** `KafkaTemplate`, `@KafkaListener`, Consumer group, DLT, concurrency
> **Ước tính:** 60 phút

| ID | Task | Trạng thái | Ghi chú |
|---|---|---|---|
| P8-01 | Khởi động Kafka qua `docker-compose up -d` | `[ ]` | Verify `kafka:9092` accessible |
| P8-02 | Tạo `OrderEventProducer.java` | `[ ]` | Key = userId (ordering guarantee), payload JSON, log success/error callback |
| P8-03 | Connect `NotificationService.onOrderCreated()` → real producer | `[ ]` | Thay stub bằng `kafkaProducer.publishOrderCreated()` |
| P8-04 | Implement `publishStatusChanged()` trong producer | `[ ]` | eventType=STATUS_CHANGED, oldStatus/newStatus |
| P8-05 | Tạo `OrderEventConsumer.java` | `[ ]` | `@KafkaListener` concurrency=3, switch eventType |
| P8-06 | Implement `handleOrderCreated()` → Redis ZSet | `[ ]` | `opsForZSet().add("recent:orders", orderId, timestamp)` |
| P8-07 | Implement `handleStatusChanged()` → Redis Hash | `[ ]` | `opsForHash().put("order:status", orderId, newStatus)` |
| P8-08 | Implement DLT handler | `[ ]` | `@KafkaListener(topics="order-events.DLT")` log error |

**Điều kiện hoàn thành Phase 8:** Gửi một event, consumer nhận được và log `Kafka consumed:`.

---

## Phase 9 — Redis Cache Layer
> **Kiến thức luyện:** `RedisTemplate`, `opsForValue`, `opsForZSet`, `opsForHash`, TTL
> **Ước tính:** 45 phút

| ID | Task | Trạng thái | Ghi chú |
|---|---|---|---|
| P9-01 | Khởi động Redis qua `docker-compose up -d` | `[ ]` | Verify `redis:6379` accessible |
| P9-02 | Tạo `OrderCacheService.java` — constructor DI `RedisTemplate` | `[ ]` | |
| P9-03 | Implement `cacheOrder()` với TTL | `[ ]` | Key = `order:{id}`, `opsForValue().set(key, order, Duration)` |
| P9-04 | Implement `getFromCache()` | `[ ]` | Cache HIT/MISS log, return `Optional<Order>` |
| P9-05 | Implement `evictOrder()` | `[ ]` | `redisTemplate.delete(key)` |
| P9-06 | Implement `incrementTodayOrderCount()` | `[ ]` | `opsForValue().increment()` + `expire()` 1 ngày |
| P9-07 | Implement `getTodayOrderCount()` | `[ ]` | `opsForValue().get()`, null → 0L |
| P9-08 | Implement `getRecentOrders(limit)` | `[ ]` | `opsForZSet().reverseRange()` |

**Điều kiện hoàn thành Phase 9:** Gọi `getOrder()` lần 2 thấy log `Cache HIT`.

---

## Phase 10 — REST Controller
> **Kiến thức luyện:** `@RestController`, `@Valid`, `ResponseEntity`, `@ExceptionHandler`
> **Ước tính:** 45 phút

| ID | Task | Trạng thái | Ghi chú |
|---|---|---|---|
| P10-01 | Tạo `OrderController.java` — constructor DI | `[ ]` | Inject: OrderService, OrderCacheService |
| P10-02 | Implement `POST /api/orders` | `[ ]` | `@Valid @RequestBody`, trả `201 CREATED` |
| P10-03 | Implement `GET /api/orders/{id}` | `[ ]` | Cache-first qua `OrderService.getOrder()` |
| P10-04 | Implement `PATCH /api/orders/{id}/status` | `[ ]` | `@RequestParam OrderStatus status` |
| P10-05 | Implement `GET /api/orders/stats/today` | `[ ]` | Lấy từ Redis: count + recentOrders(10) |
| P10-06 | Implement `@ExceptionHandler EntityNotFoundException` | `[ ]` | `404 {error: message}` |
| P10-07 | Implement `@ExceptionHandler MethodArgumentNotValidException` | `[ ]` | `400 {errors: {field: message}}` |

**Điều kiện hoàn thành Phase 10:** Tất cả 4 curl commands trong spec chạy thành công.

---

## Phase 11 — Testing
> **Kiến thức luyện:** `@ExtendWith(MockitoExtension)`, `@WebMvcTest`, `MockMvc`, `@SpringBootTest`
> **Ước tính:** 90 phút

| ID | Task | Trạng thái | Ghi chú |
|---|---|---|---|
| P11-01 | Tạo `OrderServiceTest.java` | `[ ]` | `@Mock` OrderRepo, UserRepo, CacheService, EventPublisher |
| P11-02 | Viết `shouldCreateOrderSuccessfully()` | `[ ]` | given/when/then, verify `publishEvent()` + `cacheOrder()` |
| P11-03 | Viết `shouldThrowWhenUserNotFound()` | `[ ]` | `assertThatThrownBy().isInstanceOf(EntityNotFoundException)` |
| P11-04 | Tạo `OrderControllerTest.java` | `[ ]` | `@WebMvcTest`, `@MockBean` services |
| P11-05 | Viết `shouldReturn201WhenCreateOrderValid()` | `[ ]` | MockMvc POST, `andExpect(status().isCreated())` |
| P11-06 | Viết `shouldReturn400WhenProductNameBlank()` | `[ ]` | `andExpect(jsonPath("$.errors.productName").exists())` |
| P11-07 | Tạo `OrderIntegrationTest.java` | `[ ]` | `@SpringBootTest`, H2, EmbeddedKafka, kiểm tra full flow |
| P11-08 | Chạy `./mvnw test` — tất cả PASS | `[ ]` | Không có test FAIL |

**Điều kiện hoàn thành Phase 11:** `BUILD SUCCESS` với 0 failures, 0 errors.

---

## Phase 12 — i18n & Custom Validator
> **Kiến thức luyện:** `MessageSource`, `@Constraint`, `ConstraintValidator`, DI trong Validator
> **Ước tính:** 45 phút

| ID | Task | Trạng thái | Ghi chú |
|---|---|---|---|
| P12-01 | Điền nội dung `messages_vi.properties` | `[ ]` | `order.status.changed=Đơn hàng {0} đã chuyển sang {1}`, `order.productName.required=Tên sản phẩm không được trống` |
| P12-02 | Điền nội dung `messages_en.properties` | `[ ]` | English versions |
| P12-03 | Tạo `@ValidUserId` annotation | `[ ]` | `@Constraint(validatedBy=ValidUserIdValidator.class)`, message default |
| P12-04 | Tạo `ValidUserIdValidator.java` | `[ ]` | Implements `ConstraintValidator<ValidUserId, Long>`, DI `UserRepository` |
| P12-05 | Áp dụng `@ValidUserId` vào `CreateOrderRequest.userId` | `[ ]` | Test với userId không tồn tại → `400` |
| P12-06 | Verify i18n trong `NotificationService` | `[ ]` | Log message hiển thị tiếng Việt đúng |

**Điều kiện hoàn thành Phase 12:** Gửi request với userId không tồn tại nhận được validation error tiếng Việt.

---

## Nhật Ký Phiên Làm Việc

| Phiên | Ngày | Tasks hoàn thành | Vấn đề gặp | Ghi chú |
|---|---|---|---|---|
| #1 | — | — | — | Chưa bắt đầu |

---

## Vấn Đề & Quyết Định

| ID | Ngày | Vấn đề | Quyết định | Lý do |
|---|---|---|---|---|
| — | — | — | — | — |

---

## Checklist Hoàn Thành Dự Án

- [ ] Tất cả 42 tasks đánh dấu `[x]`
- [ ] `./mvnw test` → `BUILD SUCCESS`, 0 failures
- [ ] `docker-compose up -d` → tất cả services healthy
- [ ] Tất cả 4 curl API commands chạy thành công
- [ ] Log hiển thị: AOP timing, Kafka produced/consumed, Redis HIT/MISS
- [ ] i18n hoạt động: message tiếng Việt khi `Locale=vi`
- [ ] Custom validator `@ValidUserId` reject userId không tồn tại

---

## Hướng Dẫn Cho Agent / Developer

Trước khi bắt đầu bất kỳ phiên làm việc nào:

1. **Đọc file này** — xác định task nào đang `[ ]` hoặc `[~]`
2. **Thông báo task** — "Tôi sẽ làm P5-02: Implement createOrder()"
3. **Implement** — theo đúng spec trong `PROJECT_SPEC.md`
4. **Cập nhật ngay** — đổi `[ ]` → `[x]` sau khi xong
5. **Ghi nhật ký** — thêm vào bảng "Nhật Ký Phiên Làm Việc"
6. **Nếu blocked** — đổi sang `[!]` và ghi rõ lý do vào bảng "Vấn Đề & Quyết Định"

**Không được implement task mà không cập nhật PROGRESS.md.**
