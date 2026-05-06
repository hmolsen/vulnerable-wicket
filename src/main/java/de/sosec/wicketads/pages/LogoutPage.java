package de.sosec.wicketads.pages;

import de.sosec.wicketads.WicketAdsSession;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class LogoutPage extends WebPage {
    private static final long serialVersionUID = 1L;

    public LogoutPage() {
        WicketAdsSession.get().signOut();
        throw new RestartResponseException(HomePage.class);
    }

    public LogoutPage(PageParameters params) {
        super(params);
        WicketAdsSession.get().signOut();
        throw new RestartResponseException(HomePage.class);
    }
}
