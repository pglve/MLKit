//#extension GL_OES_EGL_image_external : require
//precision mediump float;
//uniform samplerExternalOES videoTex;
//varying vec2 textureCoordinate;
//
//void main() {
//    vec4 tc = texture2D(videoTex, textureCoordinate);
//    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;//这里进行的颜色变换处理，传说中的黑白滤镜。
//    gl_FragColor = vec4(color,color,color,1.0);
//}

#extension GL_OES_EGL_image_external : require
//SurfaceTexture比较特殊
//float数据是什么精度的
precision mediump float;

//采样点的坐标
varying vec2 aCoord;

//采样器
uniform samplerExternalOES vTexture;

void main(){
    //变量 接收像素值
    // texture2D：采样器 采集 aCoord的像素
    //赋值给 gl_FragColor 就可以了
    gl_FragColor = texture2D(vTexture,aCoord);

//    vec4 tc = texture2D(vTexture,aCoord);
//    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;//这里进行的颜色变换处理，传说中的黑白滤镜。
//    gl_FragColor = vec4(color,color,color,1.0);
}