package com.mygdx.game.terrains;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Settings;
import com.mygdx.game.physics.parser.Parser;
import com.mygdx.game.terrains.attributes.TerrainMaterialAttribute;
import com.mygdx.game.terrains.attributes.TerrainTextureAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;

import java.util.HashMap;
import java.util.Map;

public class TerrainHeight extends Terrain {

    private Parser parser;
    private String heightFunction;

    public TerrainHeight(Parser parser, String heightFunction) {
        this.parser = parser;
        this.heightFunction = heightFunction;
        this.sizeRatio = Settings.SIZE_RATIO;
        this.size = (int) Settings.SIZE;
        this.resolution = size/6;

        createTerrainMesh();
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        modelBuilder.part("terrain", mesh, GL20.GL_TRIANGLES, new Material());
        modelInstance = new ModelInstance(modelBuilder.end());

//        Texture texture = new Texture(Gdx.files.internal("textures/meadow_grass.png"), true);
//        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
//        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        Material material = modelInstance.materials.get(0);
        TerrainTextureAttribute baseAttribute = TerrainTextureAttribute.createDiffuseBase(getMipMapTexture("textures/water.png"));
        TerrainTextureAttribute terrainHeightTexture = TerrainTextureAttribute.createDiffuseHeight(getMipMapTexture("textures/meadow_grass.png"));

        baseAttribute.scaleU = 40f;
        baseAttribute.scaleV = 40f;

        TerrainMaterial terrainMaterial = new TerrainMaterial();
        terrainMaterial.set(baseAttribute);
        terrainMaterial.set(terrainHeightTexture);

        material.set(TerrainMaterialAttribute.createTerrainMaterialAttribute(terrainMaterial));
    }

    private Texture getMipMapTexture(String path){
        Texture texture = new Texture(Gdx.files.internal(path), true);
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        return texture;
    }

    @Override
    public float getHeight(float x, float y) {
        Map<String, Double> valMap = new HashMap<>();
        valMap.put("x", (double) x);
        valMap.put("y", (double) y);
        float eval = (float) (parser.evaluateAt(valMap, heightFunction));
        if (eval < 0) return -0.001f;
        return  (float) (parser.evaluateAt(valMap, heightFunction));
    }

    private void createTerrainMesh() {
        mesh = new Mesh(true,  resolution * resolution, resolution * resolution * 6,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"),
                new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, "a_color"));

        float[] vertices = new float[resolution * resolution * 12];
        short[] indices = new short[(resolution - 1) * (resolution - 1) * 6];

        int idx = 0;
        int idc = 0;
        for (int y = 0; y < resolution; y++) {
            for (int x = 0; x < resolution; x++) {
                float realX = x * 6 * sizeRatio;
                float realY = y * 6 * sizeRatio;
                float realX2 = (x * 6 - size/2)*sizeRatio;
                float realY2 = (y * 6 - size/2)*sizeRatio ;

                float height = getHeight(realX2, realY2);
                //System.out.println(realX2 + " " + realY2 + " " + height);

                // Positions
                vertices[idx] = realX/sizeRatio;
                vertices[idx + 1] = height/sizeRatio;
                vertices[idx + 2] = realY/sizeRatio;

                vertices[idx + 3] = 0;
                vertices[idx + 4] = 1;
                vertices[idx + 5] = 0;

                // Texture coordinates
                vertices[idx + 6] = (float) x / (resolution - 1);
                vertices[idx + 7] = (float) y / (resolution - 1);

                // colour
                vertices[idx + 8] =  0.5f;  // Mid-intensity red
                vertices[idx + 9] =  0.5f;  // Mid-intensity green
                vertices[idx + 10] =  0.5f;  // Mid-intensity blue
                vertices[idx + 11] =  1.0f; // fully opaque

                idx += 12;

                if (x < resolution - 1 && y < resolution - 1) {
                    int i0 = x + y * resolution;
                    int i1 = x + 1 + y * resolution;
                    int i2 = x + (y + 1) * resolution;
                    int i3 = x + 1 + (y + 1) * resolution;

                    indices[idc++] = (short) i0;
                    indices[idc++] = (short) i2;
                    indices[idc++] = (short) i1;

                    indices[idc++] = (short) i2;
                    indices[idc++] = (short) i3;
                    indices[idc++] = (short) i1;
                }
            }
        }

        calculateNormals(vertices, indices);

        mesh.setVertices(vertices);
        mesh.setIndices(indices);
    }


    private void calculateNormals(float[] vertices, short[] indices) {
        Vector3 v1 = new Vector3();
        Vector3 v2 = new Vector3();
        Vector3 normal = new Vector3();

        for (int i = 0; i < indices.length; i += 3) {
            int idx1 = indices[i] * 12;
            int idx2 = indices[i + 1] * 12;
            int idx3 = indices[i + 2] * 12;

            Vector3 p1 = new Vector3(vertices[idx1], vertices[idx1 + 1], vertices[idx1 + 2]);
            Vector3 p2 = new Vector3(vertices[idx2], vertices[idx2 + 1], vertices[idx2 + 2]);
            Vector3 p3 = new Vector3(vertices[idx3], vertices[idx3 + 1], vertices[idx3 + 2]);

            v1.set(p2).sub(p1);
            v2.set(p3).sub(p1);
            normal.set(v1).crs(v2).nor();

            for (int j = 0; j < 3; j++) {
                int normIdx = indices[i + j] * 12 + 3;
                vertices[normIdx] += normal.x;
                vertices[normIdx + 1] += normal.y;
                vertices[normIdx + 2] += normal.z;
            }
        }

        // Normalize the normals
        for (int i = 0; i < vertices.length; i += 12) {
            normal.set(vertices[i + 3], vertices[i + 4], vertices[i + 5]).nor();
            vertices[i + 3] = normal.x;
            vertices[i + 4] = normal.y;
            vertices[i + 5] = normal.z;
        }
    }


    @Override
    public void dispose() {
        mesh.dispose();
    }


    public static void main(String[] args) {
    }

}
