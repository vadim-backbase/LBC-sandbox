package com.backbase.accesscontrol.mappers.model;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

import com.backbase.accesscontrol.mappers.model.validation.ValidatePayload;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PayloadConverter {

    private Map<ConverterKey, AbstractPayloadConverter> convertersMap;

    public PayloadConverter(List<AbstractPayloadConverter> converters) {

        convertersMap = converters.stream()
            .collect(Collectors.toMap(AbstractPayloadConverter::getConverterKey, converter -> converter));
    }

    private <I, O> O convertPayload(I input, Class<O> outputClass) {

        ConverterKey key = new ConverterKey(input.getClass().getCanonicalName(), outputClass.getCanonicalName());
        if (!convertersMap.containsKey(key)) {
            throw new InternalServerErrorException("Invalid payload converter request.");
        }
        return (O) convertersMap
            .get(key)
            .convertTo(input);
    }

    @ValidatePayload
    public <I, O> O convertAndValidate(I input, Class<O> outputClass) {
        return convertPayload(input, outputClass);
    }

    public <I, O> O convert(I input, Class<O> outputClass) {
        return convertPayload(input, outputClass);
    }

    public <I, O> List<O> convertListPayload(List<I> input, Class<O> outputClass) {

        if (isNull(outputClass)) {
            return emptyList();
        }
        return input
            .stream()
            .map(objectToBeConverted -> convertPayload(objectToBeConverted, outputClass))
            .collect(Collectors.toList());
    }
}
