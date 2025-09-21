package com.ancevt.ash.engine.asset;

import lombok.Getter;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

public class Atlas {

    private final List<ImageData> images = new ArrayList<>();
    private final Map<String, UVRect> uvMap = new HashMap<>();
    private int atlasWidth = 0;
    private int atlasHeight = 0;
    @Getter
    private int textureId = -1;

    public Atlas addImage(String name, String resourcesPath) {
        try (InputStream in = Atlas.class.getResourceAsStream(resourcesPath)) {
            if (in == null) {
                throw new IOException("Resource not found: " + resourcesPath);
            }
            return addImage(name, in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image from resource: " + resourcesPath, e);
        }
    }

    public Atlas addImage(String name, InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes);
            buffer.flip(); // flip обязательно перед передачей
            return addImage(name, buffer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image from InputStream", e);
        }
    }

    public Set<String> getNames() {
        return Set.copyOf(uvMap.keySet());
    }

    public Atlas addImage(String name, ByteBuffer imageBuffer) {
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer c = BufferUtils.createIntBuffer(1);

        ByteBuffer pixels = stbi_load_from_memory(imageBuffer, w, h, c, 4);
        if (pixels == null) {
            throw new RuntimeException("Failed to load image: " + stbi_failure_reason());
        }

        images.add(new ImageData(name, pixels, w.get(0), h.get(0), 4));
        return this;
    }

    public void build() {
        // Реши, какая у тебя максимально допустимая ширина атласа
        atlasWidth = images.stream().mapToInt(img -> img.width).max().orElse(0) * 4;

        // Skyline: список "барханов" (x, y, width)
        List<AtlasSpan> skyline = new ArrayList<>();
        skyline.add(new AtlasSpan(0, 0, atlasWidth));

        int usedHeight = 0;

        // Упаковка
        for (ImageData img : images) {
            // ищем позицию, где картинка влезет
            Pos pos = findPosition(skyline, img.width, img.height);
            if (pos == null) {
                throw new RuntimeException("Does not fit: " + img.name);
            }

            img.x = pos.x;
            img.y = pos.y;
            usedHeight = Math.max(usedHeight, pos.y + img.height);

            // обновляем skyline
            insertSkyline(skyline, pos.x, pos.y + img.height, img.width);
        }

        atlasHeight = usedHeight;
        ByteBuffer atlasBuffer = BufferUtils.createByteBuffer(atlasWidth * atlasHeight * 4);

        // рисуем всё в буфер
        for (ImageData img : images) {
            for (int y = 0; y < img.height; y++) {
                for (int x = 0; x < img.width; x++) {
                    int srcIndex = (y * img.width + x) * 4;
                    int dstIndex = ((img.y + y) * atlasWidth + (img.x + x)) * 4;
                    for (int i = 0; i < 4; i++) {
                        atlasBuffer.put(dstIndex + i, img.pixels.get(srcIndex + i));
                    }
                }
            }
            uvMap.put(img.name, new UVRect(
                    (float) img.x / atlasWidth,
                    (float) img.y / atlasHeight,
                    (float) img.width / atlasWidth,
                    (float) img.height / atlasHeight
            ));
            stbi_image_free(img.pixels);
        }

        // OpenGL
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, atlasWidth, atlasHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, atlasBuffer);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    }

// ==== helpers ====

    private Pos findPosition(List<AtlasSpan> skyline, int w, int h) {
        int bestY = Integer.MAX_VALUE;
        int bestX = -1;

        for (int i = 0; i < skyline.size(); i++) {
            int x = skyline.get(i).x;
            int y = canFit(skyline, i, w, h);
            if (y >= 0 && (y < bestY || (y == bestY && x < bestX))) {
                bestY = y;
                bestX = x;
            }
        }
        if (bestX == -1) return null;
        return new Pos(bestX, bestY);
    }

    private int canFit(List<AtlasSpan> skyline, int index, int w, int h) {
        int x = skyline.get(index).x;
        int y = skyline.get(index).y;
        int spaceLeft = w;

        if (x + w > atlasWidth) return -1;
        int i = index;
        int maxY = y;
        while (spaceLeft > 0) {
            if (i >= skyline.size()) return -1;
            maxY = Math.max(maxY, skyline.get(i).y);
            if (maxY + h > atlasHeight + 10000) return -1; // safety
            spaceLeft -= skyline.get(i).width;
            i++;
        }
        return maxY;
    }

    private void insertSkyline(List<AtlasSpan> skyline, int x, int y, int w) {
        skyline.add(new AtlasSpan(x, y, w));
        skyline.sort(Comparator.comparingInt(s -> s.x));
        // сливаем барханы
        for (int i = 0; i < skyline.size() - 1; i++) {
            AtlasSpan a = skyline.get(i);
            AtlasSpan b = skyline.get(i + 1);
            if (a.y == b.y) {
                a.width += b.width;
                skyline.remove(i + 1);
                i--;
            }
        }
    }

    private static class AtlasSpan {
        int x, y, width;

        AtlasSpan(int x, int y, int width) {
            this.x = x;
            this.y = y;
            this.width = width;
        }
    }

    private record Pos(int x, int y) {
    }


    public boolean isReady() {
        return textureId != -1;
    }

    public void dispose() {
        if (textureId != -1) {
            glDeleteTextures(textureId);
            textureId = -1;
        } else {
            throw new RuntimeException("Texture not created");
        }
    }

    public UVRect getUV(String name) {
        return uvMap.get(name);
    }


    private static class ImageData {
        String name;
        ByteBuffer pixels;
        int width, height, channels;
        int x, y;

        ImageData(String name, ByteBuffer pixels, int width, int height, int channels) {
            this.name = name;
            this.pixels = pixels;
            this.width = width;
            this.height = height;
            this.channels = channels;
        }
    }
}
