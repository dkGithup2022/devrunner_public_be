package dev.devrunner.model.common;

public enum TechCategory {
    // 데이터베이스 관련
    SQL, RDMS, MYSQL, POSTGRESQL, SQLITE, MONGO_DB, REDIS, MARIA_DB, ORACLE, DYNAMO_DB, ELASTICSEARCH, CASSANDRA, COUCHDB, CLICK_HOUSE,

    // 클라우드 관련
    AWS, AZURE, GCP, SERVERLESS,

    // 네트워크 및 인프라 관련
    NETWORK, FIREWALL, WEBSERVER, NGINX, HOME_SERVER, DOCKER, K8S, PROMETHEUS, GRAFANA, NAGIOS, SPLUNK,

    // DevOps 도구
    TERRAFORM, PUPPET, ANSIBLE, IAC, JENKINS, GITLAB, GIT, CIRCLE_CI, TRAVIS_CI, CICD,

    // 프로그래밍 언어
    PYTHON, JAVASCRIPT, JAVA, C_SHARP, C_PLUS_PLUS, PHP, RUBY, SWIFT, KOTLIN, RUST, TYPESCRIPT, R, PERL, GO,

    // 프레임워크 및 라이브러리
    REACT, ANGULAR, RUBY_ON_RAILS, SPRING, EXPRESS_JS, LARAVEL, ASP_NET_CORE, NEXT_JS, NUXT_JS, SVELTE, FLUTTER, REACT_NATIVE,

    // AI 및 머신러닝
    ML, NLP, LLM, LLAMA, OPENAI,

    // 기타 기술
    VM, GITHUB_ACTIONS,
    // 추가 하기 0316
    CAREER, FLUENT_BIT, ETC, HARDWARE, OS,

    // 추가 하기 0706
    BACKEND, FRONTEND, DEVOPS, MACHINE_LEARNING, DATA_ENGINEERING, SYSTEM_ARCHITECTURE, ANDROID, NODE_JS, IOS, KAFKA, NO_SQL;

    public static TechCategory safeFrom(String value) {
        if (value == null) return null;

        try {
            return TechCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Invalid tech category value, return null
            return null;
        }
    }
}
