package com.plivo.directdial;

import java.io.IOException;

import com.plivo.helper.exception.PlivoException;
import com.plivo.helper.xml.elements.Dial;
import com.plivo.helper.xml.elements.Hangup;
import com.plivo.helper.xml.elements.Number;
import com.plivo.helper.xml.elements.PlivoResponse;
import com.plivo.helper.xml.elements.User;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class directDialApp extends HttpServlet {
    private static final long serialVersionUID = 1L;    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String toNumber    = request.getParameter("ForwardTo")==null?"":request.getParameter("ForwardTo");
        String fromNumber  = request.getParameter("CLID")==null?"":request.getParameter("CLID");
        String callerName  = request.getParameter("CallerName")==null?"":request.getParameter("CallerName");
        String hangupCause = request.getParameter("HangupCause")==null?"":request.getParameter("HangupCause");
        String disableCall = request.getParameter("DisableCall")==null?"":request.getParameter("DisableCall");
        String dialMusic   = request.getParameter("DialMusic")==null?"":request.getParameter("DialMusic");

        // flags
        boolean isSipUser = false;

        // if 'ForwardTo' is not set specifically use 'To'.
        // caveat: if 'ForwardTo' is not set when app is linked 
        // to a number, it would a loop.
        if ( toNumber.isEmpty() ) {
            toNumber  = request.getParameter("To")==null?"":request.getParameter("To");
        }

        // if caller ID is not set specifically use 'From'
        if ( fromNumber.isEmpty() ) {
            fromNumber  = request.getParameter("From")==null?"":request.getParameter("From");
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
            System.out.println("SIP Route hangup callback");
        }
      
        try {
            // if no destination nunber is available, hang up.
            if ( toNumber.isEmpty() ) {
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
                    plivoResponse.append(dial);
                }
            }
        } catch (PlivoException e) {
            System.out.printf("Error while generating XML - ", e.getLocalizedMessage());
        }
            
        System.out.println(plivoResponse.toXML());
        response.addHeader("Content-Type", "text/xml");
        response.getWriter().print(plivoResponse.toXML());
    }

    public static void main(String[] args) throws Exception {
        String port = System.getenv("PORT");
        if(port==null)
            port ="8000";
        Server server = new Server(Integer.valueOf(port));
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new directDialApp()),"/direct-dial/");
        server.start();
        server.join();
    }
}
