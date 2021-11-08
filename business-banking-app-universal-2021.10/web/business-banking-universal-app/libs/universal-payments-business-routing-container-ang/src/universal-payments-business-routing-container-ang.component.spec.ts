import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UniversalPaymentsBusinessRoutingContainerAngComponent } from './universal-payments-business-routing-container-ang.component';

describe('UniversalPaymentsBusinessRoutingContainerAngComponent', () => {
  let component: UniversalPaymentsBusinessRoutingContainerAngComponent;
  let fixture: ComponentFixture<UniversalPaymentsBusinessRoutingContainerAngComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UniversalPaymentsBusinessRoutingContainerAngComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UniversalPaymentsBusinessRoutingContainerAngComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
