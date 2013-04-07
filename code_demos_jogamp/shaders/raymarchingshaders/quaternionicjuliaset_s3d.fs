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
 ** Advanced fragment shader implementing a GPGPU raymarcher to render a 4D quaternionic Julia Set.
 ** The raymarching process is bound by "marchingmaxraylength". Raymarching accuracy can be configured 
 ** with "marchingstepsize" and "marchingaccuracy". Surface normal approximation can be controlled with 
 ** "epsilon". Uses phong based lighting with specular (implicit white). Currently 8 lightsources are 
 ** configured in a cube-layout around the worlds origin. Lightsource colors a obtained from a 1D LUT 
 ** configured as sampler uniform. The Matrix setup and calculation is done by hand as using the buildin
 ** OpenGL matrices would be quite inconvenient handling wise.
 **
 ** On Windows consider to disable/reconfigure this "feature" when using a slow GPU: 
 ** http://www.blog-gpgpu.com/ 
 ** http://www.microsoft.com/whdc/device/display/wddm_timeout.mspx
 **
 ** Inspired by the following sources:
 ** IQ's Terrain Raymarching http://iquilezles.org/www/articles/terrainmarching/terrainmarching.htm
 ** Potatro, RayMarching and DistanceFields - A story of SphereTracing: http://code4k.blogspot.com/2009/10/potatro-and-raymarching-story-of.html
 ** Algebraic surfaces: http://www.freigeist.cc/gallery.html
 ** Raymarching discussion on pouet.net: http://www.pouet.net/topic.php?which=6675&page=1&x=13&y=12
 ** Bisection and isosurface normal approximation: http://sizecoding.blogspot.com/2008/08/isosurfaces-in-glsl.html
 ** Raytracing on the GPU (in german): http://www.uninformativ.de/?section=news&ndo=single&newsid=108
 ** WebGL Quaternionic Julia Set raymarching: http://www.iquilezles.org/apps/shadertoy/
 ** Ray Tracing Quaternion Julia Sets on the GPU: http://www.devmaster.net/forums/showthread.php?t=4448
 ** GPU Gems 3 - Chapter 30 - Real-Time Simulation and Rendering of 3D Fluids: http://http.developer.nvidia.com/GPUGems3/gpugems3_ch30.html
 ** GPU Raycasting Tutorial: http://cg.alexandra.dk/2009/04/28/gpu-raycasting-tutorial/
 ** A Simple and Flexible Volume Rendering Framework for Graphics-Hardware-based Raycasting: http://www.vis.uni-stuttgart.de/ger/research/fields/current/spvolren/
 **
 ** This is a simple adaption to generate a stereoscopic image pair. It used kinda Toe-in (incorrect)
 ** method for stereo. See here for more information on calculating stereoscopic image pairs: 
 ** http://paulbourke.net/stereographics/stereorender/   
 **
 **/

uniform vec2 resolution;
uniform float time;
uniform sampler1D sampler0;
uniform float eyeoffset;

float surfacefunction(vec3 hitpoint) {
    vec4 z  = vec4(hitpoint,0.0);
    vec4 z2 = vec4(1.0,0.0,0.0,0.0);
    vec4 c = vec4(0.7*cos(0.5*time),0.7*sin(0.3*time),0.7*cos(1.0*time),0.0);
    float n = 0.0;
    float squared_z = 0.0;
    while (n<6.0) {
        z2 = vec4(z[0]*z2[0]-z[1]*z2[1]-z[2]*z2[2]-z[3]*z2[3],
                  z[0]*z2[1]+z[1]*z2[0]+z[2]*z2[3]-z[3]*z2[2],
                  z[0]*z2[2]-z[1]*z2[3]+z[2]*z2[0]+z[3]*z2[1],
                  z[0]*z2[3]+z[1]*z2[2]-z[2]*z2[1]+z[3]*z2[0])*2.0;
        z = vec4(z[0]*z[0]-z[1]*z[1]-z[2]*z[2]-z[3]*z[3],
                 2.0*z[0]*z[1],
                 2.0*z[0]*z[2],
                 2.0*z[0]*z[3])+c;
        squared_z = dot(z, z);
        if (squared_z>=4.0) {
            break;
        }
        n++;
    }
    return squared_z-4.0;
} 

vec4 main_rendering(vec2 fragmentcoord) {
    //hardcode everything X-)
    int numlights = 5;
    vec3 lightsposition[5];
    lightsposition[0] = vec3( 2.0,-2.0,-2.0);
    lightsposition[1] = vec3( 2.0,-2.0, 2.0);
    lightsposition[2] = vec3(-2.0,-2.0, 2.0);
    lightsposition[3] = vec3(-2.0,-2.0,-2.0);
    lightsposition[4] = vec3( 2.0, 2.0,-2.0);
    vec3 lightsdiffuse[5];
    lightsdiffuse[0] = texture1D(sampler0,0.00).rgb;
    lightsdiffuse[1] = texture1D(sampler0,0.25).rgb;
    lightsdiffuse[2] = texture1D(sampler0,0.50).rgb;
    lightsdiffuse[3] = texture1D(sampler0,0.75).rgb;
    lightsdiffuse[4] = texture1D(sampler0,1.00).rgb;

    const vec3 materialdiffuse = vec3(1.0, 1.0, 1.0);
    const float materialspecularexponent = 256.0;

    //interpolate eye position from billboard fragment coordinates    
    vec2 position = -1.0+2.0*fragmentcoord.xy/resolution.xy;
    position.x *= resolution.x/resolution.y;

    //define camera position and target
    vec3 camera = vec3(0.0+eyeoffset, 0.0, 5.0);
    vec3 target = vec3(position,0.0) + vec3(0.0, 0.0,1.5);

    //do the matrix calculations by hand X-)
    //as mat4 constructor and arithmetic assignments are 
    //currently broken (2010-09-21) on ATI cards i found
    //a workaround using vec4 constructors wich works on
    //both NVIDIA+ATI --- MAGIC. DO NOT TOUCH! -=#:-)
    float theta = 0.25*time;
    mat4 yrot = mat4(
        vec4(cos(theta), 0.0, -sin(theta), 0.0),
        vec4(       0.0, 1.0,         0.0, 0.0),
        vec4(sin(theta), 0.0,  cos(theta), 0.0),
        vec4(       0.0, 0.0,         0.0, 1.0)
    );

    camera = vec3(yrot*vec4(camera,1.0));
    target = vec3(yrot*vec4(target,1.0));

    vec3 ray = normalize(target-camera);
    //config raymarching bound parameters
    //const float baseaccuracy = 0.1;
    const float baseaccuracy = 0.01;
    //const float baseaccuracy = 0.001; //Madness? THIS IS SPARTA!
    const float marchingstepsize = baseaccuracy;
    const float marchingaccuracy = baseaccuracy/100.0;
    const float marchingmaxraylength = 10.0;
    const float epsilon = baseaccuracy/1000.0;
    //fixed step raymarching with simple bisection refinement
    float currentstep = marchingstepsize;
    float currentpos = currentstep;
    vec3 hitpoint = camera+currentpos*ray;
    bool currentevaluation = (surfacefunction(hitpoint)<0.0);
    currentpos += currentstep;
    bool startevaluation = currentevaluation;
    //core raymarching loop
    while (currentpos<marchingmaxraylength) {
        hitpoint = camera+currentpos*ray;
        currentevaluation = (surfacefunction(hitpoint)<0.0);
        //bisection inner loop
        if (currentevaluation!=startevaluation) {
            float temppos = currentpos-marchingstepsize;
            while (currentstep>marchingaccuracy) {
                currentstep *= 0.5;
                currentpos = temppos+currentstep;
                hitpoint = camera+currentpos*ray;
                currentevaluation = (surfacefunction(hitpoint)<0.0);
                if (currentevaluation==startevaluation) {
                    temppos = currentpos;
                }
            }
            //found an intersection :-) calculate the normal
            vec3 normal;
            //seems inefficient to me - there must be a better way to do this ?-)
            //single finite difference shot to approximate the normal derivate
            normal.x = surfacefunction(hitpoint+vec3(epsilon,0.0,0.0));
            normal.y = surfacefunction(hitpoint+vec3(0.0,epsilon,0.0));
            normal.z = surfacefunction(hitpoint+vec3(0.0,0.0,epsilon));
            //---
            normal -= surfacefunction(hitpoint);
            normal = normalize(normal);
            //phong shading calculations for surface hitpoint
            vec3 color = vec3(0, 0, 0);
            vec3 camera_direction = normalize(camera-hitpoint);
            for (int i=0; i<numlights; i++) {
                vec3 light_dir = normalize(lightsposition[i]-hitpoint);
                float diffuse = max(dot(light_dir, normal),0.0);
                float specular = max(dot(reflect(-light_dir, normal), camera_direction),0.0);
                color += (lightsdiffuse[i]*diffuse).xyz*materialdiffuse.xyz;
                //implicit white specular
                color += pow(specular, materialspecularexponent);
            }
            return vec4(color, 1.0);
        }
        currentpos += currentstep;
    }
    //no intersection found X-(
    return vec4(0.0,0.0,0.0,1.0);
}

void main(void) {

    vec4 color = vec4(0);
    
#define antialiasing 0.5       
#ifdef antialiasing
    float n = 0.0;
    for (float x = 0.0; x < 1.0; x += float(antialiasing)) {
        for (float y = 0.0; y < 1.0; y += float(antialiasing)) {
            color += main_rendering(gl_FragCoord.xy + vec2(x, y));
            n += 1.0;
        }
    }
    color /= n;
#else
    color = main_rendering(gl_FragCoord.xy);
#endif    
    //output
    gl_FragColor = color;
}
