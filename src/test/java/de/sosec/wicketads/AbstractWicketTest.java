package de.sosec.wicketads;

import de.sosec.wicketads.db.DatabaseInitializer;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for WicketTester-based integration tests.
 * Each test gets a fresh WicketTester (fresh session, fresh request cycle)
 * but shares the same in-memory H2 database (seeded once).
 */
public abstract class AbstractWicketTest {

    protected WicketTester tester;

    @BeforeAll
    static void initializeDatabase() {
        // Ensures DB is ready even before the first WicketAdsApplication.init() runs
        DatabaseInitializer.initialize();
    }

    @BeforeEach
    void setUpWicket() {
        tester = new WicketTester(new WicketAdsApplication());
    }

    @AfterEach
    void tearDownWicket() {
        tester.destroy();
    }

    /** Authenticate the WicketTester session directly (no form round-trip). */
    protected void loginAs(String username, String password) {
        WicketAdsSession session = (WicketAdsSession) tester.getSession();
        session.signIn(username, password);
    }

    protected void loginAsAdmin()  { loginAs("admin", "admin"); }
    protected void loginAsAlice()  { loginAs("alice", "alice123"); }
    protected void loginAsBob()    { loginAs("bob",   "bob456"); }
}
