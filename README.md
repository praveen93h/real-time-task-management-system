# Real-Time Collaborative Task Management Platform

A Trello/Asana-like collaborative task management system where multiple users can create, assign, and track tasks in real-time with live updates across all connected clients.

## Features

- **Real-time Collaboration** - Tasks update instantly across all connected clients via WebSocket
- **Kanban Board** - Drag-and-drop task management with customizable columns
- **Project Management** - Create projects, invite team members, assign roles
- **Task Management** - Create, assign, prioritize, and track tasks
- **User Presence** - See who's online and working on the same project
- **Comments** - Discuss tasks with real-time comment updates
- **Activity Log** - Track all changes and actions
- **Redis Caching** - Fast data access with intelligent cache invalidation

## Tech Stack

### Backend
- Java 17
- Spring Boot 3.2
- Spring Security with JWT
- Spring WebSocket (STOMP over SockJS)
- Spring Data JPA
- PostgreSQL
- Redis

### Frontend
- React 18
- Vite
- Redux Toolkit
- Material-UI
- @hello-pangea/dnd (drag-and-drop)
- STOMP.js / SockJS

## Quick Start with Docker

### Prerequisites
- Docker and Docker Compose installed

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/real-time-task-management.git
   cd real-time-task-management
   ```

2. **Create environment file**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Start all services**
   ```bash
   docker-compose up -d
   ```

4. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - API Documentation: http://localhost:8080/swagger-ui.html (if enabled)

### Stop services
```bash
docker-compose down
```

### View logs
```bash
docker-compose logs -f
```

## Local Development

### Backend

1. **Prerequisites**
   - Java 17+
   - Maven 3.9+
   - PostgreSQL 15+
   - Redis 7+

2. **Configure database**
   ```bash
   # Create PostgreSQL database
   createdb taskmanagement
   ```

3. **Run backend**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

### Frontend

1. **Prerequisites**
   - Node.js 18+
   - npm 9+

2. **Install dependencies**
   ```bash
   cd frontend
   npm install
   ```

3. **Run development server**
   ```bash
   npm run dev
   ```

4. **Access** http://localhost:5173

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register new user |
| POST | /api/auth/login | Login |
| POST | /api/auth/refresh | Refresh token |
| GET | /api/auth/me | Get current user |

### Projects
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/projects | Get user's projects |
| POST | /api/projects | Create project |
| GET | /api/projects/:id | Get project details |
| PUT | /api/projects/:id | Update project |
| DELETE | /api/projects/:id | Delete project |
| GET | /api/projects/:id/members | Get project members |
| POST | /api/projects/:id/members | Add member |
| DELETE | /api/projects/:id/members/:userId | Remove member |

### Tasks
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/projects/:id/tasks | Get project tasks |
| POST | /api/projects/:id/tasks | Create task |
| GET | /api/tasks/:id | Get task details |
| PUT | /api/tasks/:id | Update task |
| DELETE | /api/tasks/:id | Delete task |
| PATCH | /api/tasks/:id/status | Update task status |
| PATCH | /api/tasks/:id/assign | Assign task |

### Comments
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/tasks/:id/comments | Get task comments |
| POST | /api/tasks/:id/comments | Add comment |
| DELETE | /api/tasks/:id/comments/:commentId | Delete comment |

## WebSocket Events

Connect to `/ws` endpoint with STOMP over SockJS.

### Topics
- `/topic/project/{projectId}` - Project and task updates
- `/topic/project/{projectId}/presence` - User presence updates

### Event Types
- `TASK_CREATED`, `TASK_UPDATED`, `TASK_DELETED`
- `TASK_STATUS_CHANGED`, `TASK_ASSIGNED`
- `COMMENT_ADDED`, `COMMENT_DELETED`
- `PROJECT_UPDATED`, `PROJECT_DELETED`
- `MEMBER_ADDED`, `MEMBER_REMOVED`
- `USER_JOINED`, `USER_LEFT`

## Project Structure

```
real-time-task-management/
├── backend/
│   ├── src/main/java/com/taskmanagement/
│   │   ├── config/          # Spring configuration
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data transfer objects
│   │   ├── entity/          # JPA entities
│   │   ├── enums/           # Enumerations
│   │   ├── exception/       # Exception handling
│   │   ├── repository/      # JPA repositories
│   │   ├── security/        # Security configuration
│   │   └── service/         # Business logic
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/      # React components
│   │   ├── contexts/        # React contexts
│   │   ├── hooks/           # Custom hooks
│   │   ├── pages/           # Page components
│   │   ├── services/        # API services
│   │   └── store/           # Redux store
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml
└── README.md
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| DB_NAME | PostgreSQL database name | taskmanagement |
| DB_USER | PostgreSQL username | postgres |
| DB_PASSWORD | PostgreSQL password | password |
| DB_PORT | PostgreSQL port | 5432 |
| REDIS_PORT | Redis port | 6379 |
| JWT_SECRET | JWT signing secret | (default dev secret) |
| BACKEND_PORT | Backend server port | 8080 |
| FRONTEND_PORT | Frontend server port | 3000 |
| ALLOWED_ORIGINS | CORS allowed origins | http://localhost:3000 |

## License

MIT License - see LICENSE file for details.
