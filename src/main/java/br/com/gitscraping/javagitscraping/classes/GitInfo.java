package br.com.gitscraping.javagitscraping.classes;

public class GitInfo {
    private String extension;
    private String percent;

    public GitInfo(String extension, String percent){
        this.extension = extension;
        this.percent = percent;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }
}
