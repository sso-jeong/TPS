#!/bin/bash

printf "\n\n"
echo "####################################################"
echo "#                                                  #"
echo "#                                                  #"
echo "#           start ProObject21 Test TPS             #"
echo "#                                                  #"
echo "#                                                  #"
echo "####################################################"

DATE=`date +%Y%m%d`
DIR=$PWD

FIND_RESULT=`find "$PWD/apache-jmeter-5.4.1.tgz"`

if [ -z $FIND_RESULT ] ; then
 echo "empty"
 tar -xvf $PWD/apache-jmeter-5.4.1.tgz
else
 echo "not empty"
fi

export JMETER_HOME=$DIR/apache-jmeter-5.4.1/bin
export TEST_NAME
export SERVER_IP
export SERVER_PORT
export ENCODING
export REST_TYPE
export REQUEST_URL
export INPUT_DATA
export LOOP_COUNT

if [ ! -d $DIR/Result ] ; then
 mkdir $DIR/Result
else
 echo "not empty"
fi

if [ ! -d $DIR/Result/BackUp ] ; then
 mkdir $DIR/Result/BackUp
else
 echo "not empty"
fi


#### Test Name ####
printf "\n\n"
printf "Enter the Test Name   "
printf "\n"
read -e -p "ex)PO21_CALL_TPS_TEST                   : " TEST_NAME

if [ ! -d $DIR/Result/$TEST_NAME ] ; then
 echo "$TEST_NAME" >> $DIR/Result/BackUp/inputBackup_$DATE.txt
 mkdir $DIR/Result/$TEST_NAME
else
 echo "Duplicate Test Name"
 echo "Please Run again"
 exit 1
fi

#### server info ####
printf "\n\n"
printf "Enter the Test Server Ip (SFM Master Server IP)   "
printf "\n"
read -e -p "ex)210.x.x.x                   : " SERVER_IP

echo "$SERVER_IP" >> $DIR/Result/BackUp/inputBackup_$DATE.txt

#### server port info ####
printf "\n\n"
printf "Enter the Test Server Port (SFM MS Http-server-port)   "
printf "\n"
read -e -p "ex)14000                   : " SERVER_PORT

echo "$SERVER_PORT" >> $DIR/Result/BackUp/inputBackup_$DATE.txt

#### test encoding info ####
printf "\n\n"
printf "Enter the Test Encoding Type   "
printf "\n"
read -e -p "ex)UTF-8                    : " ENCODING

echo "$ENCOFING" >> $DIR/Result/BackUp/inputBackup_$DATE.txt

#### test restType info ####
printf "\n\n"
printf "Enter the Test RestType   "
printf "\n"
read -e -p "ex)POST                    : " REST_TYPE


echo "$REST_TYPE" >> $DIR/Result/BackUp/inputBackup_$DATE.txt


#### test reqest url info ####
printf "\n\n"
printf "Enter the Test Request URL   "
printf "\n"
read -e -p "ex)TestBootMain-21/TestBootMain/TestBootSG/CallerService_call                   : " REQUEST_URL

echo "$REQUEST_URL" >> $DIR/Result/BackUp/inputBackup_$DATE.txt

#### input data info ####
printf "\n\n"
printf "Enter the input Key:value   "
printf "\n"
read -e -p "ex)\{\"dto\"\:\{\"value\"\:\"TestBootMain\.TestBootSG\.MainCalleeService\_sameThread\"\}} : " INPUT_DATA

echo "$INPUT_DATA" >> $DIR/Result/BackUp/inputBackup_$DATE.txt

INPUT=`cat $DIR/Result/BackUp/inputBackup_$DATE.txt | sed -e "s/{/\&#xd;/g" -e "s/}/\&#xd;/g" -e "s/\"/\&quot;/g"`


#### loop count ####
printf "\n\n"
printf "Enter the TPS   "
printf "\n"
read -e -p "ex)5000                   : " LOOP_COUNT

cp -r $JMETER_HOME/ORIGIN.jmx $JMETER_HOME/${TEST_NAME}.${DATE}.jmx

sed -i "s/TEST_NAME/${TEST_NAME}/g" $JMETER_HOME/${TEST_NAME}.${DATE}.jmx
sed -i "s/SERVER_IP/${SERVER_IP}/g" $JMETER_HOME/${TEST_NAME}.${DATE}.jmx
sed -i "s/SERVER_PORT/${SERVER_PORT}/g" $JMETER_HOME/${TEST_NAME}.${DATE}.jmx
sed -i "s/ENCODING/${ENCODING}/g" $JMETER_HOME/${TEST_NAME}.${DATE}.jmx
sed -i "s/REST_TYPE/${REST_TYPE}/g" $JMETER_HOME/${TEST_NAME}.${DATE}.jmx
sed -i "s|REQUEST_URL|${REQUEST_URL}|g" $JMETER_HOME/${TEST_NAME}.${DATE}.jmx
sed -i "s/INPUT/${INPUT_DATA}/g" $JMETER_HOME/${TEST_NAME}.${DATE}.jmx
sed -i  "s/LOOP_COUNT/${LOOP_COUNT}/g" $JMETER_HOME/${TEST_NAME}.${DATE}.jmx

if [ ! -d $DIR/Result/$TEST_NAME/report ] ; then
 mkdir $DIR/Result/$TEST_NAME/report
else
 echo "not empty"
fi

sh $JMETER_HOME/jmeter.sh -n -t $JMETER_HOME/${TEST_NAME}.${DATE}.jmx -l $DIR/Result/$TEST_NAME/${TEST_NAME}.${DATE}.csv -e -o $DIR/Result/$TEST_NAME/report

tar -cvf $DIR/Result/$TEST_NAME/$TEST_NAME.report.tar $DIR/Result/$TEST_NAME/report/

echo "####################################################"
echo "#                                                  #"
echo "#                                                  #"
echo "#           end ProObject21 Test TPS               #"
echo "#                                                  #"
echo "#                                                  #"
echo "####################################################"
