echo "deployment meta information"
echo "timestamp: {{info.timestamp}}"
echo "host: {{info.host}}"
echo 'formation: {{info.formation}} --end'

STARTTIME=$(date +%s.%N)
echo "started at $STARTTIME"