import { NgModule } from '@angular/core';
import { AccountStatementBusinessJourneyModule } from '@backbase/account-statement-business-journey-ang';

@NgModule({
  imports: [AccountStatementBusinessJourneyModule.forRoot()],
})
export class AccountStatementBusinessJourneyBundleModule {}
