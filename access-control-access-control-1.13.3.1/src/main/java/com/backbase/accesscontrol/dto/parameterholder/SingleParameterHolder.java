package com.backbase.accesscontrol.dto.parameterholder;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Parameter holder for single parameter.
 */
@AllArgsConstructor
@Getter
public class SingleParameterHolder<T> implements GenericParameterHolder {

    private T parameter;

}
