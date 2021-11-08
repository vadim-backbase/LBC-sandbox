import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UniversalOmniPaymentWrapperWidgetAngComponent } from './universal-omni-payment-wrapper-widget-ang.component';

describe('UniversalOmniPaymentWrapperWidgetAngComponent', () => {
  let component: UniversalOmniPaymentWrapperWidgetAngComponent;
  let fixture: ComponentFixture<UniversalOmniPaymentWrapperWidgetAngComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UniversalOmniPaymentWrapperWidgetAngComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UniversalOmniPaymentWrapperWidgetAngComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
