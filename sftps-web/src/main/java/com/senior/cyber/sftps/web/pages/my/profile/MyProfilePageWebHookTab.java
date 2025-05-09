package com.senior.cyber.sftps.web.pages.my.profile;

import com.senior.cyber.sftps.web.factory.WebSession;
import com.senior.cyber.sftps.web.repository.UserRepository;
import com.senior.cyber.frmk.common.base.WicketFactory;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.tabs.ContentPanel;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.tabs.Tab;
import com.senior.cyber.frmk.common.wicket.layout.Size;
import com.senior.cyber.frmk.common.wicket.layout.UIColumn;
import com.senior.cyber.frmk.common.wicket.layout.UIContainer;
import com.senior.cyber.frmk.common.wicket.layout.UIRow;
import com.senior.cyber.frmk.common.wicket.markup.html.panel.ContainerFeedbackBehavior;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class MyProfilePageWebHookTab extends ContentPanel {

    protected long uuid;

    protected Form<Void> form;

    protected UIRow row1;

    protected UIColumn webhook_column;
    protected UIContainer webhook_container;
    protected DropDownChoice<Boolean> webhook_field;
    protected boolean webhook_value;

    protected UIRow row2;

    protected UIColumn url_column;
    protected UIContainer url_container;
    protected TextField<String> url_field;
    protected String url_value;

    protected Button saveButton;
    protected BookmarkablePageLink<Void> cancelButton;

    public MyProfilePageWebHookTab(String id, String name, TabbedPanel<Tab> containerPanel, Map<String, Object> data) {
        super(id, name, containerPanel, data);
    }

    @Override
    protected void onInitData() {
        WebSession session = (WebSession) getSession();
        this.uuid = session.getUserId();
        ApplicationContext context = WicketFactory.getApplicationContext();
        UserRepository userRepository = context.getBean(UserRepository.class);

        Optional<User> optionalUser = userRepository.findById(this.uuid);
        User user = optionalUser.orElseThrow(() -> new WicketRuntimeException(""));

        this.webhook_value = user.isWebhookEnabled();
        this.url_value = user.getWebhookUrl();
    }

    @Override
    protected void onInitHtml(MarkupContainer body) {
        this.form = new Form<>("form");
        body.add(this.form);

        this.row1 = UIRow.newUIRow("row1", this.form);

        this.webhook_column = this.row1.newUIColumn("webhook_column", Size.Four_4);
        this.webhook_container = this.webhook_column.newUIContainer("webhook_container");
        this.webhook_field = new DropDownChoice<>("webhook_field", new PropertyModel<>(this, "webhook_value"), Arrays.asList(true, false));
        this.webhook_field.setLabel(Model.of("Web Hook"));
        this.webhook_field.setRequired(true);
        this.webhook_field.add(new ContainerFeedbackBehavior());
        this.webhook_container.add(this.webhook_field);
        this.webhook_container.newFeedback("webhook_feedback", this.webhook_field);

        this.row1.lastUIColumn("last_column");

        this.row2 = UIRow.newUIRow("row2", this.form);

        this.url_column = this.row2.newUIColumn("url_column", Size.Four_4);
        this.url_container = this.url_column.newUIContainer("url_container");
        this.url_field = new TextField<>("url_field", new PropertyModel<>(this, "url_value"));
        this.url_field.setLabel(Model.of("Web Hook Address"));
        this.url_field.add(new ContainerFeedbackBehavior());
        this.url_container.add(this.url_field);
        this.url_container.newFeedback("url_feedback", this.url_field);

        this.row2.lastUIColumn("last_column");

        this.saveButton = new Button("saveButton") {
            @Override
            public void onSubmit() {
                saveButtonClick();
            }
        };
        this.form.add(this.saveButton);

        this.cancelButton = new BookmarkablePageLink<>("cancelButton", MyProfilePage.class);
        this.form.add(this.cancelButton);
    }

    protected void saveButtonClick() {
        ApplicationContext context = WicketFactory.getApplicationContext();
        UserRepository userRepository = context.getBean(UserRepository.class);

        Optional<User> optionalUser = userRepository.findById(this.uuid);
        User user = optionalUser.orElseThrow();

        user.setWebhookEnabled(this.webhook_value);
        user.setWebhookUrl(this.url_value);

        userRepository.save(user);

        setResponsePage(MyProfilePage.class);
    }

}
