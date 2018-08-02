package valkyrienwarfare.mod.client.render;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.best108.atom_animation_reader.IAtomAnimation;
import com.best108.atom_animation_reader.IAtomAnimationBuilder;
import com.best108.atom_animation_reader.IModelRenderer;
import com.best108.atom_animation_reader.basic_parser.BasicParser;
import com.best108.atom_animation_reader.impl.BasicAtomAnimationBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

public class GibsAnimationRegistry {

	private static final Map<String, IAtomAnimation> ANIMATION_MAP = new HashMap<String, IAtomAnimation>();
	private static final IModelRenderer MODEL_RENDERER = new IModelRenderer() {
		@Override
		public void renderModel(String modelName, int renderBrightness) {
			GibsModelRegistry.renderGibsModel(modelName, renderBrightness);
		}
	};
	
	public static void registerAnimation(String name, ResourceLocation location) {
		try {
			IResource animationResource = Minecraft.getMinecraft().getResourceManager().getResource(location);
			Scanner data = new Scanner(animationResource.getInputStream());
			BasicParser dataParser = new BasicParser(data);
			IAtomAnimationBuilder animationBuilder = new BasicAtomAnimationBuilder(dataParser);
			ANIMATION_MAP.put(name, animationBuilder.build(MODEL_RENDERER));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(5);
		}
	}
	
	public static IAtomAnimation getAnimation(String name) {
		return ANIMATION_MAP.get(name);
	}
}
