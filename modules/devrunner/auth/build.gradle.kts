/*
 * Copyright 2024 breakin Inc. - All Rights Reserved.
 */

apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

dependencies {
    // Spring Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // User 조회를 위한 의존성
    implementation(project(":modules:devrunner:model"))
    implementation(project(":modules:devrunner:infrastructure"))



    // Spring Data JDBC
    // 임시 - 이후 redis 등 다른 로직으로 변경 필요 .
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    testImplementation("com.h2database:h2")

    // Caffeine Cache (TTL 자동 관리)
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Logging
    implementation("org.slf4j:slf4j-api")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
