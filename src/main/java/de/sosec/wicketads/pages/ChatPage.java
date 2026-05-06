package de.sosec.wicketads.pages;

import de.sosec.wicketads.WicketAdsSession;
import de.sosec.wicketads.model.ConversationSummary;
import de.sosec.wicketads.model.Message;
import de.sosec.wicketads.service.MessageService;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.List;

public class ChatPage extends WebPage {
    private static final long serialVersionUID = 1L;

    public ChatPage() {
        this(new PageParameters());
    }

    public ChatPage(PageParameters params) {
        super(params);
        if (!WicketAdsSession.get().isLoggedIn()) {
            throw new RestartResponseException(LoginPage.class);
        }

        int sessionUserId = WicketAdsSession.get().getCurrentUser().getId();

        // VULNERABILITY: userId from URL, not validated against session
        // Any logged-in user can read any conversation by manipulating userId parameter
        int viewUserId = params.get("userId").toInt(sessionUserId);
        int otherId = params.get("otherId").toInt(-1);
        int adId = params.get("adId").toInt(-1);

        // Load conversations for viewUserId (may differ from session user!)
        List<ConversationSummary> conversations = MessageService.getConversations(viewUserId);

        ListView<ConversationSummary> convList = new ListView<>("conversations", conversations) {
            @Override
            protected void populateItem(ListItem<ConversationSummary> item) {
                ConversationSummary conv = item.getModelObject();
                PageParameters lp = new PageParameters();
                lp.add("otherId", conv.getOtherUserId());
                if (conv.getAdId() > 0) lp.add("adId", conv.getAdId());

                BookmarkablePageLink<Void> link = new BookmarkablePageLink<>("convLink", ChatPage.class, lp);
                link.add(new Label("otherUsername", conv.getOtherUsername()));
                String adInfo = conv.getAdTitle() != null ? " re: " + conv.getAdTitle() : "";
                link.add(new Label("adInfo", adInfo));   // adInfo is inside the link in HTML
                item.add(link);
            }
        };
        add(convList);

        // Load thread - no check that viewUserId is actually the session user
        List<Message> thread = new ArrayList<>();
        if (otherId > 0) {
            thread = MessageService.getThread(viewUserId, otherId);
        }

        ListView<Message> messageList = new ListView<>("messages", thread) {
            @Override
            protected void populateItem(ListItem<Message> item) {
                Message msg = item.getModelObject();
                item.add(new Label("senderUsername", msg.getSenderUsername()));
                // VULNERABILITY: XSS - message body rendered without escaping
                Label body = new Label("body", msg.getBody());
                body.setEscapeModelStrings(false);
                item.add(body);
                item.add(new Label("sentAt", msg.getSentAt() != null ? msg.getSentAt().toString() : ""));
            }
        };
        add(messageList);

        WebMarkupContainer noConvMsg = new WebMarkupContainer("noConversation");
        noConvMsg.setVisible(otherId < 0);
        add(noConvMsg);

        // Send message form
        Model<String> bodyModel = Model.of("");
        int finalOtherId = otherId;
        int finalAdId = adId;

        Form<Void> sendForm = new Form<>("sendForm") {
            @Override
            protected void onSubmit() {
                String body = bodyModel.getObject();
                if (body != null && !body.isBlank() && finalOtherId > 0) {
                    MessageService.send(sessionUserId, finalOtherId, finalAdId, body);
                    PageParameters rp = new PageParameters();
                    rp.add("otherId", finalOtherId);
                    if (finalAdId > 0) rp.add("adId", finalAdId);
                    setResponsePage(ChatPage.class, rp);
                }
            }
        };
        sendForm.add(new TextArea<>("messageBody", bodyModel));
        sendForm.setVisible(otherId > 0);
        add(sendForm);
    }
}
