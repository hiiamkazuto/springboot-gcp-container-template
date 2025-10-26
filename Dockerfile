# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk-noble AS deps

WORKDIR /workspace

COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/

# Mavenの依存関係を事前に取得してビルドキャッシュ化
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -DskipTests

FROM deps AS package

WORKDIR /workspace

COPY ./src src/

# 事前に取得した依存関係を使い、アプリケーションをビルド
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests && \
    mv target/*.jar target/app.jar

FROM package AS extract

WORKDIR /workspace

# LayertoolsでJARを複数レイヤーに分割し、キャッシュ効率を向上
RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

FROM gcr.io/distroless/java25-debian13:nonroot AS final

# アプリケーションのコピー（キャッシュ効率のため、変更頻度の低い順にコピー）
COPY --from=extract workspace/target/extracted/dependencies/ ./
COPY --from=extract workspace/target/extracted/spring-boot-loader/ ./
COPY --from=extract workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=extract workspace/target/extracted/application/ ./

ENTRYPOINT [ "java", "org.springframework.boot.loader.launch.JarLauncher" ]
