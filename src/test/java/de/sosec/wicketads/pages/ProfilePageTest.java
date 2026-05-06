package de.sosec.wicketads.pages;

import de.sosec.wicketads.AbstractWicketTest;
import de.sosec.wicketads.model.User;
import de.sosec.wicketads.service.UserService;
import org.apache.wicket.util.tester.FormTester;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfilePageTest extends AbstractWicketTest {

    @Test
    void anonymousUser_redirectedToLogin() {
        tester.startPage(ProfilePage.class);
        tester.assertRenderedPage(LoginPage.class);
    }

    @Test
    void loggedInUser_seesProfilePage() {
        loginAsAlice();
        tester.startPage(ProfilePage.class);
        tester.assertRenderedPage(ProfilePage.class);
    }

    @Test
    void profileForm_isPopulatedWithCurrentUserData() {
        loginAsAlice();
        tester.startPage(ProfilePage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("alice"), "Username field must be pre-filled");
        assertTrue(html.contains("alice@example.com"), "Email field must be pre-filled");
    }

    @Test
    void successfulSave_updatesUserInDatabase() {
        // Create a throwaway user so we don't disturb alice's seed data
        String uname = "profiletest_" + System.currentTimeMillis();
        UserService.create(uname, "pass", "user");

        loginAs(uname, "pass");
        tester.startPage(ProfilePage.class);
        FormTester ft = tester.newFormTester("form");
        ft.setValue("username",    uname);
        ft.setValue("password",    "pass");
        ft.setValue("fullName",    "Updated Name");
        ft.setValue("street",      "New Street");
        ft.setValue("houseNumber", "99");
        ft.setValue("zipCode",     "11111");
        ft.setValue("city",        "New City");
        ft.setValue("country",     "DE");
        ft.setValue("phone",       "123");
        ft.setValue("email",       "new@example.com");
        ft.submit();

        tester.assertRenderedPage(ProfilePage.class);
        User updated = UserService.findByUsername(uname);
        assertEquals("Updated Name", updated.getFullName());
        assertEquals("New City",     updated.getCity());
        assertEquals("new@example.com", updated.getEmail());
    }

    @Test
    void idor_hiddenUserIdField_canUpdateAnotherUsersProfile() {
        // VULN IDOR / BROKEN-ACCESS-CONTROL:
        // ProfilePage renders a hidden "userId" field with the logged-in user's ID.
        // An attacker can tamper this field in the POST to update a different user.
        //
        // We simulate the tampering by directly calling UserService.update() with
        // bob's ID while acting as alice (no ownership check in the service call from
        // the form's onSubmit, which uses the hidden field value verbatim).

        loginAsAlice();
        tester.startPage(ProfilePage.class);
        String html = tester.getLastResponseAsString();

        // The hidden field is present in the rendered HTML
        assertTrue(html.contains("type=\"hidden\""),
                "VULN IDOR: hidden userId field must be present in the form HTML");

        // Simulate a tampered POST: alice's session but targeting bob's ID (3)
        // In a real attack, the browser DevTools would change the hidden field value.
        // Here we verify the service call itself accepts any ID with no auth check.
        User bobBefore = UserService.findById(3);
        String tamperedEmail = "tampered_by_alice@evil.com";
        UserService.update(3,                         // bob's ID, not alice's
                bobBefore.getUsername(), bobBefore.getPassword(), bobBefore.getRole(),
                "Tampered by Alice",
                bobBefore.getStreet(), bobBefore.getHouseNumber(),
                bobBefore.getZipCode(), bobBefore.getCity(), bobBefore.getCountry(),
                bobBefore.getPhone(), tamperedEmail);

        User bobAfter = UserService.findById(3);
        assertEquals(tamperedEmail, bobAfter.getEmail(),
                "VULN IDOR: alice was able to overwrite bob's profile via the hidden field");

        // Restore bob's email so other tests are not affected
        UserService.update(3, bobBefore.getUsername(), bobBefore.getPassword(),
                bobBefore.getRole(), bobBefore.getFullName(),
                bobBefore.getStreet(), bobBefore.getHouseNumber(),
                bobBefore.getZipCode(), bobBefore.getCity(), bobBefore.getCountry(),
                bobBefore.getPhone(), bobBefore.getEmail());
    }

    @Test
    void hiddenUserIdField_isPresentInHtml() {
        loginAsAlice();
        tester.startPage(ProfilePage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("type=\"hidden\""),
                "Hidden userId input must appear in the page source");
    }
}
