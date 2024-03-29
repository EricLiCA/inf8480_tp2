#!/bin/bash

pushd $(dirname $0) > /dev/null
basepath=$(pwd)
popd > /dev/null

cat << EndOfMessage
HELP:
./nameService.sh ip_address port
EndOfMessage

IPADDR=$1
if [ -z "$1" ]
  then
    IPADDR="127.0.0.1"
fi

java -cp "$basepath"/nameService.jar \
  -Djava.rmi.server.codebase=file:"$basepath"/nameService.jar \
  -Djava.security.policy="$basepath"/policy \
  -Djava.rmi.server.hostname="$IPADDR" \
  ca.polymtl.inf8480.tp1.nameService.NameService $*
