package cat.nyaa.HamsterEcoHelper.quest.gui;

import java.util.*;

public class PairList<K, V> implements Iterable<PairList<K, V>.Pair>{
    public class Pair {
        public Pair(K k, V v) {
            key = k;
            value = v;
        }

        public K key;
        public V value;
    }

    private List<Pair> l = new ArrayList<>();
    private Map<K, V> m = new HashMap<>();

    public void put(K key, V val) {
        l.add(new Pair(key, val));
        m.put(key, val);
    }

    public V get(K key) {
        return m.get(key);
    }

    public List<V> getValues(int startIdx, int endIdx) {
        int size = endIdx < l.size()? endIdx-startIdx: l.size()-startIdx;
        List<V> ret = new ArrayList<>();
        for (int i=0;i<size;i++) {
            ret.add(l.get(startIdx+i).value);
        }
        return ret;
    }

    public K getKey(int index) {
        return index>=l.size()? null:l.get(index).key;
    }

    public int size() {
        return l.size();
    }

    @Override
    public Iterator<Pair> iterator() {
        return new Iterator<Pair>() {
            private int idx = 0;
            @Override
            public boolean hasNext() {
                return idx < l.size();
            }

            @Override
            public Pair next() {
                return l.get(idx++);
            }
        };
    }
}
