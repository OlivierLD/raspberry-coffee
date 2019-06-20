#!/usr/bin/env bash
echo -e "Welcome to the install Script of the whole repo"
git clone https://github.com/OlivierLD/raspberry-coffee.git
cd raspberry-coffee
./gradlew tasks
echo -e "---------------------------------------------------------"
echo -e "You should be good to go, the infrastructure is in place."
echo -e "Try for example:"
echo -e " \$ cd RESTNavServer"
echo -e " \$../gradlew shadowJar"
echo -e ""
echo -e "Contact: olivier@lediouris.net"
