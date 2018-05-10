#!/bin/bash

NAMES="$( sed '$d' "$3" | grep -E "^\s*$4[0-9]{3}\s" | sed "s/\s*[0-9]*\s\.\///" | sed "s/\.txt//" )"

mkdir "$2/$4xxx"
for FI in $NAMES; do
  cp "$1/$FI.txt" "$2/$4xxx/"
  cp "$1/$FI.csv" "$2/$4xxx/"
done
