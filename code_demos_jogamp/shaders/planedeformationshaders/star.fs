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
 ** Simple plane deformation fragment shader to render a distorted texture to a billboard. 
 ** Most shader code is from this site: http://www.iquilezles.org/www/articles/deform/deform.htm
 **
 **/

uniform float time;
uniform vec2 resolution;
uniform vec3 position;
uniform sampler2D sampler0;

void main(void) {
    vec2 uv;

    vec2 p = -1.0 + 2.0 * gl_FragCoord.xy / resolution.xy;
    float a = atan(p.y,p.x);
    float r = sqrt(dot(p,p));
    float s = r * (1.0+0.8*cos(time*1.0));

    uv.x =          .02*p.y+.03*cos(-time+a*3.0)/s;
    uv.y = .1*time +.02*p.x+.03*sin(-time+a*3.0)/s;

    float w = .9 + pow(max(1.5-r,0.0),4.0);

    w*=0.6+0.4*cos(time+3.0*a);

    vec3 col =  texture2D(sampler0,uv).xyz;

    gl_FragColor = vec4(col*w,1.0);
}