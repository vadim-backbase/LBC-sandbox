import { NgModule } from '@angular/core';
import { AuditJourneyModule } from '@backbase/audit-journey-ang';

@NgModule({
  imports: [
    AuditJourneyModule.forRoot()
  ]
})
export class AuditJourneyLoaderModule {}
