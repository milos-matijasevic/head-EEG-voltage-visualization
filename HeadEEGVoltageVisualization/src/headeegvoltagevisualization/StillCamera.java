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
public class StillCamera extends Camera {

    public StillCamera() {
        projection.perspective((float) Math.toRadians(45.0), 1.0f, 0.1f, 1000.0f);

        view.translate(0, 0, -80.0f);
//     
    }

    public void moveZ(float z) {
        view.translate(0, 0, z);
    }
}
