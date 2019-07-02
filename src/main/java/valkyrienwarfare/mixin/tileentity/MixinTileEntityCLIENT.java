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

package valkyrienwarfare.mixin.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.mod.client.render.IntrinsicTileEntityInterface;
import valkyrienwarfare.mod.common.physics.collision.polygons.Polygon;
import valkyrienwarfare.mod.common.physics.management.PhysicsObject;
import valkyrienwarfare.mod.common.util.ValkyrienUtils;

import java.util.Optional;

@Mixin(TileEntity.class)
@Implements(@Interface(iface = IntrinsicTileEntityInterface.class, prefix = "vw$"))
public abstract class MixinTileEntityCLIENT {

    private final TileEntity thisAsTileEntity = TileEntity.class.cast(this);

    @Intrinsic(displace = true)
    public AxisAlignedBB vw$getRenderBoundingBox() {
        AxisAlignedBB toReturn = thisAsTileEntity.getRenderBoundingBox();
        BlockPos pos = new BlockPos(toReturn.minX, toReturn.minY, toReturn.minZ);
        Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(thisAsTileEntity.getWorld(), pos);
        if (physicsObject.isPresent()) {
            Polygon poly = new Polygon(toReturn, physicsObject.get()
                    .getShipTransformationManager()
                    .getCurrentTickTransform(), TransformType.SUBSPACE_TO_GLOBAL);
            return poly.getEnclosedAABB();
        }
        return toReturn;
    }

}
