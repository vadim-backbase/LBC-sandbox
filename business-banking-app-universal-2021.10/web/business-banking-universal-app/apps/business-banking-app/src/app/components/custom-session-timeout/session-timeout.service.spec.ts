import { CustomSessionTimeoutService } from './session-timeout.service';
import { Countdown } from './session-timeout.interface';
import { NgZone } from '@angular/core';

describe('CustomSessionTimeoutService', () => {
  let service: CustomSessionTimeoutService;
  const mockLogoutService = jasmine.createSpyObj('LogoutService', ['logout', 'goToLoginPage']);
  const mockSessionService = jasmine.createSpyObj('SessionService', ['registerCountdown', 'refresh']);
  const mockNgZone: jasmine.SpyObj<NgZone> = jasmine.createSpyObj('NgZone', ['runOutsideAngular']);
  mockNgZone.runOutsideAngular.and.callFake((fn: Function): any => fn());
  let mockResponse: Promise<void>;

  beforeAll(() => {
    service = new CustomSessionTimeoutService(mockLogoutService, mockSessionService, mockNgZone);
  });

  describe('logout', () => {
    beforeAll(() => {
      mockResponse = Promise.resolve();
      mockLogoutService.logout.and.returnValue(mockResponse);
    });
    it('should call LogoutService to logout and return an observable', () => {
      expect(service.logout()).toEqual(mockResponse);
      expect(mockLogoutService.logout).toHaveBeenCalled();
    });
  });

  describe('goToLoginPage', () => {
    it('should call LogoutService to goToLoginPage', () => {
      service.goToLoginPage();
      expect(mockLogoutService.goToLoginPage).toHaveBeenCalled();
    });
  });

  describe('refresh', () => {
    beforeAll(() => {
      mockResponse = Promise.resolve();
      mockSessionService.refresh.and.returnValue(mockResponse);
    });
    it('should call SessionService to refresh and return an observable', () => {
      expect(service.refresh()).toEqual(mockResponse);
      expect(mockSessionService.refresh).toHaveBeenCalled();
    });
  });

  describe('registerCountdown', () => {
    it('should call SessionService to registerCountdown and return an observable', () => {
      const mockCountdown: Countdown = {
        duration: 1,
        start: () => {},
        end: () => {},
        reset: () => {},
        tick: () => {},
      };
      service.registerCountdown(mockCountdown);
      expect(mockSessionService.registerCountdown).toHaveBeenCalledWith(mockCountdown);
    });
  });
});
