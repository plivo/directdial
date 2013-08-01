package com.plivo.directdial;

import com.plivo.helper.PlivoException;
import com.plivo.helper.xml.elements.Dial;
import com.plivo.helper.xml.elements.User;
import com.plivo.helper.xml.elements.Number;
import com.plivo.helper.xml.elements.Hangup;
import com.plivo.helper.xml.elements.PlivoResponse;

import static spark.Spark.*;
import spark.*;

public class App {
	public static void main(String[] args) {
		get(new Route("/response/sip/route/") {
				@Override
				public Object handle(Request request, Response response) {
					// request parameters
					String toNumber    = request.queryParams("ForwardTo");
					String fromNumber  = request.queryParams("CLID");
					String callerName  = request.queryParams("CallerName");
					String hangupCause = request.queryParams("HangupCause");
					String disableCall = request.queryParams("DisableCall");
					String dialMusic   = request.queryParams("DialMusic");

					// flags
					boolean isSipUser = false;

					// if 'ForwardTo' is not set specifically use 'To'.
					// caveat: if 'ForwardTo' is not set when app is linked 
					// to a number, it would a loop.
					if ( toNumber.isEmpty() ) {
						toNumber  = request.queryParams("To");
					}

					// if caller ID is not set specifically use 'From'
					if ( fromNumber.isEmpty() ) {
						fromNumber  = request.queryParams("From");
					}
                  
					// Plivo XML elements
					PlivoResponse plivoResponse = new PlivoResponse();

					Hangup hangup = new Hangup();
					hangup.setReason("busy");

					Dial dial = new Dial();
					Number number ;
					User sipUser ;
					// if invoked as a hangup_url send a text response
					if ( !hangupCause.isEmpty() ) {
						return "SIP Route hangup callback";
					}
                  
					try {
						// if no destination nunber is available, hang up.
						if ( !toNumber.isEmpty() ) {
							System.out.println("SIP Route cannot identify destination number");
							plivoResponse.append(hangup);
						} else {
							if ( toNumber.length() > 4 && toNumber.substring(4) == "sip:" ) {
								isSipUser = true;
							}
							// check for outbound call disable status - all, sip or number
							if ( isSipUser && disableCall == "all" || disableCall == "sip" ) {
								System.out.printf("SIP Route calling sip user is disabled : %s", disableCall);
								plivoResponse.append(hangup);
							} else if ( !isSipUser && disableCall == "all" || disableCall == "number" ) {
								System.out.printf("SIP Route calling number is disabled : %s", disableCall);
								plivoResponse.append(hangup);
							} else {
								System.out.printf("SIP Route dialing %s", toNumber);
								if ( !dialMusic.isEmpty() && !isSipUser ) {
									dial.setCallerName(callerName);
									dial.setCallerId(fromNumber);
									dial.setDialMusic(dialMusic);
									number = new Number(toNumber);
									dial.append(number);
								} else if ( dialMusic.isEmpty() && !isSipUser ) {
									dial.setCallerName(callerName);
									dial.setCallerId(fromNumber);
									number = new Number(toNumber);
									dial.append(number);
								} else if ( !dialMusic.isEmpty() && isSipUser ) {
									dial.setCallerName(callerName);
									dial.setCallerId(fromNumber);
									dial.setDialMusic(dialMusic);
									sipUser = new User(toNumber);
									dial.append(sipUser);
								} else if ( dialMusic.isEmpty() && isSipUser ) {
									dial.setCallerName(callerName);
									dial.setCallerId(fromNumber);
									sipUser = new User(toNumber);
									dial.append(sipUser);
								}
							}
						}
						plivoResponse.append(dial);
					} catch (PlivoException e) {
						System.out.printf("Error while generating XML - ", e.getLocalizedMessage());
					}

					response.type("text/xml");
					response.body(plivoResponse.toXML());
					return response;
				}
			});
	}
}
