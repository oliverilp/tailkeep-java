'use client';

import React from 'react';
import { CircleAlert, Loader2 } from 'lucide-react';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';

import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle
} from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { type Login as LoginType, loginSchema } from '@/schemas/login';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage
} from '@/components/ui/form';
import { useLogin } from '@/lib/use-auth';
import ErrorMessage from '@/components/display-error-message';
import { AxiosError } from 'axios';

interface LoginProps {
  isDemo: boolean;
}

function Login({ isDemo }: LoginProps) {
  const { mutate: login, isPending, error } = useLogin();

  const form = useForm<LoginType>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: '',
      password: ''
    }
  });

  function onSubmit(data: LoginType): void {
    console.log('Login username:', data.username);
    login(data);
  }

  return (
    <main className="grid h-screen w-full items-center justify-center">
      <Card className=" h-fit w-[90vw]  sm:w-[500px]">
        <CardHeader>
          <CardTitle className="text-2xl">Login</CardTitle>
        </CardHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)}>
            <CardContent className="grid gap-4">
              {isDemo && (
                <Alert>
                  <CircleAlert className="h-4 min-h-4 w-4 min-w-4" />
                  <AlertTitle className="font-medium">
                    App is running in demo environment!
                  </AlertTitle>
                  <AlertDescription>
                    <div className="mt-3 flex flex-col gap-1">
                      <span className="text-sm font-medium leading-none">
                        Username is demo
                      </span>
                      <span className="text-sm font-medium leading-none">
                        Password is Demo1Demo1
                      </span>
                    </div>
                  </AlertDescription>
                </Alert>
              )}

              <FormField
                control={form.control}
                name="username"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Username</FormLabel>
                    <FormControl>
                      <Input
                        type="text"
                        placeholder="Enter your username"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Password</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="Enter your password"
                        type="password"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </CardContent>
            <CardFooter className="flex flex-col gap-3">
              {isPending ? (
                <Button className="w-full" disabled>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Please wait
                </Button>
              ) : (
                <Button className="w-full">Sign in</Button>
              )}
              {error && (
                <ErrorMessage
                  text={(error as AxiosError<any>).response?.data.message}
                />
              )}
            </CardFooter>
          </form>
        </Form>
      </Card>
    </main>
  );
}

export default Login;
