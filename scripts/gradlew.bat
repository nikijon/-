@echo off
setlocal
set BASEDIR=%~dp0
if not defined GRADLE_HOME (
  set "GRADLE_HOME=%BASEDIR%\.gradle\gradle"
)
"%GRADLE_HOME%\bin\gradle" %*
