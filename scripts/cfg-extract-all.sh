#!/bin/bash

SDIR="$(dirname "$(readlink -f "$0")")"
RDIR="$(readlink -f "$1")"
TDIR="$(dirname "$SDIR")/tmp"

if [ ! -d "$TDIR" ]; then mkdir "$TDIR"; fi

EFILE="$RDIR/extract$3.cfg"
RFILE="$RDIR/rouge$3.cfg"
if [[ ! "$2" =~ ^[0-9]+(\.[0-9]+)?$ ]]; then SIZE="$(readlink -f "$2")"; else SIZE="$2"; fi
>"$EFILE"
echo "# ROUGE parameters" >"$RFILE"
echo "MODELROOT=\"models$3\"" >>"$RFILE"
echo "PEERROOT=\"peers$3\"" >>"$RFILE"
echo "declare -a PEERS=(" >>"$RFILE"
for MEASURE in "cosine" "lcs" "isf-lcs" "first" "random"; do
    for TOKENS in "word" "lemma"; do
        for TAGS in "all" "VN"; do
            for STOPWORDS in "none" "stop"; do
                TFILE="$(mktemp "$TDIR/cfg-extract-all-XXXXXX")"
                echo "RDIR=\"$RDIR\"" >"$TFILE"
                echo "# extract parameters" >>"$TFILE"
                echo "MEASURE=\"$MEASURE\"" >>"$TFILE"
                echo "THRESHOLD=\"cont\"" >>"$TFILE"
                echo "EPSILON=\"0.0001\"" >>"$TFILE"
                echo "DAMPING=\"0.15\"" >>"$TFILE"
                echo "SIZE=\"$SIZE\"" >>"$TFILE"
                echo "TOKENS=\"$TOKENS\"" >>"$TFILE"
                echo "TAGS=\"$TAGS\"" >>"$TFILE"
                echo "STOPWORDS=\"$STOPWORDS\"" >>"$TFILE"
                echo "INDIR=\"texts\"" >>"$TFILE"
                echo "OUTDIR=\"peers$3\"" >>"$TFILE"
                "$SDIR/cfg-extract.sh" "$TFILE" 2>&1 >>"$EFILE"
                rm "$TFILE"
                echo "    \"$MEASURE-$TOKENS-$TAGS-$STOPWORDS\"" >>"$RFILE"
            done
        done
    done
done
echo ")" >>"$RFILE"
echo "PARAMS=\"-n 4 -2 -2 -U -w 1.2\"" >>"$RFILE"
echo "OUTFILE=\"result$3.txt\"" >>"$RFILE"
