@echo off
set JAVAC_PATH=javac
where %JAVAC_PATH% >nul 2>nul
if %errorlevel% neq 0 (
    echo 'javac' not found in PATH. Checking Android Studio JBR...
    if exist "C:\Program Files\Android\Android Studio\jbr\bin\javac.exe" (
        set JAVAC_PATH="C:\Program Files\Android\Android Studio\jbr\bin\javac.exe"
        echo Found javac in Android Studio JBR!
    ) else (
        echo 'javac' could not be found. Please ensure JDK is installed and added to PATH.
        pause
        exit /b 1
    )
)

echo Compiling...
if not exist bin mkdir bin
%JAVAC_PATH% --release 8 -d bin -cp "lib/*" src/com/electricity/model/*.java src/com/electricity/calculator/*.java src/com/electricity/db/*.java src/com/electricity/exception/*.java src/com/electricity/gui/*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)
echo Compilation successful!
pause
