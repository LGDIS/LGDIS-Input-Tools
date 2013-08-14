#!/bin/sh
#  input-tools archivelog delete script.
#

USER="mrsuser"
APP_NAME="`basename $0`"
ARCIVELOG="/home/mrsuser/LGDIS-Input-Tools/MRS/archives/socket/jma/"

#to syslog
LOGGER="/usr/bin/logger -t ${APP_NAME} -i"
ERRMSG="`basename $0`: command error"

#source function library.
. /etc/rc.d/init.d/functions

if [ $UID -eq 0 ]; then
    abspath=$(cd $(dirname $0); pwd)/$(basename $0)
    exec su - $USER $abspath "$@"
    false
fi

check() {
  cd ${ARCIVELOG} || exit 1 
  find .  -name "*" -type f -daystart -mtime +93  
}

command(){
  cd ${ARCIVELOG} || exit 1
  find .  -name "*" -type f -daystart -mtime +93 | xargs rm -f 
  if [ ! $? -eq 0 ]; then
    echo ${ERRMSG}
  fi
}

case "$1" in

  start)
    command 2>&1 | $LOGGER 
    ;;
  check)
    check 
    ;;
  *)
    echo "Usage: ${APP_NAME} {start|check}" >&2
    exit 1
    ;;

esac

