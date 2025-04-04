/*
 * Created on 20 aout 2003
 */
package org.javlo.context;
  
import java.io.Serializable;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.title.Heading;
import org.javlo.config.StaticConfig;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.ClipBoard;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;
import org.javlo.user.UserEditFilter;
import org.javlo.user.UserInfo;

/**
 * @author pvanderm
 */
public class EditContext implements Serializable {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(EditContext.class.getName());

	private static final List<String> EDIT_LANGUAGE = Arrays.asList(new String[] { "en", "fr" });

	public static final int CONTENT_VIEW = 1;

	public static final int USER_VIEW = 2;

	public static final int STAT_VIEW = 3;

	public static final int STATIC_VIEW = 4;

	public static final int MAILING_VIEW = 5;

	public static final int ADMIN_USER_VIEW = 6;

	public static final int DATA_VIEW = 7;

	static final String STATIC_DIR_KEY = "static_directory";

	static final String IMAGES_DIR_KEY = "images_directory";

	static final String TEASERS_DIR_KEY = "teasers_directory";

	static final String FILES_DIR_KEY = "files_directory";

	static final String USER_STATIC_DIR_KEY = "user_static_directory";

	static final String GALLERY_DIR_KEY = "gallery.dir";

	static final String FLASH_DIR_KEY = "flash.dir";

	static final String MENU_CUT_DIR_KEY = "menu.title-size";

	static final String USER_ROLES_KEY = "user_roles";

	static final String USER_ROLES_DEFAULT_KEY = "user_roles_default";

	static final String DEFAULT_VIEW_LANGUAGE_KEY = "default_view_language";

	static final String EDIT_TEMPLATE_KEY = "edit_template";

	static final String LICENCE_KEY = "licence";

	public static final String USER_ROLES_SEPARATOR = ";";

	String activeType = Heading.TYPE; // TODO: clean this init.

	String teasersDirectory = null;

	String defaultViewLanguage = null;

	// static final HashSet<String> adminUserRoles = new
	// HashSet<String>(Arrays.asList(new String[] {
	// AdminUserSecurity.FULL_CONTROL_ROLE, AdminUserSecurity.CONTENT_ROLE,
	// AdminUserSecurity.REMOVE_STATIC_ROLE, AdminUserSecurity.NAVIGATION_ROLE,
	// AdminUserSecurity.ADD_NAVIGATION_ROLE, AdminUserSecurity.USER_ROLE,
	// AdminUserSecurity.ADMIN_USER_ROLE, AdminUserSecurity.STATISTICS_ROLE,
	// AdminUserSecurity.PUBLISHER_ROLE, AdminUserSecurity.MAILING_ROLE,
	// AdminUserSecurity.MACRO_ROLE, AdminUserSecurity.WEBDESGIN_ROLE,
	// AdminUserSecurity.LIGHT_INTERFACE_ROLE, AdminUserSecurity.SYNCHRO_CLIENT,
	// AdminUserSecurity.SYNCHRO_ADMIN, AdminUserSecurity.SYNCHRO_SERVER }));

	static final HashSet<String> adminUserRoles = new HashSet<String>(Arrays.asList(new String[] { AdminUserSecurity.FULL_CONTROL_ROLE, AdminUserSecurity.CONTENT_ROLE, AdminUserSecurity.MODEL_ROLE, AdminUserSecurity.CONTRIBUTOR_ROLE, AdminUserSecurity.DESIGN_ROLE, AdminUserSecurity.NAVIGATION_ROLE, AdminUserSecurity.USER_ROLE, AdminUserSecurity.ADMIN_USER_ROLE, AdminUserSecurity.STATISTICS_ROLE, AdminUserSecurity.PUBLISHER_ROLE, AdminUserSecurity.MACRO_ROLE, AdminUserSecurity.MAILING_ROLE, AdminUserSecurity.LIGHT_INTERFACE_ROLE, AdminUserSecurity.SYNCHRO_CLIENT, AdminUserSecurity.SYNCHRO_ADMIN, AdminUserSecurity.SYNCHRO_SERVER, AdminUserSecurity.UPLOAD_RESOURCE, AdminUserSecurity.VALIDATION_ROLE, AdminUserSecurity.ADD_ONLY, AdminUserSecurity.VIEW_ACCESS, AdminUserSecurity.CUSTOMER_ROLE, AdminUserSecurity.PROVIDER_ROLE }));

	Set<String> userRolesDefault = new HashSet<String>(Arrays.asList(new String[] { "guest" }));

	Set<String> licence = new HashSet<String>();

	String editTemplate = "index.jsp";

	String messageTemplate = "message.jsp";

	String boxTemplate = "box.jsp";

	String breadcrumbsTemplate = "breadcrumbs.jsp";

	String loginRenderer = "login.jsp";

	String ajaxRenderer = null;

	private String currentArea = ComponentBean.DEFAULT_AREA;

	UserEditFilter userEditFilter = new UserEditFilter();

	UserEditFilter adminUserEditFilter = new UserEditFilter();

	boolean previewEditionMode = true;

	// int menuCut = 18;

	ContentContextBean contextForCopy = null;

	Map<String, Set<String>> filters = new HashMap<String, Set<String>>();

	String filter = null;

	static final String SESSION_KEY = "__edit__context__";

	static final String[][] views = { { "" + CONTENT_VIEW, "content" }, { "" + USER_VIEW, "user" }, { "" + ADMIN_USER_VIEW, "user-admin" }, { "" + STAT_VIEW, "stat" }, { "" + STATIC_VIEW, "static" }, { "" + DATA_VIEW, "data" } };

	private User editUser = null;

	private AdminUserSecurity adminUserSecurity = null;

	private StaticConfig staticConfig = null;

	private GlobalContext globalContext = null;

	private boolean mailing = false;

	private String mainRenderer = null;

	private String commandRenderer = null;

	private boolean viewCommand = true;

	private boolean viewComponent = true;

	private boolean viewMode = true;

	private Boolean lightInterface = null;

	private EditContext(GlobalContext globalContext, HttpSession session) {
		ServletContext servletContext = session.getServletContext();
		staticConfig = StaticConfig.getInstance(servletContext);

		// TODO: reload properties when updated in admin
		StaticConfig staticConfig = StaticConfig.getInstance(session);
		Properties staticProps = staticConfig.getProperties();

		String userRolesRaw = staticProps.getProperty(USER_ROLES_KEY);
		if (userRolesRaw != null && globalContext != null) {
			String[] userRoles = userRolesRaw.split(USER_ROLES_SEPARATOR);
			if (globalContext.getUserRoles().size() == 0) {
				Set<String> roleSet = new HashSet<String>(Arrays.asList(userRoles));
				globalContext.setUserRoles(roleSet); // set default value of
														// userRole
			}
		}

		String userRolesDefaultRaw = staticProps.getProperty(USER_ROLES_DEFAULT_KEY);
		if (userRolesDefaultRaw != null) {
			userRolesDefault = new HashSet<String>(Arrays.asList(userRolesDefaultRaw.split(USER_ROLES_SEPARATOR)));
		}

		String defaultEditTemplate = staticProps.getProperty(EDIT_TEMPLATE_KEY);
		if (defaultEditTemplate != null) {
			editTemplate = defaultEditTemplate;
		}

		String defaultLg = staticProps.getProperty(DEFAULT_VIEW_LANGUAGE_KEY);
		if (defaultLg != null) {
			defaultViewLanguage = defaultLg;
		} else if (globalContext != null) {
			defaultViewLanguage = globalContext.getDefaultLanguages().iterator().next();
		} else {
			defaultViewLanguage = "en";
		}

		String rawLicence = staticProps.getProperty(LICENCE_KEY);
		if ((rawLicence != null) && (rawLicence.trim().length() > 0)) {
			String[] arrayLicence = rawLicence.split(",");
			for (String element : arrayLicence) {
				licence.add(element);
				logger.fine("licence found : " + element);
			}
		}

		adminUserSecurity = AdminUserSecurity.getInstance();
	};

	public static final EditContext getInstance(GlobalContext globalContext, HttpSession session) {
		String key = SESSION_KEY + globalContext.getContextKey();
		EditContext editCtx = (EditContext) session.getAttribute(key);
		if (editCtx == null) {
			editCtx = new EditContext(globalContext, session);
			session.setAttribute(key, editCtx);
			editCtx.globalContext = globalContext;
		}
		return editCtx;
	}

	/**
	 * @return
	 */
	public String getActiveType() {
		return activeType;
	}

	/**
	 * @param string
	 */
	public void setActiveType(String string) {
		activeType = string;
	}

	/**
	 * @return
	 */
	public String getTeasersDirectory() {
		return teasersDirectory;
	}

	/**
	 * @param string
	 */
	public void setTeasersDirectory(String string) {
		teasersDirectory = string;
	}

	/**
	 * @return
	 */
	public String getFilter() {
		return filter;
	}

	/**
	 * @param string
	 */
	public void setFilter(String string) {
		filter = string;
	}

	/**
	 * @return
	 */
	public ContentContext getContextForCopy(ContentContext ctx) {
		if (contextForCopy == null) {
			return null;
		} else {
			ContentContext out = contextForCopy.createContentContext(ctx);
			try {
				if (out.getCurrentPage() == null) {
					contextForCopy = null;
					out = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				contextForCopy = null;
				out = null;
			}
			return out;
		}
	}

	/**
	 * @param string
	 * @throws Exception
	 */
	public void setPathForCopy(ContentContext ctx) throws Exception {
		if (ctx == null) {
			contextForCopy = null;
		} else {
			ClipBoard.getInstance(ctx.getRequest()).clear();
			contextForCopy = new ContentContextBean(ctx);
		}
	}

	/**
	 * @return
	 */
	public Set<String> getUserRoles() {
		if (globalContext == null) {
			return new HashSet<String>();
		}
		Set<String> roles = globalContext.getUserRoles();
		return roles;
	}

	/**
	 * @return
	 */
	public int getCurrentAdminView() {
		return 1;
	}

	/**
	 * @return
	 */
	public UserEditFilter getUserEditFilter() {
		return userEditFilter;
	}

	public String[][] getViews() {
		return views;
	}

	public String[][] getAdminViews() {
		return new String[][] { { "1", "global" } };
	}

	/**
	 * @return Returns the defaultViewLanguage.
	 */
	public String getDefaultViewLanguage() {
		return defaultViewLanguage;
	}

	public String[] getActions(GlobalContext globalContext) {
		String[] result = new String[0];
		if (globalContext.isMailing()) {
			result = new String[] { "mailing" };
		}
		return result;

	}

	public boolean haveLicence(String module) {
		return licence.contains(module);
	}
	
	public User getEditUser(String login) {
		User user = staticConfig.getEditUsers().get(login);
		return user;
	}

	/**
	 * log user with user define in config file and not in user interface.
	 * 
	 * @param inUser
	 * @param inPassword
	 * @return true if user is logged.
	 */
	public boolean hardLogin(String inUser, String inPassword) {
		if (StringHelper.isEmpty(inPassword)) {
			return false;
		}

		String pwd = inPassword;
		pwd = StringHelper.encryptPassword(pwd);

		logger.info("try hard login : " + inUser);

		User outUser = getEditUser(inUser);
		if ((outUser != null && globalContext != null)) {
			if (!pwd.equals(outUser.getPassword())) {
				outUser = null;
			}
		}
		// editUser=outUser;
		if (outUser != null) {
			UserInfo ui = new UserInfo();			
			ui.setLogin(outUser.getName());
			ui.setRoles(new HashSet<String>(Arrays.asList(new String[] { AdminUserSecurity.GENERAL_ADMIN, AdminUserSecurity.FULL_CONTROL_ROLE })));
			editUser = new User(ui);
		}
		return outUser != null;
	}

	public User hardLoginByToken(String token) {
		if (StringHelper.isEmpty(token)) {
			return null;
		}
		logger.info("try hard login by token.");
		for (User user : staticConfig.getEditUsers().values()) {
			if (user != null && user.getUserInfo().getToken().equals(token)) {
				IUserInfo ui = user.getUserInfo();
				ui.setRoles(new HashSet<String>(Arrays.asList(new String[] { AdminUserSecurity.GENERAL_ADMIN, AdminUserSecurity.FULL_CONTROL_ROLE })));
				editUser = new User(ui);
				return editUser;
			}
		}
		return null;
	}

	/**
	 * autolog user with user define in WEB.XML and not in user interface.
	 * 
	 * @param inUser
	 * @return true if user is logged.
	 */
	public boolean hardAutoLogin(String inUser) {
		User outUser = getEditUser(inUser);
		if (outUser != null) {
			UserInfo ui = new UserInfo();
			ui.setLogin(outUser.getName());
			ui.setRoles(new HashSet<String>(Arrays.asList(new String[] { AdminUserSecurity.GENERAL_ADMIN, AdminUserSecurity.FULL_CONTROL_ROLE })));
			editUser = new User(ui);
		}
		return outUser != null;
	}

	public void logout() {
		if (editUser != null) {
			logger.info(editUser.getName() + " logout.");
		}
		editUser = null;
	}

	public Principal getUserPrincipal() {
		return editUser;
	}

	public Set<String> getUserRolesDefault() {
		return userRolesDefault;
	}

	public String getEditTemplate() {
		return URLHelper.mergePath(staticConfig.getEditTemplateFolder(), editTemplate);
	}

	public String getMessageTemplate() {
		return URLHelper.mergePath(staticConfig.getEditTemplateFolder(), messageTemplate);
	}

	public String getBoxTemplate() {
		return URLHelper.mergePath(staticConfig.getEditTemplateFolder(), boxTemplate);
	}

	public String getBreadcrumbsTemplate() {
		return URLHelper.mergePath(staticConfig.getEditTemplateFolder(), breadcrumbsTemplate);
	}

	public void setMessageTemplate(String messageTemplate) {
		this.messageTemplate = messageTemplate;
	}

	public String getLoginRenderer() {
		return URLHelper.mergePath(staticConfig.getEditTemplateFolder(), loginRenderer);
	}

	public void setLoginRenderer(String loginRenderer) {
		this.loginRenderer = loginRenderer;
	}

	public String getAjaxRenderer() {
		return ajaxRenderer;
	}

	public void setAjaxRenderer(String ajaxRenderer) {
		this.ajaxRenderer = ajaxRenderer;
	}

	public UserEditFilter getAdminUserEditFilter() {
		return adminUserEditFilter;
	}

	public Set<String> getDefaultAdminUserRoles() {
		return adminUserRoles;
	}

	public Set<String> getAdminUserRoles() {
		if (globalContext == null) {
			return adminUserRoles;
		}
		Set<String> roles = globalContext.getAdminUserRoles();
		Set<String> outRoles = new HashSet<String>();
		for (String role : adminUserRoles) {
			outRoles.add(role);
		}
		for (String role : roles) {
			outRoles.add(role);
		}
		return outRoles;
	}

	public User getEditUser() {
		return editUser;
	}

	public void setEditUser(User editUser) {
		this.editUser = editUser;
	}

	public boolean isMailing() {
		return mailing;
	}

	public void setMailing(boolean mailing) {
		this.mailing = mailing;
	}

	public String getMainRenderer() {
		return mainRenderer;
	}

	public void setMainRenderer(String mainRenderer) {
		this.mainRenderer = mainRenderer;
	}

	public String getCommandRenderer() {
		return commandRenderer;
	}

	public void setCommandRenderer(String commandRenderer) {
		this.commandRenderer = commandRenderer;
	}

	public boolean isViewCommand() {
		return viewCommand;
	}

	public void setViewCommand(boolean viewCommand) {
		this.viewCommand = viewCommand;
	}

	public boolean isViewComponent() {
		return viewComponent;
	}

	public void setViewComponent(boolean viewComponent) {
		this.viewComponent = viewComponent;
	}

	public boolean isViewMode() {
		return viewMode;
	}

	public void setViewMode(boolean viewMode) {
		this.viewMode = viewMode;
	}

	public String getCurrentArea() {
		return currentArea;
	}

	public void setCurrentArea(String currentArea) {
		this.currentArea = currentArea;
	}

	public List<String> getEditLanguages() {
		return EDIT_LANGUAGE;
	}

	public boolean isLightInterface() {
		if (lightInterface == null) {
			return adminUserSecurity.haveRole(editUser, AdminUserSecurity.LIGHT_INTERFACE_ROLE);
		} else {
			return lightInterface;
		}
	}

	public void setLightInterface(Boolean isLightInterface) {
		this.lightInterface = isLightInterface;
	}

	public boolean isPreviewEditionMode() {
		if (getEditUser() == null) {
			return false;
		} else {
			if (!globalContext.isPreviewMode()) {
				return true;
			} else {
				return previewEditionMode;
			}
		}
	}

	public void setPreviewEditionMode(boolean editPreview) {
		this.previewEditionMode = editPreview;
	}
}
