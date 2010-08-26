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
 ** Basic particle system vertex shader (stateless). Also calculates diffuse and specular lighting
 ** for the particles (using three colored lights with hardcoded specular exponent) used for point
 ** based rendering.
 **
 **/

uniform float time;
uniform vec3 lightPos[3];
uniform vec4 lightCol[3];

void main(void) {
    vec3 L[3], H[3];
//--- particle system begin ...
    float offsettime = time-(gl_Vertex.y+0.5);
    if (gl_Vertex.y+0.5<time) {
        vec3 pos;
        //spread particles ...
        pos.x = (1.3 * cos(262.0 * gl_Vertex.z))*sin(offsettime);
        pos.z = (2.0 * sin(163.0 * gl_Vertex.z))*sin(offsettime*2.0);
        pos.y = -2.5 * sin(offsettime*2.5) * offsettime;
        //---
        gl_Vertex.xyz += pos.xyz;
        gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    } else {
        gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    }
//--- particle system end ...
    vec3 N = normalize(gl_NormalMatrix * gl_Normal);
    vec4 V = gl_ModelViewMatrix * gl_Vertex;
    gl_FrontColor = vec4(0.0);
    for (int i = 0; i < 3; i++) {
        //light vectors
        L[i] = normalize(lightPos[i] - V.xyz);
        //half-angles
        H[i] = normalize(L[i] + vec3(0.0, 0.0, 1.0));
        float NdotL = max(0.0, dot(N, L[i]));
        vec3 L = normalize(lightPos[i] - V.xyz);
        vec3 H = normalize(L + vec3(0.0, 0.0, 1.0));
        const float specularExp = 128.0;
        //calculate specular lighting
        float NdotH = max(0.0, dot(N, H));
        vec4 specular = vec4(0.0);
        if (NdotL > 0.0) {
            specular = vec4(pow(NdotH, specularExp));
        }
        //accumulate the diffuse contributions
        gl_FrontColor += (lightCol[i] * vec4(NdotL) + specular)*clamp(1.0-(offsettime*3.0),0.0,1.0);
    }
    gl_PointSize = 10.0;
}