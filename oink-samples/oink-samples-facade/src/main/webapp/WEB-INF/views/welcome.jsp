<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>OINK Facade Module</title>
</head>
<body>
	<h1>OINK Facade Module v1.0</h1>
	<p>
		This is a component of the OINK System that allows <a
			href="http://www.openeyes.org.uk">OpenEyes</a> to interact with other
		components using OINK by making FHIR requests.
	</p>
	<p>This instance is configured to accept the following requests (* indicates wildcard)</p>
	<ul>
		<c:forEach items="${resources}" varStatus="loop">
		<c:url value="/fhir${resources[loop.index]}" var="url"/>
			<li><c:out value="${resources[loop.index]}" /> <c:out value="${methods[loop.index]}" /> (<a href='<c:out value="${url}"/>'>link</a>)</li>  
		</c:forEach>
	</ul>
</body>
</html>