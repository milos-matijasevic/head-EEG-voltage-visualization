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
            + "out vec3 vertexPos;\n"
            + "void main()\n"
            + "{\n"
            + "vertexPos = vertexPosition;"
            + "gl_Position = vec4(vertexPosition, 1.0);\n"
            + "}\n";

    private String fragmentShaderSrc
            = "#version 400\n"
            + "in vec3 vertexPos;\n"
            + "out vec4 outColor;\n"
            + "void main()\n"
            + "{\n"
            + "float hue = abs(vertexPos.y) - 0.2;"
            + "hue /= 0.70;"
            + "hue = hue * 2.8 / 3.6;"
            + "vec3 c = vec3(hue, 1.0, 1.0);\n"
            + "vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);\n"
            + "vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);\n"
            + "vec3 color = c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);"
            + "outColor = vec4(color, 1.0);\n"
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

        shaderProgram.build(gl, uniforms);
        System.out.println(shaderProgram.getLog());
    }

}
