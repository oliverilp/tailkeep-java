import { Inter } from 'next/font/google';
import './globals.css';
import React from 'react';
import { cn } from '@/lib/utils';
import { Providers } from '@/components/providers';
import { Toaster } from 'sonner';

export const dynamic = 'force-dynamic';

const inter = Inter({ subsets: ['latin'] });

const siteConfig = {
  title: 'Tailkeep',
  description: 'YouTube Downloader UI'
};

export default function RootLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>): JSX.Element {
  return (
    <html lang="en">
      <head>
        <title>{siteConfig.title}</title>
        <meta name="description" content={siteConfig.description} />
        <link
          rel="icon"
          href="/icon?<generated>"
          type="image/png"
          sizes="32x32"
        />
      </head>
      <body
        className={cn('bg-background font-sans antialiased', inter.className)}
      >
        <Providers>{children}</Providers>
        <Toaster />
      </body>
    </html>
  );
}
