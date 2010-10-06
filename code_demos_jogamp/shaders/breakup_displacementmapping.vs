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
 ** Basic displacement mapping vertex shader. Uses a gray 2D texture (uv-unwrapped) as displacement
 ** offset for the vertices of the given geometry. Uses some kind of random/jittering scheme to get
 ** very rough displacement values (looks better in combination with the erosion postprocessing filter).
 ** The scale value is used to attenuate the displacement map in sync with the spectrum analyzer.
 **/

uniform sampler2D sampler0_displace;
uniform sampler2D sampler1_diffuse;
uniform float scale;
uniform vec2 tc_offset[9];

void main(void) {
    gl_TexCoord[0].xy = gl_MultiTexCoord0.xy;
    vec4 dv = texture2D(sampler0_displace, gl_MultiTexCoord0.xy);
    vec4 cdv1 = texture2D(sampler0_displace, gl_MultiTexCoord0.xy + tc_offset[0]);
    vec4 cdv2 = texture2D(sampler0_displace, gl_MultiTexCoord0.xy + tc_offset[3]);
    vec4 cdv3 = texture2D(sampler0_displace, gl_MultiTexCoord0.xy + tc_offset[8]);
    //to change effect shift weight between constant displacement and offset texture coordinates ...
    //df = (0.30*(dv.x/255.0) + 0.59*(dv.y/255.0) + 0.11*(dv.z/255.0))*0.75 + (0.33*cdv1.x/255.0 + 0.33*cdv1.x/255.0 + 0.33*cdv1.z/255.0)*0.25; //NAH!
    //df = (0.30*(dv.x/255.0) + 0.59*(dv.y/255.0) + 0.11*(dv.z/255.0))*0.25 + (0.33*cdv1.x/255.0 + 0.33*cdv2.y/255.0 + 0.33*cdv3.z/255.0)*0.75; //OK!
    float df = (0.30*(dv.x/255.0) + 0.59*(dv.y/255.0) + 0.11*(dv.z/255.0))*0.15 + (0.33*cdv1.x/255.0 + 0.33*cdv2.y/255.0 + 0.33*cdv3.z/255.0)*0.85; //BETTER
    vec4 newVertexPos = vec4(gl_Normal * df * scale, 0.0) + gl_Vertex;
    gl_Position = gl_ModelViewProjectionMatrix * newVertexPos;
}
