# Deloitte Graduate Hiring Assessment

**Full Name:** Suryavikram K
**Email ID:** suryavikramkrishnanr@gmail.com
**College Name:** Sona College of Technology
**Selected Skill Track:** Java & API Development

---

## 🌐 Live Demo

> **Deployed on Railway** — click the links below to access the live application:

| Endpoint | Link |
|----------|------|
| 🖥️ Frontend UI | [View App](https://deloitte-library-api.up.railway.app/) |
| 📚 Books API | [GET /books](https://deloitte-library-api.up.railway.app/books) |
| 💚 Health Check | [GET /health](https://deloitte-library-api.up.railway.app/health) |
| 📊 Statistics | [GET /books/stats](https://deloitte-library-api.up.railway.app/books/stats) |
| 🕑 Borrow History | [GET /books/history](https://deloitte-library-api.up.railway.app/books/history) |

[![Deploy on Railway](https://railway.app/button.svg)](https://railway.app/new/template)

---

## Project Title
Library Management System – Enhanced REST API (Java)

## Description
A production-quality REST API built using core Java (`com.sun.net.httpserver`) with 8 professional enhancements: input validation, search & filter, pagination, borrow history tracking, health check endpoint, statistics, structured logging, and standardised JSON wrapper responses.

## Technologies Used
- Java (Core – no external frameworks or dependencies)
- `com.sun.net.httpserver` (Built-in Java HTTP Server)
- Object-Oriented Programming (7 classes)
- REST API Design Principles
- JSON serialization (manual)
- LocalDateTime for timestamps

## Enhancements Implemented

| # | Enhancement | Details |
|---|-------------|---------|
| 1 | Input Validation | Rejects empty/null fields on POST with HTTP 400 |
| 2 | Search & Filter | Filter by genre, author (partial), availability |
| 3 | Pagination | page/size query params + pagination metadata in response |
| 4 | Borrow History | Tracks borrower name + borrow/return timestamps |
| 5 | Health Check | GET /health returns service status |
| 6 | Statistics | GET /books/stats returns totals and genres |
| 7 | Structured Logging | Timestamped console log per request |
| 8 | JSON Wrapper | Standard {status, data, count} format for all responses |

## Project Structure

```
deloitte-library-api/
├── src/
│   ├── Main.java           # Server entry point
│   ├── Book.java           # Book model
│   ├── BorrowRecord.java   # Borrow history model
│   ├── BookRepository.java # Data layer (CRUD + search + stats)
│   ├── BookHandler.java    # HTTP request handler
│   ├── HealthHandler.java  # Health check endpoint
│   ├── FrontendHandler.java# Frontend HTML file server
│   ├── JsonUtil.java       # JSON serialization helpers
│   └── Logger.java         # Structured logging utility
├── web/
│   └── index.html          # Frontend UI (dark-themed dashboard)
├── Dockerfile              # Docker build for Railway deployment
├── run.sh                  # Local build & run script
└── README.md
```

## API Endpoints

### Book Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /books | List all books (supports filter + pagination) |
| GET | /books/{id} | Get a book by ID |
| POST | /books | Add a new book (with validation) |
| DELETE | /books/{id} | Delete a book |
| PUT | /books/{id}/checkout | Checkout a book (send borrowerName in body) |
| PUT | /books/{id}/return | Return a book |
| GET | /books/stats | Library statistics |
| GET | /books/history | Full borrow history |
| GET | /books/{id}/history | Borrow history for a specific book |

### System Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /health | Health check |

## Query Parameters (GET /books)
| Parameter | Example | Description |
|-----------|---------|-------------|
| genre | ?genre=Technology | Filter by genre (case-insensitive) |
| author | ?author=Martin | Filter by author (partial match) |
| available | ?available=true | Filter by availability |
| page | ?page=1 | Page number (default: 1) |
| size | ?size=5 | Page size (default: 10) |

## Sample Requests & Responses

### POST /books
**Request body:**
```json
{"title":"Effective Java","author":"Joshua Bloch","genre":"Technology"}
```
**Response (201):**
```json
{"status":"success","data":{"id":7,"title":"Effective Java","author":"Joshua Bloch","genre":"Technology","available":true}}
```

### GET /books?genre=Technology&page=1&size=2
**Response:**
```json
{
  "status": "success",
  "count": 2,
  "data": [...],
  "pagination": {"page":1,"size":2,"totalItems":4,"totalPages":2}
}
```

### GET /health
```json
{"status":"healthy","service":"Library Management API","version":"2.0.0","timestamp":"...","startedAt":"..."}
```

### GET /books/stats
```json
{"status":"success","data":{"totalBooks":6,"availableBooks":5,"checkedOutBooks":1,"totalBorrowRecords":1,"genres":["Technology","Fiction","Self-Help"]}}
```

### PUT /books/1/checkout
**Request body:**
```json
{"borrowerName":"Suryavikram"}
```
**Response:**
```json
{"status":"success","message":"Book checked out successfully by Suryavikram"}
```

## How to Run on Replit
1. Go to https://replit.com and create a **Java** Repl
2. Create a `src/` folder and add all `.java` files
3. Click **Run**
4. Server starts on port **8080**
5. Use the Replit webview URL to test endpoints

## Console Log Sample
```
=========================================
  Library Management API v2.0 - RUNNING
  Server started on port 8080
=========================================
[2026-03-09 10:15:32] GET /books -> 200 OK
[2026-03-09 10:15:45] POST /books -> 201 Created
[2026-03-09 10:15:50] GET /books/99 -> 404 Not Found
[2026-03-09 10:16:00] GET /health -> 200 OK
```

## Deployment

### Railway (Live)
This project is deployed on [Railway](https://railway.app) using Docker.

- **Live URL:** https://deloitte-library-api.up.railway.app/
- Auto-deploys on every push to `main`
- Uses `Dockerfile` for containerised deployment
- `PORT` environment variable is injected by Railway automatically

### Run Locally
```bash
chmod +x run.sh
./run.sh
```
Then open http://localhost:8080 in your browser.
