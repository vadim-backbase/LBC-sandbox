package com.backbase.accesscontrol.util;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtils {

    /**
     * Util method for dividing a list in chunks received as an argument.
     *
     * @param source    - list to be divided
     * @param chunkSize - chunk size
     * @return stream of lists
     */

    public static <T> Stream<List<T>> getBatchRequestOnChunks(List<T> source, int chunkSize) {
        int size = source.size();
        if (size <= 0) {
            return Stream.empty();
        }
        int fullChunks = (size - 1) / chunkSize;
        return IntStream.range(0, fullChunks + 1).mapToObj(
            n -> source.subList(n * chunkSize, n == fullChunks ? size : (n + 1) * chunkSize));
    }
}
