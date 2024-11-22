import { CircleAlert } from 'lucide-react';

export default function ErrorMessage({ text }: { text: string }) {
  return (
    <div className="self-start">
      <div className="flex items-center gap-1 text-destructive">
        <CircleAlert className="h-4 min-h-4 w-4 min-w-4" />
        <p>{text}</p>
      </div>
    </div>
  );
}
