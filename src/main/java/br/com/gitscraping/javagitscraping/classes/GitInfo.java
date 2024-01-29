package br.com.gitscraping.javagitscraping.classes;

public class GitInfo {
    private String extension;
    private int count;
    private int lines;
    private int bytes;

    // Constructor 
    public GitInfo(String extension, int count, int lines, int bytes){
        this.extension = extension;
        this.count = count;
        this.lines = lines;
        this.bytes = bytes;

    }

    //Getters and setters
    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getLines() {
        return lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    public int getBytes() {
        return bytes;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }
}
