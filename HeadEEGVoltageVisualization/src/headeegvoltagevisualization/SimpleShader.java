/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package headeegvoltagevisualization;

import com.jogamp.opengl.GL4;
import java.util.ArrayList;
import shaders.FragmentShader;
import shaders.ShaderProgram;
import shaders.VertexShader;

public class SimpleShader extends ShaderProgram {

    private String vertexShaderSrc
            = "#version 400 core \n"
            + "layout(location = 0) in vec3 vertexPosition;\n"
            + "layout(location = 1) in vec3 vertexColor;\n"
            + "out vec4 interpolatedVertexColor;\n"
            + "uniform mat4 MVPTransform;\n"
            + "uniform mat4 rotate;\n"
            + "void main()\n"
            + "{\n"
            + "interpolatedVertexColor = vec4(vertexColor, 1);"
            + "gl_Position = MVPTransform * rotate * vec4(vertexPosition, 1.0);\n"
            + "}\n";

    private String fragmentShaderSrc
            = "#version 400\n"
            + "in vec4 interpolatedVertexColor;\n"
            + "out vec4 outColor;\n"
            + "void main()\n"
            + "{\n"
            + "outColor = interpolatedVertexColor;\n"
            + "}\n";

    private VertexShader vertexShader;
    private FragmentShader fragmentShader;
    private ShaderProgram shaderProgram;

    public SimpleShader(String _name) {
        super(_name);
    }

    public int getProgramObjectID() {
        return shaderProgram.getID();
    }

    public ShaderProgram GetShaderProgram() {
        return shaderProgram;
    }

    public void buildShader(GL4 gl) {
        vertexShader = new VertexShader("SimpleVS");
        fragmentShader = new FragmentShader("Simple FS");
        shaderProgram = new ShaderProgram("Simple");

        vertexShader.setSource(vertexShaderSrc);
        fragmentShader.setSource(fragmentShaderSrc);

        shaderProgram.addShader(vertexShader);
        shaderProgram.addShader(fragmentShader);

        ArrayList<String> uniforms = new ArrayList<String>();
        uniforms.add("MVPTransform");
        
        uniforms.add("MVTransform");
        uniforms.add("rotate");
        uniforms.add("NormalTransform");
        uniforms.add("LightPosition");
        
        shaderProgram.build(gl, uniforms);
        System.out.println(shaderProgram.getLog());
    }

}
