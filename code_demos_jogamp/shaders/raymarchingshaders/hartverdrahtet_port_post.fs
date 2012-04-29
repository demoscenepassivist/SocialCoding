/**
 **   __ __|_  ___________________________________________________________________________  ___|__ __
 **  //    /\                                           _                                  /\    \\  
 ** //____/  \__     __ _____ _____ _____ _____ _____  | |     __ _____ _____ __        __/  \____\\ 
 **  \    \  / /  __|  |     |   __|  _  |     |  _  | | |  __|  |     |   __|  |      /\ \  /    /  
 **   \____\/_/  |  |  |  |  |  |  |     | | | |   __| | | |  |  |  |  |  |  |  |__   "  \_\/____/   
 **  /\    \     |_____|_____|_____|__|__|_|_|_|__|    | | |_____|_____|_____|_____|  _  /    /\     
 ** /  \____\                       http://jogamp.org  |_|                              /____/  \    
 ** \  /   "' _________________________________________________________________________ `"   \  /    
 **  \/____.                                                                             .____\/     
 **
 ** JOGL2 port of my PC 4k intro competition entry for Revision 2012. This is the post-processing
 ** fragment shader that takes the raymarching shader output from the FBO and applies god-rays, tv-lines
 ** and noise to it to make the overall look more interesting and less 'sterile'. As this shader is the
 ** 'orignal' minified version of the shader and a little bit hard to understand I also included the 
 ** 'normal' version I used during development: 'hartverdrahtet_development_post.fs'.
 **
 ** For an impression how this routine looks like see here: http://www.youtube.com/watch?v=UjgRGDhgehA
 ** Original release from the Revision 2012 can be found here: http://www.pouet.net/prod.php?which=59086
 **/
 
uniform vec2 rs;
uniform float tm;
uniform sampler2D s0;
uniform int sn;
uniform float st,cm;

void main()
{
   vec2 s=gl_FragCoord.xy,i=s/(rs*2),f=i;
   vec3 y=vec3(0.);
   int v=75;
   float m;
   vec2 t=vec2(1.);
   float x=st;
   int r=sn;
   vec2 c=vec2(12.9898,78.233);
   float z=43758.5,d=fract(sin(dot(s.xy,c))*z+x*.25)*(sin(tm+s.y)*2+4.);
   if(r>=1)
   {
       if(d>x)
         r=r-1;
   }
   //x=mod(tm*5,110.);
   //r=int(mod(tm*.11,10.));
   if(r==1)
     t.x=.5,m=1.5;
   if(r==2)
     t.y=-1.,m=5.;
   if(r==3)
     m=1.25;
   if(r==4)
     t=vec2(3.,-.25),m=2.;
   if(r==5)
     t.y=0.,m=2.5;
   if(r==6)
     t=vec2(.75,.75),m=2.5;
   if(r==7)
     t=vec2(0.,1.),m=1.5;
   if(r==8)
     t.y=1.5,m=2.5;
   if(r==9)
     t.y=.95,m=2.;
   m*=cm;
   float n=1./v,u=1.;
   for(int g=0;g<v;g++)
     {
       vec3 o=texture2D(s0,i+f).xyz;
       y+=u*smoothstep(.1,1.,o*o);
       u*=1.-n;
       f+=(t*.25-i)/v;
     }
   y/=v;
   vec3 g=texture2D(s0,s/rs).xyz+y*(m/(1.+dot(i,i)));
   i=s/rs.xy;
   i.y*=-1.;
   g*=.5+8.*i.x*i.y*(1.-i.x)*(-1.-i.y);
   g*=1.+.3*sin(i.y*(rs.y*1.55)); //tv-line-scaling 1280x720
   //g*=1.+.3*sin(i.y*(rs.y*1.0)); //tv-line-scaling 1920x1080
   g*=1.15+.2*sin(1.75*tm);
   float o=fract(sin(dot(i.xy,c))*z+tm*.5);
   g=g*.85+g*.25*vec3(o);
   gl_FragColor=vec4(g,1.);
};
 