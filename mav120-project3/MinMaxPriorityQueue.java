import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MinMaxPriorityQueue<T extends HasKey> {
    Comparator<T> minComparator;
    Comparator<T> maxComparator;

    IndexedPriorityQueuePart<T> minPQ;
    IndexedPriorityQueuePart<T> maxPQ;

    HashMap<String, Integer[]> map = new HashMap<>();

    public MinMaxPriorityQueue(Comparator<T> min, Comparator<T> max) {
        minComparator = min;
        maxComparator = max;
        minPQ = new IndexedPriorityQueuePart<>(min, map, 0);
        maxPQ = new IndexedPriorityQueuePart<>(max, map, 1);
    }

    public void add(T item) {
        map.put(item.getKey(), new Integer[2]);
        minPQ.add(item);
        maxPQ.add(item);
    }

    public T getMin() {
        return minPQ.peek();
    }

    public T getMax() {
        return maxPQ.peek();
    }

    public T get(String key) {
        return minPQ.get(key);
    }

    public void remove(String key) {
        minPQ.remove(key);
        maxPQ.remove(key);
        map.remove(key);
    }

    public void update(String key) {
        minPQ.update(key);
        maxPQ.update(key);
    }

    public String toString() {
        StringBuilder pqs = new StringBuilder("\nmin\n" + minPQ + "\nmax\n" + maxPQ + "\nMAP:\n");
        for(String key : map.keySet()) {
            pqs.append(key).append(":").append(Arrays.toString(map.get(key))).append("\n");
        }
        return pqs.toString();
     }
}
