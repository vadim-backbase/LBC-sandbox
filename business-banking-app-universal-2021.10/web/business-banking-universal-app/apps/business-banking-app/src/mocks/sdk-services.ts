import { ExternalServices } from '@backbase/foundation-ang/start';
import { of } from 'rxjs';
import { tree, TreeType } from './navigation-link-widget/tree.data';
import { reauthenticate } from './reauthenticate';

const normalizeNavigationTreeProperties = (navigationTree: any) => {
  const normalizedTree = {
    ...navigationTree,
    properties: Object.keys(navigationTree.preferences).reduce(
      (previousValue, currentValue) => ({
        ...previousValue,
        [currentValue]: navigationTree.preferences[currentValue].value,
      }),
      {},
    ),
  };
  delete normalizedTree.preferences;
  if (normalizedTree.children && Array.isArray(normalizedTree.children) && normalizedTree.children.length) {
    normalizedTree.children = normalizedTree.children.map((child: any) => normalizeNavigationTreeProperties(child));
  }
  return normalizedTree;
};

export const services: ExternalServices = {
  eventBus() {
    const subscriptions = {} as any;
    const events = {
      publish(eventName: string, data: any) {
        if (subscriptions[eventName]) {
          subscriptions[eventName].forEach(function (listener: any) {
            listener(data);
          });
        }
      },
      subscribe(eventName: string, listener: any) {
        subscriptions[eventName] = subscriptions[eventName] || [];
        subscriptions[eventName].push(listener);
      },
      unsubscribe(eventName: string, listener: any) {
        const eventListeners = subscriptions[eventName];
        if (eventListeners) {
          eventListeners.splice(eventListeners.indexOf(listener), 1);
        }
      },
    };
    return events;
  },
/*  pageConfig() {
    return {
      version: '6',
      apiRoot: '/api',
      contextRoot: '/api/portal',
      portalName: 'business-banking-app',
      pageName: 'index',
      designmode: false,
      currentLink: '',
      assetsRoot: '',
    };
  },*/
/*  auth() {
    return {
      login: () => Promise.resolve(),
      logout: () => Promise.resolve(),
      goToLoginPage: () => Promise.resolve(),
      register: () => Promise.resolve(),
      refresh: () => Promise.resolve(),
      timeToLive: () => Promise.resolve(),
      reauthenticate,
      initToken: ['token'],
    };
  },*/
  navigation() {
    return {
      getBreadcrumbs: (uuid: string, depth: number) => {
        return Promise.resolve({
          type: 'externalLink',
          title: 'Backbase',
          url: 'http://www.backbase.com',
          isCurrent: true,
          properties: {},
        });
      },
      getTree: (uuid: string, depth: number) => {
        return of(normalizeNavigationTreeProperties(tree[uuid as TreeType]));
      },
    };
  },
  conditions() {
    return {
      resolveEntitlements(triplets: string) {
        if (triplets === 'ProductSummary.ProductSummary.view') {
          return Promise.resolve(true);
        } else if (triplets === 'ProductSummary.ProductSummary.edit') {
          return Promise.resolve(false);
        } else if (triplets === 'Payments.ManagePositivePay.view') {
          return Promise.resolve(true);
        } else if (triplets === 'Payments.ManagePositivePay.create') {
          return Promise.resolve(false);
        } else if (triplets === 'Payments.ACHManagePositivePay.view') {
          return Promise.resolve(true);
        } else if (triplets === 'Payments.ACHManagePositivePay.create') {
          return Promise.resolve(true);
        }

        return Promise.resolve(true);
      },
    };
  },
};
