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
 ** Fragment shader simulating refraction (and reflection). Also takes fresnels term into account
 ** to correctly calculate view dependant blend of reflection/refraction. 
 **
 **/

const float materialdepth = 0.2;
const float materialsamplermixratio = 1.7;
const float samplerwidth = 1920.0;
const float samplerheight = 1200.0;
uniform sampler2D sampler0;
varying vec3  normal;
varying vec3  cameradir;
varying vec4  camerapos;

void main (void) {
    vec3 reflectdirection = reflect(cameradir, normal);
    vec2 index;
    index.y = dot(normalize(reflectdirection), vec3(0.0, 1.0, 0.0));
    reflectdirection.y = 0.0;
    index.x = dot(normalize(reflectdirection), vec3(1.0, 0.0, 0.0)) * 0.5;
    if (reflectdirection.z >= 0.0) {
        index = (index + 1.0) * 0.5;
    } else {
        index.t = (index.t + 1.0) * 0.5;
        index.s = (-index.s) * 0.5 + 1.0;
    }
    vec3 reflectioncolor = vec3 (texture2D(sampler0, index));
    float fresnel = abs(dot(normalize(cameradir), normal));
    fresnel *= materialsamplermixratio;
    fresnel = clamp(fresnel, 0.1, 0.9);
    vec3 refractionDir = normalize(cameradir) - normalize(normal);
    float depthVal = materialdepth / -refractionDir.z;
    float recipW = 1.0 / camerapos.w;
    vec2 eye = camerapos.xy * vec2(recipW);
    index.s = (eye.x + refractionDir.x * depthVal);
    index.t = (eye.y + refractionDir.y * depthVal);
    index.s = index.s / 2.0 + 0.5;
    index.t = index.t / 2.0 + 0.5;
    float recip1kX = 1.0 / 1920; //2048.0;
    float recip1kY = 1.0 / 1200; //2048.0;
    index.s = clamp(index.s, 0.0, 1.0 - recip1kX);
    index.t = clamp(index.t, 0.0, 1.0 - recip1kY);
    index.s = index.s * samplerwidth * recip1kX;
    index.t = index.t * samplerheight * recip1kY;
    vec3 refractioncolor = vec3(texture2D(sampler0, index));
    gl_FragColor = vec4 (mix(reflectioncolor, refractioncolor, fresnel), 1.0);
}