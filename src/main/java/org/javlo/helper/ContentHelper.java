package org.javlo.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.image.GlobalImage;
import org.javlo.component.image.Image;
import org.javlo.component.links.ExternalLink;
import org.javlo.component.list.DataList;
import org.javlo.component.meta.DateComponent;
import org.javlo.component.multimedia.Video;
import org.javlo.component.text.Paragraph;
import org.javlo.component.text.WysiwygParagraph;
import org.javlo.component.title.SubTitle;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.XMLManipulationHelper.BadXMLException;
import org.javlo.helper.XMLManipulationHelper.TagDescription;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.utils.DocxUtils;
import org.javlo.utils.UnclosableInputStream;
import org.javlo.xml.NodeXML;
import org.javlo.xml.XMLFactory;
import org.javlo.ztatic.IStaticContainer;


public class ContentHelper {

	// public static final String IMPORT_FOLDER = "/import/";
	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ContentHelper.class.getName());

	/**
	 * remove tag. sample: <a href="#">link</a> -> link
	 * 
	 * @param text
	 *            XHTML Code
	 * @return simple text
	 */
	public static String removeTag(String text) {
		StringBuffer notTagStr = new StringBuffer();
		boolean inTag = false;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if ((!inTag) && (c == '<')) {
				inTag = true;
			} else if (inTag && (c == '>')) {
				inTag = false;
			} else if (!inTag) {
				notTagStr.append(c);
			}
		}
		return notTagStr.toString();
	}

	public static List<ComponentBean> createContentWithHTML(String html, String lg) throws BadXMLException {
		List<ComponentBean> outContent = new LinkedList<ComponentBean>();
		TagDescription[] tags = XMLManipulationHelper.searchAllTag(html, false);
		for (TagDescription tag : tags) {
			ComponentBean newBean = null;
			if (tag.getName().equalsIgnoreCase("h1")) {
				String content = removeTag(tag.getInside(html));
				newBean = new ComponentBean(Title.TYPE, content, lg);
			} else if (tag.getName().equalsIgnoreCase("p")) {
				String inside = tag.getInside(html).trim();
				String content = removeTag(inside);
				newBean = new ComponentBean(Paragraph.TYPE, content, lg);
				if (inside.startsWith("<strong")) {
					newBean.setStyle("important");
				}
			} else if (tag.getName().equalsIgnoreCase("ul")) {
				String content = removeTag(tag.getInside(html));
				newBean = new ComponentBean(DataList.TYPE, content, lg);
			} else if (tag.getName().equalsIgnoreCase("img")) {
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				PrintStream out = new PrintStream(outStream);
				String imagePath = tag.getAttributes().get("src");
				if (imagePath != null && imagePath.trim().length() > 0) {
					out.println(Image.FILE_NAME_KEY + "=" + StringHelper.getFileNameFromPath(imagePath));
					out.println(Image.DIR_KEY + "=" + StringHelper.getDirNameFromPath(imagePath).replace("###/webdav", ""));
					out.println(GlobalImage.IMAGE_FILTER + "=full");
					out.close();
					newBean = new ComponentBean(GlobalImage.TYPE, new String(outStream.toByteArray()), lg);
					newBean.setStyle(Image.STYLE_CENTER);
				}
			} else {
				for (int i = 2; i < 8; i++) {
					if (tag.getName().equalsIgnoreCase("h" + i)) {
						String content = removeTag(tag.getInside(html));
						newBean = new ComponentBean(SubTitle.TYPE, content, lg);
						newBean.setStyle("" + i);
					}
				}
			}
			if (newBean != null) {
				newBean.setValue(newBean.getValue().replace("&nbsp;", "").trim());
				if (newBean.getValue().length() > 0) {
					outContent.add(newBean);
				}
			}
		}
		return outContent;
	}

	public static void main(String[] args) {
		try {
			/*
			 * String html = ResourceHelper.loadStringFromFile(new
			 * File("d:/trans/test_doc.htm")); List<ComponentBean> content =
			 * createContentWithHTML(html, "en"); for (ComponentBean
			 * componentBean : content) { System.out.println("**** " +
			 * componentBean.getType()); // TODO: remove debug trace
			 * System.out.println(componentBean.getValue()); // TODO: remove
			 * debug trace System.out.println(""); }
			 */

			List<ComponentBean> content = createContentFromODT(null, new FileInputStream(new File("d:/trans/mep_test.odt")), "map_test.odt", "fr");
			System.out.println("***** ContentHelper.main : imported : " + content.size()); // TODO:
																							// remove
																							// debug
																							// trace
			for (ComponentBean componentBean : content) {
				System.out.println("**** " + componentBean.getType()); // TODO:
																		// remove
																		// debug
																		// trace
				// System.out.println(componentBean.getValue()); // TODO: remove
				// debug trace
				// System.out.println("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * import a zip entry localy.
	 * 
	 * @param gc
	 * @param entry
	 *            the zip entry.
	 * @param localFolder
	 *            the local forlder in static folder.
	 * @return false if file not create.
	 * @throws IOException
	 */
	public static boolean importZipEntryToDataFolder(GlobalContext gc, ZipEntry entry, InputStream in, String localFolder) throws IOException {
		File newFile = new File(URLHelper.mergePath(gc.getDataFolder(), localFolder, entry.getName()));
		if (newFile.exists() || entry.isDirectory()) {
			return false;
		} else {
			newFile.getParentFile().mkdirs();
			if (!newFile.createNewFile()) {
				return false;
			} else {
				ResourceHelper.writeStreamToFile(in, newFile);
			}
			return true;
		}

	}

	private static String getContentODTNode(NodeXML node, Collection<NodeXML> nodeDone) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		if (node.getContentPrefix() != null) {
			out.print(node.getContentPrefix());
		}
		boolean firstPara = true;
		for (NodeXML child : node.getAllChildren()) {
			if (child.getName().endsWith(":span")) {
				out.print(StringHelper.neverNull(child.getContent()) + ' ');
				nodeDone.add(child);
			} else if (child.getName().endsWith(":p")) {
				if (firstPara) {
					out.print(StringHelper.neverNull(child.getContent()));
				} else {
					out.println(StringHelper.neverNull(child.getContent()));
				}
				firstPara = false;
				nodeDone.add(child);
			}
		}
		if (node.getContentSuffix() != null && !node.getContentSuffix().equals(node.getContentPrefix())) {
			out.print(node.getContentSuffix());
		}
		out.close();
		return new String(outStream.toByteArray());
	}

	public static List<ComponentBean> createContentFromODT(GlobalContext gc, InputStream in, String name, String lang) throws Exception {
		List<ComponentBean> outBeans = new LinkedList<ComponentBean>();
		ZipInputStream zipIn = new ZipInputStream(in);
		ZipEntry entry = zipIn.getNextEntry();
		String baseStaticFolder = URLHelper.mergePath(gc.getStaticConfig().getImportFolder(), name);

		while (entry != null) {
			if (gc != null && StringHelper.isImage(entry.getName())) {
				importZipEntryToDataFolder(gc, entry, zipIn, URLHelper.mergePath(gc.getStaticConfig().getImageFolder(), baseStaticFolder));
			} else if (entry.getName().equals("content.xml")) {
				NodeXML root = XMLFactory.getFirstNode(new UnclosableInputStream(zipIn));
				// Collection<NodeXML> nodes =
				// root.searchChildren("//p|//h|//image");
				Collection<NodeXML> nodes = root.searchChildren("//*");
				String title = null;
				Map<String, String> stylesTree = new HashMap<String, String>();
				for (NodeXML node : nodes) {
					if (node.getName().endsWith(":style")) {
						if (node.getAttributeValue("style:parent-style-name") != null) {
							stylesTree.put(node.getAttributeValue("style:name"), node.getAttributeValue("style:parent-style-name"));
						}
					}
				}
				Collection<NodeXML> nodeDone = new HashSet<NodeXML>();
				for (NodeXML node : nodes) {
					if (!nodeDone.contains(node)) {
						String value = StringHelper.removeTag(node.getContent()).trim();
						ComponentBean bean = null;
						if (node.getName().endsWith(":a")) {
							String href = node.getAttributeValue("xlink:href");
							String label = StringHelper.removeTag(node.getContent()).trim();
							if (href.contains("youtube.com")) {
								label = NetHelper.getPageTitle(new URL(href));
								bean = new ComponentBean(Video.TYPE, StringHelper.writeLines("title=" + label, "link=" + href), lang);
							} else {
								bean = new ComponentBean(ExternalLink.TYPE, StringHelper.writeLines("label=" + label, "link=" + href), lang);
							}
							nodeDone.add(node);
						}
						if (value.length() > 0 || node.getName().endsWith(":image") || node.getName().endsWith(":list")) {
							if (node.getName().endsWith(":h") || "title".equalsIgnoreCase(stylesTree.get(node.getAttributeValue("text:style-name")))) {
								if (node.getAttributeValue("text:outline-level", "1").equals("1") || "title".equalsIgnoreCase(stylesTree.get(node.getAttributeValue("text:style-name")))) {
									bean = new ComponentBean(Title.TYPE, getContentODTNode(node, nodeDone), lang);
									title = value;
								} else {
									bean = new ComponentBean(SubTitle.TYPE, getContentODTNode(node, nodeDone), lang);
									bean.setStyle(node.getAttributeValue("text:outline-level", "2"));
								}
								nodeDone.add(node);
							}

							if (node.getParent() != null && node.getParent().getAttributeValue("text:style-name", "").equalsIgnoreCase("subtitle")) {
								bean = new ComponentBean(SubTitle.TYPE, getContentODTNode(node, nodeDone), lang);
								bean.setStyle(node.getAttributeValue("text:outline-level", "2"));
								nodeDone.add(node);
							}

							if (bean == null && node.getName().endsWith(":p") && !node.getParent().getName().endsWith(":list-item")) {
								bean = new ComponentBean(Paragraph.TYPE, getContentODTNode(node, nodeDone), lang);
								nodeDone.add(node);
							} else if (node.getName().endsWith(":list")) {
								NodeXML parent = node.getParent();
								boolean subList = false;
								while (parent != null) {
									if (parent.getName().endsWith(":list")) {
										subList = true;
									}
									parent = parent.getParent();
								}
								if (!subList) {
									ByteArrayOutputStream outStream = new ByteArrayOutputStream();
									PrintStream out = new PrintStream(outStream);
									for (NodeXML child : node.getAllChildren()) {
										if (child.getName().endsWith(":list-item")) {
											int parentDistance = child.getParentDistance(node);
											String prefix = "";
											if (parentDistance > 2) {
												for (int i = 0; i < (parentDistance - 2) / 2; i++) {
													prefix = prefix + '-';
												}
											}
											out.println(prefix + getContentODTNode(child, nodeDone));
											nodeDone.add(child);
										}
									}
									out.close();
									value = new String(outStream.toByteArray());
									bean = new ComponentBean(DataList.TYPE, value, lang);
								}
							} else if (node.getName().endsWith(":image")) {
								if (node.getAttributeValue("xlink:href") != null) {
									ByteArrayOutputStream outStream = new ByteArrayOutputStream();
									PrintStream out = new PrintStream(outStream);
									File file = new File(node.getAttributeValue("xlink:href"));
									String folder = "";
									if (file.getParentFile() != null) {
										folder = file.getParentFile().getPath();
									}
									out.println("dir=" + URLHelper.mergePath(baseStaticFolder, folder));
									out.println("file-name=" + file.getName());
									out.println(GlobalImage.IMAGE_FILTER + "=full");
									if (title != null) {
										out.println("label=" + title);
									}
									out.close();
									value = new String(outStream.toByteArray());
									bean = new ComponentBean(GlobalImage.TYPE, value, lang);
									nodeDone.add(node);
								}
							}
						}
						if (bean != null) {
							outBeans.add(bean);
						}
					}
				}
			}
			entry = zipIn.getNextEntry();

		}

		return outBeans;
	}

	public static List<ComponentBean> createContentFromDocx(ContentContext ctx, InputStream in, MenuElement page, String name, String lang) throws Exception {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ResourceHelper.writeStreamToStream(in, out);

		ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()));
		ZipEntry entry = zipIn.getNextEntry();
		
		String importFolder = AbstractVisualComponent.getImportFolderPath(ctx, page);
		String baseStaticFolder = URLHelper.mergePath(importFolder, name);

		// import static images
		while (entry != null) {
			if (ctx != null && StringHelper.isImage(entry.getName())) {
				importZipEntryToDataFolder(ctx.getGlobalContext(), entry, zipIn, URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getImageFolder(), baseStaticFolder));
			}
			entry = zipIn.getNextEntry();
		}

		// import content
		List<ComponentBean> beans = DocxUtils.extractContent(ctx.getGlobalContext(), new ByteArrayInputStream(out.toByteArray()), baseStaticFolder);
		for (ComponentBean bean : beans) {
			bean.setLanguage(lang);
		}
		return beans;
	}

	public static List<ComponentBean> createContentFromArray(GlobalContext gc, InputStream in, String name, String lang) throws Exception {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ResourceHelper.writeStreamToStream(in, out);

		ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()));
		ZipEntry entry = zipIn.getNextEntry();
		String baseStaticFolder = URLHelper.mergePath(gc.getStaticConfig().getImportFolder(), name);
		;

		// import content
		List<ComponentBean> beans = DocxUtils.extractContent(gc, new ByteArrayInputStream(out.toByteArray()), baseStaticFolder);
		for (ComponentBean bean : beans) {
			bean.setLanguage(lang);
		}
		return beans;
	}

	private static Locale getLocalBySuffix(String name) {
		if (name.contains("_")) {
			String[] splitted = name.split("_");
			if (splitted.length == 2) {
				if (splitted[1].length() == 2) {
					return new Locale(splitted[1]);
				}
			} else if (splitted.length > 2) {
				if (splitted[splitted.length - 2].length() == 2) {
					return new Locale(splitted[splitted.length - 2], splitted[splitted.length - 1]);
				} else {
					return new Locale(splitted[splitted.length - 1]);
				}
			}
		}
		return null;
	}

	public static String importJCRFile(ContentContext ctx, InputStream in, String name, MenuElement page, String titleXPath, String dateXPath, String dateFormat, String contentXPath, String pageRootXPath, boolean explodeHTML) throws ZipException, IOException {
		ZipInputStream zipIn = new ZipInputStream(in);
		/*
		 * Enumeration<? extends ZipEntry> entries = zipIn.getNextEntry();
		 * String pageName =
		 * StringHelper.getFileNameWithoutExtension(zip.getName());
		 */
		GlobalContext gc = GlobalContext.getInstance(ctx.getRequest());
		String pageName = StringHelper.createFileName(StringHelper.getFileNameWithoutExtension(name));
		ZipEntry entry = zipIn.getNextEntry();
		int countComp = 0;
		while (entry != null) {
			if (!entry.isDirectory() && entry.getName().endsWith(".xml")) {
				String fileName = entry.getName().replace(".xml", "");
				Locale locale = getLocalBySuffix(fileName);
				if (locale != null) {
					try {
						NodeXML node = XMLFactory.getFirstNode(new UnclosableInputStream(zipIn));
						String title = node.searchValue(titleXPath);
						String dateStr = node.searchValue(dateXPath);
						SimpleDateFormat format = new SimpleDateFormat(dateFormat);
						Date date = format.parse(dateStr);
						String xhtml = node.searchValue(contentXPath);

						ContentService content = ContentService.getInstance(ctx.getRequest());
						if (page == null) {
							page = content.getNavigation(ctx).searchChildFromName(pageName);
						}
						MenuElement rootPage = content.getNavigation(ctx).searchChildFromName(pageRootXPath);

						if (rootPage != null || page != null) {
							if (page == null) {
								page = MacroHelper.createArticlePage(ctx, rootPage, date);
								page.setName(pageName);
								ctx.setPath(page.getPath());
							}
							logger.info("create page : in " + locale + " " + page.getPath());
							String compId = content.createContent(ctx, page, new ComponentBean(Title.TYPE, title, locale.getLanguage()), "0", false);
							countComp++;
							compId = content.createContent(ctx, page, new ComponentBean(DateComponent.TYPE, StringHelper.renderTime(date), locale.getLanguage()), compId, false);
							countComp++;
							if (!explodeHTML) {
								compId = content.createContent(ctx, page, new ComponentBean(WysiwygParagraph.TYPE, xhtml, locale.getLanguage()), compId, true);
								countComp++;
							} else {
								Collection<ComponentBean> beans = createContentWithHTML(xhtml, locale.getLanguage());
								compId = content.createContent(ctx, page, beans, compId, true);
								countComp = countComp + beans.size();
							}

						} else {
							return "page not found : " + pageRootXPath;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					logger.info("page create with " + countComp + " components.");
				}
			} else if (StringHelper.isImage(entry.getName())) {
				File localFile = new File(URLHelper.mergePath(gc.getDataFolder(), gc.getStaticConfig().getImageFolder(), entry.getName()));
				if (!localFile.exists()) {
					ResourceHelper.writeStreamToFile(zipIn, localFile);
				}
			}
			entry = zipIn.getNextEntry();
		}
		return null;
	}
	
	public static MenuElement createChild(ContentContext ctx, MenuElement parent) {
		int index = 1;
		String childPageName = parent.getName()+"_"+index;
		ContentService content = ContentService.getInstance(ctx.getRequest());
		while (content.getNavigation(ctx).searchChildFromName(childPageName) != null) {
			index++;
			childPageName = parent.getName()+"_"+index;
		}
		MenuElement child =  MenuElement.getInstance(ctx);
		child.setName(childPageName);
		parent.addChildMenuElement(child);
		parent.releaseCache();
		return child;
	}

	public static void copyPage(ContentContext ctx, MenuElement source, MenuElement target) throws Exception {

		String sourceImportFolder = AbstractVisualComponent.getImportFolderPath(ctx.getContextOnPage(source), source);
		String targetImportFolder = AbstractVisualComponent.getImportFolderPath(ctx.getContextOnPage(target), target);

		ComponentBean[] beans = source.getContent();
		ComponentBean[] newBeans = new ComponentBean[beans.length];
		for (int i = 0; i < beans.length; i++) {
			newBeans[i] = new ComponentBean(beans[i]);
			newBeans[i].setId(StringHelper.getRandomId());
		}
		target.setContent(newBeans);
		target.setTemplateId(source.getTemplateId());		

		for (IContentVisualComponent comp : target.getContent(ctx).getContentElements()) {			
			if (comp instanceof IStaticContainer) {
				IStaticContainer image = (IStaticContainer) comp;
				if (image.getDirSelected(ctx) != null && image.getDirSelected(ctx).equals(sourceImportFolder)) {
					for (File sourceFile : image.getFiles(ctx)) {
						File targetFile = new File(StringHelper.cleanPath(sourceFile.getAbsolutePath()).replace(sourceImportFolder, targetImportFolder));
						if (!targetFile.exists()) {
							ResourceHelper.copyFile(sourceFile, targetFile, false);
						}
						image.setDirSelected(targetImportFolder);
					}
				}
			}
		}
	}
	
	public static MenuElement createPage(ContentContext ctx, MenuElement template, MenuElement root, Date date) throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String yearPageName = root.getName() + "-" + cal.get(Calendar.YEAR);
		MenuElement yearPage = MacroHelper.addPageIfNotExist(ctx, root.getName(), yearPageName, true);
		MacroHelper.createMonthStructure(ctx, yearPage);
		String mountPageName = MacroHelper.getMonthPageName(ctx, yearPage.getName(), date);
		MenuElement mounthPage = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx).searchChildFromName(mountPageName);
		if (mounthPage != null) {
			MenuElement newPage = MacroHelper.createArticlePageName(ctx, mounthPage);
			if (newPage != null) {
				ContentService content = ContentService.getInstance(ctx.getRequest());
				newPage.setTemplateId(template.getTemplateId());
				ContentContext noAreaCtx = ctx.getContextWithArea(null);
				ContentElementList contentList = template.getContent(noAreaCtx);
				Map<String, String> parents = new HashMap<String, String>();
				while (contentList.hasNext(noAreaCtx)) {
					IContentVisualComponent comp = contentList.next(noAreaCtx);
					if (!comp.isRepeat()) {
						ComponentBean bean = new ComponentBean(comp.getComponentBean());
						bean.setId(StringHelper.getRandomId());
						String parent = parents.get(bean.getArea());
						if (parent == null) {
							parent = "0";
						}
						parent = content.createContent(ctx, bean, parent, false);
						parents.put(bean.getArea(), parent);
					}
				}
				ctx.getCurrentPage().releaseCache();
			}
			return newPage;
		}
		return null;
	}

	/**
	 * get all component in a page and children of the page.
	 * 
	 * @param page
	 * @return
	 * @throws Exception
	 */
	public static List<ComponentBean> getAllComponentsOfChildren(MenuElement page) throws Exception {
		List<ComponentBean> comps = new LinkedList<ComponentBean>();
		comps.addAll(Arrays.asList(page.getContent()));
		for (MenuElement subPage : page.getAllChildrenList()) {
			comps.addAll(Arrays.asList(subPage.getContent()));
		}
		return comps;
	}
}
