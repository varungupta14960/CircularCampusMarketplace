<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Marketplace" scope="request"/>
<%@ include file="common/header.jsp" %>

<h2 class="mb-4">Marketplace</h2>

<form method="get" action="${pageContext.request.contextPath}/marketplace" class="row g-2 mb-4 cc-filter-bar p-3 rounded-3">
    <div class="col-md-4">
        <input type="text" class="form-control" name="q" placeholder="Search title or description..." value="${qValue}">
    </div>
    <div class="col-md-3">
        <select class="form-select" name="category">
            <option value="">All Categories</option>
            <c:forEach var="cat" items="${categories}">
                <option value="${cat}" ${cat == categoryValue ? 'selected' : ''}>${cat}</option>
            </c:forEach>
        </select>
    </div>
    <div class="col-md-2">
        <input type="number" step="0.01" min="0" class="form-control" name="minPrice" placeholder="Min ₹" value="${minPriceValue}">
    </div>
    <div class="col-md-2">
        <input type="number" step="0.01" min="0" class="form-control" name="maxPrice" placeholder="Max ₹" value="${maxPriceValue}">
    </div>
    <div class="col-md-1 d-grid">
        <button type="submit" class="btn btn-warning">Go</button>
    </div>
</form>

<c:choose>
    <c:when test="${empty listings}">
        <div class="cc-empty-state text-center py-5">
            <p class="fs-5">No listings match your search.</p>
            <a href="${pageContext.request.contextPath}/marketplace" class="btn btn-outline-secondary">Clear filters</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="row row-cols-1 row-cols-md-3 g-4">
            <c:forEach var="item" items="${listings}">
                <div class="col">
                    <div class="card h-100 cc-card">
                        <div class="card-body d-flex flex-column">
                            <span class="badge bg-secondary mb-2 align-self-start">${item.category}</span>
                            <h5 class="card-title"><c:out value="${item.title}"/></h5>
                            <p class="card-text text-muted small">Condition: ${item.itemCondition} &middot; Seller: <c:out value="${item.sellerName}"/></p>
                            <p class="card-text fw-bold fs-5 mt-auto">&#8377;<fmt:formatNumber value="${item.price}" minFractionDigits="2"/></p>
                            <a href="${pageContext.request.contextPath}/listing?id=${item.listingId}" class="btn btn-outline-primary">View Details</a>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="common/footer.jsp" %>
