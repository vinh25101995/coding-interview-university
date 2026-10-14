# System Design Study & Interview Guide

Tài liệu ôn tập toàn diện System Design cho kỹ sư phần mềm và phỏng vấn Technical System Design (Mid / Senior / Lead). Tài liệu này tập trung vào kiến trúc hệ thống phân tán, các building blocks, network protocols, reliability, security, observability, design patterns và các bài toán phỏng vấn thực tế kinh điển.

> 💡 **Tài liệu liên quan**:
> - [`Database.md`](Database.md): B+Tree, Storage Engine, MVCC, WAL, ARIES Recovery, Distributed DB, Paxos/Raft, Parquet.
> - [`SpringBoot.md`](SpringBoot.md): Kiến trúc chuyên sâu Spring Framework, Spring Boot, Spring Data JPA, Spring Security, Kafka manual partition assignment.
> - [`Java.md`](Java.md): Core Java, Concurrency, JVM Internals, Memory Model, Garbage Collection.
> - [`Networking.md`](Networking.md): OSI/TCP-IP, TCP deep dive (handshake, TIME_WAIT, congestion control), DNS, HTTP, TLS, Kubernetes networking, troubleshooting.

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


### 🗺 Chuỗi bài toán khi một hệ thống lớn dần

```
 1 server + 1 DB chạy tốt ... cho tới khi traffic tăng
   ├─ 1 server không gánh nổi              → scale out: stateless app + load balancer (1)
   ├─ DB quá tải vì đọc                     → cache, CDN (2) → read replica (6.3, Database.md 19)
   ├─ việc chậm làm request phải chờ, service gọi dây chuyền dễ sập
   │                                        → message queue, event-driven (3)
   ├─ nhiều client, nhiều service cần hợp đồng giao tiếp → API design, gateway, rate limit (4)
   ├─ một codebase quá lớn cho nhiều team   → microservices + resiliency + saga (5)
   ├─ dữ liệu vượt 1 máy                     → chọn kho lưu trữ, sharding (6) → consistent hashing (11.1)
   └─ nhiều máy = nhiều thứ hỏng             → availability, DR (8), observability (9)
                                              → hệ phân tán: CAP, consensus, lock, ID, clock (11)
 Mọi lựa chọn đều là ĐÁNH ĐỔI: latency ↔ consistency ↔ availability ↔ chi phí ↔ độ phức tạp vận hành
 → Khung phỏng vấn (12) & case study (13): luôn bắt đầu từ yêu cầu + con số, rồi mới chọn công nghệ.
```

## Mục lục

1. [Scalability Fundamentals](#1-scalability-fundamentals)
2. [Caching & CDN](#2-caching--cdn)
3. [Message Queue & Event-Driven Architecture](#3-message-queue--event-driven-architecture)
4. [API Design & Gateways](#4-api-design--gateways)
5. [Microservices Architecture & Resiliency](#5-microservices-architecture--resiliency)
6. [Storage & Data Systems](#6-storage--data-systems)
7. [Networking & Communication Protocols](#7-networking--communication-protocols)
8. [Reliability, Availability & Disaster Recovery](#8-reliability-availability--disaster-recovery)
9. [Monitoring, Logging & Observability](#9-monitoring-logging--observability)
10. [Security & Authentication](#10-security--authentication)
11. [Distributed System Design Patterns](#11-distributed-system-design-patterns)
    - [11.1. Consistent Hashing](#111-consistent-hashing-băm-nhất-quán--virtual-nodes)
    - [11.2. Merkle Tree](#112-merkle-tree-cây-băm-chống-bất-đồng-bộ--anti-entropy)
    - [11.3. Gossip Protocol](#113-gossip-protocol-giao-thức-tin-đồn)
    - [11.4. Split-Brain Problem & Fencing Tokens](#114-split-brain-problem--fencing-tokens)
    - [11.5. Distributed Unique ID Generation](#115-distributed-unique-id-generation-sinh-id-phân-tán-duy-nhất)
    - [11.6. Distributed Locking Mechanisms](#116-distributed-locking-mechanisms-cơ-chế-khóa-phân-tán)
    - [11.7. Distributed Consensus: Raft vs Paxos](#117-distributed-consensus-raft-vs-paxos)
    - [11.8. Định lý CAP & PACELC Deep Dive](#118-định-lý-cap--pacelc-deep-dive)
    - [11.9. Consistency Models](#119-consistency-models-từ-mạnh--yếu)
    - [11.10. Thời gian & Thứ tự trong hệ phân tán](#1110-thời-gian--thứ-tự-trong-hệ-phân-tán)
    - [11.11. Leader Election, Lease & Heartbeat](#1111-leader-election-lease--heartbeat)
    - [11.12. Cấu trúc dữ liệu xác suất](#1112-cấu-trúc-dữ-liệu-xác-suất-probabilistic-data-structures)
12. [System Design Interview Framework & Back-of-Envelope](#12-system-design-interview-framework--back-of-envelope)
13. [Common System Design Interview Questions (Deep Dives)](#13-common-system-design-interview-questions-deep-dives)
    - [Case 1: Rút gọn URL (TinyURL / Bit.ly)](#case-1-thiết-kế-hệ-thống-rút-gọn-url-tinyurl--bitly)
    - [Case 2: Distributed Rate Limiter](#case-2-thiết-kế-hệ-thống-giới-hạn-tốc-độ-phân-tán-distributed-rate-limiter)
    - [Case 3: Chat Thời Gian Thực (WhatsApp / Messenger)](#case-3-thiết-kế-hệ-thống-chat-thời-gian-thực-whatsapp--slack--messenger)
    - [Case 4: News Feed / Timeline (Twitter / Facebook)](#case-4-thiết-kế-news-feed--timeline-twitter--facebook--instagram)
    - [Case 5: Thông báo Phân tán (Notification System)](#case-5-thiết-kế-hệ-thống-thông-báo-phân-tán-notification-system)
    - [Case 6: Search Autocomplete (Google Search Box)](#case-6-thiết-kế-search-autocomplete--typeahead-google-search-box)
    - [Case 7: Video Streaming (YouTube / Netflix)](#case-7-thiết-kế-nền-tảng-video-streaming-youtube--netflix)
    - [Case 8: Hệ thống Thanh toán Kỹ thuật số (Payment System)](#case-8-thiết-kế-hệ-thống-thanh-toán-kỹ-thuật-số-payment-system)
    - [Case 9: Flash Sale & Đặt vé Sự kiện (Ticketmaster)](#case-9-thiết-kế-hệ-thống-flash-sale--đặt-vé-sự-kiện-ticketmaster--concert-booking)
    - [Case 10: Dịch vụ Định vị & Gọi xe (Uber / Grab)](#case-10-thiết-kế-dịch-vụ-định-vị--gọi-xe-ride-sharing---uber--grab--nearby-friends)
    - [Case 11: Lưu trữ & Đồng bộ Tệp Đám mây (Google Drive / Dropbox)](#case-11-thiết-kế-hệ-thống-lưu-trữ--đồng-bộ-tệp-đám-mây-google-drive--dropbox)
    - [Case 12: Bảng Xếp Hạng Real-time Triệu Người (Gaming Leaderboard)](#case-12-thiết-kế-bảng-xếp-hạng-thời-gian-thực-hàng-triệu-người-real-time-gaming-leaderboard)
    - [Case 13: Thu Thập Dữ Liệu Web Phân Tán (Web Crawler - Googlebot)](#case-13-thiết-kế-hệ-thống-thu-thập-dữ-liệu-web-phân-tán-distributed-web-crawler---googlebot)
    - [Case 14: Distributed Key-Value Store (Dynamo / Cassandra)](#case-14-thiết-kế-distributed-key-value-store-kiểu-amazon-dynamo--cassandra)
    - [Case 15: Distributed Job Scheduler / Delayed Task](#case-15-thiết-kế-distributed-job-scheduler--delayed-task-vd-gửi-nhắc-lịch-hủy-đơn-sau-30-phút-cron-phân-tán)
    - [Case 16: Ad Click Aggregation / Real-time Analytics](#case-16-thiết-kế-ad-click-aggregation--real-time-analytics-đếm-click-theo-phút-cho-hàng-tỷ-sự-kiện)
    - [Case 17: Collaborative Editing (Google Docs / Figma)](#case-17-thiết-kế-collaborative-editing-google-docs--notion--figma)

---

## 1. Scalability Fundamentals

> 🎯 **Bài toán:** Traffic tăng 10× — một server không còn đủ CPU/RAM/kết nối, và nếu nó chết thì cả hệ thống chết.
>
> ❌ **Cách ngây thơ:** Mua server to hơn mãi (scale up) — đơn giản nhưng có trần, giá tăng phi tuyến, và vẫn là một điểm chết duy nhất.
>
> 💡 **Ý tưởng giải:** Chạy **nhiều bản sao giống nhau** sau một **load balancer**. Điều kiện tiên quyết: app server phải **stateless** — trạng thái (session, file, cache dùng chung) được đẩy ra kho dùng chung (Redis, DB, object storage), để request nào đi tới bản sao nào cũng được và bản sao có thể thêm/bớt/chết tùy ý.


Khả năng mở rộng (Scalability) là năng lực của một hệ thống có thể xử lý khối lượng công việc ngày càng tăng (traffic, data volume, concurrency) bằng cách bổ sung thêm tài nguyên phần cứng mà không làm suy giảm hiệu năng.

### 1.1. Vertical Scaling (Scale Up) vs Horizontal Scaling (Scale Out)

```
Vertical Scaling (Scale Up)               Horizontal Scaling (Scale Out)
┌─────────────────────────┐               ┌───────┐  ┌───────┐  ┌───────┐
│     Single Big Server   │               │ Node1 │  │ Node2 │  │ Node3 │
│  ┌───────────────────┐  │               └───┬───┘  └───┬───┘  └───┬───┘
│  │ CPU: 128 Cores    │  │                   │          │          │
│  │ RAM: 1 TB         │  │               ────┴──────────┴──────────┴────
│  │ Disk: 10 TB NVMe  │  │                     Distributed System
│  └───────────────────┘  │
└─────────────────────────┘
```

| Tiêu chí | Vertical Scaling (Scale Up) | Horizontal Scaling (Scale Out) |
| :--- | :--- | :--- |
| **Bản chất** | Nâng cấp cấu hình của 1 server hiện tại (thêm CPU, RAM, NVMe). | Thêm nhiều server máy chủ cấu hình vừa/nhỏ vào pool xử lý. |
| **Độ phức tạp code** | Đơn giản, kiến trúc không đổi, không cần xử lý distributed state. | Phức tạp: cần Load Balancer, stateless servers, distributed locks, network partition handling. |
| **Giới hạn vật lý** | Chạm trần phần cứng (Hard hardware limit) & chi phí tăng theo cấp số nhân (Supercomputers rất đắt). | Về mặt lý thuyết là vô hạn (hàng ngàn commodity servers). |
| **Downtime** | Thường yêu cầu downtime để nâng cấp linh kiện / switch instance. | Không downtime, zero-downtime rolling deployment. |
| **Fault Tolerance** | Single Point of Failure (SPOF) - máy sập là toàn bộ hệ thống chết. | High Availability (HA) - 1 node chết, các node khác tự động gánh traffic. |

---

### 1.2. Stateless vs Stateful Architecture

```
Stateless Architecture:
                       ┌──────────────┐
                 ┌────►│ App Server 1 │─────┐
┌─────────┐      │     └──────────────┘     │      ┌───────────────┐
│ Client  ├──────┼────►┌──────────────┐     ├─────►│ Shared State  │
└─────────┘      │     │ App Server 2 │     │      │ (Redis/DB)    │
  (Requests      │     └──────────────┘     │      └───────────────┘
   any server)   └────►┌──────────────┐     │
                       │ App Server 3 │─────┘
                       └──────────────┘

Stateful Architecture:
┌─────────┐            ┌──────────────┐ (Session User A)
│ Client A├───────────►│ App Server 1 │ ◄── Sticky Session
└─────────┘            └──────────────┘
┌─────────┐            ┌──────────────┐ (Session User B)
│ Client B├───────────►│ App Server 2 │
└─────────┘            └──────────────┘
```

* **Stateless (Phi trạng thái)**:
  * Server không lưu bất kỳ thông tin phiên (session data) hay trạng thái client nào giữa các requests.
  * Mọi request gửi lên đều độc lập và mang đầy đủ context cần thiết (e.g., JWT Token) hoặc trạng thái được đẩy xuống một Shared State Store tập trung (Redis cluster, DB cluster).
  * **Lợi ích**: Tự do scale out/in bằng Auto Scaling Group; bất kỳ server nào cũng có thể xử lý bất kỳ request nào.
* **Stateful (Có trạng thái)**:
  * Server lưu giữ state nội bộ (e.g., HTTP Session trong local memory, Socket connection state, In-memory gaming room state).
  * Đòi hỏi cơ chế **Sticky Session (Session Affinity)** trên Load Balancer để map cố định user về 1 server.
  * **Nhược điểm**: Khó scale out, failover phức tạp (máy chết -> mất session của user đang kết nối).

---

### 1.3. Load Balancing (Cân bằng tải)

Load Balancer (LB) là thành phần đứng giữa Client và Server pool, phân phối lưu lượng truy cập đều ra các máy chủ backend nhằm tối đa hóa throughput, giảm latency và tránh quá tải cho một node đơn lẻ.

#### Phân loại Layer 4 (L4) vs Layer 7 (L7) Load Balancer

```
Client ──► [ Layer 4 LB ] ──(TCP Passthrough)──► Backend Server
             (Chỉ nhìn thấy IP, Port)

Client ──► [ Layer 7 LB ] ──(HTTP Inspection)──► Route: /api/v1/users ──► User Service
             (Đọc URL, Headers, Cookies)  ──► Route: /api/v1/orders ──► Order Service
```

| Tiêu chí | Layer 4 Load Balancer (Transport Layer) | Layer 7 Load Balancer (Application Layer) |
| :--- | :--- | :--- |
| **Giao thức** | TCP, UDP. | HTTP, HTTPS, gRPC, WebSocket. |
| **Thông tin định tuyến** | Dựa trên IP nguồn/đích và Port (`IP:Port`). Không giải mã payload. | Dựa trên URL path, HTTP Header, Cookie, HTTP Method, Content type. |
| **SSL/TLS** | TCP Passthrough (không giải mã TLS). | SSL/TLS Termination (giải mã TLS tại LB để inspect payload). |
| **Hiệu năng & Resource** | Cực nhanh, tốn ít CPU/RAM vì chỉ xử lý packet forwarding ở kernel space (NAT/DSR). | Chậm hơn, tốn nhiều CPU/RAM hơn vì phải parse HTTP packet và decrypt TLS. |
| **Công nghệ tiêu biểu** | AWS NLB (Network Load Balancer), HAProxy (TCP mode), IPVS, LVS, F5 BIG-IP. | AWS ALB (Application Load Balancer), NGINX, HAProxy (HTTP mode), Envoy, Traefik. |

#### Các thuật toán Load Balancing phổ biến

1. **Round Robin**: Luân chuyển tuần tự qua danh sách server (`S1 -> S2 -> S3 -> S1...`). Phù hợp khi các server có cấu hình phần cứng đồng đều và các request tốn tài nguyên tương đương.
2. **Weighted Round Robin**: Mỗi server có một trọng số (weight) tỷ lệ với năng lực phần cứng. Server mạnh nhận nhiều request hơn.
3. **Least Connections**: Gửi request tới server đang có số lượng active TCP connections ít nhất. Rất hiệu quả cho các kết nối dài (WebSocket, SQL query nặng, file download).
4. **Weighted Least Connections**: Kết hợp số active connections hiện tại với trọng số server.
5. **IP Hash**: Dùng `hash(Client_IP) % N` để map cố định client về 1 server. Giúp duy trì session thô sơ, nhưng dễ bị mất cân bằng khi có nhiều user đứng sau một NAT Gateway/Proxy lớn.
6. **Consistent Hashing**: Dùng vòng tròn băm (Hash Ring). Khi thêm hoặc bớt server, chỉ có `K/N` keys phải migrate thay vì rehash toàn bộ (chi tiết ở [Mục 11](#11-distributed-system-design-patterns)).

---

### 1.4. Forward Proxy vs Reverse Proxy

```
Forward Proxy (Bảo vệ / Đại diện cho Client):
[ Client 1 ] ──┐
[ Client 2 ] ──┼──► [ Forward Proxy ] ─────Internet─────► [ Web Server ]
[ Client 3 ] ──┘    (Anonymity, Caching, Corporate Firewall)

Reverse Proxy (Bảo vệ / Đại diện cho Server):
[ Client ] ─────Internet─────► [ Reverse Proxy ] ──┬──► [ Backend Srv 1 ]
                               (Load Balancer,     ├──► [ Backend Srv 2 ]
                                SSL, WAF, Cache)   └──► [ Backend Srv 3 ]
```

* **Forward Proxy**: Nằm phía trước client. Ẩn danh tính của client (IP), lọc traffic ra ngoài (Content filtering tại trường học/doanh nghiệp), bypass geo-blocking.
* **Reverse Proxy**: Nằm phía trước backend servers. Ẩn topology và IP nội bộ của backend, đóng vai trò Load Balancer, SSL Termination, Caching static assets, WAF (Web Application Firewall), Rate limiting.

---

### 1.5. Auto-Scaling Strategies

1. **Reactive / Target Tracking Scaling**: Tự động scale dựa trên ngưỡng metric thực tế (e.g., Target Average CPU Utilization = 70%, Request Count per Target = 1000 req/s).
2. **Scheduled Scaling**: Tự động scale theo lịch biết trước (e.g., Tăng số lượng instance vào 9:00 AM các ngày Flash Sale, giảm vào nửa đêm).
3. **Predictive / ML Scaling**: Dùng machine learning phân tích chu kỳ lịch sử của traffic để scale trước khi traffic bùng nổ (lead time của EC2 boot ~ 2-5 phút).
4. **Cooldown Period**: Khoảng thời gian đóng băng giữa các lần scale liên tiếp để tránh tình trạng "thrashing" (liên tục bật tắt instance vì spike ngắn hạn).

✅ **Vì sao thực tế thường "scale up trước, scale out khi cần":** scale up không đổi kiến trúc, rẻ về công sức; nhưng tầng **app** nên được thiết kế stateless ngay từ đầu để scale out gần như miễn phí. Phần khó thật sự là tầng **dữ liệu** (stateful) — đó là lý do cache, replica, sharding (chương 2, 6) tồn tại.

> 🛠 **Áp dụng:**
> - Không lưu session/file upload trên local disk của pod; dùng Redis/JWT và object storage.
> - Load balancer cần health check để tự loại instance hỏng; deploy rolling để không downtime.
> - Auto-scale theo chỉ số phản ánh **tải thật** (CPU, request/s, độ dài hàng đợi), và nhớ downstream (DB) cũng phải chịu được số instance tăng lên (connection pool × số pod).

<details><summary>🧠 Tự kiểm tra</summary>

1. **Thêm từ 10 lên 50 pod, mỗi pod HikariCP 20 connection — chuyện gì xảy ra với DB?**
   → Từ 200 lên 1.000 connection tới DB → có thể vượt `max_connections`, DB tốn RAM/context switch. Scale out app phải tính cả giới hạn tầng dữ liệu (dùng PgBouncer/proxy, giảm pool mỗi pod).
2. **Sticky session giải quyết gì và gây hại gì?**
   → Cho phép app stateful hoạt động sau LB, nhưng phân tải lệch, mất session khi instance chết, khó scale in — chỉ là giải pháp tạm.
</details>


---

## 2. Caching & CDN

> 🎯 **Bài toán:** 90% request đọc lại cùng một phần nhỏ dữ liệu; mỗi lần đọc đều vào DB (ms, I/O đĩa) → DB là nút cổ chai và latency cao. Người dùng ở xa server → mỗi round-trip mất hàng trăm ms.
>
> 💡 **Ý tưởng:** Đặt bản sao dữ liệu ở nơi **nhanh hơn** (RAM — Redis) hoặc **gần người dùng hơn** (CDN edge). Cái giá luôn là **dữ liệu có thể cũ** + bài toán **vô hiệu hóa** ("there are only two hard things: cache invalidation and naming things").


Caching là kỹ thuật lưu tạm dữ liệu thường xuyên được truy cập vào bộ nhớ tốc độ cao (In-memory RAM) để giảm I/O xuống Disk, giảm tải cho Database và hạ thấp response time (từ hàng chục ms xuống microsecond).

### 2.1. Các chiến lược ghi Cache (Cache Writing Policies)

```
1. Cache-Aside (Lazy Loading):
   Read:  App ──1. Read Cache──► Cache (Miss)
          App ──2. Query DB────► Database
          App ──3. Write Cache─► Cache
   Write: App ──1. Update DB───► Database
          App ──2. Invalidate──► Cache (Evict)

2. Write-Through:
   Write: App ──1. Write Data──► Cache ──2. Sync Write──► Database (Atomic)

3. Write-Behind (Write-Back):
   Write: App ──1. Write Data──► Cache (Ack ngay)
                                  │
                                  └── Async Batch Write ──► Database

4. Write-Around:
   Write: App ──1. Write Data───────────────────────────► Database (Bypass Cache)
   Read:  Cache-Aside flow khi đọc sau đó
```

| Pattern | Cơ chế hoạt động | Ưu điểm | Nhược điểm | Use Case phù hợp |
| :--- | :--- | :--- | :--- | :--- |
| **Cache-Aside (Lazy Loading)** | Ứng dụng tự quản lý: Đọc cache -> miss -> đọc DB -> ghi cache. Khi ghi: cập nhật DB rồi xóa (evict) key trong cache. | Chỉ cache những data thực sự được query; Cache sập hệ thống vẫn chạy (fallback DB). | Cache miss penalty ở lần đọc đầu tiên; nguy cơ stale data nếu update DB thành công nhưng evict cache fail. | Hệ thống thông dụng, Read-heavy workload (User Profile, Article). |
| **Read-Through** | Giống cache-aside nhưng **cache layer/thư viện** tự gọi loader đọc DB khi miss (vd Caffeine `LoadingCache`, Spring `@Cacheable`). | Code ứng dụng gọn, gom logic tải dữ liệu 1 chỗ. | Như cache-aside. | Kết hợp với write-through. |
| **Write-Through** | Ứng dụng ghi vào Cache, Cache layer chịu trách nhiệm ghi đồng bộ (sync) vào DB trước khi trả về OK. | Cache và DB nhất quán hơn nhiều (nhưng 2 lần ghi **không atomic** — 1 bên lỗi vẫn có thể lệch nếu không có cơ chế bù). | Write latency cao vì phải chờ cả 2 lớp ghi xong; ghi cả data có thể không bao giờ được đọc lại. | Ứng dụng yêu cầu tính nhất quán cao, không chấp nhận mất dữ liệu (Banking, Order ledger). |
| **Write-Behind (Write-Back)** | Ứng dụng ghi vào Cache và nhận phản hồi thành công ngay lập tức. Cache tích lũy và ghi bất đồng bộ (async batch) xuống DB. | Write throughput cực nhanh, giảm tải write trực tiếp cho DB, tối ưu batch I/O. | Nguy cơ mất mát dữ liệu (Data Loss) nếu cache node crash trước khi kịp flush data xuống DB. | Tác vụ write bùng nổ, chấp nhận eventual consistency (Click tracking, View count, IoT sensor data). |
| **Write-Around** | Ghi thẳng vào Database, hoàn toàn bỏ qua (bypass) cache. | Không làm ô nhiễm cache bằng những data ghi 1 lần và không bao giờ đọc lại. | Lần đọc đầu tiên sau khi ghi chắc chắn bị Cache Miss. | Hệ thống ghi log, archive file, user upload video một lần. |

> ⚠️ **Race condition kinh điển của Cache-Aside** — tại sao là "update DB rồi **xóa** cache" chứ không phải "update cache"?
> - Nếu **update cache** sau khi update DB: 2 request ghi song song có thể ghi DB theo thứ tự A→B nhưng ghi cache theo thứ tự B→A → cache giữ giá trị cũ vĩnh viễn (tới khi hết TTL).
> - Ngay cả "xóa cache" vẫn có cửa sổ hẹp: Reader miss → đọc DB (giá trị cũ) → Writer update DB + xóa cache → Reader ghi giá trị cũ vào cache. Xác suất thấp (đọc DB thường nhanh hơn ghi) nhưng có thể xảy ra.
> - Giảm thiểu: luôn có **TTL**; **delayed double delete** (xóa cache, update DB, sau vài trăm ms xóa lần nữa); hoặc chuẩn nhất: **invalidate bằng CDC** (Debezium đọc binlog/WAL → xóa key) để thứ tự invalidation bám đúng thứ tự commit; dữ liệu cực nhạy (số dư) thì không cache hoặc đọc thẳng DB.
> - Xóa cache phải **sau khi transaction commit**, không phải trong transaction.

---

### 2.2. CDN (Content Delivery Network) & Edge Caching

CDN là mạng lưới các server phân tán địa lý trên toàn cầu (gọi là PoPs - Points of Presence), lưu cache các static assets (hình ảnh, video, CSS, JS, HTML) hoặc dynamic API responses ở vị trí gần người dùng nhất để giảm Network Latency (RTT) và offload traffic cho Origin Server.

```
                  ┌─────────────────────────────────────────┐
                  │              Origin Server              │
                  └────────────────────┬────────────────────┘
                                       │
                ┌──────────────────────┴──────────────────────┐
                ▼                                             ▼
       ┌─────────────────┐                           ┌─────────────────┐
       │   Edge CDN US   │                           │   Edge CDN EU   │
       └────────┬────────┘                           └────────┬────────┘
                │                                             │
         ┌──────┴──────┐                               ┌──────┴──────┐
         ▼             ▼                               ▼             ▼
    [ User NYC ]  [ User LA ]                     [ User London ] [ User Berlin ]
```

* **Push CDN**:
  * Content creator chủ động upload trực tiếp nội dung mới/cập nhật lên CDN mỗi khi có thay đổi.
  * *Ưu điểm*: Nội dung luôn sẵn sàng tại Edge, không có Cache Miss penalty cho user.
  * *Nhược điểm*: Tốn dung lượng CDN cho cả nội dung ít người xem; phức tạp trong việc sync file.
  * *Use case*: Website cập nhật phiên bản mới, game download patch dung lượng lớn.
* **Pull CDN**:
  * CDN không giữ file ban đầu. Khi user request lần đầu tới Edge -> Edge bị Cache Miss -> Edge tự "pull" file từ Origin Server về, trả về cho user và lưu cache lại theo HTTP Header `Cache-Control: max-age=...`.
  * *Ưu điểm*: Tiết kiệm chi phí lưu trữ CDN; chỉ cache nội dung được xem; cài đặt đơn giản.
  * *Nhược điểm*: User đầu tiên tại mỗi PoP chịu độ trễ tải từ Origin.

---

### 2.3. Redis vs Memcached

| Tiêu chí | Redis (Remote Dictionary Server) | Memcached |
| :--- | :--- | :--- |
| **Data Structures** | Strings, Hashes, Lists, Sets, Sorted Sets (ZSet), Bitmaps, HyperLogLog, Geospatial, Streams. | Chỉ hỗ trợ Key-Value dạng thuần (String / Binary Blob). |
| **Threading Model** | Single-threaded Event Loop (I/O multiplexing với epoll) cho core execution (từ Redis 6.0 có multi-thread cho network I/O). | Multi-threaded native architecture, tận dụng tối đa multi-core CPU. |
| **Persistence** | Hỗ trợ RDB (Snapshot) và AOF (Append-Only File) xuống Disk. | Pure In-Memory. Khởi động lại là mất toàn bộ dữ liệu. |
| **Replication & HA** | Redis Sentinel (Auto Failover) và Redis Cluster (Sharding tự động 16,384 hash slots). | Không hỗ trợ native replication. Phải quản lý sharding ở Client-side (Consistent Hashing). |
| **Pub/Sub & Lua Scripting** | Hỗ trợ đầy đủ Pub/Sub, Lua Scripting (Atomic execution), Transaction (MULTI/EXEC). | Không hỗ trợ. |
| **Bộ nhớ tối ưu** | Tốn RAM hơn do lưu trữ metadata phong phú của các kiểu dữ liệu phức tạp. | Cực kỳ tiết kiệm RAM cho key-value đơn giản nhờ cơ chế Slab Allocator chống phân mảnh bộ nhớ. |
| **Eviction khi đầy RAM** | Cấu hình `maxmemory-policy`: `noeviction` (mặc định — báo lỗi khi ghi), `allkeys-lru`, `allkeys-lfu`, `volatile-lru/lfu/ttl` (chỉ key có TTL), `allkeys-random` | LRU theo từng slab class |

> 📌 **Giấy phép & các bản fork**: năm 2024 Redis đổi license từ BSD sang RSALv2/SSPL → Linux Foundation fork thành **Valkey** (được AWS, Google Cloud dùng cho dịch vụ managed); Redis 8 (2025) bổ sung lựa chọn license **AGPLv3**. Về giao thức/lệnh, Valkey và Redis 7.2 tương thích; ngoài ra có **Dragonfly**, **KeyDB** (đa luồng, tương thích Redis).
>
> 📌 **Redis persistence**: RDB (snapshot định kỳ bằng `fork()` + copy-on-write — nhanh khi restart nhưng mất dữ liệu từ lần snapshot cuối) vs AOF (log mọi lệnh ghi; `appendfsync everysec` mất tối đa ~1s) → production thường bật cả hai. Redis **không** phải database bền vững tuyệt đối — replication là bất đồng bộ (`WAIT` chỉ giảm rủi ro).

---

### 2.4. Các vấn đề kinh điển trong Caching (Cache Pitfalls)

```
1. Cache Penetration:
   Request: Key KHÔNG TỒN TẠI (ID=-9999) ──► Cache (Miss) ──► DB (Miss, rỗng)
   → Hacker spam 1M requests ID rác → sập DB!
   Giải pháp: Bloom Filter phía trước Cache HOẶC Cache Null Object với TTL ngắn.

2. Cache Avalanche (Tuyết lở):
   10,000 keys cùng hết hạn (TTL) đúng 00:00:00 ──► Đồng loạt Miss ──► 10,000 queries dội thẳng DB
   Giải pháp: TTL Jitter (TTL = Base_TTL + Random(1..300s)).

3. Cache Stampede / Thundering Herd:
   Key "Hot_Trending_News" (100k QPS) vừa hết hạn TTL
   ──► 10,000 threads cùng thấy Miss ──► Cùng chạy query nặng vào DB để build cache
   Giải pháp: Distributed Mutex Lock (Redis SETNX) HOẶC XFetch / Probabilistic Early Expiration.
```

#### Bảng tổng hợp giải pháp các lỗi Caching

| Hiện tượng | Nguyên nhân | Hậu quả | Giải pháp xử lý triệt để |
| :--- | :--- | :--- | :--- |
| **Cache Penetration** | Query tìm các key **chắc chắn không tồn tại** ở cả Cache lẫn Database (do scan lỗi hoặc attacker cố tình tấn công). | Mọi request đều xuyên thủng cache đâm thẳng vào Database, gây quá tải I/O. | 1. **Bloom Filter**: Đặt trước cache để kiểm tra nhanh. Nếu Bloom filter báo không có -> từ chối ngay lập tức.<br>2. **Cache Null Object**: Lưu `key: null` với TTL ngắn (30s - 2 phút) vào cache. |
| **Cache Avalanche** | Hàng loạt key quan trọng **hết hạn (expire) cùng một thời điểm**, hoặc server Cache bị restart. | Lượng query khổng lồ dội thẳng vào Database cùng một giây khiến DB sập hàng loạt. | 1. **Randomize TTL (TTL Jitter)**: `TTL = 3600 + random(0, 300)` để phân tán thời điểm expire.<br>2. **Cache Cluster High Availability**: Setup Redis Sentinel/Cluster Multi-AZ để chống sập hạ tầng. |
| **Cache Stampede (Thundering Herd)** | Một key có lượng truy cập cực lớn (**Hot Key**) bị hết hạn, hàng ngàn request đồng thời phát hiện miss. | Cả ngàn thread cùng lúc query DB và tính toán lại dữ liệu đắt đỏ cho cùng 1 key. | 1. **Distributed Mutex Lock (Mutex)**: Chỉ thread đầu tiên lấy được lock (`SET key value NX EX 5`) mới được query DB; các thread khác sleep và retry.<br>2. **Pre-emptive Refresh / Background Thread**: Dùng cron job hoặc async worker chủ động cập nhật key trước khi TTL chạm mốc 0. |
| **Hot Key Problem** | Một key duy nhất (e.g., Idol livestream, Flash Sale Item) nhận hàng triệu request/s, vượt quá giới hạn throughput của 1 Redis node. | Redis node chứa key đó bị bão hòa CPU/Network bandwidth. | 1. **Local In-Memory Cache (Multi-level)**: Lưu bản copy key đó tại local memory của App Server (Caffeine Cache trong Java, Guava Cache).<br>2. **Key Splitting**: Phân tán key thành `key_1`, `key_2`, ... `key_N` và random access. |

✅ **Vì sao Cache-Aside + TTL + xóa (không cập nhật) cache là mặc định:** cache sập thì hệ thống vẫn chạy (đọc DB); chỉ cache dữ liệu thực sự được đọc; "xóa" tránh được race ghi đè giá trị cũ; TTL là lưới an toàn cuối cùng cho mọi lỗi invalidation.

> 🛠 **Áp dụng:**
> - Trước khi cache, hỏi: dữ liệu **chịu cũ được bao lâu**? Không chịu được (số dư, tồn kho khi đặt hàng) → không cache hoặc chỉ cache để hiển thị, quyết định cuối vẫn đọc DB.
> - Luôn đặt TTL + jitter (chống avalanche), cache cả kết quả rỗng (chống penetration), khóa/`sync` khi miss key nóng (chống stampede) — 2.4.
> - Nội dung tĩnh và API công khai cache được → CDN với `Cache-Control` đúng.

<details><summary>🧠 Tự kiểm tra</summary>

1. **Hit ratio 90% nghe có vẻ tốt. Từ 90% lên 99% giảm tải DB bao nhiêu lần?**
   → Lượng request vào DB giảm từ 10% xuống 1% → **10 lần**. Hit ratio gần 100% mới là nơi lợi ích lớn nhất.
</details>


---

## 3. Message Queue & Event-Driven Architecture

> 🎯 **Bài toán:** Đặt hàng xong phải gửi email, cập nhật kho, tính điểm thưởng, ghi analytics. Gọi đồng bộ tất cả: request chậm bằng tổng các bước, một service phụ chết làm hỏng cả việc đặt hàng, tải đột biến dồn thẳng vào service yếu nhất.
>
> 💡 **Ý tưởng:** Đặt **hàng đợi/log bền vững** ở giữa: bên gửi ghi message rồi trả lời ngay, bên nhận xử lý theo khả năng của mình. Được: **tách rời** (decoupling), **hấp thụ tải đột biến** (buffer), **thêm consumer mới không sửa producer**. Mất: **nhất quán cuối cùng**, phải xử lý **trùng lặp, thứ tự, message lỗi**, khó debug hơn.


Trong kiến trúc phân tán, Message Queue (MQ) và Event Streaming Platforms đóng vai trò là "chất kết dính" giúp tách rời (decoupling) các service, chuyển đổi giao tiếp đồng bộ (synchronous blocking) sang bất đồng bộ (asynchronous non-blocking), làm mịn lưu lượng truy cập (traffic leveling / peak shaving) và tăng khả năng chịu lỗi.

### 3.1. Message Queue vs Event Stream vs Pub/Sub

```
Message Queue (Point-to-Point / Work Queue):
[ Producer ] ──► [ Queue: Job1, Job2, Job3 ] ──► [ Consumer 1 ] (Nhận Job1)
                                             ──► [ Consumer 2 ] (Nhận Job2)
(Mỗi message chỉ được xử lý bởi DUY NHẤT 1 Consumer, sau đó bị xóa khỏi Queue)

Event Stream / Pub-Sub (Log-centric):
                 ┌── Topic: OrderCreated ──┐
[ Producer ] ──► │ Offset: 0, 1, 2, 3, 4   │
                 └────────────┬────────────┘
          ┌───────────────────┴───────────────────┐
          ▼                                       ▼
[ Consumer Group: Email ]               [ Consumer Group: Inventory ]
  (Đọc tại Offset 3)                      (Đọc tại Offset 4)
(Message được lưu vĩnh viễn trên Append-Only Log, nhiều nhóm Consumer đọc độc lập)
```

---

### 3.2. Apache Kafka Deep Dive

Apache Kafka là một distributed event store và stream-processing platform có throughput cực cao nhờ kiến trúc Append-Only Log và tối ưu I/O phần cứng.

```
Kafka Cluster
┌──────────────────────────────────────────────────────────────────┐
│ Topic: "payment-events" (Replication Factor = 3)                 │
│                                                                  │
│  Partition 0:                                                    │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┐        │
│  │ Offset 0 │ Offset 1 │ Offset 2 │ Offset 3 │ Offset 4 │ ...    │
│  └──────────┴──────────┴──────────┴──────────┴──────────┘        │
│  [Broker 1: Leader]  [Broker 2: Follower]  [Broker 3: Follower]  │
│                                                                  │
│  Partition 1:                                                    │
│  [Broker 2: Leader]  [Broker 3: Follower]  [Broker 1: Follower]  │
└──────────────────────────────────────────────────────────────────┘
```

#### Core Components & Cơ chế vận hành

1. **Broker & Controller**: Node trong cluster. Controller quản lý metadata, phát hiện broker crash và bầu chọn Leader cho các partition. Metadata được quản lý bằng **KRaft** (Kafka Raft — controller quorum riêng); **Kafka 4.0 (3/2025) đã loại bỏ hoàn toàn ZooKeeper** — cluster cũ phải migrate sang KRaft (qua Kafka 3.9) trước khi nâng lên 4.x.
2. **Topic & Partition**:
   * Topic là logic stream phân loại message.
   * Topic được chia nhỏ thành nhiều **Partitions** nằm rải rác trên các Broker khác nhau để mở rộng song song (Parallelism).
   * **Partition Key**: Producer gửi message kèm `Key`. Kafka dùng `murmur2(Key) % Number_Of_Partitions` để định tuyến. Tất cả message có cùng Key vào cùng một Partition -> **Đảm bảo thứ tự theo Key** (với điều kiện số partition không đổi và producer bật idempotence để retry không đảo thứ tự).
   * **Mỗi partition = 1 log append-only** chia thành các **segment** file (`.log` + `.index` + `.timeindex`); dữ liệu cũ bị xóa theo **retention** (thời gian/dung lượng) hoặc **log compaction** (chỉ giữ bản ghi mới nhất cho mỗi key — dùng cho changelog/state, vd `__consumer_offsets`).
3. **Producer Acks & Độ bền dữ liệu (Durability)**:
   * `acks = 0`: Producer bắn message và không chờ phản hồi. Nhanh nhất, nguy cơ mất dữ liệu cao nhất.
   * `acks = 1`: Producer chờ duy nhất Partition Leader ghi vào local log thành công là trả về OK. Mất data nếu Leader chết trước khi sync sang Follower.
   * `acks = all` (hoặc `-1`): Leader chờ **toàn bộ các bản sao trong ISR (In-Sync Replicas)** ghi thành công rồi mới phản hồi OK. Cấu hình bền vững chuẩn: `replication.factor = 3` + `min.insync.replicas = 2` + `acks = all` + `unclean.leader.election.enable = false` → chịu được mất 1 broker mà không mất dữ liệu đã ack. (Kafka ack khi dữ liệu vào **page cache** của đủ replica, không fsync từng message — độ bền dựa vào replication chứ không phải fsync.)
4. **Consumer Group & Rebalancing**:
   * Nhiều consumer cùng thuộc 1 `group.id` sẽ chia nhau các partition trong Topic (1 partition tại 1 thời điểm chỉ được đọc bởi 1 consumer trong cùng 1 group).
   * Số consumer > số partition -> Consumer thừa sẽ ở trạng thái idle (ngồi chơi).
   * **Rebalance**: Khi có consumer join/leave group, Kafka phân phối lại partition cho các consumer còn lại. Offset đã xử lý được commit vào topic nội bộ `__consumer_offsets`.
   * **Consumer lag** (offset mới nhất − offset đã commit) là metric quan trọng nhất để autoscale consumer và phát hiện tắc nghẽn.
5. **Tại sao Kafka có Throughput khổng lồ?**
   * **Sequential Disk I/O**: Ghi append-only vào cuối file log — I/O tuần tự nhanh hơn I/O ngẫu nhiên hàng trăm–hàng nghìn lần trên HDD (và vẫn nhanh hơn rõ rệt trên SSD).
   * **Page Cache OS**: Tận dụng kernel memory cache của hệ điều hành thay vì allocate JVM Heap (tránh GC pause).
   * **Zero-Copy Network Transfer**: Sử dụng Linux system call `sendfile()` (Java `FileChannel.transferTo`), chuyển data trực tiếp từ Page Cache ra Network Socket buffer mà không cần copy qua User Space memory (không áp dụng khi bật TLS vì dữ liệu phải mã hóa ở user space). Xem `Java.md` 15.2.
   * **Batching & Compression**: Gom nhóm nhiều message thành 1 batch nén (Snappy, Zstandard, Gzip) trước khi gửi qua mạng.

---

### 3.3. RabbitMQ (AMQP Protocol)

RabbitMQ hoạt động dựa trên mô hình định tuyến linh hoạt của chuẩn AMQP 0-9-1: `Producer -> Exchange -> Binding -> Queue -> Consumer`.

```
                  ┌───────────────────┐
                  │ Exchange (Direct) │ ──(routing_key='error')──► [ Queue Errors ] ──► Worker
                  └─────────┬─────────┘
                            │
                   (routing_key='info')
                            ▼
                    [ Queue General ] ──► Worker
```

#### 4 Loại Exchange chính trong RabbitMQ:
1. **Direct Exchange**: Định tuyến message tới Queue có `Binding Key` khớp chính xác 100% với `Routing Key` của message.
2. **Fanout Exchange**: Bỏ qua routing key, broadcast (nhân bản) message tới **tất cả** các Queue đang bind vào Exchange đó (Pub/Sub pattern).
3. **Topic Exchange**: Định tuyến dựa trên wildcard pattern (`*` khớp đúng 1 từ, `#` khớp 0 hoặc nhiều từ). Ví dụ: routing key `order.eu.created` khớp với binding `order.*.created` hoặc `order.#`.
4. **Headers Exchange**: Định tuyến dựa trên các key-value trong message header thay vì routing key.

#### Độ tin cậy trong RabbitMQ
- **Publisher confirms** (broker xác nhận đã nhận/lưu message) + **consumer manual ack** (`basic.ack` sau khi xử lý; `nack/reject` để requeue hoặc chuyển **Dead Letter Exchange**).
- **Prefetch (QoS)**: giới hạn số message chưa ack mà 1 consumer giữ → tránh 1 consumer ôm hết.
- **Quorum Queues** (3.8+, dựa trên **Raft**): queue được replicate an toàn — thay thế classic mirrored queues (đã bị loại bỏ ở RabbitMQ 4.0). **Streams** (3.9+): log append-only kiểu Kafka, cho phép replay.
- Delayed message: TTL + DLX, hoặc plugin `rabbitmq-delayed-message-exchange`.

---

### 3.4. So sánh các Message Broker phổ biến

| Tiêu chí | Apache Kafka | RabbitMQ | AWS SQS | Redis Streams |
| :--- | :--- | :--- | :--- | :--- |
| **Mô hình** | Distributed Append-Only Log. | Smart Broker / Dumb Consumer (Queue). | Fully Managed Cloud Queue. | In-Memory Append-Only Log. |
| **Throughput** | Cực cao (> 1M msg/s per cluster). | Trung bình (~50k - 100k msg/s). | Tự động scale theo request cloud. | Cực cao (In-Memory speed). |
| **Độ trễ (Latency)** | Thấp (~ vài milliseconds). | Cực thấp (< 1 millisecond). | Trung bình (10 - 50ms). | Siêu thấp (< 1ms). |
| **Lưu trữ & Replay** | Lưu vĩnh viễn trên Disk theo Retention time (cho phép Consumer replay lại log cũ). | Message bị xóa ngay sau khi Consumer Ack thành công. | Message bị xóa sau khi Consumer xử lý và xóa khỏi queue. | Lưu trên RAM (có thể cấu hình trim maxlen). |
| **Đảm bảo thứ tự** | Thứ tự nghiêm ngặt trên từng Partition. | Đảm bảo FIFO cơ bản trên 1 Queue. | SQS Standard không đảm bảo; SQS FIFO hỗ trợ nhưng throughput thấp hơn. | Đảm bảo theo ID tuần tự trên Stream. |
| **Use Case chính** | Event Sourcing, Log Aggregation, Clickstream analytics, High-volume Microservices. | Transactional Messaging, Complex Routing, Background Tasks, Job Queues. | Serverless cloud-native apps trên AWS, decoupling đơn giản. | Real-time chat, in-memory activity stream, gaming events. |

---

### 3.5. Event Sourcing & CQRS Pattern

```
Command Side (Write):
[ User Action ] ──► [ Command Handler ] ──► Append to [ Event Store ]
                                                          │ (Emit Event)
                                                          ▼
Query Side (Read):                                   [ Event Bus ]
[ Client Query ] ◄── [ Read DB (Elastic/Redis) ] ◄── [ Projector ]
```

* **Event Sourcing**:
  * Thay vì chỉ lưu trạng thái hiện tại (State) của entity trong DB (dòng `balance = 500$`), hệ thống lưu trữ **toàn bộ chuỗi các sự kiện thay đổi bất biến (Immutable Events)** theo thời gian (`Created(0$) -> Deposited(+1000$) -> Withdrawn(-500$)`).
  * Trạng thái hiện tại được tái tạo bằng cách replay lại chuỗi event từ đầu (hoặc từ Snapshot gần nhất).
  * *Ưu điểm*: Cung cấp Audit Trail 100% hoàn hảo, hỗ trợ Time-travel debugging, khôi phục data về bất kỳ thời điểm nào trong quá khứ.
* **CQRS (Command Query Responsibility Segregation)**:
  * Tách biệt hoàn toàn model xử lý ghi dữ liệu (**Command**: INSERT/UPDATE/DELETE) và model xử lý đọc dữ liệu (**Query**: SELECT).
  * Command DB tối ưu cho ghi nhanh và toàn vẹn dữ liệu (Normalized relational DB).
  * Read DB tối ưu cho truy vấn phức tạp và tìm kiếm (Denormalized Elasticsearch, Read-replica, Redis).
  * Dữ liệu được đồng bộ từ Command side sang Query side bất đồng bộ qua Message Queue/CDC (Change Data Capture).

---

### 3.6. Transactional Outbox Pattern & Distributed Transactions

Trong Microservices, việc thực hiện đồng thời: (1) Cập nhật Database và (2) Bắn message vào Message Queue trong cùng 1 business flow rất dễ dẫn đến mất nhất quán dữ liệu (DB cập nhật nhưng Kafka sập -> message mất; hoặc bắn Kafka xong DB rollback -> Ghost message).

```
Transactional Outbox Pattern:
┌────────────────────────────────────────────────────────┐
│ Order Service                                          │
│                                                        │
│  Begin Local ACID Transaction                          │
│  ├── 1. INSERT INTO orders (id, total, status...)      │
│  └── 2. INSERT INTO outbox_table (id, event_payload...)│
│  Commit Local Transaction                              │
└──────────────────────────┬─────────────────────────────┘
                           │
             (CDC - Debezium / Polling Publisher)
                           ▼
                 [ Apache Kafka Cluster ]
```

* **Cách hoạt động**:
  1. Service lưu entity nghiệp vụ VÀ lưu message cần gửi vào bảng `outbox_table` trong **cùng 1 Local Database Transaction**.
  2. Một tiến trình riêng biệt (CDC tool như **Debezium** đọc database Transaction Log/Binlog hoặc Message Relay Worker) liên tục đọc bảng `outbox_table` và bắn message lên Kafka.
  3. Sau khi Kafka Ack thành công, bản ghi trong outbox được đánh dấu là đã gửi hoặc bị xóa.
* **Đảm bảo**: Deliver message đạt chuẩn **At-Least-Once Delivery** mà không cần cơ chế Two-Phase Commit (2PC) phân tán đắt đỏ.
* **Polling vs CDC**: Polling (`SELECT ... FROM outbox WHERE sent = false ORDER BY id LIMIT 100 FOR UPDATE SKIP LOCKED`) đơn giản nhưng tăng tải DB và có độ trễ; CDC (Debezium đọc WAL/binlog) độ trễ thấp, không tốn query nhưng vận hành phức tạp hơn (Kafka Connect).
* Bảng outbox cần được dọn định kỳ (xóa bản ghi đã gửi / partition theo ngày).

---

### 3.7. Delivery Semantics, Idempotency & Ordering

| Semantic | Ý nghĩa | Cách đạt được |
| :--- | :--- | :--- |
| **At-most-once** | Có thể mất, không bao giờ trùng | Ack/commit **trước** khi xử lý; không retry |
| **At-least-once** | Không mất, **có thể trùng** | Ack/commit **sau** khi xử lý + retry — mặc định của hầu hết hệ thống |
| **Exactly-once** | Không mất, không trùng (về **hiệu ứng**) | Thực tế = **at-least-once + idempotent consumer**; Kafka EOS (transactions) chỉ đảm bảo trong phạm vi Kafka → Kafka |

> 📌 "Exactly-once delivery" qua mạng là bất khả thi (Two Generals Problem); thứ ta đạt được là **exactly-once processing/effect** nhờ idempotency.

**Idempotent Consumer / Inbox Pattern**:
```sql
-- Trong CÙNG 1 transaction với xử lý nghiệp vụ:
INSERT INTO processed_messages (message_id, processed_at) VALUES (:msgId, now());  -- PK(message_id) → trùng thì lỗi → bỏ qua
UPDATE accounts SET balance = balance - :amount WHERE id = :accId;
```
- Hoặc dùng **thao tác tự nhiên idempotent**: `UPSERT`, `SET status = 'PAID' WHERE status = 'PENDING'`, ghi theo version (`WHERE version = :expected`).
- Message cần có **ID duy nhất ổn định** do producer sinh (không phải offset).

**Ordering**:
- Kafka: thứ tự chỉ trong 1 partition → chọn **key** đúng (orderId, userId). RabbitMQ: FIFO trong 1 queue với **1 consumer**; nhiều consumer/prefetch/requeue có thể đảo thứ tự.
- Khi phải xử lý song song mà vẫn cần thứ tự theo entity: chia luồng theo hash(key) vào N hàng đợi nội bộ (mỗi hàng đợi 1 thread).
- Consumer nhận event lệch thứ tự (ví dụ "OrderShipped" trước "OrderPaid"): dùng **version/sequence number** trong event, bỏ qua event cũ hơn trạng thái hiện tại.

**Backpressure & Dead Letter**:
- Consumer chậm hơn producer → lag tăng → scale consumer (≤ số partition), batch xử lý, hoặc giảm tốc producer.
- Message lỗi lặp lại (**poison message**) → retry có giới hạn với backoff → chuyển **DLQ/DLT** + cảnh báo + công cụ replay sau khi sửa lỗi.

✅ **Vì sao Kafka phổ biến cho event-driven giữa các service, còn RabbitMQ cho task queue:** Kafka là **log** — message được giữ lại, nhiều consumer group đọc độc lập, đọc lại được, throughput rất cao, thứ tự theo partition; RabbitMQ là **broker định tuyến** — routing linh hoạt, ack từng message, ưu tiên, delay; message bị xóa sau khi xử lý. Chọn theo câu hỏi: *cần lưu và phát lại sự kiện cho nhiều bên (Kafka) hay phân phát công việc tới worker (RabbitMQ/SQS)?*

> 🛠 **Áp dụng:**
> - Consumer **luôn idempotent** (at-least-once là mặc định thực tế).
> - Ghi DB và phát sự kiện nhất quán → **Outbox** (3.6), không gửi message trong transaction rồi "hy vọng".
> - Message lỗi lặp lại → retry có giới hạn + **DLQ** + cảnh báo; theo dõi **consumer lag**.

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao không thể đảm bảo "ghi DB rồi gửi Kafka" bằng cách bọc cả hai trong `@Transactional`?**
   → DB và Kafka là hai hệ thống, không chung transaction; commit DB thành công rồi gửi lỗi (hoặc ngược lại) → lệch. Outbox biến nó thành **một** lần ghi DB.
</details>


---

## 4. API Design & Gateways

> 🎯 **Bài toán:** Nhiều loại client (web, mobile, đối tác, service nội bộ) cần giao tiếp với hệ thống qua một **hợp đồng** ổn định, hiệu quả, an toàn trước lạm dụng.
>
> 💡 **Ý tưởng:** Chọn **kiểu API** theo người dùng API (REST cho công khai, gRPC cho nội bộ hiệu năng cao, GraphQL cho client cần linh hoạt), đặt **gateway** làm cửa ngõ chung (auth, rate limit, định tuyến), và thiết kế để **tiến hóa không phá vỡ** client cũ (versioning, thêm field không bắt buộc).


### 4.1. So sánh các kiến trúc API: REST vs GraphQL vs gRPC

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│      REST       │       │     GraphQL     │       │      gRPC       │
│  JSON over HTTP │       │  Client-defined │       │ Protobuf binary │
│  Resource-based │       │   Single POST   │       │  over HTTP/2    │
└─────────────────┘       └─────────────────┘       └─────────────────┘
```

| Tiêu chí | REST (Representational State Transfer) | GraphQL | gRPC (Google Remote Procedure Call) |
| :--- | :--- | :--- | :--- |
| **Format Payload** | JSON, XML (Text-based). | JSON (Text-based). | Protocol Buffers - Protobuf (Strict Binary). |
| **Transport Protocol**| HTTP/1.1 hoặc HTTP/2. | Chủ yếu HTTP/1.1 hoặc HTTP/2 (Single Endpoint `/graphql`). | HTTP/2 thuần túy (Hỗ trợ Multiplexing, Streaming). |
| **Schema Contract** | OpenAPI / Swagger (Tùy chọn, không bắt buộc). | Strongly Typed Schema Definition Language (SDL). | Strict Contract file `.proto` sinh code tự động đa ngôn ngữ. |
| **Over/Under-fetching**| Dễ bị (Trả về toàn bộ object hoặc phải gọi nhiều endpoint để lấy đủ quan hệ). | Giải quyết triệt để (Client chỉ định chính xác các field cần lấy). | Không bị (Schema định nghĩa chặt chẽ). |
| **Streaming** | Hạn chế (Dùng SSE / Chunked Transfer). | Hỗ trợ qua Subscriptions (WebSocket). | Native 4 cơ chế: Unary, Client-streaming, Server-streaming, Bidirectional. |
| **Hiệu năng & Network**| Trung bình (JSON overhead lớn, serialize text chậm). | Trung bình (Tốn CPU để parse query AST tại server). | Cực nhanh và nhẹ (Binary packing, serialize/deserialize cực nhanh). |
| **Use Case tối ưu** | Public API cho bên thứ 3, Web API chuẩn hóa, CRUD services. | Mobile Apps (băng thông yếu, màn hình đa dạng), Frontend tổng hợp data từ nhiều microservices (BFF). | Inter-service communication giữa các internal Microservices, IoT, High-throughput systems. |

---

### 4.2. Rate Limiting Algorithms (Thuật toán giới hạn tốc độ)

Rate Limiter bảo vệ hệ thống khỏi DoS, web scraping, brute-force attacks và ngăn chặn một client chiếm dụng toàn bộ tài nguyên hạ tầng (Noisy Neighbor).

```
1. Token Bucket:
   Tokens rơi vào Bucket với tốc độ cố định (r token/s).
   Bucket có sức chứa tối đa (Capacity = B).
   Mỗi request đến lấy đi 1 token. Nếu bucket rỗng -> REJECT 429.
   -> Hỗ trợ cho phép Request Burst (bùng nổ lưu lượng tức thời).

2. Leaky Bucket:
   Request đổ vào Bucket (Queue FIFO) với tốc độ bất kỳ.
   Bucket "rò rỉ" xử lý request với tốc độ không đổi (Constant outflow rate).
   Nếu Bucket đầy -> REJECT request mới.
   -> Làm mượt traffic (Smooth out bursts), xuất lượng ra luôn ổn định.

3. Sliding Window Log:
   Lưu timestamp của từng request vào Sorted Set (Redis ZSET).
   Khi request mới đến:
   - Xóa các timestamp cũ ngoài khoảng thời gian [now - Window_Size].
   - Đếm số lượng phần tử còn lại trong Set. Nếu < Max_Limit -> ACCEPT, ngược lại REJECT.
   -> Chính xác 100%, nhưng tốn dung lượng RAM để lưu toàn bộ timestamp.

4. Sliding Window Counter:
   Kết hợp Fixed Window và trọng số thời gian:
   Requests = (Số req ở window trước) * (Tỷ lệ thời gian gối đầu) + (Số req ở window hiện tại).
   -> Tiết kiệm bộ nhớ, độ chính xác xấp xỉ ~99%.
```

#### Code Logic: Distributed Token Bucket dùng Redis Lua Script
Dùng Redis Lua script để đảm bảo tính **Atomic** tuyệt đối giữa các cluster node:

```lua
-- KEYS[1]: token_bucket_key
-- ARGV[1]: capacity (sức chứa)
-- ARGV[2]: refill_rate (tokens per second)
-- ARGV[3]: now (current timestamp in seconds)
-- ARGV[4]: requested (số token muốn lấy, thường là 1)

local bucket = redis.call('HMGET', KEYS[1], 'tokens', 'last_refill')
local tokens = tonumber(bucket[1])
local last_refill = tonumber(bucket[2])

local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

if tokens == nil then
    tokens = capacity
    last_refill = now
else
    -- Tính lượng token hồi phục theo thời gian trôi qua
    local delta = math.max(0, now - last_refill)
    tokens = math.min(capacity, tokens + delta * refill_rate)
    last_refill = now
end

local allowed = 0
if tokens >= requested then
    tokens = tokens - requested
    allowed = 1 -- ACCEPT
end
redis.call('HSET', KEYS[1], 'tokens', tokens, 'last_refill', last_refill)   -- HMSET đã deprecated từ Redis 4
redis.call('EXPIRE', KEYS[1], math.ceil(capacity / refill_rate) * 2)        -- key tự hết hạn khi client không còn gọi
return allowed -- 0 = REJECT (429 Too Many Requests)
```
> - `now` truyền từ app server có thể lệch giữa các server → có thể lấy giờ ngay trong script bằng `redis.call('TIME')` (từ Redis 5 cho phép vì script được replicate theo hiệu ứng).
> - Trên **Redis Cluster**, mọi key trong 1 script phải cùng hash slot (dùng hash tag `{user123}:bucket`).
> - Thư viện có sẵn: Bucket4j (Java, backend Redis/Hazelcast), Resilience4j RateLimiter (cục bộ), Spring Cloud Gateway `RequestRateLimiter` (Redis), NGINX `limit_req` (leaky bucket), Envoy global rate limit service.

---

### 4.3. Pagination: Offset-based vs Cursor-based

```
Offset-based (LIMIT, OFFSET):
SELECT * FROM posts ORDER BY created_at DESC LIMIT 10 OFFSET 1000000;
──► Database phải quét 1,000,010 bản ghi trên đĩa rồi vứt bỏ 1,000,000 dòng đầu! (O(N) - Rất chậm)
──► Dễ bị nhảy trang / lặp dữ liệu khi có bài viết mới chèn vào giữa lúc user chuyển page.

Cursor-based (Keyset Pagination):
SELECT * FROM posts WHERE id < 592837 ORDER BY id DESC LIMIT 10;
──► Dùng B+Tree Index seek thẳng tới ID=592837 trong O(log N) rồi đọc tuần tự 10 bản ghi tiếp theo — chi phí không phụ thuộc "đang ở trang thứ mấy".
──► Không bị duplicate data khi có new record; hỗ trợ Infinite Scroll hoàn hảo.
```
* Cursor nên là chuỗi **opaque** (base64 của `(created_at, id)`) để client không phụ thuộc vào cấu trúc; nhược điểm: không nhảy tới trang bất kỳ, không có "tổng số trang" rẻ. Chi tiết SQL: `Database.md` 20.4.

---

### 4.4. API Gateway, BFF & các nguyên tắc thiết kế API

```
                ┌────────── Mobile BFF ──────────┐
[ Mobile ] ────►│                                │──► User Service
[ Web    ] ────►│  API Gateway (auth, rate limit,│──► Order Service
[ Partner] ────►│  routing, TLS, logging)        │──► Payment Service
                └────────── Web BFF ─────────────┘
```
* **API Gateway**: điểm vào duy nhất — TLS termination, xác thực token, rate limiting, routing theo path/version, request/response transformation, aggregation đơn giản, canary routing, gắn trace id. Ví dụ: Kong, Spring Cloud Gateway, AWS API Gateway, Envoy/Istio Gateway, NGINX.
  * ⚠️ Không nhét business logic vào gateway; gateway phải HA (nhiều instance) vì là single point of entry.
* **BFF (Backend for Frontend)**: mỗi loại client (mobile/web) có 1 lớp backend riêng gom/định dạng dữ liệu đúng nhu cầu màn hình → tránh over-fetching trên mobile, tách vòng đời phát triển của từng frontend.
* **Nguyên tắc thiết kế API hay được hỏi**:
  * Resource là danh từ số nhiều (`/orders/{id}/items`), dùng đúng HTTP method & status code; lọc/sắp xếp/phân trang qua query param.
  * **Idempotency**: `PUT/DELETE` idempotent tự nhiên; `POST` tạo tài nguyên/thanh toán nhận header `Idempotency-Key` (Case 8).
  * **Versioning**: không phá vỡ client cũ (chỉ thêm field, không đổi nghĩa/xóa field); breaking change → version mới, có lịch deprecate.
  * **Error format** thống nhất: RFC 9457 Problem Details (`type`, `title`, `status`, `detail`, `instance` + traceId).
  * **Long-running operation**: trả `202 Accepted` + URL theo dõi trạng thái (`/jobs/{id}`), hoặc callback **webhook**.
* **Webhook** (server gọi ngược client khi có sự kiện): ký HMAC payload (`X-Signature`) để bên nhận xác thực, gửi kèm event id để bên nhận dedupe, retry với backoff khi bên nhận lỗi, bên nhận trả 2xx nhanh rồi xử lý bất đồng bộ.

> 🛠 **Áp dụng:**
> - API công khai/đối tác → REST + OpenAPI; giữa service nội bộ cần hiệu năng/streaming → gRPC; nhiều màn hình mobile cần dữ liệu khác nhau → BFF hoặc GraphQL.
> - Danh sách lớn → **cursor pagination** (offset chậm dần và lệch khi dữ liệu thay đổi — 4.3).
> - Thao tác ghi có thể bị retry → **Idempotency-Key**.
> - Rate limit: **token bucket** cho phép burst ngắn và đơn giản → lựa chọn phổ biến nhất; sliding window khi cần chính xác hơn (4.2).

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao gRPC không phổ biến cho API công khai trên trình duyệt?**
   → Trình duyệt không cho điều khiển HTTP/2 frame/trailer trực tiếp → cần gRPC-Web + proxy; payload nhị phân khó debug và khó cho đối tác tích hợp.
</details>


---

## 5. Microservices Architecture & Resiliency

> 🎯 **Bài toán:** Một codebase do 100 người sửa: deploy một tính năng phải chờ cả hệ thống, một module lỗi làm sập tất cả, không thể scale riêng phần nóng.
>
> 💡 **Ý tưởng:** Tách theo **ranh giới nghiệp vụ** (bounded context) thành service độc lập: tự sở hữu dữ liệu, deploy riêng, scale riêng, team riêng.
>
> ❌ **Cái giá (rất lớn):** gọi hàm thành gọi mạng (chậm, có thể lỗi), transaction ACID một DB thành **saga**, debug cần tracing phân tán, vận hành cần CI/CD, service discovery, observability trưởng thành.
>
> ✅ **Vì sao "monolith trước" (modular monolith) thường là lựa chọn đúng:** khi team nhỏ và ranh giới nghiệp vụ chưa rõ, cái giá của phân tán lớn hơn lợi ích; tách sau khi ranh giới đã ổn định và có lý do cụ thể (scale riêng, team riêng, chu kỳ release khác nhau) — xem 5.0.


### 5.0. Monolith vs Microservices — khi nào nên tách?

| Tiêu chí | Monolith (kể cả **Modular Monolith**) | Microservices |
| :--- | :--- | :--- |
| **Triển khai** | 1 artifact, deploy cả khối | Deploy độc lập từng service |
| **Scale** | Scale cả khối | Scale riêng phần nóng |
| **Dữ liệu** | 1 DB, transaction ACID dễ dàng | **Database per service** → nhất quán qua Saga/Outbox, eventual consistency |
| **Gọi nhau** | Method call (nhanh, tin cậy) | Network call (chậm, có thể lỗi/timeout) → cần resiliency |
| **Vận hành** | Đơn giản | Cần CI/CD mạnh, observability, service discovery, K8s... |
| **Tổ chức team** | Phù hợp team nhỏ | Phù hợp nhiều team độc lập (**Conway's Law**) |

* Lời khuyên phổ biến: **bắt đầu bằng modular monolith** với ranh giới module rõ ràng (theo **DDD bounded context**), chỉ tách khi có lý do cụ thể (scale khác biệt, tần suất deploy khác biệt, team độc lập, yêu cầu công nghệ/bảo mật riêng).
* **Anti-pattern "Distributed Monolith"**: tách service nhưng vẫn dùng chung DB, phải deploy cùng lúc, gọi nhau đồng bộ thành chuỗi dài → nhận đủ nhược điểm của cả 2 bên.
* **Strangler Fig pattern**: chuyển dần từ monolith — đặt proxy/gateway phía trước, từng tính năng được viết lại thành service mới và route traffic sang, monolith co nhỏ dần.
* **Giao tiếp**: đồng bộ (REST/gRPC — đơn giản, nhưng tạo coupling về thời gian & lỗi dây chuyền) vs bất đồng bộ (event qua Kafka/RabbitMQ — lỏng lẻo, chịu lỗi tốt, nhưng eventual consistency & khó debug). Quy tắc: chuỗi gọi đồng bộ càng ngắn càng tốt; availability của chuỗi = tích availability từng service (3 service 99.9% nối tiếp ≈ 99.7%).

### 5.1. Service Discovery: Client-Side vs Server-Side

```
Client-Side Discovery:
[ Client/Service A ] ──1. Query Service Registry──► [ Service Registry ]
         │           ◄──2. Return IPs list────────┘   (Eureka / Consul)
         └──3. Load Balance & Call Directly───────► [ Service B (Node 2) ]

Server-Side Discovery:
[ Client/Service A ] ──1. Request 'http://service-b'──► [ Internal Load Balancer / Kube Proxy ]
                                                                 │ (Resolve DNS & Forward)
                                                                 ▼
                                                        [ Service B (Node 2) ]
```

* **Client-Side Discovery** (e.g., Netflix Eureka, Spring Cloud LoadBalancer):
  * Client chủ động gọi Service Registry lấy danh sách IP sống, tự chạy thuật toán LB local và gọi trực tiếp tới target service.
  * Giảm bớt 1 network hop qua LB trung gian, nhưng client phải gắn chặt thư viện discovery theo ngôn ngữ.
* **Server-Side Discovery** (e.g., Kubernetes CoreDNS + Kube-Proxy / AWS ALB):
  * Client chỉ cần gửi request tới một DNS name hoặc Router/Proxy cố định. Router tự tra cứu registry và forward request tới pod đích.
  * Trong suốt (transparent) với client, hỗ trợ mọi ngôn ngữ lập trình.

---

### 5.2. Các mẫu thiết kế chịu lỗi (Resiliency Patterns)

Hệ thống microservices với hàng trăm service phụ thuộc lẫn nhau rất dễ gặp hiện tượng **Cascading Failure** (Một service hạ nguồn phản hồi chậm -> nghẽn thread pool service thượng nguồn -> sập toàn bộ hệ thống).

```
Circuit Breaker State Machine:
              ┌────────────────────────┐
              │                        │ (Success rate >= Threshold)
              ▼                        │
       ┌──────────────┐          ┌─────┴────────┐
 ┌────►│    CLOSED    │          │  HALF-OPEN   │
 │     │ (Normal Ops) │          │(Thử nghiệm)  │
 │     └──────┬───────┘          └─────▲────────┘
 │            │ (Lỗi liên tiếp         │
 │            │  vượt ngưỡng FailRate) │ (Hết timeout thử lại)
 │            ▼                        │
 │     ┌──────────────┐                │
 └─────┤     OPEN     ├────────────────┘
       │(Chặn toàn bộ,│
       │ Fail Fast /  │
       │ Fallback)    │
       └──────────────┘
```

1. **Circuit Breaker (Cầu dao ngắt mạch)**:
   * **Closed**: Trạng thái bình thường, cho phép mọi request đi qua.
   * **Open**: Khi tỷ lệ request lỗi/timeout vượt ngưỡng (e.g., 50% trong 10s), Circuit Breaker tự ngắt mạch. Mọi request tiếp theo bị từ chối ngay lập tức (**Fail-Fast**) hoặc trả về dữ liệu dự phòng (**Fallback Response**) mà không gọi tới remote service.
   * **Half-Open**: Sau một khoảng thời gian chờ (e.g., 30s), cho phép một lượng nhỏ request thử nghiệm đi qua. Nếu thành công -> chuyển về `Closed`; nếu vẫn lỗi -> quay lại `Open`.
2. **Bulkhead Pattern (Vách ngăn tàu thủy)**:
   * Tách biệt các tài nguyên quan trọng (Thread Pool, Connection Pool, CPU quota) cho từng dependency riêng biệt.
   * Nếu Service Payment bị treo -> chỉ làm cạn kiệt Thread Pool riêng của Payment; các chức năng Search, Product Catalog vẫn có Thread Pool độc lập để hoạt động bình thường.
3. **Retry with Exponential Backoff and Jitter**:
   * Khi gọi service bị lỗi tạm thời (Transient Network Error), không retry dồn dập ngay lập tức.
   * Thời gian chờ giữa các lần retry tăng theo cấp số nhân: `Wait_Time = 2^attempt * Base_Delay`.
   * **Jitter (Nhiễu ngẫu nhiên)**: Thêm một khoảng random `+ random(0, 100ms)` vào wait time để tránh việc hàng ngàn client cùng retry chính xác tại cùng một millisecond (gây thundering herd lên server vừa hồi phục). Phổ biến: **full jitter** `wait = random(0, min(cap, base × 2^attempt))`.
   * Chỉ retry lỗi **tạm thời** và thao tác **idempotent**; giới hạn số lần; tránh retry ở **nhiều tầng** cùng lúc (retry storm: 3 tầng × 3 lần = 27 request); dùng **retry budget** (vd retry tối đa 10% tổng request).
4. **Timeout** (quan trọng nhất và hay bị quên): mọi lời gọi mạng phải có connect/read timeout; truyền **deadline** xuống các tầng dưới (gRPC deadline propagation) để không làm việc vô ích khi caller đã bỏ cuộc.
5. **Fallback / Graceful Degradation**: khi dependency phụ lỗi, trả kết quả kém hơn nhưng vẫn dùng được — dữ liệu từ cache cũ (stale-while-revalidate), giá trị mặc định, ẩn widget gợi ý sản phẩm... thay vì lỗi toàn trang.
6. **Load Shedding & Rate Limiting**: khi quá tải, **chủ động từ chối sớm** (503/429) phần request ít quan trọng thay vì để mọi request cùng chậm rồi timeout (hàng đợi dài vô ích). Kết hợp **adaptive concurrency limit** (Netflix concurrency-limits, Envoy).
7. **Health Check & Self-healing**: liveness/readiness probe, outlier detection (loại instance lỗi khỏi pool LB), auto-restart.
8. **Idempotency & Deduplication**: mọi thao tác có thể bị retry đều phải an toàn khi thực hiện lại (3.7).

---

### 5.3. Distributed Transactions: Saga Pattern

Trong Microservices, nguyên tắc **Database per Service** ngăn cản việc sử dụng Transaction ACID của cơ sở dữ liệu trên nhiều service. Saga Pattern chia nhỏ một Business Transaction lớn thành một chuỗi các Local Transactions liên tiếp.

```
1. Choreography Saga (Dựa trên Event phi tập trung):
   [ Order Service ] ──OrderCreated Event──► [ Payment Service ]
                                                   │
                                     PaymentProcessed Event
                                                   ▼
   [ Notification Srv ] ◄──StockReserved─── [ Inventory Service ]

2. Orchestration Saga (Điều phối tập trung qua Orchestrator):
                           ┌────────────────────────┐
                           │   Order Orchestrator   │
                           └──┬───────┬────────┬────┘
                  1. Execute  │       │        │ 3. Reserve
                  Payment     │       │        │    Inventory
                              ▼       │        ▼
                      [ Payment Srv ] │  [ Inventory Srv ]
                                      ▼
                             [ Shipping Srv ]
```

* **Choreography-based Saga**:
  * Các service tự lắng nghe Event của nhau qua Message Queue để thực thi local transaction tiếp theo.
  * Phù hợp cho workflow đơn giản (2-4 services), không có điểm nghẽn tập trung. Nhưng khó theo dõi khi luồng nghiệp vụ phức tạp.
* **Orchestration-based Saga**:
  * Có một Orchestrator Service trung tâm (State Machine, e.g., Temporal, AWS Step Functions) gửi lệnh gọi trực tiếp tới từng service và theo dõi tiến độ.
  * Dễ quản lý luồng nghiệp vụ phức tạp, dễ handle timeout.
* **Compensating Transaction (Giao dịch bù trừ)**:
  * Nếu bước `N` trong Saga thất bại (e.g., Hết hàng tồn kho) -> Orchestrator kích hoạt các giao dịch bù trừ ngược từ `N-1` về `1` (e.g., Hoàn lại tiền vào thẻ, Hủy order) để khôi phục hệ thống về trạng thái nhất quán cuối cùng (**Eventual Consistency**).
  * Compensation là **nghiệp vụ ngược** (refund), không phải "rollback" kỹ thuật — dữ liệu trung gian đã từng được người khác nhìn thấy. Compensation phải **idempotent** và **retry được tới khi thành công**.
  * Phân loại bước: **compensatable** (có thể bù) → **pivot transaction** (điểm không quay đầu, vd đã charge thẻ thành công) → **retriable** (các bước sau pivot phải chắc chắn thành công bằng cách retry). Đặt bước khó bù (gửi email, gọi bên thứ 3) **sau pivot**.
* **Saga thiếu Isolation (chữ I trong ACID)** → có thể gặp lost update, dirty read giữa các saga chạy song song. Biện pháp (*countermeasures*): **semantic lock** (trạng thái `PENDING_*` để saga khác biết bản ghi đang được xử lý), commutative updates (cộng/trừ thay vì gán), pessimistic view (sắp xếp lại thứ tự bước), reread value, version file.
* **So sánh với 2PC**: 2PC cho tính atomic mạnh nhưng chặn tài nguyên & coordinator là điểm yếu, hầu như không dùng xuyên microservice/HTTP; Saga không chặn, scale tốt, đổi lại phải thiết kế compensation & chấp nhận eventual consistency (xem `Database.md` 19.6).
* **Công cụ**: Temporal/Cadence (workflow-as-code, tự lưu trạng thái & retry), Camunda/Zeebe (BPMN), AWS Step Functions, Axon (Java), Eventuate Tram.

---

### 5.4. Service Mesh & Sidecar

```
Pod A                                     Pod B
┌───────────────────────┐                ┌───────────────────────┐
│ App A ──► Envoy proxy │ ══ mTLS ══════►│ Envoy proxy ──► App B │
└───────────────────────┘                └───────────────────────┘
              ▲  cấu hình (routing, retry, policy, cert) từ Control Plane (Istio istiod / Linkerd)
```
* **Data plane** (sidecar proxy Envoy/linkerd2-proxy, hoặc mô hình *ambient/sidecar-less* của Istio) chặn mọi traffic vào/ra của service; **control plane** phát cấu hình.
* Cung cấp **không cần sửa code**: mTLS giữa service (zero-trust), retry/timeout/circuit breaking (outlier detection), traffic splitting cho canary, metrics/tracing đồng nhất, authorization policy.
* Đánh đổi: thêm độ trễ mỗi hop (~vài trăm µs–1ms), tốn tài nguyên, độ phức tạp vận hành; trùng chức năng với thư viện resilience trong app → tránh cấu hình retry ở cả 2 nơi.

### 5.5. Chiến lược triển khai (Deployment Strategies)

| Chiến lược | Cách làm | Ưu điểm | Nhược điểm |
| :--- | :--- | :--- | :--- |
| **Recreate** | Tắt toàn bộ bản cũ rồi bật bản mới | Đơn giản | Có downtime |
| **Rolling Update** | Thay dần từng instance (K8s mặc định: `maxSurge`, `maxUnavailable`) | Không downtime, không tốn thêm nhiều tài nguyên | 2 phiên bản cùng chạy một lúc; rollback chậm |
| **Blue-Green** | Dựng đầy đủ môi trường mới (green) song song, chuyển toàn bộ traffic 1 lần | Rollback tức thì (chuyển lại blue) | Tốn gấp đôi tài nguyên; DB dùng chung phải tương thích cả 2 bản |
| **Canary** | Chuyển 1% → 5% → 25% → 100% traffic sang bản mới, theo dõi metric/SLO từng bước | Giảm bán kính ảnh hưởng (blast radius), có thể tự động rollback (Argo Rollouts, Flagger) | Cần routing theo tỷ lệ & observability tốt |
| **Shadow / Dark launch** | Nhân bản traffic thật sang bản mới nhưng bỏ kết quả | Test với tải thật, không ảnh hưởng user | Cẩn thận side effect (ghi DB, gửi email) |
| **Feature Flag** | Deploy code nhưng bật/tắt tính năng theo user/% qua cấu hình | Tách **deploy** khỏi **release**, kill switch tức thì | Nợ kỹ thuật nếu không dọn flag cũ |

> ⚠️ Mọi chiến lược không-downtime đều đòi hỏi **schema DB tương thích ngược** giữa 2 phiên bản đang cùng chạy → dùng **expand–contract migration** (`Database.md` 20.10) và API/event tương thích ngược (thêm field, không xóa/đổi nghĩa).

> 🛠 **Áp dụng:**
> - Mỗi lời gọi đồng bộ giữa service: **timeout + retry có giới hạn + circuit breaker + bulkhead** (5.2).
> - Thao tác nghiệp vụ trải qua nhiều service → **saga** (orchestration khi luồng phức tạp, cần nhìn tổng thể; choreography khi luồng đơn giản, ít bước) + bước bù trừ idempotent.
> - Không chia sẻ DB giữa service — chia sẻ DB là "distributed monolith": vẫn phụ thuộc chặt mà lại thêm độ trễ mạng.

<details><summary>🧠 Tự kiểm tra</summary>

1. **Service A gọi B gọi C, mỗi service có availability 99,9%. Availability của chuỗi đồng bộ là bao nhiêu?**
   → ≈ 0,999³ ≈ 99,7% — chuỗi gọi đồng bộ càng dài thì availability càng thấp; lý do nên giảm phụ thuộc đồng bộ.
</details>


---

## 6. Storage & Data Systems

> 🎯 **Bài toán:** Dữ liệu khác nhau có **mẫu truy cập** khác nhau: giao dịch cần ACID, phiên đăng nhập cần đọc cực nhanh, log cần ghi khổng lồ, tìm kiếm cần full-text, báo cáo cần quét hàng tỷ dòng. Không kho lưu trữ nào tối ưu cho tất cả.
>
> 💡 **Ý tưởng:** Bắt đầu từ **câu hỏi truy vấn và yêu cầu nhất quán**, rồi mới chọn kho lưu trữ (*polyglot persistence*). Mặc định là **RDBMS** cho dữ liệu nghiệp vụ, thêm kho chuyên dụng khi có nhu cầu cụ thể — mỗi kho thêm vào là thêm chi phí vận hành và bài toán đồng bộ dữ liệu (thường qua CDC).


> 💡 *Chi tiết về Storage Engines (LSM-Tree vs B+Tree), MVCC, Concurrency Isolation, ARIES Crash Recovery, Distributed Database Consensus (2PC, Paxos, Raft, Multi-Paxos) đã được viết chi tiết tại [`Database.md`](Database.md).*

### 6.1. SQL vs NoSQL Taxonomy

```
                              Data Storage Paradigms
                     ┌──────────────────┴──────────────────┐
                     ▼                                     ▼
             Relational (SQL)                       NoSQL Systems
         (PostgreSQL, MySQL, Oracle)                       │
       ┌──────────────────┬────────────────────┬───────────┴───────────┐
       ▼                  ▼                    ▼                       ▼
  Document Store     Key-Value Store     Wide-Column Store        Graph Store
 (MongoDB, CouchDB)  (Redis, DynamoDB)  (Cassandra, HBase, Scylla)  (Neo4j, AWS Neptune)
```

| Loại Storage | Đại diện tiêu biểu | Cấu trúc dữ liệu & Đặc tính | Use Case tối ưu |
| :--- | :--- | :--- | :--- |
| **Relational (RDBMS)** | PostgreSQL, MySQL | Bảng với Schema cố định (Rows & Columns), Hỗ trợ ACID nghiêm ngặt, JOIN phức tạp. | Tài chính ngân hàng, E-commerce Core Orders, User Authentication, quan hệ phức tạp. |
| **Document Store** | MongoDB, Amazon DocumentDB | JSON / BSON documents bán cấu trúc (Semi-structured), schema linh hoạt, hỗ trợ nested object. | Content Management System (CMS), User Profile đa dạng thuộc tính, Product Catalogs. |
| **Key-Value Store** | Redis, AWS DynamoDB, Memcached | Lưu trữ cặp `Key -> Value (Blob/Primitive)`. Tối ưu O(1) lookup theo Key. | Session Store, User Shopping Cart, Leaderboard, Token cache. |
| **Wide-Column Store** | Apache Cassandra, ScyllaDB, HBase | Dữ liệu tổ chức theo Row Key và các Column Families. Tối ưu cực cao cho Write throughput (LSM-Tree). | Time-series metrics, IoT telemetry logs, Financial tick data, Messenger chat history. |
| **Graph Database** | Neo4j, Amazon Neptune | Nodes (Thực thể), Edges (Quan hệ) và Properties. Tối ưu duyệt đồ thị sâu (Graph Traversal). | Mạng xã hội (Bạn của bạn), Fraud Detection (Phát hiện rửa tiền qua mạng lưới), Recommendation Engine. |
| **Object Storage** | AWS S3, Google Cloud Storage, MinIO | Lưu trữ file phi cấu trúc (Unstructured Binary Blobs) đi kèm Metadata qua HTTP REST API. Dung lượng vô hạn. | Video, Hình ảnh, Backup Database archives, Big Data raw files. |

---

### 6.2. Data Analytics: Data Warehouse vs Data Lake vs Lakehouse

```
Data Lake (Raw files: S3, HDFS) ──► Parquet/Avro (Schema-on-Read, Structured + Unstructured)
                                         │
                                   [ ETL / ELT ]
                                         │
                                         ▼
Data Warehouse (Snowflake, BigQuery, Redshift) ──► Optimized Columnar (Schema-on-Write, BI/SQL)

┌────────────────────────────────────────────────────────────────────────┐
│ Data Lakehouse (Delta Lake, Apache Iceberg, Apache Hudi):             │
│ Kết hợp: ACID Transactions + Metadata Layer chạy trực tiếp trên S3     │
└────────────────────────────────────────────────────────────────────────┘
```

* **ETL (Extract - Transform - Load)**: Dữ liệu thô từ database nguồn được trích xuất -> Biến đổi chuẩn hóa (clean, aggregate) trên compute server -> Tải vào Data Warehouse.
* **ELT (Extract - Load - Transform)**: Trích xuất dữ liệu -> Nạp thẳng toàn bộ raw data vào Data Lake/Cloud Warehouse -> Tận dụng sức mạnh tính toán song song (Massive Parallel Processing - MPP) của Cloud Engine (Snowflake, BigQuery) để transform sau bằng SQL.
* **Batch vs Stream processing**: Batch (Spark, dbt chạy theo lịch — độ trễ giờ/ngày, đơn giản, dễ chạy lại) vs Streaming (Flink, Kafka Streams, Spark Structured Streaming — độ trễ giây, phải xử lý event đến muộn bằng **watermark**, windowing, state, exactly-once checkpoint). **Lambda architecture** = batch layer + speed layer (2 code path); **Kappa architecture** = chỉ streaming, replay log khi cần tính lại.
* **CDC (Change Data Capture)**: đọc WAL/binlog của OLTP DB (Debezium) → Kafka → đồng bộ sang warehouse, search index, cache — thay cho việc "dual write" từ ứng dụng.

---

### 6.3. Database Scaling Playbook (thứ tự nên áp dụng)

```
1. Tối ưu query & index ──► 2. Cache (Redis) ──► 3. Read replicas ──► 4. Vertical scale
        ──► 5. Partitioning (trong 1 node) ──► 6. Tách DB theo domain (functional partitioning)
        ──► 7. Sharding (horizontal) ──► 8. Đổi sang DB phân tán (NewSQL / NoSQL) nếu cần
```
| Kỹ thuật | Giải quyết | Cái giá phải trả |
| :--- | :--- | :--- |
| **Index / query tuning** | Hầu hết vấn đề hiệu năng thực tế | Gần như miễn phí — luôn làm trước (`Database.md` 20) |
| **Cache** | Read-heavy, dữ liệu nóng | Invalidation, stale data (2.1) |
| **Read replica** | Scale đọc | **Replication lag** → read-your-writes, monotonic reads (`Database.md` 19.4) |
| **Connection pooling / proxy** (PgBouncer, ProxySQL, RDS Proxy) | Quá nhiều connection từ nhiều instance app | Hạn chế session state |
| **Partitioning** | Bảng quá lớn, xóa dữ liệu cũ, pruning | PK phải chứa partition key |
| **Functional partitioning** | Tách DB theo service/domain | Mất JOIN & transaction xuyên domain |
| **Sharding** | Scale **ghi** & dung lượng vượt 1 node | Chọn shard key, cross-shard query/transaction, rebalancing, unique toàn cục |
| **NewSQL** (CockroachDB, TiDB, YugabyteDB, Spanner) | Cần SQL + ACID + scale ngang tự động | Latency cao hơn (consensus mỗi ghi), chi phí, vận hành |

**Sharding chi tiết**:
* **Chiến lược**: range (dễ range query, dễ hot spot), hash (phân bố đều, mất range query), **directory/lookup-based** (bảng tra `tenant → shard`, linh hoạt khi di chuyển tenant lớn), geo (theo vùng — data residency).
* **Shard key tốt**: cardinality cao, phân bố đều, có mặt trong phần lớn query, gom các dữ liệu hay dùng chung về cùng shard (vd tất cả dữ liệu của 1 tenant/user).
* **Vấn đề**: hot shard (celebrity, tenant lớn) → tách riêng / thêm hậu tố ngẫu nhiên vào key; **scatter-gather** query khi thiếu shard key; join & transaction xuyên shard; **resharding** (pre-split thành nhiều shard logic hơn số node vật lý, vd 1024 logical shards → di chuyển cả shard logic giữa node); sinh ID toàn cục (11.5).
* **Công cụ**: Vitess (MySQL — YouTube/Slack), Citus (PostgreSQL), ShardingSphere (Java), MongoDB sharding, hoặc tự sharding ở tầng application.

### 6.4. Search Engine (Elasticsearch / OpenSearch)
* Dựa trên **Lucene inverted index** (`Database.md` 9.4); index chia thành **shard** (primary + replica), mỗi shard là 1 Lucene index gồm nhiều **segment** bất biến.
* **Near real-time**: document chỉ tìm được sau `refresh` (mặc định 1s) — không đọc-sau-ghi ngay như DB.
* **Không dùng làm nguồn dữ liệu chính (source of truth)**: không có transaction đa document, có thể mất dữ liệu khi sự cố hiếm gặp → DB là nguồn gốc, đồng bộ sang ES qua **CDC/outbox** và có khả năng **reindex** lại từ đầu.
* Ranking: BM25 (mặc định); kết hợp vector search (kNN/HNSW) cho **hybrid search**.
* Bài toán thường gặp: deep pagination (dùng `search_after` thay `from/size` lớn), mapping explosion (quá nhiều field động), chọn số shard (shard quá nhỏ/nhiều → overhead; khuyến nghị vài chục GB/shard).

### 6.5. Chọn công nghệ lưu trữ — hướng dẫn nhanh
| Nhu cầu | Gợi ý |
| :--- | :--- |
| Dữ liệu nghiệp vụ cốt lõi, quan hệ, cần transaction | PostgreSQL / MySQL |
| Cache, session, rate limit, leaderboard, distributed lock | Redis / Valkey |
| Ghi cực lớn, truy vấn theo partition key biết trước (chat, IoT, timeline) | Cassandra / ScyllaDB / DynamoDB |
| Tìm kiếm full-text, autocomplete, log search | Elasticsearch / OpenSearch |
| Metrics / time-series | Prometheus (+ Thanos/Mimir), TimescaleDB, InfluxDB, ClickHouse |
| Analytics / OLAP | ClickHouse, BigQuery, Snowflake, Redshift, DuckDB |
| File, ảnh, video, backup | Object storage (S3, GCS, MinIO) + CDN |
| Quan hệ đồ thị sâu (gợi ý bạn bè, fraud ring) | Neo4j, Neptune (hoặc recursive CTE nếu đơn giản) |
| Event log / message bus / CDC | Kafka (hoặc Pulsar, Redpanda) |
| Vector similarity (RAG, gợi ý) | pgvector, Qdrant, Milvus, Elasticsearch kNN |

✅ **Vì sao RDBMS (PostgreSQL/MySQL) là mặc định:** ACID, JOIN, ràng buộc toàn vẹn, truy vấn linh hoạt khi yêu cầu thay đổi, hệ sinh thái trưởng thành; một node hiện đại xử lý được hàng chục nghìn QPS và vài TB. NoSQL chỉ thắng rõ khi có nhu cầu **scale ghi ngang** vượt một máy, mô hình dữ liệu đặc thù (wide-column cho time-series ghi lớn, document schema linh hoạt) hoặc latency cực thấp theo key.

> 🛠 **Áp dụng:** Scale DB theo thứ tự rẻ → đắt (6.3): tối ưu query/index → cache → read replica → partition → sharding. Sharding là bước **cuối cùng** vì mất JOIN/transaction xuyên shard và khó đổi shard key.

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao không nên dùng Elasticsearch làm nguồn dữ liệu chính (source of truth)?**
   → Near-real-time (refresh), không có transaction đa document, mapping khó đổi, có thể mất dữ liệu khi sự cố cluster → dùng làm **index phụ** đồng bộ từ DB qua CDC.
</details>


---

## 7. Networking & Communication Protocols

> 🎯 **Vì sao cần trong System Design:** mọi con số latency (round-trip, handshake TLS, DNS) và nhiều quyết định (HTTP/2 vs HTTP/3, WebSocket vs SSE, keep-alive, CDN) đến từ tầng mạng. Phần này là tóm tắt — giải thích nguyên lý đầy đủ (vì sao TCP cần 3 bước, TIME_WAIT, flow vs congestion control...) xem [`Networking.md`](Networking.md).


> 💡 Phần dưới là tóm tắt cho System Design. Kiến thức mạng chi tiết (TCP internals, DNS, HTTP caching/CORS, TLS, K8s networking, lỗi kết nối trong Java, công cụ chẩn đoán) xem [`Networking.md`](Networking.md).

### 7.1. DNS Resolution Flow (Phân giải tên miền)

```
[ Browser ] ──1. Query 'api.example.com'──► [ Local DNS Cache / Hosts file ] (Miss)
    │
    └──2. Ask ISP DNS Resolver ─────────► [ Recursive Resolver ]
                                                 │
      ┌──────────────────────────────────────────┤
      │ 3. Ask Root Server ('.')                 ▼
      │ ◄── Return TLD Server IP ('.com') ── [ Root DNS Server ]
      │
      │ 4. Ask TLD Server ('.com')               ▼
      │ ◄── Return Name Server IP ────────── [ TLD DNS Server ]
      │
      │ 5. Ask Authoritative Name Server         ▼
      │ ◄── Return A Record (IP: 1.2.3.4) ── [ Authoritative DNS ]
      │
[ Browser ] ◄──6. Return Final IP: 1.2.3.4 ──────┘
```

#### Các bản ghi DNS quan trọng:
* **A Record**: Map domain name về IPv4 address (`example.com -> 93.184.216.34`).
* **AAAA Record**: Map domain name về IPv6 address.
* **CNAME Record (Canonical Name)**: Alias trỏ một domain sang một domain khác (`www.example.com -> example.com`).
* **MX Record (Mail Exchange)**: Chỉ định mail server tiếp nhận email của domain.
* **TXT Record**: Lưu text arbitrary (dùng để verify quyền sở hữu domain, cấu hình SPF/DKIM chống email spam).

---

### 7.2. TCP 3-Way Handshake & TCP vs UDP

```
TCP 3-Way Handshake (Thiết lập kết nối tin cậy):
Client ──────── SYN (seq=x) ────────► Server
Client ◄──── SYN-ACK (seq=y, ack=x+1) ─── Server
Client ──────── ACK (ack=y+1) ──────► Server (Bắt đầu gửi dữ liệu)
```

| Tiêu chí | TCP (Transmission Control Protocol) | UDP (User Datagram Protocol) |
| :--- | :--- | :--- |
| **Bản chất kết nối** | Connection-oriented (Bắt buộc 3-way handshake). | Connectionless (Bắn packet ngay không cần handshake). |
| **Độ tin cậy** | Đảm bảo 100% (Có ACK, Tự động retransmit khi mất packet, Checksum kiểm tra toàn vẹn). | Không đảm bảo (Packet có thể mất, trùng lặp hoặc đến sai thứ tự). |
| **Đảm bảo thứ tự** | Có (Đánh số `Sequence Number`, sắp xếp lại packet đúng thứ tự ban đầu). | Không (Packet đến thứ tự nào ứng dụng nhận thứ tự đó). |
| **Kiểm soát nghẽn** | Có Flow Control (Sliding Window) và Congestion Control (TCP Tahoe, Cubic, BBR). | Không có cơ chế kiểm soát nghẽn (Gửi tối đa băng thông). |
| **Overhead** | Header lớn (20 - 60 bytes), tốn tài nguyên duy trì state. | Header nhỏ gọn (8 bytes), tốn cực ít tài nguyên. |
| **Đóng kết nối** | 4-way FIN/ACK; bên chủ động đóng giữ trạng thái **TIME_WAIT** (2×MSL) → server tạo quá nhiều kết nối ngắn ra ngoài có thể cạn ephemeral port → dùng connection pooling/keep-alive | Không có |
| **Use Case** | Web browsing (HTTP/HTTPS), File Transfer (FTP), Email (SMTP), Database connection. | Video Streaming trực tiếp, Voice/Video Call (VoIP, WebRTC), Game online FPS, DNS lookup. |

---

### 7.3. Sự phát triển của HTTP: HTTP/1.1 vs HTTP/2 vs HTTP/3

```
HTTP/1.1 (Head-of-Line Blocking ở Application Layer):
TCP Connection: [ Request 1 ] ──► [ Response 1 ] ──► [ Request 2 ] ──► [ Response 2 ]
(Request 2 phải chờ Response 1 hoàn thành)

HTTP/2 (Multiplexing trên 1 TCP Connection):
Stream 1: [ Req 1 Data ] ───► [ Resp 1 Chunk ]
Stream 2:   [ Req 2 Data ] ─►   [ Resp 2 Chunk ]  (Chạy song song lồng ghép Streams)
(Nhưng nếu 1 packet TCP bị rớt -> TOÀN BỘ các stream bị chặn ở tầng TCP!)

HTTP/3 (QUIC over UDP - Giải quyết triệt để HoL Blocking):
UDP Datagrams: [ Stream 1 Pkt ] [ Stream 2 Pkt ] [ Stream 3 Pkt ]
(Stream 1 rớt packet CHỈ ảnh hưởng Stream 1, Stream 2 và 3 vẫn chạy 100% mượt mà)
```

* **HTTP/1.1**: Hỗ trợ Persistent Connection (`Keep-Alive`), nhưng bị lỗi **Head-of-Line (HoL) Blocking** trên từng connection (mỗi request phải chờ response trước đó về mới được gửi tiếp). Trình duyệt phải lách luật bằng cách mở 6 TCP connections song song cho mỗi domain.
* **HTTP/2**:
  * **Binary Framing**: Thay thế text thô bằng các binary frames nhỏ.
  * **Multiplexing**: Gửi đồng thời hàng trăm request/response song song trên **duy nhất 1 kết nối TCP**.
  * **HPACK Header Compression**: Nén bảng header, loại bỏ lặp lại metadata.
  * **Server Push**: Server chủ động đẩy trước CSS/JS về client trước khi HTML parse yêu cầu. ⚠️ Thực tế hiệu quả kém (đẩy cả thứ browser đã cache) → **Chrome đã gỡ bỏ (2022)**, thay bằng `103 Early Hints` + `<link rel=preload>`.
* **HTTP/3 (QUIC Protocol)**:
  * Chuyển từ transport TCP sang **QUIC chạy trên nền UDP**.
  * **1-RTT / 0-RTT Handshake**: Gộp TLS 1.3 và transport handshake trong **1 round-trip** cho kết nối mới (TCP + TLS 1.3 cần 2 RTT); kết nối lại tới server đã biết có thể gửi dữ liệu ngay (**0-RTT**) — ⚠️ dữ liệu 0-RTT có thể bị **replay** nên chỉ dùng cho request idempotent (GET).
  * **Connection Migration**: Chuyển đổi mạng (từ Wi-Fi sang 4G trên điện thoại) không bị đứt kết nối nhờ định danh bằng `Connection ID` thay vì `Client IP + Port`.
  * Header compression **QPACK** (thay HPACK); congestion control & mất gói xử lý ở user space (QUIC) — dễ cập nhật hơn kernel TCP nhưng tốn CPU hơn. Một số mạng doanh nghiệp chặn UDP → client phải fallback về HTTP/2.

### 7.4. HTTPS & TLS 1.3 Handshake

```
Client                                              Server
  │── ClientHello (cipher suites, key share ECDHE) ──►│
  │◄─ ServerHello (key share) + Certificate +         │
  │   CertificateVerify + Finished (đã mã hóa) ───────│   ← 1 RTT
  │── Finished + HTTP Request (mã hóa) ──────────────►│
```
* **Mã hóa bất đối xứng** (ECDHE) chỉ dùng để **thỏa thuận khóa phiên**; dữ liệu được mã hóa bằng **khóa đối xứng** (AES-GCM / ChaCha20) vì nhanh hơn nhiều.
* **Certificate** do CA ký chứng minh server là chủ domain; client kiểm tra chuỗi chứng chỉ tới root CA tin cậy, tên miền (SAN), hạn dùng, thu hồi (OCSP stapling).
* **Forward secrecy**: khóa phiên ECDHE sinh mới mỗi kết nối → lộ private key của server sau này không giải mã được traffic cũ đã ghi lại. TLS 1.3 bắt buộc forward secrecy, bỏ RSA key exchange và các cipher yếu, rút handshake còn 1 RTT (TLS 1.2 cần 2 RTT).
* **mTLS** (mutual TLS): cả client cũng trình certificate → xác thực service-to-service (service mesh, 5.4).
* **TLS termination** tại LB/gateway (giải mã ở biên, traffic nội bộ có thể là HTTP hoặc re-encrypt) vs **TLS passthrough** (L4, mã hóa end-to-end tới backend).

### 7.5. "Điều gì xảy ra khi gõ `https://example.com` vào trình duyệt?" (câu hỏi kinh điển)
1. Trình duyệt parse URL, kiểm tra **HSTS** (ép HTTPS), cache.
2. **DNS resolution**: browser cache → OS cache/hosts → recursive resolver → root → TLD → authoritative (7.1); có thể trả về IP của **CDN edge** gần nhất (GeoDNS/Anycast).
3. **TCP handshake** (3-way) tới IP:443 — hoặc QUIC nếu server quảng cáo HTTP/3 (`Alt-Svc`).
4. **TLS handshake** (7.4), thỏa thuận giao thức ứng dụng qua **ALPN** (h2 / http/1.1).
5. Gửi **HTTP request** (method, path, headers, cookies).
6. Phía server: CDN/WAF → **Load Balancer** (L4/L7) → reverse proxy/API gateway → **app server** → cache/DB/dịch vụ khác → tạo response.
7. Browser nhận response (status, headers `Cache-Control`, `Set-Cookie`, body nén gzip/br) → parse HTML → tải CSS/JS/ảnh (nhiều request song song trên HTTP/2) → build DOM/CSSOM → layout → paint; JS chạy và có thể gọi thêm API.
8. Kết nối được giữ (keep-alive) để tái sử dụng.

---

### 7.6. Real-Time Communication Technologies

```
1. Short Polling:
   Client ──Request──► Server ──► Response (Data/Empty)
   (Client lặp lại mỗi 2 giây -> Lãng phí 95% tài nguyên mạng và CPU)

2. Long Polling:
   Client ──Request──► Server (Giữ kết nối treo cho đến khi có event mới hoặc timeout)
   Server ──Push Event / Timeout──► Client (Client xử lý xong lập tức mở Request mới)

3. Server-Sent Events (SSE):
   Client ──HTTP GET (Accept: text/event-stream)──► Server
   Server ──Stream Data Frame 1 (HTTP Persistent)──► Client
   Server ──Stream Data Frame 2────────────────────► Client (1 chiều từ Server -> Client)

4. WebSockets:
   Client ──HTTP Upgrade Handshake (101 Switching Protocols)──► Server
   Client ◄════════ Full-Duplex Bi-directional TCP Socket ═══► Server (2 chiều thời gian thực)
```

| Tiêu chí | Long Polling | Server-Sent Events (SSE) | WebSocket | WebRTC |
| :--- | :--- | :--- | :--- | :--- |
| **Giao thức** | HTTP thông thường. | HTTP/1.1 hoặc HTTP/2. | WebSocket Protocol (`ws://`, `wss://`). | UDP (SRTP, SCTP qua P2P Mesh). |
| **Chiều truyền tin** | Half-duplex (Request-Response). | Đơn công (1 chiều: Server -> Client). | Song công toàn phần (Full-Duplex 2 chiều). | Peer-to-Peer 2 chiều. |
| **Tự động Reconnect**| Tự viết ở client code. | Hỗ trợ Native ở trình duyệt (`EventSource`). | Phải tự viết xử lý reconnect & heartbeat. | Có cơ chế ICE/STUN/TURN. |
| **Header Overhead** | Rất lớn (Mỗi lần poll gửi lại toàn bộ HTTP Headers). | Rất nhỏ (Chỉ tốn header ở request mở stream đầu tiên). | Siêu nhỏ (Frame header chỉ 2 - 10 bytes sau handshake). | Siêu nhỏ. |
| **Use Case tối ưu** | Hệ thống legacy đơn giản, thông báo tần suất thấp. | Dashboard theo dõi chứng khoán, Live Log monitoring, AI ChatGPT stream câu trả lời. | Chat realtime đa chiều, Gaming multiplayer, Collaborative tools (Figma, Google Docs). | Video Call 1-1 / Group (Zoom, Meet), Audio streaming P2P. |

---

## 8. Reliability, Availability & Disaster Recovery

> 🎯 **Bài toán:** Phần cứng hỏng, deploy lỗi, cả một vùng (region) mất điện — chắc chắn sẽ xảy ra. Cần định lượng "đủ tin cậy" là bao nhiêu và chuẩn bị cho thảm họa.
>
> 💡 **Ý tưởng:** Định nghĩa mục tiêu bằng con số (**SLI → SLO → SLA**), dùng **error budget** để cân bằng tốc độ ra tính năng với độ ổn định; loại bỏ điểm chết duy nhất bằng **dư thừa** (redundancy) ở mọi tầng; với thảm họa, chọn chiến lược theo **RTO** (chấp nhận ngừng bao lâu) và **RPO** (chấp nhận mất bao nhiêu dữ liệu).
>
> ✅ **Vì sao không nhắm 100%:** mỗi số 9 thêm vào tốn chi phí tăng theo cấp số nhân (multi-AZ → multi-region active-active) và làm chậm phát triển; mục tiêu đúng là **mức mà người dùng thực sự cảm nhận được** và nghiệp vụ trả nổi.


### 8.1. SLA, SLO, SLI & Bảng số 9 sẵn sàng (Nines of Availability)

* **SLI (Service Level Indicator)**: Chỉ số đo lường hiệu năng thực tế theo thời gian thực (e.g., Tỷ lệ request thành công = 99.95%, p99 Latency = 120ms).
* **SLO (Service Level Objective)**: Mục tiêu cam kết nội bộ mà team engineering hướng tới (e.g., SLI Request Success Rate phải luôn $\ge 99.9\%$).
* **SLA (Service Level Agreement)**: Hợp đồng cam kết pháp lý với khách hàng kèm mức phạt tài chính nếu vi phạm (e.g., SLA 99.9% uptime, nếu dưới 99.9% hoàn trả 20% phí thuê bao tháng).

$$\text{Availability} = \frac{\text{Uptime}}{\text{Uptime} + \text{Downtime}} \times 100\%$$

| Uptime SLA | Thời gian Downtime tối đa mỗi Năm | Thời gian Downtime tối đa mỗi Tháng | Thời gian Downtime mỗi Ngày |
| :--- | :--- | :--- | :--- |
| **99% (Two Nines)** | 3.65 ngày | 7.31 giờ | 14.4 phút |
| **99.9% (Three Nines)** | 8.77 giờ | 43.83 phút | 1.44 phút |
| **99.99% (Four Nines)** | 52.60 phút | 4.38 phút | 8.64 giây |
| **99.999% (Five Nines - High Availability chuẩn viễn thông)** | **5.26 phút** | **26.30 giây** | **864.00 mili-giây** |
| **99.9999% (Six Nines)** | 31.56 giây | 2.63 giây | 86.40 mili-giây |

**Tính availability của hệ thống ghép**:
* **Nối tiếp** (request phải đi qua cả A và B): $A = A_1 \times A_2$ → 99.9% × 99.9% ≈ **99.8%** (càng nhiều tầng phụ thuộc đồng bộ, availability càng giảm).
* **Song song / dự phòng** (chỉ cần 1 trong N chạy): $A = 1 - (1 - A_1)^N$ → 2 instance 99% ≈ **99.99%** (giả định lỗi độc lập — thực tế hay lỗi cùng lúc do chung zone, chung bug, chung config).

**Error budget** (khái niệm SRE của Google): SLO 99.9%/tháng → được phép "tiêu" ~43 phút lỗi. Còn budget → thoải mái release tính năng; hết budget → đóng băng release, ưu tiên ổn định. **Alert theo burn rate** (tốc độ tiêu budget, vd tiêu 2% budget trong 1 giờ) thay vì alert theo từng lỗi đơn lẻ → ít alert rác.

---

### 8.2. Disaster Recovery: RTO vs RPO

```
┌────────────────────────────────────────────────────────┐
│                        Disaster Event                  │
│                              │                         │
│  ◄──── RPO (Data Loss) ──────┼───── RTO (Downtime) ──► │
│                              │                         │
│  [ Last Clean Backup ]       │             [ System Restored ]
└──────────────────────────────┴─────────────────────────┘
```

* **RPO (Recovery Point Objective)**: Lượng dữ liệu tối đa chấp nhận bị mất (tính theo đơn vị thời gian) khi có sự cố.
  * *Ví dụ*: Backup database mỗi 4 tiếng -> RPO = 4 tiếng (mất tối đa 4 tiếng dữ liệu mới nhất).
* **RTO (Recovery Time Objective)**: Thời gian tối đa cho phép hệ thống ngừng hoạt động trước khi khôi phục chạy lại bình thường.
  * *Ví dụ*: RTO = 30 phút -> Đội ngũ kỹ thuật phải khôi phục hệ thống trong vòng 30 phút sau khi data center bị cháy.

#### 4 Chiến lược Disaster Recovery theo chi phí:
1. **Backup & Restore**: Backup data lên S3/Tape. RPO & RTO tính bằng giờ/ngày. Rẻ nhất.
2. **Pilot Light**: Core data luôn được sync liên tục sang Region dự phòng. Compute (Server) ở trạng thái tắt, chỉ bật lên khi có thảm họa. RTO tính bằng 15-30 phút.
3. **Warm Standby**: Một phiên bản thu nhỏ (Minimal capacity) của hệ thống luôn chạy 24/7 ở Region dự phòng. Khi có thảm họa, scale up hệ thống lên 100%. RTO tính bằng vài phút.
4. **Active-Active Multi-Region**: Toàn bộ hệ thống chạy 100% công suất song song ở nhiều Region toàn cầu. Traffic được định tuyến thông minh qua Geo-DNS/Anycast. RTO $\approx 0$; RPO $\approx 0$ **chỉ khi** replication giữa region là đồng bộ (tốn latency liên lục địa) — với replication bất đồng bộ vẫn có thể mất vài giây dữ liệu và phải giải quyết **xung đột ghi** giữa các region (multi-leader, `Database.md` 19.4/19.7). Chi phí đắt nhất.

**Các điểm hay bị bỏ quên**:
* **Backup không được test restore = không có backup.** Diễn tập khôi phục định kỳ; đo thời gian restore thực tế so với RTO.
* **Point-in-Time Recovery (PITR)**: base backup + WAL/binlog archive → khôi phục tới 1 thời điểm bất kỳ (cứu lỗi `DELETE` nhầm — replication **không** cứu được vì nó sao chép cả lỗi).
* Quy tắc **3-2-1**: 3 bản sao, 2 loại phương tiện, 1 bản ở nơi khác (offsite/khác account — chống ransomware, xóa nhầm cả account).
* **Chaos engineering** (Netflix Chaos Monkey, Gremlin, AWS FIS) & **game day**: chủ động gây lỗi có kiểm soát để kiểm chứng khả năng chịu lỗi.
* **Cell-based architecture**: chia hệ thống thành nhiều "cell" độc lập (mỗi cell phục vụ 1 nhóm khách hàng) → sự cố chỉ ảnh hưởng 1 cell (giảm blast radius) — AWS, Slack dùng.

---

## 9. Monitoring, Logging & Observability

> 🎯 **Bài toán:** Hệ thống nhiều service, một request đi qua 10 nơi; lỗi chỉ xảy ra với một số người dùng. Cần biết **có sự cố không**, **ở đâu**, **vì sao** — trước khi khách hàng báo.
>
> 💡 **Ý tưởng:** Metrics để **phát hiện** (rẻ, cảnh báo theo SLO), traces để **khoanh vùng** (request chậm ở hop nào), logs để **giải thích** (chi tiết lỗi). Cảnh báo theo **triệu chứng người dùng thấy** (error rate, latency — RED) thay vì theo nguyên nhân (CPU cao), vì nguyên nhân thì vô số còn triệu chứng mới là điều quan trọng.


### 9.1. Ba trụ cột của Observability (Three Pillars)

```
                       Observability
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
           Metrics         Logs          Traces
        (Định lượng)    (Ngữ cảnh)     (Hành trình)
        "Có gì sai?"   "Tại sao sai?"  "Sai ở đâu?"
```

1. **Metrics (Số liệu thống kê)**:
   * Dữ liệu số có thể tổng hợp theo thời gian (Time-series data). Tốn ít dung lượng, truy vấn cực nhanh để vẽ biểu đồ và trigger cảnh báo.
   * **Prometheus Data Types**:
     * *Counter*: Giá trị chỉ tăng hoặc reset về 0 khi restart (e.g., `http_requests_total`).
     * *Gauge*: Giá trị có thể tăng hoặc giảm tự do (e.g., `memory_usage_bytes`, `active_threads`).
     * *Histogram*: Đếm số lượng sample rơi vào các bucket định sẵn và tính tổng (dùng tính p50, p90, p99 latency).
     * *Summary*: Tương tự Histogram nhưng tính toán quantile trực tiếp ở client-side.
2. **Logs (Nhật ký sự kiện)**:
   * Các bản ghi text có cấu trúc (**Structured JSON Logs**) ghi lại chi tiết một sự kiện cụ thể kèm timestamp, log level (`INFO`, `WARN`, `ERROR`), `userId`, `errorCode`.
   * Tập trung hóa qua **ELK Stack** (Elasticsearch, Logstash, Kibana) hoặc **Grafana Loki** (nhẹ hơn, chỉ đánh index metadata nhãn giống Prometheus).
3. **Distributed Tracing (Dấu vết phân tán)**:
   * Theo dõi toàn bộ hành trình của 1 request khi đi qua hàng chục microservices khác nhau.
   * **Trace**: Đại diện cho toàn bộ luồng xử lý của 1 request.
   * **Span**: Đại diện cho 1 đơn vị công việc riêng lẻ trong 1 service (chứa execution time, metadata tags).
   * **W3C Trace Context**: Chuẩn hóa việc truyền header `traceparent` (chứa `TraceID` và `ParentSpanID`) qua các giao thức HTTP/gRPC.
   * Công nghệ: **OpenTelemetry (OTel)**, Jaeger, Zipkin.

---

**Percentile, không phải trung bình**:
* Latency trung bình che giấu đuôi chậm: 99 request 10ms + 1 request 5s → trung bình ~60ms trông "ổn". Theo dõi **p50, p95, p99, p99.9**.
* **Tail latency amplification**: 1 trang gọi song song 100 backend, mỗi backend chỉ có 1% request chậm (p99) → xác suất trang bị chậm = $1 - 0.99^{100} \approx 63\%$. Giải pháp: hedged requests (gửi request dự phòng sau p95), giảm fan-out, timeout chặt.
* Không thể lấy trung bình các percentile từ nhiều instance → phải gộp **histogram** rồi mới tính percentile.

### 9.2. Framework giám sát: RED Method vs USE Method

* **RED Method** (Dành cho **Request-driven Services / APIs**):
  * **R - Rate**: Số lượng requests nhận được trên mỗi giây (Throughput: req/s).
  * **E - Errors**: Số lượng request bị lỗi thất bại (Error rate: HTTP 5xx count).
  * **D - Duration**: Thời gian xử lý request (Latency percentiles: p50, p95, p99).
* **USE Method** (Dành cho **Hạ tầng phần cứng / Hardware Resources: CPU, RAM, Disk, Network**):
  * **U - Utilization**: Phần trăm thời gian tài nguyên đang bận xử lý công việc (e.g., CPU 85%).
  * **S - Saturation**: Mức độ công việc bị ứ đọng chờ xử lý (e.g., CPU Run Queue Length, Disk I/O Wait).
  * **E - Errors**: Số lượng lỗi phần cứng/driver phát sinh (e.g., Network dropped packets).
* **Four Golden Signals** (Google SRE book): **Latency, Traffic, Errors, Saturation** — gần như RED + phần "S" của USE.
* **Alerting tốt**: alert theo **triệu chứng ảnh hưởng người dùng** (SLO, error rate, latency p99) thay vì theo nguyên nhân (CPU 80%); mỗi alert phải actionable và có runbook; tránh alert fatigue.
* **OpenTelemetry (OTel)**: chuẩn mở thống nhất API/SDK/protocol (OTLP) cho cả traces, metrics, logs (và profiles); thu thập qua **OTel Collector** rồi đẩy tới backend tùy chọn (Prometheus, Jaeger/Tempo, Loki, Datadog...) → không bị khóa vào nhà cung cấp.

---

## 10. Security & Authentication

> 🎯 **Bài toán:** Xác định người dùng **là ai** (authentication), **được làm gì** (authorization), cho phép ứng dụng bên thứ ba truy cập **mà không lộ mật khẩu** (OAuth 2.0), và chặn các lỗ hổng phổ biến.
>
> 💡 **Ý tưởng:** Phòng thủ nhiều lớp (*defense in depth*): TLS mọi nơi, xác thực tập trung (IdP), quyền tối thiểu, validate mọi input, bí mật không nằm trong code, và mặc định từ chối.
>
> ✅ **Vì sao dùng OAuth 2.0/OIDC + IdP có sẵn thay vì tự viết auth:** auth là nơi một lỗi nhỏ thành lỗ hổng lớn (lưu mật khẩu, token, MFA, khôi phục tài khoản); các giao thức chuẩn và IdP đã được kiểm chứng.


### 10.1. Authentication: Session-based vs Token-based (JWT) vs OAuth 2.0

```
JWT Structure (Header.Payload.Signature):
┌────────────────────────┬─────────────────────────┬───────────────────────────┐
│       Header           │        Payload          │         Signature         │
│ {"alg":"HS256",        │ {"sub":"12345",         │ HMACSHA256(               │
│  "typ":"JWT"}          │  "role":"admin",        │   base64(Header) + "." +  │
│                        │  "exp":1719829382}      │   base64(Payload), Secret)│
└────────────────────────┴─────────────────────────┴───────────────────────────┘
```

* **Session-based Authentication**: Server tạo session trong RAM/Redis và trả về `SessionID` qua `Set-Cookie: HttpOnly; Secure`. Server phải tra cứu session store ở mỗi request.
* **Token-based (JWT - JSON Web Token)**: Token tự chứa thông tin (Self-contained) và được **ký** (signed — JWS) bằng Secret Key (HMAC — `HS256`, bên verify cũng phải biết secret) hoặc Private Key (RSA/ECDSA — `RS256`/`ES256`, bên verify chỉ cần **Public Key**, thường lấy qua endpoint **JWKS**). Server verify chữ ký mà không cần query Database -> **Tối ưu cho Stateless Microservices**.
  * ⚠️ JWT **được ký chứ không được mã hóa**: payload chỉ là Base64URL — ai cũng đọc được → không đặt thông tin nhạy cảm vào claims (muốn mã hóa phải dùng JWE).
  * ⚠️ **Không thu hồi được** trước khi hết hạn (stateless) → access token ngắn hạn (5–15 phút) + refresh token (lưu server-side, xoay vòng, thu hồi được) hoặc denylist theo `jti`.
  * Luôn kiểm tra: chữ ký với thuật toán **cố định** (chống tấn công `alg: none` / đổi RS256 → HS256), `exp`, `nbf`, `iss`, `aud`.
* **Session vs JWT — chọn thế nào?** Web app truyền thống, cần logout/thu hồi tức thì → session (Redis) đơn giản & an toàn hơn. API cho nhiều service / mobile / bên thứ 3 → token (OAuth2/OIDC). Phổ biến: gateway xác thực JWT ở biên, service nội bộ tin header đã được gateway xác thực + mTLS.
* **OAuth 2.0 vs OpenID Connect (OIDC)**: OAuth 2.0 là framework **ủy quyền** (authorization — "app này được phép đọc Google Drive của tôi"), trả **access token**. OIDC là lớp **xác thực** (authentication — "người dùng này là ai") xây trên OAuth 2.0, bổ sung **ID Token** (JWT chứa thông tin user) + endpoint `userinfo`. "Login with Google" = OIDC.
* **Các grant type**: *Authorization Code + PKCE* (web/mobile/SPA — chuẩn hiện nay), *Client Credentials* (service-to-service, không có user), *Refresh Token*, *Device Code* (TV, CLI). **Implicit** và **Password grant** đã bị loại bỏ trong **OAuth 2.1**.

#### OAuth 2.0 Authorization Framework: Authorization Code Flow with PKCE
Chuẩn an toàn nhất hiện nay cho Web và Mobile Apps để cấp quyền cho ứng dụng bên thứ 3:

```
[ User/Browser ] ──1. Login & Authorize (Code Challenge)──► [ Authorization Server ]
       │                                                                 │
       ◄──2. Return Authorization Code ──────────────────────────────────┘
       │
[ Client Backend ] ──3. Exchange (Code + Code Verifier)──► [ Authorization Server ]
       │                                                                 │
       ◄──4. Return Access Token & Refresh Token ────────────────────────┘
       │
[ Client Backend ] ──5. Request Data (Bearer Token)──────► [ Resource Server (API) ]
```

---

### 10.2. Các lỗ hổng bảo mật phổ biến (OWASP Top 10) & Phòng chống

1. **SQL Injection (SQLi)**: Attacker chèn SQL độc hại vào input.
   * *Phòng chống*: Sử dụng **Prepared Statements / Parameterized Queries** 100%, tuyệt đối không cộng chuỗi SQL thủ công.
2. **Cross-Site Scripting (XSS)**: Attacker chèn mã JavaScript độc hại vào trang web để đánh cắp Cookie/Session của user.
   * *Stored XSS*: Lưu script vào DB. *Reflected XSS*: Script nằm trong URL query params.
   * *Phòng chống*: Encode HTML output, cấu hình HTTP Header `Content-Security-Policy (CSP)`, lưu session token trong Cookie với cờ `HttpOnly` (chặn JavaScript `document.cookie`).
3. **Cross-Site Request Forgery (CSRF)**: Lừa browser của user đã đăng nhập gửi request độc hại tới trang web mục tiêu.
   * *Phòng chống*: Sử dụng **Anti-CSRF Token** (Synchronizer Token Pattern), thiết lập thuộc tính Cookie `SameSite=Strict` hoặc `SameSite=Lax`.
4. **Server-Side Request Forgery (SSRF)**: Lừa server backend thực hiện HTTP request tới các tài nguyên nội bộ nhạy cảm (e.g., AWS Metadata Endpoint `http://169.254.169.254`).
   * *Phòng chống*: Whitelist domain hợp lệ, cấm gọi tới dải IP Private/Loopback (`10.0.0.0/8`, `192.168.0.0/16`, `127.0.0.1`).
5. **Broken Access Control / IDOR** (đứng **#1** trong OWASP Top 10): user đổi `GET /orders/123` thành `/orders/124` và xem được đơn của người khác.
   * *Phòng chống*: kiểm tra quyền **sở hữu tài nguyên** ở server cho mọi request (không chỉ kiểm tra "đã đăng nhập"), deny-by-default, không tin dữ liệu phân quyền gửi từ client.
6. **Security Misconfiguration & lộ secret**: actuator/admin endpoint mở public, debug bật trên production, secret commit vào Git, bucket S3 public.
7. **Vulnerable Dependencies / Supply chain** (vd Log4Shell 2021): quét dependency (OWASP Dependency-Check, Snyk, Dependabot), SBOM, pin version, ký artifact.

#### OWASP Top 10 — phiên bản 2021 (bản phổ biến nhất khi phỏng vấn)
| # | Hạng mục | Ví dụ |
| :--- | :--- | :--- |
| A01 | Broken Access Control | IDOR, leo thang quyền, CORS sai |
| A02 | Cryptographic Failures | Truyền/lưu dữ liệu nhạy cảm không mã hóa, hash password yếu |
| A03 | Injection | SQL/NoSQL/OS command/LDAP injection, XSS |
| A04 | Insecure Design | Thiếu rate limit cho OTP, luồng nghiệp vụ có lỗ hổng logic |
| A05 | Security Misconfiguration | Cấu hình mặc định, XXE, header bảo mật thiếu |
| A06 | Vulnerable & Outdated Components | Thư viện có CVE |
| A07 | Identification & Authentication Failures | Brute-force, session fixation, không có MFA |
| A08 | Software & Data Integrity Failures | Deserialization không an toàn, CI/CD bị chèn mã |
| A09 | Security Logging & Monitoring Failures | Không log/không cảnh báo khi bị tấn công |
| A10 | Server-Side Request Forgery (SSRF) | Gọi metadata endpoint cloud |

> 📌 **OWASP Top 10:2025** (bản mới, công bố cuối 2025): A01 Broken Access Control (**SSRF được gộp vào đây**) · A02 Security Misconfiguration (tăng từ #5) · **A03 Software Supply Chain Failures (mới)** · A04 Cryptographic Failures · A05 Injection · A06 Insecure Design · A07 Authentication Failures · A08 Software or Data Integrity Failures · A09 Logging & Alerting Failures · **A10 Mishandling of Exceptional Conditions (mới)**. Khi phỏng vấn nên biết cả bản 2021 (phổ biến trong tài liệu) và điểm khác của bản 2025.

**Các biện pháp nền tảng khác**: HTTPS everywhere + HSTS; hash mật khẩu bằng bcrypt/Argon2; mã hóa dữ liệu nhạy cảm at-rest (KMS, envelope encryption); nguyên tắc **least privilege** (DB user của app không có quyền DDL); rate limit cho login/OTP; audit log; quản lý secret bằng Vault/KMS; security headers (`Content-Security-Policy`, `X-Content-Type-Options`, `Strict-Transport-Security`).

---

## 11. Distributed System Design Patterns

> 🎯 **Bài toán:** Khi dữ liệu và xử lý nằm trên nhiều máy, những điều hiển nhiên trên một máy không còn đúng: **mạng có thể mất/trễ gói tin, máy có thể chết hoặc chỉ chậm (không phân biệt được), đồng hồ các máy lệch nhau**.
>
> 💡 **Mỗi pattern giải một câu hỏi:**
>
> | Câu hỏi | Pattern |
> | :--- | :--- |
> | Dữ liệu nằm ở node nào, thêm node mà ít phải di chuyển? | Consistent hashing (11.1) |
> | Hai bản sao lệch nhau ở đâu, so sánh nhanh thế nào? | Merkle tree (11.2) |
> | Node nào còn sống, phổ biến thông tin không cần trung tâm? | Gossip (11.3) |
> | Hai node cùng nghĩ mình là leader? | Fencing token, quorum (11.4) |
> | Sinh ID duy nhất không cần hỏi một nơi trung tâm? | Snowflake, segment (11.5) |
> | Chỉ một tiến trình được làm việc X? | Distributed lock + fencing (11.6) |
> | Nhiều node đồng ý một giá trị dù có node chết? | Consensus: Raft/Paxos (11.7) |
> | Mạng bị chia cắt thì chọn đúng hay chọn sẵn sàng? | CAP / PACELC (11.8) |
> | Sự kiện nào xảy ra trước khi đồng hồ không tin được? | Lamport/vector clock, HLC (11.10) |


### 11.1. Consistent Hashing (Băm nhất quán) & Virtual Nodes

Trong hệ thống Distributed Cache (Memcached/Redis) hoặc Distributed DB (Cassandra/DynamoDB), thuật toán băm truyền thống `hash(key) % N` có nhược điểm chí mạng: Khi thêm hoặc bớt 1 server ($N \to N+1$), gần như **100% keys bị đổi vị trí bucket**, gây Cache Stampede trên toàn hệ thống.

```
Consistent Hashing Ring (0 -> 2^32 - 1):
                      Node A [v1] (0°)
                      ┌─────────┐
             Key 1 ──►│         │◄── Node C [v2] (60°)
                      │         │
    Node B [v1] ─────►│  RING   │◄── Key 2
      (240°)          │         │
                      └─────────┘
                      Node B [v2] (180°)
```

* **Nguyên lý hoạt động**:
  1. Ánh xạ cả Server Nodes và Data Keys lên cùng một vòng tròn băm logic từ $0$ đến $2^{32}-1$.
  2. Để xác định Key nằm ở Server nào: Tìm vị trí của `hash(Key)` trên vòng tròn, sau đó đi theo chiều kim đồng hồ đến khi gặp Server Node đầu tiên.
  3. **Khi thêm/xóa 1 Server**: Chỉ có trung bình $K/N$ keys nằm giữa node mới và node liền kề bị di chuyển ($K$ là tổng số keys, $N$ là số server nodes).
* **Virtual Nodes (Vnodes)**:
  * Mỗi server vật lý được gán cho nhiều điểm ảo (e.g., 100 - 256 Virtual Nodes) rải đều khắp vòng tròn băm.
  * *Tác dụng*: Phân phối tải dữ liệu đều tuyệt đối giữa các server, loại bỏ tình trạng **Hotspot Node**, và dễ dàng gán server mạnh nhiều vnodes hơn server yếu.

---

### 11.2. Merkle Tree (Cây băm chống bất đồng bộ / Anti-Entropy)

Merkle Tree là cây nhị phân trong đó mỗi lá (Leaf Node) chứa cryptographic hash của một data block, và mỗi node cha chứa hash của các node con: `Hash(Node_Left + Node_Right)`.

```
                  Root Hash: H(AB + CD)
                    /                \
          Hash AB: H(A + B)      Hash CD: H(C + D)
             /          \           /          \
         Hash A       Hash B    Hash C       Hash D
           │            │         │            │
       [Data 1]     [Data 2]  [Data 3]     [Data 4]
```

* **Ứng dụng trong Cassandra/Dynamo-style DB (anti-entropy repair), Git, Blockchain (Bitcoin, Ethereum), Certificate Transparency, IPFS**:
  * Đồng bộ và phát hiện sự sai lệch dữ liệu giữa 2 node bản sao (Replicas) qua mạng cực nhanh mà không cần truyền toàn bộ dữ liệu thô.
  * Hai server chỉ cần so sánh `Root Hash`. Nếu giống nhau -> Dữ liệu đồng nhất 100%.
  * Nếu `Root Hash` khác nhau -> Duyệt đệ quy xuống cây con để xác định chính xác data block nào bị sai lệch và chỉ cần sync đúng data block đó qua mạng.

---

### 11.3. Gossip Protocol (Giao thức tin đồn)

* Giao thức truyền thông phân tán phi tập trung (Decentralized Peer-to-Peer) dựa trên cơ chế lây lan dịch bệnh.
* Mỗi giây, mỗi node chọn ngẫu nhiên $k$ node khác trong cluster để gửi thông tin trạng thái sức khỏe (Heartbeat, Node Join, Node Crash) của bản thân và các node nó biết.
* Trong vòng $O(\log N)$ vòng trao đổi, thông tin được loan báo và hội tụ (converge) trên toàn bộ $N$ nodes trong cluster.
* *Ứng dụng*: Quản lý Cluster Membership và Failure Detection trong Apache Cassandra, Consul, Redis Cluster.

---

### 11.4. Split-Brain Problem & Fencing Tokens

Trong hệ thống Active-Passive Master-Slave, khi xảy ra Network Partition giữa Master và Slave, Slave tưởng Master đã chết nên tự bầu mình lên làm New Master. Khi mạng phục hồi, hệ thống có **2 Master cùng ghi dữ liệu độc lập** dẫn đến hỏng toàn bộ data (**Split-Brain**).

```
Fencing Token Mechanism:
[ ZooKeeper / Raft Consensus ] ──Issue Token=34──► [ Old Master ] (Bị mạng lag, tưởng còn sống)
                               ──Issue Token=35──► [ New Master ]
                                                          │
                                     Write with Token=35  ▼
[ Shared Storage Engine ] ◄── Reject Write (Token 34 < Current 35) ── [ Old Master ]
(Storage chỉ chấp nhận token có số thứ tự tăng đơn điệu lớn nhất)
```

* **Fencing Token**: Một số nguyên tăng đơn điệu (Monotonically Increasing Number) được cấp bởi Consensus Service (ZooKeeper/etcd) mỗi khi một Master mới được phong tước.
* Storage Server kiểm tra Token đi kèm mỗi thao tác ghi. Nếu Token nhỏ hơn Token cao nhất từng thấy -> Từ chối request ngay lập tức.

---

### 11.5. Distributed Unique ID Generation (Sinh ID Phân tán Duy nhất)

Trong hệ thống phân tán chia shard nhiều Database, khóa chính `AUTO_INCREMENT` của cơ sở dữ liệu truyền thống không thể sử dụng trực tiếp vì các node sẽ sinh trùng ID với nhau.

```
                    ┌────────────────────────────┐
                    │      64-bit Snowflake      │
                    └──────────────┬─────────────┘
                                   │
 1 bit        41 bits                10 bits              12 bits
┌─────┬──────────────────────┬──────────────────────┬─────────────────┐
│  0  │ Timestamp (Milisec)  │  Worker ID / Node ID │ Sequence Number │
└─────┴──────────────────────┴──────────────────────┴─────────────────┘
 (Sign) (Đủ dùng ~69 năm)       (1024 nodes)           (4096 IDs/ms/node)
```

#### So sánh các chiến lược sinh ID phân tán:

| Chiến lược | Thuật toán / Cơ chế | Ưu điểm | Nhược điểm & Rủi ro | Use Case |
| :--- | :--- | :--- | :--- | :--- |
| **UUID (v4)** | 128-bit Random / Pseudo-random number. | Tạo độc lập tại Client/App server; không cần kết nối mạng; không có SPOF. | 128-bit quá dài; **không sắp xếp theo thời gian (Non-sequential)**; làm phân mảnh B+Tree Clustered Index trầm trọng. | Request ID, Correlation ID, Tracing ID, Token tạm. |
| **UUIDv7 / ULID** (RFC 9562, 2024) | 48-bit Unix timestamp (ms) + bit ngẫu nhiên/counter. | Sinh độc lập không cần điều phối như UUIDv4, nhưng **tăng dần theo thời gian** → thân thiện B+Tree; chuẩn hóa (PostgreSQL 18 có `uuidv7()`). | Vẫn 128-bit; lộ thời điểm tạo. | PK cho hệ thống phân tán hiện đại, thay UUIDv4. |
| **DB Ticket Server (Flickr)** | Cụm MySQL chuyên dụng dùng `REPLACE INTO Tickets64 ...` và `LAST_INSERT_ID()`. | Đơn giản, số nguyên 64-bit tăng dần tuần tự. | Database là bottleneck chịu tải; nguy cơ SPOF; trễ mạng khi gọi DB. | Hệ thống quy mô vừa, không muốn quản lý hạ tầng ID phức tạp. |
| **Twitter Snowflake** | 64-bit Integer: 41-bit time + 10-bit machine ID + 12-bit sequence. | 64-bit số nguyên; **Roughly Time-Sorted** (tối ưu B+Tree Index); throughput cực cao (~4 triệu ID/s/node); tạo hoàn toàn in-memory. | Phụ thuộc vào đồng hồ hệ thống. **Clock Drift (Clock rollback)** có thể sinh trùng ID nếu server NTP lùi giờ. | Twitter, Discord, Instagram, E-commerce Orders. |
| **Segment-based (Meituan Leaf)** | Lấy theo lô (Batch segment, ví dụ 1,000 ID/lần) từ DB vào memory của App Server. | Giảm 99.9% tải xuống DB; số nguyên tăng liên tục; không sợ clock drift. | Nếu App server khởi động lại sẽ lãng phí phần ID chưa dùng trong segment. | Hệ thống tài chính, hóa đơn, đơn hàng cần ID liên tục. |

> 🛡️ **Cách xử lý Clock Drift trong Snowflake**:
> Khi phát hiện `currentTimestamp < lastTimestamp` (đồng hồ bị lùi giờ):
> 1. Nếu độ lệch nhỏ ($< 5\text{ ms}$): Sleep thread chờ cho đến khi thời gian vượt qua `lastTimestamp`.
> 2. Nếu độ lệch lớn: Ném ngoại lệ, từ chối cấp ID, hoặc chuyển đổi mượn Worker ID dự phòng (Backup Worker ID), đồng thời kích hoạt cảnh báo DevOps kiểm tra NTP server.

---

### 11.6. Distributed Locking Mechanisms (Cơ chế Khóa Phân tán)

Khi nhiều worker/service chạy song song trên nhiều server vật lý cần đồng bộ truy cập vào một tài nguyên dùng chung (ví dụ: trừ tồn kho, tính số dư ví, đồng bộ hóa đơn), ta cần **Distributed Lock**.

```
1. Redis Distributed Lock (SETNX + Lua Script):
   App ──► SET resource_key client_uuid NX PX 30000 ──► Redis (Khóa thành công với TTL 30s)
   
   Release Lock (Bắt buộc dùng Lua script để đảm bảo tính nguyên tử - Atomicity):
   if redis.call("get", KEYS[1]) == ARGV[1] then
       return redis.call("del", KEYS[1])
   else
       return 0
   end

2. Redlock Controversy (Martin Kleppmann vs Salvatore Sanfilippo):
   - Thuật toán Redlock: Client cố gắng lấy lock trên N (thường là 5) master Redis độc lập. Nếu lấy được quá bán (N/2 + 1) trong khoảng thời gian < TTL thì coi như thành công.
   - Phản biện của Martin Kleppmann: Redlock không an toàn cho các tác vụ cần tính đúng đắn tuyệt đối (safety-critical) vì:
     + System Clock Drift trên các node Redis phá vỡ giả định về thời gian timeout.
     + Stop-the-world GC pause kéo dài ở Client làm hết hạn Lock Lease mà Client không hay biết, dẫn đến 2 client cùng ghi dữ liệu một lúc!
   - Kết luận: Luôn kết hợp **Fencing Token** ở tầng Storage Engine nếu cần tính đúng đắn tuyệt đối!

3. ZooKeeper / etcd Distributed Lock:
   - Dựa trên cơ chế **Ephemeral Sequential ZNode** và **Watchers**.
   - Client tạo znode tuần tự: `/lock/lock-00000001`.
   - Node có sequence nhỏ nhất giữ lock. Các node khác chỉ Watch node liền trước mình (tránh Herd Effect).
   - Khi client crash, kết nối TCP đứt -> Ephemeral node tự động biến mất -> ZK đánh thức node tiếp theo.
   - **Ưu điểm**: An toàn tuyệt đối, không sợ clock drift, tự động giải phóng lock khi client chết.
```

---

### 11.7. Distributed Consensus: Raft vs Paxos

Thuật toán Đồng thuận (Consensus Algorithm) giải quyết bài toán: Làm thế nào để một tập hợp các máy chủ phân tán (Cluster) cùng đồng thuận về một chuỗi nhật ký lệnh (Replicated State Machine) ngay cả khi có một số máy chủ bị crash hoặc mạng bị ngắt kết nối.

```
Raft Consensus Model:
┌─────────────────┐       Heartbeat / AppendEntries       ┌─────────────────┐
│ Leader (Term 2) ├──────────────────────────────────────►│ Follower Node B │
└────────┬────────┘                                       └─────────────────┘
         │
         └───────────────────────────────────────────────►┌─────────────────┐
                                                          │ Follower Node C │
                                                          └─────────────────┘
```

#### 3 Trạng thái Node trong Raft:
- **Leader**: Tiếp nhận mọi request ghi từ client, ghi vào log nội bộ và replicate sang các Follower. Gửi heartbeat định kỳ để duy trì quyền lực.
- **Follower**: Hoàn toàn thụ động, chỉ phản hồi request từ Leader và Candidate.
- **Candidate**: Khi không nhận được heartbeat trong khoảng thời gian ngẫu nhiên (**Election Timeout**: $150-300\text{ ms}$), Follower nâng Term và tự ứng cử làm Candidate để bầu cử Leader mới.

#### Quy trình Bầu cử & Ghi Log trong Raft:
1. **Leader Election**: Candidate gửi `RequestVote` RPC tới tất cả node. Nếu nhận được quá bán phiếu bầu (Quorum = $\lfloor N/2 \rfloor + 1$), Candidate trở thành New Leader.
2. **Log Replication**: 
   - Client gửi lệnh `SET x = 5` tới Leader.
   - Leader ghi vào log tạm (Uncommitted) và gửi `AppendEntries` RPC tới các Follower.
   - Khi quá bán Follower đã ghi log thành công, Leader đánh dấu lệnh là **Committed**, áp dụng vào State Machine và trả về kết quả cho Client.
3. **Safety Guarantee (Leader Completeness)**: Một node chỉ có thể được bầu làm Leader nếu Log của nó chứa đầy đủ tất cả các entry đã từng được commit ở các Term trước (ngăn ngừa ghi đè dữ liệu cũ).

---

### 11.8. Định lý CAP & PACELC Deep Dive

```
Định lý CAP:                                 Định lý PACELC:
        Consistency (C)                                       ┌── (A)vailability
              /\                                              │
             /  \                                  Nếu có (P) ┤
            /    \                                            └── (C)onsistency
           /      \                                
          /        \                         Hệ thống
Availability (A)──Partition Tolerance (P)          │
                                                   │          ┌── (L)atency
                                                   └── Nếu (E)┤
                                                              └── (C)onsistency
```

#### 1. Định lý CAP (Eric Brewer)
Trong bất kỳ hệ thống dữ liệu phân tán nào, khi xảy ra **Network Partition (P)** (đứt cáp mạng giữa các datacenter, switch hỏng), hệ thống **BẮT BUỘC** phải đánh đổi giữa 2 lựa chọn:
- **CP (Consistency / Partition Tolerance)**: Từ chối phục vụ hoặc trả về lỗi nếu không đảm bảo dữ liệu mới nhất được đồng bộ qua phân vùng mạng. Ưu tiên tính nhất quán dữ liệu (Banking, HDFS, HBase, ZooKeeper, etcd).
- **AP (Availability / Partition Tolerance)**: Tiếp tục trả về dữ liệu khả dụng từ các node còn sống, chấp nhận dữ liệu có thể bị cũ (Stale Data). Ưu tiên tính sẵn sàng (DNS, Cassandra, CouchDB, DynamoDB).

#### 2. Định lý PACELC (Daniel Abadi)
Định lý CAP chỉ mô tả hành vi của hệ thống **khi có sự cố mạng (Partition)**. Trong điều kiện hoạt động bình thường (99.9% thời gian), hệ thống vận hành thế nào? PACELC ra đời để trả lời đầy đủ:
- **P / A - C**: Nếu xảy ra **Partition (P)**, hệ thống chọn **Availability (A)** hay **Consistency (C)**?
- **E / L - C**: **Else (E)** (khi mạng bình thường), hệ thống đánh đổi giữa **Latency (L)** hay **Consistency (C)**?

#### Bảng Phân loại Database Hiện đại theo PACELC:

| Hệ thống DB | Phân loại PACELC | Hành vi khi có sự cố mạng (P) | Hành vi khi mạng bình thường (E) |
| :--- | :--- | :--- | :--- |
| **Apache Cassandra** | **PA / EL** | Vẫn nhận đọc/ghi ở các node khả dụng (Eventual consistency). | Ghi trả về nhanh ngay khi đủ Quorum cục bộ, ưu tiên độ trễ cực thấp (Low Latency). |
| **Amazon DynamoDB** | **PA / EL** (mặc định) | Ưu tiên Availability tối đa (di sản của hệ Dynamo cho giỏ hàng). | Mặc định đọc *eventually consistent* (nhanh, rẻ); có tùy chọn *strongly consistent read* và transaction → khi đó chuyển về phía C. Global Tables (multi-region) dùng last-writer-wins. |
| **Google Spanner** | **PC / EC** | Chặn ghi nếu không đạt Quorum Paxos liên lục địa (Đảm bảo External Consistency). | Sử dụng đồng hồ nguyên tử (TrueTime API) để đảm bảo Strict Serializability, chấp nhận độ trễ commit cao hơn. |
| **MongoDB** | **PC / EC** (hoặc PC/EL tùy write concern) | Đóng ghi nếu Primary node bị cô lập khỏi đa số replica. | Với `w: majority`, chờ đa số replica ack để bảo vệ tính nhất quán dữ liệu. |
| **PostgreSQL (Sync Rep)** | **PC / EC** | Chặn ghi nếu Slave đồng bộ không phản hồi. | Client phải chờ câu lệnh commit xong ở cả Primary và Secondary. |

### 11.9. Consistency Models (từ mạnh → yếu)

| Mô hình | Bảo đảm | Ví dụ hệ thống |
| :--- | :--- | :--- |
| **Linearizability** (strong / atomic consistency) | Mọi thao tác như xảy ra **tức thời tại 1 thời điểm** giữa lúc gọi và lúc trả về; đọc luôn thấy bản ghi mới nhất **theo thời gian thực** | etcd, ZooKeeper (ghi), Spanner, 1 leader đọc/ghi |
| **Sequential consistency** | Mọi node thấy các thao tác theo **cùng 1 thứ tự**, giữ thứ tự của từng client, nhưng không ràng buộc thời gian thực | — |
| **Causal consistency** | Các thao tác có **quan hệ nhân quả** được mọi node thấy đúng thứ tự (câu trả lời không xuất hiện trước câu hỏi); thao tác đồng thời có thể khác thứ tự | MongoDB causal sessions, COPS |
| **Session guarantees** | *Read-your-writes*, *Monotonic reads*, *Monotonic writes*, *Writes-follow-reads* trong phạm vi 1 phiên | Sticky session, đọc từ leader sau khi ghi |
| **Eventual consistency** | Nếu ngừng ghi, cuối cùng mọi replica sẽ hội tụ — không bảo đảm gì trong lúc chờ | DNS, Cassandra (CL=ONE), S3 cũ (từ 12/2020 S3 đã strong read-after-write) |

* ⚠️ Linearizability (C trong CAP) ≠ Serializability (I trong ACID): serializability nói về **transaction nhiều object** tương đương chạy tuần tự (không nói thời gian thực); linearizability nói về **từng object đơn lẻ** theo thời gian thực. Cả hai = **strict serializability** (Spanner, CockroachDB ở mức gần đúng).
* Câu hỏi thiết kế điển hình: *"Hệ thống của bạn cần mức nhất quán nào?"* → số dư/tồn kho/khóa phân tán: strong; feed/like count/view count: eventual; hồ sơ người dùng vừa sửa: read-your-writes là đủ.

### 11.10. Thời gian & Thứ tự trong hệ phân tán
* **Đồng hồ vật lý không đáng tin**: lệch giữa các máy (clock skew) vài ms–vài trăm ms, NTP có thể kéo lùi giờ, VM bị pause. → Không dùng `timestamp` máy để quyết định "cái nào xảy ra trước" giữa các node (LWW có thể mất dữ liệu — `Database.md` 19.7).
* **Lamport Clock**: mỗi node giữ counter; tăng mỗi sự kiện; gửi kèm message; nhận thì `counter = max(local, received) + 1`. Đảm bảo: A → B (nhân quả) thì `L(A) < L(B)` (nhưng không suy ngược) → tạo được **thứ tự toàn phần nhất quán** (kèm node id để phá hòa).
* **Vector Clock**: phát hiện được 2 sự kiện **đồng thời** (concurrent) — dùng để phát hiện xung đột (`Database.md` 19.7).
* **Hybrid Logical Clock (HLC)**: kết hợp thời gian vật lý + counter logic → gần với giờ thật mà vẫn giữ quan hệ nhân quả (CockroachDB, YugabyteDB, MongoDB).
* **Google TrueTime** (Spanner): API trả về **khoảng** `[earliest, latest]` nhờ đồng hồ nguyên tử + GPS ở mỗi datacenter (sai số ~vài ms); khi commit, Spanner **chờ hết khoảng bất định** (*commit wait*) → đảm bảo external consistency toàn cầu.

### 11.11. Leader Election, Lease & Heartbeat
* Nhiều hệ thống cần **đúng 1** node làm việc gì đó (master DB, scheduler, consumer partition đặc biệt).
* Cách làm: dùng dịch vụ đồng thuận có sẵn — **etcd/ZooKeeper/Consul** (ephemeral node + watch, hoặc lease có TTL), K8s **Lease** object (client-go leader election), hoặc DB lock (`pg_advisory_lock`).
* **Lease**: quyền leader có thời hạn, phải gia hạn định kỳ; mất kết nối thì hết hạn và node khác tiếp quản. Leader cũ có thể chưa biết mình đã mất quyền (GC pause, mạng lag) → vẫn cần **fencing token** ở tầng tài nguyên (11.4).
* **Phát hiện lỗi (failure detection)**: heartbeat + timeout — timeout ngắn → phát hiện nhanh nhưng dễ báo nhầm (false positive → failover không cần thiết → split-brain); dài → phản ứng chậm. *Phi Accrual Failure Detector* (Cassandra, Akka) tính xác suất node chết dựa trên phân phối độ trễ heartbeat thay vì ngưỡng cố định.

### 11.12. Cấu trúc dữ liệu xác suất (Probabilistic Data Structures)
| Cấu trúc | Trả lời câu hỏi | Bộ nhớ | Ứng dụng |
| :--- | :--- | :--- | :--- |
| **Bloom Filter** | "Phần tử X **chắc chắn không có** / **có thể có**?" | ~10 bit/phần tử cho 1% FP | Chống cache penetration, crawler URL đã thăm, SSTable (`Database.md` 9.1) |
| **HyperLogLog** | "Có **bao nhiêu phần tử khác nhau**?" (cardinality) | ~12 KB cho hàng tỷ phần tử, sai số ~0.81% | Đếm unique visitors/DAU (`PFADD`/`PFCOUNT` trong Redis) |
| **Count-Min Sketch** | "Phần tử X xuất hiện **bao nhiêu lần**?" (ước lượng trên, không bao giờ thấp hơn thực tế) | Ma trận d × w counter | Top-K / heavy hitters (trending hashtag), phát hiện hot key, rate limit gần đúng |
| **Top-K (Heavy Keeper / Space-Saving)** | "K phần tử phổ biến nhất?" | O(K) | Trending, hot product |
| **t-digest / KLL** | Percentile (p99) trên luồng dữ liệu | Nhỏ, gộp được giữa các node | Latency percentile trong hệ thống metrics |

✅ **Nguyên tắc chung khi chọn:** dùng hệ thống đã hiện thực sẵn các pattern này (etcd/ZooKeeper cho consensus và leader election, Kafka cho log có thứ tự, DB có transaction) thay vì tự viết — các lỗi trong thuật toán phân tán chỉ lộ ra khi có sự cố hiếm gặp.

> 🛠 **Áp dụng:**
> - Cần khóa phân tán để **tối ưu** (tránh làm trùng việc) → Redis lock là đủ; cần khóa cho **đúng đắn** (không được ghi hai lần) → khóa trên hệ consensus + **fencing token** được kiểm tra tại nơi ghi.
> - Không dùng timestamp của máy để quyết định thứ tự giữa các máy.

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao Redis lock có TTL vẫn có thể để hai client cùng ghi?**
   → Client A bị GC pause/mạng chậm quá TTL, lock hết hạn, B lấy lock và ghi; A tỉnh dậy vẫn tưởng mình giữ lock và ghi tiếp. Chỉ fencing token kiểm tra ở storage mới chặn được A.
2. **Vì sao cluster Raft thường có số node lẻ?**
   → Quorum là đa số: 4 node chịu được 1 node chết, bằng với 3 node, nhưng tốn thêm máy và thêm độ trễ ghi.
</details>


---

## 12. System Design Interview Framework & Back-of-Envelope

> 🎯 **Bài toán:** 45 phút, đề bài mơ hồ ("thiết kế Twitter"). Lỗi phổ biến nhất là nhảy ngay vào vẽ Kafka/Redis mà chưa biết hệ thống cần gì.
>
> 💡 **Ý tưởng:** Đi theo chuỗi **yêu cầu → con số → thiết kế đơn giản nhất chạy được → tìm nút cổ chai → giải từng nút với trade-off rõ ràng**. Người phỏng vấn đánh giá **lập luận "vì sao"** nhiều hơn là danh sách công nghệ.


### 12.1. Khung 4 bước chuẩn mực khi phỏng vấn (4-Step Interview Framework)

Thời lượng chuẩn: **45 - 60 phút**.

```
┌────────────────────────────────────────────────────────────────────────┐
│ Bước 1: Làm rõ Yêu cầu & Scope (5 - 8 phút)                           │
│ - Functional Requirements (3 - 4 tính năng cốt lõi nhất)               │
│ - Non-Functional Requirements (High Availability, Latency, Scale, CAP)│
│ - Out of Scope (Những tính năng KHÔNG làm trong buổi phỏng vấn)       │
├────────────────────────────────────────────────────────────────────────┤
│ Bước 2: Ước lượng Back-of-the-Envelope (5 phút)                        │
│ - Daily Active Users (DAU), Read/Write Ratio, QPS (Average & Peak)     │
│ - Storage Capacity (5 năm), Băng thông Mạng (Inbound/Outbound)        │
├────────────────────────────────────────────────────────────────────────┤
│ Bước 3: High-Level Architecture Design (10 - 15 phút)                  │
│ - Vẽ sơ đồ khối các components: Client, CDN, LB, API Gateway, Services│
│ - Định nghĩa Data Model (Schema SQL/NoSQL) & Core API Signatures       │
├────────────────────────────────────────────────────────────────────────┤
│ Bước 4: Deep Dive & Bottleneck Resolution (20 - 25 phút)               │
│ - Đào sâu vào 2-3 vấn đề hóc búa nhất theo yêu cầu của Interviewer    │
│ - Scale bottlenecks: Caching, Database Partitioning, Message Queues   │
│ - Failure modes: Node crash, Network partition, Data inconsistency    │
└────────────────────────────────────────────────────────────────────────┘
```

---

### 12.2. Các con số quan trọng mọi Kỹ sư cần thuộc lòng

#### Bảng đơn vị luỹ thừa của 2:
* $2^{10} \approx 1\text{ Thousand} = 1\text{ KB}$ (Kilobyte)
* $2^{20} \approx 1\text{ Million} = 1\text{ MB}$ (Megabyte)
* $2^{30} \approx 1\text{ Billion} = 1\text{ GB}$ (Gigabyte)
* $2^{40} \approx 1\text{ Trillion} = 1\text{ TB}$ (Terabyte)
* $2^{50} \approx 1\text{ Petabyte} = 1\text{ PB}$

#### Ước lượng thời gian & độ trễ phần cứng (Latency Numbers Every Programmer Should Know):
* **L1 Cache reference**: $0.5\text{ ns}$
* **Branch mispredict**: $5\text{ ns}$
* **L2 Cache reference**: $7\text{ ns}$
* **Mutex lock/unlock**: $25\text{ ns}$
* **Main Memory (RAM) reference**: $100\text{ ns}$
* **Đọc 1 MB tuần tự từ Memory (RAM)**: $250\text{ }\mu\text{s}$ ($0.25\text{ ms}$)
* **Đọc 1 MB tuần tự từ NVMe SSD**: $1\text{ ms}$
* **Đọc 1 MB tuần tự từ Đĩa từ (HDD)**: $20\text{ ms}$
* **Round-trip trong cùng 1 Datacenter**: $0.5\text{ ms}$
* **Round-trip xuyên lục địa (US to Europe)**: $150\text{ ms}$

> 📌 Đây là bộ số "kinh điển" (Jeff Dean / Peter Norvig, ~2010). Phần cứng hiện đại nhanh hơn ở một số mục (đọc tuần tự 1 MB từ RAM ~ vài chục µs, NVMe ~ 100–300 µs, 1 random read NVMe ~ 10–100 µs), nhưng **tỷ lệ giữa các tầng** mới là điều cần nhớ: RAM nhanh hơn SSD ~100–1000×, SSD nhanh hơn HDD random I/O ~100×, mạng trong DC ~0.5 ms, liên lục địa ~100+ ms (giới hạn bởi tốc độ ánh sáng).

#### Công thức chuyển đổi QPS & Dung lượng chuẩn:
* $1\text{ ngày} = 86,400\text{ giây} \approx 100,000\text{ giây} = 10^5\text{ s}$ (Làm tròn để tính nhẩm nhanh).
* **Average QPS** $= \frac{\text{Daily Active Users (DAU)} \times \text{Average Requests per User per Day}}{100,000\text{ s}}$.
* **Peak QPS** $= \text{Average QPS} \times 2$ (hoặc $\times 5$ với các event bùng nổ traffic).
* **Storage** $= \text{Số bản ghi mới/ngày} \times \text{kích thước TB/bản ghi} \times 365 \times \text{số năm} \times \text{replication factor}$ (thường ×3).
* **Bandwidth** $= \text{QPS} \times \text{kích thước response}$ (vd 10k QPS × 100 KB = 1 GB/s ≈ 8 Gbps → cần CDN).
* **Cache memory** (quy tắc 80/20): cache 20% dữ liệu "nóng" của 1 ngày đọc: $0.2 \times \text{read QPS} \times 86400 \times \text{kích thước object}$.
* **Số server ước lượng** $= \text{Peak QPS} / \text{QPS mỗi server chịu được}$ (1 server web Java xử lý vài nghìn req/s cho API đơn giản; 1 PostgreSQL vài nghìn–vài chục nghìn TPS tùy query; 1 Redis ~100k ops/s; 1 partition Kafka ~10 MB/s).
* Ví dụ nhanh: 100M DAU, mỗi user 10 request/ngày → $10^9 / 10^5 = 10^4$ = **10k QPS trung bình**, peak ~20–50k QPS.

### 12.3. Checklist trade-off khi Deep Dive
| Câu hỏi | Lựa chọn thường gặp |
| :--- | :--- |
| Đọc nhiều hay ghi nhiều? Tỷ lệ? | Read-heavy → cache, replica, CDN; Write-heavy → queue, batch, LSM store, sharding |
| Cần nhất quán mạnh hay eventual? | Tiền/tồn kho → strong (SQL transaction, lock, consensus); feed/counter → eventual |
| Đồng bộ hay bất đồng bộ? | Người dùng cần kết quả ngay → sync; còn lại → async qua queue (giảm latency, chịu lỗi tốt hơn) |
| Push hay pull? | Fan-out on write vs on read (Case 4), WebSocket vs polling |
| SQL hay NoSQL? | Quan hệ & transaction → SQL; access pattern đơn giản, scale ghi lớn → NoSQL (6.5) |
| Điểm lỗi đơn (SPOF) ở đâu? | Thêm replica, multi-AZ, failover tự động |
| Hot spot ở đâu? | Hot key/hot partition → local cache, key splitting, thêm salt |
| Khi một thành phần chết thì sao? | Timeout, retry, circuit breaker, fallback, DLQ (5.2) |
| Đo lường thế nào? | Metric SLI, alert theo SLO, tracing (9) |

---

## 13. Common System Design Interview Questions (Deep Dives)

> 📌 **Cách đọc các case:** mỗi case có một **bài toán cốt lõi** quyết định thiết kế (ô 🎯 ở đầu mỗi case). Hãy tự trả lời câu hỏi đó trước khi đọc lời giải; chú ý **vì sao** chọn phương án này mà không chọn phương án khác — đó là phần được chấm điểm.


---

### Case 1: Thiết kế Hệ thống Rút gọn URL (TinyURL / Bit.ly)

> 🎯 **Bài toán cốt lõi:** Sinh mã ngắn **duy nhất** với tốc độ cao, và phục vụ **đọc gấp 100 lần ghi** với latency cực thấp.
>
> ✅ **Quyết định then chốt:** ID tăng dần (cấp theo dải) + Base62 → không va chạm, không cần kiểm tra DB (hash thì phải xử lý va chạm); cache + CDN cho đường đọc; **302** nếu cần analytics, **301** nếu chỉ cần giảm tải.


#### 1. Yêu cầu hệ thống:
* **Functional**:
  * Cho một Long URL -> Tạo một Short URL duy nhất (e.g., `https://tiny.url/aB7zK9`).
  * Khi truy cập Short URL -> Redirect về Long URL gốc với HTTP 301 hoặc 302.
  * Tùy chọn đặt link alias tùy chỉnh và hạn sử dụng (Expiration date).
* **Non-Functional**: Độ trễ redirect cực thấp ($< 10\text{ ms}$), High Availability (99.99%), Read-heavy (Tỷ lệ Read/Write = 100:1).

#### 2. Back-of-the-envelope Calculation:
* Giả sử: 100M URLs mới được tạo mỗi tháng -> Write QPS $= \frac{100\text{M}}{30 \times 86400} \approx 40\text{ req/s}$.
* Read QPS (Tỷ lệ 100:1) $= 40 \times 100 = 4,000\text{ req/s}$ (Peak Read $= 8,000\text{ req/s}$).
* Storage 5 năm: $100\text{M} \times 12 \times 5 = 6\text{ Billion records}$. Mỗi record $\approx 500\text{ bytes} \to 6\text{B} \times 500\text{B} = 3\text{ TB}$ (Rất nhỏ, vừa trên 1 server database hiện đại).

#### 3. Core Algorithm: Base62 Encoding vs Key Generation Service (KGS)

```
Tại sao chọn Base62?
Bảng ký tự: [0-9, a-z, A-Z] = 10 + 26 + 26 = 62 ký tự.
Độ dài chuỗi 7 ký tự Base62 tạo được:
62^7 = 3.52 nghìn tỷ URLs duy nhất (Đủ dùng cho hàng trăm năm).
```

* **Cách tiếp cận 1: Hash Long URL (`MD5/SHA-256`) lấy 7 ký tự đầu**:
  * *Vấn đề*: Rất dễ bị Hash Collision. Xử lý collision bằng cách append thêm chuỗi salt và hash lại -> Tốn nhiều I/O query check DB.
* **Cách tiếp cận 2: Sử dụng Distributed Unique ID Generator rồi convert sang Base62**:
  * `ID = 10293847561` -> `Base62(ID)` = chuỗi 6 ký tự -> 100% Unique, không bao giờ collision.
  * ⚠️ ID Snowflake 64-bit (~19 chữ số) khi đổi sang Base62 dài **~11 ký tự**, không phải 7. Muốn URL ngắn 7 ký tự → dùng **counter tăng dần** (ticket server / segment-based như Meituan Leaf, mỗi server xin trước 1 dải ID) — $62^7 \approx 3.5 \times 10^{12}$ đủ dùng.
  * ⚠️ ID tăng dần → URL **đoán được** (enumeration, lộ số lượng link) → có thể xáo trộn bằng phép biến đổi song ánh (vd nhân với số nguyên tố modulo $62^7$, hoặc mã hóa block nhỏ) trước khi encode.
* **Cách tiếp cận 3: Key Generation Service (KGS) độc lập**:
  * Một service sinh sẵn hàng tỷ chuỗi 7 ký tự Base62 ngẫu nhiên từ trước và lưu vào 2 bảng `used_keys` và `unused_keys`.
  * KGS load sẵn một dải keys vào RAM. Khi có request -> Lấy ngay 1 key từ RAM và gán mà không cần tính toán tại runtime.

#### 4. Architecture Diagram:

```
[ Client ] ──► [ CDN (Edge Redirect) ] ──► [ Load Balancer ]
                                                    │
             ┌──────────────────────────────────────┴──────────────────────────────────────┐
             ▼                                                                             ▼
    [ Write URL Service ]                                                         [ Read URL Service ]
             │                                                                             │
     (Get ID from KGS)                                                              (Query Cache first)
             ▼                                                                             ▼
   [ Database (NoSQL) ] ◄─────────────── Sync Updates ────────────────────────── [ Redis Cache Cluster ]
  (DynamoDB / Cassandra)                                                           (LRU Eviction 20% Hot URLs)
```

#### 5. HTTP 301 vs 302 Redirect:
* **HTTP 301 (Moved Permanently)**: Trình duyệt lưu cache redirect tại local. Các lần truy cập sau của user không gửi request về TinyURL server nữa -> **Giảm tải tối đa cho server, nhưng mất khả năng thu thập Analytics/Click tracking**.
* **HTTP 302 (Found / Temporary Redirect)**: Mọi request đều phải chạy qua TinyURL server rồi mới redirect -> **Chính xác cho Click Analytics, đếm lượt click theo thời gian thực**.

---

### Case 2: Thiết kế Hệ thống Giới hạn tốc độ phân tán (Distributed Rate Limiter)

> 🎯 **Bài toán cốt lõi:** Đếm số request của mỗi người dùng **chính xác trên nhiều instance** mà không làm chậm mọi request.
>
> ✅ **Quyết định then chốt:** Bộ đếm tập trung trong Redis + **Lua script** để kiểm tra-và-trừ nguyên tử (tránh race giữa nhiều instance); token bucket vì cho phép burst và chỉ cần 2 giá trị mỗi key; fail-open khi Redis lỗi để limiter không thành điểm chết.


#### 1. Kiến trúc hệ thống:

```
[ Client ] ──► [ API Gateway / NGINX ]
                      │
            (Execute Lua Script)
                      ▼
          [ Redis Cluster (Master-Replica) ]
          - Token Bucket State per Client IP
          - In-Memory Atomic Execution
```

#### 2. Các điểm Deep-Dive then chốt:
* **Data Locality & Multi-Tier Rate Limiting**:
  * *Lớp 1 (Local In-Memory Cache tại Gateway Pod)*: Giới hạn sơ bộ các IP spam tấn công DoS thô bạo mà không cần tốn network call tới Redis.
  * *Lớp 2 (Centralized Redis Cluster)*: Rate limiting chính xác dùng Token Bucket Lua Script.
* **Race Condition trong môi trường phân tán**:
  * Tuyệt đối không dùng mô hình: (1) `GET count` -> (2) Check if count > limit -> (3) `SET count = count + 1` ở code ứng dụng vì nhiều thread/pod chạy song song sẽ gây ra race condition.
  * Phải gom toàn bộ logic check và update vào trong **duy nhất 1 Redis Lua Script** để Redis thực thi nguyên tử (Atomic).
* **HTTP Response Headers cho Rate Limiting**:
  * Phổ biến (de facto): `X-RateLimit-Limit` (số request tối đa trong window), `X-RateLimit-Remaining` (còn lại), `X-RateLimit-Reset` (thời điểm reset).
  * Chuẩn HTTP: **`Retry-After`** (RFC 9110) đi kèm `429 Too Many Requests` hoặc `503` — số giây (hoặc thời điểm) client nên chờ.
  * IETF đang chuẩn hóa header `RateLimit` / `RateLimit-Policy` (draft) để thay các header `X-` không chuẩn.
* **Fail-open hay fail-closed khi Redis chết?** Thường **fail-open** (cho request qua, fallback về local limiter) để rate limiter không trở thành SPOF làm sập toàn hệ thống; với API nhạy cảm (login/OTP) có thể fail-closed.
* **Giới hạn theo gì?** user id / API key (chính xác hơn IP vì nhiều user chung NAT), IP (cho endpoint chưa đăng nhập), endpoint, tenant; nhiều tầng: global → per-tenant → per-user.

---

### Case 3: Thiết kế Hệ thống Chat Thời Gian Thực (WhatsApp / Slack / Messenger)

> 🎯 **Bài toán cốt lõi:** Đẩy tin nhắn tới người nhận **ngay lập tức** khi họ online, **không mất tin** khi họ offline, đúng thứ tự trong một cuộc hội thoại.
>
> ✅ **Quyết định then chốt:** WebSocket giữ kết nối lâu (server chủ động đẩy) + registry "user đang kết nối ở gateway nào"; lưu tin vào DB ghi nhiều (Cassandra) **trước** khi đẩy; sequence ID theo hội thoại để sắp xếp và đồng bộ khi kết nối lại.


#### 1. Yêu cầu hệ thống:
* Chat 1-1 và Chat nhóm (Group chat lên tới 500 người).
* Hiển thị trạng thái Online/Offline của bạn bè (Presence Status).
* Đánh dấu trạng thái tin nhắn: Sent -> Delivered -> Read.
* Lưu trữ lịch sử chat vĩnh viễn, đồng bộ trên nhiều thiết bị (Multi-device sync).

#### 2. Architecture Diagram:

```
[ Client A ] ═══════ WebSocket (TLS) ═══════► [ Chat Server 1 ]
                                                    │
                                          (Publish Message Event)
                                                    ▼
                                          [ Apache Kafka Cluster ]
                                                    │
                                     ┌──────────────┴──────────────┐
                                     ▼                             ▼
                           [ Message Storage Srv ]       [ Chat Server 2 ]
                                     │                             │
                        (Batch Write to Cassandra)      (Push to Recipient)
                                     ▼                             ▼
                            [ Cassandra NoSQL ]            [ Client B ]
```

#### 3. Deep-Dive Design Decisions:

```
Cassandra Data Model cho Chat Message:
CREATE TABLE group_messages (
    group_id uuid,
    bucket int,          -- vd: yyyyMM hoặc số thứ tự theo tuần → giới hạn kích thước partition
    message_id timeuuid, -- Đảm bảo Unique và Tự động sắp xếp theo thời gian
    sender_id uuid,
    content text,
    media_url text,
    PRIMARY KEY ((group_id, bucket), message_id)
) WITH CLUSTERING ORDER BY (message_id DESC);   -- DESC: lấy tin mới nhất trước (màn hình chat)
```
> ⚠️ Nếu partition key chỉ là `group_id`, nhóm chat hoạt động lâu năm tạo **partition không giới hạn** (Cassandra khuyến nghị partition < ~100 MB) → hotspot & chậm. Thêm **bucket theo thời gian** vào partition key; khi đọc lịch sử thì duyệt lùi từng bucket.

* **Giao thức kết nối**:
  * Client kết nối tới Chat Server qua **WebSocket kết nối dài (Persistent Connection)**.
  * Chat Server lưu session mapping vào Redis: `user_id -> server_ip:port`.
* **Flow gửi tin nhắn 1-1**:
  1. Client A gửi message tới `Chat Server 1` qua WebSocket.
  2. `Chat Server 1` cấp phát `MessageID` (Snowflake ID có chứa timestamp).
  3. `Chat Server 1` bắn message vào Kafka topic.
  4. Worker đọc Kafka lưu message vào **Cassandra** (Tối ưu cực cao cho Write throughput và query lịch sử theo time range).
  5. Tra cứu Redis tìm thấy Client B đang kết nối tại `Chat Server 2`.
  6. Message được forward tới `Chat Server 2` qua internal gRPC và đẩy xuống Client B qua WebSocket.
  7. Nếu Client B đang offline -> Bắn notification qua **Push Notification Service (APNs / FCM)**.
* **Presence System (Trạng thái Online/Offline)**:
  * Client gửi Heartbeat qua WebSocket mỗi 5 giây.
  * Redis lưu trữ key với TTL ngắn: `SET online:user_123 "true" EX 15`.
  * Nếu quá 15 giây không nhận được heartbeat -> Key tự hết hạn trong Redis -> User chuyển sang trạng thái Offline.
  * Thông báo trạng thái cho bạn bè qua pub/sub; với user có hàng nghìn bạn hoặc nhóm lớn → chỉ cập nhật presence khi người kia **mở** cuộc trò chuyện (lazy) để tránh fan-out bùng nổ.
* **Đảm bảo không mất & không trùng tin nhắn**:
  * Client gắn **client_msg_id** (UUID) cho mỗi tin → server dedupe khi client gửi lại do mất mạng.
  * Mỗi cuộc hội thoại có **sequence number tăng dần** (do server cấp) → client phát hiện thiếu tin (gap) để kéo bù khi reconnect: `GET /messages?conv=X&after_seq=1234`.
  * Trạng thái *Delivered/Read* là các event ACK từ client người nhận, lưu theo `(conversation, user) → last_read_seq` thay vì từng tin.
* **Nhóm lớn** (hàng nghìn thành viên): fan-out-on-write ghi vào inbox từng người rất tốn → chuyển sang fan-out-on-read (mỗi người kéo tin từ timeline của nhóm), tương tự Case 4.
* **End-to-end encryption** (Signal Protocol): server chỉ chuyển tiếp ciphertext, khóa nằm trên thiết bị → server không đọc/tìm kiếm được nội dung; multi-device phức tạp hơn.

---

### Case 4: Thiết kế News Feed / Timeline (Twitter / Facebook / Instagram)

> 🎯 **Bài toán cốt lõi:** Đọc timeline rất thường xuyên, nhưng gom bài từ hàng trăm người đang theo dõi lúc đọc thì quá chậm.
>
> ✅ **Quyết định then chốt:** **Fan-out on write** (đẩy bài vào timeline đã tính sẵn của từng follower) để đọc O(1); nhưng với người nổi tiếng có hàng triệu follower thì chuyển sang **fan-out on read** → mô hình **lai**.


#### 1. Hai mô hình cốt lõi: Fan-out-on-Write vs Fan-out-on-Read

```
1. Fan-out on Write (Push Model):
   User đăng bài ──► Lấy list 5,000 Followers ──► Ghi bài viết vào 5,000 Feed Cache của từng Follower!
   - Ưu điểm: Read News Feed siêu nhanh (O(1) từ Redis List cá nhân).
   - Nhược điểm: Celebrity Problem (KOL có 50M followers đăng bài -> Server nghẽn vì phải ghi vào 50M cache).

2. Fan-out on Read (Pull Model):
   User đăng bài ──► Chỉ lưu vào DB bài viết của User.
   Follower mở App ──► Lấy list Following ──► Query lấy bài mới nhất của tất cả Following ──► Merge & Sort!
   - Ưu điểm: Đăng bài cực nhanh, không tốn tài nguyên ghi fan-out.
   - Nhược điểm: Đọc News Feed rất chậm (N+1 query, tốn CPU merge sort tại runtime).
```

#### 2. Kiến trúc Hybrid tối ưu (Thực tế của Twitter/Instagram):

```
                   ┌── [ Celebrity / KOL (> 20k Followers) ] ──► Fan-out on Read (Pull on demand)
[ User Post ] ─────┤
                   └── [ Regular User (< 20k Followers) ]   ──► Fan-out on Write (Push to Timeline)

Khi User bình thường mở App:
Read Timeline = (Lấy Pre-computed Feed từ Redis List) MERGE (Pull bài mới của các KOL đang follow).
```

---

### Case 5: Thiết kế Hệ thống Thông báo Phân tán (Notification System)

> 🎯 **Bài toán cốt lõi:** Gửi hàng triệu thông báo qua nhiều kênh (push, SMS, email) mà nhà cung cấp bên ngoài có thể chậm/lỗi, **không gửi trùng**, tôn trọng tùy chọn người dùng.
>
> ✅ **Quyết định then chốt:** Queue riêng cho từng kênh (một kênh chậm không chặn kênh khác), worker có retry + backoff + DLQ, **khóa idempotency** để không gửi trùng khi retry, rate limit theo nhà cung cấp.


```
[ Business Services ] ──► [ Notification Gateway ]
                                  │
                       (Rate Limit & Deduplication)
                                  │
                 ┌────────────────┼────────────────┐
                 ▼                ▼                ▼
         [ Kafka: iOS ]    [ Kafka: Android ] [ Kafka: SMS/Email ]
                 │                │                │
                 ▼                ▼                ▼
           [ iOS Worker ]  [ Android Worker ] [ Third-party Worker ]
                 │                │                │
                 ▼                ▼                ▼
            [ Apple APNs ]    [ Google FCM ]    [ Twilio / SendGrid ]
                 │                │                │
                 ▼                ▼                ▼
             [ iPhone ]     [ Android Phone ]   [ SMS / Email ]
```

#### Các thành phần quan trọng:
1. **Deduplication Engine**: Dùng Redis caching `hash(userId + eventType + payload)` với TTL ngắn (10 phút) để ngăn chặn việc gửi trùng lặp cùng 1 notification do retry lỗi từ service nguồn.
2. **User Preferences & Rate Limiting**: Kiểm tra cấu hình của user (e.g., Tắt nhận marketing notification, Không làm phiền từ 22:00 - 07:00) trước khi đẩy vào Worker.
3. **Priority Queues**: Tách biệt queue thông báo khẩn cấp (Mã OTP đăng nhập, Cảnh báo trừ tiền) với queue thông báo độ ưu tiên thấp (Khuyến mãi, Gợi ý kết bạn).

---

### Case 6: Thiết kế Search Autocomplete / Typeahead (Google Search Box)

> 🎯 **Bài toán cốt lõi:** Trả về gợi ý cho **mỗi lần gõ phím** trong vài chục ms trên hàng tỷ truy vấn.
>
> ✅ **Quyết định then chốt:** **Tính trước** top-K cho mỗi tiền tố (trie/bảng prefix → top-K) bằng batch từ log truy vấn, phục vụ hoàn toàn từ bộ nhớ/cache; chấp nhận dữ liệu cập nhật chậm vài phút–giờ để đường đọc cực nhanh.


#### 1. Cấu trúc dữ liệu: Trie kết hợp Top-K Caching

```
Trie Node Structure:
                     (root)
                    /      \
                  [a]      [c]
                  /          \
                [am]        [ca]
               /    \         \
            [ama]  [ame]     [cat] ◄── Node lưu Top-K: ["cat food", "cat tree", "cats"]
```

* **Cải tiến Trie truyền thống**:
  * Trie chuẩn yêu cầu duyệt đệ quy xuống toàn bộ cây con từ prefix -> Cực chậm với hàng triệu từ.
  * **Tối ưu**: Tại mỗi Node của Trie, lưu sẵn **Top 5 hoặc Top 10 cụm từ tìm kiếm phổ biến nhất** kèm theo tần suất (Frequency score) tính toán từ trước (Offline Aggregation).
  * Tra cứu autocomplete cho prefix bất kỳ chỉ tốn thời gian **$O(L)$** ($L$ là độ dài từ khóa người dùng đang gõ, $L \le 20\text{ characters} \to O(1)$).

#### 2. Kiến trúc Offline Aggregation & Real-time Serving:
* **Real-time Serving Path**: Trình duyệt -> CDN Edge Cache -> Redis In-Memory Trie Cache -> Trả về danh sách gợi ý trong $< 5\text{ ms}$.
* **Offline Processing Path**: User search log -> Apache Kafka -> Apache Flink / Spark Streaming -> Tổng hợp tần suất từ khóa theo tuần -> Build ra snapshot Trie mới -> Đẩy vào Redis Cache.

---

### Case 7: Thiết kế Nền tảng Video Streaming (YouTube / Netflix)

> 🎯 **Bài toán cốt lõi:** File video rất lớn, người xem có thiết bị và băng thông khác nhau, khắp thế giới.
>
> ✅ **Quyết định then chốt:** Chuyển mã thành nhiều độ phân giải và **chia đoạn nhỏ** (HLS/DASH) → trình phát tự chọn chất lượng theo băng thông (adaptive bitrate); phục vụ qua **CDN** vì băng thông là chi phí lớn nhất; xử lý transcode bằng pipeline bất đồng bộ song song theo đoạn.


```
[ Creator Video Upload ]
           │
           ▼
[ Upload Service ] ──► [ Raw Storage (S3) ]
                              │
                    (Message: NewVideoUploaded)
                              ▼
                [ Video Transcoding Pipeline ]
                (Phân mảnh video thành Chunks 4s-10s)
                (Encode đa độ phân giải: 4K, 1080p, 720p, 360p)
                (Hỗ trợ HLS / DASH adaptive bitrate)
                              │
                              ▼
                 [ Processed Storage (S3) ]
                              │
                              ▼
                   [ Global Edge CDN ]
                              │
                              ▼
                     [ User Video Player ]
          (Tự động điều chỉnh chất lượng theo tốc độ mạng)
```

* **Adaptive Bitrate Streaming (ABR)**:
  * Video được cắt thành hàng ngàn chunk nhỏ (thời lượng 2 đến 6 giây mỗi file `.ts` hoặc `.m4s`).
  * File Playlist Manifest (`.m3u8` cho **HLS** hoặc `.mpd` cho **DASH**) chứa URL của tất cả các chunks ở các mức độ phân giải khác nhau.
  * Video Player phía Client liên tục đo lường băng thông mạng khả dụng: Nếu mạng nhanh -> Tự kéo chunk 1080p; nếu mạng chập chờn -> Tự hạ xuống chunk 480p mượt mà không bị xoay vòng buffering.
* **CDN Edge Caching Strategy**: 80% lưu lượng xem video tập trung vào 20% video thịnh hành nhất (Quy luật Pareto). Lưu cache toàn bộ chunk của các video Hot tại CDN Edge; các video cũ (Cold video) chỉ lưu tại Origin S3 và kéo về theo cơ chế Pull CDN khi có request.

---

### Case 8: Thiết kế Hệ thống Thanh toán Kỹ thuật số (Payment System)

> 🎯 **Bài toán cốt lõi:** **Không bao giờ trừ tiền hai lần hoặc mất tiền**, dù mạng lỗi, retry, hay nhà cung cấp thanh toán trả lời chậm.
>
> ✅ **Quyết định then chốt:** **Idempotency key** cho mọi thao tác; trạng thái thanh toán là máy trạng thái lưu bền; **sổ cái kép** (double-entry ledger) chỉ ghi thêm; đối soát định kỳ với nhà cung cấp — chọn **nhất quán** thay vì tốc độ.


```
[ Checkout Service ] ──1. POST /payments (Idempotency-Key: abc-123)──► [ Payment Gateway ]
                                                                               │
                                                                   (Check Key in Redis/DB)
                                                                               │
                                                                               ▼
[ Double-Entry Ledger DB ] ◄──2. Atomic Write (Debit & Credit)────── [ Payment Engine ]
                                                                               │
                                                               3. Call External PSP
                                                               (Stripe / PayPal / Bank)
```

#### 3 Nguyên tắc bất di bất dịch trong Thiết kế Hệ thống Thanh toán:

1. **Idempotency (Tính bất biến)**:
   * Client gửi request thanh toán bắt buộc kèm `Idempotency-Key` (UUIDv4).
   * Payment Service lưu trạng thái của key vào Redis/Database. Nếu client retry do rớt mạng -> Service nhận ra key đã tồn tại và trả về kết quả cũ, tuyệt đối không trừ tiền thẻ tín dụng 2 lần.
2. **Double-Entry Bookkeeping (Kế toán kép)**:
   * Mọi giao dịch tài chính bắt buộc phải ghi nhận ít nhất 2 dòng dữ liệu cân bằng: **Nợ (Debit)** và **Có (Credit)**.
   * Tổng số tiền Debit luôn phải bằng chính xác tuyệt đối tổng số tiền Credit: $\sum \text{Debit} - \sum \text{Credit} = 0$. Tiền không tự nhiên sinh ra hay mất đi.
3. **Reconciliation (Đối soát định kỳ)**:
   * Một tiến trình Background độc lập chạy vào cuối ngày, lấy file sao kê giao dịch từ Ngân hàng / PSP (Stripe, PayPal) và so khớp từng dòng với Ledger Database nội bộ để phát hiện sai lệch và tự động kích hoạt xử lý bù trừ.

#### Các điểm deep-dive thêm:
* **Lưu idempotency key không bị race**: `INSERT INTO idempotency_keys(key, status, request_hash) VALUES (?, 'IN_PROGRESS', ?)` với **UNIQUE(key)** — request thứ 2 cùng key bị lỗi unique → chờ/trả kết quả của request đầu; khi xong cập nhật `status = COMPLETED, response = ...`. Kiểm tra `request_hash` để phát hiện client dùng lại key cho request khác. (Kiểm tra bằng `GET` trong Redis rồi mới `SET` là **sai** — race condition; nếu dùng Redis thì `SET key val NX`.)
* **State machine của payment**: `CREATED → PENDING (đã gửi PSP) → SUCCEEDED / FAILED`, có thể `REFUNDED`; chỉ cho phép chuyển trạng thái hợp lệ (`UPDATE ... WHERE id = ? AND status = 'PENDING'`).
* **Gọi PSP bị timeout** — không biết đã trừ tiền hay chưa: **không** retry mù với request mới. Retry với **cùng idempotency key** phía PSP (Stripe hỗ trợ), hoặc truy vấn trạng thái giao dịch; trạng thái "unknown" được xử lý bởi job đối soát.
* **Webhook từ PSP** là nguồn xác nhận cuối cùng (người dùng có thể đóng app giữa chừng): verify chữ ký, dedupe theo event id, xử lý idempotent.
* **Tiền**: lưu `BIGINT` theo đơn vị nhỏ nhất (cent/đồng) + mã tiền tệ; không dùng float.
* **Ledger append-only**: không UPDATE/DELETE bút toán — sai thì ghi bút toán điều chỉnh (audit trail).
* Bảo mật: không lưu số thẻ (dùng token của PSP để giảm phạm vi **PCI DSS**).

---

### Case 9: Thiết kế Hệ thống Flash Sale & Đặt vé Sự kiện (Ticketmaster / Concert Booking)

> 🎯 **Bài toán cốt lõi:** Hàng trăm nghìn người tranh **số lượng rất ít** hàng trong vài giây — không được bán quá số lượng, DB không được sập.
>
> ✅ **Quyết định then chốt:** Chặn tải từ sớm (hàng đợi ảo, rate limit, CDN cho trang tĩnh); giữ chỗ **nguyên tử trong Redis** (Lua) để lọc nhanh; ghi đơn hàng **bất đồng bộ** qua queue; DB vẫn là nơi đảm bảo cuối cùng (ràng buộc/điều kiện `stock >= 1`); giữ chỗ có thời hạn để hoàn lại hàng chưa thanh toán.


#### 1. Thách thức kỹ thuật:
- **Lưu lượng bùng nổ (Traffic Spike)**: Hàng triệu người dùng truy cập tranh mua $10,000$ vé trong vòng vài giây.
- **Nguy cơ sập hệ thống**: Toàn bộ request dồn vào Database gây Row Lock Contention (`SELECT ... FOR UPDATE`), làm cạn kiệt Connection Pool và sập DB.
- **Chống Bán Quá Số Lượng (Over-selling Prevention)**: Tuyệt đối không được bán ra $10,001$ vé khi kho chỉ có $10,000$ vé.
- **Rớt mạng / Hủy đơn**: Người dùng giữ vé nhưng không thanh toán hoặc tắt app -> Cần tự động hoàn kho (Rollback stock).

#### 2. Kiến trúc Đa tầng (Multi-Tier Architecture):

```
[ 1M Người dùng ]
       │
       ▼
[ Cloudflare CDN & WAF ] ──► Chặn Bot tự động, Captcha xác thực người thật
       │
       ▼
[ Virtual Waiting Room ] ──► Phòng chờ ảo (Xếp hàng lượt vào bằng Token Bucket)
       │
       ▼ (Chỉ cho phép 20,000 req/s lọt vào)
[ API Gateway / Load Balancer ]
       │
       ▼
[ Flash Sale Service Pool ]
       │
       ▼ (1. Atomic Pre-deduction bằng Lua Script)
┌────────────────────────────────────────────────────────┐
│ Redis Cluster: stock = 10000, user_order = Set<UserId> │
└──────────────────────────┬─────────────────────────────┘
                           │ 2. Đặt chỗ thành công -> Bắn message
                           ▼
                 [ Apache Kafka: OrderTopic ]
                           │
                           ▼
                  [ Order Consumer Worker ]
                 (3. Ghi DB với Atomic Update)
                           │
       ┌───────────────────┴───────────────────┐
       ▼                                       ▼
[ Order & Inventory DB ]             [ Delay Queue (10 phút) ]
(WHERE stock > 0)                    (Nếu hết hạn chưa thanh toán)
                                               │
                                               ▼
                                     [ Tự động hoàn tồn kho ]
                                     (INCR Redis + UPDATE DB)
```

#### 3. Các Giải pháp Kỹ thuật Trọng yếu:

1. **Atomic Inventory Pre-Deduction với Redis Lua Script**:
   Thay vì query xuống DB, toàn bộ kho được nạp sẵn vào Redis in-memory. Kiểm tra tồn kho và trừ kho diễn ra nguyên tử (Atomic) trong 1 single thread Redis:
   ```lua
   -- Lua script chạy trên Redis:
   local stock = tonumber(redis.call('get', KEYS[1]))
   if (stock == nil or stock <= 0) then
       return -1 -- Hết hàng
   end
   if (redis.call('sismember', KEYS[2], ARGV[1]) == 1) then
       return -2 -- User đã mua rồi (chống spam trùng lặp)
   end
   redis.call('decr', KEYS[1])
   redis.call('sadd', KEYS[2], ARGV[1])
   return 1 -- Trừ kho thành công!
   ```
2. **Delayed Queue cho Vé Giữ Chỗ (Reservation Timeout)**:
   - Khi trừ kho thành công, vé được chuyển sang trạng thái `HOLD` (Giữ chỗ) trong 10 phút.
   - Một tin nhắn hoãn (Delayed Message) được đẩy vào RabbitMQ Dead Letter Exchange (DLX) hoặc Redis Sorted Set với `score = timestamp + 600s`.
   - Nếu sau 10 phút user chưa thanh toán: Worker nhặt message, kiểm tra trạng thái đơn hàng. Nếu chưa `PAID` -> Hủy đơn và cộng hoàn lại $+1$ vào Redis + Database.
3. **Chốt chặn Cuối cùng tại Database (Optimistic / Pessimistic Guard)**:
   ```sql
   UPDATE inventory 
   SET available_stock = available_stock - 1, locked_stock = locked_stock + 1 
   WHERE item_id = 1001 AND available_stock > 0;
   ```
   Nếu `row_affected == 0`, transaction tự động rollback, bảo vệ tuyệt đối tính toàn vẹn tài chính.
4. **Đặt chỗ theo ghế cụ thể (Ticketmaster)** — khác flash sale "số lượng":
   - Bảng `seats(event_id, seat_no, status, hold_until, order_id)` với khóa `(event_id, seat_no)`; giữ chỗ = `UPDATE seats SET status='HELD', hold_until=now()+10min, order_id=? WHERE event_id=? AND seat_no=? AND (status='AVAILABLE' OR (status='HELD' AND hold_until < now()))` → 0 dòng = ghế đã bị người khác giữ (optimistic, không cần lock dài).
   - Hoặc giữ chỗ tạm bằng Redis `SET seat:{event}:{no} {userId} NX EX 600` rồi xác nhận vào DB khi thanh toán.
   - Hiển thị sơ đồ ghế: cache trạng thái ghế, chấp nhận hơi trễ; nguồn chân lý vẫn là thao tác giữ chỗ nguyên tử.
5. **Chống bot & công bằng**: virtual waiting room cấp token theo thứ tự đến (hoặc xổ số ngẫu nhiên khi mở bán), giới hạn số vé/tài khoản, CAPTCHA, xác thực thiết bị.

---

### Case 10: Thiết kế Dịch vụ Định vị & Gọi xe (Ride-Sharing - Uber / Grab / Nearby Friends)

> 🎯 **Bài toán cốt lõi:** Hàng triệu tài xế gửi vị trí vài giây một lần, và cần tìm nhanh "tài xế gần tôi".
>
> ✅ **Quyết định then chốt:** Chỉ mục không gian (**geohash/H3/quadtree**) biến tìm kiếm 2D thành tra cứu theo ô; vị trí hiện tại lưu **trong bộ nhớ** (Redis GEO) vì ghi rất nhiều và chỉ cần giá trị mới nhất; lịch sử ghi vào kho riêng.


#### 1. Yêu cầu & Thách thức:
- **Tần suất ghi khổng lồ (High Write QPS)**: $500,000$ tài xế đang hoạt động gửi tọa độ GPS (`latitude, longitude`) mỗi $4$ giây $\to \approx 125,000\text{ Writes/s}$.
- **Truy vấn lân cận (Proximity Search)**: Hành khách mở app cần tìm $10$ tài xế gần nhất trong bán kính $3\text{ km}$ với độ trễ $< 50\text{ ms}$.

#### 2. Các Thuật toán Lập chỉ mục Không gian (Spatial Indexing Algorithms):

```
1. Geohash (Hierarchical Spatial Index):
   Chia địa cầu thành lưới ô vuông đệ quy dạng chuỗi Base32 (e.g. "w4g8n").
   - 2 điểm có chung tiền tố (Prefix) càng dài thì càng nằm gần nhau.
   - Nhược điểm: Biên giới lưới (Boundary Problem) - 2 điểm sát nhau nhưng nằm ở 2 rìa ô khác nhau có thể có tiền tố khác biệt hoàn toàn -> Cần query thêm 8 ô lân cận (8 neighboring cells).

2. QuadTree (Cây phân cấp 4 nhánh):
   - Một Node đại diện cho 1 vùng địa lý. Nếu số lượng tài xế trong ô vượt quá ngưỡng (ví dụ > 100 xe), ô đó tự động chia thành 4 ô con (NW, NE, SW, SE).
   - Vùng trung tâm thành phố chia nhỏ thành nhiều node lá; vùng nông thôn giữ node to.

3. Google S2 Geometry:
   - Chiếu mặt cầu Trái Đất lên 6 mặt hình lập phương, sử dụng Hilbert Space-filling Curve để biến tọa độ 2D thành số nguyên 64-bit Cell ID duy nhất.
   - Truy vấn lân cận chỉ là thao tác tìm kiếm khoảng số nguyên (1D Range Query), cực nhanh và không bị biến dạng cực trị.
```

#### 3. Kiến trúc Hệ thống:

```
[ Driver Phone (GPS) ]
          │ (125k QPS qua WebSocket / gRPC)
          ▼
[ Location Ingestion Service ]
          │
          ▼ (Cập nhật tọa độ in-memory)
[ Redis Cluster (Redis GEO) ] ◄── GEOADD drivers <lon> <lat> <driver_id>
          ▲
          │ GEOSEARCH drivers FROMLONLAT <user_lon> <user_lat> BYRADIUS 3 km
          │
[ Matching & Dispatch Service ]
          ▲
          │ (Request tìm xe: "Find nearby drivers")
[ Rider Phone ]
```

* **Uber H3**: hệ lưới **lục giác** phân cấp — mọi ô lục giác có 6 hàng xóm **cách đều** nhau (ô vuông có 4 cạnh + 4 góc khoảng cách khác nhau) → tính "vùng lân cận k vòng" (`kRing`) & làm **surge pricing** theo khu vực rất tự nhiên.
* **Lưu trữ Tọa độ Thời gian thực**: Dùng **Redis Sorted Set với Redis GEO**. Dưới nền tảng, Redis GEO mã hóa tọa độ thành Geohash 52-bit và lưu vào Sorted Set, cho phép lệnh `GEOSEARCH` quét tài xế theo bán kính với độ trễ chỉ $\approx 1-2\text{ ms}$.
* **Bộ lọc 2 giai đoạn (Two-Phase Matching)**:
  - **Giai đoạn 1 (Lọc thô - Coarse Filter)**: Dùng Redis GEO lấy ra 50 tài xế nằm trong bán kính đường chim bay $3\text{ km}$.
  - **Giai đoạn 2 (Lọc tinh - Fine Filter)**: Gửi danh sách 50 tài xế sang **Routing Engine (OSRM / OpenStreetMap)** để tính toán thời gian di chuyển thực tế (ETA) theo mạng lưới đường sá giao thông và chọn ra tài xế đón nhanh nhất.

---

### Case 11: Thiết kế Hệ thống Lưu trữ & Đồng bộ Tệp Đám mây (Google Drive / Dropbox)

> 🎯 **Bài toán cốt lõi:** Đồng bộ file lớn giữa nhiều thiết bị mà không tải lại toàn bộ file mỗi lần sửa, và xử lý xung đột khi sửa offline.
>
> ✅ **Quyết định then chốt:** **Chia file thành block** có hash → chỉ tải block thay đổi và khử trùng lặp; tách **metadata** (DB, nhất quán mạnh) khỏi **dữ liệu** (object storage); thông báo thay đổi qua long polling/WebSocket; xung đột → giữ cả hai bản.


#### 1. Yêu cầu Hệ thống:
- Upload và Download file dung lượng lớn (lên tới vài chục GB) an toàn, mượt mà.
- Đồng bộ thời gian thực (Real-time Sync) giữa máy tính cá nhân, điện thoại và giao diện web.
- Tiết kiệm tối đa băng thông mạng (Delta Sync) và dung lượng đĩa cứng lưu trữ (Deduplication).

#### 2. Kiến trúc Tách rời: Metadata DB vs Block Storage:

```
[ Client Desktop App ] ──► (1. File Chunking 4MB + Hash SHA-256)
        │
        ├────── 2. Gửi Chunk Hash Metadata ──────► [ Metadata Service ]
        │                                                    │
        │                                           (Kiểm tra hash tồn tại)
        ▼                                                    │
[ Block Storage Service (S3) ] ◄── 3. Chỉ upload chunk mới ──┘
        │
        ▼ (4. Phát event: FileCommitted)
[ Notification Service (WebSocket) ] ──► Đánh thức Mobile & Web App tải chunk mới!
```

#### 3. Các Kỹ thuật Trọng yếu:

1. **Phân mảnh Tệp (File Chunking) & Content-Addressable Storage (CAS)**:
   - File lớn được cắt thành các mảnh nhỏ cố định (thường là **4MB** mỗi chunk — Dropbox dùng block 4MB).
   - ⚠️ **Fixed-size chunking** có nhược điểm: chèn thêm 1 byte vào **đầu** file làm **mọi** chunk phía sau bị dịch → hash đổi hết → phải upload lại gần như toàn bộ. **Content-Defined Chunking (CDC)** dùng rolling hash (**Rabin fingerprint**, FastCDC) để đặt ranh giới chunk theo **nội dung** → chèn/xóa chỉ ảnh hưởng 1–2 chunk lân cận (rsync, restic, borg dùng ý tưởng này).
   - Mỗi chunk được tính giá trị băm mật mã học **SHA-256** làm mã định danh (`chunk_hash`).
   - Tên lưu trữ trên Object Storage (S3 / Ceph) chính là chuỗi SHA-256 đó.
2. **Khử trùng lặp Toàn cầu (Global Deduplication)**:
   - Trước khi upload một chunk lên mây, Client hỏi Metadata Service xem mã `chunk_hash` này đã có trong hệ thống chưa.
   - Nếu một user khác trên thế giới đã từng upload chunk/file y hệt -> Server chỉ tăng reference count trong Metadata DB và trả về thành công ngay lập tức (**Zero-byte Instant Upload**)!
   - ⚠️ Dedup **xuyên người dùng** có rủi ro bảo mật/riêng tư: attacker có thể dò xem một file cụ thể có tồn tại trong hệ thống không (dựa vào việc upload "tức thì") → nhiều dịch vụ chỉ dedup **trong phạm vi 1 user**, hoặc vẫn bắt client chứng minh sở hữu (proof-of-ownership) trước khi cho dedup.
3. **Đồng bộ Phần sai khác (Delta Sync)**:
   - Khi chỉnh sửa một file tài liệu 500MB (ví dụ chỉ sửa 2 câu văn), thuật toán chia chunk phát hiện chỉ có duy nhất **1 chunk 4MB bị thay đổi** hash.
   - Hệ thống chỉ upload đúng 4MB mới đó lên mây thay vì upload lại toàn bộ 500MB, tiết kiệm $99\%$ băng thông mạng.
4. **Resumable Multipart Upload (Tải lên tiếp tục khi rớt mạng)**:
   - Các chunk 4MB được upload độc lập song song qua nhiều kết nối HTTP.
   - Nếu mạng bị ngắt giữa chừng, client chỉ cần upload lại các chunk chưa hoàn tất mà không phải bắt đầu lại từ đầu.
   - Upload thẳng lên object storage bằng **pre-signed URL** (S3 Multipart Upload) → dữ liệu lớn không đi qua app server.
5. **Đồng bộ & xung đột**:
   - Metadata DB lưu cây thư mục + danh sách chunk của mỗi **version** file; mỗi thay đổi tăng version → thiết bị khác nhận notification (long polling/WebSocket) rồi kéo **delta** metadata theo cursor.
   - 2 thiết bị sửa cùng file khi offline → không tự merge được file nhị phân → tạo **"conflicted copy"** để người dùng tự xử lý (cách của Dropbox).

---

### Case 12: Thiết kế Bảng Xếp Hạng Thời Gian Thực Hàng Triệu Người (Real-time Gaming Leaderboard)

> 🎯 **Bài toán cốt lõi:** Cập nhật điểm liên tục và truy vấn "top 100" / "hạng của tôi" cho hàng triệu người theo thời gian thực.
>
> ✅ **Quyết định then chốt:** **Redis Sorted Set** (skip list) cho `ZINCRBY`, `ZREVRANK`, `ZREVRANGE` đều O(log n) — trong khi `ORDER BY` + `COUNT` trên DB với dữ liệu thay đổi liên tục là quá chậm; DB vẫn là nguồn sự thật để dựng lại.


#### 1. Yêu cầu:
- $50,000,000$ game thủ đang chơi.
- Truy vấn **Top 100** người chơi có điểm cao nhất toàn cầu trong thời gian thực ($< 5\text{ ms}$).
- Truy vấn thứ hạng chính xác (Exact Rank) và điểm số của người chơi hiện tại bất kỳ lúc nào.

#### 2. Cấu trúc Dữ liệu Nền tảng: Redis Sorted Set (ZSet):

* **Bản chất kỹ thuật**: Redis ZSet được hiện thực hóa bằng sự kết hợp giữa **Hash Table** (cho phép tra cứu điểm số theo `user_id` với độ phức tạp $O(1)$) và **SkipList - Bảng nhảy** (cho phép chèn, xóa và tìm kiếm theo thứ tự điểm với độ phức tạp $O(\log N)$).
* **Các câu lệnh cốt lõi**:
  - `ZADD leaderboard <score> <user_id>`: Cập nhật điểm của user ($O(\log N)$).
  - `ZREVRANGE leaderboard 0 99 WITHSCORES`: Lấy Top 100 người cao điểm nhất ($O(\log N + M)$ với $M=100$).
  - `ZREVRANK leaderboard <user_id>`: Lấy thứ hạng của một user cụ thể ($O(\log N)$).

#### 3. Xử lý Đồng điểm (Tie-Breaking Rule):
Nếu 2 người chơi cùng có $1500$ điểm, ai nộp điểm sớm hơn phải xếp trên. Ý tưởng: ghép điểm và thời gian vào 1 số:
$$\text{Score}_{\text{composite}} = \text{UserScore} \times 10^{k} + (T_{\max} - T_{\text{submit}})$$
* ⚠️ Score trong Redis ZSET là **double 64-bit** → chỉ biểu diễn **chính xác số nguyên tới $2^{53} \approx 9 \times 10^{15}$**. Công thức dạng `score + (1 - timestamp_ms / 10^13)` (phần thập phân) **không đủ độ chính xác** để phân biệt chênh lệch 1 ms khi phần nguyên đã chiếm nhiều chữ số.
* Cách an toàn: dùng **số nguyên** và tính trước ngân sách bit. Ví dụ: thời gian tính bằng **giây kể từ khi mùa giải bắt đầu** (≤ 10⁸ giây ≈ 3 năm → $k = 8$), điểm ≤ 10⁷ → composite ≤ $10^{15} < 2^{53}$ ✅.
* Hoặc tie-break ở **tầng ứng dụng**: ZSET chỉ lưu điểm, khi trùng điểm thì sắp xếp phụ theo thời gian lưu ở hash riêng (Redis ZSET mặc định sắp các member cùng score theo **thứ tự từ điển** của member).

#### 4. Scale Out khi Quy mô Vượt quá 1 Redis Instance:

```
Score Range Partitioning (Phân mảnh theo Dải điểm):
┌─────────────────────────┐     ┌─────────────────────────┐     ┌─────────────────────────┐
│ Redis Node 1            │     │ Redis Node 2            │     │ Redis Node 3            │
│ Dải điểm: 0 - 1,000     │     │ Dải điểm: 1,001 - 5,000 │     │ Dải điểm: > 5,000       │
│ (35M Casual Users)      │     │ (14M Mid Users)         │     │ (1M Pro / Top Rank)     │
└─────────────────────────┘     └─────────────────────────┘     └─────────────────────────┘
```
- Khi số lượng user quá lớn làm 1 instance Redis bị đầy RAM hoặc nghẽn CPU, chia partition theo **Dải điểm (Score Range)**.
- Top 100 toàn cầu luôn nằm trọn vẹn trong **Node 3** (Dải điểm cao nhất, miễn node này có ≥ 100 người), việc lấy Top 100 chỉ truy vấn trên 1 node duy nhất mà không cần cross-node merge!
- **Thứ hạng chính xác** của 1 user ở Node 2 = `ZCARD(Node 3)` + `ZREVRANK` của user trong Node 2 (+1).
- Khi user tăng điểm vượt dải → phải **di chuyển** member sang node khác (xóa ở node cũ, thêm ở node mới — cần làm nguyên tử hoặc chấp nhận sai lệch ngắn). Dải điểm cần được điều chỉnh khi phân bố điểm thay đổi.
- Thay thế: **hash partition theo user** (phân bố đều) + scatter-gather lấy top-K mỗi shard rồi merge (top 100 = merge top 100 của từng shard); thứ hạng chính xác thì khó hơn (ước lượng bằng histogram điểm).
- **Bền vững**: Redis là bản "đọc nhanh"; điểm gốc vẫn ghi vào DB/Kafka để có thể dựng lại leaderboard khi Redis mất dữ liệu.

---

### Case 13: Thiết kế Hệ thống Thu Thập Dữ Liệu Web Phân Tán (Distributed Web Crawler - Googlebot)

> 🎯 **Bài toán cốt lõi:** Duyệt hàng tỷ trang web **lịch sự** (không làm sập website người khác), không duyệt trùng, ưu tiên trang quan trọng.
>
> ✅ **Quyết định then chốt:** **URL frontier** hai tầng (hàng đợi ưu tiên + hàng đợi theo host để giới hạn tốc độ mỗi host); **Bloom filter** để loại URL đã thấy với bộ nhớ nhỏ; phân tán theo host để mỗi worker sở hữu một nhóm host.


#### 1. Yêu cầu Hệ thống:
- Thu thập hàng tỷ trang web trên Internet, tự động phân tích và trích xuất URL để crawl tiếp.
- **Tính lịch sự (Politeness)**: Không gửi quá nhiều request dồn dập làm sập web server của mục tiêu.
- **Tính ưu tiên (Priority)**: Crawl các trang web quan trọng (Wikipedia, báo lớn) thường xuyên hơn các trang rác.
- **Chống trùng lặp (Deduplication)**: Không crawl lại URL đã duyệt và bỏ qua các trang web có nội dung trùng lặp.

#### 2. Kiến trúc URL Frontier & Crawler Pipeline:

```
[ Seed URLs ] ──► [ URL Frontier ]
                         │
                         ▼ (Lấy URL tiếp theo theo nguyên tắc Politeness & Priority)
                  [ DNS Resolver Pool ] ◄── Cache IP tránh gọi DNS server ngoài
                         │
                         ▼ (Download nội dung trang HTML)
                   [ HTML Fetcher ]
                         │
                         ▼ (Kiểm tra trùng lặp nội dung bằng SimHash)
               [ Content Seen / Duplicate? ] ──(Trùng)──► Bỏ qua
                         │ (Không trùng)
                         ▼
                  [ Document Storage ]
                         │
                         ▼ (Trích xuất các liên kết mới)
                   [ Link Extractor ]
                         │
                         ▼ (Kiểm tra URL đã từng duyệt chưa bằng Bloom Filter)
                [ URL Filter / Bloom Filter ] ──(Đã duyệt)──► Bỏ qua
                         │ (Chưa duyệt)
                         ▼
                  [ URL Frontier ] (Đẩy ngược lại hàng đợi để crawl tiếp)
```

#### 3. Các Thành phần Cốt lõi:

1. **URL Frontier Architecture**:
   - Gồm 2 tầng hàng đợi: **Priority Queues** và **Politeness Queues**.
   - **Politeness Guarantee**: Mỗi hostname/domain (e.g., `cnn.com`) được map cố định vào một queue riêng. Giữa 2 lần fetch liên tiếp từ cùng một domain bắt buộc phải có khoảng nghỉ cố định ($1-2\text{ giây}$) và luôn tuân thủ nghiêm ngặt file `robots.txt`.
2. **Kiểm tra URL Trùng lặp với Bloom Filter**:
   - Hàng tỷ URL được đưa vào một **Bloom Filter** dung lượng vài GB trong RAM.
   - Thao tác kiểm tra xem URL đã crawl chưa tốn thời gian $O(k)$ cực nhanh mà không cần chạm vào đĩa cứng.
3. **Phát hiện Trùng lặp Nội dung với SimHash / MinHash**:
   - Hai bài báo có nội dung giống nhau $98\%$ (chỉ khác banner quảng cáo hoặc footer).
   - Sử dụng thuật toán băm bảo toàn độ tương đồng (**Locality-Sensitive Hashing - SimHash**): Hai văn bản gần giống nhau sẽ sinh ra hai mã SimHash có **khoảng cách Hamming (Hamming Distance)** $\le 3$ bits. Khi khoảng cách $\le 3$, crawler loại bỏ ngay để tránh tốn dung lượng lưu trữ tài liệu trùng lặp.

4. **Các vấn đề thực tế khác**:
   - **Spider trap**: URL sinh vô hạn (lịch `?date=` vô tận, path lặp `/a/a/a/...`) → giới hạn độ sâu, độ dài URL, số trang mỗi domain.
   - **Chuẩn hóa URL** trước khi dedupe (bỏ fragment `#`, sắp xếp query param, lowercase host, bỏ tham số tracking `utm_*`).
   - **Recrawl**: tần suất crawl lại theo tốc độ thay đổi của trang (trang tin tức vài phút, trang tĩnh vài tuần); dùng `ETag`/`Last-Modified` để tải có điều kiện.
   - Trang render bằng JavaScript cần headless browser (tốn tài nguyên gấp nhiều lần) → tách pipeline riêng.
   - Ước lượng: 1 tỷ trang/tháng ≈ 400 trang/s; 1 trang ~ 500 KB → ~ 500 TB/tháng chưa nén.

---

### Case 14: Thiết kế Distributed Key-Value Store (kiểu Amazon Dynamo / Cassandra)

> 🎯 **Bài toán cốt lõi:** Kho key-value **luôn ghi được** (kể cả khi mạng chia cắt), scale bằng cách thêm node.
>
> ✅ **Quyết định then chốt:** Consistent hashing + virtual node để chia dữ liệu; nhân bản N bản với **quorum điều chỉnh được** (R + W > N khi cần đọc mới nhất); chọn **AP** — xung đột giải bằng vector clock/LWW, sửa lệch bằng hinted handoff, read repair và Merkle tree.


#### 1. Yêu cầu
- API `put(key, value)`, `get(key)`; dữ liệu lớn hơn 1 máy; tự scale khi thêm node; luôn ghi được kể cả khi có node/mạng lỗi (ưu tiên **AP**), độ trễ thấp; consistency có thể điều chỉnh.

#### 2. Các khối xây dựng (ghép từ các mục trước)
| Vấn đề | Kỹ thuật | Tham chiếu |
| :--- | :--- | :--- |
| Chia dữ liệu | **Consistent hashing + virtual nodes** | 11.1 |
| Sao chép | Mỗi key lưu ở **N** node kế tiếp trên ring (preference list), thường N = 3, rải qua nhiều AZ | `Database.md` 19.4 |
| Consistency điều chỉnh được | **Quorum** `W + R > N` (vd N=3, W=2, R=2); ưu tiên ghi: W=1 | `Database.md` 19.4 |
| Node tạm chết | **Sloppy quorum + hinted handoff** | `Database.md` 19.4 |
| Phiên bản xung đột | **Vector clock** (hoặc LWW cho đơn giản) + trả siblings cho client merge | `Database.md` 19.7 |
| Replica lệch nhau lâu dài | **Anti-entropy bằng Merkle tree**, read repair | 11.2 |
| Thành viên cluster & phát hiện lỗi | **Gossip protocol** + phi-accrual failure detector | 11.3, 11.11 |
| Lưu trữ trên mỗi node | **LSM-tree**: commit log + memtable + SSTable + bloom filter + compaction | `Database.md` 4 |

#### 3. Luồng ghi / đọc
```
Client ──put(k,v)──► Coordinator (bất kỳ node nào; hoặc client "token-aware" gửi thẳng node sở hữu)
                        │ hash(k) → tìm N node trên ring
                        ├──► Replica 1 ─┐
                        ├──► Replica 2 ─┼── chờ đủ W ack → trả OK cho client
                        └──► Replica 3 ─┘   (replica còn lại ghi nốt bất đồng bộ / hinted handoff)

get(k): gửi tới R replica → chọn version mới nhất (hoặc trả nhiều version nếu xung đột) → read repair replica cũ
```
- Mỗi replica ghi: **commit log (append, fsync theo batch)** → **memtable** → flush **SSTable**.
- Trade-off cần nêu khi phỏng vấn: chọn N/W/R theo yêu cầu (W=N: ghi chậm nhưng đọc R=1 nhanh; W=1: ghi nhanh, dễ mất dữ liệu nếu node chết trước khi replicate), xử lý xóa bằng tombstone (+ `gc_grace_seconds` để tránh dữ liệu "sống lại"), hot key.

---

### Case 15: Thiết kế Distributed Job Scheduler / Delayed Task (vd: gửi nhắc lịch, hủy đơn sau 30 phút, cron phân tán)

> 🎯 **Bài toán cốt lõi:** Chạy hàng triệu tác vụ **đúng hạn**, **mỗi tác vụ đúng một lần** về mặt hiệu ứng, dù scheduler có nhiều instance và có thể chết.
>
> ✅ **Quyết định then chốt:** Lưu tác vụ bền vững theo thời điểm chạy (DB index theo `run_at`, hoặc Redis ZSET), worker lấy việc bằng **claim nguyên tử** (`SELECT ... FOR UPDATE SKIP LOCKED` / lease) + tác vụ idempotent; timing wheel trong bộ nhớ cho độ chính xác cao.


#### 1. Yêu cầu
- Đăng ký job chạy **tại thời điểm T** hoặc theo **cron**; hàng triệu job/ngày; mỗi job chạy **ít nhất 1 lần** và không bị chạy song song trùng lặp; chịu được worker chết; theo dõi trạng thái, retry.

#### 2. Kiến trúc
```
[ API ] ──create job──► [ Job DB (PostgreSQL/Cassandra) ]  jobs(id, run_at, status, payload, attempts, owner, lease_until)
                                   │
              ┌────────────────────┴───────────────────┐
              ▼                                        ▼
   [ Scheduler (poller, nhiều instance) ]     (job sắp tới hạn có thể nạp vào
   SELECT ... WHERE run_at <= now()+δ          Redis ZSET score=run_at để giảm tải DB)
     AND status='PENDING'
     FOR UPDATE SKIP LOCKED LIMIT 500
   → set status='QUEUED' → đẩy vào queue
              │
              ▼
   [ Kafka / SQS / RabbitMQ ] ──► [ Workers ] ── thực thi (idempotent) ──► cập nhật status=DONE / retry với backoff / DLQ
```
#### 3. Các điểm deep-dive
- **Không chạy trùng**: claim job nguyên tử (`FOR UPDATE SKIP LOCKED`, hoặc `UPDATE ... SET owner = ?, lease_until = now()+5m WHERE id = ? AND status = 'PENDING'`). Worker chết giữa chừng → **lease hết hạn** → job được claim lại → vì thế job phải **idempotent** (at-least-once).
- **Độ chính xác thời gian**: poll mỗi 1s là đủ cho đa số nghiệp vụ; cần chính xác hơn → nạp job sắp đến hạn vào bộ nhớ dạng **timing wheel** (Kafka, Netty `HashedWheelTimer` dùng) hoặc Redis ZSET (`ZRANGEBYSCORE jobs 0 now`).
- **Scale**: partition job theo `hash(job_id) % N`, mỗi scheduler instance phụ trách một số partition (gán qua leader election/consistent hashing) → không tranh chấp cùng hàng.
- **Hàng đợi có sẵn hỗ trợ delay**: SQS delay (tối đa 15 phút), RabbitMQ TTL + DLX / delayed-message plugin, Redis ZSET, Kafka không hỗ trợ delay tự nhiên (phải có topic theo mức delay + consumer chờ).
- **Cron**: lưu biểu thức cron, sau mỗi lần chạy tính `next_run_at`; tránh "thundering herd" lúc 00:00 bằng jitter.
- **Giải pháp có sẵn**: Quartz (cluster mode), db-scheduler, JobRunr, Temporal (durable timers), K8s CronJob, Airflow (batch/data pipeline).

---

### Case 16: Thiết kế Ad Click Aggregation / Real-time Analytics (đếm click theo phút cho hàng tỷ sự kiện)

> 🎯 **Bài toán cốt lõi:** Đếm hàng tỷ sự kiện theo cửa sổ thời gian với độ trễ thấp, **không đếm thiếu/thừa** khi có sự kiện đến muộn hoặc xử lý lại.
>
> ✅ **Quyết định then chốt:** Kafka làm log đầu vào (phát lại được) + stream processing (Flink) gom theo **event time** với watermark; kết quả ghi idempotent vào OLAP; đối soát bằng batch trên dữ liệu thô (kiến trúc kiểu Lambda/Kappa).


#### 1. Yêu cầu
- 1 tỷ click/ngày (~10k/s trung bình, peak 50k/s); truy vấn "số click của ad X trong 5 phút gần nhất", "top 100 ad trong 1 giờ"; độ trễ dữ liệu vài phút; **số liệu dùng để tính tiền → phải chính xác** (không mất, không đếm trùng).

#### 2. Kiến trúc
```
[ Click log (ad server) ] ──► [ Kafka: raw_clicks (key = ad_id) ]
                                       │
                                       ▼
                        [ Stream processor: Flink ]
                        - dedupe theo click_id (state có TTL)
                        - tumbling window 1 phút theo EVENT TIME, watermark cho dữ liệu trễ
                        - aggregate (ad_id, minute) → count
                                       │  (exactly-once: checkpoint + 2PC sink / upsert idempotent)
                                       ▼
                 [ OLAP store: ClickHouse / Druid / Pinot ] ◄── Query service / Dashboard
                                       ▲
[ Raw data lưu S3 (Parquet) ] ── batch job đối soát hằng ngày (Spark) ─┘  (Lambda: sửa sai số của streaming)
```
#### 3. Các điểm deep-dive
- **Event time vs Processing time**: click xảy ra lúc 10:00:59 nhưng tới hệ thống lúc 10:01:05 → phải tính vào cửa sổ 10:00 theo **event time**; **watermark** quyết định đợi dữ liệu trễ bao lâu, dữ liệu trễ hơn nữa → cập nhật bổ sung (allowed lateness) hoặc ghi side output để batch xử lý.
- **Cửa sổ**: tumbling (không chồng lấn), sliding/hopping (chồng lấn — "5 phút gần nhất cập nhật mỗi phút"), session window.
- **Exactly-once**: Kafka (offset) + state của Flink được **checkpoint** nhất quán; sink ghi **idempotent** (upsert theo khóa `(ad_id, window)`) hoặc transactional sink.
- **Hot ad** (1 ad chiếm phần lớn click) → key `ad_id` dồn vào 1 partition: pre-aggregate cục bộ ở mỗi task rồi mới gộp (two-phase aggregation), hoặc salt key.
- **Top-K**: mỗi window giữ min-heap K phần tử theo partition rồi merge; nếu chấp nhận xấp xỉ → Count-Min Sketch (11.12).
- **Chống gian lận click**: lọc bot/IP bất thường, dedupe theo (user, ad) trong khoảng thời gian.
- **Đối soát (reconciliation)**: batch tính lại từ raw data trên S3 và so với kết quả streaming — vì tiền nên phải có đường kiểm tra độc lập.

---

### Case 17: Thiết kế Collaborative Editing (Google Docs / Notion / Figma)

> 🎯 **Bài toán cốt lõi:** Nhiều người sửa **cùng lúc** cùng một tài liệu, mỗi người thấy ngay thay đổi của mình (không chờ server), và cuối cùng mọi bản sao **hội tụ** giống nhau.
>
> ✅ **Quyết định then chốt:** Áp dụng thay đổi cục bộ ngay, gửi **operation** thay vì cả tài liệu; giải xung đột bằng **OT** (cần server trung tâm sắp thứ tự — Google Docs) hoặc **CRDT** (hội tụ không cần trung tâm, hợp offline — Figma/ứng dụng local-first).


#### 1. Yêu cầu
- Nhiều người cùng sửa 1 tài liệu theo thời gian thực, thấy con trỏ của nhau, làm việc offline rồi đồng bộ, lưu lịch sử phiên bản.

#### 2. Vấn đề cốt lõi: hợp nhất các thao tác đồng thời
Alice chèn "X" ở vị trí 0, Bob đồng thời xóa ký tự ở vị trí 2 → nếu áp thao tác "thô" theo thứ tự khác nhau ở 2 máy, kết quả sẽ khác nhau.

| | **Operational Transformation (OT)** | **CRDT** (Conflict-free Replicated Data Types) |
| :--- | :--- | :--- |
| Ý tưởng | Server trung tâm nhận thao tác, **biến đổi (transform)** thao tác đến sau theo các thao tác đã áp dụng (vd dịch vị trí xóa từ 2 → 3 vì có ký tự chèn trước nó) | Mỗi phần tử có **ID duy nhất bất biến** (không dùng chỉ số vị trí); thao tác được thiết kế giao hoán → mọi replica áp theo thứ tự bất kỳ đều hội tụ |
| Kiến trúc | Cần **server trung tâm** sắp thứ tự | Peer-to-peer / offline-first được |
| Độ phức tạp | Hàm transform khó đúng với nhiều loại thao tác | Metadata lớn (ID cho từng ký tự, tombstone), cần nén |
| Dùng ở | Google Docs (Jupiter/Wave), Etherpad | Figma (biến thể), Notion (một phần), Apple Notes, thư viện **Yjs**, **Automerge** |

#### 3. Kiến trúc tham khảo
```
[ Clients ] ══ WebSocket ══► [ Collaboration Service ] (mỗi document được "gắn" vào 1 server — sticky theo doc_id
                                   │                     qua consistent hashing, giữ trạng thái doc trong RAM)
                                   ├─► broadcast thao tác đã transform/merge tới các client khác trong cùng doc
                                   ├─► append operation log (Kafka/DB) — nguồn để replay, undo, lịch sử
                                   └─► snapshot định kỳ vào object storage/DB (load nhanh, không replay từ đầu)
[ Presence: con trỏ, selection ] ── chỉ qua pub/sub, không lưu bền (ephemeral)
```
- **Offline**: client lưu thao tác cục bộ, khi online gửi lên kèm phiên bản cơ sở; CRDT hợp nhất tự nhiên, OT phải transform theo lịch sử dài.
- **Phân quyền** theo document, **rate limit** thao tác, giới hạn kích thước tài liệu; lịch sử phiên bản = snapshot + operation log.

---

### Tổng kết: Khung trả lời một bài System Design (mẫu nhanh)

```
1. Làm rõ yêu cầu (5')    : chức năng chính, người dùng, quy mô (DAU, QPS, dữ liệu), SLA latency/availability, consistency
2. Ước lượng (5')         : QPS avg/peak, storage N năm, bandwidth, cache size (12.2)
3. API & Data model (5')  : endpoint chính, schema, chọn DB theo access pattern (6.5)
4. High-level design (10'): client → CDN → LB → gateway → services → cache/DB/queue; vẽ luồng đọc & ghi
5. Deep dive (15-20')     : bottleneck lớn nhất (hot key, fan-out, consistency, ordering, idempotency...)
6. Độ tin cậy (5')        : SPOF, replication, failover, retry/timeout/circuit breaker, monitoring & alert
7. Tổng kết               : trade-off đã chọn và hướng mở rộng tiếp theo
```
> Mẹo: luôn **nói ra trade-off** ("chọn X vì..., đánh đổi Y"), bắt đầu bằng thiết kế đơn giản chạy được rồi mới tối ưu, và hỏi lại interviewer muốn đào sâu phần nào.
