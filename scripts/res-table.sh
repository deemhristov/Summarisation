#!/bin/bash

echo -e "Мярка\tЛем\tЧнР\tСтоп\tR-1\tR-2\tR-3\tR-4\tR-L\tR-W\tR-S\tR-SU\tСр. Харм."
egrep -e 'ROUGE-[0-9].*Average_R' -e 'ROUGE-[A-Z].*Average_F' | cut -f 1,4 -d ' ' | awk -F' ' -v OFS='\t' '{x=$1;$1="";a[x]=a[x]$0}END{for(x in a)print x,a[x]}' | sed -E 's:\t\t+:\t:g' | sed -E -e 's:^COSINE:Косинус:g' -e 's:^FIRST(-[^-\t]+){3}:Първи\t?\t?\t?:g' -e 's:^RANDOM(-[^-\t]+){3}:Случайни\t?\t?\t?:g' -e 's:^(ISF-)?LCS(-[^-\t]+){3}:(isf-)LCS\t?\t?\t?:g' -e 's:-WORD|-ALL|-NONE:\t:g' -e 's:-LEMMA|-VN|-STOP:\tX:g' | LC_ALL=C sort | uniq
