'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import {
  Delete,
  SquarePlay,
  Link as LinkIcon,
  MoreHorizontal
} from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu';
import { TableCell, TableRow } from '@/components/ui/table';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle
} from '@/components/ui/alert-dialog';
import { toast } from 'sonner';
import { AxiosError } from 'axios';
import { useQueryClient, useMutation } from '@tanstack/react-query';

import type { DownloadProgressDto } from '@/schemas/progress';
import { deleteDownload } from '@/api/downloads';
import { ApiError } from 'next/dist/server/api-utils';

function formatDate(date: Date): string {
  const options: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  };
  const locale = navigator.language ?? 'en-GB';

  const datePart = date.toLocaleDateString(locale, options);
  const timePart = date.toLocaleTimeString(locale);

  return `${datePart} ${timePart}`;
}

function formatStatus(status: string | null): string {
  if (status === null) {
    return '';
  }
  return status.toLowerCase();
}

interface DeleteDialogProps {
  open: boolean;
  setOpen: React.Dispatch<React.SetStateAction<boolean>>;
  item: DownloadProgressDto;
}

function DeleteDialog({ open, setOpen, item }: DeleteDialogProps) {
  const queryClient = useQueryClient();

  const { mutate: deleteItem } = useMutation({
    mutationFn: () => deleteDownload(item.id),
    onSuccess: () => {
      setOpen(false);
      queryClient.invalidateQueries({ queryKey: ['downloads-dashboard'] });
      toast.success('Download history deleted successfully');
    },
    onError: (error: AxiosError<ApiError>) => {
      console.error('Failed to delete download:', error);
      toast.error('Failed to delete download history', {
        description: error.response?.data.message,
        duration: 7000
      });
    }
  });

  return (
    <AlertDialog open={open} onOpenChange={setOpen}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
          <AlertDialogDescription>
            This action cannot be undone. This will permanently delete the
            download progress history for &quot;{item.video.title}&quot;. The
            video file itself will not be affected.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Cancel</AlertDialogCancel>
          <AlertDialogAction
            variant={'destructive'}
            onClick={() => deleteItem()}
          >
            Delete
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}

function DownloadsTableRow({ item }: { item: DownloadProgressDto }) {
  const [open, setOpen] = useState(false);

  return (
    <>
      <DeleteDialog open={open} setOpen={setOpen} item={item} />
      <TableRow>
        <TableCell className="hidden sm:table-cell">
          <Link href={`/dashboard/video/${item.video.id}`} className="">
            <Image
              alt={item.video.title}
              className="aspect-video rounded-md object-cover"
              width="80"
              height="45"
              src={item.video.thumbnailUrl}
            />
          </Link>
        </TableCell>
        <TableCell className="font-medium">{item.video.title}</TableCell>
        <TableCell className="hidden lg:table-cell">
          <Badge variant="outline">{formatStatus(item.status)}</Badge>
        </TableCell>
        <TableCell>{item.progress}%</TableCell>
        <TableCell className="hidden md:table-cell">{item.speed}</TableCell>
        <TableCell className="hidden md:table-cell">{item.eta}</TableCell>
        <TableCell className="hidden md:table-cell">{item.size}</TableCell>
        <TableCell className="hidden xl:table-cell">
          {formatDate(item.createdAt)}
        </TableCell>
        <TableCell>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button aria-haspopup="true" size="icon" variant="ghost">
                <MoreHorizontal className="h-4 w-4" />
                <span className="sr-only">Toggle menu</span>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuLabel>Actions</DropdownMenuLabel>
              <Link href={`/dashboard/video/${item.video.id}`} className="">
                <DropdownMenuItem
                  onClick={() => navigator.clipboard.writeText(item.video.url)}
                >
                  <SquarePlay className="mr-2 h-4 w-4" />
                  <span>Open video</span>
                </DropdownMenuItem>
              </Link>

              <DropdownMenuItem
                onClick={() => navigator.clipboard.writeText(item.video.url)}
              >
                <LinkIcon className="mr-2 h-4 w-4" />
                <span>Copy YouTube URL</span>
              </DropdownMenuItem>
              <DropdownMenuSeparator />

              <DropdownMenuItem onClick={() => setOpen(true)}>
                <Delete className="mr-2 h-4 w-4" />
                <span>Delete</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </TableCell>
      </TableRow>
    </>
  );
}

export default DownloadsTableRow;
