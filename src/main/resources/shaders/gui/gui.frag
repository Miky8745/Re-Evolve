#version 330

in vec2 fragTextCoords;
in vec4 fragColor;

uniform sampler2D txtSampler;

out vec4 outColor;

void main()
{
    outColor = (fragColor * texture(txtSampler, fragTextCoords)) * 0.001 + vec4(fragTextCoords, 0.0, 1.0);
}