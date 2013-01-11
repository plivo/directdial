directdial
==========

Direct Dial application.

Apps ready to be used with Heroku: python, ruby

## Use case
* To be able to make outbound calls from a Plivo SIP endpoint.
* To be able to set up call forwarding.

## Usage 
###SIP endpoint
* [Create a SIP endpoint](https://plivo.com/endpoint/create/) from the [web dashboard](https://plivo.com/dashboard/) and link it to a Plivo application.
* Set this web app url as the "Answer Url" of the Plivo application to which the SIP endpoint is linked.

###Call Forwarding
* [Rent a number](https://plivo.com/number/search/) from the [web dashboard](https://plivo.com/dashboard/) and link it to a Plivo application.
* Set this web app url as the "Answer Url" of the Plivo application to which the number is linked.

## Usage Examples
###Default usage => bridge call to the 'To' parameter
* ####Set call forwarding <br/>
  http://plivodirectdial.herokuapp.com/response/sip/route/?ForwardTo=15555559999<br/>

* ####Set dial music for call ring <br/>
**Play music from an URL while the call is getting connected**<br/>
  http://plivodirectdial.herokuapp.com/response/sip/route/?ForwardTo=15555559999&DialMusic=http://myserver.com/playsound/
  <br/>or<br/>
**Ring back tone from the actual device**<br/>
  http://plivodirectdial.herokuapp.com/response/sip/route/?ForwardTo=15555559999&DialMusic=real

* ####Disable outbound calls<br/>
**to phone numbers**<br/>
  http://plivodirectdial.herokuapp.com/response/sip/route/?DisableCall=number <br/>
  so, when 'To' is a number the XML response is a simple hangup. e.g. <br/>
  http://plivodirectdial.herokuapp.com/response/sip/route/?To=15555559999&DisableCall=number
<br/> 
**to SIP endpoints**<br/>
  http://plivodirectdial.herokuapp.com/response/sip/route/?DisableCall=sip <br/>
  So, when 'To' is a SIP uri the XML response is a simple hangup.
<br/> 
**to all destinations**<br/>
  http://plivodirectdial.herokuapp.com/response/sip/route/?DisableCall=all
