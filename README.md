# Fifteen Puzzle Game - Android Documentation

## Overview

The Fifteen Puzzle Game is a classic sliding puzzle game implemented as an Android application. The game consists of a 4×4 grid with 15 numbered tiles and one empty space. Players slide tiles into the empty space to arrange them in numerical order from 1 to 15.

## Project Structure

```
FifteenPuzzleGame/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/fifteenpuzzle/
│   │   │   │   └── MainActivity.java
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   └── activity_main_mobile.xml
│   │   │   │   ├── mipmap-*/
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── values-night/
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/
│   │   └── test/
│   ├── proguard-rules.pro
│   └── build.gradle
├── gradle/
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle
```

## Features

### Core Gameplay
- **4×4 Grid Interface**: Classic fifteen puzzle layout with 15 numbered tiles
- **Touch Controls**: Tap tiles adjacent to empty space to move them
- **Puzzle Shuffling**: Random initial puzzle configuration
- **Win Detection**: Automatic detection when puzzle is solved
- **Responsive Design**: Optimized for different screen sizes

### Technical Features
- **Android Native**: Built using Java and Android SDK
- **Adaptive Layouts**: Separate layouts for different screen orientations
- **Material Design**: Modern Android UI components
- **Dark Mode Support**: Theme support for day/night modes

## Installation & Setup

### Prerequisites
- Android Studio (latest version recommended)
- Android SDK API Level 21 or higher
- Java 8 or higher
- Gradle 7.0+

### Getting Started

1. **Clone the Repository**
   ```bash
   git clone https://github.com/s0ura8hs/FifteenPuzzleGame.git
   cd FifteenPuzzleGame
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the cloned repository folder
   - Click "OK" to open the project

3. **Build the Project**
   - Wait for Gradle sync to complete
   - Build → Make Project (Ctrl+F9)

4. **Run the Application**
   - Connect an Android device or start an emulator
   - Run → Run 'app' (Shift+F10)

## Architecture

### MainActivity.java
The main activity serves as the core controller for the game, handling:
- Game state management
- User input processing
- UI updates
- Win condition checking
- Puzzle generation and shuffling

### Layout Files
- **activity_main.xml**: Primary layout for the game interface
- **activity_main_mobile.xml**: Mobile-optimized layout variant

### Resources
- **colors.xml**: Color scheme definitions
- **strings.xml**: Text resources and localization
- **themes.xml**: App theme configurations including dark mode support

## Game Logic

### Puzzle Mechanics
1. **Initialization**: Generate a solvable random puzzle configuration
2. **Move Validation**: Check if selected tile can move to empty space
3. **Tile Movement**: Swap positions of selected tile and empty space
4. **Win Detection**: Check if tiles are in correct numerical order
5. **Game Reset**: Shuffle puzzle for new game

### Algorithm Considerations
- **Solvability Check**: Ensures generated puzzles are actually solvable
- **Optimal Shuffling**: Maintains puzzle solvability during randomization
- **Efficient Movement**: Direct tile-to-empty-space swapping

## User Interface

### Main Screen Elements
- **Game Grid**: 4×4 interactive tile grid
- **Tiles**: Numbered buttons (1-15) with touch handlers
- **Empty Space**: Invisible tile representing the moveable space
- **Game Controls**: New game, reset, and menu options
- **Status Display**: Move counter, timer, or win message

### Responsive Design
- Adapts to different screen sizes and orientations
- Touch-friendly tile sizing
- Consistent spacing and alignment
- Accessibility considerations

## Configuration

### Build Configuration
```gradle
android {
    compileSdkVersion 34
    defaultConfig {
        applicationId "com.example.fifteenpuzzle"
        minSdkVersion 21
        targetSdkVersion 34
        versionCode 1
        versionName "1.0"
    }
}
```

### Permissions
No special permissions required - the app runs entirely offline.

## Testing

### Unit Tests
- Located in `src/test/java/com/example/fifteenpuzzle/`
- Test game logic and puzzle algorithms
- Run with: `./gradlew test`

### Instrumented Tests
- Located in `src/androidTest/java/com/example/fifteenpuzzle/`
- Test UI interactions and Android-specific functionality
- Run with: `./gradlew connectedAndroidTest`

## Development Guidelines

### Code Style
- Follow Android development best practices
- Use descriptive variable and method names
- Include javadoc comments for public methods
- Maintain consistent indentation and formatting

### Adding Features
When extending the game, consider:
- Different grid sizes (3×3, 5×5)
- Time tracking and high scores
- Hint system
- Animation effects
- Sound effects
- Multiple difficulty levels

### Performance Optimization
- Minimize object creation in game loops
- Use efficient data structures for grid representation
- Implement smooth animations without blocking UI thread
- Optimize for different device capabilities

## Troubleshooting

### Common Issues

**Build Errors**
- Ensure all SDK components are installed
- Check Gradle version compatibility
- Verify Java version requirements

**Runtime Issues**
- Check device API level compatibility
- Verify app permissions if needed
- Monitor logcat for detailed error messages

**UI Problems**
- Test on different screen sizes
- Check layout resource configurations
- Verify theme and style applications

## Contributing

### Development Setup
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

### Code Standards
- Follow Android coding conventions
- Write clear commit messages
- Include tests for new features
- Update documentation as needed

## License

This project is open source. Please check the repository for specific license information.

## Future Enhancements

### Planned Features
- Multiple puzzle sizes
- Difficulty levels
- Statistics tracking
- Online leaderboards
- Achievement system
- Custom tile themes
- Solver algorithm demonstration

### Technical Improvements
- Kotlin migration
- Modern Android architecture (MVVM)
- Room database for persistence
- Jetpack Compose UI
- Automated testing pipeline

## Support

For issues, questions, or contributions:
- Create an issue on GitHub
- Fork the repository for contributions
- Follow Android development best practices

---

*Last updated: June 2025*
