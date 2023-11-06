#!/bin/bash

cd `dirname $0`/../target
target_dir=`pwd`

pid=`ps ax | grep -i 'destino-egolessness' | grep ${target_dir} | grep java | grep -v grep | awk '{print $1}'`
if [ -z "$pid" ] ; then
        echo "No destino server is running."
        exit -1;
fi

echo "The destino server (${pid}) is running..."

kill -15 ${pid}

echo "Send termination command to destino server (${pid}) OK!"
