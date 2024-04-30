package com.robbiebowman;

import java.util.List;

public record Word(char[][] sides, List<Coordinate> coordinates) {
    public String getText() {
        return coordinates.stream()
                .map(coord -> Character.toString(sides[coord.sideIndex()][coord.charIndex()]))
                .reduce((acc, e) -> acc + e)
                .get();
    }
}
