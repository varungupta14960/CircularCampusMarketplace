<%@ page isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Something Went Wrong" scope="request"/>
<%@ include file="common/header.jsp" %>

<div class="text-center py-5">
    <h1 class="display-4">Oops!</h1>
    <p class="fs-5">Something went wrong on our end. Please try again in a moment.</p>
    <a href="${pageContext.request.contextPath}/" class="btn btn-warning">Go Home</a>
</div>

<%@ include file="common/footer.jsp" %>
