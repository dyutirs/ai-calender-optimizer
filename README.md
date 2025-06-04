# AI Calendar Assistant

An intelligent calendar assistant that integrates OpenAI's GPT models with Google Calendar to help users manage their schedule through natural language conversations.

## Features

- **Natural Language Interface**: Chat with the AI assistant to manage your calendar
- **Google Calendar Integration**: Directly create and view events in your Google Calendar
- **Quick Actions**: Schedule meetings and check your calendar with one click
- **Responsive Design**: Works on desktop and mobile devices

## Prerequisites

- Java 17 or higher
- Maven
- Google Cloud Platform account with Calendar API enabled
- OpenAI API key

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/yourusername/calendar-assistant.git
cd calendar-assistant
```

### 2. Configure Google Calendar API

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project
3. Enable the Google Calendar API
4. Create OAuth 2.0 credentials (Web application type)
5. Add `http://localhost:8080/oauth2callback` as an authorized redirect URI
6. Download the credentials JSON file
7. Save it as `src/main/resources/credentials.json`

### 3. Configure OpenAI API

1. Get your API key from [OpenAI](https://platform.openai.com/account/api-keys)
2. Open `src/main/resources/application.properties`
3. Set your OpenAI API key:
   ```
   openai.api.key=your_api_key_here
   ```

### 4. Build and run the application

```bash
mvn clean install
mvn spring-boot:run
```

The application will be available at http://localhost:8080

## Usage

1. Open the application in your web browser
2. Click "Connect Google Calendar" to authorize access to your calendar
3. Start chatting with the AI assistant
4. Use natural language to create events, e.g., "Schedule a meeting with John tomorrow at 2pm"
5. Use the quick action buttons for common tasks

## Project Structure

- `src/main/java/org/studyeasy/controller/` - REST controllers
- `src/main/java/org/studyeasy/service/` - Business logic services
- `src/main/resources/templates/` - Thymeleaf HTML templates
- `src/main/resources/application.properties` - Application configuration

## Technologies Used

- Spring Boot 2.7.14
- Thymeleaf
- Google Calendar API
- OpenAI API (GPT-4o)
- Bootstrap 5

## Security Considerations

- The application stores OAuth tokens locally in the `tokens` directory
- API keys should be kept confidential and not committed to version control
- For production use, implement proper user authentication and secure token storage

## Acknowledgments

- OpenAI for providing the GPT models
- Google for the Calendar API
- Spring Boot team for the framework
