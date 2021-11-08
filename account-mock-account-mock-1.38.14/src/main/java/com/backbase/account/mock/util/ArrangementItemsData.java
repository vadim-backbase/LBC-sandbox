package com.backbase.account.mock.util;

import static com.backbase.account.mock.util.ArrangementDetailsData.getCurrentAccount;
import static com.backbase.account.mock.util.ArrangementDetailsData.getInvestmentAccount;
import static com.backbase.account.mock.util.ArrangementDetailsData.getSavingsAccount;

import com.backbase.dbs.arrangement.commons.model.DebitCardItemDto;
import com.backbase.mock.outbound.account.link.model.ArrangementItemDto;

import java.util.*;

import org.mapstruct.factory.Mappers;


public class ArrangementItemsData {

    private static ArrangementMapper mapper = Mappers.getMapper(ArrangementMapper.class);

    public static List<ArrangementItemDto> getArrangementsByLegalEntityId(String externalLegalEntityId,
        Set<DebitCardItemDto> debitCards) {
        List<ArrangementItemDto> arrangementItems = new ArrayList<>();
        Map<String, String> additions = Collections.emptyMap();

        switch (externalLegalEntityId.trim()) {
            case "BANK0001":
                arrangementItems.add(mapper.map(getCurrentAccount(debitCards, additions), "LINKING_ARRANGEMENT_01", "productId1"));
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_02", "productId2"));
                //arrangementItems.add(mapper.map(getInvestmentAccount(debitCards, additions), "LINKING_ARRANGEMENT_03", ""));
                break;
            case "CompanyABC":
                arrangementItems.add(mapper.map(getCurrentAccount(debitCards, additions), "LINKING_ARRANGEMENT_ABC_01", "productId1"));
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_ABC_02", "productId2"));
                //arrangementItems.add(mapper.map(getInvestmentAccount(debitCards, additions), "LINKING_ARRANGEMENT_ABC_03", ""));
                break;
            case "Real Estate":
                arrangementItems.add(mapper.map(getCurrentAccount(debitCards, additions), "LINKING_ARRANGEMENT_Real_Estate_01", "productId1"));
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_Real_Estate_02", "productId2"));
                //arrangementItems.add(mapper.map(getInvestmentAccount(debitCards, additions), "LINKING_ARRANGEMENT_Real_Estate_03", ""));
                break;
            case "CORPCUST_1":
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_03", "productId1"));
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_04", "productId2"));
                //arrangementItems.add(mapper.map(getInvestmentAccount(debitCards, additions), "LINKING_ARRANGEMENT_05", ""));
                break;
            case "RealEstateCommercial1":
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_RealEstateCommercial1_03", "productId1"));
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_RealEstateCommercial1_04", "productId2"));
                //arrangementItems.add(mapper.map(getInvestmentAccount(debitCards, additions), "LINKING_ARRANGEMENT_RealEstateCommercial1_05", ""));
                break;
            case "Company1":
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_Company1_03", "productId1"));
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_Company1_04", "productId2"));
                //arrangementItems.add(mapper.map(getInvestmentAccount(debitCards, additions), "LINKING_ARRANGEMENT_Company1_05", ""));
                break;
            case "CORPCUST_2":
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_02", "productId1"));
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_04", "productId2"));
                //arrangementItems.add(mapper.map(getInvestmentAccount(debitCards, additions), "LINKING_ARRANGEMENT_06", ""));
                break;
            case "RealEstateCommercial2":
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_RealEstateCommercial2_02", "productId1"));
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_RealEstateCommercial2_04", "productId2"));
                //arrangementItems.add(mapper.map(getInvestmentAccount(debitCards, additions), "LINKING_ARRANGEMENT_RealEstateCommercial2_06", ""));
                break;
            case "Company2":
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_Company2_02", "productId1"));
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_Company2_04", "productId2"));
                //arrangementItems.add(mapper.map(getInvestmentAccount(debitCards, additions), "LINKING_ARRANGEMENT_Company2_06", ""));
                break;
            case "RealEstateCommercial3":
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_RealEstateCommercial3_02", "productId1"));
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_RealEstateCommercial3_04", "productId2"));
                //arrangementItems.add(mapper.map(getInvestmentAccount(debitCards, additions), "LINKING_ARRANGEMENT_RealEstateCommercial3_06", ""));
                break;
            case "Company3":
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_Company3_902", "productId1"));
                arrangementItems.add(mapper.map(getSavingsAccount(debitCards, additions), "LINKING_ARRANGEMENT_Company3_904", "productId2"));
                //arrangementItems.add(mapper.map(getInvestmentAccount(debitCards, additions), "LINKING_ARRANGEMENT_Company3_906", ""));
                break;
            default:
                ArrangementItemDto arrangementItem = new ArrangementItemDto().withId("A01").withDebitCards(debitCards);
                //arrangementItem.setAdditions(additions);
                arrangementItems.add(arrangementItem);
                break;
        }

        return arrangementItems;
    }
}