apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

dependencies {
    api(platform("org.springframework.ai:spring-ai-bom:1.0.0"))

    api("org.springframework.ai:spring-ai-starter-model-openai")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
