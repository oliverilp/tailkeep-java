'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2, Plus } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormMessage
} from '@/components/ui/form';
import {
  Card,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle
} from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { startDownload } from '@/api/downloads';
import { toast } from 'sonner';
import { AxiosError } from 'axios';

const formSchema = z.object({
  url: z.string().url({
    message: 'Must be a valid YouTube video URL.'
  })
});

type FormType = z.infer<typeof formSchema>;

function AddVideo() {
  const queryClient = useQueryClient();

  const { mutate: addVideo, isPending } = useMutation({
    mutationFn: (data: FormType) => startDownload(data.url),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['downloads-dashboard'] });
      toast.success('Download added to queue successfully');
    },
    onError: (error: AxiosError<any>) => {
      toast.error('Failed to add download to queue', {
        description: error.response?.data.message,
        duration: 7000
      });
    }
  });

  const form = useForm<FormType>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      url: ''
    }
  });

  function onSubmit(data: FormType): void {
    addVideo(data);
    form.reset();
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)}>
        <Card className="sm:col-span-2">
          <CardHeader className="pb-3">
            <CardTitle>Add Video</CardTitle>
            <CardDescription className="max-w-lg text-balance leading-relaxed">
              Enter the YouTube link to queue your video for download.
            </CardDescription>
          </CardHeader>
          <CardFooter className="flex items-start gap-3 sm:w-full">
            <div className="w-full max-w-[515px]">
              <FormField
                control={form.control}
                name="url"
                render={({ field }) => (
                  <FormItem>
                    <FormControl>
                      <Input
                        className="w-full"
                        placeholder="https://www.youtube.com"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
            {isPending ? (
              <Button disabled>
                <Loader2 className="h-5 w-5 animate-spin" />
              </Button>
            ) : (
              <Button type="submit">
                <Plus className="h-5 w-5" />
              </Button>
            )}
          </CardFooter>
        </Card>
      </form>
    </Form>
  );
}

export default AddVideo;
