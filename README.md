# Zest Employee Management System

## Overview
The Zest Employee Management System is a Spring Boot application designed to manage employees, departments, positions, and user authentication. It features role-based access control with stateless JWT-based authentication.

## Features
- **User Authentication**: Register and login endpoints with JWT token generation.
- **Employee Management**: Create, read, update, and soft-delete employees. Pagination and sorting are supported.
- **Department & Position Tracking**: Employees are associated with specific departments and positions.
- **Search Capabilities**: Search employees by name or filter by department.

## Technology Stack
- **Java 17+**
- **Spring Boot** (Web, Data JPA, Security)
- **MySQL** (Relational Database)
- **Lombok** (Boilerplate reduction)
- **JWT (JSON Web Tokens)** (Stateless Authentication)
- **Maven** (Dependency Management)

## Setup and Installation

### 1. Database Configuration
Ensure MySQL is running and create the database (if not automatically created by Hibernate). Update `src/main/resources/application.properties` with your database credentials if necessary:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/employee_db
spring.datasource.username=root
spring.datasource.password=yourpassword
```

### 2. Build the Project
Use Maven to build the project:
```bash
mvn clean install
```

### 3. Run the Application
You can run the application using Maven:
```bash
mvn spring-boot:run
```
The server will start on `http://localhost:8080`.

## API Testing with Postman
A Postman collection (`postman_collection.json`) is included in the project root to test all available endpoints. 

1. Import `postman_collection.json` into Postman.
2. The collection includes a `base_url` variable (`http://localhost:8080`).
3. Start by using the **Auth -> Login as Admin** endpoint (Username: `admin`, Password: `Admin@123`).
4. The login request automatically saves the JWT token to the `jwt_token` collection variable.
5. You can now use the Employee endpoints (like Get All Employees, Create Employee, etc.) without manually setting the token.

## Endpoints Summary

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Authenticate and receive a JWT token
- `GET /api/auth/me` - Get current authenticated user details

### Employees
- `GET /api/employees` - Get all employees (paginated)
- `GET /api/employees/{id}` - Get an employee by ID
- `GET /api/employees/department/{deptId}` - Get employees by department
- `GET /api/employees/search?name={name}` - Search employees by name
- `POST /api/employees` - Create a new employee
- `PUT /api/employees/{id}` - Update an existing employee
- `DELETE /api/employees/{id}` - Soft delete an employee

## Default Seed Data
The application comes with seed data for roles, users, departments, and positions.
- **Admin User**: Username: `admin`, Password: `Admin@123`
- **Departments**: Engineering, HR, Finance, Marketing, Operations
- **Positions**: Junior Developer, Senior Developer, Team Lead, Manager, Analyst, HR Executive, Accountant
