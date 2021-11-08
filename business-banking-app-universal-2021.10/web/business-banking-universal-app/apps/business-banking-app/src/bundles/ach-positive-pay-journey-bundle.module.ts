import { NgModule } from '@angular/core';
import { EntitlementsModule } from '@backbase/foundation-ang/entitlements';
import { AchPositivePayJourneyModule } from '@backbase/ach-positive-pay-journey-ang';

@NgModule({
  imports: [EntitlementsModule, AchPositivePayJourneyModule.forRoot()],
})
export class AchPositivePayJourneyBundleModule {}
