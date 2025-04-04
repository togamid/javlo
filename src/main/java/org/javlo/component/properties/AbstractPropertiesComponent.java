package org.javlo.component.properties;

import org.apache.commons.lang3.StringEscapeUtils;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;
import org.javlo.service.exception.ServiceException;
import org.javlo.service.google.translation.ITranslator;

import java.io.*;
import java.util.*;

public abstract class AbstractPropertiesComponent extends AbstractVisualComponent {

	private static final String I18N_SUFFIX = ".i18n";
	protected Properties properties = new Properties();

	protected String createKeyWithField(String inField) {
		return getInputName(inField);
	}
	
	/**
	 * all field value is different on any language, if false, only field end with .i18n is different.
	 * @return
	 */
	protected boolean isAllTranslated() {
		return true;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		Map<String, String> fields = new HashMap<String, String>();
		for (String field : getFields(ctx)) {
			fields.put(getFieldName(field), getFieldValue(ctx, field));
			String fieldName = StringHelper.snakeToCamel(getFieldName(field));
			fields.put(fieldName, getFieldValue(ctx, field));
		}
		ctx.getRequest().setAttribute("fields", fields); // depreciated
		ctx.getRequest().setAttribute("field", fields);
	}

	protected static String getFieldName(String field) {
		if (!field.contains("#")) {
			return field;
		} else {
			return field.substring(0, field.indexOf('#'));
		}
	}

	protected static String getFieldType(String field) {
		if (!field.contains("#")) {
			return "text";
		} else {
			return field.substring(field.indexOf('#') + 1);
		}
	}
	
	public Collection<Map.Entry<String, String>> getFieldChoice(ContentContext ctx, String fieldName) {
		return null;
	}
	
	protected int getMdSize() {
		return 4;		
	}
	
	protected int getXsSize() {
		return 6;
	}
	
	protected String getLabel(I18nAccess i18nAccess, String fieldName) {
		String label = i18nAccess.getText("field." + fieldName, fieldName);
		if (label.endsWith(I18N_SUFFIX)) {
			return label.substring(0, label.lastIndexOf(I18N_SUFFIX));
		} else {
			return label;
		}
	}

	protected void renderField(PrintWriter out, ContentContext ctx, String field) throws ServiceException, Exception {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		String fieldName = getFieldName(field);
		String fieldType = getFieldType(field);
		out.println("<div class=\"col-md-"+getMdSize()+" col-xs-"+getXsSize()+"\">");
		out.println("<div class=\"form-group\">");
		
		String value = getFieldValue(ctx, fieldName);
		String readonly = "";
		if (!isAllTranslated() && !fieldName.endsWith(I18N_SUFFIX) && !ctx.getContentLanguage().equals(ctx.getGlobalContext().getDefaultLanguage())) {
			readonly = " readonly=\"readonly\"";
		}
		
		if (fieldType.equals("text")) {
			out.println("<label for=\"" + createKeyWithField(fieldName) + "\">");
			out.println(getLabel(i18nAccess, fieldName));
			out.println("</label>");
			Collection<Map.Entry<String, String>> choices = getFieldChoice(ctx, fieldName);
			if (choices == null) {
				out.print("<textarea"+readonly+" class=\"form-control\" rows=\"" + getRowSize(fieldName) + "\" id=\"");
				out.print(createKeyWithField(field));
				out.print("\" name=\"");
				out.print(createKeyWithField(fieldName));
				out.print("\">");
				out.print(value);
				out.println("</textarea>");
			} else {
				out.print("<select"+readonly+" class=\"form-control\" id=\"");
				out.print(createKeyWithField(field));
				out.print("\" name=\"");
				out.print(createKeyWithField(fieldName));
				out.println("\">");
				for (Map.Entry<String, String> option : choices) {
					String selected ="";
					if (option.getKey().equals(value)) {
						selected = " selected=\"selected\"";
					}
					out.println("<option value=\""+option.getKey()+"\""+selected+">"+option.getValue()+"</option>");
				}
				out.print("</select>");
			}
		} else if (fieldType.equals("checkbox")) {
			out.println("<div class=\"checkbox\"><label>");
			String checked = "";
			if (getFieldValue(ctx, fieldName).length() > 0) {
				checked = " checked=\"checked\"";
			}
			out.print("<input"+readonly+" type=\"checkbox\" id=\"");
			out.print(createKeyWithField(field));
			out.print("\" name=\"");
			out.print(createKeyWithField(fieldName));
			out.print("\" " + checked + " />");
			out.println(i18nAccess.getText("field." + fieldName, fieldName.replace("_", " ")));
			out.println("</label>");
			out.println("</div>");
		} else {
			out.println("<div class=\"form-group\">");
			out.println("<label for=\"" + createKeyWithField(fieldName) + "\">");
			out.println(i18nAccess.getText("field." + fieldName, fieldName));
			out.println("</label>");
			out.print("<input"+readonly+" type=\""+fieldType+"\" id=\"");
			out.print(createKeyWithField(field));
			out.print("\" name=\"");
			out.print(createKeyWithField(fieldName));
			out.print("\" value=\"");
			out.print(value);
			out.print("\" />");
			

			out.println("</div>");
		}
		out.println("</div></div>");
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		List<String> fields = getFields(ctx);
		out.println("<div class=\"row\">");
		boolean label = false;
		for (String field : fields) {
			if (!field.startsWith("label")) {
				renderField(out, ctx, field);
			} else {
				label = true;
			}
		}
		out.println("</div>");
		if (label) {
			out.println("<h3>label</h3><div class=\"row\">");
			for (String field : fields) {
				if (field.startsWith("label")) {
					renderField(out, ctx, field);
				}
			}
			out.println("</div>");
		}
		out.flush();
		out.close();
		return writer.toString();
	}

	protected double getFieldDoubleValue(String inField) {
		try {
			String dbl = properties.getProperty(inField, "0");
			if (dbl.contains(",")) {
				dbl = dbl.replace(",", ".");
			}
			return Double.parseDouble(dbl);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	protected long getFieldLongValue(String inField) {
		try {
			return Long.parseLong(properties.getProperty(inField, "0"));
		} catch (NumberFormatException e) {
			logger.warning(e.getMessage());
			return 0;
		}
	}

	public abstract List<String> getFields(ContentContext ctx) throws Exception;
	
	/**
	 * work only if all fields is translated
	 * @param inField
	 * @return
	 */
	protected String getFieldValue(String inField) {
		return getFieldValue((ContentContext)null, inField);
	}

	protected String getFieldValue(ContentContext ctx, String inField) {
		String value;
		if (isAllTranslated() || ctx == null || inField.endsWith(I18N_SUFFIX)) {
			value = properties.getProperty(getFieldName(inField), "");
		} else {
			AbstractPropertiesComponent comp = null;
			try {
				comp = (AbstractPropertiesComponent)getReferenceComponent(ctx);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (comp == null) {
				value = "error reference component not found!";
			} else {
				value = comp.properties.getProperty(getFieldName(inField), "");
			}
		}

		return value;
	}

	protected String getFieldValue(String inField, String defaultValue) {
		return properties.getProperty(inField, defaultValue);
	}

	public String getHeader() {
		return getType();
	}

	public int getRowSize(String field) {
		return 1;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		List<String> fields = getFields(ctx);
		if (getRenderer(ctx) != null) {
			for (String field : fields) {
				ctx.getRequest().setAttribute(field, getFieldValue(ctx, field));
			}
			return executeJSP(ctx, getRenderer(ctx));
		} else {
			StringBuffer out = new StringBuffer();
			out.append("<div class=\"");
			out.append(getType());
			out.append("\"><ul class=\"list-group\">");
			for (String field : fields) {
				if (!StringHelper.isEmpty(getFieldValue(ctx, field))) {
					out.append("<li  class=\"list-group-item ");
					out.append(field);
					out.append("\">");
					out.append(getFieldValue(ctx, field));
					out.append("</li>");
				}
			}
			out.append("</ul></div>");
			return out.toString();
		}
	}

	@Override
	public int getWordCount(ContentContext ctx) {
		Collection<Object> values = properties.values();
		int wordCount = 0;
		for (Object value : values) {
			if (value != null) {
				wordCount = wordCount + value.toString().split(" ").length;
			}
		}
		return wordCount;
	}

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
		properties.load(stringToStream(getValue()));
	}
	
//	@Override
//	public void setModify() {
//		super.setModify();
//		properties.clear();
//		try {
//			properties.load(stringToStream(getValue()));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	protected String getListSeparator() {
		return ",";
	}

	public String validateField(ContentContext ctx, String fieldName, String fieldValue) throws Exception {
		return null;
	}
	
	@Override
	protected boolean getColumnableDefaultValue() {
		return true;
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		if (isColumnable(ctx)) {
			performColumnable(ctx);
		}

		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		List<String> fields = getFields(ctx);
		String msg = null;
		for (String fieldKey : fields) {
			String field = getFieldName(fieldKey);
			String fieldValue = requestService.getParameter(createKeyWithField(field), null);
			String newMsg = validateField(ctx, field, fieldValue);
			if (newMsg != null) {
				msg = newMsg;
			}

			String[] fieldValues = requestService.getParameterValues(createKeyWithField(field), null);
			if (fieldValues != null && fieldValues.length > 1) {
				fieldValue = StringHelper.arrayToString(fieldValues, getListSeparator());
			}

			if (fieldValue != null) {
				if (!fieldValue.equals(getFieldValue(ctx, field))) {
					setModify();
					properties.put(field, fieldValue);
				}
			} else {
				if (StringHelper.isTrue(properties.get(field))) {
					setModify();
				}				
				properties.remove(field);
			}
		}
		if (isModify()) {
			storeProperties();
		}

		return msg;
	}

	protected void setFieldValue(String inField, String value) {
		if (value == null) {
			properties.remove(inField);
		} else if (!properties.getProperty(inField, value + "diff").equals(value)) {
			properties.setProperty(inField, value);
			storeProperties();
			setModify();
		}
	}

	public void storeProperties() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String res = "";
		try {
			properties.store(out, getHeader());
			out.flush();
			res = new String(out.toByteArray());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setValue(res);
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		try {
			for (String field : getFields(ctx)) {
				String fieldValue = getFieldValue(ctx, field);
				if (fieldValue != null && fieldValue.trim().length() > 0) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Map<String, Object> getContentAsMap(ContentContext ctx) throws Exception {
		Map<String, Object> content = super.getContentAsMap(ctx);
		content.put("value", properties);
		return content;
	}

	@Override
	public String getContentAsText(ContentContext ctx) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		for (Object value : properties.values()) {
			out.println(value);
		}
		out.close();
		return new String(outStream.toByteArray());
	}

	public static void main(String[] args) {
		Properties properties = new Properties();
		properties.setProperty("key", "été");

		System.out.println("été = " + properties.getProperty("key"));
	}

	@Override
	public boolean transflateFrom(ContentContext ctx, ITranslator translator, String lang) {
		if (isValueTranslatable()) {
			boolean translated = true;
			try {
				for (String field : getFields(ctx)) {
					String value = StringEscapeUtils.unescapeHtml4(getFieldValue(ctx, field));
					String newValue = translator.translate(ctx, value, lang, ctx.getRequestContentLanguage());
					if (newValue == null) {
						translated = false;
						newValue = ITranslator.ERROR_PREFIX + value;
					}
					setFieldValue(field, newValue);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return translated;
		} else {
			return false;
		}
	}

}
