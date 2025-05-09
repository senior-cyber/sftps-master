package com.senior.cyber.sftps.web.pages.my.profile;

import com.senior.cyber.sftps.web.factory.WebSession;
import com.senior.cyber.sftps.web.repository.UserRepository;
import com.senior.cyber.sftps.web.tink.MasterAead;
import com.senior.cyber.frmk.common.base.WicketFactory;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.tabs.ContentPanel;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.tabs.Tab;
import com.senior.cyber.frmk.common.wicket.layout.Size;
import com.senior.cyber.frmk.common.wicket.layout.UIColumn;
import com.senior.cyber.frmk.common.wicket.layout.UIContainer;
import com.senior.cyber.frmk.common.wicket.layout.UIRow;
import com.senior.cyber.frmk.common.wicket.markup.html.panel.ContainerFeedbackBehavior;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.ApplicationContext;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

public class MyProfilePageSecretTab extends ContentPanel {

    protected long uuid;

    protected Form<Void> form;

    protected UIRow row1;

    protected UIColumn secret_column;
    protected UIContainer secret_container;
    protected TextField<String> secret_field;
    protected String secret_value;

    protected Button generateButton;
    protected BookmarkablePageLink<Void> cancelButton;

    public MyProfilePageSecretTab(String id, String name, TabbedPanel<Tab> containerPanel, Map<String, Object> data) {
        super(id, name, containerPanel, data);
    }

    @Override
    protected void onInitData() {
        WebSession session = (WebSession) getSession();
        this.uuid = session.getUserId();
        ApplicationContext context = WicketFactory.getApplicationContext();
        UserRepository userRepository = context.getBean(UserRepository.class);
        MasterAead masterAead = context.getBean(MasterAead.class);

        Optional<User> optionalUser = userRepository.findById(this.uuid);
        User user = optionalUser.orElseThrow();

        Aead aeadDek = null;
        if (user.getDek() != null) {
            try {
                aeadDek = KeysetHandle.read(JsonKeysetReader.withString(user.getDek()), masterAead).getPrimitive(Aead.class);
            } catch (GeneralSecurityException | IOException e) {
                throw new WicketRuntimeException(e);
            }
        }

        if (aeadDek != null && user.getWebhookSecret() != null) {
            try {
                byte[] secret = aeadDek.decrypt(Base64.getDecoder().decode(user.getWebhookSecret()), "".getBytes(StandardCharsets.UTF_8));
                this.secret_value = new String(secret, StandardCharsets.UTF_8);
            } catch (GeneralSecurityException e) {
                throw new WicketRuntimeException(e);
            }
        } else {
            this.secret_value = user.getWebhookSecret();
        }
    }

    @Override
    protected void onInitHtml(MarkupContainer body) {
        this.form = new Form<>("form");
        body.add(this.form);

        this.row1 = UIRow.newUIRow("row1", this.form);

        this.secret_column = this.row1.newUIColumn("secret_column", Size.Six_6);
        this.secret_container = this.secret_column.newUIContainer("secret_container");
        this.secret_field = new TextField<>("secret_field", new PropertyModel<>(this, "secret_value"));
        this.secret_field.setLabel(Model.of("Web Hook Secret"));
        this.secret_field.setEnabled(false);
        this.secret_field.add(new ContainerFeedbackBehavior());
        this.secret_container.add(this.secret_field);
        this.secret_container.newFeedback("secret_feedback", this.secret_field);

        this.row1.lastUIColumn("last_column");

        this.generateButton = new Button("generateButton") {
            @Override
            public void onSubmit() {
                generateButtonClick();
            }
        };
        this.form.add(this.generateButton);

        this.cancelButton = new BookmarkablePageLink<>("cancelButton", MyProfilePage.class);
        this.form.add(this.cancelButton);
    }

    protected void generateButtonClick() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES", BouncyCastleProvider.PROVIDER_NAME);
            generator.init(256);
            SecretKey secretKey = generator.generateKey();
            this.secret_value = Base64.getEncoder().encodeToString(secretKey.getEncoded());

            ApplicationContext context = WicketFactory.getApplicationContext();
            UserRepository userRepository = context.getBean(UserRepository.class);
            MasterAead masterAead = context.getBean(MasterAead.class);

            Optional<User> optionalUser = userRepository.findById(this.uuid);
            User user = optionalUser.orElseThrow();

            Aead aeadDek = null;
            if (user.getDek() != null) {
                try {
                    aeadDek = KeysetHandle.read(JsonKeysetReader.withString(user.getDek()), masterAead).getPrimitive(Aead.class);
                } catch (GeneralSecurityException | IOException e) {
                    throw new WicketRuntimeException(e);
                }
            }

            if (aeadDek != null && this.secret_value != null) {
                try {
                    byte[] secret = aeadDek.encrypt(this.secret_value.getBytes(StandardCharsets.UTF_8), "".getBytes(StandardCharsets.UTF_8));
                    user.setWebhookSecret(Base64.getEncoder().encodeToString(secret));
                } catch (GeneralSecurityException e) {
                    throw new WicketRuntimeException(e);
                }
            } else {
                user.setWebhookSecret(this.secret_value);
            }

            userRepository.save(user);

            setResponsePage(MyProfilePage.class);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

}
