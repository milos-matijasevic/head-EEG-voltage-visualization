/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package headeegvoltagevisualization;

import org.joml.Vector3f;

/**
 *
 * @author 
 */
public abstract class SceneObject 
{
    protected Vector3f position = new Vector3f();
    
    public Vector3f getPosition()
    {
        return position;
    }
    
    public void setPosition(Vector3f pos)
    {
        position.set(pos);
    }
    
    public void setPosition(float x, float y, float z)
    {
        position.x = x;
        position.y = y;
        position.z = z;
    }
    
    public abstract void update();
}
