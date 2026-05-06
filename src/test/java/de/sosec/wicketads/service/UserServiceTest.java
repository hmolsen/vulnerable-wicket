package de.sosec.wicketads.service;

import de.sosec.wicketads.AbstractDbTest;
import de.sosec.wicketads.model.User;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest extends AbstractDbTest {

    // ── findByUsername ──────────────────────────────────────────────────────

    @Test
    @Order(1)
    void findByUsername_returnsSeededAdmin() {
        User u = UserService.findByUsername("admin");
        assertNotNull(u);
        assertEquals("admin", u.getUsername());
        assertEquals("admin", u.getRole());
    }

    @Test
    @Order(2)
    void findByUsername_returnsNull_forUnknownUser() {
        assertNull(UserService.findByUsername("nobody_xyz_99"));
    }

    @Test
    @Order(3)
    void findByUsername_returnsSeededAliceWithCorrectFields() {
        User alice = UserService.findByUsername("alice");
        assertNotNull(alice);
        assertEquals("alice123",      alice.getPassword());   // plain-text stored
        assertEquals("user",          alice.getRole());
        assertEquals("Alice Smith",   alice.getFullName());
        assertEquals("alice@example.com", alice.getEmail());
    }

    // ── findById ────────────────────────────────────────────────────────────

    @Test
    @Order(4)
    void findById_returnsCorrectUser() {
        User u = UserService.findById(1);   // admin is always ID 1
        assertNotNull(u);
        assertEquals("admin", u.getUsername());
    }

    @Test
    @Order(5)
    void findById_returnsNull_forMissingId() {
        assertNull(UserService.findById(99999));
    }

    // ── findAll ─────────────────────────────────────────────────────────────

    @Test
    @Order(6)
    void findAll_containsAllSeededUsers() {
        List<User> all = UserService.findAll();
        assertTrue(all.size() >= 3, "Should have at least admin, alice, bob");
        assertTrue(all.stream().anyMatch(u -> "admin".equals(u.getUsername())));
        assertTrue(all.stream().anyMatch(u -> "alice".equals(u.getUsername())));
        assertTrue(all.stream().anyMatch(u -> "bob".equals(u.getUsername())));
    }

    // ── create ──────────────────────────────────────────────────────────────

    @Test
    @Order(7)
    void create_persistsUserAndReturnsId() {
        String uname = "testuser_create_" + System.currentTimeMillis();
        int id = UserService.create(uname, "pass", "user");
        assertTrue(id > 0);
        User found = UserService.findByUsername(uname);
        assertNotNull(found);
        assertEquals(uname, found.getUsername());
        assertEquals("pass", found.getPassword());   // plain-text
        assertEquals("user", found.getRole());
    }

    // ── update ──────────────────────────────────────────────────────────────

    @Test
    @Order(8)
    void update_changesAllFields() {
        String uname = "testuser_update_" + System.currentTimeMillis();
        int id = UserService.create(uname, "oldpass", "user");
        UserService.update(id, uname, "newpass", "user",
                "Full Name", "Street", "1", "12345", "City", "Country", "555", "e@e.com");
        User u = UserService.findById(id);
        assertEquals("newpass", u.getPassword());
        assertEquals("Full Name", u.getFullName());
        assertEquals("City", u.getCity());
    }

    // ── deleteById ──────────────────────────────────────────────────────────

    @Test
    @Order(9)
    void deleteById_removesUserAndCascades() {
        String uname = "testuser_delete_" + System.currentTimeMillis();
        int id = UserService.create(uname, "pass", "user");
        assertNotNull(UserService.findById(id));
        UserService.deleteById(id);
        assertNull(UserService.findById(id));
    }

    // ── Security note ───────────────────────────────────────────────────────

    @Test
    @Order(10)
    void passwords_areStoredAsPlainText_sensitiveDataExposure() {
        User alice = UserService.findByUsername("alice");
        assertNotNull(alice);
        // Passwords are stored and retrievable as plain text – deliberate vulnerability
        assertEquals("alice123", alice.getPassword(),
                "VULN: password must be stored and returned as plain text");
    }
}
