#version 120

uniform sampler2D image;

void main() {
    vec2 uv = gl_TexCoord[0].st;

    vec4 color = texture2D(image, uv);

//    if (color.a == 0) {
//        return gl_FragColor()
//
//    }

}
