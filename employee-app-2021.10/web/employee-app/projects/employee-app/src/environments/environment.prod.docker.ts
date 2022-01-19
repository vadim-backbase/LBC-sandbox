import {
  EmployeeWebAppEnvironmentProperties,
  identityAuthInitializerProvider,
  sessionCountdownInitializer,

} from '@backbase/employee-web-app-shared-util-core';
import { environmentBase } from './environment.base';

/*
 * ENVIRONMENT VARIABLE EXPANSION
 *
 * The web-base container image that is used in the Dockerfile will replace any
 * instances of ${SOME_ENV_VAR} with the value of the given environment variable
 * at runtime.
 */

const apiRoot = '${API_ROOT}';



export const environment: EmployeeWebAppEnvironmentProperties = {
  ...environmentBase,
  production: true,
  useHashNavigation: false,
  providers: [
    ...environmentBase.providers,
    identityAuthInitializerProvider,
    sessionCountdownInitializer,
  ],
  webSdkConfig: {
    auth: {
      authUrl: '${AUTH_URL}',
      clientId: '${AUTH_CLIENT_ID}',
      realm: '${AUTH_REALM}',
      scope: '${AUTH_SCOPE}',
      maxInactivityDuration: 300,
      countdownDuration: 60,
    },
    apiRoot,
    staticResourcesRoot: `${apiRoot}/contentservices/api/contentstream/resourceRepository/contextRoot/static/items`,
    locales: ['en'],
  },

};
