package com.backbase.accesscontrol.domain.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bound {

    private BigDecimal amount;
    private String currencyCode;
}