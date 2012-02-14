<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <fmt:setBundle basename="com.docdoku.server.localization.explorer_resource"/>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="Content-Type"/>
        <meta name="gwt:property" content="locale=<%=request.getLocale()%>"/>
        <title><fmt:message key="title"/></title>
        <link rel="Shortcut Icon" type="image/ico" href="<%=request.getContextPath()%>/images/favicon.ico"/>

		<link rel="stylesheet/less" href="<%=request.getContextPath()%>/less/style.less">

		<script src="<%=request.getContextPath()%>/js/lib/less-1.2.1.min.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/mustache-0.5.0-dev.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/jquery-1.7.1.min.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/underscore-1.3.1.min.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/backbone-0.9.1.min.js"></script>
		<script src="<%=request.getContextPath()%>/js/lib/bootstrap-2.0.0.min.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/workspace.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/folder.js"></script>
		<script src="<%=request.getContextPath()%>/js/models/document.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/folder.js"></script>
		<script src="<%=request.getContextPath()%>/js/collections/document.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/workspace.js"></script>
		<script src="<%=request.getContextPath()%>/js/views/folder.js"></script>
		<script src="<%=request.getContextPath()%>/js/app.js"></script>
		<script type="text/javascript">
			$(document).ready(function () {
				app({
					workspaceId: "${workspaceID}",
					login: "${login}"
				});
			});
		</script>   
    </head>    
    <body>
        <%@ include file="/WEB-INF/explorer_header.jspf" %>
        <div id="workspace" class="row">
			<nav class="span4 well">
				<ul class="nav nav-list">
					<li class="nav-header">Workspace</li>
					<li>
						<ul id="folders">
						</ul>
					</li>
					<li>
					</li>
				</li>
				<li>
					<h1 class="nav-header">Autres</h1>
				</ul>
			</nav>
			<div class="content span8 well">
			</div>
        </div>
		<script id="folder-tpl" type="text/html">
			<div class="header">
				<span class="status"></span>
				<span class="icon"></span>
				<a class="name" href="#folders/{{completePath}}"
						data-toggle="collapse"
						data-target="#subfolders-{{view_cid}}">
					{{name}}
				</a>
				<div class="actions btn-group">
					<a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
						<span class="caret"></span>
					</a>
					<ul class="dropdown-menu">
						<li class="new-folder"><a href="#">Nouveau dossier</a></li>
						<li class="edit"><a href="#">Renomer</a></li>
						<li class="delete"><a href="#">Supprimer</a></li>
					</ul>
				</div>
			</div>
			<ul id="subfolders-{{view_cid}}" class="subfolders collapse"> </ul>
		</script>
		<script id="folder-new-tpl" type="text/html">
			<div class="modal new-folder">
				<div class="modal-header">
					<a class="close" data-dismiss="modal">×</a>
					<h3>Nouveau Dossier</h3>
				</div>
				<div class="modal-body">
					<form id="new-folder-form">
						<label>Nom&nbsp;:</label>
						<input class="name" type="text" value=""
							placeholder="Nom du dossier" />
					</form>
				</div>
				<div class="modal-footer">
					<button class="btn create btn-primary" for="new-folder-form">
						Créer
					</button>
					<button class="btn cancel">Annuler</button>
				</div>
			</div>
		</script>
		<script id="folder-edit-tpl" type="text/html">
			<div class="modal edit-folder">
				<div class="modal-header">
					<a class="close" data-dismiss="modal">×</a>
					<h3>Nouveau Dossier</h3>
				</div>
				<div class="modal-body">
					<form id="edit-folder-form">
						<label>Nom&nbsp;:</label>
						<input class="name" type="text" value=""
							placeholder="Nom du dossier" />
					</form>
				</div>
				<div class="modal-footer">
					<button class="btn save btn-primary" for="edit-folder-form">
						Enregistrer
					</button>
					<button class="btn cancel">Annuler</button>
				</div>
			</div>
		</script>
    </body>
</html>
