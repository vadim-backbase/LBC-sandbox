import {
  EmployeeWebAppEnvironmentProperties,
  identityAuthInitializerProvider,
  sessionCountdownInitializer,
} from '@backbase/employee-web-app-shared-util-core';
import { environmentBase } from './environment.base';

export const environment: EmployeeWebAppEnvironmentProperties = {
  ...environmentBase,
  production: true,
  useHashNavigation: true,
  providers: [
    ...environmentBase.providers,
    identityAuthInitializerProvider,
    sessionCountdownInitializer,
  ],
  assetsConfig: {
    assetsStaticItemName: 'employee-web-app-page',
  },
  webSdkConfig: {
    auth: window?.BB?.config?.cx,
    apiRoot: window?.BB?.config?.apiRoot,
    staticResourcesRoot: window?.BB?.config?.staticResourcesRoot,
    locales: window?.BB?.config?.locales?.split(','),
  },
};
