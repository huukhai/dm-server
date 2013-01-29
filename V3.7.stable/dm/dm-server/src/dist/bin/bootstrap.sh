#!/bin/sh

#
# Set JAVA_HOME and J2EE_HOME before running this script.
#
# set JAVA_HOME to the path where you have Java 2 installed.
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
    echo "Please start this script from the Funambol DM Server home directory."
    exit;
fi


$JAVA_HOME/bin/java -classpath ./lib:$J2EE_HOME/bin/client/jboss-client.jar:./default/lib/funambol-server.jar:./default/lib/funambol-framework.jar:./default/lib/dmdemo.jar:./lib/{funambol-dm-ext.jar} com.funambol.bootstrap.client.ClientBootstrap $*
