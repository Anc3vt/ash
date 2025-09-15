package com.ancevt.ash.engine.scene;

import com.ancevt.ash.engine.render.Camera;
import com.ancevt.ash.engine.render.Light;
import com.ancevt.ash.engine.render.ShaderProgram;
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

    public void applyLight(Light light) {
        int lightPosLoc = glGetUniformLocation(shader.getId(), "lightPos");
        int lightColorLoc = glGetUniformLocation(shader.getId(), "lightColor");
        int viewPosLoc = glGetUniformLocation(shader.getId(), "viewPos");

        Vector3f pos = light.getPosition();
        Vector3f col = new Vector3f(light.getColor()).mul(light.getIntensity());

        glUniform3f(lightPosLoc, pos.x, pos.y, pos.z);
        glUniform3f(lightColorLoc, col.x, col.y, col.z);

        Vector3f camPos = camera.getPosition();
        glUniform3f(viewPosLoc, camPos.x, camPos.y, camPos.z);
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
