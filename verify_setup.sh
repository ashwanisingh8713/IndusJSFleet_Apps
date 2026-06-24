#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "🔍 Verifying IndusJS Fleet Android Studio Setup...\n"

# Check 1: local.properties exists and has sdk.dir
echo -n "✓ Checking local.properties... "
if [ -f "local.properties" ]; then
    if grep -q "sdk.dir=" local.properties; then
        echo -e "${GREEN}OK${NC}"
    else
        echo -e "${RED}MISSING sdk.dir${NC}"
        echo "  Add to local.properties: sdk.dir=/Users/ashwani/Library/Android/sdk"
    fi
else
    echo -e "${RED}NOT FOUND${NC}"
    echo "  Create local.properties with: sdk.dir=/Users/ashwani/Library/Android/sdk"
fi

# Check 2: Gradle wrapper
echo -n "✓ Checking gradle wrapper... "
if [ -f "gradlew" ] && [ -x "gradlew" ]; then
    echo -e "${GREEN}OK${NC}"
else
    echo -e "${RED}MISSING or NOT EXECUTABLE${NC}"
fi

# Check 3: Android SDK
echo -n "✓ Checking Android SDK... "
if [ -d "$HOME/Library/Android/sdk" ]; then
    echo -e "${GREEN}OK${NC}"
else
    echo -e "${RED}NOT FOUND${NC}"
    echo "  Install via Android Studio: Tools → SDK Manager"
fi

# Check 4: JDK version
echo -n "✓ Checking JDK version... "
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | grep version | head -1)
    if echo "$JAVA_VERSION" | grep -qE "17|18|19|20|21"; then
        echo -e "${GREEN}$JAVA_VERSION${NC}"
    else
        echo -e "${YELLOW}WARNING: ${NC}$JAVA_VERSION"
        echo "  Project requires JDK 17+. Configure in Android Studio Preferences → Gradle"
    fi
else
    echo -e "${RED}NOT FOUND${NC}"
    echo "  Install JDK 17+ or use Android Studio's bundled JDK"
fi

# Check 5: Gradle build
echo -n "✓ Testing Gradle... "
if ./gradlew --version &> /dev/null; then
    echo -e "${GREEN}OK${NC}"
else
    echo -e "${RED}FAILED${NC}"
    echo "  Run: ./gradlew clean"
fi

# Check 6: androidApp module exists
echo -n "✓ Checking androidApp module... "
if [ -d "androidApp" ] && [ -f "androidApp/build.gradle.kts" ]; then
    echo -e "${GREEN}OK${NC}"
else
    echo -e "${RED}NOT FOUND${NC}"
fi

# Check 7: google-services.json exists
echo -n "✓ Checking Firebase config... "
if [ -f "androidApp/google-services.json" ]; then
    echo -e "${GREEN}OK${NC}"
else
    echo -e "${RED}NOT FOUND${NC}"
    echo "  Firebase integration file missing (may cause runtime issues)"
fi

echo "\n${GREEN}✓ Setup verification complete!${NC}"
echo ""
echo "📖 Next steps:"
echo "  1. Open the project in Android Studio (File → Open)"
echo "  2. Wait for Gradle sync to complete"
echo "  3. Connect a device or start an emulator"
echo "  4. Click the Run button (▶️) or press Ctrl+R"
echo ""
echo "For detailed instructions, see: ANDROID_STUDIO_SETUP.md"
