plugins {
    id("project.application-conventions")
    id("project.integration-test-conventions")
    id("project.kotlin-conventions")
    id("project.spring-conventions")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // jpa & querydsl
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
    runtimeOnly("com.h2database:h2")

    // upbit
    implementation("com.auth0:java-jwt:3.18.2") // upbit api 헤더 생성을 위함
}