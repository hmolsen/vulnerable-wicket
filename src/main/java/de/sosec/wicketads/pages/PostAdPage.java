package de.sosec.wicketads.pages;

import de.sosec.wicketads.WicketAdsSession;
import de.sosec.wicketads.service.AdService;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class PostAdPage extends WebPage {
    private static final long serialVersionUID = 1L;

    public PostAdPage() {
        this(new PageParameters());
    }

    public PostAdPage(PageParameters params) {
        super(params);
        if (!WicketAdsSession.get().isLoggedIn()) {
            throw new RestartResponseException(LoginPage.class);
        }

        add(new FeedbackPanel("feedback"));

        List<String> categories = Arrays.asList("Electronics", "Vehicles", "Real Estate", "Jobs", "Services", "Other");

        Model<String> titleModel = Model.of("");
        Model<String> descriptionModel = Model.of("");
        Model<String> priceModel = Model.of("");
        Model<String> categoryModel = Model.of("Electronics");

        Form<Void> form = new Form<>("form") {
            @Override
            protected void onSubmit() {
                String title = titleModel.getObject();
                String description = descriptionModel.getObject();
                String priceStr = priceModel.getObject();
                String category = categoryModel.getObject();

                if (title == null || title.isBlank()) {
                    error("Title is required.");
                    return;
                }

                BigDecimal price = BigDecimal.ZERO;
                try {
                    if (priceStr != null && !priceStr.isBlank()) {
                        price = new BigDecimal(priceStr.replace(",", "."));
                    }
                } catch (NumberFormatException e) {
                    error("Price must be a valid number.");
                    return;
                }

                int ownerId = WicketAdsSession.get().getCurrentUser().getId();
                int newAdId = AdService.create(ownerId, title, description, price, category);

                PageParameters pp = new PageParameters();
                pp.add("id", newAdId);
                setResponsePage(AdDetailPage.class, pp);
            }
        };

        form.add(new TextField<>("title", titleModel));
        form.add(new TextArea<>("description", descriptionModel));
        form.add(new TextField<>("price", priceModel));
        form.add(new DropDownChoice<>("category", categoryModel, categories));
        add(form);
    }
}
