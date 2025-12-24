package main.com.app.root;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.joml.Vector3f;

import main.com.app.root.env.tress.TreeController;
import main.com.app.root.mesh.Mesh;

public class Spawner {
    private final Tick tick;
    private final Mesh mesh;
    private Vector3f centerPosition;

    private int maxObj;
    private float spawnRadius;
    private boolean isActive;

    private final List<TreeController> trees;
    private final Map<Integer, TreeData> treeConfig;
    private final Random random;
    private final Map<Integer, Float> levelDistribution;
    private int currentTreeId;

    private float spawnTimer;
    private float spawnRate = 2.0f;
    private float minSpawnDistance = 5.0f;
    private float maxSpawnDistance = 100.0f;

    private enum SpawnType {
        TREE
    }
    private SpawnType currentType = SpawnType.TREE;

    public Spawner(
        Tick tick,
        Mesh mesh,
        Vector3f centerPosition,
        int maxObj,
        float spawnRadius
    ) {
        this.tick = tick;
        this.mesh = mesh;
        this.centerPosition = centerPosition;
        this.maxObj = maxObj;
        this.spawnRadius = spawnRadius;

        this.trees = new ArrayList<>();
        this.treeConfig = new HashMap<>();
        this.random = new Random();
        this.currentTreeId = 0;

        this.levelDistribution = new HashMap<>();
        initLevelDistribution();

        loadTreeConfigs();
        initSpawn();
    }

    private void initLevelDistribution() {
        levelDistribution.put(0, 100.0f);
    }
}
