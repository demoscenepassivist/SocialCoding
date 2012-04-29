echo on
cd .
set PATH=.;.\tools\;.\capture\;
cd .\capture\
mencoder.exe "mf://*.png" -mf fps=60 -o "..\Jogamp_Capture.avi" -ovc lavc -lavcopts vcodec=mpeg4:vbitrate=24000000:mbd=2:mv0:trell:v4mv:cbp:last_pred=3:predia=2:dia=2:vmax_b_frames=2:vb_strategy=1:precmp=2:cmp=2:subcmp=2:preme=2:qns=2