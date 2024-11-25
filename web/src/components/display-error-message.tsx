import { CircleAlert } from 'lucide-react';

export default function ErrorMessage({ text }: { text: string }) {
  const displayText = text?.trim() ? text : "Something went wrong";
  
  return (
    <div className="self-start">
      <div className="flex items-center gap-1 text-destructive">
        <CircleAlert className="h-4 min-h-4 w-4 min-w-4" />
        <p>{displayText}</p>
      </div>
    </div>
  );
}
