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
import { ServiceAgreementModeRoutesModule } from './service-agreement-mode-routes.module';
import { serviceAgreementModeModuleImports } from './service-agreement-mode-module-imports';
import { ServiceAgreementModeNavigationComponent } from './navigation/service-agreement-mode-navigation.component';
import { RouterModule } from '@angular/router';
import { EmployeeWebAppLayoutModule, Sidebar } from '@backbase/employee-web-app-shared-ui-layout';

export const sidebar = new Sidebar(ServiceAgreementModeNavigationComponent);

@NgModule({
  imports: [
    EmployeeWebAppLayoutModule,
    ServiceAgreementModeRoutesModule,
    RouterModule,
    ...serviceAgreementModeModuleImports,
  ],
  declarations: [ServiceAgreementModeNavigationComponent],
  entryComponents: [ServiceAgreementModeNavigationComponent],
  providers: [{ provide: Sidebar, useValue: sidebar }],
})
export class ServiceAgreementModeLoaderModule {}
