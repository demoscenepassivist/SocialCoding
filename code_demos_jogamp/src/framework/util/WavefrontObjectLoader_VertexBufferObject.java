package framework.util;

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
 ** Wavefront .obj mesh loader with vertices, face and normal support. Other than the retention mode
 ** wavefront object loader with display-list support, this class supports model loading/drawing using
 ** vertex buffer objects. The code is slightly modified copypasta from the open source project "jglmark"
 ** (https://jglmark.dev.java.net/). Original author is Chris "Crash0veride007" Brown (crash0veride007@gmail.com).
 ** Also added support for compressed mesh files (.zip).
 **
 **/

import framework.base.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.zip.*;
import javax.media.opengl.*;
import com.jogamp.opengl.util.*;
import static javax.media.opengl.GL2.*;

public class WavefrontObjectLoader_VertexBufferObject {

    private String OBJModelPath;                                    //the path to the model file
    private ArrayList<float[]> vData = new ArrayList<float[]>();    //list of vertex coordinates
    private ArrayList<float[]> vtData = new ArrayList<float[]>();   //list of texture coordinates
    private ArrayList<float[]> vnData = new ArrayList<float[]>();   //list of normal coordinates
    private ArrayList<int[]> fv = new ArrayList<int[]>();           //face vertex indices
    private ArrayList<int[]> ft = new ArrayList<int[]>();           //face texture indices
    private ArrayList<int[]> fn = new ArrayList<int[]>();           //face normal indices
    private FloatBuffer vbuff;                                      //buffer for vertex data
    private FloatBuffer tbuff;                                      //buffer for hold texture data
    private FloatBuffer nbuff;                                      //buffer for hold normal data
    private int FaceFormat;                                         //format of the faces triangles or quads
    private int FaceMultiplier;                                     //number of possible coordinates per face
    private int PolyCount = 0;                                      //the models polygon count
    private int vSize = 0, tSize = 0, nSize = 0;                    //lengths of buffered vertex, texcoord, and normal data
    private int vc = 0, tc = 0, nc = 0;                             //numbers of coords per vertex, texcoord, or normal
    private boolean ve = false, te = false, ne = false;             //flags to check if certain data type should be enabled for rendering
    private boolean init  = true;                                   //flag to initialize upon an intial draw and then never again after 
    private int[] VBOid = new int[3];                               //allocate space for up to 3 VBO identifiers

    public WavefrontObjectLoader_VertexBufferObject(String inModelPath) {
        BaseLogging.getInstance().info("LOADING WAVEFRONT OBJECT MODEL AS VERTEX BUFFER OBJECT "+inModelPath);
        OBJModelPath = inModelPath;
        LoadOBJModel(OBJModelPath);
        SetFaceRenderType();
        BaseLogging.getInstance().info("POLYGON COUNT FOR MODEL="+PolyCount);
        BaseLogging.getInstance().info("VERTEX COUNT FOR MODEL="+vData.size());
        BaseLogging.getInstance().info("TEXTURE COORDINATE COUNT FOR MODEL="+vtData.size());
        BaseLogging.getInstance().info("NORMAL COUNT FOR MODEL="+vnData.size());
    }

    private void CheckExtensions(GL gl) {
        if (!gl.isExtensionAvailable("GL_ARB_vertex_buffer_object")) {
            BaseLogging.getInstance().error("YOUR GRPAHICS CARD DOES NOT SUPPORT THE ARB EXTENSION GL_ARB_vertex_buffer_object ...");
        }
    }

    private void LoadOBJModel(String inModelPath) {
        try {
            BufferedReader br = null;
            if (inModelPath.endsWith(".zip")) {
                BaseLogging.getInstance().info("WAVEFRONT MESH IS COMPRESSED! TRY TO EXTRACT FIRST/SINGLE ENTRY!");
                ZipInputStream tZipInputStream = new ZipInputStream(new BufferedInputStream((new Object()).getClass().getResourceAsStream(inModelPath)));
                ZipEntry tZipEntry;
                tZipEntry = tZipInputStream.getNextEntry();
                String inZipEntryName = tZipEntry.getName();
                if (inZipEntryName==null) {
                    BaseLogging.getInstance().fatalerror("ERROR! ZIP ENTRY IS NULL!");
                }
                BaseLogging.getInstance().info("EXTRACTING: "+inZipEntryName);
                if (!tZipEntry.isDirectory()) {
                    br = new BufferedReader(new InputStreamReader(tZipInputStream));
                } else {
                    BaseLogging.getInstance().fatalerror("ERROR! ZIP ENTRY IS DIRECTORY! SHOULD BE PLAIN FILE!");
                }
            } else {
                br = new BufferedReader(new InputStreamReader((new Object()).getClass().getResourceAsStream(inModelPath)));
            }
            String  line = null;
            while((line = br.readLine()) != null) {
                if (line.startsWith("#")) { 
                    //read any descriptor data in the file ...
                    //Zzzz ...
                } else if (line.equals("")) {
                    //ignore whitespace data ...
                } else if (line.startsWith("v ")) { //read in vertex data
                    vData.add(ProcessData(line));
                } else if (line.startsWith("vt ")) { //read texture coordinates
                    vtData.add(ProcessData(line));
                } else if (line.startsWith("vn ")) { //read normal coordinates
                    vnData.add(ProcessData(line));
                } else if (line.startsWith("f ")) { //read face data
                    ProcessfData(line);
                }
            }
            br.close();
            BaseLogging.getInstance().info("MODEL "+inModelPath+" SUCCESSFULLY LOADED!");
        } catch (IOException e) {
            BaseLogging.getInstance().fatalerror(e);
        }
    }

    private float[] ProcessData(String read) { //processes the incoming data and return it back 
        final String s[] = read.split("\\s+");
        return (ProcessFloatData(s)); 
    }

    private float[] ProcessFloatData(String sdata[]) { //returns an array of processed float data
        float data[] = new float[sdata.length-1];
        for (int loop=0; loop < data.length; loop++) {
            data[loop] = Float.parseFloat(sdata[loop+1]);
        }
        return data; //return an array of floats
    }

    private void ProcessfData(String fread) {//processes the incoming face data
        PolyCount++;
        String s[] = fread.split("\\s+");
        if (fread.contains("//")) { //pattern is present if obj has only v and vn in face data
            for (int loop=1; loop < s.length; loop++) {
                s[loop] = s[loop].replaceAll("//","/0/"); //insert a zero for missing vt data
            }
        }
        ProcessfIntData(s); //pass in face data
    }

    private void ProcessfIntData(String sdata[]) {
        int vd[] = new int[sdata.length-1];
        int vtd[] = new int[sdata.length-1];
        int vnd[] = new int[sdata.length-1];
        for (int loop = 1; loop < sdata.length; loop++) {
            String s = sdata[loop]; //hack off the f leaving just the face indices
            String[] temp = s.split("/"); //split up the parms now....
            vd[loop-1] = Integer.valueOf(temp[0]); //we will always have vertex indices 
            if (temp.length > 1) { //we have v and vt data
                vtd[loop-1] = Integer.valueOf(temp[1]); //add in vt indices
            } else { 
                vtd[loop-1] = 0; //if no vt data is present fill in zeros
            }
            if (temp.length > 2) { //we have v, vt, and vn data
                vnd[loop-1] = Integer.valueOf(temp[2]); //add in vn indices
            } else {
                vnd[loop-1] = 0; //if no vn data is present fill in zeros
            }
        }
        fv.add(vd);
        ft.add(vtd);
        fn.add(vnd);
    }

    private void SetFaceRenderType() {
        final int temp [] = (int[]) fv.get(0);
        if ( temp.length == 3) {
            FaceFormat = GL_TRIANGLES; //the faces come in sets of 3 so we have triangular faces
            FaceMultiplier = 3;
        } else if (temp.length == 4) {
            FaceFormat = GL_QUADS; //the faces come in sets of 4 so we have quadrilateral faces
            FaceMultiplier = 4;
        } else {
            FaceFormat = GL_POLYGON; //fall back to render as free form polygons
        }
    }

    private void BuildVBOModel(GL2 inGL) {
        final int tv[] = (int[]) fv.get(0);
        final int tt[] = (int[]) ft.get(0);
        final int tn[] = (int[]) fn.get(0);
        float vlen[] = null; //array to later hold length of vertice sets
        float tlen[] = null; //array to later hold length of texture coordinate sets
        float nlen[] = null; //array to later hold length of normal sets
        vlen = (float[]) vData.get(0);
        vc = vlen.length;
        if (vtData.size() != 0)
            tlen = (float[]) vtData.get(0);
        if (vnData.size() != 0)
            nlen = (float[]) vnData.get(0);
        if (tlen != null)
            tc = tlen.length;
        if (nlen != null)
            nc = nlen.length;
        inGL.glGenBuffers(3,VBOid,0); //generate 3 VBO identifiers 
        //if a value of zero is found that it tells us we don't have that type of data
        if ((tv[0] != 0) && (tt[0] != 0) && (tn[0] != 0)) {
            ConstructVTN(inGL, vc, tc, nc); //we have vertex, 2D texture, and normal Data
            ve = true; 
            te = true;
            ne = true;
        } else if ((tv[0] != 0) && (tt[0] != 0) && (tn[0] == 0)) {
            ConstructVT(inGL, vc, tc); //we have just vertex and texture data
            ve = true; 
            te = true;
        } else if ((tv[0] != 0) && (tt[0] == 0) && (tn[0] != 0)) {
            ConstructVN(inGL, vc, nc); //we have just vertex and normal Data
            ve = true; 
            ne = true;
        } else if ((tv[0] != 0) && (tt[0] == 0) && (tn[0] == 0)) {
            ConstructV(inGL, vc);
            ve = true; 
        }
    }

    private void ConstructVTN(GL gl, int vlen, int tlen, int nlen) {
        int[] v, t, n;
        int bufflength =0;
        vSize = PolyCount*(FaceMultiplier*vlen);
        tSize = PolyCount*(FaceMultiplier*tlen);
        nSize = PolyCount*(FaceMultiplier*nlen);
        vbuff = GLBuffers.newDirectFloatBuffer(vSize);
        tbuff = GLBuffers.newDirectFloatBuffer(tSize);
        nbuff = GLBuffers.newDirectFloatBuffer(nSize);
        for (int oloop=0; oloop < fv.size(); oloop++) {
            v = (int[])(fv.get(oloop));
            t = (int[])(ft.get(oloop));
            n = (int[])(fn.get(oloop));
            for (int iloop=0; iloop < v.length; iloop++) {
                for (int vloop=0; vloop < vlen; vloop++) { //fill in the vertex coordinate data
                    vbuff.put(((float[])vData.get(v[iloop] - 1))[vloop]);
                }
                for (int vtloop=0; vtloop < tlen; vtloop++) { //fill in the texture coordinate data
                    tbuff.put(((float[])vtData.get(t[iloop] - 1))[vtloop]);
                }
                for (int vnloop=0; vnloop < nlen; vnloop++) { //fill in the normal coordinate data
                    nbuff.put(((float[])vnData.get(n[iloop] - 1))[vnloop]);
                }
            }
        }
        vbuff.rewind();
        bufflength = vSize*GLBuffers.SIZEOF_FLOAT;
        BufferData(gl, VBOid[0], bufflength, vbuff);
        tbuff.rewind();
        bufflength = tSize*GLBuffers.SIZEOF_FLOAT;
        BufferData(gl, VBOid[1], bufflength, tbuff);
        nbuff.rewind();
        bufflength = nSize*GLBuffers.SIZEOF_FLOAT;
        BufferData(gl, VBOid[2], bufflength, nbuff);
    }

    private void ConstructVT(GL gl, int vlen, int tlen) {
        int[] v, t;
        int bufflength =0;
        vSize = PolyCount*(FaceMultiplier*vlen);
        tSize = PolyCount*(FaceMultiplier*tlen);
        vbuff = GLBuffers.newDirectFloatBuffer(vSize);
        tbuff = GLBuffers.newDirectFloatBuffer(tSize);
        for (int oloop=0; oloop < fv.size(); oloop++) {
            v = (int[])(fv.get(oloop));
            t = (int[])(ft.get(oloop));
            for (int iloop=0; iloop < v.length; iloop++) { 
                for (int vloop=0; vloop < vlen; vloop++) { //fill in the vertex coordinate data
                    vbuff.put(((float[])vData.get(v[iloop] - 1))[vloop]);
                }
                for (int vtloop=0; vtloop < tlen; vtloop++) { //fill in the texture coordinate data
                    tbuff.put(((float[])vtData.get(t[iloop] - 1))[vtloop]);
                }
            }
        }
        vbuff.rewind();
        bufflength = vSize*GLBuffers.SIZEOF_FLOAT;
        BufferData(gl, VBOid[0], bufflength, vbuff);
        tbuff.rewind();
        bufflength = tSize*GLBuffers.SIZEOF_FLOAT;
        BufferData(gl, VBOid[1], bufflength, tbuff);
    }

    private void ConstructVN(GL gl, int vlen, int nlen) {
        int[] v, n;
        int bufflength =0;
        vSize = PolyCount*(FaceMultiplier*vlen);
        nSize = PolyCount*(FaceMultiplier*nlen);
        vbuff = GLBuffers.newDirectFloatBuffer(vSize);
        nbuff = GLBuffers.newDirectFloatBuffer(nSize);
        for (int oloop=0; oloop < fv.size(); oloop++) {
            v = (int[])(fv.get(oloop));
            n = (int[])(fn.get(oloop));
            for (int iloop=0; iloop < v.length; iloop++) {
                for (int vloop=0; vloop < vlen; vloop++) { //fill in the vertex coordinate data
                    vbuff.put(((float[])vData.get(v[iloop] - 1))[vloop]);
                }
                for (int vnloop=0; vnloop < nlen; vnloop++) { //fill in the normal coordinate data
                    nbuff.put(((float[])vnData.get(n[iloop] - 1))[vnloop]);
                }
            }
        }
        vbuff.rewind();
        bufflength = vSize*GLBuffers.SIZEOF_FLOAT;
        BufferData(gl, VBOid[0], bufflength, vbuff);
        nbuff.rewind();
        bufflength = nSize*GLBuffers.SIZEOF_FLOAT;
        BufferData(gl, VBOid[2], bufflength, nbuff);
    }

    private void ConstructV(GL gl, int vlen) {
        int[] v;
        int bufflength =0;
        vSize = PolyCount*(FaceMultiplier*vlen); 
        vbuff = GLBuffers.newDirectFloatBuffer(vSize);
        for (int oloop=0; oloop < fv.size(); oloop++) {
            v = (int[])(fv.get(oloop)); 
            for (int iloop=0; iloop < v.length; iloop++) {
                for (int vloop=0; vloop < vlen; vloop++) { //fill in the vertex coordinate data
                    vbuff.put(((float[])vData.get(v[iloop] - 1))[vloop]);
                }
            }
        }
        vbuff.rewind(); 
        bufflength = vSize*GLBuffers.SIZEOF_FLOAT;
        BufferData(gl, VBOid[0], bufflength, vbuff); 
    }

    private void BufferData(GL gl, int id, int length, FloatBuffer buffer) { //function to simplfy buffering the VBO data
        gl.glBindBuffer(GL_ARRAY_BUFFER, id);
        gl.glBufferData(GL_ARRAY_BUFFER, length, buffer, GL_STATIC_DRAW);
    }  

    public void DrawModel(GL2 inGL) {
        if (init) {
            CheckExtensions(inGL);
            BuildVBOModel(inGL);
            cleanup(); 
            init = false;
        }
        inGL.glEnable(GL.GL_CULL_FACE);
        inGL.glCullFace(GL.GL_BACK);
        if (ve && te && ne) {
            inGL.glEnableClientState(GL_VERTEX_ARRAY);
            inGL.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
            inGL.glEnableClientState(GL_NORMAL_ARRAY);
            inGL.glBindBuffer(GL_ARRAY_BUFFER, VBOid[0]);
            inGL.glVertexPointer(vc, GL.GL_FLOAT, 0, 0);
            inGL.glBindBuffer(GL_ARRAY_BUFFER, VBOid[1]);
            inGL.glTexCoordPointer(tc, GL.GL_FLOAT, 0, 0);
            inGL.glBindBuffer(GL.GL_ARRAY_BUFFER, VBOid[2]);
            inGL.glNormalPointer(GL.GL_FLOAT, 0, 0);
        }
        else if (ve && te) {
            inGL.glEnableClientState(GL_VERTEX_ARRAY);
            inGL.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
            inGL.glBindBuffer(GL_ARRAY_BUFFER, VBOid[0]);
            inGL.glVertexPointer(vc, GL.GL_FLOAT, 0, 0);
            inGL.glBindBuffer(GL_ARRAY_BUFFER, VBOid[1]);
            inGL.glTexCoordPointer(tc, GL.GL_FLOAT, 0, 0);
        }
        else if (ve && ne) {
            inGL.glEnableClientState(GL_VERTEX_ARRAY);
            inGL.glEnableClientState(GL_NORMAL_ARRAY);
            inGL.glBindBuffer(GL_ARRAY_BUFFER, VBOid[0]);
            inGL.glVertexPointer(vc, GL.GL_FLOAT, 0, 0);
            inGL.glBindBuffer(GL_ARRAY_BUFFER, VBOid[2]);
            inGL.glNormalPointer(GL.GL_FLOAT, 0, 0);
        }
        else if (ve) {
            inGL.glEnableClientState(GL_VERTEX_ARRAY);
            inGL.glBindBuffer(GL_ARRAY_BUFFER, VBOid[0]);
            inGL.glVertexPointer(vc, GL.GL_FLOAT, 0, 0);
        }
        inGL.glDrawArrays(FaceFormat, 0, vSize/vc);
        if (ve && te && ne) {
            inGL.glDisableClientState(GL_VERTEX_ARRAY);
            inGL.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
            inGL.glDisableClientState(GL_NORMAL_ARRAY);
        }
        else if (ve && te) {
            inGL.glDisableClientState(GL_VERTEX_ARRAY);
            inGL.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        }
        else if (ve && ne) {
            inGL.glDisableClientState(GL_VERTEX_ARRAY);
            inGL.glDisableClientState(GL_NORMAL_ARRAY);
        }
        else if (ve) {
            inGL.glDisableClientState(GL_VERTEX_ARRAY);
        }
        inGL.glDisable(GL.GL_CULL_FACE);
    }

    private void cleanup() {
        vData.clear(); 
        vtData.clear(); 
        vnData.clear();
        fv.clear(); 
        ft.clear(); 
        fn.clear();
        vbuff.clear();
        if (tbuff != null)
        tbuff.clear();
        if (nbuff != null)
        nbuff.clear();
        System.gc();
    }

}
