/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package headeegvoltagevisualization;

import com.jogamp.opengl.GL4;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import shaders.FragmentShader;
import shaders.ShaderProgram;
import shaders.ShaderProgramWrapper;
import shaders.VertexShader;

/**
 *
 * @author Milos
 */
public class NeckFaceShader extends ShaderProgramWrapper {
    @Override
    protected void setupSources() 
    {
        String shadersPath = Paths.get(".").toAbsolutePath().normalize().toString() + "/shaders/";
        
        vertexShader = new VertexShader("LightingPhong VS");
        fragmentShader = new FragmentShader("LightingPhong FS");
        shaderProgram = new ShaderProgram("LightingPhong");
     
        try
        {
            vertexShader.readSource( Paths.get(shadersPath+"LightingPhong_VS.shader") );
            fragmentShader.readSource( Paths.get(shadersPath+"LightingPhong_FS.shader") );
        }
        catch(IOException e)
        {
            System.out.println(e);
            return;
        }
        
        uniforms = new ArrayList<>();
        uniforms.add("MVPTransform");
        uniforms.add("MVTransform");
        uniforms.add("Rotate");
        uniforms.add("NormalTransform");
        uniforms.add("LightPosition");   
        uniforms.add("ElectrodesNumber");
        uniforms.add("ElectrodesValues");
        uniforms.add("Electrodes");
        uniforms.add("MaxAngle");
    }
    
}
