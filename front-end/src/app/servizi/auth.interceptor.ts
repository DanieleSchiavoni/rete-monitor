import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { URL_API } from '../config';
import { AuthService } from './auth.service';

/** Aggiunge il token a ogni chiamata al backend; se il backend risponde 401 riporta alla pagina di accesso. */
export const authInterceptor: HttpInterceptorFn = (richiesta, prossimo) => {
  const auth = inject(AuthService);
  const pubblica = richiesta.url.startsWith(`${URL_API}/auth/`) && !richiesta.url.endsWith('/auth/io');
  const token = pubblica ? null : auth.token();

  const conToken = token ? richiesta.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : richiesta;

  return prossimo(conToken).pipe(
    catchError((errore: unknown) => {
      if (errore instanceof HttpErrorResponse && errore.status === 401 && !pubblica) {
        auth.esci();
      }
      return throwError(() => errore);
    }),
  );
};
