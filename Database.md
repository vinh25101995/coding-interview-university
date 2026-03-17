### 6. Column-Oriented Storage
1, Các dạng storage
- NSM(N-ary Storage Model)
  Lưu trữ data trong
- DSM(Decomposition Storage Model) sử dụng variable-length encoding để lưu trữ dữ liệu

 [ ]Kiểm tra lại nếu null thì sao? Có phải dùng bitmap để lưu trữ không? -> Có sử dụng bitmap để lưu trữ null data

 [ ]Nếu cần lưu trữ string

    Thay vì sử dụng padding để variable-length encoding thì ta sử dụng 1 dictory compression(32 bit integer)

- Lí do không sử dụng id vào trong tuple là do nếu tốn thêm data để lưu id thì sẽ làm tăng dung lượng của tuple, dẫn đến cần nhiều I/O hơn để đọc dữ liệu
- Do data đồng nhất trong 1 page nên ta tăng tốc được tốc độ đọc, bù lại write sẽ chậm hơn rất nhiều
- Hầu hết các trường hợp ta cần nhiều hơn 1 column để trả về kết quả, do đó vẫn cần các atttribute của 1 tuple gần nhau -> PAX Storage Model
 
- PAX Storage Model:
    - Nhóm các partion data vào 1 group
    - Các attribute lưu trữ theo các column trunk
    - Page chứa các meta data

Cấu tạo của 1 pax file:
    - Row group
        - Row group meta data: Chứa thông tin trong 1 chunk: Offset?
        - Column trunk
    - Page footer: Chứa thông tin của cả page
        -  Các thông tin này là gì? Tại sao lại cần footer

2, Compression
- Giảm I/O, tăng CPU
- Các tiêu chuẩn compression:
    - Phải tạo ra các giá trị có độ dài cố định (fixed-length values).
    - Trì hoãn việc decompression trong quá trình đọc dữ liệu: Kĩ thuật nào giúp tính toán trên dữ liệu đã nén? Trong slilde đề cập đến kĩ thuật mod log: Ghi thay đổi vào và log ở đầu file nén(mod log). Sau khi đọc file ta chỉ cần áp dụng các thay đổi trong log vào file nén để có được file gốc.
    - Không làm mất mát dữ liệu
Nén theo block
Nén theo tuple
Nén theo attribute
Nén theo column
- Khi nào thì data bị nén? Có phải mọi data đều bị nén không?
- Không phải data được lưu thành các page sao? Nếu nén dạng sort thì lưu kiểu gì?
- Pathching: khi data không thể lưu trữ với max size đã được nén

### 7. Database hashtable
- Tác dụng
    - Sử dụng trong join
    - Internal meta data
    - ....
- Design decision
    - Data organization
    - Concurrency
- Hashtable
    - Dù có O(1) nhưng việc implement khiến hiệu năng khác nhau

Phần lớn các database sử dụng xxhash của facebook
Static hashing schema

    - Linear probing
      Nếu xảy ra collisions bằng cách tìm tới slot trống tiếp theo
      Vậy khi search thì như nào nếu ta ko chắc nó chưa đi tới cuối
      Resize: Load factor = active key / total slot
    - Hash table: Non-unique key(cùng key nhưng khác value(ví dụ trong mối quan hệ 1 - n))
        - Separate linked list
        - Redunant Keys


Dynamic hashing schema
    - Chained hashing(giống cách java hashmap vận hành): bucket pointer
    Nhưng nếu bucket quá to
    - Extendible Hashing

    - Linear hashing



Mới chỉ đề cập đến cách hash, chưa đề cập đến cách quản lý bộ nhớ
