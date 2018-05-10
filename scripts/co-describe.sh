#!/bin/bash

#CCNT=$(wc -m < "$1")

NAME=$(basename "$1")
TMP=$NAME

if [[ $TMP == en__* ]]; then ENPR="en__"; fi
TMP=${TMP#$ENPR}

if [[ $TMP == L* ]]; then LONG="L"; fi
TMP=${TMP#$LONG}

ID=${TMP:0:8}
TMP=${TMP#$ID}

BN=${TMP:0:1}
TMP=${TMP#$BN}

CD=${TMP:0:3}
TMP=${TMP#$CD}

if [[ $TMP == en* ]]; then ENSU="en"; fi
TMP=${TMP#$ENSU}

EXT=$TMP
TMP=${TMP#$EXT}

#printf "%8d%6s%3s%10D%3s%5s%4s  %s\n" "$CCNT" "$ENPR" "$LONG" "$ID" "$BN" "$CD" "$ENSU" "$EXT"
printf "%s\t%s\t%s\t%s\t%s\t%s\t%s\n" "$ENPR" "$LONG" "$ID" "$BN" "$CD" "$ENSU" "$EXT"
