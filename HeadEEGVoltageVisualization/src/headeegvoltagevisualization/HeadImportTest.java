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
import com.jogamp.opengl.util.FPSAnimator;
import com.mokiat.data.front.parser.IOBJParser;
import com.mokiat.data.front.parser.OBJDataReference;
import com.mokiat.data.front.parser.OBJFace;
import com.mokiat.data.front.parser.OBJMesh;
import com.mokiat.data.front.parser.OBJModel;
import com.mokiat.data.front.parser.OBJNormal;
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
import java.util.HashMap;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 *
 * @author mm193059m
 */
public class HeadImportTest implements GLEventListener, MouseListener, KeyListener {

    private GLWindow prozor; // prozor, drawable objekat
    private static String naslov = "Head EEG"; // naslov prozora
    private int sirinaProzora = 700, visinaProzora = 600; // sirina i visina prozora
    private static final int FPS = 60; // ucestanost kojom ce objekat animatora da poziva display() metod (videti nize)
    private FPSAnimator animator;
    private static OBJModel model;

    private float draggx, draggy;
    private float ugaox, ugaoy;
    private static List<Float> electrodes = new ArrayList<>();
    private static List<List<Float>> electrodes_values = new ArrayList<>();
    private int electrodes_idx = 0;
    private int electrodes_base = 0;
    float angle_max = 70;

    private NeckFaceShader neckFaceShader;
    private SimpleShader simpleShader;
    public static Light mainLight = new Light(0, -1000, 5000);

    private int vertexBufferID;
    private int vertexArrayID;
    private int vertexIndexBufferID;
    private int colorBufferID;
    private int normalBufferID;
    private int num_faces;

    //private SimpleShader ss;
    public StillCamera mainCamera = new StillCamera();

    private Matrix4f transformHead = new Matrix4f();
    private float[] transformArray = new float[16];

    private Matrix3f normalTransform = new Matrix3f();
    private float[] normalTransformArray = new float[16];
    private float[] lightPosArray = new float[3];
    private float maxAngle = 10f;
    private static int frequency;

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
        prozor.setResizable(false);
        animator.start();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL4 gl = drawable.getGL().getGL4();

        neckFaceShader = new NeckFaceShader();
        neckFaceShader.buildShader(gl);
        simpleShader = new SimpleShader("ss");
        simpleShader.buildShader(gl);
        

        OBJObject obj = null;
        for (OBJObject object : model.getObjects()) {
            obj = object;
        }

        List<Float> vertexi = new ArrayList<>();
        List<Integer> indeksi = new ArrayList<>();
        List<Float> normals = new ArrayList<>();

        /* Map normals and vertexes*/
        HashMap<String, Integer> vertexNormalToIndex = new HashMap<>();

        int idx = 0;
        for (OBJMesh mesh : obj.getMeshes()) {
            for (OBJFace face : mesh.getFaces()) {
                for (OBJDataReference reference : face.getReferences()) {
                    int vertexIdx = reference.vertexIndex;
                    int normalIdx = reference.normalIndex;
                    String key = vertexIdx + "," + normalIdx;

                    int faceIdx;
                    if (vertexNormalToIndex.containsKey(key)) {
                        faceIdx = vertexNormalToIndex.get(key);
                    } else {
                        vertexi.add(model.getVertices().get(vertexIdx).x);
                        vertexi.add(model.getVertices().get(vertexIdx).y);
                        vertexi.add(model.getVertices().get(vertexIdx).z);

                        normals.add(model.getNormals().get(normalIdx).x);
                        normals.add(model.getNormals().get(normalIdx).y);
                        normals.add(model.getNormals().get(normalIdx).z);

                        faceIdx = idx;
                        vertexNormalToIndex.put(key, faceIdx);
                        idx++;
                    }
                    indeksi.add(faceIdx);
                }
            }
        }

        float[] vertexData = new float[vertexi.size()];
        for (int i = 0; i < vertexi.size(); i++) {
            vertexData[i] = vertexi.get(i);
        }

        float[] colorData = new float[vertexData.length];

        float[] normalData = new float[vertexData.length];
        for (int i = 0; i < normals.size(); i++) {
            normalData[i] = normals.get(i);
        }

        int[] indices = new int[indeksi.size()];
        for (int i = 0; i < indeksi.size(); i++) {
            indices[i] = indeksi.get(i);
        }
        num_faces = indices.length;

        // Skin color
        for (int i = 0; i < vertexData.length; i += 3) {
            colorData[i] = 224 / 255f;
            colorData[i + 1] = 172 / 255f;
            colorData[i + 2] = 105 / 255f;
        }

        IntBuffer intBuffer = IntBuffer.allocate(1);
        gl.glGenVertexArrays(1, intBuffer);
        vertexArrayID = intBuffer.get(0);

        gl.glBindVertexArray(vertexArrayID);

        FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(vertexData, 0);
        FloatBuffer vertexColorBuffer = Buffers.newDirectFloatBuffer(colorData, 0);
        FloatBuffer vertexNormalBuffer = Buffers.newDirectFloatBuffer(normalData, 0);
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
        normalBufferID = intBuffer.get(0);
        gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, normalBufferID);
        gl.glBufferData(GL4.GL_ARRAY_BUFFER, normalData.length * Float.BYTES, vertexNormalBuffer, GL4.GL_STATIC_DRAW);
        gl.glEnableVertexAttribArray(2);
        gl.glVertexAttribPointer(2, 3, GL4.GL_FLOAT, false, 0, 0);

        intBuffer.rewind();
        gl.glGenBuffers(1, intBuffer);
        vertexIndexBufferID = intBuffer.get(0);
        gl.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, vertexIndexBufferID);
        gl.glBufferData(GL4.GL_ELEMENT_ARRAY_BUFFER, indices.length * Integer.BYTES, vertexIndexBuffer, GL4.GL_STATIC_DRAW);

        gl.glEnable(GL4.GL_DEPTH_TEST);
        int error = gl.glGetError();

        System.out.println("Init: error = " + error);
    }

    public void destroy(GL4 gl) {
        IntBuffer buffer = Buffers.newDirectIntBuffer(3);
        buffer.put(vertexBufferID);
        buffer.put(colorBufferID);
        buffer.put(normalBufferID);
        buffer.put(vertexIndexBufferID);
        buffer.rewind();
        gl.glDeleteBuffers(4, buffer);

        buffer.rewind();
        buffer.put(vertexArrayID);
        buffer.rewind();
        gl.glDeleteVertexArrays(1, buffer);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        
        GL4 gl = drawable.getGL().getGL4();

        // Obrisi bafer za boje i bafer za dubine
        gl.glClearColor(0, 0, 0, 0.0f);

        gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_STENCIL_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);

        gl.glUseProgram(neckFaceShader.getProgramObjectID());

        int transformLoc = neckFaceShader.GetShaderProgram().getUniformLocation("MVPTransform");
        mainCamera.GetViewProjection().get(transformArray);
        gl.glUniformMatrix4fv(transformLoc, 1, false, transformArray, 0);

        int rotateLoc = neckFaceShader.GetShaderProgram().getUniformLocation("Rotate");
        transformHead.get(transformArray);
        gl.glUniformMatrix4fv(rotateLoc, 1, false, transformArray, 0);

        int MVtransformLoc = neckFaceShader.GetShaderProgram().getUniformLocation("MVTransform");
        mainCamera.GetView().get(transformArray);
        gl.glUniformMatrix4fv(MVtransformLoc, 1, false, transformArray, 0);

        int normalTransformLoc = neckFaceShader.GetShaderProgram().getUniformLocation("NormalTransform");
        Matrix4f MVtransform = mainCamera.GetView();
        MVtransform.get3x3(normalTransform);
        normalTransform = normalTransform.invert().transpose();
        MVtransform = new Matrix4f(normalTransform);
        MVtransform.get(normalTransformArray);
        gl.glUniformMatrix4fv(normalTransformLoc, 1, false, normalTransformArray, 0);

        int LightPositionLoc = neckFaceShader.GetShaderProgram().getUniformLocation("LightPosition");
        lightPosArray[0] = mainLight.getPosition().x;
        lightPosArray[1] = mainLight.getPosition().y;
        lightPosArray[2] = mainLight.getPosition().z;
        gl.glUniform3fv(LightPositionLoc, 1, lightPosArray, 0);

        int electrodesValuesArrayLoc = neckFaceShader.GetShaderProgram().getUniformLocation("ElectrodesValues");
        float[] electrodesValuesArray = new float[electrodes_values.get(electrodes_base + electrodes_idx).size()];
        int index = 0;
        for (final Float value : electrodes_values.get(electrodes_base + electrodes_idx)) {
            electrodesValuesArray[index++] = value;
        }
        gl.glUniform1fv(electrodesValuesArrayLoc, electrodesValuesArray.length, electrodesValuesArray, 0);

        int electrodesNumberLoc = neckFaceShader.GetShaderProgram().getUniformLocation("ElectrodesNumber");
        gl.glUniform1i(electrodesNumberLoc, electrodesValuesArray.length);

        int electrodesArrayLoc = neckFaceShader.GetShaderProgram().getUniformLocation("Electrodes");
        float[] electrodesArray = new float[electrodes.size()];
        index = 0;
        for (final Float value : electrodes) {
            electrodesArray[index++] = value;
        }
        gl.glUniform3fv(electrodesArrayLoc, electrodesArray.length / 3, electrodesArray, 0);

        int maxAngleLoc = neckFaceShader.GetShaderProgram().getUniformLocation("MaxAngle");
        gl.glUniform1f(maxAngleLoc, maxAngle);

        gl.glBindVertexArray(vertexArrayID);
        gl.glUseProgram(neckFaceShader.getProgramObjectID());
        gl.glDrawElements(GL4.GL_TRIANGLES, num_faces, GL4.GL_UNSIGNED_INT, 0);
        gl.glBindVertexArray(0);

        
        gl.glUseProgram(simpleShader.getProgramObjectID());
        
        
        GL2 gl2 = drawable.getGL().getGL2();
        gl2.glColor3f(1f,1f,1f);
        gl2.glBegin(GL2.GL_POLYGON);
        gl2.glVertex2f(-0.90f, -0.90f);
        gl2.glVertex2f(-0.80f, -0.90f);
        gl2.glVertex2f(-0.80f, -0.20f);
        gl2.glVertex2f(-0.90f, -0.20f);
        gl2.glEnd();
        
        electrodes_idx = (electrodes_idx + 1) % frequency;
        if (electrodes_base + electrodes_idx >= electrodes_values.size()){
            electrodes_idx = 0;
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
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

        //      mainCamera.moveZ(newx - draggx);
        ugaox += newx - draggx;
        ugaoy += newy - draggy;
        draggx = newx;
        draggy = newy;

        transformHead = new Matrix4f().rotate((float) Math.toRadians(ugaoy), 1, 0, 0)
                .rotate((float) Math.toRadians(ugaox), 0, 1, 0);
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
        float r = e.getRotation()[1];
        if (r == 0) {
            return;
        }
        mainCamera.moveZ(-r * 3);
    }

//    @Override
    public void keyPressed(KeyEvent ke) {

        if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
            if (electrodes_base - frequency >= 0) {
                electrodes_base -= frequency;
                electrodes_idx = 0;
            }
        }
        if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (electrodes_base + 2*frequency < electrodes_values.size()) {
                electrodes_base += frequency;
                electrodes_idx = 0;
            } 
        }
        if (ke.getKeyCode() == KeyEvent.VK_UP) {
            if (maxAngle < 90) {
                maxAngle++;
            }
        }
        if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
            if (maxAngle > 2) {
                maxAngle--;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
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
            frequency = (int)Float.parseFloat(br.readLine().split(" ")[0]);
            System.out.println("Freq " + frequency);
            
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
                    electrodes.add(v.x);
                    electrodes.add(v.y);
                    electrodes.add(v.z);
                    //System.out.println(split[2] + " " + split[3]);
                    //System.out.println(v.x + " " + v.y + " " + v.z);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HeadImportTest.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(electrodes.size() + " ");
        System.out.println(electrodes_values.get(0).size() + " ");
        new HeadImportTest();
    }
}
