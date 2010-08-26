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
 ** Wavefront .obj mesh loader with vertices, face and normal support. Provides a convenience
 ** method to load the whole model as display-list. The code is slightly modified copypasta from
 ** the open source project "jglmark" (https://jglmark.dev.java.net/). Original author is Chris 
 ** "Crash0veride007" Brown (crash0veride007@gmail.com). Also added support for compressed mesh
 ** files (.zip).
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

public class WavefrontObjectLoader {

    private String OBJModelPath;                                    //the path to the model file
    private ArrayList<float[]> vData = new ArrayList<float[]>();    //list of vertex coordinates
    private ArrayList<float[]> vtData = new ArrayList<float[]>();   //list of texture coordinates
    private ArrayList<float[]> vnData = new ArrayList<float[]>();   //list of normal coordinates
    private ArrayList<int[]> fv = new ArrayList<int[]>();           //face vertex indices
    private ArrayList<int[]> ft = new ArrayList<int[]>();           //face texture indices
    private ArrayList<int[]> fn = new ArrayList<int[]>();           //face normal indices
    private FloatBuffer modeldata;                                  //buffer which will contain vertice data
    private int FaceFormat;                                         //format of the faces triangles or quads
    private int FaceMultiplier;                                     //number of possible coordinates per face
    private int PolyCount = 0;                                      //the model polygon count
    private boolean init  = true;

    public WavefrontObjectLoader(String inModelPath) {
        BaseLogging.getInstance().info("LOADING WAVEFRONT OBJECT MODEL "+inModelPath);
        OBJModelPath = inModelPath;
        LoadOBJModel(OBJModelPath);
        SetFaceRenderType();
        BaseLogging.getInstance().info("POLYGON COUNT FOR MODEL="+PolyCount);
        BaseLogging.getInstance().info("VERTEX COUNT FOR MODEL="+vData.size());
        BaseLogging.getInstance().info("TEXTURE COORDINATE COUNT FOR MODEL="+vtData.size());
        BaseLogging.getInstance().info("NORMAL COUNT FOR MODEL="+vnData.size());
    }

    private void LoadOBJModel(String ModelPath) {
        try {
            BufferedReader br = null;
            if (ModelPath.endsWith(".zip")) {
                BaseLogging.getInstance().info("WAVEFRONT MESH IS COMPRESSED! TRY TO EXTRACT FIRST/SINGLE ENTRY!");
                ZipInputStream tZipInputStream = new ZipInputStream(new BufferedInputStream((new Object()).getClass().getResourceAsStream(ModelPath)));
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
                br = new BufferedReader(new InputStreamReader((new Object()).getClass().getResourceAsStream(ModelPath)));
            }
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {         //read any descriptor data in the file
                    // Zzzz ...
                } else if (line.equals("")) {
                    // Ignore whitespace data
                } else if (line.startsWith("v ")) {  //read in vertex data
                    vData.add(ProcessData(line));
                } else if (line.startsWith("vt ")) { //read texture coordinates
                    vtData.add(ProcessData(line));
                } else if (line.startsWith("vn ")) { //read normal coordinates
                    vnData.add(ProcessData(line));
                } else if (line.startsWith("f ")) {  //read face data
                    ProcessfData(line);
                }
            }
            br.close();
            BaseLogging.getInstance().info("MODEL "+ModelPath+" SUCCESSFULLY LOADED!");
        } catch (IOException e) {
            BaseLogging.getInstance().fatalerror(e);
        }
    }

    private float[] ProcessData(String read) {
        final String s[] = read.split("\\s+");
        return (ProcessFloatData(s)); //returns an array of processed float data
    }

    private float[] ProcessFloatData(String sdata[]) {
        float data[] = new float[sdata.length - 1];
        for (int loop = 0; loop < data.length; loop++) {
            data[loop] = Float.parseFloat(sdata[loop + 1]);
        }
        return data; //return an array of floats
    }

    private void ProcessfData(String fread) {
        PolyCount++;
        String s[] = fread.split("\\s+");
        if (fread.contains("//")) { //pattern is present if obj has only v and vn in face data
            for (int loop = 1; loop < s.length; loop++) {
                s[loop] = s[loop].replaceAll("//", "/0/"); //insert a zero for missing vt data
            }
        }
        ProcessfIntData(s); //pass in face data
    }

    private void ProcessfIntData(String sdata[]) {
        int vdata[] = new int[sdata.length - 1];
        int vtdata[] = new int[sdata.length - 1];
        int vndata[] = new int[sdata.length - 1];
        for (int loop = 1; loop < sdata.length; loop++) {
            String s = sdata[loop];
            String[] temp = s.split("/");
            vdata[loop - 1] = Integer.valueOf(temp[0]);         //always add vertex indices
            if (temp.length > 1) {                              //we have v and vt data
                vtdata[loop - 1] = Integer.valueOf(temp[1]);    //add in vt indices
            } else {
                vtdata[loop - 1] = 0;                           //if no vt data is present fill in zeros
            }
            if (temp.length > 2) {                              //we have v, vt, and vn data
                vndata[loop - 1] = Integer.valueOf(temp[2]);    //add in vn indices
            } else {
                vndata[loop - 1] = 0;                           //if no vn data is present fill in zeros
            }
        }
        fv.add(vdata);
        ft.add(vtdata);
        fn.add(vndata);
    }

    private void SetFaceRenderType() {
        final int temp[] = (int[]) fv.get(0);
        if (temp.length == 3) {
            FaceFormat = GL_TRIANGLES; 	//the faces come in sets of 3 so we have triangular faces
            FaceMultiplier = 3;
        } else if (temp.length == 4) {
            FaceFormat = GL_QUADS; 		//the faces come in sets of 4 so we have quadrilateral faces
            FaceMultiplier = 4;
        } else {
            FaceFormat = GL_POLYGON; 	//fall back to render as free form polygons
        }
    }

    private void ConstructInterleavedArray(GL2 inGL) {
        final int tv[] = (int[]) fv.get(0);
        final int tt[] = (int[]) ft.get(0);
        final int tn[] = (int[]) fn.get(0);
        //if a value of zero is found that it tells us we don't have that type of data
        if ((tv[0] != 0) && (tt[0] != 0) && (tn[0] != 0)) {
            ConstructTNV(); //we have vertex, 2D texture, and normal Data
            inGL.glInterleavedArrays(GL_T2F_N3F_V3F, 0, modeldata);
        } else if ((tv[0] != 0) && (tt[0] != 0) && (tn[0] == 0)) {
            ConstructTV(); //we have just vertex and 2D texture Data
            inGL.glInterleavedArrays(GL_T2F_V3F, 0, modeldata);
        } else if ((tv[0] != 0) && (tt[0] == 0) && (tn[0] != 0)) {
            ConstructNV(); //we have just vertex and normal Data
            inGL.glInterleavedArrays(GL_N3F_V3F, 0, modeldata);
        } else if ((tv[0] != 0) && (tt[0] == 0) && (tn[0] == 0)) {
            ConstructV();
            inGL.glInterleavedArrays(GL_V3F, 0, modeldata);
        }
    }

    private void ConstructTNV() {
        int[] v, t, n;
        float tcoords[] = new float[2]; //only T2F is supported in interLeavedArrays!!
        float coords[] = new float[3];
        int fbSize = PolyCount * (FaceMultiplier * 8); //3v per poly, 2vt per poly, 3vn per poly
        modeldata = GLBuffers.newDirectFloatBuffer(fbSize);
        modeldata.position(0);
        for (int oloop = 0; oloop < fv.size(); oloop++) {
            v = (int[]) (fv.get(oloop));
            t = (int[]) (ft.get(oloop));
            n = (int[]) (fn.get(oloop));
            for (int iloop = 0; iloop < v.length; iloop++) {
                //fill in the texture coordinate data
                for (int tloop = 0; tloop < tcoords.length; tloop++)
                    //only T2F is supported in interleavedarrays!!
                    tcoords[tloop] = ((float[]) vtData.get(t[iloop] - 1))[tloop];
                modeldata.put(tcoords);
                //fill in the normal coordinate data
                for (int vnloop = 0; vnloop < coords.length; vnloop++)
                    coords[vnloop] = ((float[]) vnData.get(n[iloop] - 1))[vnloop];
                modeldata.put(coords);
                //fill in the vertex coordinate data
                for (int vloop = 0; vloop < coords.length; vloop++)
                    coords[vloop] = ((float[]) vData.get(v[iloop] - 1))[vloop];
                modeldata.put(coords);
            }
        }
        modeldata.position(0);
    }

    private void ConstructTV() {
        int[] v, t;
        float tcoords[] = new float[2]; //only T2F is supported in interLeavedArrays!!
        float coords[] = new float[3];
        int fbSize = PolyCount * (FaceMultiplier * 5); //3v per poly, 2vt per poly
        modeldata = GLBuffers.newDirectFloatBuffer(fbSize);
        modeldata.position(0);
        for (int oloop = 0; oloop < fv.size(); oloop++) {
            v = (int[]) (fv.get(oloop));
            t = (int[]) (ft.get(oloop));
            for (int iloop = 0; iloop < v.length; iloop++) {
                //fill in the texture coordinate data
                for (int tloop = 0; tloop < tcoords.length; tloop++)
                    //only T2F is supported in interleavedarrays!!
                    tcoords[tloop] = ((float[]) vtData.get(t[iloop] - 1))[tloop];
                modeldata.put(tcoords);
                //fill in the vertex coordinate data
                for (int vloop = 0; vloop < coords.length; vloop++)
                    coords[vloop] = ((float[]) vData.get(v[iloop] - 1))[vloop];
                modeldata.put(coords);
            }
        }
        modeldata.position(0);
    }

    private void ConstructNV() {
        int[] v, n;
        float coords[] = new float[3];
        int fbSize = PolyCount * (FaceMultiplier * 6); //3v per poly, 3vn per poly
        modeldata = GLBuffers.newDirectFloatBuffer(fbSize);
        modeldata.position(0);
        for (int oloop = 0; oloop < fv.size(); oloop++) {
            v = (int[]) (fv.get(oloop));
            n = (int[]) (fn.get(oloop));
            for (int iloop = 0; iloop < v.length; iloop++) {
                //fill in the normal coordinate data
                for (int vnloop = 0; vnloop < coords.length; vnloop++)
                    coords[vnloop] = ((float[]) vnData.get(n[iloop] - 1))[vnloop];
                modeldata.put(coords);
                //fill in the vertex coordinate data
                for (int vloop = 0; vloop < coords.length; vloop++)
                    coords[vloop] = ((float[]) vData.get(v[iloop] - 1))[vloop];
                modeldata.put(coords);
            }
        }
        modeldata.position(0);
    }

    private void ConstructV() {
        int[] v;
        float coords[] = new float[3];
        int fbSize = PolyCount * (FaceMultiplier * 3); //3v per poly
        modeldata = GLBuffers.newDirectFloatBuffer(fbSize);
        modeldata.position(0);
        for (int oloop = 0; oloop < fv.size(); oloop++) {
            v = (int[]) (fv.get(oloop));
            for (int iloop = 0; iloop < v.length; iloop++) {
                //fill in the vertex coordinate data
                for (int vloop = 0; vloop < coords.length; vloop++)
                    coords[vloop] = ((float[]) vData.get(v[iloop] - 1))[vloop];
                modeldata.put(coords);
            }
        }
        modeldata.position(0);
    }

    public void drawModel(GL2 inGL) {
        if (init) {
            ConstructInterleavedArray(inGL);
            cleanup();
            init = false;
        }
        inGL.glDrawArrays(FaceFormat, 0, PolyCount * FaceMultiplier);
    }

    private void cleanup() {
        vData.clear();
        vtData.clear();
        vnData.clear();
        fv.clear();
        ft.clear();
        fn.clear();
        modeldata.clear();
    }

    public static int loadWavefrontObjectAsDisplayList(GL2 inGL,String inFileName) {
        int tDisplayListID = inGL.glGenLists(1);
        WavefrontObjectLoader tWaveFrontObjectModel = new WavefrontObjectLoader(inFileName);
        inGL.glNewList(tDisplayListID,GL_COMPILE);
        tWaveFrontObjectModel.drawModel(inGL);
        inGL.glEndList();
        return tDisplayListID;
    }

}
