package com.ancevt.devgame;

import com.ancevt.ash.engine.asset.*;
import com.ancevt.ash.engine.core.Application;
import com.ancevt.ash.engine.core.Engine;
import com.ancevt.ash.engine.core.EngineContext;
import com.ancevt.ash.engine.core.LaunchConfig;
import com.ancevt.ash.engine.render.ShaderProgram;
import com.ancevt.ash.engine.scene.*;
import com.ancevt.ash.engine.util.TextLoader;
import com.ancevt.ash.engine.util.TransformUtil;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;

public class DevGame2 implements Application {


    public static void main(String[] args) {
        new Engine(
                LaunchConfig.builder()
                        .width(2000)
                        .height(1000)
                        .title("Ash Dev")
                        .build()
        ).start(new DevGame2());
    }

    private EngineContext ctx;

    @Override
    public void init(EngineContext ctx) {
        this.ctx = ctx;
        AssetManager assetManager = ctx.getAssetManager();

        // === Skybox ===
        String[] faces = {
                "skybox/right.png",
                "skybox/left.png",
                "skybox/top.png",
                "skybox/bottom.png",
                "skybox/front.png",
                "skybox/back.png"
        };
        int cubemapTex = TextureLoader.loadCubemap(faces);

        ShaderProgram skyboxShader = new ShaderProgram();
        skyboxShader.attachShader(TextLoader.load("shaders/skybox.vert"), GL_VERTEX_SHADER);
        skyboxShader.attachShader(TextLoader.load("shaders/skybox.frag"), GL_FRAGMENT_SHADER);
        skyboxShader.link();

        skyboxShader.use();
        int loc = glGetUniformLocation(skyboxShader.getId(), "skybox");
        glUniform1i(loc, 0);

        Engine.skybox = new Skybox(cubemapTex, skyboxShader);

        // === Atlas ===
        Atlas atlas = new Atlas()
                .addImage("ground", "/texture/ground1.png")
                .addImage("wall", "/texture/wall.png")
                .addImage("sq-tiger", "/texture/sq-tiger.png")
                .build();


        atlas.debugSave("test_atlas.png");


        generateHall(5, 5, 5, 6, atlas);
    }

    public void generateHall(int sizeX, int sizeZ, int sizeY, float cubeSize, Atlas atlas) {
        List<float[]> chunks = new ArrayList<>();
        List<AABB> colliders = new ArrayList<>();

        UVRect wallUV = atlas.getUV("wall");
        UVRect groundUV = atlas.getUV("ground");
        UVRect tigerUV = atlas.getUV("sq-tiger");

        float groundThickness = 0.1f;

        // === Пол ===
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                float[] floorVerts = MeshFactory.createFloorTile(cubeSize, groundThickness, groundUV);
                Matrix4f transform = new Matrix4f()
                        .translate(x * cubeSize, -groundThickness / 2f, z * cubeSize);

                chunks.add(TransformUtil.transformVertices(floorVerts, transform));

                colliders.add(new AABB(
                        new Vector3f(x * cubeSize - cubeSize / 2f, -groundThickness, z * cubeSize - cubeSize / 2f),
                        new Vector3f(x * cubeSize + cubeSize / 2f, 0, z * cubeSize + cubeSize / 2f)
                ));
            }
        }

        // === Потолок ===
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                float[] floorVerts = MeshFactory.createFloorTile(cubeSize, groundThickness, groundUV);
                Matrix4f transform = new Matrix4f()
                        .translate(x * cubeSize, sizeY * cubeSize + groundThickness / 2f, z * cubeSize)
                        .rotate((float) Math.PI, 1, 0, 0);

                chunks.add(TransformUtil.transformVertices(floorVerts, transform));

                colliders.add(new AABB(
                        new Vector3f(x * cubeSize - cubeSize / 2f, sizeY * cubeSize, z * cubeSize - cubeSize / 2f),
                        new Vector3f(x * cubeSize + cubeSize / 2f, sizeY * cubeSize + groundThickness, z * cubeSize + cubeSize / 2f)
                ));
            }
        }

        // === Стены ===
        int midY = sizeY / 2;
        int midX = sizeX / 2;
        int midZ = sizeZ / 2;

        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                for (int z = 0; z < sizeZ; z++) {
                    boolean onBoundary = (x == 0 || x == sizeX - 1 || z == 0 || z == sizeZ - 1);
                    if (!onBoundary) continue;

                    // === Центр передней стены по Z ===
                    boolean isTiger = (z == 0 && x == midX && y == midY);

                    // === Условие окна (не вырезаем тигра) ===
                    boolean isWindow = (y == midY && (x % 3 == 0 || z % 3 == 0)) && !isTiger;
                    if (isWindow) continue;

                    UVRect uv = isTiger ? atlas.getUV("sq-tiger") : wallUV;

                    float[] cubeVerts = MeshFactory.createTexturedCube(cubeSize, uv);
                    Matrix4f transform = new Matrix4f()
                            .translate(x * cubeSize, y * cubeSize + cubeSize / 2f, z * cubeSize);

                    chunks.add(TransformUtil.transformVertices(cubeVerts, transform));

                    colliders.add(new AABB(
                            new Vector3f(x * cubeSize - cubeSize / 2f, y * cubeSize, z * cubeSize - cubeSize / 2f),
                            new Vector3f(x * cubeSize + cubeSize / 2f, y * cubeSize + cubeSize, z * cubeSize + cubeSize / 2f)
                    ));

                    if (isTiger) {
                        System.out.println("🐯 Tiger placed at " + x + "," + y + "," + z);
                    }
                }
            }
        }


        // === Сборка меша ===
        int totalLen = chunks.stream().mapToInt(a -> a.length).sum();
        float[] merged = new float[totalLen];
        int pos = 0;
        for (float[] arr : chunks) {
            System.arraycopy(arr, 0, merged, pos, arr.length);
            pos += arr.length;
        }

        Mesh mesh = new Mesh(merged, 8);
        MazeNode node = new MazeNode(mesh, atlas.getTextureId(), colliders);
        ctx.getEngine().root.addChild(node);
    }



    private boolean[][] generateMaze(int width, int height) {
        boolean[][] maze = new boolean[width][height];

        // Заполнить всё стенами
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                maze[x][z] = true; // стена
            }
        }

        // Начало в (1,1)
        carve(1, 1, maze, width, height);

        return maze;
    }

    private void carve(int x, int z, boolean[][] maze, int width, int height) {
        int[] dx = {2, -2, 0, 0};
        int[] dz = {0, 0, 2, -2};

        Integer[] dirs = {0, 1, 2, 3};
        java.util.Collections.shuffle(java.util.Arrays.asList(dirs));

        maze[x][z] = false; // пустота

        for (int dir : dirs) {
            int nx = x + dx[dir];
            int nz = z + dz[dir];

            if (nx > 0 && nz > 0 && nx < width - 1 && nz < height - 1) {
                if (maze[nx][nz]) {
                    maze[x + dx[dir] / 2][z + dz[dir] / 2] = false; // пробить стену
                    carve(nx, nz, maze, width, height);
                }
            }
        }
    }


    public static GameObject createGround(float size, int textureId, float repeat) {
        float[] vertices = {
                -size, 0, -size, 0, 0, 0, 1, 0,
                size, 0, -size, repeat, 0, 0, 1, 0,
                size, 0, size, repeat, repeat, 0, 1, 0,

                -size, 0, -size, 0, 0, 0, 1, 0,
                size, 0, size, repeat, repeat, 0, 1, 0,
                -size, 0, size, 0, repeat, 0, 1, 0,
        };

        Mesh mesh = new Mesh(vertices, 8);
        return new GameObject(mesh, textureId);
    }


    private GameObjectNode createCastle(String filename, float x, float y, float z) {
        AssetManager assetManager = ctx.getAssetManager();

        OBJModel obj1 = assetManager.loadObj("models/" + filename);
        int tex1 = assetManager.loadTexture("texture/wall.png", true);
        GameObjectNode go1 = new GameObjectNode(obj1.mesh, tex1);
        go1.setPosition(x, y, z);

        go1.setTextureRepeat(5, 5);

        return go1;
    }

    @Override
    public void update() {
        ctx.getEngine().mainLight.getPosition().y += 0.001f;
    }

    @Override
    public void shutdown() {

    }
}
