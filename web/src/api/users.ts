import { apiClient } from '@/lib/api-client';
import type { ChangePassword } from '@/schemas/change-password';

export async function changePassword(data: ChangePassword): Promise<void> {
  await apiClient.patch('/users', {
    currentPassword: data.oldPassword,
    newPassword: data.newPassword,
    confirmationPassword: data.confirmNewPassword
  });
}