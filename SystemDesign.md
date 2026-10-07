# System Design Study & Interview Guide

Tài liệu ôn tập toàn diện System Design cho kỹ sư phần mềm và phỏng vấn Technical System Design (Mid / Senior / Lead). Tài liệu này tập trung vào kiến trúc hệ thống phân tán, các building blocks, network protocols, reliability, security, observability, design patterns và các bài toán phỏng vấn thực tế kinh điển.

> 💡 **Tài liệu liên quan**:
> - [`Database.md`](file:///e:/Source/coding-interview-university/Database.md): B+Tree, Storage Engine, MVCC, WAL, ARIES Recovery, Distributed DB, Paxos/Raft, Parquet.
> - [`SpringBoot.md`](file:///e:/Source/coding-interview-university/SpringBoot.md): Kiến trúc chuyên sâu Spring Framework, Spring Boot, Spring Data JPA, Spring Security, Kafka manual partition assignment.
> - [`Java.md`](file:///e:/Source/coding-interview-university/Java.md): Core Java, Concurrency, JVM Internals, Memory Model, Garbage Collection.

---

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

---

## 1. Scalability Fundamentals

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

---

## 2. Caching & CDN

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
| **Write-Through** | Ứng dụng ghi vào Cache, Cache layer chịu trách nhiệm ghi đồng bộ (sync) vào DB trước khi trả về OK. | Đảm bảo dữ liệu trong Cache và DB luôn nhất quán (consistent); không bao giờ đọc phải stale data. | Write latency cao vì phải chờ cả 2 lớp ghi xong; ghi cả data có thể không bao giờ được đọc lại. | Ứng dụng yêu cầu tính nhất quán cao, không chấp nhận mất dữ liệu (Banking, Order ledger). |
| **Write-Behind (Write-Back)** | Ứng dụng ghi vào Cache và nhận phản hồi thành công ngay lập tức. Cache tích lũy và ghi bất đồng bộ (async batch) xuống DB. | Write throughput cực nhanh, giảm tải write trực tiếp cho DB, tối ưu batch I/O. | Nguy cơ mất mát dữ liệu (Data Loss) nếu cache node crash trước khi kịp flush data xuống DB. | Tác vụ write bùng nổ, chấp nhận eventual consistency (Click tracking, View count, IoT sensor data). |
| **Write-Around** | Ghi thẳng vào Database, hoàn toàn bỏ qua (bypass) cache. | Không làm ô nhiễm cache bằng những data ghi 1 lần và không bao giờ đọc lại. | Lần đọc đầu tiên sau khi ghi chắc chắn bị Cache Miss. | Hệ thống ghi log, archive file, user upload video một lần. |

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

---

## 3. Message Queue & Event-Driven Architecture

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

1. **Broker & Controller**: Node trong cluster. Controller (chạy qua KRaft - Kafka Raft Metadata mode hoặc ZooKeeper) quản lý metadata, phát hiện broker crash và bầu chọn Leader cho các partition.
2. **Topic & Partition**:
   * Topic là logic stream phân loại message.
   * Topic được chia nhỏ thành nhiều **Partitions** nằm rải rác trên các Broker khác nhau để mở rộng song song (Parallelism).
   * **Partition Key**: Producer gửi message kèm `Key`. Kafka dùng `hash(Key) % Number_Of_Partitions` để định tuyến. Tất cả message có cùng Key chắc chắn vào cùng một Partition -> **Đảm bảo thứ tự tuyệt đối (Strict Ordering) theo Key**.
3. **Producer Acks & Độ bền dữ liệu (Durability)**:
   * `acks = 0`: Producer bắn message và không chờ phản hồi. Nhanh nhất, nguy cơ mất dữ liệu cao nhất.
   * `acks = 1`: Producer chờ duy nhất Partition Leader ghi vào local log thành công là trả về OK. Mất data nếu Leader chết trước khi sync sang Follower.
   * `acks = all` (hoặc `-1`): Leader chờ **toàn bộ các bản sao trong ISR (In-Sync Replicas)** ghi thành công rồi mới phản hồi OK. Đảm bảo an toàn dữ liệu tuyệt đối (kết hợp với `min.insync.replicas = 2`).
4. **Consumer Group & Rebalancing**:
   * Nhiều consumer cùng thuộc 1 `group.id` sẽ chia nhau các partition trong Topic (1 partition tại 1 thời điểm chỉ được đọc bởi 1 consumer trong cùng 1 group).
   * Số consumer > số partition -> Consumer thừa sẽ ở trạng thái idle (ngồi chơi).
   * **Rebalance**: Khi có consumer join/leave group, Kafka phân phối lại partition cho các consumer còn lại.
5. **Tại sao Kafka có Throughput khổng lồ?**
   * **Sequential Disk I/O**: Ghi append-only vào cuối file log, tốc độ ghi tuần tự đĩa cơ tiệm cận RAM.
   * **Page Cache OS**: Tận dụng kernel memory cache của hệ điều hành thay vì allocate JVM Heap (tránh GC pause).
   * **Zero-Copy Network Transfer**: Sử dụng Linux system call `sendfile()`, chuyển data trực tiếp từ Page Cache ra Network Socket buffer mà không cần copy qua User Space memory.
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

---

## 4. API Design & Gateways

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

if tokens >= requested then
    tokens = tokens - requested
    redis.call('HMSET', KEYS[1], 'tokens', tokens, 'last_refill', last_refill)
    redis.call('EXPIRE', KEYS[1], math.ceil(capacity / refill_rate))
    return 1 -- ACCEPT
else
    redis.call('HMSET', KEYS[1], 'tokens', tokens, 'last_refill', last_refill)
    return 0 -- REJECT (429 Too Many Requests)
end
```

---

### 4.3. Pagination: Offset-based vs Cursor-based

```
Offset-based (LIMIT, OFFSET):
SELECT * FROM posts ORDER BY created_at DESC LIMIT 10 OFFSET 1000000;
──► Database phải quét 1,000,010 bản ghi trên đĩa rồi vứt bỏ 1,000,000 dòng đầu! (O(N) - Rất chậm)
──► Dễ bị nhảy trang / lặp dữ liệu khi có bài viết mới chèn vào giữa lúc user chuyển page.

Cursor-based (Keyset Pagination):
SELECT * FROM posts WHERE id < 592837 ORDER BY id DESC LIMIT 10;
──► Dùng B+Tree Index tìm trực tiếp ID=592837 trong O(log N) và lấy ngay 10 bản ghi tiếp theo! (O(1) seek)
──► Không bị duplicate data khi có new record; hỗ trợ Infinite Scroll hoàn hảo.
```

---

## 5. Microservices Architecture & Resiliency

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
   * **Jitter (Nhiễu ngẫu nhiên)**: Thêm một khoảng random `+ random(0, 100ms)` vào wait time để tránh việc hàng ngàn client cùng retry chính xác tại cùng một millisecond (gây thundering herd lên server vừa hồi phục).

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

---

## 6. Storage & Data Systems

> 💡 *Chi tiết về Storage Engines (LSM-Tree vs B+Tree), MVCC, Concurrency Isolation, ARIES Crash Recovery, Distributed Database Consensus (2PC, Paxos, Raft, Multi-Paxos) đã được viết chi tiết tại [`Database.md`](file:///e:/Source/coding-interview-university/Database.md).*

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

---

## 7. Networking & Communication Protocols

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
  * **Server Push**: Server chủ động đẩy trước CSS/JS về client trước khi HTML parse yêu cầu.
* **HTTP/3 (QUIC Protocol)**:
  * Chuyển từ transport TCP sang **QUIC chạy trên nền UDP**.
  * **0-RTT Handshake**: Kết hợp TLS 1.3 và Transport Handshake trong 1 round-trip duy nhất (kết nối lại tốn 0-RTT).
  * **Connection Migration**: Chuyển đổi mạng (từ Wi-Fi sang 4G trên điện thoại) không bị đứt kết nối nhờ định danh bằng `Connection ID` thay vì `Client IP + Port`.

---

### 7.4. Real-Time Communication Technologies

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
4. **Active-Active Multi-Region**: Toàn bộ hệ thống chạy 100% công suất song song ở nhiều Region toàn cầu. Traffic được định tuyến thông minh qua Geo-DNS/Anycast. RTO $\approx 0$, RPO $\approx 0$. Chi phí đắt nhất.

---

## 9. Monitoring, Logging & Observability

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

### 9.2. Framework giám sát: RED Method vs USE Method

* **RED Method** (Dành cho **Request-driven Services / APIs**):
  * **R - Rate**: Số lượng requests nhận được trên mỗi giây (Throughput: req/s).
  * **E - Errors**: Số lượng request bị lỗi thất bại (Error rate: HTTP 5xx count).
  * **D - Duration**: Thời gian xử lý request (Latency percentiles: p50, p95, p99).
* **USE Method** (Dành cho **Hạ tầng phần cứng / Hardware Resources: CPU, RAM, Disk, Network**):
  * **U - Utilization**: Phần trăm thời gian tài nguyên đang bận xử lý công việc (e.g., CPU 85%).
  * **S - Saturation**: Mức độ công việc bị ứ đọng chờ xử lý (e.g., CPU Run Queue Length, Disk I/O Wait).
  * **E - Errors**: Số lượng lỗi phần cứng/driver phát sinh (e.g., Network dropped packets).

---

## 10. Security & Authentication

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
* **Token-based (JWT - JSON Web Token)**: Token tự chứa thông tin (Self-contained) và được ký mã hóa bằng Secret Key (HMAC) hoặc Private Key (RSA/ECDSA). Server chỉ cần dùng Public Key verify chữ ký toán học mà không cần query Database -> **Tối ưu cho Stateless Microservices**.

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

---

## 11. Distributed System Design Patterns

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

* **Ứng dụng trong Cassandra, DynamoDB, Git, Blockchain**:
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
| **Amazon DynamoDB** | **PA / EL** | Ưu tiên Availability tối đa cho khách hàng mua sắm giỏ hàng. | Replicate async giữa các replica để duy trì response time mili-giây. |
| **Google Spanner** | **PC / EC** | Chặn ghi nếu không đạt Quorum Paxos liên lục địa (Đảm bảo External Consistency). | Sử dụng đồng hồ nguyên tử (TrueTime API) để đảm bảo Strict Serializability, chấp nhận độ trễ commit cao hơn. |
| **MongoDB** | **PC / EC** (hoặc PC/EL tùy write concern) | Đóng ghi nếu Primary node bị cô lập khỏi đa số replica. | Với `w: majority`, chờ đa số replica ack để bảo vệ tính nhất quán dữ liệu. |
| **PostgreSQL (Sync Rep)** | **PC / EC** | Chặn ghi nếu Slave đồng bộ không phản hồi. | Client phải chờ câu lệnh commit xong ở cả Primary và Secondary. |

---

## 12. System Design Interview Framework & Back-of-Envelope

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

#### Công thức chuyển đổi QPS & Dung lượng chuẩn:
* $1\text{ ngày} = 86,400\text{ giây} \approx 100,000\text{ giây} = 10^5\text{ s}$ (Làm tròn để tính nhẩm nhanh).
* **Average QPS** $= \frac{\text{Daily Active Users (DAU)} \times \text{Average Requests per User per Day}}{100,000\text{ s}}$.
* **Peak QPS** $= \text{Average QPS} \times 2$ (hoặc $\times 5$ với các event bùng nổ traffic).

---

## 13. Common System Design Interview Questions (Deep Dives)

---

### Case 1: Thiết kế Hệ thống Rút gọn URL (TinyURL / Bit.ly)

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
* **Cách tiếp cận 2: Sử dụng Distributed Unique ID Generator (Snowflake) rồi convert sang Base62**:
  * `ID = 10293847561` -> `Base62(ID) = "7bK9zX"` -> 100% Unique, không bao giờ collision.
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
* **HTTP Response Headers chuẩn cho Rate Limiting**:
  * `X-Ratelimit-Limit`: Số request tối đa được phép trong window (e.g., 100).
  * `X-Ratelimit-Remaining`: Số request còn lại được phép gọi (e.g., 23).
  * `X-Ratelimit-Retry-After`: Số giây cần chờ trước khi thử lại nếu bị HTTP 429 Too Many Requests.

---

### Case 3: Thiết kế Hệ thống Chat Thời Gian Thực (WhatsApp / Slack / Messenger)

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
    message_id timeuuid, -- Đảm bảo Unique và Tự động sắp xếp theo thời gian
    sender_id uuid,
    content text,
    media_url text,
    PRIMARY KEY ((group_id), message_id)
) WITH CLUSTERING ORDER BY (message_id ASC);
```

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

---

### Case 4: Thiết kế News Feed / Timeline (Twitter / Facebook / Instagram)

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

---

### Case 9: Thiết kế Hệ thống Flash Sale & Đặt vé Sự kiện (Ticketmaster / Concert Booking)

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
                                     (DECR Redis + UPDATE DB)
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

---

### Case 10: Thiết kế Dịch vụ Định vị & Gọi xe (Ride-Sharing - Uber / Grab / Nearby Friends)

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

* **Lưu trữ Tọa độ Thời gian thực**: Dùng **Redis Sorted Set với Redis GEO**. Dưới nền tảng, Redis GEO mã hóa tọa độ thành Geohash 52-bit và lưu vào Sorted Set, cho phép lệnh `GEOSEARCH` quét tài xế theo bán kính với độ trễ chỉ $\approx 1-2\text{ ms}$.
* **Bộ lọc 2 giai đoạn (Two-Phase Matching)**:
  - **Giai đoạn 1 (Lọc thô - Coarse Filter)**: Dùng Redis GEO lấy ra 50 tài xế nằm trong bán kính đường chim bay $3\text{ km}$.
  - **Giai đoạn 2 (Lọc tinh - Fine Filter)**: Gửi danh sách 50 tài xế sang **Routing Engine (OSRM / OpenStreetMap)** để tính toán thời gian di chuyển thực tế (ETA) theo mạng lưới đường sá giao thông và chọn ra tài xế đón nhanh nhất.

---

### Case 11: Thiết kế Hệ thống Lưu trữ & Đồng bộ Tệp Đám mây (Google Drive / Dropbox)

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
   - File lớn được cắt thành các mảnh nhỏ cố định (thường là **4MB** mỗi chunk).
   - Mỗi chunk được tính giá trị băm mật mã học **SHA-256** làm mã định danh (`chunk_hash`).
   - Tên lưu trữ trên Object Storage (S3 / Ceph) chính là chuỗi SHA-256 đó.
2. **Khử trùng lặp Toàn cầu (Global Deduplication)**:
   - Trước khi upload một chunk lên mây, Client hỏi Metadata Service xem mã `chunk_hash` này đã có trong hệ thống chưa.
   - Nếu một user khác trên thế giới đã từng upload chunk/file y hệt -> Server chỉ tăng reference count trong Metadata DB và trả về thành công ngay lập tức (**Zero-byte Instant Upload**)!
3. **Đồng bộ Phần sai khác (Delta Sync)**:
   - Khi chỉnh sửa một file tài liệu 500MB (ví dụ chỉ sửa 2 câu văn), thuật toán chia chunk phát hiện chỉ có duy nhất **1 chunk 4MB bị thay đổi** hash.
   - Hệ thống chỉ upload đúng 4MB mới đó lên mây thay vì upload lại toàn bộ 500MB, tiết kiệm $99\%$ băng thông mạng.
4. **Resumable Multipart Upload (Tải lên tiếp tục khi rớt mạng)**:
   - Các chunk 4MB được upload độc lập song song qua nhiều kết nối HTTP.
   - Nếu mạng bị ngắt giữa chừng, client chỉ cần upload lại các chunk chưa hoàn tất mà không phải bắt đầu lại từ đầu.

---

### Case 12: Thiết kế Bảng Xếp Hạng Thời Gian Thực Hàng Triệu Người (Real-time Gaming Leaderboard)

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
Nếu 2 người chơi cùng có $1500$ điểm, ai nộp điểm sớm hơn phải xếp trên:
$$\text{Score}_{\text{composite}} = \text{UserScore} + \left(1 - \frac{\text{Timestamp}}{10^{13}}\right)$$
Điểm tổng hợp lưu vào Redis sẽ là một số thực floating-point, đảm bảo người đạt điểm trước luôn có giá trị lớn hơn một lượng cực nhỏ.

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
- Top 100 toàn cầu luôn nằm trọn vẹn trong **Node 3** (Dải điểm cao nhất), việc lấy Top 100 chỉ truy vấn trên 1 node duy nhất mà không cần cross-node merge!

---

### Case 13: Thiết kế Hệ thống Thu Thập Dữ Liệu Web Phân Tán (Distributed Web Crawler - Googlebot)

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

