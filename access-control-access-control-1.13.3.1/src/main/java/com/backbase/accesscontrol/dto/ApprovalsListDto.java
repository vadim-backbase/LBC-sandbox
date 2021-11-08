package com.backbase.accesscontrol.dto;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalItem;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@With
@EqualsAndHashCode
public class ApprovalsListDto {

    private String cursor;
    private List<PresentationApprovalItem> presentationApprovalItems;

}
