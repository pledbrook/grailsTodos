<!doctype html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <title><g:layoutTitle default="Grails"/></title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon">
  <link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
  <link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-retina.png')}">
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}" type="text/css">
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'mobile.css')}" type="text/css">
  <r:require module="todos"/>
  <g:layoutHead/>
  <r:layoutResources/>
  <r:script>
$(function() {
  if (!window.grailsEvents) windows.grailsEvents = new grails.Events('${createLink(uri: '')}', null, {transport:'long-polling'});

  grailsEvents.on("packageWarStart", function(data) {
    $("#deployBox h2").html("Packaging the application");
  });

  grailsEvents.on("packageWarStatus", function(data) {
    $("#deployBox pre").text(data);
  });

  grailsEvents.on("deployStart", function(data) {
    $("#deployBox h2").html("Pushing application to Cloud Foundry...");
    $("#deployBox pre").text('');
  });

  grailsEvents.on("deployEnd", function(data) {
    $("#deployBox h2").html("Application now running <a href=\"" + data + "\">on Cloud Foundry</a>");
    $("#deployBox pre").text('');
  });
});
  </r:script>
  <g:setProvider library="jquery"/>
</head>

<body>
<div id="grailsLogo" role="banner"><a href="http://grails.org"><img
        src="${resource(dir: 'images', file: 'grails_logo.png')}" alt="Grails"/></a>
<g:hasAccessToken provider="uaa">
You are logged into Cloud Foundry and can <g:remoteLink controller="cloudFoundry" action="deploy">deploy the app</g:remoteLink>!
</g:hasAccessToken>
<g:lacksAccessToken provider="uaa">
<oauth:connect provider="uaa">Connect to Cloud Foundry</oauth:connect>
</g:lacksAccessToken>
</div>
<div id="deployBox">
    <h2></h2>
    <pre></pre>
</div>

<g:layoutBody/>
<div class="footer" role="contentinfo"></div>

<div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
<r:layoutResources/>
</body>
</html>
