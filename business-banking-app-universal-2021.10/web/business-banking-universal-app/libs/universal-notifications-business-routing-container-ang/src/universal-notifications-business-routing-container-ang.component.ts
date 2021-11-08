import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ItemModelTree } from '@backbase/foundation-ang/core';
import { Observable } from 'rxjs';

@Component({
  selector: 'bb-universal-notifications-business-routing-container-ang',
  templateUrl: './universal-notifications-business-routing-container-ang.component.html',
})
export class UniversalNotificationsBusinessRoutingContainerAngComponent {
  @Output() viewConversation = new EventEmitter<string>();
  @Output() selectedAccount = new EventEmitter<string>();
  @Output() viewContact = new EventEmitter<string>();
  @Output() viewContactsToApprove = new EventEmitter<void>();
  @Output() viewPayment = new EventEmitter<string>();
  @Output() viewPaymentsToApprove = new EventEmitter<void>();
  @Output() viewPaymentsList = new EventEmitter<void>();
  @Output() viewTransaction = new EventEmitter<string>();

  @Input() set notificationRouting(routing: any) {
    const payloadId =
      (routing['where-to'] === 'transaction-view' ? routing?.data?.arrangementId : routing?.data?.id) || '';

    switch (routing['where-to']) {
      case 'conversation-view':
        this.viewConversation.emit(payloadId);
        break;
      case 'transaction-view':
        this.viewTransaction.emit(payloadId);
        break;
      case 'arrangement-view':
        this.selectedAccount.emit(payloadId);
        break;
      case 'party-view':
        this.viewContact.emit(payloadId);
        break;
      case 'party-approve-view':
        this.viewContactsToApprove.emit();
        break;
      case 'payment-view':
        this.viewPayment.emit(payloadId);
        break;
      case 'payment-approve-view':
        this.viewPaymentsToApprove.emit();
        break;
      case 'payments-list-view':
        this.viewPaymentsList.emit();
        break;
    }
  }

  children: Observable<Array<ItemModelTree>> = this.model.children;

  constructor(private readonly model: ItemModelTree) {}
}
