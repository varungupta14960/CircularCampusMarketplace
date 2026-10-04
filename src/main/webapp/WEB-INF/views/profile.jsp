<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Profile" scope="request"/>
<%@ include file="common/header.jsp" %>

<h2 class="mb-4">My Profile</h2>

<div class="row justify-content-center">
    <div class="col-md-6">
        <div class="card cc-card">
            <div class="card-body p-4">
                <h4 class="mb-1"><c:out value="${profileStudent.name}"/></h4>
                <p class="text-muted"><c:out value="${profileStudent.email}"/></p>
                <hr>
                <div class="row text-center">
                    <div class="col-6">
                        <p class="text-muted mb-1">Wallet Balance</p>
                        <p class="fs-3 fw-bold">&#8377;<fmt:formatNumber value="${profileStudent.walletBalance}" minFractionDigits="2"/></p>
                    </div>
                    <div class="col-6">
                        <p class="text-muted mb-1">Sustainability Points</p>
                        <p class="fs-3 fw-bold">&#127807; ${profileStudent.sustainabilityPoints}</p>
                    </div>
                </div>
                <hr>
                <div class="d-grid gap-2">
                    <a href="${pageContext.request.contextPath}/my-listings" class="btn btn-outline-primary">My Listings</a>
                    <a href="${pageContext.request.contextPath}/transactions" class="btn btn-outline-primary">Transaction History</a>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="common/footer.jsp" %>
