/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shaders;

import com.jogamp.opengl.GL4;

/**
 *
 * @author djordje
 */
public abstract class ShaderProgramActivation 
{
    protected ShaderProgram program;
    private boolean isInitialized;
    
    void assignProgram(ShaderProgram p)
    {
        program = p;
    }
    
    public void activate(GL4 gl)
    {
        if(! isInitialized )
        {
            initialize(gl);
            isInitialized = true;
        }
        
        activateInternal(gl);
    }
    
    protected abstract void activateInternal(GL4 gl);
    protected abstract void initialize(GL4 gl);
}
