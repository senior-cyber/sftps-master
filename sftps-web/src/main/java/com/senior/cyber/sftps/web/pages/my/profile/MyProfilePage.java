package com.senior.cyber.sftps.web.pages.my.profile;

import com.senior.cyber.sftps.dao.entity.Role;
import com.senior.cyber.sftps.web.pages.MasterPage;
import com.senior.cyber.frmk.common.base.Bookmark;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.tabs.Tab;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;

import java.util.ArrayList;
import java.util.List;

@Bookmark("/my/profile")
@AuthorizeInstantiation({Role.NAME_ROOT, Role.NAME_Page_MyProfile})
public class MyProfilePage extends MasterPage {

    protected TabbedPanel tabs;

    protected Tab info_tab;

    protected Tab pwd_tab;

    protected Tab webhook_tab;

    protected Tab secret_tab;

    @Override
    protected void onInitData() {
        super.onInitData();
        this.info_tab = new Tab("info", "My Info", MyProfilePageInfoTab.class);
        this.pwd_tab = new Tab("pwd", "Password", MyProfilePagePwdTab.class);
        this.webhook_tab = new Tab("webhook", "Web Hook", MyProfilePageWebHookTab.class);
        this.secret_tab = new Tab("secret", "Secret", MyProfilePageSecretTab.class);
    }

    @Override
    protected void onInitHtml(MarkupContainer body) {
        List<Tab> tabs = new ArrayList<>();
        if (this.info_tab != null) {
            tabs.add(this.info_tab);
        }
        if (this.pwd_tab != null) {
            tabs.add(this.pwd_tab);
        }
        if (this.webhook_tab != null) {
            tabs.add(this.webhook_tab);
        }
        if (this.secret_tab != null) {
            tabs.add(this.secret_tab);
        }
        this.tabs = new TabbedPanel("tabs", tabs);
        body.add(this.tabs);
    }

}
