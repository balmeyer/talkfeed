<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>


<div>
	<c:forEach items="${posts}" var="p">
		<div id="post_${p.key.id}" class="post">
			<a href="${p.link}" target="out">${p.title}</a> 
			<div class="lastDate">${p.pubDate }</div>
		</div>
	</c:forEach>
</div>