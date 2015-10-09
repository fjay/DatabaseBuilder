#!/bin/sh

cd `dirname $0`
java -Dfile.encoding=UTF-8 -cp "./*:./lib/*" com.asiainfo.dbb.command.Client $1