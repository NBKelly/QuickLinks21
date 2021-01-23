#!/bin/bash
for entry in "data"/*.in
do
    echo "$entry"
    #get the basename
    baseName=${entry%.in}
    echo $baseName
    
    #get the name of the ans file
    ansName="$baseName.ans"
    java -ea com.nbkelly.QuickLinks21 --file "$entry" > ans_tmp.txt
    DIFF=$(diff "ans_tmp.txt" "$ansName")
    if [ "$DIFF" != "" ]
    then
	echo "Mismatch at $baseName"
    fi
done
