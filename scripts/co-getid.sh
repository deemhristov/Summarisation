#!/bin/bash

FILE=$(readlink -f "$1")
BASE=$(basename "$FILE")

RID="L?[0-9]{8}[a-z][A-Z]{3}"
RBGTXT="^($RID)\.txt$"
RBGSEG="^($RID)\.txt-SEG$"
RBGCSV="^($RID)\.csv$"
RENTXT="^($RID)en\.txt$"
RENCSV="^en__($RID)\.csv$"

TYPE="unknown"
ID="UNKNOWN"

if [[ $BASE =~ $RBGTXT ]]; then TYPE="bg-txt"; ID=${BASH_REMATCH[1]}; fi
if [[ $BASE =~ $RBGSEG ]]; then TYPE="bg-seg"; ID=${BASH_REMATCH[1]}; fi
if [[ $BASE =~ $RBGCSV ]]; then TYPE="bg-csv"; ID=${BASH_REMATCH[1]}; fi
if [[ $BASE =~ $RENTXT ]]; then TYPE="en-txt"; ID=${BASH_REMATCH[1]}; fi
if [[ $BASE =~ $RENCSV ]]; then TYPE="en-csv"; ID=${BASH_REMATCH[1]}; fi

echo $ID
