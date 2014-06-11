<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
<%@ taglib uri="http://liferay.com/tld/security" prefix="liferay-security" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://liferay.com/tld/util" prefix="liferay-util" %>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>

<portlet:defineObjects />


This is the <b>Remote Staging Re Import</b> portlet in View mode.

<liferay-ui:success key="success" message="success" />

<portlet:actionURL name="reImport" var="reImportAction">
</portlet:actionURL>

<form method="post" action="<%=reImportAction %>" >
	<h1>LAR FILE:</h1>
	<input type="text" name="<portlet:namespace/>filenameAndPath" value=""/>
	<h1>Property file:</h1>
 	<input type="text" name="<portlet:namespace/>propertiesFileNameAndPath" value=""/>
 	<h1>remoteGroupId</h1>
 	<input type="text" name="<portlet:namespace/>remoteGroupId" value=""/>
 	<h1>privateLayout</h1>
 	<input type="checkbox" name="<portlet:namespace/>privateLayout" value="false"/>
 	<input type="submit" value="Do it" />
</form>