#version 420

in vec2 typeTextureCoords;
in vec2 contentsTextureCoords;

flat in int hasContents;

flat in int doColorSwap;
in vec4 rChannelColor;
in vec4 gChannelColor;
in vec4 bChannelColor;
in vec4 aChannelColor;

flat in int depth;

layout (binding = 0) uniform sampler2D tileTexture;
layout (binding = 1) uniform sampler2D contentsTexture;

layout(origin_upper_left) in vec4 gl_FragCoord;

out vec4 out_color;

const int max_lights = 8;
const float a = 0.5;
const float b = 0.1;

uniform vec2 screenRatio;

uniform float ambientLight;
uniform vec2 lightPositions[max_lights];
uniform vec4 lightColors[max_lights];
uniform float lightIntensities[max_lights];
uniform float lightInnerRadii[max_lights];
uniform float lightOuterRadii[max_lights];
uniform float lightMaxRadii[max_lights];

// declare that colorSwap function exists
vec4 colorSwap(vec4);

in vec2 pos;

void main(void) {

	// fix screen coordinates
	vec2 frag_position = gl_FragCoord.xy * screenRatio;

	// light calculations
	vec4 totalLight = vec4(0.0);

	for (int i = 0; i < max_lights; i++) {
		if (lightOuterRadii[i] == -1) continue; // skip empty lights
		vec2 lightVector = frag_position - lightPositions[i];
		float distance = length(lightVector);
//		float radius = lightOuterRadii[i] - lightInnerRadii[i];
//		if (distance <= lightInnerRadii[i]) {
//			float att = radius / (radius + a * distance + b * distance * distance);
//			att = max(att, 1.0);
//			totalLight += lightColors[i] * lightIntensities[i] * att;
//		}
//		else {
//			if (distance < lightMaxRadii[i]) {
//				distance -= lightInnerRadii[i];
//				float att = radius / (radius + a * distance + b * distance * distance);
//				if (att < 0.02) att = 0; // lower bound 0.02; anything lower gets ignored
//				totalLight += lightColors[i] * lightIntensities[i] * att;
//			}
//		}
		if (distance < lightInnerRadii[i]) {
			totalLight += lightColors[i] * lightIntensities[i];
		}
		else if (distance < lightOuterRadii[i]) {
			totalLight += lightColors[i] * lightIntensities[i] * (1 - smoothstep(lightInnerRadii[i], lightOuterRadii[i], distance));
		}
	}

	// add sky light
	float pixelDepth = depth + pos.y * 64;

	if (depth < 128) totalLight = vec4(1);
	else if (depth < 176) {
		float light = 1 - smoothstep(128, 176, pixelDepth);
		vec4 fadeLight = vec4(light);
		totalLight += fadeLight;

//		totalLight += vec4(0.5);
	}


	// get final light total
	totalLight = max(totalLight, ambientLight);

	// cap at 1
	totalLight = min(totalLight, vec4(1));

	// get texture color
	out_color = texture(tileTexture, typeTextureCoords);

	// swap colors?
	if (bool(doColorSwap)) {
		out_color = colorSwap(out_color);
	}

	// contents?
	if (bool(hasContents)) {
		vec4 contentsColor = texture(contentsTexture, contentsTextureCoords);
		float source = contentsColor.a;
		float destination = 1 - source;
		out_color = contentsColor * source + out_color * destination;

		// TEST
//		out_color = contentsColor;
//		out_color = vec4(co.z);
		// END_TEST
	}

	// finally, use light value to get result
	out_color = out_color * totalLight;

	// TEST
//	out_color = vec4(totalLight);

}

vec4 colorSwap(vec4 color) {

	vec4 rChannel = rChannelColor * color.r;
	vec4 gChannel = gChannelColor * color.g;
	vec4 bChannel = bChannelColor * color.b;
	vec4 aChannel = aChannelColor * color.a;

	return rChannel + gChannel + bChannel + aChannel;

}