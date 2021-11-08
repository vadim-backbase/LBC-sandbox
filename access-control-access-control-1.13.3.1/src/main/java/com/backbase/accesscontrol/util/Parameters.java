package com.backbase.accesscontrol.util;

import java.util.Arrays;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 *  Parameters.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Parameters {


    /**
     * count the number of parameters that are not null and not empty strings.
     * @param args list of arguments that need to be counted.
     * @return number of parameters.
     */
    public static int numberOfParameters(String... args) {
        return Arrays.stream(args).filter(param -> Objects.nonNull(param) && (param.length() > 0)).toArray().length;
    }
}
