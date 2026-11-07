# Cartify: Microservices E-commerce Platform

Cartify is a robust, end-to-end e-commerce marketplace built using a microservices architecture. It leverages **Spring Boot** for the backend, **Angular** for the frontend, and **MongoDB** for persistent storage, with **Kafka** for asynchronous communication — all backed by an automated **Jenkins CI/CD pipeline** with **SonarQube**-enforced quality gates.

## 🏗️ Architecture Overview

The system is decomposed into several specialized services, each handling a specific domain of the application:

- **Nginx (Reverse Proxy):** Central ingress handling SSL/HTTPS termination, static asset serving for the Angular frontend, and proxying API traffic (`/api/*`) to the API Gateway.
- **API Gateway:** Entry point for microservice routing and CORS management.
- **Discovery Service (Eureka):** Service registry for dynamic service discovery.
- **User Service:** Manages authentication, user profiles, and roles (CLIENT, SELLER).
- **Product Service:** Handles product catalog management and CRUD operations.
- **Media Service:** Manages image uploads and storage (integrated with Cloudinary).
- **Order Service:** Manages shopping carts, order creation, order tracking, and status management.
- **Frontend:** A modern Angular SPA for user interaction.

### System Diagram (Simplified)
```text
                        [ Client (Browser) ]
                                 |
                                 v (https://localhost:4200)
                    [ Nginx Reverse Proxy / SSL ]
                     /                         \
          (Static Assets)                     (API Requests: /api/*)
                   /                             \
                  v                               v
         [ Angular SPA Frontend ]       [ API Gateway (8080) ]
                                                  |
                 ---------------------------------------------------
                 |                |                |               |
          [User Service]   [Product Service] [Media Service] [Order Service]
             |       |        |       |        |       |        |       |
             v       |        v       |        v       |        v       |
        [User DB]    |   [Product DB] |   [Media DB]   |   [Order DB]   |
                     \________|_______|________|_______/                |
                              |                                         |
                              v                                         v
                      [ Eureka Discovery ]                    [ Kafka Broker (9092) ]
```



## 🚀 Key Features

### 🔐 Security & Auth
- **JWT-based Authentication:** Secure access to protected resources.
- **Role-Based Access Control (RBAC):** Distinct permissions for `CLIENT` and `SELLER`.
- **Password Security:** Hashing and salting with BCrypt.
- **Ownership Enforcement:** Sellers can only manage their own products and media.

### 📦 Product Management
- Sellers can Create, Read, Update, and Delete (CRUD) products.
- Clients can browse the public product catalog.
- Products are linked to media URLs managed by the Media Service.

### 🖼️ Media Handling
- Secure file uploads with MIME type validation (`image/*`).
- Strict file size limits (max 2MB per upload).
- Cloudinary integration for scalable image hosting.

### 🛒 Cart & Order Management
- Shopping cart management (add, update, delete items).
- Order creation and checkout workflow with pricing snapshot.
- Order history tracking and detailed item viewing for buyers.
- Order cancellation and re-order ("redo") functionality.
- Seller-facing order overview to monitor item sales.
- Asynchronous Kafka event publishing on order actions.

### ⚙️ CI/CD & Code Quality
- **Automated Build & Test:** Each Spring Boot service is built and tested independently via Maven Wrapper before deployment.
- **SonarQube Static Analysis:** Every service (`product-service`, `user-service`, …) is scanned as its own SonarQube project, run against a merged preview of the PR and the target branch.
- **Quality Gates:** The pipeline automatically fails if a service doesn't meet its quality gate (coverage, bugs, vulnerabilities, code smells, duplication).
- **Branch Protection:** Merging into `main` requires both a passing Jenkins/SonarQube status check **and** an approved code review — enforced directly by GitHub branch protection rules.
- **Rollback Strategy:** Failed deployments automatically roll back to the last known-good commit (`git checkout ${GIT_PREVIOUS_SUCCESSFUL_COMMIT}`).
- **Notifications:** Email alerts on build success/failure.

### 🛠️ Technical Stack
- **Backend:** Java 17+, Spring Boot 3, Spring Cloud, Spring Security.
- **Frontend:** Angular 19, Tailwind CSS, Angular Material.
- **Data:** MongoDB (per-service instances).
- **Messaging:** Apache Kafka.
- **Infrastructure:** Docker & Docker Compose (rootless).
- **CI/CD:** Jenkins (Multibranch Pipeline, custom `jdk21` + Temurin 17 agent image), ngrok (webhook tunnel).
- **Code Quality:** SonarQube Community Edition, Docker Compose (Postgres-backed).

---

## 🛠️ Setup & Installation

### Prerequisites
- Docker & Docker Compose
- Java 17 (for local builds)
- Node.js & npm (for frontend)
- Angular CLI (`npm install -g @angular/cli`)

### 1. Environment Configuration
Create a `.env` file in the root directory based on `.env.example`:

### 2. SSL Certificate Setup
To support end-to-end HTTPS, you must provide SSL certificates in the `frontend/` directory. Create or place the following files in `frontend/`:

- `secureCertificate.crt`: Your SSL certificate.
- `private.key`: The private key for your certificate.
- `securePassphrase`: A file containing the passphrase for the private key (if applicable).

Example command to generate self-signed certificates for local development:
```bash
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout frontend/private.key \
  -out frontend/secureCertificate.crt
# Create a dummy passphrase file if your key is not encrypted
touch frontend/securePassphrase
```

### 3. Build the Services
Use the provided build script to package all backend microservices:
```bash
chmod +x ./scripts/build.sh
./scripts/build.sh
```

### 4. Launch the Application
Run the entire system using Docker Compose:
```bash
docker compose up -d
```
*Note: The services will wait for the Discovery Service to be healthy before starting.*

The application will be available at `https://localhost:4200`.

---

## 🔁 CI/CD Pipeline

The CI/CD pipeline is fully implemented for automated build, test, and quality gate execution. To run the pipeline, Jenkins should be configured locally.

---

## 📖 API Documentation

The **API Gateway** (Port 8080) routes traffic to the following endpoints:

| Service | Endpoint | Description | Auth Required |
|---------|----------|-------------|---------------|
| **User** | `POST /auth/register` | User registration | No |
| **User** | `POST /auth/login` | User login -> JWT | No |
| **User** | `GET /users/me` | Current user profile | Yes |
| **Product** | `GET /api/products` | List all products | No |
| **Product** | `POST /api/products` | Create product | Yes (SELLER) |
| **Media** | `POST /media` | Upload image | Yes (SELLER) |
| **Order** | `POST /carts` | Add item to cart | Yes |
| **Order** | `GET /carts` | Get user cart items | Yes |
| **Order** | `PUT /carts/{id}` | Update cart item quantity | Yes |
| **Order** | `DELETE /carts/{id}` | Remove item from cart | Yes |
| **Order** | `POST /orders` | Create order / Checkout | Yes |
| **Order** | `GET /orders` | List user orders | Yes |
| **Order** | `GET /orders/{id}` | Get order details | Yes |
| **Order** | `GET /orders/{id}/items` | Get order items | Yes |
| **Order** | `PUT /orders/{id}/cancel` | Cancel order | Yes |
| **Order** | `POST /orders/{id}/redo` | Reorder / Redo order | Yes |
| **Order** | `GET /orders/seller` | Get seller orders | Yes (SELLER) |

---

## 🛠️ Developer Scripts

- `build.sh`: Packages all Spring Boot services using Maven Wrapper.
- `run_docker_dns.sh`: Helper to restart Docker with custom DNS settings (if needed for registry access).

---

## 🧪 Evaluation Criteria Compliance

### MR-Jenk (CI/CD)
- ✅ **Automation:** GitHub-triggered Jenkins Multibranch Pipeline (push + PR events).
- ✅ **Testing Integration:** Backend tests run via Maven; pipeline fails on test failure.
- ✅ **Deployment:** Docker-based deploy with health checks and rollback.
- ✅ **Notifications:** Email alerts on build/deploy status.
- ✅ **Pipeline Quality:** Declarative pipeline, per-service staging, Credentials Store for secrets.

### SafeZone (Code Quality & Security)
- ✅ **Setup & Configuration:** SonarQube via Docker Compose, Postgres-backed.
- ✅ **Integration:** GitHub → Jenkins → SonarQube → Jenkins → GitHub status loop.
- ✅ **Code Analysis:** Per-service static analysis and vulnerability detection.
- ✅ **Automation:** Scans triggered automatically on every push and pull request.
- ✅ **Process:** Mandatory PR review + passing quality gate required before merge.

### General
- ✅ **Microservices:** Separated into User, Product, Media, Order, Gateway, and Discovery.
- ✅ **Persistence:** Independent MongoDB instances for each service (User DB, Product DB, Media DB, Order DB).
- ✅ **Media Security:** MIME validation and 2MB limit enforced in Media Service.
- ✅ **Frontend:** Responsive Angular UI with Guards and Interceptors.
- ✅ **Observability:** Actuator `/health` endpoints configured.


