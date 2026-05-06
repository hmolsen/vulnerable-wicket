package de.sosec.wicketads.service;

import de.sosec.wicketads.AbstractDbTest;
import de.sosec.wicketads.model.ConversationSummary;
import de.sosec.wicketads.model.Message;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageServiceTest extends AbstractDbTest {

    // ── send & getThread ────────────────────────────────────────────────────

    @Test
    @Order(1)
    void send_persistsMessage_andAppearsInThread() {
        long tag = System.currentTimeMillis();
        String body = "Unit-test message " + tag;
        MessageService.send(2, 3, 1, body);   // alice → bob about ad 1

        List<Message> thread = MessageService.getThread(2, 3);
        assertTrue(thread.stream().anyMatch(m -> body.equals(m.getBody())),
                "Sent message must appear in thread");
    }

    @Test
    @Order(2)
    void getThread_includesBothDirections() {
        // Seed already has messages from bob→alice and alice→bob
        List<Message> thread = MessageService.getThread(2, 3);
        boolean hasSentByAlice = thread.stream().anyMatch(m -> m.getSenderId() == 2);
        boolean hasSentByBob   = thread.stream().anyMatch(m -> m.getSenderId() == 3);
        assertTrue(hasSentByAlice, "Thread must include messages sent by alice");
        assertTrue(hasSentByBob,   "Thread must include messages sent by bob");
    }

    @Test
    @Order(3)
    void getThread_includesSenderUsername() {
        List<Message> thread = MessageService.getThread(2, 3);
        assertFalse(thread.isEmpty());
        assertTrue(thread.stream().allMatch(m -> m.getSenderUsername() != null),
                "Each message must carry the sender username");
    }

    @Test
    @Order(4)
    void getThread_emptyForUnrelatedUsers() {
        // admin (1) and alice (2) have not exchanged any seed messages
        // (unless a prior test added one, which none do)
        List<Message> thread = MessageService.getThread(1, 2);
        assertTrue(thread.isEmpty(), "No messages exist between admin and alice initially");
    }

    // ── getConversations ────────────────────────────────────────────────────

    @Test
    @Order(5)
    void getConversations_listsCounterparty() {
        List<ConversationSummary> convs = MessageService.getConversations(2); // alice
        assertTrue(convs.stream().anyMatch(c -> c.getOtherUserId() == 3),
                "Alice's conversations must include bob");
    }

    @Test
    @Order(6)
    void getConversations_returnsOtherUsername() {
        List<ConversationSummary> convs = MessageService.getConversations(3); // bob
        assertTrue(convs.stream().anyMatch(c -> "alice".equals(c.getOtherUsername())));
    }

    @Test
    @Order(7)
    void getConversations_emptyForUserWithNoMessages() {
        // Create a fresh user who has never sent/received messages
        int newUserId = UserService.create("msg_test_lonely_" + System.currentTimeMillis(), "p", "user");
        List<ConversationSummary> convs = MessageService.getConversations(newUserId);
        assertTrue(convs.isEmpty());
    }

    // ── IDOR vulnerability ───────────────────────────────────────────────────

    @Test
    @Order(8)
    void getThread_idor_noOwnershipCheck_allowsArbitraryAccess() {
        // In the real app, the ChatPage passes userId from the URL without
        // verifying it matches the session user. getThread() itself performs
        // no ownership check either, so any caller can read any pair's thread.
        //
        // Here admin (id=1) is not a participant, yet we can fetch alice/bob's thread.
        List<Message> aliceBobThread = MessageService.getThread(2, 3);
        assertFalse(aliceBobThread.isEmpty(),
                "VULN IDOR: getThread returns messages regardless of who calls it");

        // An attacker logged in as admin can also supply userId=2 in the URL
        // to impersonate alice and read her conversations.
        List<ConversationSummary> aliceConvs = MessageService.getConversations(2);
        assertFalse(aliceConvs.isEmpty(),
                "VULN IDOR: getConversations for alice is reachable with any logged-in session");
    }

    @Test
    @Order(9)
    void send_noSenderValidation_anyIdAccepted() {
        // MessageService.send() accepts arbitrary sender IDs without validating
        // that the senderId matches the current session. The page passes
        // the session user ID, but the service doesn't enforce it.
        assertDoesNotThrow(() -> MessageService.send(3, 2, 0, "spoofed sender test"),
                "VULN BROKEN-ACCESS: send accepts any sender ID without session check");
    }
}
