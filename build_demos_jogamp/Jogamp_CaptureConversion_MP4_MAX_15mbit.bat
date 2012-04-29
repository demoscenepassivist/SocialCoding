echo on
cd .
set PATH=.;.\tools\;.\capture\;
cd .\capture\
ffmpeg -r 60 -i "JOGAMP_SCREENCAPTURE_%%06d.png" -vcodec libx264 -b 15000k -threads 0 -fpre libx264-max.ffpreset "..\Jogamp_Capture.mp4"



