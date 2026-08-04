package supercritical.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;

import java.util.Collections;

/**
 * Recipe Logic for a Multiblock that does not require power.
 */
public class NoEnergyRecipeLogic extends MultiblockRecipeLogic {

	public NoEnergyRecipeLogic(RecipeMapMultiblockController tileEntity) {
		super(tileEntity);
	}

	@Override
	protected long getEnergyInputPerSecond() {
		return Integer.MAX_VALUE;
	}

	@Override
	protected long getEnergyStored() {
		return Integer.MAX_VALUE;
	}

	@Override
	protected long getEnergyCapacity() {
		return Integer.MAX_VALUE;
	}

	@Override
	protected boolean drawEnergy(long recipeEUt, boolean simulate) {
		return true; // spoof energy being drawn
	}

	@Override
	protected boolean hasEnoughPower(long eut, int duration) {
		return true;
	}

	@Override
	public long getMaxVoltage() {
		return GTValues.LV;
	}

	@Override
	public long getMaximumOverclockVoltage() {
		return GTValues.V[GTValues.LV];
	}

	/**
	 * Used to reset cached values in the Recipe Logic on structure deform
	 */
	public void invalidate() {
		previousRecipe = null;
		progressTime = 0;
		maxProgressTime = 0;
		recipeEUt = 0;
		fluidOutputs = Collections.emptyList();
		itemOutputs = Collections.emptyList();
		setActive(false); // this marks dirty for us
	}
}
