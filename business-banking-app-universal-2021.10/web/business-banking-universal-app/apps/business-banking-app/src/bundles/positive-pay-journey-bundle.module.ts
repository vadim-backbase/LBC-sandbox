import { NgModule } from '@angular/core';
import { EntitlementsModule } from '@backbase/foundation-ang/entitlements';
import { PositivePayJourneyModule } from '@backbase/positive-pay-journey-ang';

@NgModule({
  imports: [EntitlementsModule, PositivePayJourneyModule.forRoot()],
})
export class PositivePayJourneyBundleModule {}
