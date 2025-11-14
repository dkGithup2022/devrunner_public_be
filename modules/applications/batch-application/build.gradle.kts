
apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

dependencies {
    // Common modules
    implementation(project(":modules:common:logging"))
    implementation(project(":modules:schema"))

    // Batch modules
    implementation(project(":modules:tasks:resource-crawl-task"))
    implementation(project(":modules:tasks:elasticsearch-sync-task"))
    implementation(project(":modules:common:openai-base"))

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")

    // Database
    runtimeOnly("com.mysql:mysql-connector-j")  // 프로덕션용 MySQL 드라이버
    runtimeOnly("com.h2database:h2")            // 로컬 개발용

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// application-batch는 실행 가능한 JAR
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}

tasks.named<Jar>("jar") {
    enabled = false
}
