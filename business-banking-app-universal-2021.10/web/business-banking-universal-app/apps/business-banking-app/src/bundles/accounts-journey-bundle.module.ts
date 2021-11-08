import { NgModule } from '@angular/core';
import { EntitlementsModule } from '@backbase/foundation-ang/entitlements';
import { AccountsJourneyModule } from '@backbase/accounts-journey-ang';
import { AccountsConfigProvider } from '../app/config.providers';

@NgModule({
  imports: [EntitlementsModule, AccountsJourneyModule.forRoot()],
  providers: [AccountsConfigProvider],
})
export class AccountsJourneyBundleModule {}
