#!/usr/bin/ruby -wKU

require 'rubygems'
require 'plivo'
require 'sinatra'
require 'sinatra/multi_route'

include Plivo

route :get, :post, '/response/sip/route/' do
  puts "SIP Route #{request.params}"
  to = params[:ForwardTo]
  if not to 
    to = params[:To]
  end
  from = params[:CLID] ? params[:CLID] : params[:From] ? params[:From] : ''
  cname = params[:CallerName] ? params[:CallerName] : ''
  hangup = params[:HangupCause]
  dial_music = params[:DialMusic]
  disable_call = params[:DisableCall]
  
  if hangup
    puts "SIP Route hangup callback"
  end

  r = Response.new()

  if not to
    puts "SIP Route cannot identify destination number"
    r.addHangup()
  else
    if to[0, 4] == "sip:"
      is_sip_user = true
    else
      is_sip_user = false
    end

    if is_sip_user and ['all', 'sip'].include? disable_call
      puts "SIP Route calling sip user is disabled : #{disable_call}"
      r.addHangup(reason='busy')
    elsif not is_sip_user and ['all', 'number'].include? disable_call
      puts "SIP Route calling number is disabled : #{disable_call}"
      r.addHangup(reason='busy')
    else
      puts "SIP Route dialing #{to}"
      if not dial_music
        parameters = { 'callerId' => from, 'callerName' => cname }
      else
        parameters = { 'callerId' => from, 'callerName' => cname, 'dialMusic' => dial_music }
      end 
      d = r.addDial(parameters)
      if is_sip_user
          d.addUser(to)
      else
          d.addNumber(to)
      end
    end
  end

  content_type :xml         
  r.to_xml()
end
