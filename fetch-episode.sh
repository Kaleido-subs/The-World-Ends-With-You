#!/usr/bin/env bash

if [[ -n "$2" ]]; then
  mkdir -p "$2"
  cd "$2" || exit
fi

url="$1"
episode="${PWD##*/}"

filter='
/^Title: .*/c\
Title: [Kaleido-subs] The World Ends With You - '"$episode"'
/^PlayResX: .*/c\
PlayResX: 1920
/^PlayResY: .*/c\
PlayResY: 1080\
YCbCr Matrix: TV.709
'
filter_dialogue='
/^Style: .*/c\
Style: Default,Gandhi Sans,74,&H00FFFFFF,&H000000FF,&H00000000,&HA0000000,-1,0,0,0,100,100,0,0,1,3.6,1.5,2,200,200,56,1\
Style: Alt,Gandhi Sans,74,&H00FFFFFF,&H000000FF,&H00564100,&HA0000000,-1,0,0,0,100,100,0,0,1,3.6,1.5,2,200,200,56,1
/^\[Events]/,/^Format/ {
/^Format/a\
Comment: 0,0:00:00.00,0:00:00.00,Default,,0,0,0,,== Dialogue ============================
}
/^Dialogue:/ { /{[^}]*\\an8[^}]*}/d }
'
filter_signs='
/^Style: .*/c\
Style: Sign,Gandhi Sans,74,&H00FFFFFF,&H000000FF,&H00000000,&H00000000,0,0,0,0,100,100,0,0,1,0,0,5,100,100,56,1
/^\[Events]/,/^Format/ {
/^Format/a\
Comment: 0,0:00:00.00,0:00:00.00,Signs,,0,0,0,,== Signs ===============================
}
/^Dialogue:/ {
  /{[^}]*\\an8[^}]*}/!d
  s/,Default,/,Signs,/
  s/{\\an8}//
}
'

dialogue="twewy_${episode} - Dialogue.ass"
signs="twewy_${episode} - TS.ass"

curl -L "$url" | xz --decompress --stdout | sed "$filter" | tee >(sed "$filter_dialogue" >"$dialogue") >(sed "$filter_signs" | tee "$signs" "${signs%.ass}.raw.ass" >/dev/null) >/dev/null
