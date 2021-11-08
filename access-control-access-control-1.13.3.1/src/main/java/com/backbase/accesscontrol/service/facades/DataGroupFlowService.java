package com.backbase.accesscontrol.service.facades;

import com.backbase.accesscontrol.business.flows.datagroup.CreateDataGroupFlow;
import com.backbase.accesscontrol.business.flows.datagroup.UpdateDataGroupFlow;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationSingleDataGroupPutRequestBody;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DataGroupFlowService {

    private CreateDataGroupFlow createDataGroupFlow;
    private UpdateDataGroupFlow updateDataGroupFlow;

    public DataGroupsPostResponseBody createDataGroup(DataGroupBase dataGroupBase) {
        return createDataGroupFlow.start(dataGroupBase);
    }

    public Void updateDataGroup(PresentationSingleDataGroupPutRequestBody requestBody) {
        return updateDataGroupFlow.start(requestBody);
    }
}
