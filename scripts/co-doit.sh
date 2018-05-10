#!/bin/bash

SDIR="$(readlink -f "$(dirname "${BASH_SOURCE[0]}")")"
IDIR="$(readlink -f "$1")"
mkdir -p "$2"
ODIR="$(readlink -f "$2")"

echo "$IDIR"
echo "$ODIR"

for DI in "$IDIR/"*; do
  DI=$(basename "$DI")
  echo "**/$DI"
  mkdir -p "$ODIR/$DI"
  "$SDIR/co-select-dir.sh" -f "$IDIR/$DI" -r $3 -c $4 -d "$IDIR/$DI" -t "$ODIR/$DI" -n "$5"
done