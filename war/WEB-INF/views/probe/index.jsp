<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<b>hello !</b>

<div>Active users : ${nbusers}</div>
<div>Active blogs : ${nbactiveblogs}</div>


<h2>actions</h2>

<div>
<a href="/tasks/activeblogs" target="_task">active blogs</a>
</div>
<div>
<a href="/tasks/refreshblogs" target="_task">refresh blogs</a>
</div>
<div>
<a href="/tasks/refreshsubscriptions" target="_task">refresh subscriptions</a>
</div>

<hr/>


<h2>détails active users</h2>

<c:forEach items="${users}" var="user">
	<div>${user}</div>
</c:forEach>


<h2>users to update</h2>

<c:forEach items="${usersToUpdate}" var="uu">
	<div>${uu}</div>
</c:forEach>

<hr/>


<h2>détails active blogs</h2>

<c:forEach items="${blogs}" var="blog">
	<div>${blog}</div>
</c:forEach>

<h2>détails active blogs to update</h2>
<c:forEach items="${blogstoupdate}" var="blog">
	<div>${blog}</div>
</c:forEach>



