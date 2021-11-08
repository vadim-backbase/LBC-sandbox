import { Injectable, Provider } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpInterceptor,
  HttpEvent,
  HttpResponse,
  HttpHeaders,
  HTTP_INTERCEPTORS,
} from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { balanceHistotyItems, csvExportData } from './balance-history.data';

interface Params {
  arrangementIds?: string[];
  from: number;
  size: number;
  searchTerm?: string;
  ignoredArrangementIds?: string[];
}

@Injectable()
export class BalanceHistoryInterceptor implements HttpInterceptor {
  intercept(request: HttpRequest<Params>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (
      `${localStorage.getItem('enableMocks')}` === 'true' &&
      request.url.includes('balance-history') &&
      !request.url.includes('arrangement')
    ) {
      if (request.urlWithParams.includes('from')) {
        return of(
          new HttpResponse<any>({
            body: csvExportData,
            status: 200,
            headers: new HttpHeaders({
              'x-total-count': `${5}`,
            }),
          }),
        );
      } else {
        return of(
          new HttpResponse<Partial<any>>({
            body: balanceHistotyItems,
            status: 200,
            headers: new HttpHeaders({
              'x-total-count': `${5}`,
            }),
          }),
        );
      }
    }
    return next.handle(request);
  }
}

export const BalanceHistoryInterceptorProvider: Provider = {
  provide: HTTP_INTERCEPTORS,
  useClass: BalanceHistoryInterceptor,
  multi: true,
};
