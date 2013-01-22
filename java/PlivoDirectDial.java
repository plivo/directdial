import com.plivo.sdk.xml.elements.*;
import static spark.Spark.*;
import spark.*;

public class PlivoDirectDial {
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
                  bool isSipUser = false;

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
                  com.plivo.sdk.xml.elements.Response plivoResponse 
                      = new com.plivo.sdk.xml.elements.Response();

                  Hangup hangup = new Hangup();
                  hangup.setReason("busy");

                  Dial dial = new Dial();
                      
                  // if invoked as a hangup_url send a text response
                  if ( !hangupCause.isEmpty() ) {
                      return "SIP Route hangup callback";
                  }
                  
                  // if no destination nunber is available, hang up.
                  if ( !toNumber.isEmpty() ) {
                      System.out.println("SIP Route cannot identify destination number");
                      plivoResponse.setHangup(hangup);
                  } else {
                      if ( toNumber.length > 4 && toNumber.substring(4) == "sip:" ) {
                          isSipUser = true;
                      }
                      // check for outbound call disable status - all, sip or number
                      if ( isSipUser && disableCall == "all" || disableCall == "sip" ) {
                          System.out.println("SIP Route calling sip user is disabled : %s", disableCall);
                          plivoResponse.setHangup(hangup);
                      } else if ( !isSipUser && disableCall == "all" || disableCall == "number" ) {
                          System.out.println("SIP Route calling number is disabled : %s", disableCall);
                          plivoResponse.setHangup(hangup);
                      } else {
                          System.out.println("SIP Route dialing %s", dst);
                          if ( !dialMusic.isEmpty() && !isSipUser ) {
                              dial.setCallerName(callerName);
                              dial.setCallerId(fromNumber);
                              dial.setDialMusic(dialMusic);
                              dial.setNumber(toNumber);
                              plivoResponse.setDial(dial);
                          } else if ( dialMusic.isEmpty() && !isSipUser ) {
                              dial.setCallerName(callerName);
                              dial.setCallerId(fromNumber);
                              dial.setNumber(toNumber);
                              plivoResponse.setDial(dial);
                          } // else if ( !dialMusic.isEmpty() && isSipUser ) {
                          //     dial.setCallerName(callerName);
                          //     dial.setCallerId(fromNumber);
                          //     dial.setDialMusic(dialMusic);
                          //     dial.setUser(toNumber);
                          //     plivoResponse.setDial(dial);
                          // } else if ( dialMusic.isEmpty() && isSipUser ) {
                          //     dial.setCallerName(callerName);
                          //     dial.setCallerId(fromNumber);
                          //     dial.setUser(toNumber);
                          //     plivoResponse.setDial(dial);
                          // }
                      }
                  }
                  response.type("text/xml");
                  response.body(plivoResponse.serializeToXML());
              }
          });
   }
}
