/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.addon.control.block;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.tileentity.TileEntityPilotsChair;
import valkyrienwarfare.mod.common.math.Vector;
import valkyrienwarfare.mod.common.physics.management.PhysicsObject;
import valkyrienwarfare.mod.common.physics.management.PhysicsWrapperEntity;
import valkyrienwarfare.mod.common.physmanagement.interaction.EntityDraggable;
import valkyrienwarfare.mod.common.physmanagement.interaction.IDraggable;
import valkyrienwarfare.mod.common.util.ValkyrienUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class BlockShipPilotsChair extends BlockPilotableBasic {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockShipPilotsChair(Material materialIn) {
        super(materialIn);
    }

    public static double getChairYaw(IBlockState state, BlockPos pos) {
        EnumFacing enumFace = state.getValue(BlockShipPilotsChair.FACING);
        double chairYaw = -enumFace.getHorizontalAngle() - 90;
        return chairYaw;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(worldIn, pos);
            if (physicsObject.isPresent()) {
                PhysicsWrapperEntity wrapperEntity = physicsObject.get()
                        .getWrapperEntity();
                if (playerIn.getLowestRidingEntity() != wrapperEntity.getLowestRidingEntity()) {
                    TileEntity tileEntity = worldIn.getTileEntity(pos);
                    if (tileEntity instanceof TileEntityPilotsChair) {
                        Vector playerPos = new Vector(playerIn);

                        wrapperEntity.getPhysicsObject()
                                .getShipTransformationManager()
                                .fromLocalToGlobal(playerPos);

                        playerIn.posX = playerPos.X;
                        playerIn.posY = playerPos.Y;
                        playerIn.posZ = playerPos.Z;

                        IDraggable entityDraggable = EntityDraggable.getDraggableFromEntity(playerIn);
                        // Only mount the player if they're standing on the ship.
                        if (entityDraggable.getWorldBelowFeet() == wrapperEntity) {
                            playerIn.startRiding(wrapperEntity);
                            Vector localMountPos = getPlayerMountOffset(state, pos);
                            wrapperEntity.getPhysicsObject()
                                    .fixEntity(playerIn, localMountPos);
                        }

                        ((TileEntityPilotsChair) tileEntity).setPilotEntity(playerIn);
                        wrapperEntity.getPhysicsObject()
                                .getShipTransformationManager()
                                .fromGlobalToLocal(playerPos);

                        playerIn.posX = playerPos.X;
                        playerIn.posY = playerPos.Y;
                        playerIn.posZ = playerPos.Z;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> itemInformation, ITooltipFlag advanced) {
        itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.BLUE + "Use to mount and control Ships!");
        itemInformation.add(TextFormatting.BOLD + "" + TextFormatting.BOLD + TextFormatting.RED + "Can only be placed on a Ship");
    }

    private Vector getPlayerMountOffset(IBlockState state, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
        switch (facing) {
            case NORTH:
                return new Vector(pos.getX() + .5D, pos.getY(), pos.getZ() + .6D);
            case SOUTH:
                return new Vector(pos.getX() + .5D, pos.getY(), pos.getZ() + .4D);
            case WEST:
                return new Vector(pos.getX() + .6D, pos.getY(), pos.getZ() + .5D);
            case EAST:
                return new Vector(pos.getX() + .4D, pos.getY(), pos.getZ() + .5D);
            default:
                return new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityPilotsChair();
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.isSneaking() ? placer.getHorizontalFacing().getOpposite() : placer.getHorizontalFacing());
    }

    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing facingHorizontal = placer.getHorizontalFacing();

        if (!placer.isSneaking()) {
            facingHorizontal = facingHorizontal.getOpposite();
        }

        return this.getDefaultState().withProperty(FACING, facingHorizontal);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byIndex(meta);
        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = state.getValue(FACING)
                .getIndex();
        return i;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

}