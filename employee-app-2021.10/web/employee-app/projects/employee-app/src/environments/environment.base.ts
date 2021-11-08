// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

import {
  EmployeeWebAppEnvironmentProperties,
  LoginPageRedirectionInterceptor,
} from '@backbase/employee-web-app-shared-util-core';
import { HTTP_INTERCEPTORS } from '@angular/common/http';

export const environmentBase: Partial<EmployeeWebAppEnvironmentProperties> = {
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: LoginPageRedirectionInterceptor,
      multi: true,
    },
  ],
};
