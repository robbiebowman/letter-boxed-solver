package com.robbiebowman;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        var solver = new Solver(List.of("tul", "dca", "rph", "nes"), 6); // 1m22s
        var answer = solver.getShortestAnswer();
        var words = answer.words().stream().map(w -> w.getText()).toList();
        System.out.println(words);
    }
}
