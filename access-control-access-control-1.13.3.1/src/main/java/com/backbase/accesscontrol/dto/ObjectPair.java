package com.backbase.accesscontrol.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

/**
 * convenience class to represent request-response pairs.
 */
@AllArgsConstructor
@Getter
@Setter
@With
@NoArgsConstructor
public class ObjectPair<F, S> {
    private F request;
    private S response;
}

