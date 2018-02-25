#!/bin/bash
echo -e "+--------------------------------------+"
echo -e "| This is for the examples in sample01 |"
echo -e "+--------------------------------------+"
export HTTP_PROXY=http://www-proxy.us.oracle.com:80
export HTTPS_PROXY=http://www-proxy.us.oracle.com:80
bower install juicy-ace-editor --save
