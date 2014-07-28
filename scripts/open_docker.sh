echo "open Docker remote API..."
service docker stop
docker -H tcp://0.0.0.0:4243 -H unix:///var/run/docker.sock -d > {{log.docker}} 2>&1 &
sleep 10
echo "opened Docker remote API"