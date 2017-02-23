#version 330 core

uniform sampler2D texUnit;
in vec2 vTexCoords;

out vec4 g_FragColor;

void main() {
    g_FragColor = texture(texUnit, vTexCoords);
}