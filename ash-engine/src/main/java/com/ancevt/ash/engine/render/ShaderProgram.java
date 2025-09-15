package com.ancevt.ash.engine.render;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class ShaderProgram {
    private final int programId;

    public ShaderProgram() {
        programId = glCreateProgram();
        if (programId == NULL) {
            throw new RuntimeException("Could not create Shader");
        }
    }

    public void attachShader(String code, int type) {
        int shaderId = glCreateShader(type);
        glShaderSource(shaderId, code);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Error compiling Shader: " + glGetShaderInfoLog(shaderId));
        }

        glAttachShader(programId, shaderId);
    }

    public void link() {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Error linking Shader: " + glGetProgramInfoLog(programId));
        }
    }

    public void use() {
        glUseProgram(programId);
    }

    public void cleanup() {
        glDeleteProgram(programId);
    }

    public int getId() {
        return programId;
    }
}

