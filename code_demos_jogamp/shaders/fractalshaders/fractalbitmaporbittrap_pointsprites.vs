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
 ** Procedural point sprite vertex shader generating a fractal-bitmap-orbit-trapping image 
 ** (here a single fragment for a single vertex ofcourse). The generated depth corresponds
 ** to the iteration count of the IFS before it spirals to 0/escapes to infinity.
 **
 ** Papers and articles you should be familiar with before trying to understand the code:
 ** Geometric orbit traps: http://www.iquilezles.org/www/articles/ftrapsgeometric/ftrapsgeometric.htm
 ** Bitmap orbit traps: http://www.iquilezles.org/www/articles/ftrapsbitmap/ftrapsbitmap.htm
 **
 **/

#version 120

//uniform sampler2D sampler1;
varying float depth;
varying vec4 frag_color;

//#define supersamplingfactor 0.5

uniform float time;
uniform vec2 resolution;
uniform sampler2D texture;
uniform bool juliamode;
uniform bool zoomin;
uniform int juliapower;
uniform float orbittrapscale;
uniform int iterationslimit;
uniform bool invertdisplace;
uniform bool rubber;

#define complexArg(z) float(atan(z.y, z.x))
#define polar(r,a) vec2(cos(a) * r, sin(a) * r)
#define complexPower(z,p) vec2(polar(pow(length(z), float(p)), float(p) * complexArg(z)))
#define maxiterations 256

float zoomfactor;

void calculateorbittrap(vec2 z,out vec4 color,out int iterationscounter) {
    vec2 juliaoffset = vec2(0.36,0.36); //from -2 up to 2
    //vec4 color = vec4(0.0,0.0,0.0,1.0); //backgroundcolor
    color = vec4(0.0,0.0,0.0,1.0); //backgroundcolor

    vec2 c;
    if (zoomin && juliamode) {
       juliaoffset = vec2(-0.74000,-0.42000);
       c = juliaoffset;
    } else if (juliamode) {
       juliaoffset.x = juliaoffset.x*(sin(time*0.15)*0.5+orbittrapscale*0.25);
       juliaoffset.y = juliaoffset.y*(cos(time*0.25)*3.0+orbittrapscale*0.25);
       c = juliaoffset;
    } else {
       c = z;
    }
    float otrcos = cos(radians(time*0));
    float otrsin = sin(radians(time*0));
    mat2 orbitrotation = mat2(otrcos, otrsin, -otrsin, otrcos);
    float otscos = cos(radians(time*25));
    float otssin = sin(radians(time*25));
    mat2 orbitspin = mat2(otscos, otssin, -otssin, otscos); 
    float otscale = sin(orbittrapscale*0.1)*15;

    vec4 orbitpixels[maxiterations];
    iterationscounter = 0;
    for (int i = 0; i < iterationslimit; i++) {
        z = complexPower(z, juliapower) + c;    
        if(dot(z,z)>4.0) {
            break;
        }
        iterationscounter++;
        //vec2 orbittrapoffset = vec2(0.0,0.0);
        //vec2 sp = 0.5 + (z / otscale * orbitrotation - orbittrapoffset) * orbitspin;
        //formula without orbittrapoffset:
        vec2 sp = 0.5 + (z / otscale * orbitrotation) * orbitspin;
        orbitpixels[i] = texture2D(texture, sp);
    }
    //optimize iteration count ... doesnt look good on mandelbrot
    //int startiteration = max(maxiterations-iterationslimit,iterationscounter);   
    //for (int i = startiteration; i <maxiterations; i++) {
    
    for (int i =  maxiterations-iterationslimit; i <maxiterations; i++) {
        float iterationfade = 1.0-(i/(int(maxiterations)-int(zoomfactor/3500)));
        color = mix(color,
                    orbitpixels[(maxiterations-1)-i], 
                    orbitpixels[(maxiterations-1)-i].a*iterationfade);
    }    
    //return color;
}

void render(vec2 fragcoord,out vec4 color,out int iterationscounter) {
    float aspectratio = resolution.x/resolution.y;
    vec3 cameraposition;
    float globalrotation;
    if (juliamode) {
        cameraposition = vec3(-0.038698,-0.042873,2.095347);
        //globalrotation = time;
        globalrotation = 0.0;
    } else {
        cameraposition = vec3(-0.5,0.0,2.25);
        //globalrotation = time;
        globalrotation = 0.0;
    }    
    vec2  z = ((fragcoord - (resolution * 0.5)) / resolution) * vec2(aspectratio, 1.0) * cameraposition.z + cameraposition.xy;
    mat2 rotationmatrix = mat2(cos(radians(globalrotation)), sin(radians(globalrotation)), -sin(radians(globalrotation)), cos(radians(globalrotation)));
    z *= rotationmatrix;
    if (zoomin && juliamode) {
       float zoom = .72+.38*sin(.1*time);
       zoom = pow(zoom,9.0);
       zoomfactor = 1.0/zoom; //~200000 at the end of single precision
       const float xcenter = 0.51628;
       const float ycenter = 0.20632;      
       calculateorbittrap(vec2(xcenter,ycenter)+z*zoom,color,iterationscounter);
    } else if (zoomin) {
       float zoom = .72+.38*sin(.1*time);
       zoom = pow(zoom,8.0);
       zoomfactor = 1.0/zoom; //~200000 at the end of single precision
       const float xcenter = 0.3245046418497685;
       const float ycenter = 0.04855101129280834;
       calculateorbittrap(vec2(xcenter,ycenter)+z*zoom,color,iterationscounter);
    } else {
       calculateorbittrap(z,color,iterationscounter);
    }
}

void main(void) {
    frag_color = vec4(0);
    gl_TexCoord[0] = gl_MultiTexCoord0;
    int iterationscounter;
#ifdef supersamplingfactor
    vec4 color = vec4(0.0);
    float iterations = 0;
    float n = 0.0;
    vec4 current_fragment;
    int current_iterationscounter;
    vec2 fragmentcoord = gl_TexCoord[0].st*resolution;
    for (float x = 0.0; x < 1.0; x += float(supersamplingfactor)) {
        for (float y = 0.0; y < 1.0; y += float(supersamplingfactor)) {
            
            render(fragmentcoord + vec2(x, y),current_fragment,current_iterationscounter);
            color += current_fragment;
            iterations += current_iterationscounter;
            n += 1.0;
        }
    }
    frag_color = color/n;
    iterationscounter = iterations/n;
#else
    //color = render(gl_FragCoord.xy);
    //frag_color = 
    //int iterationscounter;
    render(gl_TexCoord[0].st*resolution,frag_color,iterationscounter);
#endif
    
    //vec4 displace_texture = texture2D(sampler1, gl_TexCoord[0].st);    
    float gray = dot(frag_color.rgb, vec3(0.299, 0.587, 0.114));
 
    vec4 raw_vertex = gl_Vertex;
    if (invertdisplace) {
        raw_vertex.z += gray*15;
        raw_vertex.z += iterationscounter/3.5;
    } else {
        raw_vertex.z -= gray*15;
        raw_vertex.z -= iterationscounter/3.5;
    }
    
    if (rubber) {
        //do the matrix calculations by hand X-)
        //as mat4 constructor and arithmetic assignments are 
        //currently broken (2010-09-21) on ATI cards i found
        //a workaround using vec4 constructors wich works on
        //both NVIDIA+ATI --- MAGIC. DO NOT TOUCH! -=#:-)
        float rubberfactor = 10.0;
        /*
        float phi = gl_TexCoord[0].t;
        mat4 xrot = mat4(
            vec4(1.0,       0.0,      0.0, 0.0),
            vec4(0.0,  cos(phi), sin(phi), 0.0),
            vec4(0.0, -sin(phi), cos(phi), 0.0),
            vec4(0.0,       0.0,      0.0, 1.0)
        );
        */
        float theta = 0.35*(time+(gl_TexCoord[0].t*rubberfactor));
        mat4 yrot = mat4(
            vec4(cos(theta), 0.0, -sin(theta), 0.0),
            vec4(       0.0, 1.0,         0.0, 0.0),
            vec4(sin(theta), 0.0,  cos(theta), 0.0),
            vec4(       0.0, 0.0,         0.0, 1.0)
        );
        /*
        float psi = 0.15*(time+(gl_FragCoord.y*rubberfactor));   
        mat4 zrot = mat4(
            vec4( cos (psi), sin (psi), 0.0, 0.0),
            vec4(-sin (psi), cos (psi), 0.0, 0.0),
            vec4(       0.0,       0.0, 1.0, 0.0),
            vec4(       0.0,       0.0, 0.0, 1.0)        
        );  
        */
        gl_Position = gl_ModelViewProjectionMatrix*yrot*raw_vertex;
    } else {
        gl_Position = gl_ModelViewProjectionMatrix*raw_vertex;
    }
    
    depth = gl_Position.z;
    
    gl_PointSize = max(30.0-(gl_Position.z*0.125),4.0);    
}