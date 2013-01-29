@echo off

setlocal

rem 
rem Set JAVA_HOME and J2EE_HOME before running this script.
rem 
rem set JAVA_HOME to the path where you have Java 2 installed.
rem
rem set J2EE_HOME to the path where you have installed this package (EJB server).
rem

if not "%J2EE_HOME%" == "" goto CONT0
echo ERROR: Set J2EE_HOME before running this script.
goto END
:CONT0

if EXIST "%J2EE_HOME%\bin\run.bat" goto CONT1
echo ERROR: Set J2EE_HOME to the path of a valid JBoss installation.
goto END
:CONT1

if not "%JAVA_HOME%" == "" goto CONT2
echo ERROR: Set JAVA_HOME before running this script.
goto END
:CONT2

if EXIST "%JAVA_HOME%\bin\java.exe" goto CONT3
echo ERROR: Set JAVA_HOME to the path of a valid jdk.
goto END
:CONT3

if EXIST "bin\start.cmd" goto CONT4
echo ERROR: you must run this script from the Funambol DM Server home directory
goto END
:CONT4

REM
REM Other settings
REM


%JAVA_HOME%\bin\java.exe -classpath .\lib;%J2EE_HOME%\bin\client\jboss-client.jar;.\default\lib\funambol-server.jar;.\default\lib\funambol-framework.jar;.\default\lib\dmdemo.jar;.\lib\funambol-dm-ext-3.1.0.jar com.funambol.bootstrap.client.ClientBootstrap %*
:END
endlocal
