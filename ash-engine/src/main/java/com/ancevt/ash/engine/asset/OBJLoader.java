package com.ancevt.ash.engine.asset;

import com.ancevt.ash.engine.scene.Mesh;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class OBJLoader {

    public static OBJModel load(String resourcePath) {
        List<Vector3f> positions = new ArrayList<>();
        List<Vector2f> texCoords = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();

        List<Float> verticesList = new ArrayList<>();

        OBJModel objModel = new OBJModel();

        try (InputStream in = OBJLoader.class.getClassLoader().getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("v ")) {
                    String[] tokens = line.split("\\s+");
                    positions.add(new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    ));
                } else if (line.startsWith("vt ")) {
                    String[] tokens = line.split("\\s+");
                    texCoords.add(new Vector2f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2])
                    ));
                } else if (line.startsWith("vn ")) {
                    String[] tokens = line.split("\\s+");
                    normals.add(new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    ));
                } else if (line.startsWith("f ")) {
                    String[] tokens = line.split("\\s+");
                    for (int i = 1; i <= 3; i++) { // треугольник
                        String[] parts = tokens[i].split("/");
                        int posIndex = Integer.parseInt(parts[0]) - 1;
                        int texIndex = parts.length > 1 && !parts[1].isEmpty() ? Integer.parseInt(parts[1]) - 1 : -1;
                        int normIndex = parts.length > 2 ? Integer.parseInt(parts[2]) - 1 : -1;

                        Vector3f pos = positions.get(posIndex);
                        Vector2f tex;
                        if (texIndex >= 0 && texIndex < texCoords.size() && texCoords.size() > 1) {
                            // использовать UV только если их несколько
                            tex = texCoords.get(texIndex);
                        } else {
                            // генерим UV по X и Y, чтобы текстура легла как кирпич
                            tex = new Vector2f(pos.x * 0.2f, pos.y * 0.2f);
                        }

                        Vector3f norm = normIndex >= 0 ? normals.get(normIndex) : new Vector3f(0, 0, 1);

                        // posXYZ (3) + texUV (2) + normXYZ (3) = stride 8
                        verticesList.add(pos.x);
                        verticesList.add(pos.y);
                        verticesList.add(pos.z);

                        verticesList.add(tex.x);
                        verticesList.add(tex.y);

                        verticesList.add(norm.x);
                        verticesList.add(norm.y);
                        verticesList.add(norm.z);
                    }
                } else if (line.startsWith("mtllib ")) {
                    String[] tokens = line.split("\\s+");
                    String mtlFile = tokens[1];
                    objModel.textureFile = parseMTL(resourcePath.replace(".obj", ".mtl"));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load OBJ file: " + resourcePath, e);
        }

        float[] vertices = new float[verticesList.size()];
        for (int i = 0; i < verticesList.size(); i++) {
            vertices[i] = verticesList.get(i);
        }

        objModel.mesh = new Mesh(vertices, 8);
        return objModel;
    }

    private static String parseMTL(String mtlResourcePath) {
        try (InputStream in = OBJLoader.class.getClassLoader().getResourceAsStream(mtlResourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("map_Kd ")) {
                    String[] tokens = line.split("\\s+");
                    return tokens[1]; // например "monu.png"
                }
            }
        } catch (IOException ignored) {}
        return null;
    }
}
