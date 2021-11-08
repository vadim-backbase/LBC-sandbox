import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import {
  PayordOmniPaymentWidgetAngComponent,
  OmniPaymentWidgetOptions,
  PaymentTypeConfig,
} from '@backbase/business-ang/payment-order';
import { CopyRoutes, ItemModel } from '@backbase/foundation-ang/core';
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
import { IdentifiedPaymentOrder } from '@backbase/payments-common-ang';
import { RoutableModalService, normalizePaymentType, } from '@backbase/payment-orders-ang';
import { BehaviorSubject, of } from 'rxjs';

@Component({
  selector: 'bb-universal-omni-payment-wrapper-widget-ang',
  templateUrl: './universal-omni-payment-wrapper-widget-ang.component.html',
})
@CopyRoutes(PayordOmniPaymentWidgetAngComponent)
export class UniversalOmniPaymentWrapperWidgetAngComponent implements OnInit {
  paymentType$ = this.itemModel.property<string>('paymentType');

  /**
   * Selected Payment template
   */
  @Input() selectedPaymentTemplate!: PaymentTemplate;

  /**
   * Flag to denote if the check for closed permission will be performed
   */
  @Input() checkClosedPaymentsAccess = false;

  /**
   * Is modal view flag
   */
  @Input() isModalView = false;

  /**
   * Is one-off to Recurrent allowed flag
   */
  @Input() isOneOffToRecurrentAllowed = false;

  /**
   * Is Recurrent to one-off allowed flag
   */
  @Input() isRecurrentToOneOffAllowed = false;

  /**
   * Event emitted when payment or template is created
   */
  @Input() successEventName = '';
  /**
   * Edit payment item
   */
  @Input()
  set editPaymentModal(payment: IdentifiedPaymentOrder | undefined) {
    if (payment?.id) {
      this.paymentType$ = of(payment.paymentType);
      this.selectedPayment = payment;
      this.routableModalService.openModal(<string>this.modalViewName);
    }
  }
  /**
   * Payment selected
   */
  @Input() selectedPayment!: IdentifiedPaymentOrder;
  /**
   * Name of the modal view. Required to display the widget in modal
   */
  @Input() modalViewName = '';
  /**
   * Name title of the modal.
   */
  @Input() modalHeading = '';
  /**
   * Flag to display close button
   */
  @Input() modalCloseBtn = false;
  /**
   * Name title icon.
   */
  @Input() modalHeadingIcon = '';
  /**
   * Is Payment object to be edited
   */
  @Input() isEditPayment = false;
  /**
   * Arrangements page size
   * Default value is `50`
   */
  @Input() pageSize = 50;
  /**
   * Event emitter to initiate redirection to scheduled payments
   */
  @Output() redirectAfterSuccess = new EventEmitter();

  isModalOpen$ = new BehaviorSubject<boolean>(false);

  constructor(private readonly itemModel: ItemModel, private readonly routableModalService: RoutableModalService) {}

  ngOnInit() {
    if (this.shouldCloseModal()) {
      this.routableModalService.closeModal();
    }
  }

  getPaymentConfig(paymentType: string): PaymentTypeConfig | undefined {
    switch (normalizePaymentType(paymentType)) {
      case 'SEPA_CREDIT_TRANSFER':
      case 'SEPA_CT':
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

  getBusinessFunction(paymentType: string): string {
    switch (normalizePaymentType(paymentType)) {
      case 'SEPA_CREDIT_TRANSFER':
      case 'SEPA_CT':
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
      case 'INTERNATIONAL_TRANSFER':
        return 'US Foreign Wire';
    }
    return 'SEPA CT';
  }

  getWidgetOptions(): OmniPaymentWidgetOptions | any {
    return {
      defaultScheme: 'BBAN',
      enableApprovals: true,
      enablePaymentTemplateSelector: true,
      enableSavePaymentAsTemplate: true,
      defaultCountry: 'US',
      isErrorTitleDisplayed: true,
      checkClosedPaymentsAccess: this.checkClosedPaymentsAccess,
      isModalView: this.isModalView,
      successEventName: this.successEventName,
      modalViewName: this.modalViewName,
      isEditPaymentModal: this.isEditPayment,
      isOneOffToRecurrentAllowed: this.isOneOffToRecurrentAllowed,
      isRecurrentToOneOffAllowed: this.isRecurrentToOneOffAllowed,
      pageSize: this.pageSize,
    };
  }

  onAfterSuccessRedirect() {
    this.redirectAfterSuccess.emit();
  }

  onModalToggle(isModalOpen: boolean) {
    this.isModalOpen$.next(isModalOpen);
  }

  shouldCloseModal(): boolean {
    return !this.selectedPayment && this.isEditPayment;
  }
}
