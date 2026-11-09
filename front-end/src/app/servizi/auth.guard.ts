import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/** Lascia entrare solo chi ha fatto l'accesso. */
export const autenticatoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.token() ? true : inject(Router).createUrlTree(['/accesso']);
};

/** La pagina di accesso non serve a chi è già dentro. */
export const ospiteGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.token() ? inject(Router).createUrlTree(['/']) : true;
};
