#!/bin/bash

source "$1"

SDIR="$(dirname "$(readlink -f "$0")")"
RDIR="$(dirname "$(readlink -f "$1")")"
TDIR="$(dirname "$SDIR")/tmp"

if [ ! -d "$TDIR" ]; then mkdir "$TDIR"; fi

cd "$RDIR"

FCFG="$(mktemp "$TDIR/run-rouge-XXXXXX")"
>"$FCFG"

printf "<ROUGE-EVAL version=\"1.0\">\n" >>"$FCFG"
for FPATH in "$MODELROOT"/*; do
    FNAME="$(basename "$FPATH")"
    printf "    <EVAL ID=\"%s\">\n" "$FNAME" >>"$FCFG"
    printf "        <PEER-ROOT>%s</PEER-ROOT>\n" "$PEERROOT" >>"$FCFG"
    printf "        <MODEL-ROOT>%s</MODEL-ROOT>\n" "$MODELROOT" >>"$FCFG"
    printf "        <INPUT-FORMAT TYPE=\"SPL\"></INPUT-FORMAT>\n" >>"$FCFG"
    printf "        <PEERS>\n" >>"$FCFG"
    for PEER in "${PEERS[@]}"; do
        printf "            <P ID=\"%s\">%s-%s</P>\n" "${PEER^^}" "$FNAME" "$PEER" >>"$FCFG"
    done
    printf "        </PEERS>\n" >>"$FCFG"
    printf "        <MODELS>\n" >>"$FCFG"
    printf "            <M ID=\"MODEL\">%s</M>\n" "$FNAME" >>"$FCFG"
    printf "        </MODELS>\n" >>"$FCFG"
    printf "    </EVAL>\n" >>"$FCFG"
done
printf "</ROUGE-EVAL>\n" >>"$FCFG"

echo "Now running ROUGE ..."
"$SDIR/tools/ROUGE/ROUGE-1.5.6.pl" -e "$SDIR/tools/ROUGE/data" -a $PARAMS "$FCFG" >"$OUTFILE"

rm "$FCFG"
