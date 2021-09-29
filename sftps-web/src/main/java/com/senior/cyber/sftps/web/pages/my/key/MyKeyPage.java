package com.senior.cyber.sftps.web.pages.my.key;

import com.senior.cyber.sftps.dao.entity.Key;
import com.senior.cyber.sftps.dao.entity.Role;
import com.senior.cyber.sftps.dao.entity.User;
import com.senior.cyber.sftps.web.MasterPage;
import com.senior.cyber.sftps.web.configuration.PkiApiConfiguration;
import com.senior.cyber.sftps.web.data.MySqlDataProvider;
import com.senior.cyber.sftps.web.dto.CertificateRequestDto;
import com.senior.cyber.sftps.web.repository.KeyRepository;
import com.senior.cyber.sftps.web.repository.UserRepository;
import com.senior.cyber.sftps.web.utility.*;
import com.senior.cyber.frmk.common.base.Bookmark;
import com.senior.cyber.frmk.common.base.WicketFactory;
import com.senior.cyber.frmk.common.pki.CertificateUtils;
import com.senior.cyber.frmk.common.pki.PrivateKeyUtils;
import com.senior.cyber.frmk.common.provider.QueryDataProvider;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.AbstractDataTable;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.DataTable;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.cell.ClickableCell;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.filter.*;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.filter.convertor.BooleanConvertor;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.filter.convertor.LongConvertor;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.filter.convertor.StringConvertor;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.repeater.data.table.translator.IHtmlTranslator;
import com.senior.cyber.frmk.common.wicket.layout.Size;
import com.senior.cyber.frmk.common.wicket.layout.UIColumn;
import com.senior.cyber.frmk.common.wicket.layout.UIContainer;
import com.senior.cyber.frmk.common.wicket.layout.UIRow;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.IResourceStream;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.persistence.Tuple;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Bookmark("/my/key")
@AuthorizeInstantiation({Role.NAME_ROOT, Role.NAME_Page_MyKey})
public class MyKeyPage extends MasterPage implements IHtmlTranslator<Tuple> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyKeyPage.class);

    protected Form<Void> form;

    protected UIRow row1;

    protected UIColumn nameColumn;
    protected UIContainer nameContainer;
    protected org.apache.wicket.markup.html.form.TextField<String> nameField;
    protected String nameValue;

    protected Button createButton;

    protected FilterForm<Map<String, Expression<?>>> keyBrowseForm;
    protected QueryDataProvider keyBrowseProvider;
    protected List<IColumn<Tuple, String>> keyBrowseColumn;
    protected AbstractDataTable<Tuple, String> keyBrowseTable;

    @Override
    protected void onInitData() {
        super.onInitData();
        this.keyBrowseProvider = new MySqlDataProvider("tbl_key");
        this.keyBrowseProvider.setCountField("key_id");
        this.keyBrowseProvider.applyWhere("user_id", "user_id = '" + getSession().getUserId() + "'");

        this.keyBrowseColumn = new ArrayList<>();
        this.keyBrowseColumn.add(Column.normalColumn(Model.of("ID"), "id", "key_id", this.keyBrowseProvider, new LongConvertor()));
        this.keyBrowseColumn.add(Column.normalColumn(Model.of("Name"), "name", "name", this.keyBrowseProvider, new StringConvertor()));
        this.keyBrowseColumn.add(Column.normalColumn(Model.of("Enabled"), "enabled", "enabled", this.keyBrowseProvider, new BooleanConvertor()));
        this.keyBrowseColumn.add(Column.normalColumn(Model.of("Download"), "download", "key_id", this.keyBrowseProvider, new StringConvertor(), this));
        this.keyBrowseColumn.add(new ActionFilteredColumn<>(Model.of("Action"), this::key_browse_action_link, this::key_browse_action_click));
    }

    @Override
    protected void onInitHtml(MarkupContainer body) {
        this.form = new Form<>("form");
        body.add(this.form);

        this.row1 = UIRow.newUIRow("row1", this.form);

        this.nameColumn = this.row1.newUIColumn("nameColumn", Size.Four_4);
        this.nameContainer = this.nameColumn.newUIContainer("nameContainer");
        this.nameField = new TextField<>("nameField", new PropertyModel<>(this, "nameValue"));
        this.nameField.setLabel(Model.of("Name"));
        this.nameField.setRequired(true);
        this.nameContainer.add(this.nameField);
        this.nameContainer.newFeedback("nameFeedback", this.nameField);

        this.row1.newUIColumn("lastColumn");

        this.createButton = new Button("createButton") {
            @Override
            public void onSubmit() {
                createButtonClick();
            }
        };
        this.form.add(createButton);

        this.keyBrowseForm = new FilterForm<>("keyBrowseForm", this.keyBrowseProvider);
        body.add(this.keyBrowseForm);

        this.keyBrowseTable = new DataTable<>("keyBrowseTable", this.keyBrowseColumn, this.keyBrowseProvider, 20);
        this.keyBrowseForm.add(this.keyBrowseTable);
    }

    @Override
    public ItemPanel htmlColumn(String key, IModel<String> display, Tuple object) {
        long uuid = object.get("id", long.class);
        return new ClickableCell(this::download, object, uuid + ".zip");
    }

    protected void download(Tuple tuple, Link<Void> link) {
        try {
            long uuid = tuple.get("id", long.class);
            ApplicationContext context = WicketFactory.getApplicationContext();
            KeyRepository keyRepository = context.getBean(KeyRepository.class);


            Optional<Key> optionalKey = keyRepository.findById(uuid);
            Key key = optionalKey.orElseThrow(() -> new WicketRuntimeException(""));

            String name = StringUtils.replace(key.getName(), " ", "_");

            String changeit = "changeit";

            ByteArrayOutputStream data = new ByteArrayOutputStream();
            ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(data);

            {
                StringBuffer buffer = new StringBuffer();
                buffer.append("# We export a p12 file with default password '" + changeit + "' for your convenience").append("\n");
                buffer.append("====================================================================================").append("\n");
                buffer.append(name + ".p12").append("\n");
                buffer.append("\n");
                buffer.append("# Reference OpenSSL command line to create p12 file").append("\n");
                buffer.append("====================================================================================").append("\n");
                buffer.append("openssl pkcs12 -inkey " + name + ".pem -in " + name + ".crt -export -out " + name + ".p12").append("\n");

                String crt = buffer.toString();
                ZipArchiveEntry caChainEntry = new ZipArchiveEntry("readme.txt");
                caChainEntry.setSize(crt.getBytes(StandardCharsets.UTF_8).length);
                zipArchiveOutputStream.putArchiveEntry(caChainEntry);
                zipArchiveOutputStream.write(crt.getBytes(StandardCharsets.UTF_8));
                zipArchiveOutputStream.closeArchiveEntry();
            }

            {
                ZipArchiveEntry certificateEntry = new ZipArchiveEntry(name + ".crt");
                certificateEntry.setSize(key.getCertificate().getBytes(StandardCharsets.UTF_8).length);
                zipArchiveOutputStream.putArchiveEntry(certificateEntry);
                zipArchiveOutputStream.write(key.getCertificate().getBytes(StandardCharsets.UTF_8));
                zipArchiveOutputStream.closeArchiveEntry();
            }

            {
                ZipArchiveEntry privateKeyEntry = new ZipArchiveEntry(name + ".pem");
                privateKeyEntry.setSize(key.getPrivateKey().getBytes(StandardCharsets.UTF_8).length);
                zipArchiveOutputStream.putArchiveEntry(privateKeyEntry);
                zipArchiveOutputStream.write(key.getPrivateKey().getBytes(StandardCharsets.UTF_8));
                zipArchiveOutputStream.closeArchiveEntry();
            }

            {
                PrivateKey privateKey = PrivateKeyUtils.read(key.getPrivateKey());
                String o = writeOpenSSL(privateKey);
                ZipArchiveEntry privateKeyEntry = new ZipArchiveEntry(name + "-openssl.pem");
                privateKeyEntry.setSize(o.getBytes(StandardCharsets.UTF_8).length);
                zipArchiveOutputStream.putArchiveEntry(privateKeyEntry);
                zipArchiveOutputStream.write(o.getBytes(StandardCharsets.UTF_8));
                zipArchiveOutputStream.closeArchiveEntry();
            }


            byte[] p12Data = null;
            {
                KeyStore store = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);
                store.load(null, changeit.toCharArray());
                java.security.cert.Certificate[] chain = new java.security.cert.Certificate[1];
                chain[0] = CertificateUtils.read(key.getCertificate());

                PrivateKey privateKey = PrivateKeyUtils.read(key.getPrivateKey());

                store.setKeyEntry(name, privateKey, changeit.toCharArray(), chain);
                ByteArrayOutputStream p12 = new ByteArrayOutputStream();
                store.store(p12, changeit.toCharArray());
                p12.close();
                p12Data = p12.toByteArray();
            }

            {
                ZipArchiveEntry p12Entry = new ZipArchiveEntry(name + ".p12");
                p12Entry.setSize(p12Data.length);
                zipArchiveOutputStream.putArchiveEntry(p12Entry);
                zipArchiveOutputStream.write(p12Data);
                zipArchiveOutputStream.closeArchiveEntry();
            }

            zipArchiveOutputStream.close();

            IResourceStream resourceStream = new MemoryResourceStream("application/zip", data.toByteArray());
            getRequestCycle().scheduleRequestHandlerAfterCurrent(
                    new ResourceStreamRequestHandler(resourceStream) {
                        @Override
                        public void respond(IRequestCycle requestCycle) {
                            super.respond(requestCycle);
                        }
                    }.setFileName(uuid + ".zip")
                            .setContentDisposition(ContentDisposition.INLINE)
                            .setCacheDuration(Duration.ZERO));

        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    protected void createButtonClick() {
        try {
            ApplicationContext context = WicketFactory.getApplicationContext();
            UserRepository userRepository = context.getBean(UserRepository.class);
            KeyRepository keyRepository = context.getBean(KeyRepository.class);

            Optional<User> optionalUser = userRepository.findById(getSession().getUserId());
            User user = optionalUser.orElseThrow(() -> new WicketRuntimeException(""));

            PkiApiConfiguration pkiApiConfiguration = context.getBean(PkiApiConfiguration.class);

            PrivateKey issuerPrivateKey = PrivateKeyUtils.read(pkiApiConfiguration.getPrivateKey());
            X509Certificate issuerCertificate = CertificateUtils.read(pkiApiConfiguration.getCertificate());

            KeyPair keyPair = KeyPairUtility.generate();
            X500Name subject = SubjectUtility.generate(null, null, null, user.getDisplayName(), null, null, user.getEmailAddress());
            PKCS10CertificationRequest csr = CertificationSignRequestUtility.generate(keyPair.getPrivate(), keyPair.getPublic(), subject);

            LocalDate validFrom = LocalDate.now();
            LocalDate validUntil = validFrom.plusYears(1);

            CertificateRequestDto requestDto = new CertificateRequestDto();
            requestDto.setBasicConstraints(false);
            requestDto.setCsr(csr);
            requestDto.setIssuerCertificate(issuerCertificate);
            requestDto.setIssuerPrivateKey(issuerPrivateKey);
            requestDto.setDuration(Days.daysBetween(validFrom, validUntil).getDays());
            requestDto.setSerial(System.currentTimeMillis());

            requestDto.setBasicConstraintsCritical(true);
            requestDto.setKeyUsageCritical(true);

            requestDto.setSubjectAlternativeNameCritical(false);

            requestDto.setSubjectKeyIdentifierCritical(false);
            requestDto.setAuthorityKeyIdentifierCritical(false);
            requestDto.setAuthorityInfoAccessCritical(false);

            requestDto.setExtendedKeyUsageCritical(false);

            requestDto.setcRLDistributionPointsCritical(false);

            X509Certificate x509Certificate = CertificateUtility.generate(requestDto);

            Key key = new Key();
            key.setUser(user);
            key.setName(this.nameValue);
            key.setEnabled(true);
            key.setPrivateKey(PrivateKeyUtils.write(keyPair.getPrivate()));
            key.setLastSeen(LocalDate.now().toDate());
            key.setCertificate(CertificateUtils.write(x509Certificate));

            keyRepository.save(key);

            setResponsePage(MyKeyPage.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected List<ActionItem> key_browse_action_link(String link, Tuple model) {
        boolean enabled = model.get("enabled", boolean.class);
        List<ActionItem> actions = new ArrayList<>();
        if (!enabled) {
            actions.add(new ActionItem("Enable", Model.of("Enable"), ItemCss.DANGER));
        } else {
            actions.add(new ActionItem("Disable", Model.of("Disable"), ItemCss.DANGER));
        }
        return actions;
    }

    protected void key_browse_action_click(String link, Tuple model, AjaxRequestTarget target) {
        ApplicationContext context = WicketFactory.getApplicationContext();
        KeyRepository keyRepository = context.getBean(KeyRepository.class);

        long id = model.get("id", long.class);

        Optional<Key> optionalKey = keyRepository.findById(id);
        Key key = optionalKey.orElseThrow(() -> new WicketRuntimeException(""));

        if ("Enable".equals(link)) {
            key.setEnabled(true);
            keyRepository.save(key);
            setMessage(String.format("Enabled key [%s]", model.get("name")));
            target.add(getMessageContainer());
            target.add(this.keyBrowseTable);
        } else if ("Disable".equals(link)) {
            key.setEnabled(false);
            keyRepository.save(key);

            setMessage(String.format("Disabled key [%s]", model.get("name")));
            target.add(getMessageContainer());
            target.add(this.keyBrowseTable);
        }

    }

    protected static String writeOpenSSL(PrivateKey privateKey) throws IOException {
        StringWriter pem = new StringWriter();
        JcaPEMWriter writer = new JcaPEMWriter(pem);

        try {
            writer.writeObject(privateKey);
        } catch (Throwable var6) {
            try {
                writer.close();
            } catch (Throwable var5) {
                var6.addSuppressed(var5);
            }

            throw var6;
        }

        writer.close();
        return pem.toString();
    }

}
