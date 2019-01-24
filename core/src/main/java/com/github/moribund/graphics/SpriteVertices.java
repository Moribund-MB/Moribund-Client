package com.github.moribund.graphics;

import lombok.Getter;

public enum SpriteVertices {
    PLAYER(7, 50, 10, 33, 16, 25, 24, 19, 33, 16, 43, 15, 51, 17, 57, 12, 63, 10, 71, 12, 73, 19, 72, 27, 67, 32, 71, 44, 71, 54, 69, 63, 66, 69, 71, 74, 73, 83, 69, 91, 62, 93, 53, 86, 44, 86, 34, 86, 25, 84, 18, 78, 12, 71, 8, 62),
    PLAYER_WITH_BOW(16, 87, 6, 71, 4, 59, 4, 45, 7, 37, 14, 26, 21, 21, 31, 17, 43, 16, 48, 17, 49, 10, 54, 5, 60, 4, 68, 6, 74, 17, 81, 16, 87, 14, 95, 11, 102, 8, 108, 7, 114, 12, 116, 21, 114, 29, 117, 36, 111, 45, 100, 45, 95, 39, 93, 33, 86, 28, 75, 27, 68, 29, 74, 41, 76, 58, 73, 71, 69, 83, 61, 89, 51, 95, 40, 97, 30, 96),
    ARROW_PROJECTILE(0, 24, 0, 18, 4, 21, 11, 21, 31, 20, 36, 18, 39, 22, 34, 26, 26, 23, 17, 23),
    DART_PROJECTILE(3, 22, 8, 27, 14, 24, 22, 23, 35, 24, 34, 19, 15, 19, 5, 16),
    SPEAR_PROJECTILE(1, 37, 59, 37, 69, 34, 58, 30, 1, 30),
    PLAYER_WITH_DART(12, 82, 9, 61, 16, 49, 21, 41, 31, 37, 39, 36, 43, 21, 46, 13, 53, 11, 61, 17, 61, 27, 59, 50, 64, 67, 63, 75, 70, 83, 67, 91, 61, 95, 54, 90, 46, 96, 37, 99, 25, 95),
    PLAYER_WITH_SPEAR(47, 78, 43, 63, 44, 46, 50, 37, 55, 29, 3, 14, 9, 9, 27, 9, 45, 8, 71, 9, 87, 9, 95, 4, 104, 6, 109, 10, 121, 9, 141, 9, 148, 3, 160, 6, 176, 11, 182, 13, 105, 43, 107, 53, 107, 66, 105, 74, 113, 80, 111, 93, 101, 96, 95, 91, 82, 96, 67, 96, 57, 91);

    @Getter
    private final float[] vertices;

    SpriteVertices(float... vertices) {
        this.vertices = vertices;
    }
}