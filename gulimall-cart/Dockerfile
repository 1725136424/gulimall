FROM java:8
MAINTAINER 1725136424@qq.com
EXPOSE 8080
VOLUME /tmp
ADD target/*.jar /app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=prod"]