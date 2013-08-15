<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page isELIgnored="false" %>
<%@ page contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>


<ul class="list-group">
	<c:forEach items="${posts}" var="p">
		<li id="post_${p.key.id}" class="list-group-item">
			<p class="text-left">
			<a href="${p.link}" target="out">${p.title}</a> 
			
			<span class="label label-default">
			<fmt:formatDate value="${p.pubDate}" pattern="yyyy-MM-dd hh:mm:ss" />
			</span>
			</p>
		</li>
	</c:forEach>
</ul>