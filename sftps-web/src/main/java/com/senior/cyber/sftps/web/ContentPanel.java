package com.senior.cyber.sftps.web;

import com.senior.cyber.sftps.web.factory.WebSession;
import com.senior.cyber.frmk.common.wicket.extensions.markup.html.tabs.Tab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Map;

public abstract class ContentPanel extends com.senior.cyber.frmk.common.wicket.extensions.markup.html.tabs.ContentPanel {

    public ContentPanel(String id, String name, TabbedPanel<Tab> containerPanel, Map<String, Object> data) {
        super(id, name, containerPanel, data);
    }

    @Override
    public WebSession getSession() {
        return (WebSession) super.getSession();
    }

    public final void setMessage(String message) {
        getSession().setAttribute(MasterPage.MESSAGE_KEY, message);
    }

    public final WebMarkupContainer getMessageContainer() {
        return ((MasterPage) getPage()).getMessageContainer();
    }

    protected PageParameters getPageParameters() {
        return getPage().getPageParameters();
    }

}
