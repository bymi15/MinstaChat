@echo off
set /p port="Enter a port: "
java -jar MinstaChatServer.jar %port%
pause