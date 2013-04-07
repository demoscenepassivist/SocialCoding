echo on
cd .
set PATH=.;.\tools\;.\capture\;
cd .\capture\
ffmpeg -y -r 60 -i "JOGAMP_SCREENCAPTURE_%%06d.bmp" -vcodec huffyuv "..\Jogamp_Capture_2D.avi"
cd ..



