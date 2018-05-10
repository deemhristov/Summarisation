#!/bin/bash

SDIR="$(readlink -f "$(dirname "$0")")"
RDIR="$(readlink -f "$1")"

for FILE in "$RDIR"/*; do
    echo "Processing $(basename "$FILE") ... " >&2
    TEMP="$("$SDIR/tools/bglpc/sspltok" \
        "$SDIR/tools/bglpc/model/ss3_lexicon.txt" \
        "$SDIR/tools/bglpc/model/ss3_rules.txt" \
        "$SDIR/tools/bglpc/model/tokenizer.bin" \
        "$SDIR/tools/bglpc/model/toktypes.bin" \
        split "$FILE" 2>/dev/null \
        | tr '\n' ' ' \
        | sed -e 's/ <\/S> /\n/g' -e 's/<S> //g')"
    echo "$TEMP" >"$FILE"
done
