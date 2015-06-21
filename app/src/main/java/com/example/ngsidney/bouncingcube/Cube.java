package com.example.ngsidney.bouncingcube;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Created by ngsidney on 6/1/15.
 *
 */
public class Cube {
    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" + // the model view projection matrix
                    "attribute vec4 vPosition;" + // the cube vertex coordinates
                    "attribute vec2 a_TexCoordinate;" + // texture coordinate data
                    "varying vec2 vTexCoordinate;" + // pass to fragment shader
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  vTexCoordinate = a_TexCoordinate;" +
                    "}";

    // Use to access and set the view transformation
    private int mMVPMatrixHandle;

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" + // input color
                    "uniform sampler2D uTexture;" + // input texture
                    "varying vec2 vTexCoordinate;" + // interpolated texture coordinate per fragment?
                    // "diffuse = diffuse * (1.0 / (1.0 + (0.10 * distance)));" // some fancy lighting
                    // "diffuse = diffuse + 0.3;"
                    "void main() {" +
                    // texture2D to read in value of the texture at the current coordinate
                    // multiply by color (and lighting) for final output
                    "  gl_FragColor = (vColor * texture2D(uTexture, vTexCoordinate));" +
                    "}";

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float cubeCoords[] = {

            // Front face
            -0.3f, 0.3f, 0.3f,
            -0.3f, -0.3f, 0.3f,
            0.3f, 0.3f, 0.3f,
            -0.3f, -0.3f, 0.3f,
            0.3f, -0.3f, 0.3f,
            0.3f, 0.3f, 0.3f,

            // Right face
            0.3f, 0.3f, 0.3f,
            0.3f, -0.3f, 0.3f,
            0.3f, 0.3f, -0.3f,
            0.3f, -0.3f, 0.3f,
            0.3f, -0.3f, -0.3f,
            0.3f, 0.3f, -0.3f,

            // Back face
            0.3f, 0.3f, -0.3f,
            0.3f, -0.3f, -0.3f,
            -0.3f, 0.3f, -0.3f,
            0.3f, -0.3f, -0.3f,
            -0.3f, -0.3f, -0.3f,
            -0.3f, 0.3f, -0.3f,

            // Left face
            -0.3f, 0.3f, -0.3f,
            -0.3f, -0.3f, -0.3f,
            -0.3f, 0.3f, 0.3f,
            -0.3f, -0.3f, -0.3f,
            -0.3f, -0.3f, 0.3f,
            -0.3f, 0.3f, 0.3f,

            // Top face
            -0.3f, 0.3f, -0.3f,
            -0.3f, 0.3f, 0.3f,
            0.3f, 0.3f, -0.3f,
            -0.3f, 0.3f, 0.3f,
            0.3f, 0.3f, 0.3f,
            0.3f, 0.3f, -0.3f,

            // Bottom face
            0.3f, -0.3f, -0.3f,
            0.3f, -0.3f, 0.3f,
            -0.3f, -0.3f, -0.3f,
            0.3f, -0.3f, 0.3f,
            -0.3f, -0.3f, 0.3f,
            -0.3f, -0.3f, -0.3f

            /*
            -0.3f,  0.3f, 0.3f,   // top left -- front face
            -0.3f, -0.3f, 0.3f,   // bottom left
            0.3f, -0.3f, 0.3f,   // bottom right
            0.3f,  0.3f, 0.3f,  // top right
            -0.3f,  0.3f, -0.3f,   // top left -- back face
            -0.3f, -0.3f, -0.3f,   // bottom left
            0.3f, -0.3f, -0.3f,   // bottom right
            0.3f,  0.3f, -0.3f */}; // top right

    private short drawOrder[] = {
            0, 1, 3, 1, 2, 3, // front
            3, 2, 7, 2, 6, 7, // right
            7, 6, 4, 6, 5, 4, // back
            4, 5, 0, 5, 1, 0, // left
            4, 0, 7, 0, 3, 7, // top
            1, 5, 2, 5, 6, 2 // bottom
    };

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    private final int mProgram;

    // S, T (or X, Y)
    // Texture coordinate data.
    // Because images have a Y axis pointing downward (values increase as you move down the image) while
    // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
    // What's more is that the texture coordinates are the same for every face.
    final float[] cubeTextureCoordinateData =
            {
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

    // members to hold the stuff to pass to the shader and a ref to the texture
    /** Store our model data in a float buffer. */
    private final FloatBuffer cubeTextureCoordinatesBuffer;

    /** This will be used to pass in the texture. */
    private int mTextureUniformHandle;

    /** This will be used to pass in model texture coordinate information. */
    private int mTextureCoordinateHandle;

    /** Size of the texture coordinate data in elements. */
    private final int mTextureCoordinateDataSize = 2;

    /** This is a handle to our texture data. */
    private int mTextureDataHandle;

    public Cube(MyGLSurfaceView surfaceView) {


//        GLES20.glGenBuffers(1, intVertexBuffer);
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, intVertexBuffer);


        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                cubeCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubeCoords);
        vertexBuffer.position(0);

        // Use VBO's instead of arrays
        int vtxBuffer = 0; // TODO: is this right?
        IntBuffer intVertexBuffer = bb.asIntBuffer();
        GLES30.glGenBuffers(1, intVertexBuffer);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vtxBuffer);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, cubeCoords.length * 4, intVertexBuffer, GLES30.GL_STATIC_DRAW);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // init buffer for texture data
        ByteBuffer ctbb = ByteBuffer.allocateDirect(
                cubeTextureCoordinateData.length * 4);
        ctbb.order(ByteOrder.nativeOrder());
        cubeTextureCoordinatesBuffer = ctbb.asFloatBuffer();
        cubeTextureCoordinatesBuffer.put(cubeTextureCoordinateData);
        cubeTextureCoordinatesBuffer.position(0);

        // init buffer for color data
        int clrBuffer = 1;


        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);

        mTextureDataHandle = TextureHandler.loadTexture(surfaceView, R.drawable.scaly);
    }

    private int mPositionHandle;
    private int mColorHandle;

    private final int vertexCount = drawOrder.length; //squareCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        /** Textures */
        // get handle to fragment shader's uTexture member
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "uTexture");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // texture coordinate handle
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // Pass in the texture coordinate information. every vertex needs 2 values to define texture
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, cubeTextureCoordinatesBuffer);


        /** Position */
        // Enable a handle to the square vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Prepare the square coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);




        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the square
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);


        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the square by drawing two triangles
//        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
//                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        //GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
    }
}
