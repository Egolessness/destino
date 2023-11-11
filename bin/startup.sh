#!/bin/bash

cygwin=false
darwin=false
os400=false
case "`uname`" in
CYGWIN*) cygwin=true;;
Darwin*) darwin=true;;
OS400*) os400=true;;
esac
error_exit ()
{
    echo "ERROR: $1 !!"
    exit 1
}
[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=$HOME/jdk/java
[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=/usr/java
[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=/opt/taobao/java
[ ! -e "$JAVA_HOME/bin/java" ] && unset JAVA_HOME

if [ -z "$JAVA_HOME" ]; then
  if $darwin; then

    if [ -x '/usr/libexec/java_home' ] ; then
      export JAVA_HOME=`/usr/libexec/java_home`

    elif [ -d "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home" ]; then
      export JAVA_HOME="/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home"
    fi
  else
    JAVA_PATH=`dirname $(readlink -f $(which javac))`
    if [ "x$JAVA_PATH" != "x" ]; then
      export JAVA_HOME=`dirname $JAVA_PATH 2>/dev/null`
    fi
  fi
  if [ -z "$JAVA_HOME" ]; then
        error_exit "Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better!"
  fi
fi

export VERSION="1.0.1"
export MODE="cluster"
export SERVER="destino-server-${VERSION}"
export CLUSTER_MEMBERS=""
export JAVA="$JAVA_HOME/bin/java"
export BASE_DIR=`cd $(dirname $0)/..; pwd`
export CONFIG_LOCATION=${BASE_DIR}/conf/

while getopts ":m:s:c:" opt
do
    case $opt in
        m)
            MODE=$OPTARG;;
        s)
            SERVER=$OPTARG;;
        c)
            CLUSTER_MEMBERS=$OPTARG;;
        ?)
        echo "Unknown parameter"
        exit 1;;
    esac
done

export JVM_OPTS="-server -jar ${BASE_DIR}/target/${SERVER}.jar -Xms4g -Xmx4g -Xmn2g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=320m -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${BASE_DIR}/logs/java_heapdump.hprof -XX:-UseLargePages"
export DESTINO_OPTS="-Ddestino.home=${BASE_DIR} -Dserver.mode=${MODE} -Ddestino.cluster.nodes=${CLUSTER_MEMBERS}"

if [[ "${MODE}" == "standalone" ]] || [[ "${MODE}" == "monolithic" ]] || [[ "${MODE}" == "mono" ]] ; then
  JVM_OPTS="-Xms512m -Xmx512m -Xmn256m"
  echo "destino is starting in ${MODE} mode"
else
  echo "destino is starting in cluster mode"
fi

JAVA_MAJOR_VERSION=$($JAVA -version 2>&1 | sed -E -n 's/.* version "([0-9]*).*$/\1/p')
if [[ "$JAVA_MAJOR_VERSION" -ge "9" ]] ; then
  JVM_OPTS="${JVM_OPTS} -Xlog:gc*:file=${BASE_DIR}/logs/destino_gc.log:time,tags:filecount=10,filesize=100m"
else
  JVM_OPTS_EXT_FIX="-Djava.ext.dirs=${JAVA_HOME}/jre/lib/ext:${JAVA_HOME}/lib/ext"
  JVM_OPTS="${JVM_OPTS} ${JVM_OPTS_EXT} -Xloggc:${BASE_DIR}/logs/destino_gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M"
fi

JVM_OPTS="${JVM_OPTS} -Ddestino.config.location=${CONFIG_LOCATION}"
JVM_OPTS="${JVM_OPTS} -Ddestino.logging.config=${BASE_DIR}/conf/destino-log4j.yml"

if [ ! -d "${BASE_DIR}/logs" ]; then
  mkdir ${BASE_DIR}/logs
fi

echo "$JAVA $JVM_OPTS_EXT_FIX ${JVM_OPTS}"

if [ ! -f "${BASE_DIR}/logs/start.out" ]; then
  touch "${BASE_DIR}/logs/start.out"
fi

echo "$JAVA $JVM_OPTS_EXT_FIX ${JVM_OPTS}" > ${BASE_DIR}/logs/start.out 2>&1 &

if [[ "$JVM_OPTS_EXT_FIX" == "" ]]; then
  nohup "$JAVA" ${DESTINO_OPTS} ${JVM_OPTS} destino-egolessness >> ${BASE_DIR}/logs/start.out 2>&1 &
else
  nohup "$JAVA" "$JVM_OPTS_EXT_FIX" ${DESTINO_OPTS} ${JVM_OPTS} destino-egolessness >> ${BASE_DIR}/logs/start.out 2>&1 &
fi
