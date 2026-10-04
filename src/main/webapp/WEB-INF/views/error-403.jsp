<%@ page isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Access Denied" scope="request"/>
<%@ include file="common/header.jsp" %>

<div class="text-center py-5">
    <h1 class="display-4">403</h1>
    <p class="fs-5">You don't have permission to do that.</p>
    <a href="${pageContext.request.contextPath}/" class="btn btn-warning">Go Home</a>
</div>

<%@ include file="common/footer.jsp" %>
