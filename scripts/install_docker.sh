echo "installing Docker..."
apt-get update && apt-get dist-upgrade -y
apt-get install curl -y
curl -s https://get.docker.io/ubuntu/ | sh
usermod -a -G docker ubuntu
echo "installed Docker"