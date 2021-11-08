import { NgModule } from '@angular/core';
import { CashFlowJourneyModule } from '@backbase/cash-flow-journey-ang';

import { CashFlowConfigProvider } from '../app/config.providers';

@NgModule({
  imports: [CashFlowJourneyModule.forRoot()],
  providers: [CashFlowConfigProvider],
})
export class CashFlowJourneyBundleModule {}
