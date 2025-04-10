package org.javlo.module.template;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XMLManipulationHelper.BadXMLException;
import org.javlo.i18n.I18nAccess;
import org.javlo.image.ImageConfig;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.content.Edit;
import org.javlo.module.core.Module;
import org.javlo.module.core.Module.Box;
import org.javlo.module.core.ModulesContext;
import org.javlo.module.file.FileModuleContext;
import org.javlo.module.mailing.MailingModuleContext;
import org.javlo.module.template.remote.IRemoteResourcesFactory;
import org.javlo.module.template.remote.RemoteTemplateFactoryManager;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.NavigationWithContent;
import org.javlo.remote.IRemoteResource;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.servlet.zip.ZipManagement;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.RoleWrapper;
import org.javlo.utils.ReadOnlyPropertiesConfigurationMap;
import org.javlo.utils.StructuredProperties;
import org.javlo.ztatic.FileCache;

public class TemplateAction extends AbstractModuleAction {
	
	private static Logger logger = Logger.getLogger(TemplateAction.class.getName());

	@Override
	public String getActionGroupName() {
		return "template";
	}

	public static String getPreviewImageURL(ContentContext ctx, String filter, String area) {
		return null;
	}

	private static List<String> getTextProperties() {
		List<String> textProperties = new LinkedList<String>();
		textProperties.add("width");
		textProperties.add("height");
		textProperties.add("max-width");
		textProperties.add("max-height");
		textProperties.add("margin-top");
		textProperties.add("margin-right");
		textProperties.add("margin-bottom");
		textProperties.add("margin-left");
		textProperties.add("adjust-color");
		textProperties.add("brightness");
		textProperties.add("replace-alpha");
		textProperties.add("web2.height");
		textProperties.add("web2.separation");
		textProperties.add("background-color");
		return textProperties;
	}

	private static List<String> getBooleanProperties() {
		List<String> booleanProperties = new LinkedList<String>();
		booleanProperties.add("grayscale");
		booleanProperties.add("add-border");
		booleanProperties.add("crop-resize");
		booleanProperties.add("crystallize");
		booleanProperties.add("edge");
		booleanProperties.add("framing");
		booleanProperties.add("emboss");
		booleanProperties.add("web2");
		booleanProperties.add("round-corner");
		return booleanProperties;

	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext moduleContext) throws Exception {

		if (ctx.getRequest().getRequestURL().toString().endsWith(".wav")) { // hack
																			// for
																			// elfinder
																			// js
			return null;
		}

		String msg = null;
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Module module = moduleContext.getCurrentModule();
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		TemplateContext templateContext = TemplateContext.getInstance(ctx.getRequest().getSession(), globalContext, module);

		Collection<Template> allTemplate = TemplateFactory.getAllDiskTemplates(ctx.getRequest().getSession().getServletContext());
		Collection<String> contextTemplates = globalContext.getTemplatesNames();

		Collection<Template.TemplateBean> templates = new LinkedList<Template.TemplateBean>();

		templateContext.checkEditMode(ctx);

		if (templateContext.getCurrentLink().equals(TemplateContext.MY_TEMPLATES_LINK.getUrl())) {
			ctx.getRequest().setAttribute("nobrowse", "true");
		}

		RoleWrapper roleWrapper = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession()).getRoleWrapper(ctx, ctx.getCurrentEditUser());

		boolean editTemplate = StringHelper.isTrue(ctx.getRequest().getAttribute("editTemplate"));
		
		if (templateContext.getCurrentLink().equals("hierarchy") && (!editTemplate || ctx.getRequest().getParameter("back") != null)) {
			Map<String, TemplateHierarchy> templatesH = new HashMap<String, TemplateHierarchy>();
			List<TemplateHierarchy> rootTemplateH = new LinkedList<TemplateHierarchy>();
			for (Template template : allTemplate) {
				TemplateHierarchy.insertTemplateInHirarchy(ctx, rootTemplateH, templatesH, template);
			}
			if (ctx.getRequest().getParameter("templateh") != null) {
				rootTemplateH.clear();
				rootTemplateH.add(templatesH.get(ctx.getRequest().getParameter("templateh")));
			}
			ctx.getRequest().setAttribute("htemps", rootTemplateH);

			if (module.getMainBoxes().size() > 0) {
				module.getMainBoxes().iterator().next().setRenderer("/jsp/hierarchy.jsp");
				module.setRenderer(null);
			} else {
				module.createMainBox("hierarchy", "Hierarchy", "/jsp/hierarchy.jsp", false);
				module.setRenderer(null);
			}
		} else {
			for (Template template : allTemplate) {
				if (!template.isTemplateInWebapp(ctx)) {
					template.importTemplateInWebapp(StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext()), ctx);
				}
				Boolean acceptTemplate = roleWrapper.acceptTemplate(template.getName());
				if (acceptTemplate == null) {
					acceptTemplate = !templateContext.getCurrentLink().equals(TemplateContext.MY_TEMPLATES_LINK.getUrl()) || contextTemplates.contains(template.getName());
					if (moduleContext.getFromModule() != null && moduleContext.getFromModule().getName().equals("admin")) {
						acceptTemplate = true;
					}
				}
				if (template.visibleForRoles(ctx.getCurrentEditUser().getRoles()) && acceptTemplate) {
					templates.add(new Template.TemplateBean(ctx, template));
				}

			}
		}

		ctx.getRequest().setAttribute("templates", templates);
		ctx.getRequest().setAttribute("fromAdmin", moduleContext.getFromModule() != null && moduleContext.getFromModule().getName().equals("admin"));

		Map<String, String> params = new HashMap<String, String>();
		
		String templateName = requestService.getParameter("templateid", null);
		if (templateName == null) {
			templateName = (String)ctx.getRequest().getAttribute("templateid");
		}

		if (templateName != null) {
			Template template = TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).get(templateName);
			if (template == null) {
				msg = "template not found : " + templateName;
				module.restoreAll();
			} else {
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				
				Map<String, List<String>> folders = template.getCSSByFolder(ctx.getRequest().getParameter("search"));
				Map<String, List<String>> htmlFolders = template.getHtmlByFolder(ctx.getRequest().getParameter("search"));
				ctx.getRequest().setAttribute("currentTemplate", new Template.TemplateBean(ctx, template));
				ctx.getRequest().setAttribute("cssFolder", folders);
				ctx.getRequest().setAttribute("htmlFolder", htmlFolders);
				params.put("templateid", templateName);
				FileModuleContext fileModuleContext = FileModuleContext.getInstance(ctx.getRequest());
				fileModuleContext.clear();
				fileModuleContext.setRoot(template.getTemplateRealPath());
				fileModuleContext.setTitle("<a href=\"" + URLHelper.createModuleURL(ctx, ctx.getPath(), TemplateContext.NAME, params) + "\">" + template.getId() + "</a>");

				ImageConfig imageConfig = ImageConfig.getNewInstance(globalContext, ctx.getRequest().getSession(), template);
				ImageConfig parentImageConfig = ImageConfig.getNewInstance(globalContext, ctx.getRequest().getSession(), template.getParent());
				ctx.getRequest().setAttribute("filters", imageConfig.getFilters());

				if (requestService.getParameter("filter", null) != null && requestService.getParameter("back", null) == null) {
					ctx.getRequest().setAttribute("areas", template.getAreas());
					ctx.getRequest().setAttribute("textProperties", getTextProperties());
					ctx.getRequest().setAttribute("booleanProperties", getBooleanProperties());
					ctx.getRequest().setAttribute("allValues", new ReadOnlyPropertiesConfigurationMap(parentImageConfig.getProperties(), false));
					if (template.getImageConfigFile().exists()) {
						Properties values = new Properties();
						Reader fileReader = new FileReader(template.getImageConfigFile());
						values.load(fileReader);
						fileReader.close();
						ctx.getRequest().setAttribute("values", values);
					}
					Iterator<Box> ite = module.getMainBoxes().iterator();
					if (ite.hasNext()) {
						ite.next().setRenderer("/jsp/images.jsp");
					}
					// module.setRenderer("/jsp/images.jsp");
				} else if (requestService.getParameter("css", null) != null && requestService.getParameter("back", null) == null) {
					if (module.getMainBoxes().size() > 0) {
						module.getMainBoxes().iterator().next().setRenderer("/jsp/css.jsp");
						module.setRenderer(null);
					} else {
						module.createMainBox("edit_template", i18nAccess.getText("template.edit.title") + " : " + template.getName(), "/jsp/css.jsp", false);
						module.setRenderer(null);
					}
				} else if (requestService.getParameter("html", null) != null && requestService.getParameter("back", null) == null) {
					if (module.getMainBoxes().size() > 0) {
						module.getMainBoxes().iterator().next().setRenderer("/jsp/html.jsp");
						module.setRenderer(null);
					} else {
						module.createMainBox("edit_template", i18nAccess.getText("template.edit.title") + " : " + template.getName(), "/jsp/html.jsp", false);
						module.setRenderer(null);
					}
				} else if (requestService.getParameter("back", null) != null) {
					module.restoreAll();
					//module.createMainBox("edit_template", i18nAccess.getText("template.edit.title") + " : " + template.getName(), "/jsp/edit_template.jsp", false);
				}
			}
		} else if (requestService.getParameter("list", null) == null) {
			FileModuleContext fileModuleContext = FileModuleContext.getInstance(ctx.getRequest());
			fileModuleContext.clear();
			fileModuleContext.setRoot(globalContext.getStaticConfig().getTemplateFolder());
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			fileModuleContext.setTitle("<a href=\"" + URLHelper.createModuleURL(ctx, ctx.getPath(), TemplateContext.NAME, params) + "\">" + i18nAccess.getText("template.action.browse") + "</a>");
			module.restoreAll();
		}

		params.clear();
		params.put("webaction", "browse");
		ctx.getRequest().setAttribute("fileURL", URLHelper.createInterModuleURL(ctx, ctx.getPath(), FileModuleContext.MODULE_NAME, params));

		/** choose template if we come from admin module **/
		if (moduleContext.getFromModule() != null && moduleContext.getFromModule().getName().equals("admin")) {
			ctx.getRequest().setAttribute("selectUrl", moduleContext.getFromModule().getBackUrl());
		}
		return msg;
	}
	
	public String selectTemplateForEdit(ContentContext ctx, String id) throws Exception {
		
		ctx.getRequest().setAttribute("templateid", id);
		ctx.getRequest().setAttribute("editTemplate", true);
		
		String msg = null;
		Module module = ModulesContext.getInstance(ctx.getSession(), ctx.getGlobalContext()).getCurrentModule();
		
		Template template = TemplateFactory.getDiskTemplate(ctx.getServletContext(), id);
		if (template == null) {
			msg = "template not found : " + id;
			module.clearAllBoxes();
			module.restoreAll();
		} else {
			ctx.getRequest().setAttribute("currentTemplate", new Template.TemplateBean(ctx, template));
			module.setRenderer(null);
			// module.setToolsRenderer(null);
			module.clearAllBoxes();
			try {
				template.getRenderer(ctx); // prepare ids list
			} catch (BadXMLException e) {
				e.printStackTrace();
			}
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
			module.createMainBox("edit_template", i18nAccess.getText("template.edit.title") + " : " + template.getName(), "/jsp/edit_template.jsp", false);
		}
		return msg;
	}

	public String performGoEditTemplate(ServletContext application, HttpServletRequest request, ContentContext ctx, RequestService requestService, Module module, I18nAccess i18nAccess) throws Exception {
		return selectTemplateForEdit(ctx, requestService.getParameter("templateid", null));
	}

	public String performEditTemplate(ServletContext application, StaticConfig staticConfig, ContentContext ctx, RequestService requestService, Module module, I18nAccess i18nAccess, MessageRepository messageRepository) throws Exception {
		String msg = null;
		Template template = TemplateFactory.getTemplates(application).get(requestService.getParameter("templateid", null));
		if (template == null) {
			return "template not found : " + requestService.getParameter("templateid", null);
		}
		if (requestService.getParameter("back", null) != null) {
			module.restoreAll();
		} else {
			try {
				template.setAuthors(requestService.getParameter("author", template.getAuthors()));
				Date date = StringHelper.parseDate(requestService.getParameter("creation-date", null), staticConfig.getDefaultDateFormat());
				template.setCreationDate(date);
				template.setValid(requestService.getParameter("valid", null) != null);
				messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("template.message.updated"), GenericMessage.INFO));

				Collection<String> areas = template.getAreas();
				for (String area : areas) {
					String areaId = requestService.getParameter("free-area-" + area, "");
					if (areaId.trim().length() == 0) {
						areaId = requestService.getParameter("area-" + area, "");
					}
					if (areaId.trim().length() > 0 && !template.getAreasMap().get(area).equals(areaId)) {
						template.setArea(area, areaId);
					}
				}

				template.setImageFiltersRAW(requestService.getParameter("image-filter", template.getImageFiltersRAW()));
				template.setParentName(requestService.getParameter("parent", template.getParentName()));

				String newArea = requestService.getParameter("new-area", "");
				if (newArea.trim().length() > 0) {
					String areaId = requestService.getParameter("free-area-new", "");
					if (areaId.trim().length() == 0) {
						areaId = requestService.getParameter("newarea-id", "");
					}
					if (areaId.trim().length() > 0) {
						if (template.getAreasMap().values().contains(areaId)) {
							return i18nAccess.getText("template.error.same-id");
						} else if (template.getAreasMap().keySet().contains(newArea)) {
							return i18nAccess.getText("template.error.same-area");
						} else {
							template.setArea(newArea, areaId);
						}
					} else {
						return i18nAccess.getText("template.error.choose-id");
					}
				}

			} catch (ParseException e) {
				msg = e.getMessage();
			}
			TemplateFactory.clearTemplate(application);
		}
		return msg;
	}

	public String performChangeRenderer(HttpSession session, RequestService requestService, GlobalContext globalContext, Module currentModule, I18nAccess i18nAccess) throws Exception {

		currentModule.restoreAll();

		String list = requestService.getParameter("list", null);
		if (list == null) {
			return "bad request structure : need 'list' as parameter.";
		}
		TemplateContext.getInstance(session, globalContext, currentModule).setCurrentLink(list);

		IRemoteResourcesFactory tempFact = RemoteTemplateFactoryManager.getInstance(session.getServletContext()).getRemoteTemplateFactory(globalContext, list);
		session.setAttribute("templateFactory", tempFact);
		if (tempFact != null) {
			try {
				tempFact.refresh();
			} catch (Throwable e) {
				e.printStackTrace();
				currentModule.restoreAll();
				return e.getMessage();
			}
			currentModule.setRenderer("/jsp/remote_templates.jsp");
			currentModule.createSideBox("sponsors", i18nAccess.getText("global.sponsors"), "/jsp/sponsors.jsp", false);
		} else {
			currentModule.restoreAll();
		}
		return null;
	}

	public String performDeleteArea(ServletContext application, RequestService requestService) throws Exception {
		String area = requestService.getParameter("area", null);
		if (area == null) {
			return "bad request structure, need 'area' as parameter.";
		}
		Template template = TemplateFactory.getDiskTemplate(application, requestService.getParameter("templateid", null));
		template.deleteArea(area);
		return null;
	}

	public String performImport(RequestService requestService, HttpSession session, ContentContext ctx, GlobalContext globalContext, Module currentModule, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String list = requestService.getParameter("list", null);
		String templateName = requestService.getParameter("templateid", null);
		if (list == null || templateName == null) {
			return "bad request structure : need 'list' and 'name' as parameter.";
		}
		TemplateContext templateContext = TemplateContext.getInstance(session, globalContext, currentModule);
		templateContext.setCurrentLink(list);
		if (list != null) {
			IRemoteResourcesFactory tempFact = RemoteTemplateFactoryManager.getInstance(session.getServletContext()).getRemoteTemplateFactory(globalContext, list);
			IRemoteResource template = tempFact.getResource(templateName);
			if (template == null) {
				return "template not found : " + templateName;
			}
			Template newTemplate = TemplateFactory.createDiskTemplates(session.getServletContext(), templateName);

			newTemplate.setAuthors(template.getAuthors());

			InputStream in = null;
			OutputStream out = null;
			try {
				URL zipURL = new URL(template.getDownloadURL());
				in = zipURL.openConnection().getInputStream();
				ZipManagement.uploadZipTemplate(ctx.getGlobalContext().getStaticConfig().getTemplateFolder(), in, newTemplate.getId());
				in.close();

//				URL imageURL = new URL(template.getImageURL());
//				File visualFile = new File(URLHelper.mergePath(newTemplate.getTemplateRealPath(), newTemplate.getVisualFile()));
//				RenderedImage image = JAI.create("url", imageURL);
//				out = new FileOutputStream(visualFile);
//				JAI.create("encode", image, out, "png", null);
//				out.close();

				messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("template.message.imported", new String[][] { { "name", newTemplate.getId() } }), GenericMessage.INFO));

				templateContext.setCurrentLink(null); // return to local
														// template list.
				currentModule.restoreAll();

			} catch (Exception e) {
				e.printStackTrace();
				newTemplate.delete();
				return e.getMessage();
			} finally {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			}
		}

		return null;
	}

	public String performValidate(RequestService requestService, HttpSession session, ContentContext ctx) throws Exception {
		Template template = TemplateFactory.getTemplates(session.getServletContext()).get(requestService.getParameter("id", null));
		if (template == null) {
			Collection<Template> templates;
			templates = TemplateFactory.getAllTemplates(session.getServletContext());
			for (Template template2 : templates) {
				template2.setValid(true);
			}
		} else {
			template.setValid(true);
		}
		return null;
	}

	public String performDelete(RequestService requestService, HttpSession session, ContentContext ctx) throws Exception {
		Template template = TemplateFactory.getDiskTemplate(session.getServletContext(), requestService.getParameter("id", null));
		if (template != null) {
			template.delete();
		}
		return null;
	}

	public String performCommit(RequestService requestService, ServletContext application, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		Template template = TemplateFactory.getDiskTemplate(application, requestService.getParameter("templateid", null));
		template.clearRenderer(ctx);
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("template.message.commited", new String[][] { { "name", requestService.getParameter("templateid", null) } }), GenericMessage.INFO));
		I18nAccess.getInstance(ctx).resetViewLanguage(ctx);
		return null;
	}

	public String performCommitChildren(RequestService requestService, ServletContext application, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		Template template = TemplateFactory.getDiskTemplate(application, requestService.getParameter("templateid", null));
		template.clearRenderer(ctx);
		Collection<Template> children = TemplateFactory.getTemplateAllChildren(application, template);
		for (Template child : children) {
			child.clearRenderer(ctx);
		}
		messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("template.message.commited", new String[][] { { "name", requestService.getParameter("templateid", null) } }), GenericMessage.INFO));
		I18nAccess.getInstance(ctx).resetViewLanguage(ctx);		
		return null;
	}

	public static String performChangeFromPreview(RequestService rs, HttpSession session, ContentContext ctx, Module currentModule, MessageRepository messageRepository, I18nAccess i18nAccess) throws FileNotFoundException, IOException {
		TemplateContext.getInstance(session, ctx.getGlobalContext(), currentModule).checkEditMode(ctx);
		return null;
	}

	public static String performSelectTemplate(RequestService rs, ContentContext ctx, EditContext editContext, MenuElement currentPage, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		if (!Edit.checkPageSecurity(ctx)) {
			messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.block"), GenericMessage.ERROR));
		} else {
			String templateName = rs.getParameter("templateid", null);
			Template template = TemplateFactory.getDiskTemplate(ctx.getRequest().getSession().getServletContext(), templateName);
			if (templateName != null && template == null) {
				return "template not found : " + templateName;
			} else {
				if (ctx.getGlobalContext().isOpenPlatform()) {
					currentPage = currentPage.getRoot();
				}
				currentPage.setTemplateId(templateName);
				MailingModuleContext mailingCtx = MailingModuleContext.getInstance(ctx.getRequest());
				mailingCtx.setCurrentTemplate(null);
				if (template != null && !ctx.getGlobalContext().isMailingPlatform()) {
					if (template.isOnePage()) {
						currentPage.setChildrenAssociation(true);
					} else {
						currentPage.setChildrenAssociation(false);
					}
				}
			}
		}

		if (ctx.isEditPreview()) {
			ctx.setClosePopup(true);
		}

		PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);

		return null;
	}

	public static String performUpdateFilter(RequestService rs, ServletContext application, GlobalContext globalContext, HttpSession session, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String filter = rs.getParameter("filter", null);

		if (filter == null) {
			return "need 'filter' as parameter.";
		}
		Template template = TemplateFactory.getDiskTemplate(application, rs.getParameter("templateid", null));
		if (template == null) {
			return "error, template not found.";
		}

		StructuredProperties imageConfig = new StructuredProperties();
		if (template.getImageConfigFile().exists()) {
			Reader fileReader = new FileReader(template.getImageConfigFile());
			imageConfig.load(fileReader);
			fileReader.close();
		}

		boolean modifiy = false;

		for (String prop : getTextProperties()) {
			String allKey = filter + '.' + prop;
			String val = rs.getParameter(allKey, "").trim();
			if (val.length() == 0) {
				if (rs.getParameter(allKey, null) != null) {
					if (!val.equals(imageConfig.getProperty(allKey))) {
						imageConfig.remove(allKey);
						modifiy = true;
					}
				}
			} else {
				imageConfig.setProperty(allKey, val);
				modifiy = true;
			}
			for (String area : template.getAreas()) {
				String key = filter + '.' + area + '.' + prop;
				val = rs.getParameter(key, "").trim();
				if (val.length() == 0) {
					if (rs.getParameter(key, null) != null) {
						imageConfig.remove(key);
					}
				} else {
					if (!val.equals(imageConfig.getProperty(key))) {
						imageConfig.setProperty(key, val);
						ctx.getRequest().setAttribute("modifiedArea", area);
						modifiy = true;
					}
				}
			}
		}

		for (String prop : getBooleanProperties()) {
			String key = filter + '.' + prop;

			boolean val = StringHelper.isTrue(rs.getParameter(key, null));

			if (rs.getParameter("_CK_" + key, null) != null) {
				if (rs.getParameter(key, "").trim().length() == 0) {
					imageConfig.remove(key);
					modifiy = true;
				} else {
					if (val != StringHelper.isTrue(imageConfig.getProperty(key, null)) || imageConfig.getProperty(key, null) == null) {
						imageConfig.setProperty(key, "" + val);
						modifiy = true;
					}
				}
			}

			for (String area : template.getAreas()) {
				key = filter + '.' + area + '.' + prop;
				val = StringHelper.isTrue(rs.getParameter(key, null));
				if (rs.getParameter("_CK_" + key, null) != null) {
					System.out.println(">>>>>>>>> TemplateAction.performUpdateFilter : 1.key = "+key); //TODO: remove debug trace
					if (rs.getParameter(key, "").trim().length() == 0) {
						imageConfig.remove(key);
						modifiy = true;
						ctx.getRequest().setAttribute("modifiedArea", area);
					} else {
						System.out.println(">>>>>>>>> TemplateAction.performUpdateFilter : 2.key = "+key); //TODO: remove debug trace
						if (val != StringHelper.isTrue(imageConfig.getProperty(key, null)) || imageConfig.getProperty(key, null) == null) {
							System.out.println(">>>>>>>>> TemplateAction.performUpdateFilter : val = "+val); //TODO: remove debug trace
							imageConfig.setProperty(key, "" + val);
							modifiy = true;
							ctx.getRequest().setAttribute("modifiedArea", area);
						}
					}
				}
			}
		}

		if (modifiy) {
			FileCache.getInstance(application).clear(globalContext.getContextKey());
			Writer fileWriter = new FileWriter(template.getImageConfigFile());
			imageConfig.store(fileWriter, "template module store.");
			fileWriter.close();
			ImageConfig.getNewInstance(globalContext, session, template);
		}

		return null;
	}

	public static String performEditHTML(RequestService rs, ServletContext application, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String html = rs.getParameter("html", null);
		if (html == null) {
			return "error : no 'html' param.";
		} else {
			Template template = TemplateFactory.getTemplates(application).get(rs.getParameter("templateid", ""));
			if (template == null) {
				return "template not found";
			} else {
				// store new value
				if (rs.getParameter("text", null) != null) {
					File htmlFile = new File(URLHelper.mergePath(template.getSourceFolder().getAbsolutePath(), rs.getParameter("file", "")));
					if (htmlFile.exists() && htmlFile.isFile()) {
						ResourceHelper.writeStringToFile(htmlFile, rs.getParameter("text", null), ContentContext.CHARACTER_ENCODING);
						messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("global.file.saved"), GenericMessage.INFO));
					} else {
						return "file not found : " + htmlFile;
					}
				}
				// load current value
				File htmlFile = new File(URLHelper.mergePath(template.getSourceFolder().getAbsolutePath(), html));
				if (!htmlFile.exists()) {
					return "file not found : " + htmlFile;
				} else {
					String text = ResourceHelper.loadStringFromFile(htmlFile);
					ctx.getRequest().setAttribute("text", text);
				}
			}
		}
		return null;
	}

	public static String performEditCSS(RequestService rs, ServletContext application, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String css = rs.getParameter("css", null);
		Template template = TemplateFactory.getTemplates(application).get(rs.getParameter("templateid", ""));
		if (template == null) {
			logger.severe("template not found : "+rs.getParameter("templateid", ""));
			return "template not found";
		} else {
			// store new value
			if (rs.getParameter("text", null) != null) {
				File cssFile = new File(URLHelper.mergePath(template.getSourceFolder().getAbsolutePath(), rs.getParameter("file", "")));
				if (cssFile.exists() && cssFile.isFile()) {
					String text = rs.getParameter("text", null);
					if (rs.getParameter("indent") != null) {
						text = StringHelper.indentScss(text);
						ctx.setNeedRefresh(true);
					}
					ResourceHelper.writeStringToFile(cssFile, text, ContentContext.CHARACTER_ENCODING);
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("global.file.saved"), GenericMessage.INFO));
				} else {
					return "file not found : " + cssFile;
				}
			}
			// load current value
			if (css != null) {
				File cssFile = new File(URLHelper.mergePath(template.getSourceFolder().getAbsolutePath(), css));
				if (!cssFile.exists()) {
					return "file not found : " + cssFile;
				} else {
					String text = ResourceHelper.loadStringFromFile(cssFile);
					ctx.getRequest().setAttribute("text", text);
				}
			}
		}
		return null;
	}

	public static String getContextROOTFolder(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (AdminUserSecurity.getInstance().isGod(ctx.getCurrentEditUser())) {
			return globalContext.getDataFolder();
		} else {
			return URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder());
		}
	}

	private static boolean uploadTemplate(ContentContext ctx, InputStream in, File file) throws Exception {
		if (!StringHelper.getFileExtension(file.getName()).toLowerCase().equals("zip")) {
			return false;
		} else {
			ZipManagement.uploadZipTemplate(ctx.getGlobalContext().getStaticConfig().getTemplateFolder(), in, StringHelper.getFileNameWithoutExtension(file.getName()));
			return true;
		}
	}

	public static String performUpload(ContentContext ctx, RequestService rs, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String sourceFolder = getContextROOTFolder(ctx);
		FileModuleContext fileModuleContext = FileModuleContext.getInstance(ctx.getRequest());
		File folder = new File(sourceFolder, fileModuleContext.getPath());
		for (FileItem file : rs.getAllFileItem()) {
			File newFile = new File(URLHelper.mergePath(folder.getAbsolutePath(), StringHelper.createFileName(file.getName())));
			newFile = ResourceHelper.getFreeFileName(newFile);
			InputStream in = file.getInputStream();
			try {
				if (!uploadTemplate(ctx, in, newFile) && in != null && file.getName().trim().length() > 0) {
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("template.error.bad-template-file"), GenericMessage.ALERT));
				}
			} finally {
				ResourceHelper.closeResource(in);
			}
		}

		String urlStr = rs.getParameter("url", "");
		
		if (urlStr.trim().length() > 0) {
			URL url = new URL(urlStr);
			InputStream in = url.openConnection().getInputStream();
			try {
				File newFile = new File(URLHelper.mergePath(folder.getAbsolutePath(), StringHelper.createFileName(StringHelper.getFileNameFromPath(urlStr))));
				newFile = ResourceHelper.getFreeFileName(newFile);
				if (!uploadTemplate(ctx, in, newFile)) {
					messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("template.error.bad-template-file"), GenericMessage.ALERT));
				}
			} finally {
				ResourceHelper.closeResource(in);
			}
		}

		return null;
	}

	public static Collection<PageTemplateRef> searchPageTemplate(ContentContext ctx, String templateName) throws Exception {
		if (ctx.getGlobalContext().isMaster()) {
			Collection<PageTemplateRef> outPages = new LinkedList<PageTemplateRef>();
			Collection<GlobalContext> allContext = GlobalContextFactory.getAllGlobalContext(ctx.getRequest().getSession().getServletContext());
			ContentContext externalCtx = new ContentContext(ctx);
			for (GlobalContext context : allContext) {
				externalCtx.setForceGlobalContext(context);
				externalCtx.setRenderMode(ContentContext.PREVIEW_MODE);
				externalCtx.setForcePathPrefix(context.getContextKey());
				for (NavigationWithContent nav : TemplateFactory.searchPageNeedTemplate(externalCtx, templateName)) {
					String ref = "direct";
					Template template = TemplateFactory.getTemplate(externalCtx, nav.getPage());
					if (template != null) {
						if (!template.getName().equals(templateName)) {
							ref = "inherited";
						}
						outPages.add(new PageTemplateRef(nav.getPage().getName(), context.getContextKey(), URLHelper.createURL(externalCtx, "/"), ref));
					}
				}
			}
			return outPages;
		} else {
			return Collections.EMPTY_LIST;
		}
	}
}
