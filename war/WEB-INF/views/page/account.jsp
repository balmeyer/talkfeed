<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<div class="block">
	<a href="/disconnect.htm">disconnect</a>

</div>


<div class="block">
	Your subscriptions : <br/>
	<c:forEach items="${blogs}" var="blog">
		<div class="blog" id="blog_${blog.key.id}">
			<a href="javascript:unsubscribe(${blog.key.id});"><img src="/img/close.png" border="0"></a>
			<a href="javascript:loadPosts(${blog.key.id});" target="out">${blog.label}</a> <a href="${blog.link}" target="out"><img src="/img/new-window.png" border="0"/></a>
			<div class="lastDate">last update : ${blog.lastUpdate }</div>
			
			<div id="posts_${blog.key.id}" class="posts"></div>
		</div>
	</c:forEach>
</div>

<div class="block">
	<div id="invitation"><a href="javascript:invite();"><img src="/img/signup.gif"/></a></div>
</div>