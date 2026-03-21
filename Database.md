### 6. Column-Oriented Storage

#### 6.1 Các dạng Storage

- **NSM (N-ary Storage Model)**: Lưu trữ tất cả attribute của 1 tuple liên tiếp trong cùng 1 page
- **DSM (Decomposition Storage Model)**: Lưu trữ từng attribute riêng biệt, sử dụng variable-length encoding

**Đặc điểm DSM**:
- Không sử dụng id trong tuple → tránh tốn thêm dung lượng → giảm I/O
- Data đồng nhất trong 1 page → **đọc nhanh**, nhưng **write chậm hơn**
- Null handling: Sử dụng **bitmap** để đánh dấu null data
- String storage: Thay vì dùng padding cho variable-length encoding → sử dụng **dictionary compression** (32 bit integer)

**Hạn chế**: Hầu hết query cần nhiều hơn 1 column → vẫn cần các attribute gần nhau → **PAX Storage Model**

#### 6.2 PAX Storage Model

- Nhóm các partition data vào 1 **row group**
- Trong mỗi row group, attribute lưu theo các **column chunk**
- Kết hợp ưu điểm của cả NSM (locality) và DSM (scan nhanh)

**Cấu tạo 1 PAX file**:
- **Row Group**
    - Row group metadata: Chứa thông tin chunk (offset, size, ...)
    - Column chunk: Dữ liệu của từng column
- **Page Footer**: Chứa metadata tổng hợp của cả page
    - > ❓ Footer chứa thông tin gì cụ thể? Tại sao cần footer?

#### 6.3 Compression

- **Mục tiêu**: Giảm I/O, đánh đổi bằng tăng CPU
- **Tiêu chuẩn compression**:
    - Tạo ra giá trị có **độ dài cố định** (fixed-length values)
    - **Trì hoãn decompression**: Tính toán trên dữ liệu đã nén bằng kỹ thuật **mod log** — ghi thay đổi vào log ở đầu file nén, khi đọc chỉ cần áp dụng log vào file nén
    - Không làm mất mát dữ liệu (lossless)
- **Các cấp độ nén**:
    - Nén theo block
    - Nén theo tuple
    - Nén theo attribute
    - Nén theo column
- **Patching**: Xử lý khi data vượt quá max size đã được nén

> ❓ Khi nào data bị nén? Có phải mọi data đều bị nén không?
> ❓ Data lưu thành page — nếu nén dạng sort thì lưu kiểu gì?

---

### 7. Database Hashtable

#### 7.1 Tổng quan

- **Tác dụng**: Join operations, internal metadata, index, ...
- **Design decisions**: Data organization, Concurrency
- Dù hashtable có O(1) lookup, nhưng cách implement ảnh hưởng lớn tới hiệu năng thực tế
- Phần lớn database sử dụng **xxHash** (của Facebook)

#### 7.2 Static Hashing Schema

- **Linear Probing**
    - Khi collision → tìm slot trống tiếp theo (tuần tự)
    - Khi search: duyệt từ vị trí hash cho tới khi tìm thấy key hoặc gặp slot trống
    - **Resize**: Khi `load factor = active keys / total slots` vượt ngưỡng → rehash toàn bộ

- **Non-unique Key Handling** (cùng key, khác value — ví dụ quan hệ 1-N):
    - **Separate linked list**: Mỗi key trỏ tới linked list các value
    - **Redundant keys**: Lưu trùng key với các value khác nhau


Dynamic hashing schema

- **Chained Hashing**
    - Giống cách Java HashMap hoạt động: mỗi bucket có pointer trỏ tới linked list (chain)
    - Khi collision → thêm phần tử vào chain của bucket đó
    - **Nhược điểm**: Chain quá dài → tìm kiếm O(n), tốn bộ nhớ cho pointer(java đã cải thiện lại bằng red black tree trong version 8)

- **Extendible Hashing**
    - Sử dụng **directory** (bảng tra cứu) nằm giữa hash value và bucket:
      `Hash(key) → lấy N bit đầu → tra directory → tìm bucket`
    - Directory có `2^(global depth)` entry, mỗi entry là pointer trỏ tới bucket
    - **Global depth**: Số bit dùng để tra directory (áp dụng toàn bộ)
    - **Local depth**: Số bit mà mỗi bucket thực sự dùng
    - Khi bucket đầy → tăng local depth → **chỉ tách bucket bị đầy**, không rehash toàn bộ
    - Nhiều entry trong directory có thể trỏ chung 1 bucket (khi local depth < global depth) dẫn đến việc cần sử dụng 1 directory để quản lý

- **Linear Hashing**
    - **Không cần directory**, tính trực tiếp bucket từ hash value
    - Dùng **split pointer** duyệt qua các bucket theo thứ tự tuần tự
    - Khi bất kỳ bucket nào overflow → tách bucket mà split pointer đang trỏ tới (không nhất thiết là bucket bị overflow)
    - Dùng **2 hàm hash**: hash cũ cho bucket chưa split, hash mới cho bucket đã split
    - Đơn giản hơn Extendible Hashing, không cần quản lý directory

> Mới chỉ đề cập đến cách hash, chưa đề cập đến cách quản lý bộ nhớ

#### 7.3 Extra: YogabyteDB
- Basing on postgress

### 7. B tree index

##### 7.1. Các loại b tree
B+ tree
   - Data chỉ lưu ở leaf node
   - Leaf node được nối với nhau tạo thành linked list
B link tree
   - Mỗi node có thêm con trỏ trỏ tới node anh em

##### 7.2. Các thao tác trên cây b tree
      - Các thao tác trên b tree như insert thường đi kèm với việc split lại node
      - Duplicate key sẽ được giải quyết bằng apppend record vào cuối
        - Trong innodb hay postgresql, do them id vào cuối nên với non-cluster index thực chất là 1 compound index với id
    - Nhiều db trì hoãn việc merge node khi half full → dẫn đến việc node có thể dưới 50% full -> Postgress gọi là nonbalance b tree

##### 7.3. Index

- Index với variale length key
    - Lưu trữ theo dạng bitmap
       - Header chứa thông tin
       - Tiếp theo là các slot array chứa thông tin độ dài và offset(cần check kĩ lại vùng data này xem thực sự lưu trữ gì)
       - Data được lưu từ cuối lên
       ```
       ┌──────────────────────────────────┐
       │         Page Header              │  ← Chứa: số slot, con trỏ free space, ...
       ├──────────────────────────────────┤
       │  Slot 1 │ Slot 2 │ Slot 3 │ ...  │  ← Slot array (mọc xuống ↓)
       ├──────────────────────────────────┤
       │                                  │
       │          Free Space              │  ← Vùng trống ở giữa
       │                                  │
       ├──────────────────────────────────┤
       │  ... │ Data 3 │ Data 2 │ Data 1  │  ← Actual data (mọc lên ↑)
       └──────────────────────────────────┘
       ```
       - Khi insert, delete thì data đc chỉnh sửa như nào? Đặc biệt là các page vật lý
        - Khi insert, thêm data vào nếu đủ
        - Slot array được sort chứ data thì không? Do đó khi cần chèn ta đơn giản là sắp xếp lại các slot


- Node size
    - Tốc độ disk càng nhanh thì kích thước node càng nhỏ(hdd < ssd < ram so ...)
    - Kích thước node lớn giúp ích trong việc search theo range nh
    - Các hệ thống enterprise như DB2 cho phép thay đổi kích thước page theo từng bàng, với cả index

- Intra node search
    - Linear
       - SIMD to compare
       Load: Nạp một khối dữ liệu từ bộ nhớ vào thanh ghi SIMD (thanh ghi rộng như SSE, AVX-2 hoặc AVX-512).

Compare: Thực thi một lệnh máy (ví dụ: _mm256_cmpeq_epi32 trong bộ lệnh AVX) để so sánh toàn bộ thanh ghi đó với một giá trị đích (target value).

Masking: Kết quả của lệnh so sánh không phải là true/false đơn lẻ, mà là một vector mặt nạ (mask). Ví dụ: Nếu phần tử thứ 1 và 3 khớp, các bit tương ứng trong mặt nạ sẽ được bật lên 1.
Tuy nhiên có vẻ kĩ thuật này không áp dụng được

Store/Process: Sử dụng mặt nạ này để trích xuất các dòng dữ liệu thỏa mãn hoặc đếm số lượng bản ghi (aggregation).
    - Binary search
    -  Interpolation: Ước tính vị trí

##### 7.3. Optimize

###### 7.3.1. Pointer Swizzling
- **Vấn đề**: Trong B+Tree, mỗi node lưu **page id** của node con → khi traverse phải tra **page directory** để tìm offset vật lý → tốn thời gian lookup mỗi lần duyệt cây
- **Giải pháp**: Khi page đã được load vào buffer pool, **thay page id bằng con trỏ bộ nhớ trực tiếp** (raw pointer) tới page đó → bỏ qua bước tra page directory
- **Cơ chế hoạt động**:
    1. Lần đầu truy cập node con → tra page directory bình thường, load page vào buffer pool
    2. Sau khi load xong → **ghi đè page id** bằng pointer tới vị trí trong buffer pool
    3. Các lần truy cập sau → dùng trực tiếp pointer, không cần tra directory
- **Khi nào unswizzle** (chuyển ngược lại page id):
    - Khi page bị **evict** khỏi buffer pool → phải quét tất cả node cha có pointer trỏ tới page đó và chuyển lại thành page id
    - Sử dụng **reference counter** hoặc **back-pointer** để biết ai đang trỏ tới page
- **Trade-off**: Tăng tốc traversal đáng kể nhưng phức tạp hóa quá trình eviction. Phù hợp khi working set nằm gọn trong bộ nhớ (ít eviction)

###### 7.3.2. Buffered Update (Lazy Propagation)
- **Vấn đề**: Mỗi lần insert/delete có thể gây **split/merge** node → tốn kém vì phải cập nhật nhiều node, ghi nhiều page xuống disk
- **Giải pháp**: Thay vì cập nhật trực tiếp vào leaf node, **ghi các thay đổi vào buffer** (modification log) gắn với mỗi internal node
- **Cơ chế hoạt động**:
    1. Khi insert/delete → ghi operation vào buffer của node gốc (hoặc node gần nhất)
    2. Buffer được flush xuống node con khi **buffer đầy** hoặc khi có **read query** cần dữ liệu chính xác
    3. Khi flush → áp dụng tất cả operations trong buffer vào node con → có thể trigger flush tiếp xuống leaf
- **Ưu điểm**:
    - Giảm số lần I/O cho write-heavy workload (batch nhiều update thành 1 lần ghi)
    - Giảm tần suất split/merge vì nhiều insert/delete có thể triệt tiêu nhau
- **Nhược điểm**:
    - Read chậm hơn vì phải apply pending updates trước khi trả kết quả
    - Tăng độ phức tạp cho crash recovery
- **Ứng dụng thực tế**: Bε-tree, Fractal Tree (TokuDB/PerconaFT) sử dụng kỹ thuật này

###### 7.3.3. Partition Index (Partial Index)
- **Ý tưởng**: Chỉ index **một tập con** của bảng thay vì toàn bộ bảng, dựa trên điều kiện WHERE
- **Ví dụ SQL**:
    ```sql
    -- Chỉ index các order chưa hoàn thành
    CREATE INDEX idx_pending ON orders(created_at)
    WHERE status = 'pending';
    
    -- Chỉ index user active
    CREATE INDEX idx_active_users ON users(email)
    WHERE is_active = true;
    ```
- **Ưu điểm**:
    - Index nhỏ hơn → ít tốn disk và bộ nhớ
    - Insert/Update nhanh hơn vì chỉ cập nhật index khi row thỏa điều kiện
    - Scan index nhanh hơn vì ít entry hơn
- **Hỗ trợ**: PostgreSQL, SQL Server (filtered index), SQLite. MySQL **không** hỗ trợ partial index

###### 7.3.4. Include Column (Covering Index)
- **Vấn đề**: Khi query cần các column không nằm trong index → phải quay lại bảng chính để lấy data (**bookmark lookup / table access by index rowid**)
- **Giải pháp**: **INCLUDE** thêm column vào leaf node của index mà **không** dùng chúng làm search key
- **Ví dụ SQL**:
    ```sql
    -- Index trên (department_id) nhưng INCLUDE thêm salary, name
    CREATE INDEX idx_dept ON employees(department_id)
    INCLUDE (salary, name);
    
    -- Query này sẽ chỉ cần đọc index, không cần truy cập bảng chính
    SELECT salary, name FROM employees WHERE department_id = 5;
    ```
- **So sánh với compound index**:
    - Compound index `(dept_id, salary, name)`: cả 3 column đều là search key → dùng được cho WHERE trên cả 3
    - Include column: chỉ `dept_id` là search key → `salary`, `name` chỉ được **lưu kèm** ở leaf → index nhỏ gọn hơn, cập nhật nhanh hơn
- **Ưu điểm**: Biến index thành **covering index** → tránh random I/O quay lại bảng
- **Hỗ trợ**: SQL Server, PostgreSQL 11+. MySQL không có INCLUDE nhưng dùng compound index thay thế

###### 7.3.5. Prefix Compression
- **Quan sát**: Trong B+Tree, các key trong cùng 1 leaf node thường chia sẻ **prefix chung** (đặc biệt với string key đã được sort)
- **Cơ chế**:
    1. Lưu **prefix chung** 1 lần cho mỗi nhóm key
    2. Mỗi key chỉ lưu **phần suffix khác biệt**
    ```
    Trước nén:         Sau nén:
    "database_admin"   Prefix: "database_"
    "database_backup"  Suffixes: ["admin", "backup", "cache", "dump"]
    "database_cache"
    "database_dump"
    ```
- **Ưu điểm**:
    - Giảm đáng kể kích thước index (đặc biệt với string dài, prefix giống nhau)
    - Nhiều key hơn trên mỗi page → cây thấp hơn → ít I/O hơn
- **Nhược điểm**: Cần decompress khi so sánh key → tăng CPU
- **Ứng dụng**: InnoDB, PostgreSQL, SQLite đều sử dụng prefix compression

###### 7.3.6. Deduplication
- **Vấn đề**: Với non-unique index, cùng 1 key value có thể xuất hiện nhiều lần (ví dụ: index trên `status` column chỉ có vài giá trị distinct)
- **Giải pháp**: Lưu key value **1 lần** kèm theo **danh sách các pointer** (tuple id / row id) tới các row chứa key đó
    ```
    Trước dedup:                  Sau dedup:
    ("pending", row_1)            "pending" → [row_1, row_5, row_9]
    ("pending", row_5)            "completed" → [row_2, row_3]
    ("pending", row_9)
    ("completed", row_2)
    ("completed", row_3)
    ```
- **Ưu điểm**:
    - Giảm kích thước index khi cardinality thấp (ít giá trị distinct)
    - Ít entry hơn → ít split hơn → ít fragmentation
- **Hỗ trợ**: PostgreSQL 13+ hỗ trợ deduplication cho B-Tree index mặc định

###### 7.3.7. Suffix Truncation
- **Quan sát**: Trong B+Tree, **internal node** chỉ cần đủ thông tin để **định hướng search** xuống đúng child → không cần lưu toàn bộ key
- **Cơ chế**: Cắt bớt suffix của key trong internal node, chỉ giữ lại **phần ngắn nhất đủ để phân biệt** 2 child
    ```
    Leaf keys:   "database"  |  "dataflow"
    
    Trước truncation: internal key = "dataflow"  (full key)
    Sau truncation:   internal key = "dataf"     (đủ để phân biệt "datab..." vs "dataf...")
    ```
- **Ưu điểm**:
    - Internal node nhỏ hơn → fan-out lớn hơn → cây thấp hơn
    - Đặc biệt hiệu quả với string key dài
- **Lưu ý**: Chỉ áp dụng cho **internal node**, leaf node vẫn lưu full key
- **Ứng dụng**: PostgreSQL sử dụng suffix truncation từ version 12

###### 7.3.8. Bulk Insert (Bottom-Up Build)
- **Vấn đề**: Insert từng key vào B+Tree → mỗi lần insert phải traverse từ root → có thể gây nhiều lần split → rất chậm khi load lượng lớn dữ liệu
- **Giải pháp**: Xây cây từ dưới lên (**bottom-up**) thay vì insert từ trên xuống:
    1. **Sort** toàn bộ key cần insert
    2. Lần lượt **fill đầy** các leaf node (theo thứ tự sort)
    3. Khi leaf node đầy → tạo entry tương ứng trong internal node (parent)
    4. Lặp lại cho các tầng cao hơn cho tới root
- **Ưu điểm**:
    - Nhanh hơn rất nhiều so với insert từng key (O(N log N) cho sort vs O(N log²N) cho N lần insert)
    - Leaf node được fill **tuần tự** → tận dụng tối đa sequential I/O
    - Không xảy ra split trong quá trình build → ít write amplification
    - Index compact hơn (ít fragmentation)
- **Ứng dụng**: `CREATE INDEX` trong hầu hết database đều dùng bulk insert. PostgreSQL, MySQL/InnoDB, Oracle đều sort data trước rồi build B-Tree bottom-up


### 8. Filter, inverted index, vector index

#### 8.1. Filter

##### 8.1.1. Bloom filter
- **Ý tưởng**: Sử dụng **multiple hash functions** để đánh dấu các bit trong một **bit array**
- **Cơ chế**:
    1. Khởi tạo bit array toàn 0
    2. Khi insert element: hash element bằng k hash functions → set k bit tương ứng thành 1
    3. Khi query: hash element bằng k hash functions → check k bit có đều là 1 không
        - Nếu **có** → **có thể** element tồn tại (false positive)
        - Nếu **không** → **chắc chắn** element không tồn tại (true negative)
- **Ví dụ**:
    ```
    Bit array size: 10 (0-9)
    Hash functions: h1, h2
    
    Insert "apple":
    h1("apple") = 2 → set bit 2
    h2("apple") = 5 → set bit 5
    Array: [0, 0, 1, 0, 0, 1, 0, 0, 0, 0]
    
    Query "apple":
    h1("apple") = 2 (bit 2 = 1)
    h2("apple") = 5 (bit 5 = 1)
    → Có thể tồn tại
    
    Query "banana":
    h1("banana") = 3 (bit 3 = 0)
    → Chắc chắn không tồn tại
    ```
- **False positive rate**:
    - Phụ thuộc vào size của bit array (m) và số hash functions (k)
    - **Tối ưu khi `k ≈ (m/n) * ln(2)` (n = số element)**
    - **False positive rate ≈ `(0.6185)^m`**
    [] Tìm hiểu cách chywngs minh
- **Ưu điểm**:
    - Rất nhỏ gọn (chỉ lưu bit array)
    - Insert/Query rất nhanh (O(k))
    - Không cần disk I/O (chỉ dùng RAM)
- **Nhược điểm**:
    - **False positive** (không thể tránh hoàn toàn)
    - Không thể **delete** element (chỉ có **counting bloom filter** hỗ trợ)
    - Không lưu được value, chỉ lưu sự tồn tại
- **Ứng dụng**:
    - **Database**: Kiểm tra nhanh xem key có tồn tại trước khi query disk (Cassandra, HBase, RocksDB)
    - **Web browser**: Chặn URL độc hại
    - **Spell checker**: Kiểm tra từ có tồn tại không
    - **Distributed systems**: Kiểm tra membership

###### 8.1.1.1. Couting bloom filter
- **Ý tưởng**: Thay vì bit array, sử dụng **counter array** (mỗi phần tử là một counter)
- **Cơ chế**:
    - Insert: tăng counter tại các vị trí hash
    - Delete: giảm counter tại các vị trí hash
    - Query: check counter > 0
- **Ưu điểm**: Hỗ trợ **delete** element
- **Nhược điểm**: Tốn nhiều bộ nhớ hơn bloom filter thông thường


###### 8.1.1.2. Cuckoo Filter
- **Tên gọi**: Lấy cảm hứng từ **chim cúc cu** (cuckoo bird) - loài chim đẩy trứng khác ra khỏi tổ để chiếm chỗ
- **Ý tưởng**: Lưu **fingerprint** (bản tóm tắt nhỏ của hash) vào **bucket array**, mỗi bucket chứa nhiều slot (thường 4)
- **Cơ chế**:
    1. Tính `fingerprint f = hash(element)` (ví dụ 8-bit hoặc 16-bit)
    2. Tính 2 vị trí bucket khả dụng:
        - `i1 = hash(element)`
        - `i2 = i1 ^ hash(fingerprint)` (phép XOR cho phép tính ngược i1 từ i2)
    3. **Insert**: Thử đặt fingerprint vào bucket `i1` hoặc `i2`
        - Nếu cả 2 đều đầy -> chọn ngẫu nhiên 1 entry đang có, **đá ra** (relocate) sang bucket thay thế của nó
        - Entry bị đá tiếp tục tìm bucket thay thế -> lặp lại (giống chim cúc cu đẩy trứng)
        - Nếu vượt quá **max kick** (thường ~500) -> coi như filter đầy -> cần resize
    4. **Query**: Check bucket `i1` và `i2` có chứa fingerprint không
    5. **Delete**: Tìm và xóa fingerprint khỏi bucket `i1` hoặc `i2`
- **Ví dụ**:
    ```
    Bucket array (4 buckets, mỗi bucket 2 slot):
    
    Insert "apple":
    f = fingerprint("apple") = 0xA3
    i1 = hash("apple") % 4 = 1
    i2 = 1 ^ hash(0xA3) % 4 = 3
    
    Bucket[1]: [0xA3, _]    <- đặt vào đây
    Bucket[3]: [_, _]
    
    Delete "apple":
    Tìm 0xA3 trong Bucket[1] hoặc Bucket[3] -> xóa khỏi Bucket[1]
    ```
- **So sánh với Bloom Filter**:
    | | Bloom Filter | Cuckoo Filter |
    |---|---|---|
    | Delete | Không hỗ trợ | Hỗ trợ |
    | Space (cùng FP rate) | Tốt khi FP > 3% | **Tốt hơn khi FP < 3%** |
    | Lookup | k hash functions | 2 bucket lookups (cache-friendly) |
    | Insert worst-case | O(k) | O(max_kicks) - có thể chậm |
- **Ưu điểm**:
    - Hỗ trợ **delete** mà không cần counter (nhẹ hơn Counting Bloom Filter)
    - False positive rate thấp hơn Bloom Filter với cùng lượng bộ nhớ (khi target FP < 3%)
    - Lookup nhanh hơn - chỉ cần check 2 bucket (cache-friendly)
- **Nhược điểm**:
    - Insert có thể chậm khi filter gần đầy (nhiều kick)
    - Cần **load factor < ~95%** để hoạt động tốt
    - Không hỗ trợ insert trùng lặp quá nhiều (cùng element insert nhiều lần -> tràn bucket)
- **Ứng dụng**: Network packet filtering, database key lookup, memory-efficient caching

##### 8.1.3. Succinct Range Filter (SuRF)
- **Vấn đề**: Bloom Filter chỉ hỗ trợ **point query** ("key X có tồn tại không?"), **không** hỗ trợ **range query** ("có key nào trong khoảng [A, B] không?")
- **Ý tưởng**: Xây dựng filter từ **Fast Succinct Trie (FST)** - một trie nén cực kỳ nhỏ gọn, hỗ trợ cả point query và range query
- **Cấu trúc FST**: Chia trie thành 2 phần:
    1. **LOUDS-Dense** (cho các tầng trên - gần root): Dùng **bitmap** để encode, mỗi node dùng 256 bit
        - Tầng trên có ít node nhưng nhiều truy cập -> cần nhanh
    2. **LOUDS-Sparse** (cho các tầng dưới - gần leaf): Dùng **label + bit sequence** encode
        - Tầng dưới có rất nhiều node nhưng ít truy cập -> cần tiết kiệm bộ nhớ
- **Các biến thể SuRF**:
    | Biến thể | Lưu gì ở leaf | FP Rate | Space |
    |---|---|---|---|
    | **SuRF-Base** | Chỉ lưu key prefix (cắt tại tầng chuyển đổi) | Cao | Nhỏ nhất |
    | **SuRF-Hash** | Prefix + vài bit hash của full key | Thấp hơn (point query) | Trung bình |
    | **SuRF-Real** | Prefix + vài bit thực của suffix | Thấp hơn (range query) | Trung bình |
    | **SuRF-Mixed** | Prefix + hash bits + real bits | Tốt cho cả 2 | Lớn nhất |
- **Ví dụ range query**:
    ```
    Keys trong database: ["abc", "abd", "bcd", "bce", "xyz"]
    
    SuRF lưu trie nén của các key prefix
    
    Query: có key nào trong range ["ab", "bd"] không?
    -> Traverse trie: tìm thấy nhánh "ab*" và "bc*" -> CÓ THỂ tồn tại -> trả true
    
    Query: có key nào trong range ["ca", "cz"] không?
    -> Traverse trie: không tìm thấy nhánh "c*" -> CHẮC CHẮN không -> trả false
    ```
- **Ưu điểm**:
    - Hỗ trợ cả **point query** và **range query** (bloom filter chỉ hỗ trợ point)
    - Rất nhỏ gọn: ~10-14 bits per key (bloom filter ~10 bits per key cho 1% FP)
    - Có thể tune trade-off giữa space và false positive rate
- **Nhược điểm**:
    - **Static**: Phải rebuild khi data thay đổi (không hỗ trợ dynamic insert/delete)
    - False positive rate cho range query phụ thuộc vào kích thước range
    - Phức tạp hơn bloom filter đáng kể
- **Ứng dụng**: LSM-tree storage engine (RocksDB) - filter trước khi đọc SSTable, tránh disk I/O cho cả point và range query

**Các phần dưới đây không đề cập trong khóa học**
###### 8.1.1.4. Scalable Bloom Filter
- **Vấn đề**: Bloom filter thông thường cần biết trước **số lượng element (n)** khi khởi tạo. Nếu n vượt quá định mức -> false positive rate tăng vọt
- **Ý tưởng**: Sử dụng **chuỗi bloom filters** (slices), filter mới được thêm tự động khi filter hiện tại gần đầy
- **Cơ chế**:
    1. Bắt đầu với 1 bloom filter có kích thước `m0` và target FP rate `P0`
    2. Khi số element vượt ngưỡng -> tạo filter mới với:
        - FP rate **chặt hơn**: `P1 = P0 * r` (r nhỏ hơn 1, thường r = 0.5)
        - Kích thước lớn hơn tương ứng
    3. **Insert**: Luôn insert vào filter **mới nhất**
    4. **Query**: Check **tất cả** filters theo thứ tự -> trả true nếu bất kỳ filter nào trả true
- **FP rate tổng**: `P_total = 1 - product(1 - Pi)` - vẫn bị giới hạn nhờ cấp số nhân
- **Ưu điểm**: Không cần biết trước n, tự mở rộng khi cần thiết
- **Nhược điểm**:
    - Query chậm hơn khi có nhiều filters (phải check tuần tự)
    - Tốn bộ nhớ hơn 1 bloom filter có kích thước tối ưu ngay từ đầu (do overlap giữa các filter)
    - Không hỗ trợ delete

###### 8.1.1.5. Hybrid Bloom Filter
- **Vấn đề**: Trong hệ thống phân tán, mỗi node có bloom filter riêng -> query thường phải broadcast tới **tất cả node** để check
- **Ý tưởng**: Kết hợp **local bloom filter** (trên mỗi node) với **global summary** (aggregated) để giảm network traffic
- **Cơ chế**:
    1. Mỗi node duy trì **local bloom filter** cho data của mình
    2. Một **coordinator** giữ **compressed summary** (bằng log tập trung của các filters) của tất cả local filters
    3. Query đến coordinator trước:
        - Nếu summary trả false -> chắc chắn không có -> **không cần hỏi bất kỳ node nào**
        - Nếu summary trả true -> forward query tới các node tiềm năng
- **Ưu điểm**: Giảm đáng kể số lượng network roundtrips trong hệ thống phân tán
- **Nhược điểm**:
    - Cần đồng bộ liên tục summary khi data ở các node thay đổi
    - Summary tổng hợp có false positive rate cao hơn (do là union của nhiều filters)
- **Ứng dụng**: Distributed databases (Cassandra), CDN cache lookup, P2P networks



##### 8.1.2. Skip List
- **Ý tưởng**: Xây dựng dựa trên **sorted linked list** nhưng bổ sung thêm các "đường cao tốc" (express lanes) ở các tầng cao hơn để skip qua nhiều phần tử, giúp tăng tốc độ tìm kiếm từ O(N) xuống trung bình **O(log N)**.
- **Ví dụ cấu trúc**:
    ```text
    Level 2: 1 --------------------------> 12
    Level 1: 1 --------> 6 --------------> 12
    Level 0: 1 -> 3 -> 5 -> 6 -> 10 -> 12
    ```

- **Cơ chế tìm kiếm (Cách đi xuống từng tầng)**:
    - Bắt đầu từ Node đầu tiên (Head) ở **tầng cao nhất**.
    - Nhìn sang node tiếp theo (bên phải) trên **cùng tầng**:
        - Nếu node tiếp theo có giá trị **nhỏ hơn hoặc bằng** giá trị cần tìm -> Di chuyển sang node đó.
        - Nếu node tiếp theo có giá trị **lớn hơn** (hoặc là NULL/cuối danh sách) -> **Đi xuống 1 tầng** (drop down) ngay tại node hiện tại.
    - Lặp lại cho đến khi tìm thấy giá trị hoặc đi xuống quá Level 0.
    - _Ví dụ tìm số 5_:
        1. Ở Level 2, Head(1) nhìn sang phải là 12. 12 > 5 -> **Đi xuống** Level 1 tại node 1.
        2. Ở Level 1, node(1) nhìn sang phải là 6. 6 > 5 -> **Đi xuống** Level 0 tại node 1.
        3. Ở Level 0, node(1) nhìn sang phải là 3. 3 <= 5 -> **Đi sang** node 3.
        4. Ở Level 0, node(3) nhìn sang phải là 5. 5 <= 5 -> **Đi sang** node 5 (Tìm thấy!).

- **Cơ chế Insert**:
    1. Tìm vị trí cần insert ở Level 0 giống quá trình tìm kiếm ở trên (lưu lại đường đi nhánh bị cắt để biết cần nối pointer ở đâu nếu ngoi lên).
    2. Insert node mới vào Level 0 (như linked list bình thường).
    3. **Tung đồng xu (Coin Flip/Random)** để quyết định chiều cao của node mới:
        - Nếu ra sấp (1 hoặc True) -> Node này được ngoi lên 1 tầng (Level 1), thực hiện cập nhật con trỏ ở Level 1.
        - Tiếp tục tung đồng xu, nếu vẫn ra sấp -> Ngoi lên Level 2. Cứ tiếp tục cho đến khi ra ngửa (0) hoặc đạt giới hạn số tầng (Max Level).
    - _Hiệu ứng_: Khoảng 1/2 số node sẽ có độ cao 1, 1/4 số node có độ cao 2, 1/8 có độ cao 3, v.v., mô phỏng lại cấu trúc cây cân bằng một cách ngẫu nhiên.

- **Ưu điểm**:
    - **Không cần rebalancing phức tạp** (như khi xoay node của AVL hay B-Tree).
    - Cài đặt **concurrency/lock-free cực kì hiệu quả**, dễ scale cho multi-threading.
    - Insertion và Deletion rất nhanh (không cần khoá diện rộng).
- **Nhược điểm**:
    - **Kém Cache-friendly**: Các node cấp phát rải rác trong bộ nhớ (đặc trưng linked list) gây cache miss, không tốt bằng mảng liền khối của B-Tree.
    - Tốn bộ nhớ hơn (lưu nhiều pointer).
    - Phù hợp chủ yếu cho **In-memory Database** (như Redis Sorted Set, Memtable của RocksDB/LevelDB) hơn là Disk-based DB.

- [ ] Bài tập: Tự implement Skip List bằng Java (Có thể tham khảo `java.util.concurrent.ConcurrentSkipListMap`).

---

##### 8.1.3. Trie Index (Prefix Tree)
- **Ý tưởng**: Không lưu toàn bộ key ở một node mà **lưu trữ theo từng ký tự**. Độ sâu của cây phụ thuộc vào độ dài của key thay vì số lượng data.
- **Cấu trúc**: Mỗi đường đi từ root xuống một node (được đánh dấu là kết thúc `end`) đại diện cho 1 key.
- **Ví dụ**:
    ```text
    Keys: "cat", "car", "dog"
    
    root 
     ├── c ─ a ┬─ t (end)
     │         └─ r (end)
     └── d ─ o ─ g (end)
    ```

- **Ưu điểm**:
    - **Tìm kiếm/Insert prefix rất nhanh**: Độ phức tạp tối đa là O(K) với K là độ dài chuỗi key, không phụ thuộc vào số lượng N node trong cây.
    - Tự động sắp xếp data theo thứ tự từ điển -> Hỗ trợ tốt `Range Query` và **Tìm kiếm theo Prefix** (Auto-complete).
    - Tiết kiệm bộ nhớ nếu có rất nhiều key có **chung prefix** dài.
- **Nhược điểm**:
    - Nếu data không chia sẻ nhiều prefix -> **Tốn cực kỳ nhiều bộ nhớ** cho các node rời rạc.
    - Cấu trúc cây có xu hướng "dài và gầy", không phù hợp để duyệt (I/O reading) trên Disk. Do đó, thường biến thể thành **Radix Tree / Patricia Trie** (gộp các node liên tiếp chỉ có 1 child thành 1 node chung) để nén cấu trúc.

##### 8.1.4 Trie key span (Node Span / Radix size)
- **Định nghĩa**: **Span** là lượng byte/bit (hoặc ký tự) được phân tích và kiểm tra tại mỗi tầng của Trie. Thông số này quyết định **fan-out** (số lượng nhánh con tối đa) của mỗi node.
- **Ví dụ**:
    - **1-bit span**: Mỗi node check 1 bit $\rightarrow$ tối đa 2 nhánh con (0 hoặc 1). Cây sẽ **rất sâu** (ví dụ chuỗi 32-bit int cần duyệt 32 tầng), tốn thời gian traversal (vì nhảy pointer nhiều) nhưng lượng byte mỗi node nhỏ.
    - **8-bit span** (1 byte): Mỗi node check 1 ký tự ASCII (8 bit) $\rightarrow$ sinh ra tối đa 256 nhánh con. Cây sẽ **ngắn lại** (nhanh hơn vì ít bước duyệt) nhưng mỗi node phải cấp phát sẵn array gồm 256 pointers.
- **Sự đánh đổi**: Span càng lớn $\rightarrow$ cây càng lùn (tìm nhanh) nhưng node phình to (tốn RAM kinh khủng do phần lớn pointer trỏ tới NULL nếu key phân bố thưa).
- **Ứng dụng thực tế trong cơ sở dữ liệu**:
    - DB ít khi dùng Trie có span tĩnh. Các hệ thống hiện đại chuyên In-memory (như HyPer, DuckDB) sử dụng **ART (Adaptive Radix Tree)**.
    - ART linh động tăng/giảm kích thước node (Node4, Node16, Node48, Node256). Khi có ít node con $\rightarrow$ dùng span gộp nhỏ, nếu thêm nhiều dữ liệu $\rightarrow$ tự động vươn node to ra. Điều này giúp hệ thống truy xuất cực mạnh mà vẫn cực kỳ tối ưu RAM.

##### 8.1.5. Radix tree (Patricia Trie / Space-optimized Trie)
- **Định nghĩa**: Là một phiên bản nâng cấp của Trie, được thiết kế để nén cấu trúc. Cốt lõi của nó là **bất kỳ node nào chỉ có ĐÚNG 1 node con sẽ được gộp (merge) vào chung với node đó**.
- **Cơ chế**: Thay vì một cạnh/node chỉ đại diện cho 1 ký tự, cạnh trong Radix Tree đại diện cho nguyên một chuỗi (chuỗi ký tự / chuỗi bit).
- **Ví dụ so sánh**:
    ```text
    Với 2 key "rubic" và "ruby":
    
    [Trie thông thường - lãng phí node r, u, b]
    root -> r -> u -> b -> i -> c (end)
                        -> y (end)
    
    [Radix Tree - nén đuôi thẳng]
    root -> "rub" ┬─ "ic" (end)
                  └─ "y" (end)
    ```
- **Ưu điểm**:
    - Triệt tiêu tình trạng cây "dài và gầy" (những đoạn cây thẳng tắp không phân nhánh do chuỗi key dài, đơn điệu).
    - Mức độ **bảo lưu Cache (Cache-friendly)** tốt hơn rất nhiều so với Trie cơ bản vì giảm bớt số lượng lớn Object rời rạc và bước nhảy (pointer traversal).
    - Cực kỳ hữu dụng khi DB chứa những record có tiền tố chung rất lớn (như prefix domain, chuỗi uuid, URL).
- **Ứng dụng trong kiến trúc System/Database**:
    - **Redis**: Sử dụng Radix Tree nền tảng (cụ thể là `Rax` library) để chạy cơ chế dữ liệu `Redis Streams` và quản lý Redis Cluster routing.
    - **Relational Databases (PostgreSQL, InnoDB)**: Radix tree được sử dụng ẩn bên trong Engine (Ví dụ: Memory tracking, quản lý lock id, hay Indexing block dạng SP-GiST engine của PostgreSQL). 
    - **Khác**: Core cơ chế IP Routing của Linux Kernel cũng dùng Radix Tree vì IP subnet có tính chất nén Radix hoàn hảo.


#### 8.2. Inverted Index (Chỉ mục đảo ngược)
- **Ý tưởng cốt lõi**: Đi ngược lại với cách truyền thống (Document $\rightarrow$ Chứa Words), Inverted Index xây dựng một "từ điển" mapping từ **Words (Terms)** $\rightarrow$ **Danh sách các Document IDs** chứa từ đó (gọi là Postings List).
- **Thành phần chính**:
    1. **Term Dictionary (Từ điển)**: Lưu trữ tất cả các từ khóa (terms) có trong hệ thống và chỉ mục trỏ tới cụm document.
    2. **Postings List (Danh sách đối chiếu)**: Mảng các Document ID (và trật tự tần suất, vị trí xuất hiện) tương ứng với từng term.
- **Ứng dụng**: Là cấu trúc dữ liệu nền tảng cho mọi Search Engine ngày nay (Elasticsearch, Solr, Google Search).

##### 8.2.1. Apache Lucene (Elasticsearch) - Cách cài đặt Từ điển bằng FST
- **Vấn đề**: Term Dictionary của một Search Engine có thể chứa hàng tỷ từ. Nếu để dưới Disk thì mỗi lần search tốn I/O quá chậm, nếu đưa hết lên RAM bằng Array/Hashmap hay Tree/Trie thông thường thì ngốn vỡ bộ nhớ.
- **Giải pháp của Lucene**: Sử dụng **FST (Finite State Transducer)**.
    - Đây là một cấu trúc đồ thị trạng thái hữu hạn (Automaton). Khác với Trie chỉ chia sẻ được khoản "Tiền tố - Prefix", FST chia sẻ cả **hậu tố (Suffix)**, khiến cho đồ thị nén siêu nhỏ gọn (compression đỉnh cao).
    - FST không chỉ kiểm tra "từ này có tồn tại không" (gọi là FSA), mà nó còn **map một Input Sequence (khoá) thành một Output Sequence (Trọng số/ID)**.

- **Cơ chế tính Output (Term ID) bằng FST**:
    - Lucene gán một giá trị "Trọng lượng" (Weight / Output) vào các **cây cầu nối (edges/transitions)** của đồ thị.
    - Khi duyệt qua các ký tự của một từ cần tìm, ta **cộng dồn** các trọng số dọc đường. Tổng cuối cùng khi đến node Tạm dừng (End node) chính là **ID (Block pointer)** trỏ tới khu vực Postings List trên ổ cứng.
    - **Ví dụ phân tích** của bạn (giả sử Dictionary cần map Term $\rightarrow$ Block ID tăng dần):
        ```text
        Dictionary:
        ID=1: PAG
        ID=2: BNB
        ID=3: BTC
        ```
        - **Cách FST gán trọng số**: FST sẽ nhìn vào thứ tự từ điển (đã sort chữ cái) và phân bổ số dư output vào ngã rẽ đầu tiên để luồng duyệt có thể phân kì được đúng số.
        - Đồ thị diễn giải (đơn giản hoá):
        ```text
        [Node Gốc]
          ├── nhánh (P, weight: 1) ── qua A ── qua G (weight: 0)  -> Tổng = 1
          |
          └── nhánh (B, weight: 2) ── qua N ── qua B (weight: 0)  -> Tổng = 2
                                   └── nhánh (T, weight: 1) ── qua C -> Tổng = 2+1 = 3
        ```
    - **Ví dụ đi tìm kiếm cụ thể**:
        - Khách tìm "PAG": Đi vào ngã `P` (cộng 1), qua `A` (cộng 0), qua `G` (cộng 0) $\rightarrow$ ID = 1. Lọc lấy Doc số 1.
        - Khách tìm "BNB": Đi vào ngã `B` (hành trang = 2), qua `N` (+0), qua `B` (+0) $\rightarrow$ ID = 2.
        - Khách tìm "BTC": Đi vào ngã `B` (hành trang = 2), qua ngã rẽ `T` (có giá +1), qua `C` (+0) $\rightarrow$ ID = 2 + 1 = 3.
        
- **Sức mạnh cực lớn của FST trong Lucene**:
    - **Cực kì tiết kiệm RAM**: Nén toàn bộ metadata dictionary từ hàng chục GB text data nằm gọn lỏn vào một vài chục MB RAM mà không cần cắt cụt từ.
    - **Tốc độ tra cứu $O(K)$**: Thời gian chỉ phụ thuộc vào độ dài chữ $K$, cộng trừ vài phép toán bit siêu nhẹ.
    - Sẵn sàng hỗ trợ **Wildcard / Regex Search / Fuzzy match** (ví dụ search `B*C` hoặc tìm sai chính tả `BNC`) dễ dàng vì căn nguyên gốc của nó là Automaton Engine.(tính điểm)

- **Cơ chế Xử lý khi Insert (Tính Bất biến của Term Dictionary)**:
    - **Vấn đề nghịch lý**: FST tính toán phân chia trọng lượng (weight) rất tinh vi ở từng ngã rẽ. Nếu có một Term mới chèn vào giữa, đồ thị sẽ phải tính toán lại toàn bộ trọng số dọc đường để bảo toàn ID, gây sập hiệu năng.
    - **Giải pháp - Kiến trúc Segment**: **Apache Lucene KHÔNG BAO GIỜ chỉnh sửa cây FST cũ**. Nó sử dụng nguyên lý Không thay đổi (Immutable).
        1. **In-Memory Buffer**: Khi Insert, text mới nằm tạm trong RAM bằng cấu trúc thông thường (như Skip List).
        2. **Flush to Disk**: Khi đầy, Buffer xả xuống đĩa tạo thành một khối dữ liệu đóng băng gọi là **Segment** mới.
        3. **Build FST 1 Lần Duy Nhất**: Trước khi ghi xuống, danh sách từ vựng được Sắp xếp Alphabet (`Sort`). Thuật toán của Lucene bắt đầu Build cây FST từ dưới lên (Bottom-up). Vì đã có sẵn mảng đã Sort, FST biết chính xác phải đặt bao nhiêu trọng số lên ngã rẽ nào, làm 1 lần ăn ngay rồi cất vĩnh viễn vào Segment đó.
    - **Quy trình Tìm kiếm (Query)**: Bắn truy vấn Search tới toàn bộ FST của tất cả Segments đang có $\rightarrow$ Gom Postings List lại $\rightarrow$ Hợp nhất (Merge) $\rightarrow$ Trả kết quả.
    - **Gộp rác (Background Merge)**: Khi lượng Segment quá nhiều (đọc nhiều FST gây chậm), máy sẽ chạy ngầm tiến trình gộp nhiều Segment nhỏ thành 1 Segment bự, loại bỏ rác/doc đã xóa, và **Build lại 1 cây FST duy nhất** cho Segment lớn, sau đó xóa đống cây FST lẻ tẻ cũ.

##### 8.2.1. PostgreSQL GIN (B+ Tree) vs Lucene (FST)

###### A. Cách PostgreSQL GIN liên kết data bằng B+ Tree

- **Cấu trúc tổng quan**: PostgreSQL dùng **B+ Tree** làm Term Dictionary cho GIN (Generalized Inverted Index)

```text
┌─────────────────────────────────────────────────────────────┐
│                  B+ Tree (Term Dictionary)                  │
│                                                             │
│  Internal Nodes: [key1 | key2 | key3 | ...]                 │
│       ↓           ↓           ↓                             │
│  Leaf Nodes:  [term_A → ptr] [term_B → ptr] [term_C → ptr]  │
│                   │               │               │         │
└───────────────────┼───────────────┼───────────────┼─────────┘
                    ↓               ↓               ↓
          ┌─────────────┐  ┌──────────────┐  ┌───────────────┐
          │ Posting List│  │ Posting List │  │ Posting Tree  │
          │ (sorted arr)│  │ (sorted arr) │  │ (B+ Tree)     │
          │ [rid1, rid2]│  │ [rid5]       │  │ Hàng ngàn rids│
          └─────────────┘  └──────────────┘  └───────────────┘
```

- **Luồng tra cứu**:
    1. **Traverse B+ Tree**: Query `WHERE content @@ 'database'` → traverse từ root xuống leaf bằng binary search tại mỗi node
    2. **Tìm entry tại leaf**: Leaf node chứa entry cho term `"database"` → entry có **pointer** trỏ tới Posting List/Tree
    3. **Đọc Posting List**: Lấy danh sách `heap TIDs` (row IDs)
    4. **Truy cập heap table**: Dùng TIDs truy cập bảng chính lấy dữ liệu thực

- **2 cấu trúc Posting List tuỳ theo số lượng document**:

| Số lượng Document IDs | Cấu trúc | Lý do |
|---|---|---|
| **Ít** (fit trong 1 page) | **Sorted Array** | Compact, sequential scan nhanh, ít overhead |
| **Nhiều** (vượt 1 page) | **B+ Tree riêng** (Posting Tree) | Binary search trên tập lớn, insert/delete hiệu quả không cần rewrite toàn bộ |

- **Chuyển đổi Posting List → Posting Tree**:
    ```text
    Ban đầu: term "the" → Posting List: [1, 5, 9, 12]     (sorted array, 1 page)
                              ↓ (thêm nhiều document chứa "the")
    Sau đó:  term "the" → Posting Tree (B+ Tree):
                             [root: 50, 150]
                            /       |        \
                        [1..49]  [51..149]  [151..200]   ← leaf nodes = sorted doc IDs
    ```
    - Khi posting list quá dài → PostgreSQL tự động **promote** thành B+ Tree (Posting Tree)
    - Ngưỡng chuyển đổi dựa trên việc data có fit trong 1 page hay không

###### B. So sánh PostgreSQL GIN (B+ Tree) vs Lucene (FST)

**Kiến trúc Term Dictionary**:

| Tiêu chí | PostgreSQL GIN | Lucene |
|---|---|---|
| Cấu trúc Dictionary | **B+ Tree** trên disk | **FST (Finite State Transducer)** trên RAM |
| Chia sẻ dữ liệu | Chỉ chia sẻ **prefix** | Chia sẻ cả **prefix VÀ suffix** → nén cực mạnh |
| Bộ nhớ Dictionary | Tốn hơn (mỗi node = 1 page 8KB) | Cực nhỏ (GB data → vài chục MB FST) |
| Tốc độ lookup | O(log N) — traverse qua nhiều page | O(K) — K = độ dài term |

**Posting List**:

| Tiêu chí | PostgreSQL GIN | Lucene |
|---|---|---|
| Cấu trúc | Sorted Array hoặc B+ Tree | Sorted Array nén (delta + varint + block compression) |
| Nén | Không nén đặc biệt | Rất mạnh: Frame of Reference, PForDelta, roaring bitmap |
| Hỗ trợ update | ✅ Insert/Delete trực tiếp | ❌ Immutable — phải tạo segment mới |

**Write (Insert/Update/Delete)**:

| Tiêu chí | PostgreSQL GIN | Lucene |
|---|---|---|
| Insert | Trực tiếp vào B+ Tree (có pending list để batch) | Buffer RAM → flush thành **Segment mới** (immutable) |
| Delete | Xóa trực tiếp posting list/tree | Soft delete → gộp khi merge segment |
| Consistency | Full **ACID transaction** | **Near real-time** (cần refresh mới visible) |

**Ưu điểm PostgreSQL GIN**:
- ACID compliant: transaction, rollback được
- Mutable: Insert/Delete/Update trực tiếp, không cần background merge
- Tích hợp sẵn: query SQL bình thường
- Phù hợp write-heavy + mixed workload (OLTP + full-text search)

**Nhược điểm PostgreSQL GIN**:
- Tốn RAM hơn (B+ Tree không nén tốt bằng FST)
- Full-text search chậm hơn: O(log N) vs O(K), posting list không nén mạnh
- Không hỗ trợ fuzzy/wildcard tốt (B+ Tree chỉ match prefix, không có automaton engine)
- Scale hạn chế: single-node, không có distributed segment

**Ưu điểm Lucene**:
- Search cực nhanh: FST trên RAM, posting list nén tối ưu
- Cực tiết kiệm RAM cho dictionary (nén prefix + suffix)
- Hỗ trợ fuzzy, wildcard, regex nhờ bản chất automaton
- Horizontal scaling qua sharding segment (Elasticsearch)

**Nhược điểm Lucene**:
- Không ACID: near real-time, không rollback được
- Immutable segment: Delete/Update tốn kém (soft delete + rewrite)
- Background merge bắt buộc: tốn CPU/IO, có thể gây spike
- Write amplification: rebuild FST từ đầu khi merge segment

**Khi nào dùng gì?**:

| Tình huống | Nên dùng |
|---|---|
| ACID + full-text search đơn giản | PostgreSQL GIN |
| Search engine chuyên biệt, tỷ records, fuzzy | Lucene/Elasticsearch |
| Write-heavy + cần search | PostgreSQL GIN (ít overhead merge) |
| Read-heavy + search phức tạp + distributed | Lucene/Elasticsearch |
| Cần cả hai | PostgreSQL OLTP + Elasticsearch search (đồng bộ CDC) |


##### 8.2.2. Vector index (ANN - Approximate Nearest Neighbor)
Trong môi trường cơ sở dữ liệu vector, việc tìm kiếm chính xác tuyệt đối (gọi là **KNN - K-Nearest Neighbors**) đòi hỏi phải quét và tính khoảng cách với *toàn bộ* vector đang có. Độ phức tạp là $O(N \times D)$ (N số record, D số chiều), điều này là thảm hoạ hiệu năng khi DB có hàng triệu vector lớn. 

Do đó, hầu hết các hệ thống Vector DB sử dụng các thuật toán **ANN (Approximate Nearest Neighbor)** để đánh đổi một chút định luật "khoảng cách sát nhất" lấy tốc độ search cực nhanh $O(\log N)$ hoặc $O(1)$. Ở đây ta bỏ qua quá trình biến text/image thành các embedded vector, chỉ đề cập cấu trúc dữ liệu bên dưới.

###### 8.2.2.1. Inverted File Index (IVF)
- **Ý tưởng cốt lõi**: Chia không gian vector rộng lớn thành nhiều "hộp" nhỏ (clusters / cells). Inverted table sẽ chứa `Cluster ID -> [Posting list các danh sách vector thuộc hộp đó]`. Thay vì tìm cả thế giới, ta chỉ tìm trong 1 vài hộp gần ta nhất.
- **Phân chia cluster như nào?**
  - Hệ thống sử dụng thuật toán gom cụm máy học, chuyên biệt và phổ biến nhất là **K-means Clustering** trong quá trình build index.
  - DB sẽ tính toán và đánh dấu ra $K$ điểm làm "tâm" (Centroids) mang tính đại diện cho không gian dữ liệu đó.
- **Làm sao đảm bảo các vector trong cùng 1 cluster gần nhau?**
  - Cơ sở toán học của nó là **Voronoi Diagram (Biểu đồ Voronoi)**. Không gian n chiều được chia thành nhiều khối đa giác vây quanh $K$ tâm.
  - Mọi điểm vector rơi vào đa giác A đều được chứng minh bằng toán học là **có khoảng cách đến tâm A gần hơn bất kỳ tâm của các tế bào xung quanh nào khác**.
  - **Cơ chế Search (tham số $nprobe$)**: Đầu tiên, DB đo query vector với $K$ tâm. Chọn ra $nprobe$ tâm gần query nhất (ví dụ chỉ nhặt 10 vùng trên tổng 1000 vùng mốc) ->  truy cập Posting list của 10 vùng đó và vét cạn (brute-force) cục bộ để nhặt ra Top vector sát nhất. Tốc độ rất nhanh vì chỉ phải quét 1% lượng dữ liệu.

###### 8.2.2.2. Graph Index (HNSW - Hierarchical Navigable Small World)
**HNSW** là thuật toán tìm kiếm vector state-of-the-art đỉnh cao nhất hiện nay, là trái tim của Milvus, Qdrant, pgvector.
- **Ý tưởng thiết kế**: Phép lai ghép rực rỡ giữa **NSW (Navigable Small World)** (đồ thị điều hướng các nút mạng bạn bè lân cận, giống mạng lưới liên kết Facebook) và **Skip List** (cấu trúc nhảy vọt phân tầng).
- **Search các vector lân cận** và **Sử dụng ý tưởng của skip list để search theo từng tầng**:
  - Đồ thị HNSW không phẳng mà phân thành **nhiều tầng (layers)**.
  - **Tầng trên cùng**: Rất thưa thớt, khoảng cách giữa các node rất xa, đóng vai trò như các "trạm trung chuyển cao tốc" (Hubs).
  - **Tầng dưới cùng (Layer 0)**: Chứa toàn bộ 100% vector chằng chịt các ngã rẽ lân cận.
  - **Luồng đi (Routing)**: Khi vector truy vấn (query) bay vào, nó bắt đầu ở node gốc ẩn tại tầng cao nhất $\rightarrow$ dò dẫm xem có node "bạn bè" cùng tầng nào gần query hơn không $\rightarrow$ Cứ men theo chiều gần hơn $\rightarrow$ Cho tới khi bị kẹt (không thấy ai ở tầng đó gần hơn nữa), nó lập tức **Xuyên thủng (Drop down)** xuống tầng kế tiếp (y nguyên logic Skip List).
  - Liên tục lặp lại các bước rơi xuống cho đến tầng đáy (Layer 0). Lăng kính ngày càng thu hẹp lại. Tại đây, hệ thống tung lưới lân cận và chấm điểm chính xác (local beam search) để trả kết quả. Nhờ nhảy cóc từ trên cao, ta triệt tiêu đi việc phải lết từng centimet từ ngoài rìa vào không gian sâu.
- **Xác định khoảng cách như nào để không đi lệch vector?**
  Bất kể thuật toán IVF hay HNSW, việc định lượng "2 vector vector như thế nào thì được coi là gần nhau" phụ thuộc vào việc cấu hình hàm **Distance Metrics**. Quá trình search là chập query vector vào metric này để đo đạc với các vector trong Index.
  1. **Cosine Similarity (Khoảng cách Cosine)**:
     - Đo **Góc (Angle)** tạo bởi 2 tuyến vector. Càng hẹp (gần 0 độ) thì Cosine Similarity càng tiến về 1 (giống nhau nhất).
     - Nó bỏ qua "độ dài thẳng" cường độ (Magnitude), chỉ chắt lọc **Hướng đi (Direction)**.
     - *Dùng khi nào?*: Cực kì lý tưởng cho **Text Embeddings (NLP)** (VD: OpenAI text-embedding). Vì một câu siêu ngắn hay câu siêu dài cùng giải thích về chữ "Mèo" thì hướng vector phát triển giống nhau, chỉ khác độ dài.
  2. **Euclidean Distance (Độ đo L2 / L2 Norm)**:
     - Lấy thước đo đoạn thẳng vật lý nối trực tiếp 2 toạ độ điểm (Định lý Pytago không gian N chiều).
     - Đo cả khoảng cách và cường độ (Magnitude).
     - *Dùng khi nào?*: Computer Vision (Hình ảnh, Âm thanh) hay các Time-series recommendation.
  3. **Inner Product (Tích vô hướng / Dot Product - IP)**:
     - Gần y hệt đo Cosine nhưng nhân thêm độ dài, dễ tính hơn Cosine rất nhiều.
     - **Bí kíp tối ưu hệ thống**: Muốn chạy siêu tốc? Hãy đảm bảo mô hình AI ngay từ ban đầu sinh ra output vector đã ép độ dài bằng 1 (gọi là *L2 normalized*). Lúc đó toán học chứng minh `Inner Product = Cosine Similarity`. Lúc này ta cấu hình DB xài Inner Product thay vì Cosine. Việc này giúp bỏ 100% các phép Khai căn bậc 2 và chia phân số phức tạp $\rightarrow$ Tính bằng tập lệnh *SIMD* trực tiếp trên cấu trúc thanh ghi CPU quét vèo vèo siêu tốc độ!



### 9. Latching in database
Là cơ chế đảm bảo multi thread cho data trong nội bộ database(không phải transaction)

#### 9.1. Mục tiêu
   - Small memory footprint
   - Fast execution when no contention
   - Decentralize management of latches
   - Avoid expensive system calls(Linux torvals phản đối nó vào năm 2020)

#### 9.2. Các loại latch

   **1. Test-and-set Spinlock (Atomic)**
   - **Cơ chế hoạt động**: Sử dụng vòng lặp vô hạn (spin) liên tục kiểm tra và giành khóa bằng lệnh nguyên thủy của vi xử lý (như Compare-And-Swap - CAS). Khi không lấy được khóa, thread sẽ không ngủ mà liên tục "chạy không tải" (busy-wait).
   - **Nhược điểm**: Hiệu năng cao cho các giao dịch siêu ngắn, nhưng **không scale** khi có tranh chấp cao. Gây lãng phí CPU (burning cycle), hiện tượng quá tải cache coherence (các core liên tục giật cache line của nhau), và không thân thiện với OS (không nhường CPU cho thread khác).
   - **Nơi sử dụng**: Rất hiếm khi dùng độc lập trong Database hiện đại vì hao tổn CPU lớn. Chủ yếu dùng làm block xây dựng cơ sở hoặc bảo vệ các đoạn mã cực kỳ ngắn (ví dụ: cập nhật một biến counter nội bộ duy nhất tốn vài chỉ thị CPU).

   **2. Blocking Mutex (OS Lock)**
   - **Cơ chế hoạt động**: Sử dụng cơ chế khóa của hệ điều hành (như `std::mutex` hay `pthread_mutex`). Khi không lấy được khóa, thread chuyển trạng thái sang **ngủ (sleep)** và được OS đưa vào hàng đợi chờ (wait queue). Khi khóa được giải phóng, OS sẽ đánh thức (wake up) thread.
   - **Nhược điểm**: **Chi phí Context Switch (chuyển đổi ngữ cảnh) cao** (~25ns cho mỗi lần sys-call và sleep/wake). Nếu thời gian giữ khóa siêu ngắn (vài nanosecond), việc phải gọi sys-call rồi ngủ mất tài nguyên đáng kể, dẫn tới không thể scale.
   - **Nơi sử dụng**: Bảo vệ các cấu trúc dữ liệu hoặc tác vụ tốn nhiều thời gian, đặc biệt là quá trình đọc/ghi page vật lý từ đĩa (Disk I/O) lên Buffer Pool (để nhường CPU cho các thread khác chạy trong lúc phần cứng làm việc).

   **3. Read-Writer Latch (Shared/Exclusive Lock)**
   - **Cơ chế hoạt động**: Cho phép nhiều thread đọc (Shared/Read) truy cập cùng lúc, nhưng chỉ cho phép tối đa 1 thread ghi (Exclusive/Write) truy cập độc quyền.
   - **Nhược điểm**: Dễ bị "đói" Writer (Writer Starvation). Lỗ hổng lớn nhất là **Read-Contention** - dù nhiều luồng chỉ Đọc (không sửa dữ liệu), chúng vẫn phải cùng tranh nhau tăng/giảm một biến đếm (Reader Counter) ở dưới nền (dựa trên spin/mutex), khiến cache line bị thắt cổ chai $\rightarrow$ Thực chất vẫn không scale mạnh cho Multicore CPU.
   - **Nơi sử dụng**: Được sử dụng rộng rãi làm khóa tiêu chuẩn trong các CSDL truyền thống, ví dụ bảo vệ các Node cha con trong lúc đi từ trên xuống dưới B+ Tree (kỹ thuật Crabbing lock). 

   **4. Adaptive Spinlock (Hybrid Lock)**
   - **Cơ chế hoạt động**: Lai ghép giữa Spinlock và Blocking Mutex. Khi khóa đang bận, thread sẽ **spin** (xoay tại chỗ) trong một số vòng lặp cố định hoặc thời gian cực ngắn. Nếu hết lượt spin mà chưa lấy được khóa, nó sẽ lùi bước (fallback) và **đi ngủ** (chuyển qua Blocking Mutex dính tới OS).
   - **Nơi sử dụng**: **Loại Latch được sử dụng phổ biến nhất** trong CSDL hiện đại. 
     - PostgreSQL sử dụng cho cơ chế `LWLock` (Lightweight Lock). 
     - MySQL (InnoDB) sử dụng `Mutex` nội bộ có Spin Wait trước khi nhường luồng (Cấu hình bằng tham số `innodb_spin_wait_delay`).

   **5. Queue-based Spinlock (MCS Lock)**
   - **Cơ chế hoạt động**: Giải quyết hiện tượng "căng thẳng cache" của Spinlock cơ bản. Thay vì hàng ngàn thread cùng dồn tụ kiểm tra và cố mở **một địa chỉ bộ nhớ duy nhất**, nó cho các thread xếp lại thành một hàng đợi (Queue). Mỗi thread chỉ spin trên **vùng nhớ nội hạt cục bộ riêng của nó** (Local flag). Khi một thread làm xong, nó sẽ qua đánh dấu cờ cho thread tiếp theo trong Queue.
   - **Nơi sử dụng**: Hiệu quả mạnh mẽ ở vùng có mức độ cạnh tranh siêu cấp trên các hệ thống CPU máy chủ cực lớn kiến trúc NUMA (nơi mà việc ghi bộ nhớ chéo core rất đắt đỏ). Giải pháp này còn đảm bảo tính **công bằng** (Ai đợi trước sẽ lấy khóa trước, không có thread nào bị đợi mãi mãi). MySQL đã bắt đầu áp dụng thay thế cho một phần các Spinlock nặng trĩu.

   **6. Optimistic Lock Coupling (Hardware-assisted Latching)**
   - **Cơ chế hoạt động**: Đột phá tư duy hoàn toàn: **Thread Read không cần lấy bất kỳ khóa (lock) nào cả**. Mọi object (như Node của Tree) được gắn cho 1 biến `Version counter`. 
     1. Reader tự động ghi nhận bộ đếm `version` hiện tại.
     2. Reader thỏa sức Đọc dữ liệu.
     3. Trước khi Reader dời đi, sẽ **kiểm tra lại** `version` đó. Nếu version thay đổi $\rightarrow$ Có một Writer nào đó vừa làm xáo trộn $\rightarrow$ Thread đọc coi như thất bại và phải bắt đầu **Làm lại (Retry)** từ đầu.
   - **Nơi sử dụng**: Khuynh hướng chung của các hệ thống **In-Memory DBMS** tương lai (Silo, HyPer, SAP HANA). Phổ biến nhất trong việc duyệt các node gốc của cấu trúc B+ Tree vì ở trên đỉnh root tỷ lệ read gấp hàng triệu lần tỷ lệ write, nếu loại bỏ hoàn toàn quá trình ghi lock Read (tránh được cập nhật Reader Counter vật lý) sẽ đẩy tốc độ duyệt cao phi mã. Nó sửa sai tuyệt đối rào cản từ Read-Writer Latch phía trên.

Hash table latching
   Các phương án:
   1. Global latch: single latch để chặn entire of data structure

   2. Page/block latch: Lock theo từng page và block với read-writer lock

   3. Slot latch: lock theo từng slot

#### 9.3. B+Tree Concurrency Control
- **Mục tiêu**: Đảm bảo an toàn tính nhất quán khi nhiều threads thao tác vào cây, ngăn cấu trúc phân thân đứt gãy trong các sự kiện biến động (Split/Merge), đồng thời tối ưu hóa thông lượng tải chạy song song.

- **Safe Node (Node an toàn)** — Định nghĩa then chốt cho tất cả kỹ thuật bên dưới:
    - Một node được coi là **Safe** khi thao tác hiện tại chắc chắn **không lan truyền (propagate)** thay đổi cấu trúc lên node cha.
    - Điều kiện Safe tuỳ loại thao tác:
        | Thao tác | Điều kiện Safe |
        |---|---|
        | **Read (SELECT)** | Mọi node đều safe → nhả latch cha ngay lập tức |
        | **Insert** | Node con **chưa đầy** → không thể split → cha không bị ảnh hưởng |
        | **Delete** | Node con **hơn nửa đầy** → không thể merge/redistribute → cha không bị ảnh hưởng |

- **1. Latch Crabbing (Latch Coupling)**
    - **Tên gọi**: "Crabbing" (Cua bò) — lấy ý từ hình ảnh con cua di chuyển: **luôn bám ít nhất 1 chân** (giữ latch con) trước khi nhấc chân khác (nhả latch cha). Thread luôn giữ ít nhất 1 latch trên đường đi, đảm bảo không bao giờ "rơi tự do" giữa cây.
    - **Cơ chế chung**: "Khóa cha → Chụp con → Nhả cha nếu con Safe". Thread duyệt từ đỉnh xuống, chiếm lấy latch của node cha, sau đó lấy latch của node con. Khi xác nhận node con đã **Safe**, thread lập tức **nhả (Unlock)** sớm latch của tất cả tổ tiên (ancestors) phía trên.
    - **Với Read**: Thread chỉ cần *S-Latch (Shared)* xuyên suốt. Vì mọi node đều safe cho Read nên thread nhả latch cha **ngay khi** lấy được S-Latch con → nhiều luồng Read thoải mái chen chân cùng lúc, tốc độ cực cao.
    - **Với Insert/Delete**: Thread phải dùng *X-Latch (Exclusive)* trên đường đi xuống:
        1. Lấy X-Latch Root
        2. Lấy X-Latch node con
        3. Nếu node con **Safe** → nhả toàn bộ latch tổ tiên (vì chắc chắn split/merge không lan lên)
        4. Nếu node con **Unsafe** → giữ nguyên latch cha, tiếp tục xuống
    - **Phòng ngừa Deadlock**: Bắt buộc chiều khóa 1-way (Top-down). Nếu cho phép khóa ngược (Bottom-up — ví dụ khi split phải sửa cha), hai luồng ngược chiều một lên một xuống có thể ôm kẹt nhau vĩnh viễn. Quy tắc Top-down triệt tiêu hoàn toàn deadlock dọc thân cây.
    - **Ưu điểm**: Thu hẹp tối đa không gian bị khóa thay vì phong tỏa cả thân cây, "mở hẻm" cho hàng loạt phiên quét khác lách vào các nhánh kế cận.
    - **Nhược điểm (Thắt cổ chai Root)**: Bất kỳ lệnh Ghi/Xóa (`INSERT`/`DELETE`) nào cũng khởi hành bằng việc lấy *X-Latch* trên Root. Trong lưu lượng Write ác liệt, đỉnh Root chính là nút cổ chai nghiêm trọng — mọi Write phải xếp hàng tuần tự tại đây dù chúng nhắm vào các nhánh con khác nhau.

- **2. Optimistic Latching (Khóa lạc quan)**
    - **Giả định**: Tuyệt đại đa số thao tác cập nhật (`INSERT`/`DELETE`) sẽ rơi vào những Node Lá còn dư không gian, hiếm khi dẫn tới Split/Merge.
    - **Cơ chế (Optimistic Crabbing)**:
        1. **Lướt xuống bằng S-Latch**: Dù mang sứ mệnh Ghi, luồng vẫn chỉ cầm *S-Latch (Shared)* duyệt từ Root xuyên qua các internal node, **nhả ngay** S-Latch cha khi chụp được S-Latch con (y hệt Read crabbing). Điều này buông rộng cửa cho các luồng khác chen chân cùng lúc vượt qua Root và internal nodes.
        2. **Đến Leaf — đổi sang X-Latch**: Vừa chạm tới Node Lá, thread nhả S-Latch cuối cùng trên internal node và **lấy X-Latch** trên Leaf. Lúc này thread **không giữ bất kỳ latch nào trên internal nodes** — toàn bộ thân cây đã được giải phóng.
        3. **Phân xử kết quả**:
           - **Lá Safe** (còn chỗ trống) → Ghi trực tiếp và hoàn tất. Tốc độ tối ưu.
           - **Lá Unsafe** (thiếu không gian → cần Split/Merge) → Dự đoán lạc quan thất thủ! Thread buộc phải **Hủy bỏ (Abort)** toàn bộ, nhả X-Latch leaf, lùi về Root và **Làm lại từ đầu (Retry)** theo kỹ thuật Latch Crabbing bi quan cổ điển (ôm X-Latch dọc đường xuống).
    - **Ưu điểm**: Xóa sổ nút cổ chai Root — Writer không còn phải giành X-Latch trên Root trong trường hợp phổ biến (leaf safe). Đẩy mạnh song song Read/Write khi cấu trúc cây ổn định.
    - **Nhược điểm**: Trả giá cực đắt nếu đoán lầm — đội chi phí CPU qua nhiều vòng Retry. Thể hiện thê thảm nếu database bị Bulk Import một đợt Insert dữ liệu chưa Sort, liên tục gây Split → retry bất tận.

- **3. Leaf Node Scan (Vấn đề Deadlock trên dãy lá)**
    - **Bối cảnh**: Trong B+Tree, các leaf node được nối thành **doubly-linked list** để hỗ trợ range scan. Khi một thread cần quét tuần tự (ví dụ `SELECT ... WHERE id BETWEEN 100 AND 500`), nó sẽ latch leaf hiện tại → latch leaf kế tiếp → nhả leaf cũ.
    - **Nguy cơ Deadlock**: Nếu 2 thread quét **ngược chiều** nhau trên cùng dãy leaf:
        ```
        Thread A: giữ Leaf-3, chờ latch Leaf-4 →
        Thread B: giữ Leaf-4, chờ latch Leaf-3 ← 
        → Deadlock!
        ```
        Latch Crabbing chỉ phòng deadlock theo chiều **dọc** (top-down). Chiều **ngang** (leaf-to-leaf) không được bảo vệ bởi quy tắc này.
    - **Giải pháp**: Sử dụng **No-Wait protocol** — Nếu thread không lấy được latch sibling ngay lập tức (latch đang bị thread khác giữ), nó **nhả hết** tất cả latch đang giữ, ghi nhớ vị trí hiện tại, rồi **retry** từ đầu (hoặc từ vị trí đã lưu). Không bao giờ chờ đợi → triệt tiêu deadlock.
    - **Lưu ý thực tế**: Một số hệ thống quy ước quét leaf **luôn 1 chiều** (left-to-right) để tránh hoàn toàn kịch bản ngược chiều.

- **4. B-link Tree (Lehman-Yao Algorithm)**
    - **Bối cảnh**: Latch Crabbing buộc phải **giữ latch cha** trong suốt quá trình split con → giảm song song. Lehman & Yao (1981) đề xuất B-link Tree để giải phóng cha sớm hơn.
    - **Ý tưởng cốt lõi**: Mỗi node có thêm **right-link pointer** trỏ sang node anh em bên phải (sibling) cùng tầng, kèm theo một **high-key** (giá trị lớn nhất mà node chịu trách nhiệm).
    - **Cơ chế Split an toàn** (chỉ cần lock tối đa 3 nodes thay vì cả path):
        1. **Latch node hiện tại** (node bị đầy cần split)
        2. **Tạo node mới** (nửa phải), copy nửa trên key sang
        3. **Cài right-link**: Node cũ trỏ sang node mới, node mới thừa kế right-link cũ
        4. **Nhả latch node cũ** — lúc này node cũ đã "an toàn" nhờ right-link: nếu ai đó đang duyệt mà key vượt quá high-key của node cũ, họ tự động nhảy sang node mới qua right-link
        5. **Latch node cha** → chèn key phân cách (separator key) và pointer tới node mới → **Nhả cha**
    - **Ưu điểm**: Cha không bị giữ latch trong suốt quá trình split → tăng song song đáng kể. Luồng Read/Search khi gặp node đang split vẫn tìm được đúng kết quả nhờ cơ chế right-link + high-key.
    - **Ứng dụng thực tế**: **PostgreSQL** sử dụng B-link Tree (thuật toán Lehman-Yao) làm chiến lược concurrency chính cho B+Tree index (`nbtree`). Đây là lý do PostgreSQL có thể handle write-heavy workload trên index hiệu quả.



    ### 10. Sorting
    
    Khi mọi thứ không được lưu trữ trên ram mà trên disk thì tối ưu I/O đôi khi lại quan trọng hơn là tốc độ thuật toán
    Việc lấy data: tuple hay chỉ record id()