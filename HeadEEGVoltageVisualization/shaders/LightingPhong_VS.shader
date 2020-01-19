#version 400 core
layout(location = 0) in vec3 vertexPosition;
layout(location = 1) in vec3 vertexColor;
layout(location = 2) in vec3 vertexNormal;

out vec3 interpolatedVertexColor;
out vec3 interpolatedNormal;
out vec3 lightPosInCameraSpace;
out vec3 vertexPosInCameraSpace;

uniform mat4 MVPTransform;
uniform mat4 MVTransform;
uniform mat3 NormalTransform;
uniform vec3 LightPosition;

void main()
{
	interpolatedNormal = NormalTransform * vertexNormal;
	lightPosInCameraSpace = (MVTransform * vec4(LightPosition, 1.0)).xyz;
	vertexPosInCameraSpace = (MVTransform * vec4(vertexPosition, 1.0)).xyz;
	
	interpolatedVertexColor = vertexColor;
	
	gl_Position = MVPTransform * vec4(vertexPosition, 1.0);
}
