# HttpServer

A hand-built HTTP/1.x server in Java, implemented from scratch on top of raw TCP sockets — no Netty, no Servlet containers, no Spring. The server parses raw HTTP byte streams, routes requests to typed handlers, and serializes responses back over the wire. Includes a thread pool for concurrent connections, structured request/response logging, and support for JSON, form-encoded, and multipart file upload bodies.

---

## Features

- **Raw socket HTTP parsing** — reads and parses the request line, headers, and body directly from a `Socket`'s `InputStream`, byte by byte, with correct `\r\n` handling
- **Full HTTP/1.x request/response model** — typed `HttpRequest`, `HttpResponse`, `HttpRequestLine`, `HttpResponseLine`, and `HttpHeaders` classes
- **Thread pool** — 50-thread fixed pool via `ExecutorService`; each accepted connection is dispatched without blocking the accept loop
- **Path-based routing** — `HttpRequestDelegator` maps URL paths to `HttpRequestHandler` implementations
- **In-memory key-value store** — thread-safe `ConcurrentHashMap` exposed over both JSON and form-encoded APIs
- **File upload & download** — multipart/form-data parsing for upload; binary file serving with `Content-Disposition` header for download
- **Custom header serialization** — `HttpHeaderKey` enum auto-formats enum names to proper HTTP header casing (`CONTENT_TYPE` → `Content-Type`)
- **Structured logging** — SLF4J + Log4j2; requests and responses logged as formatted JSON via a custom `BodySerializer` that renders `byte[]` fields as readable strings
- **Query string parsing** — URL-decoded query parameters extracted from the request line
- **Sample HTTP requests** — ready-to-run `.http` files for every endpoint (IntelliJ HTTP Client compatible)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Transport | Raw TCP (`java.net.ServerSocket` / `Socket`) |
| Concurrency | `ExecutorService` fixed thread pool (50 threads) |
| JSON | Jackson 2.20 |
| Logging | SLF4J + Log4j2 |
| Build | Maven |
| Utilities | Lombok |

---

## Getting Started

### Prerequisites

- Java 21+
- Maven

### Build & Run

```bash
mvn clean package
java -jar target/HttpServer-1.0-SNAPSHOT.jar
```

The server starts on **port 8000**.

Create the files directory for file upload/download endpoints:

```bash
mkdir files
```

---

## API Reference

### Key-Value Map — Form Encoded

| Method | Path | Description |
|---|---|---|
| `POST` | `/map/post/form` | Insert key-value pairs (`application/x-www-form-urlencoded`) |
| `GET` | `/map/get/form?key=<key>` | Retrieve a value by key (plain text response) |
| `GET` | `/map/get/all/form` | Retrieve all entries as `key=value` lines |

**Example — insert:**
```http
POST http://localhost:8000/map/post/form
Content-Type: application/x-www-form-urlencoded

foo=bar&foo1=bar1&foo2=bar2
```

**Example — get:**
```http
GET http://localhost:8000/map/get/form?key=foo
```

---

### Key-Value Map — JSON

| Method | Path | Description |
|---|---|---|
| `POST` | `/map/post/json` | Insert key-value pairs (JSON body) |
| `GET` | `/map/get/json` | Retrieve a value by key (JSON body with `"key"` field) |
| `GET` | `/map/get/all/json` | Retrieve all entries as a JSON object |

**Example — insert:**
```http
POST http://localhost:8000/map/post/json
Content-Type: application/json

{
  "foo": "bar",
  "foo1": "bar1"
}
```

**Example — get:**
```http
GET http://localhost:8000/map/get/json
Content-Type: application/json

{ "key": "foo" }
```

---

### File Upload & Download

| Method | Path | Description |
|---|---|---|
| `POST` | `/file/post` | Upload a file (`multipart/form-data`); saved to `./files/` |
| `GET` | `/file/get?filename=<name>` | Download a file from `./files/` as `application/octet-stream` |

**Example — upload:**
```http
POST http://localhost:8000/file/post
Content-Type: multipart/form-data; boundary=WebAppBoundary

--WebAppBoundary
Content-Disposition: form-data; name="field-name"; filename="notes.txt"

< /path/to/notes.txt
--WebAppBoundary--
```

**Example — download:**
```http
GET http://localhost:8000/file/get?filename=notes.txt
```

---

## How It Works

### Request Parsing

Each incoming connection is handled on a thread pool thread. `HttpRequest` reads the socket's `InputStream` directly:

1. Reads the request line byte by byte, handling both `\n` and `\r\n` line endings
2. Reads header lines until a blank line is encountered
3. Reads the body using the `Content-Length` header to know exactly how many bytes to consume

### Header Handling

`HttpHeaders.HttpHeaderKey` is an enum that maps to standard HTTP header names. The enum `toString()` converts `CONTENT_TYPE` → `Content-Type` automatically. Parsing does the reverse via `fromString()`.

### Routing

`HttpRequestDelegator` holds a static `Map<String, HttpRequestHandler>` keyed by URL path. Each handler implements the `HttpRequestHandler` interface and is responsible for method validation, body parsing, and response construction.

### Multipart Parsing

`SinglePartRequestBody` implements a pure-Java multipart boundary splitter. It scans the raw body byte array for boundary markers, splits parts, and parses each part's sub-headers independently — no external libraries.

### Logging

Requests and responses are serialized to JSON via a custom `BodySerializerModifier` that intercepts Jackson's serialization pipeline and renders `byte[]` body fields as UTF-8 strings instead of Base64, making logs human-readable.

---

## Project Structure

```
src/main/java/org/pavl/
├── Main.java                        # Entry point — starts server on port 8000
├── HttpServer.java                  # ServerSocket accept loop + thread pool dispatch
├── HttpRequest.java                 # Raw stream → parsed request
├── HttpRequestLine.java             # Method, path, query string parsing
├── HttpHeaders.java                 # Typed header map + HttpHeaderKey enum
├── HttpResponse.java                # Response record (line + headers + body)
├── HttpResponseLine.java            # Status line record
├── HttpRequestDelegator.java        # Path-based routing
├── SinglePartRequestBody.java       # Multipart/form-data parser
├── HttpUtils.java                   # Content type constants + request type helpers
├── HttpMethod.java                  # HTTP method enum
├── config/
│   ├── BodySerializer.java          # Jackson byte[] → UTF-8 string serializer
│   └── BodySerializerModifier.java  # Registers BodySerializer for "body" fields
└── handler/
    ├── HttpRequestHandler.java      # Interface + static response factory methods
    ├── file/
    │   ├── FileGetHandler.java      # Serve files from ./files/
    │   └── FilePostHandler.java     # Save multipart uploads to ./files/
    └── map/
        ├── MapGetFormHandler.java
        ├── MapGetJsonHandler.java
        ├── MapGetAllFormHandler.java
        ├── MapGetAllJsonHandler.java
        ├── MapPostFormHandler.java
        └── MapPostJsonHandler.java

requests/                            # Sample .http files (IntelliJ HTTP Client)
├── FileGet.http
├── FilePost.http
├── MapGetAllForm.http
├── MapGetAllJson.http
├── MapGetForm.http
├── MapGetJson.http
├── MapPostForm.http
└── MapPostJson.http
```

---

## Author

**Giorgi Pavliashvili**  
[LinkedIn](https://www.linkedin.com/in/giorgi-pavliashvili-6718861b6/)
