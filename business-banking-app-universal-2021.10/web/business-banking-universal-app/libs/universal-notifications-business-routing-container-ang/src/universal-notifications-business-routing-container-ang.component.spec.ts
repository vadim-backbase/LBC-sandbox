import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UniversalNotificationsBusinessRoutingContainerAngComponent } from './universal-notifications-business-routing-container-ang.component';

describe('UniversalNotificationsBusinessRoutingContainerAngComponent', () => {
  let component: UniversalNotificationsBusinessRoutingContainerAngComponent;
  let fixture: ComponentFixture<UniversalNotificationsBusinessRoutingContainerAngComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UniversalNotificationsBusinessRoutingContainerAngComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UniversalNotificationsBusinessRoutingContainerAngComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
