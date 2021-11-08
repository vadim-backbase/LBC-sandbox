import { Component, Input, OnInit } from '@angular/core';
import {
  PayordOmniPaymentWidgetAngComponent,
  OmniPaymentWidgetOptions,
  PaymentTypeConfig,
} from '@backbase/business-ang/payment-order';
import { CopyRoutes } from '@backbase/foundation-ang/core';
import {
  CHAPS,
  SEPA,
  US_ACH_CREDIT,
  US_DOMESTIC_WIRE,
  UK_FASTER_PAYMENT,
  INTRABANK_TRANSFER,
  INTERNATIONAL_TRANSFER,
  INTERNAL_TRANSFER_BUSINESS,
} from '@backbase/business-ang/payment-configs';
import { PaymentTemplate } from '@backbase/data-ang/payment-template';
import { BehaviorSubject } from 'rxjs';
import { RoutableModalService } from '@backbase/payment-orders-ang';

enum TemplateModeType {
  CREATE = 'CREATE',
  EDIT = 'EDIT',
}

@Component({
  selector: 'bb-universal-create-payment-template-widget-ang',
  templateUrl: './universal-create-payment-template-widget-ang.component.html',
})
@CopyRoutes(PayordOmniPaymentWidgetAngComponent)
export class UniversalCreatePaymentTemplateWidgetAngComponent implements OnInit {
  /**
   * Event emitted when payment or template is created
   */
   @Input() successEventName = '';
   /**
    * Flag to denote if the widget is rendered inside a modal
    */
   @Input() isModalView = true;
   /**
    * Name of the modal window. (Url friendly without spaces)
    */
   @Input() modalViewName = '';
   /**
    * Comma separated payment type names for creating payment template.
    */
   @Input() templateTypes = '';
   /**
    * The edited payment template
    */
   @Input()
   set editedPaymentTemplate(template: PaymentTemplate | undefined) {
     if (template?.details) {
       this.templateModeType$.next(TemplateModeType.EDIT);
       this.selectedPaymentTemplate$.next(template);
       this.routableModalService.openModal(<string>this.modalViewName);
     }
   }
   /**
   * Flag to denote if the check for closed permission will be performed
   */
  @Input() checkClosedPaymentsAccess = false;
  /**
   * Arrangements page size
   * Default value is `50`
   */
   @Input() pageSize = 50;
 
   isModalOpen$ = new BehaviorSubject<boolean>(false);
   selectedPaymentType$ = new BehaviorSubject<string>('');
   selectedPaymentTemplate$ = new BehaviorSubject<PaymentTemplate | undefined>(undefined);
   templateModeType$ = new BehaviorSubject<TemplateModeType | undefined>(undefined);
   templateItems!: Array<{value: string, label: string}>;
 
   constructor(private readonly routableModalService: RoutableModalService) {}
 
   ngOnInit() {
     this.templateItems = this.templateTypes.split(',').map(value => ({
      value,
      label: this.getPaymentTemplateLabel(value),
    }));
    this.selectedPaymentType$.next(this.templateItems[0]?.value);
   }
 
   getWidgetOptions(): OmniPaymentWidgetOptions | any {
     return {
       defaultScheme: 'BBAN',
       defaultCountry: 'US',
       isTemplateMode: true,
       isModalView: this.isModalView,
       successEventName: this.successEventName,
       templateModeType: this.templateModeType$.value,
       checkClosedPaymentsAccess: this.checkClosedPaymentsAccess,
       pageSize: this.pageSize,
     };
   }
 
   onSelectPaymentType(paymentType: string) {
     this.selectedPaymentType$.next(paymentType);
     this.resetTemplateDetails();
   }
 
   resetTemplateDetails() {
     this.selectedPaymentTemplate$.next(undefined);
     this.templateModeType$.next(undefined);
   }
 
   onModalToggle(isModalOpen: boolean) {
     this.isModalOpen$.next(isModalOpen);
 
     if (!isModalOpen) {
       this.selectedPaymentType$.next(this.templateItems[0]?.value);
       this.resetTemplateDetails();
     }
   }

   getPaymentConfig(paymentType: string): PaymentTypeConfig | undefined {
    switch (paymentType) {
      case 'SEPA_CREDIT_TRANSFER':
      case 'SEPA':
        return SEPA;
      case 'ACH_CREDIT':
      case 'US_ACH_CREDIT':
        return US_ACH_CREDIT;
      case 'FASTER_PAYMENT':
        return UK_FASTER_PAYMENT;
      case 'CHAPS':
        return CHAPS;
      case 'US_DOMESTIC_WIRE':
        return US_DOMESTIC_WIRE;
      case 'INTERNAL_TRANSFER':
      case 'INTERNAL_TRANSFER_BUSINESS':
        return INTERNAL_TRANSFER_BUSINESS;
      case 'INTRABANK_TRANSFER':
        return INTRABANK_TRANSFER;
      case 'INTERNATIONAL_TRANSFER':
        return INTERNATIONAL_TRANSFER;
      default:
        return undefined;
    }
  }
 
  getPaymentTemplateLabel(paymentType: string): string {
     switch (paymentType) {
       case 'SEPA_CREDIT_TRANSFER':
       case 'SEPA':
         return 'SEPA';
       case 'ACH_CREDIT':
       case 'US_ACH_CREDIT':
         return 'ACH';
       case 'US_DOMESTIC_WIRE':
         return 'Domestic Wire';
       case 'INTERNAL_TRANSFER':
       case 'INTERNAL_TRANSFER_BUSINESS':
         return 'Internal Transfer';
       case 'INTERNATIONAL_TRANSFER':
         return 'International (SWIFT)';
       default:
         return '';
     }
   }
 
   getBusinessFunction(paymentType: string): string {
     switch (paymentType) {
       case 'SEPA_CREDIT_TRANSFER':
       case 'SEPA':
         return 'SEPA CT';
       case 'ACH_CREDIT':
       case 'US_ACH_CREDIT':
         return 'ACH Credit Transfer';
       case 'CHAPS':
         return 'UK CHAPS';
       case 'FASTER_PAYMENT':
         return 'UK Faster Payments';
       case 'US_DOMESTIC_WIRE':
         return 'US Domestic Wire';
       case 'INTERNAL_TRANSFER':
       case 'INTERNAL_TRANSFER_BUSINESS':
         return 'A2A Transfer';
       case 'INTRABANK_TRANSFER':
         return 'A2A Transfer';
       case 'INTERNATIONAL_TRANSFER':
         return 'US Foreign Wire';
     }
     return '';
   }
 }
 