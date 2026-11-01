import { createFileRoute } from '@tanstack/react-router';
import Login from '@/features/login/login';

function LoginPage() {
  const { config } = Route.useRouteContext();

  return (
    <div className="h-full w-full">
      <Login isDemo={config.demoMode} />
    </div>
  );
}

export const Route = createFileRoute('/login')({
  component: LoginPage
});
