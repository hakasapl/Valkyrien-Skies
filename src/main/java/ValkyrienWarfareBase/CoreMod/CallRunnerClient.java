package ValkyrienWarfareBase.CoreMod;

import ValkyrienWarfareBase.Collision.Polygon;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.Proxy.ClientProxy;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;

public class CallRunnerClient extends CallRunner {

    public static AxisAlignedBB getRenderBoundingBox(TileEntity tile){
    	AxisAlignedBB toReturn = tile.getRenderBoundingBox();
//    	System.out.println("running");
    	BlockPos pos = tile.getPos();
    	PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(Minecraft.getMinecraft().world, pos);
    	if(wrapper != null){
    		Polygon inWorldPoly = new Polygon(toReturn, wrapper.wrapping.coordTransform.lToWTransform);
    		return inWorldPoly.getEnclosedAABB();
    	}
    	return toReturn;
    }

	public static void onDrawBlockDamageTexture(RenderGlobal renderGlobal, Tessellator tessellatorIn, VertexBuffer worldRendererIn, Entity entityIn, float partialTicks) {
		double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
		double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
		double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;

		if (!renderGlobal.damagedBlocks.isEmpty()) {
			renderGlobal.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			renderGlobal.preRenderDamagedBlocks();
			worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
			worldRendererIn.setTranslation(-d0, -d1, -d2);
			worldRendererIn.noColor();
			Iterator<DestroyBlockProgress> iterator = renderGlobal.damagedBlocks.values().iterator();

			while (iterator.hasNext()) {
				DestroyBlockProgress destroyblockprogress = (DestroyBlockProgress) iterator.next();
				BlockPos blockpos = destroyblockprogress.getPosition();
				double d3 = (double) blockpos.getX() - d0;
				double d4 = (double) blockpos.getY() - d1;
				double d5 = (double) blockpos.getZ() - d2;
				Block block = renderGlobal.world.getBlockState(blockpos).getBlock();
				TileEntity te = renderGlobal.world.getTileEntity(blockpos);
				boolean hasBreak = block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockSign || block instanceof BlockSkull;
				if (!hasBreak)
					hasBreak = te != null && te.canRenderBreaking();

				if (!hasBreak) {
					PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(renderGlobal.world, blockpos);
					if (wrapper == null && (d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D)) {
						iterator.remove();
					} else {
						IBlockState iblockstate = renderGlobal.world.getBlockState(blockpos);
						if (wrapper != null) {
							wrapper.wrapping.renderer.setupTranslation(partialTicks);
							worldRendererIn.setTranslation(-wrapper.wrapping.renderer.offsetPos.getX(), -wrapper.wrapping.renderer.offsetPos.getY(), -wrapper.wrapping.renderer.offsetPos.getZ());
						}
						if (iblockstate.getMaterial() != Material.AIR) {
							int i = destroyblockprogress.getPartialBlockDamage();
							TextureAtlasSprite textureatlassprite = renderGlobal.destroyBlockIcons[i];
							BlockRendererDispatcher blockrendererdispatcher = renderGlobal.mc.getBlockRendererDispatcher();
							try {
								blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, renderGlobal.world);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						worldRendererIn.setTranslation(-d0, -d1, -d2);
						// TODO: Reverse the Matrix Transforms here
						if (wrapper != null) {
							tessellatorIn.draw();
							worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
							wrapper.wrapping.renderer.inverseTransform(partialTicks);
						}
					}
				}
			}

			tessellatorIn.draw();
			worldRendererIn.setTranslation(0.0D, 0.0D, 0.0D);
			renderGlobal.postRenderDamagedBlocks();
		}

	}

	public static void onDrawSelectionBox(RenderGlobal renderGlobal, EntityPlayer player, RayTraceResult movingObjectPositionIn, int execute, float partialTicks) {
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world, movingObjectPositionIn.getBlockPos());
		if (wrapper != null && wrapper.wrapping != null && wrapper.wrapping.renderer != null && wrapper.wrapping.renderer.offsetPos != null) {;
			wrapper.wrapping.renderer.setupTranslation(partialTicks);

			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer vertexbuffer = tessellator.getBuffer();

			double xOff = (player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks) - wrapper.wrapping.renderer.offsetPos.getX();
			double yOff = (player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks) - wrapper.wrapping.renderer.offsetPos.getY();
			double zOff = (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks) - wrapper.wrapping.renderer.offsetPos.getZ();

			vertexbuffer.xOffset += xOff;
			vertexbuffer.yOffset += yOff;
			vertexbuffer.zOffset += zOff;

			renderGlobal.drawSelectionBox(player, movingObjectPositionIn, execute, partialTicks);

			vertexbuffer.xOffset -= xOff;
			vertexbuffer.yOffset -= yOff;
			vertexbuffer.zOffset -= zOff;

			wrapper.wrapping.renderer.inverseTransform(partialTicks);
		} else {
			renderGlobal.drawSelectionBox(player, movingObjectPositionIn, execute, partialTicks);
		}
	}

	//TODO: Theres a lighting bug caused by Ships rendering TileEntities, perhaps use the RenderOverride to render them instead
	public static void onRenderEntities(RenderGlobal renderGlobal, Entity renderViewEntity, ICamera camera, float partialTicks) {
		((ClientProxy) ValkyrienWarfareMod.proxy).lastCamera = camera;

		renderGlobal.renderEntities(renderViewEntity, camera, partialTicks);
	}

	public static int onRenderBlockLayer(RenderGlobal renderer, BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn) {
		for (PhysicsWrapperEntity wrapper : ValkyrienWarfareMod.physicsManager.getManagerForWorld(renderer.world).physicsEntities) {
			GL11.glPushMatrix();
			if (wrapper.wrapping.renderer != null && wrapper.wrapping.renderer.shouldRender()) {
				wrapper.wrapping.renderer.renderBlockLayer(blockLayerIn, partialTicks, pass);
			}
			GL11.glPopMatrix();
		}
		GlStateManager.resetColor();
		return renderer.renderBlockLayer(blockLayerIn, partialTicks, pass, entityIn);
	}
}