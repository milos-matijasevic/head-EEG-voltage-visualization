/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package headeegvoltagevisualization;

import com.jogamp.opengl.GL4;
import shaders.FragmentShader;
import shaders.ShaderProgram;
import shaders.VertexShader;

public class SimpleShader extends ShaderProgram
{
    private String vertexShaderSrc =
            "#version 400 core \n" +
            "layout(location = 0) in vec3 vertexPosition;\n" +
            "void main()\n" +
            "{\n" +
            "gl_Position = vec4(vertexPosition, 1.0);\n" +
            "}\n"
            ;
    
    private String fragmentShaderSrc =
            "#version 400\n" +
            "out vec4 outColor;\n" +
           
            "void main()\n" +
            "{\n" +
            "outColor = vec4(0.0, 0.0, 1.0, 1.0);\n" +
            "}\n"
            ;
    
    private VertexShader vertexShader;
    private FragmentShader fragmentShader;
    private ShaderProgram shaderProgram;

    public SimpleShader(String _name) {
        super(_name);
    }

    public int getProgramObjectID()
    {
        return shaderProgram.getID();
    }
    
    public void buildShader(GL4 gl)
    {
        vertexShader = new VertexShader("SimpleVS");
        fragmentShader = new FragmentShader("Simple FS");
        shaderProgram = new ShaderProgram("Simple");
     
        vertexShader.setSource(vertexShaderSrc);
        fragmentShader.setSource(fragmentShaderSrc);
        
        shaderProgram.addShader(vertexShader);
        shaderProgram.addShader(fragmentShader);
        
        shaderProgram.build(gl);
        System.out.println(shaderProgram.getLog());
    }
    
    
}
