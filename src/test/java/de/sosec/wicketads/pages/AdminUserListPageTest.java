package de.sosec.wicketads.pages;

import de.sosec.wicketads.AbstractWicketTest;
import de.sosec.wicketads.model.User;
import de.sosec.wicketads.service.UserService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminUserListPageTest extends AbstractWicketTest {

    @Test
    void anonymousUser_redirectedToHome() {
        tester.startPage(AdminUserListPage.class);
        tester.assertRenderedPage(HomePage.class);
    }

    @Test
    void regularUser_redirectedToHome() {
        loginAsAlice();
        tester.startPage(AdminUserListPage.class);
        tester.assertRenderedPage(HomePage.class);
    }

    @Test
    void adminUser_seesAdminPage() {
        loginAsAdmin();
        tester.startPage(AdminUserListPage.class);
        tester.assertRenderedPage(AdminUserListPage.class);
    }

    @Test
    void adminPage_showsAllSeededUsers() {
        loginAsAdmin();
        tester.startPage(AdminUserListPage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("admin"), "Admin row must appear");
        assertTrue(html.contains("alice"), "Alice row must appear");
        assertTrue(html.contains("bob"),   "Bob row must appear");
    }

    @Test
    void sensitiveDataExposure_plainTextPasswordsVisible() {
        // VULN SENSITIVE-DATA-EXPOSURE: the admin page renders passwords in plain text
        loginAsAdmin();
        tester.startPage(AdminUserListPage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("alice123"),
                "VULN: alice's plain-text password must appear in the admin table");
        assertTrue(html.contains("bob456"),
                "VULN: bob's plain-text password must appear in the admin table");
        assertTrue(html.contains("admin"),
                "VULN: admin's plain-text password must appear in the admin table");
    }

    @Test
    void deleteUser_removesUserFromDatabase() {
        // Use a throwaway user so we don't break seed data for other tests
        String uname = "admin_delete_test_" + System.currentTimeMillis();
        int id = UserService.create(uname, "pass", "user");
        assertNotNull(UserService.findById(id), "User must exist before delete");

        loginAsAdmin();
        tester.startPage(AdminUserListPage.class);
        // Locate and click the delete link for our throwaway user.
        // The ListView renders rows in the order returned by UserService.findAll().
        List<User> all = UserService.findAll();
        int rowIndex = -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == id) { rowIndex = i; break; }
        }
        assertTrue(rowIndex >= 0, "Throwaway user must be in the list");
        tester.clickLink("userList:" + rowIndex + ":deleteLink");

        assertNull(UserService.findById(id),
                "User must be gone from the database after admin delete");
    }

    @Test
    void editLink_navigatesToAdminEditPage() {
        loginAsAdmin();
        tester.startPage(AdminUserListPage.class);
        // Click the edit link for the first row (admin, index 0)
        tester.clickLink("userList:0:editLink");
        tester.assertRenderedPage(AdminEditUserPage.class);
    }
}
