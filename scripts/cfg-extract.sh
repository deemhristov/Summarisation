#!/bin/bash

if [ ! -f "$1" ]; then exit; fi

source "$1"

SDIR="$(dirname "$(readlink -f "$0")")"

INDIR="$RDIR/$INDIR"
OUTDIR="$RDIR/$OUTDIR"

if [ ! -d "$INDIR" ]; then exit; fi
if [ ! -d "$OUTDIR" ]; then mkdir "$OUTDIR"; fi

if [[ ! "$SIZE" =~ ^[0-9]+(\.[0-9]+)?$ ]]; then
    source "$SIZE"
    SIZECFG="true"
else
    SIZECFG="false"
fi

for FPATH in "$INDIR"/*; do
    if [ -d "$FPATH" ]; then continue; fi
    FNAME="$(basename "$FPATH")"
    
    if [[ "$SIZECFG" == "true" ]]; then SIZE="${SIZES["$FNAME"]}"; fi
    STD="$SDIR/bin/extract -m$MEASURE -t$THRESHOLD -e$EPSILON -d$DAMPING"
    ERR="\e[1mextract\e[0m -m\e[32;1m$MEASURE\e[0m -t\e[33;1m$THRESHOLD\e[0m -e\e[33;1m$EPSILON\e[0m -d\e[33;1m$DAMPING\e[0m"
    if [[ "$SIZE" == *"."* ]]; then
        STD="$STD -s$SIZE"
        ERR="$ERR -s\e[36;1m$SIZE\e[0m"
    else
        STD="$STD -S$SIZE"
        ERR="$ERR -S\e[36;1m$SIZE\e[0m"
    fi
    if [[ "$TOKENS" == "lemma" ]]; then
        STD="$STD -l"
        ERR="$ERR -l"
    fi
    STD="$STD -g$TAGS"
    ERR="$ERR -g\e[35;1m$TAGS\e[0m"
    if [[ "$STOPWORDS" == "stop" ]]; then
        STD="$STD -p"
        ERR="$ERR -p"
    fi
    STD="$STD $INDIR/bglpc/$FNAME 2>&1 >$OUTDIR/$FNAME-$MEASURE-$TOKENS-$TAGS-$STOPWORDS"
    ERR="$ERR \e[1;4m$FNAME\e[0m"

    echo -e "$ERR"
    echo "$STD"
    echo -e "$ERR" >&2
done
