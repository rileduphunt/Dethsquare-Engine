#version 330 core

uniform mat4 transform;
in vec2 position;
in vec2 texCoords;
in vec3 colour;

out vec2 vTexCoords;
out vec3 vColour;

void main() {
    vTexCoords = texCoords;
    vColour = colour;
    gl_Position = transform * vec4(position, 0.0, 1.0);
}