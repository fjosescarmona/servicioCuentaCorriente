FROM openjdk:8
VOLUME /tmp
ADD ./target/servicioCuentaCorriente-0.0.1-SNAPSHOT.jar servicioCuentaCorrienteP.jar
ENTRYPOINT ["java","-jar","/servicioCuentaCorrienteP.jar"]