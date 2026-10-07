### 1. OOP Fundamentals

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

---

### 2. Java Type System

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
> Cache này áp dụng cho: `Byte`, `Short`, `Integer`, `Long`, `Character` (0-127).
> `Float` và `Double` KHÔNG có cache.

#### 2.3. String

**Immutability**: String objects không thể thay đổi sau khi tạo. Mọi operation tạo String mới.

```java
String s1 = "Hello";    // String pool (heap)
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
    PENDING("Chờ xử lý"),
    PROCESSING("Đang xử lý"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đã hủy");

    private final String description;
    OrderStatus(String description) { this.description = description; }
    public String getDescription() { return description; }

    // Enum có thể có abstract method
    public abstract boolean isFinal();
    // Mỗi constant phải implement
    COMPLETED("Hoàn thành") {
        @Override public boolean isFinal() { return true; }
    };
}

// Enum internals:
// - Mỗi enum constant là một SINGLETON instance
// - ordinal(): vị trí (0, 1, 2, ...)
// - valueOf("PENDING"): parse từ String
// - values(): array tất cả constants
// - Enum implement Comparable (theo ordinal)
// - Enum CÓ THỂ implement interface, KHÔNG THỂ extend class
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

---

### 3. Collections Framework

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

---

### 4. Generics

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

---

### 5. Exception Handling

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

---

### 6. Java 8 Features

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

---

### 7. JVM Internals

#### 7.1. ClassLoader Hierarchy

```
Bootstrap ClassLoader (native, C++)
    │ ← Load: java.lang.*, java.util.*, rt.jar (core Java)
    ▼
Platform/Extension ClassLoader (Java)
    │ ← Load: javax.*, extensions, jre/lib/ext
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

**Object Memory Layout** (64-bit JVM, Compressed Oops):

```
┌─────────────────────────────────────┐
│ Object Header (12 bytes)            │
│   ├── Mark Word (8 bytes)           │
│   │   ├── hashCode (25 bits)        │
│   │   ├── age (4 bits) → GC age     │
│   │   ├── biased lock info          │
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
* Compressed Oops: 4 bytes thay vì 8 bytes khi heap < 32GB
```

#### 7.3. JIT Compilation

- **Interpreted mode**: JVM đọc bytecode, interpret từng instruction → chậm
- **JIT (Just-In-Time)**: Khi method được gọi đủ nhiều (hot method) → compile thành native code
- **Tiered Compilation** (default từ Java 8):
  - Level 0: Interpreter
  - Level 1-3: **C1 Compiler** (Client) → compile nhanh, ít optimization
  - Level 4: **C2 Compiler** (Server) → compile chậm, aggressive optimization

**C2 Optimizations quan trọng**:
- **Inlining**: Thay method call bằng method body → giảm call overhead
- **Escape Analysis**: Kiểm tra object có "escape" khỏi method/thread không
  - Nếu không escape → **Stack allocation** (thay vì heap) → không cần GC
  - Nếu không escape thread → **Lock elimination** (loại bỏ synchronized)
  - **Scalar replacement**: Thay object bằng các field riêng lẻ trên stack
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
-XX:+UseZGC                   # ZGC (Java 15+ production)
-XX:+UseShenandoahGC          # Shenandoah

# GC tuning
-XX:MaxGCPauseMillis=200      # G1: target max pause time
-XX:NewRatio=2                # Old/Young ratio (Old = 2 × Young)
-XX:SurvivorRatio=8           # Eden/Survivor ratio

# Logging
-Xlog:gc*                     # GC logging (Java 9+ unified logging)
-XX:+HeapDumpOnOutOfMemoryError  # Heap dump khi OOM
```

---

### 8. Garbage Collection

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
| **ZGC** | Colored pointers, Load barriers | Concurrent | < 10ms | Ultra-low latency, large heap | Production J15+ |
| **Shenandoah** | Brooks pointers, concurrent compact | Concurrent | < 10ms | Ultra-low latency | OpenJDK |

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

**Generational ZGC (Java 21)**: Thêm generations (Young + Old) cho ZGC → GC Young Gen thường xuyên hơn, hiệu quả hơn.

**Reference Types và GC**:

| Type | Class | GC Behavior | Use Case |
|------|-------|-------------|----------|
| **Strong** | (default) | KHÔNG bị GC nếu reachable | Normal usage |
| **Soft** | `SoftReference<T>` | GC khi sắp OOM | Memory-sensitive cache |
| **Weak** | `WeakReference<T>` | GC ở next GC cycle | WeakHashMap, listeners |
| **Phantom** | `PhantomReference<T>` | Sau khi finalize, trước khi reclaim | Resource cleanup (thay finalize) |

---

### 9. Concurrency & Multithreading (CHI TIẾT)

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

Flow: No contention → Biased → Lightweight → Heavyweight
      (escalation chỉ đi 1 chiều, không downgrade)
```

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

---

### 10. Java 17 Features (LTS)

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
- **Strong encapsulation of JDK internals**: Không thể access internal APIs (sun.misc.Unsafe) → dùng `--add-opens` để workaround khi migrate
- **New RandomGenerator API**: Pluggable random number generators, algorithm-independent
- **Deprecations**: Applet API, Security Manager, RMI Activation

---

### 11. Java 21 Features (LTS)

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

**Pinning Problem — ⚠️ QUAN TRỌNG**:

```java
// Virtual thread bị "PIN" lên carrier thread (KHÔNG unmount) khi:
// 1. Bên trong synchronized block/method
synchronized (lock) {
    httpClient.send(request); // ❌ VT bị pin → carrier thread bị block!
}

// 2. Native method hoặc foreign function

// ✅ Giải pháp: dùng ReentrantLock thay synchronized
ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    httpClient.send(request); // ✅ VT unmount bình thường khi I/O block
} finally {
    lock.unlock();
}

// Detect pinning: -Djdk.tracePinnedThreads=full
```

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

#### 11.2. Structured Concurrency (Preview → Finalized Java 25)

```java
// Structured Concurrency: quản lý lifetime của concurrent subtasks
// Nếu 1 subtask fail → cancel tất cả subtasks khác

try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Subtask<User> userTask = scope.fork(() -> fetchUser(id));
    Subtask<List<Order>> ordersTask = scope.fork(() -> fetchOrders(id));

    scope.join();           // Chờ tất cả tasks hoàn thành
    scope.throwIfFailed();  // Throw nếu bất kỳ task nào fail
                            // → cancel tasks chưa xong

    // Lấy kết quả
    User user = userTask.get();
    List<Order> orders = ordersTask.get();
    return new UserProfile(user, orders);
}
// Scope close → tất cả subtasks PHẢI đã kết thúc
// → Không có "leaked threads"

// ShutdownOnSuccess: trả về kết quả task đầu tiên thành công
try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
    scope.fork(() -> fetchFromServer1());
    scope.fork(() -> fetchFromServer2());
    scope.fork(() -> fetchFromServer3());
    scope.join();
    String fastest = scope.result(); // task đầu tiên thành công
}
```

#### 11.3. Scoped Values (Preview → Finalized Java 25)

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

---

### 12. Java 25 Features (LTS — Latest)

Java 25 là LTS release mới nhất, finalize nhiều features đã preview trong Java 22-24:

#### 12.1. Structured Concurrency (Finalized)
- API ổn định cho `StructuredTaskScope` (đã cover ở mục 11.2)
- `Subtask` thay thế `Future` trong structured scope

#### 12.2. Scoped Values (Finalized)
- `ScopedValue` API ổn định (đã cover ở mục 11.3)

#### 12.3. Stream Gatherers

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
// ❌ Trước Java 25: KHÔNG thể có code trước super()
public class PositiveInteger {
    public PositiveInteger(int value) {
        // Không thể validate ở đây!
        super(value);
    }
}

// ✅ Java 25: statements trước super() OK
public class PositiveInteger extends Number {
    public PositiveInteger(int value) {
        if (value <= 0) throw new IllegalArgumentException("Must be positive");
        // Validation TRƯỚC super() call!
        super(value);
    }
}

// Hạn chế: KHÔNG thể access this (instance) trước super()
// Chỉ có thể access parameters và static members
```

#### 12.5. Primitive Types in Patterns

```java
// Pattern matching bây giờ hỗ trợ primitive types
switch (statusCode) {
    case 200 -> "OK";
    case 404 -> "Not Found";
    case int i when i >= 500 -> "Server Error: " + i;
    case int i -> "Unknown: " + i;
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

#### 12.7. Compact Source Launcher & Multi-file Programs

```bash
# Chạy Java file trực tiếp không cần compile (Java 11 cho single file, Java 25 cho multi-file)
java Main.java  # tự tìm và compile dependencies trong cùng directory

# Shebang support (Unix):
#!/usr/bin/java --source 25
public class Script {
    public static void main(String[] args) { ... }
}
```

#### 12.8. Foreign Function & Memory API (Finalized — Project Panama)

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

- **Security Manager**: Hoàn toàn removed (deprecated từ Java 17)
- **Vector API** (incubating): SIMD operations trong Java — `FloatVector`, `IntVector` cho parallel math
- **Class-File API**: Programmatic parsing và generation bytecode (thay ASM)
- **Compact Object Headers** (experimental): Object header từ 12 bytes → 8 bytes → giảm memory footprint

---

### 13. Design Patterns in Java

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
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        sendEmail(event.getOrder());
    }
}
```

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
    isolation = Isolation.READ_COMMITTED,  // default
    readOnly = false,
    timeout = 30,
    rollbackFor = Exception.class          // default chỉ rollback RuntimeException
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

> 📖 **Xem tài liệu chi tiết đầy đủ**: Toàn bộ kiến trúc chuyên sâu về Bean Lifecycle, Security JWT, JPA Internals, WebFlux, Kafka, Virtual Threads và câu hỏi phỏng vấn Spring Boot Senior xem tại [`SpringBoot.md`](file:///e:/Source/coding-interview-university/SpringBoot.md).

---

### 15. Testing in Java

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
// when(spyList.size()).thenReturn(100) → mock size()

// ── ArgumentCaptor: capture arguments ──
ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
verify(repo).save(captor.capture());
Order savedOrder = captor.getValue();
assertEquals("item1", savedOrder.getItem());
```

---

### 16. Common Java Interview Questions

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
