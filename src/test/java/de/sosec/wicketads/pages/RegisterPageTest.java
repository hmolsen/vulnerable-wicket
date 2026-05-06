package de.sosec.wicketads.pages;

import de.sosec.wicketads.AbstractWicketTest;
import de.sosec.wicketads.WicketAdsSession;
import org.apache.wicket.util.tester.FormTester;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterPageTest extends AbstractWicketTest {

    @Test
    void pageRenders() {
        tester.startPage(RegisterPage.class);
        tester.assertRenderedPage(RegisterPage.class);
    }

    @Test
    void successfulRegistration_autoLoginsAndRedirectsHome() {
        tester.startPage(RegisterPage.class);
        FormTester ft = tester.newFormTester("form");
        ft.setValue("username",        "newuser_" + System.currentTimeMillis());
        ft.setValue("password",        "secret");
        ft.setValue("confirmPassword", "secret");
        ft.submit();
        tester.assertRenderedPage(HomePage.class);
        assertTrue(((WicketAdsSession) tester.getSession()).isLoggedIn(),
                "User must be logged in after registration");
    }

    @Test
    void duplicateUsername_showsExplicitError_usernameEnumerationVuln() {
        // VULN USERNAME-ENUMERATION: "Username already taken" confirms the account exists
        tester.startPage(RegisterPage.class);
        FormTester ft = tester.newFormTester("form");
        ft.setValue("username",        "alice");   // existing seed user
        ft.setValue("password",        "pass");
        ft.setValue("confirmPassword", "pass");
        ft.submit();
        tester.assertRenderedPage(RegisterPage.class);
        tester.assertErrorMessages("Username already taken.");
    }

    @Test
    void passwordMismatch_showsError() {
        tester.startPage(RegisterPage.class);
        FormTester ft = tester.newFormTester("form");
        ft.setValue("username",        "mismatch_user_" + System.currentTimeMillis());
        ft.setValue("password",        "pass1");
        ft.setValue("confirmPassword", "pass2");
        ft.submit();
        tester.assertRenderedPage(RegisterPage.class);
        tester.assertErrorMessages("Passwords do not match.");
    }

    @Test
    void emptyUsername_showsError() {
        tester.startPage(RegisterPage.class);
        FormTester ft = tester.newFormTester("form");
        ft.setValue("username",        "");
        ft.setValue("password",        "pass");
        ft.setValue("confirmPassword", "pass");
        ft.submit();
        tester.assertRenderedPage(RegisterPage.class);
        tester.assertErrorMessages("Username is required.");
    }

    @Test
    void emptyPassword_showsError() {
        tester.startPage(RegisterPage.class);
        FormTester ft = tester.newFormTester("form");
        ft.setValue("username",        "someuser_" + System.currentTimeMillis());
        ft.setValue("password",        "");
        ft.setValue("confirmPassword", "");
        ft.submit();
        tester.assertRenderedPage(RegisterPage.class);
        tester.assertErrorMessages("Password is required.");
    }

    @Test
    void feedbackPanel_isPresent() {
        tester.startPage(RegisterPage.class);
        tester.assertComponent("feedback",
                org.apache.wicket.markup.html.panel.FeedbackPanel.class);
    }
}
