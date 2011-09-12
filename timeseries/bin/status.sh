#!/bin/bash
echo ====================================================

id=`ps -ef | grep GSM | grep -v grep | awk '{print $2}'`

if test -n "$id"

then

    echo GSM is UP

else

    echo GSM is DOWN

fi

 

for i in black white blue green yellow red pink purple silver orange indigo brown

do

        cnt=`ps -ef | grep com.gs.zones=$i | grep -v grep | wc -l`

        id=`ps -ef | grep com.gs.zones=$i | grep -v grep | awk '{print $2}'`

        if test -n "$id"

        then

            echo GSC Zone: $i is UP  - No.of jvms: $cnt

        else

            echo GSC Zone: $i is DOWN

        fi

done

echo ====================================================

id=`ps -ef | grep ApacheLoadBalancerAgent | grep -v grep | awk '{print $2}'`

if test -n "$id"

then

    echo Apache LB Agent is UP

else

    echo Apache LB Agent  is DOWN

fi

echo ====================================================

id=`ps -ef | grep apache2 | grep -v grep | awk '{print $2}'`

if test -n "$id"

then

    echo Apache is UP

else

    echo Apache is DOWN

fi
echo ====================================================

id=`ps -ef | grep mqm | grep -v grep | awk '{print $2}'`

if test -n "$id"

then

    echo MQ is UP

else

    echo MQ is DOWN

fi
echo ====================================================


