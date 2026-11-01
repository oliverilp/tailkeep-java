import React, { useEffect } from 'react';

// The app never had its own 404 or error pages, so these keep the look of the
// Next.js defaults it was showing.

const fontFamily =
  'system-ui,"Segoe UI",Roboto,Helvetica,Arial,sans-serif,"Apple Color Emoji","Segoe UI Emoji"';

const notFoundCss =
  'body{color:#000;background:#fff;margin:0}.not-found-code{border-right:1px solid rgba(0,0,0,.3)}@media (prefers-color-scheme:dark){body{color:#fff;background:#000}.not-found-code{border-right:1px solid rgba(255,255,255,.3)}}';

const errorCss =
  ':root{--error-bg:#fff;--error-text:#171717;--error-button-text:#fff;--error-button-bg:#171717;--error-secondary-border:1px solid rgba(0,0,0,0.08)}@media (prefers-color-scheme:dark){:root{--error-bg:#0a0a0a;--error-text:#ededed;--error-button-text:#0a0a0a;--error-button-bg:#ededed;--error-secondary-border:1px solid rgba(255,255,255,0.14)}}body{margin:0;color:var(--error-text);background:var(--error-bg)}';

const button: React.CSSProperties = {
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  height: '32px',
  padding: '0 12px',
  fontSize: '14px',
  fontWeight: 500,
  lineHeight: '20px',
  borderRadius: '6px',
  cursor: 'pointer'
};

export function NotFound() {
  useEffect(() => {
    const { title } = document;
    document.title = '404: This page could not be found.';

    return () => {
      document.title = title;
    };
  }, []);

  return (
    <div
      style={{
        fontFamily,
        height: '100vh',
        textAlign: 'center',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center'
      }}
    >
      <div>
        <style>{notFoundCss}</style>
        <h1
          className="not-found-code"
          style={{
            display: 'inline-block',
            margin: '0 20px 0 0',
            padding: '0 23px 0 0',
            fontSize: 24,
            fontWeight: 500,
            verticalAlign: 'top',
            lineHeight: '49px'
          }}
        >
          404
        </h1>
        <div style={{ display: 'inline-block' }}>
          <h2
            style={{
              fontSize: 14,
              fontWeight: 400,
              lineHeight: '49px',
              margin: 0
            }}
          >
            This page could not be found.
          </h2>
        </div>
      </div>
    </div>
  );
}

export function RouteError() {
  return (
    <div
      style={{
        fontFamily,
        height: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
      }}
    >
      <style>{errorCss}</style>
      <div
        style={{
          marginTop: '-32px',
          maxWidth: '325px',
          padding: '32px 28px',
          textAlign: 'left'
        }}
      >
        <svg
          width="32"
          height="32"
          viewBox="-0.2 -1.5 32 32"
          fill="none"
          style={{ marginBottom: '24px' }}
        >
          <path
            d="M16.9328 0C18.0839 0.000116771 19.1334 0.658832 19.634 1.69531L31.4299 26.1309C32.0708 27.4588 31.1036 28.9999 29.6291 29H2.00215C0.527541 29 -0.439628 27.4588 0.201371 26.1309L11.9973 1.69531C12.4979 0.658823 13.5474 7.75066e-05 14.6984 0H16.9328ZM3.59493 26H28.0363L16.9328 3H14.6984L3.59493 26ZM15.8156 19C16.9202 19.0001 17.8156 19.8955 17.8156 21C17.8156 22.1045 16.9202 22.9999 15.8156 23C14.7111 23 13.8156 22.1046 13.8156 21C13.8156 19.8954 14.7111 19 15.8156 19ZM17.3156 16.5H14.3156V8.5H17.3156V16.5Z"
            fill="var(--error-text)"
          />
        </svg>
        <h1
          style={{
            fontSize: '24px',
            fontWeight: 500,
            letterSpacing: '-0.02em',
            lineHeight: '32px',
            margin: '0 0 12px 0',
            color: 'var(--error-text)'
          }}
        >
          This page couldn’t load
        </h1>
        <p
          style={{
            fontSize: '14px',
            fontWeight: 400,
            lineHeight: '21px',
            margin: '0 0 20px 0',
            color: 'var(--error-text)'
          }}
        >
          Reload to try again, or go back.
        </p>
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
          <button
            type="button"
            style={{
              ...button,
              color: 'var(--error-button-text)',
              background: 'var(--error-button-bg)',
              border: 'none'
            }}
            onClick={() => window.location.reload()}
          >
            Reload
          </button>
          <button
            type="button"
            style={{
              ...button,
              color: 'var(--error-text)',
              background: 'transparent',
              border: 'var(--error-secondary-border)'
            }}
            onClick={() => {
              if (window.history.length > 1) {
                window.history.back();
              } else {
                window.location.href = '/';
              }
            }}
          >
            Back
          </button>
        </div>
      </div>
    </div>
  );
}
