echo on
cd .
set PATH=.;.\tools\;.\capture\;
cd .\capture\
mencoder.exe "mf://*.png" -mf fps=60 -o "..\Jogamp_Capture.avi" -ovc lavc -lavcopts vcodec=msmpeg4v2:vbitrate=8000