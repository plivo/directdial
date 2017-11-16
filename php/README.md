PHP
===

Direct Dial application.

The file plivo_directdial.php needs to be hosted on a [server](http://www.appfog.com/).

## Usage Examples
### Default usage => bridge call to the 'To' parameter
* #### Set call forwarding <br/>
  http://server/plivo_directdial.php?ForwardTo=15555559999<br/>

* #### Set dial music for call ring <br/>
**Play music from an URL while the call is getting connected**<br/>
  http://server/plivo_directdial.php?ForwardTo=15555559999&DialMusic=http://myserver/playsound/
  <br/>or<br/>
**Ring back tone from the actual device**<br/>
  http://server/plivo_directdial.php?ForwardTo=15555559999&DialMusic=real

* #### Disable outbound calls<br/>
**to phone numbers**<br/>
  http://server/plivo_directdial.php?DisableCall=number <br/>
  so, when 'To' is a number the XML response is a simple hangup. e.g. <br/>
  http://server/plivo_directdial.php?To=15555559999&DisableCall=number
<br/> 
**to SIP endpoints**<br/>
  http://server/plivo_directdial.php?DisableCall=sip <br/>
  So, when 'To' is a SIP uri the XML response is a simple hangup.
<br/> 
**to all destinations**<br/>
  http://server/plivo_directdial.php?DisableCall=all
