echo on
cd .
set PATH=.;.\tools\;.\capture\;
cd .\capture\
mencoder.exe "mf://*.png" -mf fps=60 -o "..\Jogamp_Capture.avi" -ovc xvid -xvidencopts fixed_quant=4 bitrate=25000000 -vf harddup

