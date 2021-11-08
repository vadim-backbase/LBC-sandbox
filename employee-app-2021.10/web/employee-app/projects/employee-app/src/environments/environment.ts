// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

import {
  EmployeeWebAppEnvironmentProperties, identityAuthInitializerProvider, sessionCountdownInitializer,

} from '@backbase/employee-web-app-shared-util-core';
import { mockProviders } from './mockProviders';
import { environmentBase } from './environment.base';
import { AuthService } from '@backbase/foundation-ang/auth';
import { DevAuthService } from '@backbase/employee-web-app-root-util-dev-tools';

const apiRoot = '/api';

export const environment: EmployeeWebAppEnvironmentProperties = {
  ...environmentBase,
  production: false,
  useHashNavigation: false,
  providers: [
    ...environmentBase.providers,
    identityAuthInitializerProvider,
    sessionCountdownInitializer
  ],
  webSdkConfig: {
    auth: {
      authUrl: 'http://localhost:8180/auth',
      clientId: 'bb-web-client',
      realm: 'LaurentianBank',
      scope: 'openid',
      maxInactivityDuration: 300,
      countdownDuration: 60,
    },
    apiRoot,
    staticResourcesRoot: `${apiRoot}/contentservices/api/contentstream/resourceRepository/contextRoot/static/items`,
    locales: ['en'],
  },

};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.
