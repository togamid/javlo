/*
 * Created on 06-janv.-2004
 */
package org.javlo.component.form;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.StringSecurityUtil;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.mailing.MailConfig;
import org.javlo.mailing.MailService;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;
import org.javlo.user.exception.UserAllreadyExistException;

/**
 * This class is use for register a user :</h4>
 * <ul>
 * <li>{@link Boolean} name : true if contributor need first name and laste name
 * </li>
 * </ul>
 * 
 * @author Patrick Vandermaesen
 */
public class FormMailingComponent extends AbstractVisualComponent implements IAction {

	private boolean needForm = true;

	private static final String DATA_SEPARATOR = "<<>>";
	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(FormMailingComponent.class.getName());

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getType()
	 */
	@Override
	public String getType() {
		return "form-mailing";
	}

	public String _getViewXHTMLCode(ContentContext ctx) throws Exception {
		ctx.getRequest().setAttribute("comp", this);
		needForm = true;
		return executeJSP(ctx, "/jsp/registration/mailing.jsp?comp-id=" + getId());
	}

	/**
	 * 
	 * @see org.javlo.component.AbstractVisualComponent#init(java.lang.String,
	 *      java.lang.String, org.javlo.ContentContext)
	 */
	@Override
	protected void init(ComponentBean bean, ContentContext ctx) throws Exception {
		super.init(bean, ctx);
		needForm = true;
		Form form = FormMailing.getFormRegister(ctx.getRequest().getSession());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IUserFactory fact = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		if (fact.getCurrentUser(globalContext, ctx.getRequest().getSession()) != null) {
			IUserInfo userInfo = fact.getCurrentUser(globalContext, ctx.getRequest().getSession()).getUserInfo();
			form.setValues(BeanHelper.bean2Map(userInfo));
			form.setValue("password2", userInfo.getPassword());
		}
	}

	/*
	 * edit management
	 */

	protected String getInputRolesName() {
		return "__" + getId() + ID_SEPARATOR + "roles";
	}

	protected String getInputNameName() {
		return "__" + getId() + ID_SEPARATOR + "name";
	}

	protected String getInputConfirmEmailName() {
		return "__" + getId() + ID_SEPARATOR + "confirm_email";
	}

	protected String getInputEmailSubjectName() {
		return "__" + getId() + ID_SEPARATOR + "email_subject";
	}

	public boolean isNeedName() {
		if (!getValue().contains(DATA_SEPARATOR)) { // old version of the
													// component
			return true;
		} else {
			return StringHelper.isTrue(getValue().split(DATA_SEPARATOR)[1]);
		}
	}

	public boolean isNeedForm() {
		return needForm;
	}

	public String getConfirmEmail() {
		if (getValue().split(DATA_SEPARATOR).length <= 2) { // old version of
			return null;
		} else {
			String email = getValue().split(DATA_SEPARATOR)[2];
			if (email.trim().length() == 0) {
				return null;
			} else {
				return email;
			}
		}
	}
	
	public String getEmailSubject() {
		if (getValue().split(DATA_SEPARATOR).length <= 3) { // old version
			return "";
		} else {
			String subject = getValue().split(DATA_SEPARATOR)[3];
			if (subject == null) {
				return "";
			} else {
				return subject;
			}
		}
	}
	
	public String getTitle() {
		if (getValue().split(DATA_SEPARATOR).length <= 4) { // old version of
			return null;
		} else {
			String title = getValue().split(DATA_SEPARATOR)[4];
			if (title.trim().length() == 0) {
				return null;
			} else {
				return title;
			}
		}
	}

	/**
	 * @see org.javlo.component.AbstractVisualComponent#getEditXHTMLCode()
	 */
	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
		out.println(getSpecialInputTag());

		out.println(getDebugHeader(ctx));

		out.println("<div class=\"form-group\">");
		out.println("<label for=\"" + getInputName("title") + "\">" + i18n.getText("global.title") + "</label>");
		out.println("<input type=\"text\" id=\"" + getInputName("title") + "\" name=\"" + getInputName("title") + "\" value=\"" + StringHelper.neverEmpty(getTitle(), "") + "\" /> ");
		out.println("</div>");

		out.println("<div class=\"form-group\">");
		out.print("<label for=\"");
		out.print(getInputRolesName());
		out.print("\">");
		out.print(i18n.getText("form.choose-role"));
		out.println("</label>");
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		out.print(XHTMLHelper.getInputMultiSelect(getInputRolesName(), editCtx.getUserRoles(), getRoles(), "form-control"));
		out.println("</div>");

		out.println("<div class=\"checkbox\">");
		String checked = "";
		if (isNeedName()) {
			checked = " checked=\"checked\"";
		}

		out.println("<label for=\"" + getInputNameName() + "\">");
		out.println("<input type=\"checkbox\" id=\"" + getInputNameName() + "\" name=\"" + getInputNameName() + "\"" + checked + " /> ");
		out.print(i18n.getText("content.form-mailing.use-name"));
		out.println("</label>");
		out.println("</div>");

		out.println("<div class=\"form-group\">");
		out.println("<label for=\"" + getInputEmailSubjectName() + "\">");
		out.print(i18n.getText("field.subject"));
		out.println("</label>");
		out.println("<input class=\"form-control\" id=\"" + getInputEmailSubjectName() + "\" name=\"" + getInputEmailSubjectName() + "\" value=\"" + StringHelper.removeTag(getEmailSubject()) + "\" />");
		out.println("</div>");

		out.println("<div class=\"form-group\">");
		out.println("<label for=\"" + getInputConfirmEmailName() + "\">");
		out.print(i18n.getText("content.form-mailing.confirm-email"));
		out.println("</label>");
		out.println("<textarea class=\"form-control\" id=\"" + getInputConfirmEmailName() + "\" rows=\"6\" cols=\"60\" name=\"" + getInputConfirmEmailName() + "\">");
		out.println(StringHelper.escapeHTML(StringHelper.neverEmpty(getConfirmEmail(), "")));
		out.println("</textarea>");
		out.println("</div>");

		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String[] newContent = requestService.getParameterValues(getInputRolesName(), new String[0]);

		boolean needName = requestService.getParameter(getInputNameName(), null) != null;

		String emailConfirm = requestService.getParameter(getInputConfirmEmailName(), "");

		String emailSubject = requestService.getParameter(getInputEmailSubjectName(), null);
		
		String title = requestService.getParameter(getInputName("title"), null);

		if (emailSubject != null) {
			String value = StringHelper.arrayToString(newContent) + DATA_SEPARATOR + needName + DATA_SEPARATOR + emailConfirm + DATA_SEPARATOR + emailSubject + DATA_SEPARATOR + title;
			if (!getValue().equals(value)) {
				setValue(value);
				setModify();
			}
		}
		return null;
	}

	protected String getRolesAsRaw() {
		if (!getValue().contains(DATA_SEPARATOR)) { // old version of the
			// component
			return getValue();
		} else {
			return getValue().split(DATA_SEPARATOR)[0];
		}
	}

	protected Set<String> getRoles() {
		Set<String> roles = new HashSet<String>(StringHelper.stringToCollection(getRolesAsRaw()));
		return roles;
	}

	protected Set<String> getRolesAsArray() {
		return new HashSet<String>(StringHelper.stringToCollection(getRolesAsRaw()));
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("name", isNeedName());
		ctx.getRequest().setAttribute("comp", this);
		ctx.getRequest().setAttribute("title", getTitle());
		needForm = true;
	}

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return true;
	}

	/*
	 * ACTION
	 */

	public static String performSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {

		RequestService requestService = RequestService.getInstance(request);

		String email = requestService.getParameter("email", null);
		String firstName = requestService.getParameter("firstname", null);
		String lastName = requestService.getParameter("lastname", null);
		String compId = requestService.getParameter(COMP_ID_REQUEST_PARAM, null);

		ContentContext ctx = ContentContext.getContentContext(request, response);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
		MessageRepository messageRepository = MessageRepository.getInstance(ctx);

		FormMailingComponent comp = (FormMailingComponent) ContentService.getInstance(request).getComponent(ctx, compId);

		if (!StringHelper.isEmpty(comp.getEmailSubject()) && PatternHelper.MAIL_PATTERN.matcher(email).matches() && !StringHelper.isTrue(request.getAttribute(StringSecurityUtil.REQUEST_ATT_FOR_SECURITY_FORWARD))) {
			
			StringBuffer urlParam = new StringBuffer("?webaction=" + comp.getActionGroupName() + ".submit");
			logger.info("send email confirmation to : " + email + " (" + firstName + ' ' + lastName + ')');
			urlParam.append("&email=" + email);
			if (firstName != null) {
				urlParam.append("&firstname=" + firstName);
			}
			if (lastName != null) {
				urlParam.append("&lastname=" + lastName);
			}
			urlParam.append("&" + COMP_ID_REQUEST_PARAM + '=' + compId);
			urlParam.append("&direct=d");
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext());
			String encodedParam = StringSecurityUtil.encode(urlParam.toString(), staticConfig.getSecretKey());
			ContentContext absURLCtx = new ContentContext(ctx);
			absURLCtx.setAbsoluteURL(true);
			String registerURL = URLHelper.createURL(absURLCtx) + '?' + RequestHelper.CRYPTED_PARAM_NAME + "=" + encodedParam + "#reg_" + comp.getId();
			
			String body = XHTMLHelper.textToXHTML(comp.getConfirmEmail());
			if (firstName != null) {
				body = body.replace("##firstname##", firstName);
				body = body.replace("${firstname}", firstName);
			}
			if (lastName != null) {
				body = body.replace("##lastname##", lastName);
				body = body.replace("${lastname}", lastName);
			}

			String mailBody = XHTMLHelper.createUserMail(ctx, "/images/font/envelope.png", body, "", registerURL, comp.getEmailSubject(), "");
			
			MailService mailService = MailService.getInstance(new MailConfig(globalContext, StaticConfig.getInstance(request.getSession()), null));
			mailService.sendMail(new InternetAddress(globalContext.getAdministratorEmail()), new InternetAddress(email), comp.getEmailSubject(), mailBody, true);

			GenericMessage msg = new GenericMessage(i18nAccess.getContentViewText("user.error.email-send"), GenericMessage.INFO);
			messageRepository.setGlobalMessage(msg);

			comp.needForm = false;

		} else if (comp.getConfirmEmail() == null || StringHelper.isTrue(request.getAttribute(StringSecurityUtil.REQUEST_ATT_FOR_SECURITY_FORWARD))) {
			if (!PatternHelper.MAIL_PATTERN.matcher(email).matches()) {
				GenericMessage msg = new GenericMessage(i18nAccess.getContentViewText("mailing.error.email"), GenericMessage.ERROR);
				messageRepository.setGlobalMessage(msg);
				logger.warning("user not registred : " + email);
				return null;
			}

			IUserInfo userInfo = null;
			IUserFactory fact = UserFactory.createUserFactory(globalContext, request.getSession());
			Collection<IUserInfo> userInfos = fact.getUserInfoList();
			for (IUserInfo iUserInfo : userInfos) {
				String login = email;
				if (iUserInfo.getLogin().equals(login)) {
					Set<String> roles = new HashSet<String>(iUserInfo.getRoles());
					if (roles.containsAll(comp.getRoles())) {
						GenericMessage msg = new GenericMessage(i18nAccess.getContentViewText("user.error.email-allready-exist"), GenericMessage.ERROR);
						messageRepository.setGlobalMessage(msg);
						logger.warning("user not registred : " + email);
						return "";
					} else {
						userInfo = iUserInfo;
					}

				}
			}
			// if (fact.getCurrentUser() == null) {
			boolean update = true;
			if (userInfo == null) {
				update = false;
				userInfo = fact.createUserInfos();
				userInfo.setLogin(email);
			}
			userInfo.addRoles(comp.getRoles());
			// } else {
			// userInfo = fact.getCurrentUser().getUserInfo();
			// }
			userInfo.setEmail(email);
			userInfo.setFirstName(firstName);
			userInfo.setLastName(lastName);
			try {
				if (update) {
					fact.updateUserInfo(userInfo);
				} else {
					fact.addUserInfo(userInfo);
				}
				request.setAttribute(StringHelper.REQUEST_KEY_FORM_VALID, "true");
				GenericMessage msg = new GenericMessage(i18nAccess.getContentViewText("form.message"), GenericMessage.INFO);
				messageRepository.setGlobalMessage(msg);
				logger.info("user registred in '" + globalContext.getContextKey() + "' site : " + email);
				fact.store();
			} catch (UserAllreadyExistException e) {
				GenericMessage msg = new GenericMessage(i18nAccess.getContentViewText("user.error.email-allready-exist"), GenericMessage.ERROR);
				messageRepository.setGlobalMessage(msg);
				logger.warning("user not registred : " + email);
				return "";
			}
			/*
			 * if (fact.getCurrentUser() == null) {
			 * fact.login(GlobalContext.getInstance(request), userInfo.getLogin(),
			 * userInfo.getPassword()); }
			 */

		}

		return null;
	}

	@Override
	public String getActionGroupName() {
		return "mailing-registration";
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

	@Override
	public String getIcon() {
		return "bi bi-envelope";
	}
}
