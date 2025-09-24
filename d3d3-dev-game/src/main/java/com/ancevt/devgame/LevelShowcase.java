package com.ancevt.devgame;

import com.ancevt.d3d3.engine.asset.Atlas;
import com.ancevt.d3d3.engine.asset.UVRect;
import com.ancevt.d3d3.engine.core.EngineContext;
import com.ancevt.d3d3.engine.scene.*;
import com.ancevt.d3d3.engine.util.TransformUtil;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LevelShowcase {

    private final EngineContext ctx;
    private final Random rnd = new Random(1337); // фиксированный seed

    public LevelShowcase(EngineContext ctx) {
        this.ctx = ctx;
    }

    public void build(Atlas atlas, float unit) {
        List<float[]> chunks = new ArrayList<>();
        List<AABB> colliders = new ArrayList<>();

        UVRect UV_WALL = atlas.getUV("wall");
        UVRect UV_GROUND = atlas.getUV("ground");
        UVRect UV_TIGER = atlas.getUV("sq-tiger");

        float groundH = 3f;

        // ---- параметры зоны
        float ox = 0, oy = 0, oz = 0;

        // 1) Центральная площадь с мозаикой
        int plazaX = 48, plazaZ = 48;
        addMosaicFloor(chunks, colliders, UV_GROUND, unit, groundH, ox, oy, oz, plazaX, plazaZ);

        // 2) Периметр — колоннада (столбики) + низкая стена
        addColonnade(chunks, colliders, UV_WALL, unit, ox, oy, oz, plazaX, plazaZ, 4);

        // 3) Арочные стены на входах (с «окнами»)
        addArches(chunks, colliders, UV_WALL, UV_TIGER, unit, ox, oy, oz, plazaX, plazaZ, 6, 8);

        // 4) Две лестницы на террасы (слева/справа)
        addStairs(chunks, colliders, UV_WALL, UV_GROUND, unit, groundH,
                ox - unit * 10, oy, oz + unit * 4, 10, 8, +1);
        addStairs(chunks, colliders, UV_WALL, UV_GROUND, unit, groundH,
                ox + unit * (plazaX + 2), oy, oz + unit * (plazaZ - 12), 10, 8, -1);

        // 5) Мостики через «ямы/воду»
        addPitsAndBridges(chunks, colliders, UV_GROUND, UV_WALL, unit, groundH,
                ox + unit * 4, oy, oz + unit * (plazaZ / 2 - 6), 8, 6);
        addPitsAndBridges(chunks, colliders, UV_GROUND, UV_WALL, unit, groundH,
                ox + unit * (plazaX - 12), oy, oz + unit * (plazaZ / 2 + 2), 8, 6);

        // 6) Узкие коридоры-каньоны, ведущие в «залы»
        addCanyonCorridor(chunks, colliders, UV_WALL, UV_GROUND, unit, groundH,
                ox + unit * (plazaX + 8), oy, oz + unit * 4, 18, 3, 6);
        addCanyonCorridor(chunks, colliders, UV_WALL, UV_GROUND, unit, groundH,
                ox - unit * (18), oy, oz + unit * (plazaZ - 8), 18, 3, 6);

        // 7) Два «зала»: решётка колонн + ажурные стены
        addHall(chunks, colliders, UV_WALL, UV_GROUND, unit, groundH,
                ox + unit * (plazaX + 8 + 18), oy, oz + unit * 2, 16, 12, 6);
        addHall(chunks, colliders, UV_WALL, UV_GROUND, unit, groundH,
                ox - unit * (18 + 16), oy, oz + unit * (plazaZ - 14), 16, 12, 6);

        // 8) Несколько «скульптур»/обелисков на площади
        addObelisks(chunks, colliders, UV_TIGER, unit, ox + unit * 6, oy, oz + unit * 6);
        addObelisks(chunks, colliders, UV_TIGER, unit, ox + unit * (plazaX - 10), oy, oz + unit * (plazaZ - 10));

        // ---- мерджим и добавляем в сцену
        int total = chunks.stream().mapToInt(a -> a.length).sum();
        float[] merged = new float[total];
        int p = 0;
        for (float[] a : chunks) {
            System.arraycopy(a, 0, merged, p, a.length);
            p += a.length;
        }

        Mesh mesh = new Mesh(merged, 8);
        MazeNode node = new MazeNode(mesh, atlas.getTextureId(), colliders);
        ctx.getEngine().root.addChild(node);
    }

    // ---------- Блоки-помощники ----------

    private void addMosaicFloor(List<float[]> chunks, List<AABB> col, UVRect uv, float unit, float h,
                                float ox, float oy, float oz, int sx, int sz) {
        for (int x = 0; x < sx; x++) {
            for (int z = 0; z < sz; z++) {
                float[] tile = MeshFactory.createFloorTile(unit, h, uv);
                Matrix4f tr = new Matrix4f()
                        .translate(ox + x * unit, oy - h / 2f, oz + z * unit)
                        .rotate((x + z) % 2 == 0 ? 0f : (float) Math.PI, 0, 1, 0); // простая «мозаика»
                chunks.add(TransformUtil.transformVertices(tile, tr));
                addBox(col,
                        ox + x * unit - unit / 2f, oy - h, oz + z * unit - unit / 2f,
                        ox + x * unit + unit / 2f, oy, oz + z * unit + unit / 2f);
            }
        }
    }

    private void addColonnade(List<float[]> chunks, List<AABB> col, UVRect uv, float unit,
                              float ox, float oy, float oz, int sx, int sz, int height) {
        // столбики по периметру каждые 3 клетки
        for (int x = 0; x < sx; x++) {
            for (int z = 0; z < sz; z++) {
                boolean border = (x == 0 || z == 0 || x == sx - 1 || z == sz - 1);
                if (!border) continue;
                if ((x + z) % 3 != 0) continue;

                for (int y = 0; y < height; y++) {
                    float[] cube = MeshFactory.createTexturedCube(unit, uv);
                    Matrix4f tr = new Matrix4f().translate(
                            ox + x * unit,
                            oy + y * unit + unit / 2f,
                            oz + z * unit
                    );
                    chunks.add(TransformUtil.transformVertices(cube, tr));
                    addBox(col,
                            ox + x * unit - unit / 2f, oy + y * unit, oz + z * unit - unit / 2f,
                            ox + x * unit + unit / 2f, oy + (y + 1) * unit, oz + z * unit + unit / 2f);
                }
            }
        }

        // низкая стена (по внутренней стороне периметра)
        for (int x = 1; x < sx - 1; x++) {
            for (int side = 0; side < 2; side++) {
                int z = (side == 0) ? 1 : sz - 2;
                float[] cube = MeshFactory.createTexturedCube(unit, uv);
                Matrix4f tr = new Matrix4f().translate(ox + x * unit, oy + unit / 2f, oz + z * unit);
                chunks.add(TransformUtil.transformVertices(cube, tr));
                addBox(col,
                        ox + x * unit - unit / 2f, oy, oz + z * unit - unit / 2f,
                        ox + x * unit + unit / 2f, oy + unit, oz + z * unit + unit / 2f);
            }
        }
        for (int z = 1; z < sz - 1; z++) {
            for (int side = 0; side < 2; side++) {
                int x = (side == 0) ? 1 : sx - 2;
                float[] cube = MeshFactory.createTexturedCube(unit, uv);
                Matrix4f tr = new Matrix4f().translate(ox + x * unit, oy + unit / 2f, oz + z * unit);
                chunks.add(TransformUtil.transformVertices(cube, tr));
                addBox(col,
                        ox + x * unit - unit / 2f, oy, oz + z * unit - unit / 2f,
                        ox + x * unit + unit / 2f, oy + unit, oz + z * unit + unit / 2f);
            }
        }
    }

    private void addArches(List<float[]> chunks, List<AABB> col, UVRect uvWall, UVRect uvCenter,
                           float unit, float ox, float oy, float oz,
                           int sx, int sz, int archH, int archSpan) {
        // север/юг
        for (int x = sx / 2 - archSpan / 2; x <= sx / 2 + archSpan / 2; x++) {
            for (int h = 0; h < archH; h++) {
                boolean window = (h == archH / 2) && (x % 2 == 0);
                UVRect uv = window ? uvCenter : uvWall;

                // северная стенка
                placeCube(chunks, col, uv, unit,
                        ox + x * unit, oy + h * unit + unit / 2f, oz - unit);

                // южная стенка
                placeCube(chunks, col, uv, unit,
                        ox + x * unit, oy + h * unit + unit / 2f, oz + sz * unit);
            }
        }

        // запад/восток
        for (int z = sz / 2 - archSpan / 2; z <= sz / 2 + archSpan / 2; z++) {
            for (int h = 0; h < archH; h++) {
                boolean window = (h == archH / 2) && (z % 2 == 1);
                UVRect uv = window ? uvCenter : uvWall;

                // западная стенка
                placeCube(chunks, col, uv, unit,
                        ox - unit, oy + h * unit + unit / 2f, oz + z * unit);

                // восточная стенка
                placeCube(chunks, col, uv, unit,
                        ox + sx * unit, oy + h * unit + unit / 2f, oz + z * unit);
            }
        }
    }

    private void addStairs(List<float[]> chunks, List<AABB> col, UVRect uvWall, UVRect uvGround,
                           float unit, float groundH, float ox, float oy, float oz,
                           int len, int width, int dir) {
        // ступени из кубов + промежутки пола
        for (int i = 0; i < len; i++) {
            float y = oy + i * (unit * 0.6f);
            float z = oz + i * unit * dir;

            // площадка пола
            for (int w = 0; w < width; w++) {
                float[] tile = MeshFactory.createFloorTile(unit, groundH, uvGround);
                Matrix4f trF = new Matrix4f().translate(ox + w * unit, y - groundH / 2f, z);
                chunks.add(TransformUtil.transformVertices(tile, trF));
                addBox(col,
                        ox + w * unit - unit / 2f, y - groundH, z - unit / 2f,
                        ox + w * unit + unit / 2f, y, z + unit / 2f);
            }

            // ступенька борта
            for (int side = -1; side <= width; side += width + 1) {
                float[] cube = MeshFactory.createTexturedCube(unit, uvWall);
                Matrix4f tr = new Matrix4f().translate(ox + side * unit, y + unit / 2f, z);
                chunks.add(TransformUtil.transformVertices(cube, tr));
                addBox(col,
                        ox + side * unit - unit / 2f, y, z - unit / 2f,
                        ox + side * unit + unit / 2f, y + unit, z + unit / 2f);
            }
        }
    }

    private void addPitsAndBridges(List<float[]> chunks, List<AABB> col,
                                   UVRect uvGround, UVRect uvWall,
                                   float unit, float groundH,
                                   float ox, float oy, float oz, int sx, int sz) {
        // яма — просто не кладём часть пола и при этом делаем «бордюры»
        for (int x = 0; x < sx; x++) {
            for (int z = 0; z < sz; z++) {
                boolean hole = (x > 1 && x < sx - 2 && z > 1 && z < sz - 2);
                if (!hole || (x == sx / 2)) { // мостик по центру X
                    float[] tile = MeshFactory.createFloorTile(unit, groundH, uvGround);
                    Matrix4f tr = new Matrix4f().translate(ox + x * unit, oy - groundH / 2f, oz + z * unit);
                    chunks.add(TransformUtil.transformVertices(tile, tr));
                    addBox(col,
                            ox + x * unit - unit / 2f, oy - groundH, oz + z * unit - unit / 2f,
                            ox + x * unit + unit / 2f, oy, oz + z * unit + unit / 2f);
                }
            }
        }

        // борта «ямы» кубами
        for (int x = 1; x < sx - 1; x++) {
            placeCube(chunks, col, uvWall, unit, ox + x * unit, oy + unit / 2f, oz + 1 * unit);
            placeCube(chunks, col, uvWall, unit, ox + x * unit, oy + unit / 2f, oz + (sz - 2) * unit);
        }
        for (int z = 1; z < sz - 1; z++) {
            placeCube(chunks, col, uvWall, unit, ox + 1 * unit, oy + unit / 2f, oz + z * unit);
            placeCube(chunks, col, uvWall, unit, ox + (sx - 2) * unit, oy + unit / 2f, oz + z * unit);
        }
    }

    private void addCanyonCorridor(List<float[]> chunks, List<AABB> col, UVRect uvWall, UVRect uvGround,
                                   float unit, float groundH, float ox, float oy, float oz,
                                   int len, int width, int height) {
        // пол
        for (int i = 0; i < len; i++) {
            for (int w = 0; w < width; w++) {
                float[] tile = MeshFactory.createFloorTile(unit, groundH, uvGround);
                Matrix4f tr = new Matrix4f().translate(ox + i * unit, oy - groundH / 2f, oz + w * unit);
                chunks.add(TransformUtil.transformVertices(tile, tr));
                addBox(col,
                        ox + i * unit - unit / 2f, oy - groundH, oz + w * unit - unit / 2f,
                        ox + i * unit + unit / 2f, oy, oz + w * unit + unit / 2f);
            }
        }
        // стены-каньона
        for (int i = 0; i < len; i++) {
            for (int h = 0; h < height; h++) {
                // левая
                placeCube(chunks, col, uvWall, unit,
                        ox + i * unit, oy + h * unit + unit / 2f, oz - unit);
                // правая
                placeCube(chunks, col, uvWall, unit,
                        ox + i * unit, oy + h * unit + unit / 2f, oz + width * unit);
            }
        }
    }

    private void addHall(List<float[]> chunks, List<AABB> col, UVRect uvWall, UVRect uvGround,
                         float unit, float groundH, float ox, float oy, float oz,
                         int sx, int sz, int h) {
        // пол
        addMosaicFloor(chunks, col, uvGround, unit, groundH, ox, oy, oz, sx, sz);

        // сетка колонн 3x3 шаг
        for (int x = 2; x < sx - 2; x += 3) {
            for (int z = 2; z < sz - 2; z += 3) {
                int height = h + (x + z) % 3;
                for (int yy = 0; yy < height; yy++) {
                    placeCube(chunks, col, uvWall, unit,
                            ox + x * unit, oy + yy * unit + unit / 2f, oz + z * unit);
                }
            }
        }

        // ажурные стены по периметру
        for (int x = 0; x < sx; x++) {
            for (int yy = 0; yy < h; yy++) {
                boolean window = yy == h / 2 && (x % 2 == 0);
                if (x == 0 || x == sx - 1) {
                    placeCube(chunks, col, window ? uvGround : uvWall, unit,
                            ox + x * unit, oy + yy * unit + unit / 2f, oz - unit);
                    placeCube(chunks, col, window ? uvGround : uvWall, unit,
                            ox + x * unit, oy + yy * unit + unit / 2f, oz + sz * unit);
                }
            }
        }
        for (int z = 0; z < sz; z++) {
            for (int yy = 0; yy < h; yy++) {
                boolean window = yy == h / 2 && (z % 2 == 1);
                if (z == 0 || z == sz - 1) {
                    placeCube(chunks, col, window ? uvGround : uvWall, unit,
                            ox - unit, oy + yy * unit + unit / 2f, oz + z * unit);
                    placeCube(chunks, col, window ? uvGround : uvWall, unit,
                            ox + sx * unit, oy + yy * unit + unit / 2f, oz + z * unit);
                }
            }
        }
    }

    private void addObelisks(List<float[]> chunks, List<AABB> col, UVRect uv, float unit, float ox, float oy, float oz) {
        int pillars = 5 + rnd.nextInt(5);
        for (int i = 0; i < pillars; i++) {
            int h = 3 + rnd.nextInt(5);
            float px = ox + rnd.nextInt(10) * unit;
            float pz = oz + rnd.nextInt(10) * unit;
            for (int y = 0; y < h; y++) {
                placeCube(chunks, col, uv, unit, px, oy + y * unit + unit / 2f, pz);
            }
        }
    }

    private void placeCube(List<float[]> chunks, List<AABB> col, UVRect uv, float unit,
                           float x, float y, float z) {
        float[] cube = MeshFactory.createTexturedCube(unit, uv);
        Matrix4f tr = new Matrix4f().translate(x, y, z);
        chunks.add(TransformUtil.transformVertices(cube, tr));
        addBox(col, x - unit / 2f, y - unit / 2f, z - unit / 2f, x + unit / 2f, y + unit / 2f, z + unit / 2f);
    }

    private void addBox(List<AABB> col, float minx, float miny, float minz, float maxx, float maxy, float maxz) {
        col.add(new AABB(new Vector3f(minx, miny, minz), new Vector3f(maxx, maxy, maxz)));
    }
}
