# Spring & Spring Boot Comprehensive Architecture & Interview Guide

Tài liệu ôn tập chuyên sâu và toàn diện về **Spring Framework**, **Spring Boot**, **Spring Data JPA**, **Spring Security**, **Kafka Integration**, và **Hệ sinh thái Microservices**. Tài liệu được biên soạn phục vụ công việc thực tế và phỏng vấn kỹ thuật từ cấp độ Mid đến Senior / Lead Software Engineer.

> 💡 **Tài liệu liên quan**:
> - [`Java.md`](Java.md): Kiến thức nền tảng Java Core, JVM Internals, Concurrency, Garbage Collection, Collections, Design Patterns.
> - [`Database.md`](Database.md): Chi tiết về Database Storage Engine, B+Tree, MVCC, Indexing, Transaction Isolation, Sharding.
> - [`SystemDesign.md`](SystemDesign.md): Kiến trúc hệ thống phân tán, Caching, Event-driven, Load balancing, Resiliency.
> - [`Networking.md`](Networking.md): TCP/IP, DNS, HTTP, TLS, timeout & lỗi kết nối, troubleshooting mạng.

---

## 📖 Cách đọc tài liệu này

Mỗi chương được viết theo mạch **Bài toán → Cách giải → Áp dụng**:

| Ký hiệu | Ý nghĩa |
| :--- | :--- |
| 🎯 **Bài toán** | Vấn đề thực tế cần giải quyết — vì sao kỹ thuật này tồn tại |
| ❌ **Cách ngây thơ** | Cách làm "tự nhiên nhất" và lý do nó không đủ — giúp hiểu *vì sao* cần giải pháp phức tạp hơn |
| 💡 **Ý tưởng giải** | Nguyên lý cốt lõi của giải pháp (phần chi tiết cơ chế nằm ngay bên dưới) |
| ⚖️ **Các phương án** | Khi có nhiều cách: bảng ưu/nhược điểm |
| ✅ **Vì sao chọn** | Lý do hệ thống thực tế chọn phương án đó, trong điều kiện nào |
| 🛠 **Áp dụng** | Dùng kiến thức này vào công việc / thiết kế / debug thế nào |
| 🧠 **Tự kiểm tra** | Câu hỏi "tại sao" để tự đánh giá đã hiểu bản chất chưa (bấm để xem đáp án) |


### 🗺 Chuỗi bài toán của Spring & Spring Boot

```
 Ứng dụng doanh nghiệp: hàng trăm class phụ thuộc nhau, mỗi class tự `new` dependency → không test, không thay được
   └─► IoC container + Dependency Injection (1, 2)
         ├─ hai bean cần nhau                       → circular dependency, three-level cache (3)
         └─ transaction/log/security lặp ở mọi method → AOP proxy (4)
               └─ proxy chỉ chặn lời gọi TỪ BÊN NGOÀI → bẫy self-invocation (4.3, 8.3)
 Cấu hình hàng trăm bean hạ tầng (DataSource, Jackson, Tomcat...) bằng tay quá mệt
   └─► Spring Boot auto-configuration + @Conditional + starter + fat JAR (5)
 Nhận HTTP request → MVC, DispatcherServlet, Tomcat thread pool (6) → validation, REST (15)
 Làm việc với DB bằng object  → JPA/Hibernate: persistence context, dirty checking, N+1 (7)
 Nhiều thao tác DB phải cùng thành/bại → @Transactional, propagation (8)
 Chặn truy cập trái phép      → Security filter chain, JWT (9)
 Giao tiếp bất đồng bộ giữa service → Kafka (10); việc chạy nền → @Async, scheduling, virtual threads (11)
 Gọi service khác qua mạng    → HTTP client + timeout + circuit breaker (16); giảm tải DB → cache (17)
 Vận hành production          → observability (12), hiệu năng (13), cấu hình (14), testing (19), cloud (20)
```

## Mục lục

0. [✅ Checklist trọng tâm Spring Boot](#-checklist-trọng-tâm-spring-boot-đọc-theo-thứ-tự-ưu-tiên)
1. [Kiến trúc Tổng quan Spring Framework & Spring Boot](#1-kiến-trúc-tổng-quan-spring-framework--spring-boot) (gồm Spring Boot 4 / Framework 7)
2. [IoC Container & Bean Lifecycle Internals](#2-ioc-container--bean-lifecycle-internals)
3. [Circular Dependency & Three-Level Cache](#3-circular-dependency--three-level-cache)
4. [Spring AOP & Proxy Mechanisms](#4-spring-aop--proxy-mechanisms)
5. [Spring Boot Auto-Configuration Internals](#5-spring-boot-auto-configuration-internals)
6. [Spring MVC Architecture & Request Lifecycle](#6-spring-mvc-architecture--request-lifecycle)
7. [Spring Data JPA & Hibernate Internals](#7-spring-data-jpa--hibernate-internals)
8. [Quản lý Giao dịch (@Transactional Deep Dive)](#8-quản-lý-giao-dịch-transactional-deep-dive)
9. [Spring Security Architecture & JWT Stateless Flow](#9-spring-security-architecture--jwt-stateless-flow)
10. [Spring Kafka: Cấu hình, Producer, Consumer & Manual Partition Assignment](#10-spring-kafka-cấu-hình-producer-consumer--manual-partition-assignment)
11. [Xử lý Bất đồng bộ, Scheduling & Virtual Threads](#11-xử-lý-bất-đồng-bộ-scheduling--virtual-threads-java-21)
12. [Production Observability: Actuator, Metrics & Tracing](#12-production-observability-spring-boot-actuator-metrics--tracing)
13. [Tối ưu Hiệu năng & Khắc phục Sự cố Production](#13-tối-ưu-hiệu-năng--khắc-phục-sự-cố-production)
14. [Externalized Configuration & Profiles](#14-externalized-configuration--profiles)
15. [Validation & thiết kế REST API](#15-validation--thiết-kế-rest-api)
16. [HTTP Clients & Resilience](#16-http-clients--resilience)
17. [Caching với Spring Cache & Redis](#17-caching-với-spring-cache--redis)
18. [Spring WebFlux & Reactive](#18-spring-webflux--reactive-tóm-tắt)
19. [Testing trong Spring Boot](#19-testing-trong-spring-boot)
20. [Spring Cloud & Microservices](#20-spring-cloud--microservices-tổng-quan)
21. [Tổng hợp Câu hỏi Phỏng vấn Spring Boot (Senior / Lead)](#21-tổng-hợp-câu-hỏi-phỏng-vấn-spring-boot-thường-gặp-senior--lead)

---

## ✅ Checklist trọng tâm Spring Boot (đọc theo thứ tự ưu tiên)

| Mức | Chủ đề | Phải giải thích được | Mục |
| :--- | :--- | :--- | :--- |
| ⭐⭐⭐ | IoC / DI | IoC là gì, 3 cách inject & vì sao chọn constructor injection, `@Primary`/`@Qualifier` | 1.1, 21-Q7, Q8 |
| ⭐⭐⭐ | Bean lifecycle & scope | Các bước tạo bean, BPP tạo proxy ở đâu, singleton có thread-safe không, prototype trong singleton | 2.2, 2.3, 2.4 |
| ⭐⭐⭐ | AOP & Proxy | JDK proxy vs CGLIB, **self-invocation** làm mất `@Transactional/@Async/@Cacheable` | 4 |
| ⭐⭐⭐ | Auto-configuration | `@SpringBootApplication`, file `AutoConfiguration.imports`, `@Conditional...`, cách override & debug | 5 |
| ⭐⭐⭐ | `@Transactional` | Propagation, rollback rules, 10 bẫy (private method, checked exception, rollback-only, gọi API trong transaction) | 8 |
| ⭐⭐⭐ | JPA/Hibernate | Persistence context & dirty checking, **N+1**, lazy loading/OSIV, optimistic vs pessimistic lock, batch insert | 7 |
| ⭐⭐⭐ | Request lifecycle | Filter → DispatcherServlet → HandlerMapping → Interceptor → Controller → MessageConverter; xử lý exception tập trung | 6 |
| ⭐⭐⭐ | Thread model | Tomcat 200 thread, HikariCP pool, `@Async` executor, virtual threads — nút thắt nằm ở đâu | 6.4, 11, 13.1 |
| ⭐⭐ | Security | Filter chain, authentication flow, JWT vs session, resource server, CSRF/CORS | 9 |
| ⭐⭐ | Configuration | Thứ tự ưu tiên property, profiles, `@ConfigurationProperties`, secrets | 14 |
| ⭐⭐ | Messaging (Kafka) | Partition & key, consumer group, commit offset, retry/DLT, idempotent consumer | 10 |
| ⭐⭐ | Resilience | Timeout, retry, circuit breaker, bulkhead | 16 |
| ⭐⭐ | Observability & Production | Actuator, liveness vs readiness, metrics/tracing, graceful shutdown, xử lý CPU 100%/chậm | 12, 13 |
| ⭐⭐ | Testing | Test slices, Testcontainers, `@MockitoBean`, context caching | 19 |
| ⭐ | Packaging & startup | Fat jar & `JarLauncher`, layered jar, CDS/AOT/native image | 5.4, 13.4 |
| ⭐ | Caching / WebFlux / Spring Cloud | `@Cacheable` + Redis, khi nào dùng WebFlux, gateway/discovery/config | 17, 18, 20 |
| ⭐ | Phiên bản | Thay đổi Boot 3 (Jakarta, Java 17) và Boot 4 (Jackson 3, modular starters, API versioning) | 1.3, 1.4 |

> Nếu chỉ có 1 buổi ôn: tập trung toàn bộ mục ⭐⭐⭐ và tự trả lời được các câu ở mục 21 không cần nhìn tài liệu.

---

## 1. Kiến trúc Tổng quan Spring Framework & Spring Boot

> 🎯 **Bài toán:** `OrderService` tự `new MySqlOrderRepository()`, `new SmtpEmailSender()`... → muốn test phải có DB và mail server thật; muốn đổi cài đặt phải sửa code ở mọi nơi; mỗi object tự quản lý vòng đời (pool, cấu hình).
>
> ❌ **Cách cũ:** EJB thời đầu — nặng nề, bắt buộc kế thừa API của container, chỉ chạy trong application server.
>
> 💡 **Ý tưởng giải:** **Đảo ngược quyền điều khiển (IoC):** class chỉ **khai báo cần gì** (tham số constructor), một **container** tạo object và **tiêm** dependency vào. Class vẫn là POJO bình thường. Những việc lặp lại quanh business logic (transaction, security) được thêm vào bằng **proxy** (AOP) thay vì viết tay. **Spring Boot** giải bài toán tiếp theo: cấu hình hàng trăm bean hạ tầng — tự cấu hình theo những gì có trên classpath.


### 1.1. Triết lý Thiết kế: Inversion of Control & Loose Coupling

Spring Framework ra đời nhằm giải quyết vấn đề phụ thuộc chặt chẽ (tight coupling) và sự phức tạp của kiến trúc EJB (Enterprise JavaBeans) thời kỳ đầu.
- **Inversion of Control (IoC)**: Thay vì class tự khởi tạo các phụ thuộc (`new Service()`), quyền kiểm soát khởi tạo và quản lý vòng đời object được chuyển giao hoàn toàn cho **IoC Container**.
- **Dependency Injection (DI)**: Là cơ chế hiện thực hóa IoC, nơi container "tiêm" các dependencies vào bean thông qua Constructor, Setter hoặc Field.
- **Aspect-Oriented Programming (AOP)**: Tách biệt các mối quan tâm xuyên suốt (Cross-cutting concerns) như Transaction, Logging, Security, Metric khỏi Business Logic chính.

### 1.2. Spring Framework vs Spring Boot

| Đặc điểm | Spring Framework | Spring Boot |
| :--- | :--- | :--- |
| **Mục đích** | Cung cấp enterprise framework toàn diện, linh hoạt. | Framework giúp phát triển ứng dụng Spring nhanh chóng (RAD - Rapid Application Development). |
| **Cấu hình** | Cần cấu hình thủ công nhiều file XML hoặc class `@Configuration` phức tạp (web.xml, DispatcherServlet, DataSource...). | **Convention over Configuration**: Cung cấp cấu hình mặc định thông minh, tự động cấu hình dựa trên classpath. |
| **Server Deployment** | Yêu cầu đóng gói thành file `.war` và deploy thủ công lên Standalone Web Server ngoài (Tomcat, JBoss, WebLogic). | **Embedded Server** tích hợp sẵn (Tomcat, Jetty, Undertow) đóng gói thành file `.jar` độc lập có thể chạy trực tiếp bằng `java -jar`. |
| **Quản lý Dependency** | Tự quản lý từng dependency riêng lẻ, dễ xung đột phiên bản giữa các thư viện liên quan. | Cung cấp **Starters** (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`...) quản lý gom nhóm và đồng bộ tương thích version (BOM). |
| **Production-ready** | Không có công cụ theo dõi giám sát tích hợp sẵn. | Tích hợp sẵn **Spring Boot Actuator** (Health check, Metrics, Thread Dump, Auditing). |

### 1.3. Những thay đổi cốt lõi trên Spring Boot 3.x & Java 17/21 Baseline

1. **Java 17/21 Baseline**: Spring Boot 3 yêu cầu tối thiểu Java 17 và tối ưu mạnh mẽ trên Java 21 (Records, Pattern Matching, Sealed Classes, Sequenced Collections).
2. **Chuyển dịch sang Jakarta EE 9/10**:
   - Toàn bộ package `javax.*` chuyển sang `jakarta.*` (ví dụ: `javax.persistence.*` → `jakarta.persistence.*`, `javax.servlet.*` → `jakarta.servlet.*`, `javax.validation.*` → `jakarta.validation.*`).
3. **Ahead-of-Time (AOT) & GraalVM Native Image**:
   - Biên dịch ứng dụng Java thành mã máy nhị phân thuần túy (native binary).
   - Thời gian khởi động (Startup time) giảm từ hàng giây xuống **vài chục mili-giây** ($10-50\text{ ms}$).
   - Dung lượng RAM chiếm dụng khi chạy (Memory footprint) giảm từ $300\text{ MB} - 1\text{ GB}$ xuống chỉ còn $30 - 80\text{ MB}$, cực kỳ tối ưu cho Serverless (AWS Lambda) và Containerized Microservices.
4. **Hỗ trợ Virtual Threads (Java 21 Project Loom)**:
   - Kích hoạt thông qua một thuộc tính cấu hình duy nhất: `spring.threads.virtual.enabled=true`.
   - Chuyển đổi mô hình xử lý Tomcat từ Platform Thread (gắn chặt với OS thread) sang Virtual Thread nhẹ (lightweight user-mode thread), giải phóng nghẽn I/O khi gọi database hoặc microservice khác.
5. **Declarative HTTP Interfaces**:
   - Định nghĩa HTTP client dạng interface tương tự OpenFeign bằng `@HttpExchange`, `@GetExchange`, `@PostExchange` mà không cần cài thêm thư viện ngoài.
6. **Observability mới**: Micrometer Observation API + **Micrometer Tracing** thay thế Spring Cloud Sleuth (đã ngừng phát triển); hỗ trợ OTLP/OpenTelemetry.
7. **`ProblemDetail`** (RFC 7807/9457) cho error response chuẩn hóa; **`RestClient`** (3.2) — client đồng bộ, API fluent thay thế `RestTemplate`.
8. Các mốc 3.x đáng nhớ: 3.1 Testcontainers `@ServiceConnection` + Docker Compose support; 3.2 virtual threads, `RestClient`, `JdbcClient`; 3.4 **structured logging** (ECS/Logstash/GELF JSON), `@MockitoBean` thay `@MockBean`; 3.5 (5/2025) là bản 3.x cuối cùng.

### 1.4. Spring Boot 4 & Spring Framework 7 (GA 11/2025)

> Thế hệ mới nhất tính đến 2026. Một số chi tiết có thể thay đổi theo bản vá — luôn đối chiếu *Spring Boot 4.0 Migration Guide* trên spring.io trước khi nâng cấp.

| Hạng mục | Thay đổi chính |
| :--- | :--- |
| **Baseline** | Java **17** tối thiểu (khuyến nghị 21/25), **Jakarta EE 11** (Servlet 6.1, JPA 3.2 → **Hibernate ORM 7**, Bean Validation 3.1), Kotlin 2.2, GraalVM 25 cho native image |
| **Modularization** | `spring-boot-autoconfigure` khổng lồ được tách thành nhiều module nhỏ theo công nghệ; đổi tên starter (vd `spring-boot-starter-web` → **`spring-boot-starter-webmvc`**, `spring-boot-starter-web-services` → `spring-boot-starter-webservices`); có các starter "classic" (gom toàn bộ auto-config như cũ) để migrate nhanh — đã deprecated |
| **Null safety** | Toàn bộ portfolio chuyển sang **JSpecify** (`@Nullable`, `@NullMarked`) → Kotlin & IDE/NullAway phát hiện NPE lúc compile |
| **JSON** | **Jackson 3** là mặc định (package đổi sang `tools.jackson`); Jackson 2 chỉ còn hỗ trợ tạm thời (deprecated) |
| **API Versioning** | Hỗ trợ versioning **có sẵn** trong Spring MVC/WebFlux: `@GetMapping(path = "/orders", version = "1.1")`, cấu hình chiến lược (header, path, query param, media type) qua `spring.mvc.apiversion.*` |
| **HTTP Service Clients** | Interface `@HttpExchange` được auto-config theo nhóm (`@ImportHttpServices`) — gần như thay thế OpenFeign |
| **Resilience trong core** | `@Retryable`, `@ConcurrencyLimit` + `@EnableResilientMethods` ngay trong Spring Framework (không cần thư viện spring-retry cho trường hợp đơn giản) |
| **Testing** | `RestTestClient` mới (thay `TestRestTemplate`, bật bằng `@AutoConfigureRestTestClient`); `@SpringBootTest` **không còn tự cấu hình** MockMvc/WebTestClient/TestRestTemplate — phải thêm annotation `@AutoConfigure...` tương ứng |
| **Gỡ bỏ** | **Undertow** (chưa tương thích Servlet 6.1 → dùng Tomcat/Jetty), launch scripts của executable jar, một số API deprecated lâu năm (vd `ListenableFuture`); JUnit 4 support deprecated; `hibernate-jpamodelgen` → `hibernate-processor` |
| **Observability** | Starter **OpenTelemetry** chính thức; tiếp tục dùng Micrometer |
| **Virtual threads** | Vẫn bật bằng `spring.threads.virtual.enabled=true` |

**Câu hỏi phỏng vấn hay gặp**: *"Nâng cấp Spring Boot 2.7 → 3.x/4.x cần làm gì?"* → (1) Java 17+, (2) `javax.*` → `jakarta.*` (dùng **OpenRewrite** recipe tự động), (3) Spring Security 6: bỏ `WebSecurityConfigurerAdapter`, dùng bean `SecurityFilterChain` + lambda DSL, `authorizeRequests` → `authorizeHttpRequests`, `antMatchers` → `requestMatchers`, (4) Hibernate 6/7 (thay đổi ID generation, HQL), (5) Sleuth → Micrometer Tracing, (6) property đổi tên (dùng `spring-boot-properties-migrator` để báo), (7) Boot 4: Jackson 3, các starter mới.

---

## 2. IoC Container & Bean Lifecycle Internals

> 🎯 **Bài toán:** Container phải (1) biết cần tạo những object nào và phụ thuộc thế nào, (2) tạo đúng thứ tự, (3) cho phép can thiệp vào quá trình tạo (để thêm proxy, đọc cấu hình), (4) quản lý vòng đời (khởi tạo, hủy).
>
> 💡 **Ý tưởng:** Tách làm hai pha: **đọc định nghĩa** (`BeanDefinition` — "công thức" tạo bean, có thể sửa bằng `BeanFactoryPostProcessor`) rồi mới **tạo instance** (khởi tạo → inject → callback → `BeanPostProcessor` có thể **thay bean bằng proxy**). Hầu hết "phép màu" của Spring (`@Transactional`, `@Async`, `@Value`) đều là một post-processor cắm vào vòng đời này.


### 2.1. BeanFactory vs ApplicationContext

```
               ┌────────────────────────┐
               │      BeanFactory       │ (org.springframework.beans.factory)
               └───────────┬────────────┘
                           │ (extends)
               ┌───────────▼────────────┐
               │   ApplicationContext   │ (org.springframework.context)
               └────────────────────────┘
```

- **`BeanFactory`**:
  - Là gốc rễ của IoC Container, cung cấp các tính năng quản lý bean cơ bản nhất (`getBean()`, `containsBean()`, `isSingleton()`).
  - Sử dụng cơ chế **Lazy Loading**: Bean chỉ được khởi tạo khi có request gọi tới nó lần đầu tiên.
  - Nhẹ, ít chiếm bộ nhớ, phù hợp cho môi trường cực kỳ hạn chế tài nguyên phần cứng (e.g., thiết bị di động/IoT ngày trước).
- **`ApplicationContext`**:
  - Kế thừa `BeanFactory` và mở rộng thêm nhiều tính năng cấp enterprise:
    - **Eager Loading**: Mặc định khởi tạo tất cả Singleton Beans ngay khi ứng dụng khởi động (giúp phát hiện sớm lỗi cấu hình).
    - Hỗ trợ Internationalization (i18n) qua `MessageSource`.
    - Hỗ trợ xuất và bắt sự kiện ứng dụng (Event Publishing & Listening qua `ApplicationEventPublisher`).
    - Tích hợp sâu AOP, Resource loader (`ResourceLoader`), và môi trường cấu hình (`EnvironmentCapable`).
  - Các implementation phổ biến: `AnnotationConfigApplicationContext`, `AnnotationConfigServletWebServerApplicationContext` (cho Spring Boot Web).

### 2.2. Bean Scopes & Vấn đề Thread-Safety

| Scope | Vòng đời & Số lượng Instance | Use Case tiêu biểu | Thread-Safety |
| :--- | :--- | :--- | :--- |
| **`singleton`** (Default) | Duy nhất **1 instance** cho mỗi BeanDefinition trong 1 Spring IoC Container. | Stateless Service, Repository, Component, Controller. | ⚠️ **Không tự động Thread-safe**! Cần thiết kế Stateless (không lưu mutable state). |
| **`prototype`** | Tạo ra **instance mới** mỗi khi bean được yêu cầu (`getBean()` hoặc được inject). | Stateful object, Builder, Task runner mang dữ liệu riêng. | Thread-safe nếu mỗi thread dùng một instance độc lập. |
| **`request`** | 1 instance cho mỗi vòng đời HTTP Request đơn lẻ (Web context). | Request context, User audit log per request. | Thread-safe per HTTP request thread. |
| **`session`** | 1 instance cho toàn bộ vòng đời của 1 HTTP Session (Web context). | Shopping Cart, User Session State. | Cần lock đồng bộ nếu nhiều request cùng session truy cập đồng thời. |
| **`application`**| 1 instance gắn với vòng đời của `ServletContext` (Web context). | Toàn cục web application. | Tương tự Singleton nhưng trong scope ServletContext. |
| **`websocket`**  | 1 instance gắn với vòng đời của một WebSocket session. | Chat room context, WebSocket connection handler. | Thread-safe per websocket session. |

> ⚠️ **Phỏng vấn trap**: *"Spring Singleton Bean có thread-safe không?"*
> **Trả lời**: Mặc định **KHÔNG**. Spring chỉ đảm bảo có duy nhất 1 instance được container quản lý. Nếu bean chứa **mutable state** (ví dụ field `private int counter = 0;` hoặc `private List<String> list;`) và nhiều HTTP request thread cùng đọc/ghi vào đó, hiện tượng **Race Condition** chắc chắn sẽ xảy ra. Do đó, các Service/Controller trong Spring bắt buộc phải là **Stateless** (chỉ chứa các dependency `final` bất biến).

#### Vấn đề Inject Prototype Bean vào Singleton Bean (Scope Inconsistency)
Khi inject một `prototype` bean vào một `singleton` bean, container chỉ inject một lần duy nhất lúc khởi tạo singleton bean. Sau đó, singleton bean sẽ tái sử dụng mãi mãi instance prototype đầu tiên đó, làm mất đi tính chất "mỗi lần gọi là tạo mới" của prototype!

**Cách giải quyết**:
1. Sử dụng `ObjectFactory<MyPrototypeBean>` hoặc `Provider<MyPrototypeBean>`:
```java
@Service
public class SingletonService {
    private final ObjectFactory<PrototypeTask> taskFactory;

    public SingletonService(ObjectFactory<PrototypeTask> taskFactory) {
        this.taskFactory = taskFactory;
    }

    public void execute() {
        PrototypeTask task = taskFactory.getObject(); // Mỗi lần gọi tạo 1 instance mới!
        task.run();
    }
}
```
2. Sử dụng `@Lookup` annotation trên method để Spring CGLIB override dynamic lookup tại runtime.

---

### 2.3. Quy trình Vòng đời của một Bean (Bean Lifecycle)

Spring quản lý bean theo một chu trình khép kín chặt chẽ từ lúc đọc cấu hình đến khi giải phóng tài nguyên:

```
[ ApplicationContext Starts ]
              │
              ▼
1. Đọc Bean Definition (Metadata: Class, Scope, Dependencies)
              │
              ▼
2. Customization Factory: BeanFactoryPostProcessor.postProcessBeanFactory()
              │
              ▼
3. Instantiation: Gọi Constructor hoặc Factory Method (Tạo Java Object thô)
              │
              ▼
4. Populate Properties: Dependency Injection (Inject fields / setter)
              │
              ▼
5. Aware Callbacks:
   - BeanNameAware.setBeanName()
   - BeanClassLoaderAware.setBeanClassLoader()
   - BeanFactoryAware.setBeanFactory()
   - ApplicationContextAware.setApplicationContext()
              │
              ▼
6. BeanPostProcessor.postProcessBeforeInitialization()
              │
              ▼
7. Initialization:
   - Method đánh dấu @PostConstruct
   - InitializingBean.afterPropertiesSet()
   - Custom initMethod() (khai báo trong @Bean(initMethod = "..."))
              │
              ▼
8. BeanPostProcessor.postProcessAfterInitialization()
   (Proxy Wrapper AOP: @Transactional, @Async, @Cacheable ĐƯỢC TẠO TẠI ĐÂY!)
              │
              ▼
══════════════ [ BEAN SẴN SÀNG PHỤC VỤ (READY) ] ══════════════
              │
              ▼ (Container Shutdown / Context Close)
9. Destruction:
   - Method đánh dấu @PreDestroy
   - DisposableBean.destroy()
   - Custom destroyMethod() (khai báo trong @Bean(destroyMethod = "..."))
```

### 2.4. Các Extension Point của IoC Container (hay hỏi ở mức Senior)

| | `BeanFactoryPostProcessor` (BFPP) | `BeanPostProcessor` (BPP) |
| :--- | :--- | :--- |
| **Chạy khi nào** | Sau khi nạp **BeanDefinition**, **trước** khi tạo bất kỳ bean thường nào | Trong lúc **khởi tạo từng bean** (trước/sau init callback — bước 6 & 8 ở 2.3) |
| **Thao tác trên** | **Định nghĩa** bean (metadata: class, scope, property value...) | **Instance** bean đã tạo — có thể bọc/thay thế bằng proxy |
| **Ví dụ trong Spring** | `PropertySourcesPlaceholderConfigurer` (thay `${...}`), `ConfigurationClassPostProcessor` (xử lý `@Configuration`, `@ComponentScan`, `@Import`, `@Bean` → sinh BeanDefinition) | `AutowiredAnnotationBeanPostProcessor` (`@Autowired`, `@Value`), `CommonAnnotationBeanPostProcessor` (`@PostConstruct`, `@PreDestroy`, `@Resource`), `AnnotationAwareAspectJAutoProxyCreator` (tạo AOP proxy cho `@Transactional`, `@Aspect`...), `AsyncAnnotationBeanPostProcessor` |
| Biến thể | `BeanDefinitionRegistryPostProcessor` (đăng ký thêm BeanDefinition) | `InstantiationAwareBeanPostProcessor`, `SmartInstantiationAwareBeanPostProcessor` (`getEarlyBeanReference` — 3.2) |

> ⚠️ Khai báo BFPP bằng `@Bean` nên dùng method **`static`** — nếu không, Spring phải tạo cả class `@Configuration` chứa nó quá sớm (trước khi các BPP sẵn sàng) → `@Autowired`/`@Value` trong class đó không hoạt động.

**`BeanFactory` vs `FactoryBean`** (tên gần giống, khác hoàn toàn):
- `BeanFactory`: chính là **container** (2.1).
- `FactoryBean<T>`: một bean đặc biệt dùng để **sản xuất bean khác** khi logic khởi tạo phức tạp. `getBean("x")` trả về **sản phẩm** `getObject()`, còn `getBean("&x")` trả về chính FactoryBean. Ví dụ: `SqlSessionFactoryBean` (MyBatis), `LocalContainerEntityManagerFactoryBean`, proxy của Spring Data repository (`JpaRepositoryFactoryBean`), Feign client.

**`@Configuration` full mode vs lite mode (`proxyBeanMethods`)**:
```java
@Configuration                       // full mode: class bị CGLIB subclass
public class AppConfig {
    @Bean DataSource dataSource() { return new HikariDataSource(); }
    @Bean JdbcTemplate jdbcTemplate() { return new JdbcTemplate(dataSource()); }  // gọi dataSource() → proxy trả về BEAN SINGLETON đã có
}

@Configuration(proxyBeanMethods = false)   // lite mode: không proxy → gọi dataSource() trực tiếp sẽ tạo object MỚI (không phải bean!)
public class LiteConfig {
    @Bean JdbcTemplate jdbcTemplate(DataSource ds) { return new JdbcTemplate(ds); }  // ✅ cách đúng: inject qua tham số
}
```
- Spring Boot dùng `proxyBeanMethods = false` cho hầu hết auto-configuration → khởi động nhanh hơn, tương thích GraalVM native image. `@Bean` khai báo trong `@Component` thường cũng là lite mode.

**Lifecycle của chính ApplicationContext** (các hook ở mức ứng dụng):
- `SmartLifecycle` (`start()/stop()` theo `phase`) — dùng cho thành phần cần chạy/dừng theo thứ tự: web server, Kafka listener container, scheduler. Graceful shutdown dừng theo phase ngược lại.
- `ApplicationRunner` / `CommandLineRunner` — chạy 1 lần sau khi context sẵn sàng (warm-up cache, kiểm tra cấu hình).
- Event: `ContextRefreshedEvent`, `ApplicationStartedEvent`, `ApplicationReadyEvent`, `ContextClosedEvent`; Boot còn có `AvailabilityChangeEvent` (liveness/readiness).

### 2.5. Application Events
```java
public record OrderPlacedEvent(Long orderId) {}           // Spring 4.2+: event là object bất kỳ, không cần extends ApplicationEvent

@Service class OrderService {
    private final ApplicationEventPublisher events;
    OrderService(ApplicationEventPublisher events) { this.events = events; }

    @Transactional
    public void place(Order o) { /* save... */ events.publishEvent(new OrderPlacedEvent(o.getId())); }
}

@Component class Listeners {
    @EventListener                                         // ĐỒNG BỘ, cùng thread & cùng transaction với publisher
    void audit(OrderPlacedEvent e) { ... }                 // listener ném exception → làm hỏng luôn transaction của publisher

    @Async @EventListener                                  // chạy thread khác (cần @EnableAsync)
    void analytics(OrderPlacedEvent e) { ... }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)   // chỉ chạy nếu transaction commit thành công
    void sendEmail(OrderPlacedEvent e) { ... }
}
```
- Thứ tự giữa các listener: `@Order`. Lọc: `@EventListener(condition = "#e.orderId > 100")`.
- Event trong cùng JVM **không bền**: app crash là mất → nghiệp vụ quan trọng giữa các service dùng Outbox + message broker (SystemDesign 3.6). Spring Modulith có *Event Publication Registry* lưu event vào DB để publish lại.

**⚖️ Constructor vs Setter vs Field injection**

| | Constructor | Setter | Field (`@Autowired` trên field) |
| :--- | :--- | :--- | :--- |
| Field `final`, object luôn hợp lệ | ✅ | ❌ | ❌ |
| Test không cần Spring (`new Service(mock)`) | ✅ | ✅ | ❌ phải dùng reflection |
| Lộ ra "quá nhiều dependency" (code smell) | ✅ rõ ngay | ❌ | ❌ bị che giấu |
| Dependency tùy chọn/thay đổi sau | ❌ | ✅ | ✅ |

✅ **Vì sao chọn constructor injection:** object bất biến, phụ thuộc tường minh, test dễ, và vòng phụ thuộc được phát hiện ngay lúc khởi động thay vì chạy ngầm.

> 🛠 **Áp dụng:**
> - Bean singleton dùng chung cho mọi request → **không giữ trạng thái theo request** trong field.
> - Cần trạng thái theo request/prototype trong singleton → `ObjectProvider<T>` hoặc scoped proxy (2.2).
> - Viết tích hợp tự động (gắn metric cho mọi bean có annotation X) → `BeanPostProcessor`.

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao `@Transactional` không hoạt động trên bean tạo ra trong `BeanPostProcessor` khởi tạo sớm?**
   → Bean được tạo trước khi các post-processor tạo proxy được đăng ký nên không được bọc proxy (log cảnh báo "not eligible for getting processed by all BeanPostProcessors").
2. **Inject bean prototype vào singleton, mỗi lần gọi có được object mới không?**
   → Không; inject xảy ra một lần khi tạo singleton. Phải lấy qua `ObjectProvider`/lookup method mỗi lần cần.
</details>


---

## 3. Circular Dependency & Three-Level Cache

> 🎯 **Bài toán:** A cần B, B cần A. Muốn tạo A phải có B, muốn tạo B phải có A → vòng lặp vô hạn.
>
> 💡 **Ý tưởng:** Với setter/field injection, có thể **cho B một tham chiếu tới A "đã tạo nhưng chưa xong"** (đã gọi constructor, chưa inject xong). Three-level cache tồn tại để giải chi tiết khó: nếu A cần được bọc **proxy**, B phải nhận **proxy** chứ không phải A gốc → cache cấp 3 lưu một *factory* sinh tham chiếu sớm (có proxy nếu cần).
>
> ✅ **Vì sao Spring Boot 2.6+ cấm mặc định:** vòng phụ thuộc gần như luôn là dấu hiệu thiết kế sai (hai class gánh trách nhiệm đan xen); và với constructor injection thì không thể giải được. Cách sửa đúng là **tách trách nhiệm** sang class thứ ba hoặc dùng event, không phải bật `allow-circular-references`.


### 3.1. Bản chất Vấn đề Circular Dependency

Circular Dependency xảy ra khi Bean A phụ thuộc vào Bean B, và Bean B lại phụ thuộc ngược lại Bean A (`A -> B -> A`), hoặc vòng tròn nhiều mắt xích (`A -> B -> C -> A`).

```
┌─────────┐   depends on    ┌─────────┐
│ ServiceA│ ───────────────►│ ServiceB│
└─────────┘                 └─────────┘
     ▲                           │
     └───────────────────────────┘
              depends on
```

- **Constructor Injection**: Gây lỗi `BeanCurrentlyInCreationException` ngay lập tức khi ứng dụng khởi động. Cả hai bean đều đang đợi constructor của bên kia hoàn thành để tạo object, dẫn đến Deadlock khởi tạo!
- **Setter / Field Injection**: Spring có thể tự động xử lý được nhờ cơ chế **Three-Level Cache (Bộ nhớ đệm 3 cấp)**.

### 3.2. Three-Level Cache trong DefaultSingletonBeanRegistry

Để giải quyết vòng lặp cho các Singleton bean khởi tạo qua Setter/Field injection (kể cả bean có AOP Proxy), Spring thiết kế 3 Map lưu trữ:

```
Level 1: singletonObjects        (ConcurrentHashMap<String, Object>)
         Chứa các Bean HOÀN CHỈNH: đã tạo instance, đã inject dependencies, đã init xong.

Level 2: earlySingletonObjects   (HashMap<String, Object>)
         Chứa các Bean SƠ KHAI (Early Beans): đã gọi constructor nhưng CHƯA hoàn tất inject dependencies.

Level 3: singletonFactories      (HashMap<String, ObjectFactory<?>>)
         Chứa các ObjectFactory (lambda): dùng để tạo Early Reference (đặc biệt là tạo sớm AOP Proxy nếu có).
```

#### Quy trình xử lý giải quyết `ServiceA <-> ServiceB`:

```
B1: Bắt đầu tạo ServiceA
    - Tạo object thô của ServiceA qua Constructor: `new ServiceA()`
    - Đưa lambda `() -> getEarlyBeanReference(A)` vào Level 3 Cache (singletonFactories)
    - Tiến hành inject dependencies cho ServiceA -> Phát hiện cần ServiceB!

B2: Chuyển sang tạo ServiceB
    - Tạo object thô của ServiceB qua Constructor: `new ServiceB()`
    - Đưa lambda vào Level 3 Cache cho ServiceB
    - Tiến hành inject dependencies cho ServiceB -> Phát hiện cần ServiceA!

B3: ServiceB tìm ServiceA
    - Kiểm tra Level 1 (singletonObjects): Chưa có.
    - Kiểm tra Level 2 (earlySingletonObjects): Chưa có.
    - Kiểm tra Level 3 (singletonFactories): Thấy Factory của ServiceA!
    - Thực thi Factory: Tạo ra Early Reference của ServiceA (nếu ServiceA có AOP thì tạo Proxy cho A trước).
    - Lưu reference của ServiceA vào Level 2, xóa khỏi Level 3.
    - Inject ServiceA (sơ khai) này vào ServiceB.

B4: ServiceB hoàn thiện
    - ServiceB hoàn tất inject properties, gọi @PostConstruct, tạo Proxy (nếu có).
    - ServiceB đưa vào Level 1 (singletonObjects).

B5: Quay lại hoàn thiện ServiceA
    - ServiceA nhận được ServiceB (đã hoàn chỉnh) từ Level 1.
    - ServiceA hoàn tất các bước còn lại và chuyển từ Level 2 sang Level 1.
```

> ❓ **Tại sao cần 3 cấp mà không phải 2?** Nếu chỉ có Level 2, Spring buộc phải tạo **AOP proxy ngay sau khi new** cho mọi bean (để lỡ có ai cần early reference) — đi ngược thiết kế "proxy được tạo ở bước cuối (postProcessAfterInitialization)". Level 3 lưu một **ObjectFactory** để **trì hoãn**: chỉ khi thật sự có vòng tròn thì mới gọi `getEarlyBeanReference()` để tạo proxy sớm; không có vòng tròn thì Level 3 không bao giờ được dùng.
>
> ⚠️ **Bẫy**: Bean có **`@Async`** nằm trong vòng tròn vẫn có thể lỗi `BeanCurrentlyInCreationException: Bean with name 'x' has been injected into other beans [...] in its raw version as part of a circular reference, but has eventually been wrapped` — vì proxy của `@Async` được tạo bởi `AsyncAnnotationBeanPostProcessor` (không tham gia cơ chế early reference như auto-proxy của `@Transactional`), nên bean khác đã giữ bản "raw" khác với bean cuối cùng. Chỉ hoạt động với **singleton**; prototype bean vòng tròn luôn lỗi.

> ⚠️ **Tại sao Spring Boot 2.6+ disable Circular References mặc định?**
> Bắt đầu từ Spring Boot 2.6, cấu hình `spring.main.allow-circular-references=false` là mặc định.
> **Lý do**:
> 1. Circular dependency là biểu hiện rõ ràng của **Code Smell** và vi phạm nghiêm trọng nguyên tắc Single Responsibility Principle (SRP) của SOLID.
> 2. Gây khó khăn khi viết Unit Test độc lập.
> 3. Tăng nguy cơ lỗi tiềm ẩn với Lifecycle callbacks và làm chậm thời gian khởi động container.
> 
> **Cách xử lý chuẩn khi gặp Circular Dependency**:
> - Tách phần logic chung mà cả 2 service cùng cần sang một Service thứ 3 (ví dụ `CommonService` hoặc `EventPublisher`).
> - Sử dụng Event-driven architecture: ServiceA phát ra `ApplicationEvent`, ServiceB lắng nghe qua `@EventListener`.
> - Dùng `@Lazy` tại điểm inject (chỉ là giải pháp tình thế, không khuyến khích trong thiết kế chuẩn).

---

## 4. Spring AOP & Proxy Mechanisms

> 🎯 **Bài toán:** Mở/commit/rollback transaction, kiểm tra quyền, đo thời gian, cache... lặp lại ở hàng trăm method và trộn lẫn với business logic.
>
> 💡 **Ý tưởng:** Gói hành vi chung vào **advice**, chỉ định nơi áp dụng bằng **pointcut**, và chèn vào bằng **proxy**: container đưa cho nơi dùng một object **đứng giữa** (proxy) — nó chạy advice rồi gọi bean thật.
>
> ⚖️ **Ba cách "chèn" hành vi:** (1) proxy lúc runtime (Spring AOP) — đơn giản, không cần công cụ build, nhưng chỉ chặn được **lời gọi đi qua proxy** tới method public; (2) AspectJ weaving lúc compile/load — chặn được mọi thứ (kể cả self-invocation, private) nhưng cần compiler/agent riêng; (3) viết tay (decorator).
>
> ✅ **Vì sao Spring chọn proxy runtime làm mặc định:** đủ cho 95% nhu cầu (transaction, security, cache ở ranh giới service), không đòi hỏi thay đổi build. Cái giá phải nhớ: **self-invocation không qua proxy** (4.3).


### 4.1. So sánh JDK Dynamic Proxy vs CGLIB Proxy

Spring AOP là kiến trúc dựa trên **Runtime Proxy Pattern**. Khi một bean được đánh dấu các annotation như `@Transactional`, `@Async`, `@Cacheable`, `@PreAuthorize`, Spring không gọi trực tiếp class gốc mà tạo ra một đối tượng Proxy bao bọc bên ngoài.

```
Client ──► [ Proxy Object (Intercept advice) ] ──► [ Real Target Bean ]
```

| Tiêu chí | JDK Dynamic Proxy | CGLIB Proxy |
| :--- | :--- | :--- |
| **Cơ chế hoạt động** | Tạo proxy class tại runtime hiện thực hóa (implement) cùng một tập **Interfaces** với Target object thông qua `java.lang.reflect.Proxy`. | Tạo proxy class tại runtime bằng cách **kế thừa (subclassing)** Target class thông qua việc sinh bytecode trực tiếp (`org.springframework.cglib`). |
| **Yêu cầu đối với Target** | Bắt buộc Target class phải implement ít nhất một **Interface**. | Target class **không bắt buộc** có interface. |
| **Hạn chế** | Không thể proxy các method không nằm trong Interface; inject bằng **class cụ thể** sẽ lỗi (`BeanNotOfRequiredTypeException`) vì proxy chỉ là interface. | **Không thể proxy** class `final` và **không intercept** method `final`/`private`/`static` (vì không override được). Spring 4+ dùng **Objenesis** để tạo proxy instance **không gọi constructor** của target (trước đây constructor bị gọi 2 lần). ⚠️ Kotlin class mặc định `final` → cần plugin `kotlin-spring` (all-open). |
| **Cấu hình mặc định trong Spring Boot** | Mặc định trong Spring Framework truyền thống (khi class có interface). | **Mặc định trong Spring Boot 2.x và 3.x** (`spring.aop.proxy-target-class=true`). |

### 4.2. Các loại Advice trong Spring AOP

```java
@Aspect
@Component
public class PerformanceLoggingAspect {

    @Pointcut("execution(* com.example.service.*.*(..))")
    public void serviceLayerMethods() {}

    @Before("serviceLayerMethods()")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("Starting method: " + joinPoint.getSignature().getName());
    }

    @AfterReturning(pointcut = "serviceLayerMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("Method completed with result: " + result);
    }

    @AfterThrowing(pointcut = "serviceLayerMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        System.err.println("Method failed with error: " + ex.getMessage());
    }

    @Around("serviceLayerMethods()")
    public Object measureExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return pjp.proceed(); // Tiếp tục thực thi method gốc
        } finally {
            long duration = System.currentTimeMillis() - start;
            System.out.println("Execution time: " + duration + " ms");
        }
    }
}
```

### 4.3. Cạm bẫy Tự gọi Nội bộ (Self-Invocation Pitfall) & Cách khắc phục

Đây là một trong những câu hỏi phỏng vấn Senior phổ biến nhất.

```java
@Service
public class OrderService {

    public void processOrder(Order order) {
        // Business logic...
        this.saveWithTransaction(order); // ❌ LỖI: Bypass proxy!
    }

    @Transactional
    public void saveWithTransaction(Order order) {
        // Lưu DB cần transaction
    }
}
```

**Nguyên nhân**: Khi `processOrder()` gọi `this.saveWithTransaction()`, từ khóa `this` trỏ trực tiếp đến instance thực tế của class chứ không đi qua Spring Proxy. Do đó, toàn bộ AOP Interceptor (`TransactionInterceptor`) bị bỏ qua, dẫn đến việc transaction **không bao giờ được mở**! Tương tự với `@Async`, `@Cacheable`.

#### 3 Cách Khắc phục Triệt để:

1. **Tách class (Khuyến nghị số 1)**: Chuyển method cần proxy sang một bean/service riêng biệt để luôn được gọi qua proxy.
2. **Self-Injection (Tiêm chính mình)**:
```java
@Service
public class OrderService {
    @Lazy
    @Autowired
    private OrderService self; // Inject chính proxy của mình

    public void processOrder(Order order) {
        self.saveWithTransaction(order); // ✅ Đi qua proxy -> Có Transaction!
    }

    @Transactional
    public void saveWithTransaction(Order order) { ... }
}
```
3. **Sử dụng `AopContext.currentProxy()`**:
Cần bật cấu hình `@EnableAspectJAutoProxy(exposeProxy = true)` và gọi:
```java
((OrderService) AopContext.currentProxy()).saveWithTransaction(order);
```

> 🛠 **Áp dụng:**
> - Annotation dựa trên proxy (`@Transactional`, `@Async`, `@Cacheable`, `@Retryable`, `@PreAuthorize`) chỉ có tác dụng khi được gọi **từ bean khác** vào method **public**.
> - Tự viết aspect cho đo thời gian/audit; giữ advice nhẹ và không nuốt exception.

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao Spring Boot mặc định dùng CGLIB thay vì JDK proxy?**
   → Để inject theo **class cụ thể** vẫn chạy được và hành vi nhất quán dù bean có interface hay không.
2. **`@Transactional` trên method `private` có tác dụng không?**
   → Không — proxy (kế thừa class) không override được method private.
</details>


---

## 5. Spring Boot Auto-Configuration Internals

> 🎯 **Bài toán:** Một web app với JPA cần cấu hình DataSource, connection pool, EntityManagerFactory, TransactionManager, Jackson, DispatcherServlet, Tomcat... — hàng trăm dòng giống nhau ở mọi dự án, và phải chọn phiên bản thư viện tương thích nhau.
>
> 💡 **Ý tưởng giải:**
> - **Starter** = gói dependency đã kiểm chứng tương thích.
> - **Auto-configuration** = các lớp `@Configuration` được nạp tự động, mỗi bean đi kèm **điều kiện** (`@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`).
> - Nguyên tắc **"convention over configuration, nhưng nhường cho người dùng"**: bạn khai báo bean của mình → bean mặc định tự lùi lại (`@ConditionalOnMissingBean`).


### 5.1. Phân tích `@SpringBootApplication`

Annotation `@SpringBootApplication` là tổ hợp của 3 annotation cốt lõi:
1. `@SpringBootConfiguration`: Đánh dấu class là một Spring `@Configuration`.
2. `@ComponentScan`: Tự động scan các `@Component`, `@Service`, `@Repository`, `@Controller` từ package chứa class chính trở xuống.
3. `@EnableAutoConfiguration`: Kích hoạt cơ chế tự động dò tìm và cấu hình các Bean dựa vào classpath dependencies.

### 5.2. Cơ chế SPI Auto-configuration

```
Classpath có "h2.jar" + "spring-boot-starter-data-jpa.jar"
                          │
                          ▼
Spring Boot đọc cấu hình Imports:
- Spring Boot 2.x: META-INF/spring.factories (key: EnableAutoConfiguration)
- Spring Boot 3.x: META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
                          │
                          ▼
Duyệt qua danh sách các AutoConfiguration classes (e.g., DataSourceAutoConfiguration)
                          │
                          ▼
Kiểm tra các điều kiện @Conditional:
- @ConditionalOnClass(DataSource.class) -> Thỏa mãn!
- @ConditionalOnMissingBean(DataSource.class) -> User chưa tự cấu hình DataSource nào!
                          │
                          ▼
Tự động đăng ký HikariDataSource Bean vào ApplicationContext!
```

### 5.3. Họ Annotation `@Conditional`

- `@ConditionalOnClass(X.class)`: Chỉ kích hoạt nếu class X xuất hiện trong Classpath.
- `@ConditionalOnMissingBean(X.class)`: Chỉ kích hoạt nếu ApplicationContext chưa có bean kiểu X nào. Giúp người dùng dễ dàng override bean bằng cấu hình riêng.
- `@ConditionalOnProperty(name = "feature.enabled", havingValue = "true")`: Chỉ kích hoạt nếu config property trong `application.yml` có giá trị tương ứng.
- `@ConditionalOnWebApplication`: Chỉ kích hoạt khi ứng dụng đang chạy dưới dạng Web Application.
- `@ConditionalOnResource`: Chỉ kích hoạt khi một file tài nguyên cụ thể tồn tại.

### 5.4. Fat JAR & `java -jar` hoạt động thế nào?
```
app.jar
├── META-INF/MANIFEST.MF        Main-Class: org.springframework.boot.loader.launch.JarLauncher   (Boot 3.2+; cũ: ...loader.JarLauncher)
│                               Start-Class: com.example.Application                            ← class main của bạn
├── org/springframework/boot/loader/...   ← code của "Spring Boot Loader"
├── BOOT-INF/classes/           ← class & resource của ứng dụng
├── BOOT-INF/lib/               ← các dependency jar NẰM NGUYÊN trong jar (nested jar)
├── BOOT-INF/classpath.idx      ← thứ tự classpath
└── BOOT-INF/layers.idx         ← thông tin layer cho Docker
```
- JDK chuẩn **không đọc được jar lồng trong jar** → `JarLauncher` (là `Main-Class` thật) tạo một **ClassLoader riêng** biết đọc `BOOT-INF/lib/*.jar` lồng bên trong, rồi gọi `main()` của `Start-Class`.
- **Layered jar** (`java -Djarmode=tools -jar app.jar extract --layers` — Boot 3.3+; trước đó `-Djarmode=layertools`): tách thành các layer `dependencies` → `spring-boot-loader` → `snapshot-dependencies` → `application`. Trong Dockerfile copy từng layer riêng → đổi code chỉ build lại layer `application` nhỏ, các layer dependency được cache.
- Có thể chạy dạng "exploded" (`java -cp BOOT-INF/classes:BOOT-INF/lib/* com.example.Application`) — khởi động nhanh hơn chút, cần cho CDS/AOT cache.
- WAR truyền thống: extends `SpringBootServletInitializer`, đóng gói `war`, deploy vào Tomcat ngoài.

### 5.5. Debug Auto-Configuration
- Chạy với `--debug` (hoặc `debug=true`) → in **Condition Evaluation Report**: auto-config nào được áp dụng (*Positive matches*) / bị bỏ qua và **vì sao** (*Negative matches*).
- Actuator: `/actuator/conditions` (báo cáo điều kiện), `/actuator/beans` (danh sách bean), `/actuator/configprops`, `/actuator/env`.
- Tắt 1 auto-config: `@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)` hoặc `spring.autoconfigure.exclude=...`.
- Thứ tự giữa các auto-config: `@AutoConfiguration(before = ..., after = ...)`; auto-config luôn được xử lý **sau** cấu hình của người dùng → `@ConditionalOnMissingBean` mới nhìn thấy bean bạn tự khai báo.

> 🛠 **Áp dụng:**
> - Muốn đổi hành vi: ưu tiên **property** (`spring.datasource.hikari.*`) → khai báo **bean của mình** để thay bean mặc định → cuối cùng mới `exclude` auto-config.
> - Không hiểu vì sao một bean có/không được tạo: chạy với `--debug` để xem **Condition Evaluation Report** (5.5).
> - Dùng chung cấu hình cho nhiều service trong công ty → viết **starter riêng** (Q6).

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao auto-configuration phải dùng `@ConditionalOnMissingBean`?**
   → Để bean của người dùng luôn được ưu tiên; nếu không, sẽ có hai bean cùng kiểu và Spring không biết inject cái nào.
2. **Thêm `spring-boot-starter-data-jpa` vào nhưng chưa cấu hình URL DB thì sao?**
   → Auto-config DataSource kích hoạt (có class trên classpath) nhưng không có URL/DB nhúng → khởi động thất bại. Đây chính là tác dụng của `@ConditionalOnClass`.
</details>


---

## 6. Spring MVC Architecture & Request Lifecycle

> 🎯 **Bài toán:** Mỗi HTTP request cần: tìm đúng method xử lý, chuyển tham số (path, query, JSON body) thành object Java, validate, gọi business, chuyển kết quả thành JSON, xử lý lỗi thống nhất — cho hàng trăm endpoint.
>
> 💡 **Ý tưởng:** **Front Controller** (`DispatcherServlet`) nhận mọi request rồi ủy quyền cho các thành phần có thể thay thế: `HandlerMapping` (tìm method), `HandlerAdapter` + `ArgumentResolver` (chuẩn bị tham số), `HttpMessageConverter` (JSON ↔ object), `HandlerExceptionResolver` (lỗi). Mỗi request chạy trên **một thread của Tomcat** từ đầu đến cuối (thread-per-request).


### 6.1. Chi tiết Vòng đời HTTP Request

```
[ HTTP Request từ Client ]
            │
            ▼
┌───────────────────────────────────────────────────────────────────┐
│ Servlet Container Filters (DelegatingFilterProxy, Spring Security)│
└───────────────────────────────────┬───────────────────────────────┘
                                    │
                                    ▼
┌───────────────────────────────────────────────────────────────────┐
│                        DispatcherServlet                          │
└───────────────────────────────────┬───────────────────────────────┘
                                    │ 1. getHandler(request)
                                    ▼
┌───────────────────────────────────────────────────────────────────┐
│                         HandlerMapping                            │ (Tìm Controller phù hợp)
└───────────────────────────────────┬───────────────────────────────┘
                                    │ 2. Trả về HandlerExecutionChain (Interceptor + Handler)
                                    ▼
┌───────────────────────────────────────────────────────────────────┐
│             HandlerInterceptor.preHandle()                        │
└───────────────────────────────────┬───────────────────────────────┘
                                    │ 3. handle(request, response, handler)
                                    ▼
┌───────────────────────────────────────────────────────────────────┐
│                         HandlerAdapter                            │ (Gọi Controller method)
│   (HandlerMethodArgumentResolver: chuyển Json -> Request DTO)     │
└───────────────────────────────────┬───────────────────────────────┘
                                    │ 4. Invocation
                                    ▼
┌───────────────────────────────────────────────────────────────────┐
│                      @RestController Controller                   │ (Xử lý Business Logic)
└───────────────────────────────────┬───────────────────────────────┘
                                    │ 5. Return Response Entity
                                    ▼
┌───────────────────────────────────────────────────────────────────┐
│         HttpMessageConverter (MappingJackson2HttpMessageConverter)│
│                     (Serialize Java Object -> JSON)               │
└───────────────────────────────────┬───────────────────────────────┘
                                    │
                                    ▼
┌───────────────────────────────────────────────────────────────────┐
│            HandlerInterceptor.postHandle()                        │
└───────────────────────────────────┬───────────────────────────────┘
                                    │
                                    ▼
┌───────────────────────────────────────────────────────────────────┐
│            HandlerInterceptor.afterCompletion()                  │
└───────────────────────────────────┬───────────────────────────────┘
                                    │
                                    ▼
[ HTTP 200 OK / Response Payload trả về Client ]
```

> ⚠️ Với `@RestController`/`@ResponseBody`, body đã được `HttpMessageConverter` ghi (và response có thể đã **commit**) **trước** `postHandle()` → không sửa được response trong `postHandle`; muốn sửa body dùng `ResponseBodyAdvice`. `afterCompletion()` luôn chạy (kể cả khi có exception) → nơi dọn ThreadLocal/MDC.
>
> 📌 Các thành phần khác hay được hỏi: `HandlerExceptionResolver` (xử lý exception → `@ExceptionHandler`/`@ControllerAdvice`), `ViewResolver` (chỉ với `@Controller` trả về view), `HandlerMethodArgumentResolver` (custom tham số như `@CurrentUser User u`), `@InitBinder`/`Converter`/`Formatter`.

### 6.2. So sánh Filter vs HandlerInterceptor vs AOP Aspect

| Tiêu chí | Servlet Filter | HandlerInterceptor | AOP Aspect |
| :--- | :--- | :--- | :--- |
| **Thuộc tầng** | Servlet Container chuẩn (Java EE / Jakarta Servlet). Nằm **trước** DispatcherServlet. | Spring MVC Framework. Nằm **giữa** DispatcherServlet và Controller. | Spring AOP Core. Bao bọc bất kỳ Spring Bean method nào. |
| **Phạm vi nhận biết** | Chỉ biết `HttpServletRequest`, `HttpServletResponse`. Hoàn toàn không biết method/controller nào sắp được gọi. | Biết được `Object handler` (Method, Class Controller đang xử lý request). | Can thiệp sâu vào method arguments, return values, exceptions tại tầng service/repository. |
| **Use cases phù hợp** | Gzip compression, CORS headers, Request Body logging/caching, Security authentication filter (Spring Security). | Phân quyền theo endpoint, Rate limiting theo controller, thêm request execution metrics, gán Tenant-ID context. | Business audit log, Cache eviction, Performance tracing, Custom retry logic. |

### 6.3. Xử lý Lỗi Chuẩn Hóa với `@RestControllerAdvice` & RFC 7807 Problem Details

Từ Spring Boot 3+, Spring hỗ trợ đặc tả chuẩn **RFC 7807 (Problem Details for HTTP APIs)** — nay được thay thế bởi **RFC 9457** (2023, tương thích ngược) — thông qua class `ProblemDetail` (content type `application/problem+json`). Bật cho các exception có sẵn của Spring MVC bằng `spring.mvc.problemdetails.enabled=true`.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource Not Found");
        problem.setType(URI.create("https://api.example.com/errors/not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setTitle("Invalid Request Payload");
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        problem.setProperty("invalid_fields", fieldErrors);
        return problem;
    }
}
```

### 6.4. Embedded Tomcat: Mô hình luồng & tuning

```
                 ┌──────────── Tomcat NIO Connector (mặc định) ────────────┐
Client TCP ────► │ Acceptor thread ──► Poller thread(s) (Selector/epoll) ──► Worker thread pool ──► Filter → DispatcherServlet → Controller
connections      │   accept()           theo dõi hàng nghìn kết nối          (server.tomcat.threads.max = 200)
                 │                      keep-alive đang rảnh                 1 thread xử lý trọn 1 request (blocking)
                 └──────────────────────────────────────────────────────────┘
```
| Property | Mặc định | Ý nghĩa |
| :--- | :--- | :--- |
| `server.tomcat.threads.max` | **200** | Số worker thread tối đa = số request được **xử lý đồng thời** |
| `server.tomcat.threads.min-spare` | 10 | Số thread luôn giữ sẵn |
| `server.tomcat.max-connections` | **8192** | Số kết nối TCP Tomcat chấp nhận cùng lúc (kể cả đang rảnh keep-alive) — NIO nên nhiều hơn số thread nhiều |
| `server.tomcat.accept-count` | **100** | Hàng đợi **backlog của OS** khi đã đạt `max-connections`; đầy nữa → client bị từ chối/timeout kết nối |
| `server.tomcat.connection-timeout` | 60s (mặc định của Tomcat connector; file `server.xml` của Tomcat standalone đặt 20s) | Thời gian chờ dữ liệu request sau khi kết nối |
| `server.tomcat.keep-alive-timeout` | = connection-timeout | Giữ kết nối rảnh bao lâu |
| `server.tomcat.max-keep-alive-requests` | 100 | Số request tối đa trên 1 kết nối keep-alive |

- **Khi quá tải**: 200 thread đều bận (thường do **chờ DB/downstream**, không phải CPU) → request mới xếp hàng trong Tomcat → latency tăng vọt → client timeout & retry → càng quá tải. Tăng `threads.max` thường **không** giải quyết được nếu nút thắt là DB pool (20 connection) — xem 13.3.
- **Virtual threads** (`spring.threads.virtual.enabled=true`): Tomcat dùng virtual thread cho mỗi request → không còn giới hạn 200, nhưng phải giới hạn concurrency ở tài nguyên phía sau (11.2).
- **Jetty / Reactor Netty**: Jetty tương tự Tomcat; Netty (WebFlux) dùng **event loop** (số thread ≈ số core) — không bao giờ được block (18).
- Liên quan network: `accept-count` chính là TCP accept backlog; **keep-alive timeout của Tomcat nên lớn hơn idle timeout của LB** (vd ALB 60s) để tránh lỗi 502 lác đác — xem [`Networking.md`](Networking.md) mục 3.2, 9 và 13.3.

> 🛠 **Áp dụng:**
> - Việc gì áp dụng cho **mọi request kể cả ngoài Spring** (CORS, auth, log request) → Filter; cần biết **controller nào** xử lý → Interceptor; logic nghiệp vụ chéo → AOP (6.2).
> - Throughput giới hạn bởi số thread Tomcat (mặc định 200) × thời gian xử lý mỗi request. Request chủ yếu chờ I/O → bật **virtual threads** thay vì tăng `max-threads`.

<details><summary>🧠 Tự kiểm tra</summary>

1. **200 thread Tomcat, mỗi request mất 100 ms chờ DB. Throughput tối đa xấp xỉ bao nhiêu?**
   → ~200 / 0,1 s = 2.000 request/s (Little's Law) — và thực tế còn bị giới hạn bởi DB pool nhỏ hơn nhiều.
</details>


---

## 7. Spring Data JPA & Hibernate Internals

> 🎯 **Bài toán:** Ứng dụng nghĩ theo **object & quan hệ giữa object**, DB nghĩ theo **bảng & khóa ngoại** (*object–relational impedance mismatch*). Viết tay SQL + map từng cột rất dài dòng.
>
> 💡 **Ý tưởng — ORM:** Hibernate giữ một **Persistence Context** (bộ đệm theo transaction): mỗi bản ghi chỉ có một object (identity map), tự **phát hiện thay đổi** (dirty checking) và tự sinh SQL lúc flush (unit of work), nạp quan hệ **lười** khi cần.
>
> ❌ **Cái giá:** SQL bị ẩn đi → dễ sinh **N+1 query**, nạp thừa dữ liệu, `LazyInitializationException`, tốn bộ nhớ khi xử lý lô lớn. Hiểu persistence context là chìa khóa để dùng JPA đúng.


### 7.1. Persistence Context & 4 Trạng thái của Entity

Persistence Context là bộ nhớ đệm cấp 1 (First-Level Cache) được quản lý bởi `EntityManager`. Mọi thao tác đọc/ghi trong cùng một Transaction đều tương tác trước tiên với Persistence Context.

```
       [ Transient (New) ]  ─── entity mới tạo bằng new, chưa có ID, DB chưa biết
                │
                │ em.persist(entity) / repo.save()
                ▼
       [ Persistent (Managed) ] ◄── Đang được Persistence Context quản lý
          │           │             Tự động dirty check khi commit transaction!
          │           │
 em.detach()│         │ em.remove()
          │           │
          ▼           ▼
   [ Detached ]   [ Removed ] ─── Đã đánh dấu xóa, chờ flush câu lệnh DELETE
          │
 em.merge()│
          ▼
   [ Managed ]
```

> **Dirty Checking**: Trong trạng thái Managed, bạn **không cần gọi `repo.save(entity)`** sau khi sửa đổi dữ liệu! Khi transaction kết thúc, Hibernate tự động so sánh trạng thái hiện tại của entity với snapshot ban đầu được lưu trong Persistence Context. Nếu có thay đổi, Hibernate tự sinh câu lệnh SQL `UPDATE` tương ứng khi flush.

### 7.2. Vấn đề N+1 Query & Giải pháp Triệt để

Giả sử ta có entity `Department` có quan hệ 1-N với `Employee` (`fetch = FetchType.LAZY`). Khi query lấy danh sách 100 phòng ban và duyệt qua danh sách nhân viên của từng phòng ban:

```java
List<Department> depts = deptRepo.findAll(); // 1 câu SELECT lấy 100 departments
for (Department d : depts) {
    System.out.println(d.getEmployees().size()); // 100 câu SELECT riêng lẻ vào bảng employee!
}
// Tổng cộng: 1 + 100 = 101 queries! (N+1 Problem)
```

#### 3 Giải pháp Xử lý:

1. **Sử dụng `JOIN FETCH` trong JPQL**:
```java
@Query("SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.employees")
List<Department> findAllWithEmployees();
// Hibernate 5: cần DISTINCT để loại Department bị nhân bản theo số employee.
// Hibernate 6+: tự loại trùng entity root → DISTINCT không còn bắt buộc (và không bị đẩy xuống SQL).
// ⚠️ Không JOIN FETCH 2 collection kiểu List cùng lúc → MultipleBagFetchException (dùng Set, hoặc fetch từng collection bằng 2 query).
```
2. **Sử dụng `@EntityGraph`**:
```java
@EntityGraph(attributePaths = {"employees"})
List<Department> findAll();
```
3. **Sử dụng Batch Fetching (Khắc phục triệt để khi phân trang)**:
Khi kết hợp `JOIN FETCH` với `Pageable` phân trang, Hibernate sẽ warning: *"firstResult/maxResults specified with collection fetch; applying in memory"*, dẫn đến việc kéo **toàn bộ table** vào RAM rồi mới phân trang!
**Khắc phục**: Dùng batch fetching trong `application.yml`:
```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 50 # Tự động gom 50 id bằng câu lệnh WHERE id IN (?, ?, ...)
```

### 7.3. `LazyInitializationException` & Cạm bẫy Open Session in View (OSIV)

- **Nguyên nhân**: Ngoại lệ này xảy ra khi ta cố tình truy cập vào một lazy collection sau khi Hibernate `Session` (EntityManager) đã bị đóng (thường là sau khi rời khỏi method `@Transactional` của tầng Service và ra tới tầng Controller/View).
- **Anti-pattern OSIV (`spring.jpa.open-in-view=true`)**:
  - Mặc định Spring Boot bật `open-in-view=true`. Cơ chế này giữ Hibernate Session mở xuyên suốt toàn bộ vòng đời của HTTP Request (từ Filter đến View rendering).
  - **Mối nguy hại khôn lường**: Database Connection bị chiếm dụng cho đến tận khi response JSON được serialize và gửi xong qua mạng cho client. Nếu client mạng chậm hoặc Controller gọi thêm external 3rd-party API tốn 3-5 giây, **Connection Pool (HikariCP) sẽ cạn kiệt lập tức**, khiến toàn bộ hệ thống bị nghẽn!
  - **Khuyến nghị chuẩn Production**: Luôn cấu hình `spring.jpa.open-in-view=false`. Mọi dữ liệu cần thiết phải được fetch đầy đủ ở Service layer thông qua DTO Projection hoặc `JOIN FETCH`.

### 7.4. Optimistic Locking vs Pessimistic Locking trong JPA

```java
// 1. Optimistic Locking: Dùng version column, không lock ở DB level
@Entity
public class Product {
    @Id private Long id;
    private Integer stock;

    @Version // Hibernate tự sinh: UPDATE product SET stock = ?, version = version + 1 WHERE id = ? AND version = ?
    private Long version;
}
// Nếu 2 transaction cùng update -> 1 thành công, 1 ném OptimisticLockException.

// 2. Pessimistic Locking: Khóa trực tiếp tại Database (SELECT ... FOR UPDATE)
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);
}
// Các transaction khác phải chờ (blocking) cho đến khi transaction giữ lock commit.
// Có thể đặt timeout: @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
```
- Optimistic lock thất bại → Spring dịch thành `ObjectOptimisticLockingFailureException` → thường **retry** cả nghiệp vụ (đọc lại → tính lại → ghi). So sánh chi tiết 2 cách ở `Database.md` mục 21 Q7.

### 7.5. Hiệu năng JPA/Hibernate — những điều phải biết

**1. `save()` không phải lúc nào cũng là INSERT**
```java
// SimpleJpaRepository.save():
if (entityInformation.isNew(entity)) em.persist(entity);   // INSERT
else                                 return em.merge(entity); // SELECT trước rồi UPDATE/INSERT
```
- `isNew()` mặc định dựa vào **ID == null** (hoặc `@Version` == null). Nếu bạn **tự gán ID** (UUID sinh ở app) → Spring coi là entity cũ → `merge()` → **thêm 1 câu SELECT** cho mỗi lần save. Cách sửa: implement `Persistable<ID>` (tự định nghĩa `isNew()`), hoặc dùng `@Version`.

**2. Batch insert/update**
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc.batch_size: 50          # gom 50 câu lệnh/1 round-trip
        order_inserts: true          # sắp xếp để batch theo bảng
        order_updates: true
```
- ⚠️ **`GenerationType.IDENTITY` (auto-increment) vô hiệu hóa batch insert** — Hibernate phải INSERT ngay để lấy ID. Dùng `SEQUENCE` với **pooled optimizer** (`@SequenceGenerator(allocationSize = 50)`) — Hibernate lấy 1 lần 50 ID từ sequence (PostgreSQL). MySQL không có sequence → bulk insert lớn nên dùng `JdbcTemplate.batchUpdate` + `rewriteBatchedStatements=true` (MySQL Connector/J).
- Xử lý hàng trăm nghìn entity trong 1 transaction → Persistence Context phình to (mỗi entity giữ thêm snapshot cho dirty checking) → `flush()` + `clear()` định kỳ mỗi N bản ghi, hoặc dùng `StatelessSession`/JDBC.

**3. Chỉ lấy dữ liệu cần thiết — Projection**
```java
// Interface-based projection: Spring chỉ SELECT các cột cần
interface OrderSummary { Long getId(); BigDecimal getTotal(); }
List<OrderSummary> findByCustomerId(Long customerId);

// Class/record-based (DTO) projection
@Query("select new com.example.OrderDto(o.id, o.total) from Order o where o.status = :status")
List<OrderDto> findDtos(@Param("status") Status status);
```
→ Không tạo managed entity, không dirty checking, không lazy loading bất ngờ — lựa chọn tốt nhất cho API **chỉ đọc**.

**4. `@Transactional(readOnly = true)`** cho method chỉ đọc: Hibernate đặt `FlushMode.MANUAL` & bỏ giữ snapshot dirty-checking (tiết kiệm bộ nhớ/CPU), driver có thể gửi `SET TRANSACTION READ ONLY`, và có thể dùng để **route sang read replica** (`AbstractRoutingDataSource` + `LazyConnectionDataSourceProxy`).

**5. Phân trang: `Page` vs `Slice`**
- `Page<T>` chạy **thêm 1 câu `COUNT(*)`** mỗi lần → bảng lớn rất tốn; `Slice<T>` chỉ lấy `size + 1` dòng để biết còn trang sau không → dùng cho infinite scroll. Có thể tự viết `countQuery` tối ưu.
- Offset lớn chậm → keyset pagination (xem `Database.md` 20.4). Spring Data 3.1+ có `ScrollPosition`/`Window` hỗ trợ keyset.

**6. `@Modifying` bulk query bỏ qua Persistence Context**
```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("update Product p set p.price = p.price * 1.1 where p.category = :c")
int increasePrice(@Param("c") String category);
```
- Câu UPDATE/DELETE hàng loạt chạy thẳng xuống DB → entity đang nằm trong Persistence Context **không biết** đã đổi → đọc lại vẫn ra giá trị cũ nếu không `clear`. Không kích hoạt lifecycle callback/`@Version`.

**7. `equals()/hashCode()` cho Entity**
- Không dùng Lombok `@Data` cho entity (`hashCode` dựa trên mọi field → đổi sau khi persist/khi lazy collection được load → hỏng `HashSet`, có thể kích hoạt lazy loading hoặc StackOverflow với quan hệ 2 chiều trong `toString`).
- Cách an toàn: dựa trên **business key bất biến** (natural id), hoặc dựa trên `id` nhưng `hashCode()` trả về hằng số theo class (`getClass().hashCode()`).

**8. Fetch type mặc định**: `@ManyToOne`/`@OneToOne` mặc định **EAGER** (nguồn N+1 phổ biến!) → luôn khai báo `fetch = FetchType.LAZY`; `@OneToMany`/`@ManyToMany` mặc định LAZY.

**9. Second-level cache & Query cache** (Ehcache/Caffeine/Redis qua JCache): cache entity theo id ở tầng `SessionFactory`, chia sẻ giữa các session. Hợp với dữ liệu **đọc nhiều, ít đổi** (danh mục, cấu hình). Cẩn thận khi nhiều instance (cần cache phân tán hoặc chấp nhận stale).

**10. Quan sát SQL thực sự chạy**: `spring.jpa.show-sql` chỉ in ra stdout → nên dùng `logging.level.org.hibernate.SQL=DEBUG` + `org.hibernate.orm.jdbc.bind=TRACE`, hoặc **datasource-proxy / p6spy** để đếm số query mỗi request (phát hiện N+1 trong test).

### 7.6. Khi nào KHÔNG nên dùng JPA?
- Báo cáo/analytics query phức tạp, nhiều aggregate/window function → **native SQL**, `JdbcClient` (Boot 3.2), **jOOQ** (type-safe SQL), MyBatis.
- Bulk ETL hàng triệu dòng → JDBC batch / `COPY` (PostgreSQL).
- Kết hợp được: JPA cho phần CRUD nghiệp vụ (aggregate root), JDBC/jOOQ cho phần đọc phức tạp (CQRS nhẹ).

**⚖️ JPA/Hibernate vs jOOQ/JdbcClient vs MyBatis**

| | JPA / Hibernate | jOOQ / JdbcClient (SQL tường minh) | MyBatis |
| :--- | :--- | :--- | :--- |
| Mô hình | Object graph, dirty checking | SQL type-safe / SQL thuần | SQL trong XML/annotation map vào object |
| CRUD domain phức tạp | ✅ rất nhanh | Dài hơn | Trung bình |
| Truy vấn báo cáo, SQL phức tạp | Khó, phải native query | ✅ | ✅ |
| Kiểm soát SQL thực thi | Thấp (phải bật log mới thấy) | ✅ Hoàn toàn | ✅ |
| Rủi ro hiệu năng ẩn | N+1, lazy loading | Thấp | Thấp |

✅ **Lựa chọn hay gặp:** JPA cho phần **ghi theo aggregate** (CRUD nghiệp vụ), SQL tường minh (JdbcClient/jOOQ, projection DTO) cho **đọc/báo cáo** và xử lý lô lớn — kết hợp cả hai trong một service là bình thường.

> 🛠 **Áp dụng:**
> - Mặc định mọi quan hệ `LAZY`; nạp dữ liệu cần thiết theo use case bằng `JOIN FETCH`/`@EntityGraph`; trả DTO projection cho API đọc.
> - Tắt OSIV (`spring.jpa.open-in-view=false`) để lỗi lazy loading lộ ra ở tầng service.
> - Bật log số câu SQL trong test để bắt N+1 sớm.

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao gọi `setName()` trên entity trong `@Transactional` mà không gọi `save()` vẫn cập nhật DB?**
   → Entity đang ở trạng thái **managed**; khi commit, Hibernate flush và so sánh với snapshot (dirty checking) rồi sinh `UPDATE`.
2. **Vì sao `JOIN FETCH` một collection kèm phân trang là nguy hiểm?**
   → Kết quả JOIN nhân bản dòng nên Hibernate không thể `LIMIT` ở DB; nó nạp **toàn bộ** vào bộ nhớ rồi phân trang (cảnh báo HHH90003004).
</details>


---

## 8. Quản lý Giao dịch (@Transactional Deep Dive)

> 🎯 **Bài toán:** "Trừ tiền tài khoản A, cộng tiền tài khoản B" phải **cùng thành công hoặc cùng thất bại**. Viết tay `conn.setAutoCommit(false)` / `commit` / `rollback` trong `try/catch` ở mọi method → lặp lại, dễ quên rollback, và khó gộp nhiều method vào cùng một transaction.
>
> 💡 **Ý tưởng:** **Khai báo** ranh giới transaction bằng `@Transactional`; proxy mở transaction trước method và commit/rollback sau đó. Connection của transaction được **gắn vào thread hiện tại** (`ThreadLocal` qua `TransactionSynchronizationManager`) → mọi repository trong cùng thread tự dùng chung connection đó. **Propagation** trả lời câu hỏi "method này được gọi khi đã có transaction thì làm gì?".


### 8.1. Các Loại Propagation trong Spring

| Propagation | Hành vi |
| :--- | :--- |
| `REQUIRED` (Default) | Nếu đang có transaction, tham gia vào nó. Nếu chưa có, tạo transaction mới. |
| `REQUIRES_NEW` | Luôn luôn tạo một transaction độc lập mới. Transaction hiện tại (nếu có) sẽ bị tạm ngưng (suspend). Thích hợp cho ghi Audit log độc lập (Audit log phải lưu kể cả khi nghiệp vụ chính rollback). |
| `NESTED` | Chạy bên trong một transaction lồng nhau sử dụng Database Savepoint. Nếu transaction con lỗi, chỉ rollback về savepoint mà không làm hỏng transaction cha. |
| `MANDATORY` | Bắt buộc phải có transaction đang hoạt động. Nếu gọi khi không có transaction -> ném ngoại lệ `IllegalTransactionStateException`. |
| `SUPPORTS` | Nếu có transaction thì chạy cùng, không có thì chạy không transaction. |
| `NOT_SUPPORTED` | Luôn chạy không transaction. Nếu có transaction đang chạy, tạm ngưng nó. |
| `NEVER` | Bắt buộc chạy không có transaction. Nếu có transaction đang chạy -> ném ngoại lệ. |

### 8.2. Quy tắc Rollback mặc định & RollbackFor

> ⚠️ **Lưu ý sống còn**: Mặc định Spring `@Transactional` **CHỈ ROLLBACK đối với Unchecked Exceptions** (`RuntimeException` và `Error`).
> Nếu method ném ra **Checked Exception** (`IOException`, `SQLException`, custom checked exception), Spring **VẪN COMMIT TRANSACTION BÌNH THƯỜNG**!
> 
> **Thực hành chuẩn**: Luôn luôn khai báo tường minh:
> ```java
> @Transactional(rollbackFor = Exception.class)
> ```

### 8.3. Các bẫy `@Transactional` hay gặp (Senior interview)

| # | Bẫy | Hậu quả | Cách xử lý |
|---|---|---|---|
| 1 | Gọi method `@Transactional` **trong cùng class** (`this.method()`) | Không qua proxy → không có transaction | Tách sang bean khác (xem 4.3) |
| 2 | `@Transactional` trên method **`private`** / `final` / `static` | Proxy không intercept được → bị bỏ qua (không báo lỗi; Spring 6 hỗ trợ thêm method `protected`/package-private với class-based proxy) | Đặt trên method `public` |
| 3 | Bắt exception bên trong rồi không ném lại | Spring không biết có lỗi → **commit** | Ném lại, hoặc `TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()` |
| 4 | Ném **checked exception** | Mặc định **commit** (8.2) | `rollbackFor = Exception.class` |
| 5 | Method con `REQUIRED` ném RuntimeException, method cha **bắt lại** và chạy tiếp | Transaction đã bị đánh dấu **rollback-only** → khi cha commit: **`UnexpectedRollbackException: Transaction silently rolled back because it has been marked as rollback-only`** | Không nuốt lỗi; hoặc cho method con `REQUIRES_NEW`/`NESTED` nếu thật sự muốn lỗi con không ảnh hưởng cha |
| 6 | Gọi HTTP/API bên ngoài, gửi email, publish Kafka **bên trong** transaction | Giữ DB connection + lock trong lúc chờ mạng → cạn pool; nếu transaction rollback thì email/message **đã gửi rồi** | Gọi ngoài transaction; dùng `@TransactionalEventListener(AFTER_COMMIT)` hoặc **Transactional Outbox** (SystemDesign 3.6) |
| 7 | `@Async` + `@Transactional` | Method async chạy trên **thread khác** → không dùng chung transaction của caller (transaction gắn với ThreadLocal) | Thiết kế để mỗi bên tự quản transaction |
| 8 | Transaction quá dài (xử lý hàng loạt trong 1 tx) | Lock lâu, undo/WAL lớn, replica lag, VACUUM bị chặn | Chia batch, mỗi batch 1 transaction |
| 9 | `REQUIRES_NEW` lồng nhau nhiều tầng | Mỗi tầng chiếm thêm **1 connection** (tx ngoài bị suspend nhưng vẫn giữ connection) → dễ **deadlock pool** khi pool nhỏ | Hạn chế, tăng pool phù hợp |
| 10 | Nhiều DataSource / transaction manager | `@Transactional` dùng nhầm manager mặc định | `@Transactional(transactionManager = "orderTxManager")`; giao dịch 2 DB không atomic — cần Saga/Outbox |

### 8.4. Programmatic Transaction & Transaction Events
```java
// Khi cần kiểm soát ranh giới transaction chi tiết (vd: xử lý từng batch trong vòng lặp)
@Service
public class BatchImporter {
    private final TransactionTemplate tx;
    public BatchImporter(PlatformTransactionManager tm) { this.tx = new TransactionTemplate(tm); }

    public void importAll(List<List<Row>> batches) {
        for (List<Row> batch : batches) {
            tx.executeWithoutResult(status -> repository.saveAll(map(batch)));  // mỗi batch 1 transaction
        }
    }
}

// Chỉ chạy SAU KHI transaction commit thành công (gửi email, publish event ra ngoài, xóa cache)
@Component
class OrderEventsHandler {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(OrderPlacedEvent e) { emailService.sendConfirmation(e.orderId()); }
}
```
- ⚠️ Trong listener `AFTER_COMMIT`, transaction cũ đã kết thúc → muốn ghi DB thêm phải mở transaction mới (`REQUIRES_NEW`). Nếu ứng dụng crash ngay sau commit, listener không chạy → việc quan trọng phải dùng Outbox.

### 8.5. Cách Spring quản lý transaction bên dưới
```
Proxy (TransactionInterceptor)
  → PlatformTransactionManager (DataSourceTransactionManager / JpaTransactionManager / JtaTransactionManager)
     → lấy Connection từ pool, setAutoCommit(false), set isolation/readOnly
     → bind Connection/EntityManager vào ThreadLocal (TransactionSynchronizationManager)
  → gọi method thật (repository dùng đúng Connection đang bind)
  → thành công: commit / RuntimeException: rollback → trả Connection về pool
```
- Vì transaction gắn với **ThreadLocal** → đổi thread (`@Async`, `CompletableFuture`, parallel stream, reactive) là mất transaction. WebFlux/R2DBC dùng `ReactiveTransactionManager` (lưu trong Reactor Context).

> 🛠 **Áp dụng:**
> - Đặt `@Transactional` ở **tầng service** (một use case = một transaction); `readOnly = true` cho truy vấn.
> - **Không gọi HTTP/Kafka bên trong transaction** — giữ connection DB trong lúc chờ mạng làm cạn pool; dùng outbox hoặc `@TransactionalEventListener(AFTER_COMMIT)`.
> - Checked exception cần rollback → `rollbackFor = Exception.class`.
> - Ghi log kiểm toán phải còn lại dù nghiệp vụ rollback → `REQUIRES_NEW` (ở **bean khác**).

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao `@Transactional` không có tác dụng với code chạy trong `CompletableFuture.supplyAsync` bên trong method?**
   → Transaction gắn với **thread**; code trên thread khác không thấy connection/transaction đó.
2. **Method con `REQUIRED` ném exception, method cha bắt exception và return bình thường → kết quả?**
   → `UnexpectedRollbackException`: method con đã đánh dấu transaction chung **rollback-only**, cha cố commit thì bị từ chối.
</details>


---

## 9. Spring Security Architecture & JWT Stateless Flow

> 🎯 **Bài toán:** Mọi request cần được xác định **là ai** (authentication) và **được làm gì** (authorization) — trước khi chạm tới controller, và nhất quán cho mọi endpoint.
>
> 💡 **Ý tưởng:** Một **chuỗi Filter** đứng trước `DispatcherServlet`: mỗi filter một nhiệm vụ (đọc token, xác thực, xử lý lỗi bảo mật, kiểm tra quyền). Kết quả xác thực đặt vào `SecurityContext` (gắn với thread) để code phía sau dùng.
>
> ⚖️ **Session vs JWT:** Session (state trên server/Redis) thu hồi tức thì, cookie an toàn hơn với XSS, nhưng cần store dùng chung khi scale. JWT (stateless, tự chứa thông tin) không cần tra store mỗi request và hợp với nhiều service/mobile, nhưng **khó thu hồi** → access token ngắn hạn + refresh token. ✅ Chọn theo bối cảnh: web app một domain → session thường đơn giản và an toàn hơn; API cho nhiều client/microservice → JWT (thường do một Authorization Server phát hành, service chỉ làm *resource server*).


### 9.1. Kiến trúc Chuỗi Lọc (Security Filter Chain)

```
Servlet Request
      │
      ▼
DelegatingFilterProxy (Cầu nối giữa Servlet container và Spring ApplicationContext)
      │
      ▼
FilterChainProxy (Quản lý SecurityFilterChains)
      │
      ▼
SecurityFilterChain (thứ tự rút gọn, Spring Security 6):
  ├── SecurityContextHolderFilter        (nạp SecurityContext — thay SecurityContextPersistenceFilter)
  ├── HeaderWriterFilter                 (X-Frame-Options, HSTS, X-Content-Type-Options...)
  ├── CorsFilter
  ├── CsrfFilter
  ├── LogoutFilter
  ├── JwtAuthenticationFilter            (custom, kế thừa OncePerRequestFilter) / BearerTokenAuthenticationFilter (resource server)
  ├── UsernamePasswordAuthenticationFilter
  ├── AnonymousAuthenticationFilter      (chưa xác thực → gán AnonymousAuthenticationToken)
  ├── ExceptionTranslationFilter         (AuthenticationException → 401 qua AuthenticationEntryPoint,
  │                                       AccessDeniedException → 403 qua AccessDeniedHandler)
  └── AuthorizationFilter                (thay FilterSecurityInterceptor — kiểm tra quyền theo URL)
```
- Có thể có **nhiều `SecurityFilterChain`** (vd `/api/**` stateless JWT, `/admin/**` form login) — `FilterChainProxy` chọn chain **đầu tiên khớp** (`securityMatcher`), nên thứ tự `@Order` quan trọng.
- **Authentication flow** bên trong: Filter tạo `Authentication` chưa xác thực → `AuthenticationManager` (`ProviderManager`) → duyệt các `AuthenticationProvider` (vd `DaoAuthenticationProvider` dùng `UserDetailsService` + `PasswordEncoder`) → trả `Authentication` đã xác thực → lưu vào `SecurityContextHolder` (ThreadLocal).
- **Method security**: `@EnableMethodSecurity` → `@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")`, `@PostAuthorize`, `@PreFilter/@PostFilter` (dựa trên AOP proxy → cũng dính bẫy self-invocation).

### 9.2. Luồng Xác thực Stateless JWT Hoàn Chỉnh

> ✅ **Khuyến nghị trước**: nếu token được phát bởi một Authorization Server (Keycloak, Auth0, Okta, Spring Authorization Server, Cognito...), **không cần tự viết JWT filter** — dùng OAuth2 Resource Server có sẵn (tự lấy public key qua JWKS, kiểm tra chữ ký, `exp`, `iss`, `aud`):
> ```java
> http.oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()));
> // application.yml: spring.security.oauth2.resourceserver.jwt.issuer-uri: https://auth.example.com/realms/demo
> ```
> Tự viết filter như dưới đây chỉ khi tự phát hành token (hệ thống nhỏ) — và cần tự lo: thuật toán ký (tránh `alg=none`, cố định thuật toán), xoay vòng khóa, thời hạn ngắn + refresh token, thu hồi token.

```java
// 1. Custom JWT Authentication Filter
// ⚠️ KHÔNG đánh dấu @Component cho filter này nếu đã add vào SecurityFilterChain:
//    Spring Boot tự đăng ký MỌI bean kiểu Filter vào servlet container → filter chạy 2 lần
//    (1 lần ngoài chain, áp dụng cả cho URL permitAll). Nếu muốn là bean, tắt đăng ký tự động:
//    @Bean FilterRegistrationBean<JwtAuthenticationFilter> reg(JwtAuthenticationFilter f) {
//        var r = new FilterRegistrationBean<>(f); r.setEnabled(false); return r; }
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsername(token);
            // Lưu ý: tra DB mỗi request làm mất một phần lợi ích "stateless" của JWT.
            // Có thể dựng Authentication trực tiếp từ claims (roles) trong token, hoặc cache UserDetails.
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Đặt Authentication vào SecurityContextHolder của Thread hiện tại
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
```

```java
// 2. Cấu hình SecurityFilterChain trong Spring Boot 3
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Cho phép @PreAuthorize("hasRole('ADMIN')")
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Tạo filter thủ công (không phải bean) → chỉ chạy bên trong SecurityFilterChain
        var jwtFilter = new JwtAuthenticationFilter(tokenProvider, userDetailsService);
        return http
            .csrf(csrf -> csrf.disable()) // An toàn CHỈ KHI token gửi qua header Authorization (browser không tự đính kèm).
                                          // Nếu lưu token/session trong COOKIE → vẫn phải bật CSRF (hoặc SameSite + CSRF token)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/actuator/health").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // Lưu dạng "{bcrypt}$2a$10$..." → đổi thuật toán sau này mà không phải migrate ngay toàn bộ
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
```

### 9.3. Những điểm bảo mật hay bị hỏi
- **Lưu mật khẩu**: hash **chậm có salt** — BCrypt, SCrypt, **Argon2**, PBKDF2. Không bao giờ MD5/SHA-256 trần (quá nhanh → brute-force bằng GPU).
- **Lưu JWT ở đâu phía browser?** `localStorage` dễ bị đánh cắp qua XSS; cookie `HttpOnly; Secure; SameSite` an toàn với XSS hơn nhưng phải chống CSRF. Pattern phổ biến: access token ngắn hạn trong bộ nhớ + refresh token trong cookie HttpOnly; hoặc **BFF (Backend-for-Frontend)** giữ token phía server.
- **Thu hồi JWT** (logout, đổi mật khẩu, bị lộ): JWT stateless nên không "xóa" được → thời hạn ngắn (5–15 phút) + refresh token có thể thu hồi (lưu DB/Redis, xoay vòng — *refresh token rotation*), hoặc denylist `jti` trong Redis tới khi hết hạn.
- **CORS** không phải cơ chế bảo mật phía server — chỉ là trình duyệt tự giới hạn; cấu hình `allowedOrigins` cụ thể, không dùng `*` cùng `allowCredentials(true)`.
- **401 vs 403**: 401 = chưa xác thực/không xác định được bạn là ai; 403 = đã biết bạn là ai nhưng không có quyền.
- **Multi-tenant / phân quyền dữ liệu**: kiểm tra quyền sở hữu tài nguyên (IDOR — `GET /orders/123` của người khác) ở tầng service, không chỉ ở URL.

---

## 10. Spring Kafka: Cấu hình, Producer, Consumer & Manual Partition Assignment

> 🎯 **Bài toán:** Gọi đồng bộ dây chuyền (Order → Payment → Inventory → Email) khiến một service chậm/chết kéo sập cả chuỗi; tải đột biến dồn thẳng vào service yếu nhất.
>
> 💡 **Ý tưởng:** Giao tiếp qua **log sự kiện bền vững** (Kafka): producer ghi rồi đi tiếp, consumer xử lý theo tốc độ của mình, có thể đọc lại. Phần khó chuyển sang: **giao nhận ít nhất một lần** → consumer phải **idempotent**; thứ tự chỉ được đảm bảo **trong một partition** → chọn key đúng; xử lý lỗi bằng retry + DLT.


> 📌 Phần này giải đáp trực tiếp bài toán được ghi chú trong file `kafka-note`: *"Làm sao setup manual topic partition in spring java"*.

### 10.1. Kiến trúc Spring for Apache Kafka

Spring Kafka cung cấp abstraction cấp cao thông qua `KafkaTemplate` (phía Producer) và `@KafkaListener` / `MessageListenerContainer` (phía Consumer).

```
Producer:  App ──► KafkaTemplate.send() ──► Kafka Cluster (Topic & Partitions)
Consumer:  Kafka Cluster ──► ListenerContainer ──► @KafkaListener (Message Consumer)
```

### 10.2. Producer Cấu hình & Idempotent Producer

```java
@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // 🛡️ Cấu hình Idempotent & Chống mất tin nhắn chuẩn Enterprise:
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Chống trùng do retry (broker loại trùng theo ProducerId + sequence number)
        props.put(ProducerConfig.ACKS_CONFIG, "all");              // Chờ đủ Leader + ISR replicate (kết hợp min.insync.replicas=2 phía topic)
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120_000); // Tổng thời gian cho phép retry — nên cấu hình cái này thay vì số lần retries
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5); // ≤ 5 vẫn giữ thứ tự khi bật idempotence
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);             // Chờ vài ms để gom batch → throughput cao hơn
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "zstd");
        // 📌 Từ Kafka 3.0, mặc định đã là enable.idempotence=true, acks=all, retries=Integer.MAX_VALUE
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

---

### 10.3. Manual Topic & Partition Assignment (Thiết lập Gán Partition Thủ công)

Trong Apache Kafka, mặc định khi các Consumer tham gia vào một **Consumer Group**, cơ chế **Group Rebalance Protocol** sẽ tự động phân bổ các partition trong topic cho các consumer (Dynamic Partition Assignment).

Tuy nhiên, trong các bài toán đặc thù:
- Bạn muốn consumer chỉ đọc từ **một partition cố định** (e.g. Partition 0 chuyên xử lý VIP customer, Partition 1 chuyên xử lý standard).
- Bạn muốn chủ động quản lý offset và replay tin nhắn từ một partition cụ thể.
- Hệ thống không muốn phụ thuộc vào Group Coordinator và tránh Rebalance penalty.

#### Cách 1: Sử dụng `@TopicPartition` và `@PartitionOffset` trong `@KafkaListener` (Khai báo trực tiếp)

Đây là cách phổ biến và tiện lợi nhất trong Spring Kafka:

```java
@Component
public class ManualPartitionListener {

    // 1. Chỉ định nghe cố định partition 0 và 1 của topic "orders"
    @KafkaListener(
        id = "manualOrderListener",
        topicPartitions = @TopicPartition(
            topic = "orders", 
            partitions = {"0", "1"}
        )
    )
    public void listenSpecificPartitions(ConsumerRecord<String, String> record) {
        System.out.printf("Nhận tin từ partition %d, offset %d: key=%s, val=%s%n",
                record.partition(), record.offset(), record.key(), record.value());
    }

    // 2. Chỉ định nghe từ Partition 2 VÀ bắt đầu từ một Offset xác định (hoặc từ đầu/cuối)
    @KafkaListener(
        id = "seekPartitionListener",
        topicPartitions = @TopicPartition(
            topic = "orders",
            partitionOffsets = {
                @PartitionOffset(partition = "2", initialOffset = "100"), // Đọc từ offset 100
                @PartitionOffset(partition = "3", initialOffset = "0")    // Replay từ đầu partition 3
            }
        )
    )
    public void listenWithInitialOffset(ConsumerRecord<String, String> record) {
        System.out.println("Processing replay message: " + record.value());
    }
}
```

#### Cách 2: Programmatic Manual Assignment bằng `ConsumerSeekAware`

Khi bạn cần dynamic gán partition hoặc seek offset dựa trên điều kiện lúc runtime:

```java
@Component
public class DynamicSeekConsumer implements ConsumerSeekAware {

    @KafkaListener(
        id = "dynamicConsumer",
        topicPartitions = @TopicPartition(topic = "payment-events", partitions = {"0"})
    )
    public void listen(String message) {
        System.out.println("Payment message: " + message);
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        assignments.forEach((topicPartition, offset) -> {
            System.out.printf("Được gán partition %s, offset hiện tại: %d%n", topicPartition, offset);
            
            // Replay: Tự động tua ngược lại 50 offset trước đó khi vừa connect:
            long targetOffset = Math.max(0, offset - 50);
            callback.seek(topicPartition.topic(), topicPartition.partition(), targetOffset);
        });
    }
}
```

#### Cách 3: Tạo thủ công `ConcurrentMessageListenerContainer` không dùng `@KafkaListener`

```java
@Configuration
public class ManualContainerConfig {

    @Bean
    public ConcurrentMessageListenerContainer<String, String> manualListenerContainer(
            ConsumerFactory<String, String> consumerFactory) {

        // Chỉ định topic và partition thủ công ở cấp ContainerProperties
        ContainerProperties containerProps = new ContainerProperties(
                new TopicPartitionOffset("sensor-data", 0),
                new TopicPartitionOffset("sensor-data", 1)
        );

        containerProps.setMessageListener((MessageListener<String, String>) record -> {
            System.out.printf("Thủ công nhận: partition=%d, msg=%s%n", record.partition(), record.value());
        });

        // Tùy chỉnh AckMode commit thủ công (Manual Immediate Ack)
        containerProps.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return new ConcurrentMessageListenerContainer<>(consumerFactory, containerProps);
    }
}
```

> ⚠️ **Lưu ý khi dùng manual assignment** (cả 3 cách trên):
> - Không có **rebalance** → nếu instance chết, partition của nó **không ai đọc** cho tới khi instance sống lại (không tự failover). Chạy nhiều instance cùng cấu hình = đọc **trùng** cùng partition.
> - `group.id` vẫn được dùng để **commit offset** (nếu có), nhưng không tham gia điều phối group.
> - Phù hợp: replay/debug, công cụ admin, xử lý theo "sticky" partition do hệ thống bên ngoài điều phối (vd K8s StatefulSet: pod-0 đọc partition 0...). Với xử lý nghiệp vụ thông thường → dùng consumer group + `concurrency`.

### 10.4. Tạo Topic với số Partition mong muốn & gửi message vào Partition cụ thể

> Cách hiểu thứ hai của câu hỏi trong `kafka-note`: *"setup topic với N partition trong Spring"* và *"chủ động chọn partition khi gửi"*.

```java
@Configuration
public class KafkaTopicConfig {
    // KafkaAdmin (Spring Boot auto-config) tự tạo topic khi khởi động nếu chưa tồn tại.
    // Nếu topic đã tồn tại với ít partition hơn → KafkaAdmin sẽ TĂNG số partition (không bao giờ giảm được).
    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name("orders")
                .partitions(6)                       // số partition = mức song song tối đa của 1 consumer group
                .replicas(3)                         // replication factor
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(7).toMillis()))
                .build();
    }
}
```

```java
// Gửi message — 3 cách quyết định partition:
kafkaTemplate.send("orders", orderId, payload);          // (1) Theo KEY: murmur2(key) % partitions → cùng key luôn cùng partition (giữ thứ tự theo key)
kafkaTemplate.send("orders", 2, orderId, payload);       // (2) Chỉ định PARTITION trực tiếp (partition = 2)
kafkaTemplate.send("orders", payload);                   // (3) Không key: sticky partitioner — gom batch vào 1 partition rồi đổi (Kafka 2.4+)

// (4) Custom Partitioner: vd khách VIP → partition 0, còn lại hash vào 1..N-1
public class VipPartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        int n = cluster.partitionCountForTopic(topic);
        if (n == 1 || (value instanceof OrderEvent e && e.vip())) return 0;
        return 1 + Utils.toPositive(Utils.murmur2(keyBytes)) % (n - 1);
    }
    @Override public void close() {}
    @Override public void configure(Map<String, ?> configs) {}
}
// props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, VipPartitioner.class);

// send() là bất đồng bộ — xử lý kết quả (Spring Kafka 3.x trả CompletableFuture)
kafkaTemplate.send("orders", orderId, payload).whenComplete((result, ex) -> {
    if (ex != null) log.error("Send failed", ex);
    else log.info("Sent to partition {} offset {}", result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
});
```
- ⚠️ **Tăng số partition làm thay đổi ánh xạ `hash(key) % N`** → message mới của cùng key có thể sang partition khác → mất thứ tự trong giai đoạn chuyển tiếp. Nên chọn dư số partition từ đầu (vd 12, 24) cho topic quan trọng.
- Chọn số partition: ≥ số consumer tối đa muốn chạy song song; ước lượng `max(throughput_mong_muốn / throughput_1_partition_phía_producer, ... / phía_consumer)`.

### 10.5. Consumer: cấu hình, commit offset & xử lý lỗi

```yaml
spring:
  kafka:
    consumer:
      group-id: order-service
      auto-offset-reset: earliest       # group mới / offset hết hạn → đọc từ đầu (latest = chỉ đọc message mới)
      enable-auto-commit: false         # để Spring container quản lý commit
      max-poll-records: 200
      properties:
        max.poll.interval.ms: 300000    # xử lý 1 batch poll lâu hơn ngưỡng này → bị coi là chết → rebalance
        isolation.level: read_committed # chỉ đọc message của transaction đã commit (khi producer dùng transaction)
    listener:
      ack-mode: record                  # hoặc batch (mặc định), manual, manual_immediate
      concurrency: 3                    # 3 consumer thread trong 1 instance (≤ số partition, phần dư sẽ idle)
```

```java
@KafkaListener(topics = "orders", groupId = "order-service")
public void onOrder(ConsumerRecord<String, OrderEvent> record, Acknowledgment ack) {   // cần ack-mode: manual
    process(record.value());   // nên IDEMPOTENT: cùng message xử lý 2 lần không sinh tác dụng phụ 2 lần
    ack.acknowledge();         // commit offset SAU khi xử lý xong → at-least-once
}

// Retry + Dead Letter Topic (DLT)
@Bean
public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
    var recoverer = new DeadLetterPublishingRecoverer(template);          // gửi sang "orders.DLT" sau khi hết retry
    var handler = new DefaultErrorHandler(recoverer, new ExponentialBackOff(1000L, 2.0));
    handler.addNotRetryableExceptions(ValidationException.class);           // lỗi dữ liệu → vào DLT ngay, không retry
    return handler;
}
```
- **Non-blocking retry** (`@RetryableTopic`): message lỗi được chuyển sang các topic `orders-retry-0`, `orders-retry-1`... với delay tăng dần, partition chính **không bị chặn**; đổi lại mất thứ tự giữa message lỗi và message sau nó.
- **Poison pill** (message không deserialize được) → dùng `ErrorHandlingDeserializer` bọc deserializer thật, nếu không consumer sẽ lặp lỗi mãi ở cùng offset.
- **Delivery semantics**:
    | Semantic | Cách đạt được |
    |---|---|
    | At-most-once | Commit offset **trước** khi xử lý (lỗi giữa chừng → mất message) |
    | **At-least-once** (phổ biến) | Commit **sau** khi xử lý + consumer **idempotent** (bảng `processed_message_id` / unique constraint / upsert) |
    | Exactly-once | Kafka transactions (`transactional.id` + `isolation.level=read_committed`) cho luồng **Kafka → xử lý → Kafka** (consume-transform-produce). Với side effect ra DB ngoài → vẫn cần idempotency/Outbox |
- **Thứ tự**: Kafka chỉ đảm bảo thứ tự **trong 1 partition**. Muốn các event của 1 order đúng thứ tự → dùng `orderId` làm key, xử lý tuần tự trong partition (không đẩy sang thread pool bất đồng bộ không có thứ tự).
- **Rebalance**: Kafka 3.x dùng `CooperativeStickyAssignor` (rebalance tăng dần, không dừng cả group); Kafka 4.0 có giao thức consumer group mới (KIP-848) chuyển việc điều phối về broker → rebalance nhanh và ít gián đoạn hơn. Static membership (`group.instance.id`) tránh rebalance khi pod chỉ restart nhanh.

> 🛠 **Áp dụng:**
> - Key = ID thực thể cần giữ thứ tự (orderId); số partition ≥ số consumer tối đa muốn chạy song song.
> - Producer: `acks=all` + idempotence; consumer: commit offset **sau khi xử lý xong**, xử lý idempotent (bảng processed_message hoặc upsert).
> - Ghi DB + gửi sự kiện nhất quán → **Transactional Outbox** (Q10, `SystemDesign.md` 11).

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao exactly-once của Kafka không giải quyết việc gọi API bên ngoài hai lần?**
   → Kafka transaction chỉ bao trùm read–process–write **trong Kafka**; tác dụng phụ ra ngoài (DB khác, HTTP) vẫn có thể lặp lại khi retry → vẫn cần idempotency.
</details>


---

## 11. Xử lý Bất đồng bộ, Scheduling & Virtual Threads (Java 21)

> 🎯 **Bài toán:** Một số việc không cần làm trong luồng request (gửi email, ghi thống kê) hoặc cần chạy định kỳ (dọn dữ liệu, đối soát).
>
> 💡 **Ý tưởng:** `@Async` chuyển việc sang **thread pool khác** qua proxy; `@Scheduled` chạy theo lịch. Hai bẫy lớn: (1) pool mặc định/không giới hạn → mất kiểm soát tài nguyên, mất việc khi tắt ứng dụng; (2) chạy nhiều instance thì job định kỳ chạy **nhiều lần** → cần khóa phân tán (ShedLock) hoặc bộ lập lịch riêng.
>
> ✅ **Vì sao việc quan trọng nên đi qua message queue thay vì `@Async`:** `@Async` chỉ nằm trong bộ nhớ — pod restart là mất việc; queue bền vững, retry được, và tách được tải.


### 11.1. `@Async` & Tối ưu ThreadPoolTaskExecutor

> ⚠️ **Executor mặc định của `@Async` phụ thuộc môi trường**:
> - **Spring Framework thuần** (không Boot): nếu không có bean `TaskExecutor` nào → dùng `SimpleAsyncTaskExecutor` — tạo **thread mới cho mỗi task**, không tái sử dụng → có thể cạn tài nguyên (`OutOfMemoryError: unable to create native thread`).
> - **Spring Boot** (2.1+): auto-config tạo sẵn bean `applicationTaskExecutor` là `ThreadPoolTaskExecutor` với **core = 8, max = không giới hạn, queue = không giới hạn** (`spring.task.execution.pool.*`) → không bùng nổ thread nhưng **hàng đợi vô hạn** có thể ăn hết heap khi task đến nhanh hơn xử lý, và max-size vô nghĩa (thread chỉ tăng quá core khi queue đầy).
> - **Spring Boot 3.2+ với virtual threads bật**: dùng `SimpleAsyncTaskExecutor` chạy trên virtual thread.
> → Production nên tự cấu hình executor **có giới hạn queue** và rejection policy rõ ràng, đặt tên thread để dễ debug; tách executor riêng cho từng loại tác vụ (bulkhead).

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "applicationTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);          // Số thread luôn duy trì
        executor.setMaxPoolSize(50);           // Số thread tối đa khi queue đầy
        executor.setQueueCapacity(200);        // Hàng đợi công việc
        executor.setThreadNamePrefix("async-exec-");
        
        // Khi queue đầy và maxPoolSize chạm trần:
        // CallerRunsPolicy: Bắt thread đang gọi (Tomcat thread) tự thực thi tác vụ đó
        // Giúp tự động throttle làm chậm client lại, tránh drop job.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setTaskDecorator(runnable -> {             // truyền MDC (traceId) / SecurityContext sang thread async
            var mdc = MDC.getCopyOfContextMap();
            return () -> {
                try { if (mdc != null) MDC.setContextMap(mdc); runnable.run(); }
                finally { MDC.clear(); }
            };
        });
        executor.initialize();
        return executor;
    }

    // Exception từ method @Async trả về void sẽ "biến mất" nếu không có handler
    @Bean
    public AsyncUncaughtExceptionHandler asyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.error("Async {} failed", method.getName(), ex);
    }
}
```
- Method `@Async` nên trả về `void` hoặc `CompletableFuture<T>`; cùng bẫy **self-invocation** như `@Transactional`; không kế thừa transaction/SecurityContext/MDC của caller (cần `TaskDecorator` hoặc `DelegatingSecurityContextAsyncTaskExecutor`).

### 11.2. Virtual Threads trong Spring Boot 3 + Java 21

Khi ứng dụng chạy Java 21 và Spring Boot 3.2+:
```yaml
spring:
  threads:
    virtual:
      enabled: true
```
**Tác động**:
- Tomcat request processing threads tự động chuyển sang sử dụng Virtual Threads.
- Mọi lời gọi `@Async` mặc định chuyển sang dùng `SimpleAsyncTaskExecutor` dạng Virtual Threads.
- Không còn giới hạn 200 concurrency threads của Tomcat Platform Threads. Khi thread thực hiện I/O blocking (gọi DB hoặc REST client), virtual thread tự động "unmount" khỏi Carrier OS Thread, giải phóng CPU để xử lý hàng chục ngàn kết nối đồng thời với lượng RAM tối thiểu.
- `@Scheduled`, Kafka listener container, `RestClient`... cũng được chuyển sang virtual threads khi bật cờ này (tùy phiên bản).

**Lưu ý khi bật virtual threads**:
1. **Giới hạn đã dời sang tài nguyên khác**: trước đây 200 Tomcat thread vô tình đóng vai trò "van" giới hạn số request đồng thời chạm DB. Bỏ van đó → hàng nghìn request cùng tranh **20 connection HikariCP** → `connection-timeout`. Cần giới hạn concurrency rõ ràng (Semaphore, rate limit, bulkhead) và đặt timeout hợp lý.
2. **Pinning** trên Java 21–23 với code dùng `synchronized` quanh I/O (một số driver/thư viện cũ) → nên chạy **Java 24+/25** (xem `Java.md` 11.1). Theo dõi JFR event `jdk.VirtualThreadPinned`.
3. ThreadLocal nặng (cache trong ThreadLocal) × số lượng virtual thread lớn = tốn bộ nhớ.
4. CPU-bound workload không được lợi gì.

### 11.3. Scheduling & Distributed Scheduling

```java
@EnableScheduling
@Configuration class SchedulingConfig {}

@Component
class ReportJob {
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Ho_Chi_Minh")   // 02:00 mỗi ngày (6 trường: giây phút giờ ngày tháng thứ)
    @SchedulerLock(name = "dailyReport", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M") // ShedLock (cần @EnableSchedulerLock + bean LockProvider)
    public void generateDailyReport() { ... }

    @Scheduled(fixedDelay = 5_000)   // 5s sau khi lần trước KẾT THÚC (fixedRate: 5s tính từ lúc lần trước BẮT ĐẦU)
    public void pollOutbox() { ... }
}
```
- ⚠️ Mặc định mọi `@Scheduled` chạy trên **1 thread duy nhất** → 1 job chậm làm trễ tất cả job khác → tăng `spring.task.scheduling.pool.size`.
- ⚠️ **Chạy nhiều instance** (K8s scale 3 pod) → job chạy **3 lần**. Giải pháp: **ShedLock** (khóa qua bảng DB/Redis — chỉ instance lấy được lock mới chạy), Quartz clustered mode (bảng `QRTZ_*`), hoặc tách job ra K8s `CronJob`/workflow engine (Temporal, Airflow).
- Job phải **idempotent** và chịu được chạy lại (lock hết hạn giữa chừng, pod bị kill).

---

## 12. Production Observability: Spring Boot Actuator, Metrics & Tracing

> 🎯 **Bài toán:** Production chậm/lỗi lúc 2 giờ sáng; không có debugger, không thể tái hiện. Cần trả lời: *có chuyện gì không? ở đâu? vì sao?*
>
> 💡 **Ý tưởng — ba trụ cột:** **Metrics** (con số tổng hợp rẻ, để cảnh báo: rate, error, latency — RED), **Logs** (chi tiết từng sự kiện), **Traces** (theo dấu một request qua nhiều service). Nối chúng bằng **traceId** để đi từ biểu đồ bất thường → trace chậm → log lỗi. Spring Boot cung cấp sẵn qua Actuator + Micrometer + OpenTelemetry.


### 12.1. Cấu hình Actuator An toàn cho Production

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus # ⚠️ Không bao giờ expose 'env' hoặc 'heapdump' ra public!
  endpoint:
    health:
      show-details: when-authorized # Chỉ hiển thị chi tiết DB, Disk khi có quyền
      probes:
        enabled: true               # Kích hoạt Kubernetes Liveness và Readiness Probes
```

### 12.2. Custom Health Indicator

```java
@Component
public class ExternalPaymentGatewayHealthIndicator implements HealthIndicator {

    private final RestClient restClient;

    public ExternalPaymentGatewayHealthIndicator(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://payment-provider.com").build();
    }

    @Override
    public Health health() {
        try {
            ResponseEntity<Void> response = restClient.get().uri("/ping").retrieve().toBodilessEntity();
            if (response.getStatusCode().is2xxSuccessful()) {
                return Health.up().withDetail("provider", "Stripe Gateway OK").build();
            }
        } catch (Exception ex) {
            return Health.down(ex).withDetail("error", "Payment Gateway Unreachable").build();
        }
        return Health.down().build();
    }
}
```
> ⚠️ **Liveness vs Readiness** trên Kubernetes:
> - **Liveness** = "process còn sống không, có cần restart không?" → **KHÔNG** đưa kiểm tra DB/dịch vụ ngoài vào liveness — DB chậm 30s sẽ khiến K8s **restart hàng loạt pod** (thảm họa dây chuyền) mà không giải quyết được gì.
> - **Readiness** = "có nhận traffic được chưa?" → có thể bao gồm dependency thiết yếu; fail → pod bị rút khỏi Service nhưng không bị restart.
> - Health check gọi ra ngoài (như ví dụ trên) nên có **timeout ngắn và cache kết quả**, nếu không chính health endpoint trở thành điểm nghẽn. Spring Boot cho phép nhóm health: `management.endpoint.health.group.readiness.include=readinessState,db`.
> - **Startup probe** cho ứng dụng khởi động chậm, tránh liveness giết pod trước khi khởi động xong.

### 12.3. Metrics với Micrometer
```java
@Service
class CheckoutService {
    private final Counter ordersPlaced;
    private final Timer paymentTimer;

    CheckoutService(MeterRegistry registry) {
        this.ordersPlaced = Counter.builder("orders.placed").tag("channel", "web").register(registry);
        this.paymentTimer = Timer.builder("payment.duration")
                .publishPercentileHistogram()           // xuất histogram bucket → tính p95/p99 ở Prometheus
                .register(registry);
    }

    void checkout(Order o) {
        paymentTimer.record(() -> paymentClient.charge(o));
        ordersPlaced.increment();
    }
}
```
- Spring Boot tự cung cấp metric: HTTP server (`http.server.requests`), JVM (heap, GC, thread), HikariCP (`hikaricp.connections.active/pending`), Tomcat, Kafka, cache... → expose qua `/actuator/prometheus`.
- ⚠️ **Cardinality bùng nổ**: không đưa giá trị có vô số khả năng (userId, orderId, URL chứa ID) vào **tag** → mỗi tổ hợp tag là 1 time series → Prometheus OOM.
- **Observation API** (Boot 3): `@Observed` hoặc `Observation.createNotStarted(...)` → một lần đo sinh ra **cả metric lẫn trace span**.

### 12.4. Distributed Tracing & Logging
- **Micrometer Tracing** (bridge sang OpenTelemetry hoặc Brave) tự gắn `traceId`/`spanId` vào **MDC** của log và tự truyền header **W3C `traceparent`** qua `RestClient`/`WebClient`/`RestTemplate` (phải tạo bằng **Builder do Spring inject** thì mới được instrument), Kafka (cần bật observation cho template/listener).
- Export qua **OTLP** tới OpenTelemetry Collector → Jaeger/Tempo/Zipkin/Datadog. Cấu hình `management.tracing.sampling.probability` (mặc định 0.1 = 10%).
- **Structured logging** (Boot 3.4+): `logging.structured.format.console=ecs` (hoặc `logstash`, `gelf`) → log JSON có sẵn traceId → đẩy vào ELK/Loki và nhảy từ log sang trace.
- Log tốt: có correlation id, không log dữ liệu nhạy cảm (password, token, số thẻ), log mức `ERROR` kèm stack trace một lần ở nơi xử lý cuối cùng (tránh log-and-rethrow ở mọi tầng).


### 12.5. Logging trong Spring Boot
- Mặc định: **SLF4J** (API) + **Logback** (implementation); đổi sang Log4j2 bằng `spring-boot-starter-log4j2` (exclude `spring-boot-starter-logging`).
- Cấu hình nhanh trong `application.yml`: `logging.level.root=INFO`, `logging.level.com.example=DEBUG`, `logging.level.org.hibernate.SQL=DEBUG`; file: `logging.file.name`; cấu hình chi tiết bằng `logback-spring.xml` (hỗ trợ `<springProfile>`, `<springProperty>`).
- Đổi log level **lúc runtime** không restart: `POST /actuator/loggers/com.example` với `{"configuredLevel":"DEBUG"}`.
- Hiệu năng: dùng placeholder `log.debug("user {}", id)` thay vì nối chuỗi; bọc log tốn kém bằng `if (log.isDebugEnabled())`; dùng **AsyncAppender** khi log nhiều (cẩn thận mất log khi crash/queue đầy).
- **MDC** (Mapped Diagnostic Context — ThreadLocal): gắn `traceId`, `userId`, `requestId` vào mọi dòng log của request; nhớ truyền sang thread async (`TaskDecorator` — 11.1) và `MDC.clear()` cuối request.
- Production: log **JSON có cấu trúc** (12.4) ra stdout → thu thập bằng Fluent Bit/Vector → Loki/Elasticsearch; không log dữ liệu nhạy cảm.

---

## 13. Tối ưu Hiệu năng & Khắc phục Sự cố Production

> 🎯 **Bài toán:** Ứng dụng chậm hoặc tốn tài nguyên — nhưng tối ưu sai chỗ thì tốn công mà không có kết quả.
>
> 💡 **Nguyên tắc:** **Đo trước, đoán sau.** Đi theo con đường của request để tìm nơi **chờ** (pool connection DB, HTTP downstream, lock, GC) hoặc nơi **tốn CPU** (serialize, vòng lặp, regex). Hầu hết vấn đề hiệu năng của ứng dụng Spring nằm ở **truy cập dữ liệu** (N+1, thiếu index, pool quá nhỏ/quá lớn) chứ không ở framework.


### 13.1. Tối ưu HikariCP Connection Pool

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20          # Gợi ý từ HikariCP wiki: connections = (core_count * 2) + effective_spindle_count (của DB server)
      minimum-idle: 20               # HikariCP khuyến nghị để = maximum-pool-size (pool cố định) cho hiệu năng ổn định
      connection-timeout: 3000       # chờ tối đa 3s để lấy connection rồi fail nhanh (mặc định 30s — quá dài cho API)
      max-lifetime: 1200000          # 20 phút — phải NHỎ HƠN timeout đóng connection phía DB/proxy/firewall (vài chục giây)
      idle-timeout: 300000           # 5 phút (chỉ có tác dụng khi minimum-idle < maximum-pool-size)
      leak-detection-threshold: 5000 # cảnh báo nếu 1 connection bị giữ > 5s (nghi transaction quá dài/quên đóng)
```
> ⚠️ YAML dùng `#` để comment — viết `//` sẽ làm giá trị bị hiểu sai hoặc file không parse được.
- **Pool lớn hơn không có nghĩa là nhanh hơn**: DB chỉ thực sự chạy song song ~số core; quá nhiều connection → context switch, lock contention trong DB. Tổng `maximum-pool-size × số instance` phải < `max_connections` của DB (trừ chỗ cho admin/migration).
- Theo dõi metric `hikaricp.connections.pending` (thread đang chờ connection) — > 0 thường xuyên mới là dấu hiệu thiếu connection **hoặc** transaction giữ connection quá lâu (nguyên nhân phổ biến hơn).

### 13.2. Quy trình Khắc phục CPU 100% trong Spring Boot

1. **Tìm Process ID (PID)** của ứng dụng Java: `jps -l` hoặc `ps aux | grep java`.
2. **Tìm Thread tiêu tốn nhiều CPU nhất**: `top -H -p <PID>` (xác định thread ID dạng thập phân, ví dụ `28914`).
3. **Chuyển Thread ID sang Hexadecimal**: `printf "%x\n" 28914` (kết quả: `0x70f2`).
4. **Chụp Thread Dump**: `jstack -l <PID> > threaddump.txt`.
5. **Tra cứu `nid=0x70f2` trong file dump**: Xem stack trace chính xác dòng code nào đang chạy vòng lặp vô tận (infinite loop) hoặc bị lock contention.
6. **Nếu là thread GC** (`GC Thread#`, `G1 Conc#`): vấn đề là bộ nhớ chứ không phải code → xem GC log / `jstat -gcutil`, heap dump (xem `Java.md` 7.6).
- Cách nhanh hơn: **async-profiler** (`asprof -d 30 -f cpu.html <PID>`) cho flame graph CPU; hoặc chụp vài thread dump cách nhau 5s và so sánh (thread nào luôn ở cùng chỗ).
- Trong container không có `top`/`jstack`: `jcmd <PID> Thread.print`, hoặc dùng endpoint `/actuator/threaddump` (bảo vệ cẩn thận).

### 13.3. Response chậm — checklist khoanh vùng
1. **Metric/trace** xem thời gian nằm ở đâu: app CPU, chờ DB connection (`hikaricp.connections.pending`), query DB, gọi service khác, GC pause.
2. Bật đếm số SQL mỗi request → phát hiện **N+1** (7.2).
3. Query chậm → `EXPLAIN ANALYZE` (`Database.md` 20.3, 20.11).
4. Gọi downstream không có **timeout** → thread treo dần cạn pool (16.2 — luôn đặt connect/read timeout).
5. Serialization JSON object graph lớn / lazy loading trong lúc serialize (OSIV).
6. Log quá nhiều (log đồng bộ ra disk/console ở mức DEBUG trong vòng lặp).
7. Lock contention trong app (`synchronized` trên singleton), pool `@Async` đầy.

### 13.4. Tối ưu thời gian khởi động & bộ nhớ
| Kỹ thuật | Tác dụng | Đánh đổi |
|---|---|---|
| `spring.main.lazy-initialization=true` | Bean chỉ tạo khi cần → khởi động nhanh | Lỗi cấu hình lộ ra muộn; request đầu chậm |
| **CDS / AOT cache** (Java 21+ AppCDS, Java 24–25 Project Leyden `-XX:AOTCache`) + Spring AOT | Giảm thời gian load class, khởi động nhanh 30–50%+ | Cần bước "training run" khi build image |
| **GraalVM Native Image** (`spring-boot:build-image -Pnative`) | Khởi động ~ vài chục ms, RAM thấp | Build lâu, hạn chế reflection/proxy động, throughput đỉnh có thể thấp hơn JIT, debug khó |
| **CRaC** (Coordinated Restore at Checkpoint) | Khôi phục JVM đã warm-up từ snapshot → khởi động gần như tức thì | Cần JDK hỗ trợ (Azul, Liberica), xử lý lại kết nối/secret khi restore |
| Layered jar / Buildpacks (`bootBuildImage`) | Docker layer cache tốt → build/push nhanh | — |
| Giới hạn heap theo container (`-XX:MaxRAMPercentage=75`) | Tránh OOMKilled | — |

### 13.5. Graceful Shutdown trên Kubernetes
```yaml
server:
  shutdown: graceful                       # mặc định từ Boot 3.4
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```
- Trình tự khi pod bị xóa: K8s gửi `SIGTERM` **đồng thời** với việc rút pod khỏi Endpoints/Service — việc cập nhật routing mất vài giây → nếu app dừng nhận request ngay sẽ có lỗi 502. Thêm `preStop` hook `sleep 5–10s` trước khi app bắt đầu shutdown.
- `terminationGracePeriodSeconds` (mặc định 30s) phải > preStop + `timeout-per-shutdown-phase`.
- Readiness chuyển `OUT_OF_SERVICE` khi bắt đầu shutdown; consumer Kafka dừng poll và commit offset; `@Async` executor chờ task (`setWaitForTasksToCompleteOnShutdown`).

---

## 14. Externalized Configuration & Profiles

> 🎯 **Bài toán:** Cùng một artifact (JAR/image) phải chạy ở dev, staging, prod với URL DB, mật khẩu, timeout khác nhau — không được build lại cho mỗi môi trường, và không được đưa secret vào code.
>
> 💡 **Ý tưởng:** Cấu hình đọc từ **nhiều nguồn có thứ tự ưu tiên** (file trong JAR < file ngoài < biến môi trường < tham số dòng lệnh) → ghi đè được ở môi trường triển khai. `@ConfigurationProperties` gom cấu hình thành object có kiểu, được validate lúc khởi động (fail fast).


### 14.1. Thứ tự ưu tiên của cấu hình (cao → thấp, rút gọn)
1. Thuộc tính trong test (`@TestPropertySource`, `@SpringBootTest(properties=...)`, `@DynamicPropertySource`)
2. **Command line args** (`--server.port=9090`)
3. `SPRING_APPLICATION_JSON`
4. **Biến môi trường OS** (`SERVER_PORT`, `SPRING_DATASOURCE_URL` — relaxed binding: `.` → `_`, viết hoa)
5. `application-{profile}.yml` **ngoài** jar → `application.yml` **ngoài** jar
6. `application-{profile}.yml` **trong** jar → `application.yml` **trong** jar
7. `@PropertySource` trên `@Configuration`, giá trị mặc định
> Hệ quả: cùng 1 image Docker chạy mọi môi trường, chỉ khác **biến môi trường / ConfigMap / Secret** (nguyên tắc 12-factor app).

### 14.2. `@Value` vs `@ConfigurationProperties`
```java
@ConfigurationProperties(prefix = "payment")
@Validated
public record PaymentProperties(
        @NotBlank String baseUrl,
        @DurationUnit(ChronoUnit.SECONDS) Duration timeout,   // "5s", "PT5S" hoặc 5 (giây)
        @DefaultValue("3") int maxRetries,
        Map<String, String> merchantIds) {}

// Đăng ký: @EnableConfigurationProperties(PaymentProperties.class) hoặc @ConfigurationPropertiesScan
```
| | `@Value("${payment.base-url}")` | `@ConfigurationProperties` |
|---|---|---|
| Phạm vi | Từng giá trị lẻ | Nhóm cấu hình có cấu trúc (type-safe, lồng nhau, list/map) |
| Relaxed binding | Hạn chế | Đầy đủ (`base-url`, `baseUrl`, `BASE_URL`) |
| Validation | Không | `@Validated` + Bean Validation → **fail fast khi khởi động** |
| Metadata cho IDE | Không | Có (`spring-boot-configuration-processor`) |
| SpEL | ✅ | ❌ |
→ Ưu tiên `@ConfigurationProperties` (dạng record bất biến).

### 14.3. Profiles & Secrets
- `spring.profiles.active=prod` (hoặc `SPRING_PROFILES_ACTIVE`); `@Profile("!prod")` cho bean chỉ dùng ở dev; **profile groups**: `spring.profiles.group.prod=prod-db,prod-kafka`.
- Khai báo nhiều document trong 1 file bằng `---` + `spring.config.activate.on-profile`; `spring.config.import=optional:configserver:,vault://,configtree:/etc/secrets/` để nạp từ Config Server/Vault/K8s Secret mount.
- **Không commit secret** vào Git; dùng Vault / AWS Secrets Manager / K8s Secret (+ External Secrets Operator). `/actuator/env` tự che (sanitize) các key nhạy cảm.
- **Refresh không restart**: `@RefreshScope` (Spring Cloud) + `/actuator/refresh` hoặc Spring Cloud Bus; trên K8s có thể dùng Spring Cloud Kubernetes reload. Feature flag nên dùng công cụ chuyên dụng (Unleash, LaunchDarkly, OpenFeature).

---

## 15. Validation & thiết kế REST API

### 15.1. Bean Validation (Jakarta Validation 3.x / Hibernate Validator)
```java
public record CreateOrderRequest(
        @NotNull @Positive Long customerId,
        @NotEmpty List<@Valid OrderLine> lines,            // @Valid để validate lồng (cascade)
        @Email String contactEmail,
        @Size(max = 500) String note) {}

public record OrderLine(@NotBlank String sku, @Min(1) @Max(100) int qty) {}

@RestController
@Validated                                                   // bật validate cho tham số đơn (@PathVariable, @RequestParam)
class OrderController {
    @PostMapping("/orders")
    ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest req) { ... }  // lỗi → MethodArgumentNotValidException (400)

    @GetMapping("/orders/{id}")
    OrderDto get(@PathVariable @Positive Long id) { ... }   // lỗi → HandlerMethodValidationException (Spring 6.1+) / ConstraintViolationException
}
```
- `@Valid` (chuẩn Jakarta, hỗ trợ cascade) vs `@Validated` (của Spring, hỗ trợ **validation groups** `@Validated(OnCreate.class)` và kích hoạt method-level validation qua AOP proxy).
- Custom constraint: annotation + `ConstraintValidator<A, T>`; validate xuyên field (vd `startDate < endDate`) bằng constraint ở cấp class.
- Validation ở API là tầng **đầu tiên**, không thay thế ràng buộc DB (`UNIQUE`, `FK`) — xem `Database.md` 2.5.

### 15.2. Thiết kế REST API — các điểm hay hỏi
- **Idempotency của HTTP method**: `GET`, `PUT`, `DELETE` idempotent; `POST` không → cho thao tác tạo/thanh toán, nhận header **`Idempotency-Key`**, lưu kết quả theo key để retry trả lại kết quả cũ (SystemDesign Case 8).
- **Status code**: 200/201 (+ `Location`)/204; 400 (validation), 401/403, 404, **409 Conflict** (trùng, xung đột version), **412** (ETag `If-Match` không khớp), 422, **429** (+ `Retry-After`), 500/502/503/504.
- **Versioning**: URI (`/api/v1`), header (`API-Version`), media type (`application/vnd.app.v2+json`) — Spring Framework 7 có hỗ trợ sẵn (1.4).
- **Phân trang**: cursor/keyset cho dữ liệu lớn; trả `next_cursor`.
- **Optimistic concurrency qua HTTP**: trả `ETag` (từ `@Version`), client gửi `If-Match` khi cập nhật → 412 nếu đã có người sửa.
- Tài liệu: **springdoc-openapi** sinh OpenAPI 3 + Swagger UI từ code.
- Không trả **Entity** trực tiếp ra API (lộ cấu trúc DB, lazy loading, vòng lặp JSON quan hệ 2 chiều) → dùng DTO/record + mapper (MapStruct).

---

## 16. HTTP Clients & Resilience

> 🎯 **Bài toán:** Gọi service khác qua mạng thì **chắc chắn** sẽ có lúc chậm hoặc lỗi. Nếu không phòng vệ: thread chờ vô hạn → hết thread → service của mình cũng chết (**cascading failure**).
>
> 💡 **Ý tưởng — các lớp phòng vệ:** **Timeout** (không chờ vô hạn) → **Retry** có backoff + jitter, chỉ cho thao tác idempotent → **Circuit breaker** (downstream lỗi liên tục thì ngừng gọi một lúc, trả fallback ngay) → **Bulkhead** (giới hạn số lời gọi đồng thời tới mỗi downstream để một downstream treo không chiếm hết tài nguyên) → **Rate limiter**.


### 16.1. Chọn HTTP client
| Client | Kiểu | Trạng thái |
|---|---|---|
| `RestTemplate` | Đồng bộ, template | **Maintenance mode** — không thêm tính năng mới |
| **`RestClient`** (Boot 3.2+) | Đồng bộ, fluent API | ✅ Khuyến nghị cho code blocking (rất hợp với virtual threads) |
| `WebClient` | Reactive, non-blocking | ✅ Cho WebFlux / streaming / gọi song song nhiều nguồn |
| **HTTP Interface** (`@HttpExchange`) | Declarative (interface) | ✅ Thay thế OpenFeign; chạy trên RestClient hoặc WebClient |
| OpenFeign (Spring Cloud) | Declarative | Spring Cloud đã thông báo chuyển sang chế độ duy trì (feature-complete) — ưu tiên HTTP Interface |

```java
@HttpExchange("/api/v1/customers")
public interface CustomerClient {
    @GetExchange("/{id}")
    CustomerDto get(@PathVariable Long id);
}

@Bean
CustomerClient customerClient(RestClient.Builder builder) {   // dùng Builder do Spring inject → có sẵn tracing/metrics
    var httpClient = java.net.http.HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))            // connect timeout
            .build();
    var factory = new JdkClientHttpRequestFactory(httpClient);
    factory.setReadTimeout(Duration.ofSeconds(3));            // read/response timeout
    RestClient rc = builder.baseUrl("http://customer-service").requestFactory(factory).build();
    return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(rc)).build().createClient(CustomerClient.class);
}
```

### 16.2. Timeouts — quy tắc số 1 của resilience
- **Mọi lời gọi mạng đều phải có timeout**: connect timeout (1–3s), read/response timeout (theo SLA của downstream), timeout lấy connection từ pool. Nhiều client mặc định **chờ vô hạn** → 1 downstream treo kéo theo toàn bộ thread của service gọi nó (cascading failure).
- **Timeout budget**: request ngoài cùng có deadline 2s thì các lời gọi bên trong phải có tổng thời gian < 2s (gRPC truyền **deadline** tự động; HTTP phải tự truyền).

### 16.3. Resilience4j (Circuit Breaker, Retry, Rate Limiter, Bulkhead, TimeLimiter)
```java
@CircuitBreaker(name = "inventory", fallbackMethod = "fallbackStock")
@Retry(name = "inventory")
public StockDto getStock(String sku) { return inventoryClient.getStock(sku); }

private StockDto fallbackStock(String sku, Throwable ex) {   // cùng tham số + Throwable
    return StockDto.unknown(sku);                             // degrade: hiển thị "liên hệ", dùng cache cũ...
}
```
```yaml
resilience4j:
  circuitbreaker:
    instances:
      inventory:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 50
        failure-rate-threshold: 50              # ≥ 50% lỗi trong 50 call gần nhất → OPEN
        slow-call-duration-threshold: 2s        # call chậm cũng tính là "xấu"
        slow-call-rate-threshold: 80
        wait-duration-in-open-state: 30s        # sau 30s → HALF_OPEN
        permitted-number-of-calls-in-half-open-state: 5
  retry:
    instances:
      inventory:
        max-attempts: 3
        wait-duration: 200ms
        enable-exponential-backoff: true
        retry-exceptions: [java.io.IOException, java.util.concurrent.TimeoutException]
        ignore-exceptions: [com.example.BusinessException]   # lỗi nghiệp vụ (4xx) không retry
```
- **Thứ tự các decorator** (mặc định Resilience4j-Spring): `Retry( CircuitBreaker( RateLimiter( TimeLimiter( Bulkhead( call )))))` → Retry ở ngoài cùng; mỗi lần retry đều được Circuit Breaker ghi nhận.
- **Chỉ retry khi an toàn**: lỗi tạm thời (timeout, 503, connection reset) và thao tác **idempotent**. Retry POST thanh toán không có idempotency key = trừ tiền 2 lần. Retry nhiều tầng (gateway retry × service retry × client retry) → **retry storm** nhân cấp số.
- Spring Framework 7 có `@Retryable` / `@ConcurrencyLimit` trong core cho nhu cầu đơn giản (1.4); Resilience4j vẫn là lựa chọn đầy đủ (có metrics, circuit breaker).

✅ **Vì sao thứ tự đó:** timeout là nền tảng (thiếu nó thì mọi cơ chế khác vô dụng); retry không có circuit breaker sẽ **nhân tải** lên downstream đang yếu (retry storm); bulkhead đảm bảo hỏng một phần không thành hỏng toàn bộ.

> 🛠 **Áp dụng:** Đặt timeout theo SLA của downstream; tổng (timeout × số lần retry) phải nhỏ hơn timeout của chính request gọi vào mình; chỉ retry lỗi tạm thời (5xx, timeout kết nối), không retry 4xx; fallback phải có ý nghĩa nghiệp vụ (dữ liệu cache, giá trị mặc định) hoặc trả lỗi rõ ràng.

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao retry một lời gọi `POST /payments` có thể gây trừ tiền hai lần, và cách xử lý?**
   → Lời gọi đầu có thể đã thành công nhưng response bị mất. Dùng **Idempotency-Key** để server nhận diện và trả lại kết quả cũ.
</details>


---

## 17. Caching với Spring Cache & Redis

> 🎯 **Bài toán:** Cùng một dữ liệu được đọc hàng nghìn lần/giây, mỗi lần đều vào DB → DB quá tải, latency cao.
>
> 💡 **Ý tưởng:** Lưu kết quả ở nơi đọc nhanh hơn (bộ nhớ trong JVM hoặc Redis) và đánh dấu bằng annotation (`@Cacheable`) qua AOP proxy. Cái giá: dữ liệu có thể **cũ** → phải chọn TTL và cách vô hiệu hóa.
>
> ⚖️ **Local (Caffeine) vs phân tán (Redis):** local nhanh nhất (ns, không qua mạng) nhưng mỗi instance một bản, khó vô hiệu hóa đồng bộ; Redis dùng chung, nhất quán hơn giữa các instance nhưng tốn một lần gọi mạng (~ms) và là thêm một hạ tầng. ✅ Dữ liệu ít đổi, chịu được cũ vài giây → Caffeine; dữ liệu dùng chung cần nhất quán giữa pod → Redis; tải rất cao → kết hợp hai tầng.


```java
@EnableCaching
@Configuration class CacheConfig {}

@Service
class ProductService {
    @Cacheable(cacheNames = "product", key = "#id", unless = "#result == null")
    public ProductDto get(Long id) { ... }                  // cache miss → chạy method → lưu kết quả

    @CachePut(cacheNames = "product", key = "#result.id")   // luôn chạy method, cập nhật cache
    public ProductDto update(UpdateProductCmd cmd) { ... }

    @CacheEvict(cacheNames = "product", key = "#id")        // xóa cache (cache-aside: update DB rồi evict)
    public void delete(Long id) { ... }

    @Cacheable(cacheNames = "hot-product", key = "#id", sync = true)  // sync = true: chỉ 1 thread trong JVM tính khi miss
    public ProductDto getHot(Long id) { ... }               //   → chống cache stampede cục bộ (không phải phân tán)
}
```
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 10m          # LUÔN đặt TTL — không có TTL là "memory leak phân tán"
      key-prefix: "app:v1:"      # đổi prefix khi đổi cấu trúc DTO để tránh deserialize lỗi
```
- **Cache abstraction là AOP proxy** → dính bẫy self-invocation như `@Transactional`.
- **Multi-level cache**: L1 **Caffeine** (in-process, nanosecond, mỗi instance một bản) + L2 **Redis** (dùng chung). Khó khăn: invalidate L1 trên mọi instance (Redis Pub/Sub, hoặc TTL L1 rất ngắn).
- **Serialization**: tránh `JdkSerializationRedisSerializer` mặc định (gắn chặt class, lỗ hổng deserialization) → dùng JSON serializer có kiểu xác định.
- **Cache + transaction**: evict **sau khi commit** (`TransactionAwareCacheManagerProxy` hoặc `@TransactionalEventListener`) — evict trước commit có thể bị request khác nạp lại dữ liệu cũ vào cache.
- Các vấn đề cache penetration / avalanche / stampede / hot key: xem `SystemDesign.md` mục 2.4.

---

## 18. Spring WebFlux & Reactive (tóm tắt)

> 🎯 **Bài toán:** Với thread-per-request, số request đồng thời bị giới hạn bởi số thread; khi phần lớn thời gian là chờ I/O, thread bị lãng phí.
>
> 💡 **Ý tưởng:** Vài **event loop thread** xử lý mọi request; không bao giờ chờ — đăng ký callback và nhận kết quả khi I/O xong (Reactive Streams, có backpressure). Từ Java 21, virtual threads giải cùng bài toán với code tuần tự → phạm vi "nên dùng WebFlux" thu hẹp lại (xem bảng so sánh ở `Java.md` 11).


- **Mô hình**: số ít **event loop** thread (Reactor Netty) xử lý mọi request non-blocking; dữ liệu là luồng **`Mono<T>`** (0..1 phần tử) / **`Flux<T>`** (0..N) theo chuẩn Reactive Streams (có **backpressure**).
```java
@GetMapping("/users/{id}/profile")
Mono<Profile> profile(@PathVariable String id) {
    Mono<User> user = userClient.get(id);                 // WebClient — non-blocking
    Mono<List<Order>> orders = orderClient.recent(id);
    return Mono.zip(user, orders, Profile::new)           // gọi song song, ghép kết quả
               .timeout(Duration.ofSeconds(2))
               .onErrorResume(e -> Mono.just(Profile.empty(id)));
}
```
- ⚠️ **Không block trong pipeline** (`.block()`, JDBC, `Thread.sleep`) — sẽ treo event loop. Bắt buộc dùng driver reactive (**R2DBC**, reactive Mongo/Redis/Kafka), hoặc cô lập code blocking bằng `subscribeOn(Schedulers.boundedElastic())`. Phát hiện bằng **BlockHound**.
- Context (security, tracing, transaction) truyền qua **Reactor Context** thay vì ThreadLocal.
- **Khi nào dùng WebFlux?** Gateway/proxy (Spring Cloud Gateway), streaming (SSE), số kết nối đồng thời rất lớn, cần backpressure, toàn bộ stack đã reactive. Với ứng dụng CRUD thông thường, **Spring MVC + virtual threads** cho throughput tương đương mà code đơn giản, dễ debug hơn nhiều.

---

## 19. Testing trong Spring Boot

> 🎯 **Bài toán:** Nạp toàn bộ ApplicationContext cho mỗi test thì chậm; mock hết thì không phát hiện lỗi cấu hình/SQL/serialize.
>
> 💡 **Ý tưởng:** **Test slice** chỉ nạp phần cần thiết (`@WebMvcTest` cho web layer, `@DataJpaTest` cho repository) + **Testcontainers** để chạy DB/Kafka thật trong Docker cho integration test. Spring **cache context** giữa các test có cùng cấu hình → hạn chế `@MockitoBean`/`@DirtiesContext` tràn lan vì mỗi cấu hình khác nhau sinh ra một context mới.


| Annotation | Nạp gì | Dùng cho |
|---|---|---|
| `@SpringBootTest` | **Toàn bộ** ApplicationContext (có thể kèm server thật `webEnvironment = RANDOM_PORT`) | Integration test end-to-end trong service |
| `@WebMvcTest(OrderController.class)` | Chỉ tầng MVC (controller, advice, filter, converter) | Test controller với `MockMvc`/`MockMvcTester`, mock service |
| `@DataJpaTest` | JPA repositories, EntityManager, (mặc định thay bằng DB nhúng + rollback mỗi test) | Test query, mapping |
| `@JsonTest`, `@RestClientTest`, `@WebFluxTest`, `@DataRedisTest`... | Các "slice" tương ứng | Test nhanh từng tầng |

```java
@SpringBootTest
@Testcontainers
class OrderFlowIT {
    @Container
    @ServiceConnection                       // Boot 3.1+: tự cấu hình spring.datasource.* theo container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:17");

    @Container @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.9.0"));

    @MockitoBean PaymentGateway paymentGateway;  // Boot 3.4+ (thay @MockBean đã deprecated)

    @Test void placesOrder() { ... }
}
```
- Dùng DB thật (Testcontainers) thay vì H2 để tránh khác biệt SQL/lock (`Java.md` 17). Với `@DataJpaTest` + Testcontainers: thêm `@AutoConfigureTestDatabase(replace = NONE)`.
- **Context caching**: Spring cache ApplicationContext giữa các test class có **cùng cấu hình**; mỗi tổ hợp `@MockitoBean`/property khác nhau tạo context mới → test suite chậm. Gom cấu hình chung vào base class.
- `@Transactional` trên test → rollback sau mỗi test, nhưng **che giấu** lỗi chỉ xuất hiện khi commit (lazy loading ngoài transaction, constraint deferred) và không thấy dữ liệu từ thread khác (server `RANDOM_PORT` chạy thread khác).
- Kafka/async: chờ kết quả bằng **Awaitility** (`await().atMost(5, SECONDS).untilAsserted(...)`), không `Thread.sleep`.

---

## 20. Spring Cloud & Microservices (tổng quan)

> 🎯 **Bài toán:** Khi tách thành nhiều service: tìm địa chỉ của nhau, cấu hình tập trung, định tuyến từ bên ngoài, phòng vệ lỗi, theo dấu request.
>
> 💡 **Lưu ý thời thế:** Trên Kubernetes, nhiều bài toán này đã được **hạ tầng** giải (Service DNS thay Eureka, ConfigMap/Secret thay Config Server, Ingress/Gateway API, service mesh). ✅ Chỉ dùng thành phần Spring Cloud khi hạ tầng chưa cung cấp — tránh giải cùng một bài toán ở hai tầng.


| Nhu cầu | Spring Cloud | Thay thế phổ biến trên Kubernetes |
|---|---|---|
| API Gateway | **Spring Cloud Gateway** (WebFlux hoặc bản Server MVC) — routing, rate limit (Redis), auth relay, circuit breaker | NGINX/Envoy Ingress, Kong, Istio Gateway |
| Service Discovery | Eureka, Consul, Spring Cloud Kubernetes | K8s Service + CoreDNS (server-side discovery) |
| Client-side LB | Spring Cloud LoadBalancer | kube-proxy, service mesh |
| Config tập trung | Spring Cloud Config Server (Git backend) | ConfigMap/Secret, Vault |
| Resilience | Spring Cloud Circuit Breaker (Resilience4j) | Service mesh (retry/timeout/outlier detection ở sidecar) |
| Messaging | Spring Cloud Stream (binder Kafka/RabbitMQ) | Spring Kafka trực tiếp |
| Tracing | Micrometer Tracing + OTel | OTel Collector, mesh tracing |
| Workflow / Saga | — (Spring State Machine) | Temporal, Camunda, AWS Step Functions |

- Trên K8s, nhiều thành phần Spring Cloud cổ điển (Eureka, Config Server, Ribbon) **không còn cần thiết** — tránh chạy 2 hệ thống discovery song song.
- Các pattern liên quan (Saga, Outbox, CQRS, Circuit Breaker, API Gateway, BFF): xem `SystemDesign.md` mục 3 và 5.
- **Spring Modulith**: xây **modular monolith** — kiểm tra ranh giới module bằng test (`ApplicationModules.verify()`), giao tiếp giữa module bằng event (có Event Publication Registry ~ outbox) → bước đệm tốt trước khi tách microservice.

---

## 21. Tổng hợp Câu hỏi Phỏng vấn Spring Boot Thường Gặp (Senior / Lead)

#### Q1: Phân biệt `@Controller` vs `@RestController`?
- `@Controller`: Dùng cho ứng dụng MVC truyền thống (render giao diện HTML qua ViewResolver như Thymeleaf, JSP).
- `@RestController`: Là annotation kết hợp giữa `@Controller` + `@ResponseBody`. Mọi method mặc định serialize kết quả trả về thành JSON hoặc XML thông qua `HttpMessageConverter` mà không tìm kiếm view template.

#### Q2: Phân biệt `@Component`, `@Service`, `@Repository`, `@Configuration`?
- Tất cả đều là meta-annotation có nguồn gốc từ `@Component`.
- `@Service`: Đánh dấu tầng nghiệp vụ (Business logic), giúp code tường minh về mặt ngữ nghĩa (DDD).
- `@Repository`: Đánh dấu tầng lưu trữ dữ liệu (DAO). Tự động kích hoạt cơ chế dịch ngoại lệ của nhà cung cấp cơ sở dữ liệu (JDBC, Hibernate) thành `DataAccessException` của Spring (`PersistenceExceptionTranslationPostProcessor`).
- `@Configuration`: Đánh dấu class chứa các định nghĩa `@Bean`. Khác với `@Component`, method trong `@Configuration` được CGLIB bọc lại để đảm bảo tính **Singleton** khi gọi trực tiếp giữa các `@Bean` methods.

#### Q3: Chuyện gì xảy ra khi 1 method `@Transactional` gọi 1 method `@Transactional(propagation = REQUIRES_NEW)` ở class khác nhưng method con bị lỗi ném ngoại lệ?
Nếu method con ném `RuntimeException` và method cha **bắt lại bằng try-catch**:
- Transaction con (REQUIRES_NEW) sẽ rollback hoàn toàn.
- Transaction cha vì đã bắt ngoại lệ nên **vẫn có thể commit thành công**!
Nếu method cha không bắt try-catch, ngoại lệ tiếp tục văng lên làm rollback cả transaction cha.
> **So sánh với `REQUIRED`** (mặc định): method con dùng chung transaction của cha → khi con ném RuntimeException, transaction bị đánh dấu **rollback-only**; dù cha bắt exception và chạy tiếp, lúc commit sẽ nhận **`UnexpectedRollbackException`** (xem 8.3 #5). Với `NESTED`: chỉ rollback về savepoint, cha commit bình thường (cần JDBC savepoint — `DataSourceTransactionManager`; JPA không hỗ trợ đầy đủ).

#### Q4: Làm thế nào để implement Graceful Shutdown trong Spring Boot?
Từ Spring Boot 2.3+, kích hoạt:
```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```
Khi nhận tín hiệu `SIGTERM` (từ Docker / Kubernetes), Embedded Tomcat ngừng nhận request mới, hoàn tất xử lý các request đang dở dang trong tối đa 30s trước khi shutdown hoàn toàn.
Từ Boot 3.4, graceful shutdown được bật mặc định. Trên Kubernetes cần thêm `preStop` sleep — xem 13.5.

#### Q5: Spring Boot khởi động như thế nào (`SpringApplication.run`)?
1. Tạo `SpringApplication`: xác định loại ứng dụng (Servlet/Reactive/None), nạp `ApplicationContextInitializer` & `ApplicationListener` (qua `spring.factories`).
2. `run()`: chuẩn bị `Environment` (đọc property theo thứ tự ưu tiên — 14.1), in banner, tạo `ApplicationContext` phù hợp.
3. `refresh()` context: đọc bean definitions (component scan + auto-configuration với `@Conditional`), chạy `BeanFactoryPostProcessor`, đăng ký `BeanPostProcessor`, **khởi tạo singleton beans** (2.3), tạo & start **embedded web server** (Tomcat).
4. Phát `ApplicationStartedEvent` → chạy `CommandLineRunner`/`ApplicationRunner` → `ApplicationReadyEvent` (readiness = ACCEPTING_TRAFFIC).

#### Q6: Làm sao viết một Spring Boot Starter riêng?
Tạo module `xxx-spring-boot-autoconfigure` chứa class `@AutoConfiguration` + các `@Conditional...` + `@ConfigurationProperties`; khai báo class đó trong `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`; module `xxx-spring-boot-starter` chỉ gom dependency. Luôn dùng `@ConditionalOnMissingBean` để người dùng override được.

#### Q7: `@Bean` vs `@Component`? Nhiều bean cùng kiểu thì inject thế nào?
`@Component` đặt trên class của chính mình (được component scan); `@Bean` đặt trên factory method trong `@Configuration` — dùng cho class của thư viện bên thứ ba hoặc khi cần logic khởi tạo. Nhiều bean cùng kiểu → `NoUniqueBeanDefinitionException`; giải quyết bằng `@Primary`, `@Qualifier("name")`, inject theo tên tham số, hoặc inject `List<PaymentProvider>`/`Map<String, PaymentProvider>` (pattern Strategy). Boot mặc định **cấm override bean trùng tên** (`spring.main.allow-bean-definition-overriding=false`).

#### Q8: Field injection vs Constructor injection?
Constructor injection được khuyến nghị: dependency `final` (bất biến), không tạo được object thiếu dependency, test không cần Spring (`new Service(mock)`), lộ ra khi class có quá nhiều dependency, và **phát hiện circular dependency ngay lúc khởi động**. Field injection (`@Autowired` trên field) ẩn dependency, khó test, không dùng được `final`.

#### Q9: Làm sao xử lý lỗi tập trung và trả lỗi chuẩn cho client?
`@RestControllerAdvice` + `@ExceptionHandler` trả `ProblemDetail` (6.3); phân loại lỗi nghiệp vụ (4xx, message cho người dùng) vs lỗi hệ thống (5xx, message chung + traceId, log chi tiết); không lộ stack trace/SQL ra ngoài.

#### Q10: Làm thế nào để đảm bảo cập nhật DB và gửi message Kafka cùng thành công hoặc cùng thất bại?
Không có transaction chung giữa DB và Kafka (trừ XA — không khuyến nghị). Dùng **Transactional Outbox**: ghi event vào bảng `outbox` cùng transaction với dữ liệu nghiệp vụ → Debezium (CDC) hoặc poller đọc outbox và publish lên Kafka → consumer idempotent (SystemDesign 3.6). Phương án yếu hơn: publish trong `@TransactionalEventListener(AFTER_COMMIT)` — có thể mất message nếu app crash ngay sau commit.

#### Q11: Có 1 API chậm khi nhiều user dùng cùng lúc, bạn điều tra thế nào?
Theo checklist 13.3: xem metric/trace để biết thời gian nằm ở đâu → kiểm tra pool (Hikari pending, Tomcat busy threads), N+1, slow query + EXPLAIN, gọi downstream thiếu timeout, lock contention (thread dump), GC (GC log). Sau đó mới tối ưu: index, cache, batch, async, scale.

#### Q12: Spring MVC + virtual threads hay WebFlux?
Xem 11.2 và 18: MVC + virtual threads cho phần lớn ứng dụng I/O-bound (code đơn giản, giữ nguyên JDBC/JPA); WebFlux khi cần streaming/backpressure, gateway, hoặc toàn bộ stack đã reactive. Cả hai đều phải giới hạn concurrency tới tài nguyên có hạn (DB pool).

#### Q13: `@Transactional` đặt ở tầng nào? Controller, Service hay Repository?
**Service** — nơi định nghĩa ranh giới use case/đơn vị công việc nghiệp vụ. Repository của Spring Data đã có transaction mặc định cho từng method (`readOnly` cho method đọc); đặt ở Controller làm transaction kéo dài cả phần serialize/gọi ngoài. Method chỉ đọc: `@Transactional(readOnly = true)`.

#### Q14: Hibernate first-level cache vs second-level cache?
First-level = Persistence Context, gắn với 1 `EntityManager`/transaction, luôn bật, đảm bảo trong 1 transaction cùng id → cùng object (identity map). Second-level = cache dùng chung giữa các session ở mức `SessionFactory`, phải bật & cấu hình provider, hợp với dữ liệu ít thay đổi (7.5 #9).

#### Q15: Làm sao chạy 1 job định kỳ chỉ trên 1 instance khi scale nhiều pod?
ShedLock / Quartz cluster / K8s CronJob / leader election (11.3).

#### Q16: Bean scope `request` được inject vào singleton như thế nào mà vẫn đúng theo từng request?
Spring inject một **scoped proxy** (`@RequestScope` mặc định `proxyMode = TARGET_CLASS`): singleton giữ proxy, mỗi lần gọi method proxy tra bean thật của request hiện tại (qua `RequestContextHolder` — ThreadLocal). Cùng nguyên lý giải quyết vấn đề prototype-in-singleton (2.2).

