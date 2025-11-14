package dev.devrunner.crawler.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * JsoupWalker
 *
 * A lightweight helper for navigating down HTML nodes step by step.
 * Each method retrieves the Nth child element (1-based index) of a given tag type.
 * Only *direct* children are considered (no recursive search).
 */
public final class JsoupWalker {

    private JsoupWalker() {}

    /* ========= Basic Methods ========= */

    /** Parse a raw HTML string into a Jsoup Document.
     *  If baseUri is needed, you can overload this method. */
    public static Document makeDoc(String html) {
        Document doc = Jsoup.parse(Objects.requireNonNullElse(html, ""));
        doc.outputSettings().charset(StandardCharsets.UTF_8);
        return doc;
    }

    /** Returns the Nth (1-based) direct child of `parent` with the given tag.
     *  Returns null if not found. */
    public static Element nth(Element parent, String tag, int n) {
        if (parent == null || tag == null || n <= 0) return null;
        int i = 0;
        for (Element child : parent.children()) {     // iterate only direct children
            if (child.tagName().equalsIgnoreCase(tag)) {
                if (++i == n) return child;
            }
        }
        return null;
    }

    /** Same as nth(), but throws an IllegalArgumentException if the element is not found.
     *  Useful for required structures (e.g., tests or strict parsing). */
    public static Element requireNth(Element parent, String tag, int n) {
        Element el = nth(parent, tag, n);
        if (el == null) {
            String where = parent instanceof Document ? "<Document>" : parent.tagName();
            throw new IllegalArgumentException(
                    "nth child not found: parent=" + where + ", tag=" + tag + ", n=" + n
            );
        }
        return el;
    }

    /* ========= Body (root correction for Document) ========= */

    /** Retrieve the Nth <body> element from a Document (normally only one). */
    public static Element nthBody(Document doc, int n) {
        if (doc == null || n <= 0) return null;
        // Document normally contains only <html> as its first child, so step down one level
        Element html = doc.childNodeSize() > 0 ? doc.child(0) : null; // <html>
        if (html == null) return null;
        return nth(html, "body", n);
    }

    /** Same as nthBody(), but throws if not found. */
    public static Element requireBody(Document doc, int n) {
        Element el = nthBody(doc, n);
        if (el == null) throw new IllegalArgumentException("body not found: n=" + n);
        return el;
    }

    /* ========= Tag Shortcuts ========= */

    public static Element nthDiv(Element parent, int n)     { return nth(parent, "div", n); }
    public static Element nthSection(Element parent, int n) { return nth(parent, "section", n); }
    public static Element nthArticle(Element parent, int n) { return nth(parent, "article", n); }
    public static Element nthHeader(Element parent, int n)  { return nth(parent, "header", n); }
    public static Element nthFooter(Element parent, int n)  { return nth(parent, "footer", n); }
    public static Element nthUl(Element parent, int n)      { return nth(parent, "ul", n); }
    public static Element nthLi(Element parent, int n)      { return nth(parent, "li", n); }
    public static Element nthA(Element parent, int n)       { return nth(parent, "a", n); }
    public static Element nthSpan(Element parent, int n)    { return nth(parent, "span", n); }
    public static Element nthP(Element parent, int n)       { return nth(parent, "p", n); }
    public static Element nthTable(Element parent, int n)   { return nth(parent, "table", n); }
    public static Element nthThead(Element parent, int n)   { return nth(parent, "thead", n); }
    public static Element nthTbody(Element parent, int n)   { return nth(parent, "tbody", n); }
    public static Element nthTr(Element parent, int n)      { return nth(parent, "tr", n); }
    public static Element nthTh(Element parent, int n)      { return nth(parent, "th", n); }
    public static Element nthTd(Element parent, int n)      { return nth(parent, "td", n); }

    /** Retrieve nth heading tag (h1~h6). */
    public static Element nthH(Element parent, int level, int n) {
        if (level < 1 || level > 6) return null;
        return nth(parent, "h" + level, n);
    }

    /* ========= Utility Helpers ========= */

    /** Count how many direct children of a parent have the given tag. */
    public static int childCountOfTag(Element parent, String tag) {
        if (parent == null || tag == null) return 0;
        int c = 0;
        for (Element child : parent.children()) {
            if (child.tagName().equalsIgnoreCase(tag)) c++;
        }
        return c;
    }

    /** Safe text extraction (returns empty string if null). */
    public static String textOrEmpty(Element el) {
        return el == null ? "" : el.text().trim();
    }

    /** Safe attribute extraction (returns empty string if null). */
    public static String attrOrEmpty(Element el, String attr) {
        return el == null ? "" : el.attr(attr).trim();
    }

    /** Safe absolute href extraction (returns empty string if null). */
    public static String absHrefOrEmpty(Element el) {
        return el == null ? "" : el.absUrl("href");
    }
}
