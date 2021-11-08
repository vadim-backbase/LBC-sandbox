/*
 *
 * The content of this file can be edited freely, but to maintain upgradability
 * this file should not be renamed and should always include an array named
 * `routes` of type `Routes` and should always export an `@NgModule` class named
 * `BankAdminWorkspaceRoutesModule`.
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
  ApprovalLogListWidgetWrapperComponent,
  ApprovalLogDetailsWidgetWrapperComponent,
  ApprovalLogModule
} from '@backbase/employee-web-app-shared-feature-approval-tasks';
import {
  ApprovalsComponent,
  ApprovalLevelsComponent,
  ApprovalPoliciesComponent,
  ApprovalsAdminModule
} from '@backbase/employee-web-app-shared-feature-approval-admin';
import {
  GlobalLimitsFormComponent,
  GlobalLimitsFormModule
} from '@backbase/employee-web-app-shared-feature-limits-admin';
import {
  CreateUserAdminWidgetWrapperComponent,
  CreateUserModule
} from '@backbase/employee-web-app-shared-feature-user-admin';
import {
  CreateServiceAgreementFormComponent,
  ServiceAgreementsSearchWidgetWrapperComponent,
  BankAdminServiceAgreementsModule
} from '@backbase/employee-web-app-shared-feature-service-agreements';
import {
  TopicManagementModule,
  MessagesTopicDetailsWidgetWrapperComponent,
  MessagesTopicsListWidgetWrapperComponent,
} from '@backbase/employee-web-app-bank-admin-feature-topic-management';
import {
  UserEnrollmentModule,
  UserEnrollmentSearchWidgetWrapperComponent,
  UserEnrollmentCustomerDetailsWidgetWrapperComponent,
  UserEnrollmentCustomerManagementComponent,
} from '@backbase/employee-web-app-bank-admin-feature-user-enrollment';
import {
  EmployeeWebAppLayoutModule,
  WorkspaceContainerComponent,
} from '@backbase/employee-web-app-shared-ui-layout';

const userEnrollmentRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'search',
  },
  {
    path: 'search',
    pathMatch: 'full',
    component: UserEnrollmentSearchWidgetWrapperComponent,
  },
  {
    path: 'search/:externalLegalEntityId',
    component: UserEnrollmentCustomerDetailsWidgetWrapperComponent,
  },
  {
    path: 'enroll',
    pathMatch: 'full',
    redirectTo: 'search',
  },
  {
    path: 'enroll/:externalLegalEntityId',
    component: UserEnrollmentCustomerManagementComponent,
  },
];

const userCreationRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: CreateUserAdminWidgetWrapperComponent,
  },
];

const auditRoutes: Routes = [
  {
    path: '',
    loadChildren: () => import('./journeys/audit-journey-loader.module').then(mod => mod.AuditJourneyLoaderModule),
  },
];

const topicManagementRoutes: Routes = [
  {
    path: ':topicId',
    component: MessagesTopicDetailsWidgetWrapperComponent,
  },
  {
    path: '',
    pathMatch: 'full',
    component: MessagesTopicsListWidgetWrapperComponent,
  },
];

const serviceAgreementsRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'search',
  },
  {
    path: 'search',
    pathMatch: 'full',
    component: ServiceAgreementsSearchWidgetWrapperComponent,
  },
  {
    path: 'create',
    pathMatch: 'full',
    component: CreateServiceAgreementFormComponent,
    canDeactivate: [CanDeactivateUnsavedChangesGuard],
  },
];

const approvalsRoutes: Routes = [
  {
    path: '',
    component: ApprovalsComponent,
    children: [
      {
        path: 'levels',
        component: ApprovalLevelsComponent,
      },
      {
        path: 'policies',
        component: ApprovalPoliciesComponent,
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'levels',
      },
    ],
  },
];

const routes: Routes = [
  {
    path: '',
    component: WorkspaceContainerComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'user-enrollment',
      },
      {
        path: 'user-enrollment',
        children: userEnrollmentRoutes,
      },
      {
        path: 'create-user',
        children: userCreationRoutes,
      },
      {
        path: 'audit',
        children: auditRoutes,
      },
      {
        path: 'topics',
        children: topicManagementRoutes,
      },
      {
        path: 'service-agreements',
        children: serviceAgreementsRoutes,
      },
      {
        path: 'approval-log',
        component: ApprovalLogListWidgetWrapperComponent,
      },
      {
        path: 'approval-log/:approvalId',
        component: ApprovalLogDetailsWidgetWrapperComponent,
      },
      {
        path: 'approvals',
        children: approvalsRoutes,
      },
      {
        path: 'global-limits',
        component: GlobalLimitsFormComponent,
        canDeactivate: [CanDeactivateUnsavedChangesGuard],
      },
    ],
  },
  {
    path: 'service-agreements',
    loadChildren: () =>
      import(
        './service-agreement-mode/service-agreement-mode-loader.module'
      ).then((mod) => mod.ServiceAgreementModeLoaderModule),
  },
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    ApprovalLogModule,
    ApprovalsAdminModule,
    GlobalLimitsFormModule,
    CreateUserModule,
    BankAdminServiceAgreementsModule,
    TopicManagementModule,
    UserEnrollmentModule,
    EmployeeWebAppLayoutModule
  ],
})
export class BankAdminWorkspaceRoutesModule {}
