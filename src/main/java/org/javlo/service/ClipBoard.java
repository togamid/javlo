/*
 * Created on 25-janv.-2004
 */
package org.javlo.service;

import javax.servlet.http.HttpServletRequest;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.data.EditInfoBean;
import org.javlo.i18n.I18nAccess;


/**
 * @author pvandermaesen
 * used for copy&paste a component in the site.
 */
public class ClipBoard {
	
	private static final String CLIPBOARD_SESSION_KEY = "clipboard";
	
	private Object copied = null;
	private String label = null;
	private String icon;
	
	private ClipBoard(){};
	
	public static ClipBoard getInstance ( HttpServletRequest request ) {
		ClipBoard res = (ClipBoard)request.getSession().getAttribute(CLIPBOARD_SESSION_KEY);
		if ( res == null ) {
			res = new ClipBoard();
			request.getSession().setAttribute(CLIPBOARD_SESSION_KEY, res );
		}
		return res;
	}
	
	public void copy ( ContentContext ctx, Object inCopied, String icon) {
		try {
			EditContext.getInstance(ctx.getGlobalContext(), ctx.getSession()).setPathForCopy(null);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		copied=inCopied;
		this.icon=icon;
		if (copied instanceof ComponentBean) {
			ComponentBean comp = (ComponentBean)copied;
			I18nAccess i18nAccess;
			try {
				i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				label = i18nAccess.getText("content."+comp.getType(), comp.getType());
			} catch (Exception e) {
				label = comp.getType();
				e.printStackTrace();
			}			
		}
	}
	
	public Object getCopied() {
		return copied;
	}
	
	public String getIcon() {
		if (icon != null) {
			return icon;
		}
		if (copied instanceof IContentVisualComponent) {
			return ((IContentVisualComponent)copied).getIcon();
		} else {
			return null;
		}
	}
	
	public String getLabel() {
		if (label == null && copied != null) {
			return ""+copied;
		} else {
			return label;	
		}
	}
	
	public boolean isEmpty(ContentContext ctx) throws Exception {
		return getCopiedComponent(ctx)==null;
	}
	
	public void clear() {
		copied=null;
		label=null;
	}
	
	public ComponentBean getCopiedComponent(ContentContext ctx) throws Exception {
		if (copied == null) {
			return null;
		}
		
		if (copied instanceof ComponentBean) {
			ContentService content = ContentService.getInstance(ctx.getRequest());
			return (ComponentBean)copied;
		} else {
			return null;
		}
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

}