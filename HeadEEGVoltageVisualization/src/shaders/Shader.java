/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shaders;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import com.jogamp.opengl.GL4;

/**
 *
 * @author djordje
 */
abstract public class Shader
{
    private int shaderObjectID;
    private final int shaderType;
    private final String shaderName;
    
    private String []source;
    private final StringBuffer compilationLog = new StringBuffer();
 
    public enum Status { UNITINITALIZED, UNCOMPILED, COMPILED_FAILURE, COMPILED_SUCCESS, CANT_CREATE_OBJECT };
    
    private Status compilationStatus = Status.UNITINITALIZED;
    
    public Shader(int _shaderType, String _shaderName)
    {
        shaderType = _shaderType;
        shaderName = _shaderName;
    }

    public void setSource(String []src)
    {
        source = src;
        if( compilationStatus != Status.UNITINITALIZED )
            compilationStatus = Status.UNCOMPILED;
    }
    
    public void setSource(String s)
    {
        String []sources = { s };
        setSource(sources);
    }
    
    public void readSource(Path filePath) throws IOException
    {
        String readSource = new String( Files.readAllBytes(filePath));       
        setSource(readSource);
    }
    
    public String getCompilationInfo()
    {
        return compilationLog.toString();
    }
    
    public int getID()
    {
        return shaderObjectID;
    }
    
    public Status getStatus()
    {
        return compilationStatus;
    }
    
    public void buildShader(GL4 gl)
    {
        if( compilationStatus == Status.CANT_CREATE_OBJECT || 
            compilationStatus == Status.COMPILED_FAILURE || 
            compilationStatus == Status.COMPILED_SUCCESS )
            return;
        
        if( shaderObjectID == 0 )
        {
            shaderObjectID = gl.glCreateShader(shaderType);

            int error = gl.glGetError();
            if( error != gl.GL_NO_ERROR )
            {
                compilationLog.append("Error creating shader " + shaderName + "(" + getClass().getName() + ")" );
                compilationStatus = Status.CANT_CREATE_OBJECT;
                return;
            }
        }
        
        
        gl.glShaderSource(shaderObjectID, source.length, source, null);
        gl.glCompileShader(shaderObjectID);
        
        int []params = new int[1];
        gl.glGetShaderiv(shaderObjectID, GL4.GL_COMPILE_STATUS, params, 0 );
        compilationLog.append("Shader " + shaderName + "(" + getClass().getName() + ") compilation status: " );
        if( params[0] == 1 )
        {
            compilationLog.append("SUCCESS");
            compilationStatus = Status.COMPILED_SUCCESS;
        }
        else
        {
            compilationLog.append("FAILURE");
            compilationStatus = Status.COMPILED_FAILURE;
        }
        compilationLog.append("\n");
        
        gl.glGetShaderiv(shaderObjectID, GL4.GL_INFO_LOG_LENGTH,  params, 0);
        int infoLogLen = params[0];
        ByteBuffer shaderLog = ByteBuffer.allocate(infoLogLen);
        gl.glGetShaderInfoLog(shaderObjectID, infoLogLen, null, shaderLog);

        try
        {
            compilationLog.append( new String(shaderLog.array(), "ASCII"));      
        }
        catch(UnsupportedEncodingException e)
        {
            
        }
    }
    
    public void delete(GL4 gl)
    {
        gl.glDeleteShader(shaderObjectID);
        shaderObjectID = 0;
    }
}
