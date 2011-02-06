#version 150 compatibility

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
 ** Fragment shader for basic volume raycasting routine, rendering a 3D volume texture inside a 
 ** unit cube. Does the real raycasting iteration process using the precalculated (backface) 
 ** 'ending' positions from the positions texture. Iteration breaks when either the ray reaches
 ** the unitcube borders, a maximum number of iterations is reached or when the pixel opacity 
 ** reaches a given threshold.
 **
 **/

uniform vec2 resolution;
uniform sampler2D positiontexture;
uniform sampler3D volumetexture;
uniform float stepsize;
uniform float alphafactor;
uniform float iterations;
uniform float brightness;
uniform float skipfactor;
in vec4 position;

void main() {
    vec2 planecoords = gl_FragCoord.xy/resolution.xy;
    vec3 direction = normalize(texture(positiontexture, planecoords).xyz-position.xyz);
    vec3 loopposition = position.xyz;
    vec4 renderedpixel = vec4(0.0,0.0,0.0,0.0);
    vec4 volumepixel = vec4(0.0,0.0,0.0,0.0);
    for (float i = 0; i < iterations; ++i) {
        volumepixel = vec4(texture(volumetexture, loopposition).r);
        volumepixel.a *= alphafactor;
        volumepixel.rgb *= volumepixel.a;
        renderedpixel += (1.0-renderedpixel.a)*volumepixel;
        if (renderedpixel.a >= skipfactor) {
            break;
        }
        loopposition += direction*stepsize;
        if (loopposition.x>1.0 || loopposition.y>1.0 || loopposition.z>1.0 || 
            loopposition.x<0.0 || loopposition.y<0.0 || loopposition.z<0.0) {
            break;
        }
    }
    gl_FragColor = brightness*renderedpixel;
}
