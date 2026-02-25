# Development Guide

## Prerequisites
- Node.js 20+
- Java 17+
- Maven (or `./mvnw`)
- MySQL 8+

## Local Setup

### Backend
```bash
cd server
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=interviewiq
export DB_USERNAME=interviewiq
export DB_PASSWORD=interviewiqpass

export GROQ_API_KEY=your_groq_key
export GOOGLE_CLIENT_ID=your_google_client_id

./mvnw spring-boot:run
```

### Frontend
```bash
cd client
npm install
REACT_APP_API_URL=http://localhost:8080 npm start
```

## Database
- Schema file: `database/schema.sql`
- Spring JPA is configured with `ddl-auto=update`.

## Testing

### Frontend
```bash
cd client
npm test -- --watchAll=false
npm run build
```

### Backend
```bash
cd server
./mvnw test
./mvnw -DskipTests compile
```

## CI
GitHub Actions workflow is in `.github/workflows/ci.yml` and runs:
- Frontend tests/build
- Backend tests/compile

## Secrets
Never commit provider keys. Use repository secrets and environment variables.
