package xpdfmergeV1;

import java.util.LinkedHashMap;

public class LRUList <S,T> extends LinkedHashMap<S,T> {
    private int capacity = 1;

    LRUList(int Capacity) {
        super(Capacity+1, 1.0F, true);
        this.capacity = Capacity;
    }

    protected boolean removeEldestEntry() {
        return (size() > this.capacity);
    }
}
