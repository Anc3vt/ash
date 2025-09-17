package com.ancevt.ash.engine.core;

import com.ancevt.ash.engine.asset.AssetManager;
import com.ancevt.ash.engine.render.Camera;
import com.ancevt.ash.engine.render.DefaultShaders;
import com.ancevt.ash.engine.render.Light;
import com.ancevt.ash.engine.render.ShaderProgram;
import com.ancevt.ash.engine.scene.*;
import com.ancevt.ash.engine.window.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class Engine {
    public static final int TARGET_FPS = 60;

    public static Skybox skybox;
    private final LaunchConfig launchConfig;
    private Window window;
    private ShaderProgram shader;
    private Camera camera;


    public static Camera cameraTmp;

    private double lastMouseX, lastMouseY;
    private boolean firstMouse = true;

    private final float cameraSpeed = 19f;
    private final float mouseSensitivity = 0.1f;

    // Engine.java
    private float velocityY = 0.0f;
    private final float gravity = -38f;
    private final float jumpStrength = 27f;
    private boolean isGrounded = false;

    private GameObject groundObj = null; // объект, на котором стоит игрок

    private Vector3f playerSize = new Vector3f(0.1f, 4f, 0.1f); // ширина, высота, глубина

    public Node root;
    public Light mainLight;
    private Application application;

    private int frames = 0;
    private long lastTime = System.currentTimeMillis();
    private int fps = 0;

    public Engine(LaunchConfig launchConfig) {
        this.launchConfig = launchConfig;
    }

    public void start(Application application) {
        this.application = application;
        window = new Window(
                launchConfig.getWidth(),
                launchConfig.getHeight(),
                launchConfig.getTitle()
        );
        window.init();

        prepareEngine();

        application.init(createContext());

        loop();

        application.shutdown();
    }

    private void prepareEngine() {
        camera = new Camera();
        cameraTmp = camera;

        camera.getPosition().y = 30;

        root = new Node();

        // === Шейдеры ===
        shader = new ShaderProgram();
        shader.attachShader(DefaultShaders.VERTEX, GL_VERTEX_SHADER);
        shader.attachShader(DefaultShaders.FRAGMENT, GL_FRAGMENT_SHADER);
        shader.link();

        glUseProgram(shader.getId());
        glUniform1i(glGetUniformLocation(shader.getId(), "texture1"), 0);

        glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        glfwSetCursorPosCallback(window.getWindowHandle(), (handle, xpos, ypos) -> {
            if (firstMouse) {
                lastMouseX = xpos;
                lastMouseY = ypos;
                firstMouse = false;
            }

            float dx = (float) (xpos - lastMouseX) * mouseSensitivity;
            float dy = (float) (lastMouseY - ypos) * mouseSensitivity;

            lastMouseX = xpos;
            lastMouseY = ypos;

            camera.addRotation(dx, dy);
        });


        mainLight = new Light(
                new Vector3f(1.2f, 21.0f, 2.0f),
                new Vector3f(1.0f, 1.0f, 1.0f),
                1.0f
        );
    }

    private EngineContext createContext() {
        EngineContext engineContext = new EngineContext(
                this,
                launchConfig,
                new AssetManager()
        );

        return engineContext;
    }

    private void loop() {
        glClearColor(0.53f, 0.81f, 0.92f, 1.0f);

        float fov = (float) Math.toRadians(70.0);
        float aspect = (float) launchConfig.getWidth() / (float) launchConfig.getHeight();
        float zNear = 0.01f, zFar = 1000f;


        long lastFrameTime = System.nanoTime();
        long targetFrameTime = 1000000000L / TARGET_FPS;
        while (!window.shouldClose()) {
            long currentFrameTime = System.nanoTime();
            float deltaTime = (currentFrameTime - lastFrameTime) / 1_000_000_000.0f;
            lastFrameTime = currentFrameTime;

            // Очистка экрана
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH_TEST);

            // Управление камерой/клавиатура/мышь
            processInput(deltaTime);

            // Шейдер активируем
            shader.use();

            // === Матрицы проекции и вида ===
            Matrix4f projection = new Matrix4f().perspective(fov, aspect, zNear, zFar);
            Matrix4f view = camera.getViewMatrix();

            int projLoc = glGetUniformLocation(shader.getId(), "projection");
            int viewLoc = glGetUniformLocation(shader.getId(), "view");

            try (var stack = MemoryStack.stackPush()) {
                glUniformMatrix4fv(projLoc, false, projection.get(stack.mallocFloat(16)));
                glUniformMatrix4fv(viewLoc, false, view.get(stack.mallocFloat(16)));
            }

            // === Источник света ===
            int lightPosLoc = glGetUniformLocation(shader.getId(), "lightPos");
            int viewPosLoc = glGetUniformLocation(shader.getId(), "viewPos");
            int lightColorLoc = glGetUniformLocation(shader.getId(), "lightColor");

            try (var stack = MemoryStack.stackPush()) {
                glUniform3fv(lightPosLoc, new float[]{1.2f, 1.0f, 2.0f});
                glUniform3fv(viewPosLoc, camera.getPosition().get(stack.mallocFloat(3)));
                glUniform3f(lightColorLoc, 1.0f, 1.0f, 1.0f);
            }

            // === Обновление и рендер всего дерева ===
            float time = (System.currentTimeMillis() % 100000) / 1000.0f;

            root.update(time);

            if (skybox != null) {
                skybox.render(camera, projection);
            }

            RenderContext ctxRender = new RenderContext(shader, camera, projection);
            ctxRender.applyLight(mainLight);
            root.render(ctxRender);

            root.render(ctxRender);

            long endTime = System.nanoTime();
            long frameTime = endTime - currentFrameTime;
            long remainingTime = targetFrameTime - frameTime;

            if (remainingTime > 0) {
                try {
                    Thread.sleep(remainingTime / 1_000_000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // === Смена кадров ===
            window.update();

            frames++;
            long now = System.currentTimeMillis();
            if (now - lastTime >= 1000) { // раз в секунду
                fps = frames;
                frames = 0;
                lastTime = now;

                // обновляем заголовок окна
                glfwSetWindowTitle(window.getWindowHandle(),
                        launchConfig.getTitle() + " | FPS: " + fps);
            }

            application.update();
        }


    }


    private final float stepHeight = 0.4f; // максимальная высота, на которую можно "шагнуть"

    private void processInput(float deltaTime) {
        long win = window.getWindowHandle();



        // направления
        Vector3f front = camera.getFront();
        Vector3f right = front.cross(new Vector3f(0, 1, 0), new Vector3f()).normalize();

        // движение по плоскости
        Vector3f moveDir = new Vector3f();
        if (glfwGetKey(win, GLFW_KEY_W) == GLFW_PRESS)
            moveDir.add(new Vector3f(front).mul(cameraSpeed));
        if (glfwGetKey(win, GLFW_KEY_S) == GLFW_PRESS)
            moveDir.add(new Vector3f(front).mul(-cameraSpeed));
        if (glfwGetKey(win, GLFW_KEY_A) == GLFW_PRESS)
            moveDir.add(new Vector3f(right).mul(-cameraSpeed));
        if (glfwGetKey(win, GLFW_KEY_D) == GLFW_PRESS)
            moveDir.add(new Vector3f(right).mul(cameraSpeed));


        Vector3f position = camera.getPosition();
        Vector3f pos = new Vector3f(position.x, position.y - 2, position.z);

        // прыжок
        if (glfwGetKey(win, GLFW_KEY_SPACE) == GLFW_PRESS && isGrounded) {
            velocityY = jumpStrength;
            isGrounded = false;
        }

        // гравитация
        velocityY += gravity * deltaTime;
        moveDir.y += velocityY;

        // позиция после движения
        Vector3f newPos = new Vector3f(pos);



        // движение по X
        newPos.x += moveDir.x * deltaTime;
        if (checkCollision(newPos)) {
            if (!tryStepUp(newPos, pos)) newPos.x = pos.x;
        }

        // движение по Z
        newPos.z += moveDir.z * deltaTime;
        if (checkCollision(newPos)) {
            if (!tryStepUp(newPos, pos)) newPos.z = pos.z;
        }

        // движение по Y
        newPos.y += moveDir.y * deltaTime;
        if (checkCollision(newPos)) {
            if (moveDir.y < 0) isGrounded = true;
            velocityY = 0;
            newPos.y = pos.y;
        } else {
            // проверяем, есть ли пол
            Float groundY = findGroundBelow(newPos);
            if (groundY != null && newPos.y <= groundY + 0.01f) {
                newPos.y = groundY;
                isGrounded = true;
                velocityY = 0;
            } else {
                isGrounded = false;
            }
        }

        // респавн
        if (glfwGetKey(win, GLFW_KEY_R) == GLFW_PRESS) {
            newPos.set(0, 60, 0);
            velocityY = 0;
        }

        camera.getPosition().set(newPos.x, newPos.y + 2, newPos.z);




        // выход
        if (glfwGetKey(win, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            glfwSetWindowShouldClose(win, true);
        }
    }


    private Float findGroundBelow(Vector3f pos) {
        float closest = Float.NEGATIVE_INFINITY;

        // проходим по всем groundColliders
        for (Node child : root.getChildren()) {
            if (child instanceof MazeNode maze) {
                for (AABB box : maze.getColliders()) {
                    if (pos.x >= box.min.x && pos.x <= box.max.x &&
                            pos.z >= box.min.z && pos.z <= box.max.z) {
                        // нашли плитку под ногами
                        if (box.max.y > closest && box.max.y <= pos.y) {
                            closest = box.max.y;
                        }
                    }
                }
            }
        }

        return closest == Float.NEGATIVE_INFINITY ? null : closest;
    }


    private boolean tryStepUp(Vector3f newPos, Vector3f oldPos) {
        float originalY = newPos.y;

        // пробуем приподняться на высоту до stepHeight
        for (float dy = 0.05f; dy <= stepHeight; dy += 0.05f) {
            newPos.y = oldPos.y + dy;
            if (!checkCollision(newPos)) {
                return true; // нашли место для подъёма
            }
        }

        newPos.y = originalY;
        return false;
    }


    private boolean checkCollision(Vector3f newPos) {
        Vector3f half = new Vector3f(playerSize).mul(0.5f);
        AABB playerAABB = new AABB(
                new Vector3f(newPos).sub(half),
                new Vector3f(newPos).add(half)
        );

        return checkCollisionRecursive(root, playerAABB);
    }

    private boolean checkCollisionRecursive(Node node, AABB playerAABB) {
        if (node instanceof MazeNode maze) {
            // Проверяем по кубикам
            if (maze.checkCollision(playerAABB)) {
                return true;
            }
        } else if (node instanceof GameObjectNode g) {
            if (!g.isCollidable()) return false;

            // Здесь оставляем bounding box только для обычных объектов
            Mesh mesh = g.getMesh();
            if (mesh != null) {
                AABB box = calculateBoundingBox(g);
                if (playerAABB.min.x <= box.max.x && playerAABB.max.x >= box.min.x &&
                        playerAABB.min.y <= box.max.y && playerAABB.max.y >= box.min.y &&
                        playerAABB.min.z <= box.max.z && playerAABB.max.z >= box.min.z) {
                    return true;
                }
            }
        }

        for (Node child : node.getChildren()) {
            if (checkCollisionRecursive(child, playerAABB)) return true;
        }
        return false;
    }


    private AABB calculateBoundingBox(GameObjectNode g) {
        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);

        Matrix4f model = g.getWorldTransform();
        for (Vector3f[] tri : g.getMesh().getTriangles()) {
            for (Vector3f v : tri) {
                Vector3f worldPos = v.mulPosition(model, new Vector3f());
                min.min(worldPos);
                max.max(worldPos);
            }
        }
        return new AABB(min, max);
    }


    private Float raycastDown(Vector3f pos, GameObject obj) {
        float groundY = Float.NEGATIVE_INFINITY;

        for (Vector3f[] tri : obj.getMesh().getTriangles()) {
            Vector3f v0 = new Vector3f(tri[0]).add(obj.getPosition());
            Vector3f v1 = new Vector3f(tri[1]).add(obj.getPosition());
            Vector3f v2 = new Vector3f(tri[2]).add(obj.getPosition());

            // Нормаль треугольника
            Vector3f edge1 = new Vector3f(v1).sub(v0);
            Vector3f edge2 = new Vector3f(v2).sub(v0);
            Vector3f normal = edge1.cross(edge2, new Vector3f()).normalize();

            // Если треугольник почти вертикальный — пропускаем (это не пол)
            if (Math.abs(normal.y) < 0.5f) continue;

            // Находим уравнение плоскости
            float d = -normal.dot(v0);

            // Луч вниз: (x, y, z) = pos + t*(0,-1,0)
            float denom = normal.y * -1.0f;
            if (Math.abs(denom) < 1e-6) continue;

            float t = -(normal.dot(pos) + d) / denom;
            if (t < 0) continue;

            float y = pos.y - t;

            // Проверяем, лежит ли (pos.x, y, pos.z) внутри треугольника
            Vector3f p = new Vector3f(pos.x, y, pos.z);
            if (pointInTriangle(p, v0, v1, v2)) {
                if (y > groundY && y <= pos.y) {
                    groundY = y;
                }
            }
        }
        return groundY == Float.NEGATIVE_INFINITY ? null : groundY;
    }

    private boolean pointInTriangle(Vector3f p, Vector3f a, Vector3f b, Vector3f c) {
        Vector3f v0 = new Vector3f(c).sub(a);
        Vector3f v1 = new Vector3f(b).sub(a);
        Vector3f v2 = new Vector3f(p).sub(a);

        float dot00 = v0.dot(v0);
        float dot01 = v0.dot(v1);
        float dot02 = v0.dot(v2);
        float dot11 = v1.dot(v1);
        float dot12 = v1.dot(v2);

        float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
        float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

        return (u >= 0) && (v >= 0) && (u + v <= 1);
    }


}
