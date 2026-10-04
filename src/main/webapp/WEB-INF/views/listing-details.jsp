<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Listing Details" scope="request"/>
<%@ include file="common/header.jsp" %>

<c:choose>
    <c:when test="${empty listing}">
        <div class="cc-empty-state text-center py-5">
            <p class="fs-5">This listing is not available.</p>
            <a href="${pageContext.request.contextPath}/marketplace" class="btn btn-outline-secondary">Back to Marketplace</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="row">
            <div class="col-md-7">
                <span class="badge bg-secondary mb-2">${listing.category}</span>
                <c:choose>
                    <c:when test="${listing.status == 'SOLD'}">
                        <span class="badge bg-danger mb-2">SOLD</span>
                    </c:when>
                    <c:when test="${listing.status == 'REMOVED'}">
                        <span class="badge bg-dark mb-2">REMOVED</span>
                    </c:when>
                </c:choose>
                <h2><c:out value="${listing.title}"/></h2>
                <p class="text-muted">Condition: ${listing.itemCondition} &middot; Listed by <c:out value="${listing.sellerName}"/></p>
                <p class="fs-3 fw-bold">&#8377;<fmt:formatNumber value="${listing.price}" minFractionDigits="2"/></p>
                <p><c:out value="${listing.description}"/></p>
            </div>
            <div class="col-md-5">
                <div class="card cc-card">
                    <div class="card-body">
                        <h5 class="card-title">Interested?</h5>
                        <c:choose>
                            <c:when test="${listing.status != 'AVAILABLE'}">
                                <p class="text-muted">This item is no longer available for purchase.</p>
                            </c:when>
                            <c:when test="${not empty sessionScope.student and sessionScope.student.studentId == listing.sellerId}">
                                <p class="text-muted">This is your own listing.</p>
                                <a href="${pageContext.request.contextPath}/listing/edit?id=${listing.listingId}" class="btn btn-outline-primary w-100">Edit Listing</a>
                            </c:when>
                            <c:otherwise>
                                <form method="post" action="${pageContext.request.contextPath}/cart">
                                    <input type="hidden" name="action" value="add">
                                    <input type="hidden" name="id" value="${listing.listingId}">
                                    <input type="hidden" name="redirect" value="/listing?id=${listing.listingId}">
                                    <button type="submit" class="btn btn-warning w-100">Add to Cart</button>
                                </form>
                                <c:if test="${empty sessionScope.student}">
                                    <p class="text-muted small mt-2 mb-0">Browsing as guest &mdash; your cart is saved in this browser. <a href="${pageContext.request.contextPath}/login">Login</a> to check out.</p>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="common/footer.jsp" %>
