
package com.backbase.accesscontrol.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceAgreementDto {

    private List<ServiceAgreementParticipantDto> participants = new ArrayList<>();
    private String validFromDate;
    private String validFromTime;
    private String validUntilDate;
    private String validUntilTime;

    public ServiceAgreementDto withParticipants(List<ServiceAgreementParticipantDto> participants) {
        this.participants = participants;
        return this;
    }
    public ServiceAgreementDto withValidFromDate(String validFromDate) {
        this.validFromDate = validFromDate;
        return this;
    }
    public ServiceAgreementDto withValidFromTime(String validFromTime) {
        this.validFromTime = validFromTime;
        return this;
    }
    public ServiceAgreementDto withValidUntilDate(String validUntilDate) {
        this.validUntilDate = validUntilDate;
        return this;
    }
    public ServiceAgreementDto withValidUntilTime(String validUntilTime) {
        this.validUntilTime = validUntilTime;
        return this;
    }
}
