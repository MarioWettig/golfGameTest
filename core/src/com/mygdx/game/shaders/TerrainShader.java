package com.mygdx.game.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.mygdx.game.terrains.TerrainMaterial;
import com.mygdx.game.terrains.attributes.TerrainMaterialAttribute;
import com.mygdx.game.terrains.attributes.TerrainTextureAttribute;
import org.xml.sax.Attributes;

public class TerrainShader extends BaseShader {


    public static class TerrainInputs {
        public final static Uniform diffuseUVTransform = new Uniform("u_diffuseUVTransform");
        public final static Uniform diffuseBaseTexture = new Uniform("u_diffuseBaseTexture");
        public final static Uniform diffuseHeightTexture = new Uniform("u_diffuseHeightTexture");
        public final static Uniform diffuseSlopeTexture = new Uniform("u_diffuseSlopeTexture");
        //public final static Uniform minSlope = new Uniform("u_minSlope");

    }

    public static class TerrainSetters {
        public final static Setter diffuseUVTransform = new LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, com.badlogic.gdx.graphics.g3d.Attributes combinedAttributes) {
                TerrainMaterial mat = getTerrainMaterial(renderable);
                TerrainTextureAttribute attr = (TerrainTextureAttribute) mat.get(TerrainTextureAttribute.DiffuseBase);
                shader.set(inputID, attr.offsetU, attr.offsetV, attr.scaleU, attr.scaleV);
            }
        };

        public final static Setter diffuseBaseTexture = new LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, com.badlogic.gdx.graphics.g3d.Attributes combinedAttributes) {
                TerrainMaterial mat = getTerrainMaterial(renderable);
                TerrainTextureAttribute attr = (TerrainTextureAttribute) mat.get(TerrainTextureAttribute.DiffuseBase);
                int unit = shader.context.textureBinder.bind(attr.textureDescription);
                shader.set(inputID, unit);
            }
        };

        public final static Setter diffuseHeightTexture = new LocalSetter() {
            @Override
            public void set (BaseShader shader, int inputID, Renderable renderable, com.badlogic.gdx.graphics.g3d.Attributes combinedAttributes) {
                TerrainMaterial mat = getTerrainMaterial(renderable);
                TerrainTextureAttribute attr = (TerrainTextureAttribute) mat.get(TerrainTextureAttribute.DiffuseHeight);
                int unit = shader.context.textureBinder.bind(attr.textureDescription);
                shader.set(inputID, unit);
            }
        };
        public final static Setter diffuseSlopeTexture = new LocalSetter() {
            @Override
            public void set (BaseShader shader, int inputID, Renderable renderable, com.badlogic.gdx.graphics.g3d.Attributes combinedAttributes) {
                TerrainMaterial mat = getTerrainMaterial(renderable);
                TerrainTextureAttribute attr = (TerrainTextureAttribute) mat.get(TerrainTextureAttribute.DiffuseSlope);
                int unit = shader.context.textureBinder.bind(attr.textureDescription);
                shader.set(inputID, unit);
            }
        };
    }


    // Gloabal uniforms
    public final int u_projViewTrans;

    // Object uniforms
    public final int u_worldTrans;
    public final int u_normalMatrix;

    // Material uniforms
    public final int u_diffuseUVTransform;
    public final int u_diffuseBaseTexture;
    public final int u_diffuseHeightTexture;
    public final int u_diffuseSlopeTexture;
    //public final int u_minSlope;




    private Renderable renderable;

    protected final long attributesMask;

    public TerrainShader(Renderable renderable, DefaultShader.Config config) {
        this.renderable = renderable;

        String prefix = DefaultShader.createPrefix(renderable, config);

        attributesMask = combinedAttributeMasks(renderable);

        this.program = new ShaderProgram(prefix + getDefaultVertexShader(), prefix + getDefaultFragmentShader());

        u_projViewTrans = register(DefaultShader.Inputs.projViewTrans, DefaultShader.Setters.projViewTrans);
        u_worldTrans = register(DefaultShader.Inputs.worldTrans, DefaultShader.Setters.worldTrans);
        u_normalMatrix = register(DefaultShader.Inputs.normalMatrix, DefaultShader.Setters.normalMatrix);

        u_diffuseUVTransform = register(TerrainInputs.diffuseUVTransform, TerrainSetters.diffuseUVTransform);
        u_diffuseBaseTexture = register(TerrainInputs.diffuseBaseTexture, TerrainSetters.diffuseBaseTexture);
        u_diffuseHeightTexture = register(TerrainInputs.diffuseHeightTexture, TerrainSetters.diffuseHeightTexture);
        u_diffuseSlopeTexture = register(TerrainInputs.diffuseSlopeTexture, TerrainSetters.diffuseSlopeTexture);

    }

    @Override
    public void init() {
        final ShaderProgram program = this.program;
        this.program = null;
        init(program, renderable);
        renderable = null;
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        super.begin(camera, context);
        context.setDepthTest(GL20.GL_LESS, 0f, 1f);
        context.setCullFace(GL20.GL_BACK);
        context.setDepthMask(true);
    }

    @Override
    public int compareTo(Shader other) {
        if (other == null) return -1;
        if (other == this) return 0;
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return combinedAttributeMasks(instance) == attributesMask;
    }

    private static final long combinedAttributeMasks(Renderable renderable) {
        long mask = 0;
        if(renderable.environment != null)mask |= renderable.environment.getMask();
        if(renderable.material != null)mask |= renderable.material.getMask();
        return mask;
    }

    public  static  String getDefaultVertexShader() {
        return Gdx.files.internal("terrain.vert.glsl").readString();

    }
    public  static  String getDefaultFragmentShader() {
        return Gdx.files.internal("terrain.frag.glsl").readString();
    }

    private static TerrainMaterial getTerrainMaterial(Renderable renderable) {
        return renderable.material.get(TerrainMaterialAttribute.class, TerrainMaterialAttribute.TerrainMaterial).terrainMaterial;
    }

}
