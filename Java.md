# Java Core & JVM Interview Guide

Tài liệu ôn tập **Java Core, JVM, Concurrency và các tính năng Java hiện đại (8 → 25)** cho phỏng vấn Backend từ Junior đến Senior.

> 💡 **Tài liệu liên quan**:
> - [`SpringBoot.md`](SpringBoot.md): Spring Framework, Spring Boot, JPA, Security, Kafka.
> - [`Database.md`](Database.md): Index, Transaction, MVCC, SQL.
> - [`SystemDesign.md`](SystemDesign.md): Kiến trúc hệ thống phân tán.
> - [`Networking.md`](Networking.md): TCP/IP, DNS, HTTP, TLS, timeout & lỗi kết nối, troubleshooting mạng.

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


### 🗺 Chuỗi bài toán của Java & JVM

```
 Muốn viết 1 lần, chạy mọi hệ điều hành/CPU, không lo hỏng bộ nhớ như C/C++
   └─► compile ra BYTECODE chạy trên JVM (7)
         ├─ thông dịch bytecode thì chậm          → JIT compile phần code "nóng" thành mã máy (7.3, 7.10)
         ├─ quản lý bộ nhớ thủ công gây leak/crash → GARBAGE COLLECTOR tự thu hồi (8)
         │     └─ GC phải dừng ứng dụng (pause)    → generational, concurrent GC: G1, ZGC (8.2, 8.3)
         └─ nạp code động, an toàn                 → ClassLoader & parent delegation (7.1, 7.5)
 Code lớn, nhiều người viết, phải dễ thay đổi
   └─► OOP & SOLID (1) → Design patterns (13) → kiểu an toàn: type system, generics (2, 4)
 Cấu trúc dữ liệu dùng chung, nhanh, đúng → Collections (3)      Lỗi phải được xử lý tường minh → Exceptions (5)
 Code xử lý dữ liệu dài dòng → lambda, Stream (6) → record, pattern matching (10–12)
 Tận dụng nhiều core & không ngồi chờ I/O
   └─► THREAD (9) → dữ liệu chia sẻ bị sai: visibility/atomicity/ordering → JMM, synchronized, volatile, CAS, Lock
         └─ thread OS đắt, 10.000 request = 10.000 thread → thread pool (9.8) → VIRTUAL THREAD (11.1)
 Giao tiếp mạng hàng chục nghìn kết nối → NIO, epoll, Netty (15)
 Framework cần "nhìn" vào code lúc runtime → reflection, annotation, proxy (16, 13.2)
```

## Mục lục
1. [OOP Fundamentals](#1-oop-fundamentals)
2. [Java Type System](#2-java-type-system)
3. [Collections Framework](#3-collections-framework)
4. [Generics](#4-generics)
5. [Exception Handling](#5-exception-handling)
6. [Java 8 Features](#6-java-8-features)
7. [JVM Internals](#7-jvm-internals) — có [✅ Checklist trọng tâm JVM](#711--checklist-trọng-tâm-jvm-cho-phỏng-vấn)
8. [Garbage Collection](#8-garbage-collection)
9. [Concurrency & Multithreading](#9-concurrency--multithreading-chi-tiết)
10. [Java 17 Features & lộ trình phiên bản](#10-java-17-features-lts)
11. [Java 21 Features (Virtual Threads...)](#11-java-21-features-lts)
12. [Java 22 → 25 Features](#12-java-22--25-features-java-25-lts--92025)
13. [Design Patterns in Java](#13-design-patterns-in-java)
14. [Spring & Spring Boot Essentials](#14-spring--spring-boot-essentials)
15. [I/O, NIO & Networking Model](#15-io-nio--networking-model)
16. [Serialization, Reflection & Annotations](#16-serialization-reflection--annotations)
17. [Testing in Java](#17-testing-in-java)
18. [Common Java Interview Questions](#18-common-java-interview-questions)

---

### 1. OOP Fundamentals

> 🎯 **Bài toán:** Phần mềm lớn hàng trăm nghìn dòng, nhiều người cùng sửa, yêu cầu thay đổi liên tục. Viết theo kiểu thủ tục (dữ liệu toàn cục + hàm) thì mọi hàm đều có thể sửa mọi dữ liệu → sửa một chỗ hỏng mười chỗ.
>
> 💡 **Ý tưởng giải:** Gom **dữ liệu và hành vi** thành object có ranh giới rõ ràng (**encapsulation**), giao tiếp qua **hợp đồng** (interface/abstraction), cho phép **thay thế cài đặt** mà không sửa nơi dùng (**polymorphism**). SOLID là bộ nguyên tắc để ranh giới đó đặt đúng chỗ — mục tiêu chung: **thay đổi một yêu cầu chỉ phải sửa ít chỗ**.


#### 1.1. Bốn trụ cột (4 Pillars of OOP)

**Encapsulation (Đóng gói)**:
- Ẩn internal state, chỉ expose thông qua public methods (getters/setters)
- Access modifiers: `private` → `default (package-private)` → `protected` → `public`
- Nguyên tắc: fields nên `private`, methods nên có access level tối thiểu cần thiết

```java
public class BankAccount {
    private double balance; // ẩn state

    public void deposit(double amount) { // kiểm soát truy cập
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        this.balance += amount;
    }

    public double getBalance() { return balance; } // chỉ cho đọc, không cho ghi trực tiếp
}
```

**Inheritance (Kế thừa)**:
- `extends` cho class (single inheritance), `implements` cho interface (multiple)
- Java không hỗ trợ multiple class inheritance → tránh **Diamond Problem**
- Constructor KHÔNG được kế thừa, nhưng có thể gọi `super()`
- `super` keyword: gọi constructor/method của parent

```java
public class Animal {
    protected String name;
    public Animal(String name) { this.name = name; }
    public void speak() { System.out.println("..."); }
}

public class Dog extends Animal {
    public Dog(String name) { super(name); } // gọi constructor parent
    @Override
    public void speak() { System.out.println(name + " barks!"); }
}
```

**Polymorphism (Đa hình)**:
- **Compile-time** (Static): Method overloading — cùng tên, khác tham số
- **Runtime** (Dynamic): Method overriding — subclass ghi đè method parent
- Dynamic dispatch: JVM quyết định method nào gọi dựa trên **actual object type** tại runtime

```java
Animal animal = new Dog("Rex"); // compile type: Animal, runtime type: Dog
animal.speak(); // → "Rex barks!" (dynamic dispatch)
```

**Abstraction (Trừu tượng)**:
- Ẩn implementation details, chỉ expose interface cần thiết
- Thực hiện qua: Abstract class, Interface

#### 1.2. Abstract Class vs Interface

| Đặc điểm | Abstract Class | Interface |
|-----------|---------------|-----------|
| **Keyword** | `abstract class` | `interface` |
| **Kế thừa** | Single (`extends`) | Multiple (`implements`) |
| **Constructor** | ✅ Có | ❌ Không |
| **Fields** | Mọi loại (private, protected, ...) | Chỉ `public static final` (constants) |
| **Methods** | abstract + concrete | abstract + `default` (Java 8) + `static` (Java 8) + `private` (Java 9) |
| **State** | ✅ Có instance state | ❌ Không có instance state |
| **Khi nào dùng** | IS-A relationship, shared state | CAN-DO capability, contract |

```java
// Abstract class — khi cần shared state và partial implementation
public abstract class Shape {
    protected String color; // instance state
    public Shape(String color) { this.color = color; }
    public abstract double area(); // contract
    public String getColor() { return color; } // shared implementation
}

// Interface — khi cần define capability
public interface Drawable {
    void draw(); // abstract method
    default void drawWithBorder() { // Java 8: default method
        draw();
        System.out.println("Drawing border...");
    }
    static Drawable empty() { return () -> {}; } // Java 8: static method
    private void log(String msg) { System.out.println(msg); } // Java 9: private method
}
```

#### 1.3. SOLID Principles

**S — Single Responsibility Principle (SRP)**:
- Một class chỉ nên có **một lý do để thay đổi**
- Mỗi class phụ trách một responsibility duy nhất

```java
// ❌ Vi phạm SRP: class vừa xử lý business logic, vừa lưu DB, vừa gửi email
public class UserService {
    public void createUser(User user) { /* validate + save + sendEmail */ }
}

// ✅ Tuân thủ SRP
public class UserValidator { public boolean validate(User user) { ... } }
public class UserRepository { public void save(User user) { ... } }
public class EmailService { public void sendWelcomeEmail(User user) { ... } }
```

**O — Open/Closed Principle (OCP)**:
- Open for extension, Closed for modification
- Thêm tính năng mới bằng cách **thêm code** (new class), không **sửa code** cũ

```java
// ✅ Dùng Strategy pattern để mở rộng mà không sửa code cũ
public interface DiscountStrategy {
    double applyDiscount(double price);
}
public class RegularDiscount implements DiscountStrategy {
    public double applyDiscount(double price) { return price * 0.9; }
}
public class VIPDiscount implements DiscountStrategy {
    public double applyDiscount(double price) { return price * 0.7; }
}
// Thêm loại discount mới → thêm class mới, không sửa code cũ
```

**L — Liskov Substitution Principle (LSP)**:
- Subclass phải có thể thay thế parent class mà **không làm hỏng** chương trình
- Ví dụ vi phạm kinh điển: `Square extends Rectangle` — gọi `setWidth()` trên Square sẽ phá vỡ expectation

**I — Interface Segregation Principle (ISP)**:
- Không ép client implement interface mà họ không dùng
- Chia interface lớn thành nhiều interface nhỏ, chuyên biệt

```java
// ❌ Vi phạm ISP: Bird buộc phải implement swim()
public interface Animal { void fly(); void swim(); void walk(); }

// ✅ Tách interface
public interface Flyable { void fly(); }
public interface Swimmable { void swim(); }
public interface Walkable { void walk(); }
public class Eagle implements Flyable, Walkable { ... }
```

**D — Dependency Inversion Principle (DIP)**:
- High-level modules không phụ thuộc low-level modules → cả hai phụ thuộc abstractions
- Abstractions không phụ thuộc details → details phụ thuộc abstractions

```java
// ❌ High-level phụ thuộc trực tiếp low-level
public class OrderService {
    private MySQLOrderRepository repo = new MySQLOrderRepository();
}

// ✅ Phụ thuộc abstraction
public class OrderService {
    private final OrderRepository repo; // interface
    public OrderService(OrderRepository repo) { this.repo = repo; } // inject
}
```

#### 1.4. Composition over Inheritance

- Inheritance tạo **tight coupling** giữa parent và child → thay đổi parent ảnh hưởng tất cả child
- Composition: class chứa reference đến object khác thay vì kế thừa
- **Nguyên tắc**: Dùng inheritance khi IS-A thực sự, dùng composition khi HAS-A

```java
// Composition: Engine là một phần của Car, không phải Car IS-A Engine
public class Car {
    private final Engine engine; // HAS-A
    private final Transmission transmission;

    public Car(Engine engine, Transmission transmission) {
        this.engine = engine;
        this.transmission = transmission;
    }

    public void drive() {
        engine.start();
        transmission.engage();
    }
}
```

#### 1.5. Method Overloading vs Overriding

| | Overloading | Overriding |
|---|---|---|
| **Thời điểm** | Compile-time (static) | Runtime (dynamic) |
| **Điều kiện** | Cùng tên, **khác** parameter list | Cùng tên, **cùng** parameter list |
| **Return type** | Có thể khác | Phải cùng hoặc **covariant** (subtype) |
| **Access modifier** | Có thể khác | Không được restrictive hơn parent |
| **Exceptions** | Có thể khác | Không được throw **broader** checked exception |
| **static/final/private** | Có thể overload | KHÔNG thể override |

```java
// Covariant return type (Java 5+)
public class Animal {
    public Animal create() { return new Animal(); }
}
public class Dog extends Animal {
    @Override
    public Dog create() { return new Dog(); } // covariant: Dog là subtype của Animal
}
```

#### 1.6. Object Class Methods

Mọi class trong Java đều kế thừa từ `java.lang.Object`:

- **`equals(Object o)`**: So sánh logical equality. Phải tuân thủ contract: reflexive, symmetric, transitive, consistent, null → false
- **`hashCode()`**: Trả về hash code. **Contract**: nếu `a.equals(b)` thì `a.hashCode() == b.hashCode()` (ngược lại không bắt buộc)
- **`toString()`**: Trả về string representation, mặc định: `ClassName@hexHashCode`
- **`clone()`**: Tạo shallow copy. Class phải implement `Cloneable`, nếu không → `CloneNotSupportedException`
- **`finalize()`**: Deprecated từ Java 9. Gọi trước khi GC thu hồi object. KHÔNG nên dùng → dùng `try-with-resources` thay thế
- **`wait() / notify() / notifyAll()`**: Dùng cho thread synchronization, phải gọi trong `synchronized` block
- **`getClass()`**: Trả về runtime class (`Class<?>` object)

> **Quy tắc quan trọng**: Khi override `equals()` → PHẢI override `hashCode()`.
> Nếu không, các collections dựa trên hash (HashMap, HashSet) sẽ hoạt động sai.

```java
public class Student {
    private final String id;
    private final String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(id, student.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // phải consistent với equals
    }
}
```

**⚖️ Kế thừa vs Composition — vì sao "ưu tiên composition"?**

| | Kế thừa (`extends`) | Composition (chứa object khác) |
| :--- | :--- | :--- |
| Quan hệ | IS-A, cố định lúc compile | HAS-A, thay được lúc runtime |
| Coupling | Chặt — lớp con phụ thuộc chi tiết cài đặt của lớp cha (*fragile base class*) | Lỏng — chỉ phụ thuộc interface |
| Tái sử dụng | Chỉ 1 lớp cha | Ghép nhiều thành phần |
| Khi dùng | Quan hệ "là" thật sự và ổn định (`IOException` là `Exception`) | Hầu hết trường hợp còn lại |

✅ **Vì sao chọn composition làm mặc định:** lớp cha đổi một method nội bộ có thể làm hỏng lớp con mà không ai biết; composition + interface cho phép test bằng mock và thay cài đặt (Strategy, Decorator) — cũng là cách Spring DI hoạt động.

> 🛠 **Áp dụng:**
> - Service phụ thuộc **interface** + inject qua constructor (DIP) → test được, thay được cài đặt (MySQL ↔ PostgreSQL, gửi email thật ↔ giả).
> - Thấy chuỗi `if/else` theo loại (`if type == "VIP"...`) mọc thêm mỗi khi có loại mới → Strategy/polymorphism (OCP).
> - Override `equals` thì **luôn** override `hashCode` — nếu không, object sẽ "mất tích" trong `HashMap/HashSet`.

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao `Square extends Rectangle` vi phạm Liskov?**
   → Code dùng `Rectangle` mong đợi `setWidth` không đổi chiều cao; `Square` buộc phải đổi cả hai → thay lớp con vào làm hỏng giả định của nơi dùng.
2. **Interface có `default` method rồi thì còn cần abstract class không?**
   → Có, khi cần **trạng thái** (field) hoặc constructor dùng chung; interface không có instance state.
</details>


---

### 2. Java Type System

> 🎯 **Bài toán:** Cần kiểu dữ liệu vừa **nhanh** (tính toán số không tạo object) vừa dùng được trong thế giới hướng đối tượng (collection, generics chỉ chứa object).
>
> 💡 **Ý tưởng giải:** Java có hai thế giới: **primitive** (giá trị thô, nằm trực tiếp trong biến/field, không có `null`) và **reference** (object trên heap). **Wrapper + autoboxing** là cây cầu giữa hai thế giới — tiện lợi nhưng có cái giá: tạo object, có thể `null`, và `==` so sánh địa chỉ. `String` được thiết kế **bất biến** vì được chia sẻ ở khắp nơi (pool, key của map, tham số bảo mật).


#### 2.1. Primitives (Kiểu nguyên thủy)

| Type | Size | Default | Range |
|------|------|---------|-------|
| `byte` | 1 byte | 0 | -128 → 127 |
| `short` | 2 bytes | 0 | -32,768 → 32,767 |
| `int` | 4 bytes | 0 | -2³¹ → 2³¹-1 (~2.1 tỷ) |
| `long` | 8 bytes | 0L | -2⁶³ → 2⁶³-1 |
| `float` | 4 bytes | 0.0f | ~±3.4 × 10³⁸ (7 digits precision) |
| `double` | 8 bytes | 0.0d | ~±1.7 × 10³⁰⁸ (15 digits precision) |
| `char` | 2 bytes | '\u0000' | 0 → 65,535 (Unicode) |
| `boolean` | ~1 byte* | false | true / false |

> *boolean size phụ thuộc JVM implementation, thường được lưu dưới dạng int (4 bytes) trong stack

#### 2.2. Wrapper Classes, Autoboxing, Integer Cache

```java
// Autoboxing: primitive → Wrapper (compiler thêm Integer.valueOf())
Integer a = 42;        // autoboxing: Integer.valueOf(42)
int b = a;             // unboxing: a.intValue()

// ⚠️ Integer Cache: -128 → 127 được cache
Integer x = 127;
Integer y = 127;
System.out.println(x == y);  // true (cùng object từ cache)

Integer m = 128;
Integer n = 128;
System.out.println(m == n);  // false (khác object, ngoài cache range)
System.out.println(m.equals(n)); // true (so sánh giá trị)
```

> **Tại sao có Integer Cache?**
> Các giá trị nhỏ (-128 → 127) được dùng rất thường xuyên. Cache giúp giảm object allocation.
> Cache này áp dụng cho: `Byte`, `Short`, `Integer`, `Long` (-128 → 127), `Character` (0 → 127), `Boolean` (`TRUE`/`FALSE`).
> `Float` và `Double` KHÔNG có cache. Riêng cận trên của `Integer` cache có thể tăng bằng `-XX:AutoBoxCacheMax=<n>`.
> ⚠️ Chỉ áp dụng khi boxing qua `valueOf()`/autoboxing; `new Integer(127)` (deprecated từ Java 9, *for removal* từ Java 16) luôn tạo object mới.
> ⚠️ Bẫy `NullPointerException` khi unboxing: `Integer x = map.get("missing"); int y = x;` → NPE.

#### 2.3. String

**Immutability**: String objects không thể thay đổi sau khi tạo. Mọi operation tạo String mới.

```java
String s1 = "Hello";    // String pool (nằm trong heap từ Java 7, trước đó ở PermGen)
String s2 = "Hello";    // cùng reference với s1 (từ pool)
String s3 = new String("Hello"); // new object trên heap, KHÔNG từ pool

System.out.println(s1 == s2);    // true (cùng pool reference)
System.out.println(s1 == s3);    // false (khác object)
System.out.println(s1.equals(s3)); // true (cùng nội dung)

String s4 = s3.intern(); // đưa vào pool, trả về pool reference
System.out.println(s1 == s4);    // true
```

**String vs StringBuilder vs StringBuffer**:

| | String | StringBuilder | StringBuffer |
|---|---|---|---|
| **Mutable** | ❌ Immutable | ✅ Mutable | ✅ Mutable |
| **Thread-safe** | ✅ (immutable) | ❌ | ✅ (synchronized) |
| **Performance** | Chậm (tạo object mới) | Nhanh nhất | Chậm hơn SB (do sync) |
| **Use case** | Ít thay đổi | Single-thread concat nhiều | Multi-thread (hiếm dùng) |

> **Tại sao String immutable?**
> 1. **String Pool**: Nhiều reference cùng share 1 object → nếu mutable → thay đổi ảnh hưởng tất cả
> 2. **Security**: Username, password, connection URL thường là String → immutable = an toàn
> 3. **Thread-safety**: Immutable tự nhiên thread-safe
> 4. **hashCode caching**: String cache hashCode (lazy computation) vì immutable → dùng làm HashMap key hiệu quả

**String internals cần biết**:
- **Compact Strings (Java 9, JEP 254)**: `String` lưu `byte[]` + cờ `coder` thay vì `char[]` → chuỗi Latin-1 chỉ tốn 1 byte/ký tự (tiết kiệm ~50% heap cho phần lớn ứng dụng).
- **Nối chuỗi bằng `+`** (Java 9+, JEP 280): compiler sinh `invokedynamic` → `StringConcatFactory` tối ưu tại runtime. Trong **vòng lặp** vẫn phải dùng `StringBuilder` (mỗi vòng `+` tạo object mới).
- **Password nên để `char[]`** thay vì `String`: String nằm trong heap/pool tới khi GC, không xóa được chủ động và dễ lộ trong heap dump; `char[]` có thể ghi đè bằng 0 sau khi dùng.
- **Text API mới**: `isBlank()`, `strip()`, `lines()`, `repeat()` (Java 11); `formatted()`, `stripIndent()` (Java 15).

#### 2.4. == vs equals() vs hashCode()

```java
// == so sánh REFERENCE (address), equals() so sánh VALUE (logical equality)
String a = new String("test");
String b = new String("test");
a == b       // false: khác object
a.equals(b)  // true: cùng nội dung

// hashCode() contract:
// 1. Nếu a.equals(b) → a.hashCode() == b.hashCode() (BẮT BUỘC)
// 2. Nếu a.hashCode() == b.hashCode() → a.equals(b) KHÔNG bắt buộc (collision)
// 3. hashCode() phải consistent trong cùng 1 run
```

#### 2.5. Pass-by-value

Java luôn là **pass-by-value**. Với object, Java pass **copy of reference** (không phải reference gốc):

```java
public void changeRef(StringBuilder sb) {
    sb = new StringBuilder("New"); // thay đổi LOCAL copy of reference → không ảnh hưởng bên ngoài
}

public void changeContent(StringBuilder sb) {
    sb.append(" World"); // thay đổi OBJECT mà reference trỏ tới → ảnh hưởng bên ngoài
}

StringBuilder sb = new StringBuilder("Hello");
changeRef(sb);      // sb vẫn là "Hello"
changeContent(sb);  // sb thành "Hello World"
```

#### 2.6. Enum

```java
public enum OrderStatus {
    // Các constant PHẢI khai báo đầu tiên trong enum
    PENDING("Chờ xử lý") {
        @Override public boolean isFinal() { return false; }
    },
    PROCESSING("Đang xử lý") {
        @Override public boolean isFinal() { return false; }
    },
    COMPLETED("Hoàn thành") {
        @Override public boolean isFinal() { return true; }
    },
    CANCELLED("Đã hủy") {
        @Override public boolean isFinal() { return true; }
    };

    private final String description;
    OrderStatus(String description) { this.description = description; } // constructor luôn private
    public String getDescription() { return description; }

    // Enum có thể có abstract method → MỌI constant đều phải implement (constant-specific body)
    public abstract boolean isFinal();
}

// Enum internals:
// - Mỗi enum constant là một SINGLETON instance
// - ordinal(): vị trí (0, 1, 2, ...)
// - valueOf("PENDING"): parse từ String
// - values(): array tất cả constants
// - Enum implement Comparable (theo ordinal)
// - Enum CÓ THỂ implement interface, KHÔNG THỂ extend class (đã ngầm extends java.lang.Enum)
// - So sánh enum bằng == là an toàn (singleton)
// - Dùng EnumMap / EnumSet thay HashMap/HashSet khi key là enum (nhanh & gọn hơn)
// - ⚠️ Không persist ordinal() xuống DB (JPA @Enumerated(EnumType.ORDINAL)) — thêm/đổi thứ tự constant là hỏng dữ liệu → dùng EnumType.STRING
```

#### 2.7. var (Java 10) — Local Variable Type Inference

```java
var list = new ArrayList<String>();  // compiler infer: ArrayList<String>
var stream = list.stream();          // compiler infer: Stream<String>
var entry = Map.entry("key", 1);     // compiler infer: Map.Entry<String, Integer>

// Chỉ dùng cho LOCAL VARIABLE:
// ❌ Không dùng cho: field, method parameter, return type
// ❌ var x = null;        // không infer được
// ❌ var arr = {1, 2, 3}; // không infer được
// ✅ var x = (String) null; // OK, infer String
```

> 🛠 **Áp dụng:**
> - So sánh wrapper/String luôn dùng `equals()`; `==` chỉ "tình cờ đúng" trong khoảng cache −128..127.
> - Vòng lặp nóng dùng primitive (`int`, `long[]`, `IntStream`) thay vì `Integer`/`List<Integer>` để tránh tạo hàng triệu object.
> - Cẩn thận NPE khi unboxing giá trị `null` từ `Map.get()` hay từ DB.
> - Java luôn **pass-by-value**: truyền object là truyền *bản sao của reference* → sửa nội dung object thì bên ngoài thấy, gán biến sang object khác thì không.

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao `Integer a = 127, b = 127; a == b` là `true` nhưng với 128 lại `false`?**
   → Autoboxing gọi `Integer.valueOf`, trả object cache sẵn cho −128..127; ngoài khoảng đó tạo object mới, `==` so sánh địa chỉ.
2. **Vì sao String bất biến lại giúp dùng làm key của HashMap an toàn?**
   → `hashCode` không bao giờ đổi sau khi đưa vào map (và được cache), nên entry không bao giờ "lạc" bucket.
</details>


---

### 3. Collections Framework

> 🎯 **Bài toán:** Hầu như mọi chương trình đều cần lưu nhóm phần tử và tra cứu/duyệt/sắp xếp chúng. Tự viết lại mỗi lần vừa tốn công vừa dễ sai.
>
> 💡 **Ý tưởng giải:** Một bộ **interface** chuẩn (`List`, `Set`, `Map`, `Queue`, `Deque`) tách khỏi **cài đặt** (`ArrayList`, `HashMap`, `TreeMap`...). Code phụ thuộc interface, chọn cài đặt theo **mẫu truy cập**: truy cập theo vị trí, tra theo khóa, cần thứ tự, cần sắp xếp, hay cần an toàn đa luồng.

**⚖️ Chọn collection theo bài toán**

| Bài toán | Chọn | Vì sao |
| :--- | :--- | :--- |
| Danh sách, duyệt/truy cập theo index | `ArrayList` | Mảng liền mạch → cache CPU tốt; `LinkedList` hiếm khi nhanh hơn trên thực tế |
| Tra cứu theo khóa | `HashMap` | O(1) trung bình |
| Cần giữ thứ tự chèn / LRU cache | `LinkedHashMap` | Hash + danh sách liên kết |
| Cần khóa sắp xếp, tìm `floor/ceiling`, range | `TreeMap` | Red-Black tree O(log n) |
| Hàng đợi / stack | `ArrayDeque` | Mảng vòng, nhanh hơn `Stack`/`LinkedList` |
| Lấy phần tử nhỏ nhất liên tục (top-K, lịch) | `PriorityQueue` | Binary heap |
| Đa luồng, tra cứu theo khóa | `ConcurrentHashMap` | Khóa theo bucket, không khóa toàn bộ |
| Khóa là enum | `EnumMap` / `EnumSet` | Mảng/bit vector theo ordinal |


#### 3.1. Collection Hierarchy

```
                    Iterable<E>
                        │
                    Collection<E>
                   ╱       │       ╲
               List<E>   Set<E>   Queue<E>
                │          │         │
           ArrayList    HashSet    PriorityQueue
           LinkedList   TreeSet    ArrayDeque
           Vector       LinkedHashSet
           
                    Map<K,V>  (KHÔNG extends Collection)
                   ╱    │    ╲
            HashMap  TreeMap  LinkedHashMap
            ConcurrentHashMap  WeakHashMap
```

#### 3.2. List Implementations

**ArrayList — Dynamic Array**:
- Backed by `Object[]` array
- **Initial capacity**: 10 (default), hoặc chỉ định qua constructor
- **Grow strategy**: Khi full → `newCapacity = oldCapacity + (oldCapacity >> 1)` = **1.5x**
  - Copy array cũ sang array mới bằng `System.arraycopy()` hoặc `Arrays.copyOf()`
- **Performance**: `get(i)` O(1), `add(e)` amortized O(1), `add(i, e)` O(n), `remove(i)` O(n)
- **Không thread-safe**

```java
// Bên trong ArrayList (simplified)
public class ArrayList<E> {
    transient Object[] elementData;
    private int size;

    public E get(int index) {
        rangeCheck(index);
        return (E) elementData[index]; // O(1) random access
    }

    public boolean add(E e) {
        ensureCapacityInternal(size + 1); // grow nếu cần
        elementData[size++] = e;
        return true;
    }

    private void grow(int minCapacity) {
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1); // 1.5x
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
}
```

**LinkedList — Doubly Linked List**:
- Implements cả `List` và `Deque`
- Mỗi element là `Node { E item; Node<E> prev; Node<E> next; }`
- **Performance**: `get(i)` O(n), `add(e)` O(1), `addFirst/Last` O(1), `remove` O(1) nếu có node reference
- **Khi nào dùng**: Rất hiếm dùng trong practice. ArrayList thường nhanh hơn do CPU cache locality

**Vector**: Giống ArrayList nhưng `synchronized`. Grow 2x thay vì 1.5x. **Legacy**, dùng `Collections.synchronizedList()` hoặc `CopyOnWriteArrayList` thay thế.

#### 3.3. Set Implementations

**HashSet**:
- Backed by `HashMap` internally: `map.put(element, PRESENT)` với `PRESENT = new Object()`
- **Performance**: add/remove/contains O(1) average
- **Không giữ thứ tự**, cho phép `null`

**LinkedHashSet**:
- Extends HashSet, backed by `LinkedHashMap`
- Giữ **insertion order** bằng doubly-linked list xuyên qua các entry
- Hơi chậm hơn HashSet do maintain linked list

**TreeSet**:
- Backed by `TreeMap` (Red-Black tree)
- **Sorted** theo natural ordering hoặc `Comparator`
- **Performance**: add/remove/contains O(log n)
- Implements `NavigableSet`: `ceiling()`, `floor()`, `higher()`, `lower()`, `subSet()`
- KHÔNG cho phép `null` (cần compare)

**EnumSet**:
- Tối ưu cho enum values
- Internally dùng **bit vector** (long hoặc long[])
- Cực kỳ nhanh và compact

#### 3.4. Map Implementations — HashMap Deep Dive

> 🎯 **Bài toán:** Tra cứu theo khóa trong O(1) cho khóa bất kỳ.
>
> 💡 **Ý tưởng:** Biến khóa thành số (`hashCode`) → chỉ số mảng (bucket). Các quyết định thiết kế bên dưới đều trả lời một câu hỏi "vì sao":
> - **Vì sao capacity luôn là lũy thừa của 2?** Để tính `index = hash & (n-1)` (một phép AND) thay cho phép chia lấy dư chậm hơn.
> - **Vì sao phải `h ^ (h >>> 16)`?** Vì `& (n-1)` chỉ dùng vài bit thấp; trộn bit cao xuống để các hashCode chỉ khác nhau ở bit cao không bị dồn cùng bucket.
> - **Vì sao load factor 0.75?** Cân bằng giữa tốn bộ nhớ (thấp quá) và nhiều va chạm (cao quá).
> - **Vì sao chuyển sang cây đỏ-đen khi bucket ≥ 8?** Chống trường hợp xấu (hashCode tệ hoặc bị tấn công cố tình va chạm) làm tra cứu thành O(n); 8 là ngưỡng mà theo phân phối Poisson gần như không xảy ra với hash tốt.


**Cấu trúc nội bộ HashMap**:

```
HashMap (Java 8+):
┌─────────────────────────────────────────────┐
│  Node<K,V>[] table (bucket array)           │
│  ┌──────┬──────┬──────┬──────┬──────┐       │
│  │  [0] │  [1] │  [2] │  [3] │ ...  │       │
│  └──┬───┴──────┴──┬───┴──────┴──────┘       │
│     │              │                         │
│     ▼              ▼                         │
│   Node           Node → Node → Node         │
│   (single)       (linked list, < 8 nodes)    │
│                                              │
│   Khi chain ≥ TREEIFY_THRESHOLD (8)          │
│   VÀ table.length ≥ MIN_TREEIFY_CAPACITY(64)│
│   → chuyển thành Red-Black TreeNode          │
│                                              │
│   Khi tree shrink ≤ UNTREEIFY_THRESHOLD (6)  │
│   → chuyển lại thành linked list             │
└─────────────────────────────────────────────┘
```

**Các thông số quan trọng**:
- `DEFAULT_INITIAL_CAPACITY = 16` (luôn là power of 2)
- `DEFAULT_LOAD_FACTOR = 0.75f`
- `TREEIFY_THRESHOLD = 8` (chuyển list → tree)
- `UNTREEIFY_THRESHOLD = 6` (chuyển tree → list)
- `MIN_TREEIFY_CAPACITY = 64` (table phải đủ lớn mới treeify)
- `threshold = capacity × loadFactor` (khi size > threshold → resize double)

**Hash spreading** (tại sao cần):

```java
// HashMap.hash() — Java 8+
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
// XOR 16 bit cao với 16 bit thấp → phân phối đều hơn
// Vì index = hash & (n-1), nếu n nhỏ (16, 32) → chỉ dùng vài bit thấp
// → spreading giúp sử dụng thông tin từ bit cao

// Tính bucket index:
// index = hash(key) & (table.length - 1)  ← bitwise AND thay vì modulo (nhanh hơn)
// Chỉ hoạt động vì table.length luôn là power of 2
```

**put() flow chi tiết**:
1. Tính `hash = hash(key)` (spread hashCode)
2. Tính `index = hash & (n-1)` → bucket
3. Nếu bucket trống → tạo `new Node` và đặt vào
4. Nếu bucket có node:
   a. So sánh `hash` và `key.equals()` với node đầu → nếu match → replace value
   b. Nếu node là `TreeNode` → gọi `putTreeVal()` (Red-Black tree insert)
   c. Nếu node là `Node` (linked list) → duyệt đến cuối:
      - Nếu tìm thấy key match → replace value
      - Nếu đến cuối → append new node, kiểm tra nếu chain length ≥ 8 → `treeifyBin()`
5. Tăng `size`, nếu `size > threshold` → `resize()` (double capacity, rehash toàn bộ)

**get() flow chi tiết**:
1. Tính `hash = hash(key)`, `index = hash & (n-1)`
2. Kiểm tra first node trong bucket
3. Nếu match → return
4. Nếu `TreeNode` → `getTreeNode()` — O(log n)
5. Nếu linked list → duyệt tuần tự — O(n) worst case

> **Null handling**: HashMap cho phép **1 null key** (luôn vào bucket 0) và **nhiều null values**.
> TreeMap và ConcurrentHashMap KHÔNG cho phép null key.

**LinkedHashMap** — HashMap + Doubly-Linked List:
- Mỗi entry có thêm `before` và `after` pointer
- Giữ **insertion order** (mặc định) hoặc **access order** (khi `accessOrder = true`)
- Khi `accessOrder = true`: mỗi lần `get()` → move entry lên cuối → **LRU Cache** tự nhiên

```java
// LRU Cache implementation dùng LinkedHashMap
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;

    public LRUCache(int maxSize) {
        super(maxSize, 0.75f, true); // accessOrder = true
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize; // auto-remove oldest when exceed capacity
    }
}
```

**TreeMap**: Red-Black tree, `NavigableMap`, O(log n) cho tất cả operations. Dùng khi cần sorted keys.

**WeakHashMap**: Keys là `WeakReference`. Khi key không còn strong reference → GC có thể thu hồi → entry tự động bị remove. Dùng cho cache mà không muốn prevent GC.

#### 3.5. Queue/Deque

**PriorityQueue**:
- Binary heap (min-heap mặc định)
- `offer()` / `poll()` / `peek()` — O(log n) / O(log n) / O(1)
- Không thread-safe → dùng `PriorityBlockingQueue` cho concurrent

**ArrayDeque**:
- Circular array, nhanh hơn `Stack` (class) và `LinkedList` cho stack/queue operations
- `addFirst` / `addLast` / `removeFirst` / `removeLast` — amortized O(1)
- Grow: double capacity khi đầy
- **Khuyến nghị**: Dùng ArrayDeque thay cho Stack class (legacy)

#### 3.6. Comparable vs Comparator

```java
// Comparable — natural ordering, implement TRONG class
public class Student implements Comparable<Student> {
    private String name;
    private int gpa;

    @Override
    public int compareTo(Student other) {
        return Integer.compare(this.gpa, other.gpa); // sort by GPA
    }
}

// Comparator — external ordering, có thể có nhiều chiến lược sort
Comparator<Student> byName = Comparator.comparing(Student::getName);
Comparator<Student> byGpaThenName = Comparator
    .comparingInt(Student::getGpa).reversed()
    .thenComparing(Student::getName);

students.sort(byGpaThenName);
```

#### 3.7. Fail-fast vs Fail-safe Iterator

- **Fail-fast**: `ArrayList`, `HashMap`, `HashSet` — detect structural modification → throw `ConcurrentModificationException`
  - Check bằng `modCount` field
  - Chỉ detect modification qua iterator ngoài (e.g., `list.remove()` trong for-each loop)
- **Fail-safe**: `ConcurrentHashMap`, `CopyOnWriteArrayList` — iterate trên copy/snapshot → KHÔNG throw exception
  - Có thể thấy hoặc không thấy modifications xảy ra sau khi iterator được tạo

```java
// ❌ Fail-fast: ConcurrentModificationException
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
for (String s : list) {
    if (s.equals("b")) list.remove(s); // BOOM!
}

// ✅ Cách đúng: dùng Iterator.remove()
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("b")) it.remove(); // OK
}

// ✅ Hoặc dùng removeIf() (Java 8)
list.removeIf(s -> s.equals("b"));
```

#### 3.8. Bảng độ phức tạp & các bẫy thường gặp

| Collection | get/contains | add | remove | Thứ tự | Null | Thread-safe |
|---|---|---|---|---|---|---|
| `ArrayList` | O(1) theo index / O(n) contains | O(1)* | O(n) | Insertion | ✅ | ❌ |
| `LinkedList` | O(n) | O(1) đầu/cuối | O(1) nếu có node | Insertion | ✅ | ❌ |
| `HashMap/HashSet` | O(1) TB, O(log n) worst (treeified) | O(1) | O(1) | Không | ✅ 1 null key | ❌ |
| `LinkedHashMap` | O(1) | O(1) | O(1) | Insertion/Access | ✅ | ❌ |
| `TreeMap/TreeSet` | O(log n) | O(log n) | O(log n) | Sorted | ❌ key | ❌ |
| `PriorityQueue` | O(n) contains, O(1) peek | O(log n) | O(log n) poll | Heap | ❌ | ❌ |
| `ConcurrentHashMap` | O(1) | O(1) | O(1) | Không | ❌ | ✅ |
| `CopyOnWriteArrayList` | O(1) | **O(n)** (copy) | O(n) | Insertion | ✅ | ✅ |

**Các bẫy hay gặp**:
- **Immutable collections** `List.of()`, `Set.of()`, `Map.of()` (Java 9): **không cho phép `null`** (NPE ngay khi tạo hoặc khi `contains(null)`), mọi thao tác sửa → `UnsupportedOperationException`; `Set.of`/`Map.of` có thứ tự duyệt **ngẫu nhiên theo mỗi lần chạy JVM** — đừng phụ thuộc thứ tự.
- **`Collections.unmodifiableList(list)`** chỉ là **view** read-only: list gốc thay đổi thì view thay đổi theo. `List.copyOf()` (Java 10) mới tạo bản sao bất biến thật.
- **`Arrays.asList(arr)`**: fixed-size (gọi `add/remove` → `UnsupportedOperationException`), ghi qua `set()` sẽ sửa luôn mảng gốc; `Arrays.asList(new int[]{1,2})` cho ra `List<int[]>` 1 phần tử (không autobox mảng primitive).
- **`stream().toList()`** (Java 16) trả list **bất biến**, còn `Collectors.toList()` hiện trả `ArrayList` (không cam kết trong spec).
- **Key mutable trong HashMap**: sửa field tham gia `hashCode()` sau khi `put` → không bao giờ `get` lại được (nằm sai bucket) → key nên là immutable (String, record, enum).
- **Resize HashMap (Java 8)**: khi gấp đôi capacity, mỗi node trong bucket `i` chỉ có thể sang bucket `i` hoặc `i + oldCap` (dựa vào 1 bit `hash & oldCap`) → tách thành 2 list "lo/hi" giữ nguyên thứ tự, không cần tính lại hash. Java 7 chèn vào đầu list khi resize → **resize đồng thời từ nhiều thread có thể tạo vòng lặp vô hạn** (CPU 100%) — một lý do nữa để không dùng HashMap đa luồng.
- **Khởi tạo capacity** khi biết trước số phần tử: `new HashMap<>(n)` vẫn có thể resize vì threshold = capacity × 0.75 → Java 19+ dùng `HashMap.newHashMap(n)` (tự tính capacity đủ chứa n phần tử).

---

### 4. Generics

> 🎯 **Bài toán:** Trước Java 5, `List` chứa `Object` → lấy ra phải ép kiểu, nhét nhầm kiểu chỉ phát hiện khi chạy (`ClassCastException`).
>
> 💡 **Ý tưởng giải:** **Generics** đưa kiểm tra kiểu lên lúc compile (`List<String>`).
>
> ⚖️ **Hai cách cài đặt generics:** *Reification* (C#: mỗi `List<int>` là một kiểu riêng lúc runtime) vs *Type erasure* (Java: compiler kiểm tra rồi **xóa** thông tin kiểu, bytecode chỉ còn `List`).
>
> ✅ **Vì sao Java chọn erasure:** **tương thích ngược** — bytecode và thư viện viết trước Java 5 (dùng raw `List`) vẫn chạy cùng code mới mà không phải biên dịch lại. Cái giá: không `new T()`, không `instanceof List<String>`, không overload theo tham số generic khác nhau.


#### 4.1. Type Erasure

- Java Generics được implement bằng **type erasure**: tất cả generic type info bị xóa tại compile time
- `List<String>` và `List<Integer>` → cùng bytecode `List` (raw type)
- Compiler thêm cast tự động khi cần

```java
// Source code:
List<String> list = new ArrayList<>();
String s = list.get(0);

// Sau erasure (bytecode):
List list = new ArrayList();     // raw type
String s = (String) list.get(0); // compiler thêm cast
```

**Hệ quả của type erasure**:
- Không thể `new T()`, `new T[]`, `instanceof T`
- Không thể overload `method(List<String>)` và `method(List<Integer>)` — cùng erasure
- Không thể dùng primitive trong generic: `List<int>` ❌ → `List<Integer>` ✅

#### 4.2. Bounded Type Parameters

```java
// Upper bound: T phải là Number hoặc subclass
public <T extends Number> double sum(List<T> list) {
    return list.stream().mapToDouble(Number::doubleValue).sum();
}

// Multiple bounds: T phải implement cả Comparable VÀ Serializable
public <T extends Comparable<T> & Serializable> T max(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
}

// Recursive type bound
public <T extends Comparable<T>> T max(Collection<T> coll) {
    return Collections.max(coll);
}
```

#### 4.3. Wildcards & PECS

**PECS: Producer Extends, Consumer Super**

```java
// ? extends T (upper bound wildcard) — PRODUCER: chỉ ĐỌC, không GHI
public double sumOfList(List<? extends Number> list) {
    // ✅ Có thể đọc: Number n = list.get(0);
    // ❌ Không thể ghi: list.add(1); // compile error
    return list.stream().mapToDouble(Number::doubleValue).sum();
}
sumOfList(List.of(1, 2, 3));       // List<Integer> → OK
sumOfList(List.of(1.0, 2.0));     // List<Double>  → OK

// ? super T (lower bound wildcard) — CONSUMER: chỉ GHI, đọc thì chỉ được Object
public void addNumbers(List<? super Integer> list) {
    // ✅ Có thể ghi: list.add(1); list.add(2);
    // ⚠️ Đọc chỉ được Object: Object o = list.get(0);
    list.add(1);
    list.add(2);
}
addNumbers(new ArrayList<Number>());  // List<Number>  → OK
addNumbers(new ArrayList<Object>());  // List<Object>  → OK

// Ví dụ thực tế: Collections.copy
public static <T> void copy(List<? super T> dest, List<? extends T> src) {
    // src là PRODUCER → extends
    // dest là CONSUMER → super
}
```

> **Ghi nhớ PECS**:
> - `? extends T`: "Tôi produce items kiểu T" → chỉ lấy ra (read-only)
> - `? super T`: "Tôi consume items kiểu T" → chỉ đưa vào (write-only)
> - `?` (unbounded): chỉ đọc được Object, không ghi được (trừ null)

> 🛠 **Áp dụng:**
> - API nhận dữ liệu để **đọc** → `? extends T`; nhận để **ghi vào** → `? super T` (**PECS**) → API linh hoạt hơn cho người gọi (vd `Collections.copy`).
> - Cần biết kiểu lúc runtime (deserialize JSON thành `List<User>`) → truyền `Class<T>` hoặc *type token* (`new TypeReference<List<User>>(){}` của Jackson) vì kiểu đã bị xóa.

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao không thêm được phần tử vào `List<? extends Number>`?**
   → List thật có thể là `List<Integer>` hay `List<Double>`; compiler không biết nên cấm mọi phần tử (trừ `null`) để giữ an toàn kiểu.
2. **Vì sao `TypeReference<List<User>>(){}` của Jackson lấy được kiểu generic dù có erasure?**
   → Lớp con ẩn danh lưu kiểu cha có tham số trong metadata của class (generic superclass) — erasure không xóa thông tin ở khai báo class.
</details>


---

### 5. Exception Handling

> 🎯 **Bài toán:** Lỗi xảy ra ở tầng sâu (đọc file, gọi DB) nhưng tầng biết cách xử lý lại ở trên cao. Dùng mã lỗi trả về (kiểu C) thì mỗi tầng đều phải kiểm tra và truyền tiếp, rất dễ bỏ sót.
>
> 💡 **Ý tưởng giải:** **Exception** tách luồng lỗi khỏi luồng chính và tự "nổi" lên tới nơi có `catch`. Java chia làm hai loại: **checked** (compiler bắt buộc xử lý — lỗi có thể phục hồi, do môi trường) và **unchecked** (lỗi lập trình — không bắt buộc).
>
> ⚖️ **Tranh luận checked vs unchecked:** checked buộc người gọi nghĩ tới lỗi, nhưng làm chữ ký method lan truyền `throws` qua mọi tầng và không hợp với lambda. ✅ Xu hướng hiện nay (Spring, Kotlin, đa số thư viện mới) **ưu tiên unchecked**, bọc lỗi kỹ thuật thành exception có ý nghĩa nghiệp vụ và xử lý tập trung ở tầng ngoài (`@RestControllerAdvice`).


#### 5.1. Exception Hierarchy

```
                    Throwable
                   ╱         ╲
               Error         Exception
              (unchecked)   ╱         ╲
             ╱      ╲   RuntimeException  IOException (checked)
     OutOfMemory  StackOverflow  (unchecked)      SQLException (checked)
                              ╱      ╲
              NullPointerException  IllegalArgumentException
              ArrayIndexOutOfBounds  ClassCastException
              ArithmeticException   UnsupportedOperationException
```

- **Checked exceptions**: Compiler bắt buộc handle (try-catch hoặc throws). Ví dụ: `IOException`, `SQLException`
- **Unchecked exceptions** (`RuntimeException`): Compiler không bắt buộc handle. Ví dụ: `NullPointerException`, `IllegalArgumentException`
- **Errors**: JVM-level problems, KHÔNG nên catch. Ví dụ: `OutOfMemoryError`, `StackOverflowError`

#### 5.2. Try-with-resources (Java 7+)

```java
// Resource phải implement AutoCloseable
// close() được gọi tự động theo thứ tự NGƯỢC khai báo
try (var conn = getConnection();
     var stmt = conn.prepareStatement(sql);
     var rs = stmt.executeQuery()) {  // rs closed first, then stmt, then conn
    while (rs.next()) { ... }
} catch (SQLException e) {
    // Suppressed exceptions: nếu body throw exception VÀ close() cũng throw
    // → close() exception được attach dưới dạng "suppressed"
    // → lấy bằng e.getSuppressed()
}
```

#### 5.3. Best practices & các bẫy về Exception

```java
// ❌ 1. return trong finally "nuốt" mất exception và ghi đè giá trị return
int f() {
    try { throw new RuntimeException("boom"); }
    finally { return 1; }     // exception biến mất, method trả về 1
}

// ❌ 2. Nuốt exception
try { ... } catch (Exception e) { }            // lỗi biến mất không dấu vết
try { ... } catch (Exception e) { log.error(e.getMessage()); }  // mất stack trace

// ✅ Bọc lại nhưng GIỮ cause
try { ... } catch (SQLException e) {
    throw new DataAccessException("Không lưu được order " + orderId, e);  // truyền e làm cause
}

// ✅ Multi-catch (Java 7)
try { ... } catch (IOException | TimeoutException e) { ... }

// ⚠️ 3. InterruptedException: không được nuốt — khôi phục cờ interrupt
try { Thread.sleep(1000); }
catch (InterruptedException e) {
    Thread.currentThread().interrupt();   // để code phía trên biết thread đã bị yêu cầu dừng
    return;
}
```
- **Checked vs Unchecked — dùng khi nào?** Checked cho tình huống **phục hồi được** mà caller buộc phải xử lý (file không tồn tại, mạng lỗi). Unchecked cho **lỗi lập trình / vi phạm điều kiện** (null, tham số sai, trạng thái sai). Xu hướng hiện nay (Spring, Kotlin): ưu tiên unchecked, bọc checked thành unchecked có ý nghĩa nghiệp vụ.
- **Custom exception**: đặt tên theo nghiệp vụ (`InsufficientBalanceException`), mang mã lỗi/ngữ cảnh, có constructor nhận `cause`.
- **Không dùng exception cho luồng điều khiển bình thường** — tạo exception tốn kém vì phải chụp stack trace (`fillInStackTrace`). Exception dùng làm tín hiệu hiệu năng cao có thể tắt stack trace bằng constructor `(msg, cause, enableSuppression=false, writableStackTrace=false)`.
- **Catch cụ thể trước, tổng quát sau**; không `catch (Throwable)`/`catch (Error)` trừ ở tầng ngoài cùng (log & thoát).
- **Fail fast**: validate tham số ở đầu method (`Objects.requireNonNull(x, "x")`).

> 🛠 **Áp dụng:**
> - Luôn giữ **cause** khi bọc exception; không nuốt exception; không dùng exception cho luồng điều khiển bình thường.
> - `try-with-resources` cho mọi thứ cần đóng (stream, connection) — chống rò rỉ tài nguyên.
> - ⚠️ Liên quan Spring: `@Transactional` mặc định **không rollback** với checked exception (`SpringBoot.md` 8.2).

<details><summary>🧠 Tự kiểm tra</summary>

1. **`return` trong `finally` gây hậu quả gì?**
   → Ghi đè giá trị trả về và **nuốt mất** exception đang được ném từ `try`.
2. **Vì sao phải gọi lại `Thread.currentThread().interrupt()` khi bắt `InterruptedException`?**
   → Ném exception đã xóa cờ interrupt; khôi phục cờ để code phía trên biết thread được yêu cầu dừng.
</details>


---

### 6. Java 8 Features

> 🎯 **Bài toán:** Code xử lý tập dữ liệu (lọc, biến đổi, gom nhóm) bằng vòng lặp lồng nhau dài dòng, khó đọc, khó song song hóa; truyền "hành vi" (comparator, callback) phải viết anonymous class rườm rà.
>
> 💡 **Ý tưởng giải:** **Lambda** cho phép truyền hành vi gọn như truyền giá trị; **Stream** mô tả *muốn gì* (filter → map → collect) theo kiểu khai báo, chạy **lười** (chỉ thực thi khi có terminal operation) và gộp các bước thành **một lượt duyệt**. **Optional** biểu diễn tường minh "có thể không có kết quả" thay cho `null`.


#### 6.1. Lambda & Functional Interfaces

```java
// Lambda syntax
Runnable r = () -> System.out.println("Hello");
Comparator<String> comp = (a, b) -> a.length() - b.length();
Function<String, Integer> len = String::length; // method reference

// Core Functional Interfaces (java.util.function)
Function<T, R>        // T → R               apply(T t)
BiFunction<T, U, R>   // (T, U) → R          apply(T t, U u)
Predicate<T>          // T → boolean          test(T t)
Consumer<T>           // T → void             accept(T t)
Supplier<T>           // () → T               get()
UnaryOperator<T>      // T → T                apply(T t)  (extends Function<T,T>)
BinaryOperator<T>     // (T, T) → T           apply(T t1, T t2) (extends BiFunction<T,T,T>)
```

**Method References** (4 loại):

```java
// 1. Static method: ClassName::staticMethod
Function<String, Integer> parse = Integer::parseInt;

// 2. Instance method of particular object: obj::instanceMethod
String prefix = "Hello";
Function<String, String> concat = prefix::concat;

// 3. Instance method of arbitrary object: ClassName::instanceMethod
Function<String, Integer> len = String::length;
// → tương đương (String s) -> s.length()

// 4. Constructor: ClassName::new
Supplier<ArrayList<String>> factory = ArrayList::new;
Function<String, File> fileFactory = File::new;
```

#### 6.2. Stream API Deep Dive

```java
// Stream pipeline: Source → Intermediate ops (lazy) → Terminal op (triggers execution)

// ── Intermediate operations (lazy, return Stream) ──
.filter(Predicate)       // lọc elements
.map(Function)           // transform mỗi element
.flatMap(Function)       // 1 element → nhiều elements, flatten
.sorted()                // sort (natural order hoặc Comparator)
.distinct()              // loại bỏ duplicate (dựa trên equals())
.peek(Consumer)          // debug: nhìn mà không modify
.limit(n)                // lấy tối đa n elements (short-circuiting)
.skip(n)                 // bỏ qua n elements đầu
.takeWhile(Predicate)    // Java 9: lấy cho đến khi predicate false
.dropWhile(Predicate)    // Java 9: bỏ cho đến khi predicate false

// ── Terminal operations (trigger execution, return result) ──
.forEach(Consumer)       // duyệt mỗi element
.collect(Collector)      // gom kết quả vào collection
.reduce(BinaryOperator)  // gộp thành 1 giá trị
.count()                 // đếm
.min() / .max()          // tìm min/max → Optional
.anyMatch() / .allMatch() / .noneMatch()  // kiểm tra điều kiện
.findFirst() / .findAny()                 // tìm element → Optional
.toArray()               // → array
.toList()                // Java 16: unmodifiable list

// ── Collectors phổ biến ──
Collectors.toList()                          // → ArrayList
Collectors.toSet()                           // → HashSet
Collectors.toUnmodifiableList()              // → unmodifiable List (Java 10)
Collectors.toMap(keyMapper, valueMapper)     // → HashMap
Collectors.groupingBy(classifier)            // → Map<K, List<V>>
Collectors.partitioningBy(predicate)         // → Map<Boolean, List<V>>
Collectors.joining(", ")                     // → String
Collectors.counting()                        // → Long
Collectors.summarizingInt(mapper)            // → IntSummaryStatistics
Collectors.toMap(k, v, mergeFunction)        // handle duplicate keys
// ⚠️ toMap(k, v) KHÔNG có mergeFunction → IllegalStateException khi trùng key; value null → NPE
// ⚠️ Stream chỉ dùng được 1 lần — gọi terminal op lần 2 → IllegalStateException
```

**Ví dụ thực tế**:

```java
// Nhóm nhân viên theo phòng ban, đếm số người mỗi phòng
Map<String, Long> deptCount = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));

// Top 3 nhân viên lương cao nhất
List<Employee> top3 = employees.stream()
    .sorted(Comparator.comparingDouble(Employee::getSalary).reversed())
    .limit(3)
    .collect(Collectors.toList());

// flatMap: flatten nested collections
List<List<String>> nested = List.of(List.of("a", "b"), List.of("c", "d"));
List<String> flat = nested.stream()
    .flatMap(Collection::stream)  // Stream<List<String>> → Stream<String>
    .collect(Collectors.toList()); // ["a", "b", "c", "d"]
```

> **Lazy evaluation**: Intermediate operations KHÔNG thực thi cho đến khi terminal operation được gọi.
> Stream framework tối ưu bằng cách **fusing** các operations thành single pass.
>
> **Parallel Streams**: `parallelStream()` hoặc `.parallel()` — dùng `ForkJoinPool.commonPool()`
> - ⚠️ Chỉ nên dùng khi: data lớn, CPU-bound operations, source dễ split (ArrayList, array)
> - ❌ Tránh dùng khi: I/O-bound, shared mutable state, nhỏ data, LinkedList
> - ⚠️ Mọi parallel stream trong JVM dùng chung `commonPool` (số thread = số core - 1) → 1 task I/O chậm làm nghẽn toàn bộ parallel stream khác của ứng dụng (kể cả `CompletableFuture.supplyAsync` không truyền executor).

#### 6.3. Optional

```java
// Tạo Optional
Optional<String> opt1 = Optional.of("Hello");       // KHÔNG cho phép null
Optional<String> opt2 = Optional.ofNullable(null);  // cho phép null → empty
Optional<String> opt3 = Optional.empty();            // empty

// Sử dụng (KHÔNG dùng get() trực tiếp!)
String result = opt1
    .filter(s -> s.length() > 3)
    .map(String::toUpperCase)
    .orElse("default");                    // fallback value

String result2 = opt2.orElseGet(() -> computeDefault()); // lazy fallback
String result3 = opt2.orElseThrow(() -> new NotFoundException("Not found"));

// Java 9+
opt1.ifPresentOrElse(
    value -> System.out.println(value),
    () -> System.out.println("empty")
);
opt1.or(() -> Optional.of("fallback")); // return Optional nếu empty
opt1.stream(); // Optional → Stream (0 or 1 element)
```

> **Optional best practices**:
> - ✅ Dùng làm return type cho method có thể không có kết quả
> - ❌ KHÔNG dùng làm method parameter
> - ❌ KHÔNG dùng làm field
> - ❌ KHÔNG dùng `optional.get()` mà không kiểm tra → dùng `orElse`, `orElseThrow`
> - ❌ KHÔNG dùng `optional.isPresent() + optional.get()` → dùng `ifPresent`, `map`

> 🛠 **Áp dụng:**
> - Stream cho biến đổi dữ liệu rõ ràng; vòng lặp thường vẫn tốt hơn khi logic có nhiều nhánh/thoát sớm phức tạp hoặc là hot path cần tối ưu.
> - Parallel stream chỉ cho tác vụ CPU-bound trên dữ liệu lớn, nguồn dễ chia (mảng/`ArrayList`) và **không có trạng thái chia sẻ**; tránh với I/O vì dùng chung `commonPool`.
> - `Optional` chỉ làm kiểu trả về; không làm field/tham số.

<details><summary>🧠 Tự kiểm tra</summary>

1. **`list.stream().filter(...).map(...)` mà không có terminal operation thì chạy gì?**
   → Không chạy gì cả — intermediate operation là lười, chỉ xây dựng pipeline.
2. **Vì sao biến dùng trong lambda phải "effectively final"?**
   → Lambda có thể chạy sau khi method kết thúc hoặc trên thread khác; Java chụp **giá trị** của biến local, nên cấm thay đổi để tránh hiểu nhầm và data race.
</details>


---

### 7. JVM Internals

> 🎯 **Bài toán:** Muốn một chương trình chạy được trên mọi hệ điều hành/CPU mà không biên dịch lại, an toàn bộ nhớ (không tràn buffer, không con trỏ treo), nhưng vẫn **nhanh gần bằng mã máy**.
>
> ❌ **Hai thái cực:** Biên dịch thẳng ra mã máy (C/C++) — nhanh nhưng phải build cho từng nền tảng và không an toàn bộ nhớ; thông dịch thuần (script) — chạy mọi nơi nhưng chậm.
>
> 💡 **Ý tưởng giải:** Biên dịch ra **bytecode** trung gian chạy trên **máy ảo (JVM)**. JVM bắt đầu bằng **thông dịch**, đo xem đoạn code nào chạy nhiều ("nóng") rồi **JIT** biên dịch đoạn đó ra mã máy được tối ưu theo **hành vi thực tế lúc chạy** — điều mà compiler tĩnh không biết được. Bộ nhớ object do JVM cấp phát và **GC** thu hồi.
>
> 👉 Các mục bên dưới trả lời: code được nạp thế nào (7.1, 7.5), dữ liệu nằm ở đâu (7.2), vì sao nhanh (7.3, 7.10), làm sao debug khi có sự cố (7.6).


#### 7.1. ClassLoader Hierarchy

> 🎯 **Bài toán:** Class được nạp từ nhiều nguồn (JDK, classpath ứng dụng, jar plugin, mạng). Nếu ai cũng nạp được class tên `java.lang.String` của riêng mình thì có thể giả mạo class lõi.
>
> 💡 **Ý tưởng:** ClassLoader xếp theo **cây cha–con** và luôn **hỏi cha trước** (parent delegation) → class lõi chỉ được nạp bởi Bootstrap; một class được định danh bởi *(tên, ClassLoader đã nạp nó)* → cho phép nhiều ứng dụng trong cùng JVM (Tomcat) có phiên bản thư viện riêng.


```
Bootstrap ClassLoader (native, C++)
    │ ← Load: java.lang.*, java.util.* (module java.base...) — Java 8: rt.jar
    ▼
Platform ClassLoader (Java 9+) — Java 8 gọi là Extension ClassLoader
    │ ← Load: một số module Java SE/JDK (java.sql, ...) — Java 8: jre/lib/ext (cơ chế ext đã bị bỏ từ Java 9)
    ▼
Application ClassLoader (Java)
    │ ← Load: classpath, your application classes
    ▼
Custom ClassLoader (e.g., Tomcat, OSGi)
```

**Parent Delegation Model**:
1. Class cần load → hỏi parent trước
2. Parent hỏi parent tiếp (recursive)
3. Nếu parent không load được → child tự load
4. **Tại sao?** Đảm bảo core Java classes (String, Object, ...) luôn được load bởi Bootstrap → tránh spoofing

> **ClassNotFoundException vs NoClassDefFoundError**:
> - `ClassNotFoundException` (checked): `Class.forName()` hoặc `ClassLoader.loadClass()` không tìm thấy class → runtime
> - `NoClassDefFoundError` (error): Class có lúc compile nhưng không có lúc runtime, hoặc static initializer throw exception

#### 7.2. JVM Memory Structure

```
JVM Memory
├── Heap (shared across threads)
│   ├── Young Generation
│   │   ├── Eden Space (new objects allocated here)
│   │   ├── Survivor S0 (From)
│   │   └── Survivor S1 (To)
│   └── Old Generation / Tenured (long-lived objects, age > threshold)
│
├── Non-Heap
│   ├── Metaspace (class metadata, replaces PermGen since Java 8)
│   │   └── Native memory, auto-grows, no fixed max by default
│   └── Code Cache (JIT compiled native code)
│
└── Per-Thread (each thread has its own)
    ├── Stack (method frames)
    │   └── Frame: local variables, operand stack, frame data (return address)
    ├── PC Register (program counter, current instruction)
    └── Native Method Stack
```

**Phân biệt các vùng — câu hỏi số 1 về JVM**:
| Vùng | Chứa gì | Shared? | Lỗi khi đầy | Cấu hình |
|---|---|---|---|---|
| **Heap** | Mọi object & array (kể cả String Pool, static field của class từ Java 8 nằm trong `Class` object trên heap) | Mọi thread | `OutOfMemoryError: Java heap space` | `-Xms`, `-Xmx`, `MaxRAMPercentage` |
| **Stack** (mỗi thread) | Frame của từng lời gọi method: biến local primitive, **reference** (con trỏ) tới object, operand stack, địa chỉ trả về | Riêng từng thread | `StackOverflowError` (đệ quy quá sâu); `OOM: unable to create native thread` khi không cấp được stack cho thread mới | `-Xss` (mặc định ~512KB–1MB) |
| **Metaspace** | Metadata của class (cấu trúc class, method, bytecode, constant pool) | Chung | `OutOfMemoryError: Metaspace` | `-XX:MaxMetaspaceSize` |
| **Code Cache** | Mã máy do JIT sinh ra | Chung | JIT ngừng compile (7.10) | `-XX:ReservedCodeCacheSize` |
| **Direct memory** | `ByteBuffer.allocateDirect`, Netty | Chung | `OOM: Direct buffer memory` | `-XX:MaxDirectMemorySize` |

```java
void demo() {
    int x = 10;                    // x nằm trên STACK (frame của demo)
    User u = new User("An");       // biến u (reference) trên STACK, object User nằm trên HEAP
}                                  // frame bị hủy → u biến mất → object User không còn ai trỏ → chờ GC
```
- ⚠️ "Primitive luôn nằm trên stack" là **sai**: field primitive của một object nằm trong object đó trên heap; chỉ **biến local** mới nằm trên stack.
- **PermGen → Metaspace (Java 8)**: PermGen nằm trong heap, kích thước cố định → hay bị `OOM: PermGen space` khi redeploy nhiều lần trên Tomcat; Metaspace dùng native memory, tự giãn (nên vẫn đặt giới hạn để tránh ăn hết RAM container).

**Object Memory Layout** (64-bit JVM, Compressed Oops):

```
┌─────────────────────────────────────┐
│ Object Header (12 bytes)            │
│   ├── Mark Word (8 bytes)           │
│   │   ├── hashCode (31 bits)        │
│   │   ├── age (4 bits) → GC age     │
│   │   ├── (biased lock — đã bỏ JDK18)│
│   │   ├── lock status (2 bits)      │
│   │   └── GC metadata               │
│   └── Klass Pointer (4 bytes*)      │
│       └── Pointer to class metadata │
├─────────────────────────────────────┤
│ Instance Data (fields)              │
│   Ordered by: longs/doubles → ints  │
│   → shorts/chars → bytes/booleans   │
│   → references (field packing)      │
├─────────────────────────────────────┤
│ Padding (alignment to 8 bytes)      │
└─────────────────────────────────────┘
* Compressed Class Pointers / Compressed Oops: 4 bytes thay vì 8 bytes khi heap < ~32GB
```
- Một `new Object()` rỗng chiếm **16 bytes** (12 header + 4 padding). Mỗi reference 4 bytes (compressed oops) — heap ≥ 32 GB thì tắt compressed oops → reference thành 8 bytes, nên heap 32–48 GB thường còn **ít** chỗ dùng hơn heap 31 GB.
- **Compact Object Headers** (JEP 450 experimental ở JDK 24 → **JEP 519 product ở JDK 25**, bật bằng `-XX:+UseCompactObjectHeaders`): gộp mark word và class pointer vào **8 bytes** → giảm 10–20% heap cho ứng dụng nhiều object nhỏ.
- Xem layout thực tế bằng thư viện **JOL** (Java Object Layout).

#### 7.3. JIT Compilation

> 🎯 **Bài toán:** Thông dịch bytecode chậm hơn mã máy hàng chục lần; nhưng biên dịch toàn bộ chương trình ngay lúc khởi động thì khởi động rất lâu, và phần lớn code chỉ chạy vài lần.
>
> 💡 **Ý tưởng:** Chỉ biên dịch **phần code nóng**, và vì biên dịch lúc chạy nên dùng được **profile thực tế** (kiểu thực sự xuất hiện, nhánh nào hay chạy) để tối ưu mạnh tay hơn compiler tĩnh (inlining, escape analysis); nếu giả định sai thì **deoptimize** quay lại thông dịch.
>
> ⚖️ **Tiered compilation** = C1 (biên dịch nhanh, tối ưu ít → cải thiện sớm) + C2 (chậm nhưng tối ưu mạnh cho code thật sự nóng). ✅ Chọn kết hợp vì muốn cả khởi động nhanh lẫn hiệu năng đỉnh.


- **Interpreted mode**: JVM đọc bytecode, interpret từng instruction → chậm
- **JIT (Just-In-Time)**: Khi method được gọi đủ nhiều (hot method) → compile thành native code
- **Tiered Compilation** (default từ Java 8):
  - Level 0: Interpreter
  - Level 1-3: **C1 Compiler** (Client) → compile nhanh, ít optimization
  - Level 4: **C2 Compiler** (Server) → compile chậm, aggressive optimization

**C2 Optimizations quan trọng**:
- **Inlining**: Thay method call bằng method body → giảm call overhead
- **Escape Analysis**: Kiểm tra object có "escape" khỏi method/thread không
  - Nếu không escape → **Scalar replacement**: không cấp phát object trên heap mà tách thành các field riêng lẻ nằm trong register/stack frame → không tạo rác cho GC. (HotSpot **không** thực sự "stack-allocate" nguyên object như nhiều tài liệu nói; hiệu quả tương đương nhờ scalar replacement.)
  - Nếu không escape thread → **Lock elimination** (loại bỏ synchronized)
  - **Lock coarsening**: gộp nhiều lần lock/unlock liên tiếp trên cùng object thành 1
- **Loop Unrolling**: Giảm loop overhead bằng cách lặp nhiều iterations trong 1 vòng
- **Dead Code Elimination**: Loại bỏ code không bao giờ thực thi

#### 7.4. JVM Flags quan trọng

```bash
# Heap
-Xms512m          # Initial heap size
-Xmx4g            # Max heap size
-Xss512k          # Thread stack size

# Metaspace
-XX:MetaspaceSize=128m        # Initial metaspace size
-XX:MaxMetaspaceSize=512m     # Max metaspace size

# GC selection
-XX:+UseG1GC                  # G1 (default Java 9+)
-XX:+UseZGC                   # ZGC (Java 15+ production; JDK 23+ mặc định generational, JDK 24 bỏ chế độ non-generational)
-XX:+UseShenandoahGC          # Shenandoah
-XX:+UseParallelGC            # Throughput collector

# Container (Docker/K8s) — JDK 10+ tự nhận giới hạn cgroup
-XX:MaxRAMPercentage=75.0     # Heap = 75% memory limit của container (thay vì hard-code -Xmx)
-XX:ActiveProcessorCount=2    # Ép số CPU JVM nhìn thấy (ảnh hưởng số GC thread, commonPool)

# GC tuning
-XX:MaxGCPauseMillis=200      # G1: target max pause time
-XX:NewRatio=2                # Old/Young ratio (Old = 2 × Young)
-XX:SurvivorRatio=8           # Eden/Survivor ratio

# Logging
-Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=20m  # GC logging (Java 9+ unified logging)
-XX:+HeapDumpOnOutOfMemoryError  # Heap dump khi OOM
-XX:HeapDumpPath=/dumps/         # Nơi ghi heap dump
-XX:+ExitOnOutOfMemoryError      # Thoát hẳn khi OOM để orchestrator (K8s) restart, tránh zombie
```

#### 7.5. Class Loading Lifecycle & thứ tự khởi tạo

```
Loading ──► Linking [ Verification → Preparation → Resolution ] ──► Initialization ──► Using ──► Unloading
```
- **Loading**: tìm file `.class` (qua ClassLoader), tạo `Class<?>` object.
- **Verification**: kiểm tra bytecode hợp lệ, an toàn (không nhảy lung tung, đúng kiểu).
- **Preparation**: cấp phát bộ nhớ cho **static field** và gán **giá trị mặc định** (0, null, false) — chưa chạy code của bạn.
- **Resolution**: chuyển symbolic reference thành reference thật (có thể lazy).
- **Initialization**: chạy `<clinit>` = các phép gán static + `static {}` block **theo thứ tự xuất hiện trong source**. JVM đảm bảo `<clinit>` chạy **đúng 1 lần, thread-safe** (đây là nền tảng của Holder Singleton ở 9.11).
- Class được initialize **lazy** khi lần đầu: `new`, gọi static method, truy cập static field (trừ hằng `static final` compile-time — được inline), `Class.forName()`, khởi tạo subclass (class cha init trước), là main class.

**Thứ tự khởi tạo khi `new Child()`**:
```
1. Static của Parent (field + static block)      ┐ chỉ 1 lần khi class được init
2. Static của Child                              ┘
3. Instance field initializer + {} block của Parent
4. Constructor của Parent
5. Instance field initializer + {} block của Child
6. Constructor của Child
```
> ⚠️ Gọi method có thể bị override **trong constructor của Parent** → method của Child chạy khi field của Child **chưa được khởi tạo** (vẫn là null/0) → bug khó hiểu. Không gọi overridable method trong constructor.

#### 7.6. Troubleshooting JVM trong Production

**Các loại OutOfMemoryError**:
| Thông báo | Nguyên nhân thường gặp | Hướng xử lý |
|---|---|---|
| `Java heap space` | Leak (collection/cache không giới hạn), load quá nhiều dữ liệu một lần, heap quá nhỏ | Heap dump → phân tích bằng Eclipse MAT (Dominator Tree, Leak Suspects) |
| `GC overhead limit exceeded` | >98% thời gian dành cho GC mà thu hồi <2% heap (Parallel GC) | Như trên — heap gần đầy liên tục |
| `Metaspace` | Sinh class động liên tục (proxy, Groovy script, reload ClassLoader rò rỉ) | Kiểm tra số class được load, ClassLoader leak |
| `unable to create native thread` | Quá nhiều thread (thread pool không giới hạn), giới hạn `ulimit -u`/container pids | Giới hạn pool, dùng virtual threads cho I/O |
| `Direct buffer memory` | `ByteBuffer.allocateDirect` / Netty không giải phóng | `-XX:MaxDirectMemorySize`, kiểm tra leak buffer Netty |
| Container bị **OOMKilled** (exit 137) mà không có OOM trong Java | Tổng memory = heap + metaspace + thread stacks + direct buffer + code cache + GC overhead **vượt limit container** | Heap chỉ nên ~70–75% memory limit; bật **Native Memory Tracking** `-XX:NativeMemoryTracking=summary` + `jcmd <pid> VM.native_memory` |

**Bộ công cụ**:
```bash
jps -l                                  # liệt kê Java process
jcmd <pid> help                         # "dao găm thụy sĩ" — thay thế phần lớn tool cũ
jcmd <pid> Thread.print                 # thread dump (≈ jstack) — tìm deadlock, thread BLOCKED
jcmd <pid> GC.heap_info                 # tình trạng heap
jcmd <pid> GC.heap_dump /tmp/heap.hprof # heap dump (≈ jmap -dump)
jcmd <pid> JFR.start duration=60s filename=rec.jfr   # Java Flight Recorder — profiling overhead ~1%
jstat -gcutil <pid> 1000                # % sử dụng các vùng heap + số lần/thời gian GC mỗi giây
```
- **async-profiler**: CPU/allocation/lock profiling, xuất flame graph — công cụ số 1 khi tìm hotspot.
- **JDK Mission Control (JMC)**: mở file `.jfr`.
- Quy trình **CPU 100%**: xem `SpringBoot.md` mục 13.2 (top -H → hex thread id → thread dump).
- Quy trình **memory leak**: theo dõi heap **sau mỗi Full GC/old GC** (nếu "đáy" tăng dần → leak) → heap dump lúc heap cao → MAT tìm object giữ nhiều bộ nhớ nhất và **GC root path** của nó.

#### 7.7. Bytecode & Execution Engine

JVM là **stack-based VM**: mỗi method frame có **local variable array** + **operand stack**; lệnh bytecode đẩy/lấy giá trị trên operand stack (khác register-based VM như Dalvik/ART).

```java
int add(int a, int b) { return a + b * 2; }
```
```text
$ javap -c -v Calc.class
  int add(int, int);
    0: iload_1        // đẩy a (slot 1; slot 0 là this) lên operand stack
    1: iload_2        // đẩy b
    2: iconst_2       // đẩy hằng 2
    3: imul           // lấy 2 giá trị, nhân, đẩy kết quả
    4: iadd
    5: ireturn
```
**5 lệnh gọi method** (hay hỏi):
| Lệnh | Dùng cho | Cách dispatch |
|---|---|---|
| `invokestatic` | static method | Tĩnh, biết lúc link |
| `invokespecial` | constructor `<init>`, `private`, `super.method()` | Tĩnh |
| `invokevirtual` | instance method thường | Động qua **vtable** (override) |
| `invokeinterface` | method của interface | Động qua **itable** (chậm hơn vtable một chút) |
| `invokedynamic` (Java 7) | **lambda**, string concat (Java 9+), record `toString/equals/hashCode`, pattern switch | Lần đầu gọi chạy **bootstrap method** để tạo `CallSite`, các lần sau gọi thẳng |

- **Lambda được compile thế nào?** Thân lambda thành method `private static lambda$main$0(...)` trong cùng class; tại chỗ dùng là `invokedynamic` → bootstrap `LambdaMetafactory` sinh **class ẩn (hidden class)** implement functional interface lúc runtime. Khác anonymous inner class (sinh file `Outer$1.class` lúc compile, luôn giữ reference tới `Outer` nếu ở instance context).
- **Constant pool**: bảng hằng của class (literal, tên class/method/field dạng symbolic reference) → được resolve thành reference thật ở bước Resolution (7.5).
- **Execution engine** = Interpreter + JIT compilers (C1/C2, hoặc Graal JIT) + GC.

#### 7.8. Cấp phát Object: TLAB & Allocation Path

```
new Object()
   │
   ├─(1) Escape analysis → không thoát method? → scalar replacement (không cấp phát gì cả) — 7.3
   │
   ├─(2) Cấp phát trong TLAB của thread (Thread-Local Allocation Buffer — 1 vùng nhỏ của Eden riêng cho mỗi thread)
   │        → "bump-the-pointer": chỉ tăng con trỏ top += size → cực nhanh (~vài ns), KHÔNG cần đồng bộ
   │
   ├─(3) TLAB hết chỗ → xin TLAB mới từ Eden (CAS) hoặc cấp phát thẳng vào Eden (shared, cần CAS)
   │
   ├─(4) Object rất lớn → G1: "humongous object" (≥ 50% region) cấp phát thẳng vào vùng Old liên tiếp
   │
   └─(5) Eden đầy → Young GC → thử lại; vẫn không đủ → Full GC → OutOfMemoryError
```
- Vì Eden được dọn bằng **copying collector** (sống thì copy đi, phần còn lại coi như trống), bộ nhớ trống luôn liên tục → mới dùng được bump-the-pointer. Đây là lý do cấp phát object nhỏ ngắn hạn trong Java **rẻ** — cái đắt là object **sống lâu** (bị copy nhiều lần, lên Old Gen).
- Object được **promote** lên Old khi: tuổi vượt `MaxTenuringThreshold`, Survivor không đủ chỗ (premature promotion — nguyên nhân phổ biến gây Old Gen đầy nhanh), hoặc là humongous.

#### 7.9. Safepoint & Stop-The-World
- **Safepoint** là các điểm trong code mà tại đó trạng thái thread được JVM biết rõ (các reference nằm đâu trên stack/register) → JVM có thể dừng thread an toàn để làm việc toàn cục: GC pause, deoptimization, thread dump (`jstack`), class redefinition, (trước JDK 18) revoke biased lock.
- JIT chèn safepoint poll ở **cuối vòng lặp (back-edge)** và khi **return** từ method. Thread đang chạy native code hoặc bị block được coi là đã ở safepoint.
- **Time-To-Safepoint (TTSP)**: JVM phải chờ **mọi** thread tới safepoint trước khi bắt đầu pause. Một vòng lặp đếm `int` rất dài đã được JIT bỏ safepoint poll (*counted loop*) có thể khiến mọi thread khác phải chờ hàng trăm ms → "GC pause dài bất thường" dù GC làm rất ít việc. (JDK 10+ có *loop strip mining* giảm vấn đề này.) Quan sát bằng `-Xlog:safepoint`.
- **Thread-local handshake** (JDK 10+): dừng từng thread riêng lẻ thay vì cả JVM cho một số thao tác → giảm STW toàn cục.

#### 7.10. JIT nâng cao: Profiling, Inline Cache & Deoptimization
- **Tiered compilation**: interpreter thu thập **profile** (số lần gọi, nhánh nào hay đi, kiểu object thực tế ở mỗi call site) → C1 compile nhanh (có profiling) → method đủ "nóng" (~10.000 lần gọi/lặp) thì **C2** compile với tối ưu mạnh dựa trên profile.
- **OSR (On-Stack Replacement)**: vòng lặp rất dài trong 1 method chỉ gọi 1 lần (vd `main`) vẫn được compile và **thay thế giữa chừng** khi đang chạy.
- **Inline cache**: tại 1 call site `shape.area()`:
  - **Monomorphic** (luôn 1 kiểu) → C2 inline thẳng code của kiểu đó + 1 phép kiểm tra kiểu → nhanh nhất.
  - **Bimorphic** (2 kiểu) → inline cả 2 với if/else.
  - **Megamorphic** (≥ 3 kiểu) → quay về tra vtable, không inline → chậm hơn đáng kể. (Lý do code "quá nhiều tầng interface/polymorphism trên hot path" có thể chậm.)
- **Deoptimization**: C2 tối ưu dựa trên **giả định lạc quan** từ profile (vd "nhánh này chưa bao giờ chạy", "chỉ có 1 implementation của interface này"). Khi giả định sai (load class mới, nhánh lạ xuất hiện) → **bỏ code đã compile**, quay về interpreter, profile lại rồi compile lại. Quan sát bằng `-XX:+PrintCompilation` ("made not entrant").
- **Warm-up**: ứng dụng vừa khởi động chạy ở interpreter/C1 → latency cao trong vài chục giây đầu → K8s nên có readiness/warm-up traffic; giải pháp: CDS/AOT cache (Project Leyden), CRaC, GraalVM native image (`SpringBoot.md` 13.4).
- **Code cache** đầy (`CodeCache is full. Compiler has been disabled`) → JIT ngừng compile, ứng dụng chậm hẳn → tăng `-XX:ReservedCodeCacheSize`.
- **Microbenchmark** phải dùng **JMH** vì JIT có thể loại bỏ code không dùng kết quả (dead code elimination), constant folding, và cần warm-up.

#### 7.11. ✅ Checklist trọng tâm JVM cho phỏng vấn

| Mức | Chủ đề | Phải trả lời được | Mục |
|---|---|---|---|
| ⭐⭐⭐ | Vùng nhớ JVM | Heap (Young/Old) vs Stack vs Metaspace; vùng nào shared/per-thread; StackOverflowError vs OutOfMemoryError | 7.2 |
| ⭐⭐⭐ | GC cơ bản | GC root, generational hypothesis, Minor/Major/Full GC, vì sao Young dùng copying | 8.1, Q13 |
| ⭐⭐⭐ | Chọn GC | G1 vs Parallel vs ZGC — khi nào dùng cái nào, G1 hoạt động theo region thế nào | 8.2 |
| ⭐⭐⭐ | Troubleshooting | Các loại OOM, tìm memory leak (heap dump + MAT), CPU 100% (thread dump), các tool `jcmd/jstat/JFR` | 7.6 |
| ⭐⭐⭐ | Java Memory Model | Visibility/ordering/atomicity, happens-before, `volatile`, DCL Singleton | 9.2–9.4 |
| ⭐⭐ | Class loading | Parent delegation, các bước load-link-init, thứ tự khởi tạo static/instance | 7.1, 7.5 |
| ⭐⭐ | JIT | Interpreter vs JIT, tiered, inlining, escape analysis, warm-up | 7.3, 7.10 |
| ⭐⭐ | Object layout | Header (mark word, klass), compressed oops, kích thước object | 7.2 |
| ⭐⭐ | Lock trong JVM | synchronized → monitor, thin/fat lock, biased locking đã bỏ | 9.3 |
| ⭐⭐ | Container | `MaxRAMPercentage`, OOMKilled vs Java OOM, native memory | 7.4, 7.6 |
| ⭐ | Bytecode | Stack-based VM, `invokedynamic` & lambda | 7.7 |
| ⭐ | Allocation & Safepoint | TLAB, promotion, safepoint/TTSP | 7.8, 7.9 |
| ⭐ | GC nâng cao | Tri-color marking, write barrier, card table / remembered set, đọc GC log | 8.3 |

---

### 8. Garbage Collection

> 🎯 **Bài toán:** Quản lý bộ nhớ thủ công (`malloc/free`) là nguồn lỗi lớn nhất của C/C++: quên giải phóng (leak), giải phóng hai lần, dùng vùng nhớ đã giải phóng (crash, lỗ hổng bảo mật).
>
> 💡 **Ý tưởng giải:** JVM tự xác định object nào **không còn ai tham chiếu tới** (không tới được từ GC roots) và thu hồi. Bài toán con: (1) tìm object sống nhanh dù heap hàng chục GB; (2) **không dừng ứng dụng quá lâu** (pause).
>
> 💡 **Quan sát then chốt — Generational hypothesis:** *hầu hết object chết rất trẻ* (object tạm của mỗi request). → Chia heap: **Young** (dọn thường xuyên bằng copying — chỉ tốn công cho số ít object còn sống) và **Old** (dọn hiếm hơn). Các GC hiện đại (G1, ZGC) thêm: chia heap thành region và làm phần lớn việc **song song với ứng dụng** để pause ngắn.


#### 8.1. Cơ bản

**GC Roots** — Đối tượng "sống" bắt đầu từ:
- Local variables trên stack (đang active)
- Static variables của loaded classes
- Active threads
- JNI references

**Thuật toán cơ bản**:
- **Mark-and-Sweep**: Mark reachable objects từ GC roots → Sweep unmarked objects. Vấn đề: fragmentation
- **Mark-Compact**: Mark → Compact (dồn live objects về 1 đầu) → giải quyết fragmentation nhưng chậm hơn
- **Copying**: Chia heap thành 2 space, copy live objects sang space mới → nhanh nhưng tốn 2x memory
  - Phù hợp cho Young Gen (ít live objects → ít copy)

**Generational Hypothesis**: Phần lớn objects chết sớm (infant mortality) → tách Young Gen (GC thường xuyên, nhanh) và Old Gen (GC ít, chậm)

**Object Lifecycle**:
```
new Object() → Eden
    │
    ▼ (Minor GC: copy surviving objects)
Survivor S0 ↔ S1 (ping-pong, age++)
    │
    ▼ (age > MaxTenuringThreshold, default 15 for G1)
Old Generation (Tenured)
    │
    ▼ (Major/Full GC)
Collected or promoted
```

#### 8.2. Garbage Collectors — So sánh chi tiết

| Collector | Algorithm | Threads | Pause | Best for | Java |
|-----------|-----------|---------|-------|----------|------|
| **Serial** | Mark-Compact (Old), Copying (Young) | Single | Long STW | Client apps, small heap | All |
| **Parallel** | Mark-Compact (Old), Copying (Young) | Multi | Medium STW | Throughput-focused, batch | Default ≤ J8 |
| **CMS** | Concurrent Mark-Sweep | Multi + concurrent | Short STW | Low latency | Deprecated J9, removed J14 |
| **G1** | Region-based, Copying + Compacting | Multi + concurrent | Predictable STW | General purpose | Default J9+ |
| **ZGC** | Colored pointers, Load barriers | Concurrent | < 1ms (thực tế, mục tiêu ban đầu < 10ms) | Ultra-low latency, heap lớn (tới 16 TB) | Production J15+, Generational J21, chỉ còn Generational từ J24 |
| **Shenandoah** | Forwarding pointer + load-reference barrier, concurrent compact | Concurrent | < 10ms | Ultra-low latency | OpenJDK 12+ (Red Hat), Generational mode product ở J25 |

**G1 GC (Garbage First) — Chi tiết**:

```
Heap chia thành ~2048 regions (1-32MB mỗi region)
┌──────┬──────┬──────┬──────┬──────┬──────┬──────┐
│ Eden │ Surv │ Old  │ Eden │ Free │ Old  │ Hum  │
│      │      │      │      │      │      │      │
└──────┴──────┴──────┴──────┴──────┴──────┴──────┘
  E       S      O      E       F      O      H

Region types: Eden, Survivor, Old, Humongous (object > 50% region)
```

- **Young GC**: STW, evacuate (copy) live objects từ Eden/Survivor → new Survivor/Old regions
- **Concurrent Marking**: Background marking để identify garbage-heavy Old regions
- **Mixed GC**: Collect Young regions + một số Old regions có nhiều garbage nhất (Garbage First!)
- **Target pause**: `-XX:MaxGCPauseMillis=200` (default) → G1 tự adjust số regions collect

**ZGC — Colored Pointers**:

```
64-bit object pointer:
┌────────────┬──┬──┬──┬──┬──────────────────────────┐
│  unused    │M1│M0│Re│Fi│     Object address         │
│  (16 bits) │  │  │ma│na│     (44 bits = 16TB)       │
│            │  │  │pp│li│                             │
│            │  │  │ed│ze│                             │
└────────────┴──┴──┴──┴──┴──────────────────────────┘

4 metadata bits trong pointer:
- Marked0, Marked1: marking phase
- Remapped: relocation tracking
- Finalizable: cần finalization

Load Barrier: mỗi khi đọc object reference → kiểm tra metadata bits
→ nếu cần → "heal" pointer (update to new location)
→ toàn bộ GC gần như concurrent, STW < 10ms bất kể heap size
```

**Generational ZGC (Java 21, JEP 439)**: Thêm generations (Young + Old) cho ZGC → GC Young Gen thường xuyên hơn, hiệu quả hơn (ít CPU, ít allocation stall). JDK 23 đặt generational làm mặc định khi `-XX:+UseZGC`, JDK 24 xóa hẳn chế độ non-generational. (Generational ZGC cũng đổi cách mã hóa metadata trong pointer so với sơ đồ ở trên.)

**Chọn GC thế nào?**
| Tình huống | Gợi ý |
|---|---|
| Mặc định, heap vài GB–vài chục GB, web service thông thường | **G1** (default) — chỉnh `MaxGCPauseMillis` nếu cần |
| Batch / data processing, ưu tiên throughput, chấp nhận pause | **Parallel GC** |
| Latency-sensitive (p99 thấp), heap lớn | **ZGC** (generational) hoặc Shenandoah |
| Container nhỏ (<2 CPU hoặc <1792 MB), CLI tool | JVM tự chọn **Serial GC** — thường hợp lý |

> 📌 Tối ưu GC tốt nhất là **giảm allocation**: tránh tạo object tạm trong vòng lặp nóng, tránh boxing, dùng `StringBuilder`, tái sử dụng buffer, chọn đúng kích thước collection ban đầu.

**Reference Types và GC**:

| Type | Class | GC Behavior | Use Case |
|------|-------|-------------|----------|
| **Strong** | (default) | KHÔNG bị GC nếu reachable | Normal usage |
| **Soft** | `SoftReference<T>` | GC khi sắp OOM | Memory-sensitive cache |
| **Weak** | `WeakReference<T>` | GC ở next GC cycle | WeakHashMap, listeners |
| **Phantom** | `PhantomReference<T>` | Sau khi finalize, trước khi reclaim | Resource cleanup (thay finalize) |

#### 8.3. Bên trong Concurrent GC: Tri-color Marking, Barrier, Remembered Set

**Tri-color marking** — cách GC đánh dấu object sống trong khi ứng dụng vẫn chạy:
```
Trắng (white): chưa thăm → cuối pha mark vẫn trắng = rác
Xám  (grey) : đã thăm object nhưng CHƯA quét hết các reference con
Đen  (black): đã thăm và đã quét hết reference con

Bắt đầu: GC roots → xám. Lặp: lấy 1 object xám, tô các con trắng thành xám, tô nó thành đen. Hết xám → xong.
```
- **Vấn đề khi mark song song với ứng dụng (mutator)**: app có thể làm "mất" object sống — đồng thời (1) gán reference tới object trắng vào 1 object **đen** (GC sẽ không quét lại object đen) và (2) xóa mọi đường đi từ object xám tới object trắng đó → object trắng còn sống nhưng bị thu hồi → **hỏng heap**.
- **Giải pháp = write barrier** (đoạn code nhỏ JIT chèn vào mỗi lần ghi field reference):
  - **SATB — Snapshot-At-The-Beginning** (G1, Shenandoah): khi một reference **bị ghi đè**, ghi lại giá trị **cũ** → coi như mọi thứ sống lúc bắt đầu mark vẫn sống (phá điều kiện 2). Có thể giữ lại thêm chút rác (floating garbage) tới chu kỳ sau.
  - **Incremental update** (CMS): khi gán reference mới vào object đen, tô lại object đen thành xám để quét lại (phá điều kiện 1).
  - ZGC/Shenandoah dùng thêm **load barrier / load-reference barrier** (chạy khi **đọc** reference) để cho phép **di chuyển (compact) object song song** với ứng dụng — barrier tự "chữa" con trỏ cũ trỏ về địa chỉ mới.

**Card table & Remembered Set** — vì sao Minor GC không phải quét cả Old Gen?
- Minor GC chỉ quét Young, nhưng object Young có thể được tham chiếu **từ Old** → những reference Old→Young cũng là "root" của Minor GC.
- **Card table**: Old Gen chia thành các "card" 512 byte; write barrier đánh dấu card **dirty** khi một field trong đó được ghi reference → Minor GC chỉ quét các card dirty thay vì cả Old Gen.
- **G1 Remembered Set (RSet)**: mỗi region có 1 RSet ghi lại "những region nào đang trỏ vào tôi" → khi evacuate một tập region (collection set) chỉ cần quét RSet của chúng. RSet tốn bộ nhớ (vài % heap) và chi phí write barrier — một phần lý do G1 có throughput thấp hơn Parallel GC.

**Chu kỳ G1 rút gọn**:
```
Young GC (STW, evacuate Eden+Survivor) ─┐ lặp lại
                                        │ khi Old chiếm ≥ IHOP (InitiatingHeapOccupancyPercent, ~45%, adaptive)
Concurrent Mark: Initial mark (đi kèm 1 Young GC) → Concurrent mark (song song app, SATB)
                 → Remark (STW ngắn) → Cleanup (STW ngắn, giải phóng region rỗng hoàn toàn)
Mixed GC (STW): evacuate Young + các Old region nhiều rác nhất (garbage first!) trong giới hạn MaxGCPauseMillis
Nếu không kịp (allocation nhanh hơn GC dọn) → Evacuation failure / "to-space exhausted" → Full GC (JDK 10+: song song nhưng vẫn STW dài)
```

**Đọc 1 dòng GC log (G1, `-Xlog:gc`)**:
```text
[12.345s][info][gc] GC(42) Pause Young (Normal) (G1 Evacuation Pause) 1024M->312M(2048M) 18.532ms
                     │       │                     │                   │     │     │        └ thời gian pause STW
                     │       │                     │                   │     │     └ tổng heap đang commit
                     │       │                     │                   │     └ heap đã dùng SAU GC  ← quan sát "đáy" này để phát hiện leak
                     │       │                     │                   └ heap đã dùng TRƯỚC GC
                     │       │                     └ nguyên nhân
                     │       └ loại pause: Young / Mixed / Remark / Cleanup / Full
                     └ số thứ tự GC
```
- Dấu hiệu xấu: `Pause Full`, `To-space exhausted`, `Humongous allocation` liên tục, heap sau GC tăng dần theo thời gian, pause vượt xa `MaxGCPauseMillis`. Công cụ phân tích log: **GCeasy**, GCViewer, JMC.

#### 8.4. Quy trình tuning GC (thực tế)
1. **Xác định mục tiêu**: latency (p99 pause) hay throughput (% thời gian không GC) hay footprint (RAM)? Không tối ưu được cả 3 cùng lúc.
2. **Bật GC log** ở production (overhead rất thấp) và đo trước khi đổi gì.
3. **Đặt heap đúng cỡ**: `-Xms = -Xmx` (tránh resize), hoặc `MaxRAMPercentage` trong container; heap sau Full GC (live set) × 3–4 là điểm khởi đầu hợp lý.
4. Ưu tiên **sửa code** trước tuning flag: giảm allocation rate, sửa leak, bỏ object sống trung bình (cache không giới hạn), tránh humongous allocation (mảng/buffer lớn).
5. Chỉ chỉnh vài flag cấp cao (`MaxGCPauseMillis`, kích thước heap, đổi collector); tránh copy hàng chục flag "bí truyền" từ Internet — G1/ZGC hiện đại tự điều chỉnh tốt.
6. Đổi collector khi mục tiêu không đạt: pause G1 không chấp nhận được → ZGC; throughput batch → Parallel.

✅ **Vì sao không có GC "tốt nhất":** mỗi collector đánh đổi giữa **throughput** (tổng thời gian dành cho ứng dụng), **latency** (độ dài pause) và **footprint** (RAM tốn thêm cho cấu trúc của GC). Parallel GC tối đa throughput nhưng pause dài; ZGC pause < 1 ms nhưng tốn thêm CPU cho barrier; G1 là điểm cân bằng nên làm mặc định.

> 🛠 **Áp dụng:**
> - Web service thông thường: G1 mặc định + đặt heap đúng là đủ; latency p99 khắt khe → ZGC; batch → Parallel.
> - Cách giảm áp lực GC tốt nhất là **giảm allocation** và tránh object "sống trung bình" (cache không giới hạn, buffer giữ lâu) — chúng bị copy nhiều lần rồi lên Old Gen.
> - Đọc GC log để phân biệt "GC nhiều vì cấp phát nhiều" với "heap đầy vì leak" (8.3, 7.6).

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao Young Gen dùng thuật toán copying còn Old Gen thì không?**
   → Copying tốn công theo số object **sống**; ở Young hầu hết đã chết nên rẻ. Ở Old phần lớn còn sống → copy rất tốn và cần gấp đôi bộ nhớ, nên dùng mark-compact/region.
2. **Có phải object còn reference thì không bao giờ bị thu hồi?**
   → Chỉ đúng khi nó **tới được từ GC roots**. Hai object trỏ lẫn nhau nhưng không ai từ root trỏ tới vẫn bị thu hồi (GC Java không dùng reference counting).
</details>


---

### 9. Concurrency & Multithreading (CHI TIẾT)

> 🎯 **Bài toán:** (1) Tận dụng nhiều core CPU; (2) không để CPU ngồi chờ khi một request đang đợi DB/mạng — phục vụ request khác trong lúc đó.
>
> ❌ **Cái giá của thread:** khi nhiều thread dùng chung dữ liệu, ba thứ có thể sai:
> - **Visibility** — mỗi core có cache riêng, thread B có thể không thấy giá trị thread A vừa ghi.
> - **Atomicity** — `count++` là đọc–cộng–ghi, hai thread chen nhau làm mất một lần cộng.
> - **Ordering** — compiler/CPU được phép đảo thứ tự lệnh để tối ưu.
>
> 💡 **Ý tưởng giải:** **Java Memory Model** định nghĩa khi nào một lần ghi chắc chắn được nhìn thấy (quan hệ *happens-before*). Các công cụ tạo ra quan hệ đó: `synchronized`/`Lock` (loại trừ lẫn nhau + visibility), `volatile` (chỉ visibility + ordering), CAS/`Atomic*` (nguyên tử không khóa), cấu trúc dữ liệu đồng thời và **tránh chia sẻ** (immutable object, ThreadLocal, confinement).


#### 9.1. Thread Fundamentals

**Tạo Thread — 3 cách**:

```java
// 1. Extends Thread (ít dùng vì single inheritance)
class MyThread extends Thread {
    @Override
    public void run() { System.out.println("Thread running"); }
}
new MyThread().start();

// 2. Implements Runnable (prefer, vì có thể implement nhiều interface)
Runnable task = () -> System.out.println("Runnable running");
new Thread(task).start();

// 3. Implements Callable<V> (có return value + throws Exception)
Callable<Integer> callable = () -> { return 42; };
ExecutorService executor = Executors.newSingleThreadExecutor();
Future<Integer> future = executor.submit(callable);
int result = future.get(); // blocking call, trả về 42
```

**Thread Lifecycle States**:

```
                 start()
    NEW ─────────────→ RUNNABLE
                          │
              ┌───────────┼───────────┐
              │           │           │
              ▼           ▼           ▼
          BLOCKED     WAITING    TIMED_WAITING
        (chờ lock)  (wait,join)  (sleep,wait(t))
              │           │           │
              └───────────┼───────────┘
                          │
                          ▼
                     TERMINATED
```

- **NEW**: Thread được tạo, chưa `start()`
- **RUNNABLE**: Đang chạy hoặc sẵn sàng chạy (OS scheduled)
- **BLOCKED**: Chờ acquire monitor lock (`synchronized`)
- **WAITING**: Chờ vô thời hạn: `wait()`, `join()`, `LockSupport.park()`
- **TIMED_WAITING**: Chờ có timeout: `sleep(ms)`, `wait(ms)`, `join(ms)`
- **TERMINATED**: Run method kết thúc (bình thường hoặc exception)

**Thread methods quan trọng**:

```java
thread.start();        // bắt đầu thread MỚI (gọi run() trên thread mới)
// ⚠️ thread.run() → KHÔNG tạo thread mới, chạy trên thread hiện tại

thread.join();         // current thread CHỜ cho đến khi thread này kết thúc
thread.join(1000);     // chờ tối đa 1 giây

Thread.sleep(1000);    // current thread ngủ 1 giây (không release lock!)
Thread.yield();        // gợi ý scheduler cho thread khác chạy (không đảm bảo)

thread.interrupt();    // đặt interrupt flag, nếu thread đang sleep/wait → InterruptedException
thread.isInterrupted(); // kiểm tra interrupt flag (không reset)
Thread.interrupted();   // kiểm tra VÀ reset interrupt flag

thread.setDaemon(true); // daemon thread: JVM thoát khi chỉ còn daemon threads
```

#### 9.2. Java Memory Model (JMM)

**Vấn đề cốt lõi**: Trong hệ thống multi-CPU, mỗi CPU có **cache riêng**. Khi thread A ghi vào biến `x`, thread B trên CPU khác có thể **không thấy** giá trị mới (vẫn đọc từ cache cũ).

```
CPU 0                    CPU 1
┌──────────┐            ┌──────────┐
│ Thread A │            │ Thread B │
│          │            │          │
│ L1 Cache │            │ L1 Cache │
│ (x = 1)  │            │ (x = 0?) │ ← stale value!
└────┬─────┘            └────┬─────┘
     │       L2 Cache        │
     └───────┬───────────────┘
             │
     ┌───────▼───────┐
     │  Main Memory  │
     │    x = ???     │
     └───────────────┘
```

**3 vấn đề chính**:
1. **Visibility (Tính khả kiến)**: Thread A ghi, Thread B không thấy
2. **Atomicity (Tính nguyên tử)**: `i++` thực ra là 3 operations: read → increment → write
3. **Ordering (Thứ tự)**: Compiler/CPU có thể **reorder** instructions để tối ưu

**Happens-Before Relationships**:

JMM định nghĩa **happens-before** — nếu action A happens-before action B, thì:
- A's effects **được đảm bảo visible** cho B
- A thực thi **trước** B (theo logic, không nhất thiết theo thời gian)

```
Các quy tắc happens-before:

1. Program Order Rule:
   Trong cùng 1 thread, mỗi statement happens-before statement tiếp theo

2. Monitor Lock Rule:
   unlock(m) happens-before mọi lock(m) tiếp theo trên cùng monitor m
   → exit synchronized block → enter synchronized block thấy mọi thay đổi

3. Volatile Variable Rule:
   Write vào volatile variable happens-before mọi subsequent read trên cùng variable
   → volatile đảm bảo visibility

4. Thread Start Rule:
   thread.start() happens-before mọi action trong thread đó
   → thread mới thấy mọi thay đổi trước start()

5. Thread Join Rule:
   Mọi action trong thread happens-before thread.join() return
   → sau join() thấy mọi thay đổi của thread

6. Transitivity:
   Nếu A happens-before B, B happens-before C → A happens-before C
```

**Memory Barriers (Fences)**:
- JIT compiler chèn **memory barrier instructions** để enforce ordering:
  - `LoadLoad`: Đảm bảo loads trước barrier hoàn thành trước loads sau
  - `StoreStore`: Đảm bảo stores trước barrier hoàn thành trước stores sau
  - `LoadStore`: Đảm bảo loads trước barrier hoàn thành trước stores sau
  - `StoreLoad`: Mạnh nhất, đảm bảo stores flush ra main memory trước loads sau

#### 9.3. synchronized keyword

**Cơ chế Monitor**:

```java
// 1. synchronized method — lock trên this object (instance) hoặc Class object (static)
public synchronized void increment() { count++; }        // lock this
public static synchronized void staticMethod() { ... }   // lock MyClass.class

// 2. synchronized block — lock trên object chỉ định
public void increment() {
    synchronized (this) {  // hoặc bất kỳ object nào
        count++;
    }
}
```

**Bytecode level**: `synchronized` biên dịch thành `monitorenter` / `monitorexit`

```
Mỗi Java object có 1 Monitor (intrinsic lock):
┌──────────────────────────────────────┐
│            Object Monitor            │
│                                      │
│  ┌─ Entry Set ───┐  ┌─ Wait Set ──┐ │
│  │ Thread 2      │  │ Thread 4    │ │
│  │ Thread 3      │  │ Thread 5    │ │
│  └───────────────┘  └────────────┘  │
│           │                  ▲       │
│     acquire lock        wait()      │
│           ▼                  │       │
│  ┌─ Owner ────────────────────┐     │
│  │ Thread 1 (holding lock)    │     │
│  │  → notify()/notifyAll()    │     │
│  │  → exit synchronized block │     │
│  └────────────────────────────┘     │
└──────────────────────────────────────┘

- Entry Set: Threads chờ acquire lock
- Wait Set: Threads đã gọi wait(), chờ notify()
- Owner: Thread đang giữ lock
```

**Reentrant**: Cùng thread có thể acquire cùng lock nhiều lần (counter tăng, phải release cùng số lần)

**Lock Optimization trong JVM** (biased → thin → fat):

```
1. Biased Locking (deprecated Java 15, removed Java 18):
   - Giả định: lock chỉ có 1 thread dùng
   - Mark Word lưu thread ID → CAS đầu tiên, sau đó không cần CAS
   - Nếu thread khác muốn lock → revoke bias → upgrade

2. Lightweight Lock (Thin Lock):
   - Khi 2+ threads cạnh tranh nhưng KHÔNG overlap (sequential access)
   - CAS copy Mark Word vào Lock Record trên stack
   - Nhanh vì không dùng OS mutex

3. Heavyweight Lock (Fat Lock):
   - Khi nhiều threads thực sự contend (cùng lúc)
   - Dùng OS mutex (parking/unparking threads)
   - Chậm nhất, nhưng cần thiết cho real contention

Flow (JDK ≤ 14): No contention → Biased → Lightweight → Heavyweight
Flow (JDK 18+):  Lightweight → Heavyweight (không còn biased locking)
```
- Một object đã "inflate" thành fat lock (ObjectMonitor) **có thể được deflate** trở lại khi monitor không còn ai dùng — từ JDK 15 việc này chạy bất đồng bộ bởi một thread riêng (async monitor deflation), nên câu "chỉ đi 1 chiều" chỉ đúng trong vòng đời 1 lần lock.
- JDK 21+ dùng cơ chế lightweight locking mới (lock-stack theo thread, không còn copy mark word lên stack) và JDK 24 (JEP 491) viết lại monitor để **virtual thread có thể unmount khi đang ở trong `synchronized`** (xem 11.1).

#### 9.4. volatile keyword

**Đảm bảo**:
1. **Visibility**: Write vào volatile variable → flush ra main memory → thread khác đọc sẽ thấy
2. **Ordering**: Prevent reordering qua volatile read/write

**KHÔNG đảm bảo Atomicity**:

```java
private volatile int count = 0;

// ❌ Không thread-safe! i++ là 3 operations:
// 1. Read count (volatile read → main memory)
// 2. Increment (local)
// 3. Write count (volatile write → main memory)
// Giữa step 1 và 3, thread khác có thể chen vào
public void increment() { count++; }

// ✅ Dùng AtomicInteger hoặc synchronized thay thế
```

**Use cases chính**:

```java
// 1. Flag để stop thread
private volatile boolean running = true;
public void stop() { running = false; }
public void run() {
    while (running) { doWork(); } // đảm bảo đọc giá trị mới nhất
}

// 2. Double-Checked Locking Singleton (phải có volatile!)
public class Singleton {
    private static volatile Singleton instance; // volatile BẮT BUỘC

    public static Singleton getInstance() {
        if (instance == null) {           // 1st check (no lock)
            synchronized (Singleton.class) {
                if (instance == null) {   // 2nd check (with lock)
                    instance = new Singleton();
                    // Không volatile: JVM có thể reorder:
                    // 1. Allocate memory
                    // 2. Assign reference (instance != null)
                    // 3. Call constructor ← chưa xong!
                    // Thread khác thấy instance != null nhưng chưa fully constructed
                }
            }
        }
        return instance;
    }
}

// 3. Publishing immutable object
private volatile ImmutableConfig config;
public void updateConfig(ImmutableConfig newConfig) {
    this.config = newConfig; // volatile write → other threads see complete new config
}
```

#### 9.5. Atomic Classes & CAS

**CAS (Compare-And-Swap)**:

```
CAS(address, expectedValue, newValue):
    1. Đọc giá trị hiện tại tại address
    2. So sánh với expectedValue
    3. Nếu bằng → ghi newValue → return true (thành công)
    4. Nếu khác → return false (thread khác đã thay đổi)
    → Toàn bộ là 1 CPU instruction (cmpxchg trên x86) → atomic

Ưu điểm: Lock-free, không cần OS mutex
Nhược điểm:
  - Spin loop khi high contention (wasted CPU)
  - ABA problem
```

**ABA Problem & Solution**:

```java
// ABA: Thread 1 đọc A, Thread 2 thay A→B→A, Thread 1 CAS thành công (nghĩ A chưa đổi)
// Giải pháp: AtomicStampedReference (thêm version stamp)
AtomicStampedReference<String> ref = new AtomicStampedReference<>("A", 0);

int[] stampHolder = new int[1];
String current = ref.get(stampHolder); // current = "A", stamp = 0

// CAS chỉ thành công nếu CẢ value VÀ stamp match
boolean success = ref.compareAndSet("A", "B", 0, 1); // stamp 0 → 1
```

**Atomic Classes**:

```java
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();     // atomic ++count
count.getAndIncrement();     // atomic count++
count.compareAndSet(5, 10);  // CAS: if count==5 then count=10
count.addAndGet(5);          // atomic count += 5
count.updateAndGet(x -> x * 2); // atomic custom function

// Khi contention CAO → dùng LongAdder thay AtomicLong
// LongAdder dùng Cell[] array (striped), mỗi thread cập nhật Cell riêng
// → giảm CAS contention drastically
// sum() = base + sum(cells[])
LongAdder adder = new LongAdder();
adder.increment();  // thread-local cell
long total = adder.sum(); // tổng hợp khi cần đọc
```

> **Khi nào dùng Atomic vs synchronized?**
> - Atomic: 1 biến, operation đơn giản (increment, CAS), low-medium contention
> - synchronized: Nhiều biến cần update cùng lúc (compound action), high contention
> - LongAdder: Counter với very high contention (write-heavy)

#### 9.6. Lock Framework (java.util.concurrent.locks)

**ReentrantLock**:

```java
private final ReentrantLock lock = new ReentrantLock(); // unfair mặc định
// new ReentrantLock(true) → fair lock (FIFO, chậm hơn)

public void doWork() {
    lock.lock();
    try {
        // critical section
    } finally {
        lock.unlock(); // PHẢI unlock trong finally!
    }
}

// Tính năng mà synchronized KHÔNG có:
// 1. tryLock() — non-blocking, trả về false nếu không lấy được lock
if (lock.tryLock()) {
    try { ... } finally { lock.unlock(); }
} else {
    // làm việc khác
}

// 2. tryLock(timeout) — chờ có giới hạn
if (lock.tryLock(1, TimeUnit.SECONDS)) { ... }

// 3. lockInterruptibly() — có thể bị interrupt khi đang chờ lock
lock.lockInterruptibly(); // throws InterruptedException

// 4. Condition (thay thế wait/notify)
Condition notEmpty = lock.newCondition();
Condition notFull = lock.newCondition();

// Producer:
lock.lock();
try {
    while (queue.isFull()) notFull.await(); // chờ cho đến khi không full
    queue.add(item);
    notEmpty.signal(); // báo cho consumer
} finally { lock.unlock(); }

// Consumer:
lock.lock();
try {
    while (queue.isEmpty()) notEmpty.await();
    item = queue.remove();
    notFull.signal(); // báo cho producer
} finally { lock.unlock(); }
```

**ReadWriteLock**:

```java
ReadWriteLock rwLock = new ReentrantReadWriteLock();
Lock readLock = rwLock.readLock();
Lock writeLock = rwLock.writeLock();

// Nhiều readers CÓ THỂ đọc đồng thời
// Writer PHẢI exclusive (không ai đọc/ghi)
// Read lock → Write lock: KHÔNG thể upgrade (deadlock!)
// Write lock → Read lock: CÓ THỂ downgrade
```

**StampedLock (Java 8)** — Hiệu năng tốt hơn ReadWriteLock:

```java
StampedLock sl = new StampedLock();

// Optimistic Read — KHÔNG acquire lock, chỉ lấy "stamp"
long stamp = sl.tryOptimisticRead(); // non-blocking
int x = this.x; // đọc data
int y = this.y;
if (!sl.validate(stamp)) { // kiểm tra: có ai write giữa chừng không?
    // Có conflict → fallback sang pessimistic read lock
    stamp = sl.readLock();
    try {
        x = this.x;
        y = this.y;
    } finally {
        sl.unlockRead(stamp);
    }
}

// Write lock
long stamp = sl.writeLock();
try {
    this.x = newX;
    this.y = newY;
} finally {
    sl.unlockWrite(stamp);
}
```

> **So sánh Lock vs synchronized**:
> 
> | | synchronized | ReentrantLock |
> |---|---|---|
> | **Release** | Tự động (exit block) | Manual (`unlock()` trong finally) |
> | **Try lock** | ❌ | ✅ `tryLock()`, `tryLock(timeout)` |
> | **Interruptible** | ❌ | ✅ `lockInterruptibly()` |
> | **Fair lock** | ❌ | ✅ `new ReentrantLock(true)` |
> | **Condition** | `wait/notify` (1 set) | Nhiều `Condition` objects |
> | **Performance** | Tương đương (Java 6+ optimized) | Tương đương |
> | **Deadlock prevention** | Khó | Dễ hơn (`tryLock` with timeout) |
> | **Best practice** | Đơn giản, short critical section | Complex locking strategies |

#### 9.7. Synchronization Utilities

```java
// ── CountDownLatch: One-shot barrier ──
// N threads phải hoàn thành trước khi main thread tiếp tục
CountDownLatch latch = new CountDownLatch(3);

for (int i = 0; i < 3; i++) {
    executor.submit(() -> {
        doWork();
        latch.countDown(); // mỗi thread giảm count
    });
}
latch.await(); // main thread chờ count = 0
// ⚠️ Không thể reset, dùng 1 lần

// ── CyclicBarrier: Reusable barrier ──
// N threads chờ nhau tại 1 điểm, sau đó tất cả tiếp tục cùng lúc
CyclicBarrier barrier = new CyclicBarrier(3, () -> {
    System.out.println("All 3 threads reached barrier!");
});
// Mỗi thread gọi barrier.await() → block cho đến khi 3 threads đều await
// Sau đó barrier reset, có thể dùng lại

// ── Semaphore: Resource limiting ──
Semaphore semaphore = new Semaphore(5); // 5 permits → tối đa 5 threads đồng thời
semaphore.acquire();  // lấy 1 permit (block nếu hết)
try {
    accessLimitedResource();
} finally {
    semaphore.release();  // trả lại permit
}
// Use case: connection pool, rate limiting

// ── Exchanger: Two-thread data swap ──
Exchanger<String> exchanger = new Exchanger<>();
// Thread A: String fromB = exchanger.exchange("data from A");
// Thread B: String fromA = exchanger.exchange("data from B");
```

#### 9.8. Thread Pools & Executors

> 🎯 **Bài toán:** Tạo thread mới cho mỗi tác vụ thì tốn (stack ~1 MB, tạo/hủy qua kernel) và không giới hạn → tải đột biến tạo hàng nghìn thread → hết bộ nhớ.
>
> 💡 **Ý tưởng:** Tái sử dụng một **số lượng thread cố định** và đưa tác vụ vào **hàng đợi**. Thiết kế quan trọng nhất là trả lời: *khi đã đủ thread và hàng đợi đầy thì làm gì?* — từ chối, chạy trên thread của người gọi (tự giảm tốc — backpressure), hay bỏ.
>
> ✅ **Vì sao cần hàng đợi giới hạn:** hàng đợi vô hạn chỉ "che giấu" quá tải: latency tăng dần rồi OOM. Giới hạn + từ chối sớm giúp hệ thống phản hồi lỗi rõ ràng và phục hồi được.


**ThreadPoolExecutor — 7 tham số cốt lõi**:

```java
new ThreadPoolExecutor(
    int corePoolSize,      // Số threads luôn giữ (kể cả idle)
    int maxPoolSize,       // Số threads tối đa
    long keepAliveTime,    // Thời gian idle thread (> core) tồn tại trước khi bị kill
    TimeUnit unit,
    BlockingQueue<Runnable> workQueue,  // Queue chứa tasks chờ xử lý
    ThreadFactory factory,             // Tạo thread (đặt tên, daemon, ...)
    RejectedExecutionHandler handler   // Xử lý khi queue full VÀ max threads đạt
);
```

**Flow xử lý task**:

```
submit(task)
    │
    ├─ active threads < corePoolSize?
    │   YES → tạo new core thread để xử lý
    │
    ├─ workQueue có chỗ?
    │   YES → đưa task vào queue
    │
    ├─ active threads < maxPoolSize?
    │   YES → tạo new non-core thread
    │
    └─ RejectedExecutionHandler
        ├── AbortPolicy (default): throw RejectedExecutionException
        ├── CallerRunsPolicy: thread submit tự chạy task (backpressure!)
        ├── DiscardPolicy: bỏ task im lặng
        └── DiscardOldestPolicy: bỏ task cũ nhất trong queue, retry submit
```

**Work Queue types**:

| Queue | Bounded | Behavior |
|-------|---------|----------|
| `SynchronousQueue` | 0 capacity | Direct handoff, mỗi offer phải có thread take ngay |
| `LinkedBlockingQueue` | Unbounded* | Task tích tụ nếu consume chậm → OOM risk |
| `ArrayBlockingQueue` | Bounded | Khi full → reject policy trigger |

**Executors factory — VÀ TẠI SAO KHÔNG NÊN DÙNG**:

```java
// ❌ newFixedThreadPool(n): LinkedBlockingQueue (UNBOUNDED) → OOM nếu task produce nhanh hơn consume
Executors.newFixedThreadPool(10); // queue chứa Integer.MAX_VALUE tasks!

// ❌ newCachedThreadPool(): maxPoolSize = Integer.MAX_VALUE → tạo quá nhiều threads → OOM
Executors.newCachedThreadPool(); // SynchronousQueue + unlimited threads

// ❌ newSingleThreadExecutor(): LinkedBlockingQueue (UNBOUNDED) → OOM
Executors.newSingleThreadExecutor();

// ✅ Best practice: Tạo ThreadPoolExecutor trực tiếp với bounded queue
ExecutorService executor = new ThreadPoolExecutor(
    10, 20, 60, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(1000),
    new ThreadPoolExecutor.CallerRunsPolicy()
);
```

**ForkJoinPool — Work Stealing**:

```
┌─────────────────────────────────────────────────────┐
│                  ForkJoinPool                        │
│                                                      │
│  Thread 1 Queue    Thread 2 Queue    Thread 3 Queue  │
│  ┌──┬──┬──┬──┐   ┌──┬──┬──┬──┐   ┌──┬──┬──┐       │
│  │T1│T2│T3│T4│   │T5│T6│  │  │   │  │  │  │       │
│  └──┴──┴──┴──┘   └──┴──┴──┴──┘   └──┴──┴──┘       │
│      push↕pop         push↕pop     Thread 3 idle!   │
│                                    → STEAL from      │
│                                      Thread 1's tail │
│                                      (deque)         │
└─────────────────────────────────────────────────────┘

- Mỗi thread có double-ended queue (deque)
- Push/Pop từ head (LIFO) — locality
- Steal từ TAIL của thread khác (FIFO) — ít contention
```

```java
// RecursiveTask<V> (có return value) / RecursiveAction (void)
class SumTask extends RecursiveTask<Long> {
    private final long[] array;
    private final int start, end;
    private static final int THRESHOLD = 10000;

    protected Long compute() {
        if (end - start <= THRESHOLD) {
            // Base case: sequential sum
            long sum = 0;
            for (int i = start; i < end; i++) sum += array[i];
            return sum;
        }
        int mid = (start + end) / 2;
        SumTask left = new SumTask(array, start, mid);
        SumTask right = new SumTask(array, mid, end);
        left.fork();  // submit left to queue
        long rightResult = right.compute(); // compute right in current thread
        long leftResult = left.join(); // wait for left result
        return leftResult + rightResult;
    }
}
```

**Thread Pool Sizing**:
- **CPU-bound**: `N + 1` threads (N = số CPU cores). +1 để bù khi 1 thread bị page fault
- **I/O-bound**: `N × (1 + W/C)` với W = wait time, C = compute time
  - Ví dụ: 8 cores, task 80% wait → 8 × (1 + 80/20) = 8 × 5 = 40 threads

#### 9.9. CompletableFuture

> 🎯 **Bài toán:** Một request cần gọi 3 service độc lập; gọi tuần tự tốn tổng thời gian cả 3. Muốn gọi song song rồi ghép kết quả, xử lý lỗi/timeout mà không viết code chờ thread thủ công.
>
> 💡 **Ý tưởng:** Biểu diễn "kết quả sẽ có trong tương lai" như một giá trị có thể **ghép nối** (`thenApply`, `thenCompose`, `thenCombine`, `allOf`) và xử lý lỗi theo chuỗi (`exceptionally`, `handle`). Với virtual threads (11.1), nhiều trường hợp có thể viết lại thành code tuần tự đơn giản + structured concurrency.


```java
// ── Tạo ──
CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
    return fetchFromAPI(); // chạy trên ForkJoinPool.commonPool()
});

// Hoặc với custom executor:
CompletableFuture<String> cf = CompletableFuture.supplyAsync(
    () -> fetchFromAPI(),
    customExecutor
);

// ── Chaining (giống Promise trong JS) ──
cf.thenApply(result -> result.toUpperCase())      // transform result (sync)
  .thenApplyAsync(result -> process(result))       // transform (async, different thread)
  .thenAccept(result -> System.out.println(result)) // consume result (no return)
  .thenRun(() -> System.out.println("Done"));      // run action (no input, no return)

// thenCompose — flatMap (khi function return CompletableFuture)
cf.thenCompose(userId -> fetchUserDetails(userId)); // avoids CompletableFuture<CompletableFuture<>>

// thenCombine — combine 2 independent futures
CompletableFuture<String> nameFuture = fetchName();
CompletableFuture<Integer> ageFuture = fetchAge();
nameFuture.thenCombine(ageFuture, (name, age) -> name + " is " + age);

// ── Exception handling ──
cf.exceptionally(ex -> "fallback value")           // catch exception, return fallback
  .handle((result, ex) -> {                        // handle cả success và error
      if (ex != null) return "error: " + ex.getMessage();
      return result;
  })
  .whenComplete((result, ex) -> {                  // peek tại result/error (không transform)
      if (ex != null) log.error("Failed", ex);
  });

// ── Combining multiple futures ──
// Chờ TẤT CẢ hoàn thành
CompletableFuture<Void> all = CompletableFuture.allOf(cf1, cf2, cf3);
all.thenRun(() -> {
    String r1 = cf1.join(); // đã hoàn thành
    String r2 = cf2.join();
    String r3 = cf3.join();
});

// Chờ BẤT KỲ 1 cái hoàn thành
CompletableFuture<Object> any = CompletableFuture.anyOf(cf1, cf2, cf3);

// ── Timeout (Java 9+) ──
cf.orTimeout(5, TimeUnit.SECONDS);              // throw TimeoutException nếu chưa xong
cf.completeOnTimeout("default", 5, TimeUnit.SECONDS); // return default value nếu timeout
```

**Pattern thực tế — Parallel API calls**:

```java
// Gọi 3 APIs song song, combine kết quả
CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(() -> userService.getUser(id));
CompletableFuture<List<Order>> ordersFuture = CompletableFuture.supplyAsync(() -> orderService.getOrders(id));
CompletableFuture<Double> balanceFuture = CompletableFuture.supplyAsync(() -> accountService.getBalance(id));

CompletableFuture<UserProfile> profile = userFuture
    .thenCombine(ordersFuture, (user, orders) -> new Pair<>(user, orders))
    .thenCombine(balanceFuture, (pair, balance) ->
        new UserProfile(pair.getFirst(), pair.getSecond(), balance));
```

#### 9.10. Concurrent Collections

> 🎯 **Bài toán:** `HashMap` dùng chung giữa nhiều thread → mất dữ liệu, thậm chí vòng lặp vô hạn (Java 7). `Collections.synchronizedMap` an toàn nhưng **mọi thao tác dùng một khóa** → nghẽn.
>
> 💡 **Ý tưởng:** Giảm phạm vi khóa — Java 7 khóa theo **segment**, Java 8+ dùng **CAS** khi bucket trống và chỉ khóa **node đầu của bucket** khi có va chạm; đọc hầu như không khóa. Thêm các thao tác **phức hợp nguyên tử** (`computeIfAbsent`, `merge`) vì "kiểm tra rồi ghi" bằng hai lời gọi riêng thì luôn có race condition.


**ConcurrentHashMap — Chi tiết**:

```
Java 7: Segment-based locking
┌────────────────────────────────────────────┐
│  Segment[0]  Segment[1]  ...  Segment[15] │
│  (ReentrantLock each)                       │
│  ┌─────────┐ ┌─────────┐                   │
│  │ bucket[]│ │ bucket[]│                   │
│  └─────────┘ └─────────┘                   │
│  → Lock per segment → 16 concurrent writes │
└────────────────────────────────────────────┘

Java 8+: Node-level CAS + synchronized on first node
┌────────────────────────────────────────────┐
│  Node[] table (giống HashMap)               │
│  ┌──────┬──────┬──────┬──────┐             │
│  │  [0] │  [1] │  [2] │  [3] │             │
│  └──┬───┴──┬───┴──────┴──────┘             │
│     │      │                                │
│  synchronized(firstNode)                    │
│  CAS khi bucket trống                       │
│  → Lock per bucket → MUCH better concurrency│
└────────────────────────────────────────────┘
```

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// Atomic compound operations (thread-safe):
map.putIfAbsent("key", 1);
map.computeIfAbsent("key", k -> expensiveComputation(k));
map.computeIfPresent("key", (k, v) -> v + 1);
map.compute("key", (k, v) -> v == null ? 1 : v + 1);
map.merge("key", 1, Integer::sum); // if exists: sum, else: put 1

// Parallel bulk operations (Java 8):
// parallelismThreshold: khi size > threshold → parallel execution
map.forEach(1000, (k, v) -> process(k, v));
long count = map.reduceValuesToLong(1000, Integer::longValue, 0L, Long::sum);
String result = map.search(1000, (k, v) -> v > 100 ? k : null);
```

> **Tại sao ConcurrentHashMap không cho phép null key/value?**
> Trong concurrent context, `map.get(key)` return `null` → ambiguous:
> - Key không tồn tại? Hay value là null?
> - Trong HashMap (single-thread) → dùng `containsKey()` để phân biệt
> - Trong ConcurrentHashMap → giữa `get()` và `containsKey()`, thread khác có thể modify → race condition

**BlockingQueue implementations**:

```java
// Producer-Consumer pattern
BlockingQueue<Task> queue = new ArrayBlockingQueue<>(100);

// Producer thread:
queue.put(task);   // BLOCK nếu queue full

// Consumer thread:
Task task = queue.take();  // BLOCK nếu queue empty

// Non-blocking alternatives:
queue.offer(task);              // return false nếu full
queue.offer(task, 1, SECONDS); // chờ 1 giây
queue.poll();                   // return null nếu empty
queue.poll(1, SECONDS);        // chờ 1 giây
```

| Implementation | Bounded | Ordering | Special |
|---|---|---|---|
| `ArrayBlockingQueue` | ✅ Fixed | FIFO | Optional fairness |
| `LinkedBlockingQueue` | Optional | FIFO | Default unbounded (Integer.MAX_VALUE) |
| `PriorityBlockingQueue` | ❌ Unbounded | Priority | Based on Comparable/Comparator |
| `DelayQueue` | ❌ Unbounded | Delay-based | Elements available after delay expires |
| `SynchronousQueue` | 0 capacity | N/A | Direct handoff (producer blocks until consumer takes) |
| `LinkedTransferQueue` | ❌ Unbounded | FIFO | `transfer()`: producer blocks until consumer receives |

**CopyOnWriteArrayList**:

```java
// Mỗi write operation → copy TOÀN BỘ internal array → modify → swap reference
// Read: KHÔNG cần lock (đọc snapshot cũ nếu write đang xảy ra)
// ✅ Read-heavy, write-rare: listener lists, configuration
// ❌ Write-heavy: mỗi write copy array → O(n) + GC pressure
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
```

#### 9.11. Common Concurrency Problems

**Deadlock — 4 điều kiện (tất cả phải có):**
1. **Mutual Exclusion**: Resource chỉ cho 1 thread sở hữu
2. **Hold and Wait**: Thread giữ resource và chờ resource khác
3. **No Preemption**: Không thể cưỡng chế lấy resource
4. **Circular Wait**: Thread A chờ B, B chờ C, ..., N chờ A

```java
// ❌ Deadlock scenario
Object lock1 = new Object(), lock2 = new Object();

// Thread 1: lock1 → lock2
synchronized (lock1) { synchronized (lock2) { ... } }

// Thread 2: lock2 → lock1
synchronized (lock2) { synchronized (lock1) { ... } }

// ✅ Prevention: Luôn acquire locks theo CÙNG THỨ TỰ
// Thread 1 & 2 đều: lock1 → lock2
```

**ThreadLocal — và Memory Leak**:

```java
// ThreadLocal: mỗi thread có bản sao riêng
private static final ThreadLocal<SimpleDateFormat> dateFormat =
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

// Trong thread:
String date = dateFormat.get().format(new Date()); // thread-safe

// ⚠️ MEMORY LEAK với Thread Pool:
// Thread pool reuse threads → ThreadLocal value KHÔNG bị GC
// → PHẢI remove() khi xong
try {
    dateFormat.get().format(...);
} finally {
    dateFormat.remove(); // QUAN TRỌNG trong thread pool!
}
```

**Thread-safe Singleton — 5 cách**:

```java
// 1. Eager initialization
public class Singleton {
    private static final Singleton INSTANCE = new Singleton();
    public static Singleton getInstance() { return INSTANCE; }
}

// 2. Synchronized method (chậm — lock mỗi lần gọi)
public static synchronized Singleton getInstance() { ... }

// 3. Double-Checked Locking (DCL) — volatile bắt buộc!
private static volatile Singleton instance;
public static Singleton getInstance() {
    if (instance == null) {
        synchronized (Singleton.class) {
            if (instance == null) instance = new Singleton();
        }
    }
    return instance;
}

// 4. Bill Pugh / Holder Pattern — ✅ RECOMMENDED
public class Singleton {
    private Singleton() {}
    private static class Holder {
        static final Singleton INSTANCE = new Singleton();
        // Loaded khi Holder class được access lần đầu → lazy + thread-safe bởi JVM class loading
    }
    public static Singleton getInstance() { return Holder.INSTANCE; }
}

// 5. Enum Singleton — ✅ BEST (chống Reflection & Serialization attack)
public enum Singleton {
    INSTANCE;
    public void doSomething() { ... }
}
```

**Livelock & Starvation** (2 vấn đề còn lại ngoài deadlock):
- **Livelock**: các thread không bị block nhưng liên tục "nhường nhau" và lặp lại cùng hành động, không ai tiến triển (vd 2 thread cùng `tryLock` thất bại → nhả → thử lại đúng cùng nhịp). Sửa: thêm **random backoff**.
- **Starvation**: thread không bao giờ được cấp tài nguyên (lock unfair, priority thấp, writer bị reader chèn mãi trong ReadWriteLock). Sửa: fair lock, giới hạn thời gian giữ lock.

#### 9.12. AQS (AbstractQueuedSynchronizer) — nền móng của `java.util.concurrent`

`ReentrantLock`, `ReentrantReadWriteLock`, `Semaphore`, `CountDownLatch`, `ThreadPoolExecutor.Worker`... đều xây trên AQS (Doug Lea).

```
AQS
├── volatile int state          ← ý nghĩa do lớp con quyết định:
│                                  ReentrantLock: 0 = free, n = số lần reentrant
│                                  Semaphore: số permit còn lại
│                                  CountDownLatch: count còn lại
├── Thread exclusiveOwnerThread ← thread đang giữ (exclusive mode)
└── CLH wait queue (FIFO, doubly-linked) các Node{thread, waitStatus}
        head ⇄ [T2] ⇄ [T3] ⇄ [T4] ← tail
```
- **acquire**: thử `CAS(state, 0, 1)` → thành công thì giữ lock; thất bại → tạo Node đưa vào cuối queue (CAS tail) → `LockSupport.park()` (ngủ).
- **release**: giảm/đặt lại `state` → `LockSupport.unpark()` thread kế tiếp trong queue.
- **Fair vs Non-fair**: non-fair (mặc định) cho thread mới đến **thử CAS ngay** ("chen hàng") trước khi xếp hàng → throughput cao hơn vì tránh chi phí đánh thức thread; fair luôn kiểm tra `hasQueuedPredecessors()` trước.
- **Exclusive** (ReentrantLock) vs **Shared** mode (Semaphore, CountDownLatch, read lock) — shared mode đánh thức lan truyền nhiều thread cùng lúc.
- `Condition.await()` → chuyển node sang **condition queue** riêng; `signal()` → chuyển lại sang wait queue chính.

#### 9.13. ThreadLocal — cơ chế bên trong & memory leak

```
Thread
└── ThreadLocal.ThreadLocalMap threadLocals
        Entry[] table (open addressing, linear probing)
        Entry extends WeakReference<ThreadLocal<?>>
           key   = ThreadLocal (WEAK reference)
           value = Object      (STRONG reference)  ← nguồn gốc leak
```
- Dữ liệu **nằm trong chính Thread**, `ThreadLocal` chỉ là key để tra → không cần đồng bộ.
- Khi biến `ThreadLocal` không còn strong reference → key bị GC thành `null`, nhưng **value vẫn bị Entry giữ mạnh** cho tới khi thread chết hoặc map tình cờ dọn "stale entry" (khi `get/set/remove` khác chạm vào). Với **thread pool** (Tomcat, executor) thread sống mãi → value rò rỉ, đồng thời request sau có thể **đọc nhầm dữ liệu của request trước** (vd user context) → lỗi bảo mật.
- Quy tắc: luôn `remove()` trong `finally` (hoặc filter/interceptor ở cuối request). Spring `RequestContextHolder`, `SecurityContextHolder`, `TransactionSynchronizationManager`, MDC của logging đều dựa trên ThreadLocal và tự dọn ở cuối request.
- `InheritableThreadLocal`: thread con copy giá trị từ thread cha **lúc tạo thread** → không hoạt động đúng với thread pool (thread được tạo trước) → cần decorator (Spring `TaskDecorator`, Alibaba TransmittableThreadLocal) hoặc dùng **ScopedValue** (11.3).
- Với **virtual threads**: ThreadLocal vẫn hoạt động, nhưng hàng triệu virtual thread × giá trị nặng (cache `SimpleDateFormat`...) = tốn bộ nhớ; pattern "cache object đắt trong ThreadLocal" không còn hợp lý → dùng object thread-safe (`DateTimeFormatter`) hoặc ScopedValue.

#### 9.14. Producer–Consumer bằng `wait/notify` (câu hỏi viết code kinh điển)

```java
public class BoundedBuffer<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;
    public BoundedBuffer(int capacity) { this.capacity = capacity; }

    public synchronized void put(T item) throws InterruptedException {
        while (queue.size() == capacity) {   // ⚠️ while, KHÔNG phải if — chống spurious wakeup
            wait();                          // nhả monitor và ngủ
        }
        queue.add(item);
        notifyAll();                         // notifyAll an toàn hơn notify (producer & consumer chung 1 wait set)
    }

    public synchronized T take() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        T item = queue.poll();
        notifyAll();
        return item;
    }
}
// Production: dùng thẳng ArrayBlockingQueue / LinkedBlockingQueue (9.10)
```
- Tại sao phải `while`? **Spurious wakeup** (thread có thể tỉnh dậy không vì lý do gì) và khi nhiều consumer cùng được đánh thức, điều kiện có thể đã bị thread khác "tiêu thụ" trước.
- `wait()/notify()` phải gọi khi đang giữ monitor của chính object đó → nếu không: `IllegalMonitorStateException`.

**⚖️ Chọn công cụ đồng bộ theo bài toán**

| Bài toán | Công cụ | Vì sao |
| :--- | :--- | :--- |
| Cờ dừng, publish object bất biến | `volatile` | Chỉ cần visibility, không cần loại trừ |
| Đếm, cộng dồn | `AtomicLong`; tranh chấp cao → `LongAdder` | CAS không khóa; `LongAdder` chia nhỏ điểm tranh chấp |
| Cập nhật nhiều biến cùng lúc (bất biến chung) | `synchronized` | Đơn giản, JVM tối ưu tốt |
| Cần timeout, tryLock, nhiều điều kiện chờ, fair | `ReentrantLock` | Tính năng mà `synchronized` không có |
| Đọc rất nhiều, ghi rất ít | `ReadWriteLock` / `StampedLock` (optimistic read) | Nhiều reader song song |
| Map dùng chung | `ConcurrentHashMap` (`compute`, `merge`) | Khóa theo bucket + thao tác phức hợp nguyên tử |
| Chuyển dữ liệu giữa thread | `BlockingQueue` | Tự xử lý chờ khi rỗng/đầy (backpressure) |
| Chạy nhiều tác vụ | `ThreadPoolExecutor` có giới hạn / virtual threads cho I/O | Không tạo thread vô hạn |

✅ **Nguyên tắc chọn:** **ít chia sẻ nhất có thể** (immutable, không trạng thái) → nếu phải chia sẻ thì dùng cấu trúc có sẵn của `java.util.concurrent` → chỉ tự khóa khi thật cần, và khóa phạm vi nhỏ nhất.

> 🛠 **Áp dụng:**
> - Spring singleton bean bị nhiều request dùng đồng thời → giữ **stateless** (`SpringBoot.md` 2.2).
> - Thread pool luôn có **hàng đợi giới hạn** + rejection policy rõ ràng; đặt tên thread; đừng dùng `Executors.newFixedThreadPool` với hàng đợi vô hạn cho production.
> - Debug treo/chậm: thread dump tìm `BLOCKED`/deadlock (7.6).

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao `volatile int count; count++` vẫn sai?**
   → `volatile` đảm bảo mỗi lần đọc/ghi thấy giá trị mới, nhưng `++` gồm 3 bước; hai thread cùng đọc 5, cùng ghi 6.
2. **Vì sao Double-Checked Locking cần `volatile`?**
   → Không có `volatile`, phép gán `instance = new X()` có thể được đảo thứ tự để gán reference trước khi constructor chạy xong → thread khác thấy object chưa khởi tạo đầy đủ.
3. **Vì sao `ConcurrentHashMap` không cho phép `null`?**
   → `get` trả `null` sẽ mơ hồ ("không có" hay "giá trị null"), và không thể kiểm tra lại bằng `containsKey` một cách nguyên tử trong môi trường đa luồng.
</details>


---

### 10. Java 17 Features (LTS)

> 🎯 **Bài toán:** Java bị chê dài dòng: class chỉ để chứa dữ liệu cần hàng chục dòng getter/equals/hashCode; kiểm tra kiểu phải `instanceof` rồi ép kiểu; `switch` dễ quên `break`; không cách nào nói "chỉ có đúng 3 loại Shape".
>
> 💡 **Ý tưởng giải:** Hướng "**data-oriented programming**": `record` (dữ liệu bất biến, tự sinh boilerplate), `sealed` (khai báo tập con đóng — compiler biết hết các trường hợp), **pattern matching** (kiểm tra kiểu + tách dữ liệu trong một bước), **switch expression** (trả giá trị, không fall-through, kiểm tra đủ trường hợp). Kết hợp lại → mô hình hóa dữ liệu kiểu "algebraic data type" an toàn: thêm một loại mới mà quên xử lý → **lỗi compile** thay vì lỗi runtime.


#### 10.0. Lộ trình phiên bản Java (cần nắm khi phỏng vấn)

Từ Java 9, Java ra bản mới **mỗi 6 tháng** (tháng 3 và tháng 9); bản **LTS** mỗi 2 năm: **8 → 11 → 17 → 21 → 25**. Feature thường đi qua các giai đoạn *Incubator → Preview (cần `--enable-preview`) → Final*.

| Phiên bản | Điểm nổi bật |
|---|---|
| **Java 8** (2014, LTS) | Lambda, Stream, `Optional`, `java.time`, default method, Metaspace thay PermGen, `CompletableFuture` |
| **Java 9** | **JPMS (module system)**, `List.of/Set.of/Map.of`, JShell, G1 làm GC mặc định, Compact Strings, private interface method, `Flow` (Reactive Streams) |
| **Java 10** | `var` (local variable type inference), container awareness |
| **Java 11** (LTS) | `HttpClient` chuẩn (HTTP/2), `String.isBlank/strip/lines/repeat`, chạy file `.java` trực tiếp, `var` trong lambda, ZGC (experimental), xóa Java EE & CORBA modules |
| **Java 12–13** | Switch expression (preview), text block (preview), Shenandoah |
| **Java 14** | **Switch expression** (final), **Helpful NPE**, record (preview), JFR streaming |
| **Java 15** | **Text block** (final), ZGC & Shenandoah production, sealed (preview), hidden classes |
| **Java 16** | **Record** (final), **pattern matching `instanceof`** (final), `Stream.toList()`, strong encapsulation mặc định |
| **Java 17** (LTS) | **Sealed classes** (final), strong encapsulation JDK internals (JEP 403), RandomGenerator API |
| **Java 21** (LTS) | **Virtual threads**, **pattern matching for switch**, **record patterns**, Sequenced Collections, Generational ZGC |
| **Java 22–24** | FFM API (final ở 22), unnamed variables `_` (22), multi-file source launcher (22), Stream Gatherers (final 24), Class-File API (final 24), synchronized không còn pin virtual thread (24), Security Manager bị vô hiệu hóa vĩnh viễn (24) |
| **Java 25** (LTS, 9/2025) | Scoped Values, Flexible Constructor Bodies, Module Import, Compact Source Files & instance `main` (đều final), Compact Object Headers (product) — xem mục 12 |

#### 10.1. Sealed Classes & Interfaces

Giới hạn class nào có thể extends/implements → compiler biết TẤT CẢ subtypes → exhaustive pattern matching.

```java
// Chỉ Circle, Rectangle, Triangle được implements Shape
public sealed interface Shape permits Circle, Rectangle, Triangle {}

public record Circle(double radius) implements Shape {}
public record Rectangle(double width, double height) implements Shape {}
public final class Triangle implements Shape { // final: không cho extend tiếp
    private final double a, b, c;
    // ...
}

// Compiler biết mọi subtypes → switch KHÔNG cần default
// (Type pattern trong switch là preview ở Java 17, final từ Java 21 — xem 11.4)
double area = switch (shape) {
    case Circle c    -> Math.PI * c.radius() * c.radius();
    case Rectangle r -> r.width() * r.height();
    case Triangle t  -> t.calculateArea();
    // Không cần default! Compiler biết đã cover hết
};

// Subclass phải là: final, sealed, hoặc non-sealed
// final: không extend tiếp
// sealed: tiếp tục restrict
// non-sealed: mở lại cho bất kỳ ai extend
```

#### 10.2. Records (Java 16, finalized)

Records = immutable data class tự động generate: constructor, getters, equals(), hashCode(), toString()

```java
// Thay thế hàng chục dòng boilerplate
public record Point(int x, int y) {}
// Auto-generates:
// - Constructor: Point(int x, int y)
// - Accessors: x(), y() (không có getX prefix!)
// - equals(): so sánh tất cả fields
// - hashCode(): based on tất cả fields
// - toString(): Point[x=1, y=2]

// Compact constructor (validation)
public record Range(int start, int end) {
    public Range { // compact constructor, không cần (int start, int end)
        if (start > end) throw new IllegalArgumentException("start > end");
        // this.start = start; this.end = end; ← tự động thêm cuối compact constructor
    }
}

// Custom constructor
public record Point(int x, int y) {
    public Point(int xy) { this(xy, xy); } // custom constructor phải delegate to canonical
}

// Records có thể implement interface, KHÔNG thể extend class
// Records là implicitly final, fields implicitly private final
// Records KHÔNG thể có thêm instance fields (chỉ static fields OK)
public record NamedPoint(String name, int x, int y) implements Serializable {}
```

#### 10.3. Pattern Matching for instanceof (Java 16)

```java
// ❌ Trước Java 16: cast thủ công
if (obj instanceof String) {
    String s = (String) obj; // explicit cast
    System.out.println(s.length());
}

// ✅ Java 16+: pattern variable
if (obj instanceof String s) {
    System.out.println(s.length()); // s đã là String, không cần cast
}

// Scope: s chỉ available khi type check TRUE
if (obj instanceof String s && s.length() > 5) { // s available vì && short-circuit
    // ...
}

// ❌ Compile error: s KHÔNG available vì || có thể true khi obj KHÔNG phải String
if (obj instanceof String s || s.length() > 5) { } // ERROR

// Kết hợp với negation
if (!(obj instanceof String s)) {
    return; // early return
}
// s available ở đây! (flow analysis: nếu đến đây thì obj IS String)
System.out.println(s.toUpperCase());
```

#### 10.4. Text Blocks (Java 15)

```java
// ❌ Trước: escape hell
String json = "{\n" +
    "  \"name\": \"John\",\n" +
    "  \"age\": 30\n" +
    "}";

// ✅ Text blocks: triple double-quotes
String json = """
        {
          "name": "John",
          "age": 30
        }
        """;
// Indentation: phần thừa so với closing """ sẽ bị strip (incidental whitespace)
// Escape: \n, \t hoạt động bình thường
// Trailing whitespace: bị strip mặc định → dùng \s để giữ
// Line continuation: \ ở cuối dòng → nối dòng (không line break)
```

#### 10.5. Switch Expressions (Java 14)

```java
// ❌ Classic switch: verbose, dễ quên break → fall-through bugs
switch (day) {
    case MONDAY: case FRIDAY:
        result = 6; break;
    case TUESDAY:
        result = 7; break;
    default:
        result = 0; break;
}

// ✅ Switch expression: arrow syntax, no fall-through, can return value
int result = switch (day) {
    case MONDAY, FRIDAY -> 6;        // multiple values
    case TUESDAY        -> 7;
    default             -> {
        log.info("Other day");
        yield 0;                     // yield cho block expression
    }
};
// Compiler enforce exhaustiveness khi dùng sealed class hoặc enum
```

#### 10.6. Các cải tiến khác trong Java 17

- **Helpful NullPointerExceptions**: `Cannot invoke "String.length()" because "a.b.c" is null` — chỉ rõ biến nào null
- **Strong encapsulation of JDK internals** (JEP 403): không thể dùng reflection truy cập internal API của JDK (vd `sun.security.*`, field private của `java.lang.*`) → lỗi `InaccessibleObjectException`; workaround khi migrate: `--add-opens java.base/java.lang=ALL-UNNAMED`. Ngoại lệ: các "critical internal API" như `sun.misc.Unsafe` vẫn dùng được qua module `jdk.unsupported` (nhưng các method truy cập bộ nhớ của Unsafe đã bị deprecated for removal ở JDK 23 — JEP 471 — và in cảnh báo từ JDK 24; thay bằng `VarHandle` và FFM API)
- **New RandomGenerator API**: Pluggable random number generators, algorithm-independent
- **Deprecations / removals**: Applet API (deprecated for removal), Security Manager (deprecated for removal), RMI Activation (removed)

> 🛠 **Áp dụng:**
> - DTO, event, value object, key của map → `record`.
> - Kết quả nghiệp vụ có nhiều dạng (`Success | NotFound | Rejected`) → `sealed interface` + `switch` pattern matching thay cho exception hoặc `instanceof` dài.
> - Nâng cấp lên Java 17/21 là điều kiện của Spring Boot 3+.

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao `switch` trên sealed interface không cần `default`, và vì sao điều đó là ưu điểm?**
   → Compiler biết đủ các lớp con; khi thêm lớp con mới, mọi `switch` chưa xử lý sẽ **báo lỗi compile** — có `default` thì lỗi bị che mất.
</details>


---

### 11. Java 21 Features (LTS)

> 🎯 **Bài toán:** Mô hình "mỗi request một thread" dễ viết, dễ debug, nhưng thread của OS đắt (~1 MB stack, chuyển ngữ cảnh qua kernel) → chỉ chạy được vài nghìn thread; ứng dụng chủ yếu **chờ I/O** nên phần lớn thread ngồi không.
>
> ❌ **Giải pháp cũ — lập trình bất đồng bộ/reactive** (CompletableFuture, WebFlux): scale tốt nhưng code bị chia thành callback/chuỗi operator, stack trace khó đọc, mọi thư viện phải non-blocking.
>
> 💡 **Ý tưởng giải — Virtual threads:** JVM tự quản lý hàng triệu thread "nhẹ" chạy trên vài thread OS; khi virtual thread **block vì I/O**, JVM **tháo nó khỏi** thread OS và cho virtual thread khác chạy. → Viết code **blocking tuần tự** như cũ mà vẫn scale như code bất đồng bộ.


#### 11.1. Virtual Threads (Project Loom) — QUAN TRỌNG NHẤT

**Vấn đề với Platform Threads**:
- Mỗi Platform Thread = 1 OS thread ≈ 1MB stack memory
- 10,000 threads ≈ 10GB RAM chỉ cho stack!
- Thread context switch tốn kém (kernel mode)
- Thread pool giới hạn → throughput bị giới hạn cho I/O-bound apps

**Virtual Threads = Lightweight threads**:
- Managed bởi JVM, KHÔNG phải OS
- Hàng triệu virtual threads trên vài OS threads
- Stack size linh hoạt (grow/shrink), bắt đầu từ vài hundred bytes
- Cheap to create and destroy

```
Platform Thread Model:
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Task 1       │  │  Task 2       │  │  Task 3       │
│  (1 OS Thread)│  │  (1 OS Thread)│  │  (1 OS Thread)│
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       │                  │                  │
───────▼──────────────────▼──────────────────▼──── OS Kernel

Virtual Thread Model (M:N scheduling):
┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐  ... (millions)
│VT 1│ │VT 2│ │VT 3│ │VT 4│ │VT 5│ │VT 6│
└──┬─┘ └──┬─┘ └──┬─┘ └──┬─┘ └──┬─┘ └──┬─┘
   │      │      │      │      │      │
   └──┬───┘      └──┬───┘      └──┬───┘
      │              │              │
┌─────▼────┐  ┌─────▼────┐  ┌─────▼────┐
│Carrier T1│  │Carrier T2│  │Carrier T3│  (few OS threads)
└──────────┘  └──────────┘  └──────────┘
```

**Cơ chế hoạt động — Continuation-based**:

```
Virtual Thread lifecycle:
1. VT được mount lên Carrier Thread (OS thread)
2. VT chạy code
3. VT gặp blocking I/O (HTTP call, DB query, file read, Thread.sleep)
4. JVM UNMOUNT VT khỏi Carrier Thread:
   - Lưu stack state (continuation) trên heap
   - Carrier Thread giải phóng → chạy VT khác
5. I/O hoàn thành → JVM schedule VT lên bất kỳ Carrier Thread nào available
6. VT resume từ continuation → tiếp tục chạy

→ Carrier Threads KHÔNG BAO GIỜ bị block bởi I/O
→ Vài Carrier Threads phục vụ hàng triệu Virtual Threads
```

**Sử dụng**:

```java
// Tạo virtual thread
Thread vt = Thread.ofVirtual().name("my-vt").start(() -> {
    System.out.println("Running on: " + Thread.currentThread());
});

// ExecutorService — mỗi task 1 virtual thread
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    // Submit 100,000 tasks → 100,000 virtual threads (không vấn đề!)
    List<Future<String>> futures = new ArrayList<>();
    for (int i = 0; i < 100_000; i++) {
        futures.add(executor.submit(() -> {
            Thread.sleep(Duration.ofSeconds(1)); // unmount, không tốn OS thread
            return fetchFromAPI();
        }));
    }
    // Lấy kết quả
    for (var future : futures) {
        String result = future.get();
    }
}

// Thread.ofVirtual().factory() cho ThreadFactory
ThreadFactory factory = Thread.ofVirtual().name("worker-", 0).factory();
```

**Khi nào dùng Virtual Threads**:

| ✅ NÊN dùng | ❌ KHÔNG nên dùng |
|---|---|
| I/O-bound tasks (HTTP, DB, file) | CPU-bound tasks (computation, compression) |
| High-concurrency servers | Tasks cần thread affinity |
| Thay thế thread pool cho I/O | |
| Request-per-thread model | |

**Pinning Problem — ⚠️ QUAN TRỌNG (và đã thay đổi theo phiên bản)**:

```java
// Virtual thread bị "PIN" lên carrier thread (KHÔNG unmount được khi block) khi:
// 1. [JDK 21–23] Đang ở trong synchronized block/method
synchronized (lock) {
    httpClient.send(request); // ❌ JDK 21–23: VT bị pin → carrier thread bị block!
                              // ✅ JDK 24+ (JEP 491): VT unmount bình thường
}
// 2. Đang chạy native method / foreign function (mọi phiên bản)
// 3. Đang chạy static initializer của class (<clinit>) — vẫn còn ở JDK 24+

// ✅ Giải pháp cho JDK 21–23: dùng ReentrantLock thay synchronized ở đoạn có I/O
ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    httpClient.send(request); // ✅ VT unmount bình thường khi I/O block
} finally {
    lock.unlock();
}

// Detect pinning:
//  - JDK 21–23: -Djdk.tracePinnedThreads=full (in stack trace khi bị pin)
//  - JDK 24+:   option trên bị xóa → dùng JFR event jdk.VirtualThreadPinned
```

> 📌 Nếu đang dùng **Java 21 LTS**: vẫn cần lưu ý pinning (nhiều thư viện cũ dùng `synchronized` quanh I/O, vd một số JDBC driver đời cũ). Từ **Java 25 LTS** vấn đề `synchronized` đã được giải quyết → không cần thay toàn bộ `synchronized` bằng `ReentrantLock` nữa.

**Những lưu ý khác khi dùng Virtual Threads**:
- Virtual thread **không làm code chạy nhanh hơn** — chỉ tăng **throughput** khi có nhiều tác vụ chờ I/O đồng thời.
- **Tài nguyên phía sau vẫn có giới hạn**: 10.000 virtual thread cùng gọi DB nhưng pool chỉ 20 connection → 9.980 thread chờ connection. Dùng `Semaphore` để giới hạn concurrency tới downstream thay vì giới hạn bằng kích thước thread pool.
- Scheduler mặc định là `ForkJoinPool` với số carrier = số core (`jdk.virtualThreadScheduler.parallelism`).
- Platform thread "1 MB stack" là **virtual memory được reserve**; RAM thực sự commit nhỏ hơn — nhưng chi phí tạo/context switch qua kernel vẫn là thật.

**Best Practices**:

```java
// ✅ KHÔNG pool virtual threads — chúng cheap, tạo mới mỗi task
// ❌ var pool = Executors.newFixedThreadPool(100); // WRONG for virtual threads
// ✅ var executor = Executors.newVirtualThreadPerTaskExecutor();

// ✅ Tránh ThreadLocal nặng — virtual threads có thể rất nhiều
// Dùng ScopedValue thay thế (xem bên dưới)

// ✅ Dùng ReentrantLock thay synchronized (tránh pinning)

// ✅ Structured Concurrency thay manual thread management
```

#### 11.2. Structured Concurrency (⚠️ vẫn là PREVIEW — JDK 21 → 26)

> Trạng thái: preview từ JDK 21 (JEP 453), **JDK 25 là preview lần 5 (JEP 505) với API được thiết kế lại**, JDK 26 tiếp tục preview. Cần `--enable-preview`. Các class `ShutdownOnFailure` / `ShutdownOnSuccess` của JDK 21–24 **đã bị xóa**, thay bằng `StructuredTaskScope.open(...)` + **`Joiner`**. Kiểm tra lại API theo đúng phiên bản JDK đang dùng.

```java
// Structured Concurrency: quản lý lifetime của concurrent subtasks như 1 khối code
// Nếu 1 subtask fail → cancel tất cả subtasks khác; scope đóng → không còn thread "rò rỉ"

// ── API JDK 25 (preview) ──
// Mặc định (Joiner.awaitAllSuccessfulOrThrow): chờ tất cả thành công, 1 cái lỗi → hủy phần còn lại
try (var scope = StructuredTaskScope.open()) {
    Subtask<User> userTask = scope.fork(() -> fetchUser(id));
    Subtask<List<Order>> ordersTask = scope.fork(() -> fetchOrders(id));

    scope.join();   // chờ; nếu có subtask lỗi → ném FailedException (cause = lỗi gốc)

    return new UserProfile(userTask.get(), ordersTask.get());
}

// "Ai nhanh thì lấy" — trả kết quả của subtask thành công đầu tiên, hủy các subtask còn lại
try (var scope = StructuredTaskScope.open(Joiner.<String>anySuccessfulResultOrThrow())) {
    scope.fork(() -> fetchFromServer1());
    scope.fork(() -> fetchFromServer2());
    scope.fork(() -> fetchFromServer3());
    String fastest = scope.join();   // join() trả luôn kết quả theo Joiner
}

// Timeout cho cả scope
try (var scope = StructuredTaskScope.open(Joiner.awaitAllSuccessfulOrThrow(),
                                          cf -> cf.withTimeout(Duration.ofSeconds(2)))) { ... }
```

```java
// ── API cũ JDK 21–24 (preview, đã bị xóa từ JDK 25) — để đọc hiểu code/tài liệu cũ ──
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Subtask<User> userTask = scope.fork(() -> fetchUser(id));
    scope.join().throwIfFailed();
    ...
}
```
- So với `CompletableFuture`/`ExecutorService` thủ công: lỗi tự lan truyền & hủy subtask anh em, thread dump hiển thị quan hệ cha–con, không quên `shutdown()`.
- Subtask được fork kế thừa **ScopedValue** binding của scope cha.

#### 11.3. Scoped Values (Preview JDK 20–24 → **Final ở Java 25**, JEP 506)

Thay thế ThreadLocal cho virtual threads — immutable, inherited, automatically cleaned up:

```java
// Khai báo
private static final ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();

// Bind value
ScopedValue.where(CURRENT_USER, authenticatedUser).run(() -> {
    processRequest(); // CURRENT_USER available trong scope này
    // Mọi code gọi từ đây đều thấy CURRENT_USER
});

// Đọc
public void processRequest() {
    User user = CURRENT_USER.get(); // lấy giá trị từ enclosing scope
    if (CURRENT_USER.isBound()) { ... }
}

// Ưu điểm so với ThreadLocal:
// 1. Immutable — không thể set() từ nơi khác
// 2. Scoped lifetime — tự động cleanup khi scope kết thúc
// 3. Không memory leak — không cần remove()
// 4. Inherited bởi child threads (StructuredTaskScope)
```

#### 11.4. Pattern Matching for Switch (Finalized)

```java
// Guarded patterns (when clause)
String describe(Object obj) {
    return switch (obj) {
        case Integer i when i > 0 -> "positive integer: " + i;
        case Integer i            -> "non-positive integer: " + i;
        case String s when s.length() > 10 -> "long string";
        case String s             -> "string: " + s;
        case int[] arr            -> "int array of length " + arr.length;
        case null                 -> "null!"; // handle null!
        default                   -> "unknown: " + obj;
    };
}

// Record Patterns — destructuring
record Point(int x, int y) {}
record Line(Point start, Point end) {}

String describe(Object obj) {
    return switch (obj) {
        case Point(int x, int y) -> "Point at " + x + "," + y;
        case Line(Point(int x1, int y1), Point(int x2, int y2))
            -> "Line from (%d,%d) to (%d,%d)".formatted(x1, y1, x2, y2);
        default -> "unknown";
    };
}
```

#### 11.5. Sequenced Collections (Java 21)

Interfaces mới cho collections có thứ tự xác định:

```java
// SequencedCollection<E>: List, Deque, SortedSet, LinkedHashSet
interface SequencedCollection<E> extends Collection<E> {
    SequencedCollection<E> reversed();  // reversed view
    void addFirst(E e);
    void addLast(E e);
    E getFirst();
    E getLast();
    E removeFirst();
    E removeLast();
}

// SequencedMap<K,V>: SortedMap, LinkedHashMap
interface SequencedMap<K,V> extends Map<K,V> {
    SequencedMap<K,V> reversed();
    Map.Entry<K,V> firstEntry();
    Map.Entry<K,V> lastEntry();
    Map.Entry<K,V> pollFirstEntry();
    Map.Entry<K,V> pollLastEntry();
    V putFirst(K k, V v);
    V putLast(K k, V v);
    SequencedSet<K> sequencedKeySet();
    SequencedCollection<V> sequencedValues();
    SequencedSet<Map.Entry<K,V>> sequencedEntrySet();
}

// Giải quyết inconsistency lâu năm:
// Trước Java 21:
// List:          list.get(0),          list.get(list.size()-1)
// Deque:         deque.getFirst(),     deque.getLast()
// SortedSet:     sortedSet.first(),    sortedSet.last()
// LinkedHashSet: ???                   ??? (không có API!)
// Bây giờ: thống nhất getFirst() / getLast() cho tất cả
```

**⚖️ Platform thread pool vs Reactive vs Virtual threads**

| | Thread pool (platform) | Reactive (WebFlux) | Virtual threads |
| :--- | :--- | :--- | :--- |
| Mô hình code | Tuần tự, blocking | Chuỗi operator, non-blocking | Tuần tự, blocking |
| Số tác vụ chờ I/O đồng thời | Vài trăm–vài nghìn | Rất lớn | Rất lớn |
| Debug / stack trace | Dễ | Khó | Dễ |
| Thư viện | Mọi thư viện | Phải có driver reactive (R2DBC...) | Mọi thư viện blocking (JDBC...) |
| Backpressure | Qua hàng đợi | Có sẵn | Phải tự giới hạn (Semaphore) |
| CPU-bound | ✅ | Không lợi | Không lợi |

✅ **Vì sao virtual threads thành lựa chọn mặc định mới cho ứng dụng I/O-bound:** giữ sự đơn giản của code tuần tự và toàn bộ hệ sinh thái blocking (JDBC, JPA), trong khi đạt mức đồng thời mà trước đây chỉ reactive làm được. Reactive vẫn đáng dùng khi cần streaming có backpressure.

> 🛠 **Áp dụng:** Spring Boot `spring.threads.virtual.enabled=true`; **giới hạn concurrency tới tài nguyên hữu hạn** (DB pool); dùng Java 24+ để `synchronized` không còn gây pinning; không pool virtual thread.

<details><summary>🧠 Tự kiểm tra</summary>

1. **Virtual thread có làm một request đơn lẻ nhanh hơn không?**
   → Không. Nó tăng **số request chạy đồng thời** khi phần lớn thời gian là chờ I/O; thời gian xử lý mỗi request không đổi.
2. **"Pinning" là gì và vì sao nguy hiểm?**
   → Virtual thread không thể tháo khỏi thread OS khi đang block (ví dụ trong `synchronized` ở JDK 21–23, hoặc native code) → chiếm luôn carrier thread; nhiều thread bị pin cùng lúc thì toàn bộ hệ thống chỉ còn vài carrier rảnh.
</details>


---

### 12. Java 22 → 25 Features (Java 25 LTS — 9/2025)

> 🎯 **Bối cảnh:** Các phiên bản 22–25 tiếp tục ba hướng: (1) **concurrency dễ đúng** (structured concurrency, scoped values — khắc phục "rò rỉ" thread và ThreadLocal khi có hàng triệu virtual thread); (2) **bớt nghi thức cho người mới & script** (compact source file, module import); (3) **hiệu năng & tương tác native** (FFM API thay JNI, compact object headers, Project Leyden giảm thời gian khởi động). Khi đọc từng feature, hãy hỏi: *nó giải quyết điểm đau nào của cách cũ?*


Java 25 là LTS mới nhất (bản LTS kế tiếp dự kiến là Java 29). Dưới đây gồm các feature đã **final** trong JDK 22–25 (tức là có sẵn khi nâng từ 21 lên 25) và ghi rõ những gì **vẫn còn preview**.

| Feature | JEP | Trạng thái ở JDK 25 |
|---|---|---|
| Unnamed variables & patterns (`_`) | 456 | Final (JDK 22) |
| Foreign Function & Memory API | 454 | Final (JDK 22) |
| Launch multi-file source programs | 458 | Final (JDK 22) |
| Stream Gatherers | 485 | Final (JDK 24) |
| Class-File API | 484 | Final (JDK 24) |
| Synchronize virtual threads without pinning | 491 | Final (JDK 24) |
| **Scoped Values** | 506 | **Final (JDK 25)** |
| **Flexible Constructor Bodies** | 513 | **Final (JDK 25)** |
| **Module Import Declarations** | 511 | **Final (JDK 25)** |
| **Compact Source Files & Instance Main Methods** | 512 | **Final (JDK 25)** |
| **Compact Object Headers** | 519 | **Product (JDK 25)** — bật bằng flag |
| Generational Shenandoah | 521 | Product (JDK 25) |
| AOT command-line ergonomics / AOT method profiling (Project Leyden — giảm startup/warm-up) | 514, 515 | Final (JDK 25) |
| Key Derivation Function API | 510 | Final (JDK 25) |
| Structured Concurrency | 505 | ⚠️ **Preview** (lần 5) |
| Primitive Types in Patterns, instanceof, switch | 507 | ⚠️ **Preview** (lần 3) |
| Stable Values (JDK 26 đổi tên thành Lazy Constants) | 502 | ⚠️ Preview |
| PEM Encodings of Cryptographic Objects | 470 | ⚠️ Preview |
| Vector API | 508 | ⚠️ Incubator (lần 10) |

#### 12.1. Structured Concurrency (vẫn Preview)
- Đã cover ở mục 11.2 — JDK 25 đổi sang API `StructuredTaskScope.open()` + `Joiner`; chưa final.

#### 12.2. Scoped Values (Final)
- `ScopedValue` API ổn định (đã cover ở mục 11.3). Thay thế ThreadLocal cho việc truyền context một chiều (user, tenant, trace id) xuống call stack, đặc biệt với virtual threads.

#### 12.3. Stream Gatherers (Final từ JDK 24)

Custom intermediate stream operations — mở rộng Stream API vượt ra ngoài built-in operations:

```java
// Built-in gatherers
// windowFixed: chia stream thành fixed-size windows
Stream.of(1, 2, 3, 4, 5, 6, 7)
    .gather(Gatherers.windowFixed(3))
    .toList();
// → [[1, 2, 3], [4, 5, 6], [7]]

// windowSliding: sliding window
Stream.of(1, 2, 3, 4, 5)
    .gather(Gatherers.windowSliding(3))
    .toList();
// → [[1, 2, 3], [2, 3, 4], [3, 4, 5]]

// fold: stateful reduction thành 1 giá trị (khác reduce vì có initializer và type khác)
Stream.of("a", "b", "c")
    .gather(Gatherers.fold(() -> "", (state, element) -> state + element))
    .toList();
// → ["abc"]

// scan: running accumulation (emit mỗi intermediate result)
Stream.of(1, 2, 3, 4)
    .gather(Gatherers.scan(() -> 0, Integer::sum))
    .toList();
// → [1, 3, 6, 10]

// mapConcurrent: xử lý concurrent (kết hợp virtual threads)
Stream.of(url1, url2, url3)
    .gather(Gatherers.mapConcurrent(10, url -> fetchContent(url)))
    .toList();
// → Tối đa 10 virtual threads chạy đồng thời

// Custom Gatherer: implement Gatherer<T, A, R>
// T: input type, A: state type, R: output type
```

#### 12.4. Flexible Constructor Bodies

Cho phép statements TRƯỚC `super()` hoặc `this()` call — giải quyết limitation lâu năm:

```java
public class Person {
    protected final String name;
    public Person(String name) { this.name = name; }
}

// ❌ Trước Java 25: super(...) bắt buộc là câu lệnh ĐẦU TIÊN
//    → phải validate bằng static helper: super(validate(name))
public class Employee extends Person {
    private final String department;

    // ✅ Java 25: được viết code TRƯỚC super() — gọi là "prologue"
    public Employee(String name, String department) {
        if (name == null || name.isBlank())                 // validate trước khi gọi constructor cha
            throw new IllegalArgumentException("name is blank");
        this.department = department;                        // ✅ được GÁN field của chính class trước super()
        super(name.strip());
    }
}

// Hạn chế trong prologue: KHÔNG được đọc field, gọi instance method, hay dùng `this`
// (ngoại trừ gán giá trị cho field chưa khởi tạo của chính class).
// Lợi ích của việc gán field trước super(): nếu constructor cha gọi method bị override,
// method đó sẽ thấy field đã có giá trị thay vì null (xem bẫy ở 7.5).
```

#### 12.5. Primitive Types in Patterns (⚠️ Preview — JEP 455 → 488 → 507)

```java
// Cần --enable-preview. Pattern matching hỗ trợ primitive types
String msg = switch (statusCode) {
    case 200 -> "OK";
    case 404 -> "Not Found";
    case int i when i >= 500 -> "Server Error: " + i;
    case int i -> "Unknown: " + i;
};

// instanceof kiểm tra chuyển đổi an toàn (không mất thông tin)
long big = 42L;
if (big instanceof int i) {          // true vì 42 vừa với int
    System.out.println("fits in int: " + i);
}

// instanceof với primitive
if (obj instanceof int i) {
    System.out.println("int value: " + i);
}
```

#### 12.6. Module Import Declarations

```java
// Import toàn bộ packages exported bởi 1 module
import module java.base;  // import java.util.*, java.io.*, java.lang.*, etc.
import module java.sql;   // import java.sql.*, javax.sql.*

// Giảm boilerplate import statements
// Đặc biệt hữu ích cho scripts và small programs
```

#### 12.7. Compact Source Files, Instance `main` & Source Launcher

```java
// Java 25 (JEP 512, final): file nguồn "compact" — không cần khai báo class, main không cần static/public/args
void main() {
    IO.println("Hello, Java 25!");   // java.lang.IO: println/readln tiện cho script & người mới học
}
```

```bash
# Chạy file .java trực tiếp không cần javac:
#  - Java 11 (JEP 330): 1 file nguồn
#  - Java 22 (JEP 458): nhiều file — tự tìm & compile các class khác trong cùng thư mục
java Main.java

# Shebang (Unix): dòng đầu file script
#!/usr/bin/java --source 25
```

#### 12.8. Foreign Function & Memory API (Final từ JDK 22 — Project Panama)

Thay thế JNI để gọi native code — type-safe, efficient, không cần viết C wrapper:

```java
// Gọi C function strlen
try (Arena arena = Arena.ofConfined()) {
    MemorySegment cString = arena.allocateFrom("Hello");

    Linker linker = Linker.nativeLinker();
    SymbolLookup stdlib = linker.defaultLookup();

    MethodHandle strlen = linker.downcallHandle(
        stdlib.find("strlen").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
    );

    long len = (long) strlen.invoke(cString); // len = 5
}
```

#### 12.9. Các thay đổi quan trọng khác

- **Security Manager**: deprecated for removal từ Java 17, **bị vô hiệu hóa vĩnh viễn** từ JDK 24 (JEP 486 — không thể bật lại; API vẫn còn để code cũ compile được).
- **Vector API** (vẫn incubating): SIMD operations trong Java — `FloatVector`, `IntVector` cho parallel math
- **Class-File API** (final JDK 24): Programmatic parsing và generation bytecode — thay thế dần ASM trong chính JDK
- **Compact Object Headers** (product JDK 25, `-XX:+UseCompactObjectHeaders`): Object header từ 12 bytes → 8 bytes → giảm memory footprint (xem 7.2)
- **Unnamed variables** `_` (JDK 22): `catch (Exception _)`, `for (var _ : list)`, `case Point(int x, _)`
- **Project Leyden** (JDK 24–25): AOT class loading & linking cache (`-XX:AOTCache`) giảm thời gian khởi động/warm-up — đối trọng nhẹ nhàng hơn GraalVM native image
- Bỏ hỗ trợ **32-bit x86** (JDK 25)

> **JDK 26** (3/2026, non-LTS): tiếp tục preview Structured Concurrency, Primitive Patterns, Lazy Constants (tên mới của Stable Values); bổ sung **HTTP/3** cho `java.net.http.HttpClient`; xóa hẳn Applet API; bắt đầu cảnh báo khi dùng deep reflection để sửa field `final` ("prepare to make final mean final"). Luôn đối chiếu với release notes chính thức vì danh sách có thể thay đổi.

---

### 13. Design Patterns in Java

> 🎯 **Bài toán:** Những vấn đề thiết kế lặp lại (tạo object phức tạp, thêm hành vi mà không sửa class, đổi thuật toán lúc runtime, thông báo khi có sự kiện...) — mỗi người tự nghĩ một kiểu thì code khó đọc và khó trao đổi.
>
> 💡 **Ý tưởng:** Design pattern là **lời giải đã được kiểm chứng** kèm **tên gọi chung**. Hiểu pattern = hiểu **vấn đề nó giải** và **cái giá** (thêm lớp trừu tượng). Đừng áp pattern khi chưa có vấn đề.

| Vấn đề | Pattern |
| :--- | :--- |
| Constructor quá nhiều tham số, nhiều tùy chọn | Builder |
| Tạo object mà không để nơi dùng phụ thuộc lớp cụ thể | Factory |
| Thêm hành vi (log, cache, nén) quanh object mà không sửa nó | Decorator / Proxy |
| Chọn thuật toán lúc runtime | Strategy |
| Nhiều bên cần biết khi có sự kiện | Observer (event) |
| Khung xử lý cố định, bước cụ thể thay đổi | Template Method |
| Request đi qua chuỗi bước xử lý | Chain of Responsibility |


#### 13.1. Creational Patterns

**Singleton** — 5 cách (xem mục 9.11)

**Builder Pattern**:

```java
public class HttpRequest {
    private final String url;
    private final String method;
    private final Map<String, String> headers;
    private final String body;

    private HttpRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    public static class Builder {
        private final String url; // required
        private String method = "GET";
        private Map<String, String> headers = new HashMap<>();
        private String body;

        public Builder(String url) { this.url = url; }
        public Builder method(String method) { this.method = method; return this; }
        public Builder header(String key, String value) { headers.put(key, value); return this; }
        public Builder body(String body) { this.body = body; return this; }
        public HttpRequest build() { return new HttpRequest(this); }
    }
}

// Usage (fluent API):
HttpRequest request = new HttpRequest.Builder("https://api.example.com")
    .method("POST")
    .header("Content-Type", "application/json")
    .body("{\"name\":\"John\"}")
    .build();
```

**Factory Method**:

```java
public interface Notification { void send(String message); }

public class NotificationFactory {
    public static Notification create(String type) {
        return switch (type) {
            case "email" -> new EmailNotification();
            case "sms"   -> new SmsNotification();
            case "push"  -> new PushNotification();
            default      -> throw new IllegalArgumentException("Unknown: " + type);
        };
    }
}
```

#### 13.2. Structural Patterns

**Decorator** — Ví dụ kinh điển: Java I/O Streams:

```java
// Java I/O dùng Decorator pattern rất nhiều
InputStream is = new BufferedInputStream(        // thêm buffering
                    new GZIPInputStream(          // thêm decompression
                        new FileInputStream("data.gz"))); // base stream

// Custom decorator
public interface DataSource {
    String readData();
    void writeData(String data);
}

public class CompressionDecorator implements DataSource {
    private final DataSource wrappee;
    public CompressionDecorator(DataSource source) { this.wrappee = source; }

    @Override
    public String readData() {
        return decompress(wrappee.readData()); // thêm tính năng rồi delegate
    }
}
```

**Proxy** — JDK Dynamic Proxy:

```java
// JDK Dynamic Proxy: chỉ cho interfaces
public class LoggingProxy implements InvocationHandler {
    private final Object target;

    public LoggingProxy(Object target) { this.target = target; }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Before: " + method.getName());
        Object result = method.invoke(target, args); // delegate to real object
        System.out.println("After: " + method.getName());
        return result;
    }
}

UserService proxy = (UserService) Proxy.newProxyInstance(
    UserService.class.getClassLoader(),
    new Class[]{UserService.class},
    new LoggingProxy(realUserService)
);
// Spring AOP dùng JDK Dynamic Proxy (interface) hoặc CGLIB Proxy (class)
```

#### 13.3. Behavioral Patterns

**Strategy**:

```java
// Define strategies
@FunctionalInterface
public interface SortStrategy<T> {
    void sort(List<T> list);
}

// Với Java 8+, strategy thường là lambda
List<String> names = new ArrayList<>(List.of("Charlie", "Alice", "Bob"));

SortStrategy<String> byNatural = Collections::sort;           // natural order
SortStrategy<String> byLength = list -> list.sort(Comparator.comparingInt(String::length));
SortStrategy<String> byReverse = list -> list.sort(Comparator.reverseOrder());

byLength.sort(names); // ["Bob", "Alice", "Charlie"]
```

**Observer** — Trong JDK và Spring:

```java
// JDK: PropertyChangeListener
// Spring: ApplicationEvent + ApplicationListener hoặc @EventListener
@Component
public class OrderService {
    @Autowired private ApplicationEventPublisher publisher;

    public void createOrder(Order order) {
        // ... save order
        publisher.publishEvent(new OrderCreatedEvent(order));
    }
}

@Component
public class EmailNotifier {
    @EventListener   // ⚠️ chạy ĐỒNG BỘ trong cùng transaction/thread với publisher
    public void onOrderCreated(OrderCreatedEvent event) {
        sendEmail(event.getOrder());
    }
}
// Gửi email chỉ khi transaction đã commit thành công:
// @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) (+ @Async nếu không muốn chặn request)
```

**Template Method** — khung thuật toán cố định ở lớp cha, các bước cụ thể do lớp con quyết định (`AbstractList`, `HttpServlet.service() → doGet/doPost`, Spring `JdbcTemplate`/`RestTemplate`/`TransactionTemplate` là biến thể dùng callback thay vì kế thừa).

**Chain of Responsibility** — request đi qua chuỗi handler, mỗi handler xử lý hoặc chuyển tiếp: Servlet `FilterChain`, Spring Security `SecurityFilterChain`, `HandlerInterceptor`, Netty `ChannelPipeline`.

#### 13.4. Bảng tổng hợp: Pattern xuất hiện ở đâu trong JDK & Spring

| Pattern | Ví dụ trong JDK | Ví dụ trong Spring / thư viện |
|---|---|---|
| Singleton | `Runtime.getRuntime()` | Bean scope singleton (mặc định) |
| Factory Method / Static Factory | `List.of()`, `Calendar.getInstance()`, `Executors.newFixedThreadPool()` | `BeanFactory`, `FactoryBean` |
| Builder | `StringBuilder`, `HttpClient.newBuilder()`, `Stream.builder()` | `UriComponentsBuilder`, `RestClient.builder()`, Lombok `@Builder` |
| Prototype | `Object.clone()` | Bean scope prototype |
| Adapter | `Arrays.asList()`, `InputStreamReader` (byte → char) | `HandlerAdapter` của Spring MVC |
| Decorator | `BufferedInputStream`, `Collections.synchronizedList()` | `TransactionAwareDataSourceProxy`, `BeanPostProcessor` bọc bean |
| Proxy | `java.lang.reflect.Proxy` | Spring AOP (`@Transactional`, `@Async`, `@Cacheable`), Hibernate lazy loading proxy |
| Facade | `javax.faces`, `java.net.URL` | `JdbcTemplate` (che giấu Connection/Statement/ResultSet) |
| Composite | `java.awt.Container` | `CompositeCacheManager` |
| Flyweight | `Integer.valueOf()` cache, String Pool | — |
| Strategy | `Comparator`, `ThreadPoolExecutor` `RejectedExecutionHandler` | `PlatformTransactionManager`, `ResourceLoader`, `PasswordEncoder` |
| Template Method | `AbstractList`, `HttpServlet` | `JdbcTemplate`, `AbstractRoutingDataSource` |
| Observer | `PropertyChangeListener`, `Flow` (reactive streams) | `ApplicationEvent` / `@EventListener` |
| Chain of Responsibility | `java.util.logging.Logger` (handler cha) | `FilterChain`, `SecurityFilterChain` |
| Iterator | `Iterator`, `Spliterator` | — |
| Command | `Runnable`, `Callable` | `TaskExecutor` task |

---

### 14. Spring & Spring Boot Essentials

#### 14.1. IoC & Dependency Injection

```java
// Constructor injection — ✅ RECOMMENDED
@Service
public class OrderService {
    private final OrderRepository repo;
    private final PaymentService payment;

    // @Autowired không bắt buộc khi chỉ có 1 constructor (Spring 4.3+)
    public OrderService(OrderRepository repo, PaymentService payment) {
        this.repo = repo;
        this.payment = payment;
    }
}

// Tại sao Constructor Injection tốt hơn:
// 1. Immutable (final fields)
// 2. Không thể tạo object thiếu dependency (compile-time check)
// 3. Dễ test (new OrderService(mockRepo, mockPayment))
// 4. Thấy rõ khi class có quá nhiều dependencies (SRP violation)
```

#### 14.2. Bean Lifecycle

```
Container starts
    │
    ▼
Instantiation (new or factory method)
    │
    ▼
Populate Properties (DI)
    │
    ▼
BeanNameAware.setBeanName()
    │
    ▼
BeanFactoryAware.setBeanFactory()
    │
    ▼
ApplicationContextAware.setApplicationContext()
    │
    ▼
BeanPostProcessor.postProcessBeforeInitialization()
    │
    ▼
@PostConstruct
    │
    ▼
InitializingBean.afterPropertiesSet()
    │
    ▼
Custom init-method
    │
    ▼
BeanPostProcessor.postProcessAfterInitialization()
    │  (AOP proxies created HERE)
    ▼
════════ Bean is READY ════════
    │
    ▼ (Container shutdown)
@PreDestroy
    │
    ▼
DisposableBean.destroy()
    │
    ▼
Custom destroy-method
```

#### 14.3. @Transactional

```java
@Transactional(
    propagation = Propagation.REQUIRED,    // default
    isolation = Isolation.DEFAULT,         // default: dùng isolation mặc định của DB (PG: READ_COMMITTED, MySQL: REPEATABLE_READ)
    readOnly = false,
    timeout = 30,
    rollbackFor = Exception.class          // default chỉ rollback RuntimeException và Error, KHÔNG rollback checked exception
)
public void transferMoney(Long from, Long to, BigDecimal amount) { ... }
```

**Propagation types**:

| Type | Behavior |
|------|----------|
| `REQUIRED` (default) | Dùng transaction hiện tại, nếu không có → tạo mới |
| `REQUIRES_NEW` | Luôn tạo transaction MỚI, suspend transaction cũ |
| `NESTED` | Tạo savepoint trong transaction hiện tại |
| `SUPPORTS` | Dùng transaction hiện tại nếu có, nếu không → chạy không transaction |
| `NOT_SUPPORTED` | Chạy không transaction, suspend transaction hiện tại nếu có |
| `NEVER` | PHẢI chạy không transaction, throw nếu có transaction |
| `MANDATORY` | PHẢI có transaction, throw nếu không có |

> **⚠️ Self-invocation pitfall**: Gọi method `@Transactional` từ CÙNG class → KHÔNG có transaction!
> Vì Spring AOP dùng proxy → chỉ intercept khi gọi qua proxy reference.
> 
> ```java
> @Service
> public class OrderService {
>     public void processOrder() {
>         this.saveOrder(); // ❌ Gọi trực tiếp, bypass proxy → no transaction!
>     }
>
>     @Transactional
>     public void saveOrder() { ... }
> }
> 
> // Giải pháp: Inject self, hoặc tách sang class khác
> ```

#### 14.4. Spring Boot Auto-configuration

```java
// Cơ chế:
// 1. @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
// 2. @EnableAutoConfiguration → đọc META-INF/spring.factories (hoặc spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports)
// 3. Mỗi AutoConfiguration class dùng @Conditional để quyết định có activate không

@Configuration
@ConditionalOnClass(DataSource.class)               // chỉ khi class có trên classpath
@ConditionalOnProperty(name = "spring.datasource.url") // chỉ khi property tồn tại
@ConditionalOnMissingBean(DataSource.class)          // chỉ khi user chưa define bean này
public class DataSourceAutoConfiguration {
    @Bean
    public DataSource dataSource() { ... }
}

// → "Convention over Configuration": chỉ cần thêm dependency → auto-configured
// → Override bằng cách define own @Bean
```

#### 14.5. Circular Dependency & Three-Level Cache

```
singletonObjects (Level 1)       ── Bean hoàn chỉnh (đã init xong)
earlySingletonObjects (Level 2)  ── Bean sơ khai (chưa inject dependencies)
singletonFactories (Level 3)     ── ObjectFactory tạo Early Reference / Proxy
```
- **Constructor Injection**: Gây deadlock khởi tạo (`BeanCurrentlyInCreationException`).
- **Field/Setter Injection**: Spring giải quyết bằng 3 cấp cache (Level 3 sinh sớm proxy, đưa vào Level 2, sau đó hoàn thiện vào Level 1).
- **Spring Boot 2.6+**: Mặc định cấm circular references (`spring.main.allow-circular-references=false`) để chống code smell.

#### 14.6. Spring Data JPA & Vấn đề N+1 Query

```java
// N+1 Query: 1 query lấy N records cha, N query con lấy collection quan hệ Lazy
// Giải pháp 1: JOIN FETCH trong JPQL
@Query("SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.employees")
List<Department> findAllWithEmployees();

// Giải pháp 2: @EntityGraph
@EntityGraph(attributePaths = {"employees"})
List<Department> findAll();

// Giải pháp 3: Batch fetching (tối ưu khi phân trang Pageable)
// application.yml: spring.jpa.properties.hibernate.default_batch_fetch_size: 50
```

#### 14.7. Spring Kafka: Manual Topic & Partition Assignment

> 💡 *Thiết lập gán partition thủ công không qua Consumer Group rebalance:*

```java
// Cách 1: @KafkaListener gán cố định partition 0 và 1 của topic "orders"
@KafkaListener(
    id = "manualPartitionListener",
    topicPartitions = @TopicPartition(
        topic = "orders", 
        partitions = {"0", "1"}
    )
)
public void listenSpecificPartitions(ConsumerRecord<String, String> record) {
    System.out.printf("Partition: %d, Offset: %d, Msg: %s%n",
            record.partition(), record.offset(), record.value());
}

// Cách 2: Gán partition kèm replay từ offset xác định
@KafkaListener(
    id = "seekListener",
    topicPartitions = @TopicPartition(
        topic = "orders",
        partitionOffsets = @PartitionOffset(partition = "2", initialOffset = "0")
    )
)
public void replayFromBeginning(ConsumerRecord<String, String> record) { ... }
```

> 📖 **Xem tài liệu chi tiết đầy đủ**: Toàn bộ kiến trúc chuyên sâu về Bean Lifecycle, Security JWT, JPA Internals, WebFlux, Kafka, Virtual Threads và câu hỏi phỏng vấn Spring Boot Senior xem tại [`SpringBoot.md`](SpringBoot.md).

---

### 15. I/O, NIO & Networking Model

> 🎯 **Bài toán:** Server phải giữ hàng chục nghìn kết nối (C10K); với blocking I/O mỗi kết nối chiếm một thread dù phần lớn thời gian không có dữ liệu.
>
> 💡 **Ý tưởng giải:** **Non-blocking I/O + I/O multiplexing**: một thread hỏi OS "trong hàng nghìn socket, cái nào đang có dữ liệu?" (`epoll`) rồi chỉ xử lý những cái đó (Reactor/event loop — Netty). Giảm copy dữ liệu bằng **zero-copy**.
>
> ✅ **Vì sao hiện nay vẫn có hai lựa chọn:** event loop (Netty, WebFlux) hiệu quả tài nguyên nhất nhưng code khó và **không được block**; virtual threads cho phép code blocking quen thuộc với hiệu quả gần tương đương. Hạ tầng (gateway, proxy, broker) thường chọn event loop; ứng dụng nghiệp vụ chọn virtual threads.


> 💡 Nền tảng mạng (TCP, DNS, HTTP, TLS, các exception kết nối, timeout, stale keep-alive) xem [`Networking.md`](Networking.md) — đặc biệt mục 13 "Networking trong Java".

#### 15.1. I/O Models: BIO vs NIO vs AIO

| Mô hình | Cơ chế | Ví dụ trong Java | Đặc điểm |
|---|---|---|---|
| **BIO** (Blocking I/O) | 1 thread chờ (block) cho tới khi dữ liệu đọc/ghi xong | `InputStream`, `Socket`, Servlet truyền thống (Tomcat thread-per-request) | Code đơn giản; 10k kết nối = 10k thread (nay giải quyết bằng virtual threads) |
| **NIO** (Non-blocking + I/O multiplexing) | 1 thread dùng **Selector** theo dõi hàng nghìn channel, chỉ xử lý channel nào "sẵn sàng" | `java.nio.channels.Selector`, `SocketChannel`, **Netty**, Tomcat NIO connector | Ít thread, scale tốt; code phức tạp (event loop, callback) |
| **AIO** (Asynchronous I/O, NIO.2) | Gửi yêu cầu rồi được **callback/Future** khi hoàn tất | `AsynchronousSocketChannel`, `AsynchronousFileChannel` | Trên Linux phần lớn vẫn mô phỏng bằng epoll + thread pool → ít dùng |

**I/O multiplexing ở tầng OS**: `select` (giới hạn 1024 fd, quét O(n)) → `poll` (không giới hạn fd, vẫn O(n)) → **`epoll`** (Linux: chỉ trả về fd sẵn sàng, O(1) theo số sự kiện; edge/level-triggered) / `kqueue` (BSD/macOS) / **`io_uring`** (Linux 5.1+, async thực sự qua ring buffer chia sẻ với kernel). Java `Selector` trên Linux dùng epoll.

```java
// NIO server tối giản — 1 thread phục vụ nhiều client
Selector selector = Selector.open();
ServerSocketChannel server = ServerSocketChannel.open();
server.bind(new InetSocketAddress(8080));
server.configureBlocking(false);
server.register(selector, SelectionKey.OP_ACCEPT);

while (true) {
    selector.select();                                   // block tới khi có ít nhất 1 channel sẵn sàng
    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
    while (it.hasNext()) {
        SelectionKey key = it.next(); it.remove();
        if (key.isAcceptable()) {
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
        } else if (key.isReadable()) {
            SocketChannel client = (SocketChannel) key.channel();
            ByteBuffer buf = ByteBuffer.allocate(1024);
            if (client.read(buf) == -1) { client.close(); continue; }
            buf.flip();                                  // chuyển buffer từ chế độ ghi sang đọc
            client.write(buf);                           // echo
        }
    }
}
```

#### 15.2. Buffer, Channel & Zero-copy
- **`ByteBuffer`**: có `position`, `limit`, `capacity`; `flip()` (ghi xong → chuẩn bị đọc), `clear()`/`compact()` (chuẩn bị ghi tiếp).
- **Heap buffer** (`allocate`) vs **Direct buffer** (`allocateDirect`): direct nằm ngoài heap → OS đọc/ghi trực tiếp không cần copy thêm, nhưng cấp phát/giải phóng đắt và không bị GC quản lý trực tiếp (giới hạn `-XX:MaxDirectMemorySize`) → nên **pool** (Netty `PooledByteBufAllocator`).
- **Zero-copy**:
    - `FileChannel.transferTo()` → syscall **`sendfile`**: dữ liệu đi từ page cache thẳng ra socket buffer, không qua user space. Đây là bí quyết throughput của **Kafka** khi consumer đọc dữ liệu (xem SystemDesign 3.2).
    - `FileChannel.map()` → **memory-mapped file** (`MappedByteBuffer`): truy cập file như mảng trong bộ nhớ (RocketMQ, Lucene dùng).
```
Copy truyền thống (read + write):   Disk → Page Cache → [User Buffer] → Socket Buffer → NIC   (4 lần copy, 4 context switch)
sendfile (zero-copy):               Disk → Page Cache ───────────────→ Socket Buffer → NIC   (2–3 lần copy, 2 context switch; với DMA gather còn ít hơn)
```

#### 15.3. Reactor Pattern & Netty
- **Reactor**: 1 (hoặc vài) **event loop** thread chờ sự kiện I/O và dispatch cho handler. Netty dùng **Main-Sub Reactor**: `bossGroup` (accept connection) + `workerGroup` (N event loop, mỗi loop quản lý nhiều channel suốt vòng đời của chúng).
- ⚠️ **Quy tắc vàng**: không bao giờ chạy code **blocking** (JDBC, `Thread.sleep`, HTTP blocking) trên event loop thread → toàn bộ kết nối của loop đó bị treo. Đẩy sang thread pool riêng.
- Ai dùng Netty: Spring WebFlux (Reactor Netty), gRPC-Java, Elasticsearch, Cassandra driver, Lettuce/Redisson (Redis client). (Kafka thì không dùng Netty — tự cài đặt network layer trên Java NIO.)
- **Virtual threads vs Reactive**: virtual threads cho phép viết code blocking "kiểu cũ" mà vẫn scale như NIO cho phần lớn ứng dụng I/O-bound; reactive (WebFlux) vẫn có lợi thế khi cần backpressure, streaming, hoặc đã có hệ sinh thái reactive.

#### 15.4. `java.time` (Java 8) — đừng dùng `Date`/`Calendar`
| Class | Ý nghĩa |
|---|---|
| `Instant` | Một điểm trên timeline UTC (timestamp) — dùng để lưu/truyền |
| `LocalDate`, `LocalTime`, `LocalDateTime` | Ngày/giờ **không có time zone** (ngày sinh, giờ mở cửa) |
| `ZonedDateTime`, `OffsetDateTime` | Có time zone/offset — dùng khi hiển thị/tính theo lịch địa phương |
| `Duration` / `Period` | Khoảng thời gian theo giây-nano / theo ngày-tháng-năm |
| `DateTimeFormatter` | **Immutable & thread-safe** (khác `SimpleDateFormat` — không thread-safe) |
- Inject `Clock` vào code thay vì gọi `Instant.now()` trực tiếp → test được.

---

### 16. Serialization, Reflection & Annotations

> 🎯 **Bài toán:** Framework (Spring, Hibernate, Jackson, JUnit) cần làm việc với class **mà nó không biết lúc được viết ra**: tạo object, đọc/ghi field, gọi method, biết method nào cần transaction. Dữ liệu cũng cần được chuyển thành byte để gửi qua mạng/lưu trữ.
>
> 💡 **Ý tưởng giải:** **Reflection** cho phép khảo sát và thao tác class lúc runtime; **annotation** là metadata khai báo ý định (`@Transactional`, `@Entity`) để framework đọc; **proxy/bytecode generation** cho phép chèn hành vi quanh method. Đánh đổi: chậm hơn, lỗi muộn (runtime), khó với native image → xu hướng mới chuyển dần sang xử lý **lúc compile** (annotation processor, Spring AOT).


#### 16.1. Java Serialization
- Class implement `Serializable` (marker interface) → `ObjectOutputStream` ghi toàn bộ object graph thành byte.
- **`serialVersionUID`**: định danh phiên bản class; không khai báo → JVM tự tính từ cấu trúc class → đổi 1 field là deserialize dữ liệu cũ bị `InvalidClassException`. Luôn khai báo tường minh.
- **`transient`**: field không được serialize (password, cache, connection). `static` field cũng không được serialize.
- Tùy biến: `writeObject/readObject`, `readResolve` (giữ singleton khi deserialize), `Externalizable`.
- ⚠️ **Bảo mật — Deserialization vulnerability**: deserialize dữ liệu không tin cậy có thể dẫn tới **Remote Code Execution** thông qua "gadget chain" (các lỗ hổng Apache Commons Collections 2015, nhiều CVE của Jackson polymorphic typing, Log4Shell cũng thuộc họ "dữ liệu bị diễn giải thành code"). Phòng chống: **không dùng Java native serialization** cho dữ liệu từ bên ngoài; dùng JSON/Protobuf/Avro; nếu buộc phải dùng thì cấu hình **`ObjectInputFilter`** (Java 9+, JEP 290) để whitelist class; với Jackson tránh `enableDefaultTyping`/`@JsonTypeInfo(use = Id.CLASS)` cho input không tin cậy.
- Thay thế phổ biến: **JSON** (Jackson, Gson), **Protobuf** (gRPC, nhỏ & nhanh, schema rõ), **Avro** (Kafka + Schema Registry), **Kryo** (nhanh, dùng trong Spark/Flink).
- **Record** serialize an toàn hơn: luôn đi qua canonical constructor khi deserialize (validation được chạy lại).

#### 16.2. Reflection
- Cho phép khảo sát & thao tác class tại runtime: `Class.forName`, `getDeclaredFields/Methods`, `Method.invoke`, `Field.setAccessible(true)`, `Constructor.newInstance`.
- **Ai dùng**: Spring (DI, tạo bean, đọc annotation), Hibernate (map entity), Jackson (serialize), JUnit/Mockito, các framework proxy.
- **Nhược điểm**: chậm hơn gọi trực tiếp (dù JIT tối ưu được nhiều — từ JDK 18 reflection core được cài lại bằng method handle, JEP 416), phá vỡ encapsulation, lỗi chỉ lộ ra lúc runtime, khó cho GraalVM native image (cần khai báo reflection config — Spring Boot AOT sinh tự động).
- **`MethodHandle` / `VarHandle`** (Java 7/9): API cấp thấp, nhanh hơn reflection, `VarHandle` cung cấp các thao tác atomic/memory ordering (thay thế `Unsafe`).
- **Dynamic Proxy** (`java.lang.reflect.Proxy`, chỉ cho interface) & **bytecode generation** (CGLIB, ByteBuddy — tạo subclass) → nền tảng của Spring AOP (xem 13.2 và `SpringBoot.md` mục 4).

#### 16.3. Annotations
- Annotation chỉ là **metadata** — tự nó không làm gì; phải có code (annotation processor lúc compile, hoặc framework dùng reflection lúc runtime) đọc và hành động.
- **`@Retention`**: `SOURCE` (chỉ trong source, vd `@Override`, Lombok annotations), `CLASS` (vào bytecode nhưng không đọc được bằng reflection — mặc định), `RUNTIME` (đọc được lúc runtime — `@Transactional`, `@Autowired`, `@Entity`).
- **`@Target`**: vị trí được gắn (TYPE, METHOD, FIELD, PARAMETER...). **`@Inherited`**: subclass thừa hưởng annotation của class cha (chỉ áp dụng cho class-level). **`@Repeatable`**.
- **Annotation Processing (compile-time)**: Lombok (sửa AST — kỹ thuật "hack" javac), MapStruct, Dagger, Micronaut/Quarkus (DI lúc compile → khởi động nhanh, không cần reflection).
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Timed {
    String value() default "";
}
// Đọc lúc runtime:
for (Method m : clazz.getDeclaredMethods()) {
    Timed t = m.getAnnotation(Timed.class);
    if (t != null) { /* bọc method bằng proxy đo thời gian... */ }
}
```

#### 16.4. JPMS — Java Platform Module System (Java 9)
- `module-info.java` khai báo `requires` (phụ thuộc), `exports` (package được lộ ra), `opens` (cho phép deep reflection — framework cần), `provides/uses` (ServiceLoader).
- Lợi ích: strong encapsulation, phát hiện thiếu dependency lúc khởi động, tạo runtime tối giản bằng **`jlink`** (image chỉ chứa module cần dùng → container nhỏ).
- Thực tế: đa số ứng dụng Spring Boot vẫn chạy trên **classpath** (unnamed module); JPMS chủ yếu được dùng trong chính JDK và thư viện.

---

### 17. Testing in Java

> 🎯 **Bài toán:** Thay đổi code mà không biết có làm hỏng chỗ khác không; test thủ công chậm và không lặp lại được.
>
> 💡 **Ý tưởng:** Test tự động theo **kim tự tháp**: nhiều unit test nhanh (cô lập bằng mock), ít hơn integration test với hạ tầng thật (Testcontainers), rất ít end-to-end. Code dễ test là code có dependency được **inject** (DIP) — kiểm thử tốt và thiết kế tốt đi cùng nhau.


```java
// ── JUnit 5 ──
@Test
@DisplayName("Should calculate total with discount")
void shouldCalculateTotal() {
    var calc = new PriceCalculator();
    assertEquals(90.0, calc.calculate(100.0, 0.10), 0.001);
}

@ParameterizedTest
@ValueSource(ints = {1, 2, 3, 5, 8, 13})
void shouldDetectFibonacci(int number) {
    assertTrue(FibChecker.isFibonacci(number));
}

@ParameterizedTest
@CsvSource({"1, 1, 2", "2, 3, 5", "10, 20, 30"})
void shouldAdd(int a, int b, int expected) {
    assertEquals(expected, calculator.add(a, b));
}

// ── Mockito ──
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrderRepository repo;
    @Mock PaymentService paymentService;
    @InjectMocks OrderService orderService;

    @Test
    void shouldCreateOrder() {
        // Given
        Order order = new Order("item1", 100.0);
        when(paymentService.charge(anyDouble())).thenReturn(true);
        when(repo.save(any(Order.class))).thenReturn(order);

        // When
        Order result = orderService.createOrder(order);

        // Then
        assertThat(result).isNotNull();
        verify(repo).save(order);
        verify(paymentService).charge(100.0);
        verifyNoMoreInteractions(repo, paymentService);
    }
}

// ── @Spy: partial mock ──
@Spy
List<String> spyList = new ArrayList<>();
// spyList.add("real") → gọi method THẬT
// ⚠️ Với spy, when(spyList.size()) sẽ GỌI method thật trước khi stub (có thể ném exception, vd spyList.get(0) trên list rỗng)
doReturn(100).when(spyList).size();   // ✅ cú pháp doReturn/when cho spy — không gọi method thật

// ── ArgumentCaptor: capture arguments ──
ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
verify(repo).save(captor.capture());
Order savedOrder = captor.getValue();
assertEquals("item1", savedOrder.getItem());
```

**Test pyramid & công cụ**:
- **Unit test** (nhiều nhất, nhanh): JUnit 5 + Mockito + **AssertJ** (`assertThat(list).hasSize(3).extracting(User::name).contains("An")`).
- **Integration test**: chạy với DB/Kafka/Redis thật bằng **Testcontainers** (Docker) thay vì H2 in-memory (H2 khác dialect/hành vi lock so với PostgreSQL/MySQL → test xanh nhưng production lỗi). Spring Boot 3.1+ có `@ServiceConnection` tự cấu hình kết nối (xem `SpringBoot.md`).
- **Contract test** (Spring Cloud Contract, Pact) cho giao tiếp giữa microservices; **E2E** ít nhất.
- **Mock đúng chỗ**: mock ở ranh giới (repository, HTTP client, clock), không mock value object hay chính class đang test. `@Mock` không cần Spring context → nhanh hơn nhiều so với `@SpringBootTest`.
- Đặt tên test theo hành vi: `shouldRejectOrder_whenBalanceInsufficient()`; cấu trúc **Given–When–Then / Arrange–Act–Assert**.
- Test concurrency: `CountDownLatch` để thả nhiều thread cùng lúc, lặp test nhiều lần; công cụ chuyên dụng: **jcstress** (OpenJDK) cho JMM. Benchmark hiệu năng dùng **JMH**, không dùng `System.currentTimeMillis()` trong vòng lặp (JIT warm-up, dead code elimination).

---

### 18. Common Java Interview Questions

#### Q1: HashMap vs ConcurrentHashMap vs Hashtable

| | HashMap | ConcurrentHashMap | Hashtable |
|---|---|---|---|
| Thread-safe | ❌ | ✅ (CAS + sync per bin) | ✅ (sync per method) |
| Null key/value | ✅ / ✅ | ❌ / ❌ | ❌ / ❌ |
| Performance | Nhanh nhất (single-thread) | Tốt (concurrent) | Chậm (toàn bộ lock) |
| Iterator | Fail-fast | Fail-safe (weakly consistent) | Fail-fast |
| Status | ✅ Dùng | ✅ Dùng | ❌ Legacy |

#### Q2: Tại sao String immutable?
1. String Pool sharing an toàn
2. Security (passwords, URLs)
3. Thread-safety miễn phí
4. hashCode caching → HashMap key hiệu quả
5. Class loading dùng String → immutable = đáng tin cậy

#### Q3: final vs finally vs finalize
- `final`: Variable không đổi, method không override, class không extend
- `finally`: Block luôn chạy sau try-catch (dùng cho cleanup)
- `finalize()`: Deprecated! GC gọi trước khi thu hồi object. Dùng try-with-resources thay thế

#### Q4: sleep() vs wait()

| | `Thread.sleep(ms)` | `object.wait()` |
|---|---|---|
| Lock | KHÔNG release lock | RELEASE lock |
| Gọi từ | Bất kỳ đâu | synchronized block |
| Wake up | Sau timeout | `notify()` / `notifyAll()` hoặc timeout |
| Class | Thread (static) | Object (instance) |

#### Q5: HashMap.put() hoạt động thế nào? (Step by step)
1. Tính `hash = hash(key)` = `key.hashCode() ^ (key.hashCode() >>> 16)`
2. Tính `index = hash & (table.length - 1)` → bucket index
3. Nếu bucket trống → tạo Node mới, đặt vào bucket
4. Nếu bucket có node:
   - Kiểm tra `hash == existingHash && (key == existingKey || key.equals(existingKey))`
   - Nếu match → thay value, return old value
   - Nếu node là TreeNode → Red-Black tree insert
   - Nếu linked list → duyệt đến cuối, append
     - Nếu chain ≥ 8 VÀ table ≥ 64 → treeify
5. `size++`, nếu `size > capacity × loadFactor` → resize (double capacity, rehash)

#### Q6: Memory leak trong Java — Các nguyên nhân phổ biến

```java
// 1. ThreadLocal không remove() trong thread pool
// 2. Static collections giữ reference mãi mãi
static List<Object> cache = new ArrayList<>(); // chỉ add, không bao giờ remove

// 3. Unclosed resources
Connection conn = getConnection(); // nếu không close → leak native resources

// 4. Inner class giữ reference đến outer class
public class Outer {
    byte[] data = new byte[1024 * 1024]; // 1MB
    class Inner { // non-static inner class giữ implicit reference đến Outer
        // → Outer không thể GC nếu Inner còn sống
    }
}
// Giải pháp: dùng static inner class

// 5. Listeners/Callbacks không unregister
// 6. String.intern() quá nhiều unique strings
// 7. Custom ClassLoader không unload properly
```

#### Q7: Volatile có đủ cho i++ không?
KHÔNG! `i++` là 3 operations: read → increment → write.
`volatile` chỉ đảm bảo visibility và ordering, KHÔNG đảm bảo atomicity.
→ Dùng `AtomicInteger.incrementAndGet()` hoặc `synchronized`

#### Q8: Khi nào dùng Comparable vs Comparator?
- **Comparable**: Natural ordering, implement trong class. Chỉ 1 cách sort.
  - `class Student implements Comparable<Student>` → `compareTo()`
- **Comparator**: External ordering, nhiều chiến lược sort khác nhau.
  - `Comparator.comparing(Student::getName).thenComparingInt(Student::getAge)`
- **Quy tắc**: Nếu class có 1 natural ordering hiển nhiên → dùng Comparable. Nếu cần nhiều cách sort → dùng Comparator.

#### Q9: `String s = new String("abc");` tạo ra mấy object?
Tối đa **2**: literal `"abc"` trong String Pool (nếu chưa tồn tại — được tạo khi class được load/literal được resolve) + 1 object `String` mới trên heap do `new`. Nếu `"abc"` đã có trong pool thì chỉ tạo 1.

#### Q10: Làm sao tạo một Immutable class? `final` có đủ không?
`final` trên biến chỉ cấm **gán lại reference**, không cấm sửa **nội dung** object. Immutable class cần: class `final` (hoặc constructor private + factory), mọi field `private final`, không có setter, **defensive copy** khi nhận/trả về object mutable (`List.copyOf(list)`, `new Date(d.getTime())`), không để `this` "thoát" khỏi constructor. Java 16+: dùng **record** (nhưng record vẫn chỉ shallow-immutable — field là `List` thì vẫn phải copy). Bonus: JMM đảm bảo field `final` được nhìn thấy đúng bởi mọi thread sau khi constructor kết thúc (final field semantics) → immutable object an toàn khi publish mà không cần đồng bộ.

#### Q11: `synchronized` vs `volatile` vs `Atomic*` vs `Lock` — chọn cái nào?
| | Visibility | Atomicity | Mutual exclusion | Dùng khi |
|---|---|---|---|---|
| `volatile` | ✅ | ❌ (chỉ đọc/ghi đơn lẻ) | ❌ | Cờ trạng thái, publish immutable object |
| `Atomic*` / `LongAdder` | ✅ | ✅ 1 biến (CAS) | ❌ | Counter, CAS trên 1 biến |
| `synchronized` | ✅ | ✅ | ✅ | Compound action trên nhiều biến, code đơn giản |
| `ReentrantLock` / `StampedLock` | ✅ | ✅ | ✅ | Cần tryLock, timeout, interruptible, nhiều Condition, fair lock, optimistic read |

#### Q12: `execute()` vs `submit()` của ExecutorService?
`execute(Runnable)` không trả kết quả; exception làm thread worker chết (được thay thread mới) và in ra qua `UncaughtExceptionHandler`. `submit()` trả `Future` — exception bị **gói trong Future** và **"im lặng"** cho tới khi gọi `future.get()` (ném `ExecutionException`) → bẫy thường gặp: submit rồi không bao giờ `get()` → lỗi biến mất. Luôn bắt/log exception bên trong task hoặc kiểm tra Future.

#### Q13: Minor GC vs Major GC vs Full GC? Khi nào Full GC xảy ra?
**Minor/Young GC**: dọn Young Gen, rất thường xuyên, nhanh (copying). **Major/Old GC**: dọn Old Gen (với G1 là *mixed GC* — young + một phần old). **Full GC**: dọn toàn bộ heap (+ metaspace), STW lâu. Nguyên nhân Full GC: Old Gen đầy/promotion failure, humongous allocation thất bại, metaspace đầy, gọi `System.gc()`, G1 "to-space exhausted"/evacuation failure. Full GC thường xuyên = dấu hiệu leak hoặc heap quá nhỏ.

#### Q14: `equals()` khi có kế thừa — dùng `getClass()` hay `instanceof`?
`instanceof` cho phép so sánh cha–con nhưng dễ phá **tính đối xứng** (`point.equals(colorPoint)` true nhưng ngược lại false). `getClass() != o.getClass()` giữ đối xứng nhưng vi phạm Liskov (subclass không bao giờ "bằng" cha). Khuyến nghị (Effective Java): ưu tiên **composition** thay vì kế thừa class có `equals` riêng; với class `final`/record thì không còn vấn đề.

#### Q15: Tại sao `wait()/notify()` nằm ở `Object` mà không ở `Thread`?
Vì chúng gắn với **monitor của object** (mọi object đều có thể làm lock), thread chờ trên *điều kiện của một object cụ thể* chứ không chờ một thread khác. Thread gọi `wait()` phải đang giữ monitor của chính object đó.

#### Q16: HashMap vs TreeMap vs LinkedHashMap — dùng khi nào?
HashMap: tra cứu nhanh nhất, không cần thứ tự. LinkedHashMap: cần giữ thứ tự insert (vd trả JSON đúng thứ tự) hoặc làm LRU cache (access order). TreeMap: cần key được sort, hoặc truy vấn kiểu `floorKey/ceilingKey/subMap` (vd tìm khung giá, lịch đặt chỗ).

#### Q17: Virtual Thread có thay thế được thread pool và reactive programming?
Thay thế được thread pool cho **tác vụ I/O-bound** (không pool virtual thread — tạo mới mỗi task). Không giúp gì cho CPU-bound (vẫn dùng pool cỡ số core). Với reactive: virtual thread cho phép viết code tuần tự dễ đọc/debug mà vẫn scale tương đương trong đa số trường hợp; reactive vẫn hữu ích cho streaming với backpressure. Lưu ý giới hạn tài nguyên downstream (DB pool) và pinning ở JDK 21–23 (11.1).

#### Q18: Làm thế nào để xử lý khi ứng dụng Java chậm dần rồi OOM sau vài ngày?
Dấu hiệu memory leak: theo dõi heap sau GC tăng dần → bật `-XX:+HeapDumpOnOutOfMemoryError` hoặc chụp heap dump (`jcmd GC.heap_dump`) → phân tích bằng Eclipse MAT (Dominator Tree, Leak Suspects, đường đi tới GC root) → nguyên nhân hay gặp: cache/static map không giới hạn, ThreadLocal không remove, listener không unregister, connection/stream không đóng (xem Q6 và 7.6). Fix bằng cache có giới hạn & TTL (Caffeine), `try-with-resources`, `remove()` trong finally.
