<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:set var="templateid" value="${param.templateid}" /><c:if test="${not empty currentTemplate && empty templateid}"><c:set var="templateid" value="${currentTemplate.name}" /></c:if>
<div class="content css">

	<div class="row">
	<div class="col-md-3">
	
	<div class="file-filter">
		<form id="form-css-template" action="${info.currentURL}" method="post">    
		<div class="form-group _jv_flex-line">		
		<input type="hidden" name="templateid" value="${templateid}" />
		<input type="hidden" name="css" value="${param.css}" />
		<input type="hidden" name="webaction" value="template.editCSS" />		
		<input type="text" name="search" value="${param.search}" placeholder="search..." id="input-filter" class="form-control" />
		<input type="submit" value="ok" class="btn btn-default btn-xs ms-1" />
		</div>
		</form>
	</div>
	<br />
	<div class="accordion">
		<c:forEach var="folder" items="${cssFolder}">
			<h4><a role="button" data-toggle="collapse" href="#${folder.key}" aria-expanded="false" aria-controls="${folder.key}">${folder.key}</a></h4>
			<div><div class="files">
				<c:forEach var="css" items="${folder.value}">
					<c:set var="file" value="${folder.key}/${css}" />
					<c:url var="cssUrl" value="${info.currentURL}" context="/">
						<c:param name="css" value="${file}" />
						<c:param name="templateid" value="${templateid}" />
						<c:param name="webaction" value="editCSS" />
						<c:param name="search" value="${param.search}" />
					</c:url>
					<a ${file == param.css?' class="btn btn-sm btn-primary"':'class="btn btn-sm btn-default"'} id="${folder.key}" href="${cssUrl}">${css}</a>
				</c:forEach>
			</div></div>
		</c:forEach>
	</div>
	
	</div>
	
	<div class="col-md-9">

	<form id="form-css-template" action="${info.currentURL}" class="standard-form js-change-submit ajax" method="post">
		<div>
			<input type="hidden" name="webaction" value="template.editCSS" /> <input type="hidden" name="templateid" value="${templateid}" />
		</div>	
		<div class="widgetbox2">
		<h3>${param.css}</h3>
		<div class="content nopadding">
		<c:if test="${not empty param.css}">
			<input type="hidden" name="file" value="${param.css}" />
			<input type="hidden" name="css" value="${param.css}" />
			<textarea name="text" class="text-editor" id="text-editor" rows="10" cols="10" data-ext="css" data-mode="text/x-scss">${text}</textarea>
		</c:if>
		</div></div>
		<div class="action">
			<input type="submit" name="back" value="${i18n.edit['global.back']}" />
			<input type="submit" value="${i18n.edit['global.save']}" />
			<input type="submit" name="indent" value="${i18n.edit['global.save']} & indent" />
		</div>
	</form>
	
	</div>
	</div>
	
</div>