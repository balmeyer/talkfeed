<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page isELIgnored="false" %>

<div class="row">

  		  <c:if test="${cookie.username == null && cookie.invited == null}">
				<img src="/img/puce.png"/> <a href="/inviteme.htm">get invited</a>
		</c:if>
<c:if test="${showInvitation == false}">
	Your subscriptions : <br/>
</c:if>

	
	<table class="table table-condensed table-striped">
		<tr>
			<th>site</th>
			<th>last update</th>
			<th></th>
			<th></th>
		</tr>
	<c:forEach items="${blogs}" var="blog">
		<tr id="blog_${blog.key.id}">

			<td>
				<a href="javascript:loadPosts(${blog.key.id});" class="btn btn-primary btn-sm">posts</a>
			<a href="javascript:loadPosts(${blog.key.id});" target="out">${blog.label}</a>
			<a href="${blog.link}" target="out"><img src="/img/new-window.png" border="0"/></a>
			
			<div id="posts_${blog.key.id}"></div>
			
			</td>
			
			<td><fmt:formatDate value="${blog.latestEntry}" pattern="yyyy-MM-dd"/>
			</td>

			<td>
			</td>
						<td>
			<a href="javascript:unsubscribe(${blog.key.id});">
				<button type="button" class="close" aria-hidden="true">&times;</button>
			</a>
			</td>
			
		</tr>
		
	</c:forEach>
	</table>
</div>
