package org.javlo.user;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.user.exception.UserAllreadyExistException;

public interface IUserFactory {
	
	public static final String TOKEN_PARAM = "j_token";

	public static final int AUTO_LOGIN_AGE_SEC = 60 * 60 * 24 * 365; // 1 years

	public abstract User getUser(String login);

	public abstract User autoLogin(HttpServletRequest request, String login);
	
	public abstract User adminFakeLogin(HttpServletRequest request, String login);

	public abstract User login(HttpServletRequest request, String login, String password);

	public abstract User login(HttpServletRequest request, String token);

	public abstract void logout(HttpSession session);

	public abstract User getCurrentUser(GlobalContext globalContext, HttpSession session);
	
	public abstract void releaseUserInfoList();

	public abstract void clearUserInfoList();

	public abstract List<IUserInfo> getUserInfoList();

	public abstract void addUserInfo(IUserInfo userInfo) throws UserAllreadyExistException;
	
	/**
	 * if user info found, update data
	 * @param userInfo
	 * @throws UserAllreadyExistException
	 */
	public abstract void addOrModifyUserInfo(IUserInfo userInfo) throws UserAllreadyExistException;

	public abstract void mergeUserInfo(IUserInfo userInfo) throws IOException;

	public abstract void updateUserInfo(IUserInfo userInfo) throws IOException;

	public abstract void deleteUser(String login);

	public abstract void store() throws IOException;

	public abstract IUserInfo createUserInfos();

	public abstract IUserInfo getUserInfos(String id);

	public abstract List<IUserInfo> getUserInfoForRoles(String[] inRoles);
	
	public abstract String getTokenCreateIfNotExist(User user)  throws IOException;
	
	/**
	 * check the login is available
	 * @param ctx
	 * @param login
	 * @return null if ok, error message otherwise
	 */
	public abstract String checkUserAviability(ContentContext ctx, String login);

	/**
	 * get all roles of the user.
	 * @param globalContext
	 * @param session
	 * @return
	 */
	public abstract Set<String> getAllRoles(GlobalContext globalContext, HttpSession session);
	
	public abstract void init(GlobalContext globalContext, HttpSession newSession);

	public abstract void reload(GlobalContext globalContext, HttpSession session);

	/**
	 * check if the user system use standard storage system of wcms
	 * 
	 * @return true if standard system is used, false else a external system is used.
	 */
	public abstract boolean isStandardStorage();

	User getUserByEmail(String email);
	
	public RoleWrapper getRoleWrapper(ContentContext ctx, User user);
	
	public String getSessionKey();

}