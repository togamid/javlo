package org.javlo.user;

import javax.mail.internet.InternetAddress;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Set;

public interface IUserInfo {

	public static final char ROLES_SEPARATOR = ';';
	public static final String PREFERRED_LANGUAGE_SEPARATOR = ",";

	public String getLogin();
	
	/**
	 * return the encrypt login the encrypt login can change when we restart. 
	 */
	public String getEncryptLogin();
	
	/**
	 * get the parent of the user, a parent can be a manager.
	 * @return
	 */
	public String getParent();

	public String getTitle(); 

	public String getFirstName();

	public String getLastName();
	
	public String getGender();

	public String getPassword();

	String getNationalRegister();

	void setNationalRegister(String nationalRegister);

	public String getEmail();
	
	public String getUrl();

	public String getInfo();

	public String getToken();
	
	public String getTokenCreateIfNotExist();
	
	public void resetToken();
	
	public String getBirthdate();
	
	public String[] getPreferredLanguage();

	public Set<String> getRoles();

	String getAddress();

	void setAddress(String address);

	String getCity();

	void setCity(String city);

	String getPostCode();

	void setPostCode(String postCode);

	public void setId(String id);

	public void setLogin(String login);

	public void setTitle(String title);

	public void setFirstName(String firstName);

	public void setLastName(String lastName);
	
	public void setPassword(String password);

	public void setEmail(String email);

	public void setInfo(String info);

	public void setToken(String token);

	public void setBirthdate(String date);
	
	public void setPreferredLanguage(String[] preferredLanguage);

	public void setRoles(Set<String> strings);

	public void addRoles(Set<String> strings);
	
	public void removeRoles(Set<String> strings);

	void setCountry(String country);

	String getPhone();

	void setPhone(String phone);

	public void setCreationDate(Date creationDate);

	public Date getCreationDate();

	public void setModificationDate(Date modificationDate);

	public Date getModificationDate();
	
	public void setValue(String field, String value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	public String[] getAllLabels();

	public String[] getAllValues();
	
	public String getAvatarURL();
	
	public void setAvatarURL(String avatarURL);
	
	public String getUserFolder();
	
	public InternetAddress getInternetAddress();
	
	public String encryptPassword(String pwd);
	
	public String getSite();
	
	public String[] getTaxonomy();
	
	public void setTaxonomy(String[] taxonomy);
	
	public void setSite(String site);

	/**
	 * get the type of account (default, facebook, google account...)
	 * @return
	 */
	public String getAccountType();

	/**
	 * define user as outside loged, as facebook or google login.
	 */
	void setExternalLoginUser();

	/**
	 * check if user is logged from external module (as facebook ou google).
	 * @return true if user logged from external module.
	 */
	boolean isExternalLoginUser();
	
	public String getCountry();
	
	public String getOrganization();

}
