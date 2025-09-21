package com.ancevt.ash.engine.asset;

import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

public class Atlas {

    private final List<ImageData> images = new ArrayList<>();
    private final Map<String, UVRect> uvMap = new HashMap<>();
    private int textureId = -1;
    private int atlasWidth;
    private int atlasHeight;
    private ByteBuffer atlasBuffer;

    public Atlas addImage(String name, String resourcePath) {
        try (InputStream in = Atlas.class.getResourceAsStream(resourcePath)) {
            if (in == null) throw new IOException("Not found: " + resourcePath);
            byte[] bytes = in.readAllBytes();
            ByteBuffer buf = BufferUtils.createByteBuffer(bytes.length);
            buf.put(bytes).flip();

            IntBuffer w = BufferUtils.createIntBuffer(1);
            IntBuffer h = BufferUtils.createIntBuffer(1);
            IntBuffer c = BufferUtils.createIntBuffer(1);

            stbi_set_flip_vertically_on_load(true);

            ByteBuffer pixels = stbi_load_from_memory(buf, w, h, c, 4);
            if (pixels == null) throw new RuntimeException("STB fail: " + stbi_failure_reason());

            images.add(new ImageData(name, pixels, w.get(), h.get()));
        } catch (IOException e) {
            throw new RuntimeException("Atlas addImage failed: " + resourcePath, e);
        }
        return this;
    }

    public Atlas build() {
        // складываем по вертикали
        atlasWidth = images.stream().mapToInt(img -> img.width).max().orElse(0);
        atlasHeight = images.stream().mapToInt(img -> img.height).sum();

        atlasBuffer = BufferUtils.createByteBuffer(atlasWidth * atlasHeight * 4);

        int yOffset = 0;
        for (ImageData img : images) {
            for (int y = 0; y < img.height; y++) {
                for (int x = 0; x < img.width; x++) {
                    int src = (y * img.width + x) * 4;
                    int dst = ((yOffset + y) * atlasWidth + x) * 4;
                    for (int i = 0; i < 4; i++) {
                        atlasBuffer.put(dst + i, img.pixels.get(src + i));
                    }
                }
            }
            // считать UV с учётом ширины
            float u = 0f;
            float v = (float) yOffset / atlasHeight;
            float uw = (float) img.width / atlasWidth;
            float vh = (float) img.height / atlasHeight;
            uvMap.put(img.name, new UVRect(u, v, uw, vh));

            yOffset += img.height;
            stbi_image_free(img.pixels);
        }

        // OpenGL upload
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, atlasWidth, atlasHeight, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, atlasBuffer);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glGenerateMipmap(GL_TEXTURE_2D);

        return this;
    }

    public void debugSave(String filePath) {
        if (atlasBuffer == null) throw new IllegalStateException("Build atlas first");
        atlasBuffer.rewind();
        boolean ok = stbi_write_png(filePath, atlasWidth, atlasHeight, 4, atlasBuffer, atlasWidth * 4);
        if (!ok) {
            throw new RuntimeException("Failed to save atlas: " + filePath);
        }
        System.out.println("Atlas saved to " + filePath);
    }

    public UVRect getUV(String name) {
        return uvMap.get(name);
    }

    public int getTextureId() {
        return textureId;
    }

    private static class ImageData {
        final String name;
        final ByteBuffer pixels;
        final int width, height;

        ImageData(String name, ByteBuffer pixels, int width, int height) {
            this.name = name;
            this.pixels = pixels;
            this.width = width;
            this.height = height;
        }
    }
}
