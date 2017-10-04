#version 330 core

uniform sampler2D texUnit;
in vec2 vTexCoords;
in vec4 vColour;

out vec4 g_FragColor;

void main() {
    vec4 tex = texture(texUnit, vTexCoords);
    g_FragColor = vec4(tex.r * vColour.r, tex.g * vColour.g, tex.b * vColour.b, tex.a * vColour.a);
}