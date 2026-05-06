package de.sosec.wicketads.pages;

import de.sosec.wicketads.AbstractWicketTest;
import org.apache.wicket.util.tester.FormTester;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HomePageTest extends AbstractWicketTest {

    @Test
    void rendersForAnonymousUser_showsGuestNav() {
        tester.startPage(HomePage.class);
        tester.assertRenderedPage(HomePage.class);
        tester.assertVisible("guestNav");
        tester.assertInvisible("userNav");
        tester.assertInvisible("adminNav");
    }

    @Test
    void rendersForLoggedInUser_showsUserNav() {
        loginAsAlice();
        tester.startPage(HomePage.class);
        tester.assertRenderedPage(HomePage.class);
        tester.assertInvisible("guestNav");
        tester.assertVisible("userNav");
        tester.assertInvisible("adminNav");
    }

    @Test
    void rendersForAdmin_showsAdminNav() {
        loginAsAdmin();
        tester.startPage(HomePage.class);
        tester.assertRenderedPage(HomePage.class);
        tester.assertVisible("adminNav");
        tester.assertVisible("userNav");
    }

    @Test
    void seededAdsAppearInListing() {
        tester.startPage(HomePage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("Vintage Guitar"),  "Seed ad must appear");
        assertTrue(html.contains("MacBook Pro"),     "Seed ad must appear");
    }

    @Test
    void adList_displaysOwnerUsername() {
        tester.startPage(HomePage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("alice") || html.contains("bob"),
                "Ad listings must show owner usernames");
    }

    @Test
    void searchForm_isPresent() {
        tester.startPage(HomePage.class);
        tester.assertComponent("searchForm", org.apache.wicket.markup.html.form.Form.class);
    }

    @Test
    void search_byKeyword_filtersResults() {
        tester.startPage(HomePage.class);
        FormTester ft = tester.newFormTester("searchForm");
        ft.setValue("keyword", "Guitar");
        ft.submit();
        tester.assertRenderedPage(HomePage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("Guitar"), "Search must return Guitar ad");
    }

    @Test
    void search_unknownKeyword_showsNoAdsMessage() {
        tester.startPage(HomePage.class);
        FormTester ft = tester.newFormTester("searchForm");
        ft.setValue("keyword", "XYZZY_NONEXISTENT_AD_12345");
        ft.submit();
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("No ads found"), "Unknown keyword must show empty message");
    }

    @Test
    void noAds_component_hiddenWhenAdsExist() {
        tester.startPage(HomePage.class);
        tester.assertInvisible("noAds");
    }
}
