package com.missing.nfp;

public class Cell {

    private int Index;
    private String Date;
    private String Code;
    private String Comments;
    private String Summation;
    private int Sticker;

    public Cell(){
    }

    public Cell(int index, String date, String code, String comments, int sticker) {
        Index = index;
        Date = date;
        Code = code;
        Comments = comments;
        Sticker = sticker;
    }

    public int getIndex() {return Index; }

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

        Summation = this.Date + "\n" + this.Code + "\n" + this.Comments;

        return Summation;
    }

    public void setSummation(String summation) {
        Summation = summation;
    }

    public void setIndex(int index) {
        Index = index;
    }

    public void setDate(String date) {
        Date = date;
    }

    public void setCode(String code) {
        Code = code;
    }

    public void setComments(String comments) {
        Comments = comments;
    }

    public void setSticker(int sticker) {
        Sticker = sticker;
    }
}
