XBMCWrapper
===========
Based on: http://forum.xbmc.org/showthread.php?tid=155526
Thread: http://forum.xbmc.org/showthread.php?tid=192651

Features:

- Samba access manipulation:
 - Auto mount as CIFS, and play from there (Medium CPU usage, perfect file handling)
 - Map to miniDLNA URL (given the database file), and play it (Low CPU usage, seek may not always work)
 - Stream over HTTP (High CPU usage, seek may not always work)
 - Replace to samba URL to local file path. (Only if your files are on the same device, e.g. if you have a shared xbmc database and playing on the server. Low CPU usage)
- Re-stream HTTP specially for Allwinner A10 devices HW decoder.
- Map XBMC PVR to TvHeadend Channels. (\*/\* login for streaming required)
- Replace URL On-the-fly
- Use any external player you want

A little explanation:

- Stream Samba shares over HTTP

 It's almost the same (compared to SmbWrapper), only that it uses less CPU.

- Auto mount as CIFS, and play from there

 Mounts the given smb share to sdcard/xbmcwrapper with busybox and cifs, and plays the file from there.
 Busybox should be in one of these places: "/system/bin/busybox", "/system/xbin/busybox", "/xbin/busybox", "/bin/busybox", "/sbin/busybox"
 CIFS support in kernel required. UTF8 iocharset support in kernel recommended.

- Map to miniDLNA URL

 Given the miniDLNA files.db database (local or samba file), that contains the file you are trying to play, the wrapper will map the file miniDLNA URL, and passes this to player.
 This has the minimal CPU usage on both Server and Client side.

- Re-stream HTTP specially for Allwinner A10 devices HW decoder.

 Pre-buffers HTTP input, sometimes it can help HW decoder.

- Map XBMC PVR to TvHeadend Channels, and possibly re-stream. (\*/\* login for streaming required)

 I've made this for TvHeadend, as normally it would play in SW decoding mode which is unenjoyable. Now it uses HW decoder, if you use this wrapper.
 With tvheadend 3.4patch1, and TvdVideo, it's possible to play in HW decoding mode without re-stream. (Less CPU usage)
 XBMC uses its own id's for TV channels(the number before their name), you can set how to remap these to tvheadend channel id's to play in external player.
 The syntax is the following:
 xbmcid,tvheadendid;xbmcid,tvheadendid;...

- Replace URL On-the-fly

 If you are using mysql databse, you are accessing even local files over samba, which is a huge overhead. With this tool you can simply replace the samba url with a local file url, thus completely removing this overhead.
