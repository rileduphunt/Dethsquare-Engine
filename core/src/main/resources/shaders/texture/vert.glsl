#version 330 core

uniform mat4 transform;
in vec2 position;
in vec2 texCoords;

out vec2 vTexCoords;
out vec4 colour;

void main() {
    vTexCoords = texCoords;
    gl_Position = transform * vec4(position, 0.0, 1.0);
}