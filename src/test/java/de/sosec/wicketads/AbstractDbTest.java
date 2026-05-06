package de.sosec.wicketads;

import de.sosec.wicketads.db.DatabaseInitializer;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for service-level unit tests that need a live H2 database.
 * The database is initialized once per JVM run; the static guard in
 * DatabaseInitializer ensures idempotency across test classes.
 */
public abstract class AbstractDbTest {

    @BeforeAll
    static void initializeDatabase() {
        DatabaseInitializer.initialize();
    }
}
