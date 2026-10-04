<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:if test="${not empty error}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <c:out value="${error}" />
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
</c:if>
<c:if test="${not empty success}">
    <div class="alert alert-success alert-dismissible fade show" role="alert">
        <c:out value="${success}" />
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
</c:if>
