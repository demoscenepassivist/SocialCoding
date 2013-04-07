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
 ** JOGL2 port of my PC 4k intro competition entry for Revision 2013. This is the post-processing
 ** fragment shader that takes the raymarching shader output from the FBO and applies radial blur,
 ** vignette, color abbrevation, tv-lines and noise to make the overall look more interesting and
 ** less 'sterile'.  
 **
 ** This is the 'normal', unminified version I used during development.
 **
 ** For an impression how this routine looks like see here: http://www.youtube.com/watch?v=aFCcneO5HIA
 ** Original release from the Revision 2012 can be found here: http://www.pouet.net/prod.php?which=61181
 **/

uniform float st;
uniform vec2 rs;
uniform sampler2D s0;

float rand(vec2 position) {
   return fract(sin(dot(position.xy ,vec2(12.9898,78.233))) * 43758.5453+(st*0.5));
}

vec3 blur(vec2 coords) {
   vec2 powers = pow(abs(vec2(coords.x - 0.5,coords.y - 0.5)),vec2(2));
   float noise = rand(coords.xy)*0.004*smoothstep(0.05,0.45,powers.x+powers.y)*5.5;
   vec2 xy1 = coords+noise;
   vec2 xy2 = coords-noise;
   return (
            (
             texture2D(s0, xy1)+
             texture2D(s0, xy2)+
             texture2D(s0, vec2(xy1.x, xy2.y))+
             texture2D(s0, vec2(xy2.x, xy1.y))
            )/4.
          ).rgb;
}

void main(void) {
   vec2 position = gl_FragCoord.xy / rs.xy;
   //cubic lens distortion
   vec2 rescale_term = 0.9*(position.xy-0.5);
   vec3 color = vec3(
      blur(1.0675*rescale_term+0.5).r,
      blur(1.045*rescale_term+0.5).g,
      blur(1.0225*rescale_term+0.5).b
   );  
   position.y *=-1.0;
   //circular vignette fade
   color *= 0.5 + 8.0*position.x*position.y*(1.0-position.x)*(-1.0-position.y);   
   //tvlines effect
   color *= 1.0+0.3*sin(position.y*(rs.y*1.55));
   //tvflicker effect
   color *= 1.45+0.3*sin(1.75*st);   
   //crazy noise
   //remove for utube support :)
   color = (color*0.85)+(color*0.25*vec3(rand(position)));
        
   gl_FragColor = vec4(color,1.0);
}