package com.backbase.account.mock.util;

import com.backbase.mock.outbound.recipient.model.RecipientArrangementIdsDto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipientArrangementsData {

    /**
     * get data for arrangements of different types.
     *
     * @param externalArrangementId arrangement id
     * @return new arrangement
     */
    public static RecipientArrangementIdsDto getRecipientArrangements(String externalArrangementId) {

        List<String> arrangementIds;

        switch (externalArrangementId) {
            case "A01":
                arrangementIds = Arrays.asList("A03", "A05", "A07", "A09");
                break;
            case "A02":
                arrangementIds = Arrays.asList("A04", "A06", "A08");
                break;
            case "A03":
                arrangementIds = Arrays.asList("A01", "A05", "A07", "A09");
                break;
            case "A04":
                arrangementIds = Arrays.asList("A02", "A06", "A08");
                break;
            case "A05":
                arrangementIds = Arrays.asList("A01", "A03", "A07", "A09");
                break;
            case "A06":
                arrangementIds = Arrays.asList("A02", "A04", "A08");
                break;
            case "A07":
                arrangementIds = Arrays.asList("A01", "A03", "A05", "A09");
                break;
            case "A08":
                arrangementIds = Arrays.asList("A02", "A04", "A06");
                break;
            case "A09":
                arrangementIds = Arrays.asList("A09", "A03", "A05", "A07");
                break;
            default:
                arrangementIds = new ArrayList<>();
                break;
        }

        RecipientArrangementIdsDto recipientArrangementIds = new RecipientArrangementIdsDto();
        recipientArrangementIds.getArrangementIds().addAll(arrangementIds);
        return recipientArrangementIds;
    }
}
