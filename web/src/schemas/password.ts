import { z } from 'zod';

export const passwordSchema = z
  .string()
  .min(10, { message: 'Password must be at least 10 characters.' })
  .max(100, { message: 'Maximum password length is 100 characters.' });
