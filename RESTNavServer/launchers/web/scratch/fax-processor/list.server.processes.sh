#!/bin/bash
#
# Python
ps -ef | grep 'python.*http.server' | grep -v grep
# Node
ps -ef | grep TinyNodeFaxServer | grep -v grep
# Java
s -ef | grep http.HTTPServer | grep -v grep

