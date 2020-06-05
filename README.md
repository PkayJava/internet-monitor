# Internet Monitoring

:/>mvn clean install -DskipTests
# you will get target/internet-monitor.zip

:/>mkdir /opt/internet-monitor
:/>cd /opt/internet-monitor
:/>unzip internet-monitor.zip

# rename sample configuation the sample is send.one.com configuration
mv /opt/internet-monitor/conf/server.xml.sample /opt/internet-monitor/conf/server.xml

# update server.xml
1. mailSenderHost : smtp server
2. mailSenderPort : smtp port
3. mailSenderProtocol : smtp
4. mailSenderSsl : true or false
5. mailSenderUsername : username
6. mailSenderPassword : password
7. mailSenderFrom : from email
8. mailSenderName : sender name
9. mailTo : recipient e-mail address
10. pingUrl : ping url
11. interval : ping frequency in minute
12. alertInterval : alert mail frequency in minute

# Deployment & Start

1. Download Tomcat 9.x.x and extract into your location, mabye (/opt/tomcat-9.0
2. It is required to compile /opt/tomcat-9.0/bin/commons-daemon-native.tar.gz and copy jsvc into /opt/tomcat-9.0/bin

:/>/opt/tomcat-9.0/bin/daemon.sh --java-home /opt/jdk-1.8.0 --catalina-home /opt/tomcat-9.0 --tomcat-user root --catalina-base /opt/internet-monitor start
