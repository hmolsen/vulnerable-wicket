package de.sosec.wicketads.service;

import de.sosec.wicketads.AbstractDbTest;
import de.sosec.wicketads.model.Ad;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdServiceTest extends AbstractDbTest {

    // ── findRecent ──────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void findRecent_returnsSeededAds() {
        List<Ad> ads = AdService.findRecent();
        assertFalse(ads.isEmpty());
        assertTrue(ads.stream().anyMatch(a -> a.getTitle().contains("Guitar")));
        assertTrue(ads.stream().anyMatch(a -> a.getTitle().contains("MacBook")));
    }

    @Test
    @Order(2)
    void findRecent_includesOwnerUsername() {
        List<Ad> ads = AdService.findRecent();
        assertTrue(ads.stream().allMatch(a -> a.getOwnerUsername() != null),
                "Each ad must include the owner's username");
    }

    // ── findById ────────────────────────────────────────────────────────────

    @Test
    @Order(3)
    void findById_returnsCorrectAd() {
        Ad ad = AdService.findById(1);   // "Vintage Guitar" seeded with id=1
        assertNotNull(ad);
        assertEquals("Vintage Guitar", ad.getTitle());
        assertEquals("alice", ad.getOwnerUsername());
    }

    @Test
    @Order(4)
    void findById_returnsNull_forMissingId() {
        assertNull(AdService.findById(99999));
    }

    // ── create ──────────────────────────────────────────────────────────────

    @Test
    @Order(5)
    void create_persistsAdAndReturnsId() {
        int id = AdService.create(2, "Test Ad", "Desc", new BigDecimal("9.99"), "Other");
        assertTrue(id > 0);
        Ad ad = AdService.findById(id);
        assertNotNull(ad);
        assertEquals("Test Ad", ad.getTitle());
        assertEquals(0, new BigDecimal("9.99").compareTo(ad.getPrice()));
        assertEquals("alice", ad.getOwnerUsername());
    }

    // ── search – normal behaviour ───────────────────────────────────────────

    @Test
    @Order(6)
    void search_byKeyword_returnsMatchingAds() {
        List<Ad> results = AdService.search("Guitar", "");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(a ->
                a.getTitle().contains("Guitar") || (a.getDescription() != null && a.getDescription().contains("Guitar"))));
    }

    @Test
    @Order(7)
    void search_byCategory_returnsOnlyThatCategory() {
        List<Ad> results = AdService.search("", "Electronics");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(a -> "Electronics".equals(a.getCategory())));
    }

    @Test
    @Order(8)
    void search_emptyInputs_returnAllRecentAds() {
        List<Ad> viaSearch = AdService.search("", "");
        List<Ad> viaRecent = AdService.findRecent();
        assertEquals(viaRecent.size(), viaSearch.size());
    }

    // ── SQL INJECTION vulnerability tests ───────────────────────────────────

    @Test
    @Order(9)
    void search_sqlInjection_tautologyReturnsAllAds() {
        // The search builds SQL via string concatenation. Injecting a tautology
        // causes the WHERE clause to always be TRUE, returning every ad.
        String payload = "' OR '1'='1' OR title LIKE '";
        List<Ad> injected = AdService.search(payload, "");
        List<Ad> all     = AdService.findRecent();

        assertEquals(all.size(), injected.size(),
                "VULN SQL-INJECTION: tautology payload must return all ads");
    }

    @Test
    @Order(10)
    void search_sqlInjection_unionExtractsUsers() {
        // A UNION-based injection extracts rows from the users table.
        //
        // The vulnerable template is:
        //   WHERE 1=1 AND (a.title LIKE '%<keyword>%' OR a.description LIKE '%<keyword>%')
        //
        // The payload closes the AND-clause parenthesis early, then appends a UNION
        // that selects password data from users, and uses -- to comment out the
        // trailing fragment that the template appends after the keyword.
        //
        // Ads table has 8 columns: id, owner_id, title, description, price, category,
        // created_at, owner_username → we supply 8 matching values from users.
        String payload = "%') UNION SELECT id,id,username,password,0.0,role,CURRENT_TIMESTAMP,username FROM users --";

        List<Ad> results = assertDoesNotThrow(() -> AdService.search(payload, ""),
                "VULN SQL-INJECTION: UNION injection must not be blocked by the app");

        // The result set combines ads rows (all, because LIKE '%%' matches everything)
        // and user rows (exposed via UNION). Total rows must exceed seed ad count.
        List<Ad> allAds = AdService.findRecent();
        assertTrue(results.size() > allAds.size(),
                "VULN SQL-INJECTION: UNION injection must return extra rows from the users table; "
                + "got " + results.size() + " rows, expected more than " + allAds.size());
    }

    // ── findByOwner ─────────────────────────────────────────────────────────

    @Test
    @Order(11)
    void findByOwner_returnsOnlyOwnersAds() {
        List<Ad> aliceAds = AdService.findByOwner(2);  // alice = id 2
        assertFalse(aliceAds.isEmpty());
        assertTrue(aliceAds.stream().allMatch(a -> a.getOwnerId() == 2));
    }
}
