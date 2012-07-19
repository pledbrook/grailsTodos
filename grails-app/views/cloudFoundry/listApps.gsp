<head>
  <title>Your applications on Cloud Foundry</title>
  <meta name="layout" content="main">
</head>
<body>
  <table>
    <tr>
      <th>Application name</th>
      <th>Status</th>
    </tr>
    <g:each in="${apps}" var="app">
    <tr>
      <td>${app.name.encodeAsHTML()}</td>
      <td>${app.state}</td>
    </tr>
    </g:each>
</body>
