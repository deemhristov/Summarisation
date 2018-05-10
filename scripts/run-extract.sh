#!/bin/bash

if [ ! -f "$1" ]; then exit; fi
if [ -z "$2" ]; then LIM="0"; else LIM="$2"; fi

while read COMMAND; do
    echo -e "$COMMAND"
    read COMMAND
    bash <<<"$COMMAND" >/dev/null &
done <"$1"

wait
