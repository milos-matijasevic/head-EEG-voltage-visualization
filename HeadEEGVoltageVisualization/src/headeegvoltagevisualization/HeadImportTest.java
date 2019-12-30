/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package headeegvoltagevisualization;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;
import com.mokiat.data.front.parser.IOBJParser;
import com.mokiat.data.front.parser.OBJDataReference;
import com.mokiat.data.front.parser.OBJFace;
import com.mokiat.data.front.parser.OBJMesh;
import com.mokiat.data.front.parser.OBJModel;
import com.mokiat.data.front.parser.OBJObject;
import com.mokiat.data.front.parser.OBJParser;
import com.mokiat.data.front.parser.OBJVertex;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mm193059m
 */
public class HeadImportTest implements GLEventListener, MouseListener {

    private GLWindow prozor; // prozor, drawable objekat
    private static String naslov = "Kocke"; // naslov prozora
    private int sirinaProzora = 600, visinaProzora = 500; // sirina i visina prozora
    private static final int FPS = 60; // ucestanost kojom ce objekat animatora da poziva display() metod (videti nize)
    private FPSAnimator animator;
    private static OBJModel model;

    float draggx = 0, draggy = 0;

    public HeadImportTest() {
        GLProfile glp = GLProfile.getDefault();

        // Podesavanje OpenGL mogucnosti, koje zavise od profila
        GLCapabilities caps = new GLCapabilities(glp);

        caps.setAlphaBits(8);
        caps.setDepthBits(24);
        caps.setDoubleBuffered(true);

        // Pravljenje OpenGL prozora gde ce da se radi iscrtavanje, drawable objekat
        prozor = GLWindow.create(caps);

        animator = new FPSAnimator(prozor, FPS, true);

        // Pravljenje animatora koji ce da poziva display() funkciju (videti nize), sa zadatim FPS.
        prozor.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDestroyNotify(WindowEvent arg0) {
                // Posebna nit poziva metodu stop()
                // kojom se prekida nit animatora, pre nego sto se program zavrsi
                new Thread() {
                    @Override
                    public void run() {
                        // prekini rad animatora
                        animator.stop();
                        System.exit(0);
                    }
                }.start();
            }
        ;
        });
		
        prozor.addGLEventListener(this);
        prozor.addMouseListener(this);
        prozor.setSize(sirinaProzora, visinaProzora);
        prozor.setTitle(naslov);
        prozor.setVisible(true);
        animator.start();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        // Pozadinska boja se postavlja na belu
        gl.glClearColor(1, 1, 1, 0);

        // Koristi se model nijansiranog sencenja
        gl.glShadeModel(GL2.GL_SMOOTH);

        // Ukljucuje se rezim provere dubine pre crtanja
        gl.glEnable(GL2.GL_DEPTH_TEST);

    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        // Obrisi bafer za boje i bafer za dubine
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL2.GL_MODELVIEW);

        gl.glLoadIdentity();

        gl.glPushMatrix();

        gl.glTranslatef(0, 0, -50);
        gl.glRotatef(60, 0, 1, 0);

        gl.glBegin(GL2.GL_TRIANGLES);
        int c = 0;
        for (OBJObject object : model.getObjects()) {
            for (OBJMesh mesh : object.getMeshes()) {
                for (OBJFace face : mesh.getFaces()) {
                    for (OBJDataReference reference : face.getReferences()) {
                        switch (c) {
                            case 0:
                                gl.glColor3f(1, 0, 0);
                                break;
                            case 1:
                                gl.glColor3f(0, 1, 0);
                                break;
                            case 2:
                                gl.glColor3f(0, 0, 1);
                                break;
                        }
                        c = (c + 1) % 3;
                        //   gl.glColor3f(1, 0, 0);
                        final OBJVertex vertex = model.getVertex(reference);
                        gl.glVertex3f(vertex.x, vertex.y, vertex.z);
                    }
                }
            }
        }

        gl.glEnd();

        gl.glPopMatrix();
        gl.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();

        // Postavljanje viewport-a
        gl.glViewport(0, 0, width, height);
        sirinaProzora = width;
        visinaProzora = height;

        postaviProjekciju(drawable);
    }

    private void postaviProjekciju(GLAutoDrawable drawable) {
        // Dohvatanje OpenGL konteksta
        GL2 gl = drawable.getGL().getGL2();

        // Biranje matrice PROJEKCIJE kao aktivne matrice za buducu manipulaciju
        gl.glMatrixMode(GL2.GL_PROJECTION);

        // Postavljanje na jedinicnu matricu
        gl.glLoadIdentity();

        gl.glFrustum(-1, 1, -1, 1, 1, 300);
        //  gl.glOrtho(-1.5, 1.5, -1.5, 1.5, 0, 6);

        // Vracanje podrazumevane matrice
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
        draggx = draggy = 0;
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }

    @Override
    public void mouseMoved(MouseEvent arg0) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        System.out.println("Mouse is being dragged");
        float newx = e.getX();
        float newy = e.getY();

        System.out.println("x = " + newx);
        System.out.println("y = " + newy);

        draggx = newx;
        draggy = newy;
    }

    @Override
    public void mouseWheelMoved(MouseEvent arg0) {
    }

    public static void main(String args[]) {
        // Open a stream to your OBJ resource
        try ( InputStream in = new FileInputStream("male_head.obj")) {
            // Create an OBJParser and parse the resource
            final IOBJParser parser = new OBJParser();
            model = parser.parse(in);

            // Use the model representation to get some basic info
            System.out.println(MessageFormat.format(
                    "OBJ model has {0} vertices, {1} normals, {2} texture coordinates, and {3} objects.",
                    model.getVertices().size(),
                    model.getNormals().size(),
                    model.getTexCoords().size(),
                    model.getObjects().size()));
        } catch (IOException ex) {
            Logger.getLogger(HeadImportTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        new HeadImportTest();
    }

}
