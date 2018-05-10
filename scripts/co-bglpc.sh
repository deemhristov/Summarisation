#!/bin/bash

SDIR="$(dirname "$(readlink -f "$0")")"
RDIR="$(readlink -f "$1")"
TDIR="$(dirname "$SDIR")/tmp"

if [ ! -d "$RDIR/bglpc" ]; then mkdir "$RDIR/bglpc"; fi
if [ ! -d "$TDIR" ]; then mkdir "$TDIR"; fi

for FPATH in "$RDIR"/*; do
    if [ -d "$FPATH" ]; then continue; fi
    FNAME="$(basename "$FPATH")"
    if [ -e "$RDIR/bglpc/$FNAME" ]; then
        echo -e "\e[1m$FNAME\e[0m is \e[34;1malready\e[0m bglpc-ed"
    else
        (
            echo -e "\e[1m$FNAME\e[0m is \e[31;1mgetting\e[0m bglpc-ed"
            INFILE="$(mktemp "$TDIR/co-bglpc-XXXXXX")"
            OUTFILE="$(mktemp "$TDIR/co-bglpc-XXXXXX")"
            sed -e 's/&lt;/</g' -e 's/&gt;/>/g' -e 's/<[^>]*>//g' "$RDIR/$FNAME" >"$INFILE"
            "$SDIR/tools/bglpc/bglpc" \
                "$SDIR/tools/bglpc/model" \
                -file "$INFILE" \
                2>/dev/null \
                | sed -e 's/<\/\?S>\tX\t/X/g' \
                | grep -P '.*\t.*\t.*\t.*' \
                | awk '{print $1 "\t" $3 "\t" substr($2, 0, 1)}' \
                >"$OUTFILE"
            HALF="$(($(wc -l <"$OUTFILE") / 2))"
            INPUT="$(head -n "$HALF" <"$OUTFILE")"
            echo "$INPUT" >"$RDIR/bglpc/$FNAME"
            rm "$INFILE"
            rm "$OUTFILE"
            echo -e "\e[1m$FNAME\e[0m is \e[32;1mdo-done\e[0m bglpc-ed"
        ) &
    fi
done

wait
