FROM maven:3-jdk-11 as build
WORKDIR /applis
COPY pom.xml .
COPY src ./src
#RUN mvn -Dmaven.test.skip=true clean package
RUN mvn -Dmaven.test.skip=true clean package spring-boot:repackage -Dspring.profiles.active=docker
#RUN mvn -Dmaven.test.skip=true clean package -Dspring.profiles.active=docker
##RUN mvn -f pom.xml clean package
##RUN mkdir -p target/dependency && (cd target/dependency; jar -xf /applis/target/*.jar)

FROM openjdk:11 as back-server
#RUN addgroup -S spring && adduser -S spring -G spring
##ARG DEPENDENCY=/applis/target/dependency
##COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /applis/lib
##COPY --from=build ${DEPENDENCY}/META-INF /applis/META-INF
##COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /applis
##ENTRYPOINT ["java","-cp","applis:applis/lib/*","fr.abes.lnevent"]
COPY --from=build /applis/target/*.jar /applis/target/licences-nationales.jar
#RUN chown  appuser:appuser /usr/local/lib/demo.jar
#USER appuser
ENTRYPOINT ["java","-jar","/applis/target/licences-nationales.jar"]
#USER spring:spring
