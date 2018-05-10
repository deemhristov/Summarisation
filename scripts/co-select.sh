#!/bin/bash

FDIR="."
TDIR="."
ID=
REQ=
COPY=
MINL=0
while getopts i:r:c:d:t:n: OPT; do
  case $OPT in
    i) ID="$OPTARG" ;;
    r) REQ="$OPTARG" ;;
    c) COPY="$OPTARG" ;;
    d) FDIR="$OPTARG" ;;
    t) TDIR="$OPTARG" ;;
    n) MINL="$OPTARG" ;;
  esac
done
mkdir -p "$TDIR"
FDIR=$(readlink -f "$FDIR")
TDIR=$(readlink -f "$TDIR")

RID="L?[0-9]{8}[a-z][A-Z]{3}"
if [[ ! $ID =~ ^$RID$ ]]; then echo "ID ($ID) missing or invalid" 1>&2; exit 1; fi

FBGTXT="$ID.txt"
FBGSEG="$ID.txt-SEG"
FBGCSV="$ID.csv"
FENTXT="${ID}en.txt"
FENCSV="en__$ID.csv"

MES="S or M:"
FLAG=false
if [[ $REQ == *t* && (! -f "$FDIR/$FBGTXT" || $(cat "$FDIR/$FBGTXT" | wc -m) -lt $MINL) ]]; then MES="$MES bg-txt"; FLAG=true; fi
if [[ $REQ == *s* && (! -f "$FDIR/$FBGSEG" || $(cat "$FDIR/$FBGSEG" | wc -m) -lt $MINL) ]]; then MES="$MES bg-seg"; FLAG=true; fi
if [[ $REQ == *c* && (! -f "$FDIR/$FBGCSV" || $(cat "$FDIR/$FBGCSV" | wc -m) -lt $MINL) ]]; then MES="$MES bg-csv"; FLAG=true; fi
if [[ $REQ == *T* && (! -f "$FDIR/$FENTXT" || $(cat "$FDIR/$FENTXT" | wc -m) -lt $MINL) ]]; then MES="$MES en-txt"; FLAG=true; fi
if [[ $REQ == *C* && (! -f "$FDIR/$FENCSV" || $(cat "$FDIR/$FENCSV" | wc -m) -lt $MINL) ]]; then MES="$MES en-csv"; FLAG=true; fi
if [[ $FLAG = true ]]; then echo "$ID" "$MES" 1>&2; exit 2; fi

MES="Copied:"
if [[ $COPY == *t* && -f "$FDIR/$FBGTXT" ]]; then MES="$MES bg-txt"; cp "$FDIR/$FBGTXT" "$TDIR/$FBGTXT"; fi
if [[ $COPY == *s* && -f "$FDIR/$FBGSEG" ]]; then MES="$MES bg-seg"; cp "$FDIR/$FBGSEG" "$TDIR/$FBGSEG"; fi
if [[ $COPY == *c* && -f "$FDIR/$FBGCSV" ]]; then MES="$MES bg-csv"; cp "$FDIR/$FBGCSV" "$TDIR/$FBGCSV"; fi
if [[ $COPY == *T* && -f "$FDIR/$FENTXT" ]]; then MES="$MES en-txt"; cp "$FDIR/$FENTXT" "$TDIR/$FENTXT"; fi
if [[ $COPY == *C* && -f "$FDIR/$FENCSV" ]]; then MES="$MES en-csv"; cp "$FDIR/$FENCSV" "$TDIR/$FENCSV"; fi
echo "$ID" "$MES"
