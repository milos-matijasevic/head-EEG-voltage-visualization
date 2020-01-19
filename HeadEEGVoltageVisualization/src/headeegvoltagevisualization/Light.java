/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package headeegvoltagevisualization;
/**
 *
 * @author 
 */
public class Light extends SceneObject
{
    public Light() {}
    
    public Light(float x, float y, float z)
    {
        position.set(x, y, z);
    }
    
    @Override
    public void update() 
    {
       double angle = (System.currentTimeMillis() % 5000)*360.0/5000.0;
       position.x = (float)(5 * Math.cos( Math.toRadians(angle) ));
       position.z = (float)(5 * Math.sin( Math.toRadians(angle) ));    
    }   
}
