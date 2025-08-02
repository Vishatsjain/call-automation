# Vibe Coder - Payment Reminder Call Manager

A modern Android app built with Jetpack Compose for managing payment reminder calls with comprehensive customer tracking and follow-up management.

## 🚀 Features

### Core Functionality
- **Customer Management**: Add, edit, and delete customers with detailed information
- **Smart Call Scheduling**: Shows only customers with today's promise date
- **Auto-calling Flow**: Sequential calling with floating action button
- **Follow-up Tracking**: Unlimited follow-up logs with timestamps
- **Visual Status Indicators**: Color-coded by follow-up count (0-5: light, 6-10: medium, 11-20: high, 20+: critical)

### Advanced Features
- **Multiple Sort Options**: Date, alphabetical, amount, follow-up count
- **Dark Mode Support**: System-wide theme toggle
- **Daily Notifications**: Multiple customizable reminder times
- **Data Export/Import**: Excel and CSV support for data portability
- **Field Locking**: Toggle edit permissions for name, phone, and amount fields
- **Modern UI**: Material Design 3 with smooth animations

## 🛠 Technology Stack

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository Pattern
- **Database**: Room (SQLite)
- **Dependency Injection**: Hilt
- **Background Work**: WorkManager for notifications
- **Date/Time**: Kotlinx DateTime
- **File I/O**: Apache POI (Excel), OpenCSV
- **Permissions**: Accompanist Permissions

## 📱 App Structure

### Core Components

#### Data Layer
- `Customer` - Main customer entity with editable field toggles
- `FollowUp` - Follow-up records with timestamps and notes
- `CustomerDao` & `FollowUpDao` - Database access objects
- `CustomerRepository` - Data management layer
- `PreferencesRepository` - Settings and preferences storage

#### UI Layer
- `MainActivity` - Entry point with theme management
- `CustomerListScreen` - Main screen showing today's calls
- `AddEditCustomerScreen` - Customer form with validation
- `SettingsScreen` - App configuration and data management
- `CallDialog` - In-call interface with follow-up form

#### Business Logic
- `CustomerViewModel` - Customer operations and state management
- `SettingsViewModel` - App settings and data export/import
- `NotificationWorker` - Daily reminder notifications
- `NotificationScheduler` - Manages notification timing

### Key Features Implementation

#### Smart Customer Display
```kotlin
// Shows only customers with today's promise date by default
val todayCustomers = customerRepository.getCustomersWithFollowUpsForDate(getCurrentDate())
```

#### Color-Coded Follow-up Status
```kotlin
enum class FollowUpStatus {
    LOW,      // 0-5 follow-ups: Light color
    MEDIUM,   // 6-10 follow-ups: Slightly darker  
    HIGH,     // 11-20 follow-ups: Even darker
    CRITICAL  // 20+ follow-ups: Dark red
}
```

#### Auto-calling Flow
- Floating action button switches between "Start Call" and "End Call"
- Sequential progression through today's customer list
- Call dialog shows customer details and previous follow-ups
- Auto-advances to next customer after update

#### Data Export/Import
- Excel (.xlsx) and CSV export with full customer and follow-up data
- Import functionality with data validation and error handling
- File sharing integration for cross-device data transfer

## 🔧 Setup and Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 26+ (minimum)
- Android SDK 34 (target)
- Kotlin 1.9.22+

### Build Instructions
1. Clone or extract the project to your local machine
2. Open Android Studio and select "Open an existing project"
3. Navigate to the `/android-app` folder and select it
4. Wait for Gradle sync to complete
5. Connect an Android device or start an emulator
6. Click Run or press Ctrl+R (Cmd+R on Mac)

### Dependencies
All dependencies are managed through Gradle and will be automatically downloaded:
- Jetpack Compose BOM 2024.02.00
- Room Database 2.6.1
- Hilt 2.48
- WorkManager 2.9.0
- Accompanist Permissions 0.32.0
- Apache POI 5.2.4 (Excel support)
- OpenCSV 5.8

## 📋 Permissions Required

- `CALL_PHONE` - Direct phone calling functionality
- `POST_NOTIFICATIONS` - Daily reminder notifications
- `SCHEDULE_EXACT_ALARM` - Precise notification timing
- `READ_EXTERNAL_STORAGE` - File import (Android 12 and below)
- `WRITE_EXTERNAL_STORAGE` - File export (Android 10 and below)
- `READ_MEDIA_*` - File access (Android 13+)

## 🎨 User Interface

### Main Screens
1. **Customer List**: Shows today's calls with quick actions
2. **Add/Edit Customer**: Comprehensive form with field toggles
3. **Settings**: Theme, notifications, and data management

### Key UI Components
- **Customer Cards**: Color-coded with follow-up indicators
- **Floating Action Button**: Call mode toggle
- **Call Dialog**: In-call interface with follow-up form
- **Sort Options**: Multiple sorting criteria
- **Date Pickers**: Material Design 3 date selection

### Design Principles
- Material Design 3 compliance
- Accessibility-first approach
- Responsive layouts for different screen sizes
- Smooth animations and transitions
- Dark mode support throughout

## 📊 Data Management

### Database Schema
- **customers** table: Core customer information
- **followups** table: Timestamped follow-up records
- Room database with proper migrations support

### Export Format
CSV and Excel exports include:
- Customer details (name, phone, amount, dates, notes)
- Field edit permissions
- Complete follow-up history with timestamps
- Creation and modification dates

### Data Validation
- Phone number format validation
- Amount numerical validation
- Required field enforcement
- Date constraint validation

## 🔔 Notification System

### Daily Reminders
- Multiple notification times per day
- Customizable time selection (15-minute intervals)
- Shows count of customers to call today
- Launches app when tapped

### Implementation
- WorkManager for reliable background execution
- Notification channels for Android 8.0+ compatibility
- Exact alarm permissions for precise timing
- Battery optimization handling

## 🚧 Future Enhancements

### Potential Features
- Voice memo support for follow-ups
- Advanced search and filtering
- Customer communication history
- Payment tracking integration
- Backup to cloud storage
- Multi-language support
- Advanced analytics and reporting

### Technical Improvements
- Offline-first architecture
- Data synchronization
- Advanced security features
- Widget support
- Wear OS companion app

## 🧪 Testing

### Test Coverage Areas
- Unit tests for ViewModels and repositories
- Integration tests for database operations
- UI tests for critical user flows
- Permission handling tests
- File import/export validation

### Running Tests
```bash
./gradlew test           # Run unit tests
./gradlew connectedCheck # Run instrumented tests
```

## 📄 License

This project is developed as a comprehensive Android application example showcasing modern Android development practices with Jetpack Compose, Room database, and Material Design 3.

## 🤝 Contributing

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Document complex business logic
- Maintain consistent formatting

### Architecture Guidelines
- Respect MVVM pattern separation
- Use repository pattern for data access
- Implement proper error handling
- Follow single responsibility principle

---

**Built with ❤️ using modern Android development tools and best practices**