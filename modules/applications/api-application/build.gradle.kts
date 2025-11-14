/*
 * Copyright 2024 devrunner Inc. - All Rights Reserved.
 */

apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation(project(":modules:apis:api"))
    implementation(project(":modules:apis:search-api"))
    implementation(project(":modules:devrunner:exception"))
    implementation(project(":modules:devrunner:service"))
    implementation(project(":modules:devrunner:repository-jdbc"))
    implementation(project(":modules:devrunner:auth"))  // Auth 모듈 (세션 관리)
    implementation(project(":modules:schema"))  // DDL/DML 스크립트

    implementation(project(":modules:common:logging"))


    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    runtimeOnly("com.mysql:mysql-connector-j")  // 프로덕션용 MySQL 드라이버
    runtimeOnly("com.h2database:h2")            // 로컬 개발용

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
    archiveClassifier = ""
}
