@rem ##########################################################################
@rem  Gradle startup script for Windows
@rem ##########################################################################
@if "%DEBUG%" == "" @echo off
@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal
set APP_HOME=%~dp0
set APP_BASE_NAME=%~n0
set WRAPPER_JAR="%APP_HOME%gradle\wrapper\gradle-wrapper.jar"
set WRAPPER_BATCH=%APP_HOME%gradlew.bat
"%JAVA_HOME%\bin\java.exe" -classpath %WRAPPER_JAR% org.gradle.wrapper.GradleWrapperMain %*
