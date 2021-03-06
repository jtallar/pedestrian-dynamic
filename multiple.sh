#!/bin/bash
if [ "$#" -ne 1 ]; then
    echo "Illegal number of parameters." 
    echo "Run with ./multiple.sh rep"
    exit 1
fi

# Disable plotting if enabled
sed -i -e 's/\"plot\": true/\"plot\": false/g' config.json

REP="$1"

N=200
D=1.2
ALL_OUT_DYN=""
ALL_OUT_EXIT=""
while (( $(echo "$N <= 380" | bc -l) ))
do
    OUT_DYN=""
    OUT_EXIT=""
    for i in $(seq 1 $REP)
    do
        DYN="dynamic-$N-$i.txt"
        EXIT="exit-$N-$i.txt"
        python3.8 generator.py $DYN $N
        ./target/tp5-simu-1.0/pedestrian-dynamic.sh -Dn="$N" -Dd="$D" -Ddynamic="$DYN" -Dexit="$EXIT"
        OUT_DYN="$OUT_DYN $DYN"
        OUT_EXIT="$OUT_EXIT $EXIT"
        echo "-----------------------------------"
    done
    # TODO: Call analysis.py with OUT_DYN or OUT_EXIT accordingly
    # python3.8 analysis.py $OUT
    echo "###################################"
    N=$(echo "$N + 60" | bc -l)
    D=$(echo "$D + 0.6" | bc -l)
    ALL_OUT_DYN="$ALL_OUT_DYN $OUT_DYN"
    ALL_OUT_EXIT="$ALL_OUT_EXIT $OUT_EXIT"
done

# Reenable plotting
sed -i -e 's/\"plot\": false/\"plot\": true/g' config.json

echo "$ALL_OUT_DYN $ALL_OUT_EXIT"
python3.8 analyzerMult.py $ALL_OUT_EXIT