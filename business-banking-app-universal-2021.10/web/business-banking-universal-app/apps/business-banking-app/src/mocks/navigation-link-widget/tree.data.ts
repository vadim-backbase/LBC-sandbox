// Response from request in cx environment
// {version}/portal/navigation/tree?type=fixed&uuid={properties.rootLink}&fixedRoot={properties.rootLink}&depth={properties.depth}
const navigation = {
  type: 'menuHeader',
  title: 'Main Navigation',
  url: 'business-banking-app/main',
  isCurrent: false,
  preferences: {
    sectionName: {
      name: 'sectionName',
      value: 'main',
    },
    itemType: {
      name: 'itemType',
      value: 'menuHeader',
    },
    Description: {
      name: 'Description',
      value: '',
    },
    generatedUrl: {
      name: 'generatedUrl',
      value: 'business-banking-app/main',
    },
    title: {
      name: 'title',
      value: 'Main Navigation',
    },
    SecuritySameAsParent: {
      name: 'SecuritySameAsParent',
      value: 'true',
    },
    order: {
      name: 'order',
      value: '10.0',
    },
  },
  children: [
    {
      type: 'menuHeader',
      title: 'ACCOUNTS & CARDS',
      url: 'business-banking-app/accounts-cards',
      isCurrent: false,
      preferences: {
        sectionName: {
          name: 'sectionName',
          value: 'accounts-cards',
        },
        itemType: {
          name: 'itemType',
          value: 'menuHeader',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-banking-app/accounts-cards',
        },
        menuIcon: {
          name: 'menuIcon',
          value: '',
        },
        menuAccessibilityTitle: {
          name: 'menuAccessibilityTitle',
          value: '',
        },
        title: {
          name: 'title',
          value: 'ACCOUNTS & CARDS',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '752.5',
        },
      },
      children: [
        {
          type: 'alias',
          title: 'Accounts',
          url: '#/accounts',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'accounts',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/accounts-cards/accounts',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'accounts',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Accounts',
            },
            Url: {
              name: 'Url',
              value: '#/accounts',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'false',
            },
            order: {
              name: 'order',
              value: '-750.0',
            },
          },
          children: [],
          isInPath: false,
        },
        {
          type: 'alias',
          title: 'Cards',
          url: '#/cards',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'cards',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/accounts-cards/cards',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'credit-card',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Cards',
            },
            Url: {
              name: 'Url',
              value: '#/cards',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'false',
            },
            order: {
              name: 'order',
              value: '-500.0',
            },
          },
          children: [],
          isInPath: false,
        },
        {
          type: 'alias',
          title: 'Account Statements',
          url: '#/account-statements',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'account-statements',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/accounts-cards/account-statements',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'description',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Account Statements',
            },
            Url: {
              name: 'Url',
              value: '#/account-statements',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'true',
            },
            order: {
              name: 'order',
              value: '-250.0',
            },
          },
          children: [],
          isInPath: false,
        },
        {
          type: 'alias',
          title: 'Transactions',
          url: '#/transactions',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'transactions',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/accounts-cards/transactions',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'transactions',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Transactions',
            },
            Url: {
              name: 'Url',
              value: '#/transactions',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'false',
            },
            order: {
              name: 'order',
              value: '0.0',
            },
          },
          children: [],
          isInPath: false,
        },
      ],
      isInPath: false,
    },
    {
      type: 'divider',
      title: '',
      url: 'business-banking-app/link_1583837742460',
      isCurrent: false,
      preferences: {
        itemType: {
          name: 'itemType',
          value: 'divider',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-banking-app/link_1583837742460',
        },
        className: {
          name: 'className',
          value: 'bd-divider',
        },
        title: {
          name: 'title',
          value: '',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '814.375',
        },
      },
      children: [],
      isInPath: false,
    },
    {
      type: 'menuHeader',
      title: 'MOVE MONEY',
      url: 'business-banking-app/move-money',
      isCurrent: false,
      preferences: {
        sectionName: {
          name: 'sectionName',
          value: 'move-money',
        },
        itemType: {
          name: 'itemType',
          value: 'menuHeader',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-banking-app/move-money',
        },
        menuIcon: {
          name: 'menuIcon',
          value: '',
        },
        menuAccessibilityTitle: {
          name: 'menuAccessibilityTitle',
          value: '',
        },
        title: {
          name: 'title',
          value: 'MOVE MONEY',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '876.25',
        },
      },
      children: [
        {
          type: 'alias',
          title: 'Transfers',
          url: '#/transfers',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'transfers',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/move-money/transfers',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'payments',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Transfers',
            },
            Url: {
              name: 'Url',
              value: '#/transfers',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'true',
            },
            order: {
              name: 'order',
              value: '-997.0703125',
            },
          },
          children: [],
          isInPath: false,
        },
        {
          type: 'alias',
          title: 'Debits',
          url: '#/debits',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'debits',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/move-money/debits',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'payments',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Debits',
            },
            Url: {
              name: 'Url',
              value: '#/debits',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'true',
            },
            order: {
              name: 'order',
              value: '-996.337890625',
            },
          },
          children: [],
          isInPath: false,
        },
        {
          type: 'alias',
          title: 'Batches',
          url: '#/batches',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'batches',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/move-money/batches',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'batches',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Batches',
            },
            Url: {
              name: 'Url',
              value: '#/batches',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'true',
            },
            order: {
              name: 'order',
              value: '-996.2158203125',
            },
          },
          children: [],
          isInPath: false,
        },
        {
          type: 'alias',
          title: 'Stop Check Payments',
          url: '#/stop-check-payments',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'stop-check-payments',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/move-money/stop-check-payments',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'cancel-presentation',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Stop Check Payments',
            },
            Url: {
              name: 'Url',
              value: '#/stop-check-payments',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'true',
            },
            order: {
              name: 'order',
              value: '-996.15478515625',
            },
          },
          children: [],
          isInPath: false,
        },
        {
          type: 'alias',
          title: 'Templates',
          url: '#/templates',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'templates',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/move-money/templates',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'contacts',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Templates',
            },
            Url: {
              name: 'Url',
              value: '#/templates',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'true',
            },
            order: {
              name: 'order',
              value: '-996.15478515625',
            },
          },
          children: [],
          isInPath: false,
        },
        {
          type: 'alias',
          title: 'Payees',
          url: '#/payees',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'payees',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/move-money/payees',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'contacts',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Payees',
            },
            Url: {
              name: 'Url',
              value: '#/payees',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'false',
            },
            order: {
              name: 'order',
              value: '-996.124267578125',
            },
          },
          children: [],
          isInPath: false,
        },
      ],
      isInPath: false,
    },
    {
      type: 'divider',
      title: '',
      url: 'business-banking-app/link_1583837736980',
      isCurrent: false,
      preferences: {
        itemType: {
          name: 'itemType',
          value: 'divider',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-banking-app/link_1583837736980',
        },
        className: {
          name: 'className',
          value: 'bd-divider',
        },
        title: {
          name: 'title',
          value: '',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '907.1875',
        },
      },
      children: [],
      isInPath: false,
    },
    {
      type: 'menuHeader',
      title: 'ACCOUNT SERVICES',
      url: 'business-banking-app/account-services',
      isCurrent: false,
      preferences: {
        sectionName: {
          name: 'sectionName',
          value: 'account-services',
        },
        itemType: {
          name: 'itemType',
          value: 'menuHeader',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-banking-app/account-services',
        },
        menuIcon: {
          name: 'menuIcon',
          value: '',
        },
        menuAccessibilityTitle: {
          name: 'menuAccessibilityTitle',
          value: '',
        },
        title: {
          name: 'title',
          value: 'ACCOUNT SERVICES',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '938.125',
        },
      },
      children: [
        {
          type: 'alias',
          title: 'Positive Pay',
          url: '#/positive-pay',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'positive-pay',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/account-services/positive-pay',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'note',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Positive Pay',
            },
            Url: {
              name: 'Url',
              value: '#/positive-pay',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'true',
            },
            order: {
              name: 'order',
              value: '-996.2158203125',
            },
          },
          children: [],
          isInPath: false,
        },
        {
          type: 'alias',
          title: 'ACH Positive Pay',
          url: '#/ach-positive-pay',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'ach-positive-pay',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/account-services/ach-positive-pay',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'verified-user',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'ACH Positive Pay',
            },
            Url: {
              name: 'Url',
              value: '#/ach-positive-pay',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'true',
            },
            order: {
              name: 'order',
              value: '-250.0',
            },
          },
          children: [],
          isInPath: false,
        },
      ],
      isInPath: false,
    },
    {
      type: 'divider',
      title: '',
      url: 'business-banking-app/link_1583837736989',
      isCurrent: false,
      preferences: {
        itemType: {
          name: 'itemType',
          value: 'divider',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-banking-app/link_1583837736989',
        },
        className: {
          name: 'className',
          value: 'bd-divider',
        },
        title: {
          name: 'title',
          value: '',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '907.1875',
        },
      },
      children: [],
      isInPath: false,
    },
    {
      type: 'menuHeader',
      title: 'FINANCE MANAGEMENT',
      url: 'business-banking-app/finance-management',
      isCurrent: false,
      preferences: {
        sectionName: {
          name: 'sectionName',
          value: 'finance-management',
        },
        itemType: {
          name: 'itemType',
          value: 'menuHeader',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-banking-app/finance-management',
        },
        menuIcon: {
          name: 'menuIcon',
          value: '',
        },
        menuAccessibilityTitle: {
          name: 'menuAccessibilityTitle',
          value: '',
        },
        title: {
          name: 'title',
          value: 'FINANCE MANAGEMENT',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '938.125',
        },
      },
      children: [
        {
          type: 'alias',
          title: 'Cash Flow',
          url: '#/cash-flow',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'cash-flow',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/finance-management/cash-flow',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'currency-trade',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Cash Flow',
            },
            Url: {
              name: 'Url',
              value: '#/cash-flow',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'true',
            },
            order: {
              name: 'order',
              value: '-500.0',
            },
          },
          children: [],
          isInPath: false,
        },
        {
          type: 'alias',
          title: 'Sweeping',
          url: '#/sweeping',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'sweeping',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/finance-management/sweeping',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'vertical-align-center',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Sweeping',
            },
            Url: {
              name: 'Url',
              value: '#/sweeping',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'false',
            },
            order: {
              name: 'order',
              value: '-375.0',
            },
          },
          children: [],
          isInPath: false,
        },
        {
          type: 'alias',
          title: 'Forex',
          url: '#/forex',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'forex',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/finance-management/forex',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'monetization-on',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Forex',
            },
            Url: {
              name: 'Url',
              value: '#/forex',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'true',
            },
            order: {
              name: 'order',
              value: '-250.0',
            },
          },
          children: [],
          isInPath: false,
        },
      ],
      isInPath: false,
    },
    {
      type: 'divider',
      title: '',
      url: 'business-banking-app/link_1583837610397',
      isCurrent: false,
      preferences: {
        itemType: {
          name: 'itemType',
          value: 'divider',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-banking-app/link_1583837610397',
        },
        className: {
          name: 'className',
          value: 'bd-divider',
        },
        title: {
          name: 'title',
          value: '',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '976.796875',
        },
      },
      children: [],
      isInPath: false,
    },
    {
      type: 'menuHeader',
      title: 'PERSONAL',
      url: 'business-banking-app/personal',
      isCurrent: false,
      preferences: {
        sectionName: {
          name: 'sectionName',
          value: 'personal',
        },
        itemType: {
          name: 'itemType',
          value: 'menuHeader',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-banking-app/personal',
        },
        menuIcon: {
          name: 'menuIcon',
          value: '',
        },
        menuAccessibilityTitle: {
          name: 'menuAccessibilityTitle',
          value: '',
        },
        title: {
          name: 'title',
          value: 'PERSONAL',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '984.53125',
        },
      },
      children: [
        {
          type: 'alias',
          title: 'Inbox',
          url: '#/messages',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'inbox',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/personal/inbox',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'local-post-office',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Inbox',
            },
            Url: {
              name: 'Url',
              value: '#/messages',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'false',
            },
            order: {
              name: 'order',
              value: '-750.0',
            },
          },
          children: [],
          isInPath: false,
        },
        {
          type: 'alias',
          title: 'My Profile',
          url: '#/my-profile',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'my-profile',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/personal/my-profile',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'profile',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'My Profile',
            },
            Url: {
              name: 'Url',
              value: '#/my-profile',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'false',
            },
            order: {
              name: 'order',
              value: '500.0',
            },
          },
          children: [],
          isInPath: false,
        },
        {
          type: 'alias',
          title: 'Find ATM or Branch',
          url: '#/find-atm-branch',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'places',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/personal/find-atm-branch',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'location-on',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Find ATM or Branch ',
            },
            Url: {
              name: 'Url',
              value: '#/find-atm-branch',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'false',
            },
            order: {
              name: 'order',
              value: '0.0',
            },
          },
          children: [],
          isInPath: false,
        },
      ],
      isInPath: false,
    },
    {
      type: 'divider',
      title: '',
      url: 'business-banking-app/link_1590057641963',
      isCurrent: false,
      preferences: {
        itemType: {
          name: 'itemType',
          value: 'divider',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-banking-app/link_1590057641963',
        },
        className: {
          name: 'className',
          value: 'bd-divider',
        },
        title: {
          name: 'title',
          value: '',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '988.3984375',
        },
      },
      children: [],
      isInPath: false,
    },
    {
      type: 'menuHeader',
      title: 'COMPANY ADMINISTRATION',
      url: 'business-banking-app/company-administration',
      isCurrent: false,
      preferences: {
        sectionName: {
          name: 'sectionName',
          value: 'company-administration',
        },
        itemType: {
          name: 'itemType',
          value: 'menuHeader',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-banking-app/company-administration',
        },
        menuIcon: {
          name: 'menuIcon',
          value: '',
        },
        menuAccessibilityTitle: {
          name: 'menuAccessibilityTitle',
          value: '',
        },
        title: {
          name: 'title',
          value: 'COMPANY ADMINISTRATION',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '992.265625',
        },
      },
      children: [
        {
          type: 'alias',
          title: 'Audit',
          url: '#/audit',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'audit',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/company-administration/audit',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'audit',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Audit',
            },
            Url: {
              name: 'Url',
              value: '#/audit',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'true',
            },
            order: {
              name: 'order',
              value: '-621.25',
            },
          },
          children: [],
          isInPath: false,
        },
        {
          type: 'alias',
          title: 'Company Permissions',
          url: '#/company-permissions',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'company-permissions',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/company-administration/company-permissions',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'users',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Company Permissions',
            },
            Url: {
              name: 'Url',
              value: '#/company-permissions',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'false',
            },
            order: {
              name: 'order',
              value: '500.0',
            },
          },
          children: [],
          isInPath: false,
        },
        {
          type: 'alias',
          title: 'Service Agreements',
          url: '#/service-agreements',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'service-agreements',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/company-administration/service-agreements',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'ballot',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Service Agreements',
            },
            Url: {
              name: 'Url',
              value: '#/service-agreements',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'false',
            },
            order: {
              name: 'order',
              value: '750.0',
            },
          },
          children: [],
          isInPath: false,
        },
        {
          type: 'alias',
          title: 'Global limits',
          url: '#/global-limits',
          isCurrent: false,
          preferences: {
            sectionName: {
              name: 'sectionName',
              value: 'global-limits',
            },
            itemType: {
              name: 'itemType',
              value: 'alias',
            },
            Description: {
              name: 'Description',
              value: '',
            },
            generatedUrl: {
              name: 'generatedUrl',
              value: 'business-banking-app/company-administration/global-limits',
            },
            menuIcon: {
              name: 'menuIcon',
              value: 'apps',
            },
            ItemRef: {
              name: 'ItemRef',
              value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
            },
            menuAccessibilityTitle: {
              name: 'menuAccessibilityTitle',
              value: '',
            },
            title: {
              name: 'title',
              value: 'Global limits',
            },
            Url: {
              name: 'Url',
              value: '#/global-limits',
            },
            SecuritySameAsParent: {
              name: 'SecuritySameAsParent',
              value: 'false',
            },
            order: {
              name: 'order',
              value: '850.0',
            },
          },
          children: [],
          isInPath: false,
        },
      ],
      isInPath: false,
    },
  ],
  isInPath: false,
};

const newTransfer = {
  type: 'menuHeader',
  title: 'New Transfer',
  url: 'business-banking-app/move-money',
  isCurrent: false,
  preferences: {
    sectionName: {
      name: 'sectionName',
      value: 'new transfer',
    },
    itemType: {
      name: 'itemType',
      value: 'menuHeader',
    },
    Description: {
      name: 'Description',
      value: '',
    },
    generatedUrl: {
      name: 'generatedUrl',
      value: 'business-banking-app/move-money',
    },
    title: {
      name: 'title',
      value: 'New Transfer',
    },
    SecuritySameAsParent: {
      name: 'SecuritySameAsParent',
      value: 'true',
    },
    order: {
      name: 'order',
      value: '10.0',
    },
  },
  children: [
    {
      type: 'alias',
      title: 'Easy transfer',
      deepPath: 'new-transfer-easy',
      url: '#/new-transfer-easy',
      icon: '',
      permission: 'userConsumer',
      preferences: {
        sectionName: {
          name: 'sectionName',
          value: 'new-transfer-easy',
        },
        itemType: {
          name: 'itemType',
          value: 'alias',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-app/move-money/new-transfer-easy',
        },
        menuIcon: {
          name: 'menuIcon',
          value: '',
        },
        ItemRef: {
          name: 'ItemRef',
          value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c620',
        },
        menuAccessibilityTitle: {
          name: 'menuAccessibilityTitle',
          value: '',
        },
        title: {
          name: 'title',
          value: 'Easy transfer',
        },
        Url: {
          name: 'Url',
          value: '#/new-transfer-easy',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '-998.046875',
        },
      },
    },
    {
      type: 'alias',
      title: 'ACH transfer',
      url: '#/new-transfer-ach',
      isCurrent: false,
      preferences: {
        sectionName: {
          name: 'sectionName',
          value: 'new-transfer-ach',
        },
        itemType: {
          name: 'itemType',
          value: 'alias',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-banking-app/move-money/new-transfer-ach',
        },
        menuIcon: {
          name: 'menuIcon',
          value: '',
        },
        ItemRef: {
          name: 'ItemRef',
          value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
        },
        menuAccessibilityTitle: {
          name: 'menuAccessibilityTitle',
          value: '',
        },
        title: {
          name: 'title',
          value: 'ACH transfer',
        },
        Url: {
          name: 'Url',
          value: '#/new-transfer-ach',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '-999.0234375',
        },
      },
      children: [],
      isInPath: false,
    },
    {
      type: 'alias',
      title: 'Wire transfer',
      url: '#/new-transfer-wire',
      isCurrent: false,
      preferences: {
        sectionName: {
          name: 'sectionName',
          value: 'new-transfer-wire',
        },
        itemType: {
          name: 'itemType',
          value: 'alias',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-banking-app/move-money/new-transfer-wire',
        },
        menuIcon: {
          name: 'menuIcon',
          value: '',
        },
        ItemRef: {
          name: 'ItemRef',
          value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c60',
        },
        menuAccessibilityTitle: {
          name: 'menuAccessibilityTitle',
          value: '',
        },
        title: {
          name: 'title',
          value: 'Wire transfer',
        },
        Url: {
          name: 'Url',
          value: '#/new-transfer-wire',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '-998.046875',
        },
      },
      children: [],
      isInPath: false,
    },
    {
      type: 'alias',
      title: 'International transfer',
      url: '#/new-international-wire',
      isCurrent: false,
      preferences: {
        sectionName: {
          name: 'sectionName',
          value: 'new-international-wire',
        },
        itemType: {
          name: 'itemType',
          value: 'alias',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-app/move-money/new-international-wire',
        },
        menuIcon: {
          name: 'menuIcon',
          value: '',
        },
        ItemRef: {
          name: 'ItemRef',
          value: 'e2fb1368-7ca6-47f9-add8-e6f81e269s70',
        },
        menuAccessibilityTitle: {
          name: 'menuAccessibilityTitle',
          value: '',
        },
        title: {
          name: 'title',
          value: 'International transfer',
        },
        Url: {
          name: 'Url',
          value: '#/new-international-wire',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '-998.046875',
        },
      },
      children: [],
      isInPath: false,
    },
  ],
  isInPath: false,
};

const manageTemplates = {
  type: 'menuHeader',
  title: 'New template',
  url: 'business-banking-app/templates',
  isCurrent: false,
  preferences: {
    sectionName: {
      name: 'sectionName',
      value: 'new template',
    },
    itemType: {
      name: 'itemType',
      value: 'menuHeader',
    },
    Description: {
      name: 'Description',
      value: '',
    },
    generatedUrl: {
      name: 'generatedUrl',
      value: 'business-banking-app/templates/payments',
    },
    title: {
      name: 'title',
      value: 'New template',
    },
    SecuritySameAsParent: {
      name: 'SecuritySameAsParent',
      value: 'true',
    },
    order: {
      name: 'order',
      value: '10.0',
    },
  },
  children: [
    {
      type: 'alias',
      title: 'Payment',
      deepPath: 'payments',
      url: '#/templates/payments?modalOpened=create-payment-template',
      icon: '',
      permission: 'userConsumer',
      preferences: {
        sectionName: {
          name: 'sectionName',
          value: 'create-payment-template',
        },
        itemType: {
          name: 'itemType',
          value: 'alias',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-banking-app#/templates/payments?modalOpened=create-payment-template',
        },
        menuIcon: {
          name: 'menuIcon',
          value: 'payments',
        },
        ItemRef: {
          name: 'ItemRef',
          value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c61',
        },
        menuAccessibilityTitle: {
          name: 'menuAccessibilityTitle',
          value: '',
        },
        title: {
          name: 'title',
          value: 'Payment',
        },
        subTitle: {
          name: 'subTitle',
          value: 'Create payment template',
        },
        Url: {
          name: 'Url',
          value: 'business-banking-app#/templates/payments?modalOpened=create-payment-template',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '-999.0234375',
        },
      },
    },
    {
      type: 'alias',
      title: 'Batch',
      url: '#/templates/batches/list?open-batch-modal=manual-batch-template',
      deepPath: 'batch',
      icon: '',
      permission: 'userConsumer',
      preferences: {
        sectionName: {
          name: 'sectionName',
          value: 'manual-batch',
        },
        itemType: {
          name: 'itemType',
          value: 'alias',
        },
        Description: {
          name: 'Description',
          value: '',
        },
        generatedUrl: {
          name: 'generatedUrl',
          value: 'business-banking-app#/templates/batches/list?open-batch-modal=manual-batch-template',
        },
        menuIcon: {
          name: 'menuIcon',
          value: 'batches',
        },
        ItemRef: {
          name: 'ItemRef',
          value: 'e2fb1368-7ca6-47f9-add8-e6f81e269c621',
        },
        menuAccessibilityTitle: {
          name: 'menuAccessibilityTitle',
          value: '',
        },
        title: {
          name: 'title',
          value: 'Batch',
        },
        subTitle: {
          name: 'subTitle',
          value: 'Create batch template',
        },
        Url: {
          name: 'Url',
          value: 'business-banking-app#/templates/batches/list?open-batch-modal=manual-batch-template',
        },
        SecuritySameAsParent: {
          name: 'SecuritySameAsParent',
          value: 'true',
        },
        order: {
          name: 'order',
          value: '-998.046875',
        },
      },
    },
  ],
  isInPath: false,
};

export enum TreeType {
  NAVIGATION = 'business-banking-app/main/navigation',
  NEW_TRANSFER = 'business-banking-app/main/new-transfer',
  NEW_TEMPLATE = 'business-banking-app/main/manage-templates',
}

export const tree = {
  [TreeType.NAVIGATION]: navigation,
  [TreeType.NEW_TRANSFER]: newTransfer,
  [TreeType.NEW_TEMPLATE]: manageTemplates,
};
