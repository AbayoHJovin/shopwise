# ShopWise

A Spring Boot e-commerce application with PostgreSQL database, Cloudinary image storage, and Gemini AI integration.

## Local Development

1. Clone the repository
2. Create a `.env` file in the root directory with the environment variables listed in `.env.sample`
3. Run the application using Maven:
   ```
   ./mvnw spring-boot:run
   ```

## Docker Build

Build the Docker image:

```
docker build -t shopwise .
```

Run the Docker container:

```
docker run -p 5000:5000 --env-file .env shopwise
```

## Deploying to Render.com

1. Fork or push this repository to your GitHub account
2. Sign up for a Render.com account
3. Click "New +" and select "Blueprint" from the dropdown
4. Connect your GitHub repository
5. Render will detect the `render.yaml` file and set up the service
6. Configure the required environment variables in the Render dashboard
7. Deploy the service

You can also deploy manually:

1. From the Render dashboard, click "New +" and select "Web Service"
2. Connect your GitHub repository
3. Set the following options:
   - Name: shopwise
   - Runtime: Docker
   - Build Command: (leave empty, using Dockerfile)
   - Start Command: (leave empty, using Dockerfile)
4. Add all required environment variables
5. Click "Create Web Service"

## Environment Variables

The application requires the following environment variables:

- `DB_URL`: PostgreSQL database URL
- `DB_USER`: Database username
- `DB_PASSWORD`: Database password
- `MAIL_USERNAME`: Gmail username
- `MAIL_PASSWORD`: Gmail app password
- `CLOUDINARY_CLOUD_NAME`: Cloudinary cloud name
- `CLOUDINARY_API_KEY`: Cloudinary API key
- `CLOUDINARY_API_SECRET`: Cloudinary API secret
- `GEMINI_API_KEY`: Google Gemini API key
- `GEMINI_MODEL_NAME`: Gemini model name
- `GEMINI_PROJECT_ID`: Google Cloud project ID
