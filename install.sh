#http://ffmpeg.org/trac/ffmpeg/wiki/CentosCompilationGuide
cd ~
mkdir ffmpeg-sources
cd ~/ffmpeg-sources
echo "Installing lame"
wget http://downloads.sourceforge.net/project/lame/lame/3.99/lame-3.99.5.tar.gz
tar xzvf lame-3.99.5.tar.gz
cd lame-3.99.5
./configure --disable-shared --enable-nasm
make
make install
../

echo "Installing libogg"
wget http://downloads.xiph.org/releases/ogg/libogg-1.3.0.tar.gz
tar xzvf libogg-1.3.0.tar.gz
cd libogg-1.3.0
./configure --disable-shared
make
make install
../


wget http://downloads.xiph.org/releases/vorbis/libvorbis-1.3.3.tar.gz
tar xzvf libvorbis-1.3.3.tar.gz
cd libvorbis-1.3.3
./configure --disable-shared
make
make install
../

wget http://downloads.sourceforge.net/opencore-amr/vo-aacenc-0.1.2.tar.gz
tar xzvf vo-aacenc-0.1.2.tar.gz
cd vo-aacenc-0.1.2
./configure --disable-shared
make
make install
../

git clone git://source.ffmpeg.org/ffmpeg
cd ffmpeg
./configure --enable-gpl --enable-libmp3lame  --enable-libvo-aacenc --enable-libvorbis --enable-version3
make