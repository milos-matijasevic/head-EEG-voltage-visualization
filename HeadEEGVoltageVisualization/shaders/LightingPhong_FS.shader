#version 400
in vec3 interpolatedVertexColor;
in vec3 interpolatedNormal;
in vec3 lightPosInCameraSpace;
in vec3 vertexPosInCameraSpace;
in vec3 vertexPos;
in vec3 color;

out vec4 outColor;


uniform int ElectrodesNumber;
uniform float ElectrodesValues[30];
uniform vec3 Electrodes[30];
uniform float MaxAngle;


void main()
{
	outColor = vec4(0.0, 0.0, 0.0, 0.0);

	float length = 0;
	float hue = 0;
	for (int i = 0; i < ElectrodesNumber; i++)
	{
		float dotProduct = dot(normalize(vertexPos), normalize(Electrodes[i]));
		float radians = acos(dotProduct);
		float angle = degrees(radians);
		angle = abs(angle);

		float contribution = max(MaxAngle - angle, 0.0);
		length += contribution;
		hue += contribution * (ElectrodesValues[i] + 50);
		
	}
	length += int(length == 0);

	hue /= length;
	
	hue = 100 - hue;
	hue = hue*2.8/360;
	
	vec3 c = vec3(hue, 1.0, 1.0);
	
	vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    vec3 color = c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
	
	outColor += int(vertexPos.y > 4.0) * vec4(color, 1.0);
	
	vec3 normal = normalize(interpolatedNormal);
	vec3 lightVec = normalize(lightPosInCameraSpace - vertexPosInCameraSpace);
	float lambert = max(dot(normal, lightVec), 0);
	outColor += int(vertexPos.y <= 4.0) * vec4(lambert*interpolatedVertexColor, 1.0);

}