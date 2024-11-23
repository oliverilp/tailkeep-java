import { NextResponse } from 'next/server';

export async function GET() {
  // Only expose specific environment variables
  const config = {
    mediaUrl: process.env.MEDIA_URL,
    apiUrl: process.env.API_URL
  };

  return NextResponse.json(config);
}
