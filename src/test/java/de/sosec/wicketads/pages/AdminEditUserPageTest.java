package de.sosec.wicketads.pages;

import de.sosec.wicketads.AbstractWicketTest;
import de.sosec.wicketads.model.User;
import de.sosec.wicketads.service.UserService;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminEditUserPageTest extends AbstractWicketTest {

    @Test
    void anonymousUser_redirectedToHome() {
        PageParameters pp = new PageParameters();
        pp.add("userId", 2);
        tester.startPage(AdminEditUserPage.class, pp);
        tester.assertRenderedPage(HomePage.class);
    }

    @Test
    void regularUser_redirectedToHome() {
        loginAsAlice();
        PageParameters pp = new PageParameters();
        pp.add("userId", 2);
        tester.startPage(AdminEditUserPage.class, pp);
        tester.assertRenderedPage(HomePage.class);
    }

    @Test
    void adminUser_seesEditPage() {
        loginAsAdmin();
        PageParameters pp = new PageParameters();
        pp.add("userId", 2);   // alice
        tester.startPage(AdminEditUserPage.class, pp);
        tester.assertRenderedPage(AdminEditUserPage.class);
    }

    @Test
    void form_isPrePopulatedWithTargetUser() {
        loginAsAdmin();
        PageParameters pp = new PageParameters();
        pp.add("userId", 2);   // alice
        tester.startPage(AdminEditUserPage.class, pp);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("alice"),         "Username must be pre-filled");
        assertTrue(html.contains("alice@example.com"), "Email must be pre-filled");
    }

    @Test
    void save_updatesUserInDatabase() {
        String uname = "adminedit_test_" + System.currentTimeMillis();
        int id = UserService.create(uname, "pass", "user");

        loginAsAdmin();
        PageParameters pp = new PageParameters();
        pp.add("userId", id);
        tester.startPage(AdminEditUserPage.class, pp);

        FormTester ft = tester.newFormTester("form");
        ft.setValue("username",    uname);
        ft.setValue("password",    "newpass");
        ft.select("role",          0);   // "user"
        ft.setValue("fullName",    "Admin Updated");
        ft.setValue("street",      "Admin St");
        ft.setValue("houseNumber", "1");
        ft.setValue("zipCode",     "00000");
        ft.setValue("city",        "AdminCity");
        ft.setValue("country",     "DE");
        ft.setValue("phone",       "000");
        ft.setValue("email",       "admin_updated@test.com");
        ft.submit();

        tester.assertRenderedPage(AdminUserListPage.class);
        User updated = UserService.findById(id);
        assertEquals("newpass",              updated.getPassword());
        assertEquals("Admin Updated",        updated.getFullName());
        assertEquals("admin_updated@test.com", updated.getEmail());
    }

    @Test
    void idor_adminCanEditAnyUserViaUrlParam() {
        // VULN IDOR: the userId comes from the URL parameter with no additional
        // validation beyond the admin role check. A different admin (or a session-
        // fixation attacker) could swap the userId to target any account.
        loginAsAdmin();

        // Access alice's edit page directly via URL parameter
        PageParameters pp = new PageParameters();
        pp.add("userId", 2);   // alice – admin is editing a different user
        tester.startPage(AdminEditUserPage.class, pp);
        tester.assertRenderedPage(AdminEditUserPage.class);
        String html = tester.getLastResponseAsString();
        // alice's data is loaded purely from the URL parameter
        assertTrue(html.contains("alice"),
                "VULN IDOR: alice's data is loaded from the URL userId parameter, not from the admin's own ID");
    }

    @Test
    void adminCanPromoteUserToAdmin_viaRoleDropdown() {
        String uname = "promote_test_" + System.currentTimeMillis();
        int id = UserService.create(uname, "pass", "user");
        assertEquals("user", UserService.findById(id).getRole());

        loginAsAdmin();
        PageParameters pp = new PageParameters();
        pp.add("userId", id);
        tester.startPage(AdminEditUserPage.class, pp);

        FormTester ft = tester.newFormTester("form");
        ft.setValue("username",    uname);
        ft.setValue("password",    "pass");
        ft.select("role",          1);   // "admin"
        ft.setValue("fullName",    "");
        ft.setValue("street",      "");
        ft.setValue("houseNumber", "");
        ft.setValue("zipCode",     "");
        ft.setValue("city",        "");
        ft.setValue("country",     "");
        ft.setValue("phone",       "");
        ft.setValue("email",       "");
        ft.submit();

        assertEquals("admin", UserService.findById(id).getRole(),
                "Admin must be able to promote a user to admin via the role dropdown");
    }

    @Test
    void unknownUserId_showsNotFoundMessage() {
        loginAsAdmin();
        PageParameters pp = new PageParameters();
        pp.add("userId", 99999);
        tester.startPage(AdminEditUserPage.class, pp);
        tester.assertRenderedPage(AdminEditUserPage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("User not found"), "Unknown userId must show 'User not found'");
    }
}
