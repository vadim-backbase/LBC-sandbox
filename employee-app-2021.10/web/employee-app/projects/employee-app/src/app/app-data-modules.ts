/*
 *
 * The content of this file can be edited freely, but to maintain upgradability
 * this file should not be renamed and should always export an Angular module named
 * `AppDataModules`.
 *
 *
 */

import { InjectionToken, NgModule } from '@angular/core';
import { EmployeeWebAppEnvironment } from '@backbase/employee-web-app-shared-util-core';
import { ACCESS_CONTROL_BASE_PATH } from '@backbase/data-ang/accesscontrol';
import { APPROVAL_BASE_PATH } from '@backbase/data-ang/approvals';
import { ARRANGEMENT_MANAGER_BASE_PATH } from '@backbase/data-ang/arrangements';
import { AUDIT_BASE_PATH } from '@backbase/data-ang/audit';
import { DEVICE_BASE_PATH } from '@backbase/data-ang/device';
import { LIMIT_BASE_PATH } from '@backbase/data-ang/limits';
import { NOTIFICATIONS_BASE_PATH } from '@backbase/data-ang/notifications';
import { USER_BASE_PATH } from '@backbase/data-ang/user';
import { EMPLOYEE_BASE_PATH } from '@backbase/data-ang/employee';
import { MESSAGES_BASE_PATH } from '@backbase/data-ang/messages';
import { PAYMENT_ORDER_BASE_PATH } from '@backbase/data-ang/payment-order';
import { USER_PROFILE_BASE_PATH } from '@backbase/data-ang/user-profile-manager';
import { COMMENTS_BASE_PATH } from '@backbase/data-ang/comments-v2';
import { IMPERSONATION_BASE_PATH } from '@backbase/data-ang/impersonation-v1';

/**
 * Returns a provider factory function for the given servicePath which will
 * prepend the global apiRoot prefix provided by EmployeeWebAppEnvironment to
 * the given service path.
 *
 * The apiRoot prefix can be configured explicitly in environment.ts.  If not
 * set, it will fall back to a default value as determined by the
 * EmployeeWebAppEnvironment class.
 *
 * WARNING:  Deleting or editing this function may prevent future upgrades
 * being correctly applied.
 */
export function servicePathFactory(
  servicePath: string
): (env: EmployeeWebAppEnvironment) => string {
  return (env: EmployeeWebAppEnvironment) =>
    `${env.webSdkConfig.apiRoot}${servicePath}`;
}

/**
 * Service paths for the individual data modules.
 *
 * The values provided here are mapped to FactoryProviders in the AppDataModules
 * module below, using the servicePathFactory function above to create the
 * factory for each injection token.
 *
 * If for some reason you do not want to use the servicePathFactory to provide
 * the base path for a service, remove it from this array and add a separate
 * provider for it to the AppDataModules module, below.
 *
 * The entries in this array may be edited, added or removed as required, but
 * deleting or renaming the array itself may prevent future upgrades being
 * correctly applied.
 */
const dataModulePaths: [InjectionToken<string>, string][] = [[ACCESS_CONTROL_BASE_PATH, '/access-control'],[APPROVAL_BASE_PATH, '/approval-service'],[ARRANGEMENT_MANAGER_BASE_PATH, '/arrangement-manager'],[AUDIT_BASE_PATH, '/audit-service'],[DEVICE_BASE_PATH, '/device-management-service'],[LIMIT_BASE_PATH, '/limit'],[NOTIFICATIONS_BASE_PATH, '/notifications-service'],[USER_BASE_PATH, '/user-manager'],[EMPLOYEE_BASE_PATH, '/employee'],[MESSAGES_BASE_PATH, '/messages-service'],[PAYMENT_ORDER_BASE_PATH, '/payment-order-service'],[USER_PROFILE_BASE_PATH, '/user-profile-manager'],[COMMENTS_BASE_PATH, '/comments'],[IMPERSONATION_BASE_PATH, '/orchestration']];

/**
 * This module is added to the `imports` array of the AppModule in app.module.ts.
 *
 * Service configuration may be customised by modifying the relevant
 * `*_BASE_PATH` provider token value or by adding a `ModuleWithProvider`
 * as an import to this module by calling `.forRoot` on an API module:
 *
 * ```
 * @NgModule({
 *   providers: [...],
 *   imports: [
 *     AuditApiModule.forRoot(() => new AuditConfiguration({ ... }))
 *   ]
 * })
 * export class AppDataModules {}
 * ```
 */
@NgModule({
  providers: [
    ...dataModulePaths.map(([token, servicePath]) => ({
      provide: token,
      useFactory: servicePathFactory(servicePath),
      deps: [EmployeeWebAppEnvironment],
    })),
  ],
})
export class AppDataModules {}
