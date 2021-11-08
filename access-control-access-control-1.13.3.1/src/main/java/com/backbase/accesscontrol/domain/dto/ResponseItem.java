package com.backbase.accesscontrol.domain.dto;

import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ResponseItem {

    private String resourceId;
    private ItemStatusCode status;
    private List<String> errors = new ArrayList<>();

    public ResponseItem withResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public ResponseItem withStatus(ItemStatusCode status) {
        this.status = status;
        return this;
    }

    public ResponseItem withErrors(List<String> errors) {
        this.errors = errors;
        return this;
    }

}
