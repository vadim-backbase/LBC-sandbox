import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BackbaseCoreModule } from '@backbase/foundation-ang/core';
import { UniversalTransferWizardWidgetAngComponent } from './universal-transfer-wizard-widget-ang.component';
import { PayordPaymentsWizardWidgetModule } from '@backbase/business-ang/payment-order';

@NgModule({
  declarations: [UniversalTransferWizardWidgetAngComponent],
  imports: [
    CommonModule,
    PayordPaymentsWizardWidgetModule,
    BackbaseCoreModule.withConfig({
      classMap: { UniversalTransferWizardWidgetAngComponent },
    }),
  ],
})
export class UniversalTransferWizardWidgetAngModule {}
