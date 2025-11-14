/*
 * Copyright 2024 devrunner Inc. - All Rights Reserved.
 */

apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation(project(":modules:devrunner:model"))
    implementation(project(":modules:devrunner:exception"))
    implementation(project(":modules:devrunner:infrastructure"))
    implementation(project(":modules:devrunner:auth"))
    implementation(project(":modules:devrunner:outbox"))

    implementation("org.springframework:spring-web")

    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-tx")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc")

    testImplementation(project(":modules:devrunner:repository-jdbc"))
    testImplementation(project(":modules:schema"))
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
