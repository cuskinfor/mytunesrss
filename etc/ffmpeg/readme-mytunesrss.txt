The FFmpeg binaries distributed with MyTunesRSS were built on a 64 Bit Mac OS X system
using the following configure command lines:

Windows (mingw32):
==================
--enable-memalign-hack --arch=x86 --target-os=mingw32 --cross-prefix=i386-mingw32- --enable-shared --disable-static --disable-doc --disable-ffmpeg --disable-ffplay --disable-ffserver --disable-ffprobe --disable-avdevice --disable-swscale

Mac OS X:
=========
--enable-shared --disable-static --disable-doc --disable-ffmpeg --disable-ffplay --disable-ffserver --disable-ffprobe --disable-avdevice --disable-swscale

The base revision of the sources is r25142 from the 0.6 branch of the FFmpeg subversion repository. You can
checkout this base revision using

svn co -r25142 svn://svn.ffmpeg.org/ffmpeg/branches/0.6 ffmpeg-0.6-r25142

The following diff was applied before compilation (also see changes.diff):

Index: libavformat/utils.c
===================================================================
--- libavformat/utils.c	(revision 25142)
+++ libavformat/utils.c	(working copy)
@@ -2754,9 +2754,9 @@
         pkt->dts= st->pts_buffer[0];
     }

-    if(st->cur_dts && st->cur_dts != AV_NOPTS_VALUE && st->cur_dts >= pkt->dts){
+    if(st->cur_dts && st->cur_dts != AV_NOPTS_VALUE && st->cur_dts > pkt->dts){
         av_log(s, AV_LOG_ERROR,
-               "st:%d error, non monotone timestamps %"PRId64" >= %"PRId64"\n",
+               "st:%d error, non monotone timestamps %"PRId64" > %"PRId64"\n",
                st->index, st->cur_dts, pkt->dts);
         return -1;
     }

This prevents the "non monotone timestamps" error message I always got when segmenting the MPEG-TS file
for HTTP Live Streaming.
