//#extension GL_OES_standard_derivatives : enable
//
//#ifdef GL_ES
//precision highp float;
//#endif
//
//uniform float time;
//uniform vec2  resolution;
//uniform float zoom;
//
//#define PI 3.1415926535
//
//mat2 rotate3d(float angle)
//{
//    return mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
//}
//
//void main()
//{
//    vec2 p = (gl_FragCoord.xy * 2.0 - resolution) / min(resolution.x, resolution.y);
//    p = rotate3d((time * 1.0) * PI) * p;
//    float t;
//    if (sin(time) == 10.0)
//    t = 0.075 / abs(1.0 - length(p));
//    else
//    t = 0.075 / abs(1.8/*sin(time)*/ - length(p));
//    gl_FragColor = vec4(     ( 1.1 -exp( -vec3(t)  * vec3(0.1*(sin(time)+12.0), p.y*0.7, 2.0) )) , 1.0);
//}
#ifdef GL_ES
precision highp float;
#endif

uniform float time;
uniform vec2 resolution;

const float COUNT = 10.0;

void main( void ) {
    vec2 uPos = ( gl_FragCoord.xy / resolution.y );//normalize wrt y axis
    uPos -= vec2((resolution.x/resolution.y)/2.0, 0.1);//shift origin to center

    float vertColor = 0.;
    for(float i=0.0; i<COUNT; i++){
        float t = time/3.0 + (i+0.1);

        uPos.y += sin(-t+uPos.x*9.0)*0.1;
        uPos.x += cos(-t+uPos.y*6.0+cos(t/1.0))*0.15;
        float value = (sin(uPos.y*8.0) + uPos.x*5.1);

        float stripColor = 1.0/sqrt(abs(value))*3.0;

        vertColor += stripColor/50.0;
    }

    float temp = vertColor;
    vec3 color = vec3(temp*0., temp*0.4, temp*1.0);
    color *= color.r+color.g+color.b;
    gl_FragColor = vec4(color, 1.0);
}