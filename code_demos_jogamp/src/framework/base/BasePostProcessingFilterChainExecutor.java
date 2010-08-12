package framework.base;

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
 ** Utility class wich helps with setup and handling of BasePostProcessingFilterChainShaderInterface
 ** instances. Simply add the filter chain and call executeFilterChain(). The uniform, texture unit
 ** and framebuffer setup is handled automatically. Also provides simple iteration support to simplify
 ** convolution-filter handling. 
 **
 **/

import java.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.jogamp.opengl.util.gl2.*;
import framework.util.*;
import static javax.media.opengl.GL2.*;

public class BasePostProcessingFilterChainExecutor {
	
	private int mScreenWidth;
	private int mScreenHeight;
	private int mFullScreenBackBufferID;
	private int mBackBufferTextureID;
	private ArrayList<BasePostProcessingFilterChainShaderInterface> mFilterList;
	private int mGLBorderMode = GL_CLAMP;
	
	public BasePostProcessingFilterChainExecutor() {
		mScreenWidth = BaseGlobalEnvironment.getInstance().getScreenWidth();
		mScreenHeight = BaseGlobalEnvironment.getInstance().getScreenHeight();
		mFilterList = new ArrayList<BasePostProcessingFilterChainShaderInterface>();
	}
	
	public void init(GL2 inGL,GLU inGLU,GLUT inGLUT) {
		mFullScreenBackBufferID = TextureUtils.generateTextureID(inGL);
		mBackBufferTextureID = TextureUtils.generateTextureID(inGL);
	}
	
	public void addFilter(BasePostProcessingFilterChainShaderInterface inBasePostProcessingFilterChainShaderInterface) {
		mFilterList.add(inBasePostProcessingFilterChainShaderInterface);
	}
	
	public void removeFilter(BasePostProcessingFilterChainShaderInterface inBasePostProcessingFilterChainShaderInterface) {
		mFilterList.remove(inBasePostProcessingFilterChainShaderInterface);
	}
	
	public void removeAllFilters() {
		mFilterList.clear();
	}
	
	public void executeFilterChain(GL2 inGL,GLU inGLU,GLUT inGLUT) {
		BasePostProcessingFilterChainShaderInterface tBasePostProcessingFilterChainShaderInterface = mFilterList.get(0);
		prepareFilterChainShaderRendering(inGL,inGLU,inGLUT,tBasePostProcessingFilterChainShaderInterface);		
		renderFilterChainShader(inGL,inGLU,inGLUT,tBasePostProcessingFilterChainShaderInterface);	
		if (mFilterList.size()>1) {
			if (mFilterList.get(1).getScreenSizeDivisionFactor()!=tBasePostProcessingFilterChainShaderInterface.getScreenSizeDivisionFactor()) {
				//rollout texture to fullscreen size if division factor of filters varies ... really hurts performance! X-)
				copyScreenBufferTo_GL_TEXTURE_2D(inGL,mScreenWidth,mScreenHeight,tBasePostProcessingFilterChainShaderInterface.getScreenSizeDivisionFactor());
				renderFullScreenQuad(inGL);
				copyScreenBufferTo_GL_TEXTURE_2D(inGL,mScreenWidth,mScreenHeight,1);
			} else {
				copyScreenBufferTo_GL_TEXTURE_2D(inGL,mScreenWidth,mScreenHeight,tBasePostProcessingFilterChainShaderInterface.getScreenSizeDivisionFactor());
			}
		}
		for (int i=1; i<mFilterList.size(); i++) {
			tBasePostProcessingFilterChainShaderInterface = mFilterList.get(i);			
			renderQuad(inGL,tBasePostProcessingFilterChainShaderInterface.getScreenSizeDivisionFactor());
			renderFilterChainShader(inGL,inGLU,inGLUT,tBasePostProcessingFilterChainShaderInterface);
			if ((mFilterList.size()-1)>=i+1) {				
				if (mFilterList.get(i+1).getScreenSizeDivisionFactor()!=tBasePostProcessingFilterChainShaderInterface.getScreenSizeDivisionFactor()) {	
					//rollout texture to fullscreen size if division factor of filters varies ... really hurts performance! X-)
					copyScreenBufferTo_GL_TEXTURE_2D(inGL,mScreenWidth,mScreenHeight,tBasePostProcessingFilterChainShaderInterface.getScreenSizeDivisionFactor());
					renderFullScreenQuad(inGL);
					copyScreenBufferTo_GL_TEXTURE_2D(inGL,mScreenWidth,mScreenHeight,1);
				} else {
					copyScreenBufferTo_GL_TEXTURE_2D(inGL,mScreenWidth,mScreenHeight,tBasePostProcessingFilterChainShaderInterface.getScreenSizeDivisionFactor());
				}
			}
		}
		//expand to fullscreen if last filter wasn't fullscreen ...
		if (mFilterList.get(mFilterList.size()-1).getScreenSizeDivisionFactor()!=1) {
			renderFullScreenQuad(inGL);
		}
	}
	
	private void prepareFilterChainShaderRendering(GL2 inGL,GLU inGLU,GLUT inGLUT,BasePostProcessingFilterChainShaderInterface inBasePostProcessingFilterChainShaderInterface) {
        int tScreenSizeDivisionFactor = inBasePostProcessingFilterChainShaderInterface.getScreenSizeDivisionFactor();
        BaseRoutineRuntime.resetFrustumToDefaultState(inGL,inGLU,inGLUT);
        inGL.glDisable(GL_LIGHTING);
    	inGL.glFrontFace(GL_CCW);
    	inGL.glDisable(GL_CULL_FACE);
    	inGL.glDisable(GL_DEPTH_TEST);			
    	inGL.glMatrixMode(GL_PROJECTION);
    	inGL.glLoadIdentity();
    	inGL.glOrtho(0, mScreenWidth, mScreenHeight, 0, -1, 1);
    	inGL.glMatrixMode(GL_MODELVIEW);
    	inGL.glLoadIdentity();
    	//---
        inGL.glActiveTexture(GL_TEXTURE0);
        inGL.glBindTexture(GL_TEXTURE_2D, mFullScreenBackBufferID);
        inGL.glEnable(GL_TEXTURE_2D);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,mGLBorderMode);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,mGLBorderMode);
        //copy full screen to texture ...
        copyScreenBufferTo_GL_TEXTURE_2D(inGL,mScreenWidth,mScreenHeight,1);
        inGL.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
        renderQuad(inGL,tScreenSizeDivisionFactor);    
        //bind old fullscreen texture to texture unit 1
        inGL.glActiveTexture(GL_TEXTURE1);
        inGL.glBindTexture(GL_TEXTURE_2D, mFullScreenBackBufferID);
    	//---
        inGL.glActiveTexture(GL_TEXTURE0);
        inGL.glBindTexture(GL_TEXTURE_2D, mBackBufferTextureID);
        inGL.glEnable(GL_TEXTURE_2D);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,mGLBorderMode);
        inGL.glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,mGLBorderMode);
	}
	
	private void renderFilterChainShader(GL2 inGL,GLU inGLU,GLUT inGLUT,BasePostProcessingFilterChainShaderInterface inBasePostProcessingFilterChainShaderInterface) {
		inBasePostProcessingFilterChainShaderInterface.prepareForProgramUse(inGL);
		int tNumberOfIterations = inBasePostProcessingFilterChainShaderInterface.getNumberOfIterations();
		int tScreenSizeDivisionFactor = inBasePostProcessingFilterChainShaderInterface.getScreenSizeDivisionFactor();
		for (int i=0; i<tNumberOfIterations; i++) {
			copyScreenBufferTo_GL_TEXTURE_2D(inGL,mScreenWidth,mScreenHeight,tScreenSizeDivisionFactor);
        	renderQuad(inGL,tScreenSizeDivisionFactor);
        }
    	inBasePostProcessingFilterChainShaderInterface.stopProgramUse(inGL);
	}
	
	public void cleanup(GL2 inGL,GLU inGLU,GLUT inGLUT) {
		TextureUtils.deleteTextureID(inGL,mBackBufferTextureID);
	}
			 
	private void copyScreenBufferTo_GL_TEXTURE_2D(GL2 inGL,int inScreenWidth,int inScreenHeight,int inScreenSizeDivisionFactor) {	    
		inGL.glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 0, inScreenHeight-(inScreenHeight/inScreenSizeDivisionFactor), inScreenWidth/inScreenSizeDivisionFactor, inScreenHeight/inScreenSizeDivisionFactor, 0);
	}
	
	private void renderFullScreenQuad(GL2 inGL) {
		renderQuad(inGL,1);
	}
	
	private void renderQuad(GL2 inGL,int inScreenSizeDivisionFactor) {
        inGL.glBegin(GL_QUADS);
	    	inGL.glTexCoord2f(0.0f, 1.0f);
			inGL.glVertex2f(0.0f, 0.0f);
			inGL.glTexCoord2f(1.0f, 1.0f);
			inGL.glVertex2f(mScreenWidth/inScreenSizeDivisionFactor, 0.0f);
			inGL.glTexCoord2f(1.0f, 0.0f);
			inGL.glVertex2f(mScreenWidth/inScreenSizeDivisionFactor, mScreenHeight/inScreenSizeDivisionFactor);
			inGL.glTexCoord2f(0.0f, 0.0f);
			inGL.glVertex2f(0.0f, mScreenHeight/inScreenSizeDivisionFactor);
		inGL.glEnd();	
	}
	
}
