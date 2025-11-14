/*
 * Copyright 2024 breakin Inc. - All Rights Reserved.
 */

dependencies {
    implementation(project(":modules:devrunner:model"))
    implementation(project(":modules:devrunner:exception"))


    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")


}
