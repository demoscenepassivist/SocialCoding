echo on
cd .
set PATH=.;.\runtime_x64\bin\;
set JAVA_HOME=.\runtime_x64\;
.\runtime_x64\bin\java.exe -version
.\runtime_x64\bin\java.exe -client -Dsun.java2d.noddraw=true -Dsun.awt.noerasebackground=true -XX:+AggressiveOpts -Xcomp -Xnoclassgc -XX:+UseConcMarkSweepGC -Xms64m -Xmx8192m -jar jogamp_x64.jar -RESOLUTION=AUTO -ROUTINE=jogamp.routine.jogl.programmablepipeline.GL3_PointSprites_VolumeTexture -FRAMERATE=AUTO -FULLSCREEN=FALSE -VSYNC=TRUE -MULTISAMPLING=FALSE -SAMPLEBUFFERS=4 -ANISOTROPICFILTERING=FALSE -ANISOTROPYLEVEL=16.0 -FRAMESKIP=FALSE -FRAMECAPTURE=FALSE -WINDOWTOOLKIT=AWT
SET /P consolewait="PRESS RETURN TO KILL CONSOLE ..."