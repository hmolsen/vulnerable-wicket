package de.sosec.wicketads.pages;

import de.sosec.wicketads.AbstractWicketTest;
import de.sosec.wicketads.model.Ad;
import de.sosec.wicketads.service.AdService;
import org.apache.wicket.util.tester.FormTester;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostAdPageTest extends AbstractWicketTest {

    @Test
    void anonymousUser_redirectedToLogin() {
        tester.startPage(PostAdPage.class);
        tester.assertRenderedPage(LoginPage.class);
    }

    @Test
    void loggedInUser_seesPostAdPage() {
        loginAsAlice();
        tester.startPage(PostAdPage.class);
        tester.assertRenderedPage(PostAdPage.class);
    }

    @Test
    void successfulSubmit_createsAdAndRedirectsToDetail() {
        loginAsAlice();
        tester.startPage(PostAdPage.class);
        FormTester ft = tester.newFormTester("form");
        ft.setValue("title",       "My Integration Test Ad");
        ft.setValue("description", "Created by PostAdPageTest");
        ft.setValue("price",       "42.00");
        ft.select("category",      0);   // first category = "Electronics"
        ft.submit();
        tester.assertRenderedPage(AdDetailPage.class);
    }

    @Test
    void successfulSubmit_adAppearsInDatabase() {
        loginAsAlice();
        tester.startPage(PostAdPage.class);
        String unique = "Unique Ad " + System.currentTimeMillis();
        FormTester ft = tester.newFormTester("form");
        ft.setValue("title",       unique);
        ft.setValue("description", "desc");
        ft.setValue("price",       "1.00");
        ft.select("category",      0);
        ft.submit();

        List<Ad> recent = AdService.findRecent();
        assertTrue(recent.stream().anyMatch(a -> unique.equals(a.getTitle())),
                "Newly posted ad must appear in the recent list");
    }

    @Test
    void emptyTitle_showsError() {
        loginAsAlice();
        tester.startPage(PostAdPage.class);
        FormTester ft = tester.newFormTester("form");
        ft.setValue("title", "");
        ft.setValue("price", "10.00");
        ft.select("category", 0);
        ft.submit();
        tester.assertRenderedPage(PostAdPage.class);
        tester.assertErrorMessages("Title is required.");
    }

    @Test
    void invalidPrice_showsError() {
        loginAsAlice();
        tester.startPage(PostAdPage.class);
        FormTester ft = tester.newFormTester("form");
        ft.setValue("title", "Valid Title");
        ft.setValue("price", "not-a-number");
        ft.select("category", 0);
        ft.submit();
        tester.assertRenderedPage(PostAdPage.class);
        tester.assertErrorMessages("Price must be a valid number.");
    }

    @Test
    void zeroPrice_isAccepted() {
        loginAsAlice();
        tester.startPage(PostAdPage.class);
        FormTester ft = tester.newFormTester("form");
        ft.setValue("title",       "Free item " + System.currentTimeMillis());
        ft.setValue("description", "free");
        ft.setValue("price",       "");   // blank → BigDecimal.ZERO
        ft.select("category",      4);   // "Services"
        ft.submit();
        tester.assertRenderedPage(AdDetailPage.class);
    }
}
