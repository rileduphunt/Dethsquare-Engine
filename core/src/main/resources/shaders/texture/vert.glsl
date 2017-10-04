#version 330 core

uniform mat4 transform;
in vec2 position;
in vec2 texCoords;
in vec4 colour;

out vec2 vTexCoords;
out vec4 vColour;

void main() {
    vTexCoords = texCoords;
    vColour = colour;
    gl_Position = transform * vec4(position, 0.0, 1.0);
}