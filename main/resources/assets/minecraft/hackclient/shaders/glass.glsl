// SDF of a rounded rectangle. Shamelessly copied from https://iquilezles.org/articles/distfunctions/.
float sdfRect(vec2 center, vec2 size, vec2 p, float r)
{
    vec2 p_rel = p - center;
    vec2 q = abs(p_rel) - size;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

// Thickness is the t in the doc.
vec3 getNormal(float sd, float thickness)
{
    float dx = dFdx(sd);
    float dy = dFdy(sd);

    // The cosine and sine between normal and the xy plane.
    float n_cos = max(thickness + sd, 0.0) / thickness;
    float n_sin = sqrt(1.0 - n_cos * n_cos);

    return normalize(vec3(dx * n_cos, dy * n_cos, n_sin));
}

// The height (z component) of the pad surface at sd.
float height(float sd, float thickness)
{
    if(sd >= 0.0)
    {
        return 0.0;
    }
    if(sd < -thickness)
    {
        return thickness;
    }

    float x = thickness + sd;
    return sqrt(thickness * thickness - x * x);
}

vec4 bgImage(vec2 uv)
{
    return texture(iChannel0, uv);
}

vec4 bgStrips(vec2 uv)
{
    if(fract(uv.y * iResolution.y / 20.0) < 0.5)
    {
        return vec4(0.0, 0.5, 1.0, 0.0);
    }
    else
    {
        return vec4(0.9, 0.9, 0.9, 0.0);

    }
}

vec4 bg(vec2 uv)
{
    if(uv.x > 0.5)
    {
        return bgStrips(uv);
    }

    return bgImage(uv);
}

void mainImage(out vec4 fragColor, in vec2 fragCoord )
{
    vec2 uv = fragCoord / iResolution.xy;

    float thickness = 15.0;
    float index = 1.5;
    float base_height = thickness * 8.0;
    float color_mix = -0.2;
    vec4 color_base = vec4(1.0, 1.0, 1.0, 0.0);

    vec2 center = iMouse.xy;
    if (center == vec2(0.0, 0.0))
    {
        center = iResolution.xy * 0.5;
    }

    float sd = sdfRect(center, vec2(128.0, 0.0), fragCoord, 64.0);
    // Naive background pass-through without anti-aliasing
    // if(sd >= 0.0)
    // {
    //     fragColor = mix(vec4(0.0), bg(uv),
    //                     clamp(sd / 100.0, 0.0, 1.0) * 0.1 + 0.9);
    //     return;
    // }

    // Background pass-through with anti-aliasing
    vec4 bg_col = vec4(0.0);
    bg_col = mix(vec4(0.0), bg(uv), clamp(sd / 100.0, 0.0, 1.0) * 0.1 + 0.9);
    bg_col.a = smoothstep(-4., 0., sd);

    vec3 normal = getNormal(sd, thickness);

    // A ray going -z hits the top of the pad, where would it hit on
    // the z = -base_height plane?
    vec3 incident = vec3(0.0, 0.0, -1.0); // Should be normalized.
    vec3 refract_vec = refract(incident, normal, 1.0 / index);
    float h = height(sd, thickness);
    float refract_length = (h + base_height) /
    dot(vec3(0.0, 0.0, -1.0), refract_vec);
    // This is the screen coord of the ray hitting the z = -base_height
    // plane.
    vec2 coord1 = fragCoord + refract_vec.xy * refract_length;
    vec4 refract_color = bg(coord1 / iResolution.xy);

    // Reflection
    vec3 reflect_vec = reflect(incident, normal);
    vec4 reflect_color = vec4(0.0);

    float c = clamp(abs(reflect_vec.x - reflect_vec.y), 0.0, 1.0);
    reflect_color = vec4(c, c, c, 0.0);

    fragColor = mix(mix(refract_color, reflect_color, (1.0 - normal.z) * 2.0),
                    color_base, color_mix);

    // Mix with bg for anti-aliasing
    fragColor = clamp(fragColor, 0., 1.);
    bg_col = clamp(bg_col, 0., 1.);
    fragColor = mix(fragColor, bg_col, bg_col.a);
}