import React, { Fragment } from 'react';
import { Link, useLocation } from '@tanstack/react-router';
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator
} from '@/components/ui/breadcrumb';
import { capitalize } from '@/lib/utils';
import type { FileRouteTypes } from '@/routeTree.gen';

interface BreadcrumbLink {
  path: string;
  name: string;
}

function BreadcrumbLinks() {
  const pathname = useLocation({ select: (location) => location.pathname });
  const segments = pathname.split('/').filter((segment) => segment);

  const breadcrumbs: BreadcrumbLink[] = [];
  let currentPath = '';

  for (const segment of segments) {
    currentPath += `/${segment}`;
    const isUUID = segment.includes('-');
    const name = isUUID ? segment : capitalize(segment);
    breadcrumbs.push({ path: currentPath, name: name });
  }

  return (
    <Breadcrumb className="hidden sm:flex">
      <BreadcrumbList>
        {breadcrumbs.map((breadcrumb, index) => (
          <Fragment key={breadcrumb.path}>
            <BreadcrumbItem>
              {index === breadcrumbs.length - 1 ? (
                <BreadcrumbPage>{breadcrumb.name}</BreadcrumbPage>
              ) : (
                <BreadcrumbLink asChild>
                  <Link to={breadcrumb.path as FileRouteTypes['to']}>
                    {breadcrumb.name}
                  </Link>
                </BreadcrumbLink>
              )}
            </BreadcrumbItem>
            {index < breadcrumbs.length - 1 && (
              <BreadcrumbSeparator key={breadcrumb.path + 'divider'} />
            )}
          </Fragment>
        ))}
      </BreadcrumbList>
    </Breadcrumb>
  );
}

export default BreadcrumbLinks;
