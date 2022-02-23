<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="csci310.model.User" %>

<html>

<head>
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/semantic-ui@2.4.2/dist/semantic.min.css">
  <script src="https://cdn.jsdelivr.net/npm/semantic-ui@2.4.2/dist/semantic.min.js"></script>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Block List</title>
</head>

<body>
  <%@include file="header.jsp" %>
  <div class="groupie content">
    <div class="ui padded text container">
      <div class="ui raised segments">
        <div class="ui padded segment">
          <h1>Block List</h1>
          <p>Users in your block list cannot send you proposal invitations.</p>
        </div>
        <div class="ui padded segment">
          <c:if test="${empty requestScope.block_list}">
            <div class="ui message">
              <div class="header">Your Block List is Empty</div>
              <p>Add people to your block list using the input below.</p>
            </div>
          </c:if>
          <c:if test="${not empty requestScope.block_list}">
            <table class="ui very basic celled table">
              <tbody>
                <c:forEach var="blockedUser" items="${requestScope.block_list}">
                  <tr>
                    <td>
                      <form class="ui form" action="/block-list" method="post" style="margin-bottom: 0;">
                        <div class="fields" style="margin-bottom: 0;">
                          <div class="thirteen wide field">
                            <p>${blockedUser.getUsername()}</p>
                          </div>
                          <div class="three wide field">
                            <input type="hidden" name="blocked-user" value="${blockedUser.getId()}" />
                            <input type="hidden" name="action" value="unblock" />
                            <button class="ui right floated small red icon button" type="submit">
                              <i class="close icon"></i>
                            </button>
                          </div>
                        </div>
                      </form>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </c:if>
        </div>
        <div class="ui padded segment">
          <form class='ui form${requestScope.error_message != null ? " error" : ""}' action="/block-list" method="post">
            <div class="fields">
              <div class="thirteen wide field">
                <div class="inline fields">
                  <label>Name</label>
                  <input type="text" placeholder="John Doe" name="blocked-user">
                </div>
              </div>
              <div class="three wide field">
                <input type="hidden" name="action" value="block" />
                <button class="ui green submit fluid button" type="submit">Add</button>
              </div>
            </div>
            <div class="ui error message">
              <p>${requestScope.error_message}</p>
            </div>
        </div>
      </div>
    </div>
    </div>
  </div>
  <%@include file="footer.jsp" %>
  <link rel="stylesheet" href="groupie.css">
</body>

</html>