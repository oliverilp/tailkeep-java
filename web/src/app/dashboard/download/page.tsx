// import { getDownloads } from '@/server/data/get-downloads';
// import { getQueueInfo } from '@/server/data/get-queue-info';
import NoSsrWrapper from '@/components/no-ssr-wrapper';
import Downloads from './downloads';

function DownloadsPage() {
  // const [downloads, queueInfo] = await Promise.all([
  //   getDownloads(),
  //   getQueueInfo()
  // ]);
  const queueInfo = null;
  const downloads: any[] = [];
  const dashboardData = { queueInfo, downloads };

  return (
    <main className="mb-16 grid items-start gap-4 p-4 sm:px-8 sm:py-0 md:gap-8">
      <div>Downloads page</div>
      {/* <NoSsrWrapper>
        <Downloads dashboardData={dashboardData} />
      </NoSsrWrapper> */}
    </main>
  );
}

export default DownloadsPage;
