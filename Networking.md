# Computer Networking for Backend Engineers — Interview Guide

Tài liệu ôn tập **Networking** dành cho Backend/Java engineer: từ mô hình phân tầng, IP, TCP/UDP, DNS, HTTP, TLS → I/O model, load balancing, networking trên Cloud/Kubernetes → kỹ năng **troubleshooting** và các câu hỏi phỏng vấn thường gặp.

> 💡 **Tài liệu liên quan**:
> - [`SystemDesign.md`](SystemDesign.md): Load Balancer, CDN, HTTP/1.1 vs 2 vs 3, WebSocket/SSE, API Gateway, Rate limiting.
> - [`Java.md`](Java.md): Mục 15 — BIO/NIO/AIO, Selector/epoll, zero-copy, Netty.
> - [`SpringBoot.md`](SpringBoot.md): Mục 6.4 — Embedded Tomcat threading; mục 16 — HTTP client, timeout, resilience.

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


### 🗺 Chuỗi bài toán: gửi một byte từ máy A tới máy B

```
 Quá nhiều việc khác nhau (dây cáp, định tuyến, tin cậy, ứng dụng) → chia TẦNG, mỗi tầng một việc (1)
 Tìm đường tới máy B giữa hàng tỷ máy                  → địa chỉ IP, subnet, routing; thiếu IPv4 → NAT (2)
 Gói tin có thể mất, trùng, đảo thứ tự, mạng bị nghẽn  → TCP: seq/ACK, retransmit, flow & congestion control (3)
   └─ tin cậy kiểu TCP gây chậm (handshake, head-of-line blocking) → UDP, QUIC (4)
 Con người nhớ tên, máy cần IP                          → DNS + cache nhiều tầng (5)
 Trao đổi tài liệu/API theo request–response           → HTTP 1.1 → 2 → 3 (6)
 Mạng không đáng tin (nghe lén, giả mạo)                → TLS: mã hóa + certificate (7)
 Một server phục vụ hàng chục nghìn kết nối             → socket, I/O multiplexing, giới hạn OS (8)
 Một server không đủ                                     → load balancer, proxy, CDN (9)
 Server cần chủ động đẩy dữ liệu / RPC hiệu năng cao    → WebSocket, SSE, gRPC (10)
 Mạng trên cloud & Kubernetes                            → VPC, Service, kube-proxy, CoreDNS (11)
 Kẻ tấn công                                             → DDoS, firewall, zero trust (12)
 Mọi thứ trên đều có thể hỏng                            → timeout, đọc exception, công cụ chẩn đoán (13, 14)
```

## Mục lục
0. [✅ Checklist trọng tâm Networking](#-checklist-trọng-tâm-networking)
1. [Mô hình phân tầng: OSI vs TCP/IP](#1-mô-hình-phân-tầng-osi-vs-tcpip)
2. [Tầng Network: IP, Subnet, NAT, Routing](#2-tầng-network-ip-subnet-nat-routing)
3. [TCP Deep Dive](#3-tcp-deep-dive)
4. [UDP & QUIC](#4-udp--quic)
5. [DNS Deep Dive](#5-dns-deep-dive)
6. [HTTP Deep Dive](#6-http-deep-dive)
7. [TLS / HTTPS](#7-tls--https)
8. [Socket, I/O Model & giới hạn kết nối](#8-socket-io-model--giới-hạn-kết-nối)
9. [Load Balancing, Proxy & CDN](#9-load-balancing-proxy--cdn)
10. [Giao thức ứng dụng: WebSocket, SSE, gRPC](#10-giao-thức-ứng-dụng-websocket-sse-grpc)
11. [Networking trên Cloud & Kubernetes](#11-networking-trên-cloud--kubernetes)
12. [Network Security](#12-network-security)
13. [Networking trong Java & các lỗi thường gặp](#13-networking-trong-java--các-lỗi-thường-gặp)
14. [Troubleshooting Toolkit & quy trình chẩn đoán](#14-troubleshooting-toolkit--quy-trình-chẩn-đoán)
15. [Câu hỏi phỏng vấn Networking](#15-câu-hỏi-phỏng-vấn-networking)

---

## ✅ Checklist trọng tâm Networking

| Mức | Chủ đề | Phải giải thích được | Mục |
| :--- | :--- | :--- | :--- |
| ⭐⭐⭐ | Gõ URL vào trình duyệt thì chuyện gì xảy ra | DNS → TCP → TLS → HTTP → LB → app → render | 15-Q1 |
| ⭐⭐⭐ | TCP | 3-way handshake (vì sao 3), 4-way close, TIME_WAIT/CLOSE_WAIT, flow vs congestion control | 3 |
| ⭐⭐⭐ | TCP vs UDP | Khác nhau thế nào, khi nào dùng UDP | 3, 4 |
| ⭐⭐⭐ | HTTP | Method & idempotency, status code, header cache, keep-alive, HTTP/1.1 vs 2 vs 3, CORS | 6 |
| ⭐⭐⭐ | HTTPS/TLS | Handshake, certificate chain, symmetric vs asymmetric, mTLS | 7 |
| ⭐⭐⭐ | DNS | Luồng phân giải, record types, TTL & cache (kể cả cache của JVM) | 5 |
| ⭐⭐⭐ | Timeout & lỗi kết nối | connection refused vs timeout vs reset; connect vs read timeout | 13, 14 |
| ⭐⭐ | Load balancing | L4 vs L7, thuật toán, health check, sticky session, connection draining | 9 |
| ⭐⭐ | I/O model | Blocking vs non-blocking, epoll, C10K, giới hạn file descriptor/port | 8 |
| ⭐⭐ | IP & subnet | CIDR, private IP, NAT, public/private subnet | 2 |
| ⭐⭐ | Kubernetes networking | Pod IP, Service (ClusterIP/NodePort/LB), Ingress, CoreDNS | 11 |
| ⭐⭐ | WebSocket / gRPC | Upgrade handshake, HTTP/2 streams, deadline | 10 |
| ⭐ | Security | DDoS L3/4 vs L7, SYN flood, firewall/SG, zero trust | 12 |
| ⭐ | Công cụ | `curl -v`, `dig`, `ss`, `tcpdump`, `openssl s_client`, `mtr` | 14 |

---

## 1. Mô hình phân tầng: OSI vs TCP/IP

> 🎯 **Bài toán:** Gửi dữ liệu qua mạng liên quan tới tín hiệu điện/sóng, địa chỉ, định tuyến qua nhiều mạng, đảm bảo tin cậy, định dạng ứng dụng... Một khối phần mềm làm tất cả thì không ai thay được phần nào (đổi Wi-Fi sang cáp quang phải viết lại trình duyệt?).
>
> 💡 **Ý tưởng:** **Chia tầng** — mỗi tầng chỉ dùng dịch vụ của tầng ngay dưới và cung cấp cho tầng trên, mỗi tầng thêm **header** của mình (encapsulation). Nhờ đó HTTP không cần biết đang chạy trên Wi-Fi hay 5G, và có thể thay TCP bằng QUIC mà không đổi ngữ nghĩa HTTP.
>
> ✅ **Vì sao thực tế dùng TCP/IP 4 tầng thay vì OSI 7 tầng:** TCP/IP được xây và triển khai trước, đơn giản hơn; OSI vẫn hữu ích làm **ngôn ngữ chung** ("load balancer L4 vs L7").


```
   OSI (7 tầng — mô hình lý thuyết)          TCP/IP (4 tầng — thực tế)      Đơn vị dữ liệu   Ví dụ
 ┌──────────────────────────────┐          ┌──────────────────────┐
 │ 7. Application               │          │                      │                       HTTP, DNS, gRPC, SMTP, SSH
 │ 6. Presentation (mã hóa, nén)│  ─────►  │ Application          │      Message          TLS*, JSON/Protobuf, gzip
 │ 5. Session                   │          │                      │                       (session/phiên)
 ├──────────────────────────────┤          ├──────────────────────┤
 │ 4. Transport                 │  ─────►  │ Transport            │      Segment/Datagram TCP, UDP, (QUIC trên UDP)
 ├──────────────────────────────┤          ├──────────────────────┤
 │ 3. Network                   │  ─────►  │ Internet             │      Packet           IP, ICMP, (routing: BGP, OSPF)
 ├──────────────────────────────┤          ├──────────────────────┤
 │ 2. Data Link                 │  ─────►  │ Link / Network Access│      Frame            Ethernet, Wi-Fi, ARP, MAC
 │ 1. Physical                  │          │                      │      Bit              Cáp quang, sóng radio
 └──────────────────────────────┘          └──────────────────────┘
 * TLS thường được xếp giữa tầng Transport và Application.
```

- **Encapsulation**: dữ liệu đi xuống mỗi tầng được bọc thêm header của tầng đó: `HTTP data` → `[TCP hdr | HTTP]` → `[IP hdr | TCP | HTTP]` → `[Eth hdr | IP | TCP | HTTP | Eth trailer]`. Bên nhận bóc ngược lại.
- **Thiết bị theo tầng**: Hub (L1) → **Switch** (L2, chuyển frame theo MAC) → **Router** (L3, chuyển packet theo IP) → **L4 Load Balancer / NAT / firewall** (theo IP:port) → **L7 LB / reverse proxy / WAF / API gateway** (hiểu HTTP).
- **Định danh ở mỗi tầng**: MAC address (L2, trong 1 mạng LAN) → IP address (L3, toàn cầu) → Port (L4, xác định process/ứng dụng trên máy). Một kết nối TCP được định danh duy nhất bởi **5-tuple**: `(protocol, src IP, src port, dst IP, dst port)`.

---

## 2. Tầng Network: IP, Subnet, NAT, Routing

> 🎯 **Bài toán:** Tìm đường cho gói tin tới đúng máy giữa hàng tỷ máy, qua nhiều mạng do nhiều tổ chức quản lý; trong khi IPv4 chỉ có ~4,3 tỷ địa chỉ.
>
> 💡 **Ý tưởng:** Địa chỉ **phân cấp** (phần mạng + phần máy — CIDR) để router chỉ cần nhớ đường tới **mạng**, không phải tới từng máy; mỗi router chỉ quyết định **bước tiếp theo** (hop-by-hop). Thiếu địa chỉ → **NAT**: nhiều máy dùng chung một IP công cộng, phân biệt bằng port.
>
> ⚖️ **Cái giá của NAT:** từ bên ngoài không thể chủ động kết nối vào máy sau NAT (cần NAT traversal cho P2P/WebRTC), và bảng NAT có giới hạn số kết nối/port (SNAT port exhaustion trên cloud). IPv6 giải gốc rễ bằng không gian địa chỉ khổng lồ nhưng triển khai chậm vì phải nâng cấp toàn bộ hệ sinh thái.


### 2.1. IPv4, CIDR & Subnet
- IPv4 = 32 bit, viết 4 octet: `192.168.1.10`. **CIDR** `/n` = n bit đầu là phần network.
    ```
    10.0.0.0/16   → mask 255.255.0.0   → 2^16 = 65,536 địa chỉ (10.0.0.0 – 10.0.255.255)
    10.0.1.0/24   → mask 255.255.255.0 → 256 địa chỉ (254 dùng được: trừ network & broadcast)
    10.0.1.0/28   → 16 địa chỉ
    ```
    Công thức: số địa chỉ = $2^{32-n}$. (AWS giữ lại thêm 5 địa chỉ mỗi subnet.)
- **Private IP ranges** (RFC 1918, không định tuyến trên Internet): `10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`. Ngoài ra: `127.0.0.0/8` (loopback), `169.254.0.0/16` (link-local — chứa **cloud metadata endpoint** `169.254.169.254`), `100.64.0.0/10` (CGNAT).
- **Bind address**: server lắng nghe `127.0.0.1` → chỉ máy local truy cập được; `0.0.0.0` → mọi network interface. (Lỗi kinh điển khi chạy app trong Docker: bind `127.0.0.1` nên từ ngoài container không gọi được.)
- **IPv6** = 128 bit (`2001:db8::1`), không cần NAT, header đơn giản hơn; `::1` là loopback. Dual-stack: DNS trả cả `A` và `AAAA`, client thử cả hai (Happy Eyeballs).

### 2.2. NAT (Network Address Translation)
- Nhiều máy private dùng chung 1 IP public: router/NAT gateway **viết lại** IP:port nguồn của gói đi ra và ghi bảng ánh xạ để chuyển gói trả về đúng máy.
- Hệ quả:
  - Từ ngoài **không thể chủ động kết nối vào** máy sau NAT (cần port forwarding, hoặc kỹ thuật NAT traversal STUN/TURN của WebRTC).
  - Bảng NAT có **timeout cho kết nối rảnh** (vd AWS NAT Gateway ~350s) → kết nối TCP dài không có dữ liệu bị âm thầm "quên" → lần gửi sau nhận RST hoặc treo → cần **TCP keepalive** / heartbeat ở tầng ứng dụng.
  - Server thấy nhiều user cùng 1 IP → rate limit theo IP dễ chặn nhầm cả công ty/mạng di động.
  - SNAT giới hạn ~64k port nguồn cho mỗi (IP đích, port đích) → service gọi ra ngoài rất nhiều kết nối ngắn có thể cạn port ở NAT gateway.

### 2.3. Routing, ARP, ICMP, MTU
- Mỗi máy có **routing table**: đích thuộc subnet của mình → gửi trực tiếp (tìm MAC bằng **ARP**); khác subnet → gửi tới **default gateway**. Router giữa các mạng lớn trên Internet trao đổi tuyến đường bằng **BGP**.
- **ICMP**: thông điệp điều khiển/lỗi của IP — `ping` (echo request/reply), "destination unreachable", "TTL exceeded" (cơ chế của `traceroute`).
- **TTL** (time to live) trong IP header giảm 1 qua mỗi router → về 0 thì gói bị hủy (chống vòng lặp định tuyến).
- **MTU** (Maximum Transmission Unit) Ethernet thường 1500 byte → TCP **MSS** ≈ 1460 byte payload. Đường đi có MTU nhỏ hơn (VPN, tunnel, overlay network của K8s) → gói lớn bị phân mảnh hoặc bị drop; nếu ICMP "fragmentation needed" bị firewall chặn → **MTU black hole**: kết nối thiết lập được, request nhỏ chạy được, nhưng response lớn/TLS handshake bị treo.

---

## 3. TCP Deep Dive

> 🎯 **Bài toán:** Tầng IP chỉ **cố gắng hết sức**: gói tin có thể mất, trùng, đến sai thứ tự, và mạng có thể bị nghẽn. Ứng dụng lại muốn một **luồng byte tin cậy, đúng thứ tự**.
>
> 💡 **Ý tưởng — mỗi vấn đề một cơ chế:**
>
> | Vấn đề | Cơ chế của TCP |
> | :--- | :--- |
> | Mất gói | ACK + **retransmission** (timeout hoặc 3 duplicate ACK) |
> | Sai thứ tự, trùng | **Sequence number** — bên nhận sắp xếp lại và loại trùng |
> | Hỏng dữ liệu | Checksum |
> | Bên nhận xử lý không kịp | **Flow control** — receive window |
> | Mạng bị nghẽn | **Congestion control** — slow start, giảm tốc khi mất gói (CUBIC, BBR) |
> | Hai bên phải thống nhất số thứ tự bắt đầu | **3-way handshake** |
> | Gói cũ lạc đường lẫn vào kết nối mới | **TIME_WAIT** chờ 2×MSL |


### 3.1. Đặc tính
TCP cung cấp luồng byte **tin cậy, đúng thứ tự, không trùng lặp, có kiểm soát luồng & tắc nghẽn**, hướng kết nối, full-duplex. Cơ chế: sequence number + ACK + retransmission + checksum + sliding window.

```
TCP header (20–60 byte): Source Port | Dest Port | Sequence Number | Acknowledgment Number |
                         Header len | Flags (SYN, ACK, FIN, RST, PSH, URG) | Window Size | Checksum | Options (MSS, Window Scale, SACK, Timestamps)
```

### 3.2. Three-way Handshake
```
Client                                    Server (LISTEN)
  │ ── SYN, seq=x ──────────────────────►  │   SYN_RECEIVED (vào SYN queue)
  │ ◄── SYN+ACK, seq=y, ack=x+1 ─────────  │
  │ ── ACK, ack=y+1 ────────────────────►  │   ESTABLISHED (chuyển sang ACCEPT queue → app gọi accept())
ESTABLISHED
```
- **Vì sao cần 3 bước, không phải 2?** Mỗi bên cần (1) gửi sequence number ban đầu (ISN) của mình và (2) **được xác nhận** là bên kia đã nhận → cần SYN(client) + ACK/SYN(server) + ACK(client). Với 2 bước, server không biết client đã nhận được ISN của nó chưa, và một **SYN cũ bị trễ** trên mạng có thể khiến server mở kết nối "ma".
- **ISN ngẫu nhiên** để chống đoán sequence (TCP hijacking) và tránh nhầm gói của kết nối cũ.
- Chi phí: **1 RTT** trước khi gửi được dữ liệu (+ 1–2 RTT cho TLS) → lý do phải tái sử dụng kết nối (keep-alive, connection pool). **TCP Fast Open** cho phép gửi dữ liệu ngay trong SYN khi kết nối lại (ít được dùng rộng rãi).

**SYN queue & Accept queue (backlog)**:
- **SYN queue**: kết nối đang handshake dở (nhận SYN, chờ ACK). **SYN flood** = gửi hàng loạt SYN giả không bao giờ ACK → đầy queue → phòng chống bằng **SYN cookies** (server không lưu trạng thái, mã hóa thông tin vào ISN).
- **Accept queue**: kết nối đã ESTABLISHED chờ ứng dụng `accept()`. Kích thước = min(`backlog` của `listen()`, `net.core.somaxconn`). Ứng dụng accept không kịp (server quá tải) → queue đầy → kernel drop SYN/ACK → client thấy **connect timeout** hoặc phải retransmit SYN (1s, 3s, 7s...) → latency tăng đột biến theo bậc thang.
- Liên hệ Tomcat: `server.tomcat.accept-count` chính là backlog (`SpringBoot.md` 6.4).

### 3.3. Đóng kết nối: Four-way Close, TIME_WAIT, CLOSE_WAIT
```
Active closer (A)                           Passive closer (B)
ESTABLISHED                                 ESTABLISHED
  │ ── FIN ───────────────────────────────►  │  CLOSE_WAIT  (B nhận FIN, chờ ỨNG DỤNG của B gọi close())
FIN_WAIT_1                                   │
  │ ◄── ACK ───────────────────────────────  │
FIN_WAIT_2                                   │  ... B có thể vẫn gửi dữ liệu (half-close) ...
  │ ◄── FIN ───────────────────────────────  │  LAST_ACK    (ứng dụng B đã close())
TIME_WAIT  ── ACK ────────────────────────►  │  CLOSED
  │ (chờ 2×MSL, Linux ~60s)
CLOSED
```
- **Vì sao 4 bước?** TCP full-duplex: mỗi chiều đóng độc lập. B nhận FIN (A không gửi nữa) nhưng B có thể còn dữ liệu cần gửi → ACK trước, FIN sau. (Có thể gộp FIN+ACK thành 3 bước nếu B đóng ngay.)
- **TIME_WAIT** (ở bên **chủ động đóng**) tồn tại để: (1) gửi lại ACK cuối nếu ACK bị mất và B gửi lại FIN; (2) đợi các gói trễ của kết nối cũ biến mất, tránh lẫn vào kết nối mới cùng 5-tuple.
    - Nhiều TIME_WAIT phía **client** (service gọi ra ngoài bằng kết nối ngắn, không pool) → cạn **ephemeral port** (`net.ipv4.ip_local_port_range` ~28k port) → lỗi `Cannot assign requested address`. Giải pháp đúng: **connection pooling / keep-alive**; `tcp_tw_reuse` (cho kết nối đi ra). (Đừng dùng `tcp_tw_recycle` — đã bị xóa khỏi Linux 4.12 vì gây lỗi với NAT.)
- **CLOSE_WAIT** nhiều = **bug ở ứng dụng của bạn**: phía bên kia đã đóng nhưng code của bạn **không bao giờ gọi `close()`** (quên đóng stream/response/connection, không trả connection về pool). Số CLOSE_WAIT tăng dần → rò rỉ file descriptor → `Too many open files`.
- **RST (reset)**: đóng đột ngột — gửi tới port không có ai lắng nghe (→ "Connection refused"), ứng dụng đóng socket khi còn dữ liệu chưa đọc, `SO_LINGER=0`, firewall/LB/NAT chủ động cắt, hoặc gửi dữ liệu vào kết nối mà bên kia đã quên (sau timeout) → "Connection reset by peer".

### 3.4. Độ tin cậy: Retransmission
- Mỗi byte có sequence number; bên nhận gửi **cumulative ACK** (đã nhận liên tục tới byte n).
- **RTO** (Retransmission Timeout) tính động từ RTT đo được; hết RTO chưa nhận ACK → gửi lại, RTO nhân đôi (exponential backoff).
- **Fast retransmit**: nhận 3 duplicate ACK → gửi lại ngay không chờ RTO. **SACK** (Selective ACK) cho bên gửi biết chính xác những đoạn nào đã tới → chỉ gửi lại phần thiếu.
- Mất gói ảnh hưởng **mọi dữ liệu phía sau** trên cùng kết nối (head-of-line blocking ở tầng TCP) → lý do HTTP/3 chuyển sang QUIC.

### 3.5. Flow Control vs Congestion Control (hay bị nhầm)
| | **Flow control** | **Congestion control** |
| :--- | :--- | :--- |
| Bảo vệ ai | **Bên nhận** khỏi bị gửi quá nhanh | **Mạng** khỏi bị nghẽn |
| Cơ chế | **Receive window (rwnd)** do bên nhận quảng cáo trong mỗi ACK (bằng chỗ trống buffer nhận); window = 0 → bên gửi dừng | **Congestion window (cwnd)** do bên gửi tự ước lượng |
| Lượng dữ liệu đang bay tối đa | `min(rwnd, cwnd)` | |

- **Congestion control**:
    - **Slow start**: cwnd bắt đầu nhỏ (~10 MSS ≈ 14 KB), **tăng gấp đôi mỗi RTT** tới ngưỡng `ssthresh`. → kết nối mới **không thể** dùng hết băng thông ngay → thêm lý do tái sử dụng kết nối; "nhét" HTML quan trọng vào ~14 KB đầu tiên.
    - **Congestion avoidance (AIMD)**: tăng tuyến tính +1 MSS/RTT; khi mất gói → giảm mạnh (Reno: cwnd/2).
    - **CUBIC** (mặc định Linux): hàm bậc 3, phù hợp mạng băng thông lớn. **BBR** (Google): dựa trên đo băng thông & RTT thực tế thay vì coi mất gói là tắc nghẽn → tốt hơn nhiều trên mạng có mất gói ngẫu nhiên (mobile, liên lục địa).
- **Window scaling**: trường Window chỉ 16 bit (64 KB) → option scale cho phép window tới 1 GB, cần cho **bandwidth-delay product** lớn: BDP = băng thông × RTT (vd 1 Gbps × 100 ms = 12.5 MB phải "đang bay" để tận dụng đường truyền).

### 3.6. Các tùy chọn socket & cơ chế hay gặp
- **Nagle's algorithm**: gom nhiều gói nhỏ thành 1 gói (chờ ACK của gói trước) → kết hợp với **Delayed ACK** của bên nhận (chờ tới ~40 ms để gửi gộp ACK) gây **độ trễ 40 ms** bí ẩn cho giao thức request/response gửi dữ liệu nhỏ nhiều lần. Giải pháp: `TCP_NODELAY = true` (Netty, gRPC, hầu hết HTTP client/DB driver đều bật sẵn).
- **TCP keepalive** (`SO_KEEPALIVE`): sau thời gian rảnh (Linux mặc định **2 giờ**!) gửi probe để phát hiện peer chết/kết nối đứt. Mặc định quá dài so với timeout của NAT/LB/firewall (vài phút) → cấu hình kernel (`tcp_keepalive_time`) hoặc heartbeat ở tầng ứng dụng. ≠ **HTTP keep-alive** (tái sử dụng kết nối cho nhiều request).
- **`SO_REUSEADDR`**: cho phép server bind lại port khi còn kết nối cũ ở TIME_WAIT (restart server không bị "Address already in use"). **`SO_REUSEPORT`**: nhiều socket/process cùng lắng nghe 1 port, kernel chia tải (NGINX, Envoy).
- **Half-open connection**: 1 bên chết (mất điện, rút cáp) không gửi được FIN/RST → bên kia vẫn tưởng kết nối sống mãi cho tới khi gửi dữ liệu (rồi timeout) hoặc keepalive phát hiện → luôn cần **read timeout**/heartbeat.

✅ **Vì sao handshake là 3 bước chứ không phải 2:** mỗi bên phải gửi số thứ tự ban đầu của mình **và** biết chắc bên kia đã nhận được nó. Với 2 bước, server không biết client có nhận được ISN của server không, và một SYN cũ bị trễ trên mạng có thể khiến server mở một kết nối "ma".

> 🛠 **Áp dụng:**
> - Thiết lập kết nối TCP + TLS tốn 2–3 RTT → **tái sử dụng kết nối** (keep-alive, connection pool) là tối ưu quan trọng nhất cho gọi service.
> - Nhiều `TIME_WAIT` ở phía client → dùng pool kết nối thay vì mở/đóng mỗi request; nhiều `CLOSE_WAIT` → **ứng dụng quên đóng kết nối** (bug code, không phải lỗi mạng).
> - Đường truyền xa, băng thông lớn (cross-region) → throughput bị giới hạn bởi window/RTT; cần window scaling và có thể BBR.

<details><summary>🧠 Tự kiểm tra</summary>

1. **Flow control và congestion control khác nhau thế nào?**
   → Flow control bảo vệ **bên nhận** (bên nhận quảng bá window); congestion control bảo vệ **mạng** (bên gửi tự ước lượng qua mất gói/RTT). Lượng gửi = min(hai window).
2. **Vì sao gửi `write()` 100 byte, bên kia có thể `read()` được 60 byte?**
   → TCP là **luồng byte**, không có ranh giới message → ứng dụng phải tự đóng khung (length-prefix, delimiter).
</details>


---

## 4. UDP & QUIC

> 🎯 **Bài toán:** Sự tin cậy của TCP có cái giá: handshake trước khi gửi, và **head-of-line blocking** — một gói mất làm mọi dữ liệu phía sau phải chờ. Với game, gọi video, DNS thì dữ liệu **cũ đã vô dụng**; còn HTTP/2 chạy nhiều stream trên một TCP thì một gói mất chặn tất cả stream.
>
> 💡 **Ý tưởng:** UDP chỉ gửi gói, không đảm bảo gì → ứng dụng tự chọn mức tin cậy cần thiết. **QUIC** tận dụng điều đó: cài đặt tin cậy **theo từng stream** + TLS 1.3 tích hợp **trong user space trên UDP**.
>
> ✅ **Vì sao QUIC chọn xây trên UDP thay vì sửa TCP:** TCP nằm trong kernel của hàng tỷ thiết bị và bị middlebox (firewall, NAT) "đóng băng" — thay đổi giao thức TCP gần như không thể triển khai; UDP đi qua được hầu hết mạng và QUIC có thể cập nhật cùng ứng dụng.


| | TCP | UDP |
| :--- | :--- | :--- |
| Kết nối | Có (handshake) | Không |
| Tin cậy, thứ tự | Có | Không — có thể mất, trùng, đảo thứ tự |
| Ranh giới message | **Byte stream** — không có ranh giới (ứng dụng tự đóng khung: length-prefix, delimiter) | **Datagram** — mỗi lần gửi là 1 gói nguyên vẹn |
| Kiểm soát luồng/tắc nghẽn | Có | Không (ứng dụng tự lo) |
| Header | 20–60 byte | 8 byte |
| Dùng cho | HTTP/1.1–2, DB, SSH, email | DNS, VoIP/video call, game, streaming realtime, QUIC/HTTP/3, metrics (StatsD), syslog, DHCP |

- Khi nào UDP hợp lý: dữ liệu **mới quan trọng hơn dữ liệu cũ** (vị trí nhân vật trong game, khung hình video — gói trễ thì bỏ), request/response nhỏ 1 gói (DNS), broadcast/multicast, hoặc muốn tự cài đặt cơ chế tin cậy riêng ở user space.
- **QUIC** (nền của HTTP/3): xây trên UDP nhưng tự cài đặt tin cậy, congestion control, **mã hóa TLS 1.3 tích hợp**, **nhiều stream độc lập** (mất gói của stream này không chặn stream khác), handshake 1-RTT/0-RTT, **connection migration** bằng Connection ID (đổi Wi-Fi → 4G không mất kết nối). Xem `SystemDesign.md` 7.3.
- Lỗi kinh điển với TCP byte stream: tưởng 1 lần `write(100 bytes)` thì bên kia 1 lần `read()` nhận đủ 100 bytes — **sai**, có thể nhận 60 rồi 40 (hoặc dính với message sau) → giao thức phải có **framing** (Netty `LengthFieldBasedFrameDecoder`).

---

## 5. DNS Deep Dive

> 🎯 **Bài toán:** Con người dùng tên (`api.example.com`), máy cần IP; có hàng trăm triệu tên miền, thay đổi liên tục, do hàng triệu tổ chức quản lý, được hỏi hàng nghìn tỷ lần mỗi ngày.
>
> 💡 **Ý tưởng:** Cơ sở dữ liệu **phân tán và phân cấp** (root → TLD → authoritative): mỗi tổ chức tự quản lý vùng của mình; **cache ở mọi tầng** với **TTL** để giảm tải. Cái giá của cache: thay đổi bản ghi **không có hiệu lực ngay** — phải chờ hết TTL.


### 5.1. Luồng phân giải
```
App ──► (1) cache trong ứng dụng/JVM ──► (2) OS: /etc/hosts, cache của OS (systemd-resolved, nscd) — đọc /etc/resolv.conf
     ──► (3) Recursive resolver (ISP, 8.8.8.8, 1.1.1.1, CoreDNS trong K8s) — có cache
            ──► (4) Root server "." ──► trả về NS của ".com"
            ──► (5) TLD server ".com" ──► trả về NS của "example.com"
            ──► (6) Authoritative server của example.com ──► trả về bản ghi A = 93.184.216.34 (kèm TTL)
```
- Client → resolver: truy vấn **recursive** ("hãy tìm hộ tôi"); resolver → root/TLD/authoritative: truy vấn **iterative** ("hỏi tiếp ở đây").
- Giao thức: **UDP port 53** (nhanh); chuyển sang **TCP 53** khi response lớn (> 512 byte nếu không có EDNS, DNSSEC, zone transfer). Mã hóa: **DoT** (DNS over TLS, 853), **DoH** (DNS over HTTPS).

### 5.2. Các loại bản ghi
| Record | Ý nghĩa | Ví dụ / Lưu ý |
| :--- | :--- | :--- |
| **A / AAAA** | Tên → IPv4 / IPv6 | Nhiều bản ghi A → round-robin DNS đơn giản |
| **CNAME** | Bí danh → tên khác | `www → example.com`; ⚠️ không đặt CNAME ở **zone apex** (`example.com`) — dùng ALIAS/ANAME/Route 53 Alias |
| **NS** | Name server quản lý zone | Ủy quyền (delegation) |
| **SOA** | Thông tin zone (serial, TTL cho phản hồi "không tồn tại") | |
| **MX** | Mail server (có priority) | |
| **TXT** | Văn bản tùy ý | SPF, DKIM, DMARC, xác minh domain (Google, Let's Encrypt DNS-01) |
| **SRV** | Host + **port** của một dịch vụ | `_sip._tcp`, service discovery (Consul, K8s headless) |
| **PTR** | IP → tên (reverse DNS) | Mail server bắt buộc có |
| **CAA** | CA nào được phép cấp cert cho domain | |

### 5.3. TTL, Cache & hệ quả thực tế
- Mỗi bản ghi có **TTL**; mọi tầng (resolver, OS, browser, **JVM**) đều cache → đổi IP (migrate server, failover) phải chờ TTL cũ hết hạn ở khắp nơi. Chuẩn bị migration: **hạ TTL xuống thấp (60s) trước vài ngày**, đổi IP, sau đó tăng TTL lại.
- **Negative caching**: kết quả "không tồn tại" (NXDOMAIN) cũng bị cache (theo SOA).
- **DNS-based load balancing / GeoDNS**: trả IP khác nhau theo vị trí người dùng hoặc tình trạng sức khỏe (Route 53 latency/failover routing) → failover chậm cỡ TTL và bị ảnh hưởng bởi client cache "cứng đầu".
- **Anycast**: cùng 1 IP được quảng bá từ nhiều nơi trên thế giới qua BGP → gói tự đi tới điểm gần nhất (root DNS, 1.1.1.1, CDN, Cloudflare).
- ⚠️ **JVM DNS cache**: Java tự cache kết quả DNS (`networkaddress.cache.ttl`; mặc định **30 giây** khi không có Security Manager, nhưng **cache vĩnh viễn** trong một số cấu hình cũ có Security Manager) và cache **kết quả lỗi** 10s (`networkaddress.cache.negative.ttl`). App chạy lâu năm gọi tới endpoint đổi IP (ELB, RDS failover) có thể kẹt IP cũ → đặt TTL rõ ràng: `java.security.Security.setProperty("networkaddress.cache.ttl", "60")` hoặc trong file `java.security`.
- **Kubernetes DNS**: `/etc/resolv.conf` trong pod có `search <ns>.svc.cluster.local svc.cluster.local cluster.local` và **`ndots:5`** → tên ngoài như `api.stripe.com` (ít hơn 5 dấu chấm) bị thử ghép với từng search domain trước (4–5 truy vấn thất bại) → tăng latency & tải CoreDNS. Khắc phục: dùng FQDN có dấu chấm cuối (`api.stripe.com.`), giảm `ndots`, NodeLocal DNSCache.
- **Sự cố DNS** là nguyên nhân outage kinh điển ("It's always DNS"): resolver chậm làm mọi kết nối mới chậm, TTL cache sai, CoreDNS quá tải, bản ghi bị xóa nhầm.

> 🛠 **Áp dụng:**
> - Chuẩn bị chuyển đổi (đổi IP, failover) → **hạ TTL trước** vài giờ.
> - JVM cache DNS (`networkaddress.cache.ttl`); client pool giữ kết nối lâu sẽ **không** thấy IP mới cho tới khi kết nối được mở lại → đặt thời gian sống tối đa cho kết nối.
> - Trên Kubernetes, `ndots:5` khiến tên ngoài cụm bị thử nhiều hậu tố → dùng FQDN có dấu chấm cuối hoặc chỉnh `ndots` (11.2).

<details><summary>🧠 Tự kiểm tra</summary>

1. **Vì sao DNS dùng UDP mà không dùng TCP?**
   → Truy vấn/đáp thường vừa một gói; UDP không cần handshake → nhanh, server phục vụ được nhiều client. Khi câu trả lời lớn (DNSSEC, zone transfer) thì chuyển sang TCP.
</details>


---

## 6. HTTP Deep Dive

> 🎯 **Bài toán:** Một giao thức chung cho trình duyệt, API, ứng dụng di động — đơn giản, mở rộng được, cache được, và ngày càng phải nhanh hơn trên mạng di động có RTT cao.
>
> 💡 **Quá trình tiến hóa là chuỗi bài toán → cách giải:**
>
> | Phiên bản | Bài toán của bản trước | Cách giải | Hạn chế còn lại |
> | :--- | :--- | :--- | :--- |
> | HTTP/1.1 | 1.0 mở kết nối mới cho mỗi request | Keep-alive | Một kết nối chỉ một request tại một thời điểm → trình duyệt mở 6 kết nối |
> | HTTP/2 | Head-of-line blocking ở tầng HTTP, header lặp lại | **Multiplexing** nhiều stream trên 1 kết nối, nén header (HPACK), dạng nhị phân | Head-of-line blocking chuyển xuống **tầng TCP** |
> | HTTP/3 | HOL blocking của TCP, handshake TCP+TLS tốn RTT | Chạy trên **QUIC** (UDP), stream độc lập, 1-RTT/0-RTT | UDP có thể bị chặn ở vài mạng → luôn có fallback HTTP/2 |


### 6.1. Method — Safe & Idempotent
| Method | Safe (không đổi trạng thái) | Idempotent (gọi N lần = 1 lần) | Ghi chú |
| :--- | :--- | :--- | :--- |
| GET, HEAD, OPTIONS | ✅ | ✅ | Có thể cache (GET/HEAD) |
| PUT | ❌ | ✅ | Thay thế toàn bộ resource |
| DELETE | ❌ | ✅ | Xóa lần 2 vẫn là "đã xóa" (status có thể 404) |
| POST | ❌ | ❌ | Tạo mới / hành động → cần `Idempotency-Key` để retry an toàn |
| PATCH | ❌ | ❌ (tùy cách thiết kế) | Sửa một phần |
- Ý nghĩa thực tế: client, proxy, LB, service mesh **chỉ được tự động retry** request idempotent khi lỗi mạng.

### 6.2. Status code cần nhớ
| Nhóm | Hay gặp |
| :--- | :--- |
| 1xx | `101 Switching Protocols` (WebSocket), `103 Early Hints` |
| 2xx | `200 OK`, `201 Created` (+ `Location`), `202 Accepted` (xử lý async), `204 No Content`, `206 Partial Content` (Range) |
| 3xx | `301` (vĩnh viễn, có thể đổi POST→GET), `302` (tạm thời), `304 Not Modified` (cache hợp lệ), **`307/308`** (như 302/301 nhưng **giữ nguyên method & body**) |
| 4xx | `400`, `401` (chưa xác thực), `403` (không có quyền), `404`, `405`, `409 Conflict`, `412 Precondition Failed` (ETag), `413 Payload Too Large`, `415`, `422`, `429 Too Many Requests` |
| 5xx | `500`, **`502 Bad Gateway`** (proxy nhận response lỗi/kết nối bị đóng từ upstream), **`503 Service Unavailable`** (quá tải/không có backend khỏe), **`504 Gateway Timeout`** (upstream không trả lời kịp trong timeout của proxy) |
- Chẩn đoán qua proxy: **502** → upstream sập, từ chối kết nối, hoặc **đóng kết nối keep-alive đúng lúc proxy đang gửi** (keep-alive timeout của app nhỏ hơn của LB — xem 13.3); **504** → upstream chậm hơn timeout của proxy.

### 6.3. Kết nối: Keep-alive, Pipelining, Multiplexing
- **HTTP/1.0**: mỗi request 1 kết nối TCP. **HTTP/1.1**: **persistent connection** (keep-alive) mặc định; pipelining tồn tại trên lý thuyết nhưng gần như không dùng → trình duyệt mở ~6 kết nối/host.
- **HTTP/2**: 1 kết nối, nhiều **stream** song song (multiplexing), binary framing, HPACK nén header, flow control theo stream. Vẫn bị HOL blocking ở tầng TCP khi mất gói.
- **HTTP/3**: QUIC trên UDP — xem 4 và `SystemDesign.md` 7.3.
- **Connection pool phía client**: giới hạn kết nối/host, idle timeout của pool phải **nhỏ hơn** idle timeout của server/LB, kiểm tra kết nối cũ trước khi dùng (validate/evict idle) — xem 13.3.

### 6.4. Caching HTTP
```http
Cache-Control: public, max-age=31536000, immutable      # asset có hash trong tên file: app.3f9a1c.js
Cache-Control: private, no-cache                         # được lưu nhưng PHẢI hỏi lại server trước khi dùng
Cache-Control: no-store                                  # không lưu ở đâu cả (dữ liệu nhạy cảm)
Cache-Control: max-age=60, stale-while-revalidate=30     # hết hạn vẫn trả bản cũ trong 30s trong khi làm mới nền
ETag: "a1b2c3"          →  request sau: If-None-Match: "a1b2c3"      →  304 Not Modified (không gửi lại body)
Last-Modified: <date>   →  request sau: If-Modified-Since: <date>    →  304
Vary: Accept-Encoding, Accept-Language                   # cache tách bản theo các header này
```
- `no-cache` ≠ "không cache" (đó là `no-store`). `s-maxage` áp dụng riêng cho shared cache (CDN). ETag cũng dùng cho **optimistic concurrency** (`If-Match` → 412).

### 6.5. Cookie & CORS
- Thuộc tính cookie: `HttpOnly` (JS không đọc được — chống đánh cắp qua XSS), `Secure` (chỉ gửi qua HTTPS), `SameSite=Strict|Lax|None` (chống CSRF; `None` bắt buộc kèm `Secure`), `Domain`, `Path`, `Max-Age/Expires`.
- **Same-Origin Policy**: origin = scheme + host + port. JS chỉ đọc được response cùng origin, trừ khi server cho phép bằng **CORS**:
    ```
    Browser (app.example.com) muốn PUT JSON tới api.example.com:
      1. Preflight:  OPTIONS /orders
                     Origin: https://app.example.com
                     Access-Control-Request-Method: PUT
                     Access-Control-Request-Headers: content-type, authorization
      2. Server:     204
                     Access-Control-Allow-Origin: https://app.example.com
                     Access-Control-Allow-Methods: GET, PUT
                     Access-Control-Allow-Headers: content-type, authorization
                     Access-Control-Allow-Credentials: true   (nếu gửi cookie)
                     Access-Control-Max-Age: 600              (cache kết quả preflight)
      3. Browser gửi request PUT thật.
    ```
    - "Simple request" (GET/POST với content-type form/text, không header lạ) không cần preflight nhưng vẫn cần `Access-Control-Allow-Origin` để JS đọc được response.
    - CORS **do trình duyệt thực thi** — không phải cơ chế bảo vệ server (curl/Postman không bị CORS). `Allow-Origin: *` không dùng được cùng credentials.

### 6.6. Một số header & tính năng khác
- **Content negotiation**: `Accept`, `Accept-Encoding` (gzip, br, zstd) ↔ `Content-Type`, `Content-Encoding`.
- **Transfer-Encoding: chunked** (HTTP/1.1): gửi body chưa biết trước độ dài (streaming, SSE). **Range** requests (`Range: bytes=0-1023` → 206): resume download, video seek.
- **Proxy headers**: `X-Forwarded-For` (chuỗi IP client qua các proxy — chỉ tin phần do proxy **của mình** thêm vào, client có thể giả mạo), `X-Forwarded-Proto`, `X-Forwarded-Host`, chuẩn mới `Forwarded`. Spring Boot: `server.forward-headers-strategy=native|framework`.
- **Request smuggling** (lỗ hổng): proxy và backend hiểu ranh giới request khác nhau (`Content-Length` vs `Transfer-Encoding`) → cập nhật proxy/server, dùng HTTP/2 tới backend.

---

## 7. TLS / HTTPS

> 🎯 **Bài toán:** Dữ liệu đi qua nhiều router, Wi-Fi công cộng, ISP — ai cũng có thể **đọc**, **sửa**, hoặc **giả làm** server.
>
> 💡 **Ý tưởng:** Kết hợp ba công cụ, mỗi cái giải một phần:
> - **Mã hóa bất đối xứng / trao đổi khóa (ECDHE)** — thỏa thuận khóa bí mật chung qua kênh không an toàn (chậm, chỉ dùng lúc handshake).
> - **Mã hóa đối xứng (AES-GCM, ChaCha20)** — mã hóa dữ liệu thật (nhanh) + chống sửa đổi.
> - **Certificate do CA ký** — chứng minh public key thật sự thuộc về `example.com` (chống giả mạo).
>
> ✅ **Vì sao TLS 1.3 bỏ trao đổi khóa RSA, chỉ giữ ECDHE:** RSA key exchange không có **forward secrecy** — lộ private key của server thì giải mã được mọi phiên đã ghi lại trước đó; ECDHE sinh khóa tạm cho mỗi phiên. Đồng thời rút handshake từ 2 RTT xuống 1 RTT.


### 7.1. Mục tiêu & mật mã cơ bản
TLS cung cấp: **Confidentiality** (mã hóa), **Integrity** (không bị sửa — AEAD/MAC), **Authentication** (đúng server, qua certificate).
- **Mã hóa bất đối xứng** (RSA, ECDHE, ECDSA): chậm → chỉ dùng cho **trao đổi khóa** và **chữ ký**.
- **Mã hóa đối xứng** (AES-GCM, ChaCha20-Poly1305): nhanh → mã hóa toàn bộ dữ liệu bằng **session key** đã thỏa thuận.
- **Hash** (SHA-256): toàn vẹn, chữ ký số.

### 7.2. Handshake TLS 1.2 vs TLS 1.3
```
TLS 1.2 (2 RTT):                                  TLS 1.3 (1 RTT):
C → S: ClientHello                                C → S: ClientHello + key_share (đoán trước nhóm ECDHE)
S → C: ServerHello, Certificate,                  S → C: ServerHello + key_share, {Certificate, CertificateVerify, Finished}  (đã mã hóa)
       ServerKeyExchange, ServerHelloDone         C → S: {Finished} + HTTP request
C → S: ClientKeyExchange, ChangeCipherSpec, Finished
S → C: ChangeCipherSpec, Finished
C → S: HTTP request
```
- TLS 1.3: bỏ RSA key exchange (bắt buộc **forward secrecy** với ECDHE), bỏ cipher yếu, mã hóa cả certificate, **0-RTT resumption** (nguy cơ replay → chỉ cho request idempotent).
- **SNI** (Server Name Indication): client gửi tên miền trong ClientHello → 1 IP phục vụ nhiều domain/certificate. (ECH — Encrypted Client Hello — đang được triển khai để giấu cả SNI.)
- **ALPN**: thỏa thuận giao thức ứng dụng trong handshake (`h2`, `http/1.1`).
- **Session resumption** (session ticket/PSK): kết nối lại bỏ qua phần trao đổi khóa đắt đỏ.

### 7.3. Certificate & chuỗi tin cậy
```
Root CA (tự ký, có sẵn trong trust store của OS/browser/JVM)
   └── Intermediate CA (ký bởi Root)
          └── Leaf certificate: CN/SAN = api.example.com, public key, thời hạn, chữ ký của Intermediate
```
- Client kiểm tra: chữ ký từng tầng tới root tin cậy, **SAN khớp hostname**, còn hạn, chưa bị thu hồi (CRL/OCSP; server nên bật **OCSP stapling**).
- Server **phải gửi kèm intermediate** cert (full chain); thiếu → browser có thể vẫn chạy (tự tải) nhưng Java/curl báo lỗi.
- Thời hạn cert công khai ngày càng ngắn (Let's Encrypt 90 ngày, CA/Browser Forum đã thông qua lộ trình rút dần về 47 ngày vào 2029) → **tự động gia hạn** (ACME, cert-manager trên K8s) là bắt buộc.
- **HSTS** (`Strict-Transport-Security: max-age=...; includeSubDomains; preload`): ép trình duyệt luôn dùng HTTPS, chống SSL stripping.
- **mTLS**: client cũng trình certificate → xác thực service-to-service (service mesh, API B2B, ngân hàng).
- **Certificate pinning** (mobile): app chỉ tin 1 cert/public key cụ thể → chống MITM bằng CA giả, nhưng xoay vòng cert phức tạp.

### 7.4. TLS trong Java (lỗi kinh điển)
- **Keystore** (chứa private key + cert **của mình**, dùng cho server hoặc mTLS client) vs **Truststore** (chứa cert CA mà mình **tin**). JDK có truststore mặc định `$JAVA_HOME/lib/security/cacerts`.
- `PKIX path building failed: unable to find valid certification path to requested target` → JVM không tin CA của server (CA nội bộ công ty, self-signed, server thiếu intermediate cert, proxy SSL inspection của công ty) → import CA vào truststore (`keytool -importcert`) hoặc cấu hình truststore riêng. **Không** tắt kiểm tra certificate (`TrustAllCerts`) ở production.
- `No subject alternative names matching IP address` / hostname mismatch → gọi bằng IP hoặc tên không có trong SAN.
- `SSLHandshakeException: Received fatal alert: handshake_failure / protocol_version` → không có chung phiên bản TLS/cipher (server cũ chỉ TLS 1.0, JDK mới đã tắt).
- Debug: `-Djavax.net.debug=ssl:handshake`, `openssl s_client -connect host:443 -servername host -showcerts`.
- Spring Boot 3.1+ có **SSL Bundles** (`spring.ssl.bundle.*`) để cấu hình keystore/truststore (kể cả PEM) và hot-reload certificate.

---

## 8. Socket, I/O Model & giới hạn kết nối

> 🎯 **Bài toán:** Server cần giữ hàng chục nghìn kết nối đồng thời, phần lớn đang rảnh. Mô hình một thread mỗi kết nối tốn bộ nhớ và context switch; ngoài ra OS còn có giới hạn số file descriptor, port, hàng đợi kết nối.
>
> 💡 **Ý tưởng:** **I/O multiplexing** (`epoll`/`kqueue`) — hỏi kernel "kết nối nào đang có dữ liệu" để một thread phục vụ nhiều kết nối; hoặc virtual threads trong Java che giấu cơ chế này sau API blocking. Đồng thời phải nâng giới hạn OS (`ulimit -n`, `somaxconn`, dải port tạm).


### 8.1. Vòng đời socket
```
Server: socket() → bind(port) → listen(backlog) → accept() [lặp] → read()/write() → close()
Client: socket() → connect(host, port) [3-way handshake] → write()/read() → close()
```
- Mỗi kết nối = 1 **file descriptor** (fd). Server lắng nghe 1 port nhưng có thể có hàng trăm nghìn kết nối (phân biệt bằng 5-tuple).

### 8.2. Mô hình I/O & C10K
- **C10K problem** (1999): phục vụ 10.000 kết nối đồng thời. Mô hình **thread-per-connection** (blocking I/O) tốn 1 thread/kết nối → bộ nhớ stack + context switch → không scale.
- Giải pháp: **non-blocking I/O + I/O multiplexing** (`select` → `poll` → **`epoll`**/kqueue → `io_uring`): 1 thread theo dõi hàng nghìn socket, chỉ xử lý socket sẵn sàng → **event loop / Reactor** (NGINX, Node.js, Netty, Redis).
- Java hiện đại: **virtual threads** cho phép giữ code blocking (thread-per-request) mà JVM tự dùng cơ chế non-blocking bên dưới.
- Chi tiết code Java NIO, zero-copy, Netty: `Java.md` mục 15.

### 8.3. Các giới hạn hệ thống khi có nhiều kết nối
| Giới hạn | Triệu chứng | Kiểm tra / chỉnh |
| :--- | :--- | :--- |
| File descriptor / process | `Too many open files` | `ulimit -n` (mặc định 1024 trên nhiều distro!), `LimitNOFILE` trong systemd, `lsof -p <pid> \| wc -l` |
| Ephemeral port (phía client) | `Cannot assign requested address`, `BindException` | `net.ipv4.ip_local_port_range`, số TIME_WAIT (`ss -s`) → dùng connection pool |
| Accept backlog | Connect timeout lúc tải cao | `net.core.somaxconn`, `backlog` của server, `ss -lnt` (cột Recv-Q của socket LISTEN = queue đang chờ accept) |
| Conntrack table (NAT/firewall/K8s) | Gói bị drop ngẫu nhiên, `nf_conntrack: table full` trong dmesg | `net.netfilter.nf_conntrack_max` |
| Memory buffer | Throughput thấp trên đường dài | `net.core.rmem_max/wmem_max`, `tcp_rmem/wmem` |

---

## 9. Load Balancing, Proxy & CDN

> 🎯 **Bài toán:** Phân phối kết nối/request tới nhiều backend, tự loại backend hỏng, và đưa nội dung tới gần người dùng.
>
> ⚖️ **L4 vs L7:** L4 chỉ nhìn IP:port → cực nhanh, ít tốn tài nguyên, nhưng cân bằng **theo kết nối**; L7 hiểu HTTP → định tuyến theo path/header, retry, cân bằng **theo request**, nhưng tốn CPU (phải kết thúc TLS). ✅ Thường dùng cả hai: L4 ở biên để chịu tải lớn, L7 phía sau để định tuyến thông minh; với gRPC/HTTP/2 (kết nối dài) **bắt buộc** cân bằng L7 hoặc client-side.


> Phần cơ bản (L4 vs L7, thuật toán, forward vs reverse proxy, CDN push/pull) ở `SystemDesign.md` 1.3, 1.4, 2.2. Dưới đây là các chi tiết **networking** hay bị hỏi sâu.

- **L4 LB** chuyển tiếp kết nối TCP/UDP theo IP:port (NAT hoặc **DSR — Direct Server Return**: response đi thẳng từ backend về client, LB chỉ xử lý chiều vào → chịu tải cực lớn). LB L4 cân bằng **theo kết nối**, không theo request → với HTTP/2/gRPC (1 kết nối dài mang nhiều request) có thể dồn hết tải vào 1 backend → cần **L7 LB** hiểu HTTP/2 hoặc client-side LB.
- **L7 LB** kết thúc TCP/TLS ở phía client và **mở kết nối riêng** tới backend (2 kết nối riêng biệt) → định tuyến theo path/header, retry, nén, nhưng tốn CPU hơn; backend thấy IP của LB (cần `X-Forwarded-For` hoặc **PROXY protocol** để biết IP client thật).
- **Health check**: active (LB gọi `/health` định kỳ) vs passive (outlier detection — đánh dấu backend lỗi khi request thật thất bại). Health check nên rẻ và phản ánh khả năng phục vụ thật (readiness).
- **Connection draining / deregistration delay**: khi gỡ instance (deploy, scale-in), LB ngừng gửi request **mới** nhưng chờ các request đang chạy hoàn tất → kết hợp graceful shutdown (`SpringBoot.md` 13.5).
- **Sticky session**: theo cookie do LB cấp hoặc hash IP → phân bố không đều, mất phiên khi instance chết → ưu tiên stateless.
- **Timeouts phải khớp nhau theo chuỗi**: `client timeout > LB timeout > app/upstream timeout`, và **keep-alive idle timeout của backend > idle timeout của LB** (nếu backend đóng kết nối rảnh trước, LB có thể gửi request vào kết nối vừa bị đóng → **502**). Ví dụ AWS ALB idle timeout mặc định 60s → để keep-alive timeout của Tomcat/Node > 60s.
- **CDN** cũng là reverse proxy phân tán: TLS kết thúc ở edge gần user (giảm RTT handshake), giữ kết nối "ấm" tới origin, cache theo `Cache-Control`, chống DDoS.

---

## 10. Giao thức ứng dụng: WebSocket, SSE, gRPC

> 🎯 **Bài toán:** HTTP request–response chỉ cho client hỏi; nhiều ứng dụng cần **server chủ động đẩy** (chat, thông báo, giá chứng khoán) hoặc gọi RPC giữa service **hiệu quả** hơn JSON/HTTP 1.1.
>
> ⚖️ **Lựa chọn:** Polling (đơn giản, lãng phí) → long polling → **SSE** (server đẩy một chiều qua HTTP thường, tự kết nối lại) → **WebSocket** (hai chiều, kết nối lâu, cần LB hỗ trợ) → **gRPC** (HTTP/2 + Protobuf, streaming hai chiều, hợp đồng chặt).
>
> ✅ **Cách chọn:** chỉ cần server đẩy → SSE (đơn giản nhất, đi qua proxy dễ); cần hai chiều tần suất cao → WebSocket; giao tiếp service nội bộ → gRPC.


### 10.1. WebSocket
```http
GET /chat HTTP/1.1
Host: example.com
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
Sec-WebSocket-Version: 13

HTTP/1.1 101 Switching Protocols
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=      ← base64(SHA-1(key + GUID cố định)) — chứng minh server hiểu WebSocket
```
- Sau handshake: kết nối TCP full-duplex trao đổi **frame** (text, binary, ping/pong, close), header frame chỉ 2–14 byte; frame từ client phải được **mask**.
- Thực tế triển khai: LB/proxy phải hỗ trợ Upgrade & **idle timeout đủ dài** (gửi ping định kỳ < idle timeout), sticky hoặc pub/sub giữa các server (Redis/Kafka) để gửi message tới user đang kết nối ở server khác (`SystemDesign.md` Case 3), xác thực khi handshake (token trong query/cookie), giới hạn số kết nối/instance, reconnect với backoff phía client.
- **SSE** (`text/event-stream`): 1 chiều server → client trên HTTP thường, tự reconnect (`Last-Event-ID`), đi qua proxy dễ hơn WebSocket — phù hợp notification, streaming câu trả lời LLM.

### 10.2. gRPC
- Chạy trên **HTTP/2**: mỗi lời gọi RPC là 1 **stream**; message mã hóa **Protobuf** (binary, có schema `.proto`, sinh code nhiều ngôn ngữ); hỗ trợ unary, server streaming, client streaming, bidirectional.
- **Deadline**: client đặt hạn chót, tự truyền xuống các service phía sau (`grpc-timeout` header) → hủy công việc thừa khi caller đã bỏ cuộc.
- Status code riêng (`OK`, `UNAVAILABLE` — thường retry được, `DEADLINE_EXCEEDED`, `NOT_FOUND`...).
- ⚠️ Load balancing: kết nối HTTP/2 dài + multiplexing → L4 LB không chia đều → dùng L7 proxy (Envoy), service mesh, hoặc client-side LB (xem 9).
- Trình duyệt không gọi gRPC trực tiếp được (không kiểm soát được HTTP/2 framing/trailers) → **gRPC-Web** qua proxy, hoặc dùng REST/JSON cho public API (gRPC cho nội bộ).

---

## 11. Networking trên Cloud & Kubernetes

> 🎯 **Bài toán:** Hàng nghìn pod sinh ra và chết đi liên tục với IP thay đổi; các team cần mạng riêng tư, cô lập, nhưng vẫn ra được Internet và gọi nhau theo tên ổn định.
>
> 💡 **Ý tưởng:** **VPC** cung cấp mạng ảo riêng (subnet public/private, route table, NAT gateway, security group). Kubernetes thêm lớp trừu tượng: mỗi pod một IP phẳng (CNI), **Service** cho một IP/tên ổn định trước nhóm pod thay đổi (kube-proxy/eBPF chuyển tiếp), **CoreDNS** phân giải tên Service, **Ingress/Gateway** cho traffic từ ngoài vào.


### 11.1. VPC trên Cloud (AWS làm ví dụ)
```
VPC 10.0.0.0/16
├── Public subnet 10.0.1.0/24  (route 0.0.0.0/0 → Internet Gateway)   : ALB, NAT Gateway, bastion
├── Private subnet 10.0.11.0/24 (route 0.0.0.0/0 → NAT Gateway)       : App servers / K8s nodes (gọi ra Internet được, Internet không gọi vào được)
└── Private subnet 10.0.21.0/24 (không route ra Internet)             : Database (chỉ app subnet truy cập)
   (mỗi tầng nhân bản qua ≥ 2 Availability Zone)
```
- **Security Group**: firewall **stateful** gắn với instance/ENI, chỉ có rule allow (traffic trả về tự được phép); có thể tham chiếu SG khác ("DB chỉ nhận 5432 từ SG của app").
- **Network ACL**: firewall **stateless** ở mức subnet, có allow/deny, phải mở cả chiều về (ephemeral ports).
- **VPC Peering / Transit Gateway / PrivateLink**: kết nối VPC với nhau hoặc gọi dịch vụ của bên khác qua mạng riêng; **VPC Endpoint** để gọi S3/DynamoDB mà không đi qua NAT (tiết kiệm chi phí NAT data processing).
- Chi phí mạng là chi phí ẩn lớn: traffic **liên AZ** và đi qua **NAT Gateway** đều tính tiền theo GB.

### 11.2. Mô hình mạng Kubernetes
- **Mỗi Pod có 1 IP riêng**, mọi pod gọi được nhau trực tiếp không qua NAT (do **CNI plugin** cài đặt: Calico, Cilium, Flannel, AWS VPC CNI...). Các container trong cùng pod dùng chung network namespace (gọi nhau qua `localhost`).
- Pod IP **thay đổi** khi pod khởi động lại → dùng **Service** (IP ảo ổn định + DNS name):
    | Loại Service | Truy cập từ | Cơ chế |
    | :--- | :--- | :--- |
    | **ClusterIP** (mặc định) | Trong cluster | IP ảo; **kube-proxy** lập trình iptables/IPVS (hoặc eBPF với Cilium) để DNAT tới 1 pod ngẫu nhiên |
    | **NodePort** | Ngoài cluster qua `<NodeIP>:30000–32767` | Mở port trên mọi node |
    | **LoadBalancer** | Internet / mạng ngoài | Cloud tạo LB (L4) trỏ vào node/pod |
    | **Headless** (`clusterIP: None`) | Trong cluster | DNS trả thẳng danh sách pod IP — dùng cho StatefulSet (Kafka, Cassandra), client-side LB |
    | **ExternalName** | — | CNAME tới tên ngoài |
- **DNS**: `<service>.<namespace>.svc.cluster.local` do **CoreDNS** phân giải (lưu ý `ndots` — 5.3).
- **Ingress / Gateway API**: L7 routing HTTP(S) theo host/path vào các Service (NGINX Ingress, Traefik, Envoy Gateway, cloud ALB). Gateway API là chuẩn kế nhiệm Ingress.
- **NetworkPolicy**: firewall giữa các pod (mặc định mọi pod gọi được nhau — nên áp **default deny** rồi mở dần).
- ⚠️ Lưu ý: ClusterIP + kube-proxy cân bằng **theo kết nối** → client giữ kết nối dài (gRPC, HTTP/2, connection pool) sẽ "dính" vào vài pod; pod mới scale lên không nhận tải → dùng headless + client-side LB hoặc service mesh.
- **Service mesh** (Istio, Linkerd): sidecar/ambient proxy xử lý mTLS, retry, timeout, traffic split — `SystemDesign.md` 5.4.

---

## 12. Network Security

> 🎯 **Bài toán:** Kẻ tấn công có thể làm ngập hệ thống (DDoS), nghe lén/giả mạo, hoặc lợi dụng server của mình để tấn công vào mạng nội bộ (SSRF).
>
> 💡 **Ý tưởng:** Phòng thủ **nhiều lớp** từ biên vào trong, và **không tin vào vị trí mạng** (Zero Trust) — "ở trong VPC" không có nghĩa là đáng tin; mọi kết nối được xác thực và mã hóa.


- **DDoS**:
    - **L3/L4** (volumetric: UDP flood, amplification qua DNS/NTP/memcached; **SYN flood**) → chặn ở biên bởi nhà cung cấp/scrubbing center, Anycast phân tán tải, SYN cookies.
    - **L7** (HTTP flood giống người dùng thật, nhắm vào endpoint đắt như search/login) → WAF, rate limiting, CAPTCHA/bot detection, cache, load shedding.
- **Firewall**: stateless (packet filter theo IP/port) vs stateful (theo dõi kết nối); **WAF** hiểu HTTP (chặn SQLi/XSS pattern).
- **Zero Trust**: không tin mạng nội bộ chỉ vì "ở trong VPC" → xác thực & mã hóa mọi kết nối (mTLS), cấp quyền tối thiểu, kiểm tra theo danh tính thay vì IP.
- **Man-in-the-middle**: TLS + kiểm tra certificate đúng cách; đừng bao giờ tắt verify; HSTS chống downgrade.
- **SSRF** — server bị lừa gọi tới địa chỉ nội bộ (`169.254.169.254` metadata, `localhost:admin`) → allowlist đích, chặn dải private/link-local sau khi **resolve DNS** (chống DNS rebinding), IMDSv2 trên AWS. Xem `SystemDesign.md` 10.2.
- **VPN / Bastion / SSM Session Manager** để truy cập hạ tầng nội bộ; không mở cổng DB/SSH ra Internet.

---

## 13. Networking trong Java & các lỗi thường gặp

> 🎯 **Bài toán:** Mạng **chắc chắn** sẽ chậm hoặc lỗi; ứng dụng phải (1) không chờ vô hạn, (2) đọc được exception để biết lỗi nằm ở đâu (DNS? kết nối? đọc?), (3) tái sử dụng kết nối mà không dùng nhầm kết nối đã chết.
>
> 💡 **Ý tưởng:** Đặt **đủ các loại timeout** (lấy kết nối từ pool, connect, read/response, tổng thể), dùng **connection pool** có kiểm soát (giới hạn, thời gian sống, loại bỏ kết nối rảnh trước khi server/LB đóng), và **map từng exception về tầng mạng tương ứng** để chẩn đoán nhanh.


### 13.1. Phân biệt các exception
| Exception / thông báo | Nghĩa ở tầng mạng | Nguyên nhân thường gặp |
| :--- | :--- | :--- |
| `ConnectException: Connection refused` | Đã tới máy đích nhưng nhận **RST** — không có process lắng nghe port đó | Service chưa chạy/đang restart, sai port, bind `127.0.0.1` |
| `SocketTimeoutException: Connect timed out` | Gửi SYN không nhận được gì | Firewall/SG **drop** gói (không reject), sai IP/route, server quá tải (accept queue đầy), host chết |
| `SocketTimeoutException: Read timed out` | Đã kết nối nhưng chờ dữ liệu quá **read timeout** | Server xử lý chậm (query nặng, GC pause, deadlock), downstream của server chậm |
| `SocketException: Connection reset` / `Connection reset by peer` | Nhận **RST** giữa chừng | Bên kia crash/đóng đột ngột, LB/NAT cắt kết nối rảnh, dùng lại kết nối keep-alive đã bị server đóng |
| `NoHttpResponseException` (Apache HttpClient), `Unexpected end of stream` (OkHttp) | Kết nối bị đóng mà không có response | Thường do **stale keep-alive connection** (13.3) |
| `UnknownHostException` | DNS không phân giải được | Sai tên, DNS lỗi, `ndots`/search domain trong K8s |
| `SSLHandshakeException`, `PKIX path building failed` | Lỗi TLS | Xem 7.4 |
| `BindException: Address already in use` | Port đã có process khác giữ | Chạy 2 instance, process cũ chưa tắt |
| `BindException: Cannot assign requested address` | Hết ephemeral port / IP không thuộc máy | Quá nhiều kết nối ngắn (TIME_WAIT) — 3.3 |

### 13.2. Timeout — phải đặt đủ 3–4 loại
| Timeout | Ý nghĩa | Gợi ý |
| :--- | :--- | :--- |
| **Connect timeout** | Thời gian tối đa để hoàn tất TCP (+TLS) handshake | Ngắn: 1–3s (mạng nội bộ ms là đủ) |
| **Read / socket timeout** | Thời gian tối đa **giữa 2 lần nhận dữ liệu** (không phải tổng thời gian request!) | Theo SLA downstream |
| **Connection request timeout** (lấy kết nối từ pool) | Chờ pool có kết nối rảnh | Ngắn để fail-fast khi pool cạn |
| **Total / call timeout** (deadline) | Tổng thời gian cả request (kể cả retry) | Bắt buộc với streaming chậm "nhỏ giọt" (read timeout không bao giờ kích hoạt) |
- Mặc định nguy hiểm: `HttpURLConnection`/`RestTemplate` mặc định **timeout = 0 (vô hạn)**; JDBC driver thường không có socket timeout mặc định → luôn cấu hình tường minh (`SpringBoot.md` 16.2).

### 13.3. Bẫy kinh điển: Stale keep-alive connection
```
t=0    Client lấy kết nối từ pool, gửi request, trả kết nối về pool (vẫn mở)
t=60s  Server/LB đóng kết nối rảnh (idle timeout 60s) → gửi FIN (hoặc NAT/firewall âm thầm quên kết nối)
t=61s  Client lấy đúng kết nối đó từ pool, gửi request mới → nhận RST / EOF
       → "Connection reset" / NoHttpResponseException / 502 ở LB — lỗi lác đác, khó tái hiện
```
- Khắc phục: **idle timeout của pool phía client < idle timeout của server/LB** (vd client 30s, server 60s, LB 65s); bật evict idle connections nền (Apache HttpClient `evictIdleConnections`, `validateAfterInactivity`); retry an toàn cho request idempotent khi gặp lỗi này; với DB pool: `max-lifetime` của HikariCP < `wait_timeout` của MySQL/timeout của proxy.

### 13.4. HTTP client & connection pool trong Java
- `java.net.http.HttpClient` (Java 11+): HTTP/1.1 & HTTP/2 (HTTP/3 từ JDK 26), async, có `connectTimeout`; timeout mỗi request qua `HttpRequest.Builder.timeout()`.
- **Apache HttpClient**: pool cấu hình chi tiết qua `maxTotal` và `defaultMaxPerRoute` — mặc định rất nhỏ (**4.x: 2 kết nối/host, 20 tổng; 5.x: 5 kết nối/host, 25 tổng**) → nghẽn ngầm khi gọi 1 service với tải cao (thread chờ lấy kết nối, trông như downstream chậm).
- **OkHttp**: pool mặc định 5 kết nối rảnh, keep-alive 5 phút.
- **Netty/Reactor Netty** (WebClient): pool theo host, `maxIdleTime`, `maxLifeTime`, `pendingAcquireTimeout`.
- Tạo client **1 lần và tái sử dụng** (singleton bean); tạo client mới cho mỗi request = mất pool + handshake mỗi lần + rò rỉ thread/kết nối.

---

## 14. Troubleshooting Toolkit & quy trình chẩn đoán

> 🎯 **Bài toán:** "Service A gọi B bị lỗi/chậm" — nguyên nhân có thể ở DNS, mạng, TLS, LB, B quá tải, hoặc chính A cạn pool.
>
> 💡 **Ý tưởng:** Đi **từ dưới lên theo từng tầng**, mỗi bước loại trừ một nhóm nguyên nhân: tên có phân giải được không (DNS) → có tới được IP:port không (TCP) → handshake TLS có thành công không → HTTP trả gì và mất bao lâu ở từng giai đoạn (`curl -w`) → phía ứng dụng có đang chờ pool không (metrics). Đo trước, đoán sau.


### 14.1. Công cụ
```bash
# DNS
dig api.example.com +short            # IP
dig api.example.com +trace            # đi từng bước root → TLD → authoritative
nslookup api.example.com

# Kết nối được tới port không?
nc -vz db.internal 5432               # hoặc: telnet host port
curl -v https://api.example.com/health        # xem DNS, TCP connect, TLS handshake, request/response header
curl -o /dev/null -s -w 'dns=%{time_namelookup} tcp=%{time_connect} tls=%{time_appconnect} ttfb=%{time_starttransfer} total=%{time_total}\n' https://api.example.com
                                      # ← tách thời gian từng giai đoạn: chậm ở DNS, kết nối, TLS hay server xử lý?

# TLS
openssl s_client -connect api.example.com:443 -servername api.example.com -showcerts

# Đường đi & mất gói
ping host; traceroute host; mtr host  # mtr = traceroute + ping liên tục, thấy hop nào mất gói

# Trạng thái socket trên máy
ss -s                                 # tổng hợp: bao nhiêu ESTAB, TIME_WAIT...
ss -tanp state close-wait             # ai đang giữ CLOSE_WAIT (bug không close)
ss -lnt                               # socket LISTEN; Recv-Q > 0 = accept queue đang dồn
ss -tnp dst :5432 | wc -l             # bao nhiêu kết nối tới PostgreSQL
lsof -p <pid> | wc -l                 # số fd đang mở

# Bắt gói
tcpdump -i any -nn port 443 -w cap.pcap   # rồi mở bằng Wireshark
tcpdump -i any 'tcp[tcpflags] & (tcp-rst) != 0'   # chỉ xem gói RST
```
- Trong K8s: `kubectl exec` vào pod có sẵn công cụ, hoặc dùng **ephemeral debug container** (`kubectl debug -it pod/x --image=nicolaka/netshoot`).

### 14.2. Quy trình chẩn đoán "service A gọi service B bị lỗi/chậm"
1. **Lỗi gì chính xác?** refused / connect timeout / read timeout / reset / 502 / 504 / TLS — mỗi loại chỉ về một tầng khác nhau (13.1).
2. **DNS** phân giải đúng IP chưa (từ chính máy/pod A)? Có bị cache IP cũ không?
3. **Kết nối TCP** tới IP:port được không (`nc -vz`)? Timeout → firewall/SG/NetworkPolicy/route; refused → B không lắng nghe.
4. **TLS** handshake ổn không (`openssl s_client`, `curl -v`)?
5. **Thời gian từng giai đoạn** (`curl -w`): nếu `ttfb` lớn → B xử lý chậm → xem metric/trace của B.
6. **Lác đác, không tái hiện được** → nghi stale connection, timeout không khớp giữa các tầng (13.3), conntrack/port exhaustion, MTU, DNS lúc chập chờn.
7. **Chỉ chậm khi tải cao** → pool cạn (client hoặc DB), accept queue đầy, thread pool của B đầy, rate limit.
8. Bắt gói (`tcpdump`) khi cần bằng chứng cuối cùng: ai gửi RST, có retransmission không, handshake dừng ở bước nào.

---

## 15. Câu hỏi phỏng vấn Networking

**Q1. Điều gì xảy ra khi gõ `https://example.com` vào trình duyệt?**
Xem chi tiết `SystemDesign.md` 7.5: kiểm tra HSTS/cache → DNS (browser → OS → resolver → root → TLD → authoritative) → TCP 3-way handshake (hoặc QUIC) → TLS handshake (SNI, ALPN, certificate) → HTTP request → CDN/LB/reverse proxy → app server → DB/cache → response (status, cache headers, cookie) → parse HTML, tải tài nguyên phụ trên kết nối tái sử dụng → render.

**Q2. TCP vs UDP? Vì sao DNS dùng UDP? Vì sao HTTP/3 lại dùng UDP?**
Bảng ở mục 4. DNS: truy vấn/đáp nhỏ vừa 1 gói, cần nhanh, không cần chi phí handshake; nếu mất gói thì client tự hỏi lại. HTTP/3: dùng UDP làm "lớp vận chuyển trơn" để tự cài đặt QUIC ở user space — tránh head-of-line blocking của TCP, handshake nhanh hơn, connection migration, và triển khai/cập nhật được mà không phải đợi kernel/middlebox.

**Q3. Vì sao bắt tay 3 bước nhưng đóng kết nối 4 bước?**
3.2 & 3.3: thiết lập cần trao đổi & xác nhận ISN 2 chiều (gộp được SYN+ACK của server); đóng thì mỗi chiều đóng độc lập và bên nhận FIN có thể còn dữ liệu cần gửi nên ACK và FIN thường tách riêng.

**Q4. Server có rất nhiều TIME_WAIT / CLOSE_WAIT, có sao không, xử lý thế nào?**
TIME_WAIT là bình thường ở bên chủ động đóng; nhiều quá ở phía client → cạn port → dùng keep-alive/pool. CLOSE_WAIT nhiều là **bug** của chính ứng dụng (không close socket/response) → tìm chỗ leak (3.3).

**Q5. Flow control khác congestion control thế nào?**
3.5: flow control bảo vệ bên nhận (rwnd), congestion control bảo vệ mạng (cwnd, slow start, AIMD, CUBIC/BBR).

**Q6. HTTP/1.1, HTTP/2, HTTP/3 khác nhau ra sao?**
6.3 và `SystemDesign.md` 7.3: keep-alive & nhiều kết nối song song → multiplexing nhiều stream trên 1 TCP + header compression → QUIC trên UDP loại bỏ HOL blocking tầng transport, handshake nhanh, connection migration.

**Q7. HTTPS hoạt động thế nào? Tại sao không mã hóa toàn bộ bằng RSA?**
7.1–7.3: bất đối xứng (ECDHE) để thỏa thuận session key + certificate để xác thực server; dữ liệu mã hóa bằng thuật toán đối xứng (AES-GCM) vì nhanh hơn hàng trăm–hàng nghìn lần. Forward secrecy nhờ khóa ECDHE tạm thời.

**Q8. Connection refused vs connection timeout vs connection reset?**
13.1: refused = có máy nhưng không có ai lắng nghe (RST ngay); timeout = không nhận được phản hồi (firewall drop, sai route, quá tải); reset = kết nối đang có bị bên kia/thiết bị trung gian cắt đột ngột.

**Q9. Một request thỉnh thoảng lỗi "Connection reset" / 502 mà không tái hiện được, bạn nghi ngờ gì?**
Stale keep-alive connection do idle timeout không khớp giữa client pool, LB và server (13.3, 9); NAT/firewall timeout với kết nối dài; deploy không có connection draining/graceful shutdown.

**Q10. Đổi IP của database/endpoint nhưng ứng dụng Java vẫn gọi IP cũ?**
DNS cache ở nhiều tầng, đặc biệt **JVM DNS cache** và kết nối đã mở sẵn trong pool vẫn trỏ IP cũ (5.3) → đặt `networkaddress.cache.ttl`, cấu hình `max-lifetime` cho pool để kết nối được làm mới.

**Q11. L4 vs L7 load balancer? Vì sao gRPC cần L7?**
Mục 9 và `SystemDesign.md` 1.3: L4 theo kết nối (nhanh, không hiểu HTTP), L7 theo request (routing theo nội dung, retry, TLS termination). gRPC dùng HTTP/2 kết nối dài mang nhiều request → L4 không chia đều tải.

**Q12. CORS là gì? Vì sao Postman gọi được mà trình duyệt bị chặn?**
6.5: CORS là cơ chế trình duyệt nới lỏng Same-Origin Policy dựa trên header của server; Postman/curl không thực thi SOP nên không bị ảnh hưởng.

**Q13. Trong Kubernetes, pod A gọi `http://order-service` thì gói tin đi thế nào?**
CoreDNS phân giải `order-service.<ns>.svc.cluster.local` → ClusterIP → iptables/IPVS/eBPF rule do kube-proxy (hoặc Cilium) cài trên node DNAT sang IP của 1 pod backend → CNI định tuyến tới pod đó (có thể ở node khác) → response quay về theo conntrack (11.2).

**Q14. Làm sao chẩn đoán một API chậm là do mạng hay do server?**
`curl -w` tách DNS / connect / TLS / TTFB (14.1); so sánh latency đo ở client với thời gian xử lý ở server (metric/trace/log access); mtr để xem mất gói; tcpdump xem retransmission.

**Q15. Tại sao nên tái sử dụng kết nối (connection pooling)?**
Tránh chi phí handshake TCP (1 RTT) + TLS (1–2 RTT) mỗi request, tránh slow start (kết nối "ấm" có cwnd lớn), tránh TIME_WAIT & cạn ephemeral port, giảm CPU cho mã hóa bất đối xứng.
