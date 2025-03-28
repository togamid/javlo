package org.javlo.macro.interactive;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItem;
import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.fields.Field;
import org.javlo.fields.FieldDate;
import org.javlo.helper.ContentHelper;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.MessageRepository;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.utils.Cell;
import org.javlo.utils.StructuredProperties;
import org.javlo.utils.XLSTools;

public class ImportContent implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(ImportContent.class.getName());

	@Override
	public String getName() {
		return "import-content";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return null;
	}

	@Override
	public String getActionGroupName() {
		return "macro-import-content";
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/import-content.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {
		return null;
	}

	@Override
	public String getInfo(ContentContext ctx) {
		return null;
	}

	public static String performImport(RequestService rs, ContentContext ctx, EditContext editCtx, ContentService content, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		String encoding = rs.getParameter("encoding", ContentContext.CHARACTER_ENCODING);
		if (StringHelper.isEmpty(encoding)) {
			encoding = ContentContext.CHARACTER_ENCODING;
		}

		Collection<FileItem> items = rs.getAllFileItem();
		for (FileItem fileItem : items) {
			InputStream in = null;
			try {
				in = fileItem.getInputStream();
				List<ComponentBean> newBeans = null;
				if (StringHelper.isHTML(fileItem.getName())) {
					String html = ResourceHelper.loadStringFromStream(fileItem.getInputStream(), Charset.forName(encoding));
					newBeans = ContentHelper.createContentWithHTML(html, ctx.getRequestContentLanguage());
				} else if (StringHelper.getFileExtension(fileItem.getName()).equalsIgnoreCase("odt")) {
					newBeans = ContentHelper.createContentFromODT(GlobalContext.getInstance(ctx.getRequest()), in, fileItem.getName(), ctx.getRequestContentLanguage());
				} else if (StringHelper.getFileExtension(fileItem.getName()).equalsIgnoreCase("xlsx")) {
					Cell[][] array = XLSTools.getXLSXArray(ctx, in, null);
					for (int i = 0; i < array.length; i++) {
						String val = array[i][0].getValue();
						if (!StringHelper.isEmpty(val)) {
							String compType = val.trim();
							IContentVisualComponent comp = null;
							for (IContentVisualComponent compItem : ComponentFactory.getGlobalContextComponent(ctx, ctx.getCurrentTemplate())) {
								if (compItem.getType().equals(compType)) {
									comp = compItem;
								}
							}
							IContentVisualComponent prevComp = null;
							if (comp != null) {
								if (comp instanceof DynamicComponent) {
									DynamicComponent dynComp = (DynamicComponent) comp;
									StructuredProperties p = dynComp.getProperties();
									for (int j = 1; j < array[0].length; j++) {
										
										Field field = dynComp.getField(ctx, array[0][j].getValue());
										if (field != null) {
											if (!StringHelper.isEmpty(array[0][j].getValue()) && array[i].length > j && !StringHelper.isEmpty(array[i][j].getValue())) {
												String value = array[i][j].getValue();
												if (field instanceof FieldDate) {
													Date date = StringHelper.parseExcelDate(value);
													value = StringHelper.renderDate(date);
												}
												p.setProperty("field."+array[0][j].getValue()+".value", value);
											}
										} else {
											logger.warning("field not found : "+array[0][j].getValue()+" (component:"+dynComp.getType()+')');
										}
									}
									dynComp.setProperties(p);
									dynComp.storeProperties();
								} else {
									comp.setValue(array[i][1].getValue());
								}
								MacroHelper.addContent(ctx.getRequestContentLanguage(), ctx.getCurrentPage(), prevComp != null ? prevComp.getId() : "0", comp.getType(), comp.getValue(ctx), ctx.getCurrentEditUser());
								prevComp = comp;
							}
						}
					}
				}
				if (newBeans != null) {
					logger.info("import file : " + newBeans.size() + " components.");
					String parentId = "0";
					for (ComponentBean bean : newBeans) {
						parentId = content.createContent(ctx, bean, parentId, false);
					}
				}

			} finally {
				ResourceHelper.closeResource(in);
			}
		}

		String url = rs.getParameter("url", "");
		if (StringHelper.isURL(url)) {
			String html = NetHelper.readPage(new URL(url));
			List<ComponentBean> newBeans = ContentHelper.createContentWithHTML(html, ctx.getRequestContentLanguage());

			logger.info("import url : " + newBeans.size() + " components.");

			String parentId = "0";
			for (ComponentBean bean : newBeans) {
				parentId = content.createContent(ctx, bean, parentId, false);
			}
		}

		ctx.getCurrentPage().releaseCache();

		if (ctx.isEditPreview()) {
			ctx.setClosePopup(true);
		}

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

	@Override
	public boolean isAdd() {
		return true;
	}

	@Override
	public boolean isInterative() {
		return true;
	}

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return ctx.getCurrentEditUser() != null;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void init(ContentContext ctx) {
	}

	@Override
	public String getModalSize() {
		return DEFAULT_MAX_MODAL_SIZE;
	}

	@Override
	public String getIcon() {
		return "bi bi-nut";
	}

	@Override
	public String getUrl() {
		return null;
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}
	
	public static void main(String[] args) throws ParseException {
		Date date = StringHelper.parseExcelDate("2/19/13");
		System.out.println(">>>>>>>>> ImportContent.main : date = "+StringHelper.renderDate(date)); //TODO: remove debug trace
	}

}
