import DownloadsTable from './table';
import TablePagination from './table-pagination';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle
} from '@/components/ui/card';
import { DownloadsPageResponse } from '@/schemas/downloads-dashboard';

function DownloadsCard({
  downloads,
  progress
}: {
  downloads: DownloadsPageResponse;
  progress: string;
}) {
  const { items, totalItems, totalPages, currentPage, pageSize } = downloads;

  const start = pageSize * (currentPage - 1) + 1;
  const end = Math.min(pageSize * currentPage, totalItems);

  return (
    <Card>
      <CardHeader>
        <CardTitle>Downloads</CardTitle>
        <CardDescription>
          Manage your YouTube video download history.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <DownloadsTable items={items} />
      </CardContent>
      <CardFooter className="flex flex-col items-start justify-between gap-4 lg:flex-row lg:items-center">
        <div className="w-full sm:w-fit">
          {totalItems > pageSize && (
            <TablePagination downloads={downloads} progress={progress} />
          )}
        </div>
        <div className="text-xs text-muted-foreground">
          Showing{' '}
          <strong>
            {start}-{end}
          </strong>{' '}
          of <strong>{totalItems}</strong> downloads
        </div>
      </CardFooter>
    </Card>
  );
}

export default DownloadsCard;
