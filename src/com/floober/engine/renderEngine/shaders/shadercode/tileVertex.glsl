#version 140

// IN VARIABLES
// vertex position
in vec3 position;

// transformations for
in mat4 typeTransformationMatrix;
in mat4 contentsTransformationMatrix;

in vec4 typeOffsets;
in vec4 contentsOffsets;

out vec4 co;

out vec2 typeTextureCoords;
out vec2 contentsTextureCoords;

flat out int hasContents;

in vec3 in_modifiers;
in vec4 in_rChannelColor;
in vec4 in_gChannelColor;
in vec4 in_bChannelColor;
in vec4 in_aChannelColor;

flat out int doColorSwap;
out vec4 rChannelColor;
out vec4 gChannelColor;
out vec4 bChannelColor;
out vec4 aChannelColor;

flat out int depth;

out vec2 pos;

// UNIFORMS
uniform float numRows;

void main(void) {

	co = contentsOffsets;

	// get relative texture coords in this tile from 0 to 1
	typeTextureCoords = vec2((position.x+1.0)/2.0, 1 - (position.y+1.0)/2.0);

	pos = typeTextureCoords;

	// get the size of a tile in the tileset atlas
	float tileSize = typeOffsets.z - typeOffsets.x;
	float size = tileSize / 1.0;
	typeTextureCoords *= size;

	// use smoothstep to find the corresponding texture sub-coords inside this tile's region of the tileset
    typeTextureCoords = typeTextureCoords + typeOffsets.xy;

//	contentsTextureCoords = (vec4(position, 1.0) * contentsTransformationMatrix).xy;
//	contentsTextureCoords *= size;
//	contentsTextureCoords = contentsTextureCoords + contentsOffsets.xy;

//	// get relative texture coords in this tile from 0 to 1
	contentsTextureCoords = vec2((position.x+1.0)/2.0, 1 - (position.y+1.0)/2.0);
//
//	// get the size of a tile in the contents atlas
	tileSize = contentsOffsets.z - contentsOffsets.x;
	size = tileSize / 1.0;
	contentsTextureCoords *= size;
//
//	// use smoothstep to find the corresponding texture sub-coords inside this tile's region of the contents atlas
	contentsTextureCoords = contentsTextureCoords + contentsOffsets.xy;

	// PASS VARIABLES
	doColorSwap = int(in_modifiers.x);
	rChannelColor = in_rChannelColor;
	gChannelColor = in_gChannelColor;
	bChannelColor = in_bChannelColor;
	aChannelColor = in_aChannelColor;

	hasContents = int(in_modifiers.y);

	depth = int(in_modifiers.z);

	// set the position of this vertex in the world
    gl_Position = typeTransformationMatrix * vec4(position, 1.0);

}