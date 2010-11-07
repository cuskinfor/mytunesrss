The FFmpeg binaries distributed with MyTunesRSS were built on a 64 Bit Mac OS X system.

SVN export of sources:
======================
svn export -r25689 svn://svn.ffmpeg.org/ffmpeg/trunk ffmpeg-r25689

Windows (mingw32):
==================
configure --enable-shared --disable-static --disable-doc --disable-ffmpeg --disable-ffplay --disable-ffserver --disable-ffprobe --enable-memalign-hack --arch=x86 --target-os=mingw32 --cross-prefix=i386-mingw32-

Mac OS X (64 Bit):
==================
configure --enable-shared --disable-static --disable-doc --disable-ffmpeg --disable-ffplay --disable-ffserver --disable-ffprobe --shlibdir=@loader_path

Mac OS X (32 Bit):
==================
configure --enable-shared --disable-static --disable-doc --disable-ffmpeg --disable-ffplay --disable-ffserver --disable-ffprobe --shlibdir=@loader_path --extra-cflags="-arch i386" --extra-ldflags='-arch i386' --arch=x86_32 --target-os=darwin --enable-cross-compile

Creation of Universal (Mac OS X):
=================================
lipo xxx32bit.dylib xxx64bit.dylib -output xxx.dylib -create
