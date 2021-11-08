import { NgModule } from '@angular/core';
import { TradeFinanceJourneyModule } from '@backbase/trade-finance-journey-ang';
import { TradeFinanceConfigProvider } from '../app/config.providers';

@NgModule({
  imports: [TradeFinanceJourneyModule.forRoot()],
  providers: [TradeFinanceConfigProvider],
})
export class TradeFinanceJourneyBundleModule {}
