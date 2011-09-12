#!/bin/bash

`dirname $0`/gs.sh deploy -zones white -cluster total_members=1  /opt/fxall/sc/live/lib/sc-mirror.jar
`dirname $0`/gs.sh deploy -zones orange -cluster schema=partitioned-sync2backup total_members=1,1 /opt/fxall/sc/live/lib/sc-user-preferences.jar
`dirname $0`/gs.sh deploy -zones indigo -cluster schema=partitioned-sync2backup total_members=1,1 -max-instances-per-vm 1 /opt/fxall/sc/live/lib/sc-notifiers.jar
`dirname $0`/gs.sh deploy -zones black  -cluster schema=partitioned-sync2backup total_members=1,1  -max-instances-per-machine 1 /opt/fxall/sc/live/lib/sc-id.jar
`dirname $0`/gs.sh deploy -zones brown  -cluster schema=partitioned-sync2backup total_members=2,0 -max-instances-per-machine 1 /opt/fxall/sc/live/lib/sc-archivesearch.jar

`dirname $0`/gs.sh deploy -zones black -cluster schema=partitioned-sync2backup total_members=16,1 -max-instances-per-machine 1 /opt/fxall/sc/live/lib/sc-components.jar

`dirname $0`/gs.sh deploy -zones silver /opt/fxall/sc/live/lib/sc-startup.jar

`dirname $0`/gs.sh deploy -zones blue -cluster schema=sync_replicated  total_members=2 -max-instances-per-vm 1 /opt/fxall/sc/live/lib/sc.war 
`dirname $0`/gs.sh deploy -zones pink -cluster total_members=1 -max-instances-per-vm 1 /opt/fxall/sc/live/lib/scadmin.war 
`dirname $0`/gs.sh deploy -zones pink -cluster total_members=1 -max-instances-per-vm 1 /opt/fxall/sc/live/lib/swift-admin.war
`dirname $0`/gs.sh deploy -zones purple -cluster total_members=1 -max-instances-per-vm 1 /opt/fxall/sc/live/lib/sc-management.jar  

`dirname $0`/gs.sh deploy -zones yellow -cluster total_members=1 -max-instances-per-vm 1 /opt/fxall/sc/live/lib/sc-mq-yellow.jar  
`dirname $0`/gs.sh deploy -zones green -cluster total_members=1  -max-instances-per-vm 1 /opt/fxall/sc/live/lib/sc-mq-green.jar 
`dirname $0`/gs.sh deploy -zones red -cluster total_members=1  -max-instances-per-vm 1 /opt/fxall/sc/live/lib/sc-aq-red.jar 

