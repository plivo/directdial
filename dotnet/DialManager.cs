Enter file contents hereusing System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Web;
using Nancy;

/// <summary>
/// Summary description for DialManager
/// </summary>
public static class DialManager
{
    public static Response ProcessDialRequest(Request request)
    {
        string dst = request.Query["ForwardTo"];
        string src = request.Query["CLID"];
        string cname = request.Query["CallerName"];
        string hangup = request.Query["HangupCause"];
        string dialMusic = request.Query["DialMusic"];
        string disableCall = request.Query["DisableCall"];

        if (String.IsNullOrEmpty(dst))
            dst = request.Query["To"];
        if (String.IsNullOrEmpty(src))
            src = request.Query["From"];

        if (String.IsNullOrEmpty(cname))
            cname = "";
        if (!String.IsNullOrEmpty(hangup))
            return "SIP Route hangup callback";

        var response = new Plivo.XML.Response();

        if (String.IsNullOrEmpty(dst))
        {
            Console.WriteLine("SIP Route cannot identify destination number");
            response.AddHangup(new Dictionary<string,string>
            {
                {"reason", "busy"}
            });
            return GetResponse(response);
        }

        bool isSipUser = dst.Length > 4 && dst.Substring(0, 4) == "sip:";

        if (isSipUser && disableCall == "all" || disableCall == "sip")
        {
            Console.WriteLine("SIP Route calling sip user is disabled : %s", disableCall);
            response.AddHangup(new Dictionary<string, string>
            {
                {"reason", "busy"}
            });
            return GetResponse(response);
        }
        
        if (!isSipUser && disableCall == "all" || disableCall == "number")
        {
            response.AddHangup(new Dictionary<string,string>
            {
                { "reason", "busy" },
            });
            return GetResponse(response);
        }

        Console.WriteLine("SIP Route dialing %s", dst);

        var parms = new Dictionary<string,string>
        {
            {"callerId", src},
            {"callerName", cname}
        };

        if (!String.IsNullOrEmpty(dialMusic))
        {
            parms.Add("dialMusic", dialMusic);
        }

        if (isSipUser)
        {
            var dial = response.AddDial(parms);
            dial.AddUser(dst, new Dictionary<string, string> {});
            response.Add(dial);
        }
        else
        {
            var dial = new Plivo.XML.Dial(parms);
            dial.AddNumber(dst, new Dictionary<string, string> { });
            response.Add(dial);
        }

        return GetResponse(response);
    }

    public static Nancy.Response GetResponse(Plivo.XML.Response response)
    {
        var xmlBytes = Encoding.UTF8.GetBytes(response.ToString());
        return new Nancy.Response
        {
            ContentType = "text/xml",
            Contents = s => s.Write(xmlBytes, 0, xmlBytes.Length)
        };
    }
}
