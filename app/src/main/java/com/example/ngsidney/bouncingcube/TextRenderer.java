package com.example.ngsidney.bouncingcube;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by ngsidney on 7/23/15.
 */
public class TextRenderer {

    private final int mProgram;

    private final String vertexShaderCode =
            "attribute vec4 a_vertex;" +
//                    "attribute vec2 a_texCoord;" +
                    "uniform mat4 a_mvpMatrix;" +
//                    "varying vec2 v_texCoord;" +

                    "void main() {  \n" +
//                    "  v_texCoord = a_texCoord;" +
                    "  gl_Position = a_mvpMatrix * a_vertex;" + // pass only the positions
                    "}";

    // Use to access and set the view transformation
    private int mMVPMatrixHandle;

    // Fragment shader for rendering the face of the shape with colors and textures
    private final String fragmentShaderCode =
            "precision mediump float;" +
//                    "varying vec2 v_texCoord;" +
//                    "uniform sampler2D u_texture;" +
                    "uniform vec4 u_color;" +
                    "void main() {" +
                    "  gl_FragColor = u_color;" + //(texture2D(u_texture, v_texCoord));" +
                    "}";

    FloatBuffer vertexBuffer;
    FloatBuffer squareTextureCoordinatesBuffer;

    float[] squareCoords = {
            -4.0f, 4.0f, 1.0f,
            -4.0f, -4.0f, 1.0f,
            4.0f, 4.0f, 1.0f,
            -4.0f, -4.0f, 1.0f,
            4.0f, -4.0f, 1.0f,
            4.0f, 4.0f, 1.0f
    };

    float[] squareTextureCoordData = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
    };

    public TextRenderer() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // init buffer for texture data
        ByteBuffer ctbb = ByteBuffer.allocateDirect(
                squareTextureCoordData.length * 4);
        ctbb.order(ByteOrder.nativeOrder());
        squareTextureCoordinatesBuffer = ctbb.asFloatBuffer();
        squareTextureCoordinatesBuffer.put(squareTextureCoordData);
        squareTextureCoordinatesBuffer.position(0);

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

    }

    private final int COORDS_PER_VERTEX = 3;
    private final int vertexStride = COORDS_PER_VERTEX * 4;

    public void print(float[] vpMatrix, String text) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        int vertexAttr = GLES20.glGetAttribLocation(mProgram, "a_vertex");
//        int texAttr = GLES20.glGetAttribLocation(mProgram, "a_texCoord");

        GLES20.glEnableVertexAttribArray(vertexAttr);
        GLES20.glVertexAttribPointer(vertexAttr, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, 0);

//        GLES20.glEnableVertexAttribArray(texAttr);
//        GLES20.glVertexAttribPointer(texAttr, 2,
//                GLES20.GL_FLOAT, false,
//                2 * 4, 0);

        // draw the text in canvas then load it to a texture
        int mTextureDataHandle = bitmapToTexture(drawTextToBitmap(text));

//        // get handle to fragment shader's uTexture member
//        int mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_texture");
//        // Set the active texture unit to texture unit 0.
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        // Bind the texture to this unit.
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
//        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
//        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "a_mvpMatrix");
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, vpMatrix, 0);

        int colorHandle = GLES20.glGetUniformLocation(mProgram, "u_color");
        // Pass the projection and view transformation to the shader
        GLES20.glUniform4f(colorHandle, 0.0f, 0.0f, 1.0f, 1.0f);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, squareCoords.length / COORDS_PER_VERTEX);
    }

    public int bitmapToTexture(Bitmap bitmap) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0); // handle for the texture (a unique id)

        if (textureHandle[0] != 0)
        {
//            // decode the image file; openGL can't just take an image file
//            final BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inScaled = false;   // No pre-scaling
//
//            // Read in the resource
//            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // binding a texture means that subsequent opengl calls should affect this texture
            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            // free memory: important step
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    public Bitmap drawTextToBitmap(/*Context context, int resId, */String text) {
//        Resources resources = context.getResources();
//        float scale = resources.getDisplayMetrics().density;
//        Bitmap bitmap =
//                BitmapFactory.decodeResource(resources, resId);
//
//        android.graphics.Bitmap.Config bitmapConfig =
//                bitmap.getConfig();
//        // set default bitmap config if none
//        if(bitmapConfig == null) {
//            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
//        }
//        // resource bitmaps are imutable,
//        // so we need to convert it to mutable one
//        bitmap = bitmap.copy(bitmapConfig, true);
        Bitmap bitmap = Bitmap.createBitmap(72, 72, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        paint.setTextSize((int) (64 /* * scale */));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width())/2;
        int y = (bitmap.getHeight() + bounds.height())/2;

        canvas.drawText(text, x, y, paint);

        return bitmap;
    }

}
