# Builds the Java café app. It talks to the PostgreSQL container over JDBC.
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Modern PostgreSQL JDBC driver, replacing the ancient pg73jdbc3.jar in the repo
# (which predates current PostgreSQL and Java).
ADD https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar /app/postgresql.jar

COPY project/java/src/Cafe.java /app/
RUN javac -cp /app/postgresql.jar -d /app/classes /app/Cafe.java

# The db service is reachable at host "db" on the compose network.
ENV DB_HOST=db

# Args are: <dbname> <port> <user> (see Cafe.java). Password is empty because
# the db uses trust auth on the compose network.
CMD ["sh", "-c", "java -cp /app/classes:/app/postgresql.jar Cafe cafe 5432 cafe"]
