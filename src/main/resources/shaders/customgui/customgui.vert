#version 330

layout (location=0) in vec3 inPos;
layout (location=1) in vec2 inTextCoords;
layout (location=2) in vec4 inColor;

out vec2 fragTextCoords;
out vec4 fragColor;

void main()
{
    fragTextCoords = inTextCoords;
    fragColor = inColor;
    gl_Position = vec4(inPos, 1.0);
}