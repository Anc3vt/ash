package com.ancevt.ash.engine.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LaunchConfig {

    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 720;
    private static final String DEFAULT_TITLE = "Ash Engine";

    private final int width;
    private final int height;
    private final String title;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int width;
        private int height;
        private String title;


        public Builder width(int width) {
            if (width > 0) {
                this.width = width;
                return this;
            } else {
                throw new IllegalArgumentException("Width must be greater than 0");
            }
        }

        public Builder height(int height) {
            if (height > 0) {
                this.height = height;
                return this;
            } else {
                throw new IllegalArgumentException("Height must be greater than 0");
            }
        }

        public Builder title(String title) {
            if (title != null && !title.isEmpty()) {
                this.title = title;
                return this;
            } else {
                throw new IllegalArgumentException("Title must not be null or empty");
            }
        }

        public LaunchConfig build() {
            return new LaunchConfig(width, height, title);
        }

    }

}
