#!/bin/bash
function localhost() {
  echo "localhost"
}

# Command, or function if command fails
IP=$(hostname -I || localhost)
#
echo -e "Machine Name: ${IP}"
