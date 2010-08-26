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
 ** based on the primary color and one white light with hardcoded specular exponent.
 **
 **/

uniform vec3 lightPos[1];

void main(void) {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    //light calculation
    vec3 N = normalize(gl_NormalMatrix * gl_Normal);
    vec4 V = gl_ModelViewMatrix * gl_Vertex;
    vec3 L = normalize(lightPos[0] - V.xyz);
    vec3 H = normalize(L + vec3(0.0, 0.0, 1.0));
    const float specularExp = 128.0;
    //calculate diffuse lighting
    float NdotL = max(0.0, dot(N, L));
    vec4 diffuse = gl_Color * vec4(NdotL);
    //calculate specular lighting
    float NdotH = max(0.0, dot(N, H));
    vec4 specular = vec4(0.0);
    if (NdotL > 0.0) {
        specular = vec4(pow(NdotH, specularExp));
    }
    //sum the diffuse and specular components
    gl_FrontColor = diffuse + specular;
}
