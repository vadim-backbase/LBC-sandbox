import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalModule, ButtonModule, HeaderModule } from '@backbase/ui-ang';
import { CustomSessionTimeoutComponent } from './session-timeout.component';

const uiModules = [ModalModule, ButtonModule, HeaderModule];

@NgModule({
  imports: [CommonModule, ...uiModules],
  declarations: [CustomSessionTimeoutComponent],
  exports: [CustomSessionTimeoutComponent, ...uiModules],
})
export class CustomSessionTimeoutModule {}
