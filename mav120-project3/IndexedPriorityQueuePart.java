import java.util.Comparator;
import java.util.Map;
import java.util.Arrays;

public class IndexedPriorityQueuePart<T extends HasKey> {
    private T[] arr;
    private Map<String, Integer[]> map;
    private int mapIndex;
    private Comparator<T> comparator;
    private int size = 0;


    public IndexedPriorityQueuePart(Comparator<T> cmp, Map<String, Integer[]> map, int mapIndex) {
        this(512, cmp, map, mapIndex);
    }

    @SuppressWarnings("unchecked")
    private IndexedPriorityQueuePart(int capacity, Comparator<T> cmp, Map<String, Integer[]> map, int mapIndex) {
        arr = (T[])(new HasKey[capacity]);
        this.comparator = cmp;
        this.map = map;
        this.mapIndex = mapIndex;
    }

    private int getLeftChildIndex(int parentIndex) { return 2*parentIndex + 1; }
    private int getRightChildIndex(int parentIndex) { return 2*parentIndex + 2; }
    private int getParentIndex(int childIndex) { return (childIndex-1)/2; }

    private boolean hasLeftChild(int index) { return getLeftChildIndex(index) < size; }
    private boolean hasRightChild(int index) { return getRightChildIndex(index) < size; }
    private boolean hasParent(int index) { return index > 0 && getParentIndex(index) >= 0; }

    private T leftChild(int index) { return arr[getLeftChildIndex(index)]; }
    private T rightChild(int index) { return arr[getRightChildIndex(index)]; }
    private T parent(int index) { return arr[getParentIndex(index)]; }

    public T peek() {
        if(size == 0) throw new IllegalStateException();
        return arr[0];
    }

    private void ensureCapacity() {
        if(size == arr.length) {
            arr = Arrays.copyOf(arr, arr.length * 2);
        }
    }

    public void add(T item) {
        ensureCapacity();
        arr[size] = item;
        size++;
        int index = heapifyUp(size-1);
        Integer[] indices = map.get(item.getKey());
        indices[mapIndex] = index;
    }

    private void swap(int a, int b) {
        T aItem = arr[a];
        T bItem = arr[b];
        Integer[] aArray = map.get(aItem.getKey());
        Integer[] bArray = map.get(bItem.getKey());
        aArray[mapIndex] = b;
        bArray[mapIndex] = a;
        arr[a] = bItem;
        arr[b] = aItem;
    }


    private boolean greater(T a, T b) {
        return comparator.compare(a, b) >= 0;
    }

    private boolean less(T a, T b) {
        return comparator.compare(a, b) < 0;
    }

    private int heapifyUp(int index) {
        while(hasParent(index) && greater(parent(index), arr[index])) {
            swap(getParentIndex(index), index);
            index = getParentIndex(index);
        }
        return index;
    }

    private int heapifyDown(int index) {
        while(hasLeftChild(index)) {
            int smallerChildIndex = getLeftChildIndex(index);
            if(hasRightChild(index) && less(rightChild(index), leftChild(index))) {
                smallerChildIndex = getRightChildIndex(index);
            }
            if(less(arr[index], arr[smallerChildIndex])) {
                break;
            } else {
                swap(index, smallerChildIndex);
            }
            index = smallerChildIndex;
        }
        return index;
    }

    public void remove(String key) {
        int toRemoveIndex = getIndex(key);
        swap(toRemoveIndex, size-1);
        size--;
        update(toRemoveIndex);
    }

    public void update(String key) {
        int toUpdateIndex = getIndex(key);
        update(toUpdateIndex);
    }

    private void update(int index) {
        heapifyUp(index);
        heapifyDown(index);
    }

    private int getIndex(String key) {
        if(!map.containsKey(key)) return -1;
        return map.get(key)[mapIndex];
    }

    public T get(String key) {
        int index = getIndex(key);
        return index == -1 ? null : arr[index];
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        for(int i=0; i<size; i++) {
            out.append(arr[i]);
            out.append('\n');
        }
        return out.toString();
    }

}
