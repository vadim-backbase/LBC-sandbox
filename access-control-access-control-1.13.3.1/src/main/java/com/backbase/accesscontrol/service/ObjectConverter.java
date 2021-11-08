package com.backbase.accesscontrol.service;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Converts Request to Response objects when communicating with Persistence and Projection layer and vice versa.
 */
@Component
@AllArgsConstructor
public class ObjectConverter {

    private ObjectMapper mapper;

    /**
     * Converts list.
     *
     * @param data          input data
     * @param classOfReturn destination class
     * @param <F>           source type
     * @param <T>           destination type
     * @return list
     */
    public <F, T> List<T> convertList(List<F> data, Class<T> classOfReturn) {
        if (isNull(data)) {
            return emptyList();
        }

        return data
            .stream()
            .map(objectToBeConverted -> mapper.convertValue(objectToBeConverted, classOfReturn))
            .collect(Collectors.toList());
    }

    /**
     * Convert object.
     *
     * @param fromValue   source value
     * @param toValueType destination class
     * @param <T>         destination type
     * @return destination value
     */
    public <T> T convertValue(Object fromValue, Class<T> toValueType) {
        return mapper.convertValue(fromValue, toValueType);
    }

    /**
     * Convert value.
     *
     * @param fromValue      value to be converted
     * @param toValueTypeRef to value type
     * @param <T>            copy to parameter type.
     * @return a converted value
     */
    public <T> T convertValue(Object fromValue, TypeReference<T> toValueTypeRef) {
        return mapper.convertValue(fromValue, toValueTypeRef);
    }

}