/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package headeegvoltagevisualization;

import com.jogamp.graph.geom.Vertex;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Color;

/**
 *
 * @author mm193059m
 */
public class HeadImportTest implements GLEventListener, MouseListener, KeyListener {

    private GLWindow prozor; // prozor, drawable objekat
    private static String naslov = "Kocke"; // naslov prozora
    private int sirinaProzora = 600, visinaProzora = 500; // sirina i visina prozora
    private static final int FPS = 60; // ucestanost kojom ce objekat animatora da poziva display() metod (videti nize)
    private FPSAnimator animator;
    private static OBJModel model;

    private float draggx, draggy;
    private float ugaox, ugaoy;
    private static List<OBJVertex> electrodes = new ArrayList<>();
    private static List<List<Float>> electrodes_values = new ArrayList<>();
    private int electrodes_idx = 0;
    float angle_max = 70;

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
        prozor.addKeyListener(this);
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
        gl.glRotatef(ugaoy, 1, 0, 0);
        gl.glRotatef(ugaox, 0, 1, 0);

        gl.glBegin(GL2.GL_TRIANGLES);
        int c = 0;
        for (OBJObject object : model.getObjects()) {
            for (OBJMesh mesh : object.getMeshes()) {
                for (OBJFace face : mesh.getFaces()) {
                    for (OBJDataReference reference : face.getReferences()) {
                        final OBJVertex vertex = model.getVertex(reference);
                        if (vertex.y > 5) {
                            float colors[] = findColor(vertex);
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
                            gl.glColor3f(colors[0], colors[1], colors[2]);
                        } else {
                            gl.glColor3f(0.945f, 0.761f, 0.49f);
                        }
                        gl.glVertex3f(vertex.x, vertex.y, vertex.z);
                    }
                }
            }
        }
        gl.glEnd();

//        gl.glBegin(GL2.GL_POINTS);
//        for (OBJVertex vertex : electrodes) {
//            gl.glColor3f(0, 0, 1);
//            gl.glVertex3f(vertex.x, vertex.y, vertex.z);
//        }
//        gl.glEnd();
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
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        draggx = e.getX();
        draggy = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }

    @Override
    public void mouseMoved(MouseEvent arg0) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        float newx = e.getX();
        float newy = e.getY();

//        System.out.println("x = " + newx);
//        System.out.println("y = " + newy);
        ugaox += newx - draggx;
        ugaoy += newy - draggy;
        draggx = newx;
        draggy = newy;
    }

    @Override
    public void mouseWheelMoved(MouseEvent arg0) {
    }

//    @Override
    public void keyPressed(KeyEvent ke) {

        if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
            if (electrodes_idx > 0) {
                electrodes_idx--;
            } else {
                electrodes_idx = electrodes_values.size() - 1;
            }
        }
        if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (electrodes_idx < electrodes_values.size() - 1) {
                electrodes_idx++;
            } else {
                electrodes_idx = 0;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
    }

    private float[] findColor(OBJVertex vertex) {
        float colors[] = new float[3];
        float uk_angle = 0;
        for (int i = 0; i < electrodes.size(); i++) {
            OBJVertex electrode = electrodes.get(i);
            float dot_prod = electrode.x * vertex.x + electrode.y * vertex.y + electrode.z * vertex.z;

            double mag_el = Math.sqrt(electrode.x * electrode.x + electrode.y * electrode.y + electrode.z * electrode.z);
            double mag_v = Math.sqrt(vertex.x * vertex.x + vertex.y * vertex.y + vertex.z * vertex.z);

            double cos = dot_prod / (mag_el * mag_v);

            float angle_rad = (float) Math.acos(cos);

            float angle = (float) Math.toDegrees(angle_rad);

            if (Math.abs(angle) < angle_max) {
                uk_angle += 100 - angle;
            }
        }

        for (int i = 0; i < electrodes.size(); i++) {
            OBJVertex electrode = electrodes.get(i);
            float dot_prod = electrode.x * vertex.x + electrode.y * vertex.y + electrode.z * vertex.z;

            double mag_el = Math.sqrt(electrode.x * electrode.x + electrode.y * electrode.y + electrode.z * electrode.z);
            double mag_v = Math.sqrt(vertex.x * vertex.x + vertex.y * vertex.y + vertex.z * vertex.z);

            double cos = dot_prod / (mag_el * mag_v);

            float angle_rad = (float) Math.acos(cos);

            float angle = (float) Math.toDegrees(angle_rad);
            //     System.out.println(angle);

            if (Math.abs(angle) < angle_max) {
                float hue = electrodes_values.get(electrodes_idx).get(i);
                hue += 50;
                hue /= 100;
                hue *= 300;
                hue /= 360;
                hue = 1 - hue;
                int rgb = Color.HSBtoRGB(hue, 1, 1);

                float curr_colors[] = new float[3];
                curr_colors[0] = (rgb >> 16) & 0xFF;
                curr_colors[1] = (rgb >> 8) & 0xFF;
                curr_colors[2] = rgb & 0xFF;

                for (int j = 0; j < colors.length; j++) {
                    colors[j] += (curr_colors[j] / 255f) * (angle / uk_angle);
                }
            }
        }
        return colors;
    }

    public static void main(String args[]) throws IOException {
        // Open a stream to your OBJ resource
        try (InputStream in = new FileInputStream("male_head.obj")) {
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
            Logger.getLogger(HeadImportTest.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        String electrodes_in[] = null;
        try {
            File voltages = new File("voltages.txt");
            FileReader fr = new FileReader(voltages);
            BufferedReader br = new BufferedReader(fr);
            String line;
            br.readLine();
            int i = 0;

            while ((line = br.readLine()) != null) {
                if (i == 0) {
                    electrodes_in = line.split(",");
                    i++;
                } else {
                    String electrodes_val[];
                    electrodes_val = line.split(",");
                    ArrayList<Float> curr_values = new ArrayList<>();
                    for (int j = 0; j < electrodes_val.length; j++) {
                        if (j == 0) {
                            curr_values.add(Float.parseFloat(electrodes_val[j]));
                        } else {
                            curr_values.add(Float.parseFloat(electrodes_val[j].substring(1)));
                        }
                    }
                    electrodes_values.add(curr_values);
                }

            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(HeadImportTest.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        try {
            File voltages = new File("electrodes.txt");
            FileReader fr = new FileReader(voltages);
            BufferedReader br = new BufferedReader(fr);
            String line;

            while ((line = br.readLine()) != null) {
                String[] split = line.split("\\s*( )+");

                float fi = Float.parseFloat(split[2]);
                float teta = Float.parseFloat(split[3]);
                fi = (float) Math.toRadians(fi);
                teta = (float) Math.toRadians(teta);
                
                float p = 1;
                OBJVertex v = new OBJVertex();
                v.x = (float) (Math.sin(fi) * Math.cos(teta)) * p;
                v.y = (float) (Math.sin(fi) * Math.sin(teta)) * p;
                v.z = (float) (Math.cos(fi)) * p;

                int j;
                for (j = 0; j < electrodes_in.length; j++) {
                    if (j > 0 && split[1].equals(electrodes_in[j].substring(1))) {
                        break;
                    } else if (split[1].equals(electrodes_in[j])) {
                        break;
                    }
                }
                if (j < electrodes_in.length) {
                    electrodes.add(v);

                }

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HeadImportTest.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(electrodes.size() + " ");
        new HeadImportTest();
    }

}
