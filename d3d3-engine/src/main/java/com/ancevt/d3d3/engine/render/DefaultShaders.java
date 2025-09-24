package com.ancevt.d3d3.engine.render;

public class DefaultShaders {

    public static String FRAGMENT = """
            #version 330 core
            out vec4 fragColor;

            in vec2 TexCoord;
            in vec3 FragPos;
            in vec3 Normal;

            uniform sampler2D texture1;
            uniform vec3 lightPos;
            uniform vec3 viewPos;
            uniform vec3 lightColor;

            uniform vec3 objectColor;
            
            void main() {
                 float ambientStrength = 0.2;
                 vec3 ambient = ambientStrength * lightColor;
             
                 vec3 norm = normalize(Normal);
                 vec3 lightDir = normalize(lightPos - FragPos);
                 float diff = max(dot(norm, lightDir), 0.0);
                 vec3 diffuse = diff * lightColor;
             
                 float specularStrength = 0.5;
                 vec3 viewDir = normalize(viewPos - FragPos);
                 vec3 reflectDir = reflect(-lightDir, norm);
                 float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
                 vec3 specular = specularStrength * spec * lightColor;
             
                 vec3 texColor = texture(texture1, TexCoord).rgb;
                 vec3 lighting = (ambient + diffuse + specular) * texColor * objectColor;
             
                 fragColor = vec4(lighting, 1.0);
             }
            """;

    public static String VERTEX = """
            #version 330 core
            layout(location = 0) in vec3 position;
            layout(location = 1) in vec2 texCoord;
            layout(location = 2) in vec3 normal;

            out vec2 TexCoord;
            out vec3 FragPos;
            out vec3 Normal;

            uniform mat4 projection;
            uniform mat4 view;
            uniform mat4 model;

            void main() {
                gl_Position = projection * view * model * vec4(position, 1.0);
                FragPos = vec3(model * vec4(position, 1.0));
                Normal = mat3(transpose(inverse(model))) * normal;
                TexCoord = texCoord;
            }
            """;
}
