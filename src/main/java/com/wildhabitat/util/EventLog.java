package com.wildhabitat.util;

import java.util.AbstractList;
import java.util.ArrayList;

/**
 * A bounded, append-only list that automatically evicts the oldest entry once
 * it exceeds its capacity — a generic ring-buffer backed event log.
 *
 * @param <T> the type of event stored in this log
 */
public class EventLog<T> extends AbstractList<T> {

    private final int capacity;
    private final ArrayList<T> buffer;

    public EventLog(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity must be at least 1.");
        }
        this.capacity = capacity;
        this.buffer   = new ArrayList<>(capacity);
    }

    /** Appends an entry, evicting the oldest one if capacity is exceeded. */
    @Override
    public boolean add(T entry) {
        buffer.add(entry);
        if (buffer.size() > capacity) {
            buffer.remove(0);
        }
        return true;
    }

    @Override
    public T get(int index) {
        return buffer.get(index);
    }

    @Override
    public int size() {
        return buffer.size();
    }

    @Override
    public void clear() {
        buffer.clear();
    }

    public int capacity() {
        return capacity;
    }
}
