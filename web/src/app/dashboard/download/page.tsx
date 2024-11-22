import NoSsrWrapper from '@/components/no-ssr-wrapper';
import Downloads from './downloads';

function DownloadsPage() {
  return (
    <main className="mb-16 grid items-start gap-4 p-4 sm:px-8 sm:py-0 md:gap-8">
      <NoSsrWrapper>
        <Downloads />
      </NoSsrWrapper>
    </main>
  );
}

export default DownloadsPage;
