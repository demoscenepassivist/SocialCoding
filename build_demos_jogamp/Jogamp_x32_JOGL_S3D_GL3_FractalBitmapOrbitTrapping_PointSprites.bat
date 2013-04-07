echo on
cd .
set PATH=.;.\runtime_x32\bin\;
set JAVA_HOME=.\runtime_x32\;
.\runtime_x32\bin\java.exe -version
.\runtime_x32\bin\java.exe -client -Dsun.java2d.noddraw=true -Dsun.awt.noerasebackground=true -XX:+AggressiveOpts -Xcomp -Xnoclassgc -XX:+UseConcMarkSweepGC -Xms64m -Xmx1024m -jar jogamp_x32.jar -RESOLUTION=1920x1080 -ROUTINE=jogamp.routine.jogl.programmablepipeline.S3D_GL3_FractalBitmapOrbitTrapping_PointSprites -FRAMERATE=AUTO -FULLSCREEN=FALSE -VSYNC=TRUE -MULTISAMPLING=FALSE -SAMPLEBUFFERS=4 -ANISOTROPICFILTERING=FALSE -ANISOTROPYLEVEL=16.0 -FRAMESKIP=TRUE -FRAMECAPTURE=TRUE -WINDOWTOOLKIT=AWT -STEREOSCOPIC=TRUE -STEREOSCOPICEYESEPARATION=0.5 -STEREOSCOPICOUTPUTMODE=HSBS -ENDFRAME=3600

echo on
cd .
set PATH=.;.\tools\;.\capture\;
cd .\capture\
ffmpeg -r 60 -i "JOGAMP_SCREENCAPTURE_HSBS_%%06d.bmp" -vcodec libx264 -b 50000k -threads 0 -fpre libx264-max.ffpreset "..\Jogamp_Capture_HSBS.mp4"

SET /P consolewait="PRESS RETURN TO KILL CONSOLE ..."