package dev.devrunner.crawler.urlUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class UrlCleaner {
    private UrlCleaner() {}

    /* ===== Public APIs ===== */

    /** Remove ALL query parameters (keeps scheme, host, path, fragment). */
    public static String stripAllQuery(String url) {
        return rebuild(url, Collections.emptyMap(), Mode.KEEP_ONLY, Collections.emptySet());
    }

    /** Remove only the given query keys (case-sensitive). */
    public static String stripParams(String url, Set<String> toRemove) {
        return rebuild(url, parseQuery(url), Mode.REMOVE, toRemove);
    }

    /** Keep ONLY the given keys; drop everything else. */
    public static String keepOnly(String url, Set<String> toKeep) {
        return rebuild(url, parseQuery(url), Mode.KEEP_ONLY, toKeep);
    }

    /* ===== Internals ===== */

    private enum Mode { REMOVE, KEEP_ONLY }

    /** Parse query into a multimap (key -> list of values). */
    private static Map<String, List<String>> parseQuery(String url) {
        URI u = URI.create(url);
        String q = u.getRawQuery();
        Map<String, List<String>> map = new LinkedHashMap<>();
        if (q == null || q.isEmpty()) return map;

        for (String pair : q.split("&")) {
            if (pair.isEmpty()) continue;
            String[] kv = pair.split("=", 2);
            String key = urlDecode(kv[0]);
            String val = kv.length > 1 ? urlDecode(kv[1]) : "";
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(val);
        }
        return map;
    }

    /** Rebuild URL after applying filter. */
    private static String rebuild(String url,
                                  Map<String, List<String>> original,
                                  Mode mode,
                                  Set<String> set) {
        URI u = URI.create(url);

        Map<String, List<String>> filtered;
        if (mode == Mode.REMOVE) {
            filtered = new LinkedHashMap<>();
            for (var e : original.entrySet()) {
                if (!set.contains(e.getKey())) filtered.put(e.getKey(), e.getValue());
            }
        } else { // KEEP_ONLY
            filtered = new LinkedHashMap<>();
            for (var k : set) {
                if (original.containsKey(k)) filtered.put(k, original.get(k));
            }
        }

        String newQuery = filtered.isEmpty() ? null : toQueryString(filtered);

        // If absolute URL: use 7-arg constructor to preserve components
        if (u.isAbsolute()) {
            try {
                return new URI(
                        u.getScheme(),
                        u.getUserInfo(),
                        u.getHost(),
                        u.getPort(),
                        u.getRawPath(),   // keep original encoding
                        newQuery,
                        u.getRawFragment()
                ).toString();
            } catch (URISyntaxException e) {
                // Fallback: simple manual rebuild
                return manualRebuild(u, newQuery);
            }
        }

        // Relative URL fallback (no scheme/host)
        StringBuilder sb = new StringBuilder();
        if (u.getRawPath() != null) sb.append(u.getRawPath());
        if (newQuery != null) sb.append('?').append(newQuery);
        if (u.getRawFragment() != null) sb.append('#').append(u.getRawFragment());
        return sb.toString();
    }

    private static String manualRebuild(URI u, String newQuery) {
        StringBuilder sb = new StringBuilder();
        if (u.getScheme() != null) sb.append(u.getScheme()).append(':');
        if (u.getRawAuthority() != null) sb.append("//").append(u.getRawAuthority());
        if (u.getRawPath() != null) sb.append(u.getRawPath());
        if (newQuery != null) sb.append('?').append(newQuery);
        if (u.getRawFragment() != null) sb.append('#').append(u.getRawFragment());
        return sb.toString();
    }

    private static String toQueryString(Map<String, List<String>> map) {
        return map.entrySet().stream()
                .flatMap(e -> e.getValue().isEmpty()
                        ? Stream.of(encode(e.getKey()))
                        : e.getValue().stream().map(v -> encode(e.getKey()) + "=" + encode(v)))
                .collect(Collectors.joining("&"));
    }

    private static String urlDecode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}