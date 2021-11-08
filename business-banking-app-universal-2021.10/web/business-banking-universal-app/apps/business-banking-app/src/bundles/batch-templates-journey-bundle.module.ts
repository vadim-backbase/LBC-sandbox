import { NgModule } from '@angular/core';
import { BatchTemplatesJourneyModule } from '@backbase/batch-templates-journey-ang';
import { BatchTemplatesJourneyConfigProvider } from '../app/config.providers';

@NgModule({
  imports: [BatchTemplatesJourneyModule.forRoot()],
  providers: [BatchTemplatesJourneyConfigProvider],
})
export class BatchTemplatesJourneyBundleModule {}
