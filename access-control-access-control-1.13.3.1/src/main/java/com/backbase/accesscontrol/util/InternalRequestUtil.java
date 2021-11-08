package com.backbase.accesscontrol.util;

import com.backbase.buildingblocks.backend.internalrequest.DefaultInternalRequestContext;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import javax.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Util class for Internal Request.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InternalRequestUtil {

    /**
     * Populates the Internal Request with appropriate data and httpServletRequest.
     *
     * @return populated internal request
     */
    public static <T> InternalRequest<T> getInternalRequest(T data, HttpServletRequest httpServletRequest,
        String randomId) {
        InternalRequestContext internalRequestContext = DefaultInternalRequestContext
            .contextFrom(httpServletRequest, randomId);
        InternalRequest<T> internalRequest = new InternalRequest<>();
        internalRequest.setData(data);
        internalRequest.setInternalRequestContext(internalRequestContext);
        return internalRequest;
    }

    /**
     * Populates the Internal Request with appropriate data and context.
     *
     * @return populated internal request
     */
    public static <T> InternalRequest<T> getInternalRequest(T data, InternalRequestContext context) {
        InternalRequest<T> internalRequest = new InternalRequest<>();
        internalRequest.setData(data);
        internalRequest.setInternalRequestContext(context);
        return internalRequest;
    }

    /**
     * Creates void Internal Request.
     */
    public static InternalRequest<Void> getVoidInternalRequest(InternalRequestContext context) {
        InternalRequest<Void> voidInternalRequest = new InternalRequest<>();
        voidInternalRequest.setInternalRequestContext(context);
        return voidInternalRequest;
    }
}
