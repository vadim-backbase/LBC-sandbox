import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BackbaseCoreModule } from '@backbase/foundation-ang/core';
import { UniversalOmniPaymentWrapperWidgetAngComponent } from './universal-omni-payment-wrapper-widget-ang.component';
import { PayordOmniPaymentWidgetAngModule } from '@backbase/business-ang/payment-order';
import { US_DOMESTIC_WIRE } from '@backbase/business-ang/payment-configs';

@NgModule({
  declarations: [UniversalOmniPaymentWrapperWidgetAngComponent],
  imports: [
    CommonModule,
    BackbaseCoreModule.withConfig({
      classMap: { UniversalOmniPaymentWrapperWidgetAngComponent },
    }),
    PayordOmniPaymentWidgetAngModule.withConfig({
      paymentType: US_DOMESTIC_WIRE,
      businessFunction: 'US Domestic Wire',
    }),
  ],
})
export class UniversalOmniPaymentWrapperWidgetAngModule {}
