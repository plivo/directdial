#!/usr/bin/ruby -wKU

require 'rubygems'
require 'plivo'
require 'sinatra'
require 'sinatra/multi_route'

include Plivo

route :get, :post, '/response/sip/route/' do
  puts "SIP Route #{request.params}"
  to = params[:ForwardTo] || params[:To] 
  from = params[:CLID] ? params[:CLID] : params[:From] ? params[:From] : ''
  cname = params[:CallerName] ? params[:CallerName] : ''
  hangup = params[:HangupCause]
  dial_music = params[:DialMusic] || nil
  disable_call = params[:DisableCall]
  
  puts "SIP Route hangup callback" if hangup

  r = Response.new()

  if to #we have someone we can call
    is_sip_user = to[0, 4] == "sip:"
    
    #hangup if recipient is a sip endpoint with inbound calling disabled
    if is_sip_user && ['all', 'sip'].include? disable_call 
      puts "SIP Route calling sip user is disabled : #{disable_call}"
      r.addHangup(reason='busy')
    
    #hangup if recipient is a sip endpoint with outbound calling disabled
    elsif !is_sip_user && ['all', 'number'].include? disable_call 
      puts "SIP Route calling number is disabled : #{disable_call}"
      r.addHangup(reason='busy')
    else #place call
      puts "SIP Route dialing #{to}"
      parameters = {
        'callerId' => from,
        'callerName' => cname,
        'dialMusic' => dial_music
      }.reject{ |_, value| value.blank?} #removes dial music key/value pair if it's blank.
      
      d = r.addDial(parameters)
      is_sip_user ? d.addUser(to) : d.addNumber(to)
    end
  else #hangup call without recipient
    puts "SIP Route cannot identify destination number"
    r.addHangup()
  end
  content_type :xml         
  r.to_xml()
end
