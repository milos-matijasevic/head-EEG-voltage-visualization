#version 400 core
layout(location = 0) in vec3 vertexPosition;
layout(location = 1) in vec3 vertexColor;
layout(location = 2) in vec3 vertexNormal;

out vec3 interpolatedVertexColor;
out vec3 interpolatedNormal;
out vec3 lightPosInCameraSpace;
out vec3 vertexPosInCameraSpace;
out vec3 vertexPos;

uniform mat4 MVPTransform;
uniform mat4 MVTransform;
uniform mat4 Rotate;
uniform mat4 NormalTransform;
uniform vec3 LightPosition;

void main()
{
	interpolatedNormal = (NormalTransform * Rotate * vec4(vertexNormal, 0.0)).xyz;
	lightPosInCameraSpace = (MVTransform * vec4(LightPosition, 1.0)).xyz;
	vertexPosInCameraSpace = (MVTransform * vec4(vertexPosition, 1.0)).xyz;
	interpolatedVertexColor = vertexColor;
	vertexPos = vertexPosition;
	
	gl_Position = MVPTransform * Rotate * vec4(vertexPosition, 1.0);
}
