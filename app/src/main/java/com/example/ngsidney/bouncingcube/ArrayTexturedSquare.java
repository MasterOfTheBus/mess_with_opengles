package com.example.ngsidney.bouncingcube;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by ngsidney on 6/19/15.
 */
public class ArrayTexturedSquare {
    final float side = 3.0f;

    public final int numInstances = 10000;

    private FloatBuffer vertexBuffer;
    private ShortBuffer orderBuffer;
    private FloatBuffer texCoordBuffer;
    private FloatBuffer vertex3dBuffer;
    private FloatBuffer tex3dCoordBuffer;
    private FloatBuffer normal3dBuffer;
    private FloatBuffer normal2dBuffer;
    private final int mProgram;

    // VBO handles
    private final int mVertexVBO;
    private final int mPosVBO;
    private final int mSizeVBO;
    private final int mColorVBO;
    private final int mTexCoordVBO;
    private final int mVertex3dVBO;
    private final int mTex3dCoordVBO;
    private final int mNormal3dVBO;
    private final int mNormal2dVBO;

    private final int mTextureDataHandle;
    private int mLightPosHandle;

    // Attribute locations
    private int vertexAttr;
    private int posAttr;
//    private final int VERTEX_LOC = 0;
//    private final int POSITION_LOC = 1;

    // vertex shader for rendering vertices
    private final String vertexShaderCode =
                    "#version 300 es\n" +
                    "in vec4 a_vertex;" +
                    "in vec4 a_position;" +
                            "in vec4 a_size;" +
                            "in vec4 a_color;" +
                            "in vec2 a_texCoord;" +
                            "in vec3 a_normal;" +

                    "uniform mat4 a_mvpMatrix;" +
                            "uniform mat4 u_mvMatrix;" +
                            "uniform float u_renderMode;" +

                            "out vec4 v_color;" +
                            "out vec2 v_texCoord;" +
                            "out vec3 v_normal;" +
                            "out vec3 v_position;" +
                            "out float v_face;" +

                    "void main() {  \n" +
                            "  v_color = a_color;" +
                            "  v_texCoord = a_texCoord;" +
                            "  v_face = a_vertex.w;" +

                            "  vec3 vertex_pos = vec3(a_vertex.x * a_size.x, a_vertex.y * a_size.y, a_vertex.z * a_size.z) + a_position.xyz;" +

//                            "  if (u_renderMode == 1.0f) {" +
                            "    v_position = vec3(u_mvMatrix * vec4(vertex_pos, 1.0f));" +
                            "    v_normal = vec3(u_mvMatrix * vec4(a_normal, 0.0f));" +
//                            "  }" +

                            "  gl_Position = a_mvpMatrix * vec4(vertex_pos, 1.0f);" + // pass only the positions
                    "}";

    // Use to access and set the view transformation
    private int mMVPMatrixHandle;
    private int mMVMatrixHandle;

    // Fragment shader for rendering the face of the shape with colors and textures
    private final String fragmentShaderCode =
            "#version 300 es\n" +
            "precision mediump float;" +
                    "in vec4 v_color;" +
                    "in vec2 v_texCoord;" +
                    "in vec3 v_normal;" +
                    "in vec3 v_position;" +
                    "in float v_face;" +

                    "uniform sampler2DArray u_texture;" +
                    "uniform float u_renderMode;" +

                    "out vec4 color;" +

                    "void main() {" +
                    "  vec3 lightColor = vec3(1.0f, 1.0f, 1.0f);" +
                    "  vec3 lightDirection = vec3(0.0f, 0.0f, 1.0f);" +
                    "  float ambientLightIntensity = 0.7f;" +

                    "  float diffuse = max(dot(v_normal, lightDirection), 0.0f) + ambientLightIntensity;" +

                    "  if (u_renderMode == 0.0f) {" +
                    "    diffuse = 1.0f;" +
                    "  }" +

                    "  vec3 texCoord = vec3(v_texCoord, 2);" +
                    "  int face = int(v_face);" +
//                    "  gl_FragColor = vec4(v_color.xyz * diffuse * lightColor, v_color.w) * texture2D(u_texture[3], v_texCoord);" +
                    "  color = vec4(diffuse * lightColor, v_color.w) * texture(u_texture, texCoord);" +

                    "}";

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float cubeCoords[] = {   // in counterclockwise order:
            // Front face -- Square
//            -1.0f, 1.0f, 1.0f,
//            -1.0f, -1.0f, 1.0f,
//            1.0f, 1.0f, 1.0f,
//            -1.0f, -1.0f, 1.0f,
//            1.0f, -1.0f, 1.0f,
//            1.0f, 1.0f, 1.0f
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f

    };


    static float texCoords[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };

    static float normal2d[] = {
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f
    };

    short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    static float cube3dCoords[] = {
// Front face
            -1.0f, 1.0f, 1.0f, 0,
            -1.0f, -1.0f, 1.0f, 0,
            1.0f, 1.0f, 1.0f, 0,
            -1.0f, -1.0f, 1.0f, 0,
            1.0f, -1.0f, 1.0f, 0,
            1.0f, 1.0f, 1.0f, 0,

            // Right face
            1.0f, 1.0f, 1.0f, 1,
            1.0f, -1.0f, 1.0f, 1,
            1.0f, 1.0f, -1.0f, 1,
            1.0f, -1.0f, 1.0f, 1,
            1.0f, -1.0f, -1.0f, 1,
            1.0f, 1.0f, -1.0f, 1,

            // Back face
            1.0f, 1.0f, -1.0f, 2,
            1.0f, -1.0f, -1.0f, 2,
            -1.0f, 1.0f, -1.0f, 2,
            1.0f, -1.0f, -1.0f, 2,
            -1.0f, -1.0f, -1.0f, 2,
            -1.0f, 1.0f, -1.0f, 2,

            // Left face
            -1.0f, 1.0f, -1.0f, 3,
            -1.0f, -1.0f, -1.0f, 3,
            -1.0f, 1.0f, 1.0f, 3,
            -1.0f, -1.0f, -1.0f, 3,
            -1.0f, -1.0f, 1.0f, 3,
            -1.0f, 1.0f, 1.0f, 3,

            // Top face
            -1.0f, 1.0f, -1.0f, 4,
            -1.0f, 1.0f, 1.0f, 4,
            1.0f, 1.0f, -1.0f, 4,
            -1.0f, 1.0f, 1.0f, 4,
            1.0f, 1.0f, 1.0f, 4,
            1.0f, 1.0f, -1.0f, 4,

            // Bottom face
            1.0f, -1.0f, -1.0f, 5,
            1.0f, -1.0f, 1.0f, 5,
            -1.0f, -1.0f, -1.0f, 5,
            1.0f, -1.0f, 1.0f, 5,
            -1.0f, -1.0f, 1.0f, 5,
            -1.0f, -1.0f, -1.0f, 5

    };

    static float texCubeCoords[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };

    static float normal3d[] = {
            // Front face
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,

            // Right face
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,

            // Back face
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,

            // Left face
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,

            // Top face
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,

            // Bottom face
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f

    };

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    MyGLSurfaceView surfaceView;

    public ArrayTexturedSquare(MyGLSurfaceView surfaceView) {
        this.surfaceView = surfaceView;

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(cubeCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubeCoords);
        vertexBuffer.position(0);

        // draw list buffer
        ByteBuffer bb2 = ByteBuffer.allocateDirect(drawOrder.length * 2);
        bb2.order(ByteOrder.nativeOrder());
        orderBuffer = bb2.asShortBuffer();
        orderBuffer.put(drawOrder);
        orderBuffer.position(0);

        // texture buffer
        ByteBuffer bb3 = ByteBuffer.allocateDirect(texCoords.length * 4);
        bb3.order(ByteOrder.nativeOrder());
        texCoordBuffer = bb3.asFloatBuffer();
        texCoordBuffer.put(texCoords);
        texCoordBuffer.position(0);

        // the 3d vertices
        ByteBuffer bb4 = ByteBuffer.allocateDirect(cube3dCoords.length * 4);
        bb4.order(ByteOrder.nativeOrder());
        vertex3dBuffer = bb4.asFloatBuffer();
        vertex3dBuffer.put(cube3dCoords);
        vertex3dBuffer.position(0);

        // the 3d texture coords
        ByteBuffer bb5 = ByteBuffer.allocateDirect(texCubeCoords.length * 4);
        bb5.order(ByteOrder.nativeOrder());
        tex3dCoordBuffer = bb5.asFloatBuffer();
        tex3dCoordBuffer.put(texCubeCoords);
        tex3dCoordBuffer.position(0);

        // the 3d normals
        ByteBuffer bb6 = ByteBuffer.allocateDirect(normal3d.length * 4);
        bb6.order(ByteOrder.nativeOrder());
        normal3dBuffer = bb6.asFloatBuffer();
        normal3dBuffer.put(normal3d);
        normal3dBuffer.position(0);

        // the 2d normals
        ByteBuffer bb7 = ByteBuffer.allocateDirect(normal2d.length * 4);
        bb7.order(ByteOrder.nativeOrder());
        normal2dBuffer = bb6.asFloatBuffer();
        normal2dBuffer.put(normal2d);
        normal2dBuffer.position(0);

        // VBOs
        final int buffers[] = new int[9];
        GLES30.glGenBuffers(9, buffers, 0);

        // bind the vertex data - will never change, do a static draw
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, cubeCoords.length * 4,
                vertexBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // bind the positional data - the center of the square
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[1]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, numInstances * COORDS_PER_VERTEX * 4, null, GLES30.GL_DYNAMIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // bind the size data
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[2]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, numInstances * 3 * 4, null, GLES30.GL_DYNAMIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // bind the color data
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[3]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, numInstances * 4 * 4, null, GLES30.GL_DYNAMIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // bind the texcoord
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[4]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, texCoords.length * 4,
                texCoordBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // bind the 3d vertex data - will never change, do a static draw
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[5]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, cube3dCoords.length * 4,
                vertex3dBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[6]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, texCubeCoords.length * 4,
                tex3dCoordBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[7]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, normal3d.length * 4,
                normal3dBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[8]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, normal2d.length * 4,
                normal2dBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // save the handles to the VBOs
        mVertexVBO = buffers[0];
        mPosVBO = buffers[1];
        mSizeVBO = buffers[2];
        mColorVBO = buffers[3];
        mTexCoordVBO = buffers[4];
        mVertex3dVBO = buffers[5];
        mTex3dCoordVBO = buffers[6];
        mNormal3dVBO = buffers[7];
        mNormal2dVBO = buffers[8];

        // Run the shader code compiler for the GLSL spec
        int vertexShader = MyGLRenderer.loadShader(GLES30.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES30.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES30.glCreateProgram();

        // add the vertex shader to program
        GLES30.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES30.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES30.glLinkProgram(mProgram);

//        mTextureDataHandle = TextureHandler.loadTexture(surfaceView, R.drawable.scaly);
        int resources[] = {R.drawable.slate, R.drawable.rainbow, R.drawable.blue_squiggle,
                R.drawable.emerald, R.drawable.sand, R.drawable.scaly};
        mTextureDataHandle = generateTextureArray(resources);

    }

    int numTex = 6;
    int width = 1;
    int height = 1;
    public int generateTextureArray(int res[]) {
        int texArray[] = new int[1];
        int params[] = new int[2];

        GLES30.glGetTexParameteriv(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_IMMUTABLE_FORMAT, params, 0);
        if (params[0] == GLES30.GL_TRUE)
            Log.d("a", "immutable is gl true");
        else
            Log.d("a", "immutable is gl false");

        GLES30.glGetIntegerv(GLES30.GL_TEXTURE_BINDING_2D_ARRAY, params, 1);
        Log.d("a", "texture binding 2d array: " + params[1]);

        GLES30.glGenTextures(1, texArray, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D_ARRAY, texArray[0]);

        if (texArray[0] != 0) {

            GLES30.glGetIntegerv(GLES30.GL_TEXTURE_BINDING_2D_ARRAY, params, 1);
            Log.d("a", "texture binding 2d array after bind method: " + params[1]);

            GLES30.glGetTexParameteriv(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_IMMUTABLE_FORMAT, params, 0);
            if (params[0] == GLES30.GL_TRUE)
                Log.d("a", "immutable after binding is gl true");
            else
                Log.d("a", "immutable after binding is gl false");

            GLES30.glTexStorage3D(GLES30.GL_TEXTURE_2D_ARRAY, 1, GLES30.GL_RGBA8, width, height, res.length);
//            GLES30.glTexImage3D(GLES30.GL_TEXTURE_2D_ARRAY, 1, GLES30.GL_RGBA, 1, 1, res.length, 0, GLES30.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, 0);

            int error = GLES30.glGetError();
            Log.d("a", "error: " + error);

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            for (int i = 0; i < res.length; i++) {
                Bitmap bitmap = BitmapFactory.decodeResource(surfaceView.getResources(), res[i], options);
                ByteBuffer texBuffer = ByteBuffer.allocateDirect(bitmap.getHeight() * bitmap.getWidth() * 4);
                bitmap.copyPixelsToBuffer(texBuffer);
                texBuffer.position(0);

                GLES30.glTexSubImage3D(GLES30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, width, height, 1,
                        GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, texBuffer);

                bitmap.recycle();
            }

            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        } else {
            throw new RuntimeException("Error loading texture");
        }

        return texArray[0];
    }

    // each shape should have its own draw method because it would be different for each shape
    private int mPositionHandle;
    private int mColorHandle;

    private final int vertexCount = cubeCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void draw(float[] vpMatrix, float[] mvMatrix, float[] positions, float[] sizes,
                     float[] colors, boolean render3d) {
        // Add program to OpenGL ES environment
        GLES30.glUseProgram(mProgram);

        // get the handles to the attribute
        vertexAttr = GLES30.glGetAttribLocation(mProgram, "a_vertex");
        posAttr = GLES30.glGetAttribLocation(mProgram, "a_position");
        int sizeAttr = GLES30.glGetAttribLocation(mProgram, "a_size");
        int coloAttr = GLES30.glGetAttribLocation(mProgram, "a_color");
        int texAttr = GLES30.glGetAttribLocation(mProgram, "a_texCoord");
        int normalAttr = GLES30.glGetAttribLocation(mProgram, "a_normal");

        // load vertices
        if (render3d) {
            GLES30.glEnableVertexAttribArray(vertexAttr);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertex3dVBO);
            GLES30.glVertexAttribPointer(vertexAttr, 4,
                    GLES30.GL_FLOAT, false,
                    4 * 4, 0);
            GLES30.glVertexAttribDivisor(vertexAttr, 0);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

            GLES30.glEnableVertexAttribArray(texAttr);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mTex3dCoordVBO);
            GLES30.glVertexAttribPointer(texAttr, 2,
                    GLES30.GL_FLOAT, false,
                    2 * 4, 0);
            GLES30.glVertexAttribDivisor(texAttr, 0);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

            GLES30.glEnableVertexAttribArray(normalAttr);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mNormal3dVBO);
            GLES30.glVertexAttribPointer(normalAttr, 3,
                    GLES30.GL_FLOAT, false,
                    3 * 4, 0);
            GLES30.glVertexAttribDivisor(normalAttr, 0);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        } else {
            GLES30.glEnableVertexAttribArray(vertexAttr);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexVBO);
            GLES30.glVertexAttribPointer(vertexAttr, COORDS_PER_VERTEX,
                    GLES30.GL_FLOAT, false,
                    vertexStride, 0);
            GLES30.glVertexAttribDivisor(vertexAttr, 0);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

            GLES30.glEnableVertexAttribArray(texAttr);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mTexCoordVBO);
            GLES30.glVertexAttribPointer(texAttr, 2,
                    GLES30.GL_FLOAT, false,
                    2 * 4, 0);
            GLES30.glVertexAttribDivisor(texAttr, 0);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

            GLES30.glEnableVertexAttribArray(normalAttr);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mNormal2dVBO);
            GLES30.glVertexAttribPointer(normalAttr, 3,
                    GLES30.GL_FLOAT, false,
                    3 * 4, 0);
            GLES30.glVertexAttribDivisor(normalAttr, 0);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        }


        ByteBuffer bb = ByteBuffer.allocateDirect(positions.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(positions);
        fb.position(0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mPosVBO);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, positions.length * 4, null, GLES30.GL_DYNAMIC_DRAW);
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, positions.length * 4, fb);

        GLES30.glEnableVertexAttribArray(posAttr);
        GLES30.glVertexAttribPointer(posAttr, COORDS_PER_VERTEX, GLES30.GL_FLOAT,
                false, vertexStride, 0);
        GLES30.glVertexAttribDivisor(posAttr, 1);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);


        ByteBuffer bb2 = ByteBuffer.allocateDirect(sizes.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        FloatBuffer sb = bb2.asFloatBuffer();
        sb.put(sizes);
        sb.position(0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mSizeVBO);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, sizes.length * 4, null, GLES30.GL_DYNAMIC_DRAW);
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, sizes.length * 4, sb);

        GLES30.glEnableVertexAttribArray(sizeAttr);
        GLES30.glVertexAttribPointer(sizeAttr, 3, GLES30.GL_FLOAT,
                false, 3 * 4, 0);
        GLES30.glVertexAttribDivisor(sizeAttr, 1);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);


        ByteBuffer bb3 = ByteBuffer.allocateDirect(colors.length * 4);
        bb3.order(ByteOrder.nativeOrder());
        FloatBuffer cb = bb3.asFloatBuffer();
        cb.put(colors);
        cb.position(0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mColorVBO);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, colors.length * 4, null, GLES30.GL_DYNAMIC_DRAW);
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, colors.length * 4, cb);

        GLES30.glEnableVertexAttribArray(coloAttr);
        GLES30.glVertexAttribPointer(coloAttr, 4, GLES30.GL_FLOAT,
                false, 4 * 4, 0);
        GLES30.glVertexAttribDivisor(sizeAttr, 1);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);


        // get handle to fragment shader's uTexture member
        int mTextureUniformHandle = GLES30.glGetUniformLocation(mProgram, "u_texture");
        // Set the active texture unit to texture unit 0.
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D_ARRAY, mTextureDataHandle);
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES30.glUniform1i(mTextureUniformHandle, 0);


        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "a_mvpMatrix");
        // Pass the projection and view transformation to the shader
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, vpMatrix, 0);

        // get handle to shape's transformation matrix
        mMVMatrixHandle = GLES30.glGetUniformLocation(mProgram, "u_mvMatrix");
        // Pass the projection and view transformation to the shader
        GLES30.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);

//        int mRenderHandle = GLES30.glGetUniformLocation(mProgram, "u_renderMode");
//        GLES30.glUniform1ui(mRenderHandle, (render3d) ? 1 : 0);
        int mRenderHandle = GLES30.glGetUniformLocation(mProgram, "u_renderMode");
//        Log.d("asdf", "render mode: " + render3d + ((render3d) ? 1 : 0));
        GLES30.glUniform1f(mRenderHandle, ((render3d) ? 1.0f : 0.0f));


//        GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES, 0, 6, numInstances);
        if (render3d) {
            GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES, 0, 36, numInstances);
        } else {
            GLES30.glDrawElementsInstanced(GLES30.GL_TRIANGLES, drawOrder.length, GLES30.GL_UNSIGNED_SHORT, orderBuffer, numInstances);
        }

        GLES30.glDisableVertexAttribArray(mPosVBO);
        GLES30.glDisableVertexAttribArray(mVertexVBO);
    }
}
