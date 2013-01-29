@echo off

setlocal

rem 
rem Set JAVA_HOME and J2EE_HOME before running this script.
rem 
rem set JAVA_HOME to the path where you have Java 1.6 installed.
rem
rem set J2EE_HOME to the path where you have installed this package (EJB server).
rem

if not "%J2EE_HOME%" == "" goto CONT0
echo ERROR: Set J2EE_HOME before running this script.
goto END
:CONT0

if EXIST "%J2EE_HOME%\bin\standalone.bat" goto CONT1
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
echo ERROR: you must run this script from the Funambol home directory
goto END
:CONT4

REM
REM Other settings
REM
set JAVA_OPTS=-Dfunambol.dm.home="%~dp0.." -Dfile.encoding=UTF-8

REM #JBoss options
set "JAVA_OPTS=%JAVA_OPTS% -Xms64M -Xmx512M -XX:MaxPermSize=256M"
set "JAVA_OPTS=%JAVA_OPTS% -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 -Djava.net.preferIPv4Stack=true"
set "JAVA_OPTS=%JAVA_OPTS% -Dorg.jboss.resolver.warning=true"
set "JAVA_OPTS=%JAVA_OPTS% -Djboss.modules.system.pkgs=org.jboss.byteman"
set "JAVA_OPTS=%JAVA_OPTS% -Djboss.server.default.config=standalone.xml"

cd /D "%J2EE_HOME%"\bin

call standalone.bat -c funambol-standalone.xml -b 0.0.0.0

:END
endlocal
