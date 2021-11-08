import { NgModule } from '@angular/core';
import {
  MessagesClientInboxJourneyModule,
  MessagesClientInboxJourneyConfigurationToken,
  MessagesClientInboxJourneyConfiguration,
} from '@backbase/messages-client-inbox-journey-ang';

@NgModule({
  imports: [MessagesClientInboxJourneyModule.forRoot()],
  providers: [
    {
      provide: MessagesClientInboxJourneyConfigurationToken,
      useValue: <MessagesClientInboxJourneyConfiguration>{
        headingTitle: 'Inbox',
        headingType: 'h1',
        headingClasses: '',
        headingWrapperClasses: 'bb-heading-widget--de-elevated',
        tabsWrapperClasses: 'd-block container--drag-up mx-4 pt-5 bb-tab--inverse',
        buttonText: 'Compose',
        buttonClasses: '',
        createMessageOpenEventName: 'bb.event.messages.create.message.open',
        createMessageClosedEventName: 'bb.event.messages.create.message.close',
        itemsPerPage: 10,
        customerServiceTitle: 'Customer Service',
        hideAssignedToColumn: false,
        maxAttachmentSize: '10',
        replyMessageMaxLength: '300',
        maxSubjectLength: 100,
        maxMessageLength: 300,
        modalHeader: 'New message',
        hideComposeButton: true,
      },
    },
  ],
})
export class MessagesClientInboxJourneyBundleModule {}
