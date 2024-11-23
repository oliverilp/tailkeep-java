import { NextResponse } from 'next/server';

export async function GET() {
  const config = {
    mediaUrl: process.env.MEDIA_URL,
    apiUrl: process.env.API_URL
  };

  return NextResponse.json(config);
}
