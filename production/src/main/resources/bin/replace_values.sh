#!/bin/bash

# Usage:
#         replace_values.sh settings.properties output_path

set -e 

FILE_FILTER_PROPERTIES="production-*.properties *.sh"

declare -A map

printUsage()
{
	echo ""
	echo "OINK property file token replacement tool"
	echo "Usage:"
	echo "        replace_values.sh settings.properties output_path"
	echo ""
}

findStr()
{
    local settings=$1                           # settings file
	local source=$2                             # source file
	local target=${2//production-/${3}\/}       # remove production- from name
	echo "-------------------------------------------------"
    echo settings : $settings
    echo source : $source
    echo target : $target
	echo "-------------------------------------------------"
	cp $source $target
    sed -e '/^[ ]*#/d' -e '/^[ ]*;/d' -e '/^\s*$/d' ${settings} | sed -e 's/ //g' | sed -e 's/\r//g' |
        while read LINE
        do
            local KEY=`echo $LINE | cut -d "=" -f 1`
            local VALUE=`echo $LINE | cut -d "=" -f 2`
			KEY="${KEY#"${KEY%%[![:space:]]*}"}"
			VALUE="${VALUE#"${VALUE%%[![:space:]]*}"}"
            echo $KEY =\> $VALUE
			VALUE=${VALUE//\//\\/}
			local COMMAND="s/#${KEY}#/${VALUE}/g"
			sed -i -e $COMMAND $target
        done
    return
	cat $target
}

# Check parameters and print usage with error message if all is not good
if [ -z "$1" ]; then
	printUsage
	echo "Error: settings file not specified"
	echo ""
	exit 0
fi
if [ -z "$2" ]; then
	printUsage
	echo "Error: output folder not specified"
	echo ""
	exit 0
fi

# Make sure the output directory exists and start processing settings files
mkdir -p $2
for entry in `ls $FILE_FILTER_PROPERTIES`; do
	findStr $1 $entry $2
done


