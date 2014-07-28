ENDTIME=$(date +%s.%N)
echo "ended at $ENDTIME"
DIFFTIME=$(echo "$ENDTIME - $STARTTIME" | bc)
echo "done in $DIFFTIME"