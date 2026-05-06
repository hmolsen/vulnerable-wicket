package de.sosec.wicketads.pages;

import de.sosec.wicketads.WicketAdsSession;
import de.sosec.wicketads.model.User;
import de.sosec.wicketads.service.UserService;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;

public class AdminUserListPage extends WebPage {
    private static final long serialVersionUID = 1L;

    public AdminUserListPage() {
        this(new PageParameters());
    }

    public AdminUserListPage(PageParameters params) {
        super(params);
        // Requires admin role - checked only in constructor
        if (!WicketAdsSession.get().isAdmin()) {
            throw new RestartResponseException(HomePage.class);
        }

        List<User> users = UserService.findAll();

        ListView<User> userList = new ListView<>("userList", users) {
            @Override
            protected void populateItem(ListItem<User> item) {
                User user = item.getModelObject();
                item.add(new Label("userId", String.valueOf(user.getId())));
                item.add(new Label("username", user.getUsername()));
                item.add(new Label("role", user.getRole()));
                item.add(new Label("email", user.getEmail()));
                item.add(new Label("phone", user.getPhone()));
                // VULNERABILITY: SENSITIVE DATA EXPOSURE - plain text password shown in admin UI
                item.add(new Label("password", user.getPassword()));

                PageParameters editParams = new PageParameters();
                editParams.add("userId", user.getId());

                item.add(new Link<Void>("editLink") {
                    @Override
                    public void onClick() {
                        setResponsePage(AdminEditUserPage.class, editParams);
                    }
                });

                item.add(new Link<Void>("deleteLink") {
                    @Override
                    public void onClick() {
                        UserService.deleteById(user.getId());
                        setResponsePage(AdminUserListPage.class);
                    }
                });
            }
        };
        add(userList);
    }
}
