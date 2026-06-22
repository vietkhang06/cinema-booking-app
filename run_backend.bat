@echo off
set "JAVA_HOME=C:\Program Files\Android\openjdk\jdk-21.0.8"
cd /d "%~dp0cinema-booking-backend"
"C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.2\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.profiles=local
