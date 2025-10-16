name: Build App

on:
  push:
    # Trigger only for main branch.
    branches: [ main ]
  # Allows you to run this workflow manually from the Actions tab
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      # Get the app source
      - name: Get the source
        uses: actions/checkout@v3

      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      # Make gradlew executable
      - name: Make gradlew executable
        run: chmod +x ./gradlew
        working-directory: android

      # Compile the apk
      - name: Generate Debug APK
        run: ./gradlew assembleDebug
        working-directory: android

      # Upload the action artifact
      - name: Upload Debug APK (artifact)
        uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: android/app/build/outputs/apk/debug/*.apk

      # Generate Release APK (without signing for now)
      - name: Generate Release APK
        run: ./gradlew assembleRelease
        working-directory: android

      # Upload Release APK
      - name: Upload Release APK (artifact)
        uses: actions/upload-artifact@v4
        with:
          name: release-apk
          path: android/app/build/outputs/apk/release/*.apk