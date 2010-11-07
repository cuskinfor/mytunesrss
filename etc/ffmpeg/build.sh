#!/bin/sh

# export sources
rm -rf src
svn export -r25689 svn://svn.ffmpeg.org/ffmpeg/trunk src

# create build dir
mkdir build
cd build

# w32
../src/configure --enable-shared --disable-static --disable-doc --disable-ffmpeg --disable-ffplay --disable-ffserver --disable-ffprobe --enable-memalign-hack --arch=x86 --target-os=mingw32 --cross-prefix=i386-mingw32-
make
for i in util core codec format; do cp libav${i}/av${i}-*.dll ../w32; done
rm -rf * .*

# m32
../src/configure --enable-shared --disable-static --disable-doc --disable-ffmpeg --disable-ffplay --disable-ffserver --disable-ffprobe --shlibdir=@loader_path --extra-cflags="-arch i386" --extra-ldflags='-arch i386' --arch=x86_32 --target-os=darwin --enable-cross-compile
make
for i in util core codec format; do cp libav${i}/libav${i}.dylib ../m32; done
rm -rf * .*

# m64
../src/configure --enable-shared --disable-static --disable-doc --disable-ffmpeg --disable-ffplay --disable-ffserver --disable-ffprobe --shlibdir=@loader_path
make
for i in util core codec format; do cp libav${i}/libav${i}.dylib ../m64; done
rm -rf * .*

# delete build dir
cd ..
rm -rf build

# mac universal
for i in format util codec core; do lipo m32/libav${i}.dylib m64/libav${i}.dylib -output mac/libav${i}.dylib -create; done
