@echo off
set "JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\jbr"
cd /d "%~dp0cinema-booking-backend"
"C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.profiles=local
