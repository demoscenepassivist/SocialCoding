echo on
cd .
set PATH=.;.\tools\;.\capture\;
cd .\capture\
ffmpeg -r 60 -i "JOGAMP_SCREENCAPTURE_%%06d.png" -vcodec rawvideo -pix_fmt bgra "..\Jogamp_Capture.avi"



