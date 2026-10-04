<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Register" scope="request"/>
<%@ include file="common/header.jsp" %>

<div class="row justify-content-center">
    <div class="col-md-5">
        <div class="card cc-card">
            <div class="card-body p-4">
                <h4 class="card-title mb-3">Create your account</h4>
                <form method="post" action="${pageContext.request.contextPath}/register">
                    <div class="mb-3">
                        <label class="form-label">Full Name</label>
                        <input type="text" class="form-control" name="name" value="${nameValue}" required maxlength="100">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">College Email</label>
                        <input type="email" class="form-control" name="email" value="${emailValue}" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Password</label>
                        <input type="password" class="form-control" name="password" required minlength="8">
                        <div class="form-text">At least 8 characters.</div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Confirm Password</label>
                        <input type="password" class="form-control" name="confirmPassword" required minlength="8">
                    </div>
                    <button type="submit" class="btn btn-warning w-100">Register</button>
                </form>
                <p class="mt-3 mb-0">Already have an account? <a href="${pageContext.request.contextPath}/login">Login</a></p>
            </div>
        </div>
    </div>
</div>

<%@ include file="common/footer.jsp" %>
