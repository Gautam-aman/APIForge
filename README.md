# ApiForge

ApiForge is a full-stack application that leverages modern technologies to build robust and AI-powered APIs.

## Tech Stack

### Backend
- **Java 21**
- **Spring Boot** (Web, Data JPA, Validation, Actuator)
- **Spring AI** (OpenAI integration)
- **PostgreSQL** (Database)
- **Swagger / OpenAPI 3** (API Documentation)
- **Maven** (Build Tool)

### Frontend
- **React 19**
- **Vite** (Build Tool)

## Prerequisites

- **Java 21** or higher
- **Node.js** (v18 or higher) and **npm**
- **PostgreSQL** (running locally or accessible via URL)
- **Maven**

## Getting Started

### 1. Clone the repository

```bash
git clone <repository-url>
cd ApiForge
```

### 2. Backend Setup

1. Navigate to the `Backend` directory:
   ```bash
   cd Backend
   ```
2. Configure your database connection and OpenAI API key. You can do this by setting environment variables or by editing the `src/main/resources/application.properties` (or `.yml`) file.
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/apiforge
   spring.datasource.username=your_db_user
   spring.datasource.password=your_db_password
   spring.ai.openai.api-key=your_openai_api_key
   ```
3. Run the Spring Boot application using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
   The backend API will be running at `http://localhost:8080`.
4. You can access the Swagger API documentation at `http://localhost:8080/swagger-ui.html` or `http://localhost:8080/v3/api-docs`.

### 3. Frontend Setup

1. Open a new terminal and navigate to the `frontend` directory:
   ```bash
   cd frontend
   ```
2. Install the frontend dependencies:
   ```bash
   npm install
   ```
3. Start the Vite development server:
   ```bash
   npm run dev
   ```
   The frontend application will be running, typically at `http://localhost:5173`.

## License

This project is open-source and available under the standard MIT License.
