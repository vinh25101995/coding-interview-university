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
    - Sẵn sàng hỗ trợ **Wildcard / Regex Search / Fuzzy match** (ví dụ search `B*C` hoặc tìm sai chính tả `BNC`) dễ dàng vì căn nguyên gốc của nó là Automaton Engine.

- **Cơ chế Xử lý khi Insert (Tính Bất biến của Term Dictionary)**:
    - **Vấn đề nghịch lý**: FST tính toán phân chia trọng lượng (weight) rất tinh vi ở từng ngã rẽ. Nếu có một Term mới chèn vào giữa, đồ thị sẽ phải tính toán lại toàn bộ trọng số dọc đường để bảo toàn ID, gây sập hiệu năng.
    - **Giải pháp - Kiến trúc Segment**: **Apache Lucene KHÔNG BAO GIỜ chỉnh sửa cây FST cũ**. Nó sử dụng nguyên lý Không thay đổi (Immutable).
        1. **In-Memory Buffer**: Khi Insert, text mới nằm tạm trong RAM bằng cấu trúc thông thường (như Skip List).
        2. **Flush to Disk**: Khi đầy, Buffer xả xuống đĩa tạo thành một khối dữ liệu đóng băng gọi là **Segment** mới.
        3. **Build FST 1 Lần Duy Nhất**: Trước khi ghi xuống, danh sách từ vựng được Sắp xếp Alphabet (`Sort`). Thuật toán của Lucene bắt đầu Build cây FST từ dưới lên (Bottom-up). Vì đã có sẵn mảng đã Sort, FST biết chính xác phải đặt bao nhiêu trọng số lên ngã rẽ nào, làm 1 lần ăn ngay rồi cất vĩnh viễn vào Segment đó.
    - **Quy trình Tìm kiếm (Query)**: Bắn truy vấn Search tới toàn bộ FST của tất cả Segments đang có $\rightarrow$ Gom Postings List lại $\rightarrow$ Hợp nhất (Merge) $\rightarrow$ Trả kết quả.
    - **Gộp rác (Background Merge)**: Khi lượng Segment quá nhiều (đọc nhiều FST gây chậm), máy sẽ chạy ngầm tiến trình gộp nhiều Segment nhỏ thành 1 Segment bự, loại bỏ rác/doc đã xóa, và **Build lại 1 cây FST duy nhất** cho Segment lớn, sau đó xóa đống cây FST lẻ tẻ cũ.