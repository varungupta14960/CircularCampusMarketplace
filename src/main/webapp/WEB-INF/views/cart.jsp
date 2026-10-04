<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Your Cart" scope="request"/>
<%@ include file="common/header.jsp" %>

<h2 class="mb-4">Your Cart</h2>

<c:if test="${empty sessionScope.student}">
    <div class="alert alert-info">
        You're browsing as a guest. Your cart is saved in this browser using a cookie.
        <a href="${pageContext.request.contextPath}/login">Login</a> to check out &mdash; your items will carry over automatically.
    </div>
</c:if>

<c:choose>
    <c:when test="${empty cartItems}">
        <div class="cc-empty-state text-center py-5">
            <p class="fs-5">Your cart is empty.</p>
            <a href="${pageContext.request.contextPath}/marketplace" class="btn btn-warning">Browse Marketplace</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="table-responsive">
            <table class="table align-middle cc-table">
                <thead>
                <tr>
                    <th>Item</th>
                    <th>Category</th>
                    <th>Seller</th>
                    <th>Price</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="item" items="${cartItems}">
                    <tr>
                        <td><a href="${pageContext.request.contextPath}/listing?id=${item.listingId}"><c:out value="${item.title}"/></a></td>
                        <td>${item.category}</td>
                        <td><c:out value="${item.sellerName}"/></td>
                        <td>&#8377;<fmt:formatNumber value="${item.price}" minFractionDigits="2"/></td>
                        <td>
                            <form method="post" action="${pageContext.request.contextPath}/cart">
                                <input type="hidden" name="action" value="remove">
                                <input type="hidden" name="id" value="${item.listingId}">
                                <input type="hidden" name="redirect" value="/cart">
                                <button type="submit" class="btn btn-sm btn-outline-danger">Remove</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>

        <div class="d-flex justify-content-between align-items-center mt-4">
            <form method="post" action="${pageContext.request.contextPath}/cart" onsubmit="return confirm('Clear your entire cart?');">
                <input type="hidden" name="action" value="clear">
                <input type="hidden" name="redirect" value="/cart">
                <button type="submit" class="btn btn-outline-secondary">Clear Cart</button>
            </form>
            <div class="text-end">
                <p class="fs-4 mb-2">Total: <strong>&#8377;<fmt:formatNumber value="${cartTotal}" minFractionDigits="2"/></strong></p>
                <c:choose>
                    <c:when test="${not empty sessionScope.student}">
                        <a href="${pageContext.request.contextPath}/checkout" class="btn btn-warning btn-lg">Proceed to Checkout</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/login" class="btn btn-warning btn-lg">Login to Checkout</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="common/footer.jsp" %>
