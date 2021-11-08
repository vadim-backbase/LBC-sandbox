import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UniversalCreatePaymentTemplateWidgetAngComponent } from './universal-create-payment-template-widget-ang.component';

describe('UniversalCreatePaymentTemplateWidgetAngComponent', () => {
  let component: UniversalCreatePaymentTemplateWidgetAngComponent;
  let fixture: ComponentFixture<UniversalCreatePaymentTemplateWidgetAngComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UniversalCreatePaymentTemplateWidgetAngComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UniversalCreatePaymentTemplateWidgetAngComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
