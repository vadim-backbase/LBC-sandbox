/*
 *
 * The content of this file can be edited freely, but to maintain upgradability
 * this file should not be renamed and should always include an array named
 * `routes` of type `Routes` and should always export an `@NgModule` class named
 * `CustomerSupportWorkspaceRoutesModule`.
 *
 * You may freely alter the routes defined in this file, but please be aware that
 * doing so may prevent some future updates being automatically applied by
 * migration schematics.  In such cases, the schematic will log a warning and
 * manual steps may be required to adopt any new features or fix breaking changes.
 *
 */

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {
  UserSearchWrapperComponent,
  UserSearchWrapperModule,
} from '@backbase/employee-web-app-shared-feature-user-admin';
import {
  EmployeeWebAppLayoutModule,
  WorkspaceContainerComponent,
} from '@backbase/employee-web-app-shared-ui-layout';
import {
  ConversationListAllComponent,
  ConversationListMineComponent,
  ConversationListResolvedByMeComponent,
  ConversationListResolvedComponent,
  ConversationListUnassignedComponent,
  MessageCenterComponent,
  MessageCenterModule
} from '@backbase/employee-web-app-customer-support-feature-message-center';

const messageCenterRoutes: Routes = [
  {
    path: '',
    component: MessageCenterComponent,
    children: [
      {
        path: 'all',
        component: ConversationListAllComponent,
      },
      {
        path: 'mine',
        component: ConversationListMineComponent,
      },
      {
        path: 'unassigned',
        component: ConversationListUnassignedComponent,
      },
      {
        path: 'resolved',
        component: ConversationListResolvedComponent,
      },
      {
        path: 'resolved-by-me',
        component: ConversationListResolvedByMeComponent,
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'all',
      },
    ],
  },
];

const routes: Routes = [
  {
    path: 'users/:userId',
    loadChildren: () =>
      import('./user-mode/user-mode-loader.module').then(
        (mod) => mod.UserModeLoaderModule
      ),
  },
  {
    path: '',
    component: WorkspaceContainerComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'users',
      },
      { path: 'users', component: UserSearchWrapperComponent },
      {
        path: 'messages',
        children: messageCenterRoutes,
      },
    ],
  },
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    EmployeeWebAppLayoutModule,
    UserSearchWrapperModule,
    MessageCenterModule,
  ],
})
export class CustomerSupportWorkspaceRoutesModule {}
