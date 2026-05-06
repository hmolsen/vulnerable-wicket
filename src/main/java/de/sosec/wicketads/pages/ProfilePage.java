package de.sosec.wicketads.pages;

import de.sosec.wicketads.WicketAdsSession;
import de.sosec.wicketads.model.User;
import de.sosec.wicketads.service.UserService;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ProfilePage extends WebPage {
    private static final long serialVersionUID = 1L;

    public ProfilePage() {
        this(new PageParameters());
    }

    public ProfilePage(PageParameters params) {
        super(params);
        if (!WicketAdsSession.get().isLoggedIn()) {
            throw new RestartResponseException(LoginPage.class);
        }

        User current = WicketAdsSession.get().getCurrentUser();
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        add(feedback);

        // VULNERABILITY: userId hidden field - can be tampered to update another user
        Model<Integer> userIdModel = Model.of(current.getId());
        Model<String> usernameModel = Model.of(current.getUsername());
        Model<String> passwordModel = Model.of(current.getPassword());
        Model<String> fullNameModel = Model.of(current.getFullName());
        Model<String> streetModel = Model.of(current.getStreet());
        Model<String> houseNumberModel = Model.of(current.getHouseNumber());
        Model<String> zipCodeModel = Model.of(current.getZipCode());
        Model<String> cityModel = Model.of(current.getCity());
        Model<String> countryModel = Model.of(current.getCountry());
        Model<String> phoneModel = Model.of(current.getPhone());
        Model<String> emailModel = Model.of(current.getEmail());

        Form<Void> form = new Form<>("form") {
            @Override
            protected void onSubmit() {
                // VULNERABILITY: uses userId from hidden form field, not from session
                // Attacker can tamper the hidden field to update any user's profile
                int targetUserId = userIdModel.getObject();

                UserService.update(
                        targetUserId,
                        usernameModel.getObject(),
                        passwordModel.getObject(),
                        "user",
                        fullNameModel.getObject(),
                        streetModel.getObject(),
                        houseNumberModel.getObject(),
                        zipCodeModel.getObject(),
                        cityModel.getObject(),
                        countryModel.getObject(),
                        phoneModel.getObject(),
                        emailModel.getObject()
                );
                success("Profile updated successfully.");
            }
        };

        form.add(new HiddenField<>("userId", userIdModel, Integer.class));
        form.add(new TextField<>("username", usernameModel));
        form.add(new TextField<>("password", passwordModel));
        form.add(new TextField<>("fullName", fullNameModel));
        form.add(new TextField<>("street", streetModel));
        form.add(new TextField<>("houseNumber", houseNumberModel));
        form.add(new TextField<>("zipCode", zipCodeModel));
        form.add(new TextField<>("city", cityModel));
        form.add(new TextField<>("country", countryModel));
        form.add(new TextField<>("phone", phoneModel));
        form.add(new TextField<>("email", emailModel));
        add(form);
    }
}
