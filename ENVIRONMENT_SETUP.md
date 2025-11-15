# Environment Configuration

This project uses environment variables for configuration to keep sensitive credentials secure.

## Setup Instructions

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

2. Edit the `.env` file and replace the placeholder values with your actual credentials:
   ```bash
   # Database Configuration
   DB_URL=jdbc:mysql://your-database-host:3306/your-database-name
   DB_USERNAME=your_username
   DB_PASSWORD=your_password

   # MySQL Docker Configuration (for local development)
   MYSQL_ROOT_PASSWORD=your_root_password
   MYSQL_DATABASE=airtrack_db
   MYSQL_USER=airtrack_user
   MYSQL_PASSWORD=your_password
   ```

3. **Important**: Never commit the `.env` file to version control. It's already included in `.gitignore`.

## Running with Docker Compose

When using Docker Compose, the environment variables from your `.env` file will be automatically loaded:

```bash
docker-compose up
```

## Running the Spring Boot Application

The application will automatically use environment variables defined in your `.env` file or system environment.

### Option 1: Using environment variables directly
```bash
export DB_URL=jdbc:mysql://localhost:3306/airtrack_db
export DB_USERNAME=airtrack_user
export DB_PASSWORD=your_password
./mvnw spring-boot:run
```

### Option 2: Using a .env file with Spring Boot
Set the environment variables in your IDE or terminal before running the application.

## Default Values

If environment variables are not set, the application will use these defaults:
- `DB_URL`: `jdbc:mysql://localhost:3306/airtrack_db`
- `DB_USERNAME`: `airtrack_user`
- `DB_PASSWORD`: (empty string - must be provided)
