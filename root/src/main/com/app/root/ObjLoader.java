package main.com.app.root;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import main.com.app.root.mesh.MeshData;

public class ObjLoader {
    public static MeshData load(String filePath, String meshId) {
        List<float[]> vertices = new ArrayList<>();
        List<float[]> texCoords = new ArrayList<>();
        List<float[]> normals = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while((line = reader.readLine()) != null) {
                if(line.startsWith("v ")) {
                    String[] parts = line.split("\\s+");
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float z = Float.parseFloat(parts[3]);
                    vertices.add(new float[]{x, y, z});
                } else if(line.startsWith("vt ")) {
                    String[] parts = line.split("\\s+");
                    float u = Float.parseFloat(parts[1]);
                    float v = Float.parseFloat(parts[2]);
                    texCoords.add(new float[]{u, v});
                } else if(line.startsWith("vn ")) {
                    String[] parts = line.split("\\s+");
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float z = Float.parseFloat(parts[3]);
                    normals.add(new float[]{x, y, z});
                } else if(line.startsWith("f ")) {
                    String[] parts = line.split("\\s+");
                    int[] face = new int[parts.length - 1];
                    for (int i = 1; i < parts.length; i++) {
                        String[] indices = parts[i].split("/");
                        face[i-1] = Integer.parseInt(indices[0]) - 1;
                    }
                    faces.add(face);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load OBJ file: " + filePath, e);
        }

        return createMeshData(
            vertices,
            texCoords,
            normals,
            faces,
            meshId
        );
    }

    private static MeshData createMeshData(
        List<float[]> vertices, 
        List<float[]> texCoords, 
        List<float[]> normals, 
        List<int[]> faces, 
        String meshId
    ) {
        MeshData meshData = new MeshData(meshId, MeshData.MeshType.CUBE);

        float[] vertexArray = new float[vertices.size() * 3];
        for(int i = 0; i < vertices.size(); i++) {
            vertexArray[i*3] = vertices.get(i)[0];
            vertexArray[i*3+1] = vertices.get(i)[1];
            vertexArray[i*3+2] = vertices.get(i)[1];
        }
        meshData.setVertices(vertexArray);

        List<Integer> indicesList = new ArrayList<>();
        for(int[] face : faces) {
            for(int i = 1; i < face.length - 1; i++) {
                indicesList.add(face[0]);
                indicesList.add(face[i]);
                indicesList.add(face[i+1]);
            }
        }
        int[] indicesArray = indicesList.stream().mapToInt(i -> i).toArray();
        meshData.setIndices(indicesArray);
        return meshData;
    }
}
