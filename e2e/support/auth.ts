import { randomUUID } from 'node:crypto';
import type { Page } from '@playwright/test';

export type Session = 'none' | 'valid' | 'expired' | 'malformed';

type StorageState = {
  cookies: [];
  origins: {
    origin: string;
    localStorage: { name: string; value: string }[];
  }[];
};

const accessTokenKey = 'accessToken';
const refreshTokenKey = 'refreshToken';

function base64Url(value: object): string {
  return Buffer.from(JSON.stringify(value)).toString('base64url');
}

// The frontend only decodes the token, so the signature can be fake. The claims
// match JwtService: subject, jti and expiry, with no role claim.
export function fakeJwt(subject: string, expiresInSeconds = 3600): string {
  const now = Math.floor(Date.now() / 1000);
  const header = base64Url({ alg: 'HS256' });
  const payload = base64Url({
    jti: randomUUID(),
    sub: subject,
    iat: now,
    exp: now + expiresInSeconds
  });

  return `${header}.${payload}.fake-signature`;
}

function tokensFor(session: Session): Record<string, string> {
  switch (session) {
    case 'valid':
      return {
        [accessTokenKey]: fakeJwt('admin'),
        [refreshTokenKey]: fakeJwt('admin', 7 * 24 * 3600)
      };
    case 'expired':
      return {
        [accessTokenKey]: fakeJwt('admin', -3600),
        [refreshTokenKey]: fakeJwt('admin', 7 * 24 * 3600)
      };
    case 'malformed':
      return {
        [accessTokenKey]: 'not-a-jwt',
        [refreshTokenKey]: 'not-a-jwt'
      };
    case 'none':
      return {};
  }
}

// Sessions go in through storageState, not addInitScript: an init script runs
// on every navigation, so it would restore tokens after a forced logout.
export function storageStateFor(session: Session, origin: string): StorageState {
  const localStorage = Object.entries(tokensFor(session)).map(
    ([name, value]) => ({ name, value })
  );

  return { cookies: [], origins: [{ origin, localStorage }] };
}

export async function storedTokens(page: Page) {
  return page.evaluate(
    ([accessKey, refreshKey]) => ({
      accessToken: window.localStorage.getItem(accessKey),
      refreshToken: window.localStorage.getItem(refreshKey)
    }),
    [accessTokenKey, refreshTokenKey]
  );
}

export const noTokens = { accessToken: null, refreshToken: null };
