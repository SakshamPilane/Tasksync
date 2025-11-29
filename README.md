# 🚀 TaskSync – Team Task & Workflow Management System

**Backend: Java + Spring Boot + MySQL + JWT Security**

TaskSync is a modern, scalable, and production-ready backend for managing users, projects, teams, and workflows inside organizations. Built with enterprise-grade patterns, RBAC security, and SaaS-friendly architecture.

![Java](https://img.shields.io/badge/Java-17+-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Maven](https://img.shields.io/badge/Build-Maven-orange)
![MySQL](https://img.shields.io/badge/Database-MySQL-blue)
![Stars](https://img.shields.io/github/stars/SakshamPilane/Tasksync)
![Forks](https://img.shields.io/github/forks/SakshamPilane/Tasksync)
![Issues](https://img.shields.io/github/issues/SakshamPilane/Tasksync)
![PRs](https://img.shields.io/github/issues-pr/SakshamPilane/Tasksync)
![Last Commit](https://img.shields.io/github/last-commit/SakshamPilane/Tasksync)
![Contributors](https://img.shields.io/github/contributors/SakshamPilane/Tasksync)
[![License](https://img.shields.io/github/license/SakshamPilane/Tasksync)](https://github.com/SakshamPilane/Tasksync/blob/main/LICENSE)

---

## 🧱 Tech Stack

- **Java 17+**
- **Spring Boot 3.x**
  - Spring Web
  - Spring Security
  - Spring Data JPA
  - JWT Authentication
- **MySQL**
- **Maven**
- **Lombok**

---

# 🔐 Authentication & Authorization Module (JWT + RBAC)

This module provides secure identity management with:

### ✔ Features
- User Registration  
- User Login (JWT Authentication)  
- JWT Access Token + Refresh Token System  
- Password Hashing (BCrypt)  
- Role-Based Access Control  
  - `ROLE_ADMIN`
  - `ROLE_MANAGER`
  - `ROLE_USER`
- Custom Authorization Rules  
- Global Exception Handling  
- Secured Endpoints  
- Token Refresh API  

### ✔ API Endpoints  
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login + JWT tokens |
| POST | `/api/auth/refresh` | Refresh access token |

---

# 👥 User Management Module

Advanced user system designed with enterprise SaaS patterns.

### ✔ Features
- Get own profile  
- Update own profile  
- Change password  
- Admin-only: Create user  
- Admin-only: Activate/Deactivate user  
- Admin/Manager: Get all users  
- Admin-only: Update user roles  
- Detailed User DTO responses  

### ✔ API Endpoints  
| Method | Endpoint | Role | Description |
|--------|----------|-------|-------------|
| GET | `/api/users/me` | All | Get logged-in user's profile |
| PUT | `/api/users/update` | All | Update own username/email |
| PUT | `/api/users/change-password` | All | Change own password |
| GET | `/api/users` | ADMIN, MANAGER | List all users |
| POST | `/api/users/create` | ADMIN | Create new user |
| PUT | `/api/users/{id}/role` | ADMIN | Update user role |
| PUT | `/api/users/{id}/status` | ADMIN | Activate/deactivate user |

---

# 📁 Project Management Module

Fully SaaS-ready project module designed like Jira/Asana/Monday.com.

### ✔ Features
- Create Projects (Admin/Manager)
- Auto-assign creator as manager (if managerId missing)
- Update project details  
- Restriction: Manager MUST already be a project member  
- Add/Remove project members  
- Prevent removing current manager  
- Project Archiving  
- Full member list with roles  
- Timestamps (createdAt, updatedAt)  
- Clean DTO mapping  

### ✔ API Endpoints  
| Method | Endpoint | Role | Description |
|--------|----------|-------|-------------|
| POST | `/api/projects` | ADMIN, MANAGER | Create new project |
| PUT | `/api/projects/{id}` | ADMIN, MANAGER | Update project |
| GET | `/api/projects/{id}` | All | Get project details |
| GET | `/api/projects` | ADMIN, MANAGER | List all projects |
| PUT | `/api/projects/{id}/archive` | ADMIN | Archive project |
| PUT | `/api/projects/{pid}/add-member/{uid}` | ADMIN, MANAGER | Add member |
| PUT | `/api/projects/{pid}/remove-member/{uid}` | ADMIN, MANAGER | Remove member |

---

## 📚 Folder Structure

```pqsql
src/main/java/com/tasksync/
│
├── controller/
│     ├── AuthController.java
│     ├── UserController.java
│     └── ProjectController.java
│
├── service/
│     ├── AuthService.java
│     ├── UserService.java
│     └── ProjectService.java
│
├── security/
│     ├── JwtUtil.java
│     ├── JwtFilter.java
│     └── SecurityConfig.java
│
├── entity/
│     ├── User.java
│     ├── Role.java
│     ├── RefreshToken.java
│     └── Project.java
│
├── repository/
│     ├── UserRepository.java
│     ├── RoleRepository.java
│     ├── RefreshTokenRepository.java
│     └── ProjectRepository.java
│
└── dto/
      ├── AuthRequest.java
      ├── AuthResponse.java
      ├── RegisterRequest.java
      ├── UserResponseDTO.java
      ├── CreateUserRequest.java
      ├── UpdateUserRequest.java
      ├── CreateProjectRequest.java
      ├── UpdateProjectRequest.java
      └── ProjectResponseDTO.java
```

---

## ⚙️ How to Run

### 1. Clone Repository
```bash
git clone https://github.com/your-username/tasksync-backend.git
cd tasksync-backend
```

### 2. Configure MySQL in application.properties
```ini
spring.datasource.url=jdbc:mysql://localhost:3306/tasksync
spring.datasource.username=root
spring.datasource.password=yourpassword

app.jwt.secret=YOUR_SECRET_KEY
app.jwt.expiration-ms=3600000
```

### 3. Run the project
```bash
mvn spring-boot:run
```

---

## 🧪 Testing (Postman)

### Includes test cases for:
- Auth + Refresh Token
- User Management
- Project Management
- RBAC-based access control
- Manager validation rules
- Member restrictions

---

### 🏗 Upcoming Modules (Next Steps)
- Task Module (CRUD, workflow, statuses)
- Comments + Attachments
- Activity Logs (GitHub-style)
- Project Progress Tracking
- Notifications System
- Automation Engine

---

### © License

### MIT License.
<a href="https://github.com/SakshamPilane/Tasksync/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/SakshamPilane/Tasksync" />
  </a>
