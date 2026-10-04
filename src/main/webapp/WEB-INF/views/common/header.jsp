<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${not empty pageTitle ? pageTitle : 'CampusCycle'}" /> | CampusCycle</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>

<nav class="navbar navbar-expand-lg navbar-dark cc-navbar">
    <div class="container">
        <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/">
            &#9851; CampusCycle
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navMenu">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navMenu">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/marketplace">Marketplace</a></li>
                <c:if test="${not empty sessionScope.student}">
                    <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/listing/create">Sell an Item</a></li>
                    <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/my-listings">My Listings</a></li>
                    <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/transactions">Transactions</a></li>
                </c:if>
            </ul>
            <ul class="navbar-nav align-items-lg-center">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/cart">
                        &#128722; Cart
                        <c:if test="${not empty sessionScope.student and not empty sessionScope.cart}">
                            <span class="badge bg-light text-dark">${sessionScope.cart.size()}</span>
                        </c:if>
                    </a>
                </li>
                <c:choose>
                    <c:when test="${not empty sessionScope.student}">
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/profile">
                                &#128100; ${sessionScope.student.name} &middot;
                                &#8377;<fmt:formatNumber value="${sessionScope.student.walletBalance}" minFractionDigits="2" maxFractionDigits="2"/>
                            </a>
                        </li>
                        <li class="nav-item">
                            <form action="${pageContext.request.contextPath}/logout" method="post" class="d-inline">
                                <button type="submit" class="btn btn-sm btn-outline-light ms-2">Logout</button>
                            </form>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/login">Login</a></li>
                        <li class="nav-item">
                            <a class="btn btn-sm btn-warning ms-2" href="${pageContext.request.contextPath}/register">Register</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
</nav>

<div class="container py-4">
    <%@ include file="alerts.jsp" %>
