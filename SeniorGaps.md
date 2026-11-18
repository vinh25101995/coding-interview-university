# Từ Mid lên Senior Backend — Lấp lỗ hổng từ chính code của mình

Tài liệu này **không lặp lại** lý thuyết đã có trong các file khác. Nó được viết từ việc đọc lại code thật của mình ở GHTK (device-manager-be, gdms-be, sms-manager-be, tracker-manager-be, project-manage-be) và trả lời 3 câu hỏi:

1. Mình đã viết sai cái gì (bug / thiết kế yếu có thật)?
2. Kiến thức nào bị thiếu dẫn tới cái sai đó?
3. Cách làm đúng ở mức Senior là gì, và luyện thế nào?

> 🏋️ **Bài tập thực hành có tiêu chí đạt (test tự động):** [`SeniorGaps-Exercises.md`](SeniorGaps-Exercises.md) — 19 bài, mỗi bài tái hiện một bug thật rồi sửa.
>
> 💡 **Tài liệu liên quan** (đọc kèm, file này chỉ trỏ sang chứ không chép lại):
> - [`Java.md`](Java.md) — §5 Exception, §9 Concurrency, §11 Virtual Threads, §13 Design Patterns, §17 Testing
> - [`SpringBoot.md`](SpringBoot.md) — §8 @Transactional, §9 Security, §10 Kafka, §11 Async/Scheduling, §12 Observability, §16 HTTP Client & Resilience, §19 Testing
> - [`Database.md`](Database.md) — §8 B+Tree Index, §16 Transaction/MVCC (lost update), §20 MySQL InnoDB
> - [`SystemDesign.md`](SystemDesign.md) — §3 Message Queue, §5 Resiliency, §9 Observability, §11 Distributed Patterns
> - [`security-architecture-vi.md`](security-architecture-vi.md) — §4 AuthN/AuthZ, §15 Lưu ý Spring Boot

---

## 📖 Cách đọc tài liệu này

Giữ cùng mạch với các file khác, thêm một ký hiệu mới:

| Ký hiệu | Ý nghĩa |
| :--- | :--- |
| 🐞 **Bug thật** | Đoạn code mình từng viết, trích từ repo, kèm hậu quả |
| 🎯 **Bài toán** | Vấn đề thực tế cần giải |
| ❌ **Cách ngây thơ** | Cách "tự nhiên" và lý do nó sai |
| 💡 **Ý tưởng giải** | Nguyên lý cốt lõi |
| 🛠 **Áp dụng** | Code / checklist dùng ngay |
| 🏋️ **Bài tập** | Bài chi tiết trong [`SeniorGaps-Exercises.md`](SeniorGaps-Exercises.md) |
| 🧠 **Tự kiểm tra** | Câu hỏi "tại sao" (bấm để xem đáp án) |

### 🗺 Bản đồ: lỗi đã mắc → chương cần đọc

| # | Lỗi trong code của mình | Ở đâu | Chương |
|---|---|---|---|
| 1 | `catch (Exception e) { log.info(e.getMessage()); }`, `printStackTrace()` | ScaleService, CdrService | [1](#1-exception--logging-trong-production) |
| 2 | List `futures` không reset → `saveAll` lặp O(n²); task lỗi bị bỏ sót vĩnh viễn | CdrService.sendDataToAIService | [2](#2-concurrency-thực-chiến-trong-spring), [3](#3-batch--scheduled-job-đúng-cách) |
| 3 | `Executors.newFixedThreadPool` tự new trong service | CdrService, AsteriskEventHandlerService | [2](#2-concurrency-thực-chiến-trong-spring) |
| 4 | Việc nền trì hoãn 7s bằng `ScheduledExecutorService` in-memory | AsteriskEventHandlerService | [3](#3-batch--scheduled-job-đúng-cách) |
| 5 | Xoá file local **trước** khi cập nhật DB | uploadAudioFileToGS | [3](#3-batch--scheduled-job-đúng-cách) |
| 6 | Đọc → `+1` → `save()` (lost update) | ScaleService.updateScale | [4](#4-mongodb-thực-chiến-cho-java-dev) |
| 7 | `findByMac` trong vòng lặp (N+1) | updatePackageInfos, updateScale | [4](#4-mongodb-thực-chiến-cho-java-dev) |
| 8 | `save()` rồi `kafkaTemplate.send()` không key, bỏ qua kết quả | ScaleService.updateScaleInfo | [5](#5-nhất-quán-giữa-db-và-kafka) |
| 9 | Poll DB mỗi 5 phút để đoán thiết bị offline | scanDbToUpdateScaleCameraStatus | [6](#6-mqtt-cho-iot-backend) |
| 10 | Class 1640–2400 dòng, 22 dependency | ScaleService, IncidentService | [7](#7-refactor-god-class) |
| 11 | Xoá assertion để test pass; test chỉ có 1 trang dữ liệu | CdrServiceTest | [8](#8-testing-ở-mức-senior) |
| 12 | `new RestTemplate()` / `new ObjectMapper()` mỗi lần gọi, không timeout | gdms-be (~86 chỗ) | [9](#9-http-client--object-dùng-chung) |
| 13 | Interceptor 20 nhánh if/else; so sánh token bằng `equals`; `switch(null)` | AuthenticationInterceptor, TokenAuthorizationFilter | [10](#10-security-lỗi-hay-gặp-trong-code-thật) |
| 14 | Commit "update"/"fix", commit > 800 dòng | tất cả repo | [11](#11-kỹ-năng-senior-ngoài-code) |

---

## Mục lục

1. [Exception & Logging trong production](#1-exception--logging-trong-production)
2. [Concurrency thực chiến trong Spring](#2-concurrency-thực-chiến-trong-spring)
3. [Batch / Scheduled job đúng cách](#3-batch--scheduled-job-đúng-cách)
4. [MongoDB thực chiến cho Java dev](#4-mongodb-thực-chiến-cho-java-dev)
5. [Nhất quán giữa DB và Kafka](#5-nhất-quán-giữa-db-và-kafka)
6. [MQTT cho IoT backend](#6-mqtt-cho-iot-backend)
7. [Refactor God Class](#7-refactor-god-class)
8. [Testing ở mức Senior](#8-testing-ở-mức-senior)
9. [HTTP client & object dùng chung](#9-http-client--object-dùng-chung)
10. [Security: lỗi hay gặp trong code thật](#10-security-lỗi-hay-gặp-trong-code-thật)
11. [Kỹ năng Senior ngoài code](#11-kỹ-năng-senior-ngoài-code)
12. [Lộ trình 6 tháng & bài tập](#12-lộ-trình-6-tháng--bài-tập)
13. [Câu hỏi tự kiểm tra tổng hợp](#13-câu-hỏi-tự-kiểm-tra-tổng-hợp)

---

## 1. Exception & Logging trong production

> 🎯 **Bài toán:** 2 giờ sáng, cân ở kho báo lỗi. Thứ duy nhất có là log. Log phải trả lời được: *lỗi gì, ở đâu (stacktrace), với dữ liệu nào (id), thuộc request nào (traceId)*.
>
> ❌ **Cách ngây thơ:** bắt `Exception`, log `e.getMessage()` ở mức `info`, đi tiếp như không có gì.
>
> 💡 **Ý tưởng giải:** Exception chỉ có 3 số phận hợp lệ: **xử lý được** (fallback có chủ đích), **dịch** sang exception nghiệp vụ rồi ném tiếp, hoặc **để nó bay** tới boundary (controller advice / job runner) — nơi log **một lần** với đầy đủ stacktrace.

### 1.1. 🐞 Bug thật

```java
// ScaleService.updateAfterInsertLog
} catch (Exception e) {
    log.info("Scale Aspect exception : {}", e.getMessage());   // (1) info, (2) mất stacktrace, (3) không có mac/id
}

// ScaleService.getCameraNetworkInfo
log.error("CANNOT GET NETWORK INFO: {}, SN: {}", e, sn);       // (4) e bị "nuốt" vào placeholder → chỉ in e.toString()

// CdrService (bản đầu)
} catch (InterruptedException | ExecutionException e) {
    e.printStackTrace();                                        // (5) ra stderr, không qua logback; (6) mất cờ interrupt
}
```

| Lỗi | Hậu quả |
|---|---|
| `e.getMessage()` | Với `NullPointerException` (Java < 14) message là `null` → log ra `"exception : null"`, không biết dòng nào |
| Mức `info` | Alert theo `level=ERROR` không bắn; lỗi chìm giữa hàng nghìn dòng info |
| Throwable điền vào `{}` | SLF4J chỉ in stacktrace khi Throwable là **tham số cuối và không có placeholder tương ứng** |
| `printStackTrace()` | Không có timestamp, không có traceId, không vào ELK |
| Nuốt `InterruptedException` | Thread pool shutdown không dừng được task → graceful shutdown treo |

### 1.2. 🛠 Quy tắc

```java
// ✅ Throwable là tham số CUỐI, không có {} tương ứng → in đủ stacktrace
log.error("[SCALE][NETWORK] Cannot get network info, sn={}", sn, e);

// ✅ InterruptedException: khôi phục cờ rồi thoát
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    throw new IllegalStateException("Interrupted while waiting AI result, cdrId=" + id, e);
}

// ✅ Bắt hẹp, có chủ đích fallback, ghi rõ vì sao được phép bỏ qua
try {
    return dvrClient.checkCamera(sn);
} catch (ResourceAccessException e) {          // timeout / connection refused
    log.warn("[SCALE][NETWORK] DVR unreachable, sn={}, fallback UNKNOWN", sn, e);
    return NetworkInfo.unknown();
}
```

**Checklist khi viết `catch`:**

- [ ] Mình có **làm gì khác** ngoài log không? Nếu không → có lẽ không nên catch ở đây.
- [ ] Bắt loại hẹp nhất có thể (`DuplicateKeyException`, `ResourceAccessException`…), không phải `Exception`.
- [ ] Log **một lần**: hoặc log, hoặc ném tiếp — không làm cả hai (tránh 1 lỗi in 5 lần).
- [ ] Có **id nghiệp vụ** trong message (`mac`, `cdrId`, `stationId`).
- [ ] Throwable là tham số cuối.
- [ ] `InterruptedException` → `Thread.currentThread().interrupt()`.
- [ ] Trong vòng lặp batch: lỗi 1 phần tử **không** làm hỏng cả batch, nhưng phần tử đó phải được **đánh dấu** (xem §3).

### 1.3. Exception nghiệp vụ & boundary

```java
// Exception nghiệp vụ mang mã lỗi, không mang HTTP status
public class BizException extends RuntimeException {
    private final ErrorCode code;
    public BizException(ErrorCode code, String msg) { super(msg); this.code = code; }
}

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    ResponseEntity<ApiError> biz(BizException e) {
        return ResponseEntity.status(e.getCode().httpStatus()).body(ApiError.of(e));   // lỗi nghiệp vụ: không log error
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unknown(Exception e, HttpServletRequest req) {
        log.error("Unhandled error {} {}", req.getMethod(), req.getRequestURI(), e);    // log 1 lần, đủ stacktrace
        return ResponseEntity.status(500).body(ApiError.internal());
    }
}
```

`BizException` nên là `RuntimeException`: code của mình có rất nhiều `throws BizException, IOException` lan khắp các tầng — đó là dấu hiệu checked exception đang làm nhiễu chữ ký method.

### 1.4. MDC thay vì `request.setAttribute("rid")`

`AuthenticationInterceptor` sinh `rid` rồi `setAttribute` — nhưng log ở tầng service không tự có `rid`. Dùng **MDC** để mọi dòng log trong request đó tự mang id:

```java
@Component
class TraceIdFilter extends OncePerRequestFilter {
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String traceId = Optional.ofNullable(req.getHeader("X-Request-Id")).orElse(UUID.randomUUID().toString());
        MDC.put("traceId", traceId);
        res.setHeader("X-Request-Id", traceId);
        try { chain.doFilter(req, res); } finally { MDC.clear(); }   // bắt buộc clear: thread được tái sử dụng
    }
}
```

`logback.xml`: `%d %-5level [%X{traceId}] %logger{36} - %msg%n` (hoặc `LogstashEncoder` — gdms-be đã có `logstash-logback-encoder`, MDC tự thành field JSON).

> ⚠️ MDC là `ThreadLocal` → **không** tự chạy sang thread pool. Khi submit task, dùng `TaskDecorator` (xem §2.2). Với Micrometer Tracing / OpenTelemetry thì traceId được truyền tự động — xem `SpringBoot.md` §12.

<details>
<summary>🧠 Vì sao "log rồi ném tiếp" là anti-pattern?</summary>

Mỗi tầng log lại cùng một lỗi → một sự cố in ra 3–5 stacktrace giống nhau, khó đếm số lỗi thật, alert bị nhân bản. Quy tắc: tầng nào **xử lý** thì tầng đó log; tầng trung gian chỉ bọc thêm ngữ cảnh (`new XException("…cdrId=" + id, e)`) rồi ném.
</details>

---

## 2. Concurrency thực chiến trong Spring

> 🎯 **Bài toán:** Gửi 5000 bản ghi sang AI service, mỗi request ~300ms. Tuần tự mất 25 phút; muốn song song nhưng (a) không làm sập AI service, (b) không mất bản ghi khi có lỗi, (c) tắt app không bỏ dở giữa chừng.
>
> ❌ **Cách ngây thơ:** `Executors.newFixedThreadPool(5)` làm field, `submit` trong vòng lặp, gom `Future` vào một list.
>
> 💡 **Ý tưởng giải:** Thread pool là **tài nguyên hạ tầng** → khai báo bean (có tên, có giới hạn, có metrics, có shutdown). Mỗi đơn vị công việc trả về **kết quả có trạng thái cuối** (thành công *hoặc* thất bại) — không để exception "bay mất" trong Future.

Lý thuyết nền (JMM, `volatile`, `synchronized`, `ExecutorService`, `CompletableFuture`): đọc `Java.md` §9 và §11. Phần dưới chỉ nói cách áp dụng trong Spring.

### 2.1. 🐞 Bug thật — `CdrService.sendDataToAIService`

```java
ExecutorService executorService = Executors.newFixedThreadPool(5);        // (A) không phải bean

List<Future<CdrVoIPAsterisk>> futures = new ArrayList<>();                 // (B) tạo NGOÀI while
while (!toDoList.isEmpty() && totalRecord < totalLimit) {
    for (cdr : toDoList) futures.add(executorService.submit(() -> callAi(cdr)));
    for (f : futures) updatedList.add(f.get());                            // (C) duyệt cả future vòng trước
    saveAll(updatedList);
    fromId = last(toDoList).getId();                                       // (D) dời mốc kể cả khi task lỗi
}
```

| | Hậu quả |
|---|---|
| (B)+(C) | Vòng k save lại k×500 bản ghi → 27.500 lượt ghi thay vì 5.000; lỗi cũ bị log lặp mỗi vòng |
| (D) | Task ném exception → bản ghi không được save, `aiAnalystStatus` vẫn `null`, mốc đã qua → **không bao giờ được xử lý lại** |
| (A) | Queue không giới hạn, tên thread `pool-3-thread-2`, không metrics, không MDC |

Unit test không bắt được vì test chỉ mock **một trang** dữ liệu → vòng `while` chỉ chạy một lần.

### 2.2. 🛠 Executor là bean

```java
@Configuration
@EnableAsync
class ExecutorConfig {

    @Bean("aiRequestExecutor")
    ThreadPoolTaskExecutor aiRequestExecutor() {
        var ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(5);                 // = số request đồng thời tối đa AI service chịu được
        ex.setMaxPoolSize(5);                  // core = max: tránh bẫy "max chỉ có tác dụng khi queue đầy"
        ex.setQueueCapacity(1000);             // có giới hạn
        ex.setThreadNamePrefix("ai-req-");
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // backpressure: thread job tự chạy
        ex.setTaskDecorator(mdcPropagating());  // truyền traceId sang thread con
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(30);
        ex.initialize();
        return ex;
    }

    static TaskDecorator mdcPropagating() {
        return runnable -> {
            Map<String, String> ctx = MDC.getCopyOfContextMap();
            return () -> {
                if (ctx != null) MDC.setContextMap(ctx);
                try { runnable.run(); } finally { MDC.clear(); }
            };
        };
    }
}
```

Lợi ích: cấu hình qua properties, Spring tự shutdown, Spring Boot (2.6+) tự gắn metrics `executor.active`, `executor.queued`… cho Prometheus, test thay bằng `SyncTaskExecutor` dễ dàng.

**Chọn kích thước pool:**

| Loại việc | Kích thước |
|---|---|
| CPU-bound (nén ảnh, tạo PDF Jasper) | ≈ số core |
| I/O-bound gọi service ngoài | = **giới hạn đồng thời của bên kia** (rate limit), không phải "càng nhiều càng nhanh" |
| Java 21 | Virtual thread cho I/O + `Semaphore` để giới hạn đồng thời (xem `Java.md` §11) |

### 2.3. 🛠 Viết lại đúng

```java
public void sendDataToAIService() {
    long fromId = checkpoint.get("LATEST_AI_ANALYZE_CDR_ID");
    long toId   = checkpoint.get("LATEST_UPLOAD_CDR_ID");
    int processed = 0;

    List<Cdr> page = repo.findPage(fromId, toId, Limit.of(500));
    while (!page.isEmpty() && processed < 5000) {
        List<CompletableFuture<Cdr>> futures = page.stream()          // ✅ tạo mới mỗi vòng
                .filter(Cdr::needsAiAnalysis)
                .map(c -> CompletableFuture.supplyAsync(() -> requestAi(c), aiRequestExecutor))
                .toList();

        List<Cdr> done = futures.stream().map(CompletableFuture::join).toList();   // join không ném vì requestAi không ném
        repo.saveAll(done);

        processed += page.size();
        fromId = page.get(page.size() - 1).getId();
        checkpoint.set("LATEST_AI_ANALYZE_CDR_ID", fromId);          // ✅ lưu mốc sau MỖI trang đã có trạng thái cuối
        page = repo.findPage(fromId, toId, Limit.of(500));
    }
}

private Cdr requestAi(Cdr cdr) {                                      // ✅ không bao giờ ném: luôn trả trạng thái cuối
    try {
        boolean ok = aiClient.request(toRequest(cdr)).isSuccess();
        cdr.markAi(ok ? REQUEST_SUCCESS : REQUEST_FAIL);
    } catch (Exception e) {
        log.error("[CDR][AI] request failed, cdrId={}", cdr.getId(), e);
        cdr.markAi(REQUEST_FAIL);
    }
    return cdr;
}
```

Điểm mấu chốt: **đơn vị công việc tự đóng gói lỗi của nó** → tầng điều phối chỉ việc gom kết quả. Bản ghi `REQUEST_FAIL` được một job retry riêng xử lý (§3.3).

### 2.4. Các bẫy concurrency khác hay gặp trong Spring

| Bẫy | Ví dụ | Sửa |
|---|---|---|
| Biến tích lũy đặt sai phạm vi | `futures` ngoài `while` | Khai báo ở phạm vi nhỏ nhất; test nhiều vòng |
| Bean singleton có field mutable | `private List<X> cache = new ArrayList<>()` trong `@Service` | Không giữ state; nếu cần thì `ConcurrentHashMap`/Caffeine |
| Lambda bắt object rồi sửa từ nhiều thread | Hai task cùng sửa một `Scale` | Mỗi task một object; hoặc trả kết quả mới thay vì sửa |
| `SimpleDateFormat` dùng chung | `ScaleService` có `new SimpleDateFormat(...)` | `DateTimeFormatter` (immutable, thread-safe) |
| `@Async` gọi nội bộ | `this.asyncMethod()` | Proxy không chặn self-invocation — xem `SpringBoot.md` §4.3 |
| Read-modify-write trên DB | `get → +1 → save` | Atomic update ở DB (§4.2) — **lock trong JVM không cứu được khi chạy nhiều pod** |
| `parallelStream()` trong web request | Dùng chung `ForkJoinPool.commonPool()` | Executor riêng |

<details>
<summary>🧠 Vì sao <code>synchronized</code> trên method <code>updateScale</code> không sửa được lost update?</summary>

`synchronized` chỉ khoá trong **một JVM**. device-manager-be chạy nhiều pod → hai pod vẫn cùng đọc `totalPckWeighed = 100`, cùng ghi `101`. Phải để DB làm phép cộng nguyên tử (`$inc`) hoặc dùng optimistic lock (`@Version`).
</details>

<details>
<summary>🧠 Vì sao <code>CallerRunsPolicy</code> là backpressure?</summary>

Khi queue đầy, task được chạy ngay trên thread đang submit (thread của job). Thread đó bận → tự nhiên ngừng submit thêm cho đến khi chạy xong → tốc độ đẩy việc tự hạ xuống bằng tốc độ xử lý, không OOM, không vứt task như `AbortPolicy`/`DiscardPolicy`.
</details>

---

## 3. Batch / Scheduled job đúng cách

> 🎯 **Bài toán:** Rất nhiều nghiệp vụ IoT là job nền: quét cân offline, upload file ghi âm lên GS, gửi AI, dọn log, tạo báo cáo ngày. Job phải: không chạy trùng giữa các pod, không mất việc khi pod restart, không bỏ sót bản ghi lỗi, chạy lại an toàn.
>
> 💡 **Ý tưởng giải:** Trạng thái tiến độ phải nằm **trong DB** (không trong RAM), mỗi bản ghi có **trạng thái** rõ ràng, và mỗi bước phải **idempotent** (chạy 2 lần cho cùng kết quả).

ShedLock, `fixedDelay` vs `fixedRate`, `@Async`: xem `SpringBoot.md` §11. Phần này tập trung vào **thiết kế** job.

### 3.1. Hai cách theo dõi tiến độ

| | **Cursor / checkpoint** (`fromId` trong config) | **Status-driven** (`WHERE status = PENDING`) |
|---|---|---|
| Ý tưởng | Lưu "đã xử lý đến id X" | Mỗi bản ghi tự mang trạng thái |
| Ưu | Query rẻ, không cần index trạng thái | **Tự chữa lành**: bản ghi lỗi vẫn PENDING/FAILED → lần sau lấy lại |
| Nhược | Dời mốc qua bản ghi lỗi = mất bản ghi | Cần index `(status, id)`; cần chống 2 pod lấy trùng |
| Dùng khi | Dòng dữ liệu append-only, xử lý không thể lỗi từng bản ghi | Mỗi bản ghi có thể lỗi riêng (gọi API ngoài) — **hầu hết trường hợp của mình** |

**Quy tắc vàng của cursor:** *chỉ dời mốc qua những bản ghi đã có trạng thái cuối cùng (kể cả FAILED).* Kafka cũng y hệt: chỉ commit offset sau khi xử lý xong message.

### 3.2. Claim pattern — nhiều pod cùng làm không trùng

ShedLock chỉ cho **một pod** chạy job → không scale. Muốn nhiều pod cùng xử lý, mỗi pod "giành" (claim) một lô:

```sql
-- MySQL 8 / PostgreSQL
START TRANSACTION;
SELECT id FROM cdr
 WHERE ai_status = 'PENDING' AND next_retry_at <= NOW()
 ORDER BY id LIMIT 100
 FOR UPDATE SKIP LOCKED;          -- pod khác bỏ qua các dòng đang bị khoá
UPDATE cdr SET ai_status = 'PROCESSING', locked_by = :podId, lease_until = NOW() + INTERVAL 5 MINUTE
 WHERE id IN (:ids);
COMMIT;
```

```java
// MongoDB: claim từng document, nguyên tử
Cdr claimed = mongoTemplate.findAndModify(
    query(where("aiStatus").is(PENDING).and("nextRetryAt").lte(now)).with(Sort.by("_id")),
    new Update().set("aiStatus", PROCESSING).set("lockedBy", podId).set("leaseUntil", now.plus(5, MINUTES)),
    FindAndModifyOptions.options().returnNew(true),
    Cdr.class);
```

`lease_until` để xử lý pod chết giữa chừng: một job "reaper" trả các bản ghi `PROCESSING` quá hạn về `PENDING`.

### 3.3. Retry có kiểm soát

```
PENDING ──► PROCESSING ──► SUCCESS
               │
               └─ lỗi ─► FAILED (attempts+1, next_retry_at = now + backoff(attempts))
                              │
                              └─ attempts ≥ 5 ─► DEAD (alert, cần người xem)
```

`backoff(n) = min(base × 2ⁿ, max) + jitter` — tránh mọi bản ghi lỗi cùng retry một lúc làm sập lại service ngoài.

### 3.4. 🐞 Bug thật — việc nền in-memory & thứ tự bước

```java
// AsteriskEventHandlerService.handleHangUpEvent
executor.schedule(() -> {
    uploadAudioFileToGS(callId);     // trong đó: upload → XOÁ FILE LOCAL → cập nhật URL vào DB
    sendReportToBC101(callId);
    ...
}, 7, TimeUnit.SECONDS);
```

1. **Mất việc khi restart:** deploy đúng lúc có cuộc gọi vừa kết thúc → task trong RAM biến mất, file không bao giờ được upload, BC101 không nhận báo cáo.
2. **Thứ tự bước sai:** nếu crash sau khi xoá file nhưng trước khi lưu URL → file mất ở cả local lẫn DB.
3. **`Thread.sleep(2000)` trong pool 4 thread:** chờ CDR xuất hiện bằng cách ngủ → giữ thread, nhiều cuộc gọi cùng kết thúc là pool nghẽn.

**Sửa:**

- Ghi một bản ghi `post_call_task(callId, step, status, run_at = now + 7s)` vào DB ngay trong event handler → job (claim pattern) xử lý. Restart không mất.
- Thứ tự an toàn cho mọi pipeline "di chuyển dữ liệu": **copy → ghi nhận đích vào DB → xoá nguồn**. Mỗi bước idempotent: upload lại cùng tên object không sao; xoá file không tồn tại coi như thành công.
- Không `sleep` chờ dữ liệu: nếu CDR chưa có → trả task về `PENDING` với `run_at = now + 2s`.

### 3.5. Phân trang trong job

| Cách | Vấn đề |
|---|---|
| `PageRequest.of(page, 500)` (offset/skip) | Dữ liệu thay đổi khi đang quét → nhảy cóc / trùng; `skip` lớn rất chậm |
| Keyset: `WHERE id > :lastId ORDER BY id LIMIT 500` | Ổn định, dùng index, tốc độ không đổi theo độ sâu |

CdrService đang **trộn cả hai** (`findByIdGreaterThan…(fromId, PageRequest.of(from, limit))`) — chạy đúng chỉ vì `from` luôn = 0. Với Spring Data 3.2+ dùng `Limit.of(500)` cho rõ ý.

### 3.6. Checklist thiết kế một job

- [ ] Chạy trên nhiều pod: ShedLock (1 pod) hay claim (nhiều pod)?
- [ ] `lockAtMostFor` > thời gian chạy lâu nhất của job.
- [ ] Tiến độ lưu trong DB, không trong RAM.
- [ ] Bản ghi lỗi có trạng thái + số lần thử + thời điểm thử lại.
- [ ] Mỗi bước idempotent; thứ tự copy → ghi nhận → xoá.
- [ ] Giới hạn khối lượng mỗi lần chạy (để job không chạy 2 tiếng).
- [ ] Log bắt đầu/kết thúc kèm số lượng thành công/thất bại; metric `job_records_total{result}`.
- [ ] Có cờ bật/tắt (mình đã có `JOB_REQUEST_AI_ENABLE` — tốt).
- [ ] Logic dùng `Clock` inject được, không gọi `ZonedDateTime.now()` trực tiếp (để test được, §8.5).

<details>
<summary>🧠 Job dọn log cũ mỗi đêm (<code>update job delete log</code>) có cách nào tốt hơn?</summary>

MongoDB **TTL index**: `db.scale_log.createIndex({createdAt: 1}, {expireAfterSeconds: 7776000})` — server tự xoá document quá 90 ngày, không cần job, không cần ShedLock. Với MySQL bảng lớn: partition theo ngày/tháng rồi `DROP PARTITION` thay vì `DELETE` hàng triệu dòng.
</details>

---

## 4. MongoDB thực chiến cho Java dev

> 🎯 **Bài toán:** Phần lớn dữ liệu IoT của mình nằm trên MongoDB (scale, scale_log, cabinet, incident_ticket…). `Database.md` đi sâu vào storage engine, MVCC, B+Tree — nhưng gần như **không có phần MongoDB ứng dụng**: update nguyên tử, bulk, index, transaction. Đây là lỗ hổng gây ra phần lớn bug dữ liệu trong code của mình.
>
> 💡 **Ý tưởng giải:** MongoDB đảm bảo **nguyên tử ở mức một document**. Thiết kế sao cho mỗi thao tác nghiệp vụ là *một lệnh update lên một document*, để DB làm phép tính thay vì Java.

### 4.1. 🐞 Bug thật — read-modify-write

```java
// ScaleService.updateScale — gọi cho MỖI log cân
Scale scale = scaleRepository.findByMac(mac);                       // đọc
scale.setTotalPckWeighed(scale.getTotalPckWeighed() + 1);           // tính trong Java
scale.setTotalPckMass(weight.add(scale.getTotalPckMass()));
scaleRepository.save(scale);                                        // ghi đè CẢ document
```

```
Pod A: đọc total=100                    Pod B: đọc total=100
Pod A: ghi total=101                    Pod B: ghi total=101     ← mất 1 lượt cân
```

Tệ hơn: `save()` ghi đè **toàn bộ** document. Nếu cùng lúc admin sửa `name` qua `updateScaleInfo`, một trong hai thay đổi bị ghi đè bằng bản cũ.

Ngoài ra nếu `scale == null` thì code `new Scale()` rồi `save` → hai log đầu tiên của một cân mới đến đồng thời sẽ tạo **hai document trùng mac** (nếu không có unique index).

### 4.2. 🛠 Atomic update

```java
// ✅ Một lệnh, DB tự cộng, chỉ đụng các field cần đổi, tự tạo nếu chưa có
Update u = new Update()
        .inc("total_pck_weighed", 1)
        .inc("total_pck_mass", weight)                         // Decimal128 nếu field là BigDecimal
        .set("last_use_date", log.getScannerTime())
        .set("last_use_user_id", log.getCreatedUserId())
        .set("status", ScaleStatus.USING)
        .setOnInsert("start_use_date", log.getScannerTime())   // chỉ set khi upsert tạo mới
        .setOnInsert("station_id", log.getStationId());

mongoTemplate.upsert(query(where("mac").is(mac)), u, Scale.class);
```

```javascript
// Bắt buộc có unique index — nếu không, upsert đồng thời vẫn có thể tạo 2 document
db.scale.createIndex({ mac: 1 }, { unique: true })
```

Với unique index, hai upsert đồng thời: một cái thành công, cái kia nhận `DuplicateKeyException` → **retry một lần** là thành update.

| Toán tử | Dùng khi |
|---|---|
| `$set` / `$unset` | Đổi field cụ thể (không ghi đè cả document) |
| `$inc` | Bộ đếm, tổng |
| `$min` / `$max` | Lưu thời điểm sớm nhất / muộn nhất (`last_online_date` = `$max`) |
| `$push` + `$slice` | Giữ N phần tử gần nhất trong mảng (tránh mảng phình vô hạn) |
| `$addToSet` | Thêm vào tập không trùng |
| `$setOnInsert` | Giá trị khởi tạo khi upsert |
| `findAndModify` | Cần **giá trị trước/sau** khi update (claim job, sinh số thứ tự) |

### 4.3. Optimistic locking khi bắt buộc phải đọc rồi ghi

Khi logic quá phức tạp để viết thành một lệnh update (ví dụ `updateScaleInfo` với nhiều validate), dùng `@Version`:

```java
@Document("scale")
class Scale {
    @Id String id;
    @Version Long version;     // Spring Data tự thêm điều kiện version = X khi save, tự tăng
    ...
}

@Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
public void updateScaleInfo(UpdateScaleCmd cmd) {
    Scale s = repo.findById(cmd.id()).orElseThrow();
    s.applyAdminChange(cmd);           // validate + đổi state
    repo.save(s);                      // ném OptimisticLockingFailureException nếu ai đó đã ghi trước
}
```

| | Pessimistic (khoá) | Optimistic (`@Version`) | Atomic update |
|---|---|---|---|
| Xung đột ít | Lãng phí | ✅ | ✅ |
| Xung đột nhiều (bộ đếm nóng) | Chậm | ❌ retry liên tục | ✅ tốt nhất |
| Logic phức tạp | OK | ✅ | Khó viết |

### 4.4. N+1 và bulk

```java
// 🐞 updatePackageInfos: mỗi log = 1 findByMac + 1–2 query package + 1 update  → 500 log = ~2000 round-trip
for (ScaleLog l : logs) {
    Scale scale = scaleRepository.findByMac(l.getScaleMac());
    ...
}
```

```java
// ✅ 1 query lấy hết, map trong RAM, 1 lệnh bulk ghi
Set<String> macs = logs.stream().map(ScaleLog::getScaleMac).collect(toSet());
Map<String, Scale> byMac = scaleRepository.findByMacIn(macs).stream()
        .collect(toMap(Scale::getMac, identity()));

BulkOperations bulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, ScaleLog.class);
for (ScaleLog l : logs) {
    Scale s = byMac.get(l.getScaleMac());
    if (s == null) continue;
    bulk.updateOne(query(where("_id").is(l.getId())), new Update().set("package_flag", flagFor(l, s)));
}
bulk.execute();                       // 1 round-trip cho cả lô
```

Quy tắc nhẩm: **mỗi round-trip ~1 ms trong cùng DC**. Vòng lặp 500 phần tử × 4 query = 2 giây chỉ để chờ mạng.

### 4.5. Index — đọc `explain` thay vì đoán

```javascript
db.scale_log.find({ scale_mac: "AA:BB", scanner_time: { $gte: ISODate("2024-01-01") } })
            .sort({ scanner_time: -1 })
            .explain("executionStats")
```

| Chỉ số | Ý nghĩa | Tốt |
|---|---|---|
| `stage` | `IXSCAN` (dùng index) / `COLLSCAN` (quét cả collection) | IXSCAN |
| `totalDocsExamined` / `nReturned` | Đọc bao nhiêu để trả bấy nhiêu | ≈ 1 |
| `SORT` stage trong plan | Sort trong RAM (giới hạn 100MB) | Không có |

**Quy tắc ESR** khi tạo compound index: **E**quality → **S**ort → **R**ange.

```javascript
// query: scale_mac = ? (E), sort scanner_time (S), scanner_time >= ? (R — cùng field với S)
db.scale_log.createIndex({ scale_mac: 1, scanner_time: -1 })
```

Khai báo trong code bằng `@CompoundIndex`, nhưng ở production nên tắt `spring.data.mongodb.auto-index-creation` và tạo index bằng migration (Mongock / script) — tạo index trên collection lớn tốn tài nguyên, không nên xảy ra ngầm lúc app khởi động.

### 4.6. Thiết kế schema

| Câu hỏi | Embed | Reference |
|---|---|---|
| Luôn đọc cùng nhau? | ✅ | |
| Quan hệ 1–ít (scale ↔ scale_camera) | ✅ | |
| Quan hệ 1–rất nhiều / tăng vô hạn (scale ↔ scale_log) | | ✅ |
| Cần cập nhật nguyên tử cùng nhau | ✅ (1 document = 1 transaction miễn phí) | |
| Dữ liệu dùng chung nhiều nơi (station) | | ✅ |

Giới hạn: document ≤ 16 MB; mảng không được phép tăng vô hạn (log, lịch sử) — dùng collection riêng hoặc `$push` + `$slice`.

### 4.7. Transaction trong MongoDB

- Chỉ có trên **replica set / sharded cluster** (standalone không có).
- Bật bằng `MongoTransactionManager` bean → `@Transactional` hoạt động.
- Đắt hơn nhiều so với update một document; giới hạn thời gian mặc định 60s.
- **Ưu tiên thiết kế để không cần transaction** (embed, atomic update). Dùng transaction cho trường hợp thật sự phải ghi nhiều document cùng thành/bại — ví dụ ghi document + outbox event (§5).
- Lưu ý: code đang dùng **cả MySQL và Mongo** trong cùng service → không có transaction xuyên hai DB; cần outbox hoặc saga.

### 4.8. Thời gian

- Lưu **UTC** (`Instant`/`Date`); chỉ chuyển múi giờ ở tầng hiển thị/báo cáo.
- Code đang có converter `ZonedDateTime` ↔ `Date` — nhớ rằng Mongo **không lưu zone**, đọc lên sẽ là zone mặc định của converter.
- Báo cáo "theo ngày" phải chốt ngày theo `Asia/Ho_Chi_Minh`, không theo UTC, nếu không số liệu 0h–7h bị đẩy sang ngày hôm trước.

<details>
<summary>🧠 <code>findByMac</code> trả về null khi có 2 document cùng mac?</summary>

Spring Data với method trả về một entity mà query ra >1 kết quả sẽ ném `IncorrectResultSizeDataAccessException` → 500. Dữ liệu trùng do upsert đồng thời không có unique index thường lộ ra theo cách này, rất lâu sau khi bug thật xảy ra.
</details>

<details>
<summary>🧠 Vì sao <code>$inc</code> an toàn khi nhiều pod cùng ghi mà <code>get → +1 → save</code> thì không?</summary>

`$inc` được thực thi **bên trong** storage engine, dưới khoá cấp document của WiredTiger: đọc giá trị hiện tại và ghi giá trị mới là một thao tác không thể bị chen ngang. `get → save` tách thành hai round-trip; giữa hai lần đó pod khác có thể ghi.
</details>

---

## 5. Nhất quán giữa DB và Kafka

> 🎯 **Bài toán:** Cân đổi trạng thái → lưu DB **và** bắn event `DEVICE_ACTIVITY_LOGS` cho hệ thống khác. Hai hệ thống, không có transaction chung.
>
> 💡 **Ý tưởng giải:** Không bao giờ "ghi hai nơi" trong cùng một luồng code (dual write). Ghi **một nơi** (DB, kèm event trong cùng transaction — **outbox**), rồi để một tiến trình riêng chuyển event sang Kafka với cơ chế at-least-once; consumer chịu được trùng lặp (**idempotent**).

Lý thuyết outbox, CDC, saga, idempotency key: `SystemDesign.md` §3, §11; cấu hình Spring Kafka, DLT: `SpringBoot.md` §10. Phần này soi vào code của mình.

### 5.1. 🐞 Bug thật — ba lỗi trong 10 dòng

```java
// ScaleService.updateScaleInfo
scaleRepository.save(scale);                                              // (1) dual write
...
scaleKafkaTemplate.send(Const.DEVICE_ACTIVITY_LOGS_TOPIC,                 // (2) không có key
                        mapper.writeValueAsString(message));              // (3) bỏ qua kết quả send (async)
log.info("{}, scale change status!", scale.getMac());                     //     log "thành công" khi chưa biết
```

| Lỗi | Kịch bản hỏng |
|---|---|
| (1) Dual write | Save OK → pod bị kill trước khi send → hệ thống khác không bao giờ biết cân đổi trạng thái. Hoặc send OK nhưng save lỗi → event về một thay đổi không tồn tại |
| (2) Không key | Kafka rải message vào partition bất kỳ → `USING → NOT_USING → USING` của **cùng một cân** có thể đến consumer sai thứ tự → trạng thái cuối sai |
| (3) Bỏ qua kết quả | `send()` là async: lỗi broker / timeout / message quá lớn xảy ra **sau** khi hàm đã return → `try/catch` bao quanh không bắt được gì |

Thêm nữa: device-manager-be chạy Spring Boot 2.4 → kafka-clients 2.6 → mặc định `acks=1` và **chưa** bật idempotent producer (từ kafka-clients 3.0 mới mặc định `acks=all` + `enable.idempotence=true`).

### 5.2. 🛠 Tối thiểu phải làm (không cần outbox)

```java
// Key = id thiết bị → mọi event của một cân vào cùng partition → giữ thứ tự
scaleKafkaTemplate.send(topic, scale.getId(), payload)
    .addCallback(                                                         // spring-kafka 2.x: ListenableFuture
        ok  -> log.debug("sent offset={}", ok.getRecordMetadata().offset()),
        err -> log.error("[SCALE][KAFKA] send failed, scaleId={}", scale.getId(), err));
// spring-kafka 3.x: .whenComplete((res, err) -> ...) vì send trả CompletableFuture
```

```properties
spring.kafka.producer.acks=all
spring.kafka.producer.properties.enable.idempotence=true
spring.kafka.producer.properties.delivery.timeout.ms=120000
```

### 5.3. 🛠 Outbox (khi event là nghiệp vụ quan trọng)

```
 ┌───────────── 1 transaction ─────────────┐
 │ update scale set status = ...           │
 │ insert outbox(id, aggregate_id, type,   │        ┌──────────────┐        ┌───────┐
 │               payload, created_at)      │ ─────► │ Relay (poll  │ ─────► │ Kafka │
 └─────────────────────────────────────────┘        │ hoặc CDC)    │        └───────┘
                                                    └──────────────┘
```

- **MySQL:** cùng `@Transactional` JPA. Relay: job poll `outbox WHERE sent_at IS NULL ORDER BY id` (claim pattern §3.2) hoặc **Debezium** đọc binlog.
- **MongoDB:** `MongoTransactionManager` + collection `outbox`; hoặc embed `pendingEvents: [...]` ngay trong document scale (không cần transaction) rồi relay `$pull` sau khi gửi; hoặc **Change Streams**.
- Relay gửi xong mới đánh dấu `sent_at` → có thể gửi trùng → consumer phải idempotent.

### 5.4. Consumer idempotent

```java
@KafkaListener(topics = "scale-log", groupId = "device-manager")
public void onScaleLog(ScaleLogEvent e) {
    // Cách 1: thao tác tự nhiên idempotent — upsert theo khoá nghiệp vụ
    mongoTemplate.upsert(query(where("pkg_order").is(e.pkgOrder()).and("scale_mac").is(e.mac())),
                         Update.fromDocument(toDoc(e)), ScaleLog.class);

    // Cách 2: bảng/collection processed_event có unique index trên eventId
    // insert trước; DuplicateKeyException → đã xử lý → bỏ qua
}
```

Lưu ý: bản thân `$inc` **không** idempotent — xử lý cùng message 2 lần là cộng 2 lần. Nếu consumer vừa ghi log vừa tăng bộ đếm, hãy ghi log (unique) trước, chỉ `$inc` khi insert log thành công.

### 5.5. Lỗi khi consume

```java
@Bean
DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> tpl) {
    var recoverer = new DeadLetterPublishingRecoverer(tpl);                  // gửi sang <topic>.DLT
    var handler = new DefaultErrorHandler(recoverer, new ExponentialBackOff(1000, 2) {{ setMaxElapsedTime(30_000); }});
    handler.addNotRetryableExceptions(JsonProcessingException.class, ValidationException.class); // lỗi dữ liệu: retry vô ích
    return handler;
}
```

| Loại lỗi | Xử lý |
|---|---|
| Message hỏng (JSON sai, thiếu field) | Không retry → DLT ngay |
| Phụ thuộc tạm thời (DB, API timeout) | Retry backoff → hết lượt thì DLT |
| Bug code | DLT + alert; sửa code rồi **replay** từ DLT |

<details>
<summary>🧠 "Exactly-once" của Kafka có giải quyết được dual write với DB không?</summary>

Không. Kafka transactions + idempotent producer cho exactly-once **trong phạm vi Kafka** (đọc từ topic → ghi sang topic → commit offset). Ghi vào MongoDB/MySQL nằm ngoài transaction đó. Với DB, cách thực tế là at-least-once + consumer idempotent ("effectively once").
</details>

---

## 6. MQTT cho IoT backend

> 🎯 **Bài toán:** Cân, tủ smart lock, controller nói chuyện với backend qua MQTT (repo có `spring-integration-mqtt`, thư mục `mqtt_ca_smart_lock`, `mqtt_ca_smart_scale`, commit "fix disconnect", "Update clean session"). Nhưng chưa có tài liệu nào trong repo học về MQTT.
>
> 💡 **Ý tưởng:** MQTT là pub/sub nhẹ cho thiết bị yếu, mạng chập chờn. Hiểu đúng 5 thứ: **topic, QoS, session, retained, Last Will** là xử lý được 90% bug.

### 6.1. Khái niệm cốt lõi

| Khái niệm | Ý nghĩa | Bẫy |
|---|---|---|
| **Topic** | Chuỗi phân cấp `ghtk/{stationId}/scale/{mac}/log`; wildcard `+` (1 cấp), `#` (nhiều cấp, cuối) | Thiết kế topic phẳng → không phân quyền / lọc được |
| **QoS 0** | Gửi tối đa 1 lần, có thể mất | Telemetry không quan trọng |
| **QoS 1** | Ít nhất 1 lần (PUBACK) | **Có thể trùng** → xử lý phải idempotent |
| **QoS 2** | Đúng 1 lần (4 bước) | Chỉ đúng trên **một chặng** client↔broker, chậm |
| **Clean session = true** (v3) / **Clean start + session expiry** (v5) | Broker không giữ subscription & message chờ khi client rớt | Backend dùng clean session → **mất message QoS1 trong lúc deploy** |
| **Client ID** | Định danh session trên broker | **Hai kết nối cùng client ID → broker ngắt kết nối cũ** (theo spec) → hai pod cùng client ID sẽ đá nhau liên tục |
| **Retained message** | Broker giữ message cuối của topic, gửi ngay cho subscriber mới | Dùng cho "trạng thái hiện tại" (config, online/offline) |
| **Last Will (LWT)** | Message broker tự publish khi client rớt bất thường (hết keepalive) | Cách chuẩn phát hiện offline |
| **Keepalive** | Client ping định kỳ; quá 1.5× không thấy → broker coi là chết | Callback xử lý chậm làm trễ ping |

### 6.2. 🐞 Thiết kế đáng xem lại

**(a) Đoán offline bằng cách quét DB:** `scanDbToUpdateScaleCameraStatus` load **toàn bộ** cân SMART_SCALE_V2 mỗi lượt, so `last_online_date` với `now - 5 phút`. Độ trễ phát hiện lên tới 5 phút + chu kỳ job, tốn query, và quét lại cả nghìn bản ghi không đổi.

```
✅ Thiết bị khi CONNECT đặt:  will topic  = ghtk/{station}/scale/{mac}/status
                              will payload = {"online":false}, retain = true, QoS 1
   Ngay sau khi kết nối:     publish {"online":true} retain = true lên cùng topic
   Backend subscribe:        ghtk/+/scale/+/status  → cập nhật DB, tạo incident khi offline
```

Phát hiện trong ~1.5 × keepalive, chỉ xử lý khi **có thay đổi**. Job quét DB chỉ còn là lưới an toàn chạy thưa.

**(b) Nhiều pod cùng subscribe một topic** → mỗi message được xử lý N lần (N = số pod). Giải pháp:

- **Shared subscription** `$share/device-manager/ghtk/+/scale/+/log` — broker chia message cho các pod trong nhóm (chuẩn trong MQTT 5; EMQX, HiveMQ, Mosquitto ≥ 1.6 đều hỗ trợ).
- Hoặc **bridge MQTT → Kafka** (EMQX rule engine / Kafka Connect) rồi backend consume Kafka với consumer group — có replay, có DLT, scale tốt hơn.

**(c) Client ID:** thư mục persistence của Paho trong repo có dạng `device-manager-be-ias-DESKTOP-STIAPMH-...` → client ID đang gắn hostname, đúng hướng. Trên K8s dùng `HOSTNAME` (tên pod) và đảm bảo pod mới không trùng tên pod cũ đang chưa chết hẳn.

### 6.3. 🛠 Spring Integration MQTT — những điểm cần nhớ

```java
@Bean
MqttPahoClientFactory mqttClientFactory() {
    var f = new DefaultMqttPahoClientFactory();
    var o = new MqttConnectOptions();
    o.setServerURIs(new String[]{ brokerUrl });
    o.setCleanSession(false);              // giữ subscription & message QoS1 khi reconnect
    o.setAutomaticReconnect(true);
    o.setKeepAliveInterval(30);
    o.setSocketFactory(mutualTlsSocketFactory());   // repo đã có CA riêng cho từng loại thiết bị
    f.setConnectionOptions(o);
    return f;
}

@Bean
MessageProducer scaleInbound(MqttPahoClientFactory f) {
    var a = new MqttPahoMessageDrivenChannelAdapter(clientId("scale-in"), f, "$share/dm/ghtk/+/scale/+/log");
    a.setQos(1);
    a.setOutputChannel(scaleLogChannel());
    return a;
}

@Bean
MessageChannel scaleLogChannel() {
    return new ExecutorChannel(mqttWorkerExecutor());   // ✅ đẩy xử lý sang pool riêng
}
```

**Vì sao `ExecutorChannel`:** mặc định handler chạy trên **thread callback của Paho**. Nếu handler chậm (gọi DB, gọi HTTP) → thread đó không đọc được socket → không kịp gửi/nhận PINGRESP → broker ngắt kết nối → "fix disconnect". Đẩy sang executor riêng để thread Paho chỉ nhận message.

### 6.4. Checklist MQTT

- [ ] Topic có cấu trúc `org/station/deviceType/deviceId/kind`; ACL trên broker: thiết bị chỉ publish vào topic của chính nó.
- [ ] Mutual TLS hoặc username/password riêng từng thiết bị — không dùng chung một tài khoản cho cả fleet.
- [ ] Client ID duy nhất trên mỗi pod.
- [ ] QoS1 + xử lý idempotent (dùng `messageId` thiết bị gửi kèm, hoặc khoá nghiệp vụ).
- [ ] Online/offline bằng LWT + retained.
- [ ] Handler không chặn thread Paho.
- [ ] Nhiều pod: shared subscription hoặc bridge sang Kafka.
- [ ] Payload có `schemaVersion` và `firmwareVersion` để backend xử lý nhiều đời thiết bị.

<details>
<summary>🧠 QoS 2 giữa thiết bị và broker, QoS 2 giữa broker và backend — vậy end-to-end có "exactly once" không?</summary>

Không. QoS là đảm bảo **theo từng chặng**. Backend nhận đúng 1 lần từ broker nhưng có thể crash sau khi nhận và trước khi ghi DB; thiết bị có thể gửi lại cùng dữ liệu sau khi reboot với message id mới. Exactly-once end-to-end chỉ đạt được bằng **idempotency ở tầng ứng dụng** (khoá nghiệp vụ duy nhất).
</details>

---

## 7. Refactor God Class

> 🎯 **Bài toán:** `ScaleService` 1640 dòng, 22 dependency, ~80 method công khai; `IncidentService` 2405 dòng. Mỗi feature mới đều chui vào đây → conflict liên tục khi merge (có commit "merge conflog", "resolve conflict", "Revert Merge branch future-scale"), không ai dám sửa, không test được.
>
> ❌ **Cách ngây thơ:** "Refactor lại toàn bộ" trong một nhánh dài 3 tuần → không review nổi, merge thất bại, bỏ dở.
>
> 💡 **Ý tưởng giải:** Tách theo **lý do thay đổi** (ai/nghiệp vụ nào yêu cầu đổi), làm **từng bước nhỏ, mỗi bước deploy được**, có test giữ hành vi trước khi động vào.

SOLID và design pattern cơ bản: `Java.md` §1, §13. Phần này là **kỹ thuật làm thật** trên codebase đang chạy.

### 7.1. Nhận diện code smell (lấy từ chính `ScaleService`)

| Smell | Biểu hiện trong code | Hệ quả |
|---|---|---|
| **Large Class** | 1640 dòng, constructor 22 tham số | Mọi thay đổi đều đụng file này |
| **Divergent Change** | Cùng một class đổi vì: log cân từ thiết bị, admin sửa cân, báo cáo Jasper, camera offline, firmware, khoá cân | Nhiều người sửa cùng file → conflict |
| **Magic number / Primitive Obsession** | `updatePackageInfoFlag(scaleLog, 2)`, `new GetNetworkInfoResponse(null,null,null,2)` | Không ai biết `2` là gì |
| **Duplicated Code** | `scanDbToUpdateScaleCameraStatus` & `scanDbToUpdateScaleTime` cùng load toàn bộ scale V2, cùng tính `timeOffline` | Sửa một chỗ quên chỗ kia |
| **Feature Envy** | Service tự set 15 field của `Scale` | Logic của `Scale` nằm ngoài `Scale` |
| **Hidden dependency** | `new RestTemplate()`, `new ObjectMapper()`, `ZonedDateTime.now()` trong method | Không mock/test được |
| **Dead code** | Khối `//` comment dài, `statisticLogErrorInDay() {}` rỗng | Nhiễu khi đọc |
| **Mixed abstraction** | Một method vừa build Kafka message, vừa publish Spring event, vừa log | Khó đọc ý định |

Ngưỡng nhẩm để báo động: class > 400 dòng, constructor > 7 dependency, method > 40 dòng, > 3 cấp lồng `if/for`.

### 7.2. Tách theo use case

```
ScaleService (1640 dòng)
 ├─► ScaleLogIngestion          — nhận log từ thiết bị: cập nhật bộ đếm, last log, time spent, package flag
 ├─► ScaleAdminService          — CRUD cân, map camera, khoá/mở cân (dùng bởi màn admin)
 ├─► ScaleHealthMonitor         — quét online/offline, uptime/downtime, tạo incident
 ├─► ScaleReportService         — báo cáo ngày/tuần, export Jasper/Excel
 ├─► ScaleFirmwareService       — version, cập nhật firmware
 └─► ScaleEventPublisher        — một chỗ duy nhất build & gửi event (Kafka/Spring event)
```

Tiêu chí: mỗi class **một nhóm người yêu cầu thay đổi** (team kho, team admin thiết bị, team báo cáo…) — đó là nghĩa thật của SRP ("một lý do để thay đổi" = một actor).

### 7.3. Đưa logic về domain object

```java
// ❌ Anemic model: Scale chỉ có getter/setter, service làm hết
scale.setTotalPckWeighed(scale.getTotalPckWeighed() + 1);
if (scale.getStatus() == NOT_USING) scale.setStatus(USING);
scale.setLastUseUserId(...); scale.setLastUseDate(...);

// ✅ Hành vi nằm cạnh dữ liệu, tên method nói lên nghiệp vụ
public class Scale {
    public void recordWeighing(BigDecimal weight, ZonedDateTime at, Long userId) {
        this.totalPckWeighed++;
        if (weight != null) this.totalPckMass = this.totalPckMass.add(weight);
        this.lastUseDate = at;
        this.lastUseUserId = userId;
        if (status == ScaleStatus.NOT_USING) status = ScaleStatus.USING;   // quy tắc nghiệp vụ ở đúng chỗ
    }
}
```

(Với bộ đếm nóng vẫn dùng `$inc` ở §4.2 — domain method phù hợp cho logic đổi trạng thái phức tạp, ít tranh chấp.)

Enum thay magic number:

```java
enum PackageMatch { NONE(0), OTHER_STATION(1), SAME_STATION(2); ... }
```

### 7.4. Ports & Adapters (Hexagonal) — mức vừa đủ

```
            ┌────────────── application / domain ───────────────┐
 REST ────► │ ScaleAdminService ──► ScaleRepositoryPort          │ ◄──── MongoScaleRepository (adapter)
 MQTT ────► │ ScaleLogIngestion ──► DeviceEventPort              │ ◄──── KafkaDeviceEventAdapter
 Job  ────► │ ScaleHealthMonitor ─► IncidentPort, ClockPort      │ ◄──── GdmsIncidentClient (HTTP)
            └────────────────────────────────────────────────────┘
```

Không cần làm "Clean Architecture" đầy đủ với 5 tầng. Lợi ích thực tế chỉ cần lấy 2 điều:

1. Code nghiệp vụ **không import** `KafkaTemplate`, `RestTemplate`, `MongoTemplate` trực tiếp → test bằng fake trong bộ nhớ.
2. Đổi hạ tầng (Kafka → MQTT bridge, RestTemplate → RestClient) chỉ đổi adapter.

Giữ ranh giới bằng **ArchUnit** (chạy như unit test, fail build nếu vi phạm):

```java
@AnalyzeClasses(packages = "ghtk.iot.devicemanager")
class ArchitectureTest {
    @ArchTest
    static final ArchRule services_do_not_use_kafka_directly =
        noClasses().that().resideInAPackage("..service..")
                   .should().dependOnClassesThat().haveFullyQualifiedName("org.springframework.kafka.core.KafkaTemplate");

    @ArchTest
    static final ArchRule no_god_class =
        classes().that().resideInAPackage("..service..")
                 .should(haveAtMostDependencies(8));   // custom ArchCondition đếm field final
}
```

### 7.5. Quy trình refactor an toàn (áp dụng cho `ScaleService`)

1. **Characterization test:** viết test chụp lại hành vi *hiện tại* (kể cả hành vi lạ) cho các method sẽ di chuyển. Không sửa bug trong bước này.
2. **Extract Class** từng nhóm method (IDE: *Refactor → Extract Delegate*). `ScaleService` cũ giữ lại method dạng uỷ quyền `return scaleReportService.daily(...)` để không phải sửa nơi gọi ngay.
3. Deploy. Theo dõi.
4. Chuyển nơi gọi (controller/job) sang class mới; xoá method uỷ quyền.
5. Lúc này mới **sửa bug** (lost update, N+1) — mỗi bug một PR, có test tái hiện bug trước.

Mỗi bước là một PR < 400 dòng thay đổi. Cách này gọi là **Strangler Fig** ở mức class: class mới mọc dần quanh class cũ cho đến khi class cũ rỗng.

<details>
<summary>🧠 Tách 1 class 1600 dòng thành 6 class 270 dòng thì có phải chỉ là "chia file" cho đẹp?</summary>

Không, nếu tách đúng theo lý do thay đổi. Lợi ích đo được: (1) mỗi class có ít dependency hơn → test dựng nhanh, ít mock; (2) hai người làm hai feature không đụng cùng file → hết conflict; (3) đọc tên class biết nghiệp vụ nằm đâu. Nếu tách sai (theo kiểu "ScaleService1, ScaleService2" hay theo tầng kỹ thuật) thì đúng là chỉ chia file và còn tệ hơn.
</details>

---

## 8. Testing ở mức Senior

> 🎯 **Bài toán:** gdms-be có 1 file test cho > 1000 class. sms-manager-be có nhiều test nhưng bug `futures` vẫn lọt, và có lần **xoá assertion** để test xanh.
>
> 💡 **Ý tưởng:** Test tồn tại để **bắt bug**, không để đạt coverage cho Sonar. Câu hỏi đúng là: *"Nếu mình cài sai chỗ này, test nào sẽ đỏ?"*

JUnit 5, Mockito, `@WebMvcTest`, `@DataJpaTest`: `Java.md` §17, `SpringBoot.md` §19.

### 8.1. 🐞 Xoá assertion

```diff
  verify(invoiceAIService, times(2)).request(any(AudioLabelingRequest.class));
- assertThat(record1.getAiAnalystStatus()).isEqualTo(REQUEST_SUCCESS);
- assertThat(record2.getAiAnalystStatus()).isEqualTo(REQUEST_FAIL);
```

Test fail sau khi đổi code chỉ có 2 khả năng:

| Khả năng | Việc đúng |
|---|---|
| Hành vi **cố ý** thay đổi | Sửa assertion sang giá trị mong đợi **mới**, ghi lý do trong commit |
| Code mới **sai** | Sửa code |

Xoá assertion là chọn khả năng thứ ba: *"không muốn biết"*. Ở đây assertion fail là vì kết quả nằm trên object khác/thời điểm khác sau khi chuyển sang async — đó chính là tín hiệu để xem lại thiết kế.

### 8.2. Test theo hình dạng bug

Đừng chỉ test "happy path một trang". Với mỗi method có vòng lặp/IO, hỏi:

| Hình dạng | Ví dụ test |
|---|---|
| **0 / 1 / nhiều** | Không có bản ghi; đúng 1; nhiều **trang** (bắt bug `futures`) |
| **Biên** | Đúng 500 bản ghi (trang đầy, trang sau rỗng); `totalLimit` |
| **Lỗi từng phần** | Bản ghi thứ 3 ném exception → bản ghi 1,2,4 vẫn được lưu; bản ghi 3 = FAIL; checkpoint đúng |
| **Null / thiếu** | `scale.getStationId() == null`, claim JWT không có `type` |
| **Đồng thời** | 50 thread cùng ghi log cho 1 cân → tổng = 50 |
| **Thời gian** | Qua 0h, đổi ngày, `now - 5 phút` đúng biên |
| **Gọi lại** | Chạy job 2 lần → kết quả như chạy 1 lần (idempotent) |

```java
@Test
void secondPageIsNotResaved() {
    when(repo.findPage(anyLong(), anyLong(), any()))
        .thenReturn(records(1, 500))        // trang 1
        .thenReturn(records(501, 600))      // trang 2
        .thenReturn(List.of());
    service.sendDataToAIService();
    verify(repo, times(2)).saveAll(captor.capture());
    assertThat(captor.getAllValues().get(1)).hasSize(100);   // bug cũ: 600
}
```

### 8.3. Testcontainers — test với DB/Kafka thật

Mock `MongoTemplate` không phát hiện được: query sai field name, thiếu index, `$inc` trên field null, lost update. Dùng DB thật trong Docker:

```java
@SpringBootTest
@Testcontainers
class ScaleLogIngestionIT {
    @Container @ServiceConnection                       // Spring Boot 3.1+: tự cấu hình URI
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @Autowired ScaleLogIngestion ingestion;
    @Autowired MongoTemplate mongoTemplate;

    @Test
    void concurrentWeighingsAreNotLost() throws Exception {
        int n = 50;
        var pool = Executors.newFixedThreadPool(10);
        var start = new CountDownLatch(1);
        for (int i = 0; i < n; i++) pool.submit(() -> { start.await(); ingestion.ingest(logFor("AA:BB")); return null; });
        start.countDown();                               // thả cả 50 cùng lúc
        pool.shutdown(); pool.awaitTermination(30, SECONDS);

        Scale s = mongoTemplate.findOne(query(where("mac").is("AA:BB")), Scale.class);
        assertThat(s.getTotalPckWeighed()).isEqualTo(n); // code get→+1→save sẽ ra < 50
    }
}
```

Boot 2.x (device-manager-be) không có `@ServiceConnection` → dùng `@DynamicPropertySource` đặt `spring.data.mongodb.uri = mongo.getReplicaSetUrl()`. `KafkaContainer` tương tự cho test producer/consumer/DLT.

### 8.4. Test code bất đồng bộ

- Inject executor → trong test thay bằng `new SyncTaskExecutor()` hoặc `Runnable::run` → chạy tuần tự, kết quả xác định.
- Khi buộc phải async thật (Kafka listener, MQTT): **Awaitility** thay cho `Thread.sleep`:

```java
await().atMost(10, SECONDS).untilAsserted(() ->
    assertThat(repo.findByCallId("c1").getRecordUrl()).startsWith("https://storage"));
```

### 8.5. `Clock` — làm logic thời gian test được

```java
@Bean Clock clock() { return Clock.system(ZoneId.of("Asia/Ho_Chi_Minh")); }

class ScaleHealthMonitor {
    private final Clock clock;
    boolean isOffline(ScaleCamera c) {
        return c.getLastOnlineDate().isBefore(ZonedDateTime.now(clock).minusMinutes(5));
    }
}

// test
var fixed = Clock.fixed(Instant.parse("2024-05-01T17:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
```

Code hiện tại gọi `ZonedDateTime.now()` / `DateTimeUtil.now()` trực tiếp ở hàng chục chỗ → mọi logic "quá 5 phút", "báo cáo hôm qua" đều không test được biên.

### 8.6. Đo chất lượng test bằng mutation testing

Coverage 80% chỉ nói *dòng code đã chạy qua*, không nói *test có kiểm tra kết quả không* (test không assert gì vẫn được tính coverage). **PIT** cố tình sửa code (`<` thành `<=`, `+1` thành `-1`, xoá lời gọi method) rồi chạy test: test không đỏ = test yếu.

```bash
mvn org.pitest:pitest-maven:mutationCoverage -DtargetClasses='ghtk.iot.device.smsmanager.service.CdrService'
```

Chỉ cần chạy cho các class nghiệp vụ quan trọng, không cần cả project.

### 8.7. Test pyramid thực dụng cho service của mình

| Tầng | Công cụ | Tỷ lệ | Bắt được |
|---|---|---|---|
| Unit (domain, mapper, validator) | JUnit + AssertJ | Nhiều | Logic nghiệp vụ, biên |
| Slice (`@DataMongoTest`, `@WebMvcTest`) | Spring test | Vừa | Query, validation, JSON, security rule |
| Integration (Testcontainers) | Mongo/Kafka/MySQL thật | Ít, cho luồng quan trọng | Concurrency, index, serialization, transaction |
| Contract (khi gọi service khác) | WireMock / Spring Cloud Contract | Ít | Đổi API phía bên kia |

---

## 9. HTTP client & object dùng chung

> 🎯 **Bài toán:** Service gọi rất nhiều hệ thống khác (DVR, IAM, GS storage, AI, BC101, GAM, Google Chat). Một hệ thống chậm không được kéo sập service của mình.

Timeout, retry, circuit breaker, Resilience4j: `SpringBoot.md` §16; timeout ở tầng mạng: `Networking.md` §13.

### 9.1. 🐞 Bug thật

```java
public GetNetworkInfoResponse getCameraNetworkInfo(String sn) {
    RestTemplate restTemplate = new RestTemplate();            // mỗi lần gọi một instance
    String iamToken = externalService.generateIamToken(...);   // xin token mới mỗi request
    ...
}
ObjectMapper mapper = new ObjectMapper();                      // trong vòng lặp / mỗi method
```

| Vấn đề | Hậu quả |
|---|---|
| `new RestTemplate()` mặc định | Dùng `SimpleClientHttpRequestFactory` (HttpURLConnection), **không timeout** → DVR treo = thread Tomcat treo vĩnh viễn; dồn đủ 200 request là cả service ngừng phản hồi |
| Không pool cấu hình được, không interceptor | Không log/metric chung, không gắn traceId |
| Xin IAM token mỗi lần | Gấp đôi số request, IAM chậm kéo theo mọi API chậm |
| `new ObjectMapper()` | Tốn CPU khởi tạo; **không có `JavaTimeModule`** như bean của Spring → serialize `ZonedDateTime` ra mảng số hoặc lỗi; cấu hình naming khác bean chung |
| Trộn Gson + Jackson | Hai bộ quy tắc snake_case/ngày tháng/null khác nhau trong cùng service |

### 9.2. 🛠 Sửa

```java
@Bean
RestTemplate dvrRestTemplate(RestTemplateBuilder b, IamTokenProvider tokens) {
    return b.rootUri(dvrBaseUrl)
            .setConnectTimeout(Duration.ofSeconds(2))
            .setReadTimeout(Duration.ofSeconds(5))
            .additionalInterceptors((req, body, exec) -> {
                req.getHeaders().setBearerAuth(tokens.get());     // token cache theo hạn dùng
                return exec.execute(req, body);
            })
            .build();
}
// Spring Boot 3.2+: RestClient.builder()... cú pháp fluent, cùng ý tưởng

@Component
class IamTokenProvider {
    private volatile CachedToken cached;
    String get() {
        CachedToken c = cached;
        if (c == null || c.expiresAt().isBefore(Instant.now().plusSeconds(60))) {
            synchronized (this) {
                if (cached == null || cached.expiresAt().isBefore(Instant.now().plusSeconds(60))) cached = fetch();
                c = cached;
            }
        }
        return c.value();
    }
}
```

`ObjectMapper`: inject bean của Spring (thread-safe sau khi cấu hình xong). Chọn **một** thư viện JSON cho cả project.

**Checklist mọi lời gọi ra ngoài:** connect timeout, read timeout, tổng thời gian (deadline) < timeout của caller, retry chỉ cho lỗi tạm thời + idempotent, circuit breaker cho phụ thuộc hay chết, metric `http.client.requests` theo `uri` template, fallback có chủ đích.

---

## 10. Security: lỗi hay gặp trong code thật

Lý thuyết AuthN/AuthZ, OWASP, JWT: `security-architecture-vi.md` §4, §6, §15; Spring Security filter chain: `SpringBoot.md` §9.

### 10.1. 🐞 Interceptor 20 nhánh

`AuthenticationInterceptor.preHandle` là chuỗi `if (path match /v1/smart-lock/**) … else if (/v1/scale/**) …` với ~20 nhánh, mỗi nhánh tự kiểm token theo cách riêng.

Vấn đề: (1) thứ tự nhánh quyết định quyền → thêm path mới đặt sai chỗ là lọt; (2) path không khớp nhánh nào rơi vào `validate(...)` mặc định — khó biết endpoint nào đang được bảo vệ thế nào; (3) chạy **sau** Spring Security filter chain (interceptor là tầng MVC) → không tận dụng được `@PreAuthorize`, `SecurityContext` đúng cách.

```java
// ✅ Mỗi loại client một SecurityFilterChain, khai báo được đọc như bảng quyền
@Bean @Order(1)
SecurityFilterChain deviceChain(HttpSecurity http) throws Exception {
    return http.securityMatcher("/v1/smart-scale/**", "/v1/scanner/**", "/v1/hardware/log/**")
               .addFilterBefore(new DeviceTokenFilter(deviceTokens), UsernamePasswordAuthenticationFilter.class)
               .authorizeHttpRequests(a -> a.anyRequest().hasRole("DEVICE"))
               .csrf(c -> c.disable())
               .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
               .build();
}

@Bean @Order(2)
SecurityFilterChain staffChain(HttpSecurity http) throws Exception {
    return http.securityMatcher("/v1/**")
               .oauth2ResourceServer(o -> o.opaqueToken(Customizer.withDefaults()))   // IAM introspection
               .authorizeHttpRequests(a -> a
                    .requestMatchers("/v1/face-recognition/validate/**").hasRole("STAFF")
                    .anyRequest().authenticated())
               .build();
}
```

### 10.2. Các lỗi nhỏ nhưng hay gặp

| Lỗi | Ở đâu | Sửa |
|---|---|---|
| `secret.equals(secretToken)` | Interceptor | `MessageDigest.isEqual(a.getBytes(UTF_8), b.getBytes(UTF_8))` — so sánh thời gian hằng, chống timing attack |
| `JWT.decode()` rồi dùng claim | Interceptor, TokenAuthorizationFilter | `decode` **không kiểm chữ ký**. Chỉ dùng claim để định tuyến khi bước sau có xác thực thật (introspection / `JWT.require(alg).verify`). Không bao giờ lấy quyền/userId từ token chưa verify |
| `switch (tokenType)` khi claim không có | project-manage-be | `switch` trên `String` null → NPE → 500. Kiểm null → 401 |
| Token tĩnh dùng chung cho cả loại thiết bị (`smartScaleToken`, `rockPiToken`) | device-manager-be | Một thiết bị bị lộ token = cả fleet lộ. Token riêng từng thiết bị / mTLS, thu hồi được |
| Secret trong `application.properties`, `docker-compose.yml` | nhiều repo | Biến môi trường từ K8s Secret / Vault; quét lịch sử git bằng `gitleaks`; **xoay vòng** secret đã lộ |
| Trả `message` exception cho client | `failedAuthentication(response, e.getMessage(), …)` | Lộ chi tiết nội bộ; trả mã lỗi chung, log chi tiết ở server |

---

## 11. Kỹ năng Senior ngoài code

> 🎯 **Bài toán:** Sự khác biệt lớn nhất giữa Mid và Senior không nằm ở biết thêm API, mà ở: **làm cho người khác hiểu, review, vận hành và tin được** thứ mình làm.

### 11.1. Commit message

Hiện tại ~12% commit là `update`, `fix`, `commit`, `add`. Sáu tháng sau đọc `git log` không biết vì sao một dòng code tồn tại.

**Conventional Commits:** `<type>(<scope>): <mô tả mệnh lệnh>` + thân trả lời **vì sao**.

| Trước | Sau |
|---|---|
| `update` | `feat(scale): lưu firmware version mới nhất từ log cân` |
| `fix bug` | `fix(cdr): không lưu lại bản ghi của trang trước khi gửi AI` |
| `update job to concurrent` | `perf(cdr): gửi AI song song 5 luồng qua aiRequestExecutor` |
| `revert` | `revert: "feat(scale): add scale" — gây NPE khi stationId null` |
| `Thêm try catch` | `fix(printer): trả 502 thay vì 500 khi máy in không phản hồi` |

```
fix(cdr): đánh dấu REQUEST_FAIL khi gọi AI ném exception

Trước đây exception trong Future chỉ được log, bản ghi giữ aiStatus=null
nhưng checkpoint vẫn dời qua → bản ghi không bao giờ được xử lý lại.
Nay mọi bản ghi đều có trạng thái cuối; job retry lấy các bản ghi FAIL.

Refs: IOT-1234
```

### 11.2. Pull request nhỏ

- < 400 dòng thay đổi (không tính file sinh tự động). Commit > 800 dòng gần như không được review thật.
- Tách: **refactor không đổi hành vi** / **thay đổi hành vi** thành PR riêng.
- Mô tả PR: *Vấn đề → Cách làm → Cách kiểm thử → Rủi ro / cách rollback*.
- Tự review diff của mình trước khi gửi (đọc trên giao diện GitLab, không phải trong IDE).

### 11.3. Checklist khi review (cho người khác và cho chính mình)

- [ ] **Đúng:** biên 0/1/nhiều, null, timezone, phân trang.
- [ ] **Đồng thời:** nhiều pod chạy cùng lúc thì sao? Có read-modify-write không?
- [ ] **Lỗi:** dependency ngoài chết/chậm thì sao? Có timeout? Exception đi đâu?
- [ ] **Dữ liệu:** có ghi 2 nơi không? Chạy lại có an toàn (idempotent)? Migration ngược được không?
- [ ] **Hiệu năng:** query trong vòng lặp? Có index? Load cả collection?
- [ ] **Quan sát:** khi hỏng, log/metric nào cho mình biết?
- [ ] **Bảo mật:** input được validate? Quyền đúng? Có secret trong code?
- [ ] **Test:** test nào đỏ nếu cài sai?
- [ ] **Đọc được:** tên rõ, không magic number, không code comment-out.

### 11.4. Design doc (1–3 trang) trước khi làm feature lớn

```markdown
# [Tên] — Design Doc
## Bối cảnh & vấn đề          (vì sao cần, số liệu hiện tại)
## Mục tiêu / Không phải mục tiêu
## Thiết kế đề xuất            (sơ đồ luồng, data model, API, topic)
## Các phương án đã cân nhắc  (bảng ưu/nhược, vì sao loại)
## Lỗi & biên                  (dependency chết, trùng message, restart giữa chừng, nhiều pod)
## Quan sát                    (metric, log, alert nào)
## Triển khai & rollback       (feature flag, migration, thứ tự deploy)
## Câu hỏi mở
```

Ví dụ nên viết lại cho các hệ thống đã làm: luồng call center (hangup → upload ghi âm → BC101 → AI), luồng log cân smart scale V2 (MQTT → DB → Kafka → incident).

### 11.5. Postmortem không đổ lỗi

Sau mỗi sự cố production: *Dòng thời gian → Tác động (bao nhiêu cân/cuộc gọi, bao lâu) → Nguyên nhân gốc (hỏi "tại sao" 5 lần) → Vì sao không phát hiện sớm hơn → Hành động (có người phụ trách, có hạn)*. Commit "Hot fix bug", "hot fix trang thai" là dấu hiệu đã có sự cố — mỗi cái đáng một postmortem ngắn.

### 11.6. Ownership

- Mình own `ScaleService`, `IncidentService` → mình chủ động: dashboard Grafana cho luồng cân, alert khi tỉ lệ lỗi tăng, runbook xử lý sự cố.
- Đề xuất cải tiến kèm **số liệu** ("job quét offline tốn 40k query/giờ, chuyển LWT giảm còn ~0, phát hiện nhanh từ 5 phút xuống 45 giây") thay vì "code này xấu".

---

## 12. Lộ trình 6 tháng & bài tập

Mỗi tuần ~6–8 giờ. Đọc lý thuyết ở file tương ứng, **làm bài tập trên code thật** (nhánh riêng / project cá nhân tái hiện). Đề bài chi tiết, tiêu chí đạt và gợi ý: [`SeniorGaps-Exercises.md`](SeniorGaps-Exercises.md).

| Tháng | Chủ đề | Đọc | 🏋️ Bài tập |
|---|---|---|---|
| 1 | Exception, logging, concurrency | §1, §2 file này; `Java.md` §5, §9; *Java Concurrency in Practice* ch.1–8 | Sửa `CdrService.sendDataToAIService` theo §2.3 + test nhiều trang + test lỗi từng phần. Chuyển mọi `new ThreadPool` thành bean |
| 2 | MongoDB & batch job | §3, §4; tài liệu MongoDB "Data Modeling", "Indexes" | Viết lại `updateScale` bằng `$inc`/upsert + unique index; test 50 thread bằng Testcontainers. Đổi `updatePackageInfos` sang `$in` + bulk. Chạy `explain` cho 5 query chậm nhất |
| 3 | Kafka & nhất quán dữ liệu | §5; `SpringBoot.md` §10; `SystemDesign.md` §3; *DDIA* ch.5, 7, 9, 11 | Project mẫu: outbox (Mongo) → relay → Kafka → consumer idempotent + DLT; viết test giết relay giữa chừng vẫn không mất event |
| 4 | MQTT & IoT | §6; tài liệu HiveMQ "MQTT Essentials" | Dựng Mosquitto/EMQX bằng Docker; demo LWT + retained online/offline; shared subscription 2 instance; handler chậm trên thread Paho vs `ExecutorChannel` |
| 5 | Refactor & testing | §7, §8; *Refactoring* (Fowler) ch.1–3, 6–7 | Tách `ScaleService` theo §7.5 (ít nhất `ScaleReportService` + `ScaleHealthMonitor`); thêm ArchUnit; chạy PIT cho 1 class |
| 6 | Observability, security, system design | §9–11; `SpringBoot.md` §12; `security-architecture-vi.md`; *System Design Interview* vol 1 | Gắn Micrometer Tracing + Grafana cho 1 service; chuyển interceptor sang `SecurityFilterChain`; viết 2 design doc (call center, smart scale V2); luyện 4 đề system design có thiết bị IoT |

**Thói quen song song từ tuần 1:** mọi commit theo Conventional Commits; mọi PR < 400 dòng, có mô tả & test; mỗi tuần review code của 1 đồng nghiệp bằng checklist §11.3.

**Tự đánh giá cuối mỗi tháng:** trả lời các câu §13 không nhìn tài liệu; giải thích được cho người khác bằng ví dụ từ code của mình.

---

## 13. Câu hỏi tự kiểm tra tổng hợp

<details>
<summary>1. Job xử lý 5000 bản ghi bằng cursor <code>fromId</code>. Bản ghi giữa chừng lỗi. Có những cách nào để không mất nó?</summary>

(a) Đánh dấu trạng thái cuối FAILED (kể cả khi exception) rồi mới dời cursor, job retry riêng lấy FAILED; (b) chỉ dời cursor tới bản ghi **liên tục thành công** cuối cùng (low watermark); (c) bỏ cursor, chuyển sang status-driven `WHERE status IN (PENDING, FAILED) AND next_retry_at <= now`. (c) đơn giản và tự chữa lành nhất.
</details>

<details>
<summary>2. Vì sao <code>ThreadPoolTaskExecutor</code> với core=5, max=20, queue không giới hạn thực tế chỉ có 5 thread?</summary>

`ThreadPoolExecutor` chỉ tạo thêm thread vượt core khi **queue đã đầy**. Queue vô hạn không bao giờ đầy → luôn 5 thread, task dồn vào queue tới khi OOM. Muốn có tác dụng: queue có giới hạn, hoặc core = max.
</details>

<details>
<summary>3. Hai pod cùng upsert cân mới với cùng MAC, collection không có unique index. Kết quả? Có unique index thì sao?</summary>

Không có index: cả hai cùng không tìm thấy → cả hai insert → 2 document trùng; sau đó `findByMac` ném `IncorrectResultSizeDataAccessException`. Có unique index: một insert thành công, cái kia `DuplicateKeyException` (E11000) → retry thành update.
</details>

<details>
<summary>4. Event trạng thái cân gửi Kafka không có key. Consumer thấy USING sau NOT_USING dù thực tế ngược lại. Vì sao, sửa sao?</summary>

Không key → message rải vào nhiều partition; Kafka chỉ đảm bảo thứ tự **trong một partition**; consumer đọc các partition song song. Sửa: key = scaleId. Thêm `version`/`occurredAt` vào event để consumer bỏ qua event cũ hơn trạng thái đã có (phòng retry làm đảo thứ tự).
</details>

<details>
<summary>5. Thiết bị rút điện đột ngột. Backend biết bằng cách nào, sau bao lâu, với MQTT?</summary>

Broker không nhận PINGREQ/packet trong 1.5 × keepalive → coi client chết → publish Last Will (`{"online":false}`, retained) → backend subscribe topic status nhận được. Keepalive 30s → ~45s. Ngắt kết nối "sạch" (DISCONNECT) thì Will **không** gửi (MQTT 5 có tuỳ chọn) → thiết bị nên tự publish offline trước khi disconnect có chủ đích.
</details>

<details>
<summary>6. Vì sao <code>log.error("Lỗi: {}, sn: {}", e, sn)</code> không in stacktrace?</summary>

SLF4J coi Throwable là "exception để in stacktrace" chỉ khi nó là tham số **cuối** và **dư ra** so với số `{}`. Ở đây `e` lấp vào `{}` đầu → chỉ gọi `toString()`. Viết `log.error("Lỗi, sn: {}", sn, e)`.
</details>

<details>
<summary>7. Khi nào dùng <code>@Version</code>, khi nào dùng <code>$inc</code>, khi nào dùng transaction?</summary>

`$inc`/`$set`: thay đổi biểu diễn được bằng một lệnh update một document, đặc biệt bộ đếm nóng. `@Version`: logic phức tạp phải đọc–tính–ghi, xung đột hiếm, chấp nhận retry. Transaction: phải ghi **nhiều document/collection** cùng thành/bại (ví dụ entity + outbox) và không thiết kế lại để gom vào một document được.
</details>

<details>
<summary>8. Pipeline: upload file lên GS, xoá file local, cập nhật URL vào DB. Thứ tự nào an toàn khi pod có thể chết ở bất kỳ bước nào?</summary>

Upload → cập nhật URL vào DB → xoá file local. Chết sau upload: chạy lại upload lần nữa (ghi đè cùng object, vô hại). Chết sau cập nhật DB: lần sau thấy đã có URL, chỉ còn xoá file. Thứ tự cũ (xoá trước khi ghi DB) chết ở giữa = mất file vĩnh viễn.
</details>

<details>
<summary>9. Bạn được giao refactor class 2000 dòng không có test. Bước đầu tiên là gì?</summary>

Viết characterization test cho các luồng chính để chụp lại hành vi hiện tại (kể cả bug), rồi mới extract class từng bước nhỏ, mỗi bước deploy được, giữ method uỷ quyền để không đổi nơi gọi cùng lúc. Sửa bug là PR riêng sau cùng.
</details>

<details>
<summary>10. Coverage 85% nhưng bug vẫn lọt. Đo chất lượng test bằng gì?</summary>

Mutation testing (PIT): đo tỉ lệ đột biến bị test "giết". Kèm theo: review test theo hình dạng bug (§8.2) — có test nhiều trang, lỗi từng phần, đồng thời, biên thời gian chưa.
</details>

<details>
<summary>11. <code>new RestTemplate()</code> gọi DVR, DVR treo không trả lời. Chuyện gì xảy ra với service khi có 300 request/giây gọi API này?</summary>

Không có read timeout → mỗi request giữ một thread Tomcat vô thời hạn. Pool Tomcat mặc định 200 thread → chưa tới một giây là cạn, **mọi API khác** của service cũng treo (cascading failure). Cần timeout + circuit breaker + bulkhead (pool riêng cho lời gọi DVR).
</details>

<details>
<summary>12. Khác nhau giữa Mid và Senior, theo cách bạn tự trả lời bằng ví dụ của chính mình?</summary>

Mid: giao được `sendDataToAIService` chạy song song, nhanh hơn. Senior: trước khi viết hỏi "lỗi giữa chừng thì sao, chạy nhiều pod thì sao, restart thì sao, đo thế nào", viết test cho các câu đó, PR nhỏ có mô tả, sau khi chạy có dashboard chứng minh nhanh hơn và không mất bản ghi — và hướng dẫn được người khác làm như vậy.
</details>
