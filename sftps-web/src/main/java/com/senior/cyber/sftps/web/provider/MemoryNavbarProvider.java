//package com.senior.cyber.sftps.web.provider;
//
//import com.senior.cyber.jdbc.query.SelectQuery;
//import com.senior.cyber.sftps.dao.meta.User;
//import com.senior.cyber.sftps.web.boot.AppProperties;
//import com.senior.cyber.sftps.web.factory.WebSession;
//import com.senior.cyber.frmk.common.model.Navbar;
//import com.senior.cyber.frmk.common.model.menu.left.TopLeftMenu;
//import com.senior.cyber.frmk.common.model.menu.left.TopLeftMenuItem;
//import com.senior.cyber.frmk.common.provider.INavbarProvider;
//import org.apache.metamodel.DataContext;
//import org.springframework.context.ApplicationContext;
//import org.springframework.dao.EmptyResultDataAccessException;
//import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class MemoryNavbarProvider implements INavbarProvider {
//
//    private WebSession session;
//
//    public MemoryNavbarProvider(WebSession session) {
//        this.session = session;
//    }
//
//    @Override
//    public Navbar fetchNavbar() {
//        ApplicationContext context = com.senior.cyber.frmk.common.base.WicketFactory.getApplicationContext();
//        DataContext dataContext = context.getBean(DataContext.class);
//        NamedParameterJdbcTemplate named = context.getBean(NamedParameterJdbcTemplate.class);
//        User userTable = User.staticInitialize(dataContext);
//        AppProperties properties = context.getBean(AppProperties.class);
//
//        Navbar navbar = new Navbar();
//        navbar.setSearchable(false);
//
//        SelectQuery selectQuery = new SelectQuery(userTable.getName());
//        selectQuery.addWhere(userTable.USER_ID.getName() + " = :user_id", session.getUserId());
//
//        Map<String, Object> userObject = null;
//        try {
//            userObject = named.queryForMap(selectQuery.toSQL(), selectQuery.toParam());
//        } catch (EmptyResultDataAccessException e) {
//        }
//
//        String welcome = "N/A";
//        if (userObject != null) {
//            String displayName = (String) userObject.get(userTable.DISPLAY_NAME.getName());
//            String emailAddress = (String) userObject.get(userTable.EMAIL_ADDRESS.getName());
//            if (displayName == null || "".equals(displayName)) {
//                welcome = emailAddress.substring(0, emailAddress.indexOf("@"));
//            } else {
//                welcome = displayName;
//            }
//        }
//
//        TopLeftMenuItem welcomeMenu = new TopLeftMenuItem(String.format("Welcome [%s]", welcome));
//        List<TopLeftMenu> leftMenu = new ArrayList<>();
//        leftMenu.add(welcomeMenu);
//        navbar.setLeft(leftMenu);
//
//        return navbar;
//    }
//
//}
