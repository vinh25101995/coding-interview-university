# Bài tập thực hành — Từ Mid lên Senior Backend

Bài tập đi kèm [`SeniorGaps.md`](SeniorGaps.md). Mỗi bài tái hiện **một bug thật** từng có trong code GHTK, yêu cầu sửa và **chứng minh bằng test** rằng đã sửa đúng.

> ⚠️ Làm trên **project lab cá nhân** (bài 0) hoặc **bản copy local** của code cũ. Không push code công ty lên repo cá nhân / GitHub.

---

## 📖 Cách làm

Mỗi bài có cấu trúc:

| Mục | Ý nghĩa |
| :--- | :--- |
| 🎯 **Mục tiêu** | Kỹ năng luyện, chương lý thuyết tương ứng |
| 🐞 **Bối cảnh** | Bug thật đã mắc |
| 📝 **Đề bài** | Việc phải làm |
| ✅ **Tiêu chí đạt** | Điều kiện kiểm chứng được — **phải có test tự động**, không tính "chạy thử thấy được" |
| 💡 **Gợi ý** | Bấm mở khi bí (cố tự làm ≥ 30 phút trước) |
| 🚀 **Mở rộng** | Cho ai làm xong sớm |

Độ khó: ⭐ (1–2 giờ) · ⭐⭐ (nửa ngày) · ⭐⭐⭐ (1–2 ngày)

**Quy tắc chung cho mọi bài:**

1. Viết test **tái hiện bug trước** (test đỏ), rồi mới sửa (test xanh). Commit hai bước riêng.
2. Mỗi bài một nhánh, một PR (tự review trên GitLab/GitHub), commit theo Conventional Commits.
3. Không xoá / nới lỏng assertion để test xanh.
4. Ghi 3–5 dòng "Bài học" vào cuối PR: mình đã sai ở đâu, giờ hiểu gì.

### Bảng tiến độ

| # | Bài | Chương | Độ khó | Mảng scorecard | Xong |
|---|---|---|---|---|---|
| 0 | Dựng lab | — | ⭐ | — | ☐ |
| 1 | Log & exception đúng chuẩn | §1 | ⭐ | Vận hành | ☐ |
| 2 | Sửa `sendDataToAIService` | §2, §3 | ⭐⭐ | Đồng thời, Testing | ☐ |
| 3 | Backpressure & giới hạn đồng thời | §2 | ⭐⭐ | Đồng thời | ☐ |
| 4 | Pipeline sau cuộc gọi bền vững | §3 | ⭐⭐⭐ | Đồng thời, DB | ☐ |
| 5 | Lost update & upsert đồng thời | §4 | ⭐⭐ | DB, Đồng thời | ☐ |
| 6 | Diệt N+1 bằng `$in` + bulk | §4 | ⭐⭐ | DB | ☐ |
| 7 | Index, `explain`, TTL | §4 | ⭐⭐ | DB | ☐ |
| 8 | Optimistic locking | §4 | ⭐ | DB | ☐ |
| 9 | Outbox + consumer idempotent + DLT | §5 | ⭐⭐⭐ | Đồng thời, Thiết kế | ☐ |
| 10 | Thứ tự event theo key | §5 | ⭐⭐ | Đồng thời | ☐ |
| 11 | MQTT: LWT, shared subscription, handler chậm | §6 | ⭐⭐⭐ | Thiết kế, Vận hành | ☐ |
| 12 | Refactor god class | §7 | ⭐⭐⭐ | Thiết kế | ☐ |
| 13 | Clock & test biên thời gian | §8 | ⭐ | Testing | ☐ |
| 14 | Mutation testing | §8 | ⭐⭐ | Testing | ☐ |
| 15 | Timeout, circuit breaker, cache token | §9 | ⭐⭐ | Vận hành | ☐ |
| 16 | Security filter chain | §10 | ⭐⭐ | Security | ☐ |
| 17 | Commit, design doc, postmortem, review | §11 | ⭐⭐ | Cộng tác, Dẫn dắt | ☐ |
| 18 | Capstone: Smart Scale Lab end-to-end | tất cả | ⭐⭐⭐ | tất cả | ☐ |

Thứ tự khuyến nghị (ưu tiên mảng điểm thấp nhất): **0 → 2 → 5 → 1 → 6 → 13 → 4 → 9 → 10 → 12 → 14 → 3 → 7 → 8 → 15 → 11 → 16 → 17 → 18**.

---

## Bài 0 — Dựng lab ⭐

🎯 Có môi trường chạy được Mongo (replica set), Kafka, MQTT broker cả trong Docker Compose lẫn Testcontainers.

📝 **Đề bài:** tạo project `iot-lab`:

- Java 21, Spring Boot 3.3+, Maven.
- Dependency: `web`, `data-mongodb`, `kafka`, `integration-mqtt` (+ `org.eclipse.paho.client.mqttv3`), `actuator`, `validation`, `security`, `micrometer-registry-prometheus`, test: `testcontainers` (`mongodb`, `kafka`, `junit-jupiter`), `awaitility`, `archunit-junit5`, `wiremock-standalone`.
- `docker-compose.yml`: `mongo:7` chạy replica set 1 node (`--replSet rs0` + lệnh `rs.initiate()`), Kafka KRaft (`apache/kafka:3.7` hoặc `bitnami/kafka`), `eclipse-mosquitto:2` (file config bật `listener 1883` + `allow_anonymous true` cho lab), Prometheus, Grafana.
- Domain tối thiểu: `Scale { id, mac, stationId, status, totalPckWeighed, totalPckMass, lastUseDate, version }`, `ScaleLog { id, scaleMac, pkgOrder, weight, scannerTime }`, `Cdr { id, recordFileName, aiStatus, attempts, nextRetryAt }`.

✅ **Tiêu chí đạt:**

- `docker compose up` chạy đủ service; `/actuator/health` = UP.
- `mvn verify` chạy được 1 integration test dùng `MongoDBContainer` + `@ServiceConnection` (insert rồi đọc lại 1 `Scale`).

<details>
<summary>💡 Gợi ý</summary>

- Mongo transaction (bài 9) cần replica set: trong Testcontainers `MongoDBContainer` mặc định đã khởi tạo replica set 1 node — dùng `getReplicaSetUrl()`.
- Tạo base class `AbstractIT` chứa các container `static` để dùng lại giữa các test (khởi động 1 lần).
</details>

---

## Bài 1 — Log & exception đúng chuẩn ⭐

🎯 §1. Không bao giờ mất stacktrace, mọi log có traceId, kể cả trong thread pool.

🐞 `log.info("... {}", e.getMessage())`, `log.error("... {}, SN: {}", e, sn)`, `e.printStackTrace()`.

📝 **Đề bài:**

1. Viết `DvrClient.checkCamera(sn)` (gọi HTTP, ném `ResourceAccessException` khi timeout). Cố tình viết **bản sai** theo kiểu cũ, viết test chứng minh log **không** chứa stacktrace.
2. Sửa: log đúng chuẩn, fallback có chủ đích, `@RestControllerAdvice` log lỗi không xác định **một lần**.
3. `TraceIdFilter` đưa `traceId` vào MDC + header `X-Request-Id`.
4. `TaskDecorator` truyền MDC sang executor.

✅ **Tiêu chí đạt** (dùng `OutputCaptureExtension` của Spring Boot):

- Test: gọi API làm DVR timeout → output có `at ` (dòng stacktrace) và có `sn=`.
- Test: một lỗi `RuntimeException` không bắt → stacktrace xuất hiện **đúng 1 lần** trong output.
- Test: request có header `X-Request-Id: abc` → mọi dòng log của request đó (kể cả log trong task `@Async`) chứa `abc`.
- Test: sau request, `MDC.get("traceId")` trên thread đó là `null` (đã clear).

<details>
<summary>💡 Gợi ý</summary>

- Pattern log trong test: `logging.pattern.console=%5p [%X{traceId}] %m%n%wEx`.
- Đếm stacktrace: đếm số lần chuỗi `java.lang.RuntimeException: ` xuất hiện trong `output.getOut()`.
</details>

🚀 Chuyển sang log JSON (`LogstashEncoder` hoặc `logging.structured.format.console=ecs` của Boot 3.4) và kiểm field `traceId` trong JSON.

---

## Bài 2 — Sửa `sendDataToAIService` ⭐⭐

🎯 §2.1–2.3, §3.1. Phạm vi biến, đóng gói lỗi trong task, checkpoint đúng, executor là bean.

🐞 List `futures` ngoài `while` → `saveAll` lặp O(n²); task lỗi bị bỏ sót vĩnh viễn; pool tự `new`.

📝 **Đề bài:** chép **nguyên văn** logic cũ vào `CdrAiJob` (starter dưới), viết test tái hiện, rồi sửa.

```java
// STARTER — cố tình giữ bug
@Service
public class CdrAiJob {
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final CdrRepository repo;          // findByIdGreaterThanAndIdLessThan(from, to, Pageable)
    private final CheckpointRepository checkpoint;
    private final AiClient aiClient;           // BaseResponse request(Cdr)

    public void run() {
        long latestId = checkpoint.get("LATEST_UPLOAD_CDR_ID");
        long fromId = checkpoint.get("LATEST_AI_ANALYZE_CDR_ID");
        int totalRecord = 0;
        List<Future<Cdr>> futures = new ArrayList<>();
        List<Cdr> toDo = repo.findByIdGreaterThanAndIdLessThan(fromId, latestId, PageRequest.of(0, 500));
        while (!toDo.isEmpty() && totalRecord < 5000) {
            for (Cdr cdr : toDo) {
                if (cdr.getRecordFileName() == null || cdr.getAiStatus() != null) continue;
                futures.add(executorService.submit(() -> {
                    cdr.setAiStatus(aiClient.request(cdr).isSuccess() ? "SUCCESS" : "FAIL");
                    return cdr;
                }));
            }
            List<Cdr> updated = new ArrayList<>();
            for (Future<Cdr> f : futures) {
                try { updated.add(f.get()); } catch (Exception e) { e.printStackTrace(); }
            }
            if (!updated.isEmpty()) repo.saveAll(updated);
            totalRecord += toDo.size();
            fromId = toDo.get(toDo.size() - 1).getId();
            toDo = repo.findByIdGreaterThanAndIdLessThan(fromId, latestId, PageRequest.of(0, 500));
        }
        checkpoint.set("LATEST_AI_ANALYZE_CDR_ID", fromId);
    }
}
```

✅ **Tiêu chí đạt** (unit test với executor đồng bộ + integration test với Mongo thật):

| Test | Kỳ vọng |
|---|---|
| 1200 bản ghi (3 trang: 500/500/200) | Tổng số bản ghi được `saveAll` = **1200** (code cũ: 500+1000+1200 = 2700) |
| `AiClient` ném exception với mọi id chia hết cho 7 | Các bản ghi đó có `aiStatus = FAIL`, `attempts = 1`; **không bản ghi nào** còn `aiStatus = null` |
| Chạy `run()` 2 lần liên tiếp | Lần 2 không gọi `AiClient` thêm lần nào (idempotent) |
| `AiClient` chậm 200ms, 1000 bản ghi | Thời gian < 1000 × 200ms / 4 (chứng minh có song song) |
| App shutdown giữa chừng (`context.close()`) | Không còn thread `ai-req-*` sống; không có bản ghi `aiStatus = PROCESSING` bị treo (nếu dùng trạng thái này) |
| Log | Không có `printStackTrace`; lỗi có `cdrId=` |

<details>
<summary>💡 Gợi ý</summary>

- Inject `Executor` qua constructor. Trong unit test truyền `Runnable::run` hoặc `new SyncTaskExecutor()` → kết quả xác định, không flaky.
- Đếm số bản ghi được save: `ArgumentCaptor<List<Cdr>>` + `verify(repo, atLeastOnce()).saveAll(captor.capture())`, cộng size các list.
- Lời giải tham khảo: `SeniorGaps.md` §2.3 — chỉ xem sau khi đã có test đỏ.
</details>

🚀 Chuyển sang status-driven (§3.1): bỏ checkpoint, query `aiStatus IN (null, FAIL) AND attempts < 5 AND nextRetryAt <= now`, backoff có jitter. Test: bản ghi lỗi 2 lần rồi thành công ở lần 3 → `SUCCESS`, `attempts = 3`.

---

## Bài 3 — Backpressure & giới hạn đồng thời ⭐⭐

🎯 §2.2, §2.4. Chọn kích thước pool theo giới hạn bên kia; queue có giới hạn; virtual thread + `Semaphore`.

📝 **Đề bài:**

1. Viết `FakeAiServer` (WireMock hoặc controller trong test) chỉ chịu **tối đa 5 request đồng thời**: dùng `AtomicInteger` đếm đang xử lý; vượt 5 → trả 429 và tăng biến `rejected`.
2. Gửi 2000 bản ghi qua 3 cấu hình, đo thời gian + `rejected`:
   - (a) `newCachedThreadPool()`
   - (b) `ThreadPoolTaskExecutor` core = max = 5, queue 100, `CallerRunsPolicy`
   - (c) `Executors.newVirtualThreadPerTaskExecutor()` + `Semaphore(5)`
3. Bắn thử cấu hình core=5, max=20, queue **không giới hạn**: in số thread thực tế đang chạy.

✅ **Tiêu chí đạt:**

- Bảng kết quả 3 cấu hình (thời gian, `rejected`, số thread tối đa) trong README của bài.
- (b) và (c): `rejected == 0`; test assert số request đồng thời lớn nhất quan sát được ở server ≤ 5.
- Giải thích được bằng lời vì sao cấu hình ở bước 3 chỉ có 5 thread (câu hỏi 2 ở `SeniorGaps.md` §13).
- Prometheus hiển thị `executor_active_threads`, `executor_queued_tasks` cho pool (b).

---

## Bài 4 — Pipeline sau cuộc gọi bền vững ⭐⭐⭐

🎯 §3.2–3.4. Việc nền không nằm trong RAM; claim pattern; thứ tự copy → ghi nhận → xoá; idempotent.

🐞 `executor.schedule(..., 7, SECONDS)` trong RAM; xoá file local trước khi lưu URL; `Thread.sleep(2000)` chờ CDR.

📝 **Đề bài:** sự kiện `HangupEvent(callId)` → phải thực hiện: (1) chờ CDR xuất hiện, (2) upload file ghi âm (thư mục local) lên "storage" (giả lập bằng thư mục khác hoặc MinIO container), (3) lưu URL vào CDR, (4) xoá file local, (5) gọi `ReportClient.send(callId)`.

- Event handler chỉ **insert** `PostCallTask { callId, step, status, runAt = now + 7s, attempts, lockedBy, leaseUntil }`.
- `PostCallWorker` (`@Scheduled` mỗi 1s) claim task bằng `findAndModify`, chạy bước tiếp theo, cập nhật `step`.
- CDR chưa có → trả task về `PENDING`, `runAt = now + 2s`, không `sleep`.

✅ **Tiêu chí đạt:**

| Kịch bản (test) | Kỳ vọng |
|---|---|
| Bình thường | File có ở storage, CDR có URL, file local bị xoá, `ReportClient` gọi đúng 1 lần |
| Ném exception **sau upload, trước khi lưu URL**, rồi chạy lại worker | Không mất file; cuối cùng trạng thái giống kịch bản bình thường |
| Ném exception **sau lưu URL, trước khi xoá**, rồi chạy lại | File local bị xoá; upload không bị gọi lại (hoặc gọi lại vô hại) |
| "Restart": tạo task, đóng `ApplicationContext`, mở context mới | Task vẫn được xử lý xong |
| 2 worker chạy song song (2 thread gọi `claim()` liên tục), 200 task | Mỗi task được xử lý **đúng 1 lần** (đếm số lần `ReportClient.send` theo callId) |
| Worker chết khi đang giữ task (không trả lock) | Sau khi `leaseUntil` hết hạn, task được worker khác lấy lại |
| CDR xuất hiện sau 5s | Task thành công; không có `Thread.sleep` trong code chính |

<details>
<summary>💡 Gợi ý</summary>

- Claim nguyên tử: `findAndModify(query(status=PENDING, runAt<=now).with(sort _id), update(status=PROCESSING, lockedBy, leaseUntil), returnNew)`.
- "Reaper": `updateMulti(query(status=PROCESSING, leaseUntil<now), set(status=PENDING))`.
- Giả lập crash: inject `FaultInjector` có thể ném exception ở bước chỉ định trong test.
- Thời gian: dùng `Clock` inject được (bài 13) để không phải chờ thật 7 giây.
</details>

---

## Bài 5 — Lost update & upsert đồng thời ⭐⭐

🎯 §4.1–4.2. Để DB làm phép tính; unique index; retry `DuplicateKeyException`.

🐞 `findByMac → +1 → save()` trong `ScaleService.updateScale`.

📝 **Đề bài:**

1. Cài `ScaleIngestionV1.ingest(log)` đúng kiểu cũ (đọc → cộng → save; `new Scale()` nếu chưa có).
2. Integration test (Mongo thật): 50 thread cùng ingest cho **cùng 1 mac đã tồn tại**; 50 thread cùng ingest cho **1 mac chưa tồn tại**.
3. Cài `ScaleIngestionV2` bằng `upsert` + `$inc` + `$setOnInsert` + unique index trên `mac` + retry 1 lần khi `DuplicateKeyException`.
4. Thêm kịch bản: trong lúc 50 thread ingest, 1 thread khác gọi `renameScale(mac, "Cân mới")`.

✅ **Tiêu chí đạt:**

| Test | V1 (phải đỏ) | V2 (phải xanh) |
|---|---|---|
| 50 ingest, mac đã có | `totalPckWeighed < 50` (chạy lặp 10 lần, ít nhất 1 lần sai) | `== 50` mọi lần |
| 50 ingest, mac mới | Có thể > 1 document cùng mac | Đúng 1 document, `== 50` |
| Tổng `totalPckMass` | Lệch | Bằng tổng weight chính xác (BigDecimal/Decimal128) |
| Rename đồng thời | Tên có thể bị ghi đè về tên cũ | Tên mới được giữ |

<details>
<summary>💡 Gợi ý</summary>

- Dùng `CountDownLatch start` để thả tất cả thread cùng lúc, tăng xác suất tái hiện race.
- `@Indexed(unique = true)` chỉ tạo index khi bật `auto-index-creation` — trong lab bật được, nhưng hãy tạo bằng `mongoTemplate.indexOps(Scale.class).ensureIndex(...)` trong test setup để quen cách làm production.
</details>

---

## Bài 6 — Diệt N+1 bằng `$in` + bulk ⭐⭐

🎯 §4.4. Đếm round-trip, gom query, bulk write.

📝 **Đề bài:**

1. Seed 10.000 `Scale`, 2.000 `PackageInfo`.
2. Cài `updatePackageInfos(List<ScaleLog> logs)` kiểu cũ (mỗi log: `findByMac` + 1–2 query package + 1 update). Chạy với 500 log.
3. Viết lại: 1 query `findByMacIn`, 1–2 query package theo `$in`, 1 `bulkOps(UNORDERED)`.

✅ **Tiêu chí đạt:**

- Gắn `CommandListener` vào `MongoClientSettings` trong test để **đếm số lệnh gửi tới Mongo**. Bản cũ ≈ 1.500–2.000 lệnh; bản mới **≤ 4 lệnh**.
- Kết quả dữ liệu hai bản giống hệt nhau (so sánh toàn bộ collection sau khi chạy).
- Ghi thời gian chạy hai bản vào README bài.

<details>
<summary>💡 Gợi ý</summary>

```java
@Bean MongoClientSettingsBuilderCustomizer counter(CommandCounter c) {
    return b -> b.addCommandListener(c);   // CommandCounter implements CommandListener, đếm commandStarted
}
```
</details>

---

## Bài 7 — Index, `explain`, TTL ⭐⭐

🎯 §4.5, §3.6 (TTL).

📝 **Đề bài:**

1. Script seed 1.000.000 `ScaleLog` (500 mac, 90 ngày, `scannerTime` ngẫu nhiên).
2. Ba query:
   - Q1: log của 1 mac trong 7 ngày, sort `scannerTime` giảm dần, limit 50.
   - Q2: đếm log theo `stationId` trong 1 ngày.
   - Q3: log có `weight > 30` của 1 mac.
3. Chạy `explain("executionStats")` trước khi có index → tạo index theo ESR → chạy lại.
4. TTL index xoá log quá 30 ngày (lab: đặt 60 giây để quan sát).

✅ **Tiêu chí đạt:**

- Bảng trước/sau cho mỗi query: `stage`, `totalDocsExamined`, `nReturned`, `executionTimeMillis`. Sau index: `IXSCAN`, không có stage `SORT`, `totalDocsExamined / nReturned ≤ 2` cho Q1.
- Giải thích được vì sao index `{scannerTime: -1, scaleMac: 1}` kém hơn `{scaleMac: 1, scannerTime: -1}` cho Q1 (có số liệu explain cho cả hai).
- Test: insert document `createdAt = now - 2 phút` → trong ~2 phút document biến mất (TTL monitor chạy mỗi 60s).

---

## Bài 8 — Optimistic locking ⭐

🎯 §4.3.

📝 **Đề bài:** hai admin cùng mở form sửa một cân: A đổi `stationId`, B đổi `name`. Cài `updateScaleInfo` kiểu cũ (đọc → set tất cả field từ form → save), sau đó thêm `@Version` + `@Retryable` (hoặc tự retry) + trả 409 cho client khi xung đột không tự giải được.

✅ **Tiêu chí đạt:**

- Test bản cũ: sau hai lần update xen kẽ, một trong hai thay đổi bị mất.
- Test bản mới: lần save thứ hai ném `OptimisticLockingFailureException`; API trả **409** kèm `version` hiện tại.
- Test: client gửi kèm `version` cũ → 409; gửi `version` mới → 200.

---

## Bài 9 — Outbox + consumer idempotent + DLT ⭐⭐⭐

🎯 §5.3–5.5. Hết dual write; at-least-once; consumer chịu trùng; message hỏng không chặn partition.

🐞 `save(scale)` rồi `kafkaTemplate.send(topic, json)` không key, bỏ qua kết quả.

📝 **Đề bài:**

1. `changeScaleStatus(id, newStatus)`: trong **một Mongo transaction** cập nhật `Scale` + insert `OutboxEvent { id, aggregateId, type, payload, createdAt, sentAt=null }`.
2. `OutboxRelay` (`@Scheduled` 500ms): claim event chưa gửi → `send(topic, aggregateId, payload).get(5s)` → đánh dấu `sentAt`.
3. `IncidentConsumer`: nhận `ScaleStatusChanged`, khi `NOT_USING` → tạo `Incident`. Idempotent bằng collection `processed_event` (unique `eventId`).
4. `DefaultErrorHandler` + `DeadLetterPublishingRecoverer`; `JsonProcessingException` không retry.

✅ **Tiêu chí đạt** (Testcontainers Mongo + Kafka):

| Kịch bản | Kỳ vọng |
|---|---|
| Bình thường, 100 lần đổi trạng thái | 100 event trên topic, key = scaleId |
| Exception sau khi update Scale, trước commit | Không có event nào (transaction rollback cả hai) |
| Relay "crash" sau `send` thành công, trước khi set `sentAt` → chạy lại relay | Event bị gửi **2 lần**; consumer chỉ tạo **1** incident |
| Kafka dừng 10s (`kafka.stop()` / pause container) rồi chạy lại | Không mất event nào; relay gửi bù |
| Gửi 1 message JSON hỏng vào topic | Message nằm ở `<topic>.DLT` sau 0 lần retry; các message sau vẫn được xử lý |
| Consumer ném lỗi tạm thời 2 lần rồi OK | Incident được tạo, không vào DLT |

<details>
<summary>💡 Gợi ý</summary>

- Bean `MongoTransactionManager` + `@Transactional` trên `changeScaleStatus`.
- Giả lập "crash sau send": inject hook ném exception sau `send().get()` trong relay khi test bật cờ.
- Dừng Kafka trong test: `DockerClientFactory.instance().client().pauseContainerCmd(kafka.getContainerId()).exec()`.
</details>

🚀 Thay relay polling bằng **Change Streams** (`mongoTemplate.changeStream(...)`), lưu resume token để restart không mất event.

---

## Bài 10 — Thứ tự event theo key ⭐⭐

🎯 §5.1 (2), §5.2.

📝 **Đề bài:** topic 6 partition; producer gửi 10 thiết bị × 200 lần đổi trạng thái xen kẽ (`USING`/`NOT_USING`, kèm `seq` tăng dần mỗi thiết bị); consumer `concurrency = 3` cập nhật trạng thái cuối vào Mongo.

✅ **Tiêu chí đạt:**

- Không key: test cho thấy ít nhất 1 thiết bị có trạng thái cuối **khác** trạng thái thật (lặp vài lần nếu cần) — hoặc đếm số lần consumer nhận `seq` nhỏ hơn `seq` đã xử lý.
- Có key = deviceId: trạng thái cuối đúng 100%, không nhận `seq` lùi.
- Thêm phòng thủ: consumer bỏ qua event có `seq ≤ lastSeq` đã lưu (`$max` hoặc update có điều kiện) → đúng kể cả khi cố tình gửi lại event cũ.

---

## Bài 11 — MQTT: LWT, shared subscription, handler chậm ⭐⭐⭐

🎯 §6. Phát hiện offline đúng cách; nhiều instance không xử lý trùng; không làm rớt kết nối.

🐞 Job quét DB mỗi 5 phút đoán offline; commit "fix disconnect", "Update clean session".

📝 **Đề bài:**

1. `DeviceSimulator` (Paho client trong test): kết nối với Will `ghtk/s1/scale/{mac}/status = {"online":false}` retained QoS1, keepalive 5s; sau khi kết nối publish `{"online":true}` retained; publish log mỗi 500ms vào `ghtk/s1/scale/{mac}/log`.
2. Backend subscribe `ghtk/+/scale/+/status` → cập nhật `Scale.online` + tạo incident khi offline.
3. Hai instance backend (2 adapter với client ID khác nhau trong cùng test) subscribe `$share/dm/ghtk/+/scale/+/log`.
4. Handler log cố tình chậm 3s/message; so sánh chạy trên thread callback của Paho vs `ExecutorChannel`.

✅ **Tiêu chí đạt** (`GenericContainer("eclipse-mosquitto:2")` với file config cho phép anonymous):

| Kịch bản | Kỳ vọng |
|---|---|
| Simulator ngắt **đột ngột** (`disconnectForcibly(0, 0, false)` — không gửi DISCONNECT) | Backend ghi `online=false` trong ≤ 1.5 × keepalive + 2s |
| Simulator ngắt **có chủ đích** | Thiết bị tự publish offline trước; backend cập nhật ngay |
| Backend mới subscribe sau khi thiết bị đã online | Nhận ngay trạng thái nhờ retained |
| 2 instance + shared subscription, 1000 log | Tổng số log được xử lý = 1000, không trùng (đếm theo `messageId` trong payload) |
| 2 instance **không** shared subscription | Mỗi log bị xử lý 2 lần (test chứng minh vấn đề) |
| Backend dùng `cleanSession=false`, tắt 10s rồi bật lại, thiết bị gửi QoS1 trong lúc tắt | Backend nhận đủ message bị lỡ |
| 2 instance dùng **cùng client ID** | Quan sát kết nối bị đá qua lại (log `connectionLost`) — ghi lại hiện tượng |
| Handler chậm trên thread Paho vs `ExecutorChannel` | Ghi lại: có/không có `connectionLost`, độ trễ nhận message, số message tồn đọng |

---

## Bài 12 — Refactor god class ⭐⭐⭐

🎯 §7. Characterization test → tách từng bước → ArchUnit giữ ranh giới.

📝 **Đề bài:** trên **bản copy local** của `device-manager-be` (hoặc tự dựng `ScaleService` ~600 dòng gộp 6 trách nhiệm trong lab):

1. Liệt kê mọi method public của `ScaleService`, gán mỗi method vào 1 trong 6 nhóm ở `SeniorGaps.md` §7.2. Ghi số dòng, số dependency, số nơi gọi.
2. Viết characterization test cho các method nhóm **Report** và **HealthMonitor** (dùng Testcontainers Mongo, dữ liệu seed cố định, snapshot kết quả).
3. Extract `ScaleReportService`, `ScaleHealthMonitor` — giữ method uỷ quyền trong `ScaleService` ở bước đầu.
4. Chuyển nơi gọi, xoá method uỷ quyền.
5. Enum hoá magic number (`updatePackageInfoFlag(.., 2)`, `cameraStatus 2`…).
6. Thêm `ArchitectureTest`: service mới không phụ thuộc `KafkaTemplate`/`RestTemplate` trực tiếp; không service nào > 8 dependency.

✅ **Tiêu chí đạt:**

- Characterization test xanh **trước và sau** mọi bước (không sửa test trong quá trình refactor).
- Mỗi bước là 1 commit < 400 dòng diff (trừ file test).
- Bảng trước/sau: số dòng `ScaleService`, số dependency, số method public.
- ArchUnit test fail nếu cố tình inject `KafkaTemplate` vào `ScaleReportService`.

---

## Bài 13 — Clock & test biên thời gian ⭐

🎯 §8.5, §4.8.

📝 **Đề bài:**

1. `ScaleHealthMonitor.isOffline(camera)` ngưỡng 5 phút, dùng `Clock` inject.
2. `DailyReport.forYesterday()` lấy log của "hôm qua" theo giờ `Asia/Ho_Chi_Minh`, dữ liệu lưu UTC.

✅ **Tiêu chí đạt:**

- Test `isOffline` tại đúng 4:59, 5:00, 5:01 kể từ `lastOnline` — kết quả đúng như định nghĩa (ghi rõ 5:00 là online hay offline).
- Test báo cáo chạy lúc 00:30 ngày 02/05 giờ VN: log lúc 23:50 ngày 01/05 giờ VN (= 16:50 UTC) **có** trong báo cáo; log lúc 06:00 ngày 02/05 giờ VN **không** có; log 01:00 ngày 01/05 giờ VN (= 18:00 UTC ngày 30/04) **có**.
- `grep -rn "now()" src/main` chỉ còn trong `ClockConfig`.

---

## Bài 14 — Mutation testing ⭐⭐

🎯 §8.6. Đo độ mạnh của test.

📝 **Đề bài:** chạy PIT cho `CdrAiJob` (bài 2) và `ScaleIngestionV2` (bài 5).

```bash
mvn org.pitest:pitest-maven:mutationCoverage \
    -DtargetClasses='lab.cdr.CdrAiJob,lab.scale.ScaleIngestionV2' \
    -DtargetTests='lab.*'
```

✅ **Tiêu chí đạt:**

- Mutation score ≥ **80%** cho hai class.
- Với mỗi mutant còn sống: hoặc thêm test giết nó, hoặc ghi lý do nó là "equivalent mutant" (đột biến không đổi hành vi).
- Ghi lại 3 mutant thú vị nhất và test đã thêm để giết chúng.

---

## Bài 15 — Timeout, circuit breaker, cache token ⭐⭐

🎯 §9. Một dependency chậm không kéo sập service.

🐞 `new RestTemplate()` không timeout; xin IAM token mỗi request.

📝 **Đề bài:**

1. WireMock giả lập DVR: endpoint `/camera/check` trễ 10s; IAM `/token` trả token hết hạn sau 60s.
2. Đặt `server.tomcat.threads.max=10` trong test. Gọi 30 request song song vào API `/scales/{sn}/network` (gọi DVR) **và** đồng thời gọi `/actuator/health`.
3. Bản cũ (`new RestTemplate()`): quan sát health check bị treo.
4. Bản mới: `RestClient`/`RestTemplateBuilder` với connect 1s / read 2s; Resilience4j `CircuitBreaker` + `Bulkhead` (tối đa 5 lời gọi DVR đồng thời); fallback `UNKNOWN`; `IamTokenProvider` cache theo hạn.

✅ **Tiêu chí đạt:**

- Bản mới: `/actuator/health` luôn trả trong < 500ms khi DVR treo.
- Sau N lỗi liên tiếp, circuit mở: WireMock ghi nhận **không** còn request mới trong thời gian mở.
- 100 lời gọi DVR trong 60s → WireMock IAM nhận đúng **1** request `/token`; sau 60s → thêm 1.
- 50 thread cùng gọi khi token hết hạn → IAM nhận **1** request (không "thundering herd").

---

## Bài 16 — Security filter chain ⭐⭐

🎯 §10.

📝 **Đề bài:** chuyển logic `AuthenticationInterceptor` (rút gọn còn 3 loại: thiết bị dùng `x-access-token`, nhân viên dùng Bearer token qua introspection — giả lập bằng WireMock, endpoint nội bộ dùng secret) sang 3 `SecurityFilterChain`.

✅ **Tiêu chí đạt** (MockMvc):

| Request | Kỳ vọng |
|---|---|
| Thiết bị gọi `/v1/smart-scale/**` với token đúng | 200 |
| Thiết bị dùng token đúng gọi `/v1/admin/**` | 401/403 |
| Bearer token thiếu claim `type` | **401**, không phải 500 |
| Bearer token chữ ký sai nhưng claim hợp lệ | 401 (không tin `JWT.decode`) |
| Endpoint mới không khai báo trong bất kỳ chain nào | Mặc định **từ chối** (deny by default) |
| So sánh token | Dùng `MessageDigest.isEqual` (có unit test riêng cho hàm so sánh) |

🚀 Chạy `gitleaks detect` trên bản copy local các repo cũ; lập danh sách secret cần xoay vòng (không ghi giá trị secret vào báo cáo).

---

## Bài 17 — Commit, design doc, postmortem, review ⭐⭐

🎯 §11. Mảng điểm "Cộng tác" và "Dẫn dắt".

📝 **Đề bài:**

1. **Commit:** lấy 20 commit cũ dạng `update`/`fix` của mình (`git log --author=vinh --format='%h %s'`), xem diff, viết lại message theo Conventional Commits có thân "vì sao". (Chỉ ghi ra file, không rewrite lịch sử repo.)
2. **Design doc:** viết design doc 2–3 trang cho **luồng call center sau cuộc gọi** theo template §11.4, có ít nhất 2 phương án được so sánh và mục "Lỗi & biên" trả lời: restart giữa chừng, 2 pod, storage chậm, CDR đến muộn.
3. **Postmortem:** chọn 1 commit "Hot fix" cũ, viết postmortem 1 trang (dựng lại dòng thời gian từ git log; tác động ước lượng).
4. **Review:** chọn 1 PR/commit lớn cũ của mình (> 500 dòng), review bằng checklist §11.3 như thể của người khác.

✅ **Tiêu chí đạt:**

- 20 commit viết lại; người khác đọc message hiểu được thay đổi mà không cần mở diff.
- Design doc được 1 người khác (đồng nghiệp / bạn) đọc và đặt ≥ 3 câu hỏi; mình cập nhật doc trả lời các câu đó.
- Review tìm ra ≥ 5 vấn đề cụ thể, mỗi vấn đề có đề xuất sửa.

---

## Bài 18 — Capstone: Smart Scale Lab end-to-end ⭐⭐⭐

🎯 Ghép tất cả. Đây là bài dùng để **đo lại level** và làm portfolio.

📝 **Đề bài:**

```
DeviceSimulator ×50 ──MQTT(QoS1, LWT)──► Backend ×2 instance ──► Mongo (atomic update, outbox)
                                              │                         │
                                              └── /metrics              └─ Relay ──► Kafka ──► IncidentConsumer ×2 ──► Mongo
                                                                                       └── .DLT
Prometheus + Grafana: log/s, độ trễ ingest, số thiết bị online, outbox lag, consumer lag, DLT count
```

- 50 thiết bị giả lập, mỗi thiết bị gửi log mỗi 200ms trong 10 phút.
- Design doc (bài 17) viết **trước** khi code.
- Mọi luồng có test tự động như các bài trước.

✅ **Tiêu chí đạt — chaos test** (script chạy tự động, kết quả đối chiếu với log gốc của simulator):

| Sự cố trong lúc chạy | Kỳ vọng sau khi hệ thống ổn định lại |
|---|---|
| Kill 1 instance backend (`docker kill`) | `totalPckWeighed` từng cân = số log simulator đã gửi (không mất, không đếm trùng) |
| Restart Mosquitto | Thiết bị & backend tự kết nối lại; không mất log QoS1 |
| Dừng Kafka 30s | Outbox lag tăng rồi về 0; số incident đúng |
| Simulator gửi trùng 5% message (cùng `messageId`) | Không đếm trùng |
| 3 thiết bị bị "rút điện" | 3 incident offline, mỗi thiết bị đúng 1 |
| Gửi 10 message hỏng | 10 message trong DLT, hệ thống vẫn chạy |

- Dashboard Grafana chụp màn hình trong README, giải thích từng biểu đồ khi có sự cố.
- README có mục "Trade-off đã chọn và vì sao".

---

## 📏 Đo lại level sau khi làm bài

Hoàn thành bài nào thì đủ căn cứ nâng điểm mảng nào trong scorecard (thang 1–5, 4 = chuẩn Senior):

| Mảng (điểm hiện tại) | Bài cần xong để lên ~3 | Thêm để lên ~4 |
|---|---|---|
| Đồng thời / phân tán (2) | 2, 5 | 3, 4, 9, 10 |
| Database (2.5) | 5, 6 | 7, 8 |
| Testing (2) | 2, 13 | 14 + mọi bài đều test-first |
| Thiết kế code (2) | 12 bước 1–3 | 12 đủ, 18 |
| Vận hành (2.5) | 1 | 15, dashboard bài 18 |
| Security (2.5) | 16 | 16 mở rộng + áp dụng vào code thật |
| Cộng tác (2) | 17 (1) | 17 đủ, duy trì 3 tháng |
| Dẫn dắt (?) | 17 (2) | Design doc được team dùng; mentor 1 người làm bài 2 hoặc 5 |

**Tự kiểm chứng:** sau mỗi tháng, trả lời lại 12 câu ở `SeniorGaps.md` §13 không nhìn tài liệu, và giải thích lời giải bài khó nhất tháng đó cho một người khác trong 10 phút.
