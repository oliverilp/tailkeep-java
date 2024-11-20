import Login from './login';

function LoginPage() {
  const isDemo = process.env.DEMO_MODE === 'true';

  return (
    <div className="h-full w-full">
      <Login isDemo={isDemo} />
    </div>
  );
}

export default LoginPage;
