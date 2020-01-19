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
public class GeometryShader extends Shader
{

    public GeometryShader(String _shaderName) 
    {
        super(GL4.GL_GEOMETRY_SHADER, _shaderName);
    }
}
