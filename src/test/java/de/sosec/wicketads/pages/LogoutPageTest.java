package de.sosec.wicketads.pages;

import de.sosec.wicketads.AbstractWicketTest;
import de.sosec.wicketads.WicketAdsSession;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogoutPageTest extends AbstractWicketTest {

    @Test
    void logout_redirectsToHome() {
        loginAsAlice();
        tester.startPage(LogoutPage.class);
        tester.assertRenderedPage(HomePage.class);
    }

    @Test
    void logout_invalidatesSession() {
        loginAsAlice();
        assertTrue(((WicketAdsSession) tester.getSession()).isLoggedIn(),
                "Must be logged in before logout");
        tester.startPage(LogoutPage.class);
        assertFalse(((WicketAdsSession) tester.getSession()).isLoggedIn(),
                "Must be logged out after visiting LogoutPage");
    }

    @Test
    void logout_clearsCurrentUser() {
        loginAsAlice();
        assertNotNull(((WicketAdsSession) tester.getSession()).getCurrentUser());
        tester.startPage(LogoutPage.class);
        assertNull(((WicketAdsSession) tester.getSession()).getCurrentUser(),
                "Current user must be null after logout");
    }

    @Test
    void anonymousVisit_alsRedirectsToHome() {
        // LogoutPage must be safe to visit even when not logged in
        tester.startPage(LogoutPage.class);
        tester.assertRenderedPage(HomePage.class);
    }
}
