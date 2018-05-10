#!/bin/bash

FILE=$(readlink -f "$1")
BASE=$(basename "$FILE")

RID="L?[0-9]{8}[a-z][A-Z]{3}"
RBGTXT="^($RID)-bg\.txt$"
RBGSEN="^($RID)-bg-sen\.txt$"
RBGCLA="^($RID)-bg-cla\.txt$"

TYPE="unknown"
ID="UNKNOWN"

if [[ $BASE =~ $RBGTXT ]]; then TYPE="bg-txt"; ID=${BASH_REMATCH[1]}; fi
if [[ $BASE =~ $RBGSEN ]]; then TYPE="bg-sen"; ID=${BASH_REMATCH[1]}; fi
if [[ $BASE =~ $RBGCLA ]]; then TYPE="bg-cla"; ID=${BASH_REMATCH[1]}; fi

echo $ID
