import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BackbaseCoreModule } from '@backbase/foundation-ang/core';
import { UniversalNotificationsBusinessRoutingContainerAngComponent } from './universal-notifications-business-routing-container-ang.component';

@NgModule({
  declarations: [UniversalNotificationsBusinessRoutingContainerAngComponent],
  imports: [
    CommonModule,
    BackbaseCoreModule.withConfig({
      classMap: {
        UniversalNotificationsBusinessRoutingContainerAngComponent: UniversalNotificationsBusinessRoutingContainerAngComponent,
      },
    }),
  ],
})
export class UniversalNotificationsBusinessRoutingContainerAngModule {}
