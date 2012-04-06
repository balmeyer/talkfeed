<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<div class="block">You have no blog subscribed !</div>

  		  <c:if test="${cookie.invited == null}">
				<img src="/img/puce.png"/> <a href="/inviteme.htm">get invited</a>
		</c:if>
