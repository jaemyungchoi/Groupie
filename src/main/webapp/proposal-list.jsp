<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="csci310.model.Proposal" %>
<%@ page import="csci310.model.ProposalDraft" %>
<%@ page import="csci310.model.User" %>

<html>

<head>
    <meta charset="utf-8" />
    <title>My Proposals</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>

<body>
    <%@include file="header.jsp" %>
    <div class="groupie content">
        <div class='ui container'>
            <h1 class="ui center aligned header">My Proposals</h1>
            <div id='proposal-list' data-proposals='${requestScope.proposalList}'
                data-drafts='${requestScope.proposalDrafts}' data-error='${requestScope.error}'
                data-uid='${requestScope.uid}'></div>
        </div>
    </div>
    <%@include file="footer.jsp" %>
    <link rel="stylesheet" href="groupie.css">
    <script type="module" src="react-widget-static/proposal-list-widget.js"></script>
    <link async rel="stylesheet" href="react-widget-static/proposal-list-widget.css" />
    <link async rel="stylesheet" href="https://cdn.jsdelivr.net/npm/semantic-ui@2/dist/semantic.min.css" />
</body>

</html>