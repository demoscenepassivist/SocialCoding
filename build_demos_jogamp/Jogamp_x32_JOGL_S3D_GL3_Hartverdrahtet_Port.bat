echo on
cd .
set PATH=.;.\runtime_x32\bin\;
set JAVA_HOME=.\runtime_x32\;
.\runtime_x32\bin\java.exe -version
.\runtime_x32\bin\java.exe -client -Dsun.java2d.noddraw=true -Dsun.awt.noerasebackground=true -XX:+AggressiveOpts -Xcomp -Xnoclassgc -XX:+UseConcMarkSweepGC -Xms64m -Xmx1024m -jar jogamp_x32.jar -RESOLUTION=1920x1080 -ROUTINE=jogamp.routine.jogl.programmablepipeline.S3D_GL3_Hartverdrahtet_Port -FRAMERATE=AUTO -FULLSCREEN=FALSE -VSYNC=TRUE -MULTISAMPLING=FALSE -SAMPLEBUFFERS=4 -ANISOTROPICFILTERING=FALSE -ANISOTROPYLEVEL=16.0 -FRAMESKIP=TRUE -FRAMECAPTURE=TRUE -WINDOWTOOLKIT=AWT -STEREOSCOPIC=TRUE -STEREOSCOPICEYESEPARATION=0.06 -STEREOSCOPICOUTPUTMODE=HSBS,FSBS,HOU -STARTFRAME=0 -ENDFRAME=11040
call Jogamp_CaptureConversion_MULTIPLE_FSBS_HSBS_HOU_BMP-HuffYUV-x264.bat
rename Jogamp_CaptureConversion_HSBS_x246.mp4 Jogamp_x32_JOGL_S3D_GL3_Hartverdrahtet_Port_HSBS_x264.mp4
rename Jogamp_CaptureConversion_FSBS_x246.mp4 Jogamp_x32_JOGL_S3D_GL3_Hartverdrahtet_Port_FSBS_x264.mp4
rename Jogamp_CaptureConversion_HOU_x246.mp4 Jogamp_x32_JOGL_S3D_GL3_Hartverdrahtet_Port_HOU_x264.mp4
SET /P consolewait="PRESS RETURN TO KILL CONSOLE ..."