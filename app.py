import traceback
import os
import os.path
from flask import Flask, render_template, request, Response, make_response
import plivo
app = Flask(__name__)

@app.route('/response/sip/route/', methods=['GET', 'POST'])
def sip_route():
    try:
        print "SIP Route %s" % request.values.items()
        if request.method == "GET":
            to = request.args.get('ForwardTo', None)
            if not to:
                to = request.args.get('To', None)
            _from = request.args.get('CLID', None)
            if _from is None:
                _from = request.args.get('From', '')
            cname = request.args.get('CallerName', '')
            hangup = request.args.get('HangupCause', None)
            dial_music = request.args.get('DialMusic', "")
            disable_call = request.args.get('DisableCall', "")
            force_webrtc = request.args.get('WebRTC', "false") == "true"
        else:
            to = request.form.get('ForwardTo', None)
            if not to:
                to = request.form.get('To', None)
            _from = request.form.get('CLID', None)
            if _from is None:
                _from = request.form.get('From', '')
            cname = request.form.get('CallerName', '')
            hangup = request.form.get('HangupCause', None)
            dial_music = request.form.get('DialMusic', "")
            disable_call = request.form.get('DisableCall', "")
            force_webrtc = request.form.get('WebRTC', "false") == "true"

        if hangup:
            return "SIP Route hangup callback"

        r = plivo.Response()

        if not to:
            print "SIP Route cannot identify destination number"
            r.addHangup()
        else:
            if to[:4] == 'sip:':
                is_sip_user = True
            else:
                is_sip_user = False
            if is_sip_user and disable_call in ('all', 'sip'):
                print "SIP Route calling sip user is disabled : %s" % str(disable_call)
                r.addHangup(reason="rejected")
            elif not is_sip_user and disable_call in ('all', 'number'):
                print "SIP Route calling number is disabled : %s" % str(disable_call)
                r.addHangup(reason="rejected")
            else:
                print "SIP Route dialing %s" % str(to)
                if not dial_music:
                    d = r.addDial(callerId=_from, callerName=cname)
                else:
                    d = r.addDial(callerId=_from, callerName=cname, dialMusic=dial_music)
                if is_web_user is True or force_webrtc is True:
                    d.addUser(to, webrtc=True)
                elif is_sip_user:
                    d.addUser(to)
                else:
                    d.addNumber(to)

        response = make_response(r.to_xml())
        response.headers['Content-Type'] = 'text/xml'
        return response

    except Exception, e:
        print str(e)
        return "ERROR %s" % str(e)


if __name__ == '__main__':
   if not os.path.isfile("templates/response_template.xml"):
       print "Error : Can't find the XML template : templates/response_template.xml"
   else:
       port = int(os.environ['PORT'])
       app.run(host='0.0.0.0', port=port)
