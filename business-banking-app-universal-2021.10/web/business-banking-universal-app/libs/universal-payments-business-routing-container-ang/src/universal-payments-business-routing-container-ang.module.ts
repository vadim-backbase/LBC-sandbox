import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BackbaseCoreModule } from '@backbase/foundation-ang/core';
import { UniversalPaymentsBusinessRoutingContainerAngComponent } from './universal-payments-business-routing-container-ang.component';

@NgModule({
  declarations: [UniversalPaymentsBusinessRoutingContainerAngComponent],
  imports: [
    CommonModule,
    BackbaseCoreModule.withConfig({
      classMap: { UniversalPaymentsBusinessRoutingContainerAngComponent },
    }),
  ],
})
export class UniversalPaymentsBusinessRoutingContainerAngModule {}
