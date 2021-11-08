/*
 *
 * The content of this file can be edited freely, but to maintain upgradability
 * this file should not be renamed and should always include an array named
 * `routes` of type `Routes` and should always export an `@NgModule` class named
 * `ServiceAgreementModeRoutesModule`.
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
  CreateDataGroupFormComponent,
  DataGroupListWidgetWrapperComponent,
  EditDataGroupFormComponent,
  AccountGroupsModule,
} from '@backbase/employee-web-app-service-agreement-mode-feature-account-groups';
import {
  EditServiceAgreementFormComponent,
  EditServiceAgreementGuard,
  ServiceAgreementModeFeatureEditServiceAgreementFormModule,
} from '@backbase/employee-web-app-service-agreement-mode-feature-edit-service-agreement-form';
import {
  FunctionGroupFormContainerComponent,
  ServiceAgreementModeFeatureFunctionGroupFormModule,
} from '@backbase/employee-web-app-service-agreement-mode-feature-function-group-form';
import {
  FunctionGroupListWidgetWrapperComponent,
  ServiceAgreementModeFeatureJobRolesModule,
} from '@backbase/employee-web-app-service-agreement-mode-feature-job-roles';
import {
  ManageApprovalPoliciesComponent,
  ServiceAgreementModeFeatureApprovalPoliciesModule,
} from '@backbase/employee-web-app-service-agreement-mode-feature-approval-policies';
import {
  ServiceAgreementModeComponent,
  ServiceAgreementModeFeatureChromeModule,
} from '@backbase/employee-web-app-service-agreement-mode-feature-chrome';
import {
  ServiceAgreementOverviewComponent,
  ServiceAgreementOverviewModule,
} from '@backbase/employee-web-app-service-agreement-mode-feature-overview';
import {
  ServiceAgreementUserListComponent,
  ViewUserPermissionsComponent,
  UsersAndPermissionsModule,
} from '@backbase/employee-web-app-service-agreement-mode-feature-users-and-permissions';
import {
  EditUserPermissionsComponent,
  EditUserPermissionsModule,
} from '@backbase/employee-web-app-service-agreement-mode-feature-users-and-permissions-edit';


const serviceAgreementModeRoutes: Routes = [
  {
    path: ':serviceAgreementId',
    component: ServiceAgreementModeComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: ServiceAgreementOverviewComponent,
      },
      {
        path: 'edit',
        component: EditServiceAgreementFormComponent,
        canDeactivate: [CanDeactivateUnsavedChangesGuard],
        canActivate: [EditServiceAgreementGuard],
      },
      {
        path: 'approvals',
        pathMatch: 'full',
        component: ManageApprovalPoliciesComponent,
      },
      {
        path: 'users-and-permissions',
        pathMatch: 'full',
        component: ServiceAgreementUserListComponent,
      },
      {
        path: 'users-and-permissions/:userId/edit',
        component: EditUserPermissionsComponent,
        canDeactivate: [CanDeactivateUnsavedChangesGuard],
      },
      {
        path: 'users-and-permissions/:userId',
        component: ViewUserPermissionsComponent,
      },
      {
        path: 'job-roles',
        pathMatch: 'full',
        component: FunctionGroupListWidgetWrapperComponent,
      },
      {
        path: 'job-roles/create',
        pathMatch: 'full',
        component: FunctionGroupFormContainerComponent,
        canDeactivate: [CanDeactivateUnsavedChangesGuard],
      },
      {
        path: 'job-roles/:functionGroupId/edit',
        component: FunctionGroupFormContainerComponent,
        canDeactivate: [CanDeactivateUnsavedChangesGuard],
      },
      {
        path: 'account-groups',
        pathMatch: 'full',
        component: DataGroupListWidgetWrapperComponent,
      },
      {
        path: 'account-groups/create',
        pathMatch: 'full',
        component: CreateDataGroupFormComponent,
        canDeactivate: [CanDeactivateUnsavedChangesGuard],
      },
      {
        path: 'account-groups/:accountGroupId/edit',
        component: EditDataGroupFormComponent,
        canDeactivate: [CanDeactivateUnsavedChangesGuard],
      },
      {
        path: 'account-groups/:accountGroupId',
        redirectTo: 'account-groups/:accountGroupId/edit',
      },
    ],
  },
];

@NgModule({
  imports: [
    RouterModule.forChild(serviceAgreementModeRoutes),
    AccountGroupsModule,
    ServiceAgreementModeFeatureApprovalPoliciesModule,
    ServiceAgreementModeFeatureChromeModule,
    ServiceAgreementModeFeatureEditServiceAgreementFormModule,
    ServiceAgreementModeFeatureFunctionGroupFormModule,
    ServiceAgreementModeFeatureJobRolesModule,
    ServiceAgreementOverviewModule,
    UsersAndPermissionsModule,
    EditUserPermissionsModule,
  ],
})
export class ServiceAgreementModeRoutesModule {}
