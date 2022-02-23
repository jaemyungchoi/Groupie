<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>

<head>
    <meta charset="utf-8" />
    <title>Proposal Detail</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>

<body>
    <%@include file="header.jsp" %>
    <div class="groupie content">
        <div id='proposal' data-uid='${requestScope.uid}' data-proposal='${requestScope.proposal}'
            data-error='${requestScope.error}'></div>
    </div>
    <%@include file="footer.jsp" %>
    <link rel="stylesheet" href="groupie.css">
    <script type="module" src="react-widget-static/detail-render.js"></script>
    <link async rel="stylesheet" href="https://cdn.jsdelivr.net/npm/semantic-ui@2/dist/semantic.min.css" />
</body>

</html>