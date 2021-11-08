import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PaymentTemplate } from '@backbase/data-ang/payment-template';
import { ItemModelTree } from '@backbase/foundation-ang/core';
import { Observable } from 'rxjs';

@Component({
  selector: 'bb-universal-payments-business-routing-container',
  templateUrl: './universal-payments-business-routing-container-ang.component.html',
})
export class UniversalPaymentsBusinessRoutingContainerAngComponent {
  /**
   * Event to navigate to ACH Omni payment widget
   */
   @Output() readonly viewWirePayment = new EventEmitter<PaymentTemplate>();

   /**
    * Event to navigate to WIRE Omni payment widget
    */
   @Output() readonly viewAchPayment = new EventEmitter<PaymentTemplate>();
 
   /**
    * Event to navigate to payments wizard widget
    */
   @Output() readonly viewPaymentWizard = new EventEmitter<PaymentTemplate>();
 
   /**
    * The selected payment template
    */
   @Input() set selectedPaymentTemplate(template: PaymentTemplate) {
     if (template?.details) {
       switch (template?.details?.paymentType) {
         //Cases for omni payment widget should be added here
         default:
           this.viewPaymentWizard.emit(template);
       }
     }
   }
 
   children: Observable<Array<ItemModelTree>> = this.model.children;
 
   constructor(private readonly model: ItemModelTree) {}
 }
 