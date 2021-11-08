package com.backbase.accesscontrol.mappers.model;


public interface AbstractPayloadConverter<I, O> {

    O convertTo(I input);

    ConverterKey getConverterKey();
}
