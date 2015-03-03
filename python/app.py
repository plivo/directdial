import os
import os.path
from flask import Flask, request, make_response
import plivoxml
app = Flask(__name__)

@app.route('/response/sip/route/', methods=['GET', 'POST'])
def sip_route():
    try:
        print "SIP Route %s" % request.values.items()
        to = request.args.get('ForwardTo', None)
        _from = request.args.get('CLID', None)
        dial_music = request.args.get('DialMusic', "")
        disable_call = request.args.get('DisableCall', "")
        if request.method == "GET":
            if not to:
                to = request.args.get('To', None)
            if _from is None:
                _from = request.args.get('From', '')
            cname = request.args.get('CallerName', '')
            hangup = request.args.get('HangupCause', None)
        else:
            if not to:
                to = request.form.get('To', None)
            if _from is None:
                _from = request.form.get('From', '')
            cname = request.form.get('CallerName', '')
            hangup = request.form.get('HangupCause', None)

        if hangup:
            response = make_response("SIP Route hangup callback")
            return response

        r = plivoxml.Response()

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
                if is_sip_user:
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
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port=port)
