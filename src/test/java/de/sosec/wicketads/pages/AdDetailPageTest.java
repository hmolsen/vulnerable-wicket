package de.sosec.wicketads.pages;

import de.sosec.wicketads.AbstractWicketTest;
import de.sosec.wicketads.service.AdService;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AdDetailPageTest extends AbstractWicketTest {

    @Test
    void pageRenders_withValidAdId() {
        PageParameters pp = new PageParameters();
        pp.add("id", 1);   // "Vintage Guitar" seed ad
        tester.startPage(AdDetailPage.class, pp);
        tester.assertRenderedPage(AdDetailPage.class);
    }

    @Test
    void adFields_areDisplayed() {
        PageParameters pp = new PageParameters();
        pp.add("id", 1);
        tester.startPage(AdDetailPage.class, pp);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("Vintage Guitar"),  "Title must appear");
        assertTrue(html.contains("1968"),            "Description content must appear");
        assertTrue(html.contains("alice"),           "Owner username must appear");
        assertTrue(html.contains("Electronics"),     "Category must appear");
    }

    @Test
    void xss_adDescription_notEscaped() {
        // VULN XSS: description rendered with setEscapeModelStrings(false)
        int adId = AdService.create(2, "XSS Test Ad",
                "<script>alert('xss')</script>",
                BigDecimal.ONE, "Other");
        PageParameters pp = new PageParameters();
        pp.add("id", adId);
        tester.startPage(AdDetailPage.class, pp);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("<script>alert('xss')</script>"),
                "VULN XSS: script tag in description must NOT be HTML-escaped");
        assertFalse(html.contains("&lt;script&gt;"),
                "VULN XSS: the &lt; entity must not appear (content is rendered raw)");
    }

    @Test
    void xss_htmlInDescription_executesInBrowser() {
        // VULN XSS: injected img with onerror fires JavaScript
        int adId = AdService.create(2, "XSS Img Ad",
                "<img src=x onerror=\"alert('pwned')\"/>",
                BigDecimal.TEN, "Other");
        PageParameters pp = new PageParameters();
        pp.add("id", adId);
        tester.startPage(AdDetailPage.class, pp);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("onerror="),
                "VULN XSS: img onerror handler must appear unescaped in the page");
    }

    @Test
    void contactButton_hiddenForAnonymousUser() {
        PageParameters pp = new PageParameters();
        pp.add("id", 1);
        tester.startPage(AdDetailPage.class, pp);
        tester.assertInvisible("contactContainer");
    }

    @Test
    void contactButton_hiddenForOwner() {
        // Alice owns ad 1 – she should not see "Contact Seller"
        loginAsAlice();
        PageParameters pp = new PageParameters();
        pp.add("id", 1);
        tester.startPage(AdDetailPage.class, pp);
        tester.assertInvisible("contactContainer");
    }

    @Test
    void contactButton_visibleForLoggedInNonOwner() {
        // Bob does not own ad 1 (alice's guitar) – he should see the button
        loginAsBob();
        PageParameters pp = new PageParameters();
        pp.add("id", 1);
        tester.startPage(AdDetailPage.class, pp);
        tester.assertVisible("contactContainer");
    }

    @Test
    void unknownAdId_rendersPageWithoutError() {
        PageParameters pp = new PageParameters();
        pp.add("id", 99999);
        tester.startPage(AdDetailPage.class, pp);
        tester.assertRenderedPage(AdDetailPage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("Ad not found"), "Unknown ID must show 'Ad not found'");
    }
}
