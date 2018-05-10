#!/bin/bash

SDIR="$(readlink -f "$(dirname "${BASH_SOURCE[0]}")")"
FDIR="."
TDIR="."
IDIR="."
REQ=
COPY=
MINL=0
while getopts f:r:c:d:t:n: OPT; do
  case $OPT in
    f) IDIR="$OPTARG" ;;
    r) REQ="$OPTARG" ;;
    c) COPY="$OPTARG" ;;
    d) FDIR="$OPTARG" ;;
    t) TDIR="$OPTARG" ;;
    n) MINL="$OPTARG" ;;
  esac
done
IDIR=$(readlink -f $IDIR)

declare -A USED

for FI in "$IDIR/"*; do
  ID=$("$SDIR/co-getid.sh" "$FI")
  if [[ ${USED[$ID]} == true ]]; then continue; fi
  USED[$ID]=true
  "$SDIR/co-select.sh" -i "$ID" -r "$REQ" -c "$COPY" -d "$FDIR" -t "$TDIR" -n "$MINL"
done
