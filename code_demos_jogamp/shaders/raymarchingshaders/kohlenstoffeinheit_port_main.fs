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
 ** JOGL2 port of my PC 4k intro competition entry for Revision 2013. Sure it got a little bigger 
 ** while porting but the shader and control code remained more or less untouched. The intro renders
 ** a fullscreen billboard using a single fragment shader. The shader basically encapsulates a 
 ** sphere-tracing based raymarcher for a single fractal formula with camera handling. The rendering 
 ** technique is a little bit different than usual as the renderer accumulates the spheretraced distances 
 ** of the estimation function over the ray-length, and converts that into a pixel brightness, resulting in
 ** some sort of "X-Ray" look for the marched volume.
 **
 ** Additionally a second post-processing shader is applied to the render output from the raymarching shader.
 ** Post effects are radial blur, vignette, color abbrevation, tv-lines and noise to make the overall look 
 ** more interesting and less 'sterile'. The different intro parts are all parameter and camera position 
 ** variations of the same fractal. 
 **
 ** As this shader is the 'orignal' minified version of the shader and a little bit hard to understand I 
 ** also included the 'normal' version I used during development: 'kohlenstoffeinheit_development_main.fs'.
 **
 ** Papers and articles you should be familiar with before trying to understand the code:
 **
 ** Distance rendering for fractals: http://www.iquilezles.org/www/articles/distancefractals/distancefractals.htm
 ** Sphere tracing: A geometric method for the antialiased ray tracing of implicit surfaces: http://graphics.cs.uiuc.edu/~jch/papers/zeno.pdf
 ** Rendering fractals with distance estimation function: http://www.iquilezles.org/www/articles/mandelbulb/mandelbulb.htm
 **
 ** For an impression how this routine looks like see here: http://www.youtube.com/watch?v=aFCcneO5HIA
 ** Original release from the Revision 2012 can be found here: http://www.pouet.net/prod.php?which=61181
 **/

uniform vec2 rs;
uniform int sn;
uniform float st,lt;
int i,v=0;
vec2 f;
mat3 x;
vec3 y,s,c,z,t,r;
float l,n,m,e,u,g;
mat3 p(vec3 s,float v) {
    float z=cos(radians(v)),l=sin(radians(v));
    return mat3(z+(1.-z)*s.x*s.x,(1.-z)*s.x*s.y-l*s.z,(1.-z)*s.x*s.z+l*s.y,(1.-z)*s.x*s.y+l*s.z,z+(1.-z)*s.y*s.y,(1.-z)*s.y*s.z-l*s.x,(1.-z)*s.x*s.z-l*s.y,(1.-z)*s.y*s.z+l*s.x,z+(1.-z)*s.z*s.z);
}

float p(vec3 y) {
    vec3 s=y,u=c,e=vec3(0),g=vec3(0),r=vec3(0),t=vec3(0),x=vec3(0),a=vec3(0),b=vec3(0,.01,.01),o=vec3(0,0,.1),d=vec3(1,-.82692,.65384),F=vec3(.1,.1,.1),C=vec3(0.,.1,.1),w=vec3(.29546,-.52272,1);
    vec2 q=vec2(1.,3.);
    bool k=false;
    float h=.54237,Z=.54,Y=.001,X=.005,W=.34146,V=.04615;
    if(v==2) {
        s+=vec3(0,0,-1)*n*.2069*10.;
        s+=vec3(0,0,-1)*(.8+sin(n*2.5974+10))*1.7241;
        float U=.368085;
        s+=cos(s*.8048/U+n*.7058*10.)*.3297*.5*U;
    }
    if(v==2)
      b=vec3(0),Y=0.,o=vec3(0),X=0.,q=vec2(1.19,2.5),d=vec3(1,.5,.5),F=vec3(.5,1.25,0),k=true,W=.13415,V=.15,C=vec3(0),h=0.,w=vec3(.13636,0,0),Z=0.;
    e=s*b*m*Y*.02;
    g=s*o*X*.2;
    r=s*C*h*.5;
    t=s*w*Z*2.;
    x=d*W*10.;
    a=(F+r)*V*.5;
    if(k)
      x=vec3(x.x,x.x,x.x)*2.;
    vec3 U=vec3(sin(n*x.x+t.x)*a.x,cos(n*x.y+t.y)*a.y,sin(n*x.z+t.z)*a.z);
    float T=clamp(m+(e.x+e.y+e.z),q.x,q.y);
    mat3 S=p(normalize(z+U+g),l);
    if(v==0)
      S=p(normalize(z),l);
    int R;
    for(R=0;R<i;R++)
      s*=S,s.xy=abs(s.xy+f.xy)-f.xy,s=s*T+u;
    return length(s)*pow(T,-float(R));
}

vec3 a(vec2 z) {
    vec2 s=(.5*rs-z)/vec2(rs.x,-rs.y);
    s.x*=rs.x/rs.y;
    vec3 v=normalize(x*vec3(s.x*vec3(1,0,0)+s.y*vec3(0,1,0)-.9*vec3(0,0,1)));
    float i=0.,l=0.,f=0.,c;
    for(int m=0;m<250;m++) {
        c=i;
        i=p(y+f*v);
        f+=i;
        if(f>g)
          break;
        l+=exp(-1.*abs(i-c))-sqrt(f)*u;
    }
    return vec3(mix(r,t,l*e*.02));
}

void a(float z,vec3 v,vec3 s,vec3 x,vec3 l) {
    t=mix(v,s,z),r=mix(x,l,z);
}
  
void a(int r,float t) {
    vec3 q=vec3(.95,.85,1.),o=vec3(0),d=vec3(.8,1.,.7),F=vec3(.1,.05,.18),C=vec3(.7,1.,1.),w=vec3(.05,.05,.08);
    v=0;
    e=1.;
    u=.3;
    g=25.;
    i=25;
    m=1.33857;
    f=vec2(0,.268);
    c=vec3(-.932,-.39,-1.211);
    z=vec3(-.304,-.239,-.739);
    if(r==0)
      y=vec3(.14,.09+t*.08,6.51),i=35,m=1.22,f=vec2(.569,1),c=vec3(-.57,.16,-1.31),z=vec3(.76,-.17,-.52),l=84,g=1.9,e=clamp(t*.05,0.,1.),s=vec3(99,195,497),a(0.,q,d,o,F);
    if(r==1)
      y=vec3(-3.1+t*.025,-.75+t*.001,.82),i=20,m=1.5,f=vec2(.24,12.85),c=vec3(.45,-1.,-.31),z=vec3(-.08,.6,-.93),l=49,s=vec3(75,70,148),a(.2,q,d,o,F);
    if(r==2)
      n=t*.08,y=vec3(4.02,-2.67,2.23),i=16,m=1.4+t*.00125,f=vec2(.91,1),c=vec3(-2,0.,-.19),z=vec3(.08,-.13,.12),l=-53,s=vec3(62+t*3,78,492),v=1,a(.4,q,d,o,F);
    if(r==3)
      y=vec3(6.65,-7.35,-3.69),m=.2+(1050.-t*2.75)*.001+.22,f=vec2(1),c=vec3(-1.72,.44,-.34),z=vec3(.08,-.1,-.97),l=110,u=.1,s=vec3(818,530,852),a(.8,q,d,o,F);
    if(r==4)
      y=vec3(3.01,-3.5+t*.07,-2.19),i=60,m=1.14765,f=vec2(.91057,1),c=vec3(-1.72034,.44068,-.34745),z=vec3(.08696,-.1087,-.97826),l=110.603,u=.23,s=vec3(77,331,351),a(.1,d,C,F,w);
    if(r==5)
      n=t*.1,y=vec3(7.1,7.87,.43),i=45,m=1.155,f=vec2(1),c=vec3(-2,.389,-.372),z=vec3(.086,-.13,.149),l=-45,e=.7,u=.1,g=4.+t*.2,s=vec3(76,98,238),v=1,a(1.,q,d,o,F);
    if(r==6)
      y=vec3(.5,-1.67+t*.012,3.9),l=-46,s=vec3(79,369,341),a(.2,d,C,F,w);
    if(r==99)
      n=t*.07,y=vec3(.46,.53,3.6),l=-58,e=.5,u=.1,s=vec3(79,406,341),a(.4,d,C,F,w),v=1;
    if(r==7)
      n=-19.+t*.17,y=vec3(7.,-22.36,-15.31),m=1.11,f=vec2(.94,.96),c=vec3(-1.72,.41,-.34),z=vec3(.08,-.13,-.98),l=106,g=50.,u=.09,s=vec3(176,53,110),v=2,a(.6,d,C,F,w);
    if(r==8)
      y=vec3(1.26-1.78*(t*.01),-2.92+1.41*(t*.01),.88),i=19,m=1.38+t*.0015,f=vec2(.34,.78),c=vec3(-1.18,-.93,-.37),z=vec3(-.08,.6,-.93),l=49,s=vec3(74,71,118),a(.8,d,C,F,w);
    if(r==9)
      y=vec3(.44,-1.47,3.93),i=30,l=-33,u=.4,s=vec3(63,254,171+t*2),a(1.,d,C,F,w);
    if(r==10)
      n=t*.25,y=vec3(7.,8.8,.49),i=15,m=1.06+t*.004,f=vec2(.48,1.04),c=vec3(-1.44,-.0901,-.212),z=vec3(.086,-.13,.149),l=-46,u=.07+t*.0025,s=vec3(76,93,238),v=1,a(1.,d,C,F,w);
    x=p(vec3(0,1,0),180.-s.y)*p(vec3(1,0,0),-s.z)*p(vec3(0,0,1),s.x);
}

void main() {
    float s=st;
    vec2 z=gl_FragCoord.xy;
    float r=fract(sin(dot(z.xy,vec2(12.9898,78.233)))*43758.5+s*.25)*(sin(s+z.y)*2+4.);
    int v=sn;
    if(v>=1) {
        if(r>s)
          s=lt,v=v-1;
    }
    a(v,s);
    vec3 l=vec3(0);
    if(v==7)
      a(99,s),i=17,l+=a(z)*.8,a(7,s),l*=clamp(a(z),.15,1.)*7;
    else
       if(v==5)
         a(99,s),i=13,l+=a(z),a(5,s),l*=clamp(a(z),.05,1.)*6;
      else
         l=a(z);
    gl_FragColor=vec4(l,1.);
}