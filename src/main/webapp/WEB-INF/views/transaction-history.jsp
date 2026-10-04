<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Transaction History" scope="request"/>
<%@ include file="common/header.jsp" %>

<h2 class="mb-4">Transaction History</h2>

<ul class="nav nav-tabs mb-3" id="txnTabs" role="tablist">
    <li class="nav-item" role="presentation">
        <button class="nav-link active" data-bs-toggle="tab" data-bs-target="#purchasesTab" type="button">Purchases</button>
    </li>
    <li class="nav-item" role="presentation">
        <button class="nav-link" data-bs-toggle="tab" data-bs-target="#salesTab" type="button">Sales</button>
    </li>
</ul>

<div class="tab-content">
    <div class="tab-pane fade show active" id="purchasesTab">
        <c:choose>
            <c:when test="${empty purchases}">
                <p class="text-muted">You haven't purchased anything yet.</p>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="table cc-table">
                        <thead><tr><th>Item</th><th>Seller</th><th>Amount Paid</th><th>Date</th></tr></thead>
                        <tbody>
                        <c:forEach var="txn" items="${purchases}">
                            <tr>
                                <td><c:out value="${txn.listingTitle}"/></td>
                                <td><c:out value="${txn.sellerName}"/></td>
                                <td>&#8377;<fmt:formatNumber value="${txn.amount}" minFractionDigits="2"/></td>
                                <td><fmt:formatDate value="${txn.txnDate}" pattern="dd MMM yyyy, HH:mm"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="tab-pane fade" id="salesTab">
        <c:choose>
            <c:when test="${empty sales}">
                <p class="text-muted">You haven't sold anything yet.</p>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="table cc-table">
                        <thead><tr><th>Item</th><th>Buyer</th><th>Amount Received</th><th>Date</th></tr></thead>
                        <tbody>
                        <c:forEach var="txn" items="${sales}">
                            <tr>
                                <td><c:out value="${txn.listingTitle}"/></td>
                                <td><c:out value="${txn.buyerName}"/></td>
                                <td>&#8377;<fmt:formatNumber value="${txn.amount}" minFractionDigits="2"/></td>
                                <td><fmt:formatDate value="${txn.txnDate}" pattern="dd MMM yyyy, HH:mm"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="common/footer.jsp" %>
