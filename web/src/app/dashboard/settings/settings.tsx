'use client';

import React from 'react';
import { CircleAlert, Loader2 } from 'lucide-react';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle
} from '@/components/ui/card';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import {
  ChangePassword,
  changePasswordSchema
} from '@/schemas/change-password';
import { changePassword } from '@/api/users';

interface SettingsProps {
  isDemo: boolean;
}

function Settings({ isDemo }: SettingsProps) {
  const form = useForm<ChangePassword>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: {
      oldPassword: '',
      newPassword: '',
      confirmNewPassword: ''
    }
  });

  const { mutate, isPending } = useMutation({
    mutationFn: changePassword,
    onSuccess: () => {
      toast.success('Password changed successfully');
      form.reset();
    },
    onError: (error: any) => {
      if (error.response?.status >= 400) {
        toast.error(error.response.data?.message ?? 'Invalid input');
      } else {
        toast.error('Failed to change password');
      }
    }
  });

  function onSubmit(data: ChangePassword) {
    mutate(data);
  }

  return (
    <main className="mb-24 grid items-start gap-4 p-4 sm:px-8 sm:py-0 md:gap-8">
      <div className="flex w-full flex-col">
        <div className="flex flex-1 flex-col gap-4 md:gap-8">
          <div className="mx-auto grid w-full max-w-6xl gap-2">
            <h1 className="text-3xl font-semibold">Settings</h1>
          </div>
          <div className="mx-auto grid w-full max-w-6xl items-start gap-6">
            <div className="grid gap-6">
              <Card>
                <Form {...form}>
                  <form
                    className="flex flex-col gap-4"
                    onSubmit={form.handleSubmit(onSubmit)}
                  >
                    <CardHeader>
                      <CardTitle>Password</CardTitle>
                      <CardDescription>
                        Your account credentials used for signing in.
                      </CardDescription>
                    </CardHeader>
                    <CardContent className="grid gap-4">
                      {isDemo && (
                        <Alert>
                          <CircleAlert className="h-4 min-h-4 w-4 min-w-4" />
                          <AlertTitle className="font-medium">
                            App is running in demo environment!
                          </AlertTitle>
                          <AlertDescription>
                            Editing account password is disabled.
                          </AlertDescription>
                        </Alert>
                      )}

                      <FormField
                        control={form.control}
                        name="oldPassword"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Old password</FormLabel>
                            <FormControl>
                              <Input
                                placeholder="Enter your old password"
                                type="password"
                                disabled={isDemo}
                                {...field}
                              />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />

                      <FormField
                        control={form.control}
                        name="newPassword"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>New password</FormLabel>
                            <FormControl>
                              <Input
                                placeholder="Enter your new password"
                                type="password"
                                disabled={isDemo}
                                {...field}
                              />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />

                      <FormField
                        control={form.control}
                        name="confirmNewPassword"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Confirm new password</FormLabel>
                            <FormControl>
                              <Input
                                placeholder="Confirm your password"
                                type="password"
                                disabled={isDemo}
                                {...field}
                              />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                    </CardContent>
                    <CardFooter className="flex flex-col items-start border-t px-6 py-4">
                      <Button
                        className="w-16"
                        type="submit"
                        disabled={isDemo || isPending}
                      >
                        {isPending ? (
                          <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                          'Save'
                        )}
                      </Button>
                    </CardFooter>
                  </form>
                </Form>
              </Card>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}

export default Settings;
