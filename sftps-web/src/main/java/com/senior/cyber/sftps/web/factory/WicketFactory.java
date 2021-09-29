package com.senior.cyber.sftps.web.factory;

import com.senior.cyber.sftps.web.pages.ErrorPage;
import com.senior.cyber.sftps.web.pages.LoginPage;
import com.senior.cyber.sftps.web.pages.my.key.MyKeyPage;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.markup.html.WebPage;

public class WicketFactory extends com.senior.cyber.frmk.common.base.AuthenticatedWicketFactory {

    @Override
    protected void init() {
        super.init();
        getApplicationSettings().setInternalErrorPage(ErrorPage.class);
        getApplicationSettings().setAccessDeniedPage(ErrorPage.class);
        getApplicationSettings().setPageExpiredErrorPage(ErrorPage.class);
    }

    @Override
    protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass() {
        return WebSession.class;
    }

    @Override
    protected Class<? extends WebPage> getSignInPageClass() {
        return LoginPage.class;
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return MyKeyPage.class;
    }

}
