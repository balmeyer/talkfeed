package talkfeed.web.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import talkfeed.api.ApiManager;

import com.google.gson.Gson;

/**
 * API for RSS reading
 * @author balmeyer
 *
 */
public class ApiServlet extends HttpServlet  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Get api connection
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		
		//get type of action
		String action = req.getPathInfo();
		
		if(action == null) return;
		
		action = action.replace("/","");
		
		System.out.println(action);
		
		//get user
		String user = req.getParameter("u");
		if (user == null) return;
		
		ApiManager api = new ApiManager(user);
		
		Object data = null;
		
		try {
			data = api.request(action);
		} finally {
		
		//api.close();
		}
		
		if (data != null) {
			
			Gson gson = new Gson();
			
			String json = gson.toJson(data);
			
			resp.getWriter().write(json);
		}
		
		resp.flushBuffer();
	}
	
	
	private Object getFeedList(String user){
		return user;
	}
	

}
