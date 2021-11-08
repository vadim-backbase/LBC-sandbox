import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BackbaseCoreModule } from '@backbase/foundation-ang/core';
import { UniversalCreatePaymentTemplateWidgetAngComponent } from './universal-create-payment-template-widget-ang.component';
import { PayordOmniPaymentWidgetAngModule } from '@backbase/business-ang/payment-order';

@NgModule({
  declarations: [UniversalCreatePaymentTemplateWidgetAngComponent],
  imports: [
    CommonModule,
    BackbaseCoreModule.withConfig({
      classMap: { UniversalCreatePaymentTemplateWidgetAngComponent },
    }),
    PayordOmniPaymentWidgetAngModule,
  ],
})
export class UniversalCreatePaymentTemplateWidgetAngModule {}
