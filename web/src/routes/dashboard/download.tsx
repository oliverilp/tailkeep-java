import { createFileRoute } from '@tanstack/react-router';
import Downloads, {
  type DownloadsSearch
} from '@/features/downloads/downloads';

function DownloadsPage() {
  return (
    <main className="mb-16 grid items-start gap-4 p-4 sm:px-8 sm:py-0 md:gap-8">
      <Downloads />
    </main>
  );
}

export const Route = createFileRoute('/dashboard/download')({
  // The router writes whatever this returns back to the URL, so defaults and
  // fallbacks live in Downloads, which keeps URLs exactly as they were entered.
  validateSearch: (search: Record<string, unknown>) =>
    search as DownloadsSearch,
  component: DownloadsPage
});
