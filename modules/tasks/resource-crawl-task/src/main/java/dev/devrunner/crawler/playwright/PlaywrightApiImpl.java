package dev.devrunner.crawler.playwright;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Playwright API 구현체
 *
 * Headless Chromium으로 동적 페이지를 렌더링하여 HTML 추출
 * - CSR(Client-Side Rendering) 페이지 지원
 * - Lazy loading 대비 스크롤
 * - 재시도 로직 (최대 3회)
 */
@Component
@Slf4j
public class PlaywrightApiImpl implements PlaywrightApi {

    private static final int TIMEOUT_MS = 20000;
    private static final int MAX_SCROLL_COUNT = 3;
    private static final int SCROLL_INTERVAL_MS = 1000;
    private static final int MAX_RETRIES = 3;

    @Value("${playwright.wait-after-load-ms:5000}")
    private int waitAfterLoadMs;

    @Override
    public String waitAndGetHtml(String url, int waitInSeconds) throws InterruptedException {
        int waitMs = waitInSeconds * 1000;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return getRenderedHtml(url, waitMs);
            } catch (Exception e) {
                log.error("❌ [Attempt {}/{}] Playwright error for URL: {}", attempt, MAX_RETRIES, url);
                log.error("Exception details: ", e);

                if (attempt == MAX_RETRIES) {
                    throw new PlaywrightException(
                            "Playwright failed after " + MAX_RETRIES + " attempts for URL: " + url, e
                    );
                }

                // 재시도 전 랜덤 대기 (봇 탐지 회피)
                int waitSeconds = 10 + new Random().nextInt(3); // 10~12초
                log.info("⏳ Retrying in {} seconds...", waitSeconds);
                Thread.sleep(waitSeconds * 1000L);
            }
        }

        throw new IllegalStateException("Unreachable code after retries");
    }

    private String getRenderedHtml(String url, int waitMs) throws InterruptedException {
        log.debug("Playwright request: url={}, waitMs={}", url, waitMs);

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );

            try (BrowserContext context = browser.newContext()) {
                Page page = context.newPage();

                // 페이지 로드
                page.navigate(url, new Page.NavigateOptions()
                        .setTimeout(TIMEOUT_MS)
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                );

                // JS 실행 완료 대기
                page.waitForTimeout(waitMs);

                // Lazy loading 대비 스크롤
                scrollPage(page);

                String html = page.content();
                log.debug("Playwright response: url={}, htmlLength={}", url, html.length());

                return html;

            } finally {
                browser.close();
            }
        }
    }

    private void scrollPage(Page page) throws InterruptedException {
        for (int i = 0; i < MAX_SCROLL_COUNT; i++) {
            page.evaluate("window.scrollBy(0, 1000);");
            Thread.sleep(SCROLL_INTERVAL_MS);
        }
    }
}
