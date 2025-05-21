# Etapa de build
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa de runtime
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
WORKDIR /app

# Argumentos de build para configurar variáveis no application.properties/yml
ARG DATASOURCE_URL
ARG DATASOURCE_USER
ARG DATASOURCE_PASS
ARG ID_CHANNEL_DISCORD
ARG DISCORD_TOKEN

# Copia o .jar gerado na etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Define variáveis de ambiente (acessíveis pelo Spring via ${})
ENV DATASOURCE_URL=${DATASOURCE_URL}
ENV DATASOURCE_USER=${DATASOURCE_USER}
ENV DATASOURCE_PASS=${DATASOURCE_PASS}
ENV ID_CHANNEL_DISCORD=${ID_CHANNEL_DISCORD}
ENV DISCORD_TOKEN=${DISCORD_TOKEN}

# Expõe a porta da aplicação (caso queira)
EXPOSE 8080

# Executa o jar
ENTRYPOINT ["java", "-jar", "app.jar"]

