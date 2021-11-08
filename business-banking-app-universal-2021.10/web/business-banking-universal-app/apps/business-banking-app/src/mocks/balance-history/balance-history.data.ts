import { AccountBalanceHistory } from '@backbase/data-ang/arrangements';

export const csvExportData = `Balance,Currency,Date,ArrangementId
10.29,EUR,2018-05-10T00:00:00,1
-123.2,EUR,2018-23-10T00:00:00,1
123.2,EUR,2018-24-10T00:00:00,2`;

export const balanceHistotyItems: AccountBalanceHistory = {
  items: [
    {
      arrangementId: '3cdb2224-8926-4b4d-a99f-1c9dfbbb4699',
      balanceHistory: [
        {
          dateFrom: '2017-05-10T00:00:00.000Z',
          dateTo: '2017-05-11T00:00:00.000Z',
          valuePtc: '232',
          value: {
            currency: 'EUR',
            balance: '12',
          },
        },
        {
          dateTo: '2017-06-13T00:00:00.000Z',
          dateFrom: '2017-05-12T00:00:00.000Z',
          value: {
            currency: 'EUR',
            balance: '22',
          },
          valuePtc: '22',
        },
        {
          dateFrom: '2017-05-23T00:00:00.000Z',
          dateTo: '2017-06-24T00:00:00.000Z',
          value: {
            balance: '32',
            currency: 'EUR',
          },
          valuePtc: '32',
        },
        {
          dateFrom: '2017-06-06T00:00:00.000Z',
          dateTo: '2017-06-07T00:00:00.000Z',
          value: {
            currency: 'EUR',
            balance: '54',
          },
          valuePtc: '54',
        },
        {
          dateTo: '2017-07-03T00:00:00.000Z',
          dateFrom: '2017-06-02T00:00:00.000Z',
          value: {
            currency: 'EUR',
            balance: '34',
          },
          valuePtc: '34',
        },
        {
          dateTo: '2017-07-13T00:00:00.000Z',
          dateFrom: '2017-06-12T00:00:00.000Z',
          value: {
            currency: 'EUR',
            balance: '4',
          },
          valuePtc: '4',
        },
        {
          dateTo: '2017-07-19T00:00:00.000Z',
          dateFrom: '2017-06-18T00:00:00.000Z',
          valuePtc: '-14',
          value: {
            currency: 'EUR',
            balance: '-14',
          },
        },
      ],
    },
  ],
};
