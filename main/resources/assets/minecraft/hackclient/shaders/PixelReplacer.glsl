#version 120

uniform float time;
uniform sampler2D texture;
uniform float texel_size;
uniform float r_offset;
uniform float g_offset;
uniform float b_offset;

#define MAX_ITER 30
void main( void ) {
    vec2 zv = gl_TexCoord[0].st;
    vec4 pix_color = texture2D(texture, zv);

    if (pix_color.a > 0.0) {
  //      vec2 p = zv.xy;
  //      for(int i = 1; i < 10; i++) {
  //          p += sin(p.yx * vec2(1.6, 1.1) * float(i + 11) + time * float(i) * vec2(3.4, 0.5) / 10.0) * 0.1;
  //      }
  //      float c = (abs(sin(p.y + time * 0.0) + sin(p.x + time * 0.0))) * 0.5;
//        gl_FragColor = vec4(vec3(r_offset + c, g_offset + c, b_offset + c), pix_color.a);
        vec2 sp = zv;
    	vec2 p = sp*5.0 - vec2(10.0);
    	vec2 i = p;
    	float c = 1.0;

    	float inten = 0.01;

    	for (int n = 0; n < MAX_ITER; n++)
    	{
    		float t = time* (11.0 - (3.0 / float(n+1)));
    		i = p + vec2(cos(t - i.x) + sin(t + i.y), sin(t - i.y) + cos(t + i.x));
    		c += 1.0/length(vec2(p.x / (sin(i.x+t)/inten),p.y / (cos(i.y+t)/inten)));
    	}
    	c /= float(MAX_ITER);
    	c = 1.5-sqrt(c);
    	gl_FragColor = vec4(vec3(c*c*c*c), 0.0) + vec4(r_offset, g_offset, b_offset, pix_color.a);
    } else {
        discard;
    }
}

