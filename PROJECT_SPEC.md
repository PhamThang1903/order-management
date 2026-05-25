# PROJECT SPEC — Smart Order Notification System
> **Version:** 1.0.0 | **Cập nhật lần cuối:** 2026-05-25
> **Loại tài liệu:** Bất biến — chỉ chỉnh sửa khi có thay đổi kiến trúc lớn

---

## 1. Tổng Quan Dự Án

| Trường | Giá trị |
|---|---|
| **Tên dự án** | `smart-order-notification` |
| **Mục tiêu** | Hệ thống quản lý đơn hàng với thông báo real-time, tích hợp Spring Core + Kafka + Redis |
| **Đối tượng** | Dự án thực hành — luyện tập toàn bộ kiến thức Spring Boot cho Junior Developer |
| **Framework chính** | Spring Boot 3.x (Spring Framework 6.x) |
| **Build tool** | Maven |
| **Java version** | Java 17+ |

### Luồng nghiệp vụ chính

```
[Client] → POST /api/orders
    │
    ▼
[OrderController]  →  @Valid validate request
    │
    ▼
[OrderService]  →  @Transactional
    ├─ 1. Lưu Order vào PostgreSQL
    ├─ 2. Cache Order vào Redis (TTL 10 phút)
    └─ 3. Publish Spring Event: OrderCreatedEvent
              │
              ▼
         [NotificationService] @EventListener
              └─ 4. Gọi KafkaProducer → publish topic "order-events"
                          │
                          ▼
                   [OrderEventConsumer] @KafkaListener
                        ├─ 5a. Ghi recent orders vào Redis ZSet
                        └─ 5b. Async gửi email thông báo
```

---

## 2. Tech Stack

| Thành phần | Công nghệ | Phiên bản | Ghi chú |
|---|---|---|---|
| Framework | Spring Boot | 3.x | Spring Framework 6.x internals |
| Database chính | PostgreSQL | 15 | Production |
| Database test | H2 (in-memory) | latest | Profile `dev` / `test` |
| ORM | Spring Data JPA + Hibernate | Boot-managed | `@Entity`, `@Repository` |
| Message Broker | Apache Kafka | 7.5.0 (Confluent) | 3 partitions, 1 replica |
| Cache | Redis | 7-alpine | TTL 10 phút, Sorted Set, Hash |
| Validation | Jakarta Bean Validation | Boot-managed | Custom `@Constraint` |
| AOP | Spring AOP + AspectJ | Boot-managed | Logging, Performance |
| Async | Spring `@Async` | Boot-managed | ThreadPoolTaskExecutor |
| i18n | Spring `MessageSource` | Boot-managed | vi / en |
| Testing | JUnit 5 + Mockito + MockMvc | Boot-managed | Unit + Integration |
| Container | Docker + Docker Compose | latest | Dev environment |
| Utility | Lombok | latest | Giảm boilerplate |

---

## 3. Kiến Trúc & Cấu Trúc Dự Án

### 3.1 Package Structure

```
smart-order-notification/
├── src/
│   ├── main/
│   │   ├── java/com/example/order/
│   │   │   ├── SmartOrderApplication.java          # Entry point
│   │   │   │
│   │   │   ├── config/                             # [Spring Config Layer]
│   │   │   │   ├── AppConfig.java                  # @Configuration, @Bean, @EnableXxx
│   │   │   │   ├── KafkaConfig.java                # KafkaTemplate, NewTopic, ProducerFactory
│   │   │   │   ├── RedisConfig.java                # RedisTemplate, CacheManager
│   │   │   │   ├── AsyncConfig.java                # @EnableAsync, ThreadPoolTaskExecutor
│   │   │   │   └── ProfileDataSourceConfig.java    # @Profile("dev"/"prod") DataSource
│   │   │   │
│   │   │   ├── domain/                             # [Domain Model]
│   │   │   │   ├── Order.java                      # @Entity, @Table("orders")
│   │   │   │   ├── OrderStatus.java                # Enum: PENDING→DELIVERED/CANCELLED
│   │   │   │   └── User.java                       # @Entity
│   │   │   │
│   │   │   ├── dto/                                # [Request/Response DTOs]
│   │   │   │   ├── CreateOrderRequest.java         # Bean Validation annotations
│   │   │   │   └── OrderResponse.java              # Response mapping
│   │   │   │
│   │   │   ├── repository/                         # [@Repository Layer]
│   │   │   │   ├── OrderRepository.java            # JpaRepository + custom @Query
│   │   │   │   └── UserRepository.java             # JpaRepository
│   │   │   │
│   │   │   ├── service/                            # [@Service Layer]
│   │   │   │   ├── OrderService.java               # Business logic, @Transactional
│   │   │   │   └── NotificationService.java        # @EventListener, @Async
│   │   │   │
│   │   │   ├── controller/                         # [@RestController Layer]
│   │   │   │   └── OrderController.java            # REST endpoints + ExceptionHandler
│   │   │   │
│   │   │   ├── event/                              # [Spring Application Events]
│   │   │   │   ├── OrderCreatedEvent.java          # extends ApplicationEvent
│   │   │   │   └── OrderStatusChangedEvent.java    # extends ApplicationEvent
│   │   │   │
│   │   │   ├── kafka/                              # [Kafka Integration]
│   │   │   │   ├── OrderEventProducer.java         # KafkaTemplate.send()
│   │   │   │   └── OrderEventConsumer.java         # @KafkaListener, concurrency=3
│   │   │   │
│   │   │   ├── cache/                              # [Redis Cache Layer]
│   │   │   │   └── OrderCacheService.java          # RedisTemplate operations, TTL
│   │   │   │
│   │   │   ├── aop/                                # [Aspect-Oriented Programming]
│   │   │   │   ├── LoggingAspect.java              # @Before, @Around, @AfterThrowing
│   │   │   │   └── PerformanceAspect.java          # @Around execution timing
│   │   │   │
│   │   │   └── validation/                         # [Custom Validators]
│   │   │       ├── ValidUserId.java                # Custom @Constraint annotation
│   │   │       └── ValidUserIdValidator.java       # ConstraintValidator<ValidUserId, Long>
│   │   │
│   │   └── resources/
│   │       ├── application.properties              # Base config
│   │       ├── application-dev.properties          # H2 override
│   │       ├── application-prod.properties         # PostgreSQL + tuning
│   │       └── messages/
│   │           ├── messages_vi.properties          # Tiếng Việt
│   │           └── messages_en.properties          # English
│   │
│   └── test/
│       └── java/com/example/order/
│           ├── service/OrderServiceTest.java        # @ExtendWith(MockitoExtension)
│           ├── controller/OrderControllerTest.java  # @WebMvcTest, MockMvc
│           └── integration/OrderIntegrationTest.java # @SpringBootTest
│
├── docker-compose.yml                              # PostgreSQL + Redis + Kafka
└── pom.xml
```

### 3.2 Layer Dependency Rules

```
Controller → Service → Repository
     │            │
     └────────────┴──→ Cache (OrderCacheService)
                  │
                  └──→ Event Publisher → NotificationService → Kafka Producer
                                                                    │
                                                              Kafka Consumer
                                                                    │
                                                              Redis (ZSet, Hash)
```

---

## 4. Chi Tiết Từng Module

### 4.1 Domain Layer

#### `Order.java`
```
@Entity @Table("orders")
Fields:
  - id: Long (@Id, @GeneratedValue IDENTITY)
  - productName: String (@NotNull)
  - quantity: int (@Min 1)
  - totalPrice: BigDecimal (@NotNull)
  - status: OrderStatus (PENDING mặc định)
  - user: User (@ManyToOne LAZY)
  - createdAt: LocalDateTime (@CreationTimestamp)
  - updatedAt: LocalDateTime (@UpdateTimestamp)
```

#### `OrderStatus.java` (Enum)
```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
                                            → CANCELLED
```

#### `User.java`
```
@Entity
Fields:
  - id: Long (@Id, @GeneratedValue)
  - email: String (unique, @NotBlank)
  - name: String
  - orders: List<Order> (@OneToMany mappedBy="user")
```

---

### 4.2 Configuration Layer

#### `AppConfig.java`
- `@Configuration`, `@ComponentScan`, `@PropertySource`
- `@EnableAspectJAutoProxy` — kích hoạt AOP
- `@EnableTransactionManagement` — kích hoạt `@Transactional`
- `@EnableCaching` — kích hoạt `@Cacheable`
- `@Bean MessageSource` — i18n với `ReloadableResourceBundleMessageSource`
- `@Value("${app.order.cache-ttl-seconds:600}")` — inject property

#### `ProfileDataSourceConfig.java`
- `@Profile("dev")` → H2 EmbeddedDatabase (không cần cài PostgreSQL)
- `@Profile("prod")` → HikariCP pool (maxPoolSize=20) kết nối PostgreSQL

#### `AsyncConfig.java`
- `@EnableAsync`, implements `AsyncConfigurer`
- ThreadPoolTaskExecutor: corePool=5, maxPool=20, queue=100
- threadNamePrefix = `async-notification-`

#### `KafkaConfig.java`
- `@Bean NewTopic` → topic `order-events`, 3 partitions, 1 replica
- `@Bean ProducerFactory` → JsonSerializer, acks=all, retries=3
- `@Bean KafkaTemplate`

#### `RedisConfig.java`
- `@Bean RedisTemplate<String, Object>` → key=String, value=JSON
- `@Bean CacheManager` → RedisCacheManager, TTL=10 phút, JSON serialization

---

### 4.3 DTO & Validation Layer

#### `CreateOrderRequest.java`
```
Fields + constraints:
  - productName: @NotBlank @Size(min=2, max=100)
  - quantity: @Min(1) @Max(1000)
  - unitPrice: @NotNull @DecimalMin("0.01")
  - userId: @NotNull @Positive
```

#### Custom Validator: `@ValidUserId`
```
@Constraint(validatedBy = ValidUserIdValidator.class)
Logic: userRepository.existsById(userId)
→ Thực hành: DI trong ConstraintValidator, DB call trong validation
```

---

### 4.4 Repository Layer

#### `OrderRepository.java`
```java
// Derived query methods:
List<Order> findByUserId(Long userId)
List<Order> findByStatus(OrderStatus status)

// JPQL custom query:
@Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status")
List<Order> findByUserIdAndStatus(...)

@Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :from")
long countOrdersSince(LocalDateTime from)
```

---

### 4.5 Service Layer

#### `OrderService.java`
| Method | Transaction | Mô tả |
|---|---|---|
| `createOrder(request)` | `@Transactional` | Tạo order → cache → publish event |
| `updateStatus(id, status)` | `@Transactional` | Update status → evict cache → publish event |
| `getOrder(id)` | `@Transactional(readOnly=true)` | Cache-first: Redis → PostgreSQL fallback |

**Constructor Injection** (bắt buộc — best practice):
- `OrderRepository`
- `UserRepository`
- `OrderCacheService`
- `ApplicationEventPublisher`

#### `NotificationService.java`
| Method | Annotation | Mô tả |
|---|---|---|
| `onOrderCreated(event)` | `@EventListener` | Trigger KafkaProducer.publishOrderCreated() |
| `onStatusChanged(event)` | `@Async @EventListener` | Gửi email/SMS async, dùng MessageSource i18n |

---

### 4.6 Kafka Layer

#### `OrderEventProducer.java`
- Key = `userId` string → đảm bảo cùng user vào cùng partition (ordering)
- Payload JSON: `eventType`, `orderId`, `userId`, `productName`, `totalPrice`, `timestamp`
- `kafkaTemplate.send().thenAccept()` → log success / `.exceptionally()` → log error

#### `OrderEventConsumer.java`
- `@KafkaListener(topics="order-events", concurrency="3")` — 3 threads = 3 partitions
- Switch theo `eventType`:
  - `ORDER_CREATED` → thêm vào Redis Sorted Set `recent:orders`
  - `STATUS_CHANGED` → cập nhật Redis Hash `order:status`
- DLT handler: `@KafkaListener(topics="order-events.DLT")` — Dead Letter Topic

---

### 4.7 Cache Layer

#### `OrderCacheService.java`
| Method | Redis Op | Key pattern | TTL |
|---|---|---|---|
| `cacheOrder(order)` | `opsForValue().set()` | `order:{id}` | 10 phút |
| `getFromCache(id)` | `opsForValue().get()` | `order:{id}` | — |
| `evictOrder(id)` | `delete()` | `order:{id}` | — |
| `incrementTodayOrderCount()` | `opsForValue().increment()` | `order:count:today` | 1 ngày |
| `getTodayOrderCount()` | `opsForValue().get()` | `order:count:today` | — |
| `getRecentOrders(limit)` | `opsForZSet().reverseRange()` | `recent:orders` | — |

---

### 4.8 AOP Layer

#### `LoggingAspect.java`
| Advice | Pointcut | Hành vi |
|---|---|---|
| `@Before` | `execution(* service.*.*(..))` | Log method entry |
| `@Around` | `execution(* service.*.*(..))` | Đo execution time, log ms |
| `@AfterThrowing` | `execution(* service.*.*(..))` | Log exception class + message |

#### Reusable Pointcuts
```java
@Pointcut("execution(* com.example.order.service.*.*(..))")
public void serviceLayer() {}

@Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)")
public void transactionalMethod() {}
```

---

### 4.9 REST API

#### Endpoints

| Method | Path | Body/Param | Response | Mô tả |
|---|---|---|---|---|
| `POST` | `/api/orders` | `CreateOrderRequest` JSON | `201 Order` | Tạo đơn hàng mới |
| `GET` | `/api/orders/{id}` | — | `200 Order` | Lấy đơn (cache-first) |
| `PATCH` | `/api/orders/{id}/status` | `?status=CONFIRMED` | `200 Order` | Cập nhật trạng thái |
| `GET` | `/api/orders/stats/today` | — | `200 Map` | Thống kê hôm nay |

#### Exception Handlers (`@ExceptionHandler`)
- `EntityNotFoundException` → `404 {error: message}`
- `MethodArgumentNotValidException` → `400 {errors: {field: message}}`

---

### 4.10 Testing Strategy

| Test class | Annotation | Scope | Mock |
|---|---|---|---|
| `OrderServiceTest` | `@ExtendWith(MockitoExtension)` | Unit | Mockito `@Mock`, `@InjectMocks` |
| `OrderControllerTest` | `@WebMvcTest(OrderController)` | Web slice | `@MockBean` OrderService |
| `OrderIntegrationTest` | `@SpringBootTest` | Full context | H2 + EmbeddedKafka |

#### Test cases bắt buộc
- `shouldCreateOrderSuccessfully()` — happy path, verify event published, cache called
- `shouldThrowWhenUserNotFound()` — EntityNotFoundException khi userId không tồn tại
- `shouldReturn201WhenCreateOrderValid()` — MockMvc POST thành công
- `shouldReturn400WhenProductNameBlank()` — validation lỗi

---

## 5. Configuration Files

### 5.1 `application.properties` (Base)
```properties
# Database (overridden by profile)
spring.datasource.url=jdbc:postgresql://localhost:5432/order_db
spring.datasource.username=postgres
spring.datasource.password=secret
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=...StringSerializer
spring.kafka.producer.value-serializer=...JsonSerializer
spring.kafka.consumer.group-id=order-notification-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.properties.spring.json.trusted.packages=com.example.order.kafka

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.type=redis
spring.cache.redis.time-to-live=600000

# App
app.order.topic=order-events
app.order.cache-ttl-seconds=600
spring.profiles.active=dev
```

### 5.2 `application-dev.properties`
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
```

### 5.3 `docker-compose.yml` — Infra Services
| Service | Image | Port |
|---|---|---|
| PostgreSQL | `postgres:15` | 5432 |
| Redis | `redis:7-alpine` | 6379 |
| Zookeeper | `confluentinc/cp-zookeeper:7.5.0` | 2181 |
| Kafka | `confluentinc/cp-kafka:7.5.0` | 9092 |

---

## 6. Mapping Kiến Thức → Code

| Kiến thức Spring | File / Nơi áp dụng |
|---|---|
| IoC Container, DI Constructor | `OrderService`, `NotificationService`, tất cả `@Service` |
| `@Service`, `@Repository`, `@Component` | Toàn bộ phân tầng |
| `@Configuration`, `@Bean` | `AppConfig`, `KafkaConfig`, `RedisConfig`, `AsyncConfig` |
| Bean Scope singleton | Mặc định — Producer, Consumer, Service đều singleton |
| `@PostConstruct` / `@PreDestroy` | Có thể thêm vào `OrderCacheService` (warm-up / flush) |
| `@Profile` dev/prod | `ProfileDataSourceConfig` |
| `@Value`, `@PropertySource` | `AppConfig`, `KafkaConfig`, `OrderCacheService` |
| SpEL `#{...}` | Có thể dùng `@Value("#{T(System).currentTimeMillis()}")` |
| AOP `@Aspect`, `@Around` | `LoggingAspect`, `PerformanceAspect` |
| Spring Events `@EventListener` | `NotificationService.onOrderCreated()` |
| `@Async` | `NotificationService.onStatusChanged()` |
| `@Transactional` | `OrderService.createOrder()`, `updateStatus()` |
| Bean Validation, `@Valid` | `CreateOrderRequest`, `OrderController` |
| Custom `@Constraint` | `ValidUserId` + `ValidUserIdValidator` |
| `MessageSource` i18n | `NotificationService` — vi/en |
| `MockMvc`, `@WebMvcTest` | `OrderControllerTest` |
| Mockito `@Mock`, `@InjectMocks` | `OrderServiceTest` |
| `KafkaTemplate` | `OrderEventProducer` |
| `@KafkaListener` | `OrderEventConsumer` |
| `RedisTemplate` | `OrderCacheService` |
| `@Cacheable` / `CacheManager` | `RedisConfig.cacheManager()` |

---

## 7. Hướng Dẫn Chạy Dự Án

```bash
# 1. Khởi động infrastructure
docker-compose up -d

# 2. Chạy với profile dev (H2)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 3. Chạy với profile prod (PostgreSQL)
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# 4. Chạy tests
./mvnw test
```

#### Test API thủ công
```bash
# Tạo đơn hàng
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productName":"Laptop Gaming","quantity":1,"unitPrice":25000000}'

# Lấy đơn (cache-first)
curl http://localhost:8080/api/orders/1

# Cập nhật trạng thái
curl -X PATCH "http://localhost:8080/api/orders/1/status?status=CONFIRMED"

# Thống kê hôm nay
curl http://localhost:8080/api/orders/stats/today
```

---

## 8. Hướng Mở Rộng (Sau Khi Hoàn Thành Core)

| Tính năng | Công nghệ | Mục đích |
|---|---|---|
| Bảo mật endpoint | Spring Security `@PreAuthorize` | Phân quyền admin/user |
| Outbox Pattern | PostgreSQL + Scheduler | Đảm bảo at-least-once Kafka delivery |
| Real-time status | Redis Pub/Sub | Thay polling bằng push |
| Revenue analytics | Kafka Streams | Tổng doanh thu sliding window |
| Monitoring | Actuator + Micrometer | Kafka lag, Redis hit rate |
| Retry logic | Spring Retry `@Retryable` | Tự động retry khi infra lỗi thoáng qua |

---

*Tài liệu này là nguồn sự thật duy nhất cho kiến trúc dự án. Mọi thay đổi lớn phải cập nhật spec trước khi implement.*
