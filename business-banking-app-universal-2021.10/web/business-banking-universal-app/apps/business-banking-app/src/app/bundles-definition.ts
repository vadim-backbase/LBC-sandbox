import { LazyConfig } from '@backbase/foundation-ang/core';

export const bundlesDefinition: LazyConfig = [
  {
    module: 'ActionsBusinessNotificationPreferencesJourneyBundleModule',
    loadChildren: () =>
      import('../bundles/actions-business-notification-preferences-journey-bundle.module').then(
        (m) => m.ActionsBusinessNotificationPreferencesJourneyBundleModule,
      ),
  },
  {
    module: 'BatchesJourneyBundleModule',
    loadChildren: () => import('../bundles/batches-journey-bundle.module').then((m) => m.BatchesJourneyBundleModule),
  },
  {
    module: 'BatchTemplatesJourneyBundleModule',
    loadChildren: () =>
      import('../bundles/batch-templates-journey-bundle.module').then((m) => m.BatchTemplatesJourneyBundleModule),
  },
  {
    module: 'CashFlowJourneyBundleModule',
    loadChildren: () => import('../bundles/cash-flow-journey-bundle.module').then((m) => m.CashFlowJourneyBundleModule),
  },
  {
    module: 'PositivePayJourneyBundleModule',
    loadChildren: () =>
      import('../bundles/positive-pay-journey-bundle.module').then((m) => m.PositivePayJourneyBundleModule),
  },
  {
    module: 'AchPositivePayJourneyBundleModule',
    loadChildren: () =>
      import('../bundles/ach-positive-pay-journey-bundle.module').then((m) => m.AchPositivePayJourneyBundleModule),
  },
  {
    module: 'LoansJourneyBundleModule',
    loadChildren: () => import('../bundles/loans-journey-bundle.module').then((m) => m.LoansJourneyBundleModule),
  },
  {
    module: 'StopChecksJourneyBundleModule',
    loadChildren: () =>
      import('../bundles/stop-checks-journey-bundle.module').then((m) => m.StopChecksJourneyBundleModule),
  },
  {
    module: 'TradeFinanceJourneyBundleModule',
    loadChildren: () =>
      import('../bundles/trade-finance-journey-bundle.module').then((m) => m.TradeFinanceJourneyBundleModule),
  },
  {
    module: 'AccountsJourneyBundleModule',
    loadChildren: () => import('../bundles/accounts-journey-bundle.module').then((m) => m.AccountsJourneyBundleModule),
  },
  {
    module: 'MessagesClientInboxJourneyBundleModule',
    loadChildren: () =>
      import('../bundles/messages-client-inbox-journey-bundle.module').then(
        (m) => m.MessagesClientInboxJourneyBundleModule,
      ),
  },
  {
    module: 'ContactJourneyBundleModule',
    loadChildren: () => import('../bundles/contact-journey-bundle.module').then((m) => m.ContactJourneyBundleModule),
  },
  {
    module: 'AccountStatementBusinessJourneyBundleModule',
    loadChildren: () =>
      import('../bundles/account-statement-business-journey-bundle.module').then(
        (m) => m.AccountStatementBusinessJourneyBundleModule,
      ),
  },
];
