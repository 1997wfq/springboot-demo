#!/bin/bash

OPTION=$1
ENV=tst
SERVER_NAME=$2
JAR_NAME=$SERVER_NAME'.jar'
JAR_PATH='/data/deploy/'
BACKUP_PATH=${JAR_PATH}'/backup/'${SERVER_NAME}
UPLOAD_PATH='/data/'
JAVA_HOME='/home/java/jdk1.8.0_291/bin'

if [[ ! $SERVER_NAME =~ 'server' ]];then
    JAR_NAME=$SERVER_NAME'.war'
fi


if [ -z "$OPTION" ]; then 
    echo -e "\033[0;31m 未输入操作名 \033[0m  \033[0;34m {start|stop|restart|deploy|status} \033[0m"
    exit 1
fi


if [ -z "$SERVER_NAME" ]; then
    echo -e "\033[0;31m 未输入应用名 \033[0m"
    exit 1
fi

echo "$JAR_PATH$JAR_NAME"

JAVA_OPTS="
	-Xms1024m
	-Xmx2048m
	-XX:MaxNewSize=512m
	-XX:MetaspaceSize=256m
	-XX:MaxMetaspaceSize=512m
	-XX:ParallelGCThreads=4
	-XX:+UseParNewGC
	-XX:+UseConcMarkSweepGC
	-XX:-UseGCOverheadLimit
	-XX:+PrintGCTimeStamps
	-XX:+PrintGCDetails
        -XX:-OmitStackTraceInFastThrow
	-XX:+HeapDumpOnOutOfMemoryError
	-XX:HeapDumpPath=/tmp/
	-Xloggc:/data/deploy/logs/$SERVER_NAME/gc.$$.log
	-Dnetworkaddress.cache.ttl=7200
	-Dsun.net.inetaddr.ttl=7200"

cd $JAR_PATH

function start()
{
	if [ ! -f "$JAR_PATH$JAR_NAME" ];then
	echo -e "\033[0;31m 应用程序包不存在 \033[0m"
	exit 1
	fi
    count=`ps -ef |grep java|grep $SERVER_NAME|grep -v grep|wc -l`
    if [ ! -d logs ];then
          mkdir -p logs
    fi
    
    if [ $count != 0 ];then
        echo "$SERVER_NAME is running..."
    else
        echo "Start $SERVER_NAME success..."
        nohup $JAVA_HOME/java $JAVA_OPTS -jar $JAR_NAME --spring.profiles.active=tst > ${JAR_PATH}logs/${SERVER_NAME}.out 2>&1 &
    fi
}

function stop()
{
    echo "Stop $SERVER_NAME"
    boot_id=`ps -ef |grep java|grep $SERVER_NAME|grep -v grep|awk '{print $2}'`
    count=`ps -ef |grep java|grep $SERVER_NAME|grep -v grep|wc -l`
    if [ $count != 0 ];then
        kill $boot_id
        sleep 10
	count=`ps -ef |grep java|grep $SERVER_NAME|grep -v grep|wc -l`
	if [ $count != 0 ];then
    		echo "kill process $boot_id"
    		kill -9 $boot_id
	fi
    fi
}

function restart()
{
    stop
    sleep 2
    start
}

function deploy()
{
	if [ ! -f "$UPLOAD_PATH$JAR_NAME" ];then
		echo -e "\033[0;31m 应用程序包未上传到$UPLOAD_PATH \033[0m"
		exit 1
	fi
	stop
    sleep 2
	
	#backup file
	if [ ! -d $BACKUP_PATH ];then
	  mkdir -p $BACKUP_PATH
	fi
	BAKUPTIME=`date +%Y%m%d%H%M%S`
	if [ -f "$JAR_PATH$JAR_NAME" ];then
		mv ${JAR_NAME} $BACKUP_PATH/${SERVER_NAME}_bak${BAKUPTIME}.jar
	fi
	mv ${UPLOAD_PATH}/${JAR_NAME} ${JAR_NAME}
	
	start
	
	#remove old backup
	find $BACKUP_PATH/ -type f -mtime +5 -exec rm {} \;
}

function status()
{
    count=`ps -ef |grep java|grep $SERVER_NAME|grep -v grep|wc -l`
    if [ $count != 0 ];then
        echo "$SERVER_NAME is running..."
    else
        echo "$SERVER_NAME is not running..."
    fi
}

case $OPTION in
    start)
    start;;
    stop)
    stop;;
    restart)
    restart;;
    status)
    status;;
	deploy)
	deploy;;
    *)

    echo -e "\033[0;31m Usage: \033[0m  \033[0;34m sh  $0  {start|stop|restart|status}  {SERVER_NAME} \033[0m
\033[0;31m Example: \033[0m
      \033[0;33m sh  $0  start esmart-test.jar \033[0m"
esac
