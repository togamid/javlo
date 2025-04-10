/** 
 * Created on Aug 13, 2003
 */
package org.javlo.actions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.content.Edit;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;

public class TimeTravelerActions implements IAction {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(TimeTravelerActions.class.getName());

	public String getActionGroupName() {
		return "time";
	}

	public synchronized static String performUndoRedo(RequestService rs, ContentContext ctx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		
		if (!Edit.checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
			return null;
		}
		
		boolean previous = rs.getParameter("previous", null) != null;
		PersistenceService pers = PersistenceService.getInstance(globalContext);
		
		final String NOT_FOUND_MSG = i18nAccess.getText("message.error.no-undo");

		if (previous) {
			if (!ctx.isCanUndo()) {
				return NOT_FOUND_MSG;
			}			
			int previousVersion = pers.getVersion() - 1;
			
			int previousUndoVersionMin = 0;			
			if (globalContext.getLatestUndoVersion() != null) {
				previousUndoVersionMin = globalContext.getLatestUndoVersion();
			} else if (globalContext.getFirstLoadVersion() != null) {
				previousUndoVersionMin = globalContext.getFirstLoadVersion();
			}
			
			
			MenuElement previousPage = pers.loadPreview(ctx, previousVersion);
			previousPage = previousPage.searchChildFromId(ctx.getCurrentPage().getId());
			
			while (ctx.getCurrentPage().equals(ctx, previousPage, ctx.getCurrentPage().isChildrenAssociation()) && pers.isPreviewVersion(previousVersion - 1) && previousVersion-1>previousUndoVersionMin) {
				previousVersion = previousVersion - 1;
				previousPage = pers.loadPreview(ctx, previousVersion);
				previousPage = previousPage.searchChildFromId(ctx.getCurrentPage().getId());
			}
			if (previousUndoVersionMin >= previousVersion) {
				return NOT_FOUND_MSG;
			}
			if (pers.isPreviewVersion(previousVersion)) {
				int version = pers.getVersion();
				globalContext.setLatestUndoVersion(pers.getVersion());
				replaceCurrentPage(ctx, previousPage, ctx.getCurrentPage().isChildrenAssociation());
				String msg = "new version loading : " + previousVersion + " (from:" + version + ')';
				logger.info(msg);
				MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(i18nAccess.getText("message.info.undo"), GenericMessage.SUCCESS));
				return null;
			} else {
				return NOT_FOUND_MSG;
			}
		}
		return null;
	}

	public static String performSettraveltime(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		ContentService content = ContentService.getInstance(request);
		Date travelTime = null;
		Integer newVersion = null;
		boolean previous = request.getParameter("previous") != null;
		boolean next = request.getParameter("next") != null;
		if (next || previous) {
			
			Integer version = globalContext.getTimeTravelerContext().getVersion();
			if (version == null) {
				version = PersistenceService.getInstance(globalContext).getVersion();
			}
			
			if (next) {
				version++;
			} else {
				version--;
			}
			
			if (PersistenceService.getInstance(globalContext).isPreviewVersion(version)) {
				newVersion = version;
			}
			
//			travelTime = globalContext.getTimeTravelerContext().getTravelTime();
//			List<Date> dates = PersistenceService.getInstance(globalContext).getBackupDates();
//			if (dates.size() > 0) {
//				Integer currentIndex = null;
//				if (travelTime != null) {
//					long minDiff = Long.MIN_VALUE;
//					for (int i = 0; i < dates.size(); i++) {
//						Date backup = dates.get(i);
//						long diff = backup.getTime() - travelTime.getTime();
//						if (diff <= 0 && diff > minDiff) {
//							minDiff = diff;
//							currentIndex = i;
//						}
//					}
//				}
//				if (currentIndex == null) {
//					if (previous) {
//						travelTime = dates.get(0);
//					}
//				} else {
//					currentIndex += previous ? 1 : -1;
//					if (currentIndex < 0) {
//						travelTime = null;
//					} else if (currentIndex >= dates.size()) {
//						travelTime = dates.get(dates.size() - 1);
//					} else {
//						travelTime = dates.get(currentIndex);
//					}
//				}
//			}
		} else {
			String version = request.getParameter("date");
			if (StringHelper.isDigit(version)) {
				newVersion = Integer.parseInt(version);
			}
			try {
				travelTime = new SimpleDateFormat("dd/MM/yy HH:mm:ss").parse(version);
			} catch (Exception ex) {
				try {
					travelTime = new SimpleDateFormat("dd/MM/yy HH:mm").parse(version);
				} catch (Exception ex2) {
					try {
						travelTime = new SimpleDateFormat("dd/MM/yy").parse(version);
					} catch (Exception ex3) {
					}
				}
			}
		}
		globalContext.getTimeTravelerContext().setTravelTime(travelTime);
		globalContext.getTimeTravelerContext().setVersion(newVersion);
		content.releaseTimeTravelerNav(ctx);
		return null;
	}

	public static String performReplacecurrentpage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		ContentContext timeCtx = ctx.getContextWithOtherRenderMode(ContentContext.TIME_MODE);
		ContentContext editCtx = ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE);
		ContentService content = ContentService.getInstance(request);

		MenuElement timeCurrentPage = timeCtx.getCurrentPage();
		MenuElement editCurrentPage = MacroHelper.addPageIfNotExistWithoutMessage(editCtx, content.getNavigation(editCtx), timeCurrentPage, false, false);
		
		editCtx.setArea(null);
		timeCtx.setArea(null);
		
		MacroHelper.deleteLocalContent(editCurrentPage, editCtx);
		MacroHelper.copyLocalContent(timeCurrentPage, timeCtx, editCurrentPage, editCtx);
		editCurrentPage.releaseCache();

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.setAskStore(true);

		return null;
	}

	public static String replaceCurrentPage(ContentContext ctx, MenuElement newPage, boolean withChildren) throws Exception {

		MenuElement page = ctx.getCurrentPage();

		if (page != null) {
			// Switch parent (Experimental!!)
			MenuElement parent = page.getParent();
			parent.removeChild(page);

			newPage.setParent(parent);
			newPage.setId(page.getId());
			newPage.setPriority(page.getPriority());
			newPage.setParent(null);

			if (!withChildren) {
				newPage.copyChildren(page);
			}

			parent.addChildMenuElement(newPage);
			parent.clearPageBean(ctx);
			ctx.setCurrentPageCached(newPage);
		} else {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String msg = i18nAccess.getText("time.message.error.page-deleted");
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		}
		PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
		ContentService content = ContentService.getInstance(ctx.getRequest());
		content.releasePreviewNav(ctx);
		return null;

	}

	public static String performReplaceCurrentPageAndChildren(HttpServletRequest request, HttpServletResponse response) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		ContentContext timeCtx = ctx.getContextWithOtherRenderMode(ContentContext.TIME_MODE);
		ContentContext editCtx = ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE);
		ContentService content = ContentService.getInstance(request);

		NavigationService navigationService = NavigationService.getInstance(globalContext);

		MenuElement timeCurrentPage = timeCtx.getCurrentPage();
		MenuElement editCurrentPage = content.getNavigation(editCtx);
		
		if (timeCurrentPage.getParent() != null) {
			editCurrentPage = editCurrentPage.searchChild(editCtx, timeCurrentPage.getPath());
		}

		// // Recreate object for other users
		// content.releaseTimeTravelerNav(timeCtx);
		// content.releasePreviewNav(editCtx);

		if (editCurrentPage != null) {
			// Switch parent (Experimental!!)
			MenuElement parent = editCurrentPage.getParent();
			timeCurrentPage.setParent(parent);
			timeCurrentPage.setId(editCurrentPage.getId());
			timeCurrentPage.setPriority(editCurrentPage.getPriority());
			editCurrentPage.setParent(null);

			parent.removeChild(editCurrentPage);
			parent.addChildMenuElement(timeCurrentPage);

		} else {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String msg = i18nAccess.getText("time.message.error.page-deleted");
			logger.severe(msg);
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		}

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(editCtx);

		content.releaseTimeTravelerNav(timeCtx);
		//content.releasePreviewNav(editCtx);

		return null;
	}
	
	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return ctx.getCurrentEditUser() != null;
	}

}

