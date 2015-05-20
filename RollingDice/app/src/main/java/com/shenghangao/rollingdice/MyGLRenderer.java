package com.shenghangao.rollingdice;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import org.apache.http.util.EncodingUtils;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Context _context;

    private static final String TAG = "MyGLRenderer";
    private Rectangle mRectTop, mRectBottom, mRectLeft, mRectRight, mRectFront, mRectBack;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];
    private float[] mRotationMatrix1 = new float[16];
    private float[] mRotationMatrix2 = new float[16];
    private float[] mRotationMatrix3 = new float[16];
    private float[] mRotationMatrix12 = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private float [] tmpMatrix = new float[16];
    private final float halfA = 0.5f;

    public volatile int turn1, turn2, turn3;
    public volatile int mode = 1;
    private int NumOfSur = 37787;
    private int NumOfVer = 20144;

    public float XSum = 0;
    public float YSum = 0;
    public float ZSum = 0;
    public float XMin = -100;
    public float XMax = 100;
    public float YMin = -100;
    public float YMax = 100;
    public float ZMin = -100;
    public float ZMax = 100;
    public int VertexCount = 0;

    private String string;
    private String[] lines;
    public ArrayList<Vertex> vertexes = new ArrayList();

    public float [] AllCoords = new float[3*3*NumOfSur];
    public float [] AllColour = new float[4*3*NumOfSur];

    private TriangleC m;
    private int CoordCount;
    private int ColourCount;

    private final float rectTexCoords1[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, (float)(1.0/6),
            1.0f, (float)(1.0/6)
    };

    private final float rectTexCoords2[] = {
            0.0f, (float)(1.0/6),
            1.0f, (float)(1.0/6),
            0.0f, (float)(2.0/6),
            1.0f, (float)(2.0/6)
    };

    private final float rectTexCoords3[] = {
            0.0f, (float)(2.0/6),
            1.0f, (float)(2.0/6),
            0.0f, (float)(3.0/6),
            1.0f, (float)(3.0/6)
    };

    private final float rectTexCoords4[] = {
            0.0f, (float)(3.0/6),
            1.0f, (float)(3.0/6),
            0.0f, (float)(4.0/6),
            1.0f, (float)(4.0/6)
    };

    private final float rectTexCoords5[] = {
            0.0f, (float)(4.0/6),
            1.0f, (float)(4.0/6),
            0.0f, (float)(5.0/6),
            1.0f, (float)(5.0/6)
    };

    private final float rectTexCoords6[] = {
            0.0f, (float)(5.0/6),
            1.0f, (float)(5.0/6),
            0.0f, 1.0f,
            1.0f, 1.0f
    };

    private float topCoords[] = {
            -halfA, halfA, -halfA,
            -halfA, halfA, halfA,
            halfA, halfA, halfA,
            halfA, halfA, -halfA
    };

    private float bottomCoords[] = {
            -halfA, -halfA, halfA,
            -halfA, -halfA, -halfA,
            halfA, -halfA, -halfA,
            halfA, -halfA, halfA
    };

    private float leftCoords[] = {
            -halfA, halfA, -halfA,
            -halfA, -halfA, -halfA,
            -halfA, -halfA, halfA,
            -halfA, halfA, halfA
    };

    private float rightCoords[] = {
            halfA, halfA, halfA,
            halfA, -halfA, halfA,
            halfA, -halfA, -halfA,
            halfA, halfA, -halfA
    };

    private float frontCoords[] = {
            -halfA, halfA, halfA,
            -halfA, -halfA, halfA,
            halfA, -halfA, halfA,
            halfA, halfA, halfA
    };

    private float backCoords[] = {
            halfA, halfA, -halfA,
            halfA, -halfA, -halfA,
            -halfA, -halfA, -halfA,
            -halfA, halfA, -halfA
    };

    public MyGLRenderer(Context context) {
        _context = context;
    }

    // Declare as volatile because we are updating it from another thread
    public volatile float mAngle;

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glClearDepthf(1.0f);

        if (mode == 1) {
            mRectTop = new Rectangle(_context, mMVPMatrix, topCoords, rectTexCoords1);
            mRectBottom = new Rectangle(_context, mMVPMatrix, bottomCoords, rectTexCoords6);
            mRectLeft = new Rectangle(_context, mMVPMatrix, leftCoords, rectTexCoords5);
            mRectRight = new Rectangle(_context, mMVPMatrix, rightCoords, rectTexCoords2);
            mRectBack = new Rectangle(_context, mMVPMatrix, backCoords, rectTexCoords4);
            mRectFront = new Rectangle(_context, mMVPMatrix, frontCoords, rectTexCoords3);
        }

        if (mode == 2) {
            CoordCount = ColourCount = 0;

            FileIn();

            lines = string.split("\r\n");
            string = null;
            for (int i=0; i<NumOfVer; ++i)
            {
                Vertex v = new Vertex(lines[i+12]);
                vertexes.add(v);
            }

            for (int i=0; i<NumOfSur; ++i)
            {
                setVertex(lines[i+12+NumOfVer]);
            }

            Log.d("Debug","CoordCount: " + CoordCount + " ColourCount:" + ColourCount);

            float XAvg = XSum/NumOfVer;
            float YAvg = YSum/NumOfVer;
            float ZAvg = ZSum/NumOfVer;
            Log.d("Debug","XAvg: "+ XAvg + " YAvg: " + YAvg + " ZAvg: " + ZAvg);
            Log.d("Debug","XMin: "+ XMin + " XMax: " + XMax);
            Log.d("Debug","YMin: "+ YMin + " YMax: " + YMax);
            Log.d("Debug","ZMin: "+ ZMin + " ZMax: " + ZMax);
            Log.d("Debug","VertexCount: "+ VertexCount);

            lines = null;
            Log.d("Debug","lines = null; ran!");
            vertexes.clear();
            Log.d("Debug","vertexes.clear(); ran!");

            m = new TriangleC(mMVPMatrix, AllCoords, AllColour);
        }
    }

    public void onDrawFrame(GL10 unused) {

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        if (mode == 1) {
            // Set the camera position (View matrix)
            Matrix.setLookAtM(mVMatrix, 0, 3, 3, 3, 0f, 0f, 0f, 0.0f, 1.0f, 0.0f);
            //Matrix.setLookAtM(mVMatrix, 0, 4, 0, 0, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            //Matrix.setLookAtM(mVMatrix, 0, 0, 4, 0, 0f, 0f, 0f, -1.0f, 0.0f, -1.0f);
            // Matrix.setLookAtM(mVMatrix, 0, 0, 0, 4, 0f, 0f, 0f, 0.0f, 1.0f, 0.0f);

            // Calculate the projection and view transformation
            Matrix.multiplyMM(tmpMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

            Matrix.setRotateM(mRotationMatrix1, 0, 90 * turn1, 1.0f, 0.0f, 0.0f);
            Matrix.setRotateM(mRotationMatrix2, 0, 90 * turn2, 0.0f, 1.0f, 0.0f);
            Matrix.setRotateM(mRotationMatrix3, 0, 90 * turn3, 0.0f, 0.0f, 1.0f);

            Matrix.multiplyMM(mRotationMatrix12, 0, mRotationMatrix1, 0, mRotationMatrix2, 0);
            Matrix.multiplyMM(mRotationMatrix, 0, mRotationMatrix12, 0, mRotationMatrix3, 0);

            // Combine the rotation matrix with the projection and camera view
            Matrix.multiplyMM(mMVPMatrix, 0, tmpMatrix, 0, mRotationMatrix, 0);

            mRectTop.draw();
            mRectBottom.draw();
            mRectLeft.draw();
            mRectRight.draw();
            mRectFront.draw();
            mRectBack.draw();
        }

        if (mode == 2) {
            Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0.0f, 1.0f, 0.0f);

            // Calculate the projection and view transformation
            Matrix.multiplyMM(tmpMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

            // Create a rotation for the triangle
            long time = SystemClock.uptimeMillis() % 4000L;

            float angle = 0.03f * ((int) time);
            Matrix.setRotateM(mRotationMatrix1, 0, angle, 0f, 1.0f, 0);
            Matrix.setRotateM(mRotationMatrix2, 0, 0, 0.0f, 0.0f, 1.0f);

            Matrix.multiplyMM(mRotationMatrix, 0, mRotationMatrix1, 0, mRotationMatrix2, 0);

            // Combine the rotation matrix with the projection and camera view
            Matrix.multiplyMM(mMVPMatrix, 0, tmpMatrix, 0, mRotationMatrix, 0);

            m.drawI();
        }
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        if (mode == 1)
        {
            Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        }

        if (mode == 2)
        {
            Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        }
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            //Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    public void FileIn() {
        try {
            InputStream in = _context.getResources().openRawResource(R.raw.mesh);

            int length = in.available();

            byte[] buffer = new byte[length];

            in.read(buffer);

            string = EncodingUtils.getString(buffer, "UTF-8");
            in.close();
            buffer = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class Vertex {
        public float [] coord = new float[3];
        public float [] colour = new float[3];
        Vertex(String string) {
            ++VertexCount;
            String [] tmp = string.split(" ");
            coord[0] = Float.parseFloat(tmp[0]);
            coord[1] = Float.parseFloat(tmp[1]);
            coord[2] = Float.parseFloat(tmp[2]);
            coord[0] = (float)((coord[0]+1.9356787204742432)/8.37401032448);
            coord[1] = (float)((coord[1]+0.3374498188495636)/8.37401032448);
            coord[2] = (float)((coord[2]+16.715673446655273)/8.37401032448);
            XSum += coord[0];
            YSum += coord[1];
            ZSum += coord[2];

        if (VertexCount == 1)
        {
            XMin = XMax = coord[0];
            YMin = YMax = coord[1];
            ZMin = ZMax = coord[2];
        }
        else
        {
            if (coord[0] < XMin) XMin = coord[0];
            if (coord[0] > XMax) XMax = coord[0];
            if (coord[1] < YMin) YMin = coord[1];
            if (coord[1] > YMax) YMax = coord[1];
            if (coord[2] < ZMin) ZMin = coord[2];
            if (coord[2] > ZMax) ZMax = coord[2];
        }
            for (int i=0; i<3; ++i)
            {
                colour[i] = Float.parseFloat(tmp[i+3])/255;
            }
        }
    }

    private void setVertex(String string)
    {
        String [] tmp = string.split(" ");
        int [] CorNum = new int[3];
        for (int i=0; i<3; ++i)
        {
            CorNum[i] = Integer.parseInt(tmp[i+1]);
        }

        for (int i=0; i<3; ++i)
        {
            for (int j=0; j<3; ++j)
            {
                AllCoords[CoordCount++] = vertexes.get(CorNum[i]).coord[j];
            }
        }

        for (int i=0; i<3; ++i)
        {
            for (int j=0; j<3; ++j)
            {
                AllColour[ColourCount++] = vertexes.get(CorNum[i]).colour[j];
            }
            AllColour[ColourCount++] = 1.0f;
        }
    }
}

class Triangle {

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +

                    "attribute vec4 vPosition;" +
                    "attribute vec4 a_color;" +
                    "attribute vec2 tCoordinate;" +
                    "varying vec2 v_tCoordinate;" +
                    "varying vec4 v_Color;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "	v_tCoordinate = tCoordinate;" +
                    "	v_Color = a_color;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 v_Color;" +
                    "varying vec2 v_tCoordinate;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    // texture2D() is a build-in function to fetch from the texture map
                    "	vec4 texColor = texture2D(s_texture, v_tCoordinate); " +
                    "  gl_FragColor = texColor;" +
                    "}";

    private final FloatBuffer vertexBuffer, texCoordBuffer, colorBuffer;
    private final int mProgram;
    private int mPositionHandle, mTexCoordHandle;
    private int mColorHandle, mTextureUniformHandle;
    private int mMVPMatrixHandle;
    private int mTextureDataHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    public float [] triangleCoords;

    public final int vertexCount;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex (should be 4 bytes per float!?)

    //===================================
    static final int COORDS_PER_TEX = 2;
    public float [] texCoord;

    private final int texCoordStride = COORDS_PER_TEX * 4; // 4 bytes per float

    //===================================
    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    // Set another color
    static final int COLORB_PER_VER = 4;
    static float colorBlend[] = {
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    };
    //private final int colorBlendCount = colorBlend.length / COLORB_PER_VER;
    private final int colorBlendStride = COLORB_PER_VER * 4;

    //===================================
    public Triangle(Context context, float [] triCoords, float [] textureCoords) {

        triangleCoords = triCoords;
        texCoord = textureCoords;
        vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
        //===================================
        // shape coordinate
        //===================================
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        //===================================
        // texture coordinate
        //===================================
        // initialize texture coord byte buffer for texture coordinates
        ByteBuffer texbb = ByteBuffer.allocateDirect(
                texCoord.length * 4);
        // use the device hardware's native byte order
        texbb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        texCoordBuffer = texbb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        texCoordBuffer.put(texCoord);
        // set the buffer to read the first coordinate
        texCoordBuffer.position(0);

        //===================================
        // color
        //===================================
        ByteBuffer cbb = ByteBuffer.allocateDirect(
                colorBlend.length * 4);
        cbb.order(ByteOrder.nativeOrder());

        colorBuffer = cbb.asFloatBuffer();
        colorBuffer.put(colorBlend);
        colorBuffer.position(0);

        //===================================
        // loading an image into texture
        //===================================
        mTextureDataHandle = loadTexture(context, R.drawable.dice);

        //===================================
        // shader program
        //===================================
        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    public static int loadTexture(final Context context, final int resourceId)
    {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // setting vertex color
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
        //Log.i("chuu", "Error: mColorHandle = "+mColorHandle);
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, COLORB_PER_VER,
                GLES20.GL_FLOAT, false,
                colorBlendStride, colorBuffer);
        MyGLRenderer.checkGlError("glVertexAttribPointer...color");

        // setting texture coordinate to vertex shader
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "tCoordinate");
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle, COORDS_PER_TEX,
                GLES20.GL_FLOAT, false,
                texCoordStride, texCoordBuffer);
        MyGLRenderer.checkGlError("glVertexAttribPointer...texCoord");

        // get handle to fragment shader's vColor member
        //mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        //GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // texture
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}

class Rectangle
{
    private Triangle tr1, tr2;
    public float [] rectangleCoords, texCoords;
    private float [] mMVPMatrix;

    public Rectangle(Context context, float [] MVPM,float [] rectCoords, float [] textureCoords)
    {
        mMVPMatrix = MVPM;
        rectangleCoords = rectCoords;
        texCoords = textureCoords;
        float [] tr1Coords = { rectCoords[0], rectCoords[1], rectCoords[2],
                rectCoords[3], rectCoords[4], rectCoords[5],
                rectCoords[6], rectCoords[7], rectCoords[8]
        };
        float [] tr2Coords = { rectCoords[0], rectCoords[1], rectCoords[2],
                rectCoords[6], rectCoords[7], rectCoords[8],
                rectCoords[9], rectCoords[10], rectCoords[11]
        };
        float [] tr1texCoords = { textureCoords[0], textureCoords[1],
                textureCoords[4], textureCoords[5],
                textureCoords[6], textureCoords[7]
        };
        float [] tr2texCoords = { textureCoords[0], textureCoords[1],
                textureCoords[6], textureCoords[7],
                textureCoords[2], textureCoords[3]
        };

        tr1 = new Triangle(context, tr1Coords, tr1texCoords);
        tr2 = new Triangle(context, tr2Coords, tr2texCoords);


    }

    public void draw()
    {
        tr1.draw(mMVPMatrix);
        tr2.draw(mMVPMatrix);
    }
}

class TriangleC {
    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +

                    "attribute vec4 vPosition;" +
                    "attribute vec4 a_color;" +
                    "attribute vec2 tCoordinate;" +
                    "varying vec2 v_tCoordinate;" +
                    "varying vec4 v_Color;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "	v_tCoordinate = tCoordinate;" +
                    "	v_Color = a_color;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 v_Color;" +
                    "varying vec2 v_tCoordinate;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    // texture2D() is a build-in function to fetch from the texture map
                    "	vec4 texColor = texture2D(s_texture, v_tCoordinate); " +
                    "  gl_FragColor = v_Color;" +
                    "}";

    private final FloatBuffer vertexBuffer, colorBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private float[] mMVPMatrix;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    public float[] triangleCoords;

    public final int vertexCount;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex (should be 4 bytes per float!?)

    // Set another color
    static final int COLORB_PER_VER = 4;
    static float colorBlend[];
    //private final int colorBlendCount = colorBlend.length / COLORB_PER_VER;
    private final int colorBlendStride = COLORB_PER_VER * 4;

    //===================================
    public TriangleC(float[] MVPM, float [] triCoords, float [] colorB) {
        mMVPMatrix = MVPM;
        triangleCoords = triCoords;
        colorBlend = colorB;
        MVPM = null;
        vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
        //===================================
        // shape coordinate
        //===================================
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);
        bb = null;

        //===================================
        // color
        //===================================
        ByteBuffer cbb = ByteBuffer.allocateDirect(
                colorBlend.length * 4);
        cbb.order(ByteOrder.nativeOrder());

        colorBuffer = cbb.asFloatBuffer();
        colorBuffer.put(colorBlend);
        colorBuffer.position(0);
        cbb = null;

        //===================================
        // shader program
        //===================================
        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

    }

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // setting vertex color
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, COLORB_PER_VER,
                GLES20.GL_FLOAT, false,
                colorBlendStride, colorBuffer);
        MyGLRenderer.checkGlError("glVertexAttribPointer...color");

        MyGLRenderer.checkGlError("glVertexAttribPointer...texCoord");

        // get handle to fragment shader's vColor member
        //mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        //GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public void drawI() {
        draw(mMVPMatrix);
    }
}
