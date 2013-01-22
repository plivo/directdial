using System;
using System.Text;
using System.Collections.Generic;

using dict = System.Collections.Generic.Dictionary<string, string>;

using Nancy;
using Plivo;

namespace GoPlivo
{
    public class PlivoDirectDial : NancyModule
    {
        public PlivoDirectDial()
        {
            Get["/response/sip/route/"] = _ => {
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
                    response.AddHangup(new dict {
                        {"reason", "busy"},
                    });
                } else {
                    bool isSipUser = false;

                    if (dst.Length > 4 && dst.Substring(0, 4) == "sip:")
                        isSipUser = true;

                    if (isSipUser && disableCall == "all" || disableCall == "sip") {
                        Console.WriteLine(String.Format("SIP Route calling sip user is disabled : %s", disableCall));
                        response.AddHangup(new dict {
                            { "reason", "busy" },
                        });
                    } else if (!isSipUser && disableCall == "all" || disableCall == "number") {
                        response.AddHangup(new dict {
                            { "reason", "busy" },
                        });
                    } else {
                        Console.WriteLine(String.Format("SIP Route dialing %s", dst));
                        
                        if (!String.IsNullOrEmpty(dialMusic) && isSipUser) {
                            var dial = response.AddDial(new dict {
                                {"callerId", src},
                                {"callerName", cname},
                                {"dialMusic", dialMusic)},
                            }); 
                            dial.AddUser(dst, new dict { });
                            response.Add(dial);
                        } else if (String.IsNullOrEmpty(dialMusic) && isSipUser) {
                            var dial = new Plivo.XML.Dial(new dict {
                                {"callerId", src},
                                {"callerName", cname},
                            });
                            dial.AddUser(dst, new dict { });
                            response.Add(dial);
                        } else if (!String.IsNullOrEmpty(dialMusic) && !isSipUser) {
                            var dial = new Plivo.XML.Dial(new dict {
                                {"callerId", src},
                                {"callerName", cname},
                                {"dialMusic", dialMusic},
                            });
                            dial.AddNumber(dst, new dict { });
                            response.Add(dial);
                        } else if (String.IsNullOrEmpty(dialMusic) && !isSipUser) {
                            var dial = new Plivo.XML.Dial(new dict {
                                {"callerId", src},
                                {"callerName", cname},
                            });
                            dial.AddUser(dst, new dict { });
                            response.Add(dial);
                        }
                        
                        var xmlBytes = Encoding.UTF8.GetBytes(response.ToString());
                        return new Response 
                        {
                          ContentType = 'text/xml';
                          Contents = s => s.Write(xmlBytes, 0, xmlBytes.Length);
                        };
                    }
                }
            };
            
            //Similarly, we can setup for HTTP POST as well.
            // Use Request.Form to get the values.
            //Post["/response/sip/route/"] = _ =>
            //{ 
            //    return Response("Hello" + Request.Form["cname"]);
            //};
        }
    }
}
