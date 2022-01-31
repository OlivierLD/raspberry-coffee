#!/bin/bash
#
# Python
ps -ef | grep 'python.*http.server' | grep -v grep > fax.txt
# Node
ps -ef | grep TinyNodeFaxServer | grep -v grep >> fax.txt
# Java
ps -ef | grep http.HTTPServer | grep -v grep >> fax.txt
#
echo -e "---- Fax Server Processes ----"
cat fax.txt
echo -e "------------------------------"
rm fax.txt

