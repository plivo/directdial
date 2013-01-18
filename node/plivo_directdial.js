var plivo = require('plivo');
var request = require('request');
var express = require('express');
var app = express();
app.use(express.bodyParser()); // Required for parsing POST

app.all('/response/sip/route/', function (req, res) {
    var dst = req.param('ForwardTo');
    var src = req.param('CLID');
    var cname = req.param('CallerName') || "";
    var hangup = req.param('HangupCause');
    var dial_music = req.param('DialMusic');
    var disable_call = req.param('DisableCall');
    var r = plivo.Response();

    if (hangup) {
        res.end("SIP Route hangup callback");
        return;
    }
    if (!dst) {
        dst = req.param('To');
    }
    if (!src) {
        src = req.param('From') || "";
    }
    if (dst) {
        var dial_params = {};
        if (src) {
            dial_params.callerId = src;
        }
        if (cname) {
            dial_params.callerName = cname;
        }
        if (dst.substr(0, 4) === "sip:") {
            var is_sip_user = true;

        } else {
            var is_sip_user = false;
        }
        if (is_sip_user && disable_call in {
            all: 1,
            sip: 1
        }) {
            console.log("SIP Route calling: Calls to sip user is disabled");
            r.addHangup({
                reason: 'busy'
            });
        } else if (!is_sip_user && disable_call in {
            all: 1,
            number: 1
        }) {
            console.log("SIP Route calling: Calls to sip user is disabled");
            r.addHangup({
                reason: 'busy'
            });
        } else {
            if (dial_music) {
                dial_params.dialMusic = dial_music;
                var d = r.addDial(dial_params);
            } else {
                var d = r.addDial(dial_params);
            }
            if (is_sip_user) {
                d.addUser(dst);
            } else {
                d.addNumber(dst);
            }
        }
    } else {
        r.addHangup();
    }

    console.log(r.toXML());
    res.set({
        'Content-Type': 'text/xml'
    });
    res.end(r.toXML());

});

app.listen(5000);
console.log('Listening on port 5000');
