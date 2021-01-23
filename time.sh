#!/bin/bash
for entry in "data"/*.in
do
    echo "$entry"
    #get the basename
    baseName=${entry%.in}
    #echo $baseName

    #we want to know the number of nodes and the number of scenarios
    line=$(head -n 1 $entry)
    echo "Nodes Scenarios"
    echo $line
    
    #get the name of the ans file
    ansName="$baseName.ans"
    start=`date +%s.%N`
    java com.nbkelly.QuickLinks21 --file "$entry" > ans_tmp.txt
    java com.nbkelly.QuickLinks21 --file "$entry" > ans_tmp.txt
    java com.nbkelly.QuickLinks21 --file "$entry" > ans_tmp.txt
    java com.nbkelly.QuickLinks21 --file "$entry" > ans_tmp.txt
    java com.nbkelly.QuickLinks21 --file "$entry" > ans_tmp.txt
    end=`date +%s.%N`
    
    runtime=$(python3 -c "print((${end} - ${start})/5.0)")    
    echo "Total: " $runtime

    #now we want to see if the answer is valid 
    DIFF=$(diff "ans_tmp.txt" "$ansName") > differences
    wc -l differences
    rm differences

    echo
done
