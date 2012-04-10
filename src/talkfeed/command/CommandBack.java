package talkfeed.command;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import talkfeed.data.DataManager;
import talkfeed.data.DataManagerFactory;
import talkfeed.data.Subscription;
import talkfeed.data.User;
import talkfeed.gtalk.TalkService;

@CommandType("back")
public class CommandBack implements Command {

	@Override
	public void execute(Map<String, String> args) {
		
		//get args
		String time = args.get("arg2");
		String unit = args.get("arg3");
		String jid = args.get("id");
		
		if (time == null) return;
		
		//get unit time
		if (unit == null) unit = "minutes";
		
		int coef = 1;
		int minutes = 0;
		
		if (unit.toLowerCase().startsWith("hour")){
			coef = 60;
		}
		
		if (unit.toLowerCase().startsWith("day")){
			coef = 60 * 24;
		}
		
		try {
			minutes = Integer.parseInt(time);
		} catch (Exception ex){
			return;
		}
		
		//number of minutes to rollback
		minutes = minutes * coef;
		
		//got to last pubDate
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -minutes);
		Date lastPubDate = cal.getTime();
		
		//last process date
		Calendar cal2 = Calendar.getInstance();
		cal2.add(Calendar.HOUR, -24);
		Date lastProcess = cal2.getTime();
		
		DataManager dm = DataManagerFactory.getInstance();
		PersistenceManager pm = dm.newPersistenceManager();
		
		User user = dm.getUserFromId(jid);
		
		Query q = pm.newQuery(Subscription.class);
		q.setFilter("userKey == uk");
		q.declareParameters("com.google.appengine.api.datastore.Key uk");
		
		@SuppressWarnings("unchecked")
		List<Subscription> list = (List<Subscription>) q.execute(user.getKey());
		
		
		for(Subscription s : list){
			pm.currentTransaction().begin();
			s.setLatestEntryNotifiedDate(lastPubDate);
			s.setLastProcessDate(lastProcess);
			pm.currentTransaction().commit();
			//TODO do something about lastProcessDate to push
		}
		
		pm.flush();
		
		pm.close();
		
		TalkService.sendMessage(user.getId(), "rollback done : " + time + " " + unit);

	}

}
