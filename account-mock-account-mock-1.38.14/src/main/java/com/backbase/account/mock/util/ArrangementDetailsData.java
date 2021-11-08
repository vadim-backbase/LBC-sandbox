package com.backbase.account.mock.util;

import static com.backbase.account.mock.util.EntityUtil.buildTestInstance;

import com.backbase.dbs.arrangement.commons.model.DebitCardItemDto;
import com.backbase.dbs.arrangement.commons.model.TimeUnitDto;
import com.backbase.mock.outbound.details.model.ArrangementDetailsDto;
import com.backbase.mock.outbound.details.model.CardDetailsDto;
import com.backbase.mock.outbound.details.model.InterestDetailsDto;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ArrangementDetailsData {

    /**
     * get data for arrangements of different types.
     *
     * @param externalArrangementId arrangement id
     * @param debitCards debit cards
     * @param additions additional properties
     * @return new arrangement
     */
    public static ArrangementDetailsDto getArrangementDetailsDto(String externalArrangementId, int tenantId) {
        Map<String, String> additions = Map.of("propName1", "value1", "propName2", "value2");

        Set<DebitCardItemDto> debitCards = Sets.newHashSet(
            (DebitCardItemDto) new DebitCardItemDto()
                .withNumber("3142")
                .withExpiryDate("2017-11-11")
                .withCardId("id1")
                .withCardholderName("John Doe")
                .withCardType("Visa Electron")
                .withCardStatus("Active"),
            (DebitCardItemDto) new DebitCardItemDto()
                .withNumber("213211")
                .withExpiryDate("2016-12-12")
                .withCardId("id2")
                .withCardholderName("Jack Sparrow")
                .withCardType("Maestro")
                .withCardStatus("Active"));

        final ArrangementDetailsDto arrangementDetails;
        final int cardDetailsId;

        switch (externalArrangementId) {
            case "A01":
                arrangementDetails = getCurrentAccount(debitCards, additions);
                cardDetailsId = 1;
                break;

            case "A02":
                arrangementDetails = getSavingsAccount(debitCards, additions);
                cardDetailsId = 2;
                break;

            case "A03":
                arrangementDetails = getDebitCardAccount(debitCards, additions);
                cardDetailsId = 3;
                break;

            case "A04":
                arrangementDetails = getCreditCardAccount(debitCards, additions);
                cardDetailsId = 4;
                break;

            case "A05":
                arrangementDetails = getLoanAccount(debitCards, additions);
                cardDetailsId = 5;
                break;

            case "A06":
                arrangementDetails = getTermDepositAccount(debitCards, additions);
                cardDetailsId = 6;
                break;

            case "A07":
                arrangementDetails = getInvestmentAccount(debitCards, additions);
                cardDetailsId = 7;
                break;

            default:
                arrangementDetails = getSimpleArrangementDetails(debitCards, additions);
                cardDetailsId = 8;
                break;
        }

        arrangementDetails.setCardDetails(
            buildTestInstance("card-details", CardDetailsDto.class, tenantId * 10 + cardDetailsId));

        return arrangementDetails;
    }

    private static ArrangementDetailsDto getSimpleArrangementDetails(Set<DebitCardItemDto> debitCards,
        Map<String, String> additions) {
        ArrangementDetailsDto arrangementDetails = new ArrangementDetailsDto()
            .withDebitCards(debitCards)
            .withName("GENERAL ACCOUNT")
            .withBookedBalance(BigDecimal.valueOf(15000.00))
            .withAvailableBalance(BigDecimal.valueOf(1300.00))
            .withIBAN("GB29NWBK60161331926819")
            .withBBAN("BBAN00010")
            .withCurrency("CAD")
            .withExternalTransferAllowed(true)
            .withUrgentTransferAllowed(false)
            .withAccruedInterest(BigDecimal.valueOf(10))
            .withNumber("271828")
            .withPrincipalAmount(BigDecimal.valueOf(100.00))
            .withCurrentInvestmentValue(BigDecimal.valueOf(100.00))
            .withProductNumber("productId1")
            .withBIC("GENERAL BIC")
            .withBankBranchCode("bank")
            .withAccountOpeningDate(dateTime(2012, 05, 12))
            .withAccountInterestRate(BigDecimal.valueOf(11))
            .withValueDateBalance(BigDecimal.valueOf(1000))
            .withCreditLimit(BigDecimal.valueOf(100.00))
            .withCreditLimitUsage(BigDecimal.valueOf(2000))
            .withCreditLimitInterestRate(BigDecimal.valueOf(3000))
            .withCreditLimitExpiryDate(dateTime(2020, 10, 10))
            .withStartDate(dateTime(2017, 12, 12))
            .withTermUnit(TimeUnitDto.D)
            .withTermNumber(BigDecimal.valueOf(56789))
            .withMaturityDate(dateTime(2019, 06, 22))
            .withMaturityAmount(BigDecimal.valueOf(5000))
            .withAutoRenewalIndicator(true)
            .withInterestPaymentFrequencyUnit(TimeUnitDto.M)
            .withInterestPaymentFrequencyNumber(BigDecimal.valueOf(2222))
            .withInterestSettlementAccount("Settlement Account")
            .withOutstandingPrincipalAmount(BigDecimal.valueOf(9442))
            .withMonthlyInstalmentAmount(BigDecimal.valueOf(23.23))
            .withAmountInArrear(BigDecimal.valueOf(16.9))
            .withMinimumRequiredBalance(BigDecimal.valueOf(250))
            .withCreditCardAccountNumber("Credit Card Number")
            .withValidThru(dateTime(2020, 9, 9))
            .withApplicableInterestRate(BigDecimal.valueOf(203.10))
            .withRemainingCredit(BigDecimal.valueOf(6540))
            .withOutstandingPayment(BigDecimal.valueOf(3000.70))
            .withMinimumPayment(BigDecimal.valueOf(100.65))
            .withMinimumPaymentDueDate(dateTime(2017, 11, 04))
            .withTotalInvestmentValue(BigDecimal.valueOf(1000))
            .withAccountHolderAddressLine1("address1")
            .withAccountHolderAddressLine2("address2")
            .withAccountHolderStreetName("Current Account street name")
            .withAccountHolderNames("John Doe,Larry Soto")
            .withTown("Current town")
            .withPostCode("10")
            .withCountrySubDivision("Current Division")
            .withAccountHolderCountry("MK")
            .withCreditAccount(true)
            .withDebitAccount(false)
            .withLastUpdateDate(dateTime(2019, 11, 12))
            .withSourceId("mock-source")
            .withParentId("parentId11")
            .withInterestDetails(createInterestDetails())
            .withReservedAmount(BigDecimal.valueOf(1234))
            .withRemainingPeriodicTransfers(BigDecimal.valueOf(4321))
            .withBankBranchCode2("bank branch 2")
            .withNextClosingDate(dateTime(2018, 11, 18).toLocalDate())
            .withOverdueSince(dateTime(2018, 11, 18).toLocalDate());
        arrangementDetails.setAdditions(additions);
        return arrangementDetails;
    }

    static ArrangementDetailsDto getCurrentAccount(Set<DebitCardItemDto> debitCards,
        Map<String, String> additions) {
        ArrangementDetailsDto arrangementDetails = new ArrangementDetailsDto()
            .withDebitCards(debitCards)
            .withName("CURRENT ACCOUNT")
            .withBookedBalance(BigDecimal.valueOf(10000.00))
            .withAvailableBalance(BigDecimal.valueOf(1000.00))
            //.withIBAN("GB29NWBK601613319"+getRandomNumber())
            .withBBAN("BBAN"+getRandomNumber())
            .withCurrency("CAD")
            .withExternalTransferAllowed(true)
            .withUrgentTransferAllowed(false)
            .withAccruedInterest(BigDecimal.valueOf(10))
            .withNumber("271828")
            .withPrincipalAmount(BigDecimal.valueOf(100.00))
            .withCurrentInvestmentValue(BigDecimal.valueOf(100.00))
            .withProductNumber("String 1")
            .withBIC("CURRENT BIC")
            .withBankBranchCode("bank")
            .withAccountOpeningDate(dateTime(2017, 05, 12))
            .withAccountInterestRate(BigDecimal.valueOf(11))
            .withValueDateBalance(BigDecimal.valueOf(1000))
            .withCreditLimit(BigDecimal.valueOf(100.00))
            .withCreditLimitUsage(BigDecimal.valueOf(2000))
            .withCreditLimitInterestRate(BigDecimal.valueOf(3000))
            .withCreditLimitExpiryDate(dateTime(2017, 10, 10))
            .withStartDate(dateTime(2017, 12, 12))
            .withTermUnit(TimeUnitDto.D)
            .withTermNumber(BigDecimal.valueOf(12345))
            .withMaturityDate(dateTime(2019, 07, 07))
            .withMaturityAmount(BigDecimal.valueOf(4000))
            .withAutoRenewalIndicator(true)
            .withInterestPaymentFrequencyUnit(TimeUnitDto.M)
            .withInterestPaymentFrequencyNumber(BigDecimal.valueOf(3333))
            .withInterestSettlementAccount("Settlement Account")
            .withOutstandingPrincipalAmount(BigDecimal.valueOf(9412))
            .withMonthlyInstalmentAmount(BigDecimal.valueOf(23.23))
            .withAmountInArrear(BigDecimal.valueOf(11.2))
            .withMinimumRequiredBalance(BigDecimal.valueOf(500))
            .withCreditCardAccountNumber("Credit Card Number")
            .withValidThru(dateTime(2019, 9, 9))
            .withApplicableInterestRate(BigDecimal.valueOf(333.10))
            .withRemainingCredit(BigDecimal.valueOf(4500))
            .withOutstandingPayment(BigDecimal.valueOf(2000.20))
            .withMinimumPayment(BigDecimal.valueOf(200.60))
            .withMinimumPaymentDueDate(dateTime(2017, 11, 04))
            .withTotalInvestmentValue(BigDecimal.valueOf(1000))
            .withAccountHolderAddressLine1("address1")
            .withAccountHolderAddressLine2("address2")
            .withAccountHolderStreetName("Current Account street name")
            .withAccountHolderNames("John Doe,Anna Smith")
            .withTown("Current town")
            .withPostCode("10")
            .withCountrySubDivision("Current Division")
            .withAccountHolderCountry("MK")
            .withCreditAccount(true)
            .withDebitAccount(false)
            .withLastUpdateDate(dateTime(2018, 11, 11))
            .withSourceId("mock-source")
            .withParentId("parentId1")
            .withInterestDetails(createInterestDetails())
            .withReservedAmount(BigDecimal.valueOf(1234))
            .withRemainingPeriodicTransfers(BigDecimal.valueOf(4321))
            .withBankBranchCode2("bank branch 2")
            .withNextClosingDate(dateTime(2018, 11, 11).toLocalDate())
            .withOverdueSince(dateTime(2018, 11, 11).toLocalDate());

        arrangementDetails.setAdditions(additions);
        return arrangementDetails;
    }

    static ArrangementDetailsDto getSavingsAccount(Set<DebitCardItemDto> debitCards,
        Map<String, String> additions) {

        ArrangementDetailsDto arrangementDetails = new ArrangementDetailsDto()
            .withDebitCards(debitCards)
            .withName("SAVINGS ACCOUNT")
            .withBookedBalance(BigDecimal.valueOf(33434.00))
            .withAvailableBalance(BigDecimal.valueOf(3423.00))
            //.withIBAN("GB29NWBK601613319"+getRandomNumber())
            .withBBAN("BBAN"+getRandomNumber())
            .withCurrency("CAD")
            .withExternalTransferAllowed(true)
            .withUrgentTransferAllowed(false)
            .withAccountInterestRate(BigDecimal.valueOf(10))
            .withNumber("1234")
            .withPrincipalAmount(BigDecimal.valueOf(100.00))
            .withCurrentInvestmentValue(BigDecimal.valueOf(100.00))
            .withProductNumber("String 1")
            .withBIC("SAVINGS BIC")
            .withBankBranchCode("code")
            .withAccountOpeningDate(dateTime(2017, 05, 12))
            .withAccountInterestRate(BigDecimal.valueOf(11))
            .withValueDateBalance(BigDecimal.valueOf(1000))
            .withCreditLimit(BigDecimal.valueOf(100.00))
            .withCreditLimitUsage(BigDecimal.valueOf(2000))
            .withCreditLimitInterestRate(BigDecimal.valueOf(3000))
            .withCreditLimitExpiryDate(dateTime(2017, 10, 10))
            .withStartDate(dateTime(2017, 12, 12))
            .withTermUnit(TimeUnitDto.D)
            .withTermNumber(BigDecimal.valueOf(12345))
            .withMaturityDate(dateTime(2019, 07, 07))
            .withMaturityAmount(BigDecimal.valueOf(4000))
            .withAutoRenewalIndicator(true)
            .withInterestPaymentFrequencyUnit(TimeUnitDto.M)
            .withInterestPaymentFrequencyNumber(BigDecimal.valueOf(3333))
            .withInterestSettlementAccount("Settlement Account")
            .withOutstandingPrincipalAmount(BigDecimal.valueOf(9412))
            .withMonthlyInstalmentAmount(BigDecimal.valueOf(23.23))
            .withAmountInArrear(BigDecimal.valueOf(11.2))
            .withMinimumRequiredBalance(BigDecimal.valueOf(500))
            .withCreditCardAccountNumber("Credit Card Number")
            .withValidThru(dateTime(2019, 9, 9))
            .withApplicableInterestRate(BigDecimal.valueOf(333.10))
            .withRemainingCredit(BigDecimal.valueOf(4500))
            .withOutstandingPayment(BigDecimal.valueOf(2000.20))
            .withMinimumPayment(BigDecimal.valueOf(200.60))
            .withMinimumPaymentDueDate(dateTime(2017, 11, 04))
            .withTotalInvestmentValue(BigDecimal.valueOf(1000))
            .withAccountHolderAddressLine1("address1")
            .withAccountHolderAddressLine2("address2")
            .withAccountHolderNames("Michael Douglas,John Doe")
            .withAccountHolderStreetName("Savings Account street name")
            .withTown("Savings town")
            .withPostCode("20")
            .withCountrySubDivision("Savings Division")
            .withAccountHolderCountry("MK")
            .withCreditAccount(true)
            .withCreditAccount(false)
            .withLastUpdateDate(dateTime(2018, 11, 11))
            .withSourceId("mock-source")
            .withParentId("parentId2")
            .withInterestDetails(createInterestDetails())
            .withReservedAmount(BigDecimal.valueOf(2345))
            .withRemainingPeriodicTransfers(BigDecimal.valueOf(5432))
            .withBankBranchCode2("bank branch 2")
            .withNextClosingDate(dateTime(2018, 11, 12).toLocalDate())
            .withOverdueSince(dateTime(2018, 11, 12).toLocalDate());
        arrangementDetails.setAdditions(additions);
        return arrangementDetails;
    }

    static ArrangementDetailsDto getDebitCardAccount(Set<DebitCardItemDto> debitCards,
        Map<String, String> additions) {
        ArrangementDetailsDto arrangementDetails = new ArrangementDetailsDto()
            .withDebitCards(debitCards)
            .withName("DEBIT CARDS")
            .withBookedBalance(BigDecimal.valueOf(7777.00))
            .withAvailableBalance(BigDecimal.valueOf(1000.00))
            .withIBAN("GB29NWBK601613319"+getRandomNumber())
            .withBBAN("BBAN"+getRandomNumber())
            .withCurrency("CHE")
            .withExternalTransferAllowed(false)
            .withUrgentTransferAllowed(true)
            .withAccruedInterest(BigDecimal.valueOf(10))
            .withNumber("1111")
            .withPrincipalAmount(BigDecimal.valueOf(100.00))
            .withCurrentInvestmentValue(BigDecimal.valueOf(100.00))
            .withProductNumber("String 1")
            .withBIC("DEBIT BIC")
            .withBankBranchCode("branch")
            .withAccountOpeningDate(dateTime(2017, 05, 12))
            .withAccountInterestRate(BigDecimal.valueOf(12345))
            .withValueDateBalance(BigDecimal.valueOf(1000))
            .withCreditLimit(BigDecimal.valueOf(100.00))
            .withCreditLimitUsage(BigDecimal.valueOf(2000))
            .withCreditLimitInterestRate(BigDecimal.valueOf(1222))
            .withCreditLimitExpiryDate(dateTime(2017, 10, 10))
            .withStartDate(dateTime(2017, 12, 12))
            .withTermUnit(TimeUnitDto.D)
            .withTermNumber(BigDecimal.valueOf(123))
            .withMaturityDate(dateTime(2019, 07, 07))
            .withMaturityAmount(BigDecimal.valueOf(4000))
            .withAutoRenewalIndicator(false)
            .withInterestPaymentFrequencyUnit(TimeUnitDto.M)
            .withInterestPaymentFrequencyNumber(BigDecimal.valueOf(123))
            .withInterestSettlementAccount("Settlement Account")
            .withOutstandingPrincipalAmount(BigDecimal.valueOf(1234))
            .withMonthlyInstalmentAmount(BigDecimal.valueOf(23.23))
            .withAmountInArrear(BigDecimal.valueOf(11.2))
            .withMinimumRequiredBalance(BigDecimal.valueOf(500))
            .withCreditCardAccountNumber("Debit Card Number")
            .withValidThru(dateTime(2019, 9, 9))
            .withApplicableInterestRate(BigDecimal.valueOf(333.10))
            .withRemainingCredit(BigDecimal.valueOf(4500))
            .withOutstandingPayment(BigDecimal.valueOf(2000.20))
            .withMinimumPayment(BigDecimal.valueOf(200.60))
            .withMinimumPaymentDueDate(dateTime(2017, 11, 04))
            .withTotalInvestmentValue(BigDecimal.valueOf(1000))
            .withAccountHolderAddressLine1("address1")
            .withAccountHolderAddressLine2("address2")
            .withAccountHolderNames("Tony Ferguson,John Doe")
            .withAccountHolderStreetName("Debit Account street name")
            .withTown("Debit town")
            .withPostCode("30")
            .withCountrySubDivision("Debit Division")
            .withAccountHolderCountry("MK")
            .withCreditAccount(true)
            .withDebitAccount(true)
            .withLastUpdateDate(dateTime(2018, 11, 11))
            .withSourceId("mock-source")
            .withParentId("parentId3")
            .withInterestDetails(createInterestDetails())
            .withReservedAmount(BigDecimal.valueOf(3456))
            .withRemainingPeriodicTransfers(BigDecimal.valueOf(6543))
            .withBankBranchCode2("bank branch 2")
            .withNextClosingDate(dateTime(2018, 11, 13).toLocalDate())
            .withOverdueSince(dateTime(2018, 11, 13).toLocalDate());
        arrangementDetails.setAdditions(additions);
        return arrangementDetails;
    }

    static ArrangementDetailsDto getCreditCardAccount(Set<DebitCardItemDto> debitCards,
        Map<String, String> additions) {
        ArrangementDetailsDto arrangementDetails = new ArrangementDetailsDto()
            .withDebitCards(debitCards)
            .withName("CREDIT CARDS")
            .withBookedBalance(BigDecimal.valueOf(999999.00))
            .withAvailableBalance(BigDecimal.valueOf(1000.00))
            .withIBAN("GB29NWBK601613319"+getRandomNumber())
            .withBBAN("BBAN"+getRandomNumber())
            .withCurrency("CAD")
            .withExternalTransferAllowed(false)
            .withUrgentTransferAllowed(false)
            .withAccruedInterest(BigDecimal.valueOf(10))
            .withNumber("3142")
            .withPrincipalAmount(BigDecimal.valueOf(100.00))
            .withCurrentInvestmentValue(BigDecimal.valueOf(100.00))
            .withProductNumber("String 1")
            .withBIC("CREDIT BIC")
            .withBankBranchCode("bank")
            .withAccountOpeningDate(dateTime(2017, 05, 12))
            .withAccountInterestRate(BigDecimal.valueOf(11))
            .withValueDateBalance(BigDecimal.valueOf(1000))
            .withCreditLimit(BigDecimal.valueOf(100.00))
            .withCreditLimitUsage(BigDecimal.valueOf(2000))
            .withCreditLimitInterestRate(BigDecimal.valueOf(3000))
            .withCreditLimitExpiryDate(dateTime(2017, 10, 10))
            .withStartDate(dateTime(2017, 12, 12))
            .withTermUnit(TimeUnitDto.D)
            .withTermNumber(BigDecimal.valueOf(12345))
            .withMaturityDate(dateTime(2019, 07, 07))
            .withMaturityAmount(BigDecimal.valueOf(4000))
            .withAutoRenewalIndicator(true)
            .withInterestPaymentFrequencyUnit(TimeUnitDto.M)
            .withInterestPaymentFrequencyNumber(BigDecimal.valueOf(3333))
            .withInterestSettlementAccount("Settlement Account")
            .withOutstandingPrincipalAmount(BigDecimal.valueOf(9412))
            .withMonthlyInstalmentAmount(BigDecimal.valueOf(23.23))
            .withAmountInArrear(BigDecimal.valueOf(11.2))
            .withMinimumRequiredBalance(BigDecimal.valueOf(500))
            .withCreditCardAccountNumber("Credit Card Number")
            .withValidThru(dateTime(2019, 9, 9))
            .withApplicableInterestRate(BigDecimal.valueOf(333.10))
            .withRemainingCredit(BigDecimal.valueOf(4500))
            .withOutstandingPayment(BigDecimal.valueOf(2000.20))
            .withMinimumPayment(BigDecimal.valueOf(200.60))
            .withMinimumPaymentDueDate(dateTime(2017, 11, 04))
            .withTotalInvestmentValue(BigDecimal.valueOf(1000))
            .withAccountHolderAddressLine1("address1")
            .withAccountHolderAddressLine2("address2")
            .withAccountHolderNames("Kent Jimenez,John Doe")
            .withAccountHolderStreetName("Current Account street name")
            .withTown("Current town")
            .withPostCode("10")
            .withCountrySubDivision("Current Division")
            .withAccountHolderCountry("MK")
            .withCreditAccount(true)
            .withCreditAccount(false)
            .withLastUpdateDate(dateTime(2018, 11, 11))
            .withSourceId("mock-source")
            .withParentId("parentId4")
            .withInterestDetails(createInterestDetails())
            .withReservedAmount(BigDecimal.valueOf(4567))
            .withRemainingPeriodicTransfers(BigDecimal.valueOf(4567))
            .withBankBranchCode2("bank branch 2")
            .withNextClosingDate(dateTime(2018, 11, 14).toLocalDate())
            .withOverdueSince(dateTime(2018, 11, 14).toLocalDate());
        arrangementDetails.setAdditions(additions);
        return arrangementDetails;
    }

    static ArrangementDetailsDto getLoanAccount(Set<DebitCardItemDto> debitCards, Map<String, String> additions) {
        ArrangementDetailsDto arrangementDetails = new ArrangementDetailsDto()
            .withDebitCards(debitCards)
            .withName("LOANS")
            .withBookedBalance(BigDecimal.valueOf(999999.00))
            .withAvailableBalance(BigDecimal.valueOf(1000.00))
            .withIBAN("MK072501200000"+getRandomNumber())
            .withBBAN("BBAN"+getRandomNumber())
            .withCurrency("CAD")
            .withExternalTransferAllowed(false)
            .withUrgentTransferAllowed(false)
            .withAccruedInterest(BigDecimal.valueOf(10))
            .withNumber("271828")
            .withPrincipalAmount(BigDecimal.valueOf(100.00))
            .withCurrentInvestmentValue(BigDecimal.valueOf(100.00))
            .withProductNumber("String 1")
            .withBIC("LOAN BIC")
            .withBankBranchCode("loan")
            .withAccountOpeningDate(dateTime(2017, 05, 12))
            .withAccountInterestRate(BigDecimal.valueOf(11))
            .withValueDateBalance(BigDecimal.valueOf(1000))
            .withCreditLimit(BigDecimal.valueOf(100.00))
            .withCreditLimitUsage(BigDecimal.valueOf(2000))
            .withCreditLimitInterestRate(BigDecimal.valueOf(3000))
            .withCreditLimitExpiryDate(dateTime(2017, 10, 10))
            .withStartDate(dateTime(2017, 12, 12))
            .withTermUnit(TimeUnitDto.D)
            .withTermNumber(BigDecimal.valueOf(12345))
            .withMaturityDate(dateTime(2019, 07, 07))
            .withMaturityAmount(BigDecimal.valueOf(4000))
            .withAutoRenewalIndicator(true)
            .withInterestPaymentFrequencyUnit(TimeUnitDto.M)
            .withInterestPaymentFrequencyNumber(BigDecimal.valueOf(3333))
            .withInterestSettlementAccount("Settlement Account")
            .withOutstandingPrincipalAmount(BigDecimal.valueOf(9412))
            .withMonthlyInstalmentAmount(BigDecimal.valueOf(23.23))
            .withAmountInArrear(BigDecimal.valueOf(999999))
            .withMinimumRequiredBalance(BigDecimal.valueOf(500))
            .withCreditCardAccountNumber("Credit Card Number")
            .withValidThru(dateTime(2019, 9, 9))
            .withApplicableInterestRate(BigDecimal.valueOf(333.10))
            .withRemainingCredit(BigDecimal.valueOf(4500))
            .withOutstandingPayment(BigDecimal.valueOf(2000.20))
            .withMinimumPayment(BigDecimal.valueOf(200.60))
            .withMinimumPaymentDueDate(dateTime(2017, 11, 04))
            .withTotalInvestmentValue(BigDecimal.valueOf(1000))
            .withAccountHolderAddressLine1("address1")
            .withAccountHolderAddressLine2("address2")
            .withAccountHolderNames("Larry Soto,John Doe")
            .withAccountHolderStreetName("Loan Account street name")
            .withTown("Loan town")
            .withPostCode("50")
            .withCountrySubDivision("Loan Division")
            .withAccountHolderCountry("MK")
            .withCreditAccount(true)
            .withCreditAccount(false)
            .withLastUpdateDate(dateTime(2018, 11, 11))
            .withSourceId("mock-source")
            .withParentId("parentId5")
            .withInterestDetails(createInterestDetails())
            .withReservedAmount(BigDecimal.valueOf(5678))
            .withRemainingPeriodicTransfers(BigDecimal.valueOf(8765))
            .withBankBranchCode2("bank branch 2")
            .withNextClosingDate(dateTime(2018, 11, 15).toLocalDate())
            .withOverdueSince(dateTime(2018, 11, 15).toLocalDate());
        arrangementDetails.setAdditions(additions);
        return arrangementDetails;
    }

    static ArrangementDetailsDto getTermDepositAccount(Set<DebitCardItemDto> debitCards,
        Map<String, String> additions) {
        ArrangementDetailsDto arrangementDetails = new ArrangementDetailsDto()
            .withDebitCards(debitCards)
            .withName("TERM DEPOSITS")
            .withBookedBalance(BigDecimal.valueOf(999999.00))
            .withAvailableBalance(BigDecimal.valueOf(1000.00))
            .withIBAN("MK072501200000"+getRandomNumber())
            .withBBAN("BBAN"+getRandomNumber())
            .withCurrency("SVC")
            .withExternalTransferAllowed(false)
            .withUrgentTransferAllowed(false)
            .withAccruedInterest(BigDecimal.valueOf(20))
            .withNumber("271828")
            .withPrincipalAmount(BigDecimal.valueOf(100.00))
            .withCurrentInvestmentValue(BigDecimal.valueOf(100.00))
            .withProductNumber("String 1")
            .withBIC("TERM BIC")
            .withBankBranchCode("term")
            .withAccountOpeningDate(dateTime(2017, 05, 12))
            .withAccountInterestRate(BigDecimal.valueOf(11))
            .withValueDateBalance(BigDecimal.valueOf(1000))
            .withCreditLimit(BigDecimal.valueOf(100.00))
            .withCreditLimitUsage(BigDecimal.valueOf(2000))
            .withCreditLimitInterestRate(BigDecimal.valueOf(3000))
            .withCreditLimitExpiryDate(dateTime(2017, 10, 10))
            .withStartDate(dateTime(2017, 12, 12))
            .withTermUnit(TimeUnitDto.D)
            .withTermNumber(BigDecimal.valueOf(12345))
            .withMaturityDate(dateTime(2019, 07, 07))
            .withMaturityAmount(BigDecimal.valueOf(4000))
            .withAutoRenewalIndicator(true)
            .withInterestPaymentFrequencyUnit(TimeUnitDto.M)
            .withInterestPaymentFrequencyNumber(BigDecimal.valueOf(3333))
            .withInterestSettlementAccount("Settlement Account")
            .withOutstandingPrincipalAmount(BigDecimal.valueOf(9412))
            .withMonthlyInstalmentAmount(BigDecimal.valueOf(23.23))
            .withAmountInArrear(BigDecimal.valueOf(11.2))
            .withMinimumRequiredBalance(BigDecimal.valueOf(500))
            .withCreditCardAccountNumber("Credit Card Number")
            .withValidThru(dateTime(2019, 9, 9))
            .withApplicableInterestRate(BigDecimal.valueOf(333.10))
            .withRemainingCredit(BigDecimal.valueOf(4500))
            .withOutstandingPayment(BigDecimal.valueOf(2000.20))
            .withMinimumPayment(BigDecimal.valueOf(200.60))
            .withMinimumPaymentDueDate(dateTime(2017, 11, 04))
            .withTotalInvestmentValue(BigDecimal.valueOf(1000))
            .withAccountHolderAddressLine1("address1")
            .withAccountHolderAddressLine2("address2")
            .withAccountHolderNames("Adam Neal,John Doe")
            .withAccountHolderStreetName("Term Account street name")
            .withTown("Term town")
            .withPostCode("60")
            .withCountrySubDivision("Term Division")
            .withAccountHolderCountry("MK")
            .withCreditAccount(true)
            .withCreditAccount(false)
            .withLastUpdateDate(dateTime(2018, 11, 11))
            .withSourceId("mock-source")
            .withParentId("parentId6")
            .withInterestDetails(createInterestDetails())
            .withReservedAmount(BigDecimal.valueOf(6789))
            .withRemainingPeriodicTransfers(BigDecimal.valueOf(9876))
            .withBankBranchCode2("bank branch 2")
            .withNextClosingDate(dateTime(2018, 11, 16).toLocalDate())
            .withOverdueSince(dateTime(2018, 11, 16).toLocalDate());
        arrangementDetails.setAdditions(additions);
        return arrangementDetails;
    }

    static ArrangementDetailsDto getInvestmentAccount(Set<DebitCardItemDto> debitCards,
        Map<String, String> additions) {
        ArrangementDetailsDto arrangementDetails = new ArrangementDetailsDto()
            .withDebitCards(debitCards)
            .withName("INVESTMENT ACCOUNT")
            .withBookedBalance(BigDecimal.valueOf(999999.00))
            .withAvailableBalance(BigDecimal.valueOf(1000.00))
            .withIBAN("MK072501200000"+getRandomNumber())
            .withBBAN("BBAN"+getRandomNumber())
            .withCurrency("ZAR")
            .withExternalTransferAllowed(false)
            .withUrgentTransferAllowed(false)
            .withAccruedInterest(BigDecimal.valueOf(10))
            .withNumber("271828")
            .withPrincipalAmount(BigDecimal.valueOf(100.00))
            .withCurrentInvestmentValue(BigDecimal.valueOf(100.00))
            .withProductNumber("String 1")
            .withBIC("INVEST BIC")
            .withBankBranchCode("invest")
            .withAccountOpeningDate(dateTime(2017, 05, 12))
            .withAccountInterestRate(BigDecimal.valueOf(11))
            .withValueDateBalance(BigDecimal.valueOf(1000))
            .withCreditLimit(BigDecimal.valueOf(100.00))
            .withCreditLimitUsage(BigDecimal.valueOf(2000))
            .withCreditLimitInterestRate(BigDecimal.valueOf(3000))
            .withCreditLimitExpiryDate(dateTime(2017, 10, 10))
            .withStartDate(dateTime(2017, 12, 12))
            .withTermUnit(TimeUnitDto.D)
            .withTermNumber(BigDecimal.valueOf(12345))
            .withMaturityDate(dateTime(2019, 07, 07))
            .withMaturityAmount(BigDecimal.valueOf(4000))
            .withAutoRenewalIndicator(true)
            .withInterestPaymentFrequencyUnit(TimeUnitDto.M)
            .withInterestPaymentFrequencyNumber(BigDecimal.valueOf(3333))
            .withInterestSettlementAccount("Settlement Account")
            .withOutstandingPrincipalAmount(BigDecimal.valueOf(9412))
            .withMonthlyInstalmentAmount(BigDecimal.valueOf(23.23))
            .withAmountInArrear(BigDecimal.valueOf(11.2))
            .withMinimumRequiredBalance(BigDecimal.valueOf(500))
            .withCreditCardAccountNumber("Credit Card Number")
            .withValidThru(dateTime(2019, 9, 9))
            .withApplicableInterestRate(BigDecimal.valueOf(333.10))
            .withRemainingCredit(BigDecimal.valueOf(4500))
            .withOutstandingPayment(BigDecimal.valueOf(2000.20))
            .withMinimumPayment(BigDecimal.valueOf(200.60))
            .withMinimumPaymentDueDate(dateTime(2017, 11, 04))
            .withTotalInvestmentValue(BigDecimal.valueOf(1000))
            .withAccountHolderAddressLine1("address1")
            .withAccountHolderAddressLine2("address2")
            .withAccountHolderNames("Patrick Beverly,John Doe")
            .withAccountHolderStreetName("Investment Account street name")
            .withTown("Investment town")
            .withPostCode("70")
            .withCountrySubDivision("Investment Division")
            .withAccountHolderCountry("MK")
            .withCreditAccount(true)
            .withCreditAccount(false)
            .withLastUpdateDate(dateTime(2018, 11, 11))
            .withSourceId("mock-source")
            .withParentId("parentId7")
            .withInterestDetails(createInterestDetails())
            .withReservedAmount(BigDecimal.valueOf(7890))
            .withRemainingPeriodicTransfers(BigDecimal.valueOf(987))
            .withBankBranchCode2("bank branch 2")
            .withNextClosingDate(dateTime(2018, 11, 17).toLocalDate())
            .withOverdueSince(dateTime(2018, 11, 17).toLocalDate());
        arrangementDetails.setAdditions(additions);
        return arrangementDetails;
    }

    static OffsetDateTime dateTime(int y, int m, int d) {
        return OffsetDateTime.of(y, m, d, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    private static InterestDetailsDto createInterestDetails() {
        return (InterestDetailsDto) new InterestDetailsDto()
            .withLastYearAccruedInterest(BigDecimal.ONE)
            .withDividendWithheldYTD("12.32")
            .withAnnualPercentageYield(BigDecimal.TEN)
            .withCashAdvanceInterestRate(BigDecimal.TEN)
            .withPenaltyInterestRate(BigDecimal.ONE);
    }

    public static String getRandomNumber() {
        Random rand = new Random(); //instance of random class
        int upperbound = 10000;
        return String.format("%05d", rand.nextInt(upperbound));
    }
}
