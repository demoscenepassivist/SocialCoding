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
 ** JOGL2 port of my PC 4k intro competition entry for Revision 2012. This is the raymarching fragment
 ** shader that is rendered to a fullscreen billboard. The shader basically encapsulates a 
 ** sphere-tracing based raymarcher for a single fractal formula with camera handling. The different 
 ** intro parts are all parameter and camera position variations of the same fractal. As this shader
 ** is the 'orignal' minified version of the shader and a little bit hard to understand I also included
 ** the 'normal' version I used during development: 'hartverdrahtet_development_main.fs'.
 **
 ** Papers and articles you should be familiar with before trying to understand the code:
 **
 ** Distance rendering for fractals: http://www.iquilezles.org/www/articles/distancefractals/distancefractals.htm
 ** Ambient occlusion techniques: http://www.iquilezles.org/www/articles/ao/ao.htm
 ** Sphere tracing: A geometric method for the antialiased ray tracing of implicit surfaces: http://graphics.cs.uiuc.edu/~jch/papers/zeno.pdf
 ** Rendering fractals with distance estimation function: http://www.iquilezles.org/www/articles/mandelbulb/mandelbulb.htm
 **
 ** For an impression how this routine looks like see here: http://www.youtube.com/watch?v=UjgRGDhgehA
 ** Original release from the Revision 2012 can be found here: http://www.pouet.net/prod.php?which=59086
 **/
 
uniform vec2 rs;
uniform int sn;
uniform float st,lt;
vec3 cp;
mat3 cr;
float ff,fu,fd;
vec3 cs;
float fs;
vec3 fc;

float dE(vec3 f)
{
   float v=1.;
   for(int i=0;i<12;i++)
     {
       f=2.*clamp(f,-cs,cs)-f;
       float c=max(fs/dot(f,f),1.);
       f*=c;
       v*=c;
       f+=fc;
     }
   float z=length(f.xy)-fu;
   return fd*max(z,abs(length(f.xy)*f.z)/sqrt(dot(f,f)))/abs(v);
}

vec4 rd(vec2 f)
{
   vec2 v=(.5*rs-f)/vec2(rs.x,-rs.y);
   v.x*=rs.x/rs.y;
   vec3 s=normalize(cr*vec3(v.x*vec3(1,0,0)+v.y*vec3(0,1,0)-.9*vec3(0,0,1)));
   float i=0.;
   vec3 z=cp+i*s;
   float c=6e-07,m;
   int y=0;
   bool p=false;
   z=cp+i*s;
   float e=1.48659*(1./rs.y)*.6;
   for(int r=0;r<200;r++)
   {
       y=r;
       m=dE(z);
       if(p&&m.x<c||i>10000.)
         {
           y--;
           break;
         }
       p=false;
       i+=m;
       z=cp+i*s;
       c=i*e;
       if(m<c)
         p=true;
   }
   float r=float(y)/float(200);
   vec4 d=vec4(1.,1.,1.,.45),u=vec4(1.,1.,0.,1.),a=vec4(0.,0.,0.,1.),x=vec4(0.,1.,1.,.55);
   float t=1.;
   vec4 l=vec4(clamp(mix(a.xyz,u.xyz,(sin(s.y*asin(1.))+1.)*.5),0.,1.),1.),n=d;
   if(p)
   {
       float g=clamp(r*x.w*3.,0.,1.),w=max(c*.5,1.5e-07);
       vec3 o=normalize(vec3(dE(z+vec3(w,0,0)).x-dE(z-vec3(w,0,0)).x,dE(z+vec3(0,w,0)).x-dE(z-vec3(0,w,0)).x,dE(z+vec3(0,0,w)).x-dE(z-vec3(0,0,w)).x));
       float E=1.,k=c*9.,b=.15/k,F=2.*k;
       for(int C=0;C<4;++C)
         E-=(F-dE(z+o*F).x)*b,F+=k,b*=.5;
       t=clamp(E,0.,1.);
       vec3 C=clamp(mix(a.xyz,u.xyz,(sin(o.y*asin(1.))+1.)*.5),0.,1.);
       C=mix(vec3(.5),C,.3);
       float h=max(dot(o,normalize(vec3(-16.,100.,-60.)-z)),0.);
       vec3 q=n.xyz*d.w;
       n.xyz=(C*q+q*h+pow(h,4.)*.8)*t;
       n.xyz=mix(n.xyz,x.xyz,g);
   }
   else
   ;
   n.xyz=mix(l.xyz,n.xyz,exp(-pow(i*exp(ff),2.)*.01));
   return vec4(n.xyz,1.);
}

mat3 mr(vec3 s,float f)
{
   float z=cos(radians(f)),c=sin(radians(f));
   return mat3(z+(1.-z)*s.x*s.x,(1.-z)*s.x*s.y-c*s.z,(1.-z)*s.x*s.z+c*s.y,(1.-z)*s.x*s.y+c*s.z,z+(1.-z)*s.y*s.y,(1.-z)*s.y*s.z-c*s.x,(1.-z)*s.x*s.z-c*s.y,(1.-z)*s.y*s.z+c*s.x,z+(1.-z)*s.z*s.z);
}

void main()
{
   float f=0.,z=90.,c=0.;
   fd=.763;
   fu=10.;
   fs=1.;
   fc=vec3(0);
   ff=-.5;
   cs=vec3(.808,.808,1.167);
   float p=1.,v=st;
   int i=sn;
   vec2 s=gl_FragCoord.xy;
   float r=fract(sin(dot(s.xy,vec2(12.9898,78.233)))*43758.5+v*.25)*(sin(v+s.y)*2+4.);
   if(i>=1)
     {
       if(r>v)
         v=lt,i=i-1;
     }
   //v=mod(time*5,100.);
   //i=int(mod(time*.11,10.));
   if(i==0)
     p=clamp(v*.055,0.,1.),z=23.9,cp=vec3(.8,14.8366-v*.095,-1.80153),cs.y=.58;
   if(i==1)
     cp=vec3(.7212+v*.00068,.1,-2.35),cs.xy=vec2(.5);
   if(i==2)
     cp=vec3(.7+(-.0092+v*.00018),.1,-2.3495),cs.xy=vec2(.5);
   if(i==3)
     fu=1.01,cp=vec3(v*.05,.02,-8.),cs.x=.9;
   if(i==4)
     fu=1.01,cp=vec3(.0007,.02,.1-v*.00028),z=0.,c=v*.009*180,cs.x=.9;
   if(i==6)
     cp=vec3(1.18,.08+v*5.5e-05,-.24),z=120.,cs=vec3(.5,.5,1.04);
   if(i==5)
     fu=.9,cp=vec3(v*.00025,1.404,-2.382+(-.05+v*.002));
   if(i==7)
     fd=.7,fs=1.34,cp=vec3(.69+(-.0092+v*.0008),.1,-2.3495),z=130+sin(v*.075)*50,f=90.,cs.xy=vec2(.5);
   if(i==8)
     cp=vec3(0.,-.86+v*.0003,.3),z=80+v*.65,fc.z=-.38;
   float x=0.;
   if(i==9)
     p=clamp(v*.025,1.,100.),fu=1.2,cp=vec3(0.,1.4+(-.02+v*.0018),-2.341),fc.z=.2584,ff=clamp(v*.05,3.6,100.),x=clamp((v-35)*.005,0.,1.);
   cr=mr(vec3(0,1,0),180.-c)*mr(vec3(1,0,0),-z)*mr(vec3(0,0,1),f);
   vec4 t=vec4(0.);
   //#define antialiasing 0.125
   //#define antialiasing 0.5
   #ifdef antialiasing
   float m=0.;
   for(float y=0.;y<1.;y+=float(antialiasing))
     {
       for(float u=0.;u<1.;u+=float(antialiasing))
         t+=rd(s.xy+vec2(y,u)),m+=1.;
     }
   t/=m;
   #else
   t=rd(s.xy);
   #endif
   gl_FragColor=vec4(pow(t.xyz+x,vec3(1./p)),t.w);
}