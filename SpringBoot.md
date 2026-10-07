# Spring & Spring Boot Comprehensive Architecture & Interview Guide

Tài liệu ôn tập chuyên sâu và toàn diện về **Spring Framework**, **Spring Boot**, **Spring Data JPA**, **Spring Security**, **Kafka Integration**, và **Hệ sinh thái Microservices**. Tài liệu được biên soạn phục vụ công việc thực tế và phỏng vấn kỹ thuật từ cấp độ Mid đến Senior / Lead Software Engineer.

> 💡 **Tài liệu liên quan**:
> - [`Java.md`](file:///e:/Source/coding-interview-university/Java.md): Kiến thức nền tảng Java Core, JVM Internals, Concurrency, Garbage Collection, Collections, Design Patterns.
> - [`Database.md`](file:///e:/Source/coding-interview-university/Database.md): Chi tiết về Database Storage Engine, B+Tree, MVCC, Indexing, Transaction Isolation, Sharding.
> - [`SystemDesign.md`](file:///e:/Source/coding-interview-university/SystemDesign.md): Kiến trúc hệ thống phân tán, Caching, Event-driven, Load balancing, Resiliency.

---

## Mục lục

1. [Kiến trúc Tổng quan Spring Framework & Spring Boot](#1-kiến-trúc-tổng-quan-spring-framework--spring-boot)
2. [IoC Container & Bean Lifecycle Internals](#2-ioc-container--bean-lifecycle-internals)
3. [Circular Dependency & Three-Level Cache](#3-circular-dependency--three-level-cache)
4. [Spring AOP & Proxy Mechanisms](#4-spring-aop--proxy-mechanisms)
5. [Spring Boot Auto-Configuration Internals](#5-spring-boot-auto-configuration-internals)
6. [Spring MVC Architecture & Request Lifecycle](#6-spring-mvc-architecture--request-lifecycle)
7. [Spring Data JPA & Hibernate Internals](#7-spring-data-jpa--hibernate-internals)
8. [Quản lý Giao dịch (@Transactional Deep Dive)](#8-quản-lý-giao-dịch-transactional-deep-dive)
9. [Spring Security Architecture & JWT Stateless Flow](#9-spring-security-architecture--jwt-stateless-flow)
10. [Spring Kafka: Cấu hình, Producer, Consumer & Manual Partition Assignment](#10-spring-kafka-cấu-hình-producer-consumer--manual-partition-assignment)
11. [Xử lý Bất đồng bộ, Scheduling & Virtual Threads (Java 21)](#11-xử-lý-bất-đồng-bộ-scheduling--virtual-threads-java-21)
12. [Production Observability: Spring Boot Actuator, Metrics & Tracing](#12-production-observability-spring-boot-actuator-metrics--tracing)
13. [Tối ưu Hiệu năng & Khắc phục Sự cố Production](#13-tối-ưu-hiệu-năng--khắc-phục-sự-cố-production)
14. [Tổng hợp Câu hỏi Phỏng vấn Spring Boot Thường Gặp (Senior / Lead)](#14-tổng-hợp-câu-hỏi-phỏng-vấn-spring-boot-thường-gặp-senior--lead)

---

## 1. Kiến trúc Tổng quan Spring Framework & Spring Boot

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

---

## 2. IoC Container & Bean Lifecycle Internals

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

---

## 3. Circular Dependency & Three-Level Cache

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

### 4.1. So sánh JDK Dynamic Proxy vs CGLIB Proxy

Spring AOP là kiến trúc dựa trên **Runtime Proxy Pattern**. Khi một bean được đánh dấu các annotation như `@Transactional`, `@Async`, `@Cacheable`, `@PreAuthorize`, Spring không gọi trực tiếp class gốc mà tạo ra một đối tượng Proxy bao bọc bên ngoài.

```
Client ──► [ Proxy Object (Intercept advice) ] ──► [ Real Target Bean ]
```

| Tiêu chí | JDK Dynamic Proxy | CGLIB Proxy |
| :--- | :--- | :--- |
| **Cơ chế hoạt động** | Tạo proxy class tại runtime hiện thực hóa (implement) cùng một tập **Interfaces** với Target object thông qua `java.lang.reflect.Proxy`. | Tạo proxy class tại runtime bằng cách **kế thừa (subclassing)** Target class thông qua việc sinh bytecode trực tiếp (`org.springframework.cglib`). |
| **Yêu cầu đối với Target** | Bắt buộc Target class phải implement ít nhất một **Interface**. | Target class **không bắt buộc** có interface. |
| **Hạn chế** | Không thể proxy các method không nằm trong Interface. | **Không thể proxy** class hoặc method được đánh dấu `final` (vì không thể kế thừa/override). Constructor của target bị gọi 2 lần (đã được tối ưu từ các bản Objesis gần đây). |
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

---

## 5. Spring Boot Auto-Configuration Internals

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

---

## 6. Spring MVC Architecture & Request Lifecycle

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

### 6.2. So sánh Filter vs HandlerInterceptor vs AOP Aspect

| Tiêu chí | Servlet Filter | HandlerInterceptor | AOP Aspect |
| :--- | :--- | :--- | :--- |
| **Thuộc tầng** | Servlet Container chuẩn (Java EE / Jakarta Servlet). Nằm **trước** DispatcherServlet. | Spring MVC Framework. Nằm **giữa** DispatcherServlet và Controller. | Spring AOP Core. Bao bọc bất kỳ Spring Bean method nào. |
| **Phạm vi nhận biết** | Chỉ biết `HttpServletRequest`, `HttpServletResponse`. Hoàn toàn không biết method/controller nào sắp được gọi. | Biết được `Object handler` (Method, Class Controller đang xử lý request). | Can thiệp sâu vào method arguments, return values, exceptions tại tầng service/repository. |
| **Use cases phù hợp** | Gzip compression, CORS headers, Request Body logging/caching, Security authentication filter (Spring Security). | Phân quyền theo endpoint, Rate limiting theo controller, thêm request execution metrics, gán Tenant-ID context. | Business audit log, Cache eviction, Performance tracing, Custom retry logic. |

### 6.3. Xử lý Lỗi Chuẩn Hóa với `@RestControllerAdvice` & RFC 7807 Problem Details

Từ Spring Boot 3+, Spring hỗ trợ đặc tả chuẩn **RFC 7807 (Problem Details for HTTP APIs)** thông qua class `ProblemDetail`.

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

---

## 7. Spring Data JPA & Hibernate Internals

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
```

---

## 8. Quản lý Giao dịch (@Transactional Deep Dive)

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

---

## 9. Spring Security Architecture & JWT Stateless Flow

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
SecurityFilterChain:
  ├── CorsFilter
  ├── CsrfFilter
  ├── LogoutFilter
  ├── JwtAuthenticationFilter (Custom Filter kế thừa OncePerRequestFilter)
  ├── UsernamePasswordAuthenticationFilter
  ├── ExceptionTranslationFilter
  └── AuthorizationFilter (FilterSecurityInterceptor)
```

### 9.2. Luồng Xác thực Stateless JWT Hoàn Chỉnh

```java
// 1. Custom JWT Authentication Filter
@Component
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

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable()) // Stateless REST API dùng Token -> tắt CSRF an toàn
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/actuator/health").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

---

## 10. Spring Kafka: Cấu hình, Producer, Consumer & Manual Partition Assignment

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
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Chống trùng tin nhắn
        props.put(ProducerConfig.ACKS_CONFIG, "all");              // Chờ đủ Leader + ISR replicate
        props.put(ProducerConfig.RETRIES_CONFIG, 10);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
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

---

## 11. Xử lý Bất đồng bộ, Scheduling & Virtual Threads (Java 21)

### 11.1. `@Async` & Tối ưu ThreadPoolTaskExecutor

> ⚠️ Không bao giờ dùng `@Async` trần mà không cấu hình Custom Executor! Mặc định Spring dùng `SimpleAsyncTaskExecutor`, cơ chế này tạo mới một OS thread cho **mỗi request** mà không tái sử dụng, dễ làm cạn kiệt tài nguyên JVM (`OutOfMemoryError: unable to create new native thread`).

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
        executor.initialize();
        return executor;
    }
}
```

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

---

## 12. Production Observability: Spring Boot Actuator, Metrics & Tracing

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

---

## 13. Tối ưu Hiệu năng & Khắc phục Sự cố Production

### 13.1. Tối ưu HikariCP Connection Pool

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20       // Công thức chuẩn PostgreSQL: (Core * 2) + Disk Count
      minimum-idle: 10
      idle-timeout: 300000        // 5 phút
      connection-timeout: 20000   // 20 giây fail nếu không lấy được connection
      max-lifetime: 1200000       // 20 phút (phải nhỏ hơn timeout phía DB server)
      leak-detection-threshold: 5000 // In cảnh báo log nếu query giữ connection quá 5s!
```

### 13.2. Quy trình Khắc phục CPU 100% trong Spring Boot

1. **Tìm Process ID (PID)** của ứng dụng Java: `jps -l` hoặc `ps aux | grep java`.
2. **Tìm Thread tiêu tốn nhiều CPU nhất**: `top -H -p <PID>` (xác định thread ID dạng thập phân, ví dụ `28914`).
3. **Chuyển Thread ID sang Hexadecimal**: `printf "%x\n" 28914` (kết quả: `0x70f2`).
4. **Chụp Thread Dump**: `jstack -l <PID> > threaddump.txt`.
5. **Tra cứu `nid=0x70f2` trong file dump**: Xem stack trace chính xác dòng code nào đang chạy vòng lặp vô tận (infinite loop) hoặc bị lock contention.

---

## 14. Tổng hợp Câu hỏi Phỏng vấn Spring Boot Thường Gặp (Senior / Lead)

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
