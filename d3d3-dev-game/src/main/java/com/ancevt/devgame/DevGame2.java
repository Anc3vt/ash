package com.ancevt.devgame;

import com.ancevt.d3d3.engine.asset.AssetManager;
import com.ancevt.d3d3.engine.asset.Atlas;
import com.ancevt.d3d3.engine.asset.TextureLoader;
import com.ancevt.d3d3.engine.asset.UVRect;
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
                        .title("D3D3 Dev")
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

        generateLevel(atlas);
    }

    private void generateLevel(Atlas atlas) {
        // Центральный зал
        generateHall(10, 10, 4, 6, atlas, 0, 0, 0);

        // Два боковых крыла
        generateHall(6, 20, 3, 6, atlas, 15 * 6, 0, 0);
        generateHall(6, 20, 3, 6, atlas, -15 * 6, 0, 0);

        // Коридоры на север/юг
        generateHall(4, 12, 3, 6, atlas, 0, 0, 15 * 6);
        generateHall(4, 12, 3, 6, atlas, 0, 0, -15 * 6);

        // Колонны вокруг центрального зала
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                placeColumn(atlas, x * 20, 0, z * 20);
            }
        }

        // Шахматный пол в центре
        placeChessFloor(atlas, 10, 10, 6, 0, 0, 0);
    }

    private void placeColumn(Atlas atlas, float x, float y, float z) {
        UVRect wallUV = atlas.getUV("wall");
        float cubeSize = 2f;
        for (int i = 0; i < 6; i++) {
            float[] cube = MeshFactory.createTexturedCube(cubeSize, wallUV);
            Matrix4f transform = new Matrix4f().translate(x, y + i * cubeSize + cubeSize / 2, z);
            float[] verts = TransformUtil.transformVertices(cube, transform);
            Mesh mesh = new Mesh(verts, 8);
            MazeNode node = new MazeNode(mesh, atlas.getTextureId(), List.of(
                    new AABB(new Vector3f(x - cubeSize/2, y+i*cubeSize, z - cubeSize/2),
                            new Vector3f(x + cubeSize/2, y+(i+1)*cubeSize, z + cubeSize/2))
            ));
            ctx.getEngine().root.addChild(node);
        }
    }

    private void placeChessFloor(Atlas atlas, int sizeX, int sizeZ, float tileSize,
                                 float offsetX, float offsetY, float offsetZ) {
        UVRect groundUV = atlas.getUV("ground");
        UVRect wallUV = atlas.getUV("wall"); // вторая текстура для контраста

        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                boolean dark = (x + z) % 2 == 0;
                UVRect uv = dark ? groundUV : wallUV;
                float[] floorVerts = MeshFactory.createFloorTile(tileSize, 0.1f, uv);
                Matrix4f transform = new Matrix4f().translate(offsetX + x * tileSize, offsetY, offsetZ + z * tileSize);
                float[] verts = TransformUtil.transformVertices(floorVerts, transform);
                Mesh mesh = new Mesh(verts, 8);
                ctx.getEngine().root.addChild(new MazeNode(mesh, atlas.getTextureId(), List.of()));
            }
        }
    }

    private void placeCube(Atlas atlas, String tex, float x, float y, float z) {
        UVRect uv = atlas.getUV(tex);
        float[] verts = MeshFactory.createTexturedCube(6, uv);
        Mesh mesh = new Mesh(verts, 8);
        GameObjectNode node = new GameObjectNode(mesh, atlas.getTextureId());
        node.setPosition(x, y, z);
        ctx.getEngine().root.addChild(node);
    }


    public void generateHall(int sizeX, int sizeZ, int sizeY, float cubeSize, Atlas atlas,
                             float offsetX, float offsetY, float offsetZ) {
        List<float[]> chunks = new ArrayList<>();
        List<AABB> colliders = new ArrayList<>();

        UVRect wallUV = atlas.getUV("wall");
        UVRect groundUV = atlas.getUV("ground");

        float groundThickness = 0.1f;

        // === Пол ===
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                float[] floorVerts = MeshFactory.createFloorTile(cubeSize, groundThickness, groundUV);
                Matrix4f transform = new Matrix4f()
                        .translate(offsetX + x * cubeSize,
                                offsetY - groundThickness / 2f,
                                offsetZ + z * cubeSize);

                chunks.add(TransformUtil.transformVertices(floorVerts, transform));

                colliders.add(new AABB(
                        new Vector3f(offsetX + x * cubeSize - cubeSize / 2f,
                                offsetY - groundThickness,
                                offsetZ + z * cubeSize - cubeSize / 2f),
                        new Vector3f(offsetX + x * cubeSize + cubeSize / 2f,
                                offsetY,
                                offsetZ + z * cubeSize + cubeSize / 2f)
                ));
            }
        }

        // === Потолок ===
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                float[] floorVerts = MeshFactory.createFloorTile(cubeSize, groundThickness, groundUV);
                Matrix4f transform = new Matrix4f()
                        .translate(offsetX + x * cubeSize,
                                offsetY + sizeY * cubeSize + groundThickness / 2f,
                                offsetZ + z * cubeSize)
                        .rotate((float) Math.PI, 1, 0, 0);

                chunks.add(TransformUtil.transformVertices(floorVerts, transform));

                colliders.add(new AABB(
                        new Vector3f(offsetX + x * cubeSize - cubeSize / 2f,
                                offsetY + sizeY * cubeSize,
                                offsetZ + z * cubeSize - cubeSize / 2f),
                        new Vector3f(offsetX + x * cubeSize + cubeSize / 2f,
                                offsetY + sizeY * cubeSize + groundThickness,
                                offsetZ + z * cubeSize + cubeSize / 2f)
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
                            .translate(offsetX + x * cubeSize,
                                    offsetY + y * cubeSize + cubeSize / 2f,
                                    offsetZ + z * cubeSize);

                    chunks.add(TransformUtil.transformVertices(cubeVerts, transform));

                    colliders.add(new AABB(
                            new Vector3f(offsetX + x * cubeSize - cubeSize / 2f,
                                    offsetY + y * cubeSize,
                                    offsetZ + z * cubeSize - cubeSize / 2f),
                            new Vector3f(offsetX + x * cubeSize + cubeSize / 2f,
                                    offsetY + y * cubeSize + cubeSize,
                                    offsetZ + z * cubeSize + cubeSize / 2f)
                    ));

                    if (isTiger) {
                        System.out.println("🐯 Tiger placed at " + (offsetX + x * cubeSize) +
                                "," + (offsetY + y * cubeSize) +
                                "," + (offsetZ + z * cubeSize));
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



    @Override
    public void update() {
        ctx.getEngine().mainLight.getPosition().y += 0.001f;
    }

    @Override
    public void shutdown() {

    }
}
