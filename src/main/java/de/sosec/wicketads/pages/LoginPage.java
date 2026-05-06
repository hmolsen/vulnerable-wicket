package de.sosec.wicketads.pages;

import de.sosec.wicketads.WicketAdsSession;
import de.sosec.wicketads.model.User;
import de.sosec.wicketads.service.UserService;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;

public class LoginPage extends WebPage {
    private static final long serialVersionUID = 1L;

    public LoginPage() {
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        add(feedback);

        Model<String> usernameModel = Model.of("");
        Model<String> passwordModel = Model.of("");

        Form<Void> form = new Form<>("form") {
            @Override
            protected void onSubmit() {
                String username = usernameModel.getObject();
                String password = passwordModel.getObject();

                // USERNAME ENUMERATION: distinct error messages
                User user = UserService.findByUsername(username);
                if (user == null) {
                    error("User not found.");
                    return;
                }
                if (!user.getPassword().equals(password)) {
                    error("Wrong password.");
                    return;
                }

                WicketAdsSession.get().signIn(username, password);
                setResponsePage(HomePage.class);
            }
        };

        form.add(new TextField<>("username", usernameModel));
        form.add(new TextField<>("password", passwordModel));
        add(form);
    }
}
