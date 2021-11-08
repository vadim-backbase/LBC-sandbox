/*
 *
 * The content of this file can be edited freely, but to maintain upgradability
 * this file should not be renamed and should always export the class
 * `BankAdminWorkspaceNavigationComponent`.
 *
 */

import { Component } from '@angular/core';
import { ConfigurationService } from '@backbase/employee-web-app-shared-util-core';
import { combineLatest, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-bank-admin-workspace-navigation',
  templateUrl: './bank-admin-workspace-navigation.component.html',
})
export class BankAdminWorkspaceNavigationComponent {
  readonly identityAdminHref$: Observable<string>;
  readonly paymentAdminHref$: Observable<string>;
  readonly showOtherToolsHeader$: Observable<boolean>;

  constructor(config: ConfigurationService) {
    this.identityAdminHref$ = config.getProperty('identityAdminHref', '');
    this.paymentAdminHref$ = config.getProperty('paymentAdminHref', '');

    const isAnyValueSet = (values) => !!values.find((value) => !!value);

    this.showOtherToolsHeader$ = combineLatest([
      this.identityAdminHref$,
      this.paymentAdminHref$,
    ]).pipe(map(isAnyValueSet));
  }
}
