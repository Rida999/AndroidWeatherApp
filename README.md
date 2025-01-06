# Weather Application
## Overview
A modern Android weather application that provides real-time weather information and forecasts. The app features user authentication, detailed weather metrics, and customizable temperature units.

## Features
### User Authentication
- Secure login and signup system
- Password reset functionality
- Account lockout protection after multiple failed attempts
- Password change capability

### Weather Information
- Real-time weather data
- 5-day weather forecast
- Hourly weather updates
- Detailed weather metrics:
  - Temperature
  - Humidity
  - Wind speed
  - Atmospheric pressure

### Customization
- Temperature unit selection (Celsius/Fahrenheit)
- City-based weather search
- Profile management

## Technical Stack
### Frontend
- Language: Kotlin
- Minimum SDK: API Level 31 (Android 12)
- Target SDK: Latest stable Android version

### Backend Services
- Firebase Authentication
- Firebase Realtime Database
- OpenWeatherMap API

### Libraries
- Retrofit2: REST API client
- GSON Converter: JSON parsing
- Lottie: Loading animations
- Firebase SDK: Authentication & database
- Kotlin Coroutines: Asynchronous operations

## Setup Instructions
### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11+
- Android SDK (API 31)
- Firebase account
- OpenWeatherMap API key

### Installation Steps
1. Clone the repository
```bash
git clone https://github.com/yourusername/weather-app.git
```

2. Firebase Setup
- Create a new Firebase project
- Add Android app in Firebase console
- Download `google-services.json`
- Place the file in the app module directory

3. API Configuration
- Create an account at OpenWeatherMap
- Obtain an API key
- Add your API key in `local.properties`:
```properties
WEATHER_API_KEY=your_api_key_here
```

4. Build and Run
- Open project in Android Studio
- Sync Gradle files
- Run on emulator or physical device

## Security Features
- Email/Password authentication
- Account lockout system
- Secure password requirements:
  - Minimum 8 characters
  - At least one number
  - At least one special character
  - Combination of uppercase and lowercase letters



## Acknowledgments
- OpenWeatherMap for weather data
- Firebase for backend services

## Contact
Carl Matta – carlmatta3@gmail.com
Rida Ajam – ridaajam999@gmail.com
