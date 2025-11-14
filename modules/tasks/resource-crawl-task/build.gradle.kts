apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

dependencies {
    // Spring Core
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-context")

    // Spring Data JDBC
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")

    // Web (for bean registration testing)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // playwright
    implementation("com.mashape.unirest:unirest-java:1.4.9")
    implementation("io.github.bonigarcia:webdrivermanager:5.5.3")

    // jsoup
    implementation("org.jsoup:jsoup:1.17.2")

    // Database
    runtimeOnly("com.h2database:h2")

    // rss parser
    implementation("com.rometools:rome:2.1.0")

    // Project modules
    implementation(project(":modules:common:openai-base"))
    implementation(project(":modules:devrunner:model"))
    implementation(project(":modules:devrunner:infrastructure"))
    implementation(project(":modules:devrunner:repository-jdbc"))
    implementation(project(":modules:devrunner:outbox"))
    // Firecrawl (예시 - 실제 dependency 확인 필요)
    // implementation("com.firecrawl:firecrawl-java:x.x.x")

    // Selenium with BOM
    implementation(platform("org.seleniumhq.selenium:selenium-bom:4.34.0"))
    implementation("io.github.bonigarcia:webdrivermanager:5.5.3")
    implementation("org.seleniumhq.selenium:selenium-java")
    implementation("org.seleniumhq.selenium:selenium-chrome-driver")
    implementation("org.seleniumhq.selenium:selenium-devtools-v138")

    // Playwright (최신 버전으로 업데이트)
    implementation("com.microsoft.playwright:playwright:1.50.0")
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
