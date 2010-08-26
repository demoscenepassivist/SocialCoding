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
 ** Vertex shader part of the parallax occlusion mapping program. Does the basic projection and 
 ** modelview matrix management, generates tangent and bitangent on-the-fly from the normal and
 ** calculates a varying light+eye vector interpolation to be used by the corresponding fragment
 ** shader.
 **
 **/

varying vec3 eyevector;
varying vec3 lightvector;
uniform vec4 lightPos;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
    //--- calculate the tangent/bitangent on-the-fly
    vec3 tangent;
    vec3 bitangent;
    vec3 c1 = cross(gl_Normal, vec3(0.0, 0.0, 1.0)); 
    vec3 c2 = cross(gl_Normal, vec3(0.0, 1.0, 0.0)); 
    if(length(c1)>length(c2)) {
        tangent = c1;	
    } else {
        tangent = c2;	
    }
    tangent = normalize(tangent);
    bitangent = cross(gl_Normal, tangent); 
    bitangent = normalize(bitangent);
    //---
    vec3 n = normalize(gl_NormalMatrix * gl_Normal);
    vec3 t = normalize(gl_NormalMatrix * tangent);
    vec3 b = cross(n, t);	
    vec3 vVertex = vec3(gl_ModelViewMatrix * gl_Vertex);
    vec3 tmpVec = lightPos.xyz - vVertex;
    lightvector.x = dot(tmpVec, t);
    lightvector.y = dot(tmpVec, b);
    lightvector.z = dot(tmpVec, n);
    tmpVec = -vVertex;
    eyevector.x = dot(tmpVec, t);
    eyevector.y = dot(tmpVec, b);
    eyevector.z = dot(tmpVec, n);
}