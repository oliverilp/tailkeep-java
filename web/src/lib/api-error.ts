// Mirrors exception/ApiError.java in the API.
export interface ApiError {
  path: string;
  message: string;
  statusCode: number;
  timestamp: string;
}
