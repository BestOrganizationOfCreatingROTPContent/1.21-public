#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

uniform vec2 InSize;

out vec4 fragColor;

void main(){
    vec4 diffuseColor = texture(InSampler, texCoord);
    float alpha = diffuseColor.a;
    if (alpha > 0) {
        float sizeMin = min(InSize.x, InSize.y);
        vec2 sizeCorr = InSize / sizeMin;
        vec2 texCoordCorr = texCoord * sizeCorr;
        vec2 centerCoordCorr = vec2(0.5, 0.5) * sizeCorr;
        
        float distFromCenter = distance(texCoordCorr, centerCoordCorr);
        float mult = min(distFromCenter + 0.5, 1);
        fragColor = vec4(diffuseColor.rgb, alpha * mult);
    }
    else {
        fragColor = diffuseColor;
    }
}
