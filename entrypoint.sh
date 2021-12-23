#!/bin/sh

cd /home
exec java -jar -Xmx64m svnWebUI.jar --project.home=/home/svnWebUI ${BOOT_OPTIONS} > /dev/null
