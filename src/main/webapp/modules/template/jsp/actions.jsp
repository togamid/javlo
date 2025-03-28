<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:set var="actionBar" value="true" /><c:if test="${!forceActionBar && not empty param.previewEdit}"><c:set var="actionBar" value="false" /></c:if>
<c:set var="templateid" value="${param.templateid}" /><c:if test="${not empty currentTemplate}"><c:set var="templateid" value="${currentTemplate.name}" /></c:if>
<c:if test="${empty templateFactory && empty templateid && actionBar}"><a class="action-button valid-all" href="${info.currentURL}?webaction=template.validate"><span>${i18n.edit['command.admin.template.all']}</span></a></c:if>
<c:url var="inheritedURL" value="${info.currentURL}" context="/">
	<c:param name="webaction" value="template.selectTemplate"></c:param>
	<c:if test="${!actionBar}">
		<c:param name="previewEdit" value="${param.previewEdit}"></c:param>
	</c:if>
</c:url> 
<c:if test="${!actionBar}"><a class="action-button valid-all${empty info.page.templateId?' active':''}" href="${inheritedURL}"><span>${i18n.edit['global.inherited']}</span></a></c:if>
<c:if test="${not empty templateFactory && empty param.viewAll}"><a class="action-button valid-all" href="${info.currentURL}?viewAll=true&list=${link.url}"><span>${i18n.edit['template.action.view-all']}</span></a></c:if>
   
<input type="text" placeholder="${i18n.edit['global.filter']}" onkeyup="filterSub(this.value, '.content li', 'h2');" />   
   
<c:if test="${not empty templateid}">
   <a class="action-button more" href="${fileURL}&templateid=${templateid}"><span>${i18n.edit['template.action.browse']}...</span></a>
   <a class="action-button more" href="${info.currentURL}?filter=&templateid=${templateid}"><span>${i18n.edit['template.action.filter-image']}</span></a>
   <c:if test="${fn:length(currentTemplate.CSS)>0}">
       <a class="action-button more" href="${info.currentURL}?css=${currentTemplate.CSS[0]}&templateid=${currentTemplate.name}&webaction=template.editCSS"><span>${i18n.edit['template.action.css']}</span></a>	
   </c:if>
   <c:if test="${fn:length(currentTemplate.htmls)>0}">
   	<a class="action-button more" href="${info.currentURL}?html=${currentTemplate.htmls[0]}&templateid=${currentTemplate.name}&webaction=template.editHTML"><span>${i18n.edit['template.action.html']}</span></a>
   </c:if>    
   
   <c:url var="commit" value="${info.currentURL}" context="/">	
       <c:param name="webaction" value="template.commit"></c:param>
       <c:param name="templateid" value="${templateid}"></c:param>       
       <c:if test="${not empty param.css}">
           <c:param name="css" value="${param.css}"></c:param>       	
           <c:param name="webaction" value="template.editCSS"></c:param>
       </c:if>
       <c:if test="${not empty param.html}">
       		<c:param name="html" value="${param.html}"></c:param>
       		<c:param name="webaction" value="template.editHTML"></c:param>
       </c:if>
       <c:if test="${not empty param.file}">
       		<c:param name="file" value="${param.file}"></c:param>
       </c:if>
       <c:if test="${not empty param.filter}">
       		<c:param name="filter" value="${param.filter}"></c:param>
       </c:if>
   </c:url>
   
   <a class="action-button ajax" href="${commit}"><span class="text">${i18n.edit['template.action.commit']}</span></a>
   
   <c:url var="commitChildren" value="${info.currentURL}" context="/">	
       <c:param name="webaction" value="template.commitChildren"></c:param>
       <c:param name="templateid" value="${templateid}"></c:param>       
       <c:if test="${not empty param.css}">
           <c:param name="css" value="${param.css}"></c:param>       	
           <c:param name="webaction" value="template.editCSS"></c:param>
       </c:if>
       <c:if test="${not empty param.html}">
       		<c:param name="html" value="${param.html}"></c:param>
       		<c:param name="webaction" value="template.editHTML"></c:param>
       </c:if>
       <c:if test="${not empty param.file}">
       		<c:param name="file" value="${param.file}"></c:param>
       </c:if>
       <c:if test="${not empty param.filter}">
       		<c:param name="filter" value="${param.filter}"></c:param>
       </c:if>
   </c:url>
   <a class="action-button ajax" href="${commitChildren}"><span>${i18n.edit['template.action.commit-children']}</span></a>   
</c:if>
<c:if test="${empty templateid and empty nobrowse}">
<a href="${info.currentModuleURL}/jsp/upload.jsp?currentURL=${info.currentURL}" class="popup cboxElement action-button"><span>${i18n.edit['action.add-template']}</span></a>
<a class="action-button more" href="${fileURL}"><span>${i18n.edit['template.action.browse']}...</span></a>
</c:if>

<div class="clear">&nbsp;</div>


