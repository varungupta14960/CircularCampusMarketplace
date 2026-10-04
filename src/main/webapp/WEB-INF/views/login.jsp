<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Login" scope="request"/>
<%@ include file="common/header.jsp" %>

<div class="row justify-content-center">
    <div class="col-md-5">
        <div class="card cc-card">
            <div class="card-body p-4">
                <h4 class="card-title mb-3">Login</h4>
                <form method="post" action="${pageContext.request.contextPath}/login">
                    <c:if test="${not empty param.returnTo}">
                        <input type="hidden" name="returnTo" value="${param.returnTo}">
                    </c:if>
                    <div class="mb-3">
                        <label class="form-label">Email</label>
                        <input type="email" class="form-control" name="email" value="${emailValue}" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Password</label>
                        <input type="password" class="form-control" name="password" required>
                    </div>
                    <button type="submit" class="btn btn-warning w-100">Login</button>
                </form>
                <p class="mt-3 mb-0">New here? <a href="${pageContext.request.contextPath}/register">Create an account</a></p>
                <p class="text-muted small mt-2 mb-0">Demo accounts: student1@example.com ... student5@example.com, password <code>Password123!</code></p>
            </div>
        </div>
    </div>
</div>

<%@ include file="common/footer.jsp" %>
