#!/bin/sh

#
# Set JAVA_HOME and J2EE_HOME before running this script.
#
# set JAVA_HOME to the path where you have Java 1.6 installed.
#
# set J2EE_HOME to the path where you have installed this package (EJB server).
#

if [ -z $JAVA_HOME ]
then
    echo "Please, set JAVA_HOME before running this script."
    exit;
fi

if [ -z $J2EE_HOME ]
then
    echo "Please, set J2EE_HOME before running this script."
    exit;
fi

if [ ! -f bin/start.sh ]
then
    echo "Please start this script from the funambol home directory."
    exit;
fi

#
# Other settings
#
FUNAMBOL_HOME=$PWD
JAVA_OPTS="-Dfunambol.dm.home=$FUNAMBOL_HOME -Dfile.encoding=UTF-8"

#
# Uncomment the following line to enable remote debugging
#
#JAVA_OPTS="-classic -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n $JAVA_OPTS"

export JAVA_OPTS;

#
# Run JBoss
#
cd $J2EE_HOME/bin

sh ./standalone.sh -c funambol-standalone.xml -b 0.0.0.0

