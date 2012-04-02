package talkfeed;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import talkfeed.data.DataManagerFactory;
import talkfeed.data.User;
import talkfeed.data.UserMark;


/**
 * Send subscription via user
 * @author vovau
 *
 */
public class UserService {

	private static final String USERMARK_ID = "1";
	
	public void updateUsers(int nbMax){
		
		PersistenceManager pm = DataManagerFactory.getInstance().newPersistenceManager();
		
		//find mark
		long lastId = 1;
		Query qm = pm.newQuery(UserMark.class);
		qm.setRange(0, 1);
		qm.setUnique(true);
		UserMark um = (UserMark) qm.execute();
		if (um == null){
			um = new UserMark();
			um.setId(USERMARK_ID);
			um.setLastId(1);
			pm.makePersistent(um);
		}
		
		lastId = um.getLastId();
		if (lastId <1) lastId = 1;
		
		//find user
		Query q = pm.newQuery(User.class);
		q.setFilter("key >= lastId");
		q.setOrdering("key");
		q.declareParameters("Long lastId");
		q.setRange(0 , nbMax);
		
		//list user
		boolean hasListed = false;
		List<User> list = (List<User>) q.execute(lastId);
		
		for(User u : list) {
			hasListed = true;
			lastId = u.getKey().getId() + 1;
		}
		
		//no one listed : start to beginning
		if (!hasListed) lastId = 1;
		
		//end of process
		um.setLastId(lastId);
		pm.flush();
		pm.close();
		
	}
	
}
