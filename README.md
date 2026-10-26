# Student Grade Management System (Enterprise Full-Stack)

A complete, persistent full-stack Student Grade Tracker built with **Java 21**, **Spring Boot 3.3.5**, and **React 18 / Vite 5**.

The application is engineered for **Single-Origin Deployment**, where the compiled React Single-Page Application (SPA) is served directly by the Spring Boot backend on a single public URL.

---

## 🌟 Key Features

1. **Single-Origin Unified Deployment**:
   - Spring Boot serves the production React frontend bundle from `classpath:/static/` on a single application port.
   - Client-side navigation (`/`, `/students`, `/reports`, `/analytics`, `/import`, `/settings`) supports direct browser entry, refreshes, and history traversal via a non-intrusive SPA fallback controller.
   - Dedicated isolation preserves REST API routing (`/api/**`), OpenAPI documentation (`/v3/api-docs`, `/swagger-ui/**`), and container health probes (`/api/health`, `/health`, `/actuator/health`).

2. **Student & Marks Management**:
   - Complete CRUD (Add, Edit, View, and Delete with modal confirmation).
   - Search by Student ID or Student Name.
   - Filter by Class/Section, Letter Grade (A/B/C/D/F), and Pass/Fail status.
   - Dynamic sorting by Rank, Marks, Percentage, Name, Student ID, or Class.
   - Dual Grading Modes: Overall Marks (0–100) and Subject-Wise Breakdown with individual passing benchmarks and compulsory failure policy.

3. **Dense Ranking Engine**:
   - Equal marks receive identical ranks (e.g., marks `99, 99` receive Rank 1).
   - The next distinct score receives the next consecutive rank without skipping (e.g., `95` receives Rank 2, `90` receives Rank 3, `85` receives Rank 4).
   - Consistent dense ranking across the Student Directory, Dashboard Leaderboards, Class Reports, and Exported Files.

4. **Bulk CSV and Excel (.xlsx) Import Wizard**:
   - 3-step import wizard: Upload → Interactive Preview & Row Validation → Persist Summary.
   - Validation against empty fields, non-numeric values, and out-of-range marks (0–100).
   - In-file and database-level duplicate Student ID detection.
   - Granular feedback: duplicate vs. invalid rejected counts.
   - CSV export of rejected/invalid rows with specific error reasons.
   - Formula injection sanitization (OWASP recommendations).

5. **Real-Time Dashboard & Analytics**:
   - Dynamic metrics: Total Enrolled, Class Average, Highest Score & Student, Lowest Score & Student, Pass Percentage.
   - Visual Charts: Letter Grade Distribution Spectrum and Pass vs. Fail Ratio.
   - Real-time Top Performers Leaderboard with dense rank badges.
   - Synchronous metric recalculation whenever students are added, edited, imported, or deleted.

6. **Official Reports & Exports**:
   - Individual student academic transcripts.
   - Comprehensive class-wide grade reports.
   - Formatted Excel (.xlsx) report export (`/api/export/excel`) with color-coded pass/fail indicators.
   - CSV data export (`/api/export/csv`) with formula injection sanitization.
   - Clean printable PDFs for class transcripts (`/api/export/pdf/class`) and student report cards (`/api/export/pdf/student/{id}`) via OpenPDF.

7. **Flexible Production Persistence**:
   - **PostgreSQL**: Cloud-native support with automatic `DATABASE_URL` parsing (e.g. Render, Railway, Heroku).
   - **H2 File Engine**: Embedded disk-backed database (`./data/studentgradedb.mv.db`) for standalone local deployments.
   - Non-destructive schema management (`spring.jpa.hibernate.ddl-auto=update`).
   - Test isolation using in-memory H2 (`jdbc:h2:mem:testdb`), preserving production records intact.

---

## 📋 System Prerequisites

Ensure the following tools are installed and available in your system `PATH`:

- **Java Development Kit (JDK)**: Version 21 or higher (Verified with Java 21 / Java 25)
  - Verify: `java -version`
- **Apache Maven**: Version 3.8 or higher
  - Verify: `mvn -version`
- **Node.js**: Version 18.x or higher & **npm** (v9+)
  - Verify: `node -v` and `npm -v`
- *(Optional for Container Deployment)* **Docker**: Version 20.x or higher

---

## 🛠️ Local Development Setup

During active feature development, you can run the backend and frontend independently with Hot Module Replacement (HMR).

### 1. Start the Spring Boot Backend

Open a terminal:

```cmd
cd StudentGradeTracker\backend
mvn spring-boot:run
```

- **Backend Port**: `8080`
- **REST API Base URL**: [http://localhost:8080/api](http://localhost:8080/api)
- **Swagger / OpenAPI Documentation**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **H2 Database Web Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

### 2. Start the React Frontend

Open a second terminal:

```cmd
cd StudentGradeTracker\frontend
npm install
npm run dev
```

- **Frontend Application URL**: [http://localhost:3000](http://localhost:3000)
- The Vite development server automatically proxies API requests (`/api/*`) to `http://localhost:8080`.

---

## 📦 Building and Running the Packaged Production Application

In production, the compiled frontend assets are embedded directly inside the Spring Boot JAR, serving the entire application from a single port.

### Step 1: Build the Frontend Production Bundle
```cmd
cd StudentGradeTracker\frontend
npm install
npm run build
```
*Outputs compiled assets to `frontend/dist/`.*

### Step 2: Build the Production JAR
```cmd
cd StudentGradeTracker\backend
mvn package -DskipTests
```
*Maven copies `frontend/dist/` into `target/classes/static/` and packages `target/student-grade-tracker-1.0.0.jar`.*

### Step 3: Run the Packaged JAR
```cmd
# Runs on default port 8080 or the PORT environment variable
java -jar target/student-grade-tracker-1.0.0.jar
```

To run on a custom port (e.g., port 8090):
- **Windows (Command Prompt)**:
  ```cmd
  set PORT=8090 && java -jar target\student-grade-tracker-1.0.0.jar
  ```
- **Windows (PowerShell)**:
  ```powershell
  $env:PORT=8090; java -jar target\student-grade-tracker-1.0.0.jar
  ```
- **Linux / macOS**:
  ```bash
  PORT=8090 java -jar target/student-grade-tracker-1.0.0.jar
  ```

Open your browser at **[http://localhost:8080](http://localhost:8080)** (or the port specified).

---

## 🧪 Testing & Verification

### Run Backend Automated Tests (25 Tests)
```cmd
cd StudentGradeTracker\backend
mvn test
```
*Runs all 25 automated tests covering calculation boundaries, dense ranking, tie stability, bulk import validation, SPA client-side routing, and API error isolation.*

### Run Frontend Production Build Validation
```cmd
cd StudentGradeTracker\frontend
npm run build
```

---

## 🐳 Docker Deployment

The project includes a multi-stage `Dockerfile` that builds both the React frontend and Spring Boot backend from source into a secure, minimal Alpine JRE image.

### Build Docker Image
```bash
docker build -t student-grade-tracker .
```

### Run Docker Container
```bash
docker run -d -p 8080:8080 -e PORT=8080 --name student-tracker student-grade-tracker
```
Access the application at `http://localhost:8080`.

---

## ☁️ Cloud Deployment (Render Blueprint / Single-URL)

The application is pre-configured for one-click deployment on [Render](https://render.com) using the included `render.yaml` Blueprint.

### Option A: Deploy via Render Blueprint (Recommended)
1. Push your repository to GitHub or GitLab.
2. Log in to your [Render Dashboard](https://dashboard.render.com/).
3. Click **New +** → **Blueprint**.
4. Connect your repository. Render will automatically detect `render.yaml` and provision:
   - A **Web Service** (Docker runtime, running on port 8080).
   - A **Managed PostgreSQL Database** (`studentgradedb`).
   - The connection string is automatically linked to the web service as `DATABASE_URL`.
5. Click **Apply**. Render will build and deploy the application with free automatic HTTPS.

### Option B: Manual Web Service on Render
1. In Render, select **New +** → **Web Service**.
2. Connect your repository.
3. Choose **Docker** as the Runtime.
4. Set the Health Check Path to `/api/health`.
5. Under **Environment Variables**, add:
   - `PORT`: `8080`
   - *(Optional)* `DATABASE_URL`: Your PostgreSQL connection string. If omitted, the application uses its persistent H2 database engine.

---

## 🔑 Environment Variables Reference

| Variable Name | Default Value | Description |
|---|---|---|
| `PORT` | `8080` | Port on which the Spring Boot web server listens |
| `DATABASE_URL` | *(None)* | Cloud-native PostgreSQL URL (`postgres://user:pass@host:port/dbname`). Automatically converted to JDBC format |
| `SPRING_DATASOURCE_URL` | `jdbc:h2:file:./data/studentgradedb;...` | Standard JDBC connection URL for H2 or PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `sa` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | *(Empty)* | Database password |
| `CORS_ALLOWED_ORIGINS` | `localhost:3000, 5173` | Optional comma-separated list of additional allowed CORS origins |
| `VITE_API_BASE_URL` | `""` *(relative)* | Frontend API base URL. Leave empty in production for single-origin `/api` requests |

---

## 🏆 Dense Ranking Specification

The application enforces **dense ranking** across all services, controllers, and frontend views:

1. **Ordering**: Students are sorted by marks descending.
2. **Ties**: Students with identical marks receive the same rank.
3. **Consecutive Increment**: When marks decrease to a new distinct value, the rank increments by exactly 1 without skipping numbers.
4. **Numeric Precision**: Ranking evaluates numeric values (`Double`), not formatted strings.

### Ranking Example

| Student ID | Student Name | Marks | Dense Rank | Notes |
|:---|:---|:---:|:---:|:---|
| S101 | Alice Johnson | 99.0 | **1** | Top score |
| S102 | Bob Smith | 99.0 | **1** | Tied with Alice |
| S103 | Charlie Brown | 95.0 | **2** | Next distinct score receives consecutive rank 2 |
| S104 | David Miller | 90.0 | **3** | Next distinct score receives rank 3 |
| S105 | Eva Green | 85.0 | **4** | Next distinct score receives rank 4 |

---

## 📥 Sample CSV / Excel Import Instructions

The Bulk Import feature allows rapid ingestion of student records with strict validation.

### 1. Accessing the Import Wizard
Navigate to `/import` (or click **Bulk Import** in the top navigation bar).

### 2. Supported Formats
- Comma-Separated Values (`.csv`)
- Microsoft Excel (`.xlsx`)

### 3. CSV Templates

#### A. Overall Marks Template (`students_overall.csv`):
```csv
studentId,name,className,marks
S201,Alice Walker,10A,94.5
S202,Brian Davis,10A,88.0
S203,Clara Evans,10A,94.5
S204,Daniel Wilson,10B,72.0
S205,Emma Thomas,10B,58.0
```

#### B. Subject-Wise Marks Template (`students_subjects.csv`):
```csv
studentId,name,className,ENG101,MTH101,SCI101
S301,George Martin,10A,85.0,90.0,88.5
S302,Hannah Clark,10A,78.0,82.5,79.0
S303,Ian Wright,10B,92.0,95.0,91.0
```

### 4. Validation Rules & Integrity Checks
- **Unique Student IDs**: Rejects duplicate IDs within the same file and IDs already existing in the database.
- **Marks Range**: Marks must be valid numbers between `0.00` and `100.00`.
- **Mandatory Fields**: Student ID, Name, and Class must be non-empty.
- **Clear Error Reporting**: Invalid and duplicate rows are flagged with human-readable error reasons. They are excluded from saving and can be downloaded as an Error Report CSV for correction. Valid rows are safely committed.
- **Formula Injection Protection**: Preventions applied to prevent spreadsheet formula injection attacks (`=`, `+`, `-`, `@`).

---

## 🔗 Application Endpoints Reference

| Service | Endpoint Path | Description |
|---|---|---|
| **Frontend Web App** | `/` | React Single-Page Application (SPA) |
| **Client Page Routes** | `/students`, `/reports`, `/analytics`, `/import`, `/settings` | Direct SPA navigation and refresh |
| **Students API** | `/api/students` | List, search, filter, and create students |
| **Analytics API** | `/api/analytics/statistics` | Class metrics, top performers, distributions |
| **Bulk Import API** | `/api/students/import` | Upload CSV/Excel files with preview validation |
| **Export Excel** | `/api/export/excel` | Download formatted .xlsx spreadsheet |
| **Export CSV** | `/api/export/csv` | Download sanitized CSV transcript |
| **Export Class PDF** | `/api/export/pdf/class` | Download printable class report card |
| **Export Student PDF** | `/api/export/pdf/student/{id}` | Download individual student report card PDF |
| **Health Check API** | `/api/health` and `/health` | Application status, uptime, and DB connectivity |
| **Actuator Health** | `/actuator/health` | Spring Boot actuator liveness/readiness endpoint |
| **Swagger UI** | `/swagger-ui.html` | Interactive OpenAPI documentation |
| **OpenAPI Schema** | `/v3/api-docs` | OpenAPI 3.0 JSON schema |
| **H2 Web Console** | `/h2-console` | Direct database explorer (when using H2 engine) |

---

## ❓ Troubleshooting

1. **Port Already in Use**:
   - If port 8080 or 8090 is in use, supply a different port: `PORT=8085 java -jar target/student-grade-tracker-1.0.0.jar`.
2. **Missing Static Files in Production JAR**:
   - Ensure `npm run build` was executed in `frontend/` before running `mvn package` in `backend/`.
3. **Database Locks (H2)**:
   - If running multiple instances locally, ensure `AUTO_SERVER=TRUE` is present in the JDBC URL, or run a single application instance.
