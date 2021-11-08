package com.backbase.accesscontrol.util.helpers;

import com.backbase.buildingblocks.backend.internalrequest.DefaultInternalRequestContext;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RequestUtils {

    public static <T> InternalRequest<T> getInternalRequest(T data, InternalRequestContext internalRequestContext) {
        InternalRequest<T> internalRequest = new InternalRequest<>();
        internalRequest.setInternalRequestContext(internalRequestContext);
        internalRequest.setData(data);
        return internalRequest;
    }

    public static <T> InternalRequest<T> getInternalRequest(T body) {
        InternalRequest<T> internalRequest = new InternalRequest<>();
        internalRequest.setInternalRequestContext(getInternalRequestContext());
        internalRequest.setData(body);
        return internalRequest;
    }

    public static <T> InternalRequest<T> getVoidInternalRequest() {
        InternalRequest<T> internalRequest = new InternalRequest<>();
        internalRequest.setInternalRequestContext(getInternalRequestContext());
        return internalRequest;
    }

    public static <T> ResponseEntity<T> getResponseEntity(T body, HttpStatus status) {
        return new ResponseEntity<>(body, status);
    }

    private static InternalRequestContext getInternalRequestContext() {
        return new DefaultInternalRequestContext();
    }
}
