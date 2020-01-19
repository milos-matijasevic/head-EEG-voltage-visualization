/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shaders;

import com.jogamp.opengl.GL4;


/**
 *
 * @author madcat
 */
public class FragmentShader extends Shader
{
    public FragmentShader(String _shaderName) 
    {
        super(GL4.GL_FRAGMENT_SHADER, _shaderName);
    }
    
}
