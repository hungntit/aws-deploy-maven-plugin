FROM openjdk:8-jdk-alpine
ADD *.war /var/lib/war/server.war
EXPOSE 8080
CMD java ${JAVA_OPTS:- -Xms384m -Xmx384m -XX:MaxMetaspaceSize=128m} -jar /var/lib/war/server.war
