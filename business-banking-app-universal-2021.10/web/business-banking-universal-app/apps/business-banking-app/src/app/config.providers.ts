import { Provider } from '@angular/core';
import { BB_PLACE_TYPE_CONFIG_TOKEN, PlaceTypeConfigProvider } from '@backbase/business-ang/places';
import { HttpXsrfInterceptor } from './services/http-xsrf.interceptor';
import { HttpXsrfTokenExtractor, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ACCOUNTS_DISPLAYING_FORMAT, AccountsDisplayingFormatConfiguration } from '@backbase/ui-ang';
import { CashFlowJourneyConfiguration, CashFlowJourneyConfigurationToken } from '@backbase/cash-flow-journey-ang';
import { AccountsJourneyConfiguration, ACCOUNTS_JOURNEY_CONFIGURATION } from '@backbase/accounts-journey-ang';
import { AccountAliasDisplayingLevel, ACCOUNT_ALIAS_DISPLAYING_LEVEL } from '@backbase/business-ang/product-summary';
import { AuthInterceptor } from '@backbase/foundation-ang/auth';
import {
  TradeFinanceJourneyConfiguration,
  TradeFinanceJourneyConfigurationToken,
} from '@backbase/trade-finance-journey-ang';
import {
  BB_CONTACT_COUNTRIES_LIST,
  BB_CONTACT_IBAN_COUNTRIES_LIST,
  BB_CONTACT_BANK_ACCOUNT_TYPES_LIST,
} from '@backbase/contact-journey-ang';
import {
  BatchTemplatesJourneyConfiguration,
  BatchTemplatesJourneyConfigurationToken,
} from '@backbase/batch-templates-journey-ang';

import { CONTACT_ACCOUNT_TYPE } from '../../src/provider.config';

export const PlacesConfigProvider: Array<Provider> = [
  {
    provide: BB_PLACE_TYPE_CONFIG_TOKEN,
    useValue: <PlaceTypeConfigProvider>{
      branch: {
        iconName: 'account',
        markerUrl: 'branch-marker.svg',
      },
      atm: {
        iconName: 'credit-card',
        markerUrl: 'atm-marker.svg',
      },
    },
  },
];

export const AuthProvider: Provider = {
  provide: HTTP_INTERCEPTORS,
  useClass: AuthInterceptor,
  multi: true,
};

export const HttpXsrfProvider: Provider = {
  provide: HTTP_INTERCEPTORS,
  useClass: HttpXsrfInterceptor,
  deps: [HttpXsrfTokenExtractor],
  multi: true,
};

export const AccountsDisplayingFormatProvider: Provider = {
  provide: ACCOUNTS_DISPLAYING_FORMAT,
  useValue: <AccountsDisplayingFormatConfiguration>{
    iban: { segments: 4 },
    bban: { segments: 4 },
    cardNumber: { segments: 4 },
  },
};

export const CashFlowConfigProvider: Provider = {
  provide: CashFlowJourneyConfigurationToken,
  useValue: {
    receivablesPageSize: 10,
    payablesPageSize: 10,
  } as Partial<CashFlowJourneyConfiguration>,
};

export const TradeFinanceConfigProvider: Provider = {
  provide: TradeFinanceJourneyConfigurationToken,
  useValue: {
    importLetterOfCreditSchema: {},
  } as TradeFinanceJourneyConfiguration,
};

export const AccountsConfigProvider: Provider = {
  provide: ACCOUNTS_JOURNEY_CONFIGURATION,
  useValue: <Partial<AccountsJourneyConfiguration>>{},
};

export const AccountsAliasDisplayingLevelProvider: Provider = {
  provide: ACCOUNT_ALIAS_DISPLAYING_LEVEL,
  useValue: AccountAliasDisplayingLevel.ACCOUNT,
};

export const ContactConfigProviders: Array<Provider> = [
  {
    provide: BB_CONTACT_COUNTRIES_LIST,
    useValue: ['AU', 'CA', 'IE', 'NL', 'NZ', 'GB', 'US'],
  },
  {
    provide: BB_CONTACT_IBAN_COUNTRIES_LIST,
    useValue: {},
  },
  {
    provide: BB_CONTACT_BANK_ACCOUNT_TYPES_LIST,
    useValue: CONTACT_ACCOUNT_TYPE,
  },
];

export const BatchTemplatesJourneyConfigProvider: Provider = {
  provide: BatchTemplatesJourneyConfigurationToken,
  useValue: {
    displayCreateTemplatesButton: false,
  } as Partial<BatchTemplatesJourneyConfiguration>,
};
