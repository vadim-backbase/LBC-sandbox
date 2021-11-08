/*
 *
 * The content of this file can be edited freely, but to maintain upgradability
 * this file should not be renamed and should always export an array named
 * `mockProviders`.
 *
 *
 */

import { createMocksInterceptor } from '@backbase/foundation-ang/data-http';
import { Provider } from '@angular/core';
import { mockUserContextCookieInterceptorProvider } from '@backbase/employee-web-app-root-util-dev-tools';
import {
  AccountServiceMocksProvider,
  ServiceAgreementsServiceMocksProvider,
  TransactionsServiceMocksProvider,
} from '@backbase/data-ang/employee';
import {
  DataGroupsHttpServiceMocksProvider,
  FunctionGroupsHttpServiceMocksProvider,
  LegalEntitiesHttpServiceMocksProvider,
  ServiceAgreementHttpServiceMocksProvider,
  ServiceAgreementsHttpServiceMocksProvider,
  UserContextHttpServiceMocksProvider,
  UsersHttpServiceMocksProvider,
} from '@backbase/data-ang/accesscontrol';
import {
  IdentityManagementServiceMocksProvider,
  UserManagementServiceMocksProvider,
} from '@backbase/data-ang/user';
import {
  ApprovalTypeAssignmentsHttpServiceMocksProvider,
  ApprovalTypesHttpServiceMocksProvider,
  ApprovalsHttpServiceMocksProvider,
  PoliciesHttpServiceMocksProvider,
  PolicyAssignmentsHttpServiceMocksProvider,
} from '@backbase/data-ang/approvals';
import {
  ProductSummaryHttpServiceMocksProvider,
  AccountsHttpServiceMocksProvider,
} from '@backbase/data-ang/arrangements';
import { NotificationsHttpServiceMocksProvider } from '@backbase/data-ang/notifications';
import { AuditClientServiceMocksProvider } from '@backbase/data-ang/audit';
import { LimitsHttpServiceMocksProvider } from '@backbase/data-ang/limits';
import { ManageOtherUsersDevicesServiceMocksProvider } from '@backbase/data-ang/device';
import { EmployeeHttpServiceMocksProvider } from '@backbase/data-ang/messages';
import { UserProfileManagementServiceMocksProvider } from '@backbase/data-ang/user-profile-manager';
import { PaymentOrdersHttpServiceMocksProvider } from '@backbase/data-ang/payment-order';
import { CommentsServiceMocksProvider } from '@backbase/data-ang/comments-v2';
import { IdentityImpersonationServiceMocksProvider } from '@backbase/data-ang/impersonation-v1';

/**
 * Mock providers for Backbase services used when running the app in dev mode.
 */
export const mockProviders: Provider[] = [
  mockUserContextCookieInterceptorProvider,
  createMocksInterceptor(),
  AccountServiceMocksProvider,
  ServiceAgreementsServiceMocksProvider,
  TransactionsServiceMocksProvider,
  DataGroupsHttpServiceMocksProvider,
  FunctionGroupsHttpServiceMocksProvider,
  LegalEntitiesHttpServiceMocksProvider,
  ServiceAgreementHttpServiceMocksProvider,
  ServiceAgreementsHttpServiceMocksProvider,
  UserContextHttpServiceMocksProvider,
  UsersHttpServiceMocksProvider,
  IdentityManagementServiceMocksProvider,
  UserManagementServiceMocksProvider,
  ApprovalTypeAssignmentsHttpServiceMocksProvider,
  ApprovalTypesHttpServiceMocksProvider,
  ApprovalsHttpServiceMocksProvider,
  PoliciesHttpServiceMocksProvider,
  PolicyAssignmentsHttpServiceMocksProvider,
  ProductSummaryHttpServiceMocksProvider,
  NotificationsHttpServiceMocksProvider,
  AuditClientServiceMocksProvider,
  LimitsHttpServiceMocksProvider,
  ManageOtherUsersDevicesServiceMocksProvider,
  AccountsHttpServiceMocksProvider,
  EmployeeHttpServiceMocksProvider,
  UserProfileManagementServiceMocksProvider,
  PaymentOrdersHttpServiceMocksProvider,
  CommentsServiceMocksProvider,
  IdentityImpersonationServiceMocksProvider,
];
