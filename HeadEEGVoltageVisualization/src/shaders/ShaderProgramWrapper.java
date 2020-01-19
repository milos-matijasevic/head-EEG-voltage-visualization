/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shaders;

import java.util.ArrayList;
import com.jogamp.opengl.GL4;

/**
 *
 * @author djordje
 */
public abstract class ShaderProgramWrapper 
{   
    protected VertexShader vertexShader;
    protected FragmentShader fragmentShader;
    protected GeometryShader geometryShader;
    protected ShaderProgram shaderProgram;
    protected ArrayList<String> uniforms;
    
    protected abstract void setupSources();
    
    public void buildShader(GL4 gl)
    {
        if( shaderProgram != null )
            return;

        setupSources();

        shaderProgram.addShader(vertexShader);
        shaderProgram.addShader(fragmentShader);
        if(geometryShader != null)
            shaderProgram.addShader(geometryShader);
        
        shaderProgram.build(gl, uniforms);
        System.out.println(shaderProgram.getLog());       
    }
    
    public int getProgramObjectID()
    {
        return shaderProgram.getID();
    }
       
    public ShaderProgram GetShaderProgram()
    {
        return shaderProgram;
    }    
}
