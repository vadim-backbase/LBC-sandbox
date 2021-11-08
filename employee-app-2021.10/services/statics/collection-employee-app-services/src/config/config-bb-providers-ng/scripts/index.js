/**
 * Project level configuration of modules
 *
 * @module config-bb-project-ng
 *
 * @example
 * Export functions to be used in config phase. E.g.:
 *
 * import { dataSomeEndpointKey } from 'data-bb-some-endpoint-ng';
 *
 * export default [
 *   [`${dataSomeEndpointKey}Provider`, function(endpoint) {
 *     endpoint.setBaseUri('http://example.com/api');
 *   }],
 * ];
 *
 */

define('config-bb-providers-ng', function (require, exports) {
  // the window._portalConfiguration currently is defined in the page template
  function getPortalName(defaultPortalName) {
    return window._portalConfiguration && window._portalConfiguration.portalName
      ? window._portalConfiguration.portalName
      : defaultPortalName;
  };
  function getLinkRoot() {
    return window._portalConfiguration && window._portalConfiguration.linkRoot
      ? window._portalConfiguration.linkRoot
      : '/gateway';
  };

  var retailPortalBaseUri = getLinkRoot() + '/' + getPortalName('retail-banking-demo');
  var businessPortalBaseUri = getLinkRoot() + '/' + getPortalName('business-banking-demo');
  var entiPortalBaseUri = getLinkRoot() + '/' + getPortalName('entitlements-demo');
  var wealthPortalBaseUri = getLinkRoot() + '/' + getPortalName('wealth-management-demo');
  var endpointBaseUri = getLinkRoot() + '/api/';

  exports.default = [
    ['$httpProvider', function(params) {
      Object.assign(params.defaults, {
        xsrfCookieName: 'XSRF-TOKEN',
	      xsrfHeaderName: 'X-XSRF-TOKEN',
      });
    }],
    /* Enable this to have the default Login widget go against a custom AUTH microservice.*/

    ['data-bb-cxp-authentication-http-ng:cXPAuthenticationDataProvider', function(endpoint) {
      endpoint.setHeaders({
        Accept: '*/*',
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
        'Cache-Control': 'no-cache, no-store, must-revalidate',
        'X-Requested-With': 'XMLHttpRequest',
      });
      endpoint.setApiRoot('/gateway/api')
      endpoint.setAuthUri('/auth');
      endpoint.setUsernameParamName('username');
      endpoint.setPasswordParamName('password');
    }],
    ['lib-bb-intent-ng:intentProvider', function(intents) {
      intents.setRoutes({
        /** Retail routes */
        'view.account.category.transactions': retailPortalBaseUri + '/insights',
        'intent.rb.dashboard.navigate.products': retailPortalBaseUri + '/my-products',
        'intent.rb.dashboard.navigate.transactions': retailPortalBaseUri + '/transactions',
        'intent.rb.dashboard.navigate.budgets': retailPortalBaseUri + '/budgets',
        'intent.rb.dashboard.navigate.insights': retailPortalBaseUri + '/insights',
        'intent.rb.transactions.dbit.list.view': retailPortalBaseUri + '/insights',
        'intent.rb.transactions.crdt.list.view': retailPortalBaseUri + '/insights',
        'intent.bb.product.summary.view': retailPortalBaseUri + '/index',
        'intent.bb.manage.products.view': retailPortalBaseUri + '/manage-products',
        'intent.rb.billpay.summary.view': retailPortalBaseUri + '/bill-pay',
        'intent.rb.billpay.single-bill.create': retailPortalBaseUri + '/bill-pay',
        'intent.rb.billpay.single-bill.edit': retailPortalBaseUri + '/bill-pay/manage-single-bill',
        'intent.rb.billpay.recurring-bill.create': retailPortalBaseUri + '/bill-pay/manage-recurring-bill',
        'intent.rb.billpay.pending-payments.view': retailPortalBaseUri + '/bill-pay/pending-payments',
        'intent.rb.billpay.pending-payments.refresh': retailPortalBaseUri + '/bill-pay',
        'intent.rb.billpay.payee.create': retailPortalBaseUri + '/bill-pay/manage-payee',
        'intent.rb.billpay.payee.edit': retailPortalBaseUri + '/bill-pay/manage-payee',
        'intent.rb.categories.management.list.view': retailPortalBaseUri + '/transactions',
        'intent.rb.budgets.create': retailPortalBaseUri + '/budgets',
        'intent.rb.transaction.category.change': retailPortalBaseUri + '/insights',
        'intent.rb.update.analysis.view': retailPortalBaseUri + '/insights',
        'intent.rb.product.selected': retailPortalBaseUri + '/transactions',
        /** Business routes */
        'view.account.accountsOverview': businessPortalBaseUri + '/payments/accounts-overview',
        'view.account.managePayments': businessPortalBaseUri + '/payments/manage-payments',
        'view.account.authorizations': businessPortalBaseUri + '/personal/authorizations',
        'view.account.transactions': businessPortalBaseUri + '/payments/transactions',
        'view.account.notifications': businessPortalBaseUri + '/personal/tools',
        'intent.bus.accounts.overview.details.view': businessPortalBaseUri + '/account-details',
        'go.payment.create': businessPortalBaseUri + '/payments/create-sepa',
        'go.contact.create': businessPortalBaseUri + '/personal/contact-manager',
        'go.action.create': businessPortalBaseUri + '/personal/tools',
        'go.message.create': businessPortalBaseUri + '/personal/secure-inbox',
        'view.currencypairs.table': businessPortalBaseUri + '/trading/fx-trading',
        'view.currencypair.details': businessPortalBaseUri + '/fx-trading-details',
        'go.batch.import': businessPortalBaseUri + '/payments/upload-batches',
        'contact.auth.create': businessPortalBaseUri + '/personal/contact-manager',
        'contact.auth.update': businessPortalBaseUri + '/personal/contact-manager',
        'payment-orders.auth.create': businessPortalBaseUri + '/payments/create-sepa',
        'intent.bus.paymentOrder.usDomesticWirePayment.initiate': businessPortalBaseUri + '/payments/create-us-wire',
        'intent.bus.paymentOrder.usInternationalWirePayment.initiate': businessPortalBaseUri + '/payments/create-us-wire',
        'intent.bus.user-identity-search.list': businessPortalBaseUri + '/users',
        'intent.bus.user-identity-details.view': businessPortalBaseUri + '/users/user-details',
        'intent.bus.user-identity-details.edit': businessPortalBaseUri + '/users/user-details',
        'intent.bus.user-service-agreement.view': businessPortalBaseUri + '/company-admin/service-agreements',
        'intent.bus.audit.table.search': businessPortalBaseUri + '/company-admin/audit',
        /** Wealth routes */
        'view.portfolio.details': wealthPortalBaseUri + '/portfolio-details',
        'view.transactions': wealthPortalBaseUri + '/portfolio-details',
        'view.portfolio.positions.assets': wealthPortalBaseUri + '/portfolio-details',
        /** Entitlements routes */
        'view.user.privileges.assign': entiPortalBaseUri + '/entitlements/master/users-permissions',
        'view.legalentities.select': entiPortalBaseUri + '/entitlements/master/legal-entities',
        'view.serviceagreement.assignpairs': entiPortalBaseUri + '/entitlements/consumer/job-profile',
        'view.serviceagreement.userprivileges': entiPortalBaseUri + '/entitlements/provider/users-permissions',
        'intent.bb.accessgroup.serviceagreement.viewBoth': entiPortalBaseUri + '/entitlements/provider-consumer/users-permissions',
        'intent.bb.accessgroup.serviceagreement.manage.entitlements': entiPortalBaseUri + '/manage-entitlements',
        'intent.bb.accessgroup.serviceagreement.manage.entitlements.stop': entiPortalBaseUri + '/company-admin/service-agreements'
      });
    }],

    [
      'data-bb-product-summary-http-ng:productSummaryDataProvider',
      function(endpoint) {
        console.log('configuring provider data-bb-product-summary-http-ng:productSummaryDataProvider to BASE_URIarrangement-manager/');
        endpoint.setBaseUri(endpointBaseUri + 'arrangement-manager/');
      }
    ],
    [
      'data-bb-arrangements-http-ng:arrangementsDataProvider',
      function(endpoint) {
        console.log('configuring provider data-bb-arrangements-http-ng:arrangementsDataProvider to BASE_URIarrangement-manager/');
        endpoint.setBaseUri(endpointBaseUri + 'arrangement-manager/');
      }
    ],
    [
      'data-bb-contact-http-ng:contactDataProvider',
      function(endpoint) {
        console.log('configuring provider data-bb-contact-http-ng:contactDataProvider to BASE_URIcontact-manager/');
        endpoint.setBaseUri(endpointBaseUri + 'contact-manager/');
      }
    ],
    [
      'data-bb-transactions-http-ng:transactionsDataProvider',
      function(endpoint) {
        console.log('configuring provider data-bb-transactions-http-ng:transactionsDataProvider to BASE_URItransactions-presentation-service/');
        endpoint.setBaseUri(endpointBaseUri + 'transactions-presentation-service/');
      }
    ],
    ['data-bb-personal-finance-management-http-ng:personalFinanceManagementDataProvider', function(endpoint) {
      console.log('configuring provider data-bb-personal-finance-management-http-ng:personalFinanceManagementDataProvider to /pfm-presentation-service/');
      endpoint.setBaseUri(endpointBaseUri + '/pfm-presentation-service/');
    }],
    [
      'data-bb-payments-http-ng:paymentsDataProvider',
      function(endpoint) {
        console.log('configuring provider data-bb-payments-http-ng:paymentsDataProvider to BASE_URIpayment-presentation-service-mock/');
        endpoint.setBaseUri(endpointBaseUri + 'payment-presentation-service-mock/');
      }
    ],
    [
      'data-bb-payment-orders-http-ng:paymentOrdersDataProvider',
      function(endpoint) {
        console.log('configuring provider data-bb-payment-orders-http-ng:paymentOrdersDataProvider to BASE_URIpayment-order-service/');
        endpoint.setBaseUri(endpointBaseUri + 'payment-order-service/');
      }
    ],
    [
      'data-bb-payment-batch-http-ng:paymentBatchDataProvider',
      function(endpoint) {
        console.log('configuring provider data-bb-payment-batch-http-ng:paymentBatchDataProvider to BASE_URIpayment-batch/');
        endpoint.setBaseUri(endpointBaseUri + 'payment-batch/');
      }
    ],
    [
      'data-bb-batches-http-ng:batchesDataProvider',
      function(endpoint) {
        console.log('configuring provider data-bb-batches-http-ng:batchesDataProvider to BASE_URIbatches-presentation-service/');
        endpoint.setBaseUri(endpointBaseUri + 'batch-presentation-service/');
      }
    ],
    ['data-bb-saving-goals-http-ng:savingGoalsDataProvider', function(endpoint) {
      console.log('configuring provider data-bb-saving-goals-http-ng:savingGoalsDataProvider to /saving-goals-presentation-service/');
      endpoint.setBaseUri(endpointBaseUri + '/saving-goals-presentation-service/');
    }],
    [
      'data-bb-messaging-service-http-ng:messagingServiceDataProvider',
      function(endpoint) {
        console.log('configuring provider data-bb-messaging-service-http-ng:messagingServiceData to BASE_URImessages-presentation-service/');
        endpoint.setBaseUri(endpointBaseUri + 'messages-service/');
      }
    ],
    [
      'data-bb-action-recipes-http-ng:actionRecipesDataProvider',
      function(endpoint) {
        console.log('configuring provider data-bb-action-recipes-http-ng:actionRecipesData to BASE_URIactionrecipes-presentation-service/');
        endpoint.setBaseUri(endpointBaseUri + 'actionrecipes-presentation-service/');
      }
    ],
    [
      'data-bb-user-http-ng:userDataProvider',
      function(endpoint) {
        console.log('configuring provider data-bb-user-http-ng:userDataProvider to BASE_URIuser-presentation-service/');
        endpoint.setBaseUri(endpointBaseUri + 'user-presentation-service/');
      }
    ],
    [
      'data-bb-notifications-http-ng:notificationsDataProvider',
      function(endpoint) {
        console.log('configuring provider data-bb-notifications-http-ng:notificationsDataProvider to BASE_URInotifications-service/');
        endpoint.setBaseUri(endpointBaseUri + 'notifications-service/');
      }
    ],
    ['data-bb-categories-management-http-ng:categoriesManagementDataProvider', function(endpoint) {
      console.log('configuring provider data-bb-categories-management-http-ng:categoriesManagementDataProvider to /categories-management-presentation-service/');
      endpoint.setBaseUri(endpointBaseUri + '/categories-management-presentation-service/');
    }],
    ['data-bb-budgeting-http-ng:budgetingDataProvider', function(endpoint) {
      console.log('configuring provider data-bb-budgeting-http-ng:budgetingDataProvider to /budgeting-presentation-service/');
      endpoint.setBaseUri(endpointBaseUri + '/budgeting-presentation-service/');
    }],
    [
      'data-bb-audit-http-ng:auditDataProvider',
      function(endpoint) {
        console.log('configuring provider data-bb-audit-http-ng:auditDataProvider to BASE_URIaudit-presentation-service/');
        endpoint.setBaseUri(endpointBaseUri + 'audit-presentation-service/');
      }
    ],
    [
      'data-bb-bill-pay-http-ng:billPayDataProvider',
      function(endpoint) {
        console.log('configuring provider data-bb-bill-pay-http-ng:billPayDataProvider to BASE_URIbillpay-integrator/');
        endpoint.setBaseUri(endpointBaseUri + 'billpay-integrator/');
      }
    ],
    [
      'data-bb-account-statements-http-ng:accountStatementsDataProvider',
      function (endpoint) {
        console.log('configuring provider data-bb-account-statements-http-ng:accountStatementsDataProvider to /account-statement-presentation-service/');
        endpoint.setBaseUri(endpointBaseUri + 'account-statement-presentation-service/');
      }
    ],
    /** Wealth data urls */
    [
      'data-bb-portfolio-summary-http-ng:portfolioSummaryDataProvider',
      function (endpoint) {
        console.log('configuring provider data-bb-portfolio-summary-http-ng:portfolioSummaryDataProvider to /portfolio-summary-presentation-service/');
        endpoint.setBaseUri(endpointBaseUri + 'portfolio-summary-presentation-service/');
      }
    ],
    /** Entitlements data urls */
    [
      'data-bb-legalentity-http-ng:legalEntityDataProvider',
      function (endpoint) {
        console.log('configuring provider data-bb-legalentity-http-ng:legalEntityDataProvider to /legalentity-presentation-service/');
        endpoint.setBaseUri(endpointBaseUri + 'legalentity-presentation-service/');
      }
    ],
    [
      'data-bb-accessgroups-http-ng:accessGroupsDataProvider',
      function (endpoint) {
        console.log('configuring provider data-bb-accessgroups-http-ng:accessGroupsDataProvider to /accessgroup-presentation-service/');
        endpoint.setBaseUri(endpointBaseUri + 'accessgroup-presentation-service/');
      }
    ],
    ['data-bb-approval-http-ng:approvalDataProvider', function (endpoint) {
      console.log('configuring provider data-bb-approval-http-ng:approvalDataProvider to /approval-service/');
      endpoint.setBaseUri(endpointBaseUri + '/approval-service/');
    }],
    [
      'data-bb-limits-http-ng:limitsDataProvider',
      function (endpoint) {
        console.log('configuring provider data-bb-limits-http-ng:limitsDataProvider to /limits-presentation-service/');
        endpoint.setBaseUri(endpointBaseUri + 'limits-presentation-service/');
      }
    ]
  ];
});
