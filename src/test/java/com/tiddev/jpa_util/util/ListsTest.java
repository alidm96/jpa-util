package com.tiddev.jpa_util.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ListsTest {

    @Test
    void partition_nullList_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> Lists.partition(null, 1));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void partition_nonPositiveSize_throwsException(int invalidSize) {
        Runnable partitionCall = () -> Lists.partition(List.of(1), invalidSize);
        assertThrows(IllegalArgumentException.class, partitionCall::run);
    }

    @Test
    void partition_emptyList_returnsEmptyPartition() {
        List<List<String>> partitioned = Lists.partition(Collections.emptyList(), 3);
        assertTrue(partitioned.isEmpty());
    }

    @Test
    void partition_exactPartitionSize_partitionsCorrectly() {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6);
        List<List<Integer>> partitioned = Lists.partition(list, 2);

        assertEquals(3, partitioned.size());
        assertEquals(List.of(1, 2), partitioned.get(0));
        assertEquals(List.of(3, 4), partitioned.get(1));
        assertEquals(List.of(5, 6), partitioned.get(2));
    }

    @Test
    void partition_partialPartitionSize_partitionsCorrectly() {
        List<Character> list = List.of('a', 'b', 'c', 'd', 'e');
        List<List<Character>> partitioned = Lists.partition(list, 2);

        assertEquals(3, partitioned.size());
        assertEquals(List.of('a', 'b'), partitioned.get(0));
        assertEquals(List.of('c', 'd'), partitioned.get(1));
        assertEquals(List.of('e'), partitioned.get(2));
    }

    @Test
    void partition_sizeLargerThanList_returnsSinglePartition() {
        List<String> list = List.of("A", "B");
        List<List<String>> partitioned = Lists.partition(list, 5);

        assertEquals(1, partitioned.size());
        assertEquals(List.of("A", "B"), partitioned.get(0));
    }

    @Test
    void partition_sizeEqualsList_returnsSinglePartition() {
        List<Integer> list = List.of(10, 20, 30);
        List<List<Integer>> partitioned = Lists.partition(list, 3);

        assertEquals(1, partitioned.size());
        assertEquals(list, partitioned.get(0));
    }

    @Test
    void partition_sizeOne_partitionsEachElement() {
        List<Double> list = List.of(1.1, 2.2, 3.3);
        List<List<Double>> partitioned = Lists.partition(list, 1);

        assertEquals(3, partitioned.size());
        assertEquals(List.of(1.1), partitioned.get(0));
        assertEquals(List.of(2.2), partitioned.get(1));
        assertEquals(List.of(3.3), partitioned.get(2));
    }

    @Test
    void partition_largeList_partitionsCorrectly() {
        List<Integer> list = IntStream.range(0, 1000).boxed().toList();
        List<List<Integer>> partitioned = Lists.partition(list, 100);

        assertEquals(10, partitioned.size());
        IntStream.range(0, 10).forEach(i -> assertEquals(100, partitioned.get(i).size()));
    }

    @Test
    void partition_equality() {
        List<Integer> list1 = List.of(1, 2, 3);
        List<Integer> list2 = List.of(1, 2, 3);
        List<Integer> list3 = List.of(4, 5, 6);

        List<List<Integer>> partitioned1 = Lists.partition(list1, 2);
        List<List<Integer>> partitioned2 = Lists.partition(list2, 2);
        List<List<Integer>> partitioned3 = Lists.partition(list3, 2);
        List<List<Integer>> partitioned4 = Lists.partition(list1, 1);

        assertEquals(partitioned1, partitioned2);
        assertNotEquals(partitioned1, partitioned3);
        assertNotEquals(partitioned1, partitioned4);
        assertEquals(partitioned1.hashCode(), partitioned2.hashCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 3})
    void partition_invalidIndex_throwsException(int invalidIndex) {
        List<Integer> list = List.of(1, 2, 3);
        List<List<Integer>> partitioned = Lists.partition(list, 2);

        assertThrows(IndexOutOfBoundsException.class, () -> partitioned.get(invalidIndex));
    }

    @Test
    void partition_mutableList_reflectsChanges() {
        List<String> mutableList = new ArrayList<>(List.of("A", "B", "C"));
        List<List<String>> partitioned = Lists.partition(mutableList, 2);

        mutableList.set(0, "X");
        mutableList.add("D");

        assertEquals(List.of("X", "B"), partitioned.get(0));
        assertEquals(List.of("C", "D"), partitioned.get(1));
    }
}