package de.sosec.wicketads;

import de.sosec.wicketads.db.DatabaseInitializer;
import de.sosec.wicketads.pages.*;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.markup.html.WebPage;

public class WicketAdsApplication extends AuthenticatedWebApplication {

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

    @Override
    protected void init() {
        super.init();
        getCspSettings().blocking().disabled();
        DatabaseInitializer.initialize();

        mountPage("/register", RegisterPage.class);
        mountPage("/login", LoginPage.class);
        mountPage("/logout", LogoutPage.class);
        mountPage("/profile", ProfilePage.class);
        mountPage("/post-ad", PostAdPage.class);
        mountPage("/ad/${id}", AdDetailPage.class);
        mountPage("/chat", ChatPage.class);
        mountPage("/admin/users", AdminUserListPage.class);
        mountPage("/admin/users/edit", AdminEditUserPage.class);
    }

    @Override
    protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass() {
        return WicketAdsSession.class;
    }

    @Override
    protected Class<? extends WebPage> getSignInPageClass() {
        return LoginPage.class;
    }
}
