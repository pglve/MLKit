package me.pglvee.mlkit.test

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import androidx.camera.core.Preview
import me.pglvee.mlkit.test.OpenGLUtils.loadFromAssets
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class GLCameraView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) :
    GLSurfaceView(context, attrs), GLSurfaceView.Renderer {
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private var textureId = 0
    private var surfaceTexture: SurfaceTexture? = null

    private var vPosition = 0
    private var vCoord = 0
    private var programId = 0

    private var textureMatrixId = 0
    private val textureMatrix = FloatArray(16)
    private var mProjectMatrix = FloatArray(16)
    private var mCameraMatrix = FloatArray(16)

    private var mGLVertexBuffer: FloatBuffer? = null
    private var mGLTextureBuffer: FloatBuffer? = null

    private var mPosCoordinate =
        floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f)
    private var mTexCoordinate =
        floatArrayOf(0f, 0f, 1f, 0f, 0f, 1f, 1f, 1f)


    init {
        setEGLContextClientVersion(2)
        setRenderer(this)
        // 设置非连续渲染
        renderMode = RENDERMODE_WHEN_DIRTY

        Matrix.setIdentityM(mProjectMatrix, 0)
        Matrix.setIdentityM(mCameraMatrix, 0)
        Matrix.setIdentityM(textureMatrix, 0)
    }

    fun attachPreview(preview: Preview) {
        preview.setSurfaceProvider { request ->
            val surface = Surface(surfaceTexture)
            request.provideSurface(surface, executor, {
                surface.release()
                surfaceTexture?.release()
                Log.v(TAG, "--accept------")
            })
        }
    }

    private fun convertToFloatBuffer(buffer: FloatArray): FloatBuffer {
        val fb = ByteBuffer.allocateDirect(buffer.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        fb.clear()
        fb.put(buffer)
        fb.position(0)
        return fb
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        val ids = IntArray(1)
        // OpenGL相关
        GLES20.glGenTextures(1, ids, 0)
        textureId = ids[0]
        surfaceTexture = SurfaceTexture(textureId).apply {
            setOnFrameAvailableListener { requestRender() }
        }

        val vertexShader: String = loadFromAssets(context, "shader/vertex_texture.glsl")
        val fragmentShader: String = loadFromAssets(context, "shader/fragment_texture.glsl")
        programId = OpenGLUtils.loadProgram(vertexShader, fragmentShader)

        vPosition = GLES20.glGetAttribLocation(programId, "vPosition")
        vCoord = GLES20.glGetAttribLocation(programId, "vCoord")

        textureMatrixId = GLES20.glGetUniformLocation(programId, "textureMatrix")

        // 4个顶点，每个顶点有两个浮点型，每个浮点型占4个字节
        mGLVertexBuffer =
            ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mGLVertexBuffer?.clear()
        // 顶点坐标
        mGLVertexBuffer?.put(mPosCoordinate)

        // 纹理坐标
        mGLTextureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        mGLTextureBuffer?.clear()
        mGLTextureBuffer?.put(mTexCoordinate)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Matrix.scaleM(textureMatrix, 0, 1f, -1f, 1f)
        val ratio = width * 1f / height
        Matrix.orthoM(mProjectMatrix, 0, -1f, 1f, -ratio, ratio, 1f, 7f) // 3和7代表远近视点与眼睛的距离，非坐标点
        Matrix.setLookAtM(mCameraMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f) // 3代表眼睛的坐标点
        Matrix.multiplyMM(textureMatrix, 0, mProjectMatrix, 0, mCameraMatrix, 0);
    }

    override fun onDrawFrame(gl: GL10) {
        // 清屏
        GLES20.glClearColor(1f, 0f, 0f, 0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // 更新纹理
        surfaceTexture?.updateTexImage();
        surfaceTexture?.getTransformMatrix(textureMatrix);
        GLES20.glUseProgram(programId);

        //变换矩阵
        GLES20.glUniformMatrix4fv(textureMatrixId, 1, false, textureMatrix, 0)

        // 传递坐标数据
        mGLVertexBuffer?.position(0)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGLVertexBuffer)
        GLES20.glEnableVertexAttribArray(vPosition)

        // 传递纹理坐标
        mGLTextureBuffer?.position(0)
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer)
        GLES20.glEnableVertexAttribArray(vCoord)

        //绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        // 解绑纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
    }
}