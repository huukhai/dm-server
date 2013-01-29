#!/bin/sh

if [ -z $JAVA_HOME ]
then
    echo ". =================================================="
    echo "."
    echo ". Please, set JAVA_HOME before running this script."
    echo "."
    echo ". =================================================="
    exit;
fi

if [ ! -f "$JAVA_HOME/bin/java" ]
then
    echo ". =================================================="
    echo "."
    echo ". Please, set JAVA_HOME to the path of a valid jdk."
    echo "."
    echo ". =================================================="
    exit;
fi

if [ -z $J2EE_HOME ]
then
    echo ". =================================================="
    echo "."
    echo ". Please, set J2EE_HOME before running this script."
    echo "."
    echo ". =================================================="
    exit;
fi

if [ ! -f bin/install.sh ]
then
    echo ". ========================================================="
    echo "."
    echo ". Please, run this script from the Funambol home directory "
    echo "."
    echo ". ========================================================="
    exit;
fi
 
if [ "$1" != "jboss7" ]
then
   echo ". =================================================="
   echo ".                                                   "
   echo ". Please, specify target application sever.         "
   echo ". Syntax:                                           "
   echo ".     install jboss7                              "
   echo ".                                                   "
   echo ". =================================================="
   exit;
fi

# just in case...
chmod +x ./ant/bin/*
unset ANT_HOME

export APPSRV="$1"

./ant/bin/ant -Ddo.install-db=true --noconfig -buildfile ./install/install.xml -q install

# In case the installation created new scripts
chmod +x ./bin/*.sh
