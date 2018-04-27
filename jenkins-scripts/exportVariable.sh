#!/bin/bash
# usage: exportVariable.sh filepath name value
# append a variable to a file so that it can be imported again later

if (( $# != 3 )); then
  echo "Expected three arguments"
  exit 1
fi

VAR_FILE_PATH=$1
VARIABLE_NAME=$2
VARIABLE_VALUE=$3

if [ -e $VAR_FILE_PATH ] && [ ! -f $VAR_FILE_PATH ]; then
    echo "$VAR_FILE_PATH exists but is not a regular file"
	exit 1
elif [ ! -e $VAR_FILE_PATH ]; then
	echo "#/bin/bash" > $VAR_FILE_PATH
	echo "# file autogenerated by jenkins-scripts/exportVariable.sh" > $VAR_FILE_PATH
fi

echo "export $VARIABLE_NAME=$VARIABLE_VALUE" >> $VAR_FILE_PATH
