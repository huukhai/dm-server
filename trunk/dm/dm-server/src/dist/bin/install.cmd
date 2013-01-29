@echo OFF

if not "%JAVA_HOME%" == "" goto CONT2
echo . ==================================================
echo .
echo . Please, set JAVA_HOME before running this script.
echo .
echo . ==================================================
goto END
:CONT2

if EXIST "%JAVA_HOME%\bin\java.exe" goto CONT3
echo . ==================================================
echo .
echo . Please, set JAVA_HOME to the path of a valid jdk.
echo .
echo . ==================================================
goto END
:CONT3

if EXIST "bin\install.cmd" goto CONT4
echo . =========================================================
echo .
echo . Please, run this script from the Funambol home directory
echo .
echo . =========================================================
goto END
:CONT4

if not "%J2EE_HOME%" == "" goto CONT5
echo . ==================================================
echo .
echo . Please, set J2EE_HOME before running this script.
echo .
echo . ==================================================
goto END
:CONT5

if /i "%1" == "jboss7"  goto JBOSS

echo . ==================================================
echo .
echo . Please, specify target application sever.
echo . Syntax:
echo .     install jboss7
echo .
echo . ==================================================
goto END:

:JBOSS

set ANT_HOME=
set APPSRV=%1

call ant\bin\ant -Ddo.install-db=true -buildfile install\install.xml -q install

:END
