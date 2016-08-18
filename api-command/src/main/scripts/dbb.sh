#!/bin/sh

cd `dirname $0`
java -Dfile.encoding=UTF-8 -cp "./*:./lib/*" org.team4u.dbb.command.Client $1