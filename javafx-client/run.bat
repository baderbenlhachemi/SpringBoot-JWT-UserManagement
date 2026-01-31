@echo off
title JWT User Management - JavaFX Client
echo ================================================
echo   JWT User Management - JavaFX Client
echo ================================================
echo.
echo Make sure the Spring Boot backend is running on localhost:9090
echo.
echo Starting the application...
echo.
cd /d "%~dp0"
call ..\mvnw.cmd -f pom.xml javafx:run
echo.
echo ================================================
echo Application has closed.
echo ================================================
pause
