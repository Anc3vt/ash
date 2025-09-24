package com.ancevt.d3d3.engine.scene;

import com.ancevt.d3d3.engine.render.Camera;
import com.ancevt.d3d3.engine.render.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.*;

public class RenderContext {
    private ShaderProgram shader;
    private Camera camera;
    private Matrix4f projection;   // 🔹 добавляем проекцию
    private int modelLoc, viewLoc, projLoc, objectColorLoc;

    public RenderContext(ShaderProgram shader, Camera camera, Matrix4f projection) {
        this.shader = shader;
        this.camera = camera;
        this.projection = projection;

        modelLoc = glGetUniformLocation(shader.getId(), "model");
        viewLoc = glGetUniformLocation(shader.getId(), "view");
        projLoc = glGetUniformLocation(shader.getId(), "projection");
        objectColorLoc = glGetUniformLocation(shader.getId(), "objectColor");
    }

    public Matrix4f getProjection() {
        return projection;
    }

    public ShaderProgram getShader() {
        return shader;
    }

    public Camera getCamera() {
        return camera;
    }




    public void renderMesh(Mesh mesh, int textureId, Vector3f color, Matrix4f model) {
        shader.use();
        glUniformMatrix4fv(modelLoc, false, model.get(new float[16]));
        glUniform3f(objectColorLoc, color.x, color.y, color.z);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);

        mesh.render();
    }
}
