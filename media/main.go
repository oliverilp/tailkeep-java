package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"time"
)

const (
	mediaDir = "/home/oliver/Videos"
	port     = ":5000"
	apiURL   = "http://10.0.0.30:8080/api/v1/videos/%s"
)

type FileResponse struct {
	Filename string `json:"filename"`
}

type ErrorResponse struct {
	Path       string     `json:"path"`
	Message    string     `json:"message"`
	StatusCode int        `json:"statusCode"`
	Timestamp  *time.Time `json:"timestamp"`
}

func writeError(w http.ResponseWriter, path, message string, statusCode int) {
	timestamp := time.Now()
	errorResp := ErrorResponse{
		Path:       path,
		Message:    message,
		StatusCode: statusCode,
		Timestamp:  &timestamp,
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(statusCode)
	json.NewEncoder(w).Encode(errorResp)
}

func serveMedia(w http.ResponseWriter, r *http.Request) {
	log.Printf("Received request from: %s", r.RemoteAddr)

	uuid := filepath.Base(r.URL.Path)

	// Get authorization header from the original request
	authHeader := r.Header.Get("Authorization")
	if authHeader == "" {
		writeError(w, r.URL.Path, "Authentication is required to access this resource", http.StatusUnauthorized)
		return
	}

	// Create request to Java API
	apiReq, err := http.NewRequest("GET", fmt.Sprintf(apiURL, uuid), nil)
	if err != nil {
		writeError(w, r.URL.Path, "Internal server error", http.StatusInternalServerError)
		log.Printf("Failed to create request: %v", err)
		return
	}

	// Forward the authorization header
	apiReq.Header.Set("Authorization", authHeader)

	// Make the request to Java API
	client := &http.Client{}
	resp, err := client.Do(apiReq)
	if err != nil {
		writeError(w, r.URL.Path, "Failed to fetch file info", http.StatusInternalServerError)
		log.Printf("API request failed: %v", err)
		return
	}
	defer resp.Body.Close()

	// Check API response status
	if resp.StatusCode != http.StatusOK {
		writeError(w, r.URL.Path, "Failed to fetch file info", resp.StatusCode)
		log.Printf("API returned status: %d", resp.StatusCode)
		return
	}

	// Parse the response
	var fileInfo FileResponse
	if err := json.NewDecoder(resp.Body).Decode(&fileInfo); err != nil {
		writeError(w, r.URL.Path, "Failed to parse file info", http.StatusInternalServerError)
		log.Printf("Failed to parse response: %v", err)
		return
	}

	// Construct full path to file using the real filename
	filePath := filepath.Join(mediaDir, fileInfo.Filename)

	// Check if file exists
	if _, err := os.Stat(filePath); os.IsNotExist(err) {
		writeError(w, r.URL.Path, "File not found", http.StatusNotFound)
		return
	}

	http.ServeFile(w, r, filePath)
}

func main() {
	// Create media directory if it doesn't exist
	if err := os.MkdirAll(mediaDir, 0755); err != nil {
		log.Fatalf("Failed to create media directory: %v", err)
	}

	http.HandleFunc("/media/", serveMedia)

	log.Printf("Starting server on port %s", port)
	if err := http.ListenAndServe(port, nil); err != nil {
		log.Fatalf("Server failed to start: %v", err)
	}
}
