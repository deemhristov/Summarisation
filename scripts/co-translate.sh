#!/bin/bash

INDIR="$1"
OUTDIR="$2"

if [ ! -d "$INDIR" ]; then exit; fi
if [ ! -d "$OUTDIR" ]; then mkdir "$OUTDIR"; fi

for FPATH in $INDIR/*; do
    FNAME="$(basename "$FPATH")"
    sed -e 'y/абвгдежзийклмнопрстуфхцчшъь/abvgdexzijklmnoprstufhcqwyj/' \
        -e 'y/АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЪЬ/abvgdexzijklmnoprstufhcqwyj/' \
        -e 's/[щЩ]/wt/g' \
        -e 's/[юЮ]/ju/g' \
        -e 's/[яЯ]/ja/g' \
        "$INDIR/$FNAME" >"$OUTDIR/$FNAME"
done
