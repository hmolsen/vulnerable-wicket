package de.sosec.wicketads.pages;

import de.sosec.wicketads.WicketAdsSession;
import de.sosec.wicketads.service.UserService;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;

public class RegisterPage extends WebPage {
    private static final long serialVersionUID = 1L;

    public RegisterPage() {
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        add(feedback);

        Model<String> usernameModel = Model.of("");
        Model<String> passwordModel = Model.of("");
        Model<String> confirmModel = Model.of("");

        Form<Void> form = new Form<>("form") {
            @Override
            protected void onSubmit() {
                String username = usernameModel.getObject();
                String password = passwordModel.getObject();
                String confirm = confirmModel.getObject();

                if (username == null || username.isBlank()) {
                    error("Username is required.");
                    return;
                }
                if (password == null || password.isBlank()) {
                    error("Password is required.");
                    return;
                }
                if (!password.equals(confirm)) {
                    error("Passwords do not match.");
                    return;
                }

                // USERNAME ENUMERATION: confirm existence explicitly
                if (UserService.findByUsername(username) != null) {
                    error("Username already taken.");
                    return;
                }

                int newId = UserService.create(username, password, "user");
                // Auto-login after registration
                WicketAdsSession.get().signIn(username, password);
                setResponsePage(HomePage.class);
            }
        };

        form.add(new TextField<>("username", usernameModel));
        // plain text input for password (not password type) - per spec
        form.add(new TextField<>("password", passwordModel));
        form.add(new TextField<>("confirmPassword", confirmModel));
        add(form);
    }
}
