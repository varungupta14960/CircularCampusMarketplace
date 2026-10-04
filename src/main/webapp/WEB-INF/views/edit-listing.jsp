<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Edit Listing" scope="request"/>
<%@ include file="common/header.jsp" %>

<div class="row justify-content-center">
    <div class="col-md-7">
        <div class="card cc-card">
            <div class="card-body p-4">
                <h4 class="card-title mb-3">Edit listing</h4>
                <form method="post" action="${pageContext.request.contextPath}/listing/edit?id=${listing.listingId}">
                    <div class="mb-3">
                        <label class="form-label">Title</label>
                        <input type="text" class="form-control" name="title" value="${listing.title}" required maxlength="150">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Description</label>
                        <textarea class="form-control" name="description" rows="4">${listing.description}</textarea>
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Category</label>
                            <select class="form-select" name="category" required>
                                <c:forEach var="cat" items="${categories}">
                                    <option value="${cat}" ${cat == listing.category ? 'selected' : ''}>${cat}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Condition</label>
                            <select class="form-select" name="condition" required>
                                <c:forEach var="cond" items="${conditions}">
                                    <option value="${cond}" ${cond == listing.itemCondition ? 'selected' : ''}>${cond}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Price (&#8377;)</label>
                        <input type="number" step="0.01" min="0.01" class="form-control" name="price" value="${listing.price}" required>
                    </div>
                    <button type="submit" class="btn btn-warning" ${listing.status != 'AVAILABLE' ? 'disabled' : ''}>Save Changes</button>
                    <a href="${pageContext.request.contextPath}/my-listings" class="btn btn-outline-secondary">Cancel</a>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="common/footer.jsp" %>
