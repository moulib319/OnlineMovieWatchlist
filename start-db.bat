@echo off
setlocal
set "BASE=C:\Program Files\MySQL\MySQL Server 8.0"
set "DATA=%LOCALAPPDATA%\MovieWatchlist\data"

if not exist "%BASE%\bin\mysqld.exe" (
    echo Could not find MySQL at "%BASE%".
    echo Edit this file and change BASE to your MySQL installation folder.
    pause
    exit /b 1
)

if not exist "%DATA%\mysql" (
    echo Initializing local MySQL data folder at "%DATA%" ...
    "%BASE%\bin\mysqld.exe" --no-defaults --initialize-insecure --basedir="%BASE%" --datadir="%DATA%"
)

powershell -NoProfile -Command "if (Get-NetTCPConnection -LocalPort 3307 -State Listen -ErrorAction SilentlyContinue) { exit 0 } else { exit 1 }"
if %errorlevel%==0 (
    echo MySQL is already running on 127.0.0.1:3307.
    goto :ready
)

echo Starting MySQL on 127.0.0.1:3307 ...
start "MovieWatchlistDB" /min "%BASE%\bin\mysqld.exe" --no-defaults --basedir="%BASE%" --datadir="%DATA%" --port=3307 --bind-address=127.0.0.1 --skip-mysqlx --log-error="%DATA%\server.log"

set /a tries=0
:wait
powershell -NoProfile -Command "if (Get-NetTCPConnection -LocalPort 3307 -State Listen -ErrorAction SilentlyContinue) { exit 0 } else { exit 1 }"
if %errorlevel%==0 goto :ready
set /a tries+=1
if %tries% GEQ 30 (
    echo MySQL did not start in time. Check "%DATA%\server.log".
    pause
    exit /b 1
)
timeout /t 1 /nobreak >nul
goto :wait

:ready
echo MySQL is ready on 127.0.0.1:3307 (database: movie_watchlist_db).
endlocal
