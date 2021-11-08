import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { HttpClientModule, HttpClientXsrfModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { BackbaseCoreModule, SessionTimeoutModule, StepUpModule } from '@backbase/foundation-ang/core';
import { environment } from '../environments/environment';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { BrowserAnimationsModule, NoopAnimationsModule } from '@angular/platform-browser/animations';
import { UniversalOmniPaymentWrapperWidgetAngModule } from '@backbase/universal-omni-payment-wrapper-widget-ang';
import { UniversalCreatePaymentTemplateWidgetAngModule } from '@backbase/universal-create-payment-template-widget-ang';
import { UniversalNotificationsBusinessRoutingContainerAngModule } from '@backbase/universal-notifications-business-routing-container-ang';
import { UniversalTransferWizardWidgetAngModule } from '@backbase/universal-transfer-wizard-widget-ang';
import { UniversalPaymentsBusinessRoutingContainerAngModule } from '@backbase/universal-payments-business-routing-container-ang';
import { StepUpAuthenticationComponent } from './components/step-up/step-up-authentication.component';
import { NavigationSpaWidgetModule, NavigationLinkWidgetModule } from '@backbase/universal-ang/navigation';
import { ContainersModule } from '@backbase/universal-ang/containers';
import { LayoutContainerModule } from '@backbase/universal-ang/layouts';
import { HeadingWidgetModule } from '@backbase/universal-ang/heading';
import { ContentWidgetModule, BackgroundContainerModule } from '@backbase/universal-ang/content';
import { SetLocaleWidgetModule } from '@backbase/universal-ang/locale';
import { ProgressTrackerWidgetModule } from '@backbase/universal-ang/progress-tracker';
import { CampaignSpaceWidgetModule } from '@backbase/universal-ang/campaign-space';
import { LoginWidgetModule } from '@backbase/business-ang/iam';
import { PlacesWidgetModule } from '@backbase/business-ang/places';
import { ProductSummaryAccountPickerModule } from '@backbase/business-ang/product-summary';
import { CardsListWidgetModule, CardDetailsWidgetModule } from '@backbase/business-ang/cards';
import { CashManagementCreateSweepWidgetModule, CashManagementSweepListWidgetModule, CashManagementSweepDetailsWidgetModule } from '@backbase/business-ang/cash-management';
import { PayordManagePaymentsWidgetModule, PayordDebitCreateWidgetModule, PayordDebitManageWidgetModule, PayordStopChecksListWidgetAngModule, PayordStopChecksWidgetModule, PayordManagePaymentTemplatesWidgetModule } from '@backbase/business-ang/payment-order';
import { TransactionsTableWidgetModule } from '@backbase/business-ang/transactions';
import { SelectContextWidgetModule, UserContextMenuWidgetModule } from '@backbase/business-ang/access-control';
import { NotificationsBadgeWidgetModule, NotificationsPopupsWidgetModule } from '@backbase/business-ang/notifications';
import { CustomSessionTimeoutModule } from './components/custom-session-timeout/session-timeout.module';
import { FxRatesTableWidgetModule, FxRatesPairDetailWidgetModule, FxRatesTradeOrderWidgetModule } from '@backbase/business-ang/trading-fx';
import { AuditQueryWidgetModule } from '@backbase/business-ang/audit';
import { AccountStatementBusinessWidgetModule } from '@backbase/business-ang/account-statement';
import { DataGroupFormWidgetModule, DataGroupListWidgetModule, FunctionGroupFormWidgetModule, FunctionGroupListWidgetModule, ManageUserPermissionsFormWidgetModule, ManageUserPermissionsViewWidgetModule, ServiceAgreementAdminsWidgetModule, ServiceAgreementCloseWidgetModule, ServiceAgreementDetailsWidgetModule, ServiceAgreementFormWidgetModule, ServiceAgreementListWidgetModule, ServiceAgreementParticipantsWidgetModule, ServiceAgreementUserListWidgetModule, PayeeGroupWidgetModule } from '@backbase/entitlements-ang/access-control';
import { ApprovalLogDetailsWidgetModule, ApprovalLogListWidgetModule, ManageApprovalPoliciesWidgetModule, ApprovalLevelsWidgetModule, ApprovalPoliciesWidgetModule } from '@backbase/entitlements-ang/approval';
import { LimitsViewLimitsWidgetModule } from '@backbase/entitlements-ang/limits';
import { DeviceInformationWidgetModule } from '@backbase/identity-ang/devices';
import { UserIdentitySecurityCenterWidgetModule } from '@backbase/business-ang/users';
import { TransactionSigningWidgetModule, TransactionSigningModule } from '@backbase/identity-ang/transaction-signing';
import { ImpersonationBannerWidgetModule } from '@backbase/identity-ang/impersonation';
import { WebSdkApiModule, PAGE_CONFIG, PageConfig } from '@backbase/foundation-ang/web-sdk';
import { FlowInteractionContainerModule, FlowInteractionCoreModule } from '@backbase/flow-interaction-sdk-ang/core';
import { PlacesConfigProvider, AuthProvider, HttpXsrfProvider, AccountsDisplayingFormatProvider, AccountsAliasDisplayingLevelProvider } from './config.providers';
import { CONTACT_MANAGER_BASE_PATH } from '@backbase/data-ang/contact-manager';
import { ARRANGEMENT_MANAGER_BASE_PATH } from '@backbase/data-ang/arrangements';
import { ACCESS_CONTROL_BASE_PATH } from '@backbase/data-ang/accesscontrol';
import { PAYMENT_ORDER_BASE_PATH } from '@backbase/data-ang/payment-order';
import { PAYMENT_ORDER_OPTIONS_BASE_PATH } from '@backbase/data-ang/payment-order-options';
import { PAYMENT_TEMPLATE_BASE_PATH } from '@backbase/data-ang/payment-template';
import { PAYMENT_BATCH_BASE_PATH } from '@backbase/data-ang/payment-batch';
import { PAYMENT_BATCH_TEMPLATE_BASE_PATH } from '@backbase/data-ang/payment-batch-template';
import { STOP_CHECKS_BASE_PATH } from '@backbase/data-ang/stop-checks';
import { APPROVAL_BASE_PATH } from '@backbase/data-ang/approvals';
import { LIMIT_BASE_PATH } from '@backbase/data-ang/limits';
import { CASH_FLOW_BASE_PATH } from '@backbase/data-ang/cash-flow';
import { CASH_MANAGEMENT_BASE_PATH } from '@backbase/data-ang/cash-management';
import { ACCOUNT_STATEMENT_BASE_PATH } from '@backbase/data-ang/account-statements';
import { USER_BASE_PATH } from '@backbase/data-ang/user';
import { TRANSACTIONS_BASE_PATH } from '@backbase/data-ang/transactions';
import { PLACES_BASE_PATH } from '@backbase/data-ang/places';
import { MESSAGES_BASE_PATH } from '@backbase/data-ang/messages';
import { ACTIONS_BASE_PATH } from '@backbase/data-ang/actions';
import { NOTIFICATIONS_BASE_PATH } from '@backbase/data-ang/notifications';
import { CARDS_BASE_PATH } from '@backbase/data-ang/cards';
import { FOREX_BASE_PATH } from '@backbase/data-ang/trading-fx';
import { AUDIT_BASE_PATH } from '@backbase/data-ang/audit';
import { DEVICE_BASE_PATH } from '@backbase/data-ang/device-management';
import { POSITIVE_PAY_CHECK_BASE_PATH } from '@backbase/data-ang/positive-pay-check-v1';
import { POSITIVE_PAY_ACH_BASE_PATH } from '@backbase/data-ang/positive-pay-ach-v1';
import { LOANS_BASE_PATH } from '@backbase/data-ang/loans';
import { Payee, ElectronicPayee } from '@backbase/data-ang/billpay';
import { bundlesDefinition } from './bundles-definition';
import { CustomSessionTimeoutComponent } from './components/custom-session-timeout/session-timeout.component';
import { StepUpConfig } from './components/step-up/step-up-config';

export function getBasePath(servicePath: string) { return (config: PageConfig) => `${config.apiRoot}/${servicePath}`; }

@NgModule({
  declarations: [
    AppComponent,
    StepUpAuthenticationComponent
  ],
  imports: [
    BrowserModule,
    StoreModule.forRoot({}),
    EffectsModule.forRoot([]),
    HttpClientModule,
    BackbaseCoreModule.forRoot({
      features: {
        EXTRA_ENCODE_URI_PARAMS: true,
        MANUAL_BATCHES: true,
      },
      lazyModules: bundlesDefinition,
      assets: {
        assetsStaticItemName: environment.assetsStaticItemName || '',
      },
      classMap: {},
      flows: [
        {
          output: { classId: 'CashManagementCreateSweepWidgetComponent', outputName: 'sweepCreated' },
          input: { classId: 'CashManagementSweepsListWidgetComponent', inputName: 'sweepCreated' },
        },
        {
          output: { classId: 'BillpayPayeeListWidgetComponent', outputName: 'payPayeeOneOff' },
          input: { classId: 'BillpayManagePaymentWidgetComponent', inputName: 'payee' },
          mapPayload: fromPayeeObjectToOneOffPaymentAndPayeeId,
        },
        {
          output: { classId: 'BillpayPayeeListWidgetComponent', outputName: 'payPayeeRecurring' },
          input: { classId: 'BillpayManagePaymentWidgetComponent', inputName: 'payee' },
          mapPayload: fromPayeeObjectToRecurringPaymentAndPayeeId,
        },
      ],
      logDeprecations: true,
    }),
    RouterModule.forRoot([], { initialNavigation: "disabled", useHash: true }),
    environment.animation ? BrowserAnimationsModule : NoopAnimationsModule,
    UniversalOmniPaymentWrapperWidgetAngModule,
    UniversalCreatePaymentTemplateWidgetAngModule,
    UniversalNotificationsBusinessRoutingContainerAngModule,
    UniversalTransferWizardWidgetAngModule,
    UniversalPaymentsBusinessRoutingContainerAngModule,
    HttpClientXsrfModule.disable(),
    SessionTimeoutModule.forRoot({
      sessionTimeoutComponentClass: CustomSessionTimeoutComponent,
      inactivityModalTime: 60,
    }),
    StepUpModule.forRoot(StepUpConfig),
    NavigationSpaWidgetModule,
    NavigationLinkWidgetModule,
    ContainersModule,
    LayoutContainerModule,
    HeadingWidgetModule,
    ContentWidgetModule,
    BackgroundContainerModule,
    SetLocaleWidgetModule,
    ProgressTrackerWidgetModule,
    CampaignSpaceWidgetModule,
    LoginWidgetModule,
    PlacesWidgetModule,
    ProductSummaryAccountPickerModule,
    CardsListWidgetModule,
    CardDetailsWidgetModule,
    CashManagementCreateSweepWidgetModule,
    CashManagementSweepListWidgetModule,
    CashManagementSweepDetailsWidgetModule,
    PayordManagePaymentsWidgetModule,
    PayordDebitCreateWidgetModule,
    PayordDebitManageWidgetModule,
    PayordStopChecksListWidgetAngModule,
    PayordStopChecksWidgetModule,
    PayordManagePaymentTemplatesWidgetModule,
    TransactionsTableWidgetModule,
    SelectContextWidgetModule,
    UserContextMenuWidgetModule,
    NotificationsBadgeWidgetModule,
    NotificationsPopupsWidgetModule,
    CustomSessionTimeoutModule,
    FxRatesTableWidgetModule,
    FxRatesPairDetailWidgetModule,
    FxRatesTradeOrderWidgetModule,
    AuditQueryWidgetModule,
    AccountStatementBusinessWidgetModule,
    DataGroupFormWidgetModule,
    DataGroupListWidgetModule,
    FunctionGroupFormWidgetModule,
    FunctionGroupListWidgetModule,
    ManageUserPermissionsFormWidgetModule,
    ManageUserPermissionsViewWidgetModule,
    ServiceAgreementAdminsWidgetModule,
    ServiceAgreementCloseWidgetModule,
    ServiceAgreementDetailsWidgetModule,
    ServiceAgreementFormWidgetModule,
    ServiceAgreementListWidgetModule,
    ServiceAgreementParticipantsWidgetModule,
    ServiceAgreementUserListWidgetModule,
    PayeeGroupWidgetModule,
    ApprovalLogDetailsWidgetModule,
    ApprovalLogListWidgetModule,
    ManageApprovalPoliciesWidgetModule,
    ApprovalLevelsWidgetModule,
    ApprovalPoliciesWidgetModule,
    LimitsViewLimitsWidgetModule,
    DeviceInformationWidgetModule,
    UserIdentitySecurityCenterWidgetModule,
    TransactionSigningWidgetModule,
    TransactionSigningModule.withConfig({
      useRedirectFlow: false,
    }),
    ImpersonationBannerWidgetModule,
    WebSdkApiModule,
    FlowInteractionContainerModule,
    FlowInteractionCoreModule
  ],
  providers: [...environment.mockProviders || [], PlacesConfigProvider, AuthProvider, HttpXsrfProvider, AccountsDisplayingFormatProvider, AccountsAliasDisplayingLevelProvider, {
    provide: CONTACT_MANAGER_BASE_PATH, useFactory: getBasePath('contact-manager'), deps: [PAGE_CONFIG]
  }, {
    provide: ARRANGEMENT_MANAGER_BASE_PATH, useFactory: getBasePath('arrangement-manager'), deps: [PAGE_CONFIG]
  }, {
    provide: ACCESS_CONTROL_BASE_PATH, useFactory: getBasePath('access-control'), deps: [PAGE_CONFIG]
  }, {
    provide: PAYMENT_ORDER_BASE_PATH, useFactory: getBasePath('payment-order-service'), deps: [PAGE_CONFIG]
  }, {
    provide: PAYMENT_ORDER_OPTIONS_BASE_PATH, useFactory: getBasePath('payment-order-options'), deps: [PAGE_CONFIG]
  }, {
    provide: PAYMENT_TEMPLATE_BASE_PATH, useFactory: getBasePath('payment-order-service'), deps: [PAGE_CONFIG]
  }, {
    provide: PAYMENT_BATCH_BASE_PATH, useFactory: getBasePath('payment-batch'), deps: [PAGE_CONFIG]
  }, {
    provide: PAYMENT_BATCH_TEMPLATE_BASE_PATH, useFactory: getBasePath('payment-batch'), deps: [PAGE_CONFIG]
  }, {
    provide: STOP_CHECKS_BASE_PATH, useFactory: getBasePath('stop-checks'), deps: [PAGE_CONFIG]
  }, {
    provide: APPROVAL_BASE_PATH, useFactory: getBasePath('approval-service'), deps: [PAGE_CONFIG]
  }, {
    provide: LIMIT_BASE_PATH, useFactory: getBasePath('limit'), deps: [PAGE_CONFIG]
  }, {
    provide: CASH_FLOW_BASE_PATH, useFactory: getBasePath('cashflow-service'), deps: [PAGE_CONFIG]
  }, {
    provide: CASH_MANAGEMENT_BASE_PATH, useFactory: getBasePath('cash-management-presentation-service'), deps: [PAGE_CONFIG]
  }, {
    provide: ACCOUNT_STATEMENT_BASE_PATH, useFactory: getBasePath('account-statement'), deps: [PAGE_CONFIG]
  }, {
    provide: USER_BASE_PATH, useFactory: getBasePath('user-manager'), deps: [PAGE_CONFIG]
  }, {
    provide: TRANSACTIONS_BASE_PATH, useFactory: getBasePath('transaction-manager'), deps: [PAGE_CONFIG]
  }, {
    provide: PLACES_BASE_PATH, useFactory: getBasePath('places-presentation-service'), deps: [PAGE_CONFIG]
  }, {
    provide: MESSAGES_BASE_PATH, useFactory: getBasePath('messages-service'), deps: [PAGE_CONFIG]
  }, {
    provide: ACTIONS_BASE_PATH, useFactory: getBasePath('action'), deps: [PAGE_CONFIG]
  }, {
    provide: NOTIFICATIONS_BASE_PATH, useFactory: getBasePath('notifications-service'), deps: [PAGE_CONFIG]
  }, {
    provide: CARDS_BASE_PATH, useFactory: getBasePath('cards-presentation-service'), deps: [PAGE_CONFIG]
  }, {
    provide: FOREX_BASE_PATH, useFactory: getBasePath('tradingfx-presentation-service'), deps: [PAGE_CONFIG]
  }, {
    provide: AUDIT_BASE_PATH, useFactory: getBasePath('audit-service'), deps: [PAGE_CONFIG]
  }, {
    provide: DEVICE_BASE_PATH, useFactory: getBasePath('device-management-service'), deps: [PAGE_CONFIG]
  }, {
    provide: POSITIVE_PAY_CHECK_BASE_PATH, useFactory: getBasePath('positive-pay-check'), deps: [PAGE_CONFIG]
  }, {
    provide: POSITIVE_PAY_ACH_BASE_PATH, useFactory: getBasePath('positive-pay-ach'), deps: [PAGE_CONFIG]
  }, {
    provide: LOANS_BASE_PATH, useFactory: getBasePath('loan'), deps: [PAGE_CONFIG]
  }],
  bootstrap: [AppComponent]
})
export class AppModule { }

export function fromPayeeObjectToOneOffPaymentAndPayeeId(payee?: Payee | ElectronicPayee) {
  return payee ? `o-${payee.id}` : '';
}

export function fromPayeeObjectToRecurringPaymentAndPayeeId(payee?: Payee | ElectronicPayee) {
  return payee ? `r-${payee.id}` : '';
}
