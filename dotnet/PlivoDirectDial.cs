using System;
using System.Text;
using System.Collections.Generic;
using System.Diagnostics;
using RestSharp;
using Nancy;
using Plivo;

namespace GoPlivo
{
    public class PlivoDirectDial : NancyModule
    {
        public PlivoDirectDial()
        {
            Get["/direct-dial/"] = x => {
                string dst = Request.Query["ForwardTo"];
                string src = Request.Query["CLID"];
                string cname = Request.Query["CallerName"];
                string hangup = Request.Query["HangupCause"];
                string dialMusic = Request.Query["DialMusic"];
                string disableCall = Request.Query["DisableCall"];
                if ( String.IsNullOrEmpty(dst) )
                    dst = Request.Query["To"];
                if ( String.IsNullOrEmpty(src) )
                    src = Request.Query["From"];
                    
                if (String.IsNullOrEmpty(cname))
                    cname = "";
                if ( !String.IsNullOrEmpty(hangup) )
                    return "SIP Route hangup callback";

                Plivo.XML.Response response = new Plivo.XML.Response();

                if ( String.IsNullOrEmpty(dst) ) {
                    Console.WriteLine("SIP Route cannot identify destination number");
                    response.AddHangup(new Dictionary<string, string> {
                        {"reason", "busy"},
                    });
                } else {
                    bool isSipUser = false;

                    if (dst.Length > 4 && dst.Substring(0, 4) == "sip:")
                        isSipUser = true;

                    if (isSipUser && disableCall == "all" || disableCall == "sip") {
                        Console.WriteLine(String.Format("SIP Route calling sip user is disabled : %s", disableCall));
                        response.AddHangup(new Dictionary<string, string>() {
                            { "reason", "busy" },
                        });
                    } else if (!isSipUser && disableCall == "all" || disableCall == "number") {
                        response.AddHangup(new Dictionary<string, string>() {
                            { "reason", "busy" },
                        });
                    } else {
                        Console.WriteLine(String.Format("SIP Route dialing %s", dst));
                        
                        if (!String.IsNullOrEmpty(dialMusic) && isSipUser) {
                            var dial = response.AddDial(new Dictionary<string, string>() {
                                {"callerId", src},
                                {"callerName", cname},
                                {"dialMusic", dialMusic}
                            }); 
                            dial.AddUser(dst, new Dictionary<string, string>() { });
                            response.Add(dial);
                        } else if (String.IsNullOrEmpty(dialMusic) && isSipUser) {
                            var dial = new Plivo.XML.Dial(new Dictionary<string, string>() {
                                {"callerId", src},
                                {"callerName", cname},
                            });
                            dial.AddUser(dst, new Dictionary<string, string>() { });
                            response.Add(dial);
                        } else if (!String.IsNullOrEmpty(dialMusic) && !isSipUser) {
                            var dial = new Plivo.XML.Dial(new Dictionary<string, string>() {
                                {"callerId", src},
                                {"callerName", cname},
                                {"dialMusic", dialMusic},
                            });
                            dial.AddNumber(dst, new Dictionary<string, string>() { });
                            response.Add(dial);
                        } else if (String.IsNullOrEmpty(dialMusic) && !isSipUser) {
                            var dial = new Plivo.XML.Dial(new Dictionary<string, string>() {
                                {"callerId", src},
                                {"callerName", cname},
                            });
                            dial.AddUser(dst, new Dictionary<string, string>() { });
                            response.Add(dial);
                        }
                    }
                }
                Debug.WriteLine(response.ToString());

                var output = response.ToString();
                var res = (Nancy.Response)output;
                res.ContentType = "text/xml";
                return res;
            };
        }
    }
}