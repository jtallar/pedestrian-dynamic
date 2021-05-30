#!/bin/bash
if [ "$#" -ne 3 ]; then
    echo "Illegal number of parameters." 
    echo "Run with ./same.sh N d rep"
    exit 1
fi

N="$1"
D="$2"
REP="$3"

# Disable plotting if enabled
sed -i -e 's/\"plot\": true/\"plot\": false/g' config.json

OUT_DYN=""
OUT_EXIT=""

for i in $(seq 1 $REP)
do
    DYN="dynamic_$i.txt"
    EXIT="exit_$i.txt"
    python3.8 generator.py $DYN $N
    ./target/tp5-simu-1.0/pedestrian-dynamic.sh -Dn="$N" -Dd="$D" -Ddynamic="$DYN" -Dexit="$EXIT"
    OUT_DYN="$OUT_DYN $DYN"
    OUT_EXIT="$OUT_EXIT $EXIT"
    echo "-----------------------------------"
done
echo "$OUT_DYN $OUT_EXIT"
# TODO: Call analysis.py with OUT_DYN or OUT_EXIT accordingly
# python3.8 analysis.py $OUT_DYN

# Reenable plotting
sed -i -e 's/\"plot\": false/\"plot\": true/g' config.json