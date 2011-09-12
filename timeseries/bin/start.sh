#!/bin/ksh

usage ()
{
    echo
    echo "Usage: start.sh gsc|gsm|web|admin|yellow|red|green|mirror|gold|purple|notifier|prefs|brown|jmx"
}

if [ $# -eq 0 ];then
  usage
  exit 1
fi

OLDDIR=$PWD
DIR=`dirname $0`
#DIR=`dirname $DIR/../.`
#DIR=$PWD/$DIR

SCRIPT_DIR=$DIR

case $1 in
  all)
    $0 gsm
    $0 gsc 1
    $0 gsc 2
    $0 gsc 3
    $0 gsc 4
    $0 gsc 5
    $0 gsc 6
    $0 gsc 7
    $0 gsc 8
    $0 mirror
    $0 notifier
    $0 prefs
    $0 red
    $0 yellow
#    $0 silver
#    $0 green
#    $0 jmx
#    $0 web
#    $0 admin
#    $0 brown
    exit 1;
    ;;
 "web")
    $SCRIPT_DIR/gsc-web.sh v32
    ;;
 "gsc")
    $SCRIPT_DIR/gsc.sh $2
    ;;
 "admin")
    $SCRIPT_DIR/gsc-admin.sh v32
    ;;
"yellow")
    $SCRIPT_DIR/gsc-yellow.sh v32
    ;;
"silver")
    $SCRIPT_DIR/gsc-taskloader.sh v32
    ;;
"green")
    $SCRIPT_DIR/gsc-green.sh v32
    ;;
"red")
    $SCRIPT_DIR/gsc-red.sh v32
    ;;
"mirror")
    $SCRIPT_DIR/gsc-mirror.sh v32
    ;;
"notifier")
    $SCRIPT_DIR/gsc-notify.sh v32
    ;;
"prefs")
    $SCRIPT_DIR/gsc-prefs.sh v32
    ;;
"jmx")
    $SCRIPT_DIR/gsc-jmx.sh v32
    ;;
"brown")
    $SCRIPT_DIR/gsc-brown.sh v32
    ;;
 "gsm")
    $SCRIPT_DIR/gsm.sh v32
    ;;
 "apache")
    /opt/fxall/apache2/bin/apachectl start
    ;;
  "agent")
    /opt/fxall/gs/tools/apache/apache-lb-agent.sh -apache /opt/fxall/apache2 -conf-dir /opt/fxall/apache2/conf/gigaspaces 
   ;;
esac
cd $OLDDIR
echo "Started $1"
