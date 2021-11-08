import { NgModule } from '@angular/core';
import { ContactJourneyModule } from '@backbase/contact-journey-ang';
import { ContactConfigProviders } from '../app/config.providers';

@NgModule({
  imports: [ContactJourneyModule.forRoot()],
  providers: [ContactConfigProviders],
})
export class ContactJourneyBundleModule {}
