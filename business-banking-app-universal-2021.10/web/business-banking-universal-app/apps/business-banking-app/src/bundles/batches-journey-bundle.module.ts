import { NgModule } from '@angular/core';
import { BatchesJourneyModule, BatchesJourneyConfiguration, BatchType, ConfidentialConfig, BatchesJourneyConfigurationToken } from '@backbase/batch-journey-ang';
import { BatchManagerWidgetModule } from '@backbase/business-ang/batch';

@NgModule({
  imports: [BatchManagerWidgetModule, BatchesJourneyModule.forRoot()],
  providers: [{
    provide: BatchesJourneyConfigurationToken, useValue: {
      batchTypes: <BatchType[]>[{ "type": "BB_SEPACT_CSV", "sizeLimit": "100", "format": ".csv", "iconClasses": "bb-sepa", "confidentialTypes": ["confidential"] }],
      appRegion: 'universal',
    } as Partial<BatchesJourneyConfiguration>
  }],
})
export class BatchesJourneyBundleModule { }
