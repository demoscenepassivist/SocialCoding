package framework.util;

import javax.media.opengl.*;
import framework.base.*;

public class GeometryUtils {

    public static int generateBufferID(GL2 inGL) {
        int[] result = new int[1];
        inGL.glGenBuffers(1, result, 0);
        BaseLogging.getInstance().info("ALLOCATED NEW JOGL BUFFER ID="+result[0]);
        return result[0];
    }
    
    public static int generateVertexArrayID(GL2 inGL) {
        int[] result = new int[1];
        inGL.glGenVertexArrays(1, result, 0);
        BaseLogging.getInstance().info("ALLOCATED NEW JOGL VERTEX ARRAY ID="+result[0]);
        return result[0];
    }
    
}
