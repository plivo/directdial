<?php
require 'plivo.php';

$dst = $_REQUEST['ForwardTo'];
if(! $dst)
    $dst = $_REQUEST['To'];
$src = $_REQUEST['CLID'];
if(! $src)
    $src = $_REQUEST['From'] or "";
$cname = $_REQUEST['CallerName'] or "";
$hangup = $_REQUEST['HangupCause'];
$dial_music = $_REQUEST['DialMusic'];
$disable_call = $_REQUEST['DisableCall'];
    
$r = new Response();
if($dst) {
    $dial_params = array();

    if($src)
        $dial_params['callerId'] = $src;

    if($cname)
        $dial_params['callerName'] = $cname;

    if(substr($dst, 0,4) == "sip:")
        $is_sip_user = TRUE;
    else
        $is_sip_user = FALSE;

    if($is_sip_user and in_array($disable_call, array("all", "sip"))) {
        $r->addHangup(array("reason" => "busy"));
    } elseif (! $is_sip_user and in_array($disable_call, array("all", "number"))) {
        $r->addHangup(array("reason" => "busy"));
    } else {
        if($dial_music)  {
            $dial_params["dialMusic"] = $dial_music;
            $d = $r->addDial($dial_params);
        } else
            $d = $r->addDial($dial_params);

        if($is_sip_user)
            $d->addUser($dst);
        else
            $d->addNumber($dst);
    } 
 } else {
     $r->addHangup();
 }

header("Content-Type: text/xml");
echo($r->toXML());
?>