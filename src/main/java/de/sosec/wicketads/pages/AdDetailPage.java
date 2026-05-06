package de.sosec.wicketads.pages;

import de.sosec.wicketads.WicketAdsSession;
import de.sosec.wicketads.model.Ad;
import de.sosec.wicketads.service.AdService;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class AdDetailPage extends WebPage {
    private static final long serialVersionUID = 1L;

    public AdDetailPage(PageParameters params) {
        super(params);
        int adId = params.get("id").toInt(0);
        Ad ad = AdService.findById(adId);

        if (ad == null) {
            add(new Label("title", "Ad not found"));
            add(new Label("category", ""));
            add(new Label("price", ""));
            add(new Label("owner", ""));
            add(new Label("createdAt", ""));
            Label desc = new Label("description", "");
            desc.setEscapeModelStrings(false);
            add(desc);
            add(new WebMarkupContainer("contactContainer").setVisible(false));
            return;
        }

        add(new Label("title", ad.getTitle()));
        add(new Label("category", ad.getCategory()));
        add(new Label("price", ad.getPrice() != null ? "$ " + ad.getPrice() : "Free"));
        add(new Label("owner", ad.getOwnerUsername()));
        add(new Label("createdAt", ad.getCreatedAt() != null ? ad.getCreatedAt().toString() : ""));

        // VULNERABILITY: XSS - description rendered without escaping
        Label description = new Label("description", ad.getDescription());
        description.setEscapeModelStrings(false);
        add(description);

        // Contact seller button (only if logged in and not the owner)
        boolean loggedIn = WicketAdsSession.get().isLoggedIn();
        boolean isOwner = loggedIn && WicketAdsSession.get().getCurrentUser().getId() == ad.getOwnerId();

        WebMarkupContainer contactContainer = new WebMarkupContainer("contactContainer");
        contactContainer.setVisible(loggedIn && !isOwner);

        PageParameters chatParams = new PageParameters();
        chatParams.add("otherId", ad.getOwnerId());
        chatParams.add("adId", ad.getId());
        contactContainer.add(new BookmarkablePageLink<>("contactLink", ChatPage.class, chatParams));
        add(contactContainer);
    }
}
