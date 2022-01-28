#!/bin/sh

cd /home
exec java -jar -Dfile.encoding=UTF-8 -Xmx64m svnWebUI.jar ${BOOT_OPTIONS} > /dev/null
