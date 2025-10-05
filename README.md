# CloudMesh
A platform for managing cloud resources across multiple providers.

## Features
- Multi-Cloud Support: Manage resources from AWS, GCP, Azure, and DigitalOcean.
- Unified Billing: Consolidate billing information from all cloud providers.
- Real-Time Monitoring: Monitor resource usage and performance metrics.
- Secure: JWT-based authentication, OAuth2, encrypted credential storage

## Installation
```bash
# 1. Clone the repository
git clone https://github.com/kjswaruph/cloudmesh.git
cd cloudmesh

# 2. Create environment file
cp .env.example .env
# Edit .env with your credentials

# 3. Start all services
docker-compose up -d

# 4. Check status
docker-compose ps

# 5. Access the application
# Frontend: http://localhost:3000
# Backend API: http://localhost:8080
```

