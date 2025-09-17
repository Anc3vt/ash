package com.ancevt.devgame;

import com.ancevt.ash.engine.asset.AssetManager;
import com.ancevt.ash.engine.asset.OBJModel;
import com.ancevt.ash.engine.asset.TextureLoader;
import com.ancevt.ash.engine.core.Application;
import com.ancevt.ash.engine.core.Engine;
import com.ancevt.ash.engine.core.EngineContext;
import com.ancevt.ash.engine.core.LaunchConfig;
import com.ancevt.ash.engine.render.ShaderProgram;
import com.ancevt.ash.engine.scene.*;
import com.ancevt.ash.engine.util.TextLoader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

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
                        .title("Ash Dev")
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

        // === Текстуры ===
        int groundTex = assetManager.loadTexture("texture/ground1.png", true);
        int wallTex = assetManager.loadTexture("texture/wall.png", true);

        // === Генерация многоэтажного лабиринта ===
        generateMultiFloorMaze(
                ctx,
                5,   // ширина
                15,   // глубина,
                5,// этажи
                8f, // размер куба
                8.3f, // высота этажа
                groundTex,
                wallTex
        );
    }


    private void generateMultiFloorMaze(
            EngineContext ctx,
            int mazeWidth,
            int mazeDepth,
            int mazeLevels,
            float cubeSize,
            float levelHeight,
            int groundTex,
            int wallTex
    ) {
        boolean[][][] maze = new boolean[mazeWidth][mazeDepth][mazeLevels];

        // === Генерация всех уровней ===
        for (int y = 0; y < mazeLevels; y++) {
            boolean[][] level = generateMaze(mazeWidth, mazeDepth);

            for (int x = 0; x < mazeWidth; x++) {
                for (int z = 0; z < mazeDepth; z++) {
                    maze[x][z][y] = level[x][z];
                }
            }

            // Добавляем лестницу (пробиваем проход между этажами)
            if (y < mazeLevels - 1) {
                int stairX = 1 + (int) (Math.random() * (mazeWidth - 2));
                int stairZ = 1 + (int) (Math.random() * (mazeDepth - 2));
                maze[stairX][stairZ][y] = false;
                maze[stairX][stairZ][y + 1] = false;
            }
        }

        // === Билдеры для объединённой геометрии ===
        MeshBuilder wallBuilder = new MeshBuilder(8);
        MeshBuilder groundBuilder = new MeshBuilder(8);

        List<AABB> wallColliders = new java.util.ArrayList<>();
        List<AABB> groundColliders = new java.util.ArrayList<>();

        float holeChance = 0.3f; // шанс дырки в полу

        // === Проходим по всем клеткам ===
        for (int y = 0; y < mazeLevels; y++) {
            for (int x = 0; x < mazeWidth; x++) {
                for (int z = 0; z < mazeDepth; z++) {

                    float posX = x * cubeSize - mazeWidth * cubeSize / 2;
                    float posY = y * levelHeight;
                    float posZ = z * cubeSize - mazeDepth * cubeSize / 2;

                    // === Стены ===
                    if (maze[x][z][y]) {
                        wallBuilder.addMesh(
                                MeshFactory.createTexturedCubeMesh(cubeSize),
                                posX,
                                posY + cubeSize / 2, // центр куба
                                posZ
                        );

                        Vector3f min = new Vector3f(
                                posX - cubeSize / 2,
                                posY,
                                posZ - cubeSize / 2
                        );
                        Vector3f max = new Vector3f(
                                posX + cubeSize / 2,
                                posY + cubeSize,
                                posZ + cubeSize / 2
                        );
                        wallColliders.add(new AABB(min, max));
                    }

                    if (Math.random() > holeChance) {
                        float thickness = cubeSize * 0.1f;

                        // === случайные углы наклона ===
                        float rx = (float) Math.toRadians((Math.random() - 0.5) * 10); // -5°..+5°
                        float rz = (float) Math.toRadians((Math.random() - 0.5) * 10);

                        Matrix4f transform = new Matrix4f()
                                .translate(posX, posY - thickness / 2, posZ)
                                .rotateX(rx)
                                .rotateZ(rz);

                        groundBuilder.addMeshTransformed(
                                MeshFactory.createFloorTileMesh(cubeSize, thickness),
                                transform
                        );

                        Vector3f min = new Vector3f(
                                posX - cubeSize / 2,
                                posY - thickness,
                                posZ - cubeSize / 2
                        );
                        Vector3f max = new Vector3f(
                                posX + cubeSize / 2,
                                posY + thickness,
                                posZ + cubeSize / 2
                        );
                        groundColliders.add(new AABB(min, max));
                    }

                }
            }
        }

        // === Финализируем меши ===
        Mesh wallMesh = wallBuilder.build();
        Mesh groundMesh = groundBuilder.build();

        MazeNode mazeNode = new MazeNode(wallMesh, wallTex, wallColliders);
        MazeNode groundNode = new MazeNode(groundMesh, groundTex, groundColliders);

        ctx.getEngine().root.addChild(groundNode);
        ctx.getEngine().root.addChild(mazeNode);
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
