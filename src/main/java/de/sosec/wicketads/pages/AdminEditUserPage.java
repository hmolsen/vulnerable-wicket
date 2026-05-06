package de.sosec.wicketads.pages;

import de.sosec.wicketads.WicketAdsSession;
import de.sosec.wicketads.model.User;
import de.sosec.wicketads.service.UserService;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Arrays;

public class AdminEditUserPage extends WebPage {
    private static final long serialVersionUID = 1L;

    public AdminEditUserPage() {
        this(new PageParameters());
    }

    public AdminEditUserPage(PageParameters params) {
        super(params);
        if (!WicketAdsSession.get().isAdmin()) {
            throw new RestartResponseException(HomePage.class);
        }

        // VULNERABILITY: IDOR - userId from URL parameter, no additional ownership validation
        int userId = params.get("userId").toInt(0);
        User user = UserService.findById(userId);

        if (user == null) {
            add(new FeedbackPanel("feedback"));
            error("User not found.");
            add(new Form<>("form").setVisible(false));
            return;
        }

        add(new FeedbackPanel("feedback"));

        Model<String> usernameModel = Model.of(user.getUsername());
        Model<String> passwordModel = Model.of(user.getPassword());
        Model<String> roleModel = Model.of(user.getRole());
        Model<String> fullNameModel = Model.of(user.getFullName());
        Model<String> streetModel = Model.of(user.getStreet());
        Model<String> houseNumberModel = Model.of(user.getHouseNumber());
        Model<String> zipCodeModel = Model.of(user.getZipCode());
        Model<String> cityModel = Model.of(user.getCity());
        Model<String> countryModel = Model.of(user.getCountry());
        Model<String> phoneModel = Model.of(user.getPhone());
        Model<String> emailModel = Model.of(user.getEmail());

        Form<Void> form = new Form<>("form") {
            @Override
            protected void onSubmit() {
                UserService.update(
                        userId,
                        usernameModel.getObject(),
                        passwordModel.getObject(),
                        roleModel.getObject(),
                        fullNameModel.getObject(),
                        streetModel.getObject(),
                        houseNumberModel.getObject(),
                        zipCodeModel.getObject(),
                        cityModel.getObject(),
                        countryModel.getObject(),
                        phoneModel.getObject(),
                        emailModel.getObject()
                );
                setResponsePage(AdminUserListPage.class);
            }
        };

        form.add(new TextField<>("username", usernameModel));
        form.add(new TextField<>("password", passwordModel));
        form.add(new DropDownChoice<>("role", roleModel, Arrays.asList("user", "admin")));
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
