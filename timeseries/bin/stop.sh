#!/bin/ksh

usage ()
{
    echo
    echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
    echo "+  WARNING: DO NOT STOP black|white|gold|green without following proper shutdown procedures  +"
    echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
    echo ""
    echo "Usage: stop.sh input|blue|red|yellow|silver|red|pink|white|green|black|gold|purple|orange|indigo|brown|gsm"
    echo ""
}
if [ $# -eq 0 ];then
  usage
  exit 1
fi

killproc() {
   PID=`ps -ef | grep "$1" | grep -v grep | grep -v $0 | awk '{printf $2 " "}'`
   if [ "x$PID" != "x" ];then
      echo "Killing ($1) $PID"
      kill -9 $PID
   else 
      echo "($1) is not running."
   fi
}

killGSC() {
   PID=`ps -ef | grep "GSC" | grep "$1" | grep -v grep | grep -v $0 | awk '{printf $2 " "}'`
   if [ "x$PID" != "x" ];then
      echo "Killing ($1) $PID"
      kill -9 $PID
   else 
      echo "($1) is not running."
   fi
}

killAgent() {
   PID=`ps -ef | grep "Apache" | grep "$1" | grep -v grep | grep -v $0 | awk '{printf $2 " "}'`
   if [ "x$PID" != "x" ];then
      echo "Killing ($1) $PID"
      kill -9 $PID
   else 
      echo "($1) is not running."
   fi
}


case $1 in
  input)
    echo "Are you sure you want to stop All input processes ? Y/N"
    read YES 
    if [[ "$YES" == "Y" ]]; then
            echo "Stopping blue, pink, yellow, silver, orange, indigo, brown, purple and red"
            $0 blue 
            $0 pink
            $0 yellow
            $0 red
            $0 silver
            $0 purple
            $0 orange
            $0 indigo
            $0 brown
            exit 0
    fi
    exit 1;
    ;;
  gsm)
    echo "Are you sure you want to stop GSM ? Y/N"
    read YES
    if [[ "$YES" == "Y" ]]; then
      killproc GSM
      exit 0
    fi
    ;;
  white|red|yellow|green|blue|pink|purple|silver|orange|indigo|brown)
    killGSC $1
    ;;
  black|gold|green|white)
    echo "Are you sure you want to stop $1 ? Y/N"
    read YES
    if [[ "$YES" == "Y" ]]; then
      killGSC $1
      exit 0
    fi
    ;;
  apache)
    /opt/fxall/apache2/bin/apachectl stop
    ;;
  agent)
     killAgent ApacheLoadBalancerAgent
     rm /opt/fxall/apache2/conf/gigaspaces/*.conf
    ;;
esac
