echo on
cd .
set PATH=.;.\runtime_x32\bin\;
set JAVA_HOME=.\runtime_x32\;
.\runtime_x32\bin\java.exe -version
.\runtime_x32\bin\java.exe -client -Dsun.java2d.noddraw=true -Dsun.awt.noerasebackground=true -XX:+AggressiveOpts -Xcomp -Xnoclassgc -XX:+UseConcMarkSweepGC -Xms64m -Xmx1024m -jar jogamp_x32.jar -RESOLUTION=AUTO -ROUTINE=jogamp.routine.jogl.programmablepipeline.S3D_GL3_Raymarcher_Mandelbulb -FRAMERATE=AUTO -FULLSCREEN=FALSE -VSYNC=TRUE -MULTISAMPLING=FALSE -SAMPLEBUFFERS=4 -ANISOTROPICFILTERING=FALSE -ANISOTROPYLEVEL=16.0 -FRAMESKIP=TRUE -FRAMECAPTURE=TRUE -WINDOWTOOLKIT=AWT -STEREOSCOPIC=TRUE -STEREOSCOPICEYESEPARATION=0.35 -STEREOSCOPICOUTPUTMODE=ALL -ENDFRAME=10800
SET /P consolewait="PRESS RETURN TO KILL CONSOLE ..."