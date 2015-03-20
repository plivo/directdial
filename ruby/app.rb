require 'rubygems'
require 'plivo'
require 'sinatra'

include Plivo

get '/direct-dial/' do
    puts "SIP Route #{request.params}"
    to = params[:ForwardTo] || params[:To] 
    from = params[:CLID] ? params[:CLID] : params[:From] ? params[:From] : ''
    cname = params[:CallerName] ? params[:CallerName] : ''
    hangup = params[:HangupCause]
    dial_music = params[:DialMusic] || nil
    disable_call = params[:DisableCall]

    if hangup
        puts "SIP Route hangup callback" 
    end

    r = Response.new()

    if to
        #we have someone we can call
        is_sip_user = to[0, 4] == "sip:"

        #hangup if recipient is a sip endpoint with inbound calling disabled
        if (is_sip_user) && (['all', 'sip'].include? disable_call)
            puts "SIP Route calling sip user is disabled : #{disable_call}"
            r.addHangup(reason='busy')

            #hangup if recipient is a sip endpoint with outbound calling disabled
        elsif (!is_sip_user) && (['all', 'number'].include? disable_call)
            puts "SIP Route calling number is disabled : #{disable_call}"
            r.addHangup(reason='busy')
        else #place call
            puts "SIP Route dialing #{to}"
            parameters = {
            'callerId' => from,
            'callerName' => cname,
            'dialMusic' => dial_music
            }.reject{ |_, value| value.empty?} #removes dial music key/value pair if it's blank.

            d = r.addDial(parameters)
            is_sip_user ? d.addUser(to) : d.addNumber(to)
        end
    else  
        #hangup call without recipient
        puts "SIP Route cannot identify destination number"
        r.addHangup()
    end

    puts r.to_xml()
    content_type 'text/xml'
    return r.to_s()
end