/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package headeegvoltagevisualization;

import org.joml.Matrix4f;

/**
 *
 * @author 
 */
public class Camera
{
    protected Matrix4f view = new Matrix4f();
    protected Matrix4f projection = new Matrix4f();    
    
    public Camera() {}
    
    public Matrix4f GetProjection()
    {
        return projection;
    }

    public Matrix4f GetView()
    {
        return view;
    }
    
    public Matrix4f GetViewProjection()
    {
        return new Matrix4f().mul(projection).mul(view); // multiply by right; matrix are column ordered; resulting matrix is P * V
    }
    
    public void update() {}
}
