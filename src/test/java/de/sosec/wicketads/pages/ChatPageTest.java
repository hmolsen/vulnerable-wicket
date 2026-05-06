package de.sosec.wicketads.pages;

import de.sosec.wicketads.AbstractWicketTest;
import de.sosec.wicketads.service.MessageService;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatPageTest extends AbstractWicketTest {

    @Test
    void anonymousUser_redirectedToLogin() {
        tester.startPage(ChatPage.class);
        tester.assertRenderedPage(LoginPage.class);
    }

    @Test
    void loggedInUser_seesConversationList() {
        loginAsAlice();
        tester.startPage(ChatPage.class);
        tester.assertRenderedPage(ChatPage.class);
        // Alice has seed messages with Bob, so the conversation list is populated
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("bob"), "Alice must see Bob in her conversation list");
    }

    @Test
    void withOtherId_rendersMessageThread() {
        loginAsAlice();
        PageParameters pp = new PageParameters();
        pp.add("otherId", 3);   // bob
        tester.startPage(ChatPage.class, pp);
        tester.assertRenderedPage(ChatPage.class);
        // Seed messages between alice and bob appear
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("Hi Alice") || html.contains("Yes Bob") || html.contains("1000"),
                "Seed messages between alice and bob must appear in the thread");
    }

    @Test
    void xss_messageBody_notEscaped() {
        // VULN XSS: message body rendered with setEscapeModelStrings(false)
        String xssBody = "<script>alert('chat-xss')</script>";
        MessageService.send(3, 2, 0, xssBody);   // bob → alice

        loginAsAlice();
        PageParameters pp = new PageParameters();
        pp.add("otherId", 3);
        tester.startPage(ChatPage.class, pp);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains("<script>alert('chat-xss')</script>"),
                "VULN XSS: script tag in message body must NOT be HTML-escaped");
        assertFalse(html.contains("&lt;script&gt;"),
                "VULN XSS: raw tag must appear, not HTML-entity-encoded form");
    }

    @Test
    void idor_userId_urlParameter_readsAnyConversation() {
        // VULN IDOR: ChatPage accepts ?userId=N from the URL without verifying
        // that N matches the session user. An attacker logged in as admin (id=1)
        // can supply ?userId=2 to impersonate alice and read alice/bob messages.
        //
        // We verify that getConversations(aliceId) returns alice's data even
        // when called from a context where the session belongs to a different user.
        loginAsAdmin();  // admin is NOT a participant in alice/bob conversations

        PageParameters pp = new PageParameters();
        pp.add("userId",  2);   // alice's ID – not the session user's ID
        pp.add("otherId", 3);   // bob
        tester.startPage(ChatPage.class, pp);
        tester.assertRenderedPage(ChatPage.class);

        String html = tester.getLastResponseAsString();
        // alice/bob seed messages appear even though admin is logged in
        assertTrue(html.contains("Hi Alice") || html.contains("guitar") || html.contains("1000"),
                "VULN IDOR: admin can read alice/bob thread by supplying ?userId=2");
    }

    @Test
    void sendMessage_appearsInThread() {
        loginAsAlice();
        long tag = System.currentTimeMillis();
        String body = "ChatPage integration test " + tag;

        PageParameters pp = new PageParameters();
        pp.add("otherId", 3);
        tester.startPage(ChatPage.class, pp);

        FormTester ft = tester.newFormTester("sendForm");
        ft.setValue("messageBody", body);
        ft.submit();

        // After send the page reloads (full redirect to ChatPage)
        tester.assertRenderedPage(ChatPage.class);
        String html = tester.getLastResponseAsString();
        assertTrue(html.contains(body),
                "Just-sent message must appear in the reloaded thread");
    }

    @Test
    void noConversationSelected_sendFormHidden() {
        loginAsAlice();
        tester.startPage(ChatPage.class);   // no otherId param
        tester.assertInvisible("sendForm");
    }

    @Test
    void noConversationSelected_showsPlaceholder() {
        loginAsAlice();
        tester.startPage(ChatPage.class);
        tester.assertVisible("noConversation");
    }
}
