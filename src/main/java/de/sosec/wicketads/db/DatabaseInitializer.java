package de.sosec.wicketads.db;

import org.h2.jdbcx.JdbcConnectionPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private static DataSource dataSource;
    private static volatile boolean initialized = false;

    public static synchronized void initialize() {
        if (initialized) return;
        initialized = true;
        dataSource = JdbcConnectionPool.create(
                "jdbc:h2:mem:wicketads;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", "sa", "");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(100) NOT NULL,
                    password VARCHAR(100) NOT NULL,
                    role VARCHAR(20) NOT NULL DEFAULT 'user',
                    full_name VARCHAR(200),
                    street VARCHAR(200),
                    house_number VARCHAR(20),
                    zip_code VARCHAR(20),
                    city VARCHAR(100),
                    country VARCHAR(100),
                    phone VARCHAR(50),
                    email VARCHAR(200)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ads (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    owner_id INTEGER NOT NULL,
                    title VARCHAR(200) NOT NULL,
                    description VARCHAR(4000),
                    price DECIMAL(10,2),
                    category VARCHAR(100),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (owner_id) REFERENCES users(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS messages (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    sender_id INTEGER NOT NULL,
                    recipient_id INTEGER NOT NULL,
                    ad_id INTEGER,
                    body VARCHAR(4000),
                    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (sender_id) REFERENCES users(id),
                    FOREIGN KEY (recipient_id) REFERENCES users(id)
                )
            """);

            // Seed: admin user
            stmt.execute("""
                MERGE INTO users (id, username, password, role, full_name, email)
                KEY(id)
                VALUES (1, 'admin', 'admin', 'admin', 'Administrator', 'admin@wicketads.local')
            """);

            // Seed: regular users
            stmt.execute("""
                MERGE INTO users (id, username, password, role, full_name, street, house_number, zip_code, city, country, phone, email)
                KEY(id)
                VALUES (2, 'alice', 'alice123', 'user', 'Alice Smith', 'Main Street', '42', '12345', 'Springfield', 'USA', '+1-555-0100', 'alice@example.com')
            """);

            stmt.execute("""
                MERGE INTO users (id, username, password, role, full_name, street, house_number, zip_code, city, country, phone, email)
                KEY(id)
                VALUES (3, 'bob', 'bob456', 'user', 'Bob Jones', 'Oak Avenue', '7', '67890', 'Shelbyville', 'USA', '+1-555-0200', 'bob@example.com')
            """);

            // Seed: 10 sample ads
            stmt.execute("""
                MERGE INTO ads (id, owner_id, title, description, price, category, created_at)
                KEY(id)
                VALUES (1, 2, 'Vintage Guitar',
                'Up for sale is my beloved 1968 Fender Stratocaster in sunburst finish — a true piece of rock history that I have owned for over twenty years. The guitar is in remarkable condition for its age, with only light play wear on the fret board and a small ding on the lower bout that has been professionally repaired and is barely visible.

The neck feels incredible: a slim C-profile with a rosewood board that has developed a beautiful patina over the decades. All original electronics are intact, including the three single-coil pickups that produce that classic glassy, bell-like Strat tone. The tremolo arm is original and the tuning machines hold pitch rock-solidly even after extended whammy use.

Included in the sale: original brown hardshell case (latches and hinges in perfect working order), a set of spare strings, the original hang tag, and a certificate of provenance from the guitar shop where I purchased it in 2004.

I am selling reluctantly — a recent move has left me without a practice space and I would rather see this instrument played than gather dust. Price is firm; no trades. Serious buyers only please, local pickup preferred in Springfield though I will ship at buyer''s expense with full insurance.',
                1200.00, 'Electronics', CURRENT_TIMESTAMP)
            """);

            stmt.execute("""
                MERGE INTO ads (id, owner_id, title, description, price, category, created_at)
                KEY(id)
                VALUES (2, 2, 'Road Bike - Trek FX 7.2',
                'Selling my Trek FX 7.2 hybrid road bike — the perfect commuter or recreational ride. I bought it new three years ago and have put roughly 800 miles on it, mostly weekend rides along the riverfront trail. It has been serviced annually at a local bike shop; last tune-up was six weeks ago.

Specs: 700c wheels, 21-speed Shimano drivetrain, lightweight aluminum frame, Bontrager saddle, and ergonomic swept-back handlebars. The gears shift crisply through all 21 speeds and the brakes are responsive and recently re-cabled. The tires are Bontrager H2 Hard-Case and still have significant tread remaining.

What comes with the bike: original pedals, front and rear reflectors, a quality Kryptonite U-lock (worth $60 alone), a frame-mounted pump, and a small saddle bag with a spare tube and tyre levers.

Reason for selling: I recently switched to a cargo bike for my daily commute and this one is just sitting in the hallway. It deserves an owner who will actually ride it. Frame size is 18'' (suits riders roughly 5''8'' to 6''0''). Cash on pickup only; I can meet anywhere in the metro area.',
                350.00, 'Vehicles', CURRENT_TIMESTAMP)
            """);

            stmt.execute("""
                MERGE INTO ads (id, owner_id, title, description, price, category, created_at)
                KEY(id)
                VALUES (3, 3, 'MacBook Pro 2021 - M1 Pro',
                'Selling my MacBook Pro 14-inch (2021) with the Apple M1 Pro chip. I am upgrading to a newer model for video editing work and this machine has served me flawlessly — I just need more memory headroom for 8K timelines.

Configuration: M1 Pro 10-core CPU / 16-core GPU, 16 GB unified memory, 512 GB SSD. macOS Sonoma installed and up to date. Battery cycle count is only 87 — exceptional for a machine of this age; Apple rates these batteries for 1,000 cycles before any noticeable degradation. All ports work perfectly: three Thunderbolt 4, one HDMI 2.0, SD card slot, and MagSafe 3.

Condition: 9.5/10. There is one hairline scratch on the lid, visible only when light catches it at a certain angle. I have kept it in a sleeve since day one and there are zero marks on the keyboard or screen. Display is immaculate — no dead pixels, no backlight bleed, no burn-in.

Included: original 96W USB-C power adapter and cable (both in perfect condition), original box and documentation. I will do a full factory reset and clean install before handover so the buyer starts completely fresh.',
                1400.00, 'Electronics', CURRENT_TIMESTAMP)
            """);

            stmt.execute("""
                MERGE INTO ads (id, owner_id, title, description, price, category, created_at)
                KEY(id)
                VALUES (4, 3, 'Freelance Full-Stack Development',
                'Experienced full-stack software engineer available for freelance engagements — short sprints, long-term retainers, or one-off consulting calls. I have twelve years of professional experience across fintech, e-commerce, and SaaS start-ups.

Primary stack: Java 17+ (Spring Boot, Quarkus, Micronaut), React 18, TypeScript, PostgreSQL/MySQL, Docker, Kubernetes, AWS. Secondary skills: Python (FastAPI, Django), Go, and mobile development with React Native. I am equally comfortable designing system architecture, reviewing code, or getting hands-on in a legacy codebase.

Recent projects include: migrating a monolith payments platform to microservices (reduced P99 latency by 68%), building a real-time inventory sync system for a national retailer, and delivering a HIPAA-compliant patient portal from scratch in four months.

Rate: $75/hour for development work; $120/hour for architecture consulting. Minimum engagement is 10 hours. I work in the Central European time zone but am flexible for US Eastern or Pacific morning calls. NDA available. References and code samples on request. Contact me through this listing to schedule a free 30-minute discovery call.',
                75.00, 'Services', CURRENT_TIMESTAMP)
            """);

            stmt.execute("""
                MERGE INTO ads (id, owner_id, title, description, price, category, created_at)
                KEY(id)
                VALUES (5, 2, 'Corner Sofa - L-Shaped, Dark Grey',
                'Large L-shaped corner sofa in dark charcoal grey fabric, offering seating for five to six adults comfortably. Purchased eighteen months ago from a Scandinavian furniture brand for €1,400; selling due to a house move where it will not fit the new living room layout.

The sofa is in excellent used condition: no tears, stains, or significant wear. The foam cushions still feel supportive and bouncy — no sagging. The fabric is a tightly woven performance weave that repels light spills and pet hair. We have one small cat and kept the sofa covered with throws; I cannot see any claw marks.

Dimensions: 290 cm wide (long side) × 190 cm wide (short side) × 85 cm tall × 65 cm seat depth. The chaise section can be configured left or right (the connection bracket is symmetrical). Legs are solid beech wood in a natural finish.

Delivery is negotiable within 30 km for a small fee — I have a van and can help carry it upstairs with one other person. Buyer must measure their doorways; the sofa disassembles into two sections, each around 145 cm. No time wasters please — priced to sell quickly.',
                480.00, 'Other', CURRENT_TIMESTAMP)
            """);

            stmt.execute("""
                MERGE INTO ads (id, owner_id, title, description, price, category, created_at)
                KEY(id)
                VALUES (6, 3, 'iPhone 14 Pro Max 256 GB - Deep Purple',
                'Selling my iPhone 14 Pro Max in the exclusive Deep Purple colourway, 256 GB storage variant. I switched ecosystems after nine years on iOS and this phone deserves a home with someone who will actually use it.

Condition: 8.5/10. The Dynamic Island is flawless, the ProMotion 120 Hz screen has zero scratches (always used a tempered glass screen protector), and the triple-camera system produces stunning photos. Battery health is 91% according to Settings — well above the threshold where any degradation would be noticeable in day-to-day use.

The only imperfection: a small scratch on the titanium rail on the bottom right corner, approximately 3 mm long. Invisible in a case. Comes factory unlocked — works with any carrier worldwide.

Included: original Apple charger (USB-C to Lightning), original box (IMEI sticker intact), and a barely-used Casetify case worth $55. I will do a full factory reset and iCloud sign-out before handover. Please bring cash or arrange a bank transfer before meeting. I can meet at a coffee shop in the city centre for safety.',
                720.00, 'Electronics', CURRENT_TIMESTAMP)
            """);

            stmt.execute("""
                MERGE INTO ads (id, owner_id, title, description, price, category, created_at)
                KEY(id)
                VALUES (7, 2, 'Canon EOS R5 Body + RF 24-70mm f/2.8L',
                'Selling my Canon EOS R5 mirrorless camera body together with the Canon RF 24-70mm f/2.8L IS USM lens — a professional kit capable of handling virtually any photography or videography assignment.

Camera body: purchased in 2022, shutter count is approximately 18,000 actuations (the rated life is 500,000 — this camera has barely been used). Full-frame 45 MP sensor, 8-stop in-body image stabilisation, 8K RAW video internally, Dual Pixel AF II with subject tracking. The EVF is crisp and the rear screen articulates fully.

Lens: the RF 24-70/2.8L is considered one of the finest standard zoom lenses ever made. Razor-sharp corner to corner even wide open, near-silent AF, and IS that stacks with IBIS for ridiculous stability. No fungus, no haze, no scratches on the optics. I had both the body and the lens professionally cleaned two months ago.

Included: two LP-E6NH batteries and dual charger, original strap, body cap, rear lens cap, front lens cap, Canon lens hood ET-87, all original packaging and warranty cards. I will throw in a 128 GB CFexpress Type B card (worth $80) for a quick sale. Priced below market — need it gone this month.',
                3400.00, 'Electronics', CURRENT_TIMESTAMP)
            """);

            stmt.execute("""
                MERGE INTO ads (id, owner_id, title, description, price, category, created_at)
                KEY(id)
                VALUES (8, 3, 'Bright 1-Bed Apartment - City Centre',
                'Available immediately: a beautifully renovated one-bedroom apartment on the third floor of a well-maintained 1920s building in the heart of the city. The apartment was fully gutted and rebuilt in 2021 with high-spec finishes throughout.

Layout: open-plan kitchen and living area (28 m²) with floor-to-ceiling windows facing south — flooded with natural light all day. The kitchen features Bosch appliances including an induction hob, integrated dishwasher, and a full-size fridge-freezer. The bedroom (14 m²) fits a king-size bed and has a walk-in wardrobe. The bathroom has a rainfall shower, heated towel rail, and underfloor heating. Total usable area: 52 m².

Building amenities: video intercom, secure bicycle storage in the basement, and a shared rooftop terrace with seating. The building is energy rated B2. Broadband is pre-wired to 1 Gbps fibre.

Location: 400 m to the main tram stop (8 minutes to the central station), 200 m to a large supermarket, and surrounded by cafes and independent restaurants. Very quiet at night despite being central — the building is set back from the main road.

Rent: $1,250/month plus utilities (typically $80-120 in winter). Deposit: two months. Minimum lease: 12 months. Pets considered case by case.',
                1250.00, 'Real Estate', CURRENT_TIMESTAMP)
            """);

            stmt.execute("""
                MERGE INTO ads (id, owner_id, title, description, price, category, created_at)
                KEY(id)
                VALUES (9, 2, 'Guitar Lessons - All Levels Welcome',
                'Professional guitar tuition available in-person (Springfield area) or online via video call. I hold a Bachelor of Music from Berklee College of Music and have been teaching privately for eleven years. My students range from complete beginners picking up their first instrument to advanced players preparing for conservatory auditions.

What I teach: acoustic and electric guitar, music theory tailored to guitarists, ear training, sight-reading in standard notation and tablature, improvisation and soloing, songwriting fundamentals, and stylistic technique in rock, blues, jazz, classical, folk, and fingerpicking. I adapt every lesson to the student''s goals and learning style — no cookie-cutter curriculum.

My students have gone on to form gigging bands, pass grade examinations with distinction, win regional songwriting competitions, and one has been accepted to a full-scholarship conservatory programme.

Lesson structure: 30-minute sessions for younger beginners, 45 or 60 minutes for everyone else. I assign practice material between lessons and provide written notes after each session. First lesson is a free 30-minute consultation so we can establish your goals and I can assess your current level.

Rate listed is per 60-minute lesson. Block bookings of 10 lessons receive a 10% discount. I am available Monday to Saturday, morning and afternoon slots.',
                55.00, 'Services', CURRENT_TIMESTAMP)
            """);

            stmt.execute("""
                MERGE INTO ads (id, owner_id, title, description, price, category, created_at)
                KEY(id)
                VALUES (10, 3, '2019 Volkswagen Golf GTI - Stage 1',
                'Selling my 2019 Volkswagen Golf GTI Performance Pack with a tasteful Stage 1 ECU tune. This is one of the most well-rounded hot hatches ever made, and this particular example has been exceptionally well maintained.

Mechanicals: 2.0 TSI turbocharged four-cylinder producing approximately 255 bhp and 380 Nm torque after the Remap (stock is 245 bhp). The power delivery is linear and predictable — this is not a nervous car. The 6-speed DSG dual-clutch gearbox shifts instantly in Sport mode and behaves perfectly in normal city driving. DCC adaptive suspension adjusted to my preference over two years — I know exactly which setting suits which road.

Maintenance history: full VW dealer service record up to 40,000 miles, then independently serviced at a specialist every 7,500 miles with Castrol Edge 5W-40. New front brake discs and pads at 48,000 miles. Current mileage: 61,200. MOT valid for 9 more months. Michelin Pilot Sport 4 tyres all round — fitted at 56,000 miles and in excellent condition.

Cosmetics: Tornado Red exterior, black Pretoria 18-inch alloys (no kerb damage), factory-tinted rear windows. Interior is immaculate — no rips or stains. Non-smoker owner. The car has never been tracked.

Included: two keys (both working), full service history folder, and the stock ECU map on a USB stick if the buyer wishes to revert.',
                19500.00, 'Vehicles', CURRENT_TIMESTAMP)
            """);

            // Seed: messages between Alice and Bob about the guitar
            stmt.execute("""
                MERGE INTO messages (id, sender_id, recipient_id, ad_id, body, sent_at)
                KEY(id)
                VALUES (1, 3, 2, 1, 'Hi Alice, is the guitar still available?', CURRENT_TIMESTAMP)
            """);

            stmt.execute("""
                MERGE INTO messages (id, sender_id, recipient_id, ad_id, body, sent_at)
                KEY(id)
                VALUES (2, 2, 3, 1, 'Yes Bob, it is! Feel free to come and try it.', CURRENT_TIMESTAMP)
            """);

            stmt.execute("""
                MERGE INTO messages (id, sender_id, recipient_id, ad_id, body, sent_at)
                KEY(id)
                VALUES (3, 3, 2, 1, 'Great, would you take $1000 for it?', CURRENT_TIMESTAMP)
            """);

            // Reset sequences so new inserts don't conflict with seeded IDs
            stmt.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 10");
            stmt.execute("ALTER TABLE ads ALTER COLUMN id RESTART WITH 20");
            stmt.execute("ALTER TABLE messages ALTER COLUMN id RESTART WITH 10");

        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed", e);
        }

        System.out.println("");
        System.out.println("==============================================");
        System.out.println("  WicketAds is running at http://localhost:8080");
        System.out.println("  Admin login: admin / admin");
        System.out.println("  Seed users:  alice / alice123  |  bob / bob456");
        System.out.println("==============================================");
        System.out.println("");
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
