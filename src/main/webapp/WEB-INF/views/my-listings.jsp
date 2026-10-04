<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="My Listings" scope="request"/>
<%@ include file="common/header.jsp" %>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h2>My Listings</h2>
    <a href="${pageContext.request.contextPath}/listing/create" class="btn btn-warning">+ Sell an Item</a>
</div>

<c:choose>
    <c:when test="${empty listings}">
        <div class="cc-empty-state text-center py-5">
            <p class="fs-5">You haven't listed anything yet.</p>
            <a href="${pageContext.request.contextPath}/listing/create" class="btn btn-warning">List your first item</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="table-responsive">
            <table class="table align-middle cc-table">
                <thead>
                <tr>
                    <th>Title</th>
                    <th>Category</th>
                    <th>Price</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="item" items="${listings}">
                    <tr>
                        <td><a href="${pageContext.request.contextPath}/listing?id=${item.listingId}"><c:out value="${item.title}"/></a></td>
                        <td>${item.category}</td>
                        <td>&#8377;<fmt:formatNumber value="${item.price}" minFractionDigits="2"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${item.status == 'AVAILABLE'}"><span class="badge bg-success">AVAILABLE</span></c:when>
                                <c:when test="${item.status == 'SOLD'}"><span class="badge bg-danger">SOLD</span></c:when>
                                <c:otherwise><span class="badge bg-dark">REMOVED</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:if test="${item.status == 'AVAILABLE'}">
                                <a href="${pageContext.request.contextPath}/listing/edit?id=${item.listingId}" class="btn btn-sm btn-outline-primary">Edit</a>
                                <form method="post" action="${pageContext.request.contextPath}/listing/delete" class="d-inline"
                                      onsubmit="return confirm('Remove this listing?');">
                                    <input type="hidden" name="id" value="${item.listingId}">
                                    <button type="submit" class="btn btn-sm btn-outline-danger">Remove</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="common/footer.jsp" %>
