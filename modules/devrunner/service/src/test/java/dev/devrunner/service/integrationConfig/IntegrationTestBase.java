package dev.devrunner.service.integrationConfig;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 서비스 레이어 통합 테스트 베이스 클래스
 *
 * 특징:
 * - @SpringBootTest: IntegrationConfig를 사용하여 전체 Spring Context 로드
 * - @Transactional: 각 테스트 메서드 실행 후 자동 롤백 (데이터 격리)
 * - H2 In-Memory DB 사용 (application.yml 설정)
 * - JDBC 레포지토리 및 서비스 레이어 Bean 자동 로드
 *
 * 사용법:
 * <pre>
 * {@code
 * class MyServiceIntegrationTest extends IntegrationTestBase {
 *
 *     @Autowired
 *     private MyService myService;
 *
 *     @Test
 *     void testSomething() {
 *         // 테스트 로직
 *     }
 * }
 * }
 * </pre>
 */
@SpringBootTest(classes = IntegrationConfig.class)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {


}
