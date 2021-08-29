package me.pglvee.mlkit.test

import android.content.Context
import android.content.res.Resources
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import java.nio.charset.Charset
import javax.microedition.khronos.opengles.GL10

const val TAG = "OpenGl"

object OpenGLUtils {

    fun createOESTextureObject(): Int {
        val tex = IntArray(1)
        // 生成一个纹理
        GLES20.glGenTextures(1, tex, 0)
        // 将此纹理绑定到外部纹理上
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0])
        // 设置纹理过滤参数
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MIN_FILTER,
            GL10.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER,
            GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_S,
            GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_T,
            GL10.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        return tex[0]
    }

    fun loadShader(type: Int, source: String): Int {
        // 创建着色器
        var shader = GLES20.glCreateShader(type)
        if (shader == GLES20.GL_NONE) {
            Log.e(TAG, "create shared failed! type: $type");
            return GLES20.GL_NONE;
        }
        // 添加着色器代码
        GLES20.glShaderSource(shader, source)
        // 编译着色器代码
        GLES20.glCompileShader(shader)
        // 检测编译状态
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == GLES20.GL_FALSE) { // 编译失败
            Log.e(TAG, "Error compiling shader. type: $type:")
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader) // 删除着色器
            shader = GLES20.GL_NONE
        }
        return shader
    }

    fun linkProgram(vertexSource: String, fragmentSource: String): Int {
        // 加载顶点着色器
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == GLES20.GL_NONE) {
            Log.e(TAG, "load vertex shader failed! ")
            return GLES20.GL_NONE
        }
        // 加载片段着色器
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragmentShader == GLES20.GL_NONE) {
            Log.e(TAG, "load fragment shader failed! ")
            return GLES20.GL_NONE
        }
        // 创建空的OpenGL ES程序
        val program = GLES20.glCreateProgram()
        if (program == GLES20.GL_NONE) {
            Log.e(TAG, "create program failed! ")
            return GLES20.GL_NONE;
        }
        // 添加顶点着色器到程序中
        GLES20.glAttachShader(program, vertexShader)
        // 添加片段着色器到程序中
        GLES20.glAttachShader(program, fragmentShader)
        // 释放着色器资源
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)
        // 创建OpenGL ES程序可执行文件
        GLES20.glLinkProgram(program)
        // 检测执行状态
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == GLES20.GL_FALSE) { // 执行失败
            Log.e(TAG, "Error link program: ")
            Log.e(TAG, GLES20.glGetProgramInfoLog(program))
            GLES20.glDeleteProgram(program) // 删除程序
            return GLES20.GL_NONE
        }
        return program
    }

    /**
     * 价值着色器并编译成GPU程序
     * @param vSource
     * @param fSource
     * @return
     */
    fun loadProgram(vSource: String?, fSource: String?): Int {
        /**
         * 顶点着色器
         */
        val vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        //加载着色器代码
        GLES20.glShaderSource(vShader, vSource)
        //编译（配置）
        GLES20.glCompileShader(vShader)

        //查看配置 是否成功
        val status = IntArray(1)
        GLES20.glGetShaderiv(vShader, GLES20.GL_COMPILE_STATUS, status, 0)
        check(status[0] == GLES20.GL_TRUE) {
            //失败
            "load vertex shader:" + GLES20.glGetShaderInfoLog(vShader)
        }
        /**
         * 片元着色器
         * 流程和上面一样
         */
        val fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        //加载着色器代码
        GLES20.glShaderSource(fShader, fSource)
        //编译（配置）
        GLES20.glCompileShader(fShader)

        //查看配置 是否成功
        GLES20.glGetShaderiv(fShader, GLES20.GL_COMPILE_STATUS, status, 0)
        check(status[0] == GLES20.GL_TRUE) {
            //失败
            "load fragment shader:" + GLES20.glGetShaderInfoLog(vShader)
        }
        /**
         * 创建着色器程序
         */
        val program = GLES20.glCreateProgram()
        //绑定顶点和片元
        GLES20.glAttachShader(program, vShader)
        GLES20.glAttachShader(program, fShader)
        //链接着色器程序
        GLES20.glLinkProgram(program)
        //获得状态
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0)
        check(status[0] == GLES20.GL_TRUE) { "link program:" + GLES20.glGetProgramInfoLog(program) }
        GLES20.glDeleteShader(vShader)
        GLES20.glDeleteShader(fShader)
        return program
    }

    fun loadFromAssets(context: Context, fileName: String): String {
        context.resources.assets.open(fileName).use {
            val data = ByteArray(it.available())
            it.read(data)
            return data.toString(Charset.defaultCharset())
                .replace("\\r\\n", "\\n")
        }
    }
}