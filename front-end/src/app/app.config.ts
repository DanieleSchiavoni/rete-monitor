import { ApplicationConfig, LOCALE_ID, provideZoneChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { registerLocaleData } from '@angular/common';
import localeIt from '@angular/common/locales/it';
import { routes } from './app.routes';
import { authInterceptor } from './servizi/auth.interceptor';

registerLocaleData(localeIt);

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([authInterceptor])),
    { provide: LOCALE_ID, useValue: 'it-IT' },
  ],
};
