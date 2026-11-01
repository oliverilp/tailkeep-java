import './globals.css';
import React from 'react';
import { Providers } from '@/components/providers';
import { Toaster } from 'sonner';

export const dynamic = 'force-dynamic';

const siteConfig = {
  title: 'Tailkeep',
  description: 'YouTube Downloader UI'
};

export default function RootLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>): React.JSX.Element {
  return (
    <html lang="en">
      <head>
        <title>{siteConfig.title}</title>
        <meta name="description" content={siteConfig.description} />
        <link
          rel="icon"
          href="/favicon.ico"
          type="image/x-icon"
          sizes="48x48"
        />
        <link rel="icon" href="/icon.png" type="image/png" sizes="32x32" />
      </head>
      <body className="bg-background font-sans! antialiased">
        <Providers>{children}</Providers>
        <Toaster offset={32} mobileOffset={20} />
      </body>
    </html>
  );
}
