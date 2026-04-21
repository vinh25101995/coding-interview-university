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
    
    Khi mọi thứ không được lưu trữ trên RAM mà trên disk thì tối ưu I/O đôi khi lại quan trọng hơn là tốc độ thuật toán
    Việc lấy data: tuple hay chỉ record id

    - Khi query chứa LIMIT thì chỉ cần dùng thuật toán Top-N (min heap / max heap)
    - Khi không biết trước số lượng kết quả, ta dùng thuật toán để chia nhỏ data và combine lại:
        - Phase 1: Sorting — chia nhỏ data fit trong RAM, sort từng phần, sau đó lưu lại trong disk thành các sorted runs
        - Phase 2: Merge — gộp các sorted runs lại với nhau
        - Công thức (với B buffer pages, N data pages):
            - Tổng quát: số pass = 1 + ⌈log_{B-1}(⌈N/B⌉)⌉
            - Trường hợp 2-way merge (B=3, 2 input + 1 output buffer): số pass = 1 + ⌈log2(N)⌉
            - Total I/O = 2N × số pass (mỗi pass đọc N page + ghi N page)
        2-pass merge sort:
            - Yêu cầu: B ≥ √N buffer pages (Phase 1 tạo ⌈N/B⌉ sorted runs, Phase 2 merge tất cả cùng lúc → cần ít nhất ⌈N/B⌉ + 1 buffer pages)

        Double buffering:
            Thay vì sử dụng toàn bộ buffer pool, ta chia chúng thành 2 nửa
            Khi 1 phần hoàn thành và thực thi ghi vào disk, ta thực hiện sort data tại nửa thứ 2
            Việc này giúp ta overlap I/O và CPU (tối ưu tài nguyên CPU), nhưng giảm hiệu quả của buffer pool đi 1 nửa

    Phân bổ tài nguyên: 
        - Postgres khá cứng nhắc việc cung cấp tài nguyên: Ví dụ 1GB RAM, mỗi query chỉ có thể dùng 100MB (work_mem)
        - Các DB doanh nghiệp (Oracle, SQL Server) thực hiện tốt hơn khi có cơ chế memory grant linh hoạt — mượn thêm từ các query không sử dụng

    Tối ưu compare:
        Kĩ thuật 1: Code specialization / JIT — thay vì gọi function pointer cho comparator, generate code cụ thể (inline function) để giảm overhead gọi hàm
        Kĩ thuật 2: Suffix truncation — compare binary prefix có độ dài cố định của varchar trước, chỉ khi prefix bằng nhau mới compare full string (giảm cache miss)
        Kĩ thuật 3: Đưa các variable leng thành fixed length và dùng thuật toán để so sánh??

    Aggregate:
       - Tổng hợp data ta cần dùng thuật toán sorting hoặc hashing
       - Tùy theo phần cứng mà sorting hoặc hash có thể nhanh hơn, nhưng trong đại bộ phận các trường hợp thì hashing nhanh hơn
       - Tương tự như sort, nếu data > memory thì ta cần chiến lược chia nhỏ

       External Hashing Aggregate (khi data không fit trong RAM):
           Vấn đề: Tại sao không chia nhỏ data thành chunk rồi hash lần lượt?
               → Các key giống nhau (cùng group) bị PHÂN TÁN khắp các chunk
               → Ví dụ: key "Vietnam" có thể xuất hiện ở chunk 1, chunk 5, chunk 99...
               → Khi merge partial results, phải tìm và gộp lại tất cả entry cùng key → chi phí merge rất lớn

           Giải pháp: Dùng hash function để đảm bảo các key giống nhau luôn rơi vào CÙNG 1 partition
               → Mỗi partition là độc lập, aggregate xong là xong, KHÔNG CẦN MERGE

           Phase 1 — Partition (dùng hash function h1):
               - Duyệt toàn bộ data, với mỗi tuple: bucket_id = h1(key) % B (B = số partition)
               - Ghi tuple vào buffer của bucket tương ứng
               - Khi buffer đầy → flush bucket ra disk
               - Kết quả: tất cả tuple có cùng key nằm trong cùng 1 partition trên disk

           Phase 2 — ReHash (dùng hash function h2 ≠ h1):
               - Với mỗi partition:
                   1. Load partition vào RAM
                   2. Build hash table bằng h2 (khác h1 để tránh cùng collision pattern)
                   3. Tính aggregate (SUM, COUNT, AVG...) trực tiếp trên hash table
                   4. Output kết quả
               - Tại sao dùng h2 ≠ h1? Vì h1 chỉ đảm bảo cùng key → cùng partition.
                 Trong partition, cần h2 để build hash table hiệu quả (tránh clustering)

           Yêu cầu bộ nhớ:
               - Cần B ≤ (số buffer pages - 1) partitions
               - Mỗi partition phải fit trong RAM ở Phase 2 → cần B ≈ √N partitions
               - Nếu 1 partition vẫn quá lớn → recursive partitioning: hash lại partition đó bằng h3 để chia nhỏ hơn

           So sánh:
               | Naive (chia chunk tuần tự)           | External Hash Aggregate              |
               | Key giống nhau phân tán khắp chunks  | Key giống nhau tập trung 1 partition |
               | Phải merge tất cả partial results    | Không cần merge — partition độc lập  |
               | I/O cao do merge                     | I/O = 3N (read + write + read lại)   |
    

    ### 11. Join
    #### 11.1. Join Algorithm
    ##### 11.1.1. Nested Loop Join
        Thuật toán đơn giản nhất: duyệt qua từng tuple của R, với mỗi tuple, duyệt qua từng tuple của S và so sánh
        Tốn I/O: O(N*M) nếu không có buffer, O(N*M/B) nếu có buffer
        
    ##### 11.1.2. Block Nested Loop Join
        Tương tự như nested loop join nhưng thay vì duyệt từng tuple của S, ta duyệt từng block của S
        Tốn I/O: O(N*M/B) nếu có buffer
        
    ##### 11.1.3. Sort-Merge Join
        Tốn I/O cho sort và join nhưng nhanh hơn rất nhiều so với nested loop join
    
    ##### 11.1.4. Hash Join
        Sử dụng hash table để join
        Cải tiến: Dùng bloom filter trước khi check hash table
        Khi data > memory ta cần tối ưu lại
        11.4.1. Partitioned Hash Join
            Đôi khi còn được gọi là grace hash join
            Tương tự: Thực hiện hash R và S thành các partition
            Phase 1: Partition
                - Dùng hash function h1 để hash R và S thành các partition
                - Ghi partition vào disk
            Phase 2: Join
                - Với mỗi partition:
                    1. Load partition vào RAM
                    2. Build hash table bằng h2 (khác h1 để tránh cùng collision pattern)
                    3. Tính join trực tiếp trên hash table
                    4. Output kết quả
            Yêu cầu bộ nhớ:
                - Cần B ≤ (số buffer pages - 1) partitions
                - Mỗi partition phải fit trong RAM ở Phase 2 → cần B ≈ √N partitions
                - Nếu 1 partition vẫn quá lớn → recursive partitioning: hash lại partition đó bằng h3 để chia nhỏ hơn
            I/O: 3 * (N+M)
            So sánh:
                | Naive (chia chunk tuần tự)           | Partitioned Hash Join              |
                | Key giống nhau phân tán khắp chunks  | Key giống nhau tập trung 1 partition |
                | Phải merge tất cả partial results    | Không cần merge — partition độc lập  |
                | I/O cao do merge                     | I/O = 3N (read + write + read lại)   |
        11.4.2. Hybrid Hash Join
            Là sự kết hợp giữa Simple Hash Join (toàn bộ trên RAM) và Grace Hash Join (toàn bộ trên Disk).
            Được sử dụng chủ yếu trong PostgreSQL, SQL Server, MySQL...
            Ý tưởng: Tận dụng tối đa bộ nhớ RAM còn thừa thay vì ép tất cả xuống Disk.
            Cách hoạt động:
                - Phase 1 (Partitioning): 
                    + Cố ý chia thành 1 phân vùng đặc biệt (Partition 0) vừa khít với lượng RAM đang có và giữ TẤT CẢ data của nó trên RAM (xây luôn Hash Table).
                    + Các phân vùng còn lại (Partition 1...k) được stream ghi xuống Disk.
                    + Khi quét bảng S, nếu tuple rơi vào Partition 0 -> Join và trả kết quả ngay lập tức (không chạm Disk). Rơi vào Partition 1...k -> Ghi xuống Disk.
                - Phase 2 (Grace Hash Join): 
                    + Chỉ thực hiện Grace Hash Join (đọc lên RAM, build & probe) cho các phân vùng từ 1..k đã lưu dưới Disk.
            Tối ưu I/O: 
                - Cực kỳ tiết kiệm I/O do Partition 0 hoàn toàn không sinh ra tác vụ Read/Write xuống Disk nào (tiết kiệm được 2*size(Partition 0) chi phí I/O).
    
    
        #### 11.2. Tổng kết IO Cost của các thuật toán Join
    | Algorithm | IO Cost | Example |
    | :--- | :--- | :--- |
    | Naïve Nested Loop Join | $M + (m \times N)$ | 1.3 hours |
    | Block Nested Loop Join | $M + (\lceil M / (B-2) \rceil \times N)$ | 0.55 seconds |
    | Index Nested Loop Join | $M + (m \times C)$ | Variable |
    | Sort-Merge Join | $M + N + \text{(sort cost)}$ | 0.75 seconds |
    | Hash Join | $3 \times (M + N)$ | 0.45 seconds |


    #### 11.3. Vấn đề kích thước của Hash Table
    Vấn đề chung với Hash Table trong DB là **Rất khó đánh giá chính xác số lượng Unique Keys (Cardinality)** từ ban đầu để cấp phát cho đúng kích thước RAM.
    Khi cấp phát sai (nhỏ hơn thực tế), Hash Table bị đầy, dẫn tới va chạm (collision) cực cao. Có các hướng giải quyết:
      1. Tình huống tĩnh (Static Hash Table): Chấp nhận lưu dồn bằng **Overflow Pages (Ghi tràn xuống đĩa bằng bucket chaining)**. Tác hại là làm mất độ truy xuất O(1) và sinh ra I/O random làm hệ thống kẹt cứng.
      2. Tình huống động (Dynamic Hash Table): Sử dụng **Extendible Hashing** hoặc **Linear Hashing** để tự dãn nở mảng mà không cần phải re-hash lại toàn bộ giá trị. Đánh đổi lại là mất thêm chi phí tính toán thu dọn khi resize.
      3. Ở Hash Join (Grace Hash Join): Xử lý bằng **Recursive Partitioning** (Dùng hàm hash mới chia lại cái phân vùng tràn đó thành các file nhỏ hơn ghi xuống đĩa tiếp).


    ### 12.Query plan
    Query plan là 1 DAG of operators

    Pineline là 1 chuỗi các operators được thực thi tuần tự mà các tuple được xử lý liên tục từ operator này sang operator khác mà không cần phải lưu toàn bộ kết quả vào bộ nhớ.
    
    Pineline breaker là operator không thể hoàn thành cho đến khi toàn bộ các con của nó emit
    -> join, aggregate, sort

    #### 12.1. Processing model
    Định nghĩa cách dbms thực thi data hoặc chuyển data giữa các operator
    - Control flow: cách các operator giao tiếp với nhau
    - Data flow: cách data được truyền giữa các operator

    #### 12.1.1. Iterator model (Volcano Model / Pipeline Model)
    - next(): return next tuple -> Các tuple được trả về và xử lý lần lượt
    - open()/close(): Khởi tạo và dọn dẹp state của operator.
    - Đây là model phổ biến nhất trong các hệ quản trị CSDL truyền thống (MySQL, PostgreSQL, SQLite...).
    - **Ưu điểm**: Bộ nhớ sử dụng (Memory footprint) rất nhỏ, do dòng dữ liệu được truyền luân phiên (pipeline) lên trên mà không cần chờ toàn bộ (trừ các Pipeline Breaker operator như Sort, Hash Join).
    - **Nhược điểm lớn nhất**: Chi phí gọi hàm `next()` liên tục (function call overhead) rất cao giữa hàng triệu/tỷ bản ghi, gây lãng phí CPU. 
    Lưu ý: Tại sao khi học ta thấy query theo các page nhưng bản chất lại xử lí lần lượt theo từng tuple?
    Nó là việc tách biệt giữa tầng thực thi (Execution Engine) và tầng lưu trữ (Storage Engine).
    Khi ta học, ta thường tập trung vào tầng lưu trữ (Storage Engine) để hiểu cách đọc dữ liệu từ đĩa.
    Nhưng khi thực thi query, ta lại tập trung vào tầng thực thi (Execution Engine) để hiểu cách xử lý dữ liệu.
    Do đó mới nảy sinh sự khác biệt giữa việc exuctation và storage.


    #### 12.1.2. Materialization model
    - Thực thi ý tưởng: Thay vì xử lý trên từng tuple được trả về, child operator sẽ trả về toàn bộ kết quả (một mảng/list) cho parent cùng một lúc.
    - Parent sẽ nhận và xử lý toàn bộ cục kết quả đó.
    - **Ứng dụng**: Phù hợp với hệ thống **In-memory Database** hoặc workload **OLTP** (giao dịch, tải nhẹ). OLTP query thường chỉ truy xuất/update 1 vài bản ghi nên trả về 1 mảng kết quả cuối cùng không tốn nhiều memory, lại tránh được overhead của hàm `next()` như Iterator model.
    - **Nhược điểm**: Không có nhiều DB dùng cho truy vấn lớn/phân tích (OLAP) vì cần phải allocate bộ nhớ khổng lồ để lưu toàn bộ kết quả trung gian, dễ dẫn tới tràn RAM/Disk I/O thảm hoạ.

    #### 12.1.3. Vectorized model / Batch model
    - Kết hợp giữa 2 model trên: Cân bằng hoàn hảo.
    - Thay vì gọi hàm `next()` trả 1 tuple (Iterator) hay xả toàn bộ về 1 cục khổng lồ (Materialization), thì gọi `next()` trả về **từng batch (mảng)** của tuple (VD: batch 1000 - 10000 tuples, hoặc kích thước dữ liệu vừa khít lọt vào L1/L2 Cache của CPU).
    - **Ưu điểm**: 
      - Cực kỳ tối ưu: Giảm tải overhead của việc gọi hàm `next()`.
      - Tận dụng được Cache của CPU hiệu quả không phải xuống RAM đọc đi đọc lại.
      - Phát huy trọn vẹn sức mạnh của tập lệnh xử lý song song **SIMD (Single Instruction, Multiple Data)** chíp hiện đại.
    - **Nhược điểm**: Vẫn tốn nhiều bộ nhớ hơn một chút so với Iterator truyền thống, và code implementation cực kỳ phức tạp.
    - **Ứng dụng**: Thống trị mảng phân tích dữ liệu **OLAP / Data Warehouse** hiện đại. Hầu hết DB phân tích dùng mô hình này (ClickHouse, Snowflake, Presto, DuckDB...).

    
    #### 12.2. Plan processing model (Pull vs Push)
    - Trong tất cả các model trên (Iterator, Materialization, Vectorized), ta đều thấy có 1 điểm chung là luồng điều khiển đi từ trên xuống dưới, nhưng luồng dữ liệu lại được "kéo" từ dưới lên (Pull) thông qua hàm `next()`.
    - Có 2 kiến trúc chính về luồng điều khiển (Control Flow):
        1. **Pull-based (Mô hình Kéo)**:
            - Parent operator sẽ gọi hàm `next()` để kéo dữ liệu từ child operator.
            - Phần lớn các RDBMS truyền thống (như MySQL, PostgreSQL) sử dụng kiến trúc này.
        2. **Push-based (Mô hình Đẩy / Data-centric)**:
            - Đảo ngược luồng điều khiển: Các child operator (như Scan) sẽ chủ động "đẩy" (push) dữ liệu lên cho parent operator ngay khi nó đọc được.
            - Thay vì gọi hàm `next()` tốn kém, hệ thống sẽ "gộp" (**Operator Fusion**) các toán tử lại với nhau vào chung một vòng lặp `for`.
            - Được sử dụng trong các hệ thống hiện đại, đặc biệt là In-memory DB (HyPer, DuckDB).

    **Ví dụ minh họa Operator Fusion trong Push Model:**
    > Truy vấn: `SELECT B.val FROM A JOIN B ON A.id = B.id WHERE A.val > 100`

    *1. Vấn đề của Pull Model (Mô hình Kéo)*
    - Trong mô hình Iterator (Pull), dữ liệu truyền qua các lời gọi hàm `next()`. Mỗi khi lấy một tuple, CPU phải nhảy từ `Join.next()` -> `Filter.next()` -> `Scan.next()`. 
    - Việc gọi hàm ảo (virtual function calls) hàng triệu lần tạo ra độ trễ (overhead) khổng lồ và **phá hỏng CPU Cache** do ngữ cảnh thực thi (code path) liên tục bị chuyển đổi qua lại giữa các operator.

    *2. Giải pháp của Push Model: "Gộp" toán tử (Operator Fusion)*
    - Kiến trúc Push (Data-centric) để node Scan chủ động đọc và đẩy thẳng dữ liệu qua các bước xử lý tiếp theo ngay trong cùng một vòng lặp.
    - Dưới đây là mã giả minh hoạ cách Push Model "Fuse" (gộp) các toán tử `Scan`, `Filter` và `Join`:

    ```cpp
    // --- PIPELINE 1: Đẩy dữ liệu bảng A (Build Phase) ---
    // Toán tử Scan A chủ động quét dữ liệu
    for (Tuple a : tableA) { 
        // Toán tử Filter được GỘP (fused) thẳng vào vòng lặp này
        if (a.val > 100) { 
            // Đẩy thẳng dữ liệu thoả mãn vào Hash Table của toán tử Join
            // (Đây là Pipeline Breaker, luồng dữ liệu của 1 tuple tàm dừng ở đây)
            hashTable.put(a.id, a); 
        }
    }

    // --- PIPELINE 2: Đẩy dữ liệu bảng B (Probe Phase) ---
    // Sau khi Pipeline 1 xong, toán tử Scan B chủ động quét
    for (Tuple b : tableB) {
        // Toán tử Join Probe được gộp thẳng vào vòng lặp
        Tuple a = hashTable.get(b.id); 
        
        if (a != null) {
            // Toán tử Project/Emit cũng được gộp luôn
            emit(b.val); 
        }
    }
    ```

    *3. Tại sao Push Model lại nhanh hơn đột phá?*
    - **Tối đa hoá CPU Cache (Data Locality)**: Khi vòng lặp `for (Tuple a : tableA)` chạy, dòng dữ liệu `a` vừa được lấy từ RAM sẽ nằm ngay trong thanh ghi (register) hoặc L1 Cache siêu tốc của CPU. Nhờ gộp lệnh `if (a.val > 100)`, CPU kiểm tra và nhét nó vào `hashTable` ngay lập tức mà không copy/di chuyển dữ liệu ra vùng nhớ trung gian. Dữ liệu được xử lý triệt để ngay khi nó đang còn "nóng" trong CPU.
    - **Biên dịch truy vấn (Query Compilation / JIT)**: Để tạo ra được mã vòng lặp lồng nhau tối ưu như trên thay vì một đồ thị cây (Tree of Objects) rời rạc, hệ quản trị cơ sở dữ liệu hiện đại (như HyPer) sẽ lấy Query Plan Tree đó, sinh ra trực tiếp mã C++ hoặc mã máy (LLVM IR), rồi dùng cơ chế biên dịch JIT (Just-In-Time) để chuyển thành file thực thi chạy thẳng dưới nhân CPU. Việc này giúp tiết kiệm tối đa tài nguyên I/O và CPU so với việc thông dịch (interpret) đồ thị cây.


    #### 12.3. Access method
    Là cách thức mà hệ quản trị cơ sở dữ liệu truy cập dữ liệu (scan) trong cấu trúc file vật lý.
    Có các phương pháp tiếp cận chính:
        1. **Sequential Scan (Full Table Scan)**: Quét toàn bộ dữ liệu lần lượt từ đầu đến cuối page trên đĩa. Nên hạn chế dùng nhưng nếu bất đắc dĩ phải quét, DB có rất nhiều luồng tối ưu hạng nặng.
            *Các kĩ thuật tối ưu để giảm thiểu I/O và tăng tốc Sequential Scan:*
            - **Prefetching (Đọc trước dữ liệu)**: Thay vì đợi CPU yêu cầu từng page rồi mới xuống đĩa lấy (bị I/O block), Storage Manager sẽ đoán trước và tuồn sẵn một loạt các page nối tiếp nhau lên Buffer Pool trước. Giúp CPU chạy mượt không bị khựng lại chờ I/O.
            - **Buffer Pool Bypass (Đi vòng qua Buffer Pool)**: Khi một query cực lớn cần quét toàn bộ bảng, nếu đẩy data đó xen dòng qua Buffer Pool trung tâm sẽ làm "trôi" sạch (evict) các trang dữ liệu đang được cache nóng của các luồng nhỏ khác. Cấu trúc DB thông minh giải quyết bằng cách cấp vùng memory cục bộ riêng rẽ để chứa dữ liệu, quét xong hủy luôn tránh xả rác vào Buffer chung.
            - **Scan Sharing / Synchronized Scans**: Đi chung xe. Nếu có nhiều request đòi table scan cùng một bảng khổng lồ, thay vì mỗi người tự đọc đĩa quét lại từ đầu, Request đến sau sẽ "bám" (chu du cùng) với con trỏ I/O của Request đang quét dở dang, đến cuối file quay lại đầu bù lấp khúc thiếu → triệt tiêu lượng đọc Disk.
            - **Data Skipping / Zone Maps**: Lưu thẻ metadata siêu nhỏ gọn thống kê từng Block chứa gì (ví dụ: ghim `MIN: 10`, `MAX: 50`). Khi Query có câu `WHERE val = 99`, nó đi lướt qua thẻ metadata, thấy không khớp là **nhảy cóc (Skip)** cả Block luôn, hoàn toàn không cần cày I/O load block lên RAM. Bí kíp chí mạng của Snowflake / Parquet file.
            - **Late Materialization**: Đặc ân của Columnar Database. Quét chập từng mảng Column riêng rẽ để lọc điều kiện ở `WHERE`. Chỗ nào không khớp sẽ bị đánh dấu loại. Phễu rơi xuống màng lọc cuối cùng mới bắt đầu tút những cột cần xuất ra ở `SELECT` rồi gộp mảng dọc (tuple reconstruction). Vừa nhàn I/O, vừa bớt chuyển vị (shuffle) trong RAM.
            - **Data Encoding & Compression**: RÚT NGẮN độ dài byte mỗi record nằm trong Disk Block thông qua thuật toán nén như RLE, Dictionary → tăng lượng tuple kéo lên trong 1 thao tác I/O.
            - **Clustering / Sorting**: Định hình vị trí vật lý. DB dồn các record hay xuất hiện chung (Cluster) hoặc Sort theo trường chủ đạo liên tiếp nhau để việc kéo data là Sequential I/O (rẻ hơn nghìn lần Random I/O cày tung xới).
            - **Parallelization / Vectorization (SIMD)**: 
                + *Task Parallelization*: Cưa bảng làm 4 khúc nhỏ, gọi 4 Threads vả đồng loạt, nhanh gấp chục lần.
                + *Data Vectorization*: Dùng SIMD của nhân CPU nạp cả cụm Data vô Cache đo chung điều kiện thay vì check 1v1.

        2. **Index Scan**: Truy cập thông qua cổng Index (B+Tree), truy xuất dãy Target IDs rồi xuống Disk lấy Tuple gốc.
        3. **Multi Index Scan / Bitmap Scan**: Sử dụng 2 hay nhiều Index cùng lúc, mỗi Index trả về một tập hợp IDs. Dùng cấu trúc **Bitmap** kết hợp **Bitwise AND/OR** để tìm ra tập hợp IDs thoả mãn tất cả điều kiện → sau đó mới xuống Disk lấy tuple theo danh sách ID đã giao. Tránh được việc lookup Disk nhiều lần.

    #### 12.4. Modification Query

    - **Update/Delete**:
        - Child operator chỉ truyền **Record ID** cho parent thay vì truyền toàn bộ tuple.
        - Cần lưu thông tin các ID đã được xử lý để tránh **Halloween Problem**.
        - **Halloween Problem**: Xảy ra khi `UPDATE` một bản ghi khiến nó thay đổi vị trí (ví dụ: thay đổi giá trị indexed column) → bản ghi có thể xuất hiện lại trong quá trình scan và bị xử lý lần nữa → cần lưu lại danh sách ID đã xử lý để phát hiện và bỏ qua.

    - **Insert**:
        1. Tuple được truyền trực tiếp trong toán tử (literal values).
        2. Tuple được lấy từ child operator.
            - Ví dụ: `INSERT INTO table1 SELECT * FROM table2`

    #### 12.5. Expression Evaluation

    - **Biểu diễn**: DBMS biểu diễn các biểu thức (WHERE clause, computed columns...) dưới dạng **Expression Tree**.

    - **Các loại node trong Expression Tree**:
        - **Comparison**: `=`, `>`, `<`, `!=`
        - **Arithmetic**: `+`, `-`, `*`, `/`
        - **Function**: các hàm built-in hoặc UDF
        - **Conjunction / Disjunction**: `AND`, `OR`
        - **Constant**: giá trị hằng số (100, 200...)
        - **Tuple Attribute Reference**: tham chiếu cột (`A.x`, `A.y`...)

    - **Cách thực thi**: DBMS duyệt cây theo kiểu **post-order** (từ lá lên gốc). Tại mỗi node, nó gọi một hàm `evaluate()` tương ứng với loại operator đó. Kết quả trả về cho node cha, cứ thế cho đến root. Tương tự như việc ta liên tục phải `switch` để xử lý từng node trong tree vậy.

    - **Vấn đề hiệu năng**: Biểu diễn dạng tree gây chậm do overhead gọi hàm ảo (virtual function call), branch misprediction, và cache miss tại mỗi node — lặp lại cho **mỗi tuple** trong bảng.

    - **Giải pháp — JIT / Inline Function**: Một số DBMS cố gắng đưa expression tree về dạng **inline function** (compiled code) để loại bỏ overhead:

        ```
        Ví dụ: SELECT * FROM A WHERE A.x > 100 AND A.y = 200

        Biểu diễn dưới dạng tree:
                             AND
                            /   \
                          >       =
                         / \     / \
                        x  100  y  200

        Biểu diễn dưới dạng inline function:
        ```
        ```cpp
        bool evaluate(Tuple t) {
            return t.x > 100 && t.y == 200;
        }
        ```

### 13. Parallel Query Engine Architectures

- Process model định nghĩa cách hệ thống được tổ chức để support concurrent request/queries.
  - Dùng từ **concurrent** vì Process model nói về cách **tổ chức** để xử lý nhiều request cùng lúc (concurrency = quản lý nhiều task). **Parallelism** (chạy thật sự song song trên nhiều CPU) chỉ là một *cách triển khai* concurrency. Một DBMS có thể concurrent mà không parallel (ví dụ: single-core dùng time-slicing).
- Đơn vị triển khai là worker.
  - 1 worker có thể là 1 process, thread hoặc embedded DBMS.
  - Chủ yếu hiện nay các hệ thống DBMS sử dụng thread model.

#### 13.1. Process model
- Được sử dụng trong PostgreSQL (đến nay vẫn dùng process-per-connection), Oracle, DB2 (hỗ trợ cả process và thread model).
- Phụ thuộc vào OS dispatcher.
- Sử dụng shared memory hoặc pipeline để giao tiếp.
- Các thư viện pthread được chuẩn hóa từ những năm 90, do đó các hệ thống từ những năm 90 trở về trước thường sử dụng process model.

#### 13.2. Thread model
- Sử dụng trong SQL Server, MySQL. Oracle và DB2 hiện tại hỗ trợ cả hai (hybrid).
- DBMS tự quản lý thread pool và scheduling thay vì phụ thuộc OS, giúp kiểm soát tốt hơn số lượng concurrent workers, giảm context switch.

#### 13.3. Embedded model
- Chạy trong cùng address space (process) với application (ví dụ: RocksDB, SQLite, LevelDB, DuckDB).
- Application chịu trách nhiệm cho thread và scheduling.

#### 13.4. Scheduling
- Với mỗi query plan, cần quyết định where, when and how to execute it → Scheduling:
  - **Task assignment**: Gán query/operator cho worker nào.
  - **Task priority**: Thứ tự ưu tiên thực thi.
- Các DB doanh nghiệp như DB2, Oracle, SQL Server tự lập lịch (self-scheduling) với thread pool do DBMS quản lý.
- PostgreSQL để OS scheduling do dùng process model → OS quyết định process nào chạy khi nào.
- MySQL (InnoDB) dùng thread model, bản Enterprise có **Thread Pool Plugin** tự quản lý scheduling. Bản Community vẫn phụ thuộc OS scheduling, nằm giữa PostgreSQL (hoàn toàn OS) và Oracle/DB2 (tự lập lịch hoàn toàn).
- **PostgreSQL có chậm hơn vì OS scheduling không?** Có ảnh hưởng nhưng không phải yếu tố chính:
  - Context switch giữa process đắt hơn thread (~vài μs).
  - OS không hiểu workload DB nên scheduling không tối ưu (ví dụ: OS có thể preempt process đang giữ latch).
  - Tuy nhiên bottleneck đọc thường nằm ở **I/O** và **query plan** hơn là scheduling overhead.

#### 13.5. Parallel Execution
Các hệ thống DBMS thực thi nhiều task đồng thời để nâng cao hiệu suất sử dụng phần cứng.
- Các task được thực thi không nhất thiết thuộc về cùng 1 query.
- High-level design không phụ thuộc vào kiến trúc thực thi (process/thread hay multi-node).

##### 13.5.1. Inter-query Parallelism
- Thực thi **nhiều query** cùng lúc (mỗi query trên 1 worker riêng).
- Phần lớn sử dụng first-come-first-serve.
- Nếu các query đều là read-only thì phần lớn không cần explicit coordination (điều phối có chủ đích) giữa những query đó.
- Nếu có ghi thì việc thực thi đúng trở nên khó khăn (tricky) — cần concurrency control (lock, MVCC...).

##### 13.5.2. Intra-query Parallelism
- Thực thi **1 query** bằng cách chạy song song các operators của nó.
- Có 3 dạng parallelism:
  1. **Intra-operator** (data parallelism).
  2. **Inter-operator** (pipeline parallelism).
  3. **Bushy** parallelism.
- Các kĩ thuật này **không loại trừ lẫn nhau**, và có thể được kết hợp với nhau.
- Với mỗi toán tử đều có 1 version chạy song song, nhờ vậy multi-thread có thể sử dụng data tập trung hoặc chia nhỏ ra để xử lý.

**Ví dụ: Parallel Grace Hash Join**
- Mỗi worker thực thi hash và probe trên 1 partition riêng của hash table.
- Các partition độc lập → không cần coordination giữa các worker.

###### 13.5.2.1. Intra-operator (Data Parallelism)
- Operators được decompose thành các phần riêng lẻ để thực thi **cùng 1 function** trên các **sub-dataset khác nhau**.
- Ví dụ: Parallel Seq Scan chia bảng thành N phần, mỗi worker scan 1 phần.
- Cần insert thêm 1 **Exchange operator** để chia nhỏ (distribute) và tổng hợp (gather) dữ liệu.

**Exchange operator — cơ chế chi tiết:**
- Exchange operator (Volcano Exchange) là operator đặc biệt được chèn vào query plan để biến plan tuần tự thành plan song song. Nó đóng 2 vai trò: **distribute** (chia data cho worker) và **gather** (gom kết quả từ worker).
- Exchange operator có **3 biến thể**, được optimizer chọn tại planning time:

| Biến thể | Partitioning | Dùng khi |
|---|---|---|
| **Gather** | Round-robin / FIFO | Sub-plan không cần ordering → Seq Scan |
| **Redistribute** | Hash(`key` % N) hoặc Range | Hash Join, Aggregate — cần cùng key về cùng worker |
| **Broadcast** | Copy toàn bộ → mọi worker | Build side của Hash Join khi bảng nhỏ |

- **Ai quyết định dùng loại nào?** → **Query Optimizer**, không phải Exchange tự quyết tại runtime. Optimizer nhìn toàn bộ query plan tree và chèn đúng loại Exchange vào từng ranh giới song song. Sự phụ thuộc vào operator bên dưới (Seq Scan, Hash Join...) được **resolve tại planning time**, lúc chạy Exchange chỉ thực hiện strategy đã gán sẵn.

- **Pull model hoạt động thế nào với Exchange?**
  ```
  Parent thread: gọi exchange.next()
         ↓
  Exchange nhìn vào shared tuple queues (mỗi worker 1 queue)
         ↓
  ┌─ queue có tuple → dequeue và return cho parent
  ├─ queue rỗng    → block chờ cho đến khi worker nào push vào
  └─ tất cả worker xong + queue rỗng → return EOF
  ```
  - Các worker **không bị pull từng tuple** bởi parent. Chúng chạy **tự do** trên thread riêng, tự gọi `next()` trên sub-plan riêng và **push kết quả vào queue**. Exchange chỉ là **cầu nối** giữa push (worker → queue) và pull (parent ← queue).

- **Gather vs Gather Merge (PostgreSQL):**

  | Operator | Cách gom | Đảm bảo thứ tự? |
  |---|---|---|
  | **Gather** | Lấy tuple từ bất kỳ worker nào có sẵn trước | ❌ Không |
  | **Gather Merge** | Merge-sort từ các queue (mỗi worker trả data đã sorted) | ✅ Có — dùng cho `ORDER BY` |

- **Ví dụ query plan với Exchange (PostgreSQL):**
  ```
  Gather (Exchange - round-robin)
    └── Hash Join
          ├── Redistribute (hash on A.id)   ← optimizer chèn đúng loại
          │     └── Parallel Seq Scan A
          └── Redistribute (hash on B.id)
                └── Parallel Seq Scan B
  ```

- **Push vs Pull model với Intra-operator parallelism:**
  - **Pull model** (PostgreSQL): Mỗi worker thread chạy riêng 1 iterator pipeline. Parent gọi `next()` trên Exchange/Gather operator. Gather kéo tuple từ các worker qua **shared queue**. Exchange đóng vai trò điều phối, tập hợp kết quả từ nhiều worker.
  - **Push model** (HyPer, DuckDB): Mỗi worker chủ động đẩy tuple/batch vào shared output buffer. Gather consume từ các buffer đó. Hiệu quả hơn vì không có overhead gọi hàm `next()` xuyên qua nhiều tầng.

###### 13.5.2.2. Inter-operator (Pipeline Parallelism)
- Nhiều **operators khác nhau** trong cùng 1 query plan chạy đồng thời, output của operator dưới **stream trực tiếp** lên operator trên.
- Ví dụ: Scan → Filter → Join — cả 3 operator chạy cùng lúc trên các threads khác nhau. Scan đẩy tuple lên Filter, Filter đẩy lên Join mà không cần đợi Scan hoàn thành.

- Hiệu quả nhất với **Push model** vì data tự nhiên chảy từ dưới lên. Pull model khó tận dụng vì parent phải chủ động kéo.
- Sử dụng trong nhiều hệ thống data stream như: kafka, data flink

###### 13.5.2.3. Bushy Parallelism
- Nhiều **nhánh độc lập** của query plan tree được thực thi song song.
- Ví dụ: `SELECT * FROM A JOIN B ON ... JOIN C ON ...` — có thể scan bảng A và scan bảng B **cùng lúc** trước khi join, vì 2 nhánh scan này độc lập với nhau.
- Thực chất là sự kết hợp giữa inter-operator parallelism trên các nhánh song song của cây query plan.

#### 13.6. IO Parallel
##### 13.6.1. Multi-disk parallelism
- Lưu trữ dữ liệu trên nhiều đĩa để tăng tốc độ đọc/ghi và tính bền vững
  - Hardware based: I/O controller giúp multidisk được nhận biết như 1 disk duy nhất (ví dụ: RAID).
  - Software based: DBMS tự quản lý ở tầng file/object

- Mục tiêu
  - Performance (Hiệu suất): Đọc/ghi dữ liệu nhanh hơn bằng cách chia nhỏ dữ liệu và thực hiện song song trên nhiều ổ đĩa cùng lúc.
  - Capacity (Dung lượng): Lưu trữ được khối lượng dữ liệu khổng lồ mà một ổ đĩa đơn lẻ không thể chứa hết.
  - Durability (Độ bền / Tính an toàn): Nếu một ổ cứng bị hỏng (crash), dữ liệu không bị mất vì đã có bản sao hoặc mã khôi phục ở các ổ khác.

##### 13.6.2. Partitioning
- Kỹ thuật chia nhỏ dữ liệu thành các khối (block) và ghi xen kẽ (interleave) lên các vùng lưu trữ khác nhau được quản lý độc lập
- Không cần rewwrite lại application
- Mục tiêu chính: **Tăng tốc độ đọc/ghi** bằng cách tận dụng băng thông song song của nhiều đĩa.
- Khi cần đọc một block dữ liệu, hệ thống có thể đọc từ nhiều đĩa cùng lúc, giảm thời gian chờ đợi.
- Thường được sử dụng trong các hệ thống cơ sở dữ liệu lớn và hệ thống lưu trữ phân tán.

### 13.7. Tổng kết: Parallel Execution

#### 1. Parallel execution is important — Vì vậy hầu hết mọi DBMS lớn đều hỗ trợ nó.
- **Tầm quan trọng**: Trong thời đại dữ liệu lớn (Big Data), việc xử lý hàng tỷ bản ghi bằng một luồng (single-thread) là không khả thi. Để tận dụng tối đa phần cứng hiện đại (CPU nhiều lõi, hệ thống lưu trữ đa đĩa SSD/NVMe), DBMS bắt buộc phải chia nhỏ công việc và chạy song song.
- **Sự phổ biến**: Hầu hết các DBMS lớn (PostgreSQL, Oracle, SQL Server, hay các hệ thống phân tán) đều có bộ thực thi song song làm nền tảng cốt lõi.

#### 2. However, it is hard to get right — Tại sao lại khó đến vậy?

Việc cứ ném thêm worker (luồng/tiến trình) vào một truy vấn không phải lúc nào cũng làm nó nhanh hơn. Dưới đây là 4 rào cản lớn nhất:

- **Coordination Overhead** (Chi phí điều phối):
  - Khi chia một truy vấn cho 10 workers, cần có cơ chế khởi tạo, phân phát dữ liệu, kiểm tra trạng thái và gom kết quả lại (như Exchange Operator).
  - Nếu truy vấn quá nhỏ hoặc chạy quá nhanh, thời gian setup và điều phối workers có khi còn lâu hơn cả việc để 1 worker tự chạy từ đầu đến cuối.

- **Scheduling** (Lập lịch và Phân bổ):
  - Bài toán: "Worker nào làm việc gì, vào lúc nào, và làm trên dữ liệu nào?"
  - **Data Skew** (Lệch dữ liệu): Đây là ác mộng của scheduling. Nếu hash phân vùng bị dồn vào một key phổ biến, Worker 1 phải xử lý 90% khối lượng trong khi 3 workers kia làm xong 10% rồi ngồi chơi → toàn bộ hệ thống vẫn phải đợi Worker 1 xong.

- **Concurrency Issues** (Các vấn đề về đồng thời):
  - Nhiều workers cùng chạy sẽ tranh nhau truy cập các cấu trúc dùng chung trong RAM của DBMS (Buffer Pool, Hash Tables, System Catalog).
  - DBMS phải dùng Locks/Latches để đảm bảo tính toàn vẹn. Nếu thiết kế không khéo, các workers sẽ block lẫn nhau → nghẽn cổ chai hoặc Deadlock.

- **Resource Contention** (Tranh chấp tài nguyên):
  - **CPU Caches**: Các luồng tranh nhau đẩy dữ liệu vào L1/L2/L3, làm trôi mất dữ liệu của luồng khác (Cache trashing).
  - **Memory Bandwidth**: Băng thông truyền tải từ RAM vào CPU có giới hạn.
  - **Disk I/O**: Nếu 50 workers cùng đọc ngẫu nhiên từ một HDD, kim đọc phải nhảy liên tục → tốc độ tổng thể giảm thê thảm so với việc để 1 worker đọc tuần tự.

### 14. Query Optimization

#### 14.1. Tại sao cần Query Optimization?

Application → Parser → Binder → **Optimizer** → Executor

Dựa trên thông tin page (catalog), các toán tử ta có thể tính được cost theo từng step.

**Ví dụ minh họa** (`SELECT DISTINCT ename FROM Emp E JOIN Dept D ON E.did = D.did WHERE D.dname = 'Toy'`):

**Catalog thông tin:**

| Bảng | Cấu trúc | Records | Pages | Index |
|---|---|---|---|---|
| Emp | (ssn, ename, addr, sal, did) | 10,000 | 1,000 | clustered + unclustered |
| Dept | (did, dname, floor, mgr) | 500 | 50 | unclustered |

**Query Plan (naive — không tối ưu):**

```
π_ename                          ← Projection (loại trùng lặp)
    │
σ_dname='Toy'                    ← Selection: 2,000 reads + 4 writes
    │                               (10K/500 = 20 emps per dept)
σ_Emp.did = Dept.did             ← Selection: 1,000,000 reads + 2,000 writes
    │                               (FK join, 10k tuples ghi vào temp T₂)
×  (Cross Product)               ← (50 + 50,000) reads + 1,000,000 writes
    │                               Ghi temp file T₁ (5 tuples/page)
   Emp
```

**Chi tiết từng bước:**

1. **Cross Product (×)**: Kết hợp toàn bộ Emp × Dept
   - Reads: 1,000 (Emp pages) + 50 (Dept pages) = 1,050 reads
   - Writes: Emp × Dept = 10,000 × 500 = 5,000,000 tuples → với 5 tuples/page = 1,000,000 pages ghi xuống T₁
   - Tổng: **(50 + 50,000) reads + 1,000,000 writes**

2. **σ_Emp.did = Dept.did**: Lọc các tuple có điều kiện FK join từ T₁
   - Reads: 1,000,000 (đọc lại T₁)
   - Kết quả: 10,000 tuples khớp (vì mỗi Emp có 1 Dept) → ghi vào T₂
   - Tổng: **1,000,000 reads + 2,000 writes**

3. **σ_dname='Toy'**: Lọc Dept thuộc Toy department từ T₂
   - Tỷ lệ selectivity: 1/500 Dept → mỗi Dept có 10,000/500 = 20 emps
   - Reads: 2,000 (đọc T₂) + Writes: 4 pages kết quả
   - Tổng: **2,000 reads + 4 writes**

4. **π_ename**: Loại bỏ duplicates (DISTINCT)

**Tổng I/O: ≈ 2,003,054 ≈ 2M I/Os** → Cực kỳ tốn kém!

#### 14.1.2. So sánh 4 Query Plan — Tác dụng của Optimizer

Cùng 1 query: `SELECT DISTINCT ename FROM Emp JOIN Dept ON E.did = D.did WHERE D.dname = 'Toy'`

**Tổng quan tiến trình tối ưu:**

```
 Plan 1              Plan 2a             Plan 2b             Plan 3
 (Naive)             (Better Join)       (+ Pipeline)        (+ Index)
                                                           
 Cross Product   →   Sort-Merge Join →   Sort-Merge Join →   Index NL Join
 + Seq Scan          + Seq Scan          + Seq Scan           + Index Scan
                                                           
 ≈ 2,000,000 I/Os    7,159 I/Os          3,151 I/Os          37 I/Os
      │                   │                   │                   │
      └── ÷280x ──────────┘   ÷2.3x ──────────┘   ÷85x ───────────┘
                                                           
 Thay đổi:      [Thuật toán join]   [Loại temp T₂]    [Dùng index]
```

---

**Bảng so sánh chi tiết:**

| | Plan 1 | Plan 2a 🔴 | Plan 2b 🔵 | Plan 3 🟢 |
|---|---|---|---|---|
| **Join Algorithm** | Cross Product (×) | Sort-Merge Join | Sort-Merge Join | Index NL Join |
| **Access Method** | Seq Scan | Seq Scan | Seq Scan | Index Scan (dname, did) |
| **Processing Model** | Materialization | Materialization | **Pipeline** | Materialization |
| **Temp T₁** | 1,000,000 pages | 2,000 pages | 2,000 pages | ❌ Không cần |
| **Temp T₂** | 2,000 pages | 2,000 pages | ❌ Pipeline | ❌ Không cần |
| **Tối ưu mới** | — | Thuật toán join | Loại temp file | Index + pushdown σ |
| **Tổng I/Os** | **≈ 2,000,000** | **7,159** | **3,151** | **37** |

---

**Chi tiết từng Plan:**

```
┌─────────── Plan 1 ───────────┐  ┌─────────── Plan 2a 🔴 ──────────┐
│ π_ename                      │  │ π_ename                          │
│   │                          │  │   │  ← 4 reads (đọc T₂)         │
│ σ_dname='Toy'                │  │ σ_dname='Toy'                    │
│   │  ← 2,000r+4w (đọc T₁)   │  │   │  ← 2,000r+4w (đọc T₁,ghi T₂│
│ σ_Emp.did=Dept.did           │  │ ⋈ Sort-Merge Join                │
│   │  ← 1M reads (đọc T₁)    │  │   │  ← 3,150r+2,000w (ghi T₁)   │
│ × Cross Product              │  │   ├── Emp  (1,000 pages)         │
│   │  ← 50K reads+1M writes   │  │   └── Dept (50 pages)            │
│   ├── Emp                    │  │ Tổng: 7,159 I/Os                 │
│   └── Dept                   │  │                                  │
│ Tổng: ~2,000,000 I/Os        │  │                                  │
└──────────────────────────────┘  └──────────────────────────────────┘

┌─────────── Plan 2b 🔵 ──────────┐  ┌─────────── Plan 3 🟢 ───────────┐
│ π_ename                         │  │ π_ename                          │
│   │  ← PIPELINE (không ghi)     │  │   │  ← 4r+1w  (đọc T₂)          │
│ σ_dname='Toy'                   │  │ ⋈ Index NL Join                  │
│   │  ← PIPELINE (T₂ bị loại)   │  │   │  ← 24r+4w                    │
│ ⋈ Sort-Merge Join               │  │   │  (1+3 idx + 20 ptr chase)     │
│   │  ← 3,150r+2,000w (ghi T₁)  │  │   ├── σ_dname='Toy'              │
│   ├── Emp  (1,000 pages)        │  │   │    Access: Index(dname)       │
│   └── Dept (50 pages)           │  │   │    ← 3r+1w                   │
│ Tổng: 3,151 I/Os                │  │   │    └── Dept                   │
│ (T₂ không cần ghi nhờ pipeline) │  │   └── Emp                        │
│                                 │  │ Tổng: ~37 I/Os                   │
└─────────────────────────────────┘  └──────────────────────────────────┘
```

> **Bài học của Optimizer**: Cùng 1 query, optimizer tìm plan tốt hơn **~50,000 lần** (2M → 37 I/Os) nhờ 3 kỹ thuật: ① chọn đúng **thuật toán join** → ② tận dụng **pipeline** để loại temp file → ③ **pushdown selection + dùng index** thay seq scan.

---

**🔴 Plan 2a — Sort-Merge Join + Materialization Model (7,159 I/Os)**

```
π_ename                              ← 4 reads          (đọc T₂)
    │
σ_dname='Toy'                        ← 2,000 reads + 4 writes  (đọc T₁, ghi T₂)
    │
⋈  Sort-Merge Join                   ← 3,150 reads + 2,000 writes  (ghi T₁)
(Emp.did = Dept.did, 50 buffers)
   ├── Emp   (1,000 pages)
   └── Dept  (50 pages)
```

Chi phí:
```
Sort-Merge Join : 3,150 reads + 2,000 writes   → ghi toàn bộ T₁ ra disk
σ_dname='Toy'  : 2,000 reads + 4 writes        → đọc T₁, ghi T₂ ra disk (No Pipeline!)
π_ename        : 4 reads                        → đọc T₂
─────────────────────────────────────────────────
Tổng: 7,159 I/Os
```

---

**🔵 Plan 2b — Sort-Merge Join + Pipeline Model (3,151 I/Os)**

```
π_ename                              ← pipeline (không ghi disk)
    │
σ_dname='Toy'                        ← pipeline (không ghi T₂ ra disk)
    │
⋈  Sort-Merge Join                   ← 3,150 reads + 2,000 writes  (ghi T₁)
(Emp.did = Dept.did, 50 buffers)
   ├── Emp   (1,000 pages)
   └── Dept  (50 pages)
```

Chi phí:
```
Sort-Merge Join : 3,150 reads + 2,000 writes   → ghi T₁ ra disk
σ + π           : ~1 I/O                        → stream trực tiếp, không có T₂
─────────────────────────────────────────────────
Tổng: 3,151 I/Os
```

> **Khác biệt 2a vs 2b**: Pipelining loại bỏ hoàn toàn chi phí đọc/ghi T₂ trung gian (2,004 I/Os).

---

**🟢 Plan 3 — Index Nested-Loop Join + Index Scan (37 I/Os)**

Optimizer nhận ra: `Dept` có index trên `dname`, `Emp` có index trên `did` → dùng index thay vì scan toàn bộ bảng.

```
π_ename                              ← 4 reads + 1 write  (đọc T₂)
    │
⋈  Index Nested-Loop Join            ← 1 + 3(idx) + 20(ptr chase) reads + 4 writes
   (Emp.did = Dept.did)                 Với mỗi Dept tuple → lookup Emp qua index(did)
   │
   ├── σ_dname='Toy'                 ← 3 reads + 1 write
   │    Access: Index(dname)            Dùng unclustered index trên Dept.dname
   │    └── Dept
   │
   └── Emp
```

Chi phí:
```
σ_dname='Toy' via Index(dname):
  1 (root) + 2 (leaf) = 3 reads + 1 write (ghi 1 Dept tuple phù hợp)

Index NL Join — với 1 Dept tuple khớp, lookup 20 Emp tương ứng:
  1 (index root) + 2 (index leaf) + 20 (ptr chase → Emp pages) + 4 writes (ghi T₂)
  = 24 reads + 4 writes

π_ename (đọc T₂): 4 reads + 1 write
─────────────────────────────────────────────────
Tổng: ~37 I/Os
```

---

**Tổng kết so sánh 4 plan:**

| Plan | Thuật toán | Model | Tổng I/Os |
|---|---|---|---|
| **Plan 1** | Cross Product + Filter | Materialization | ≈ 2,000,000 |
| **Plan 2a** | Sort-Merge Join | Materialization (No Pipeline) | 7,159 |
| **Plan 2b** | Sort-Merge Join | Vectorization (Pipeline) | 3,151 |
| **Plan 3** | Index NL Join + Index Scan | Materialization | **37** |


#### 14.2. Query Optimizers — Tổng quan

Query Optimizer nhận đầu vào là **Logical Plan** và xuất ra một **Physical Execution Plan** tương đương ngữ nghĩa nhưng có chi phí thấp nhất có thể.

```
Logical Plan (đại số quan hệ)
        │
        ▼
   [Optimizer]   ← 3 thành phần: Transformations + Search Algo + Cost Model
        │
        ▼
Physical Execution Plan (access path cụ thể)
```

**Đặc điểm của Physical Operator:**
- Định nghĩa kế hoạch thực thi thông qua 1 access path cụ thể
- Có thể phụ thuộc vào format dữ liệu vật lý (row-store vs column-store)
- **Không phải** 1-to-1 mapping từ logical operator → physical operator (một logical operator có thể có nhiều physical operator tương ứng, ví dụ: Join → Hash Join / Sort-Merge Join / Nested Loop Join)

**2 Kiến trúc Optimizer:**

| Kiến trúc | Mô tả | Ghi chú |
|---|---|---|
| **Single Query** | Tối ưu từng truy vấn một, không chia sẻ giữa các query | Phổ biến trong hầu hết DBMS hiện nay |
| **Multi Query** | Tối ưu hóa nhiều truy vấn cùng lúc, có thể chia sẻ plan | Ít phổ biến hơn, dùng khi workload có nhiều query tương đồng |

---

#### 14.3. 3 Thành phần của Optimizer

Optimizer được xây dựng dựa trên 3 thành phần phối hợp với nhau:

```
┌─────────────────────────────────────────────────────────┐
│                     OPTIMIZER                           │
│                                                         │
│  ┌─────────────────┐  ┌──────────────┐  ┌───────────┐  │
│  │ Transformations │→ │ Search Algo  │→ │ Cost Model│  │
│  │  (Liệt kê plan) │  │ (Tìm plan)   │  │(Ước lượng)│  │
│  └─────────────────┘  └──────────────┘  └───────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

#### 14.3.1. Transformations (Phép biến đổi)

**Mục tiêu**: Liệt kê tất cả các hình thức khác nhau của một query plan mà vẫn **đảm bảo tính tương đương ngữ nghĩa** (semantically equivalent) dựa trên mô hình đại số quan hệ.

> 💡 Đây cũng là cơ chế mà **Heuristic Optimizer** dùng để xác định query plan mà không cần đến cost model — nhờ đại số quan hệ đảm bảo tính đúng đắn của các phép biến đổi.

**Các phép biến đổi phổ biến:**

| Operator | Phép biến đổi |
|---|---|
| **Selection (σ)** | Pushdown filter — thực thi filter sớm nhất có thể; tách điều kiện phức tạp thành các mệnh đề AND riêng lẻ và đẩy xuống từng node |
| **Join (⋈)** | Có tính **giao hoán** (A⋈B = B⋈A) và **kết hợp** ((A⋈B)⋈C = A⋈(B⋈C)) → số lượng thứ tự join của n bảng = **n! × C(n-1)** (C là số Catalan), không gian tìm kiếm cực lớn |

**Quy tắc giới hạn không gian tìm kiếm:**

| Quy tắc | Mô tả |
|---|---|
| **Split conjunctive predicates** | Chia điều kiện phức tạp thành dạng đơn giản nhất để optimizer dễ di chuyển trong plan |
| **Replace Cartesian product** | Thay thế tích đề-các (×) bằng Join tương ứng |
| **Projection pushdown** | Đưa phép chiếu (π) xuống sớm nhất có thể để giảm số cột cần materialize ở các bước trung gian |

> 📌 **Thực tế**: SQL Server được cho là có ~4,500 transformation rules (nguồn: bài giảng CMU 15-445/721, con số chính xác không được Microsoft công bố chính thức). Ngoài ra, một số DBMS hiện đại đang thử nghiệm thêm rules mới được học bởi AI.
> PostgreSQL không hỗ trợ hint trực tiếp — cần dùng extension `pg_hint_plan`.

---

#### 14.3.2. Search Algorithm

**Nhiệm vụ**: Với tập rules đã định nghĩa, optimizer duyệt không gian kế hoạch để tìm ra plan tốt nhất.

**Lưu ý**: Đôi khi optimizer không có đầy đủ thông tin tại thời điểm tối ưu, ví dụ:
- **Prepared statement**: chưa biết tham số cụ thể
- **Thiếu thông tin phân bố dữ liệu**: statistics chưa được cập nhật

**Hai kiến trúc Search:**

##### A. Heuristic-based Search

| Đặc điểm | Chi tiết |
|---|---|
| Dùng cost model? | ❌ Không |
| Tốc độ | ✅ Nhanh — áp dụng các rule có sẵn theo thứ tự cố định |
| Ứng dụng | Nhiều DBMS mới, các hệ thống ưu tiên tốc độ planning |
| **Ưu điểm** | Dễ implement, dễ debug, dễ hiểu; nhanh với query đơn giản |
| **Nhược điểm** | Không tối ưu với query phức tạp; phụ thuộc vào "magic number" để đánh giá hiệu quả của một operator |

> ⚠️ **Lưu ý về MongoDB**: MongoDB **không thuộc** heuristic thuần hay cost-based truyền thống. Nó dùng cách tiếp cận **empirical/trial-based** riêng biệt — chạy thử song song các plan rồi chọn plan thắng cuộc (xem chi tiết tại [14.3.3 — Cost Model — Empirical](#2-empirical--trial-based)).

##### B. Cost-based Search

| Đặc điểm | Chi tiết |
|---|---|
| Dùng cost model? | ✅ Có |
| Ứng dụng | PostgreSQL, MySQL, Oracle, SQL Server |
| Cơ chế | Sinh plan → ước lượng cost (dựa trên cost model) → dùng cost để định hướng tìm kiếm. Nếu một plan quá "đắt", chuyển sang plan khác |

**Điều kiện dừng (Stopping Criteria):**

| Điều kiện | DBMS |
|---|---|
| **Wall clock time** | MySQL, PostgreSQL |
| **Cost threshold** | Khi plan hiện tại đã đủ rẻ |
| **Exhaustion** | Khi đã xét hết tất cả plan có thể |
| **Transformation count** | SQL Server — dừng sau N rules đã xem xét |

**Chiến lược duyệt không gian kế hoạch (Search Strategy):**

Trong Cost-based Search, có 2 chiến lược chính để duyệt không gian plan:

##### ① Bottom-Up / Forward Chaining

```
Xuất phát: các bảng gốc (leaf nodes)
Hướng:     từ dưới lên ↑
Duyệt:    Breadth-first Search

              ARTIST ⋈ APPEARS          ← kết quả cuối cùng
              /          \
         [Choice 1]  [Choice 2]  [Choice 3]    ← các phương án join
          /    \        /    \
     [C1] [C2]  [C1] [C2]  [C1] [C2]          ← các phương án scan
      │    │      │    │     │    │
    ARTIST       ARTIST    APPEARS             ← bảng gốc (BẮT ĐẦU TỪ ĐÂY ↑)
```

- Bắt đầu từ **các bảng gốc** (base relations), áp dụng tất cả rules có thể, tạo ra các plan con.
- Lưu plan con tốt nhất cho mỗi subset bảng (Dynamic Programming), rồi mở rộng dần lên.
- Nhờ DP, tránh tính lại các sub-plan đã xét → **tối ưu toàn cục** (global optimal).
- Framework đại diện: **IBM System R** (1979) — optimizer có ảnh hưởng lớn nhất lịch sử DB.

| Đặc điểm | Chi tiết |
|---|---|
| Thuật toán | **Dynamic Programming** — lưu plan tốt nhất cho mỗi subset |
| Chiến lược duyệt | Breadth-first (mở rộng theo tầng) |
| DBMS sử dụng | PostgreSQL, MySQL, DB2, SQLite |
| **Ưu điểm** | Đảm bảo tìm plan tối ưu toàn cục (nếu duyệt hết); dễ hiểu; ổn định |
| **Nhược điểm** | Tốn bộ nhớ lớn (lưu mọi sub-plan); khó mở rộng khi thêm operator/rule mới; khó cắt nhánh (pruning) sớm |

**Chi tiết cách DP hoạt động trong Bottom-Up Optimizer (System R):**

Ý tưởng cốt lõi: **Bài toán con tối ưu** — plan tốt nhất cho `{A, B, C}` chắc chắn được xây từ plan tốt nhất cho `{A, B}` hoặc `{A, C}` hoặc `{B, C}` kết hợp với bảng còn lại. Nên chỉ cần lưu plan tốt nhất cho mỗi subset, rồi build lên.

```
Ví dụ: Query JOIN 3 bảng A, B, C

═══════════════════════════════════════════════════════════
Pass 1 — Xét từng bảng đơn lẻ (subset kích thước 1)
═══════════════════════════════════════════════════════════
  Với mỗi bảng, tìm access path tốt nhất:

  best_plan[{A}] = Seq Scan A        (cost: 1000)
                 vs Index Scan A(idx) (cost: 50)   ← CHỌN ✓

  best_plan[{B}] = Seq Scan B        (cost: 500)   ← CHỌN ✓
                 vs Index Scan B      (cost: 600)

  best_plan[{C}] = Seq Scan C        (cost: 200)   ← CHỌN ✓

═══════════════════════════════════════════════════════════
Pass 2 — Xét tất cả cặp 2 bảng (subset kích thước 2)
═══════════════════════════════════════════════════════════
  Với mỗi cặp, thử TẤT CẢ cách join + TẤT CẢ thuật toán:

  best_plan[{A,B}] = ?
    Thử: best_plan[{A}] ⋈ best_plan[{B}]
         → Hash Join(A,B)           cost: 50+500+200  = 750
         → Sort-Merge Join(A,B)     cost: 50+500+400  = 950
         → Nested Loop(A→B)         cost: 50+50×500   = 25050
    Thử: best_plan[{B}] ⋈ best_plan[{A}]  (đảo thứ tự)
         → Hash Join(B,A)           cost: 500+50+180  = 730  ← CHỌN ✓
    ...

  best_plan[{A,C}] = Hash Join(C,A)  (cost: 400)     ← CHỌN ✓
  best_plan[{B,C}] = Sort-Merge(B,C) (cost: 900)     ← CHỌN ✓

═══════════════════════════════════════════════════════════
Pass 3 — Xét bộ 3 bảng (subset kích thước 3) → KẾT QUẢ
═══════════════════════════════════════════════════════════
  best_plan[{A,B,C}] = ?
    Thử: best_plan[{A,B}] ⋈ best_plan[{C}]
         → Hash Join cost: 730+200+X
    Thử: best_plan[{A,C}] ⋈ best_plan[{B}]
         → Hash Join cost: 400+500+X              ← CHỌN ✓
    Thử: best_plan[{B,C}] ⋈ best_plan[{A}]
         → Hash Join cost: 900+50+X

  → Plan cuối cùng: Hash Join( Hash Join(C,A), B )
```

**Khái niệm then chốt — "Interesting Orders":**

Nếu chỉ giữ plan rẻ nhất cho mỗi subset thì có thể bỏ lỡ plan tối ưu toàn cục. Ví dụ:

```
best_plan[{A,B}]:
  Plan 1: Hash Join(B,A)         cost: 730   output: KHÔNG SORTED
  Plan 2: Sort-Merge Join(A,B)   cost: 950   output: SORTED theo A.id  ← đắt hơn!

Nếu chỉ giữ Plan 1 (rẻ hơn), nhưng Pass 3 cần ORDER BY A.id:
  → Plan 1 + Sort  = 730 + 500 = 1230
  → Plan 2 (no sort) = 950          ← RẺ HƠN NHIỀU!
```

→ System R giải quyết bằng cách giữ **nhiều plan cho mỗi subset**: một plan rẻ nhất **tuyệt đối**, và thêm plan rẻ nhất cho mỗi **interesting order** (thứ tự output hữu ích cho ORDER BY, GROUP BY, hoặc join condition phía trên).

```
DP Table mở rộng:
┌──────────┬───────────────────────────────────────────────┐
│ Subset   │ Plans được giữ                                │
├──────────┼───────────────────────────────────────────────┤
│ {A}      │ cheapest: IdxScan(A) cost=50                  │
│          │ sorted(A.id): IdxScan(A) cost=50 (miễn phí!)  │
├──────────┼───────────────────────────────────────────────┤
│ {A,B}    │ cheapest: Hash Join(B,A) cost=730              │
│          │ sorted(A.id): SMJ(A,B) cost=950               │
├──────────┼───────────────────────────────────────────────┤
│ {A,B,C}  │ cheapest: dùng sorted(A.id) plan → tổng rẻ hơn│
└──────────┴───────────────────────────────────────────────┘
```

> 📌 **Tóm lại**: DP trong Bottom-Up = liệt kê plan theo **kích thước subset tăng dần** (1 bảng → 2 bảng → ... → n bảng). Tại mỗi bước, chỉ giữ plan tốt nhất (+ interesting orders) cho mỗi subset. Nhờ vậy Pass k chỉ cần tổ hợp kết quả từ Pass k-1 mà không cần tính lại từ đầu.

##### ② Top-Down / Backward Chaining

```
Xuất phát: kết quả truy vấn mong muốn (root)
Hướng:     từ trên xuống ↓
Duyệt:    Depth-first Search

    ARTIST ⋈ APPEARS             ← BẮT ĐẦU TỪ ĐÂY ↓ (goal)
         │
      [Choice 1]                  ← chọn thuật toán join
         │
      [Choice 1]  [Choice 2]     ← chọn cách scan bảng trái
         │
       ARTIST                     ← đến leaf → quay lại xét nhánh khác
```

- Bắt đầu từ **kết quả mong muốn** (logical plan gốc), phân rã ngược xuống để xác định operator cần thiết.
- Dùng **Memoization** (Memo Table) để ghi nhớ các sub-expression đã tối ưu, tránh tính lại.
- Có thể **cắt nhánh sớm** (branch-and-bound): khi đã có upper bound cost, bỏ qua toàn bộ nhánh đắt hơn → nhanh hơn Bottom-Up trên query phức tạp.
- Các thuộc tính vật lý được truyền xuống để đưa vào quyết định(khác biệt lớn so với Bottom-Up)
- Framework đại diện: **Volcano** (1993) → **Cascades** (1995, Goetz Graefe) — nền tảng của SQL Server.
- Cách SQL server thực hiện được mô tả trong cuốn: Extensible query optimizer in paractive của MS

| Đặc điểm | Chi tiết |
|---|---|
| Thuật toán | **Memoization** + Branch-and-bound pruning |
| Chiến lược duyệt | Depth-first (đi sâu rồi quay lại) |
| DBMS sử dụng | SQL Server (Cascades), CockroachDB, Greenplum |
| **Ưu điểm** | Dễ mở rộng (thêm rule/operator mới dễ dàng); cắt nhánh sớm hiệu quả; tiết kiệm bộ nhớ hơn (chỉ lưu nhánh đang xét) |
| **Nhược điểm** | Cài đặt phức tạp hơn; không đảm bảo tối ưu toàn cục nếu pruning quá mạnh; khó debug |

**So sánh tổng hợp:**

| | Bottom-Up (Forward Chaining) | Top-Down (Backward Chaining) |
|---|---|---|
| **Xuất phát** | Bảng gốc (leaf) → build lên | Kết quả (root) → decompose xuống |
| **Duyệt** | Breadth-first | Depth-first |
| **Kỹ thuật** | Dynamic Programming | Memoization + Branch-and-bound |
| **Pruning** | Khó cắt nhánh sớm | ✅ Cắt nhánh hiệu quả nhờ upper/lower bound |
| **Mở rộng** | Khó — thêm rule cần sửa code DP | ✅ Dễ — chỉ cần thêm rule object |
| **Đại diện** | System R → PostgreSQL, MySQL | Volcano → Cascades → SQL Server |

---

##### D. Access Path Transformation

Lựa chọn **access path** cho từng table sao cho tổng chi phí là nhỏ nhất. Chi phí phụ thuộc vào:

| Yếu tố | Mô tả |
|---|---|
| **Độ chọn lọc (Selectivity)** | Filter càng chọn lọc, index càng có lợi |
| **Cấu trúc index** | B+Tree cho range query; Hash index cho point lookup (highly selective) |
| **Sort order** | Index có thể tránh sort bổ sung nếu thứ tự phù hợp ORDER BY |
| **Data Accoutrements** | **Include columns**: cột đính kèm vào index (không phải khóa) — tránh table lookup; **Zone map**: lưu min/max của block — cho phép block skipping |
| **Compression/Encoding** | Ảnh hưởng đến số bytes đọc từ disk |

---

#### 14.3.3. Cost Model

**Nhiệm vụ**: Ước lượng chi phí của một plan để optimizer có thể so sánh và chọn plan tốt nhất.

##### A. Hai thành phần của Cost

| Thành phần | Mô tả |
|---|---|
| **Physical Cost** | Chi phí thực tế trên phần cứng: I/O, CPU cycles, memory usage. PostgreSQL sử dụng các hằng số có thể tuỳ chỉnh (`seq_page_cost`, `cpu_tuple_cost`...) để ước lượng. Oracle, SQL Server, DB2 sử dụng các kỹ thuật nội bộ riêng |
| **Logical Cost** | Ước tính **Cardinality** (số lượng dòng/bản ghi) kết quả của mỗi toán tử. Độc lập với thuật toán — chỉ quan tâm đến số lượng bản ghi × trọng số, không quan trọng sử dụng thuật toán nào |

##### B. Hai trường phái Cost Model

###### 1. Statistics-based (Cost-Based Optimizer — CBO)

**Ứng dụng**: PostgreSQL, MySQL, Oracle, SQL Server

| Đặc điểm | Chi tiết |
|---|---|
| Cơ chế | Duy trì **statistics** về dữ liệu (histogram, cardinality, data distribution) và ước lượng cost **trước khi chạy** |
| **Ưu điểm** | Không tốn I/O để chọn plan; nhanh và chính xác khi statistics còn tươi |
| **Nhược điểm** | Statistics bị **stale** khi data thay đổi nhanh → chọn plan sai; tốn dung lượng lưu trữ |
| Maintenance | Background task (PostgreSQL auto vacuum) / Schedule (Oracle) / Thresholds / Manual. Cần chạy định kỳ: `ANALYZE` (PostgreSQL) / `UPDATE STATISTICS` (SQL Server) |

**a) Statistic Storage**

- **Table-level statistics**: Thông tin tổng quan về bảng (số dòng, số page, kích thước trung bình tuple...)

- **Column statistics**:
   - Thông thường DBMS tạo phân tích **độc lập trên từng cột**, điều này đôi khi dẫn đến sai lệch khi thực hiện query trên nhiều cột có sự tương quan (correlated columns)
   - Một số hệ thống tự động tạo statistics trên **nhiều cột** nếu chúng nằm trong 1 compound index (MySQL)
   - Một số hệ thống cho phép chỉ định (manual) tạo statistics trên nhiều cột (Oracle, DB2)

**b) Estimation Techniques (Kỹ thuật ước lượng)**

- **Histogram**: Thống kê tần suất xuất hiện của từng giá trị trong 1 column (được sử dụng nhiều nhất)
   - Để giảm kích thước, sử dụng **Bucket** — nhóm nhiều giá trị thành từng nhóm:

      | Loại Bucket | Mô tả |
      |---|---|
      | **Equi-width** | Các bucket có cùng chiều rộng (range) |
      | **Equi-depth** | Các bucket có số lượng phần tử tương đồng nhau |
      | **End-biased** | Chỉ lưu riêng các giá trị xuất hiện nhiều nhất (most frequent values), còn lại nhóm hết vào 1 bucket |

- **Sketches**: Cấu trúc dữ liệu xác suất để ước lượng nhanh (được sử dụng nhiều trong các hệ thống phân tán)
   - Frequent items — **Count-Min Sketch**: Ước lượng tần suất xuất hiện của từng phần tử
   - Count Distinct — **HyperLogLog**: Ước lượng số giá trị duy nhất (distinct values)
   - Quantiles — **t-digest**: Ước lượng percentile / phân vị

- **Sampling**: Lấy mẫu dữ liệu để ước lượng cost
   - **Maintain Read-Only Copy** (Duy trì bản sao chỉ đọc): DBMS lấy ra khoảng 1% các dòng ngẫu nhiên từ bảng chính và tạo thành một bảng phụ gọi là Sample Table
   - **Sample Real Tables** (Lấy mẫu trực tiếp trên bảng thực): Mỗi khi cần tối ưu hoá, DBMS đọc ngẫu nhiên một vài trang dữ liệu (pages/blocks) trực tiếp từ bảng gốc trên đĩa

- **ML Model**: Sử dụng Machine Learning để dự đoán cost (mới chỉ đang thử nghiệm)

---

##### 2. Empirical / Trial-based

**Ứng dụng**: MongoDB

MongoDB **không dùng statistics**. Thay vào đó dùng cơ chế **"First Past the Post" (FPTP)**:

```
Bước 1: Candidate Plan Generation
         → Sinh ra tất cả possible plans từ các index có sẵn
              ↓
Bước 2: Trial Period (Racing)
         → Chạy SONG SONG tất cả candidate plans trong trial ngắn
              ↓
Bước 3: Empirical Measurement
         → Đo "Works" score: số index key scan + số doc fetch + stage resources
              ↓
Bước 4: Winner Selection
         → Plan trả về 101 docs đầu tiên với ít "Works" nhất → THẮNG
              ↓
Bước 5: Plan Caching
         → Cache winning plan theo query shape (không phân biệt giá trị cụ thể)
```

> ⚠️ **Lưu ý**: Không phải chạy thử **toàn bộ** query — chỉ chạy đến khi đủ 101 documents (trial period rất ngắn).

| | Ưu điểm | Nhược điểm |
|---|---|---|
| **Statistics-based** | Không tốn I/O để chọn plan | Statistics stale → plan sai |
| **Empirical (MongoDB)** | Không cần `ANALYZE`; tự thích nghi với data thay đổi; phù hợp schema-less NoSQL | Preference bias (ưu tiên index scan, sai với dataset nhỏ); plan cache stale; racing overhead khi gặp query shape mới |

> 🔍 **Debug MongoDB plans**: Dùng `.explain("allPlansExecution")` để xem "Works" score của tất cả candidate plans.

---

##### C. Cardinality Estimation
- Ước lượng số lượng record mà mỗi toán tử sẽ phải thực hiện(select, join, distinct, ...)

Derivable statistic:
   - Với mỗi relation(R1, R2, ...)
      - N(R): số lượng tuple
      - V(A, R): số lượng giá trị duy nhất trong cột A
      - Selection of A: SC(A,R):  Số lượng trung bình các tuple có cùng giá trị trong cột A = N(R) / V(R, A)
      - Độ chọn lọc ($sel$) của một vị ngữ $P$ (điều kiện lọc) là tỷ lệ các bản ghi (tuples) thỏa mãn điều kiện đó. 
        - Vị ngữ so sánh bằng (Equality Predicate): $A = \text{constant}$Công thức: $sel(A = \text{constant}) = \frac{\text{\#occurrences}}{|R|}$
        - Vị ngữ so sánh nghịch đảo: sel(not P) = 1 - sel(P)
        - Khi combine nhiều điều kiện thì sao: -> Trong đại bộ phận các mô hình giả định phân phối là độc lập và phân phối đều
          -Công thức toán học thuần túy giả định các cột độc lập (Independent):$$sel(P1 \wedge P2 \wedge P3 \wedge P4) = sel(P1) \times sel(P2) \times sel(P3) \times sel(P4)$$: càng nhiều điều kiện thì xác suất càng nhỏ
          - Optimization: Ví dụ trong sql server họ giảm hệ số ở các điều kiện tiếp theo để hệ số không còn quá nhỏ
      - Join size estimation:
         Join 2 table R và S -> số lượng tuple trong kết quả join
         - Giả định: Các giá trị trong cột A của R và cột B của S có giá trị bằng nhau
         - Công thức: $$N(R \Join S) = \frac{N(R) \times N(S)}{max(|V(A,S)|, |V(A,R|)}$$
      - Được xây dựng trên các giả định, vì vậy hiện tượng lan truyền lỗi
      - AQP(adaptive query processing) SQL server: nếu vượt quá giá trị mong đợi, ta sẽ break chúng để lên lại plan mới
      - 1 bài báo(2015) cho thấy sql server với việc sử dụng sampling cho kết quả estimate tốt nhất
   - Với mỗi join(R, S)
      - N(R ⋈ S): số lượng tuple trong kết quả join

### 15. Transaction

#### 15.1. ACID

- **Atomicity** (Tính nguyên tử): Đảm bảo "tất cả hoặc không có gì xảy ra"
   - Logging: Ghi tất cả các action của transaction vào log (WAL — Write-Ahead Log)
   - Shadow paging: Tạo 1 bản sao → thành công thì chuyển sang dùng bản sao

- **Consistency** (Tính nhất quán): Đảm bảo transaction đưa database từ trạng thái hợp lệ này sang trạng thái hợp lệ khác
   - Đảm bảo các ràng buộc (FK, CHECK, UNIQUE, NOT NULL...)
   - Lưu ý: "Eventual Consistency" là khái niệm của distributed systems (CAP theorem), **không phải** ACID Consistency. ACID Consistency chỉ nói về tính toàn vẹn dữ liệu trong phạm vi 1 database instance.

- **Isolation** (Tính cô lập): Đảm bảo các transaction đồng thời không ảnh hưởng đến nhau
   - Cơ chế quản lý nhiều transaction cùng lúc
   - Cần đảm bảo tính tuần tự (serializability)
   - **Serial Schedule** (Lịch trình nối tiếp): Chạy lần lượt từng transaction, luôn đúng nhưng kém hiệu năng
   - **Equivalent Schedule** (Lịch trình tương đương):
      - Chạy song song nhiều transaction
      - Đảm bảo kết quả tương đương với việc chạy tuần tự
   - **Conflict serializability**: Để đảm bảo tính tuần tự, ta cần xác định conflict
      - 2 operation được gọi là **conflict** nếu chúng cùng truy cập vào 1 tài nguyên và ít nhất 1 trong 2 là write
      - Xác định conflict bằng **dependency graph**:
         - Nếu có cycle → **không phải** conflict serializable
         - Nếu không có cycle → **là** conflict serializable
   - **Concurrency Anomalies** (Các hiện tượng bất thường khi chạy đồng thời):
      - **Dirty read**: T1 đọc dữ liệu mà T2 đã write nhưng chưa commit. Nếu T2 rollback, T1 đang dùng dữ liệu không bao giờ tồn tại.
      - **Unrepeatable read** (Non-repeatable read): T1 đọc một dòng, rồi T2 update/delete dòng đó và commit, sau đó T1 đọc lại và nhận được giá trị khác.
      - **Lost update**: T1 đọc dữ liệu, T2 đọc cùng dữ liệu, T1 update, T2 update → cập nhật của T1 bị mất.
      - **Phantom read**: T1 đọc một tập kết quả (range scan), T2 insert dòng mới vào range đó, T1 đọc lại → nhận được thêm dòng mới.
      - **Write-skew**: T1 và T2 cùng đọc một điều kiện chung, mỗi transaction write vào dòng khác nhau (nên không bị conflict trực tiếp), nhưng tổng thể vi phạm invariant của hệ thống.
        - **Đặc điểm**: Row-level lock và Snapshot Isolation **KHÔNG** bắt được — vì 2 txn ghi vào 2 row khác nhau → không có write-write conflict. Chỉ **Serializable** mới ngăn được.

        **Ví dụ 1 — Marble Color Swap (CMU 15-445):**
        ```
        Ban đầu: 2 viên đen (⚫⚫), 2 viên trắng (⚪⚪)

        Txn #1: "Đổi tất cả viên TRẮNG → ĐEN"
        Txn #2: "Đổi tất cả viên ĐEN → TRẮNG"

        Nếu chạy tuần tự (serial):
          T1 trước → ⚫⚫⚫⚫ → T2 sau → ⚪⚪⚪⚪  (hoặc ngược lại)
          → Kết quả: tất cả cùng 1 màu ✅

        Nếu chạy Snapshot Isolation (cả 2 đọc cùng snapshot ban đầu):
          T1 đọc snapshot: thấy ⚪⚪ → đổi thành ⚫⚫  (ghi vào row 3, 4)
          T2 đọc snapshot: thấy ⚫⚫ → đổi thành ⚪⚪  (ghi vào row 1, 2)
          → Cả 2 commit thành công (ghi vào row khác nhau, không conflict)
          → Kết quả: ⚪⚪⚫⚫ — 2 màu bị HOÁN ĐỔI thay vì cùng 1 màu 💥
          → Không tương đương bất kỳ serial schedule nào → WRITE SKEW!
        ```

        **Ví dụ 2 — Bác sĩ trực ca:**
        ```
        Invariant: Luôn phải có ≥ 1 bác sĩ trực ca
        Ban đầu: Alice (on_call=true), Bob (on_call=true) → 2 người trực

        T1 (Alice muốn nghỉ):                   T2 (Bob muốn nghỉ):
        ──────────────────────────────────────────────────────────────
        BEGIN                                     BEGIN
        SELECT COUNT(*) WHERE on_call=true        SELECT COUNT(*) WHERE on_call=true
        → Đếm được 2 (≥1, OK để nghỉ)           → Đếm được 2 (≥1, OK để nghỉ)

        UPDATE SET on_call=false                  UPDATE SET on_call=false
          WHERE doctor='Alice'                      WHERE doctor='Bob'
          ↑ ghi row ALICE                           ↑ ghi row BOB (khác row!)

        COMMIT ✅                                 COMMIT ✅
        → 0 bác sĩ trực → VI PHẠM INVARIANT! 💥
        ```

- **Durability** (Tính bền vững): Đảm bảo kết quả của transaction đã commit được lưu trữ vĩnh viễn, kể cả khi hệ thống crash.

---

#### 15.2. Concurrency Control

Các transaction liên tục xảy ra, schedule cần đảm bảo tính tuần tự (serializability) theo thời gian thực.

##### 15.2.1. Two-Phase Locking (2PL)

- 2PL là kỹ thuật quản lý concurrency control bằng cách sử dụng lock
- 2PL chia transaction thành 2 phase:
   - **Growing phase** (Expanding phase): Transaction **chỉ được acquire lock**, không được release bất kỳ lock nào
   - **Shrinking phase**: Transaction **chỉ được release lock**, không được acquire thêm lock mới
   - **Lock point**: Thời điểm transaction acquire lock cuối cùng (đỉnh của growing phase) — thứ tự các lock point xác định thứ tự serial tương đương
- 2PL đảm bảo tính tuần tự (serializability) nhưng có thể gây ra **deadlock** và **dirty read** (khi rollback trong shrinking phase)

**Biến thể:**
- **Strict 2PL (S2PL)**: Chỉ release **write lock** sau khi transaction kết thúc (commit/abort) → ngăn dirty read
- **Rigorous 2PL**: Chỉ release **tất cả lock** sau khi transaction kết thúc → đơn giản hơn cho implementation

---

###### A. Deadlock Detection

- Sử dụng **wait-for graph**: Mỗi node là 1 transaction, edge T1→T2 nghĩa là T1 đang chờ lock mà T2 đang giữ
- Nếu có cycle → deadlock → Kill 1 victim để loại bỏ deadlock
   - **Victim selection**:
      - By age (transaction trẻ nhất)
      - By progress (ít progress nhất)
      - By the # of items already locked
      - By the # of transactions that will need to rollback
   - **Rollback length** — how far to rollback the txn changes:
      - Toàn bộ (Complete rollback)
      - Partial rollback: Chỉ rollback đến savepoint gần nhất đủ để phá cycle, giữ lại phần đã làm trước đó
- Tần suất check và thời gian chờ là trade-off

###### B. Deadlock Prevention

- Khi 1 txn request tài nguyên đã bị lock bởi txn khác → kill 1 trong 2 để ngăn deadlock (không cần wait-for graph hay thuật toán detect)
- Độ ưu tiên: **Older = Higher priority**
- **Wait-Die** (non-preemptive): Nếu T1 request tài nguyên bị lock bởi T2:
   - T1 cũ hơn T2 → T1 **chờ**
   - T1 trẻ hơn T2 → T1 **rollback** (die)
- **Wound-Wait** (preemptive): Nếu T1 request tài nguyên bị lock bởi T2:
   - T1 **cũ hơn** T2 → T1 "wounds" T2: **T2 bị abort/rollback**, T1 tiếp tục
   - T1 **trẻ hơn** T2 → T1 **chờ** (wait)
- Tóm tắt so sánh:

| | T1 cũ hơn T2 | T1 trẻ hơn T2 |
|---|---|---|
| **Wait-Die** | T1 chờ | T1 abort (die) |
| **Wound-Wait** | T2 abort (bị T1 wound) | T1 chờ |

---

###### C. Lock Granularity (Mức độ chi tiết của lock)

- Bản thân việc lock tốn tài nguyên hơn nhiều so với latch
- **Scopes**: Attribute, tuple, page, table, database
- Ví dụ: MongoDB trước v3.0 (engine MMAPv1) chỉ có database-level lock. Từ MongoDB 3.0+ (WiredTiger engine) đã hỗ trợ **document-level concurrency control** (dùng intention lock ở collection/database level).

###### D. Intention Lock

Intention lock cho phép lock ở mức cao (table) mà vẫn biết có lock ở mức thấp (tuple):

- **IS** (Intention Shared): Tôi sẽ đặt S lock ở node con
- **IX** (Intention Exclusive): Tôi sẽ đặt X lock ở node con
- **SIX** (Shared + Intention Exclusive): Đọc toàn bộ node hiện tại (S) + sẽ ghi vào một số node con (IX)

###### E. Lock Hint

Đôi khi application cần kiểm soát locking thủ công:

- `SELECT ... FOR UPDATE`: Acquire exclusive lock trên các row được select
- `SELECT ... FOR SHARE` / `LOCK IN SHARE MODE`: Acquire shared lock
- `SELECT ... FOR UPDATE SKIP LOCKED`: Skip qua các bản ghi đang bị lock — hữu dụng khi implement **job queue** trong DBMS (worker chỉ lấy row chưa bị lock, tránh chờ đợi)

---

##### 15.2.2. Optimistic Concurrency Control (OCC)

- **Giả định**: Xung đột hiếm khi xảy ra → không cần lock
- **3 giai đoạn**:
   1. **Read Phase**: Đọc dữ liệu, tính toán, ghi vào local workspace (private copy)
   2. **Validate Phase**: Kiểm tra xem có xung đột với transaction khác không
   3. **Write Phase**: Nếu validate thành công → ghi dữ liệu vào database
- **Ưu điểm**: Không cần lock, hiệu năng cao khi xung đột thấp
- **Nhược điểm**: Khi xung đột cao, nhiều transaction bị abort → lãng phí tài nguyên (copy data, chỉ abort khi đã hoàn thành gần xong)
- **Cơ chế validate**:
   1. **Backward validation**: Kiểm tra xung đột với các transaction đã commit
   2. **Forward validation**: Kiểm tra xung đột với các transaction đang chạy
      - Khi transaction bắt đầu, nó ghi lại tất cả các item đọc vào **ReadSet**
      - Khi chuẩn bị commit, kiểm tra xem bất kỳ item nào trong ReadSet đã bị thay đổi bởi transaction khác (chưa commit) hay không
      - Nếu có sự thay đổi → xung đột → transaction bị abort
      - Nếu không → transaction được commit

---

##### 15.2.3. Timestamp Ordering (T/O)

- Mỗi transaction được gán 1 **timestamp** khi bắt đầu
- Mỗi data item lưu 2 timestamp: **W-TS** (write timestamp) và **R-TS** (read timestamp)
- Khi transaction đọc/ghi, hệ thống kiểm tra timestamp để đảm bảo thứ tự tương đương serial
- Ưu điểm: Không cần lock, không deadlock
- Nhược điểm: Có thể phải cascade abort khi vi phạm thứ tự timestamp

---

##### 15.2.4. Phantom Read Prevention

Cả 2PL và OCC mặc định chỉ lock trên **1 đối tượng cụ thể** (tuple, page, table) → phantom read vẫn xảy ra khi có range scan.

**Giải pháp:**

1. **Re-scan**: Thực hiện lại scan để phát hiện phantom
   - Ví dụ: DynamoDB, Hekaton (SQL Server In-Memory OLTP)

2. **Predicate locking**: Khóa theo mệnh đề điều kiện (predicate)
   - Acquire shared/exclusive lock trên predicate (ví dụ: `age > 18`)
   - Các giao dịch khác phải kiểm tra xem operation của mình có conflict với predicate lock hay không
   - Nhược điểm: Khó implement vì cần kiểm tra overlap giữa các predicate

3. **Index locking**: Khóa dựa trên cấu trúc index
   - **Key locking**: Lock theo key cụ thể trên index
   - **Gap lock**: Lock khoảng trống giữa 2 key liên tiếp: ví dụ key 5 và 10 → lock gap (5, 10)
   - **Key-range locking** (Next-key lock): Lock cả gap và 1 key kế tiếp — cần virtual key để lock infinity
      - Ví dụ: lock [5, 10) → lock key 5 và gap giữa 5 và 10
   - **Hierarchical locking**: Cho phép lock trong range với nhiều mức lock khác nhau (IX lock)

---

##### 15.2.5. Isolation Levels

Phần lớn các DBMS hiện tại **không đảm bảo Serializable** mặc định do chi phí quá đắt đỏ. Thay vào đó, cung cấp nhiều mức isolation level:

| Isolation Level | Dirty Read | Unrepeatable Read | Phantom Read | Lost Update |
|---|---|---|---|---|
| **Read Uncommitted** | ⚠️ Có thể | ⚠️ Có thể | ⚠️ Có thể | ⚠️ Có thể |
| **Read Committed** | ✅ Không | ⚠️ Có thể | ⚠️ Có thể | ⚠️ Có thể |
| **Repeatable Read** | ✅ Không | ✅ Không | ⚠️ Có thể | ✅ Không |
| **Serializable** | ✅ Không | ✅ Không | ✅ Không | ✅ Không |

> 📌 Mặc định: PostgreSQL = **Read Committed**, MySQL (InnoDB) = **Repeatable Read**, SQL Server = **Read Committed**, Oracle = **Read Committed**.

**Chi tiết từng Isolation Level:**

###### 1. Read Uncommitted

- Mức cô lập **thấp nhất**: Transaction có thể đọc dữ liệu mà transaction khác đã write nhưng **chưa commit**.
- Hầu như không dùng lock khi đọc → hiệu năng cao nhất nhưng rủi ro lớn nhất.
- **Dirty read xảy ra**:

```
T1: BEGIN
T1: UPDATE accounts SET balance = 500 WHERE id = 1  -- (ban đầu balance = 1000)
                    T2: BEGIN
                    T2: SELECT balance FROM accounts WHERE id = 1
                    T2: → Đọc được 500 (dữ liệu CHƯA commit của T1)  ← DIRTY READ
T1: ROLLBACK       -- T1 hủy, balance trở về 1000
                    T2: -- Nhưng T2 đã dùng giá trị 500 → SAI!
```

- Ứng dụng: Rất hiếm khi dùng. Chỉ phù hợp cho các truy vấn thống kê xấp xỉ (approximate analytics) nơi sai lệch nhỏ chấp nhận được.

###### 2. Read Committed

- Transaction **chỉ đọc được dữ liệu đã commit** → ngăn dirty read.
- Cơ chế phổ biến:
   - **Lock-based**: Acquire shared lock khi đọc, release ngay sau khi đọc xong (không giữ đến cuối transaction)
   - **MVCC-based** (PostgreSQL, Oracle): Đọc **snapshot tại thời điểm câu lệnh** (statement-level snapshot) — mỗi câu SELECT thấy dữ liệu đã commit tính đến thời điểm câu SELECT đó bắt đầu
- **Dirty read KHÔNG xảy ra**, nhưng **Unrepeatable read vẫn xảy ra**:

```
T1: BEGIN
T1: SELECT balance FROM accounts WHERE id = 1
T1: → Đọc được 1000
                    T2: BEGIN
                    T2: UPDATE accounts SET balance = 500 WHERE id = 1
                    T2: COMMIT  -- T2 đã commit thành công
T1: SELECT balance FROM accounts WHERE id = 1
T1: → Đọc được 500 (khác lần đọc trước!)  ← UNREPEATABLE READ
T1: COMMIT
```

- Ứng dụng: Phù hợp cho hầu hết ứng dụng web thông thường nơi mỗi request là 1 transaction ngắn. Đây là mức mặc định của PostgreSQL, Oracle, SQL Server.

###### 3. Repeatable Read

- Đảm bảo **cùng 1 câu SELECT trong cùng 1 transaction luôn trả về cùng kết quả** cho các row đã đọc → ngăn unrepeatable read.
- Cơ chế phổ biến:
   - **Lock-based**: Giữ shared lock trên các row đã đọc cho đến khi transaction kết thúc
   - **MVCC-based** (MySQL InnoDB, PostgreSQL): Đọc **snapshot tại thời điểm transaction bắt đầu** (transaction-level snapshot) — toàn bộ các câu SELECT trong transaction đều thấy cùng 1 snapshot
- **Unrepeatable read KHÔNG xảy ra**, nhưng **Phantom read vẫn có thể xảy ra**:

```
T1: BEGIN
T1: SELECT * FROM employees WHERE dept = 'Engineering'
T1: → Trả về 10 rows
                    T2: BEGIN
                    T2: INSERT INTO employees (name, dept) VALUES ('New Guy', 'Engineering')
                    T2: COMMIT
T1: SELECT * FROM employees WHERE dept = 'Engineering'
T1: → Trả về 11 rows (xuất hiện thêm 1 row mới!)  ← PHANTOM READ
T1: -- 10 rows cũ vẫn giữ nguyên giá trị (no unrepeatable read)
T1: -- Nhưng có thêm 1 row "phantom" xuất hiện
T1: COMMIT
```

> ⚠️ **Lưu ý MySQL InnoDB**: Nhờ MVCC + gap lock, InnoDB ở Repeatable Read thực tế **ngăn được cả phantom read** trong hầu hết các trường hợp — đây là điểm khác biệt so với tiêu chuẩn SQL. Tuy nhiên, phantom vẫn có thể xảy ra trong một số edge case (ví dụ: `SELECT ... FOR UPDATE` thấy row mới mà `SELECT` thường không thấy).

- Ứng dụng: Phù hợp cho các transaction cần đọc nhiều lần và yêu cầu tính nhất quán cao (báo cáo tài chính, kiểm tra số dư trước khi chuyển tiền).

###### 4. Serializable

- Mức cô lập **cao nhất**: Đảm bảo kết quả tương đương với việc chạy các transaction tuần tự.
- Ngăn chặn **tất cả** anomalies: dirty read, unrepeatable read, phantom read, write-skew.
- Cơ chế:
   - **Lock-based** (SQL Server `SERIALIZABLE`): Dùng range lock / predicate lock để khóa cả khoảng giá trị, ngăn insert/update/delete vào range đã đọc
   - **SSI — Serializable Snapshot Isolation** (PostgreSQL): Dựa trên MVCC + phát hiện dependency cycle tại thời điểm commit. Nếu phát hiện vi phạm → abort transaction
   - **2PL + Index locking** (MySQL `SERIALIZABLE`): Tự động convert tất cả `SELECT` thành `SELECT ... FOR SHARE`
- **Trade-off**: Hiệu năng thấp nhất vì lock/check nhiều nhất. Throughput giảm đáng kể khi có nhiều transaction đồng thời.
- Ứng dụng: Giao dịch tài chính quan trọng, booking vé máy bay, bất kỳ nghiệp vụ nào mà consistency quan trọng hơn performance.


##### 15.2.6. MVCC

- **Multi-Version Concurrency Control**: quản lý nhiều physical version của 1 logical object
- **Nguyên tắc cốt lõi**: Writer tạo version mới thay vì ghi đè → Reader đọc version phù hợp với snapshot của mình
- **Lợi ích**: Reader không block Writer, Writer không block Reader (nhưng Writer **vẫn cần lock/latch** để tránh write-write conflict)
- Được sử dụng rộng rãi: PostgreSQL, MySQL InnoDB, Oracle, SQL Server (Snapshot Isolation)

###### A. Cơ chế Timestamp (Begin-TS / End-TS)

Mỗi **version** của 1 row (tuple) được gắn 2 timestamp:

| Trường | Ý nghĩa |
|---|---|
| **Begin-TS** | Thời điểm version này **bắt đầu có hiệu lực** (= commit timestamp của transaction tạo ra nó) |
| **End-TS** | Thời điểm version này **hết hiệu lực** (= commit timestamp của transaction thay thế nó). Mặc định = **∞** (INF) nếu chưa bị thay thế |

> 📌 **Lưu ý**: Trong quá trình transaction chưa commit, Begin-TS/End-TS có thể tạm lưu **txn_id** thay vì timestamp thực. Khi commit, hệ thống thay txn_id bằng commit timestamp chính thức.

**Visibility Check** — Transaction T (với snapshot timestamp `ts`) thấy version V nếu:
```
Begin-TS(V) <= ts < End-TS(V)
```
Tức là: version đã bắt đầu hiệu lực trước hoặc tại `ts`, VÀ chưa hết hiệu lực tại `ts`.

**Ví dụ minh họa Version Chain:**

```
Row X (logical object)

Version Chain:
┌─────────────────────────────────────────────────────────────┐
│ V1: value=100, Begin-TS=10, End-TS=20                       │  ← đã bị thay thế bởi V2
│ V2: value=200, Begin-TS=20, End-TS=50                       │  ← đã bị thay thế bởi V3
│ V3: value=300, Begin-TS=50, End-TS=∞                        │  ← version hiện tại
└─────────────────────────────────────────────────────────────┘

Transaction với ts=25 → thấy V2 (vì 20 <= 25 < 50)
Transaction với ts=55 → thấy V3 (vì 50 <= 55 < ∞)
Transaction với ts=5  → không thấy row nào (chưa tồn tại)
```

###### B. Các thao tác trong MVCC

| Thao tác | Hành vi |
|---|---|
| **INSERT** | Tạo version mới: `Begin-TS = txn_id`, `End-TS = ∞` |
| **UPDATE** | ① Set `End-TS` của version cũ = `txn_id` (đánh dấu hết hiệu lực) → ② Tạo version mới: `Begin-TS = txn_id`, `End-TS = ∞` |
| **DELETE** | Set `End-TS` của version hiện tại = `txn_id` (một số hệ thống tạo thêm **tombstone marker**) |
| **READ** | Duyệt version chain, tìm version thỏa `Begin-TS <= snapshot_ts < End-TS` |
| **COMMIT** | Thay tất cả `txn_id` trong Begin-TS/End-TS bằng **commit timestamp** chính thức → version trở nên visible cho các transaction khác |
| **ABORT** | Xóa (hoặc đánh dấu invalid) tất cả version mới mà transaction đã tạo; khôi phục `End-TS` của version cũ về giá trị ban đầu |

> ⚠️ **Quan trọng**: ABORT **KHÔNG** phải là set `End-TS = current time`. Các version do transaction tạo ra phải bị **loại bỏ hoàn toàn** (hoặc đánh dấu aborted để GC dọn dẹp sau), và version cũ phải được khôi phục lại trạng thái ban đầu.

###### C. Write-Write Conflict — Khi nhiều Transaction cùng UPDATE

MVCC **không** loại bỏ xung đột giữa writer-writer. Khi 2 transaction cùng muốn update 1 row, cần cơ chế giải quyết:


###### D. Snap isolation
- Khi transaction bắt đầu, nó sẽ lấy 1 snapshot timestamp
- Tất cả các transaction sau khi snapshot timestamp được tạo ra sẽ không ảnh hưởng đến transaction này
- Nếu nhiều txn cùng update 1 row, nó sẽ dùng First-Writer-Wins
- Không tránh đc write skew anomaly

**Nguyên tắc: First-Writer-Wins**

Khi transaction T2 muốn update row X mà T1 đang sửa (T1 chưa commit):
1. T2 phát hiện version hiện tại có `End-TS = T1_txn_id` (đã bị T1 đánh dấu)
2. T2 **phải chờ** T1 kết thúc (commit hoặc abort)
3. Nếu T1 **commit** → T2 bị **abort** (T1 thắng — first-writer-wins)
4. Nếu T1 **abort** → T2 được tiếp tục (T1 đã hủy, row trở về trạng thái ban đầu)

```
Ví dụ: T1 và T2 cùng UPDATE row X (value=100)

Thời điểm    T1 (ts=10)                    T2 (ts=15)                    Row X
─────────────────────────────────────────────────────────────────────────────────────
  t₀         BEGIN                                                       V1: value=100
                                                                         Begin-TS=5, End-TS=∞

  t₁         UPDATE X SET value=200                                      V1: End-TS=T1  (đánh dấu)
             → Tạo V2                                                    V2: value=200
                                                                         Begin-TS=T1, End-TS=∞

  t₂                                       BEGIN
                                            UPDATE X SET value=300
                                            → Thấy V1.End-TS=T1 (≠ ∞)
                                            → T1 chưa commit → ⏳ WAIT

  t₃         COMMIT (ts=10)                                             V1: End-TS=10 (finalized)
                                                                         V2: Begin-TS=10, End-TS=∞

  t₄                                       T1 đã commit → T2 bị ABORT   (First-Writer-Wins)
                                            ❌ T2 phải retry toàn bộ

─── Kết quả cuối cùng: V2 value=200 (của T1) ───
```

**Trường hợp T1 abort:**

```
  t₃'        ABORT                                                       V2 bị xóa
                                                                         V1: End-TS=∞ (khôi phục)

  t₄'                                      T1 đã abort → T2 được tiếp tục
                                            UPDATE X SET value=300
                                            → V1: End-TS=T2
                                            → V3: value=300
                                               Begin-TS=T2, End-TS=∞

─── Kết quả cuối cùng: V3 value=300 (của T2) ───
```

> 📌 **So sánh cách xử lý write-write conflict theo từng DBMS:**
>
> | DBMS | Cơ chế |
> |---|---|
> | **PostgreSQL** | First-Writer-Wins: T2 chờ T1 → nếu T1 commit thì T2 nhận lỗi `could not serialize access` (ở Repeatable Read) hoặc re-evaluate điều kiện WHERE (ở Read Committed) |
> | **MySQL InnoDB** | Row-level lock: T2 bị block tại row lock cho đến khi T1 kết thúc. Nếu timeout → deadlock error |
> | **Oracle** | Row-level lock tương tự MySQL. T2 chờ tại lock, không dùng First-Writer-Wins |
> | **SQL Server (SI)** | Update conflict detection: Nếu T2 cố commit sau T1 đã commit → T2 abort với `snapshot isolation conflict` |

#### 15.2.7. Version Storage

MVCC tạo version mới mỗi khi UPDATE/DELETE → **lưu các version ở đâu?** Đây là bài toán Version Storage.

Có 3 chiến lược chính:

##### A. Append-Only Storage

Tất cả version (cũ + mới) được lưu **trong cùng 1 table**. Mỗi tuple có pointer trỏ đến version tiếp theo, tạo thành **version chain**.

```
Main Table (chứa TẤT CẢ versions):
┌──────────────────────────────────────────────────────┐
│ V1: value=100, Begin-TS=10, End-TS=20  → next: V2   │
│ V2: value=200, Begin-TS=20, End-TS=50  → next: V3   │
│ V3: value=300, Begin-TS=50, End-TS=∞   → next: NULL  │
│ ... (các tuple khác cũng nằm ở đây)                  │
└──────────────────────────────────────────────────────┘
```

**2 cách sắp xếp chain:**

| Approach | Mô tả | Read | Write |
|---|---|---|---|
| **Oldest-to-Newest (O2N)** | Head = version cũ nhất, append version mới vào cuối chain | **O(n)** — phải duyệt từ đầu đến cuối để tìm version mới nhất | **O(1)** — chỉ cần append |
| **Newest-to-Oldest (N2O)** | Head = version mới nhất, version cũ bị đẩy xuống | **O(1)** — head luôn là version mới nhất | **O(1)** — nhưng cần update index pointer về head mới |

> 📌 **PostgreSQL** dùng Append-Only (O2N). Đây **không phải là "tệ"** — nó đơn giản và tránh overhead cập nhật index khi write. Trade-off là cần **VACUUM** thường xuyên để dọn dead tuples tích tụ trong main table (vì version cũ nằm chung table → table bị phình to — gọi là **table bloat**).

##### B. Time-Travel Storage

Version **hiện tại** nằm trong **main table**. Khi có UPDATE, version **cũ** bị copy sang **time-travel table** (bảng lịch sử riêng).

```
Main Table (chỉ chứa version MỚI NHẤT):
┌──────────────────────────────────────────┐
│ Row X: value=300, Begin-TS=50, End-TS=∞  │  ← luôn là version hiện tại
└──────────────────────────────────────────┘
        │ pointer
        ▼
Time-Travel Table (chứa version CŨ):
┌──────────────────────────────────────────┐
│ V2: value=200, Begin-TS=20, End-TS=50    │
│ V1: value=100, Begin-TS=10, End-TS=20    │
└──────────────────────────────────────────┘
```

| Ưu điểm | Nhược điểm |
|---|---|
| Main table luôn gọn (chỉ chứa version mới nhất) → scan nhanh | Mỗi UPDATE phải copy **toàn bộ tuple** sang time-travel table (kể cả cột không thay đổi) |
| Không bị table bloat như Append-Only | Write overhead cao hơn |

##### C. Delta Storage

Chỉ lưu **sự thay đổi** (delta) thay vì copy toàn bộ tuple. Main table chứa version hiện tại, **delta segment** chứa giá trị gốc của các cột bị thay đổi.

```
Main Table:
┌──────────────────────────────────────────────────────┐
│ Row X: name="Bob", age=30, salary=5000               │  ← version hiện tại (đầy đủ)
└──────────────────────────────────────────────────────┘
        │ pointer
        ▼
Delta Segment:
┌──────────────────────────────────────────────────────┐
│ Δ2: salary=4000  (chỉ lưu cột thay đổi, TS=20→50)  │  ← muốn xem V2: apply Δ2
│ Δ1: salary=3000, age=25  (TS=10→20)                 │  ← muốn xem V1: apply Δ2+Δ1
└──────────────────────────────────────────────────────┘

Rebuild V2: lấy main → apply Δ2 → salary=4000, name="Bob", age=30
Rebuild V1: lấy main → apply Δ2 → apply Δ1 → salary=3000, age=25
```

| Ưu điểm | Nhược điểm |
|---|---|
| Tiết kiệm disk nhất — chỉ lưu cột thay đổi | Rebuild version cũ tốn CPU (phải apply nhiều delta) |
| Write nhanh — chỉ ghi delta nhỏ | Read version cũ = **O(n)** delta applications |

##### So sánh tổng hợp

| | Append-Only | Time-Travel | Delta |
|---|---|---|---|
| **Version cũ ở đâu** | Cùng main table | Table riêng | Segment riêng (chỉ delta) |
| **Disk usage** | Cao (full tuple × n versions) | Trung bình (full tuple copy) | **Thấp nhất** (chỉ lưu thay đổi) |
| **Write overhead** | Thấp (append) | Cao (full copy) | Thấp (ghi delta nhỏ) |
| **Read version cũ** | Duyệt chain | Duyệt chain | Apply delta ngược |
| **Table bloat** | ⚠️ Có (cần VACUUM) | ✅ Không | ✅ Không |
| **DBMS** | **PostgreSQL** | — | **MySQL InnoDB** (undo log), **Oracle** (undo tablespace) |

**Fact**:
  - PostgreSQL không support Read Uncommitted — nó tự động nâng lên Read Committed (vì kiến trúc MVCC của PG luôn đọc committed version)


#### 15.2.8. Garbage collection
   - Cẩn remove các physical version không còn được sử dụng hoặc aboort
   - Approach 1: Tuple level
      - Background vacumn:
         - thread định kì quét table và remove các version không còn được sử dụng
         - Dirty block bitmap: đánh dấu các block chứa version không còn được sử dụng thay vì modify
       - Cooperative cleaning
          - Chính các luồng đang thực thi truy vấn (worker threads) sẽ kiêm luôn việc dọn rác. Khi một query duyệt qua một version chain để tìm dữ liệu, nếu nó vô tình phát hiện các phiên bản đã quá cũ (nhỏ hơn Watermark), nó sẽ "tiện tay" cắt bỏ và giải phóng chúng luôn.
          - Chỉ hoạt động với O2N(Old to new)

   - Approach 2: Transaction level GC
      - Transaction dọn dẹp các version cũ của chính nó sau khi commit


#### 15.2.9. Index Management (Trong kiến trúc MVCC)

- **Primary index luôn trỏ tới head of version**:
  - Khi một bản ghi bị update nhiều lần, nó tạo thành một chuỗi các phiên bản (version chain).
  - Khóa chính (Primary Index) thường chỉ trỏ duy nhất một lần đến điểm bắt đầu của chuỗi này (Head of Version Chain). 

- **Secondary index cần phải update khi có version mới**:
  - Khi có bản ghi mới sinh ra do update (vị trí vật lý lưu đổi), các index phụ (Secondary Index) trỏ tới nó theo 2 cách:
    - **Trỏ tới Logical pointer (Con trỏ logic)**: Secondary Index trỏ đến Primary Key. Khi sinh ra version mới, chỉ Primary Index thay đổi, không cần update lại lượng lớn Secondary Index (Write nhanh). Nhược điểm là tốn thêm 1 bước tra ngược lại về Primary Key (Read chậm thêm 1 chút). *Đại diện: MySQL (InnoDB)*.
    - **Trỏ tới Physical pointer (Con trỏ vật lý)**: Secondary Index trỏ trực tiếp đến địa chỉ ổ cứng thật (Page ID + Tuple ID). Ưu điểm tra phát ra luôn bản ghi (Read rất nhanh). Nhược điểm là tốn chi phí WRITE nặng: bất kỳ update nào dời vị trí vật lý thì tất cả các Secondary Index hướng đến nó đều phải update lại con trỏ.

- **Phần lớn các dbms không lưu thông tin version của tuple trong keys**:
  - Thường các thẻ (entry) trong B+Tree Index không nhúng timestamp/version id vào khóa mà chỉ có giá trị và con trỏ. Việc giải quyết xem hiển thị cho transaction nào hoàn toàn dựa vào timestamp lưu dưới Tuple data.
  - **PostgreSQL là ngoại lệ?** Đúng vậy. Do kiến trúc thiết kế **Append-only storage** (Tất cả version đều được lưu trên cùng một table/Heap space hệt như một row độc lập). Mỗi version là 1 bản ghi có vị trí địa vật lý riêng (CTID) nên PostgreSQL bắt buộc phải ghi trực tiếp số lượng lớn con trỏ vật lý xuống TỪNG VERSION MỘT (chứ không chỉ trỏ Head). Việc này dẫn hệ lụy **Index Bloat** (Index phình to theo thời gian vì chứa quá nhiều con trỏ mồ côi).

- **Support duplicate keys from difference snapshot**:
  - Ở bề mặt Logic: `UNIQUE INDEX` hoặc `PRIMARY KEY` nghiêm cấm trùng lặp. Tuy nhiên, dưới cấu trúc vật lý của Index (trong MVCC) các DBMS phải thiết kế để support chứa nhiều keys có hệ thống giá trị giống hệt nhau.
  - Mục đích: Cùng 1 giá trị key nhưng trỏ tới luồng logical tuples theo các thời gian/snapshot độc lập. Ví dụ T1 xóa khóa `A` (nhưng chưa commit), T2 thực hiện chèn khóa `A`. Ở hạ tầng vật lý Index vẫn ghi danh sách Duplicate Key là cả hai chữ `A` này, lớp kiểm soát Constraints sẽ đứng ra ngăn chặn ở trên không để End User nhận các hiển thị sai lệnh.
  - Vì cấu trúc vật lý ở bước 1 cho phép lưu trùng lặp, nên bản thân cấu trúc này không thể tự động ngăn chặn việc người dùng cố tình chèn 2 khóa chính giống nhau. Database phải tự làm điều này bằng code (logic thực thi bổ sung).

   - Cách hoạt động (Nguyên tử - Atomic): Khi có lệnh INSERT, database không chèn thẳng vào ngay. Nó phải thực hiện một thao tác gộp không thể bị ngắt quãng (atomic): "Tìm xem khóa này đã có phiên bản nào đang active chưa -> Nếu chưa có thì chèn vào". Việc này phải làm nguyên tử để ngăn chặn trường hợp (Race Condition) khi 2 người dùng cùng lúc chèn cùng một ID.

   - Các worker (luồng xử lý) có thể nhận về nhiều kết quả cho một lần lấy dữ liệu. Sau đó, họ phải đi theo các con trỏ để tìm ra phiên bản vật lý chính xác).

Nghĩa là gì: Khi hệ thống (worker) chạy lệnh SELECT * FROM table WHERE id = 5, truy vấn này đi vào index và có thể nhận về nhiều con trỏ (pointers) khác nhau (vì như ở ý 1, đang có 3 phiên bản của id = 5).

#### 15.2.10. MVCC Deletes
   - DBMS chỉ deletes 1 tuples khi toàn bộ các version logical của nó not visible
      - Nếu 1 tuple đã bị xóa, không thể có 1 version mới được sinh ra từ nó
      - No write-write conflics, first wrtiter win
   - Cần 1 phương án để đánh dấu version đã bị xóa (Có 2 cơ chế chính):
      - **Phương án 1: Delete flag (Sử dụng cờ đánh dấu)**
         - **Cách làm**: Sửa trực tiếp version hiện tại bằng cách bật cờ (flag) ở khu vực Header hoặc ở một cột hệ thống riêng để báo rằng "Row này đã bị xóa". (Thường là đánh dấu `End-TS = mã_txn_hiện_tại`).
         - **Ưu điểm**:
            - Tiết kiệm dung lượng (không sinh ra bản ghi mới nào trên ổ cứng).
            - Thu dọn rác (Garbage Collection - GC) dọn dẹp rất nhanh và gọn gàng.
         - **Nhược điểm**:
            - Vi phạm nguyên lý "không ghi đè" (Immutable/Append-only) vì phải update trực tiếp vào dữ liệu cũ trên disk (In-place update).
            - Việc In-place update gây ra Random I/O Write và làm hỏng Cache của database block, đồng thời cần Latch/Lock tinh vi để xử lý xung đột.
            
      - **Phương án 2: Tombstone tuple (Tạo bản ghi giả "Bia mộ")**
         - **Cách làm**: Hoạt động hệt như lệnh UPDATE. Tạo 1 version rỗng (empty version) chèn thêm vào hệ thống. Các Transaction đi sau dọc theo Version Chain đụng phải cái Tombstone này thì biết data logic đã bị xóa.
         - **Ưu điểm**:
            - Việc xóa dữ liệu biến thành việc ghi nối thêm (Append/Insert mới). Bản ghi gốc hoàn toàn không bị đụng vào, đảm bảo thông lượng ổ đĩa cực tốt (Sequential Write).
            - Tái sử dụng được sơ đồ kiến trúc code của luồng UPDATE.
         - **Nhược điểm**:
            - Logic thì là Xóa, nhưng thực tế dung lượng Database lại phình to ra (Table Bloat) vì phải chứa thêm hàng loạt bản Tombstone rỗng.
            - Phức tạp hóa luồng dọn dẹp Garbage Collection sau này.
         - **Mẹo tối ưu (Reduce Overhead)**: Thay vì lưu 1 bản ghi Tombstone to, Database có thể (1) thiết kế một khu riêng biệt (Separate Pool) dành cho Tombstone hoặc (2) tận dụng 1 chuỗi bit đặc biệt (special bit pattern) nhúng thẳng vào trong cấu trúc con trỏ (Version Chain Pointer) -> Cực kỳ tiết kiệm dung lượng.


### 16. WAL + Shadow Paging

#### 16.1. Buffer Pool Policies

Quản lý Buffer Pool liên quan đến việc quyết định **khi nào** một page dữ liệu bị sửa đổi (dirty page) có thể hoặc bắt buộc phải được đẩy (flush) từ bộ nhớ (RAM) xuống đĩa cứng (Disk).

##### 16.1.1. Steal Policy (Chính sách thay thế)
Quy định liệu DBMS có thể đẩy (evict) một "dirty page" của một transaction **chưa commit** xuống ổ đĩa để lấy khoảng trống cho page khác hay không.
- **Steal** (Cho phép): Có thể đẩy dirty page của uncommitted transaction xuống đĩa. Giúp bảo vệ hệ thống không bị tràn bộ nhớ khi transaction sửa quá nhiều dữ liệu. Cách này yêu cầu DBMS phải có khả năng **UNDO** (khôi phục trạng thái cũ) nếu transaction đó bị abort.
- **No-Steal** (Không cho phép): Phải giữ dirty page trên RAM cho đến khi transaction kết thúc (commit/abort). Không cần UNDO file (chỉ cần xóa dữ liệu trên RAM) nhưng giới hạn dung lượng thay đổi của 1 transaction bằng với kích thước của Buffer Pool.

##### 16.1.2. Force Policy (Chính sách bắt buộc)
Quy định liệu DBMS có bắt buộc phải đẩy (flush) toàn bộ các dirty page của một transaction xuống đĩa ngay **vào lúc nó commit** hay không.
- **Force** (Bắt buộc): Mọi thay đổi phải được ghi đầy đủ xuống đĩa trước khi commit thành công. Đảm bảo dữ liệu không bị mất nếu crash (Durability), hệ thống không cần cơ chế **REDO**.
- **No-Force** (Không bắt buộc): Giao dịch có thể trả về commit thành công ngay cả khi dữ liệu (data pages) vẫn đang nằm trên RAM (chưa ghi xuống đĩa). Giúp tăng hiệu năng đáng kể (giảm thời gian chờ I/O ghi ngẫu nhiên đối với dữ liệu phân mảnh) nhưng buộc hệ thống phải lưu trữ các sự kiện thay đổi vào file log để có thể **REDO** khôi phục nếu xảy ra crash.

**Tổng hợp 4 trường hợp dựa trên Steal/Force Policy:**

| Policy | UNDO | REDO | Đặc điểm |
|---|---|---|---|
| **No-Steal + Force** | Không | Không | Phục hồi rất nhanh (không cần log), nhưng hiệu năng rất thấp vì Random I/O nặng nề. (Ví dụ: Shadow Paging) |
| **Steal + Force** | Có | Không | Ít dùng trong thực tế. |
| **No-Steal + No-Force** | Không | Có | Transaction bị giới hạn dung lượng thay đổi bởi bộ nhớ RAM. |
| **Steal + No-Force** | Có | Có | I/O tối ưu nhất, hiệu năng cao nhất nhưng kiến trúc phục hồi phức tạp nhất. Hầu hết các DBMS hiện nay (Hệ thống **ARIES** như PostgreSQL, MySQL, SQL Server) dùng cách này kết hợp với **WAL**. |

---

#### 16.2. Shadow Paging

Dựa trên nguyên tắc thiết kế **No-Steal + Force**. Hầu như tránh được việc phải duy trì các file Log khổng lồ cho việc UNDO/REDO. Phổ biến trong bản thiết kế đời đầu hoặc các db nhúng như (LMDB, SQLite trước đây).

- **Cơ chế hoạt động**:
  - DBMS quản lý dữ liệu qua một cây (VD: B+Tree Page Table).
  - Có một **Master Pointer** (Con trỏ gốc) nằm ở vị trí an toàn trên đĩa, luôn trỏ vào Root của cấu trúc Page Table hiện tại (chứa data thật sự đã commit).
  - Khi transaction cập nhật một page (VD: Leaf node 4), hệ thống **tuyệt đối không ghi đè** lên bản cứng. Thay vào đó, nó thiết lập 1 bản copy của page đó (Shadow Page) ra khoảng trống ổ đĩa và thao tác thay đổi ở đó.
  - Quá trình này leo dần lên đến Root (Path copying). Tức là bất kỳ node nào trên đường đi từ page thay đổi lên Root cũng sẽ được copy tương ứng.
  - Khi chuẩn bị **commit**, DBMS chỉ cần tráo đổi cái **Master Pointer** trỏ sang phiên bản Root Copy bằng duy nhất *một thao tác atomic* (Ghi đè 1 giá trị pointer). Sau lúc này, toàn bộ nhánh con mới sẽ trở thành data chính thức, các phiên bản page cũ coi như mồ côi (chờ dọn dẹp).
- **Ưu điểm**:
  - Khôi phục (Recovery) cực nhanh, gần như tức thì sau Crash. Không cần Undo hay Redo Log. Quá trình chỉ dừng ở nhánh trỏ chưa hoàn thiện do Master Pointer vẫn nằm y nguyên ở cây đời trước.
- **Nhược điểm (Overhead cực kì đắt):**
  - **Overhead bộ nhớ Copy**: Chỉ thay đổi 1 ô dữ liệu trong Page, nhưng hệ thống phải bê toàn bộ Page cùng với 1 mớ Tree-path cồng kềnh tạo bản sao.
  - **Phân mảnh dữ liệu disk (Data fragmentation)**: Vì thay đổi vị trí Page liên tục lên bộ phận trống trên đĩa, nên vị trí chuỗi logic hoàn toàn nát bét về mặt vật lý -> Làm hẹp trầm trọng khả năng đọc Sequence Scans.
  - **Tốn chu kỳ Garbage collection** lượm lặt các rác do page cũ đào thải ra.
  - Thường cực kỳ khó code Concurrency Control hiệu quả.

---

#### 16.3. WAL (Write-Ahead Logging)

Là giải pháp tiêu chuẩn phục vụ cho kiến trúc **Steal + No-Force**. Do DBMS có quyền ghi dở dang uncommitted data xuống đĩa (Steal) hoặc giữ Committed data trên bộ nhớ (No-Force), máy tính bắt buộc phải đẻ ra file Log ghi lại dấu vết để **UNDO** (khi bị abort) và **REDO** (khi bị mất điện crash lúc chưa kịp ghi xuống disk).

**A. Nguyên tắc vàng của WAL (Theo thuật toán chuẩn ARIES):**
1. Trước khi hệ thống phát hỏa tự động đẩy (flush) 1 dirty page xuống ổ đĩa, toàn bộ "Log Entry" mô tả về sự thay đổi của page này **PHẢI ĐƯỢC ÉP FLUSH** ghi xuống đĩa log trước tiên. (Quy định này để bảo đảm có tài liệu mà UNDO cho trò Steal page).
2. Một transaction chỉ được chốt lại đóng hồ sơ (return success cho Client) mốc "Commit" khi mà bản **Log Record** của nó (Chứa thông báo trạng thái commit) đã an toàn ghi xong vào disk log. (Quy định để đảm bảo thao tác REDO cho trò No-Force page). Lợi thế là lưu Log là thao tác Sequential I/O rất mượt.   

**B. Tối ưu hoá I/O với Group Commit (Cơ chế gộp nhóm)**
- **Vấn đề**: Mặc dù ghi file log là thao tác ghi tuyến tính tuần tự (Sequential I/O) cực nhanh, nhưng lệnh `fsync()` (tính năng ép hệ điều hành ghi trực tiếp từ cache xuống vật lý đĩa cứng) được gọi mỗi khi transaction báo commit lại có độ trễ lớn. Nếu 10,000 transaction cùng commit độc lập sẽ phát sinh tới 10,000 System Calls `fsync()` gây tắc nghẽn tài nguyên đĩa.
- **Giải pháp**: DBMS sẽ tự động làm chậm quy trình commit của từng transaction lại một chút xíu (chỉ khoảng vài mili-giây). Trong thời gian "chờ đợi nén" này, nó gom góp các transaction khác cùng lọt vào thời điểm commit để rồi **thực hiện `fsync()` ghi gộp toàn bộ block log của chúng vào disk trong đúng 1 lần I/O System Call duy nhất**.
- **Hiệu quả**: Loại bỏ triệt để số lượng System Calls bùng nổ, tăng trưởng thông lượng ghi đĩa (throughput) lên theo cấp số nhân đối với hệ thống áp lực cao (hàng ngàn lượt connection cùng thao tác trên giây).

**C. Cấp độ cấu trúc Log (Logging Schemes):**

*(Kịch bản: Bảng T, hiện tại số dư $X=1$. Transaction yêu cầu lệnh cập nhật `X = X + 1`; $X nằm ở Slot 1 trên Page 99)*

1. **Physical Logging (Vật lý 100% - Before/After Image)**
   - Lưu trữ chính xác giá trị byte bit của "trước" và "sau" tại vị trí khối block/vật lý ổ đĩa đó.
   ```text
   <T1, Table=T, Page=99, Offset=1024, Before_bytes=001, After_bytes=002>
   ```
   - **Ưu điểm**: Đơn giản nhất, cực kỳ deterministic (cứ bôi đúng vị trí offset lên là phục hồi xong nên an toàn và đáng tin cậy).
   - **Nhược điểm**: Kích thước log phình to lố bịch. Nếu một query Update thay làm đổi lệch 1 tỷ record, hệ thống phải sinh ra hơn 1 tỉ mục ghi Physical khổng lồ.

2. **Logical Logging (Logic 100%)**
   - Không lưu vào disk vị trí mà chỉ gom giữ rặt cú pháp mệnh lệnh truy vấn nghiệp vụ cấp cao.
   ```text
   <T1, UPDATE T SET X = X + 1 WHERE ...>
   ```
   - **Ưu điểm**: Kích cỡ Log file siêu siêu nhỏ. Cực kỳ tối giản.
   - **Nhược điểm**: Rất vất vả trong tính huống Crash-Recovery để lập lại môi trường. Đặc biệt tiềm tàng tai họa lớn với hàm tính **non-deterministic (chức năng linh động theo tự nhiên)** (Ví dụ `UPDATE SET timeout_date = NOW()`). Nếu 1 tháng sau ta khôi phục chạy REDO qua file log, biểu thức ảo NOW() sẽ biến chất, lấp giá trị sai thực tiễn chứ không lưu trữ lại dữ liệu timestamp chính xác.

3. **Physiological Logging (Lai tạo tinh giảm - Chuẩn phổ thông)**
   - *"Physical-to-a-page, logical-within-a-page"*. Lai ghép cả 2 bộ môn trên nhằm hớt ưu điểm (Database System R đi đầu rèn giũa và nay phổ biến đến 90% Relational DBMS).
   - File log chỉ trỏ cố định tọa độ tìm vào con Page cụ thể (Bản đồ vật lý), rồi ở lớp bên trong thay vì đếm bít nó sẽ gọi chuỗi lệnh (Slot mapping / logic).
   ```text
   <T1, Table=T, Page=99, Slot=1, Execute_logic: X_plus_1>
   <T1, Index=X_PKEY, IndexPage=45, Key(1, Record_1)>
   ```
   - **Lợi ích ưu việt**: Log size thu nhỏ xuống cực kì nhiều, và không bị vướng mắc rủi ro giá trị trôi nổi do đã khoanh vị trí rành kẹp cứng Page Slot mà truyền thông điệp hẹp.

**[Thảo luận mở rộng] Câu hỏi: Nếu dùng chính sách STEAL, một Dirty Page chứa data của Transaction CHƯA COMMIT có thể bị đẩy thẳng xuống đĩa (flush). Vậy làm sao để một User khác tình cờ truy cập không bị đọc nhầm cái dirty data (data rác) đó?**
- **Trả lời:** Việc cho phép đẩy data (RAM/Disk) là quyền quyết định của **Buffer Manager**. Còn việc "Bảo vệ User tránh đọc phải rác" là nhiệm vụ của **Concurrency Control (Trình kiểm soát đồng thời)**. Hai bên phối hợp như sau:
    1. **Nếu dùng Lock (Strict 2PL)**: Dù data có in hằn xuống đĩa thành bản vật lý, Transaction T1 vẫn đang nắm cục **Exclusive Lock (Write Lock)** của record đó. User 2 nhảy vào đòi truy vấn sẽ đập ngay vào rào chắn Lock, buộc phải đứng đợi tới khi T1 chốt xong (nhả Lock). Do đó, KHÔNG CÓ CƠ HỘI cho User 2 đọc trộm rác.
    2. **Nếu dùng MVCC (Postgres/MySQL)**: Bản data ghi xuống đĩa có dập luôn con dấu `Begin-TS = TxnId_T1` vào vùng Header. Khi User 2 lục Disk lôi Record này lên bèn thấy dấu tay của T1, hệ thống giám sát báo "T1 vẫn đang Active (Chưa commit) đấy!". Ngay lập tức User 2 chối bỏ mẩu Record đỏ hỏn đó (xem như tàng hình) và tự động lội xuống kho lưu trữ phiên bản cũ (Undo Log) để kiếm cái snapshot hợp lệ trước đó mà đọc.

**D. Cơ chế REDO & Checkpoint**
Việc Log liên tục cho phép khôi phục nguyên vẹn, tuy nhiên nếu dồn log từ ngày lập quốc đến hiện tại, khi ứng dụng rớt mạng sẽ mất hàng kỷ nguyên để chiếu lại toàn thể quá trình REDO. Phương án cắt giảm tốt nhất là ứng dụng **Checkpoint** khoép chặng:
- **Khi Checkpoint chạy qua (Save)**: 
  - (Theo chu kỳ hoặc dung lượng cấu hình) Hệ quản trị DBMS block hãm các tác vụ lại, tiến hành ép các log WAL chưa ghi và đặc biệt tống hết sạch sẽ các bộ **Dirty Pages** nằm trên RAM dán cứng ngắc vào đĩa.
  - Ghi 1 cờ Log `Checkpoint` báo chốt để làm chứng thư mốc dữ liệu tin cậy. (Các giao dịch trước điểm mốc được hạch toán đồng bộ hóa lên đĩa an toàn vĩnh cửu).
- **Khi Crash (Khôi phục)**: 
  - Hệ quản trị DBMS khởi động vòng máy, đảo ngược dò log để khui ra cờ `checkpoint` có giá trị gần nhất. Toàn bộ sớ log sinh trước mốc đó được ném vô kho (bỏ qua do dirty tablespace đã hòa vô Disk an toàn). Chỉ chạy replay khôi phục quy trình log tồn lại sau Checkpoint đó. Bộ máy vận hành bình thường! 


### 17. Database crash recovery
- Cần đánh dấu điểm cuối trong WAL để biết điểm bứt đầu --> Tất cả các log đềi có 1 unige log -> log sequence number(LSN)
   - Unique và tăng dần
   - Mỗi page đều có pageLSN(most recent log record that update this page)
   - FlushedLSN: LSN max đã được flush
   - Trước khi page được write: pageLSN <= flushed lsn? Why -> flushed là đã đc flused, update thì có thể chưa flushed mà??? -> Vì nó là log của WAL ko phải của TXN -> Lớn hơn
   - Trong bài giảng, chúng ta giả định như sau
     - Kích thước tất cả log record feed trong 1 single page
     - Thao tác ghi định kỳ vào page là 1 thao tác atomic
     - Chỉ 1 version tuple với Strong strich 2 PL
     - Steal/No-Force với WAL
     - Physical log record schema
     - Bỏ qua log cho index

     - Khi commit thành công ghi thêm 1 TXN-END để đánh dấu sẽ không còn thao tác nào của TXN này nữa
     - Không cần flush log này do trong WAL đã có log commit(mang ý nghĩa với application hơn là tính toàn vẹn ở vật lý)
     - Khi abort (Rollback):
        - Cần undo lại: Lưu thêm `prevLSN` của txn, hoạt động như 1 linked list ngược để truy xuất lùi lại và undo dễ dàng.
        - **CLR (Compensation Log Record):**
           - Khi tiến hành UNDO một thao tác, bản thân việc UNDO cũng làm thay đổi data trên Disk/RAM, do đó **nó bắt buộc cũng phải sinh ra một Log record**. Các log sinh ra trong quá trình UNDO này chính là **CLR**.
           - **Cấu trúc cực đỉnh của CLR**: Ngoài việc ghi nhận sự thay đổi, CLR có một con trỏ vô cùng quan trọng trỏ lùi đánh dấu: `UndoNextLSN` (Nó trỏ thẳng đến `prevLSN` của cái original log vừa bị undo - tức là chỉ đích danh hành động tiếp theo trong chuỗi cần phải undo).
           - **Mục đích của CLR**: Đảm bảo toàn bộ quy trình UNDO **không bao giờ bị lặp lại**. Nếu đang undo lỡ dở mà server bị crash, khi khởi động lại, thuật toán Recovery đọc thấy CLR thì nó sẽ túm lấy `UndoNextLSN` để undo tiếp các bước bị bỏ dở, né tránh 100% việc undo lại những thao tác đã được undo trước khi crash. (Vì thao tác UNDO không phải lúc nào cũng idempotent, chạy lại nhiều lần dễ rách việc).
           - *Ví dụ minh họa luồng ghi của Abort (từ hình ảnh minh họa)*:
              - LSN `002`: UPDATE A (30->40). (Bản ghi được ghi nhớ `prevLSN=001`)
              - LSN `003`: UPDATE B (10->24). (Bản ghi được ghi nhớ `prevLSN=002`)
              - LSN `011`: TXN ABORT (Phát lệnh đập bỏ).
              - LSN `026`: Thực thi Undo cho LSN 003 -> Bản ghi `CLR-003` sinh ra: Đảo ngược lại cập nhật B (24->10). Lúc này cái `UndoNextLSN` chỉ đến `002` (Ra hiệu hệ thống hãy lui về undo tiếp cái 002 kìa).
              - LSN `027`: Thực thi Undo cho LSN 002 -> Bản ghi `CLR-002` sinh ra: Đảo ngược cập nhật A (40->30). Lúc này `UndoNextLSN` lại tiếp tục lui về `001` (Chính là điểm BEGIN).
              - LSN `028`: TXN-END (Chính thức khép lại Transaction sau khi Undo chuỗi thành công rực rỡ).
        - **Giải đáp: Vì sao cần Hold Lock (Strict 2PL) trong suốt toàn bộ quá trình Rollback?**
           - Giả sử transaction bị Abort và hệ thống đang lùi lại gọi hàng loạt thao tác UNDO để dọn dẹp, dữ liệu lúc này đang trong tình trạng "ngổn ngang công trường" (nửa thành nửa bại, đang gỡ từng món).
           - Nếu ta Release Lock cho nó ngay khi vừa phát lệnh Abort: Một Transaction phá bĩnh khác (T2) sẽ lợi dụng nhảy vào đọc hoặc sửa đúng cái dòng đang được khôi phục. -> Dẫn đến hệ luỵ T2 đọc phải cấu trúc rác bầy hầy (Dirty Read) hoặc T2 update xong thì lệnh UNDO chậm trễ của T1 quét qua chép đè luôn thao tác của T2 bẹp dúm (Lost Update).
           - **Do đó**: Strict 2PL dùng thiết quân luật với Exclusive Lock (Write-Lock), chỉ được nhả còng ra **SAU KHI** Transaction chính thức chấm dứt (Ghi xong dòng `TXN-END` ở LSN 028). Lúc ấy data đã được Restore nguyên vẹn 100%, an toàn cho bá tánh đi qua.

#### 17.2. Fuzy checkpoint
   - Thay vì block toàn bộ transaction, ta lưu điểm bắt đầu và điểm kết thúc của checkpoint
   - ATT và PTT: 
     - ATT(Active Transaction Table): Lưu thông tin transaction hiện tại: R(running), C: Commit, A: Abort
     - DPT(Dirty page table): Lưu thông tin dirty page
        - recLSN: Lưu thông tin LSN cũ nhất đã modified page từ thời điểm lần cuối page được write xuống disk
            - Khác với pageLSN ở chỗ pageLSN là LSN của log record update page, còn recLSN là LSN của log record update page từ thời điểm lần cuối page được write xuống disk
            - Cung cấp thông tin tại thời điểm bắt đầu và kết thúc của checkpoint
   - Checkpoint đang lưu các thông tin gì:
    - Dirty page table
    - Active transaction table
    
#### 17.3. ARIES: Recovery phase
ARIES thực hiện khôi phục hệ thống qua **3 pha tuần tự (3-Phase Recovery)**:

##### 1. Analysis Phase (Pha phân tích)
- **Mục đích**: Đọc WAL để tái tạo lại trạng thái của **Active Transaction Table (ATT)** và **Dirty Page Table (DPT)** giống hệt như khoảnh khắc ngay trước khi crash.
- **Cách thực hiện**: Quét WAL log tiến lên (forward) bắt đầu từ checkpoint thành công cuối cùng (Nếu là Fuzzy Checkpoint, sẽ bắt đầu từ log `CHECKPOINT-BEGIN`).
- **Cập nhật ATT (Quản lý các Transaction)**:
   - Gặp log mới của TXN chưa có trong ATT: Thêm vào ATT với status là `U` (Undo - Mặc định cho rằng chưa hoàn thành).
   - Gặp log `COMMIT`: Chuyển status của TXN trong ATT thành `C` (Commit).
   - Gặp log `TXN-END`: TXN đã hoàn thành trọn vẹn, xóa nó khỏi ATT.
- **Cập nhật DPT (Quản lý các Dirty Page)**:
   - Nếu gặp một log thay đổi dữ liệu (Update) tác động lên một page X:
      - Nếu X chưa có trong DPT $\rightarrow$ Thêm X vào DPT. Đồng thời gán `recLSN` (Recovery LSN) của page X bằng đúng `LSN` hiện tại của log này.
- **Kết quả sau Pha 1**:
   - **ATT**: Chứa danh sách các TXN đang active ngay lúc rớt mạng. Những TXN mang status `U` sẽ bị hoàn tác ở pha 3.
   - **DPT**: Chứa các page "có nguy cơ" là dirty.

##### 2. Redo Phase (Pha làm lại - Repeating History)
- **Mục tiêu**: Vạch lại bánh xe lịch sử. Khôi phục (Redo) lại **TẤT CẢ các thao tác**, bao gồm của CẢ giao dịch đã commit LẪN chưa commit để đồng bộ trạng thái Disk khớp 100% với WAL.
- **Cách thực hiện**: Bắt đầu quét tiến lên (forward) xuất phát từ vị trí có `recLSN` thấp nhất trong bản DPT tìm được ở pha 1 (đây là vị trí xa nhất trong quá khứ mà 1 page chưa được flush an toàn xuống ổ).
- Khi gặp một Update Record, ta sẽ thực hiện bốc page trên ổ cứng lên để làm lại (Redo), **NGOẠI TRỪ** 3 trường hợp chứng minh page đã an toàn:
   1. Page không có tên trong DPT (Do Checkpoint đã flush rảnh rang từ trước).
   2. Page có trong DPT nhưng `recLSN` của nó lại LỚN HƠN mốc `LSN` của bản log này.
   3. Bốc page lên soi Header thấy `pageLSN >= log_LSN` (Chứng tỏ bản trên đĩa đang xài data đời mới hơn log này).
- Nếu không thuộc 3 điều trên: Thực thi thay đổi xuống data, cập nhật `pageLSN = log_LSN`.

##### 3. Undo Phase (Pha hoàn tác)
- **Mục tiêu**: Xóa sổ mọi tàn tích của các transaction CHƯA HOÀN TẤT (Các TXN có status `U` nằm rải rác trong ATT sau Pha 1).
- **Cách thực hiện**:
   - Duyệt ngược WAL từ dưới lên trên (Backward) dựa vào sợi xích `prevLSN`.
   - Với mỗi thao tác tìm được, ta tiến hành thao tác ngược chiều để hủy nó.
   - **QUAN TRỌNG - Sinh log CLR**: Mỗi khi dọn dẹp thành công một bước, ARIES sẽ nhả ra một bản log **CLR (Compensation Log Record)** đánh dấu "tôi đã undo xong thao tác cũ này bằng hành động kia". CLR có con trỏ `UndoNextLSN` chỉ điểm lùi tiếp về thao tác cần dọn sau đó.
   - Nhờ CLR, nếu phần mềm dọn dẹp bị crash giữa chừng, quá trình Recovery bật lại sẽ nương vào `UndoNextLSN` của CLR để biết tiến độ, không bao giờ rơi vào bẫy lặp lại việc undo vô hạn.
   - Diệt cỏ tận gốc về đến LSN đầu tiên (`BEGIN`) của TXN -> Ghi log `TXN-END` chốt hạ -> Xóa sổ TXN khỏi ATT. Quy trình ARIES Recovery hoàn tất.



### 18. Distributed database
#### 18.1. System architecture
- Share everything: 
- Share nothing: 
   - Mỗi node có cpu, memory, disk riêng
   - Better performance và effieciency
   - Khó mở rộng và đảm bảo tính nhất quán
   - Phần lớn các hệ thống hiện tại đều sử dụng hệ thống này
   - Ví dụ: 
      - Khi add thêm 1 node vào, cần rebalance lại data
- Share disk: 
   - Đơn giản trong query select
   - Khi update cần cơ chế notify tới các node khác về thay đổi
- Share memory(No one do this)

#### 18.2. Distributed query
##### A. Push query to data
- Gửi query hoặc 1 phần của query tới node chứa data
- Thực thi càng nhiều query và process nhất có thể khi data còn ở node đó
- **Ưu điểm**: Giảm thiểu data di chuyển giữa các node qua mạng (network I/O). Kết quả trả về nhỏ gọn (đã được filter/aggregate sẵn).
- **Nhược điểm**: Node chứa data cần có đủ CPU/RAM để xử lý query. Nếu data skew (1 node chứa quá nhiều data so với các node khác), node đó trở thành bottleneck. Không phù hợp với các phép tính phức tạp cần dữ liệu từ nhiều node cùng lúc (cross-partition JOIN).
- **Ví dụ**:
  - Truy vấn `SELECT SUM(salary) FROM employees WHERE department = 'IT'`. Thay vì kéo toàn bộ bảng `employees` về master node, truy vấn được đẩy tới các worker node. Mỗi worker tự lọc nhân sự phòng 'IT', tính tổng lương cục bộ, rồi chỉ trả về 1 con số tổng cho master node. Master cộng các kết quả lại → chỉ vài con số nhỏ đi qua mạng thay vì hàng nghìn dòng dữ liệu.

##### B. Pull data to query
- Gửi data tới node thực thi query (kéo dữ liệu về nơi tính toán)
- Cần thiết khi không còn resource tính toán tại node chứa data hoặc bản chất phép tính phức tạp khó chia nhỏ.
- **Ưu điểm**: Xử lý được các phép tính phức tạp cần dữ liệu từ nhiều nguồn (cross-partition JOIN, ML training). Tập trung tài nguyên tính toán mạnh tại 1 node chuyên biệt.
- **Nhược điểm**: Tốn bandwidth mạng lớn, tăng latency. Có thể gây nghẽn mạng (network congestion) khi data volume cao. Node thực thi query cần đủ bộ nhớ để chứa toàn bộ dữ liệu kéo về.
- **Ví dụ**:
  - Truy vấn `SELECT * FROM orders o JOIN customers c ON o.customer_id = c.id` — nếu `orders` và `customers` nằm ở node khác nhau và không partition theo `customer_id`, node thực thi JOIN phải kéo data từ cả 2 node về vì dữ liệu cục bộ không đủ thông tin để thực hiện phép JOIN. Tương tự với các phép tính ML phức tạp cần toàn bộ dataset tập trung tại 1 nơi.

##### C. So sánh và Hybrid Approach

| | Push query to data | Pull data to query |
|---|---|---|
| **Network I/O** | Thấp (chỉ gửi kết quả) | Cao (gửi raw data) |
| **Yêu cầu CPU tại data node** | Cao | Thấp |
| **Phù hợp với** | Filter, Aggregation, simple query | Complex JOIN, ML, cross-partition query |
| **Ví dụ DBMS** | CockroachDB, TiDB | Spark SQL (shuffle phase) |

> 📌 **Thực tế**: Hầu hết các hệ thống distributed database sử dụng **Hybrid approach** — push được phần nào thì push (filter, partial aggregation tại data node), phần nào buộc phải pull thì pull (cross-node JOIN, final aggregation). Ví dụ: Spark SQL thực hiện predicate pushdown (đẩy điều kiện WHERE xuống data source) nhưng vẫn phải shuffle data giữa các node khi thực hiện JOIN hoặc GROUP BY trên key khác partition key.


#### 18.3. Database partitioning
- **Split database vào multi resource**:
   - Chia sẻ tải trên nhiều phần cứng: Disk, CPU, RAM.
   - **Physical partitioning** (Phân mảnh vật lý) và **Logical partitioning** (Phân mảnh logic):
      - Ví dụ: Kiến trúc **Shared nothing** $\rightarrow$ Thường đi với Physical partitioning: mỗi node tự quản lý partition trên thiết bị lưu trữ riêng của nó. Kiến trúc **Shared disk** $\rightarrow$ Thường đi với Logical partitioning: các node chịu trách nhiệm tính toán xử lý cho từng phần logic khác nhau nhưng dữ liệu vẫn save về chung 1 storage tập trung.

- **Naive table partitioning**
   - 1 table được assign hoàn toàn cho riêng 1 node.
   - **Giả định**: Các table có tính độc lập cao, sẽ không xảy ra (hoặc rất hiếm) các câu lệnh JOIN giữa các table nằm ở những node khác nhau.

- **Horizontal partitioning (Sharding - Phân mảnh ngang)**
   - Chia một data table thành nhiều phần (shard), trong đó mỗi phần chứa một tập hợp các rows khác nhau. Khóa để quyết định việc đưa row vào shard nào gọi là **Partition Key**.
   - **Các chiến lược cơ bản**:
      - **Hashing**: Tính hash của partition key (VD: `hash(key) % số_node`).
      - **Range**: Phân vùng dữ liệu dựa theo dải giá trị (VD: User ID từ 1-1000 $\rightarrow$ Node 1).
      - **Predicate (List)**: Phân vùng theo một danh sách cụ thể, gán cho mảng biến (VD: Region = 'Asia' $\rightarrow$ Node 1).
      - **Round Robin**: Chia bài tuần tự, phân bổ đều data vào tuần tự các node một cách vòng tròn.
   - **Vấn đề Rebalancing**: Khi add thêm hoặc remove node, hàm hashing (`mod N`) hoặc kích cỡ dải range thay đổi nghiêm trọng, dẫn đến phải rải lại/di dời dữ liệu hàng loạt.
   - **Giải pháp tối ưu quá trình Rebalance**:
      - **Consistent Hashing**:
         - **Ý tưởng**: Băm cả Data Key và Node ID lên một vòng tròn băm (hash ring/cycle). Mỗi node sẽ quản lý các data key nằm giữa điểm của nó và node liền kề trước đó.
         - **Khi add thêm partition (node)**: Tách nhỏ vòng tròn tại vị trí đặt của node mới. Chỉ cần di chuyển phần data từ node cũ kề cận sang node mới, các node khác ngoài vùng không bị ảnh hưởng.
         - Ứng dụng nổi tiếng: Cassandra, DynamoDB, Riak.
      - **Rendezvous Hashing (Highest Random Weight Hashing)**:
         - **Ý tưởng**: Phân bổ key bằng điểm trọng số: `Hash = hash(key + node_identifier)`. Cho mỗi key, node nào có tính ra giá trị lớn nhất (ranking cao nhất) thì key sẽ thuộc về node đó.
         - **Khi add thêm node mới**: Tính và chấm điểm lại hash của mọi key đối với riêng node mới. Cho từng key, nếu node mới đạt điểm cao hơn node cũ đang chứa key đó (ranking tốt hơn) thì chỉ chuyển các data này sang. Giảm thiểu tối đa sự xáo trộn so với Hash truyền thống.

- **Shared disk partitioning**
   - Mở rộng từ Share disk architecture.
   - Tất cả các compute node cùng tiến hành phân vùng logic để chia tải, xử lý dữ liệu từ một hệ thống đĩa chung.
   - **Đặc điểm**: Giảm gánh nặng tái cấu trúc khi thêm node vì dữ liệu trung tâm không di chuyển. Tuy nhiên, lưu trữ chia sẻ có thể trở thành bottleneck ở điểm Disk I/O nếu truy cập quá dày đặc.

#### 18.4. Database Replication (Cơ chế nhân bản)
- **Khái niệm**: Là việc lưu trữ các bản sao (copy) của cùng một tập dữ liệu (dataset) trên nhiều nodes (máy chủ) khác nhau thông qua mạng network.
- **Mục đích cốt lõi**:
   - **High Availability (Sẵn sàng cao)** & **Fault Tolerance (Chịu lỗi)**: Nếu 1 node bị rãnh rớt mạng hoặc hỏng hóc, hệ thống có thể lập tức fail-over chuyển hướng cung cấp dữ liệu ở các node khác.
   - **Read Scalability (Mở rộng khả năng đọc)**: Chia sẻ gánh nặng (Load balance) các truy vấn `SELECT` cho nhiều điểm read-only node. Phù hợp với Read-heavy workload.
   - **Giảm Latency**: Đặt các replica ở nhiều khu vực địa lý khác nhau để người dùng truy cập trực tiếp vào node vật lý gần họ nhất.

##### A. Các mô hình Replication
1. **Single-Leader (Primary-Replica / Master-Slave)**:
   - **Cơ chế**: Dành một node duy nhất đóng vai trò Leader (Master) được phép nhận truy cập GHI (Write). Sau khi thay đổi, Leader truyền bản ghi (replication log) tới tất cả các node còn lại (Followers / Replicas). Các truy cập vào Follower bị giới hạn ở ngưỡng ĐỌC (Read-only).
   - **Đặc điểm**: Rất thông dụng (vd: MySQL, PostgresSQL mặc định). Dễ triển khai, nhất quán cao. Điểm yếu là Leader trở thành điểm chết duy nhất (Single Point of Failure) nếu tiến trình failover tự động cấu hình không vững.
2. **Multi-Leader (Master-Master)**:
   - **Cơ chế**: Có lớn hơn 1 node đóng vai trò Leader. Mỗi Leader đều có thể accept data write độc lập và sync chéo qua lại cho cụm Leader/Follower.
   - **Đặc điểm**: Thích hợp cho môi trường chia tách Multi-Datacenter (Mỗi châu lục có 1 Datacenter với Leader cục bộ riêng) giúp né độ trễ write. **Rủi ro to lớn**: Vấn đề giải quyết xung đột ghi chép (Write Conflict - Khi 2 user sửa cùng 1 row ở 2 Datacenter khác nhau trong cùng 1 mili-giây, dẫn đến chia rẽ dữ liệu).
3. **Leaderless (Quorum-based)**:
   - **Cơ chế**: Mọi node đều bình đẳng. Khi Client update, nó trực tiếp đẩy Write song song (broadcast) vào nhiều nodes. Khi cần Read, nó cũng gửi lấy data từ nhiều nodes để vá lỗi.
   - **Đặc điểm**: Áp dụng quy tắc số đông **Quorum ($W + R > N$)** để đọc dữ liệu chuẩn xác. Ví dụ: Có 3 node ($N=3$), bắt buộc cấu hình ghi vào ít nhất 2 node hoàn thành ($W=2$). Khi đọc, cũng yêu cầu phải tham chiếu đủ ít nhất từ 2 node ($R=2$). Lúc này chắc chắn $W+R=4 > 3$, do đó cụm trả về sẽ có ít nhất 1 node chắp nối thành công bản ghi mới nhất. Cơ chế này đạt tính High Availability cực mạnh trong môi trường chập chờn (Cassandra, DynamoDB sử dụng mô hình này).

##### B. Propagation
- **Propagation Schema**
Xác định mức cam kết bảo chứng data giữa Leader và Follower:

- **Synchronous (Đồng bộ hoàn toàn)**:
   - Leader ghi data tại cục bộ $\rightarrow$ Phải chờ các Follower apply thành công và phản hồi xác nhận $\rightarrow$ Mới return `Success` cho Client.
   - *Ưu điểm*: An toàn 100%. Data không bao giờ bay màu nếu Leader chết đột ngột. Follower không bao giờ bị lệch nhịp.
   - *Nhược điểm*: Hiệu năng Write kém nhất. Rất dễ treo ứng dụng (Unavailable) nếu bất kỳ Follower nào bị đứt cáp và nghẽn phản hồi. Thường không ai xài Synchronous toàn phần cả hệ thống.
- **Asynchronous (Bất đồng bộ - Thông dụng nhất)**:
   - Leader áp dụng thay đổi tại đĩa cục bộ $\rightarrow$ Lập tức return `Success` ngay cho Client $\rightarrow$ Log thay đổi được Follower pull và apply ngầm ở Background phụ.
   - *Ưu điểm*: Hiệu năng tuyệt vời không độ trễ. Leader không cần lo Follower sống chết ra sao.
   - *Nhược điểm*: Khiến dấy lên khái niệm **Replication Lag** (Hành động người dùng sửa profile ở Leader nhưng khi Load tự động nhảy sang Load-balancer của Follower chậm nhịp, khiến người dùng lầm tưởng lệnh save thất bại (Inconsistency)). Nguy cơ Data Loss nếu Leader Crash vĩnh viễn trước khi Log async chạy qua nhánh kia.
- **Semi-synchronous (Bán đồng bộ - Thỏa hiệp vàng)**:
   - Pha trộn. Cấu hình yêu cầu Leader phải chờ cho đến khi có **đúng MỘT (hoặc cấu hình mức tối thiểu)** Follower xác nhận ghi nhận thành công, còn mớ Follower khác để chúng nó Asynchronous tự túc.
   - Vừa bảo toàn được tốc độ hệ thống, vừa cam kết tính High Availability vì ta chắc chắn có một bản gác tạm ở server phái sinh khác ngoài Leader.

- **Propagation Timing**
  - Continous: Gửi message logs ngay thời điểm nó đượ tạo, cần gửi cả commit/abort message(phần lớn system sử dụng kiến trúc này)
  - On commit: Chỉ gửi sau khi đã commit, không tốn thời gian gửi các abort txn


##### C. K-Safety (Độ an toàn K)
- **Khái niệm**: K-Safety là một thước đo / tiêu chuẩn cấu hình khả năng **Chịu lỗi (Fault Tolerance)** trong các cơ sở dữ liệu xử lý phân tán (như Vertica, Cassandra). Giá trị **K** đại diện cho số lượng node có thể bị sập (crash) cùng một lúc mà cụm database vẫn hoạt động bình thường, không bị mất mát bất kỳ dữ liệu nào.
- **Cách thức đạt được**: Để đạt được độ an toàn `K`, hệ thống bắt buộc phải duy trì lưu trữ ít nhất **$K+1$ bản sao (replicas)** của dữ liệu phân tán trên các node độc lập.
- **Phân loại**:
   - `K-Safety = 0`: Cấu hình hệ thống không có Replica (tương đương kiến trúc Single Node). Chỉ 1 máy hỏng là tiêu tùng dữ liệu.
   - `K-Safety = 1`: Hệ thống duy trì ít nhất 2 bản sao phân tán. Chịu được rủi ro mất đột ngột 1 node. Đây là tiêu chuẩn vàng tối thiểu của các môi trường Production.
   - `K-Safety = 2`: Dữ liệu phân bổ ở 3 node. Dù 2 node chết cùng lúc thì hệ thống vẫn truy xuất được dữ liệu trọn vẹn ở node còn lại.
- **Đặc trưng**: Nếu số node hỏng **lớn hơn K** được quy định, một số Database (như Vertica) sẽ buộc phải tắt luôn hệ thống (Shutdown an toàn / Read-only) để ngăn chặn phát sinh hiện tượng dữ liệu rác không đồng nhất.

#### 18.5. Transaction coordination
- Khi txn trên nhiều node, cần cơ chế phối hợp để đảm bảo tính nguyên tử
   - Data dạng copy replication hay mỗi node có 1 loại data
- Kiến trúc: 
    - Centralized: Người điều phối trung tâm thông qua:
       - Thông báo tới các node
       - Chỉ commit khi tất cả các node đồng ý
    - Decentralized: Mỗi node tự quản lý
       - Leader thông báo tới các node commit
    - Phần lớn các dbms sử dụng hybrid khi chúng định kì chọn 1 node để làm người điều phối tạm thời
Federated(Liên bang)
   - Sử dụng 1 middle ware giữa application và database
   - Middleware sẽ điều phối transaction
   - Federated phối hợp nhiều database khác nhau

#### 18.6. Atomic Commit Protocols (Giao thức cam kết nguyên tử)
- Tiền đề:
    - Tất cả các node trong 1 distributed DBMS are well-behaved
    - Nếu không tin tưởng node, bạn cần sử dụng thuật toán ..
       -> Blockchain là 1 ví dụ tiêu biểu
**Ngữ cảnh giải quyết**: Khi một hệ thống phân tán thực thi một Transaction (giao dịch) có dữ liệu vắt qua nhiều Nodes (hoặc nhiều Partitions) khác nhau, làm sao để đảm bảo tính chất **Atomicity** của hệ ACID? Nghĩa là lệnh cập nhật phải chốt hạ: **Hoặc TẤT CẢ các nodes cùng commit thành công, hoặc tất cả đều phải Abort (hủy bỏ)**.

##### A. Two-Phase Commit (2PC - Giao thức 2 pha)
Đây là thuật toán thống trị và phổ biến nhất được các Database hiện tại sử dụng cho giao dịch phân tán. Mô hình chia làm 1 **Coordinator (Người điều phối)** và nhiều **Participants (Các node chứa dữ liệu)**.

- **Phase 1: Prepare (Pha chuẩn bị)**
   - Coordinator gửi thông điệp `PREPARE` tới tất cả các Participants.
   - Khi nhận lệnh, Participant phải bắt đầu cố định tài nguyên (tạo các write lock, ép flush ghi WAL xuống đĩa cứng). 
   - Nếu mọi thứ mượt mà, nó trả lời `YES` (Đồng nghĩa: "Tôi đã rào khóa data, tôi thề nếu anh ra lệnh commit là tôi làm được 100%"). Nếu có bất cứ trục trặc / fail lock nào, nó trả lời `NO`.
- **Phase 2: Commit / Abort (Pha chốt hạ)**
   - **Tình huống Abort**: Chỉ cần có **ít nhất 1** Participant trả lời `NO` (hoặc timeout bặt vô âm tín do vấp mạng), Coordinator ra lệnh `ABORT` đồng loạt tới tất cả các node để vứt bỏ transaction, roll-back dữ liệu.
   - **Tình huống Commit**: Nếu **TẤT CẢ** 100% độ hình trả lời `YES`. Coordinator đưa ra kết luận chốt hạ `COMMIT`. Việc đầu tiên nó làm là tự viết log chứng nhận `COMMIT` xuống đĩa của nó làm bằng chứng, rồi xả lệnh cho tất cả các Participants cùng tiến hành `COMMIT`.
   - Các Participant thực hiện lệnh thao tác dữ liệu xong, giải phóng Lock và báo `ACK` (Acknowledge) về cho điều phối viên. Kết thúc.

- **Nhược điểm chí mạng của 2PC (The Blocking Problem)**:
   - Nếu Coordinator chết / sập ngay tại đầu **Pha 2** ở khoảnh khắc nó vừa ra được quyết định trong não là sẽ `COMMIT/ABORT` nhưng *chưa kịp báo* cho toàn hệ thống $\rightarrow$ Mọi Participants rơi vào tình huống **"Tiến thoái lưỡng nan"**: Data đang bị **giữ Lock**, đã lỡ hứa `YES`, lại mất kết nối không phán đoán được anh điều phối viên đã chết thật chưa hay quyết định ra sao nên không dám tự tiện commit cũng chả dám abort. Cả hệ thống có điểm mù, giam tài nguyên khóa cứng ngắc chờ đợi mòn mỏi. Đặc điểm này gọi là biến Coordinator thành **Single Point of Failure**.

- **Optimized**
   - Early-Prepare voting(rare): 
   - Early ack after prepare: Send successful ngay sau khi các nốt đều báo ok: Cơ chế ghi log và redo nữa chứ???
##### B. Three-Phase Commit (3PC - Giao thức 3 pha)
- **Cơ chế**: Sinh ra để khắc phục nhược điểm "Treo cứng" của 2PC. Bằng cách cài thêm quy định về Timeout chặt chẽ hơn và chèn một pha đệm gọi là **Pre-Commit** nằm ở giữa. (Sơ đồ: `CanCommit` $\rightarrow$ `PreCommit` $\rightarrow$ `DoCommit`).
- **Đặc điểm**: Nhờ có pha đệm pre-commit, khi mạng bị sụp rách Coordinator, các participants có thể dọn dẹp và phân tích thông qua timeout để tự đưa ra quyết định commit/abort tập thể, khắc phục triệt để Blocking state.
- **Thực tế phũ phàng**: Lượng I/O cost đội lên khổng lồ, số chuyến khứ hồi mạng (Network round-trip) nhiều khiến nó quá trễ, gặp mạng Internet chập chờn thì thảm họa. **Kết luận: Hầu như không có hệ cơ sở dữ liệu thực tiễn nào triển khai xài 3PC**. Thay vào đó, các hệ NewSQL (Spanner, TiDB, CockroachDB) vẫn tiếp tục xài 2PC nhưng gia cố lớp khiên bằng cách đắp giải thuật đồng thuận (Raft / Paxos algorithm) làm **bảo kê cho Coordinator**, biến bộ não điều phối này thành "bất tử". Khắc triệt để Single Point of failure.


##### C. Viewstamped replication

##### D. Paxos

> 📌 **Giải đáp câu hỏi**: *"Nếu gửi abort thì sao? Data có như nhau trên các node không?"*
>
> Paxos **không phải** là Atomic Commit Protocol (như 2PC) nên không có khái niệm "commit/abort transaction" theo nghĩa ACID. Paxos là **Consensus Algorithm** — mục tiêu là để các node **đồng thuận về một giá trị duy nhất**. Một khi consensus đạt được, giá trị đó được ghi nhận vĩnh viễn.

###### Vấn đề Paxos giải quyết

Trong một hệ thống phân tán **(N nodes, có thể crash bất kỳ lúc nào)**, làm sao để đảm bảo:
- **Safety**: Chỉ **một** giá trị được chọn — không bao giờ có 2 node chọn 2 giá trị khác nhau
- **Liveness**: Nếu còn **đa số node** (`> N/2`) còn sống, tiến trình cuối cùng phải hoàn thành

###### Các vai trong Paxos

| Vai | Mô tả |
|---|---|
| **Proposer** | Đề xuất một giá trị để các node đồng thuận |
| **Acceptor** | Nhận và phê duyệt/từ chối đề xuất. Là node thực sự bỏ phiếu |
| **Learner** | Nhận thông báo về giá trị đã được chọn để "học" theo |

> Trong thực tế, 1 node thường đóng cả 3 vai cùng lúc.

###### Cơ chế hoạt động — 2 Phase

**Phase 1: Prepare / Promise**

```
Proposer                         Acceptors
   │── PREPARE(n) ──────────────▶│  n là proposal number, phải tăng dần
   │◀─ PROMISE(n, v_accepted) ───│  hứa không accept proposal nào có number < n
```

- Proposer chọn **proposal number `n`** (lớn hơn mọi `n` từng dùng trước đó)
- Gửi `PREPARE(n)` tới tất cả Acceptors
- Acceptor khi nhận `PREPARE(n)`:
  - Nếu `n > max_n_seen` → trả lời `PROMISE(n, v_accepted)` kèm giá trị đã accept trước đó (nếu có)
  - Đồng thời **cam kết** sẽ không bao giờ accept proposal nào có `n' < n` trong tương lai
  - Nếu `n ≤ max_n_seen` → từ chối (không trả lời hoặc gửi NACK)

**Phase 2: Accept / Accepted**

```
Proposer                         Acceptors              Learners
   │── ACCEPT(n, v) ────────────▶│
   │◀─ ACCEPTED(n, v) ───────────│── ACCEPTED(n, v) ───▶│
```

- Nếu Proposer nhận đủ `PROMISE` từ **quorum (đa số)** Acceptors:
  - Nếu có Acceptor nào gửi kèm `v_accepted` → Proposer **PHẢI dùng giá trị đó** (không được tự chọn) ← điểm mấu chốt của Paxos
  - Nếu không ai có `v_accepted` → Proposer tự chọn giá trị `v` mình muốn
- Gửi `ACCEPT(n, v)` tới Acceptors
- Acceptor nhận `ACCEPT(n, v)`:
  - Nếu chưa hứa với proposal nào lớn hơn → chấp nhận, lưu `(n, v)`, thông báo Learners
  - Nếu đã hứa với `n' > n` → từ chối

###### Quorum — Chìa khóa của Paxos

**Quorum = bất kỳ tập nào có hơn N/2 nodes.** Hai quorum bất kỳ **luôn có ít nhất 1 node chung** — đây là đảm bảo toán học cốt lõi:

```
5 nodes: A, B, C, D, E

Quorum Phase 1: {A, B, C} — 3/5 nodes
Quorum Phase 2: {B, C, D} — 3/5 nodes
Giao nhau:      {B, C}    ← Luôn có node nhớ về round trước!
```

Nhờ vậy, nếu round trước đã chọn được giá trị, round mới **không thể chọn giá trị khác** (vì sẽ có node nhắc "round trước đã accept `v` rồi, anh phải dùng `v` đó").

###### Ví dụ cụ thể

```
Cluster 3 nodes: A (Proposer), B, C (Acceptors)

=== ROUND 1: A đề xuất "X" ===
1. A → PREPARE(n=1) → B, C
2. B → PROMISE(n=1, Ø)     (chưa accept ai)
   C → PROMISE(n=1, Ø)     (chưa accept ai)
   ✓ Quorum đạt (2/3)

3. Không ai có v_accepted → A chọn v="X"
4. A → ACCEPT(n=1, v="X") → B, C
5. B, C → ACCEPTED(n=1, v="X") → Learners
   ✓ Consensus: "X" được chọn vĩnh viễn!

=== ROUND 2 (đồng thời): D đề xuất "Y" ===
1. D → PREPARE(n=2) → B, C
2. B → PROMISE(n=2, v_accepted="X")  ← BÁO CÁO giá trị đã accept!
   C → PROMISE(n=2, v_accepted="X")
3. D thấy v_accepted="X" → BUỘC PHẢI dùng v="X", không được chọn "Y"
4. D → ACCEPT(n=2, v="X")
   ✓ Safety đảm bảo: "X" luôn là giá trị duy nhất được chọn!
```

###### Liveness Issue — Dueling Proposers (Livelock)

Paxos có thể bị **livelock** nếu 2 proposers liên tục cạnh tranh nhau:

```
P1: PREPARE(n=1) → quorum đồng ý
P2: PREPARE(n=2) → làm n=1 invalid, quorum đồng ý P2
P1: PREPARE(n=3) → làm n=2 invalid, quorum đồng ý P1
P2: PREPARE(n=4) → ...
→ Vô tận, không ai commit được!
```

**Giải pháp**: Chọn **1 Distinguished Leader** — chỉ 1 proposer được đề xuất tại một thời điểm. Đây là nền tảng của **Multi-Paxos**.

###### Multi-Paxos — Thực tế triển khai

**Basic Paxos** chỉ đồng thuận 1 giá trị duy nhất. **Multi-Paxos** mở rộng để đồng thuận một **chuỗi giá trị (log entries)** liên tiếp:

- Khi Leader ổn định → **bỏ qua Phase 1** cho các round tiếp theo (chỉ chạy Phase 2)
- Mỗi log slot là 1 instance Paxos độc lập
- **Raft** (2014) về bản chất là Multi-Paxos được thiết kế lại cho dễ hiểu và implement hơn

###### So sánh Paxos vs 2PC

| | **Paxos (Consensus)** | **2PC (Atomic Commit)** |
|---|---|---|
| **Mục tiêu** | Đồng thuận về **một giá trị** trong log | Đảm bảo **ACID** cho transaction phân tán |
| **Fault tolerance** | Tiếp tục nếu **đa số** còn sống | **Bị block** nếu Coordinator chết |
| **Khi node fail** | Bầu leader mới, tiếp tục | Hệ thống treo đến khi Coordinator recover |
| **Dùng khi nào** | Replication log, leader election | Distributed transaction (cross-shard) |
| **Ứng dụng** | Raft, ZAB (ZooKeeper), etcd | MySQL Cluster, Google Spanner |

> 📌 **Quan hệ thực tế**: **Google Spanner, CockroachDB** dùng **2PC cho Atomic Commit** (đảm bảo ACID), nhưng dùng **Paxos/Raft để làm Coordinator "bất tử"** — giải quyết chính xác điểm yếu Single Point of Failure của 2PC thuần túy.

##### E. ZAB

##### F. Raft
Cải tiến của Paxos

#### 18.7. CAP Theorem

**CAP Theorem** (Brewer's Theorem, 2000) phát biểu rằng một hệ thống phân tán **không thể đảm bảo đồng thời cả 3 thuộc tính** sau cùng một lúc — chỉ có thể đảm bảo tối đa 2 trong 3:

| Thuộc tính | Ký hiệu | Ý nghĩa |
|---|---|---|
| **Consistency** | C | Mọi node đều trả về cùng một giá trị dữ liệu mới nhất cho mọi request. Đọc sau ghi luôn thấy bản ghi mới nhất. |
| **Availability** | A | Mọi request hợp lệ đều nhận được phản hồi (không timeout, không lỗi) — dù dữ liệu có thể không phải mới nhất. |
| **Partition Tolerance** | P | Hệ thống tiếp tục hoạt động dù mạng bị phân mảnh (một số node mất kết nối với nhau). |

> 📌 **Thực tế**: Partition Tolerance là **BẮT BUỘC** trong mọi hệ phân tán thực tế (mạng luôn có thể bị đứt). Do đó, câu hỏi thực chất là: **Khi xảy ra Network Partition — chọn C hay A?**

##### A. Khi Network Partition xảy ra

```
Trước Partition:                   Sau khi mạng đứt giữa Node1 và Node2:
┌─────────┐   ┌─────────┐          ┌─────────┐     ✗     ┌─────────┐
│  Node1  │◄──►  Node2  │    →     │  Node1  │────────   │  Node2  │
│ x = 10  │   │ x = 10  │          │ x = 10  │           │ x = 10  │
└─────────┘   └─────────┘          └─────────┘           └─────────┘
                                   Client A               Client B
                                   ghi x = 20             đọc x = ?
```

Khi `Client A` ghi `x = 20` lên `Node1` và `Client B` đọc từ `Node2` — hệ thống phải **lựa chọn**:

---

##### B. Choice 1: CP — Ưu tiên Consistency (Halt the System)

- **Cơ chế**: Khi xảy ra Network Partition, **dừng nhận Write** ở bất kỳ node nào **không có majority** (đa số quorum). Node thiểu số sẽ từ chối phục vụ hoặc trả về lỗi.
- **Kết quả**: Không bao giờ trả về dữ liệu stale (cũ). Nhưng hệ thống trở nên **Unavailable** trong thời gian partition.
- **Ví dụ thực tế**: **HBase, ZooKeeper, etcd (Raft/Paxos)** — các node thiểu số sẽ từ chối đọc/ghi và throw error.

```
Node1 (majority): Tiếp tục nhận ghi, x = 20
Node2 (minority): Từ chối phục vụ → Client B nhận lỗi 503
→ Hệ thống nhất quán nhưng một phần không khả dụng
```

---

##### C. Choice 2: AP — Ưu tiên Availability (Allow Split)

- **Cơ chế**: Tất cả các node tiếp tục nhận và phục vụ request ngay cả khi bị phân mảnh. Chấp nhận rằng các node có thể có dữ liệu **diverge (phân kỳ)** trong thời gian partition.
- **Kết quả**: Luôn Available — nhưng có thể đọc **stale data**. Cần cơ chế **conflict resolution** sau khi partition hồi phục.
- **Ví dụ thực tế**: **Cassandra, DynamoDB, CouchDB, Riak**.

```
Node1: x = 20  (Client A đã ghi)
Node2: x = 10  (vẫn giữ giá trị cũ)
→ Client B đọc từ Node2 → thấy x = 10 (stale data)
→ Sau khi mạng hồi phục: phải resolve conflict!
```

---

##### D. Conflict Resolution sau khi Partition kết thúc

Khi 2 node giao tiếp lại, chúng phát hiện ra dữ liệu đã **diverge** — cần cơ chế để quyết định version nào "thắng":

###### 1. Last Write Wins (LWW)

- **Nguyên lý**: Update nào có **timestamp mới nhất** (wall clock time) thì thắng — bản ghi cũ bị ghi đè.
- **Cài đặt**: Mỗi write đính kèm timestamp theo đồng hồ hệ thống. Khi merge: `pick max(timestamp)`.
- **Ưu điểm**: Đơn giản, dễ implement.
- **Nhược điểm nghiêm trọng**:
  - **Clock skew**: Đồng hồ trên các máy khác nhau không bao giờ đồng bộ hoàn toàn. Node bị lệch giờ có thể "thắng" dù write cũ hơn.
  - **Data loss ngầm**: Một write hợp lệ bị ghi đè mà không có cảnh báo hay thông báo lỗi nào.
- **Dùng khi**: Chấp nhận mất một số write, cần simplicity. Ví dụ: Cassandra (mặc định LWW).

```
Node1: x=20 tại T=100ms
Node2: x=15 tại T=105ms (đồng hồ lệch +5ms)
→ LWW chọn x=15 (T lớn hơn) → Mất write x=20 hoàn toàn!
```

###### 2. Vector Clock (Đồng hồ Vector)

- **Vấn đề cần giải quyết**: LWW dùng thời gian thực (wall clock) rất không đáng tin trong môi trường phân tán. Vector Clock thay bằng **logical clock** để theo dõi quan hệ **nhân quả (causality)** giữa các event — phát hiện chính xác khi nào 2 write thực sự "đồng thời" (concurrent) hay có thứ tự trước sau.

- **Cấu trúc**: Mỗi node duy trì một **vector** gồm các counter — một counter cho mỗi node trong cluster.

```
Cluster 3 nodes: N1, N2, N3
Vector Clock của 1 object: [N1=x, N2=y, N3=z]

- Mỗi khi node Ni xử lý 1 write: tăng counter Ni lên 1.
- Khi nhận message từ node khác: merge bằng cách lấy max từng phần tử.
  merge([2,1,0], [1,2,0]) → [max(2,1), max(1,2), max(0,0)] = [2,2,0]
```

- **Ví dụ minh họa**:

```
Trạng thái ban đầu (Client ghi x=10 vào N1, N1 sync sang N2):
  N1: VC=[1,0,0], x=10
  N2: VC=[1,0,0], x=10

=== Network Partition xảy ra ===

Client A ghi x=20 vào N1:
  N1: VC=[2,0,0], x=20

Client B ghi x=15 vào N2:
  N2: VC=[1,1,0], x=15

=== Mạng hồi phục — N1 và N2 liên lạc lại ===

N1 gửi (VC=[2,0,0], x=20) tới N2
N2 so sánh với local (VC=[1,1,0], x=15):

  So sánh từng chiều:
    - N1 counter: 2 > 1  → VC=[2,0,0] "tiến" hơn ở chiều N1
    - N2 counter: 0 < 1  → VC=[1,1,0] "tiến" hơn ở chiều N2
  → Không có VC nào dominates cái còn lại hoàn toàn
  → ⚠ CONFLICT! Hai write này là ĐỒNG THỜI (concurrent)
  → Cần ứng dụng hoặc người dùng giải quyết
```

- **Quy tắc so sánh Vector Clock**:

| Quan hệ | Điều kiện | Ý nghĩa |
|---|---|---|
| **A → B** (A trước B) | Mọi `VC_A[i] ≤ VC_B[i]` và tồn tại ít nhất 1 `VC_A[i] < VC_B[i]` | A xảy ra trước B (có quan hệ nhân quả) → B an toàn ghi đè A |
| **A ← B** (B trước A) | Mọi `VC_B[i] ≤ VC_A[i]` và tồn tại ít nhất 1 `VC_B[i] < VC_A[i]` | B xảy ra trước A → A an toàn ghi đè B |
| **A ∥ B** (Đồng thời) | Không cái nào dominates cái kia | Không có quan hệ nhân quả → **Conflict thực sự, cần resolve** |

- **Xử lý Conflict khi phát hiện `A ∥ B`**:
  - **Merge tự động**: Nếu data structure hỗ trợ (VD: Set → Union, Counter → Sum). Ví dụ: **CRDT (Conflict-free Replicated Data Types)** trong Riak, Redis, và các database hiện đại.
  - **Trả về cả 2 version cho client**: Client application tự quyết định version nào đúng (chiến lược của **Amazon DynamoDB** và **Riak** — trả về "siblings"). Phù hợp với Shopping Cart: merge 2 giỏ hàng bằng Union.
  - **Last Write Wins as fallback**: Dùng LWW chỉ khi không thể resolve theo cách khác.

- **Nhược điểm của Vector Clock**:
  - **Vector size tăng theo số node**: Cluster 1000 node → vector 1000 phần tử gắn theo mỗi object → tốn bộ nhớ.
  - **Phức tạp để implement** đúng cách, đặc biệt khi node join/leave cluster.
  - **Dotted Version Vectors** (Riak) là phiên bản tối ưu hơn, giải quyết vấn đề false conflict.

---

##### E. Tổng kết: CP vs AP Systems

| | **CP Systems** | **AP Systems** |
|---|---|---|
| **Ưu tiên** | Consistency | Availability |
| **Khi partition** | Reject/lỗi một phần request | Tiếp tục phục vụ, data có thể stale |
| **Conflict** | Không xảy ra (writes bị block ở minority) | Cần conflict resolution khi partition hồi phục |
| **Phù hợp** | Financial data, metadata, config, distributed lock | Shopping cart, social feed, DNS, session store |
| **Ví dụ** | HBase, ZooKeeper, etcd, Google Spanner | Cassandra, DynamoDB, CouchDB, Riak |

> 📌 **Lưu ý quan trọng — PACELC Theorem**: CAP chỉ mô tả behavior khi có Partition. **PACELC** (2012) bổ sung: ngay cả khi **không có Partition (PC)**, hệ thống phải trade-off giữa **Latency (L)** và **Consistency (C)**. Ví dụ: Synchronous replication = strong consistency nhưng latency cao; Asynchronous replication = latency thấp nhưng chỉ đạt eventual consistency.

#### 18.8. Join in distributed system
   - **Broadcast Join (Replication Join):** 
      - Áp dụng: Khi có 1 bảng đủ nhỏ (có thể fit vừa RAM của các worker node).
      - Cơ chế: Ta broadcast (sao chép và gửi) toàn bộ data từ bảng nhỏ đó tới tất cả các node đang chứa partition của bảng lớn, sau đó thực hiện join độc lập ở từng node.
      - Kết quả sau đó được merge lại. Ưu điểm là tránh được hoàn toàn việc xáo trộn dữ liệu qua mạng.
   - **Shuffle Join (Hash Shuffle Join):**
      - Áp dụng: Khi cả 2 bảng data đều quá lớn và ban đầu không được partition sẵn theo join key.
      - Cơ chế: Ta thực hiện xáo trộn (shuffle) lại data qua mạng. Hệ thống sẽ hash (băm) join key của cả 2 bảng, các dòng có cùng hash value sẽ được gửi qua mạng về chung một node. Khi dữ liệu hội tụ đủ, các node mới tiến hành join.
      - Nhược điểm: Quá trình Shuffle cực kì chậm và tốn kém tài nguyên (chi phí Network I/O để truyền data và Disk I/O để ghi data tạm ra đĩa tránh out of memory).
      - Tối ưu: Có một số dịch vụ giúp ta tách riêng khâu shuffle data để xử lý độc lập (như *External Shuffle Service*), giúp lưu kết quả trung gian ổn định để các node thực thi step tiếp theo không bị chết chùm nếu gặp lỗi.


#### 18.8. Parquet File Format

##### A. Parquet là gì?

**Apache Parquet** là một định dạng file lưu trữ dữ liệu theo **cột (columnar storage format)**, được thiết kế đặc biệt để tối ưu cho các hệ thống **OLAP (Online Analytical Processing)** và xử lý dữ liệu lớn (Big Data).

> 📌 **Lưu ý**: "Parquet" đọc là /pɑːrˈkeɪ/ — lấy tên từ sàn gỗ ghép hoa văn (parquet flooring), ám chỉ cách dữ liệu được "ghép" theo cột một cách có cấu trúc.

**Row-based vs Columnar Storage:**

```
Row-based (CSV, JSON, MySQL row format):
┌──────┬──────┬────────┬────────┐
│ id=1 │ name │ age=25 │ sal=50 │  ← Row 1 (lưu liên tiếp)
│ id=2 │ name │ age=30 │ sal=60 │  ← Row 2 (lưu liên tiếp)
│ id=3 │ name │ age=28 │ sal=55 │  ← Row 3 (lưu liên tiếp)
└──────┴──────┴────────┴────────┘
→ Đọc 1 row = 1 lần I/O (tốt cho OLTP: SELECT * WHERE id=1)
→ Đọc 1 cột = phải scan TẤT CẢ rows (tệ cho analytics)

Columnar (Parquet):
┌──────────────────┐
│ id:  1, 2, 3     │  ← Cột id (lưu liên tiếp)
│ name: A, B, C    │  ← Cột name (lưu liên tiếp)
│ age: 25, 30, 28  │  ← Cột age (lưu liên tiếp)
│ sal: 50, 60, 55  │  ← Cột salary (lưu liên tiếp)
└──────────────────┘
→ Đọc 1 cột = 1 lần sequential I/O (tốt cho analytics: SELECT AVG(salary))
→ Đọc 1 row = phải ghép từ nhiều cột (tệ cho OLTP)
```

##### B. Tại sao Parquet phổ biến trong OLAP?

| Đặc điểm | Giải thích |
|---|---|
| **Column pruning** | Query chỉ cần 3/100 cột? Parquet chỉ đọc 3 cột đó, bỏ qua 97 cột còn lại → giảm I/O cực lớn |
| **Nén hiệu quả** | Dữ liệu cùng cột có cùng kiểu + pattern lặp → compression ratio cao hơn nhiều so với row-based |
| **Predicate pushdown** | Metadata chứa min/max → engine bỏ qua cả block dữ liệu không thỏa điều kiện WHERE |
| **Parallelism** | Row Groups độc lập → nhiều thread/node xử lý song song dễ dàng |
| **Immutable** | File write 1 lần, đọc nhiều lần — phù hợp với mô hình data lake (append-only) |

> 📌 **DBMS/Engine sử dụng Parquet**: Apache Spark, Apache Hive, Apache Impala, Presto/Trino, DuckDB, Amazon Athena, Google BigQuery (internal format tương tự), Snowflake, Databricks Delta Lake.

##### C. Cấu trúc phân cấp (Hierarchical Structure)

Một file Parquet được tổ chức theo 4 cấp:

```
┌─────────────────────────────────────────────────────────────────┐
│                        PARQUET FILE                             │
│                                                                 │
│  ┌─── Magic Number ("PAR1") ──────────────────────────────┐     │
│  │                                                        │     │
│  │  ┌─────────────── Row Group 1 ───────────────────┐     │     │
│  │  │                                               │     │     │
│  │  │  ┌─ Column Chunk: id ──┐  ┌─ Column Chunk: name ┐  │     │
│  │  │  │  ┌── Page 1 ──┐    │  │  ┌── Page 1 ──┐      │  │     │
│  │  │  │  │ Data values │    │  │  │ Data values │      │  │     │
│  │  │  │  │ (encoded +  │    │  │  │ (encoded +  │      │  │     │
│  │  │  │  │ compressed) │    │  │  │ compressed) │      │  │     │
│  │  │  │  └─────────────┘    │  │  └─────────────┘      │  │     │
│  │  │  │  ┌── Page 2 ──┐    │  │  ┌── Page 2 ──┐      │  │     │
│  │  │  │  │ ...         │    │  │  │ ...         │      │  │     │
│  │  │  │  └─────────────┘    │  │  └─────────────┘      │  │     │
│  │  │  └─────────────────────┘  └───────────────────────┘  │     │
│  │  └───────────────────────────────────────────────────┘  │     │
│  │                                                        │     │
│  │  ┌─────────────── Row Group 2 ───────────────────┐     │     │
│  │  │  ...                                          │     │     │
│  │  └───────────────────────────────────────────────┘     │     │
│  │                                                        │     │
│  │  ┌─── Footer (Metadata) ─────────────────────────┐     │     │
│  │  │  - Schema (tên cột, kiểu dữ liệu)            │     │     │
│  │  │  - Row Group metadata (offset, size)          │     │     │
│  │  │  - Column Chunk metadata (min/max, null count)│     │     │
│  │  │  - Page Index (offset của từng page)          │     │     │
│  │  └───────────────────────────────────────────────┘     │     │
│  │                                                        │     │
│  └─── Magic Number ("PAR1") ──────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘
```

**Giải thích từng cấp:**

| Cấp | Mô tả |
|---|---|
| **File** | Bao gồm Header (magic number `PAR1`), data blocks (Row Groups), và Footer chứa toàn bộ metadata |
| **Row Group** | Chia ngang tập dữ liệu thành các nhóm hàng (thường 128MB–1GB mỗi group). Là đơn vị chính cho **parallelism** — mỗi Row Group có thể được xử lý độc lập bởi 1 thread/node |
| **Column Chunk** | Trong mỗi Row Group, dữ liệu được tổ chức theo cột. Mỗi Column Chunk chứa **toàn bộ giá trị của 1 cột** trong Row Group đó |
| **Page** | Đơn vị nhỏ nhất (atomic unit) của storage (thường ~1MB). Encoding và compression được áp dụng **tại cấp page** |

##### D. Encoding (Mã hóa)

Encoding biến đổi dữ liệu thành dạng compact hơn **trước khi** nén (compression). Vì dữ liệu cùng cột có cùng kiểu và pattern, encoding cực kỳ hiệu quả:

| Encoding | Cơ chế | Hiệu quả khi |
|---|---|---|
| **Dictionary Encoding** | Thay giá trị lặp bằng integer key. VD: `["VN","US","VN","JP","VN"]` → Dict: `{0:"VN", 1:"US", 2:"JP"}`, Data: `[0,1,0,2,0]` | Cột có **low cardinality** (ít giá trị unique): country, status, gender |
| **Run-Length Encoding (RLE)** | Lưu giá trị + số lần lặp liên tiếp. VD: `[1,1,1,1,2,2,3]` → `[(1,4),(2,2),(3,1)]` | Dữ liệu **đã sort** hoặc có nhiều giá trị lặp liên tiếp |
| **Delta Encoding** | Lưu hiệu số giữa các giá trị liên tiếp. VD: `[1000,1001,1003,1006]` → base=1000, deltas=`[0,1,2,3]` | Cột **số tăng dần**: timestamp, auto-increment ID |
| **Bit Packing** | Nén integer dùng ít bit hơn. VD: giá trị max=7 chỉ cần 3 bit thay vì 32 bit | Giá trị nhỏ trong phạm vi hẹp |

> 📌 Parquet tự động chọn encoding phù hợp cho từng Column Chunk dựa trên kiểu dữ liệu và thống kê. Dictionary Encoding thường được dùng mặc định, fallback sang PLAIN nếu dictionary quá lớn.

##### E. Compression (Nén)

Sau khi encoding, page được nén tiếp bằng compression algorithm:

| Algorithm | Tốc độ nén | Tốc độ giải nén | Tỷ lệ nén | Ghi chú |
|---|---|---|---|---|
| **Snappy** | Nhanh | **Rất nhanh** | Trung bình | Mặc định trong Spark, Hive. Ưu tiên tốc độ đọc |
| **Gzip** | Chậm | Chậm | **Cao** | Khi cần tiết kiệm dung lượng (cold storage) |
| **Zstd** | Nhanh | Nhanh | **Cao** | Cân bằng tốt nhất giữa tốc độ và tỷ lệ nén. Ngày càng phổ biến |
| **LZO** | Nhanh | Nhanh | Trung bình | Legacy, ít dùng trong hệ thống mới |
| **Uncompressed** | — | — | 1:1 | Khi CPU là bottleneck, không muốn tốn cycle giải nén |

**Tại sao columnar nén tốt hơn row-based?**
```
Row-based: [1,"Alice",25,5000], [2,"Bob",30,6000], [3,"Alice",28,5500]
→ Dữ liệu xen kẽ kiểu: int, string, int, int → compression khó tìm pattern

Columnar:
  name: ["Alice","Bob","Alice"] → Dictionary: {0:"Alice",1:"Bob"} → [0,1,0] → 3 bytes!
  age:  [25, 30, 28]           → Delta: base=25, [0,5,3]         → vài bytes
  sal:  [5000, 6000, 5500]     → Delta: base=5000, [0,1000,500]  → vài bytes
```

##### F. Metadata & Statistics — Chìa khóa tối ưu query

Footer của file Parquet chứa metadata phong phú giúp query engine **bỏ qua dữ liệu không cần thiết** mà không cần đọc data thực:

```
Footer Metadata:
├── Schema: {id: INT64, name: STRING, age: INT32, salary: INT64}
├── Row Group 0:
│   ├── num_rows: 1,000,000
│   ├── Column "age":
│   │   ├── min: 18,  max: 65
│   │   ├── null_count: 0
│   │   └── offset: 0x1000, size: 2MB
│   └── Column "salary":
│       ├── min: 30000,  max: 200000
│       └── null_count: 50
├── Row Group 1:
│   ├── Column "age":
│   │   ├── min: 22,  max: 45
│   │   └── ...
```

**Tối ưu query nhờ metadata:**

1. **Column Pruning**: `SELECT name, salary FROM ...` → chỉ đọc 2 Column Chunks, bỏ qua id và age
2. **Row Group Skipping (Predicate Pushdown)**:
   ```
   SELECT * FROM table WHERE age > 50
   
   Row Group 0: age min=18, max=65 → CÓ THỂ chứa data → đọc
   Row Group 1: age min=22, max=45 → CHẮC CHẮN không chứa age>50 → BỎ QUA ✨
   ```
3. **Page Index**: Từ Parquet v2, statistics ở cấp **page** cho phép skip chính xác hơn (skip từng page thay vì cả Row Group)

##### G. So sánh Parquet với các format khác

| | **Parquet** | **CSV** | **JSON** | **ORC** | **Avro** |
|---|---|---|---|---|---|
| **Lưu trữ** | Columnar | Row | Row (semi-structured) | Columnar | Row |
| **Schema** | Embedded trong file | Không có | Implicit | Embedded | Embedded |
| **Compression** | Rất cao (encoding + compression) | Kém | Kém | Rất cao | Trung bình |
| **Đọc 1 vài cột** | ✅ Cực nhanh (column pruning) | ❌ Phải đọc toàn bộ | ❌ Phải đọc toàn bộ | ✅ Cực nhanh | ❌ Phải đọc toàn bộ |
| **Predicate pushdown** | ✅ Có (min/max statistics) | ❌ Không | ❌ Không | ✅ Có | ❌ Không |
| **Phù hợp** | **OLAP**, analytics, data lake | Import/export đơn giản | API, logging | **OLAP** (Hive ecosystem) | Streaming, message queue |
| **Hệ sinh thái** | Spark, Trino, BigQuery, DuckDB | Universal | Universal | Hive, Presto | Kafka, Hadoop |

> ⚠️ **ORC vs Parquet**: Cả hai đều là columnar format với tính năng tương tự. ORC sinh ra từ hệ sinh thái Hive (Hortonworks), Parquet sinh ra từ Dremel paper của Google (Cloudera + Twitter). Ngày nay, **Parquet chiếm ưu thế** do được Spark và hầu hết cloud data warehouse chọn làm format mặc định.

##### H. Khi nào KHÔNG nên dùng Parquet?

| Tình huống | Lý do | Nên dùng |
|---|---|---|
| **OLTP** (INSERT/UPDATE/DELETE thường xuyên) | Parquet là immutable, không hỗ trợ update in-place | Row-based format (MySQL, PostgreSQL native) |
| **Đọc toàn bộ row** (`SELECT *`) | Columnar phải ghép dữ liệu từ nhiều cột → overhead | Row-based format, Avro |
| **Streaming data** | Parquet cần biết toàn bộ data trước khi write (để tính statistics) | Avro, JSON, Protobuf |
| **File rất nhỏ** (< vài MB) | Overhead metadata/footer lớn hơn lợi ích nén | CSV, JSON |