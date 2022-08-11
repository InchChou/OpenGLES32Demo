package com.example.myrenderapplication;

import android.opengl.GLES32;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Points {
    private final String TAG = "Points";

    private final String vertexShaderCode =
        "#version 320 es\n"+
        "layout (location = 0) in vec2 aPos;\n" +
        "\n" +
        "void main()\n" +
        "{\n" +
        "    gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);\n" +
        "}";

    private final String fragmentShaderCode =
        "#version 320 es\n" +
        "precision mediump float;\n" +
        "out vec4 FragColor;\n"+
        "void main()\n" +
        "{\n" +
        "    FragColor = vec4(0.0, 1.0, 0.0, 1.0);\n" +
        "}";

    private final String geometryShaderCode =
        "#version 320 es\n" +
        "layout (points) in;\n" +
        "layout (line_strip, max_vertices = 2) out;\n" +
        "void main() {\n" +
        "    gl_Position = gl_in[0].gl_Position + vec4(-0.1, 0.0, 0.0, 0.0); \n" +
        "    EmitVertex();\n" +
        "    gl_Position = gl_in[0].gl_Position + vec4( 0.1, 0.0, 0.0, 0.0);\n" +
        "    EmitVertex();\n" +
        "    EndPrimitive();\n" +
        "}";

    static float points[] = {
        -0.5f,  0.5f, // 左上
         0.5f,  0.5f, // 右上
         0.5f, -0.5f, // 右下
        -0.5f, -0.5f  // 左下
    };
    private FloatBuffer vertexBuffer;

    private int positionHandle;

    static final int COORDS_PER_VERTEX = 2;
    private final int vertexCount = points.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private final int mProgram;

    private final int mvao;
    private final int mvbo;

    public Points() {
        String version = GLES32.glGetString(GLES32.GL_VERSION);
        Log.w(TAG, "Version: " + version );
        int[] vers = new int[2];
        GLES32.glGetIntegerv(GLES32.GL_MAJOR_VERSION, vers, 0);
        GLES32.glGetIntegerv(GLES32.GL_MINOR_VERSION, vers, 1);

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                points.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(points);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        int vertexShader = MyGLRenderer.loadShader(GLES32.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES32.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        int geometryShader = MyGLRenderer.loadShader(GLES32.GL_GEOMETRY_SHADER,
                geometryShaderCode);

        String vertexShaderlog = GLES32.glGetShaderInfoLog(vertexShader);
        String fragmentShaderlog = GLES32.glGetShaderInfoLog(fragmentShader);
        String geometryShaderlog = GLES32.glGetShaderInfoLog(geometryShader);

        // create empty OpenGL ES Program
        mProgram = GLES32.glCreateProgram();

        // add the vertex shader to program
        GLES32.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES32.glAttachShader(mProgram, fragmentShader);

        // add the geometry shader to program
        GLES32.glAttachShader(mProgram, geometryShader);

        // creates OpenGL ES program executables
        GLES32.glLinkProgram(mProgram);

        int[] linked = new int[1];
        // Check the link status
        GLES32.glGetProgramiv(mProgram, GLES32.GL_LINK_STATUS, linked, 0);

        int[] length = new int[1];
        GLES32.glGetProgramiv(mProgram, GLES32.GL_INFO_LOG_LENGTH, length, 0);

        String infolog = GLES32.glGetProgramInfoLog(mProgram);

//        IntBuffer vbo = IntBuffer.allocate(1);
//        IntBuffer vao = IntBuffer.allocate(1);
//        GLES32.glGenBuffers(1, vbo);
//        GLES32.glGenVertexArrays(1, vao);
//        mvao = vao.get(0);
//        mvbo = vbo.get(0);
        int[] array = new int[1];
        GLES32.glGenBuffers(1, array, 0);
        mvbo = array[0];
        GLES32.glGenVertexArrays(1, array, 0);
        mvao = array[0];

        GLES32.glBindVertexArray(mvao);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, mvbo);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, points.length * 4, vertexBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(0, 2, GLES32.GL_FLOAT, false, vertexStride, 0);
        GLES32.glEnableVertexAttribArray(0);
        GLES32.glBindVertexArray(0);

        if (linked[0] == 0) {
            Log.e(TAG, "Error linking program:");
            Log.e(TAG, GLES32.glGetProgramInfoLog(mProgram));
            GLES32.glDeleteProgram(mProgram);
            return;
        }
    }

    public void draw(float[] mvpMatirx) {
        // Add program to OpenGL ES environment
        GLES32.glUseProgram(mProgram);


        GLES32.glBindVertexArray(mvao);

        GLES32.glDrawArrays(GLES32.GL_POINTS, 0, vertexCount);
    }
}
