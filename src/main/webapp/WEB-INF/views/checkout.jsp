<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Checkout" scope="request"/>
<%@ include file="common/header.jsp" %>

<h2 class="mb-4">Checkout</h2>

<c:choose>
    <c:when test="${empty cartItems}">
        <div class="cc-empty-state text-center py-5">
            <p class="fs-5">Your cart is empty.</p>
            <a href="${pageContext.request.contextPath}/marketplace" class="btn btn-warning">Browse Marketplace</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="row">
            <div class="col-md-8">
                <div class="table-responsive">
                    <table class="table align-middle cc-table">
                        <thead>
                        <tr><th>Item</th><th>Seller</th><th>Price</th></tr>
                        </thead>
                        <tbody>
                        <c:forEach var="item" items="${cartItems}">
                            <tr>
                                <td><c:out value="${item.title}"/></td>
                                <td><c:out value="${item.sellerName}"/></td>
                                <td>&#8377;<fmt:formatNumber value="${item.price}" minFractionDigits="2"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card cc-card">
                    <div class="card-body">
                        <p>Wallet Balance: <strong>&#8377;<fmt:formatNumber value="${walletBalance}" minFractionDigits="2"/></strong></p>
                        <p>Order Total: <strong>&#8377;<fmt:formatNumber value="${cartTotal}" minFractionDigits="2"/></strong></p>
                        <hr>
                        <c:choose>
                            <c:when test="${insufficientFunds}">
                                <p class="text-danger">Insufficient wallet balance to complete this purchase.</p>
                                <a href="${pageContext.request.contextPath}/cart" class="btn btn-outline-secondary w-100">Back to Cart</a>
                            </c:when>
                            <c:otherwise>
                                <form method="post" action="${pageContext.request.contextPath}/checkout"
                                      onsubmit="return confirm('Confirm purchase of ' + ${cartItems.size()} + ' item(s)?');">
                                    <button type="submit" class="btn btn-warning w-100">Confirm &amp; Pay</button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="common/footer.jsp" %>
