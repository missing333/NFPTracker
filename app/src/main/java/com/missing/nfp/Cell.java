package com.missing.nfp;

public class Cell {

    private String Date;
    private String Code;
    private String Comments;
    private int Sticker;


    public Cell(String date, String code, String comments, int sticker) {
        Date = date;
        Code = code;
        Comments = comments;
        Sticker = sticker;
    }

    public String getDate() {
        return Date;
    }

    public String getCode() {
        return Code;
    }

    public String getComments() {
        return Comments;
    }

    public int getSticker() {
        return Sticker;
    }

    public String getSummation() {

        return this.Date + "\n" + this.Code + "\n" + this.Comments;
    }

}
