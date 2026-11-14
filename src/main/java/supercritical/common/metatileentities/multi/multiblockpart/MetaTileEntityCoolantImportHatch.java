package supercritical.common.metatileentities.multi.multiblockpart;

import static supercritical.SCValues.FISSION_LOCK_UPDATE;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.IMetaTileEntityGuiHolder;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import java.util.List;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supercritical.api.capability.ICoolantHandler;
import supercritical.api.capability.impl.LockableFluidTank;
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch;
import supercritical.api.metatileentity.multiblock.SCMultiblockAbility;
import supercritical.api.nuclear.fission.ICoolantStats;
import supercritical.common.blocks.BlockFissionCasing;
import supercritical.common.blocks.SCMetaBlocks;

public class MetaTileEntityCoolantImportHatch extends MetaTileEntityMultiblockNotifiablePart
		implements
			IMultiblockAbilityPart<ICoolantHandler>,
			ICoolantHandler,
			IControllable,
			IFissionReactorHatch,
			IMetaTileEntityGuiHolder {

	private boolean workingEnabled;
	private LockableFluidTank fluidTank;
	private ICoolantStats coolant;
	public MetaTileEntityCoolantExportHatch pairedHatch;

	public MetaTileEntityCoolantImportHatch(ResourceLocation metaTileEntityId) {
		super(metaTileEntityId, 4, false);
	}

	@Override
	protected FluidTankList createImportFluidHandler() {
		this.fluidTank = new LockableFluidTank(16000, this, false);
		return new FluidTankList(false, fluidTank);
	}

	@Override
	public boolean isWorkingEnabled() {
		return workingEnabled;
	}

	@Override
	public void setWorkingEnabled(boolean isWorkingAllowed) {
		this.workingEnabled = isWorkingAllowed;
	}

	@Override
	public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
		return new MetaTileEntityCoolantImportHatch(metaTileEntityId);
	}

	@Override
	public @NotNull ModularPanel buildUI(MetaTileEntityGuiData metaTileEntityGuiData, PanelSyncManager syncManager, UISettings uiSettings) {
		IntSyncValue fluidAmount = new IntSyncValue(this.fluidTank::getFluidAmount);
		StringSyncValue fluidName = new StringSyncValue(() -> this.fluidTank.getFluid() != null ? this.fluidTank.getFluid().getLocalizedName() : "");
		syncManager.syncValue("fluid_amount", fluidAmount);
		syncManager.syncValue("fluid_name", fluidName);
		syncManager.registerSlotGroup(new SlotGroup("bucket_in", 1, 1, true));

		return ModularPanel.defaultPanel("coolant_import_hatch")
				.child(IKey.lang(this.getMetaFullName()).asWidget()
						.pos(7, 5))
				.child(GTGuiTextures.DISPLAY.asWidget()
						.pos(7, 16)
						.size(131, 55))
				.child(new RichTextWidget()
						.pos(11, -29)
						.size(127)
						.autoUpdate(true)
						.textBuilder(richText -> {
							richText.addLine(KeyUtil.lang(TextFormatting.WHITE, "gregtech.gui.fluid_amount"));
							richText.addLine(KeyUtil.string(TextFormatting.WHITE, String.valueOf(fluidAmount.getStringValue())));
							richText.addLine(KeyUtil.string(TextFormatting.WHITE, fluidName.getStringValue()));
						}))
				.child(new FluidSlot()
						.pos(119, 52)
						.tank(this.fluidTank))
				.child(new ItemSlot()
						.pos(140, 16)
						.background(GTGuiTextures.SLOT, GTGuiTextures.IN_SLOT_OVERLAY)
						.slot(new ModularSlot(this.importItems, 0)
								.slotGroup("bucket_in")))
				.child(GTGuiTextures.TANK_ICON.asWidget()
						.pos(141, 36)
						.size(14, 15))
				.child(new ItemSlot()
						.pos(140, 53)
						.background(GTGuiTextures.SLOT, GTGuiTextures.OUT_SLOT_OVERLAY)
						.slot(new ModularSlot(this.exportItems, 0)
								.canPut(false)))
				.bindPlayerInventory();
	}

	@Override
	public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
		super.renderMetaTileEntity(renderState, translation, pipeline);
		if (shouldRenderOverlay()) {
			Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
			Textures.FLUID_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
		}
	}

	@Override
	public boolean checkValidity(int depth) {
		this.pairedHatch = getExportHatch(depth);
		return this.pairedHatch != null;
	}

	public MetaTileEntityCoolantExportHatch getExportHatch(int depth) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
		for (int i = 1; i < depth; i++) {
			if (getWorld().getBlockState(pos.move(this.frontFacing.getOpposite())) != SCMetaBlocks.FISSION_CASING
					.getState(BlockFissionCasing.FissionCasingType.COOLANT_CHANNEL)) {
				return null;
			}
		}
		if (getWorld().getTileEntity(pos.move(this.frontFacing.getOpposite())) instanceof IGregTechTileEntity gtTe) {
			MetaTileEntity mte = gtTe.getMetaTileEntity();
			if (mte instanceof MetaTileEntityCoolantExportHatch export) {
				return export;
			}
		}
		return null;
	}

	@Override
	public MultiblockAbility<ICoolantHandler> getAbility() {
		return SCMultiblockAbility.IMPORT_COOLANT;
	}

	@Override
	public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
		abilityInstances.add(this);
	}

	@Override
	protected IItemHandlerModifiable createImportItemHandler() {
		return new FilteredItemHandler(this).setFillPredicate(
				FilteredItemHandler.getCapabilityFilter(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY));
	}

	@Override
	protected IItemHandlerModifiable createExportItemHandler() {
		return new ItemStackHandler(1);
	}

	@Override
	public void update() {
		super.update();
		if (!getWorld().isRemote) {
			fillContainerFromInternalTank(fluidTank);
			fillInternalTankFromFluidContainer(fluidTank);
			pullFluidsFromNearbyHandlers(getFrontFacing());
		}
	}

	@Override
	public void setLock(boolean isLocked) {
		fluidTank.setLock(isLocked);
		writeCustomData(FISSION_LOCK_UPDATE, (packetBuffer -> packetBuffer.writeBoolean(isLocked)));
	}

	@Override
	public void receiveCustomData(int dataId, PacketBuffer buf) {
		super.receiveCustomData(dataId, buf);
		if (dataId == FISSION_LOCK_UPDATE) {
			fluidTank.setLock(buf.readBoolean());
		}
	}

	@Override
	public boolean isLocked() {
		return fluidTank.isLocked();
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
	}

	@Override
	public ICoolantStats getCoolant() {
		return this.coolant;
	}

	@Override
	public void setCoolant(ICoolantStats prop) {
		this.coolant = prop;
	}

	@Override
	public @NotNull LockableFluidTank getFluidTank() {
		return this.fluidTank;
	}

	@Override
	public ICoolantHandler getOutputHandler() {
		return pairedHatch;
	}

	@Override
	public Fluid getLockedObject() {
		return fluidTank.getLockedObject();
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
			boolean advanced) {
		super.addInformation(stack, world, tooltip, advanced);
		tooltip.add(I18n.format("supercritical.machine.nuclear.locking.fluid"));
	}
}
