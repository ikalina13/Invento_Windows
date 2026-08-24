@echo off
cd /d "%~dp0"
echo Starting Invento...
call mvnw.cmd javafx:run
if errorlevel 1 pause
