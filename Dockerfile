FROM maven:3.6.3-jdk-11 AS MAVEN_BUILD

COPY ./ ./

RUN mvn clean package

FROM openjdk:11-jre

COPY --from=MAVEN_BUILD docker/bob_the_discord_bot-*.jar /discord.jar

CMD ["java", "-jar", "/discord.jar"]