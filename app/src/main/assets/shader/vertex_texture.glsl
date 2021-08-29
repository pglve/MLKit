//uniform mat4 textureTransform;
//attribute vec2 inputTextureCoordinate;
//attribute vec4 position;
//varying   vec2 textureCoordinate;
//
//void main() {
//    gl_Position = position;
//    textureCoordinate = inputTextureCoordinate;
////    textureCoordinate = (textureTransform * inputTextureCoordinate).xy;
//}

attribute vec4 vPosition;
attribute vec4 vCoord;
varying vec2 aCoord;

uniform mat4 textureMatrix;

void main(){
    gl_Position = vPosition;
    aCoord = (textureMatrix * vCoord).xy;
}

