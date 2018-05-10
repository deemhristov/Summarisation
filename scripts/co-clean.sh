#!/bin/bash

SDIR="$(dirname "$(readlink -f "$0")")"
TDIR="$(dirname "$SDIR")/tmp"

if [ ! -d "$TDIR" ]; then mkdir "$TDIR"; fi

for FN in "$1"/*; do
    FTMP="$(mktemp "$TDIR/co-clean-XXXXXX")"
    echo "Cleaning $FN ..." >&2
    sed -e 's/&lt;/</g' -e 's/&gt;/>/g' -e 's/<[^>]*>//g' "$FN" >"$FTMP"
    cat "$FTMP" >"$FN"
    rm "$FTMP"
done
echo "Cleaning done." >&2
