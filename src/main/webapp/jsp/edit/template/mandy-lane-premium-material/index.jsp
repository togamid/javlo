<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv"%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link rel="stylesheet" media="screen" type="text/css" href="<jv:url value='css/main_edit_and_preview.css' />" />
<style>
	#main-loader {
		position : fixed;
		z-index: 9999;
		background-color: rgba(0,0,0,0.8);
		top : 0px;
		left : 0px;
		height : 100%;
		width : 100%;
		cursor : wait;		
	}
	.lds-ripple {
	  display: inline-block;
	  position: absolute;
	  width: 128px;
	  height: 128px;
	  top: 50%;
	  margin-top: -64px;
	  left: 50%;
	  margin-left: -64px;
	  
	}
	.lds-ripple div {
	  position: absolute;
	  border: 4px solid #fff;
	  opacity: 1;
	  border-radius: 50%;
	  animation: lds-ripple 1s cubic-bezier(0, 0.2, 0.8, 1) infinite;
	}
	.lds-ripple div:nth-child(2) {
	  animation-delay: -0.5s;
	}
	@keyframes lds-ripple {
	  0% {
	    top: 28px;
	    left: 28px;
	    width: 0;
	    height: 0;
	    opacity: 1;
	  }
	  100% {
	    top: -1px;
	    left: -1px;
	    width: 58px;
	    height: 58px;
	    opacity: 0;
	  }
	}	
</style> 
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="<jv:url value='/js/edit/ajax.js?ts=${info.ts}' />"></script>
<script type="text/javascript" src="<jv:url value='/js/edit/core.js?ts=${info.ts}' />"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/javlo/core.js?ts=${info.ts}"></script>
<script type="text/javascript" src="<jv:url value='/js/lib/tooltipster-master/js/tooltipster.bundle.min.js' />"></script>
<script type="text/javascript">
	function closePopup(parentURL) {
		var url = top.location.href; // close iframe and refresh parent frame
		var anchor = "";
		if (parentURL != null) {
			url = parentURL;
		}
		<c:if test="${not empty contentContext.parentURL}">
		url = "${contentContext.parentURL}";
		</c:if>
		if (url.indexOf('#') >= 0) {
			anchor = url.substring(url.indexOf('#'));
			url = url.substring(0, url.indexOf('#'));
		}
		<c:if test="${not empty messages.rawGlobalMessage}">
		if (url.indexOf("?") >= 0) {
			url = url + "&${messages.parameterName}=${messages.rawGlobalMessage}";
		} else {
			url = url + "?${messages.parameterName}=${messages.rawGlobalMessage}";
		}
		</c:if>
		if (url != null) {
			var doc = top.document.documentElement, body = top.document.body;
			var topScroll = (doc && doc.scrollTop || body && body.scrollTop || 0);
			if (topScroll > 0) {
				var sep = "?";
				if (url.indexOf("?") >= 0) {
					sep = "&";
				}
				url = url + sep + "_scrollTo=" + topScroll;
				
			}
			var sep = "?";
			if (url.indexOf("?") >= 0) {
				sep = "&";
			}
			url = url + sep + "__ts="+Date.now();
			top.location.href = url; // close iframe and refresh parent frame
		}
	}	
</script>
<c:if test="${contentContext.closePopup}">	
<script type="text/javascript">
	closePopup();
</script>
<c:if test="${not empty contentContext.globalContext.staticConfig.htmlHead}">${contentContext.globalContext.staticConfig.htmlHead}</c:if>
	
	
</c:if>
<c:if test="${contentContext.closePopup}"></head><body></body></c:if>
<c:if test="${!contentContext.closePopup}">

	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta http-equiv="X-UA-Compatible" content="IE=edge" />
	<meta name="viewport" content="width=device-width, initial-scale=1" />

	<title>Javlo : ${currentModule.title}</title>

	<link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/bootstrap/bootstrap.css?ts=${info.ts}" />
	<link rel="stylesheet" href="${info.editTemplateURL}/css/style.css?ts=${info.ts}" />
	<link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/javlo.css?ts=${info.ts}" />
	<link rel="stylesheet" media="screen" type="text/css" href="<jv:url value='/css/edit/global.css' />" />

	<style type="text/css">
@font-face {
	font-family: "javloFont";
	src: url('${info.staticRootURL}fonts/javlo-italic.ttf')
		format("truetype");
}
</style>

	<c:if test="${not empty globalContext.editTemplateMode}">
		<link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/edit_${globalContext.editTemplateMode}.css" />
	</c:if>
	<c:if test="${not empty specificCSS}">
		<link rel="stylesheet" media="screen" href="${specificCSS}" />
	</c:if>
	<!--[if IE 9]>
    <link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/ie9.css"/>
<![endif]-->

	<!--[if IE 8]>
    <link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/ie8.css"/>
<![endif]-->

	<!--[if IE 7]>
    <link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/ie7.css"/>
<![endif]-->

	<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery-ui-1.8.20.custom.min.js"></script>
	<script type="text/javascript">
		<jsp:include page="/jsp/edit/global/dynamic_js.jsp" />
	</script>

	<script type="text/javascript">
		var i18nURL = "${info.i18nAjaxURL}";
	</script>
	
	<c:if test="${not empty contentContext.globalContext.staticConfig.htmlHead}">${contentContext.globalContext.staticConfig.htmlHead}</c:if>
	
	<!-- module '${currentModule.name}' JS -->
	<c:forEach var="js" items="${currentModule.JS}">
		<script type="text/javascript" src="<jv:url value='${js}?ts=${info.ts}' />"></script>
	</c:forEach>
	<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.jgrowl.js"></script>
	</head>

	<body id="main-body" onload="jQuery('#main-loader').remove();" class="bodygrey ${info.admin?'right-admin ':'noright-admin '}${not empty param.previewEdit?'previewEdit':''}${requestService.parameterMap.lightEdit?' light-edit':''} edition-mode-${contentContext.globalContext.editTemplateMode}">

		<div id="main-loader" onclick="jQuery('#main-loader').remove();"><div class="lds-ripple"><div></div><div></div></div></div>
		
		<c:if test="${empty param.previewEdit}">
			<div class="header">

				<c:if test="${currentModule.search}">
					<form id="search" action="${info.currentURL}" method="post">
						<input type="hidden" name="webaction" value="search" /> <input type="text" name="query" placeholder="${i18n.edit['content.search']}" />
						<button class="searchbutton"><i class="fa fa-angle-double-right" aria-hidden="true"></i></button>
					</form>
				</c:if>

				<div class="topheader">
					<ul class="notebutton">
						<c:if test="${userInterface.IM}">
							<li class="note">
								<div class="im-wizz-message" style="display: none;" title="${i18n.edit['im.title']}">${i18n.edit['im.message.wizz']}</div> <a href="${info.editTemplateURL}/im.jsp" class="messagenotify"> <i class="fa fa-comment-o" aria-hidden="true"></i> <span class="count" style="display: none;">0</span>
							</a>
							</li>
						</c:if>
						<li class="note"><a href="${info.editTemplateURL}/notifications.jsp" class="alertnotify"> <span class="wrap"> <i class="bi bi-chat"></i> <c:if test="${not empty notificationSize}">
										<span id="notification-count" class="count">${notificationSize}</span>
									</c:if>
							</span>
						</a></li>
						<c:if test="${not empty info.privateHelpURL}">
							<li class="note"><a href="${info.privateHelpURL}" target="_blanck"> <span class="wrap"> <i class="bi bi-chat"></i> <span class="count" style="display: none;">0</span>
								</span>
							</a></li>
						</c:if>
						<c:if test="${not empty integrities}">
							<li class="note"><a title="preview mode" href="${info.editTemplateURL}/integrity.jsp?path=${info.path}" class="alertintegrity"> <span class="wrap"> 
									<i class="bi bi-info-circle"></i> <span id="integrity-count"
									class="count ${integrities.errorCount == 0?'clear':'not-clear'}">${fn:length(integrities.checker)}</span>
								</span>
							</a></li>
						</c:if>
						
						<li class="note"><a href="${info.currentPreviewURL}" class="back-preview">
							<span class="wrap"> <i class="bi bi-arrow-repeat"></i></span>
							</a></li>
						
					</ul>
					<h1><a href="${info.currentViewURL}">${info.globalTitle}</a></h1>
				</div>
				<!-- topheader -->

				<!-- logo -->
				<div class="logo"><svg version="1.1" id="Calque_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" viewBox="0 0 653.4 173.8" style="enable-background:new 0 0 653.4 173.8;" xml:space="preserve">
<g>	<g>
		<path class="st0" d="M0,110v-1.3h32.6v1.3c0,20.9,10.4,33.1,32.1,33.1c21.1,0,31.5-12.3,31.5-33.1V0h33.4v110
			c0,38.8-23.2,63.9-65.2,63.9C23.2,173.9,0,148.8,0,110z"></path>
		<path class="st0" d="M296.5,38.3h34.1l38.1,107.2L407,38.3h33.4l-53.3,135.6h-37.3L296.5,38.3z"></path>
		<path class="st0" d="M459.3,10h31v164h-31V10z"></path>
		<path class="st0" d="M216.1,173.8c-0.4,0-0.9,0-1.3,0c-0.5,0-1,0-1.4,0H216.1z"></path>
		<path class="st0" d="M220.9,38.3c-38.7-0.3-69.9,29.5-70.3,68.2c-0.4,43.7,31.2,66.3,62,67.3h8.5h5.2h5.5h0v-29h0h-5.5h-5.2
			c-4.4,0-8.7-0.5-12.9-1.5c-8.8-2.5-15.8-7.8-20.4-15.1c-4.3-6.3-6.9-13.9-6.9-22.1s2.6-15.8,6.9-22.1c4.6-7.3,11.7-12.7,20.4-15.1
			c3.9-1.3,8.1-2,12.4-2c21.8,0,39.6,17.4,39.7,39v0.3v12.1v26.6v29v0.1h28.4v-66.8C288.8,65.9,261,38.6,220.9,38.3z"></path>
		<path class="st0" d="M620,92.4c1.4,4.2,2.1,8.8,2.1,13.7c0,22.7-15.1,38.8-37.8,38.8c-22.7,0-38-16.2-38-38.8
			c0-22.7,15.4-38.8,38-38.8c5.2,0,10,0.9,14.4,2.5l22.2-22.2c-10.4-5.9-22.8-9.2-36.6-9.2c-40.6,0-69.3,28.7-69.3,67.7
			s28.6,67.7,69.3,67.7c40.6,0,69.3-28.7,69.3-67.7c0-14-3.7-26.6-10.3-37.1L620,92.4z"></path>
	</g></g></svg></div>

				<div class="tabmenu">
					<ul>
						<c:forEach var="module" items="${modules}">
							<c:if test="${empty module.parent}">
								<li class="module ${module.name} ${module.name == currentModule.name || module.name == currentModule.parent?'current':''} ${module.name == fromModule.name?'from':''}"><c:url var="moduleURL" value="${info.currentURL}" context="/">
																
										<c:param name="module" value="${module.name}" />
									</c:url> <a href="${moduleURL}"><span class="ico"><i class="fa fa-${module.font}" aria-hidden="true"></i></span><span class="title">${module.title}</span> ${module.name == currentModule.name || module.name == currentModule.parent?'<div id="ajax-loader"></div>':''} <c:if
											test="${currentModule.name != module.name && module.name == currentModule.parent}">
											<span class="subname">${currentModule.title}</span>
										</c:if></a> <c:if test="${fn:length(module.children) > 0}">
										<ul class="subnav">
											<c:forEach var="submodule" items="${module.children}">
											<c:url var="moduleURL" value="${info.currentURL}" context="/"><c:param name="module" value="${submodule.name}" /></c:url>
											<li><a href="${moduleURL}"><span>${submodule.title}</span></a></li>
											</c:forEach>
										</ul>
									</c:if></li>
							</c:if>
						</c:forEach>
					</ul>
				</div>
				<!-- tabmenu -->

				<div class="accountinfo">
					<c:if test="${not empty info.currentUserAvatarUrl}">
						<img src="${info.currentUserAvatarUrl}" alt="social avatar" lang="en" />
					</c:if>
					<c:if test="${empty info.currentUserAvatarUrl}">
						<img src="${info.editTemplateURL}/images/avatar.svg" alt="default avatar" lang="en" />
					</c:if>
					<div class="info">
						<h3>${currentUser.name}</h3>
						<small>${currentUser.userInfo.email}&nbsp;</small>
						<p>
							<c:if test="${info.accountSettings}">
								<a class="account" href="${info.currentURL}?module=users&webaction=user.ChangeMode&mode=myself">${i18n.edit["global.account-setting"]}</a>
							</c:if>
							<a href="${info.currentURL}?edit-logout=logout">logout</a>
						</p>
					</div>
					<!-- info -->
				</div>
				<!-- accountinfo -->
			</div>
			<!-- header -->
		</c:if>

		<c:if test="${empty param.previewEdit}">
			<div class="sidebar">
				<div id="navigation">
					<c:forEach var="box" items="${currentModule.navigation}">
						<c:if test="${box.title != null}">
							<h3 class="open">${box.title}</h3>
						</c:if>
						<div class="content leftmenu" style="display: block;">
							<jsp:include page="${box.renderer}" />
						</div>
					</c:forEach>
					<%--<c:if test="${currentModule.helpTitle != null}">
        <h3 class="open">${currentModule.helpTitle}</h3>
        <div class="content" style="display: block;">${currentModule.helpText}</div>
        </c:if> --%>
				</div>

			</div>
			<!-- leftmenu -->
		</c:if>

		<div class="maincontent ${currentModule.name}">

			<c:if test="${currentModule.breadcrumb && empty param.nobreadcrumbs}">
				<div id="breadcrumbs" class="breadcrumbs">
					<jsp:include page="breadcrumbs.jsp" />
				</div>
			</c:if>

			<c:if test="${currentModule.sidebar}">
				<div class="two_third maincontent_inner">
			</c:if>
			<div class="left">

				<!-- ?modulesContext = ${not empty modulesContext} -->
				<!-- ?modulesContext.currentModule = ${not empty modulesContext.currentModule} -->
				<!-- currentModule = ${currentModule} -->
				<!-- currentModule.name = ${currentModule.name} -->
				<!-- currentM4odule.renderer = ${currentModule.renderer} -->
				<div id="message-container">
				
					<c:if test="${not empty globalContext.globalError}">
						<div class="alert alert-danger">${globalContext.globalError}</div>
					</c:if>
				
					<jsp:include page="message.jsp" />
				</div>

				<c:if test="${currentModule.toolsRenderer != null && info.tools}">
					<div id="tools">
						<h3>
							<span class="picto"><i class="bi bi-gear ajax-not-loading"></i>
								<span class="ajax-loader-inline"><div class="_jv_spinner-sm" role="status"><span class="sr-only" lang="en">Loading...</span></div></span>
							</span>
							${currentModule.toolsTitle}
						</h3>
						<div class="content"><jsp:include page="${currentModule.toolsRenderer}" /></div>
					</div>
				</c:if>
				<c:if test="${currentModule.renderer != null}">
					<div id="main-renderer">
						<c:if test="${not empty specialEditRenderer}"><jsp:include page="${specialEditRenderer}" /></c:if>
						<c:if test="${empty specialEditRenderer}"><jsp:include page="${currentModule.renderer}" /></c:if>
					</div>
				</c:if>
				<c:forEach var="currentBox" items="${currentModule.mainBoxes}">
					<c:set var="box" value="${currentBox}" scope="request" />
					<div class="mainBox" id="${box.id}">
						<jsp:include page="box.jsp" />
					</div>
				</c:forEach>
			</div>
			<c:if test="${currentModule.sidebar}">
		</div>
		<!-- side bar -->
</c:if>
<c:if test="${empty param.previewEdit}">
	<c:if test="${currentModule.sidebar}">
		<div class="one_third last">
			<div class="right">
				<c:forEach var="currentBox" items="${currentModule.sideBoxes}">
					<c:set var="box" value="${currentBox}" scope="request" />
					<div class="sidebox" id="${box.id}">
						<jsp:include page="box.jsp" />
					</div>
				</c:forEach>
			</div>
		</div>
	</c:if>
</c:if>
</div>
<!--maincontent-->
<br />
<c:if test="${empty param.previewEdit}">
	<div id="footer" class="footer footer_float">
		<div class="footerinner">
			<a href="http://javlo.org">javlo.org</a>
			<c:if test="${!userInterface.light}">
    		${info.currentYear} - v ${info.version} - 
    		<span id="preview-version">${info.previewVersion}</span> - 
    		<span id="server-time">${info.serverTime}</span> -   		
    		<span id="server-time">IP:${contentContext.realRemoteIp}</span>
				<c:if test="${info.localModule}">
					<span class="localmodule"><a href="${info.staticRootURL}webstart/localmodule.jnlp.jsp">Local Module</a></span>
				</c:if>
			</c:if> -
			<span id="server-time">${contentContext.globalContext.staticConfig.env}</span>
			-
			<span id="server-time">${contentContext.globalContext.staticConfig.instanceName}</span>
		</div>
		<!-- footerinner -->
	</div>
	<!-- footer -->
</c:if>
<div id="layer">&nbsp;</div>
<link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/plugins/colorbox.css" />
<link rel="stylesheet" media="print" href="${info.editTemplateURL}/css/print.css" />
<link rel="stylesheet" href="${info.editTemplateURL}/font-awesome/css/font-awesome.min.css" />

<link rel="stylesheet" href="${info.editTemplateURL}/js/plugins/datatables/Buttons-1.6.1/css/buttons.bootstrap.min.css" />

<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.alerts.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.validate.min.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.colorbox-min.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jszip.min.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/datatables/datatables.min.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/datatables/Buttons-1.6.1/js/dataTables.buttons.min.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/datatables/Buttons-1.6.1/js/buttons.html5.min.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.form.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.elastic.source.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/custom/gallery.js"></script>	

<!-- color picker -->
<script type="text/javascript" src="<jv:url value='/js/lib/colorpicker/js/colorpicker.js' />"></script>
<link rel="stylesheet" media="screen" type="text/css" href="<jv:url value='/js/lib/colorpicker/css/colorpicker.css' />" />

<!-- chosen -->
<script type="text/javascript" src="<jv:url value='/js/lib/chosen/chosen.jquery.js' />"></script>
<link rel="stylesheet" media="screen" type="text/css" href="<jv:url value='/js/lib/chosen/chosen.css' />" />

<c:if test="${not info.editLanguage eq 'en'}">
	<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.ui.datepicker-${info.editLanguage}.js"></script>
</c:if>
<script type="text/javascript" src="${info.editTemplateURL}/js/custom/general.js?ts=${info.ts}"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.autosize-min.js"></script>

<link rel="stylesheet" href="<jv:url value='/js/lib/tooltipster-master/css/tooltipster.bundle.min.css' />" />

<!-- module '${currentModule.name}' CSS -->
<c:forEach var="css" items="${currentModule.CSS}">
	<link rel="stylesheet" href="<jv:url value='${css}?ts=${info.ts}' />" />
</c:forEach>

<link href="https://fonts.googleapis.com/css?family=Roboto" rel="stylesheet">
</body>
</c:if>
</html>
