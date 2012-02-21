echo on
cd .
set PATH=.;.\tools\;.\capture\;
cd .\capture\
ffmpeg -r 60 -i "JOGAMP_SCREENCAPTURE_%%06d.png" -vcodec libx264 -fpre libx264-lossless_medium.ffpreset "..\Jogamp_Capture.mp4"



