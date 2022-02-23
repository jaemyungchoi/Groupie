<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>

<head>
    <meta charset="utf-8" />
    <title>Create Proposal</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        .ui.label .delete.icon:hover {
            color: red;
        }
    </style>
</head>

<body>
    <%@include file="header.jsp" %>
    <div class="groupie content">
        <div class="ui container">
            <div id="tm-event" data-proposal-draft='${requestScope.draft}' data-draft-id='${requestScope.draftId}'
                data-error='${requestScope.error}'>
            </div>
        </div>
        <script type="module" src="react-widget-static/search-box-widget.js"></script>
        <link async rel="stylesheet" href="https://cdn.jsdelivr.net/npm/semantic-ui@2/dist/semantic.min.css" />
    </div>
    <%@include file="footer.jsp" %>
    <link rel="stylesheet" href="groupie.css">
</body>

</html>