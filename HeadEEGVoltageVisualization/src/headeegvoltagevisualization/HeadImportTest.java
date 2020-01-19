/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package headeegvoltagevisualization;

import com.jogamp.common.nio.Buffers;
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
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.math.Matrix4;
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
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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

    private NeckFaceShader neckFaceShader;
    public static Light mainLight = new Light(1, 1, 0);

    private int vertexBufferID;
    private int vertexArrayID;
    private int vertexIndexBufferID;
    private int colorBufferID;
    private int num_faces;

    private SimpleShader ss;

    public Camera mainCamera = new StillCamera();
    public CameraFrustum cf;

    public HeadImportTest() {
        // Podesavanje OpenGL mogucnosti, koje zavise od profila
        GLProfile glp = GLProfile.getMaxProgrammable(true);

        System.out.println(glp.getGLImplBaseClassName());
        System.out.println(glp.getImplName());
        System.out.println(glp.getName());
        System.out.println(glp.hasGLSL());

        // Specifies a set of OpenGL capabilities, based on your profile.
        GLCapabilities caps = new GLCapabilities(glp);

        caps.setAlphaBits(8);
        caps.setDepthBits(24);
        caps.setDoubleBuffered(true);

        neckFaceShader = new NeckFaceShader();

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
        GL4 gl = drawable.getGL().getGL4();

        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        IntBuffer intBuffer = IntBuffer.allocate(1);
        gl.glGenVertexArrays(1, intBuffer);
        vertexArrayID = intBuffer.get(0);

        gl.glBindVertexArray(vertexArrayID);

        List<Float> vertexi = new ArrayList<>();
        for (OBJVertex v : model.getVertices()) {
            Matrix4 m;
            vertexi.add(v.x * 0.02f);
            vertexi.add(v.y * 0.02f);
            vertexi.add((v.z - 5) * 0.02f);
        }
        float[] vertexData = new float[vertexi.size()];
        for (int i = 0; i < vertexi.size(); i++) {
            vertexData[i] = vertexi.get(i);
        }

        float[] colorData = new float[vertexData.length];

        for (int i = 0; i < vertexData.length; i++) {
            colorData[i] = 0.5f;
        }

        OBJObject obj = null;
        for (OBJObject object : model.getObjects()) {
            obj = object;
        }
        List<Integer> indeksi = new ArrayList<>();
        for (OBJMesh mesh : obj.getMeshes()) {
            for (OBJFace face : mesh.getFaces()) {
                for (OBJDataReference reference : face.getReferences()) {
                    indeksi.add(reference.vertexIndex);
                }
            }
        }

        int[] indices = new int[indeksi.size()];
        for (int i = 0; i < indeksi.size(); i++) {
            indices[i] = indeksi.get(i);
        }
        num_faces = indices.length;

        FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(vertexData, 0);
        FloatBuffer vertexColorBuffer = Buffers.newDirectFloatBuffer(colorData, 0);
        IntBuffer vertexIndexBuffer = Buffers.newDirectIntBuffer(indices, 0);

        intBuffer.rewind();
        gl.glGenBuffers(1, intBuffer);
        vertexBufferID = intBuffer.get(0);
        gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vertexBufferID);
        gl.glBufferData(GL4.GL_ARRAY_BUFFER, vertexData.length * Float.BYTES, vertexBuffer, GL4.GL_STATIC_DRAW);
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL4.GL_FLOAT, false, 0, 0);

        intBuffer.rewind();
        gl.glGenBuffers(1, intBuffer);
        colorBufferID = intBuffer.get(0);
        gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, colorBufferID);
        gl.glBufferData(GL4.GL_ARRAY_BUFFER, colorData.length * Float.BYTES, vertexColorBuffer, GL4.GL_STATIC_DRAW);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 3, GL4.GL_FLOAT, false, 0, 0);

        intBuffer.rewind();
        gl.glGenBuffers(1, intBuffer);
        vertexIndexBufferID = intBuffer.get(0);
        gl.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, vertexIndexBufferID);
        gl.glBufferData(GL4.GL_ELEMENT_ARRAY_BUFFER, indices.length * Integer.BYTES, vertexIndexBuffer, GL4.GL_STATIC_DRAW);

        ss = new SimpleShader("moj");
        ss.buildShader(gl);

        gl.glUseProgram(ss.getProgramObjectID());

        int error = gl.glGetError();
        System.out.println("Init: error = " + error);

        /*Kamere*/
        cf = new CameraFrustum(mainCamera);
        cf.setShaderProgram(ss);
        cf.init(gl);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    @Override
    public void display(GLAutoDrawable drawable
    ) {
        GL4 gl = drawable.getGL().getGL4();

        // Obrisi bafer za boje i bafer za dubine
        gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);

        //gl.glDrawArrays(GL4.GL_TRIANGLES, 0, 3);
        //ee
        gl.glBindVertexArray(vertexArrayID);
        gl.glUseProgram(ss.getProgramObjectID());
        gl.glDrawElements(GL4.GL_TRIANGLES, num_faces, GL4.GL_UNSIGNED_INT, 0);
        gl.glBindVertexArray(0);

//        int c = 0;
//        for (OBJObject object : model.getObjects()) {
//            for (OBJMesh mesh : object.getMeshes()) {
//                for (OBJFace face : mesh.getFaces()) {
//                    for (OBJDataReference reference : face.getReferences()) {
//                        final OBJVertex vertex = model.getVertex(reference);
//                        if (vertex.y > 5) {
//                            float colors[] = findColor(vertex);
//                            switch (c) {
//                                case 0:
//                                    gl.glColor3f(1, 0, 0);
//                                    break;
//                                case 1:
//                                    gl.glColor3f(0, 1, 0);
//                                    break;
//                                case 2:
//                                    gl.glColor3f(0, 0, 1);
//                                    break;
//                            }
//                            c = (c + 1) % 3;
//                            gl.glColor3f(colors[0], colors[1], colors[2]);
//                        } else {
//                            gl.glColor3f(0.945f, 0.761f, 0.49f);
//                        }
//                        gl.glVertex3f(vertex.x, vertex.y, vertex.z);
//                    }
//                }
//            }
//        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height
    ) {
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
