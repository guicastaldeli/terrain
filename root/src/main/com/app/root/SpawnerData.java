package main.com.app.root;

public enum SpawnerData {
    TREE("tree"),
    TORCH("torch");

    private final String type;

    SpawnerData(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
