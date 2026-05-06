package de.sosec.wicketads.pages;

import de.sosec.wicketads.WicketAdsSession;
import de.sosec.wicketads.model.Ad;
import de.sosec.wicketads.service.AdService;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Arrays;
import java.util.List;

public class HomePage extends WebPage {
    private static final long serialVersionUID = 1L;

    private String keyword = "";
    private String category = "";

    public HomePage() {
        this(new PageParameters());
    }

    public HomePage(PageParameters params) {
        super(params);
        // Read search terms from URL parameters (set by form redirect below)
        keyword  = params.get("keyword").toString("");
        category = params.get("category").toString("");
        buildPage();
    }

    private void buildPage() {
        boolean loggedIn = WicketAdsSession.get().isLoggedIn();

        // Navigation links
        WebMarkupContainer guestNav = new WebMarkupContainer("guestNav");
        guestNav.setVisible(!loggedIn);
        guestNav.add(new BookmarkablePageLink<>("loginLink", LoginPage.class));
        guestNav.add(new BookmarkablePageLink<>("registerLink", RegisterPage.class));
        add(guestNav);

        WebMarkupContainer userNav = new WebMarkupContainer("userNav");
        userNav.setVisible(loggedIn);
        if (loggedIn) {
            userNav.add(new Label("welcomeUser", WicketAdsSession.get().getCurrentUser().getUsername()));
            userNav.add(new BookmarkablePageLink<>("profileLink", ProfilePage.class));
            userNav.add(new BookmarkablePageLink<>("postAdLink", PostAdPage.class));
            userNav.add(new BookmarkablePageLink<>("chatLink", ChatPage.class));
            userNav.add(new BookmarkablePageLink<>("logoutLink", LogoutPage.class));
        } else {
            userNav.add(new Label("welcomeUser", ""));
            userNav.add(new BookmarkablePageLink<>("profileLink", ProfilePage.class));
            userNav.add(new BookmarkablePageLink<>("postAdLink", PostAdPage.class));
            userNav.add(new BookmarkablePageLink<>("chatLink", ChatPage.class));
            userNav.add(new BookmarkablePageLink<>("logoutLink", LogoutPage.class));
        }
        add(userNav);

        WebMarkupContainer adminNav = new WebMarkupContainer("adminNav");
        adminNav.setVisible(WicketAdsSession.get().isAdmin());
        adminNav.add(new BookmarkablePageLink<>("adminUsersLink", AdminUserListPage.class));
        add(adminNav);

        // Search form
        List<String> categories = Arrays.asList("", "Electronics", "Vehicles", "Real Estate", "Jobs", "Services", "Other");
        Form<Void> searchForm = new Form<>("searchForm") {
            @Override
            protected void onSubmit() {
                // PRG: redirect so the search terms live in the URL
                PageParameters pp = new PageParameters();
                if (keyword != null && !keyword.isBlank())   pp.add("keyword",  keyword);
                if (category != null && !category.isBlank()) pp.add("category", category);
                setResponsePage(HomePage.class, pp);
            }
        };
        searchForm.add(new TextField<>("keyword",  new PropertyModel<>(this, "keyword")));
        searchForm.add(new DropDownChoice<>("category", new PropertyModel<>(this, "category"), categories));
        add(searchForm);

        // Ad listing
        List<Ad> ads;
        if ((keyword != null && !keyword.isBlank()) || (category != null && !category.isBlank())) {
            ads = AdService.search(keyword, category);
        } else {
            ads = AdService.findRecent();
        }

        ListView<Ad> adList = new ListView<>("adList", ads) {
            @Override
            protected void populateItem(ListItem<Ad> item) {
                Ad ad = item.getModelObject();
                PageParameters pp = new PageParameters();
                pp.add("id", ad.getId());
                BookmarkablePageLink<Void> titleLink = new BookmarkablePageLink<>("adLink", AdDetailPage.class, pp);
                titleLink.add(new Label("title", ad.getTitle()));
                item.add(titleLink);
                item.add(new Label("category", ad.getCategory()));
                item.add(new Label("price", ad.getPrice() != null ? "$ " + ad.getPrice() : ""));
                item.add(new Label("owner", ad.getOwnerUsername()));
            }
        };
        add(adList);

        add(new Label("noAds", "No ads found.") {
            @Override
            public boolean isVisible() { return ads.isEmpty(); }
        });
    }
}
