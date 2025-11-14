# URL List Parser

> **📝 이 파트는 공개되지 않습니다.**

## 제거된 이유

이 폴더에는 각 회사 채용 사이트의 HTML 구조를 직접 파싱하여 채용 공고 URL 목록을 추출하는 구현체들이 포함되어 있었습니다.

HTML을 직접 다루는 부분을 공개하는 것은 도의적으로 매너가 아닌 것 같아서 공개하지 않습니다.

## 인터페이스

상위 폴더의 `UrlListParser` 인터페이스를 참고하세요.

```java
public interface UrlListParser {
    /**
     * 채용 공고 목록 페이지에서 개별 공고 URL 추출
     *
     * @param html 목록 페이지 HTML
     * @return 채용 공고 URL 리스트
     */
    List<String> parse(String html);
}
```

## 구현되었던 파서들


- `NetflixUrlListParser` - Netflix Jobs
- `SpotifyUrlListParser` - Spotify Jobs
- `TikTokUrlListParser` - TikTok Careers
- `NaverUrlListParser` - Naver Recruit
- `LineUrlListParser` - Line Careers
- `WoowahanUrlListParser` - Woowahan Bros
- `KarrotUrlListParser` - Karrot (Daangn)
