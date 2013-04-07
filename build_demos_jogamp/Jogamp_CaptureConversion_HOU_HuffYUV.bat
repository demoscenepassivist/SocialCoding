echo on
cd .
set PATH=.;.\tools\;.\capture\;
cd .\capture\
ffmpeg -y -r 60 -i "JOGAMP_SCREENCAPTURE_HOU_%%06d.bmp" -vcodec huffyuv "..\Jogamp_Capture_HOU.avi"
cd ..



