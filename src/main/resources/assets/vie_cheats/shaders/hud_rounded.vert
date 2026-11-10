#version 330 core

layout (location = 0) in vec4 pos;
layout (location = 1) in vec2 uv;
layout (location = 2) in vec4 color;

out vec2 v_Uv;
out vec4 v_Color;

void main() {
    gl_Position = pos;
    v_Uv = uv;
    v_Color = color;
}
