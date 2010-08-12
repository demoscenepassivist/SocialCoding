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
    vec2 p = -1.0 + 2.0 * gl_FragCoord.xy / resolution.xy;
    vec2 uv;

    float r = pow( pow(p.x*p.x,16.0) + pow(p.y*p.y,16.0), 1.0/32.0 );
    uv.x = .5*time + 0.5/r;
    uv.y = 1.0*atan(p.y,p.x)/3.1416;

    vec3 col =  texture2D(sampler0,uv).xyz;

    gl_FragColor = vec4(col*r*r*r,1.0);
}