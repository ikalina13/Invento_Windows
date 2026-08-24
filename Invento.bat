@echo off
title Invento
cd /d "%~dp0"
start "" /b "%~dp0mvnw.cmd" -q javafx:run
