@echo off
setlocal
set "BASE=C:\Program Files\MySQL\MySQL Server 8.0"

if not exist "%BASE%\bin\mysqladmin.exe" (
    echo Could not find MySQL at "%BASE%".
    pause
    exit /b 1
)

echo Stopping the local MySQL instance on 127.0.0.1:3307 ...
"%BASE%\bin\mysqladmin.exe" --protocol=TCP -h 127.0.0.1 -P 3307 -u root -pRoot@1234 shutdown
echo Done.
endlocal
