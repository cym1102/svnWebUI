#!/bin/sh

cd /home
exec java -jar -Xmx64m svnWebUI.jar ${BOOT_OPTIONS} > /dev/null
