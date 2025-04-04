package org.javlo.helper;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.javlo.component.core.ComponentContext;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.UserInterfaceContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ClipBoard;
import org.javlo.service.ContentService;
import org.javlo.service.NotificationService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.shared.SharedContentService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.ztatic.FileCache;
import org.javlo.ztatic.StaticContext;

public class LangHelper {

	/**
	 * return the instance of a class.
	 * 
	 * @return list of class returned :
	 *         <ul>
	 *         <li>HttpServletRequest</li>
	 *         <li>HttpServletResponse</li>
	 *         <li>HttpSession</li>
	 *         <li>ServletContext</li>
	 *         <li>StaticConfig</li>
	 *         <li>ContentContext</li>
	 *         <li>GlobalContext</li>
	 *         <li>I18nAccess</li>
	 *         <li>RequestService</li>
	 *         <li>EditContext</li>
	 *         <li>ContentService</li>
	 *         <li>ComponentContext</li>
	 *         <li>MenuElement : return the current page.</li>
	 *         <li>UserFactory</li>
	 *         <li>AdminUserFactory</li>
	 *         <li>AdminUserSecurity</li>
	 *         <li>PageConfiguration</li>
	 *         <li>ModuleContext</li>
	 *         <li>Module : current module.</li>
	 *         <li>MessageRepository</li>
	 *         <li>FileCache</li>
	 *         <li>StaticContext</li>
	 *         <li>UserInterfaceContext</li>
	 *         <li>ClipBoard</li>
	 *         <li>PersistenceService</li>
	 *         <li>User</li>
	 *         <li>AbstractModuleContext : return the current module context</li>
	 *         <li>? extends AbstractModuleContext : instanciate a abstract
	 *         module</li>>
	 *         <li>String : the query parameter (when user make a search)</li>
	 *         <li>NotificationService</li>
	 *         <li>SharedContentService</li>
	 *         </ul>
	 * @throws Exception
	 */
	public static Object smartInstance(HttpServletRequest request, HttpServletResponse response, Class c) throws Exception {
		if (c.equals(HttpServletRequest.class)) {
			return request;
		} else if (c.equals(HttpServletResponse.class)) {
			return response;
		} else if (c.equals(HttpServletResponse.class)) {
			return response;
		} else if (c.equals(HttpSession.class)) {
			return request.getSession();
		} else if (c.equals(ServletContext.class)) {
			return request.getSession().getServletContext();
		} else if (c.equals(ContentContext.class)) {
			return ContentContext.getContentContext(request, response);
		} else if (c.equals(GlobalContext.class)) {
			return GlobalContext.getInstance(request);
		} else if (c.equals(StaticConfig.class)) {
			return StaticConfig.getInstance(request.getSession());
		} else if (c.equals(I18nAccess.class)) {
			return I18nAccess.getInstance(ContentContext.getContentContext(request, response));
		} else if (c.equals(RequestService.class)) {
			return RequestService.getInstance(request);
		} else if (c.equals(EditContext.class)) {
			return EditContext.getInstance(GlobalContext.getInstance(request), request.getSession());
		} else if (c.equals(ContentService.class)) {
			return ContentService.getInstance(GlobalContext.getInstance(request));
		} else if (c.equals(ComponentContext.class)) {
			return ComponentContext.getInstance(request);
		} else if (c.equals(MenuElement.class)) {
			return ContentContext.getContentContext(request, response).getCurrentPage();
		} else if (c.equals(UserFactory.class)) {
			return UserFactory.createUserFactory(GlobalContext.getInstance(request), request.getSession());
		} else if (c.equals(AdminUserFactory.class)) {
			return AdminUserFactory.createUserFactory(GlobalContext.getInstance(request), request.getSession());
		} else if (c.equals(AdminUserSecurity.class)) {
			return AdminUserSecurity.getInstance();
		} else if (c.equals(ModulesContext.class)) {
			return ModulesContext.getInstance(request.getSession(), GlobalContext.getInstance(request));
		} else if (c.equals(Module.class)) {
			return ModulesContext.getInstance(request.getSession(), GlobalContext.getInstance(request)).getCurrentModule();
		} else if (c.equals(MessageRepository.class)) {
			return MessageRepository.getInstance(request);
		} else if (c.equals(FileCache.class)) {
			return FileCache.getInstance(request.getSession().getServletContext());
		} else if (c.equals(StaticContext.class)) {
			return StaticContext.getInstance(request.getSession());
		} else if (c.equals(ClipBoard.class)) {
			return ClipBoard.getInstance(request);
		} else if (c.equals(PersistenceService.class)) {
			return PersistenceService.getInstance(GlobalContext.getInstance(request));
		} else if (c.equals(UserInterfaceContext.class)) {
			return UserInterfaceContext.getInstance(request.getSession(), GlobalContext.getInstance(request));
		} else if (c.equals(String.class)) {
			return RequestService.getInstance(request).getParameter("query", null);
		} else if (c.equals(AbstractModuleContext.class)) {
			return AbstractModuleContext.getCurrentInstance(request.getSession());
		} else if (AbstractModuleContext.class.isAssignableFrom(c)) {
			return AbstractModuleContext.getInstance(request.getSession(), GlobalContext.getInstance(request), ModulesContext.getInstance(request.getSession(), GlobalContext.getInstance(request)).getCurrentModule(), c);
		} else if (c.equals(User.class)) {
			return EditContext.getInstance(GlobalContext.getInstance(request), request.getSession()).getEditUser();
		} else if (c.equals(NotificationService.class)) {
			return NotificationService.getInstance(GlobalContext.getInstance(request));
		} else if (c.equals(SharedContentService.class)) {
			return SharedContentService.getInstance(ContentContext.getContentContext(request, response));
		}
		return null;
	}

	public static class MapEntry {
		private final String name;
		private final Object value;

		public MapEntry(String name, Object value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public Object getValue() {
			return value;
		}

	}

	public static MapEntry entry(String key, Object value) {
		return new MapEntry(key, value);
	}

	public static Map<String, Object> obj(MapEntry... props) {
		Map<String, Object> out = new LinkedHashMap<String, Object>();
		for (MapEntry prop : props) {
			out.put(prop.getName(), prop.getValue());
		}
		return out;
	}

	public static Map<String, String> objStr(MapEntry... props) {
		Map<String, String> out = new LinkedHashMap<String, String>();
		for (MapEntry prop : props) {
			if (prop.getValue() != null) {
				out.put(prop.getName(), "" + prop.getValue());
			} else {
				out.put(prop.getName(), null);
			}
		}
		return out;
	}

	public static void putAllIfNotExist(Map map1, Map map2) {
		for (Object key : map2.keySet()) {
			if (!map1.containsKey(key)) {
				map1.put(key, map2.get(key));
			}
		}
	}

	public static ObjectBuilder object() {
		return new ObjectBuilder();
	}

	public static ListBuilder list() {
		return new ListBuilder();
	}

	public static class ObjectBuilder {

		Map<String, Object> map = new LinkedHashMap<String, Object>();

		public ObjectBuilder prop(String name, Object value) {
			map.put(name, value);
			return this;
		}

		public ObjectBuilder child(String name) {
			ObjectBuilder child = new ObjectBuilder();
			prop(name, child.getMap());
			return child;
		}

		public ListBuilder list(String name) {
			ListBuilder child = new ListBuilder();
			prop(name, child.getList());
			return child;
		}

		public Map<String, Object> getMap() {
			return map;
		}

	}

	public static final int unsigned(byte b) {
		return b & 0xff;
	}

	public static class ListBuilder {

		List<Object> list = new LinkedList<Object>();

		public ListBuilder add(Object value) {
			list.add(value);
			return this;
		}

		public ObjectBuilder addObject() {
			ObjectBuilder item = new ObjectBuilder();
			add(item.getMap());
			return item;
		}

		public ListBuilder addList() {
			ListBuilder item = new ListBuilder();
			add(item.getList());
			return item;
		}

		public List<Object> getList() {
			return list;
		}

	}

	/**
	 * save access to array.
	 * 
	 * @param arrays
	 * @param i
	 *            index.
	 * @param defaultValue
	 * @return default value if array is smaller than index.
	 */
	public static Object arrays(Object[] arrays, int i, Object defaultValue) {
		if (arrays.length <= i) {
			return defaultValue;
		} else {
			return arrays[i];
		}
	}

	public static Map<?, ?> collectionToMap(Collection<?> col) {
		Map outMap = new HashMap();
		for (Object object : col) {
			outMap.put(object, object);
		}
		return outMap;
	}

	/**
	 * put item as first of a list
	 * 
	 * @param list
	 * @param item
	 * @return false if item not found in list.
	 */
	public static boolean asFirst(List list, Object item) {
		if (list.contains(item)) {
			list.remove(item);
			list.add(0, item);
			return true;
		} else {
			return false;
		}
	}

	public static void main(String[] args) {
		List<String> test = new LinkedList(Arrays.asList(new String[] { "a", "b", "c" }));
		asFirst(test, "d");
		for (String string : test) {
			System.out.println("***** LangHelper.main : " + string); // TODO:
																		// remove
																		// debug
																		// trace
		}
	}

	public static <T> void clearWeekReferenceCollection(Collection<WeakReference<T>> col) {
		Iterator<WeakReference<T>> it = col.iterator();
		while (it.hasNext()) {
			if (it.next().get() == null) {
				it.remove();
			}
		}
	}

	public static <K, T> void clearWeekReferenceMap(Map<K, WeakReference<T>> map) {
		Iterator<Map.Entry<K, WeakReference<T>>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			if (it.next().getValue().get() == null) {
				it.remove();
			}
		}
	}

	public static <E> List<E> getModifiableList(List<E> list) {
		if (list == null || list == Collections.emptyList() || list == Collections.EMPTY_LIST) {
			return new LinkedList<E>();
		} else {
			return list;
		}
	}

	public static <E> List<E> createList(E... items) {
		List<E> out = new LinkedList<>();
		for (E i : items) {
			out.add(i);
		}
		return out;
	}

	/**
	 * compare two objects with one of them null.
	 * 
	 * @param obj1
	 * @param obj2
	 * @return null if two objects != null
	 */
	public static Integer compareNull(Object obj1, Object obj2) {
		if (obj1 == null && obj2 == null) {
			return 0;
		} else {
			if (obj1 == null) {
				return 1;
			} else if (obj2 == null) {
				return -1;
			}
		}
		return null;
	}

	public static boolean isTrue(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj instanceof Boolean) {
			return (Boolean) obj;
		} else if (obj instanceof Integer) {
			return ((Integer) obj) != 0;
		} else if (obj instanceof Long) {
			return ((Long) obj) != 0;
		}else {
			return StringHelper.isTrue(obj.toString());
		}
	}

}
