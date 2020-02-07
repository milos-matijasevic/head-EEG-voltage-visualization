/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shaders;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import com.jogamp.opengl.GL4;

/**
 *
 * @author djordje
 */
public class ShaderProgram 
{
    private Dictionary<String, Integer> uniforms = new Hashtable<String, Integer>();
    private int programID;
    private ArrayList<Shader> shaders = new ArrayList<>();
    private String name;
    private StringBuilder buildLog = new StringBuilder();
    public enum Status { PROGRAM_NOT_COMPLETE, PROGRAM_COMPLETE };
    private Status programStatus = Status.PROGRAM_NOT_COMPLETE;
    
    private ShaderProgramActivation activation;
    
    public ShaderProgram(String _name)
    {
        name = _name;
    }
    
    public void setActivation(ShaderProgramActivation a)
    {
        activation = a;
        activation.assignProgram(this);
    }
    
    public ShaderProgramActivation getActivation()
    {
        return activation;
    }
    
    public ShaderProgram addShader(Shader s)
    {
        shaders.add(s);
        return this;
    }
    
    public int getID()
    {
        return programID;
    }

    public Status getStatus()
    {
        return programStatus;
    }
    
    public String getLog()
    {
        return buildLog.toString();
    }

    public int getUniformLocation(String uniformName)
    {
        Integer i = uniforms.get(uniformName);
        
        if( i == null )
            return -1;
        return  i;
    }
    
    
    public void build(GL4 gl)
    {
        build(gl, null);
    }

    public void build(GL4 gl, ArrayList<String> listOfUniforms)
    {
        if( programID == 0 )
            programID = gl.glCreateProgram();

        programStatus = Status.PROGRAM_NOT_COMPLETE;
        
        buildLog = new StringBuilder();
        buildLog.append("Shader program " + name + " build log:\n");

        boolean allSuccessful = true;
        for(Shader s : shaders)
        {
            if( s.getStatus() == Shader.Status.UNCOMPILED ||
                s.getStatus() == Shader.Status.UNITINITALIZED )
            {
                s.buildShader(gl);
            }
            
            if( s.getStatus() != Shader.Status.COMPILED_SUCCESS )
            {
                buildLog.append(s.getCompilationInfo()).append("\n");
                allSuccessful = false;
            }
        }
        
        if( ! allSuccessful )
        {
            buildLog.append("Shader program " + name + " not created!\n");
            gl.glDeleteProgram(programID);
            return;
        }
        
        for(Shader s : shaders)
            gl.glAttachShader(programID, s.getID());
        
        gl.glLinkProgram(programID);
        
        int []params = new int[1];
        gl.glGetProgramiv(programID, gl.GL_LINK_STATUS, params, 0);
        buildLog.append("Program " + name + " link stauts: ");
        if( params[0] == 1 )
        {
            buildLog.append("SUCCESS");
            programStatus = Status.PROGRAM_COMPLETE;
        }
        else
            buildLog.append("FAILURE");

        buildLog.append(".\n");
      
        if (programStatus == Status.PROGRAM_COMPLETE && listOfUniforms != null) 
        {
            for (String s : listOfUniforms) 
            {
                int location = gl.glGetUniformLocation(programID, s);
                if (location > -1) 
                {
                    uniforms.put(s, location);
                } 
                else 
                {
                    buildLog.append("Shader program " + name + ": uniform " + s + " not found.\n");
                }
            }
        }
        
        gl.glGetProgramiv(programID, gl.GL_INFO_LOG_LENGTH, params, 0);
        int infoLogLen = params[0];
        ByteBuffer programLog = ByteBuffer.allocate(infoLogLen);
        gl.glGetProgramInfoLog(programID, infoLogLen, null, programLog);
        buildLog.append(programLog.array()); 
        
    }
    
    public void delete(GL4 gl)
    {
        if( getID() == 0 )
            return;
        
        for(Shader s : shaders )
        {
            gl.glDetachShader(programID, s.getID());
            s.delete(gl);
        }
        
        gl.glDeleteProgram(programID);
        programID = 0;
    }
    
    public void activate(GL4 gl)
    {
        gl.glUseProgram(programID);
        if( activation != null )
            activation.activate(gl);
    }
    
    public static void deactivate(GL4 gl)
    {
        gl.glUseProgram(0);
    }
}
