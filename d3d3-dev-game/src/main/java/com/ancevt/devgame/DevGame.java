package com.ancevt.devgame;

import com.ancevt.d3d3.engine.asset.*;
import com.ancevt.d3d3.engine.core.Application;
import com.ancevt.d3d3.engine.core.Engine;
import com.ancevt.d3d3.engine.core.EngineContext;
import com.ancevt.d3d3.engine.core.LaunchConfig;
import com.ancevt.d3d3.engine.render.ShaderProgram;
import com.ancevt.d3d3.engine.scene.*;
import com.ancevt.d3d3.engine.util.TextLoader;
import com.ancevt.d3d3.engine.util.TransformUtil;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;

public class DevGame implements Application {


    public static void main(String[] args) {
        new Engine(
                LaunchConfig.builder()
                        .width(2000)
                        .height(1000)
                        .title("D3D3 Dev")
                        .build()
        ).start(new DevGame());
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


        // === Генерация многоэтажного лабиринта ===
        generateMultiFloorMaze(
                20,   // ширина X
                20,   // ширина Z
                20,
                6,   // размер куба
                atlas // передаём атлас
        );
    }


    public void generateMultiFloorMaze(int sizeX, int sizeZ, int sizeY, float cubeSize, Atlas atlas) {
        Random rand = new Random();

        List<float[]> chunks = new ArrayList<>();
        List<AABB> colliders = new ArrayList<>();

        UVRect wallUV = atlas.getUV("wall");
        UVRect groundUV = atlas.getUV("ground");

        float groundThickness = 0.1f;

        float wallDensity = 0.4f;   // вероятность появления стены (40%)
        float holeChance = 0.3f;    // вероятность дыры в полу (30%)

        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                for (int z = 0; z < sizeZ; z++) {

                    boolean isWall = rand.nextFloat() < wallDensity;

                    if (isWall) {
                        // === СТЕНА ===
                        float[] cubeVerts = MeshFactory.createTexturedCube(cubeSize, wallUV);

                        Matrix4f transform = new Matrix4f()
                                .translate(x * cubeSize, y * cubeSize + cubeSize / 2f, z * cubeSize);

                        chunks.add(TransformUtil.transformVertices(cubeVerts, transform));

                        colliders.add(new AABB(
                                new Vector3f(
                                        x * cubeSize - cubeSize / 2f,
                                        y * cubeSize,
                                        z * cubeSize - cubeSize / 2f
                                ),
                                new Vector3f(
                                        x * cubeSize + cubeSize / 2f,
                                        y * cubeSize + cubeSize,
                                        z * cubeSize + cubeSize / 2f
                                )
                        ));

                    } else {
                        // === ПОЛ ===
                        boolean makeHole = (y > 0) && (rand.nextFloat() < holeChance);

                        if (!makeHole) {
                            float[] floorVerts = MeshFactory.createFloorTile(cubeSize, groundThickness, groundUV);

                            Matrix4f transform = new Matrix4f()
                                    .translate(x * cubeSize, y * cubeSize - groundThickness / 2f, z * cubeSize);

                            chunks.add(TransformUtil.transformVertices(floorVerts, transform));

                            colliders.add(new AABB(
                                    new Vector3f(
                                            x * cubeSize - cubeSize / 2f,
                                            y * cubeSize - groundThickness,
                                            z * cubeSize - cubeSize / 2f
                                    ),
                                    new Vector3f(
                                            x * cubeSize + cubeSize / 2f,
                                            y * cubeSize,
                                            z * cubeSize + cubeSize / 2f
                                    )
                            ));
                        }
                    }

                }
            }
        }

        // === Сборка в один меш ===
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
