echo on
cd .
set PATH=.;.\runtime\bin\;.\native\;
set JAVA_HOME=.\runtime\;
.\runtime\bin\java.exe -version
.\runtime\bin\java.exe -client -Dsun.java2d.noddraw=true -Dsun.awt.noerasebackground=true -XX:+AggressiveOpts -Xcomp -Xnoclassgc -XX:+UseConcMarkSweepGC -Xms64m -Xmx512m -jar jogamp.jar -RESOLUTION=AUTO -ROUTINE=jogamp.routine.jogl.programmablepipeline.GL3_MonkeyMathica -FRAMERATE=AUTO -FULLSCREEN=FALSE -VSYNC=TRUE -MULTISAMPLING=FALSE -SAMPLEBUFFERS=4 -ANISOTROPICFILTERING=FALSE -ANISOTROPYLEVEL=16.0 -FRAMESKIP=TRUE -FRAMECAPTURE=FALSE -WINDOWTOOLKIT=AWT
SET /P consolewait="PRESS RETURN TO KILL CONSOLE ..."