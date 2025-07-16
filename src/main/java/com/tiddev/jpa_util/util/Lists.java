package com.tiddev.jpa_util.util;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for list operations.
 */
public final class Lists {

    private Lists() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /**
     * Partitions a list into sublist of the specified size.
     *
     * @param <T>  the type of elements in the list
     * @param list the list to partition
     * @param size the desired size of each partition
     * @return a partitioned view of the original list
     * @throws IllegalArgumentException if list is null or size is non-positive
     */
    public static <T> List<List<T>> partition(List<T> list, int size) {
        if (list == null || size <= 0) {
            throw new IllegalArgumentException("List cannot be null and size must be positive");
        }
        return new Partition<>(list, size);
    }

    /**
     * Partitioned view of a list.
     */
    private static final class Partition<T> extends AbstractList<List<T>> {
        private final List<T> list;
        private final int size;

        Partition(List<T> list, int size) {
            this.list = list;
            this.size = size;
        }

        @Override
        public List<T> get(int index) {
            int listSize = size();
            if (index < 0 || index >= listSize) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + listSize);
            }
            int start = index * size;
            int end = Math.min(start + size, list.size());
            return list.subList(start, end);
        }

        @Override
        public int size() {
            return (int) Math.ceil((double) list.size() / size);
        }

        @Override
        public boolean isEmpty() {
            return list.isEmpty();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Partition<?> other)) {
                return false;
            }
            return this.size == other.size
                   && Objects.equals(this.list, other.list);
        }

        @Override
        public int hashCode() {
            return Objects.hash(list, size);
        }
    }
}