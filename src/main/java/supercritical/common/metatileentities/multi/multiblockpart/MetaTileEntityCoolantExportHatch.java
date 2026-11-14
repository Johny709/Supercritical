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
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import supercritical.api.capability.ICoolantHandler;
import supercritical.api.capability.impl.LockableFluidTank;
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch;
import supercritical.api.metatileentity.multiblock.SCMultiblockAbility;
import supercritical.api.nuclear.fission.ICoolantStats;

public class MetaTileEntityCoolantExportHatch extends MetaTileEntityMultiblockNotifiablePart
		implements
			IMultiblockAbilityPart<ICoolantHandler>,
			ICoolantHandler,
			IControllable,
			IFissionReactorHatch,
		IMetaTileEntityGuiHolder {

	private boolean workingEnabled;
	private LockableFluidTank fluidTank;
	private ICoolantStats coolant;

	public MetaTileEntityCoolantExportHatch(ResourceLocation metaTileEntityId) {
		super(metaTileEntityId, 4, true);
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
		return new MetaTileEntityCoolantExportHatch(metaTileEntityId);
	}

	@Override
	public @NotNull ModularPanel buildUI(MetaTileEntityGuiData metaTileEntityGuiData, PanelSyncManager syncManager, UISettings uiSettings) {
		IntSyncValue fluidAmount = new IntSyncValue(this.fluidTank::getFluidAmount);
		StringSyncValue fluidName = new StringSyncValue(() -> this.fluidTank.getFluid() != null ? this.fluidTank.getFluid().getLocalizedName() : "");
		syncManager.syncValue("fluid_amount", fluidAmount);
		syncManager.syncValue("fluid_name", fluidName);
		syncManager.registerSlotGroup(new SlotGroup("bucket_in", 1, 1, true));

		return ModularPanel.defaultPanel("coolant_export_hatch")
				.child(IKey.lang(this.getMetaFullName()).asWidget()
						.pos(7, 5))
				.child(GTGuiTextures.DISPLAY.asWidget()
						.pos(7, 16)
						.size(81, 55))
				.child(new RichTextWidget()
						.pos(11, 1)
						.size(77)
						.autoUpdate(true)
						.textBuilder(richText -> {
							richText.addLine(KeyUtil.lang(TextFormatting.WHITE, "gregtech.gui.fluid_amount"));
							richText.addLine(KeyUtil.string(TextFormatting.WHITE, String.valueOf(fluidAmount.getStringValue())));
							richText.addLine(KeyUtil.string(TextFormatting.WHITE, fluidName.getStringValue()));
						}))
				.child(new FluidSlot()
						.pos(69, 52)
						.tank(this.fluidTank))
				.child(new ItemSlot()
						.pos(90, 16)
						.background(GTGuiTextures.SLOT, GTGuiTextures.IN_SLOT_OVERLAY)
						.slot(new ModularSlot(this.importItems, 0)
								.slotGroup("bucket_in")))
				.child(GTGuiTextures.TANK_ICON.asWidget()
						.pos(91, 36)
						.size(14, 15))
				.child(new ItemSlot()
						.pos(90, 53)
						.background(GTGuiTextures.SLOT, GTGuiTextures.OUT_SLOT_OVERLAY)
						.slot(new ModularSlot(this.exportItems, 0)
								.canPut(false)))
				.bindPlayerInventory();
	}

	@Override
	public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
		super.renderMetaTileEntity(renderState, translation, pipeline);
		if (shouldRenderOverlay()) {
			Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
			Textures.FLUID_HATCH_OUTPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
		}
	}

	@Override
	public boolean checkValidity(int depth) {
		// Export ports are always considered valid
		return true;
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
			this.fluidTank.setLock(buf.readBoolean());
		}
	}

	@Override
	public boolean isLocked() {
		return fluidTank.isLocked();
	}

	@Override
	public Fluid getLockedObject() {
		return fluidTank.getLockedObject();
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
		return this;
	}

	@Override
	public MultiblockAbility<ICoolantHandler> getAbility() {
		return SCMultiblockAbility.EXPORT_COOLANT;
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
	protected FluidTankList createExportFluidHandler() {
		this.fluidTank = new LockableFluidTank(16000, this, true);
		return new FluidTankList(false, fluidTank);
	}

	@Override
	public void update() {
		super.update();
		if (!getWorld().isRemote) {
			fillContainerFromInternalTank(fluidTank);
			pushFluidsIntoNearbyHandlers(getFrontFacing());
		}
	}
}
