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
 ** Fragment shader implementing fractal-bitmap-orbit-trapping as used in my JOGL2 port of 
 ** Elektronenmultiplizierer. Calculates different variations of bitmap orbit traps for the 
 ** julia- and mandelbrot-fractals.
 **
 ** Papers and articles you should be familiar with before trying to understand the code:
 ** Geometric orbit traps: http://www.iquilezles.org/www/articles/ftrapsgeometric/ftrapsgeometric.htm
 ** Bitmap orbit traps: http://www.iquilezles.org/www/articles/ftrapsbitmap/ftrapsbitmap.htm
 **
 **/

//#define supersamplingfactor 0.25
//#define singleiteration

uniform float time;
uniform vec2 resolution;
uniform sampler2D texture;
uniform bool juliamode;
uniform bool zoomin;
uniform int juliapower;
uniform float orbittrapscale;
uniform int iterationslimit;

#define complexArg(z) float(atan(z.y, z.x))
#define polar(r,a) vec2(cos(a) * r, sin(a) * r)
#define complexPower(z,p) vec2(polar(pow(length(z), float(p)), float(p) * complexArg(z)))
#define maxiterations 256

float zoomfactor;

vec4 calculateorbittrap(vec2 z) {
    vec2 juliaoffset = vec2(0.36,0.36); //from -2 up to 2
    vec4 color = vec4(0.0,0.0,0.0,1.0); //backgroundcolor
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
#ifdef singleiteration 
    vec4 orbitpixels[1];
    for (int i = 0; i < iterationslimit; i++) {
        z = complexPower(z, juliapower) + c;    
    }           
    //formula without orbittrapoffset:
    vec2 sp = 0.5 + (z / otscale * orbitrotation) * orbitspin;
    orbitpixels[0] = texture2D(texture, sp);
    return orbitpixels[0];
#else
    vec4 orbitpixels[maxiterations];
    for (int i = 0; i < iterationslimit; i++) {
        z = complexPower(z, juliapower) + c;    
        //vec2 orbittrapoffset = vec2(0.0,0.0);
        //vec2 sp = 0.5 + (z / otscale * orbitrotation - orbittrapoffset) * orbitspin;
        //formula without orbittrapoffset:
        vec2 sp = 0.5 + (z / otscale * orbitrotation) * orbitspin;
        orbitpixels[i] = texture2D(texture, sp);
    }
    for (int i =  maxiterations-iterationslimit; i <maxiterations; i++) {
        float iterationfade = 1.0-(i/(int(maxiterations)-int(zoomfactor/3500)));
        color = mix(color,
                    orbitpixels[(maxiterations-1)-i], 
                    orbitpixels[(maxiterations-1)-i].a*iterationfade);
    }    
    return color;
#endif
}

vec4 render(vec2 fragcoord) {
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
       return calculateorbittrap(vec2(xcenter,ycenter)+z*zoom);
    } else if (zoomin) {
       float zoom = .72+.38*sin(.1*time);
       zoom = pow(zoom,8.0);
       zoomfactor = 1.0/zoom; //~200000 at the end of single precision
       const float xcenter = 0.3245046418497685;
       const float ycenter = 0.04855101129280834;
       return calculateorbittrap(vec2(xcenter,ycenter)+z*zoom);
    } else {
       return calculateorbittrap(z);
    }
}

void main() {
    vec4 color = vec4(0.0);
    float n = 0.0;
#ifdef supersamplingfactor
    for (float x = 0.0; x < 1.0; x += float(supersamplingfactor)) {
        for (float y = 0.0; y < 1.0; y += float(supersamplingfactor)) {
            color += render(gl_FragCoord.xy + vec2(x, y));
            n += 1.0;
        }
    }
    color /= n;
#else
    color = render(gl_FragCoord.xy);
#endif
    gl_FragColor = color;
}
