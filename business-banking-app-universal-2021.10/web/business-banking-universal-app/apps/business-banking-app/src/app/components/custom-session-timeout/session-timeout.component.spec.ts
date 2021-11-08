import { CustomSessionTimeoutComponent } from './session-timeout.component';
import { ChangeDetectorRef, NgZone } from '@angular/core';
import { CustomSessionTimeoutService } from './session-timeout.service';
import { Countdown, SessionTimeoutTimeFormat } from './session-timeout.interface';

describe('CustomSessionTimeoutComponent', () => {
  let component: CustomSessionTimeoutComponent;
  const mockSessionTimeoutService: jasmine.SpyObj<CustomSessionTimeoutService> = jasmine.createSpyObj(
    'CustomSessionTimeoutService',
    ['logout', 'goToLoginPage', 'registerCountdown', 'refresh'],
  );
  const mockChangeDetectorRef: jasmine.SpyObj<ChangeDetectorRef> = jasmine.createSpyObj('ChangeDetectorRef', [
    'markForCheck',
  ]);
  const mockNgZone: jasmine.SpyObj<NgZone> = jasmine.createSpyObj('NgZone', ['run']);
  mockNgZone.run.and.callFake((fn: Function): any => fn());
  let mockResponse: Promise<void>;

  const runTick = (countdown: Countdown, ttl: number) => {
    if (countdown.tick) {
      countdown.tick(ttl);
    }
  };

  beforeEach(() => {
    component = new CustomSessionTimeoutComponent(mockSessionTimeoutService, mockChangeDetectorRef, mockNgZone);
  });

  describe('on initialisation', () => {
    it('should default isOpen to false', () => {
      expect(component.isOpen).toBeFalsy();
    });
    it('should default inactivityModalTime to 300', () => {
      expect(component.inactivityModalTime).toEqual(300);
    });
    it('should set modalOptions', () => {
      expect(component.modalOptions).toEqual({
        keyboard: false,
        backdrop: 'static',
        windowClass: 'session-timeout__modal',
        backdropClass: 'session-timeout__modal',
      });
    });
    it('should set timeFormat to undefined', () => {
      expect(component.timeFormat).toBeUndefined();
    });
    it('should set formats', () => {
      expect(component.formats).toEqual(SessionTimeoutTimeFormat);
    });
    it('should set minutesRemaining to undefined', () => {
      expect(component.minutesRemaining).toBeUndefined();
    });
    it('should set secondsRemaining', () => {
      expect(component.secondsRemaining).toBeUndefined();
    });
  });

  describe('ngOnInit', () => {
    beforeEach(() => {
      component.isOpen = true;
      component.ngOnInit();
    });
    it('should set isOpen to false', () => {
      expect(component.isOpen).toBeFalsy();
    });
    it('should call sessionTimeoutService to registerCountdown', () => {
      expect(mockSessionTimeoutService.registerCountdown as any).toHaveBeenCalledWith({
        duration: component.inactivityModalTime,
        start: jasmine.any(Function),
        end: jasmine.any(Function),
        reset: jasmine.any(Function),
        tick: jasmine.any(Function),
      });
    });
  });

  describe('after ngOnInit', () => {
    let countdown: Countdown;
    beforeEach(() => {
      mockSessionTimeoutService.registerCountdown.and.callFake((countdownObject: Countdown) => {
        countdown = countdownObject;
      });
      mockResponse = Promise.resolve();
      mockSessionTimeoutService.logout.and.returnValue(mockResponse);
      component.ngOnInit();
    });
    it('should open modal on countdown start', () => {
      component.isOpen = false;
      countdown.start();
      expect(component.isOpen).toBeTruthy();
    });
    it('should close modal, reset formatting, and logout on countdown end', () => {
      component.isOpen = true;
      countdown.end();
      expect(component.isOpen).toBeFalsy();
      expect(component.timeFormat).toBeUndefined();
      expect(component.minutesRemaining).toBeUndefined();
      expect(component.secondsRemaining).toBeUndefined();
      expect(mockSessionTimeoutService.logout).toHaveBeenCalled();
    });
    it('should close modal and reset Formatting on countdown reset', () => {
      component.isOpen = true;
      countdown.reset();
      expect(component.isOpen).toBeFalsy();
      expect(component.timeFormat).toBeUndefined();
      expect(component.minutesRemaining).toBeUndefined();
      expect(component.secondsRemaining).toBeUndefined();
    });

    describe('when tick is called', () => {
      describe('with less than 60', () => {
        beforeEach(() => runTick(countdown, 59));
        it('should set time format to seconds only', () => {
          expect(component.timeFormat).toEqual(SessionTimeoutTimeFormat.Seconds);
        });
        it('should set minutes remaining to 0', () => {
          expect(component.minutesRemaining).toEqual(0);
        });
        it('should set seconds remaining to the input value', () => {
          expect(component.secondsRemaining).toEqual(59);
        });
        it('should call cd to markForCheck', () => {
          expect(mockChangeDetectorRef.markForCheck).toHaveBeenCalled();
        });
      });
      describe('with a value divisible by 60 with no remainder', () => {
        beforeEach(() => runTick(countdown, 60));
        it('should set time format to minutes only', () => {
          expect(component.timeFormat).toEqual(SessionTimeoutTimeFormat.Minutes);
        });
        it('should set minutes remaining to the value divided by 60', () => {
          expect(component.minutesRemaining).toEqual(1);
        });
        it('should set seconds remaining to 0', () => {
          expect(component.secondsRemaining).toEqual(0);
        });
        it('should call cd to markForCheck', () => {
          expect(mockChangeDetectorRef.markForCheck).toHaveBeenCalled();
        });
      });
      describe('with a value greater than 60 but divisible by 60 with a remainder', () => {
        beforeEach(() => runTick(countdown, 61));
        it('should set time format to full', () => {
          expect(component.timeFormat).toEqual(SessionTimeoutTimeFormat.Full);
        });
        it('should set minutes remaining to the value divided by 60 excluding the remainder', () => {
          expect(component.minutesRemaining).toEqual(1);
        });
        it('should set seconds remaining to the remainder', () => {
          expect(component.secondsRemaining).toEqual(1);
        });
        it('should call cd to markForCheck', () => {
          expect(mockChangeDetectorRef.markForCheck).toHaveBeenCalled();
        });
      });
    });
  });

  describe('logout', () => {
    beforeEach(() => {
      mockResponse = Promise.resolve();
      mockSessionTimeoutService.logout.and.returnValue(mockResponse);
    });
    it('should call sessionTimeoutService to logout', () => {
      component.logout();
      expect(mockSessionTimeoutService.logout).toHaveBeenCalled();
    });
    it('should call sessionTimeoutService to goToLoginPage', async (done) => {
      component.logout().then(() => {
        expect(mockSessionTimeoutService.goToLoginPage).toHaveBeenCalled();
        done();
      }, fail);
    });
  });

  describe('continueSession', () => {
    beforeEach(() => {
      component.continueSession();
    });
    it('should call sessionTimeoutService to refresh', () => {
      expect(mockSessionTimeoutService.refresh).toHaveBeenCalled();
    });
  });
});
