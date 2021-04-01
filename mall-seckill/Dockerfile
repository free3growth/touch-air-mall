FROM java:8
EXPOSE 8080

VOLUME /tmp
WORKDIR /home
ADD /target/*.jar /app.jar
RUN bash -c "touch /app.jar"
ENTRYPOINT ["java","-jar","-Xms300m","-Xmx300m","/app.jar","--spring.profiles.active=prod"]