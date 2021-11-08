import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UniversalTransferWizardWidgetAngComponent } from './universal-transfer-wizard-widget-ang.component';

describe('UniversalTransferWizardWidgetAngComponent', () => {
  let component: UniversalTransferWizardWidgetAngComponent;
  let fixture: ComponentFixture<UniversalTransferWizardWidgetAngComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UniversalTransferWizardWidgetAngComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UniversalTransferWizardWidgetAngComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
