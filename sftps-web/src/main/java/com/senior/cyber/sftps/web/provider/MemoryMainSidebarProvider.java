package com.senior.cyber.sftps.web.provider;

import com.senior.cyber.sftps.dao.entity.Role;
import com.senior.cyber.sftps.dao.entity.User;
import com.senior.cyber.sftps.web.BootApplication;
import com.senior.cyber.sftps.web.factory.WebSession;
import com.senior.cyber.sftps.web.pages.LogPage;
import com.senior.cyber.sftps.web.pages.LogoutPage;
import com.senior.cyber.sftps.web.pages.group.GroupBrowsePage;
import com.senior.cyber.sftps.web.pages.my.key.MyKeyPage;
import com.senior.cyber.sftps.web.pages.my.profile.MyProfilePage;
import com.senior.cyber.sftps.web.pages.role.RoleBrowsePage;
import com.senior.cyber.sftps.web.pages.session.SessionBrowsePage;
import com.senior.cyber.sftps.web.pages.user.UserBrowsePage;
import com.senior.cyber.sftps.web.pages.user.UserExitPage;
import com.senior.cyber.sftps.web.pages.user.UserSwitchPage;
import com.senior.cyber.sftps.web.repository.UserRepository;
import com.senior.cyber.frmk.common.base.WicketFactory;
import com.senior.cyber.frmk.common.model.Brand;
import com.senior.cyber.frmk.common.model.MainSidebar;
import com.senior.cyber.frmk.common.model.UserPanel;
import com.senior.cyber.frmk.common.model.menu.sidebar.SidebarMenu;
import com.senior.cyber.frmk.common.model.menu.sidebar.SidebarMenuDropdown;
import com.senior.cyber.frmk.common.model.menu.sidebar.SidebarMenuItem;
import com.senior.cyber.frmk.common.provider.IMainSidebarProvider;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class MemoryMainSidebarProvider implements IMainSidebarProvider {

    private final WebSession session;

    public MemoryMainSidebarProvider(WebSession session) {
        this.session = session;
    }

    @Override
    public MainSidebar fetchMainSidebar() {
        ApplicationContext context = WicketFactory.getApplicationContext();
        UserRepository repository = context.getBean(UserRepository.class);

        Roles roles = this.session.getRoles();

        User user = repository.findById(this.session.getUserId()).orElseThrow(() -> new RuntimeException("user not found"));

        Brand brand = new Brand("SftpS Master", new PackageResourceReference(BootApplication.class, "logo.png"), (Class<? extends WebPage>) WebApplication.get().getHomePage());
        UserPanel userPanel = new UserPanel(new PackageResourceReference(BootApplication.class, "user.png"), user.getDisplayName(), MyProfilePage.class);
        List<SidebarMenu> children = new ArrayList<>();

        if (this.session.isSignedIn()) {
            if (roles.hasRole(Role.NAME_ROOT) || roles.hasRole(Role.NAME_Page_UserSwitch)) {
                children.add(new SidebarMenuItem("fas fa-user-secret", "Root", null, UserSwitchPage.class));
            }
        }

        if (this.session.isSignedIn()) {
            List<SidebarMenu> securityMenu = securityMenu(roles);
            if (!securityMenu.isEmpty()) {
                children.add(new SidebarMenuDropdown("fas fas fa-lock", "Security", null, securityMenu));
            }
        }

        if (this.session.isSignedIn()) {
            List<SidebarMenu> administrationMenu = administrationMenu(roles);
            if (!administrationMenu.isEmpty()) {
                children.add(new SidebarMenuDropdown("fas fa-tachometer-alt", "Administration", null, administrationMenu));
            }
        }

        if (this.session.isSignedIn()) {
            if (roles.hasRole(Role.NAME_ROOT) || roles.hasRole(Role.NAME_Page_MyKey)) {
                children.add(new SidebarMenuItem("fas fa-key", "Key", null, MyKeyPage.class));
            }
            if (roles.hasRole(Role.NAME_ROOT) || roles.hasRole(Role.NAME_Page_Log)) {
                children.add(new SidebarMenuItem("fas fa-receipt", "Log", null, LogPage.class));
            }
        }

        if (roles.hasRole(Role.NAME_Page_UserExit)) {
            children.add(new SidebarMenuItem("fas fa-door-open", "Exit", null, UserExitPage.class));
        }

        if (this.session.isSignedIn()) {
            children.add(new SidebarMenuItem("fas fa-sign-out-alt", "Logout", null, LogoutPage.class));
        }

        MainSidebar mainSidebar = new MainSidebar();
        mainSidebar.setBrand(brand);
        mainSidebar.setUserPanel(userPanel);
        mainSidebar.setSidebarMenu(children);
        mainSidebar.setSearchable(false);
        return mainSidebar;
    }

    protected List<SidebarMenu> securityMenu(Roles roles) {
        List<SidebarMenu> children = new ArrayList<>();
        if (roles.hasRole(Role.NAME_ROOT) || roles.hasRole(Role.NAME_Page_GroupBrowse)) {
            children.add(new SidebarMenuItem("fas fa-users", "Group", null, GroupBrowsePage.class));
        }
        if (roles.hasRole(Role.NAME_ROOT) || roles.hasRole(Role.NAME_Page_UserBrowse)) {
            children.add(new SidebarMenuItem("fas fa-user", "User", null, UserBrowsePage.class));
        }
        if (roles.hasRole(Role.NAME_ROOT) || roles.hasRole(Role.NAME_Page_RoleBrowse)) {
            children.add(new SidebarMenuItem("fas fa-universal-access", "Role", null, RoleBrowsePage.class));
        }
        return children;
    }

    protected List<SidebarMenu> administrationMenu(Roles roles) {
        List<SidebarMenu> children = new ArrayList<>();
        if (roles.hasRole(Role.NAME_ROOT) || roles.hasRole(Role.NAME_Page_SessionBrowse)) {
            children.add(new SidebarMenuItem("fas fa-mobile-alt", "Session", null, SessionBrowsePage.class));
        }
        return children;
    }

}
