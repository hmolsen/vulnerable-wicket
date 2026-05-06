package de.sosec.wicketads.pages;

import de.sosec.wicketads.AbstractWicketTest;
import de.sosec.wicketads.WicketAdsSession;
import org.apache.wicket.util.tester.FormTester;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginPageTest extends AbstractWicketTest {

    @Test
    void pageRenders() {
        tester.startPage(LoginPage.class);
        tester.assertRenderedPage(LoginPage.class);
    }

    @Test
    void validCredentials_redirectsToHome() {
        tester.startPage(LoginPage.class);
        FormTester ft = tester.newFormTester("form");
        ft.setValue("username", "alice");
        ft.setValue("password", "alice123");
        ft.submit();
        tester.assertRenderedPage(HomePage.class);
    }

    @Test
    void validCredentials_setsSessionUser() {
        tester.startPage(LoginPage.class);
        FormTester ft = tester.newFormTester("form");
        ft.setValue("username", "alice");
        ft.setValue("password", "alice123");
        ft.submit();
        WicketAdsSession session = (WicketAdsSession) tester.getSession();
        assertTrue(session.isLoggedIn());
        assertEquals("alice", session.getCurrentUser().getUsername());
    }

    @Test
    void unknownUsername_showsUserNotFound_enumerationVuln() {
        // VULN USERNAME-ENUMERATION: distinct message reveals the username does not exist
        tester.startPage(LoginPage.class);
        FormTester ft = tester.newFormTester("form");
        ft.setValue("username", "nobody_xyz_99999");
        ft.setValue("password", "anything");
        ft.submit();
        tester.assertRenderedPage(LoginPage.class);
        tester.assertErrorMessages("User not found.");
    }

    @Test
    void wrongPassword_showsWrongPassword_enumerationVuln() {
        // VULN USERNAME-ENUMERATION: distinct message reveals the username DOES exist
        tester.startPage(LoginPage.class);
        FormTester ft = tester.newFormTester("form");
        ft.setValue("username", "alice");
        ft.setValue("password", "wrongpassword");
        ft.submit();
        tester.assertRenderedPage(LoginPage.class);
        tester.assertErrorMessages("Wrong password.");
    }

    @Test
    void twoDistinctErrorMessages_confirmEnumerationVulnerability() {
        // An attacker can distinguish "no such user" from "wrong password",
        // allowing enumeration of valid usernames.
        tester.startPage(LoginPage.class);
        FormTester ft1 = tester.newFormTester("form");
        ft1.setValue("username", "definitely_not_a_user_" + System.currentTimeMillis());
        ft1.setValue("password", "x");
        ft1.submit();
        String resp1 = tester.getLastResponseAsString();
        assertTrue(resp1.contains("User not found"),
                "Non-existent user must say 'User not found'");

        tester = new org.apache.wicket.util.tester.WicketTester(new de.sosec.wicketads.WicketAdsApplication());

        tester.startPage(LoginPage.class);
        FormTester ft2 = tester.newFormTester("form");
        ft2.setValue("username", "alice");
        ft2.setValue("password", "wrong");
        ft2.submit();
        String resp2 = tester.getLastResponseAsString();
        assertTrue(resp2.contains("Wrong password"),
                "Existing user with wrong password must say 'Wrong password'");

        assertNotEquals(resp1.contains("User not found"), resp2.contains("User not found"),
                "VULN USERNAME-ENUMERATION: responses differ, confirming account existence");
    }

    @Test
    void adminCanLogin() {
        tester.startPage(LoginPage.class);
        FormTester ft = tester.newFormTester("form");
        ft.setValue("username", "admin");
        ft.setValue("password", "admin");
        ft.submit();
        tester.assertRenderedPage(HomePage.class);
        assertTrue(((WicketAdsSession) tester.getSession()).isAdmin());
    }
}
