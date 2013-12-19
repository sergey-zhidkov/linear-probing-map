package org.coursera.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LinearProbingMap<Key, Value> implements Map<Key, Value> {

    private static final int DEFAULT_CAPACITY = 32;

    private final int startCapacity;
    private int currentCapacity;
    private Key[] keys;
    private Value[] values;
    private int size;

    public LinearProbingMap() {
        this(DEFAULT_CAPACITY);
    }

    public LinearProbingMap(int capacity) {
        if (capacity <= 0) { throw new IllegalArgumentException("Illegal capacity: " + capacity); }
        if (capacity < DEFAULT_CAPACITY) { capacity = DEFAULT_CAPACITY; }
        startCapacity = capacity;
        initMap(capacity);
    }

    @SuppressWarnings("unchecked")
    private void initMap(int capacity) {
        keys = (Key[]) new Object[capacity];
        values = (Value[]) new Object[capacity];
        size = 0;
        currentCapacity = capacity;
    }

    /**
     * Hash function for keys - returns value between 0 and currentCapacity-1
     * 
     * @param key
     * @return
     */
    private int hash(Key key) {
        return (key.hashCode() & 0x7fffffff) % currentCapacity;
    }

    @Override
    public void clear() {
        initMap(startCapacity);
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null) { return false; }
        Set<Key> keySet = keySet();
        return keySet.contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        Collection<Value> collection = values();
        return collection.contains(value);
    }

    @Override
    public Set<java.util.Map.Entry<Key, Value>> entrySet() {
        Set<Entry<Key, Value>> entrySet = new HashSet<>();
        for (int i = 0; i < keys.length; i++) {
            Key key = keys[i];
            if (key == null) { continue; }
            Entry<Key, Value> entry = new LinearEntry<Key, Value>(key, values[i]);
            entrySet.add(entry);
        }

        return entrySet;
    }

    @Override
    public Value get(Object key) {
        if (key == null) { return null; }
        @SuppressWarnings("unchecked")
        int hash = hash((Key) key);

        for (int i = hash; i < keys.length; i++) {
            Key k = keys[i];
            if (k == null) { return null; }
            if (k.equals(key)) { return values[i]; }

            //TODO: avoid infinite cycle
            if (i == keys.length - 1) { i = 0; }
        }

        return null;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Set<Key> keySet() {
        Set<Key> keySet = new HashSet<>();
        for (int i = 0; i < keys.length; i++) {
            Key key = keys[i];
            if (key != null) { keySet.add(key); }
        }
        return keySet;
    }

    @Override
    public Value put(Key key, Value val) {
        if (key == null) { return null; } // don't support null keys

        resize();
        int hash = hash(key);
        Value old;
        for (int i = hash; i < keys.length; i++) {
            Key k = keys[i];
            if (k == null) {
                insert(i, key, val);
                size++;
                return null;
            }

            if (k.equals(key)) {
                old = values[i];
                insert(i, key, val);
                return old;
            }

            // starts from beginning
            // TODO: avoid infinite cycle
            if (i == keys.length - 1) { i = 0; }
        }

        return null;
    }

    /**
     * Resize map up if (size / currentCapacity) >= 1/2, or down if (size / currentCapacity) < 1/8.
     */
    private void resize() {
        if (isEmpty()) { return; }
        float relation = (float) size / (float) currentCapacity;

        // TODO: avoid overflow MAX_INTEGER
        if (relation > 0.5f) {
            resize(currentCapacity * 2);
        } else if (relation < 0.125f && (currentCapacity / 2) >= DEFAULT_CAPACITY) {
            resize(currentCapacity / 2);
        }
    }

    /**
     * Rehash all map.
     * @param capacity
     */
    private void resize(int capacity) {
        LinearProbingMap<Key, Value> temp = new LinearProbingMap<>(capacity);
        temp.putAll(this);

        keys = temp.keys;
        values = temp.values;
        size = temp.size;
        currentCapacity = temp.currentCapacity;
    }

    private void insert(int index, Key key, Value val) {
        keys[index] = key;
        values[index] = val;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void putAll(Map<? extends Key, ? extends Value> map) {
        Set<?> entrySet = map.entrySet();
        for (Object entry : entrySet) {
            put(((Entry<Key, Value>) entry).getKey(), ((Entry<Key, Value>) entry).getValue());
        }
    }

    @Override
    public Value remove(Object key) {
        if (key == null) { return null; }
        Value value = null;
        @SuppressWarnings("unchecked")
        int hash = hash((Key) key);
        if (keys[hash] == null) { return null; }

        Set<Entry<Key, Value>> cluster = new HashSet<>();
        Entry<Key, Value> entry;
        Key k;
        Value v;
        // get all cluster to rehash
        for (int i = hash; i < keys.length; i++) {
            k = keys[i];
            v = values[i];
            keys[i] = null;
            values[i] = null;
            // end of cluster
            if (k == null) { break; }
            entry = new LinearEntry<>(k, v);
            cluster.add(entry);
            if (i == keys.length - 1) { i = 0; }
        }

        for (Entry<Key, Value> e : cluster) {
            Key kk = e.getKey();
            if (kk.equals(key)) {
                value = e.getValue();
                size--;
            } else {
                put(kk, e.getValue());
            }
        }

        return value;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Collection<Value> values() {
        Collection<Value> collection = new ArrayList<>();
        Set<Key> keySet = keySet();
        for (Key key : keySet) {
            collection.add(get(key));
        }

        return collection;
    }


    private class LinearEntry<K, V> implements Entry<K, V> {

        private K key;
        private V value;

        private LinearEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V val) {
            V old = value;
            value = val;
            return old;
        }

    }

    /**
     * Simple tests for our Map
     * 
     * @param args
     */
    public static void main(String[] args) {

    }
}
