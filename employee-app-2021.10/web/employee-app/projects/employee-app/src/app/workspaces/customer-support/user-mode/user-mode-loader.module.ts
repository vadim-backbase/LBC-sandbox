/*
 *
 *
 *
 *
 *
 *         WARNING: Editing this file may prevent future updates via schematics.
 *                  To maintain easy upgradability, do not edit this file.
 *
 *
 *
 *
 *
 */
import { NgModule } from '@angular/core';
import { UserModeRoutesModule } from './user-mode-routes.module';
import { userModeModuleImports } from './user-mode-module-imports';
import { UserModeNavigationComponent } from './navigation/user-mode-navigation.component';
import {EmployeeWebAppLayoutModule, Sidebar} from '@backbase/employee-web-app-shared-ui-layout';
import { RouterModule } from '@angular/router';

export const sidebar = new Sidebar(UserModeNavigationComponent);

@NgModule({
  imports: [
    UserModeRoutesModule,
    RouterModule,
    ...userModeModuleImports,
    EmployeeWebAppLayoutModule
  ],
  declarations: [UserModeNavigationComponent],
  entryComponents: [UserModeNavigationComponent],
  providers: [{ provide: Sidebar, useValue: sidebar }],
})
export class UserModeLoaderModule {}
