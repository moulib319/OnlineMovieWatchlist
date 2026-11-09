@echo off
cd /d "%~dp0"

if not exist out mkdir out

javac -d out src\Main.java src\model\Movie.java src\model\Watchlist.java src\storage\FileStorage.java src\ui\MainFrame.java
if errorlevel 1 (
    echo.
    echo Compilation failed.
    pause
    exit /b 1
)

java -cp out Main
pause
