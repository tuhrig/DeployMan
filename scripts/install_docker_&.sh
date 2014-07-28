(
	echo "installing Docker..."
	apt-get update && apt-get dist-upgrade -y
	apt-get install curl -y
	curl -s https://get.docker.io/ubuntu/ | sh
	usermod -a -G docker ubuntu
	echo "installed Docker"

	echo "open Docker remote API..."
	service docker stop
	docker -H tcp://0.0.0.0:4243 -H unix:///var/run/docker.sock -d &
	echo "opened Docker remote API"
) &