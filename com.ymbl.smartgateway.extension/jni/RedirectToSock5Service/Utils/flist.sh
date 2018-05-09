#!/bin/sh

cat $1.vcxproj | grep Include | grep $2 | awk -F '"' '{print $2}' | awk '{ gsub("\\\\","/",$0); print "\t"$0" \\" }'
