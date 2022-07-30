#!/usr/bin/env bash
echo -e "Welcome to the install Script of the whole repo"
if [[ "$1" == "--dry-run" ]]; then
  echo -e "Not cloning the repo, per user's request"
else
  git clone https://github.com/OlivierLD/raspberry-coffee.git
fi
NEW_VERSION=true
if [[ "${NEW_VERSION}" != "true" ]]; then
  pushd raspberry-coffee
    # git submodule update --init
    git clone https://github.com/OlivierLD/AstroComputer.git
    # ./gradlew tasks --all
    ./gradlew projects
    echo -e "---------------------------------------------------------"
    echo -e "You should be good to go, the infrastructure is in place."
    echo -e "Try for example:"
    echo -e " \$ cd RESTNavServer"
    echo -e " \$../gradlew shadowJar"
    echo -e ""
    echo -e "Contact: olivier@lediouris.net"
  popd
else
  # ./gradlew tasks --all
  ./gradlew projects
  echo -e "---------------------------------------------------------"
  echo -e "You should be good to go, the infrastructure is in place."
  echo -e "Try for example:"
  echo -e " \$ cd RESTNavServer"
  echo -e " \$../gradlew shadowJar"
  echo -e ""
  echo -e "Contact: olivier@lediouris.net"
fi
