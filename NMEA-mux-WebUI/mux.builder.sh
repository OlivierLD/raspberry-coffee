#!/bin/bash
#
# Mux Builder.
#
LOOP=true
while [[ "$LOOP" == "true" ]]; do
	clear
  echo -e "+---------------------------------------------------------+"
  echo -e "| Customize your own NMEA Multiplexer / Navigation Server |"
  echo -e "+---------------------------------------------------------+"
  echo -e "| 1. compass-mux                                          |"
  echo -e "| 2. compass-mux-ext                                      |"
  echo -e "| 3. full-server                                          |"
  echo -e "| 4. full-server-extended-v2                              |"
  echo -e "| 5. head-up display                                      |"
  echo -e "| 6. minimal-mux                                          |"
  echo -e "| 7. small-server-extended                                |"
  echo -e "+---------------------------------------------------------+"
  echo -e "| Q. Exit                                                 |"
  echo -e "+---------------------------------------------------------+"
  echo -en "- You choose > "
  read OPTION
  case "${OPTION}" in
    "Q" | "q")
      printf "You're done.\n   Please come back soon!\n"
      LOOP=false
      ;;
    "1")
      echo -e "Compass Mux"
      pushd compass-mux
        echo -e "Running from ${PWD}"
        ./to.prod.sh
      popd
      echo -e "Hit [return]"
      read KEY
      ;;
    "2")
      echo -e "Compass Mux, Extended"
      pushd compass-mux-ext
        echo -e "Running from ${PWD}"
        ./to.prod.sh
      popd
      echo -e "Hit [return]"
      read KEY
      ;;
    "3")
      echo -e "Full Server"
      pushd full-server
        echo -e "Running from ${PWD}"
        ./to.prod.sh
      popd
      echo -e "Hit [return]"
      read KEY
      ;;
    "4")
      echo -e "Full Server, Extended"
      pushd full-server-extended-v2
        echo -e "Running from ${PWD}"
        ./to.prod.sh
      popd
      echo -e "Hit [return]"
      read KEY
      ;;
    "5")
      echo -e "Head-Up"
      pushd head-up
        echo -e "Running from ${PWD}"
        ./to.prod.sh
      popd
      echo -e "Hit [return]"
      read KEY
      ;;
    "6")
      echo -e "Minimal Server/Logger"
      pushd minimal-mux
        echo -e "Running from ${PWD}"
        ./to.prod.sh
      popd
      echo -e "Hit [return]"
      read KEY
      ;;
    "7")
      echo -e "Small server, extended"
      pushd small-server-extended
        echo -e "Running from ${PWD}"
        ./to.prod.sh
      popd
      echo -e "Hit [return]"
      read KEY
      ;;
    *)
      echo -e "What? Unknown command [${OPTION}]"
      echo -e "Hit [return]"
      read KEY
      ;;
  esac
done
#