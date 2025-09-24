package com.ancevt.d3d3.engine.scene;

import com.ancevt.d3d3.engine.render.Camera;
import com.ancevt.d3d3.engine.render.ShaderProgram;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20C.glGetUniformLocation;

public class Skybox {
    private final Mesh mesh;
    private final int cubemapTex;
    private final ShaderProgram shader;

    public Skybox(int cubemapTex, ShaderProgram shader) {
        this.mesh = MeshFactory.createSkyboxCube(); // куб без UV
        this.cubemapTex = cubemapTex;
        this.shader = shader;
    }

    public void render(Camera camera, Matrix4f projection) {
        glDepthFunc(GL_LEQUAL); // чтобы skybox был сзади
        shader.use();

        // view без трансляции (skybox не двигается при перемещении камеры)
        Matrix4f view = new Matrix4f(camera.getViewMatrix());
        view.setTranslation(0, 0, 0);

        int viewLoc = glGetUniformLocation(shader.getId(), "view");
        int projLoc = glGetUniformLocation(shader.getId(), "projection");

        glUniformMatrix4fv(viewLoc, false, view.get(new float[16]));
        glUniformMatrix4fv(projLoc, false, projection.get(new float[16]));

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemapTex);

        mesh.render();

        glDepthFunc(GL_LESS); // вернуть обратно
    }
}
