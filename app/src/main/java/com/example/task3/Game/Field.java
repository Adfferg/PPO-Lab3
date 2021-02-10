package com.example.task3.Game;

public class Field {
    private Cell[][] field;

    public Field() {
        field = new Cell[8][8];
        field[0][0] = field[0][7] = Cell.WHITE_ROOK;
        field[0][1] = field[0][6] = Cell.WHITE_HORSE;
        field[0][2] = field[0][5] = Cell.WHITE_BISHOP;
        field[0][3] = Cell.WHITE_KING;
        field[0][4] = Cell.WHITE_QUEEN;
        for (int i = 0; i < 8; i++) {
            field[1][i] = Cell.WHITE_PAWN;
        }
        for (int i = 0; i < 8; i++) {
            field[6][i] = Cell.BLACK_PAWN;
        }
        for (int i = 2; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                field[i][j] = Cell.EMPTY;
            }
        }
        field[7][0] = field[7][7] = Cell.BLACK_ROOK;
        field[7][1] = field[7][6] = Cell.BLACK_HORSE;
        field[7][2] = field[7][5] = Cell.BLACK_BISHOP;
        field[7][3] = Cell.BLACK_KING;
        field[7][4] = Cell.BLACK_QUEEN;
    }

    public Cell getCell(int i, int j) {
        return field[i][j];
    }

    public void setCell(int i, int j, Cell figure) {

        this.field[i][j] = figure;
    }


}
