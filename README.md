XBMCWrapper
===========
Based on: http://forum.xbmc.org/showthread.php?tid=155526
Thread: http://forum.xbmc.org/showthread.php?tid=192651

Features:

 - Stream Samba shares over HTTP
 - Re-stream HTTP specially for Allwinner A10 devices HW decoder.
 - Map XBMC PVR to TvHeadend Channels. (*/* login for streaming required)
 - Replace URL On-the-fly
 - Use any external player you want

A little explanations:

 - Re-stream HTTP specially for Allwinner A10 devices HW decoder.
 - - I've made this for TvHeadend, as normally it would play in SW decoding mode which is unenjoyable. Now it uses HW decoder, if you use this wrapper.

 - Map XBMC PVR to TvHeadend Channels. (*/* login for streaming required)
 - - XBMC uses its own id's for TV channels(the number before their name), you can set how to remap these to tvheadend channel id's to play in external player.
 - - The syntax is the following:
 - - '<xbmcid>,<tvheadendid>;<xbmcid>,<tvheadendid>;...'

 - Replace URL On-the-fly
 - - If you are using mysql databse, you are accessing even local files over samba, which is a huge overhead. With this tool you can simply replace the samba url with a local file url, thus completely removing this overhead.
