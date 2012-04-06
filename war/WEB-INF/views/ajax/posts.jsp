<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page isELIgnored="false" %>
<%@ page contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>


<div>
	<c:forEach items="${posts}" var="p">
		<div id="post_${p.key.id}" class="post">
			<a href="${p.link}" target="out">${p.title}</a> 
			<div class="lastDate"><fmt:formatDate value="${p.pubDate}" pattern="yyyy-MM-dd hh:mm:ss"/></div>
		</div>
	</c:forEach>
</div>