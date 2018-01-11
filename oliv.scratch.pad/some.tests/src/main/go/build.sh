#!/usr/bin/env bash
echo Cleaning
go clean
echo Building
go build
echo Running
./go
