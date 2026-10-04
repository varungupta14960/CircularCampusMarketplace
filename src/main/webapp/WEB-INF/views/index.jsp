<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Home" scope="request"/>
<%@ include file="common/header.jsp" %>

<div class="cc-hero rounded-4 p-5 mb-5 text-center">
    <h1 class="display-5 fw-bold">CampusCycle</h1>
    <p class="lead">Give Your Stuff a Second Life.</p>
    <p class="mb-4">Buy and sell used books, gadgets, hostel essentials, furniture and more &mdash; right within campus.</p>
    <a href="${pageContext.request.contextPath}/marketplace" class="btn btn-warning btn-lg me-2">Browse Marketplace</a>
    <c:if test="${empty sessionScope.student}">
        <a href="${pageContext.request.contextPath}/register" class="btn btn-outline-light btn-lg">Join CampusCycle</a>
    </c:if>
</div>

<c:if test="${not empty recentlyViewed}">
    <h4 class="mb-3">Recently Viewed</h4>
    <div class="row row-cols-1 row-cols-md-4 g-3 mb-4">
        <c:forEach var="item" items="${recentlyViewed}">
            <div class="col">
                <div class="card h-100 cc-card">
                    <div class="card-body">
                        <span class="badge bg-secondary mb-2">${item.category}</span>
                        <h6 class="card-title"><c:out value="${item.title}"/></h6>
                        <p class="card-text fw-bold">&#8377;<fmt:formatNumber value="${item.price}" minFractionDigits="2"/></p>
                        <a href="${pageContext.request.contextPath}/listing?id=${item.listingId}" class="btn btn-sm btn-outline-primary">View</a>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
</c:if>

<div class="row text-center g-4 mt-2">
    <div class="col-md-4">
        <h5>&#128218; Sell What You Don't Need</h5>
        <p>List old textbooks, gadgets and hostel essentials in minutes.</p>
    </div>
    <div class="col-md-4">
        <h5>&#128179; Campus Wallet</h5>
        <p>Buy and sell using your in-app wallet &mdash; no external payment gateway needed.</p>
    </div>
    <div class="col-md-4">
        <h5>&#127807; Sustainability Points</h5>
        <p>Earn points every time you buy or sell &mdash; and help keep good stuff in circulation.</p>
    </div>
</div>

<%@ include file="common/footer.jsp" %>
