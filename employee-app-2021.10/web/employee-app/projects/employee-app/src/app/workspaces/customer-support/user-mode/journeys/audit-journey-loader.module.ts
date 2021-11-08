import { NgModule } from '@angular/core';
import {
  AuditJourneyModule,
  AuditJourneyConfiguration,
  AuditJourneyConfigurationToken
} from '@backbase/audit-journey-ang';

const config: AuditJourneyConfiguration = {
  userIdRouteParam: 'userId',
  displayHeading: false
}

@NgModule({
  imports: [
    AuditJourneyModule.forRoot()
  ],
  providers: [
    {
      provide: AuditJourneyConfigurationToken,
      useValue: config
    }
  ]
})
export class AuditJourneyLoaderModule {}
