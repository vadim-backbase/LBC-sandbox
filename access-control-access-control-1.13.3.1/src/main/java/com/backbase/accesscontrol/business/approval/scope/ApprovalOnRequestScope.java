package com.backbase.accesscontrol.business.approval.scope;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Class for propagating approval data over for a single request.
 */
@Component
@RequestScope
@Getter@Setter
public class ApprovalOnRequestScope {

    private boolean approval = false;
}
