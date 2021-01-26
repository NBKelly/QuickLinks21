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
    
    java com.nbkelly.QuickLinks21 --file "$entry" > ans_tmp.txt
    java com.nbkelly.QuickLinks21 --file "$entry" > ans_tmp.txt
    java com.nbkelly.QuickLinks21 --file "$entry" > ans_tmp.txt
    java com.nbkelly.QuickLinks21 --file "$entry" > ans_tmp.txt
    java com.nbkelly.QuickLinks21 --file "$entry" > ans_tmp.txt
    end=`date +%s.%N`
    
    runtime=$(python3 -c "print((${end} - ${start})/10.0)")        

    start=`date +%s.%N`
    java com.nbkelly.QuickLinks21 --only-preprocess --file "$entry" > ans_trash.txt
    java com.nbkelly.QuickLinks21 --only-preprocess --file "$entry" > ans_trash.txt
    java com.nbkelly.QuickLinks21 --only-preprocess --file "$entry" > ans_trash.txt
    java com.nbkelly.QuickLinks21 --only-preprocess --file "$entry" > ans_trash.txt
    java com.nbkelly.QuickLinks21 --only-preprocess --file "$entry" > ans_trash.txt

    java com.nbkelly.QuickLinks21 --only-preprocess --file "$entry" > ans_trash.txt
    java com.nbkelly.QuickLinks21 --only-preprocess --file "$entry" > ans_trash.txt
    java com.nbkelly.QuickLinks21 --only-preprocess --file "$entry" > ans_trash.txt
    java com.nbkelly.QuickLinks21 --only-preprocess --file "$entry" > ans_trash.txt
    java com.nbkelly.QuickLinks21 --only-preprocess --file "$entry" > ans_trash.txt
    end=`date +%s.%N`
    runtime2=$(python -c "print((${end} - ${start})/10.0)")

    start=`date +%s.%N`
    java com.nbkelly.QuickLinks21 --disable-parallel --file "$entry" > ans_trash.txt
    java com.nbkelly.QuickLinks21 --disable-parallel --file "$entry" > ans_trash.txt
    java com.nbkelly.QuickLinks21 --disable-parallel --file "$entry" > ans_trash.txt
    java com.nbkelly.QuickLinks21 --disable-parallel --file "$entry" > ans_trash.txt
    java com.nbkelly.QuickLinks21 --disable-parallel --file "$entry" > ans_trash.txt

    java com.nbkelly.QuickLinks21 --disable-parallel --file "$entry" > ans_trash.txt
    java com.nbkelly.QuickLinks21 --disable-parallel --file "$entry" > ans_trash.txt
    java com.nbkelly.QuickLinks21 --disable-parallel --file "$entry" > ans_trash.txt
    java com.nbkelly.QuickLinks21 --disable-parallel --file "$entry" > ans_trash.txt
    java com.nbkelly.QuickLinks21 --disable-parallel --file "$entry" > ans_trash.txt
    end=`date +%s.%N`
    
    runtime4=$(python -c "print((${end} - ${start})/10.0)")


    runtime3=$(python -c "print((${runtime} - ${runtime2}))")
    #display the difference in runtimes

    echo "Total:         " $runtime4
    echo "With Parallel: " $runtime
    echo "Parallel-Cost  " $(python -c "print((${runtime4} - ${runtime}))")
    echo "Pre:           " $runtime2
    echo "Post:          " $runtime3    
    
    #now we want to see if the answer is valid 
    DIFF=$(diff "ans_tmp.txt" "$ansName") > differences
    wc -l differences
    rm differences
    rm ans_tmp.txt
    rm ans_trash.txt

    echo
done
