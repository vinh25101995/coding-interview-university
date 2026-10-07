import java.util.Collections;
import java.util.List;

public class SkipList<T extends Comparable<T>>{
    public SkipList<T> down;
    public SkipList<T> next;
    public T value;

    public SkipList() {}

    public SkipList(List<T> data) {
        Collections.sort(data);
        SkipList<T> head = null;
        SkipList<T> before = null;
        for(T item : data) {
            SkipList<T> node = new SkipList<>();
            node.value = item;
            if(before != null) {
                before.next = node;
            } else {
                head = node;
            }
            before = node;
        }
        // Copy head node data into 'this'
        if(head != null) {
            this.value = head.value;
            this.next = head.next;
        }
    }
}