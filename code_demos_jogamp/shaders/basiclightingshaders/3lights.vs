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
 ** Simple generic vertex shader using mvp transformation and diffuse and specular lighting 
 ** based on the primary color and three colored lights with hardcoded specular exponent.
 **
 **/

uniform vec3 lightPos[3];
uniform vec4 lightCol[3];

void main(void) {
    vec3 L[3], H[3];
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    vec3 N = normalize(gl_NormalMatrix * gl_Normal);
    vec4 V = gl_ModelViewMatrix * gl_Vertex;
    //calculate the material color using specular lighting
    gl_FrontColor = vec4(0.0);
    for (int i = 0; i < 3; i++) {
        L[i] = normalize(lightPos[i] - V.xyz);
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
        //accumulate the diffuse/specular contributions
        gl_FrontColor += gl_Color * lightCol[i] * vec4(NdotL) + specular;
    }
}
