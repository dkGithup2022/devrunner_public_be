# DevRunner Backend

서비스 devrunner (데브러너) 의 백엔드 코드를 일부 공개합니다. 보안/외부 리소스 부분을 제외한 부분이 공개됩니다.

---

## 📌 운영 서비스 소개

- **서비스 링크**: https://devrunner.dev/
- **운영 현황**: 14일 현재 Clarity 기준, 3일간 800+명 고유 접속자
- **유입 경로**: Clarity를 보니까 긱뉴스에서 많이 와주셨습니다. 감사합니다.

---

## 💡 공개 이유

1. **개발 생산성 공유**
   이전 [클로드 코드 | 멀티모듈 프로젝트 생성 커맨드 모음](https://github.com/dkGithup2022/claude_code_multimodule_script) 의 내용을 활용해서, 
   개발기간 압축한 사례로서 기록 .
   
---

## 🤖 Claude Code Command 사용 후기


### 개발 소요 시간
- **FE + BE 개발**: 2주
- **배포 세팅 및 디테일**: 1주
  - 디테일 내용: 로고 추가, 검색 컨텍스트 생성 프롬프트 손보기 등

총 3주 정도 소요 

---

## 📦 공개 범위

### ✅ 공개하는 부분
- `model`, `data`, `api`, `elasticsearch` 등 기본적인 내용들
- `repo`, `service`, `api`에 대한 테스트
- Claude Code Script: [링크](https://github.com/dkGithup2022/claude_code_multimodule_script)

### ❌ 공개하지 않는 부분
- `auth` 모듈
- 외부 리소스와 관련한 부분
- `repo`, `service`, `api`가 아닌 모듈의 테스트

---

## 🏗️ 아키텍처

### 설계 관점

완성된 아키텍처(헥사고날, 포트 앤 어댑터)보다는 **MVP와 실개발 모두에서 보편적으로 이득이 되는 중간 구조**를 채택

모듈별 역할과 제약은 아래와 같습니다.

---

### 📊 데이터 구조 분리

데이터의 물리 구조와 논리 구조를 나누고 시작합니다.

| 모듈 | 역할 |
|------|------|
| **model** | 도메인 객체, 데이터 스펙과 제약, 모듈 간 통신의 일관성 유지 |
| **repository-jdbc** | 논리 구조(model)와 물리 구조(entity) 분리, Spring Data JDBC + Derived Query 사용 |
| **infrastructure** | 모델 조작 인터페이스 모음, 현재 구조에서는 repository-jdbc와 합쳐도 무방하나 관례적으로 인터페이스는 별도 모듈 보관 |

---

### 🎯 관심사별 모듈 분리

`apis`, `applications`, `tasks`는 관심사별로 별도 모듈에서 관리합니다.

| 모듈 | 역할 |
|------|------|
| **apis** | REST API 컨트롤러 및 DTO, 하위 모듈로 관심사별로 `search`, `domain api` 분리 |
| **tasks** | 배치로 실행될 로직 분리, 개인적인 선호로 Spring Batch를 사용하지 않고 Spring과 기본 Java 기능으로 진행, 관심사별로 `es-sync`, `crawl` 별도 모듈 관리 |
| **applications** | 배포 단위 관리, 각 배포 애플리케이션이 별도 모듈에서 관리 |

---

### 🔧 기능별 모듈

| 모듈 | 역할 |
|------|------|
| **elasticsearch** | 검색에 대한 자료형, API, 추천 로직 등 공통 관리, 검색은 문서 특성과 쿼리 형식이 함께 가는 경우가 많기 때문에 데이터 분리 없이 하나의 모듈에서 관리 |
| **outbox** | Transactional Outbox 패턴 구현 |
| **auth** | 인증/인가 처리 |
| **openai-base** | OpenAI API 통합 |

---

## 🧪 테스트 규칙

### 테스트 관점

각 모듈별 **외부에 노출되는 API의 구현체**를 중점적으로 테스트합니다.

`service`, `api`는 공통적으로 mocking을 기반으로 한 단위 테스트를 진행하지만 필요한 경우 통합 테스트를 진행합니다.

외부 API(Google, Firecrawl, OpenAI)는 이 프로젝트에서는 `@Disabled`로 CI 과정에서 테스트하지 않습니다. (비용 및 시간 고려)

### 자동화 관점

각 테스트에 대한 전략이 수립되었다면, 테스트 대상, 방법, 레퍼런스를 `.claude` 하위에 기록하고 자동화를 시도합니다.

지금까지 진행된 스크립트는 [링크](https://github.com/dkGithup2022/claude_code_multimodule_script)를 참고해주세요.

---

### 모듈별 테스트 전략

#### 📁 repository-jdbc
- Entity→Model 스펙에 대한 검증이 포함됩니다
- Derived Query가 아닌 커스텀 쿼리에 대해서 H2 통합 테스트를 수행합니다
- Derived Query(자동 생성)의 경우 테스트하지 않습니다

#### 🔧 service
- 외부 모듈로 노출되는 기능에 대해서 테스트합니다
- Repository API와 필요 의존성을 모킹하여 단위 테스트를 진행합니다
- 순서, 개수, 번호 등 동시성을 케어해야 하는 경우 H2 연동한 통합 테스트를 수행합니다

#### 🌐 api
- Controller 메서드에 대한 직접 호출을 테스트합니다
- Auth Context를 주입받는 과정과 별개이므로 MockMvc 등으로 호출하지 않습니다
- 기본적으로 mocking을 통한 단위 테스트를 수행합니다

#### 🚀 application
- 현재 스크립트 제작 중
- 지저분하지 않으면서 자동화하는 방법을 찾고 있으며, 깔끔해지면 다시 공유 예정입니다
- (현재 application의 테스트는 제거된 상태)

#### 🔄 기타 모듈
- 위의 service와 유사하게 진행합니다

---

## ⚙️ 데이터 플로우

1. **모든 변경 사항은 1차적으로 RDBMS에 transactional하게 저장됩니다.**

2. **ES (피드, 검색, 추천)에 적용되어야 하는 내용이 있다면:**
   - `outbox` 모듈을 통해 RDBMS에 이력을 저장합니다
   - 이후, 초단위 배치로 `task` 쪽의 `es-sync` 기능으로 옮깁니다

## ⚙️ 배포 
   ```
   GitHub Actions → DockerHub → ArgoCD → K8s
   ```

---

## 🙏 맺음말

모두 행복하세요.
