#!/bin/sh

CONFIG4J_HOME=../../..
CONFIG4JMS_HOME=$CONFIG4J_HOME/config4jms

CLASSPATH=$CONFIG4J_HOME/lib/config4j.jar:$CLASSPATH
CLASSPATH=$CONFIG4JMS_HOME/samples/:$CLASSPATH
CLASSPATH=$CONFIG4JMS_HOME/lib/config4jms.jar:$CLASSPATH
CLASSPATH=$SONICMQ_HOME/lib/sonic_Client.jar:$CLASSPATH
CLASSPATH=$SONICMQ_HOME/lib/mfcontext.jar:$CLASSPATH
CLASSPATH=$SONICMQ_HOME/lib/gnu-regexp-1.0.6.jar:$CLASSPATH
CLASSPATH=$SONICMQ_HOME/lib/rsa_ssl.jar:$CLASSPATH
export CLASSPATH

java chat.Chat $*
