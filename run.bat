@echo off
set JAVA_PATH=java
where %JAVA_PATH% >nul 2>nul
if %errorlevel% neq 0 (
    echo 'java' not found in PATH. Checking Android Studio JBR...
    if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
        set JAVA_PATH="C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
        echo Found java in Android Studio JBR!
    ) else (
        echo 'java' could not be found. Please ensure Java is installed.
        pause
        exit /b 1
    )
)

echo Starting Electricity Bill Generator...
start "" %JAVA_PATH% -cp "bin;lib/*" com.electricity.gui.ElectricityBillGeneratorApp
