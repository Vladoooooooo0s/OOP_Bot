# Используем официальный образ OpenJDK
FROM openjdk:17-jdk-slim AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Устанавливаем Maven
RUN apt-get update && apt-get install -y maven

# Копируем файлы pom.xml и исходный код в контейнер
COPY pom.xml .
COPY src ./src

# Собираем проект с помощью Maven
RUN mvn clean package -DskipTests

# Используем образ с Java 17
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный .jar файл из предыдущего этапа
COPY --from=build /app/target/VictorinyOOPbot-0.0.1-SNAPSHOT.jar VictorinyOOPbot.jar

# Открываем порт для доступа к приложению (по умолчанию Spring Boot использует порт 8080)
EXPOSE 8080

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "VictorinyOOPbot.jar"]
