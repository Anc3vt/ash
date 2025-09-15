package com.ancevt.ash.engine.asset;

import com.ancevt.ash.engine.core.Engine;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12C.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.stbi_image_free;

public class TextureLoader {

    public static int loadTextureFromResources(String resourcePath, boolean repeat) {
        ByteBuffer imageBuffer;
        try (InputStream in = Engine.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            byte[] data = in.readAllBytes();
            imageBuffer = MemoryUtil.memAlloc(data.length);
            imageBuffer.put(data).flip();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + resourcePath, e);
        }

        int textureId;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(true);
            ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, w, h, channels, 4);
            MemoryUtil.memFree(imageBuffer);

            if (image == null) {
                throw new RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason());
            }

            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w.get(), h.get(), 0,
                    GL_RGBA, GL_UNSIGNED_BYTE, image);
            glGenerateMipmap(GL_TEXTURE_2D);

            // фильтрация
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            // управление повторением
            int wrap = repeat ? GL_REPEAT : GL_CLAMP_TO_EDGE;
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrap);

            stbi_image_free(image);
        }
        return textureId;
    }

    public static int loadCubemap(String[] faces) {
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureID);

        for (int i = 0; i < faces.length; i++) {
            ByteBuffer imageBuffer;
            try (InputStream in = Engine.class.getClassLoader().getResourceAsStream(faces[i])) {
                if (in == null) {
                    throw new IOException("Cubemap face not found: " + faces[i]);
                }
                byte[] data = in.readAllBytes();
                imageBuffer = MemoryUtil.memAlloc(data.length);
                imageBuffer.put(data).flip();
            } catch (IOException e) {
                throw new RuntimeException("Failed to load cubemap face: " + faces[i], e);
            }

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(1);

                STBImage.stbi_set_flip_vertically_on_load(false); // для cubemap не переворачиваем
                ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, w, h, channels, 0);
                MemoryUtil.memFree(imageBuffer);

                if (image == null) {
                    throw new RuntimeException("Failed to load cubemap face: " + faces[i] +
                            " reason: " + STBImage.stbi_failure_reason());
                }

                int format = (channels.get(0) == 3) ? GL_RGB : GL_RGBA;

                // 🔍 Отладочный вывод
                System.out.printf("Cubemap face %s loaded: %dx%d, channels=%d, format=%s%n",
                        faces[i], w.get(0), h.get(0), channels.get(0),
                        (format == GL_RGB ? "RGB" : "RGBA"));

                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                        0, format, w.get(0), h.get(0),
                        0, format, GL_UNSIGNED_BYTE, image);

                stbi_image_free(image);
            }
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        return textureID;
    }






}
