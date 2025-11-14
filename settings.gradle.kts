/*
 * Copyright 2024 devrunner Inc. - All Rights Reserved.
 */

rootProject.name = "devrunner-be"

// Applications (실행 가능한 애플리케이션)
include(":modules:applications:api-application")
include(":modules:applications:batch-application")

// Tasks (배치 작업 모듈)
include(":modules:tasks:resource-crawl-task")
include(":modules:tasks:elasticsearch-sync-task")

// Common (범용 기술 모듈)
include(":modules:common:openai-base")
include(":modules:common:logging")
include(":modules:common:encryption")

// APIs (외부 인터페이스 레이어)
include(":modules:apis:api")
include(":modules:apis:search-api")

// Schema (데이터베이스 스키마)
include(":modules:schema")

// DevRunner (프로젝트 고유 비즈니스 모듈)
include(":modules:devrunner:model")
include(":modules:devrunner:service")
include(":modules:devrunner:exception")
include(":modules:devrunner:infrastructure")
include(":modules:devrunner:repository-jdbc")
include(":modules:devrunner:elasticsearch")
include(":modules:devrunner:auth")
include(":modules:devrunner:outbox")
pluginManagement {
    buildscript {
        repositories {
            gradlePluginPortal()
        }
    }

    repositories {
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}