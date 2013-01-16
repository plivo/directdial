C# .NET
=======

Direct Dial application.

## Usage Examples
###Default usage => bridge call to the 'To' parameter
* ####Set call forwarding <br/>
  http://server/response/sip/route/?ForwardTo=15555559999<br/>

* ####Set dial music for call ring <br/>
**Play music from an URL while the call is getting connected**<br/>
  http://server/response/sip/route/?ForwardTo=15555559999&DialMusic=http://myserver/playsound/
  <br/>or<br/>
**Ring back tone from the actual device**<br/>
  http://server/response/sip/route/?ForwardTo=15555559999&DialMusic=real

* ####Disable outbound calls<br/>
**to phone numbers**<br/>
  http://server/response/sip/route/?DisableCall=number <br/>
  so, when 'To' is a number the XML response is a simple hangup. e.g. <br/>
  http://server/response/sip/route/?To=15555559999&DisableCall=number
<br/> 
**to SIP endpoints**<br/>
  http://server/response/sip/route/?DisableCall=sip <br/>
  So, when 'To' is a SIP uri the XML response is a simple hangup.
<br/> 
**to all destinations**<br/>
  http://server/response/sip/route/?DisableCall=all
