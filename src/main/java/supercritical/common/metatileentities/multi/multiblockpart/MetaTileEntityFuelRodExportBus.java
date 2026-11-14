package supercritical.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import gregtech.api.capability.IControllable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.IMetaTileEntityGuiHolder;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch;
import supercritical.api.metatileentity.multiblock.SCMultiblockAbility;

public class MetaTileEntityFuelRodExportBus extends MetaTileEntityMultiblockNotifiablePart
		implements
			IMultiblockAbilityPart<IItemHandlerModifiable>,
			IControllable,
			IFissionReactorHatch,
			IMetaTileEntityGuiHolder {

	private boolean workingEnabled;

	public MetaTileEntityFuelRodExportBus(ResourceLocation metaTileEntityId) {
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
		return new MetaTileEntityFuelRodExportBus(metaTileEntityId);
	}

	@Override
	public void update() {
		super.update();
		if (!getWorld().isRemote && getOffsetTimer() % 5 == 0) {
			pushItemsIntoNearbyHandlers(getFrontFacing());
		}
	}

	@Override
	protected IItemHandlerModifiable createExportItemHandler() {
		return new ItemStackHandler(1);
	}

	@Override
	protected IItemHandlerModifiable createImportItemHandler() {
		return new ItemStackHandler(1);
	}

	@Override
	public @NotNull ModularPanel buildUI(MetaTileEntityGuiData metaTileEntityGuiData, PanelSyncManager panelSyncManager, UISettings uiSettings) {
		return ModularPanel.defaultPanel("fuel_rod_export")
				.child(IKey.lang(this.getMetaFullName()).asWidget()
						.pos(7, 5))
				.child(new ItemSlot()
						.pos(79, 18)
						.background(GTGuiTextures.SLOT)
						.slot(new ModularSlot(this.exportItems, 0)
								.canPut(false)))
				.bindPlayerInventory();
	}

	@Override
	public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
		super.renderMetaTileEntity(renderState, translation, pipeline);
		if (shouldRenderOverlay()) {
			Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
			Textures.ITEM_HATCH_OUTPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
		}
	}

	@Override
	public MultiblockAbility<IItemHandlerModifiable> getAbility() {
		return SCMultiblockAbility.EXPORT_FUEL_ROD;
	}

	@Override
	public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
		abilityInstances.add(this.exportItems);
	}

	@Override
	public boolean checkValidity(int depth) {
		return true;
	}
}
