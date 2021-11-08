/*
 *
 * The content of this file can be edited freely, but to maintain upgradability
 * this file should not be renamed and should always export an array named
 * `workspaceRoutes` and an array named `additionalRoutes`.
 *
 *
 */

import { Routes } from '@angular/router';
import { UserContextGuard } from '@backbase/employee-web-app-shared-util-core';

/**
 * Route definitions in this array are added as child routes of the
 * EmployeeWebAppChromeComponent in app.module.ts.
 */
export const workspaceRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'support' },
  {
    path: 'support',
    canActivate: [UserContextGuard],
    loadChildren: () =>
      import(
        './workspaces/customer-support/customer-support-workspace-loader.module'
      ).then((mod) => mod.CustomerSupportWorkspaceLoaderModule),
  },
  {
    path: 'admin',
    canActivate: [UserContextGuard],
    loadChildren: () =>
      import('./workspaces/bank-admin/bank-admin-workspace-loader.module').then(
        (mod) => mod.BankAdminWorkspaceLoaderModule
      ),
  },
];

/**
 * Route definitions in this array are added to the routes of the root
 * RouterModule defined in app.module.ts.
 */
export const additionalRoutes: Routes = [];
