#version 400
in vec3 interpolatedVertexColor;
in vec3 interpolatedNormal;
in vec3 lightPosInCameraSpace;
in vec3 vertexPosInCameraSpace;

out vec4 outColor;
void main()
{
	vec3 normal = normalize(interpolatedNormal);
	vec3 lightVec = normalize(lightPosInCameraSpace - vertexPosInCameraSpace);
	float lambert = max(dot(normal, lightVec), 0);
	outColor = vec4(lambert*interpolatedVertexColor, 1.0);
}
