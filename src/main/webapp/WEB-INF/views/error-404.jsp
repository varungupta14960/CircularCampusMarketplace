<%@ page isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Page Not Found" scope="request"/>
<%@ include file="common/header.jsp" %>

<div class="text-center py-5">
    <h1 class="display-4">404</h1>
    <p class="fs-5">The page you're looking for doesn't exist.</p>
    <a href="${pageContext.request.contextPath}/" class="btn btn-warning">Go Home</a>
</div>

<%@ include file="common/footer.jsp" %>
