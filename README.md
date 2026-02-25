# InterviewIQ

InterviewIQ is a full-stack SaaS app for resume optimization and interview preparation.

## Stack
- Frontend: React
- Backend: Spring Boot, Spring Security, JWT, JPA
- Database: MySQL
- AI providers: Groq (primary), Mistral (fallback), heuristic fallback

## Monorepo Structure
- `client/` React web app
- `server/` Spring Boot API
- `database/` SQL schema
- `docker-compose.yml` local full-stack orchestration
- `docs/DEVELOPMENT.md` developer setup and contribution guide

## Quick Start

### 1) Run with Docker
```bash
export GROQ_API_KEY="your_groq_key"
export GOOGLE_CLIENT_ID="your_google_client_id"
docker compose up --build
```

Services:
- Client: [http://localhost:3000](http://localhost:3000)
- Server: [http://localhost:8080](http://localhost:8080)
- MySQL: `localhost:3306`

### 2) Run Locally (without Docker)
Terminal 1:
```bash
cd server
export GROQ_API_KEY="your_groq_key"
./mvnw spring-boot:run
```

Terminal 2:
```bash
cd client
npm install
npm start
```

## Environment Variables

### Backend (`server`)
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`, `JWT_EXPIRATION_MS`
- `GOOGLE_CLIENT_ID`
- `GROQ_API_KEY`, `GROQ_MODEL` (default: `llama-3.1-8b-instant`)
- `MISTRAL_API_KEY`, `MISTRAL_MODEL` (fallback provider)

### Frontend (`client`)
- `REACT_APP_API_URL` (default: `http://localhost:8080`)
- `REACT_APP_GOOGLE_CLIENT_ID`

## Notes
- Do **not** commit API keys in source files.
- Use your own provider keys from Groq/Mistral dashboards.
