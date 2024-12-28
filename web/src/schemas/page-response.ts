import { z } from 'zod';

// Generic page response schema that takes any schema as its item type
export const createPageResponseSchema = <T extends z.ZodType>(itemSchema: T) =>
  z.object({
    items: itemSchema.array(),
    totalItems: z.number(),
    totalPages: z.number(),
    currentPage: z.number(),
    pageSize: z.number(),
    hasNext: z.boolean(),
    hasPrevious: z.boolean()
  });
