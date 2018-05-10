#!/bin/bash

SDIR="$(readlink -f "$(dirname "$0")")"
FDIR="$(readlink -f "$1")"
XDIR="$(readlink -f "$(dirname "$1")")/texts"
MDIR="$(readlink -f "$(dirname "$1")")/models"
TDIR="$(dirname "$SDIR")/tmp"

FILES="$FDIR/*-$2-$3.txt"
if [ ! -d "$FDIR" ]; then exit; fi
if [ ! -d "$XDIR" ]; then mkdir "$XDIR"; fi
if [ ! -d "$MDIR-l" ]; then mkdir "$MDIR-l"; fi
if [ ! -d "$MDIR-m" ]; then mkdir "$MDIR-m"; fi
if [ ! -d "$MDIR-s" ]; then mkdir "$MDIR-s"; fi
if [ ! -d "$TDIR" ]; then mkdir "$TDIR"; fi

for FILE in $FILES; do
    (
        ID=$($SDIR/co-getid-new.sh $FILE)
        echo -e "Preparing Viki's \e[1m$FDIR/$ID-$2-$3.txt\e[0m ..."
        FTMPL="$(mktemp "$TDIR/co-prep-vik-XXXXXXX")"
        FTMPM="$(mktemp "$TDIR/co-prep-vik-XXXXXXX")"
        FTMPS="$(mktemp "$TDIR/co-prep-vik-XXXXXXX")"
        $SDIR/bin/prep-vik "$FDIR/$ID-$2-$3.txt" "$XDIR/$ID-$2" "$FTMPL" "$FTMPM" "$FTMPS"

        (
            FILEL="$MDIR-l/$ID-$2"
            echo -e "Preparing Viki's \e[36m$FILEL\e[0m ..."
            >"$FILEL"
            "$SDIR/tools/bglpc/sspltok" \
                "$SDIR/tools/bglpc/model/ss3_lexicon.txt" \
                "$SDIR/tools/bglpc/model/ss3_rules.txt" \
                "$SDIR/tools/bglpc/model/tokenizer.bin" \
                "$SDIR/tools/bglpc/model/toktypes.bin" \
                split "$FTMPL" 2>/dev/null \
                | tr '\n' ' ' \
                | sed -e 's/ <\/S> /\n/g' -e 's/<S> //g' \
                >>"$FILEL"
            rm "$FTMPL"
        ) &

        (
            FILEM="$MDIR-m/$ID-$2"
            echo -e "Preparing Viki's \e[36m$FILEM\e[0m ..."
            >"$FILEM"
            "$SDIR/tools/bglpc/sspltok" \
                "$SDIR/tools/bglpc/model/ss3_lexicon.txt" \
                "$SDIR/tools/bglpc/model/ss3_rules.txt" \
                "$SDIR/tools/bglpc/model/tokenizer.bin" \
                "$SDIR/tools/bglpc/model/toktypes.bin" \
                split "$FTMPM" 2>/dev/null \
                | tr '\n' ' ' \
                | sed -e 's/ <\/S> /\n/g' -e 's/<S> //g' \
                >>"$FILEM"
            rm "$FTMPM"
        ) &

        (
            FILES="$MDIR-s/$ID-$2"
            echo -e "Preparing Viki's \e[36m$FILES\e[0m ..."
            >"$FILES"
            "$SDIR/tools/bglpc/sspltok" \
                "$SDIR/tools/bglpc/model/ss3_lexicon.txt" \
                "$SDIR/tools/bglpc/model/ss3_rules.txt" \
                "$SDIR/tools/bglpc/model/tokenizer.bin" \
                "$SDIR/tools/bglpc/model/toktypes.bin" \
                split "$FTMPS" 2>/dev/null \
                | tr '\n' ' ' \
                | sed -e 's/ <\/S> /\n/g' -e 's/<S> //g' \
                >>"$FILES"
            rm "$FTMPS"
        ) &
    ) &
done

wait
