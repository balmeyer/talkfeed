<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page isELIgnored="false" %>

<div class="block">
  		  <c:if test="${cookie.username == null && cookie.invited == null}">
				<img src="/img/puce.png"/> <a href="/inviteme.htm">get invited</a>
		</c:if>
<c:if test="${showInvitation == false}">
	Your subscriptions : <br/>
</c:if>
	<c:forEach items="${blogs}" var="blog">
		<div class="blog" id="blog_${blog.key.id}">
			<a href="javascript:unsubscribe(${blog.key.id});"><img src="/img/close.png" border="0"></a>
			<a href="javascript:loadPosts(${blog.key.id});" target="out">${blog.label}</a> <a href="${blog.link}" target="out"><img src="/img/new-window.png" border="0"/></a>
			<div class="lastDate">last update : <fmt:formatDate value="${blog.lastUpdate}" pattern="yyyy-MM-dd"/>
			</div>
			
			
			<div id="posts_${blog.key.id}" class="posts"></div>
		</div>
	</c:forEach>
</div>

