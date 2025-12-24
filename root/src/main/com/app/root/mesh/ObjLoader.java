package main.com.app.root.mesh;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjLoader {
    public static MeshData load(String filePath, String meshId) {
        List<float[]> vertices = new ArrayList<>();
        List<float[]> texCoords = new ArrayList<>();
        List<float[]> normals = new ArrayList<>();
        List<FaceData> faces = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while((line = reader.readLine()) != null) {
                line = line.trim();
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
                    List<Integer> vertexIndices = new ArrayList<>();
                    List<Integer> texCoordIndices = new ArrayList<>();
                    List<Integer> normalIndices = new ArrayList<>();
                    
                    for(int i = 1; i < parts.length; i++) {
                        String[] indices = parts[i].split("/");
                        vertexIndices.add(Integer.parseInt(indices[0]) - 1);

                        if(indices.length > 1 && !indices[1].isEmpty()) {
                            texCoordIndices.add(Integer.parseInt(indices[1]) - 1);
                        } else {
                            texCoordIndices.add(-1);
                        }
                        
                        if(indices.length > 2 && !indices[2].isEmpty()) {
                            normalIndices.add(Integer.parseInt(indices[2]) - 1);
                        } else {
                            normalIndices.add(-1);
                        }
                    }
                    
                    faces.add(new FaceData(vertexIndices, texCoordIndices, normalIndices));
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
        List<FaceData> faces, 
        String meshId
    ) {
        MeshData meshData = new MeshData(meshId, MeshData.MeshType.OBJ);
        List<Float> vertexList = new ArrayList<>();
        List<Float> texCoordList = new ArrayList<>();
        List<Float> normalList = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();
        
        java.util.Map<String, Integer> vertexMap = new java.util.HashMap<>();
        int currentIndex = 0;
        
        for(FaceData face : faces) {
            for(int i = 1; i < face.vertexIndices.size() - 1; i++) {
                int[] triIndices = new int[] {
                    0, i, i + 1
                };
                
                for(int triIndex : triIndices) {
                    int vIdx = face.vertexIndices.get(triIndex);
                    int tIdx = face.texCoordIndices.get(triIndex);
                    int nIdx = face.normalIndices.get(triIndex);
                    String key = vIdx + "_" + tIdx + "_" + nIdx;
                    
                    if(vertexMap.containsKey(key)) {
                        indexList.add(vertexMap.get(key));
                    } else {
                        float[] vertex = vertices.get(vIdx);
                        vertexList.add(vertex[0]);
                        vertexList.add(vertex[1]);
                        vertexList.add(vertex[2]);
                        
                        if(tIdx >= 0 && tIdx < texCoords.size()) {
                            float[] texCoord = texCoords.get(tIdx);
                            texCoordList.add(texCoord[0]);
                            texCoordList.add(texCoord[1]);
                        } else {
                            texCoordList.add(0.0f);
                            texCoordList.add(0.0f);
                        }
                        
                        if(nIdx >= 0 && nIdx < normals.size()) {
                            float[] normal = normals.get(nIdx);
                            normalList.add(normal[0]);
                            normalList.add(normal[1]);
                            normalList.add(normal[2]);
                        } else {
                            normalList.add(0.0f);
                            normalList.add(1.0f);
                            normalList.add(0.0f);
                        }
                        
                        vertexMap.put(key, currentIndex);
                        indexList.add(currentIndex);
                        currentIndex++;
                    }
                }
            }
        }
        
        float[] vertexArray = new float[vertexList.size()];
        for(int i = 0; i < vertexList.size(); i++) {
            vertexArray[i] = vertexList.get(i);
        }
        float[] texCoordArray = new float[texCoordList.size()];
        for(int i = 0; i < texCoordList.size(); i++) {
            texCoordArray[i] = texCoordList.get(i);
        }
        float[] normalArray = new float[normalList.size()];
        for(int i = 0; i < normalList.size(); i++) {
            normalArray[i] = normalList.get(i);
        }
        int[] indicesArray = new int[indexList.size()];
        for(int i = 0; i < indexList.size(); i++) {
            indicesArray[i] = indexList.get(i);
        }
        
        meshData.setVertices(vertexArray);
        meshData.setTexCoords(texCoordArray);
        meshData.setNormals(normalArray);
        meshData.setIndices(indicesArray);
        
        return meshData;
    }
    
    private static class FaceData {
        List<Integer> vertexIndices;
        List<Integer> texCoordIndices;
        List<Integer> normalIndices;
        
        FaceData(List<Integer> vertexIndices, List<Integer> texCoordIndices, List<Integer> normalIndices) {
            this.vertexIndices = vertexIndices;
            this.texCoordIndices = texCoordIndices;
            this.normalIndices = normalIndices;
        }
    }
}