import { NgModule } from '@angular/core';
import { LoansJourneyModule } from '@backbase/loans-journey-ang';

@NgModule({
  imports: [LoansJourneyModule.forRoot()],
})
export class LoansJourneyBundleModule {}
