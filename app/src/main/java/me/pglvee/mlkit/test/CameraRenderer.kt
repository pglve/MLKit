package me.pglvee.mlkit.test

import android.graphics.SurfaceTexture
import android.opengl.GLES31
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraRenderer(val createCallback: (SurfaceTexture) -> Boolean = { true }) :
    GLSurfaceView.Renderer {

    private var mPosBuffer: FloatBuffer? = null
    private var mTexBuffer: FloatBuffer? = null
    private var mPosCoordinate =
        floatArrayOf(-1f, -1f, -1f, 1f, 1f, -1f, 1f, 1f)
    private var mTexCoordinateBackRight =
        floatArrayOf(1f, 1f, 0f, 1f, 1f, 0f, 0f, 0f) //顺时针转90并沿Y轴翻转  后摄像头正确，前摄像头上下颠倒
    private var mTexCoordinateFrontRight =
        floatArrayOf(0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f) //顺时针旋转90  后摄像头上下颠倒了，前摄像头正确

    private var uPosHandle = 0
    private var aTexHandle = 0
    private var mMVPMatrixHandle = 0
    private var mProjectMatrix = FloatArray(16)
    private var mCameraMatrix = FloatArray(16)
    private var mMVPMatrix = FloatArray(16)
    private var mTempMatrix = FloatArray(16)
    private var mSurfaceTexture: SurfaceTexture? = null

    public var mProgram = 0
    public var mBoolean = false
    private var isBackCamera: Boolean = true

    init {
        Matrix.setIdentityM(mProjectMatrix, 0)
        Matrix.setIdentityM(mCameraMatrix, 0)
        Matrix.setIdentityM(mMVPMatrix, 0)
        Matrix.setIdentityM(mTempMatrix, 0)
    }

    private fun convertToFloatBuffer(buffer: FloatArray): FloatBuffer {
        val fb = ByteBuffer.allocateDirect(buffer.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        fb.put(buffer)
        fb.position(0)
        return fb
    }

    //添加程序到ES环境中
    private fun activeProgram() {
        // 将程序添加到OpenGL ES环境
        GLES31.glUseProgram(mProgram);

        // 获取顶点着色器的位置的句柄
        uPosHandle = GLES31.glGetAttribLocation(mProgram, "position")
        aTexHandle = GLES31.glGetAttribLocation(mProgram, "inputTextureCoordinate")
        mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "textureTransform")

        mPosBuffer = convertToFloatBuffer(mPosCoordinate)
        if (isBackCamera) {
            mTexBuffer = convertToFloatBuffer(mTexCoordinateBackRight)
        } else {
            mTexBuffer = convertToFloatBuffer(mTexCoordinateFrontRight)
        }

        GLES31.glVertexAttribPointer(uPosHandle, 2, GLES31.GL_FLOAT, false, 0, mPosBuffer)
        GLES31.glVertexAttribPointer(aTexHandle, 2, GLES31.GL_FLOAT, false, 0, mTexBuffer)

        // 启用顶点位置的句柄
        GLES31.glEnableVertexAttribArray(uPosHandle)
        GLES31.glEnableVertexAttribArray(aTexHandle)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES31.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        mSurfaceTexture = SurfaceTexture(OpenGLUtils.createOESTextureObject())
//        mProgram = OpenGLUtils.createProgram(vertexShaderCode, fragmentShaderCode)
        mSurfaceTexture?.let { isBackCamera = createCallback(it) }
        activeProgram()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES31.glViewport(0, 0, width, height)
        Matrix.scaleM(mMVPMatrix, 0, 1f, -1f, 1f)
        val ratio = width * 1f / height
        Matrix.orthoM(mProjectMatrix, 0, -1f, 1f, -ratio, ratio, 1f, 7f) // 3和7代表远近视点与眼睛的距离，非坐标点
        Matrix.setLookAtM(mCameraMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f) // 3代表眼睛的坐标点
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mCameraMatrix, 0);
    }

    override fun onDrawFrame(gl: GL10?) {
        if (mBoolean) {
            activeProgram()
            mBoolean = false
        }
        if (mSurfaceTexture != null) {
            GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT or GLES31.GL_DEPTH_BUFFER_BIT)
            mSurfaceTexture?.updateTexImage()
            GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)
            GLES31.glDrawArrays(GLES31.GL_TRIANGLE_STRIP, 0, mPosCoordinate.size / 2)
        }
    }
}