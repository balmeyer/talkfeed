/*
 Copyright 2010 - Jean-Baptiste Vovau

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package talkfeed.talk;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.MessageType;
import com.google.appengine.api.xmpp.Presence;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

public class TalkService {

	
	/** no instance */
	private TalkService(){}
	
	/**
	 *
	 */
	public static final void sendMessage(String id, String msg){
		
		sendMessage(new JID(id), msg);
		
	}
	
	/**
	 * 
	 * @param jid
	 * @param msg
	 */
	public static final void sendMessage(JID jid, String msg){
		
		XMPPService xmpp = XMPPServiceFactory.getXMPPService();
		
		Presence presence = xmpp.getPresence(jid);
		
		if (presence.isAvailable()){
			
			MessageBuilder mb = new MessageBuilder();
			Message reply = mb.withRecipientJids(jid)
				.withMessageType(MessageType.CHAT)
				.withBody(msg)
				.build();
			
			
			
			xmpp.sendMessage(reply);
		}
	}
	
	/**
	 * Invte User
	 * @param jid
	 */
	public static final void invite(JID jid){
		XMPPService xmpp = XMPPServiceFactory.getXMPPService();
		
		xmpp.sendInvitation(jid);
	}
	
	public static final Presence getPresence(JID jid) {
		XMPPService xmpp = XMPPServiceFactory.getXMPPService();
		
		return xmpp.getPresence(jid);
	}
	
	public static final Message parseMessage(HttpServletRequest req) throws IOException{
		XMPPService xmpp = XMPPServiceFactory.getXMPPService();
		return xmpp.parseMessage(req);
	}
	
}
