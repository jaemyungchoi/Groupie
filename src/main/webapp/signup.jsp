<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>

<head>
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/semantic-ui@2.4.2/dist/semantic.min.css">
  <script src="https://cdn.jsdelivr.net/npm/semantic-ui@2.4.2/dist/semantic.min.js"></script>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Sign Up</title>
  <style type="text/css">
    body {
      background-color: #DADADA;
      margin: 0px;
    }

    .ui.middle.aligned.grid {
      height: 100%;
      margin-top: -6rem;
      margin-bottom: -6rem;
    }

    .column {
      max-width: 450px;
    }
  </style>
</head>

<body>
  <div class="groupie content">
    <div class="ui middle aligned center aligned grid">
      <div class="column">
        <h1 class="ui red header">
          Create your account
        </h1>
        <form class="ui large form${requestScope.form_error ? ' error' : ''}" action="/signup" method="post">
          <div class="ui segment">
            <div class="field${requestScope.username_error ? ' error' : ''}">
              <div class="ui left icon input">
                <i class="user icon"></i>
                <input type="text" name="username" placeholder="Name" value="${requestScope.username}">
              </div>
            </div>
            <div class="field${requestScope.password_error ? ' error' : ''}">
              <div class="ui left icon input">
                <i class="lock icon"></i>
                <input type="password" name="password" placeholder="Password">
              </div>
            </div>
            <div class="field${requestScope.password_error ? ' error' : ''}">
              <div class="ui left icon input">
                <i class="lock icon"></i>
                <input type="password" name="password2" placeholder="Confirm Password">
              </div>
            </div>
            <button class="ui fluid large red submit button" type="submit">Create User</button>
            <div class="ui horizontal divider">
              Or
            </div>
            <a class="ui fluid large button" href="/login">Cancel</a>
          </div>
          <div class="ui error message">
            <ul class="list">
              <c:forEach items="${error_messages}" var="message">
                <li>${message}</li>
              </c:forEach>
            </ul>
          </div>
        </form>
      </div>
    </div>
  </div>
  <%@include file="footer.jsp" %>
  <link rel="stylesheet" href="groupie.css">
</body>

</html>