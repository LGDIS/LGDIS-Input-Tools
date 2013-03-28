#!/bin/sh
#

# Source function library.
. /etc/rc.d/init.d/functions

# default parameters

APP_HOME=/home/dis/develop/LGDIS-Input-Tools

JVM=server
JAVA_USER=dis
JAVA_MAIN_CLASS=jp.lg.ishinomaki.city.mrs.ReceiverMain

JAVA_STDOUT=$APP_HOME/log/java_receiver.log
JAVA_STDERR=$APP_HOME/log/java_receiver.err

JAVA_HOME=${JAVA_HOME}

### set jvm's params ###
JVM_ARGS=java.util.logging.config.file=$APP_HOME/config/receiver_logging.properties

JVM_OPTIONS=mx256M

progname=receiverd
pidfile=${PIDFILE-/var/run/receiverd.pid}
lockfile=${LOCKFILE-/var/lock/subsys/receiverd}

JSVC=/home/dis/bin/jsvc
RETVAL=0

#JSVC_DEBUG="-debug"
JSVC_DEBUG=""

### set classpathes ###
for file in `ls $APP_HOME/libs`; do
  CLASSPATH=$CLASSPATH:$APP_HOME/libs/$file
done

### set jsvc's params ###
JSVC_ARGS="-jvm $JVM -cp $CLASSPATH -home $JAVA_HOME -user $JAVA_USER "
JSVC_ARGS="$JSVC_ARGS -pidfile ${pidfile} -outfile $JAVA_STDOUT -errfile $JAVA_STDERR"
JSVC_ARGS="$JSVC_ARGS $JSVC_DEBUG -D$JVM_ARGS -X$JVM_OPTIONS -procname $progname"

### set application's params ###
JAVA_ARGS="$APP_HOME/config/receiver.yml $APP_HOME/config/queue.yml"


### start ###
start() {
  echo -n $"Starting $progname$: "
  $JSVC $JSVC_ARGS $JAVA_MAIN_CLASS $JAVA_ARGS
  echo
  [ $RETVAL = 0 ] && touch ${lockfile}
  return $RETVAL
}

### stop ###
stop() {
  echo -n $"Stopping $prog: "
  $JSVC $JSVC_ARGS -stop $JAVA_MAIN_CLASS
  RETVAL=$?
  echo
  [ $RETVAL = 0 ] && rm -f ${lockfile} ${pidfile}
  echo
}


case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  restart)
    stop
    start
    ;;
  status)
    status -p ${pidfile} $progname
    RETVAL=$?
    ;;
  *)
    echo $"Usage: $progname {start|stop|restart|status}"
    RETVAL=3
esac

exit $RETVAL
