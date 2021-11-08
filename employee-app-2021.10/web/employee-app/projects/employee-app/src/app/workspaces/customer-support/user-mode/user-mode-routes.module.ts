/*
 *
 * The content of this file can be edited freely, but to maintain upgradability
 * this file should not be renamed and should always include an array named
 * `routes` of type `Routes` and should always export an `@NgModule` class named
 * `UserModeRoutesModule`.
 *
 * You may freely alter the routes defined in this file, but please be aware that
 * doing so may prevent some future updates being automatically applied by
 * migration schematics.  In such cases, the schematic will log a warning and
 * manual steps may be required to adopt any new features or fix breaking changes.
 *
 */

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { CanDeactivateUnsavedChangesGuard } from '@backbase/employee-web-app-shared-util-unsaved-changes';
import {
  UserCommentsComponent,
  UserModeFeatureUserCommentsModule,
} from '@backbase/employee-web-app-user-mode-feature-user-comments';
import {
  AccountDetailsByTypeComponent,
  AccountDetailsComponent,
  AccountDetailsModule,
} from '@backbase/employee-web-app-user-mode-feature-account-details';
import {
  TransactionsComponent,
  TransactionsModule,
} from '@backbase/employee-web-app-user-mode-feature-transactions';
import {
  UserModeComponent,
  UserModeFeatureChromeModule,
} from '@backbase/employee-web-app-user-mode-feature-chrome';
import {
  UserModeSecurityComponent,
  UserModeFeatureSecurityModule,
} from '@backbase/employee-web-app-user-mode-feature-security';
import {
  DeviceInformationWrapperComponent,
  DeviceInformationWrapperModule,
} from '@backbase/employee-web-app-user-mode-feature-devices';
import {
  ProductsComponent,
  AccountSummaryModule,
} from '@backbase/employee-web-app-user-mode-feature-account-summary';
import {
  PaymentsComponent,
  PaymentsModule,
} from '@backbase/employee-web-app-user-mode-feature-payments';
import {
  MessagesComponent,
  UserModeFeatureMessagesModule,
} from '@backbase/employee-web-app-user-mode-feature-messages';
import {
  ActivityLogComponent,
  UserModeFeatureActivityLogModule,
} from '@backbase/employee-web-app-user-mode-feature-activity-log';
import {
  UserDetailsWrapperComponent,
  UserDetailsWrapperModule,
} from '@backbase/employee-web-app-user-mode-feature-user-details';
import {
  UserOverviewComponent,
  UserOverviewModule,
} from '@backbase/employee-web-app-user-mode-feature-user-overview';
import {
  SessionsComponent,
  SessionsModule,
} from '@backbase/employee-web-app-user-mode-feature-sessions';
import { CardsComponent } from '@backbase/employee-web-app-user-mode-feature-cards';
import { ClosedCardsComponent } from '@backbase/employee-web-app-user-mode-feature-cards';
import { CurrentCardsComponent } from '@backbase/employee-web-app-user-mode-feature-cards';
import { UserModeFeatureCardsModule } from '@backbase/employee-web-app-user-mode-feature-cards';

const accountDetailsRoutes: Routes = [
  {
    path: '',
    component: AccountDetailsComponent,
    children: [
      {
        path: 'transactions',
        component: TransactionsComponent,
      },
      {
        path: 'info',
        component: AccountDetailsByTypeComponent,
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'transactions',
      },
    ],
  },
];

const cardsRoutes: Routes = [
  {
    path: '',
    component: CardsComponent,
    children: [
      {
        path: 'current',
        component: CurrentCardsComponent,
      },
      {
        path: 'closed',
        component: ClosedCardsComponent,
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'current',
      },
    ],
  },
];

const routes: Routes = [
  {
    path: 'sa',
    redirectTo: 'sa/-',
  },
  {
    path: 'sa/:serviceAgreementId',
    component: UserModeComponent,
    children: [
      {
        path: 'security',
        component: UserModeSecurityComponent,
      },
      {
        path: 'devices',
        component: DeviceInformationWrapperComponent,
      },
      {
        path: 'products',
        component: ProductsComponent,
      },
      {
        path: 'payments',
        component: PaymentsComponent,
      },
      {
        path: 'messages',
        component: MessagesComponent,
      },
      {
        path: 'activity-log',
        component: ActivityLogComponent,
        loadChildren: () => import('./journeys/audit-journey-loader.module').then(m => m.AuditJourneyLoaderModule)
      },
      {
        path: 'profile',
        component: UserDetailsWrapperComponent,
        canDeactivate: [CanDeactivateUnsavedChangesGuard],
      },
      {
        path: '',
        pathMatch: 'full',
        component: UserOverviewComponent,
      },
      {
        path: 'sessions',
        component: SessionsComponent,
      },
      {
        path: 'comments',
        component: UserCommentsComponent,
        canDeactivate: [CanDeactivateUnsavedChangesGuard],
      },
      {
        path: 'products/:accountId',
        children: accountDetailsRoutes,
      },
      {
        path: 'cards',
        children: cardsRoutes,
      },
    ],
  },
  {
    path: '',
    redirectTo: 'sa/-',
  },
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    UserModeFeatureUserCommentsModule,
    AccountDetailsModule,
    TransactionsModule,
    UserModeFeatureChromeModule,
    UserModeFeatureSecurityModule,
    DeviceInformationWrapperModule,
    AccountSummaryModule,
    PaymentsModule,
    UserModeFeatureMessagesModule,
    UserModeFeatureActivityLogModule,
    UserDetailsWrapperModule,
    UserOverviewModule,
    SessionsModule,
    UserModeFeatureCardsModule,
  ],
})
export class UserModeRoutesModule {}
