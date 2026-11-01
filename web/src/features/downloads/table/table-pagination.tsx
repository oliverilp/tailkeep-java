import React from 'react';
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious
} from '@/components/ui/pagination';
import { DownloadsPageResponse } from '@/schemas/downloads-dashboard';
import type { DownloadsTab } from '../downloads';

interface TablePaginationProps {
  downloads: DownloadsPageResponse;
  progress: DownloadsTab;
}

type Page = number | string;

function getPages(totalPages: number, page: number): Page[] {
  const pages: Page[] = [];
  const ellipsis = '...';
  const totalPaginationElements = 8;
  const maxVisiblePages = 5;
  const isNearStart = page <= 4;
  const isNearEnd = page >= totalPages - 3;

  if (totalPages <= totalPaginationElements) {
    for (let i = 1; i <= totalPages; i++) {
      pages.push(i);
    }
  } else {
    if (isNearStart) {
      for (let i = 1; i <= 6; i++) {
        pages.push(i);
      }
      pages.push(ellipsis);
      pages.push(totalPages);
    } else if (isNearEnd) {
      pages.push(1);
      pages.push(ellipsis);
      for (let i = totalPages - maxVisiblePages; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      pages.push(1);
      pages.push(ellipsis);
      for (let i = page - 1; i <= page + 2; i++) {
        pages.push(i);
      }
      pages.push(ellipsis);
      pages.push(totalPages);
    }
  }

  return pages;
}

function TablePagination({ downloads, progress }: TablePaginationProps) {
  const { currentPage, totalPages, hasPrevious, hasNext } = downloads;
  const pages = getPages(totalPages, currentPage);

  return (
    <div>
      <Pagination>
        <PaginationContent className="w-full justify-between sm:w-fit lg:items-center lg:justify-normal">
          <PaginationItem>
            <PaginationPrevious
              disabled={!hasPrevious}
              to="/dashboard/download"
              search={{ progress, page: currentPage - 1 }}
            />
          </PaginationItem>

          <div className="sm:hidden">{`Page: ${currentPage}/${totalPages}`}</div>

          {pages.map((item, index) => {
            if (item === '...') {
              return (
                <PaginationItem
                  className="hidden sm:flex"
                  key={`ellipsis-${index}`}
                >
                  <PaginationEllipsis />
                </PaginationItem>
              );
            } else {
              return (
                <PaginationItem className="hidden sm:flex" key={item}>
                  <PaginationLink
                    to="/dashboard/download"
                    search={{ progress, page: Number(item) }}
                    isActive={item === currentPage}
                  >
                    {item}
                  </PaginationLink>
                </PaginationItem>
              );
            }
          })}
          <PaginationItem>
            <PaginationNext
              disabled={!hasNext}
              to="/dashboard/download"
              search={{ progress, page: currentPage + 1 }}
            />
          </PaginationItem>
        </PaginationContent>
      </Pagination>
    </div>
  );
}

export default TablePagination;
