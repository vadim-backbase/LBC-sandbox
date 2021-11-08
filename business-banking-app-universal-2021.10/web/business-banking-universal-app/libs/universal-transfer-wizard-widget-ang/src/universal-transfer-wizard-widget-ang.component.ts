import { Component, Input, OnInit } from '@angular/core';
import { PayordPaymentsWizardWidgetComponent, PaymentWizardConfig } from '@backbase/business-ang/payment-order';
import {
  US_DOMESTIC_WIRE,
  US_ACH_CREDIT,
  INTERNATIONAL_TRANSFER,
  INTERNAL_TRANSFER_BUSINESS,
} from '@backbase/business-ang/payment-configs';
import { CopyRoutes } from '@backbase/foundation-ang/core';
import { PaymentTemplate } from '@backbase/data-ang/payment-template';

enum AccessContextScope {
  USER = 'USER',
  SA = 'SA',
  LE = 'LE',
}

@Component({
  selector: 'bb-universal-transfer-wizard-widget-ang',
  templateUrl: './universal-transfer-wizard-widget-ang.component.html',
})
@CopyRoutes(PayordPaymentsWizardWidgetComponent)
export class UniversalTransferWizardWidgetAngComponent implements OnInit {
  /**
   * The default country, this is used for sanctioned currencies and sanctioned countries request.
   */
   @Input() defaultCountry: string | undefined;
   /**
    * The access context scope for crreating contacts
    */
   @Input() accessContextScope: AccessContextScope | undefined;
   /**
    * Flag to enable payment approval
    */
   @Input() enableApprovals: boolean | undefined;
   /**
    * Flag to enable saving payment as template
    */
   @Input() enableSavePaymentAsTemplate: boolean | undefined;
   /**
    * Flag to enable payment template selector
    */
   @Input() enablePaymentTemplateSelector: boolean | undefined;
   /**
    * Selected Payment template
    */
   @Input() selectedPaymentTemplate!: PaymentTemplate;
   /**
    * Flag to display error title
    */
   @Input() isErrorTitleDisplayed: boolean | undefined;
   /**
   * Flag to denote if the check for closed permission will be performed
   */
  @Input() checkClosedPaymentsAccess = false;
  /**
   * Arrangements page size
   * Default value is `50`
   */
   @Input() pageSize = 50;
 
   config!: PaymentWizardConfig;
 
   ngOnInit() {
     this.config = this.getConfig(
       this.defaultCountry,
       this.enableApprovals,
       this.accessContextScope,
       this.enablePaymentTemplateSelector,
       this.enableSavePaymentAsTemplate,
       this.isErrorTitleDisplayed,
       this.checkClosedPaymentsAccess,
       this.pageSize,
     );
   }
 
   getConfig(
     defaultCountry = 'US',
     enableApprovals = true,
     accessContextScope = AccessContextScope.SA,
     enablePaymentTemplateSelector = true,
     enableSavePaymentAsTemplate = true,
     isErrorTitleDisplayed = true,
     checkClosedPaymentsAccess = false,
     pageSize = 50,
   ) {
     return {
       businessFunctions: [
         'US Domestic Wire',
         'US Domestic Wire - Intracompany',
         'US Foreign Wire',
         'ACH Credit Transfer',
         'A2A Transfer',
       ],
       paymentconfigs: [
         US_DOMESTIC_WIRE,
         INTERNATIONAL_TRANSFER,
         US_ACH_CREDIT,
         INTERNAL_TRANSFER_BUSINESS,
       ],
       defaultCountry,
       enableApprovals,
       accessContextScope,
       enablePaymentTemplateSelector,
       enableSavePaymentAsTemplate,
       isErrorTitleDisplayed,
       checkClosedPaymentsAccess,
       pageSize,
     };
   }
 }
 