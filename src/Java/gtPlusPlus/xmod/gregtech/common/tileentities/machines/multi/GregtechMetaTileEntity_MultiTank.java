package gtPlusPlus.xmod.gregtech.common.tileentities.machines.multi;

import gregtech.api.enums.Textures;
import gregtech.api.gui.GT_GUIContainer_MultiMachine;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Input;
import gregtech.api.objects.GT_RenderedTexture;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import gtPlusPlus.core.block.ModBlocks;
import gtPlusPlus.core.util.Utils;
import gtPlusPlus.core.util.array.Pair;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.GregtechMeta_MultiBlockBase;
import gtPlusPlus.xmod.gregtech.common.blocks.textures.TexturesGtBlock;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

public class GregtechMetaTileEntity_MultiTank
extends GregtechMeta_MultiBlockBase {
	public GregtechMetaTileEntity_MultiTank(int aID, String aName, String aNameRegional) {
		super(aID, aName, aNameRegional);
	}

	private long fluidStored = 0;
	private short multiblockCasingCount = 0;
	private short storageMultiplier = getStorageMultiplier();
	private long maximumFluidStorage = getMaximumTankStorage();
	private FluidStack internalStorageTank = null;

	@Override
	public void saveNBTData(NBTTagCompound aNBT) {
		aNBT.setInteger("mEUt", mEUt);
		aNBT.setInteger("mProgresstime", mProgresstime);
		aNBT.setInteger("mMaxProgresstime", mMaxProgresstime);
		aNBT.setInteger("mEfficiencyIncrease", mEfficiencyIncrease);
		aNBT.setInteger("mEfficiency", mEfficiency);
		aNBT.setInteger("mPollution", mPollution);
		aNBT.setInteger("mRuntime", mRuntime);
		aNBT.setLong("mFluidStored", fluidStored);
		aNBT.setShort("mStorageMultiplier", storageMultiplier);
		aNBT.setLong("mMaxFluidStored", maximumFluidStorage);
		aNBT.setShort("mCasingCount", multiblockCasingCount);

		if (mOutputItems != null) for (int i = 0; i < mOutputItems.length; i++)
			if (mOutputItems[i] != null) {
				NBTTagCompound tNBT = new NBTTagCompound();
				mOutputItems[i].writeToNBT(tNBT);
				aNBT.setTag("mOutputItem" + i, tNBT);
			}
		if (mOutputFluids != null) for (int i = 0; i < mOutputFluids.length; i++)
			if (mOutputFluids[i] != null) {
				NBTTagCompound tNBT = new NBTTagCompound();
				mOutputFluids[i].writeToNBT(tNBT);
				aNBT.setTag("mOutputFluids" + i, tNBT);
			}

		aNBT.setBoolean("mWrench", mWrench);
		aNBT.setBoolean("mScrewdriver", mScrewdriver);
		aNBT.setBoolean("mSoftHammer", mSoftHammer);
		aNBT.setBoolean("mHardHammer", mHardHammer);
		aNBT.setBoolean("mSolderingTool", mSolderingTool);
		aNBT.setBoolean("mCrowbar", mCrowbar);
	}

	private short getStorageMultiplier(){
		int tempstorageMultiplier = (1*multiblockCasingCount);
		if (tempstorageMultiplier <= 0){
			return 1;
		}
		return (short) tempstorageMultiplier;
	}

	private long getMaximumTankStorage(){
		int multiplier = getStorageMultiplier();
		Utils.LOG_WARNING("x = "+multiplier+" * 96000");
		long tempTankStorageMax = (96000*multiplier);
		Utils.LOG_WARNING("x = "+tempTankStorageMax);
		if (tempTankStorageMax <= 0){
			return 96000;
		}
		return tempTankStorageMax;
	}


	@Override
	public void loadNBTData(NBTTagCompound aNBT) {
		mEUt = aNBT.getInteger("mEUt");
		mProgresstime = aNBT.getInteger("mProgresstime");
		mMaxProgresstime = aNBT.getInteger("mMaxProgresstime");
		if (mMaxProgresstime > 0) mRunningOnLoad = true;
		mEfficiencyIncrease = aNBT.getInteger("mEfficiencyIncrease");
		mEfficiency = aNBT.getInteger("mEfficiency");
		mPollution = aNBT.getInteger("mPollution");
		mRuntime = aNBT.getInteger("mRuntime");
		fluidStored = aNBT.getLong("mFluidStored");
		storageMultiplier = aNBT.getShort("mStorageMultiplier");
		maximumFluidStorage = aNBT.getLong("mMaxFluidStored");
		multiblockCasingCount = aNBT.getShort("mCasingCount");
		mOutputItems = new ItemStack[getAmountOfOutputs()];
		for (int i = 0; i < mOutputItems.length; i++) mOutputItems[i] = GT_Utility.loadItem(aNBT, "mOutputItem" + i);
		mOutputFluids = new FluidStack[getAmountOfOutputs()];
		for (int i = 0; i < mOutputFluids.length; i++)
			mOutputFluids[i] = GT_Utility.loadFluid(aNBT, "mOutputFluids" + i);
		mWrench = aNBT.getBoolean("mWrench");
		mScrewdriver = aNBT.getBoolean("mScrewdriver");
		mSoftHammer = aNBT.getBoolean("mSoftHammer");
		mHardHammer = aNBT.getBoolean("mHardHammer");
		mSolderingTool = aNBT.getBoolean("mSolderingTool");
		mCrowbar = aNBT.getBoolean("mCrowbar");
	}

	public GregtechMetaTileEntity_MultiTank(String aName) {
		super(aName);
	}

	@Override
	public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
		return new GregtechMetaTileEntity_MultiTank(this.mName);
	}

	@Override
	public String[] getDescription() {
		return new String[]{
				"Controller Block for the Multitank",
				"Size: 3xHx3 (Block behind controller must be air)", 
				"Structure must be at least 4 blocks tall, maximum 20.",
				"Each casing within the structure adds 96000L storage.",
				"Controller (front centered)",
				"1x Input hatch (anywhere)", 
				"1x Output hatch (anywhere)",
				"1x Energy Hatch (anywhere)",
				"1x Maintenance Hatch (anywhere)",
				"Multitank Exterior Casings for the rest (16 at least!)",
				"Stored Fluid: "+fluidStored};
	}

	@Override
	public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
		if (aSide == aFacing) {
			return new ITexture[]{Textures.BlockIcons.CASING_BLOCKS[68], new GT_RenderedTexture(aActive ? TexturesGtBlock.Overlay_Machine_Screen_Logo : TexturesGtBlock.Overlay_Machine_Screen_Logo)};
		}
		return new ITexture[]{Textures.BlockIcons.CASING_BLOCKS[68]};
	}

	@Override
	public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
		return new GT_GUIContainer_MultiMachine(aPlayerInventory, aBaseMetaTileEntity, getLocalName(), "VacuumFreezer.png");
	}

	@Override
	public GT_Recipe.GT_Recipe_Map getRecipeMap() {
		return null;
	}

	@Override
	public boolean isFacingValid(byte aFacing) {
		return aFacing > 1;
	}

	@Override
	public boolean checkRecipe(ItemStack aStack) {
		Utils.LOG_INFO("Okay");
		
		
		
		ArrayList<ItemStack> tInputList = getStoredInputs();
		for (int i = 0; i < tInputList.size() - 1; i++) {
			for (int j = i + 1; j < tInputList.size(); j++) {
				if (GT_Utility.areStacksEqual((ItemStack) tInputList.get(i), (ItemStack) tInputList.get(j))) {
					if (((ItemStack) tInputList.get(i)).stackSize >= ((ItemStack) tInputList.get(j)).stackSize) {
						tInputList.remove(j--);
					} else {
						tInputList.remove(i--);
						break;
					}
				}
			}
		}
		ItemStack[] tInputs = (ItemStack[]) Arrays.copyOfRange(tInputList.toArray(new ItemStack[tInputList.size()]), 0, 2);

		ArrayList<FluidStack> tFluidList = getStoredFluids();
		for (int i = 0; i < tFluidList.size() - 1; i++) {
			for (int j = i + 1; j < tFluidList.size(); j++) {
				if (GT_Utility.areFluidsEqual((FluidStack) tFluidList.get(i), (FluidStack) tFluidList.get(j))) {
					if (((FluidStack) tFluidList.get(i)).amount >= ((FluidStack) tFluidList.get(j)).amount) {
						tFluidList.remove(j--);
					} else {
						tFluidList.remove(i--);
						break;
					}
				}
			}
		}
		FluidStack[] tFluids = (FluidStack[]) Arrays.copyOfRange(tFluidList.toArray(new FluidStack[1]), 0, 1);
		
		if (tFluids.length >= 2){
			Utils.LOG_INFO("Bad");
			return false;
		}
		
		ArrayList<Pair<GT_MetaTileEntity_Hatch_Input, Boolean>> rList = new ArrayList<Pair<GT_MetaTileEntity_Hatch_Input, Boolean>>();
		int slotInputCount = 0;
		for (GT_MetaTileEntity_Hatch_Input tHatch : mInputHatches) {
			boolean containsFluid = false;
			if (isValidMetaTileEntity(tHatch)) {
				slotInputCount++;
				for (int i=0; i<tHatch.getBaseMetaTileEntity().getSizeInventory(); i++) {
					if (tHatch.canTankBeEmptied()){containsFluid=true;}					
				}
				rList.add(new Pair<GT_MetaTileEntity_Hatch_Input, Boolean>(tHatch, containsFluid));
			}
		}
		if (tFluids.length <= 0 || slotInputCount > 1){
			Utils.LOG_INFO("Bad");
			return false;
		}
		
		Utils.LOG_INFO("Okay - 2");  
		if (internalStorageTank == null){
			if (rList.get(0).getKey().mFluid != null && rList.get(0).getKey().mFluid.amount > 0){
				Utils.LOG_INFO("Okay - 1"+" rList.get(0).getKey().mFluid.amount: "+rList.get(0).getKey().mFluid.amount +" internalStorageTank:"+internalStorageTank.amount);  
				internalStorageTank = rList.get(0).getKey().mFluid;
				internalStorageTank.amount = rList.get(0).getKey().mFluid.amount;
				rList.get(0).getKey().mFluid.amount = 0;
				Utils.LOG_INFO("Okay - 1.1"+" rList.get(0).getKey().mFluid.amount: "+rList.get(0).getKey().mFluid.amount +" internalStorageTank:"+internalStorageTank.amount);  
				return true;
			}
			Utils.LOG_INFO("No Fluid in hatch.");
			return false;			
		}
		else if (internalStorageTank.isFluidEqual(rList.get(0).getKey().mFluid)){
			Utils.LOG_INFO("Storing "+rList.get(0).getKey().mFluid.amount+"L");
			Utils.LOG_INFO("Contains "+internalStorageTank.amount+"L");
			
			int tempAdd = 0;
			tempAdd = rList.get(0).getKey().getFluidAmount();
			rList.get(0).getKey().mFluid = null;		
			Utils.LOG_INFO("adding "+tempAdd);
			internalStorageTank.amount = internalStorageTank.amount + tempAdd;			
			Utils.LOG_INFO("Tank now Contains "+internalStorageTank.amount+"L of "+internalStorageTank.getFluid().getName()+".");
			
				if (mOutputHatches.get(0).mFluid == null || mOutputHatches.isEmpty()){
					Utils.LOG_INFO("Okay - 3");  
					int tempCurrentStored = internalStorageTank.amount;
					int tempSubtract = 0;
					int tempResult = 0;
					int tempHatchSize = mOutputHatches.get(0).getCapacity();
					FluidStack tempOutputFluid = internalStorageTank;
					if (tempHatchSize > tempCurrentStored){
						Utils.LOG_INFO("Okay - 3.1.1"+" hatchCapacity: "+tempHatchSize +" tempCurrentStored:"+tempCurrentStored);  
						tempOutputFluid.amount = tempHatchSize;
						tempSubtract = tempHatchSize;
						tempResult = tempCurrentStored - tempSubtract;	
						Utils.LOG_INFO("Okay - 3.1.2"+" result: "+tempResult +" tempCurrentStored:"+tempCurrentStored);  										
						mOutputHatches.get(0).mFluid = tempOutputFluid;			
						internalStorageTank.amount = tempResult;
					}
					else if (tempCurrentStored >= 5000){
						Utils.LOG_INFO("Okay - 3.2");  
						tempOutputFluid.amount = tempCurrentStored;
						tempSubtract = tempOutputFluid.amount;
						tempResult = tempCurrentStored - tempSubtract;											
						mOutputHatches.get(0).mFluid = tempOutputFluid;			
						internalStorageTank.amount = tempResult;
					}
					Utils.LOG_INFO("Tank");
					return true;
				}
				else if (mOutputHatches.get(0).mFluid.isFluidEqual(internalStorageTank)){
					Utils.LOG_INFO("Okay - 4");  
					int tempCurrentStored = internalStorageTank.amount;
					int tempSubtract = 0;
					int tempResult = 0;
					int tempHatchSize = mOutputHatches.get(0).getCapacity();
					FluidStack tempOutputFluid = internalStorageTank;
					if (tempHatchSize > tempCurrentStored){
						tempOutputFluid.amount = tempHatchSize;
						tempSubtract = tempOutputFluid.amount;
						tempResult = tempCurrentStored - tempSubtract;											
						mOutputHatches.get(0).mFluid = tempOutputFluid;			
						internalStorageTank.amount = tempResult;						
					}
					else if (tempCurrentStored >= 5000){
						tempOutputFluid.amount = tempCurrentStored;
						tempSubtract = tempOutputFluid.amount;
						tempResult = tempCurrentStored - tempSubtract;											
						mOutputHatches.get(0).mFluid = tempOutputFluid;			
						internalStorageTank.amount = tempResult;
					}
					Utils.LOG_INFO("Tank");
					return true;
				}			
				Utils.LOG_INFO("Tank");
			return true;
		}
		else {
			Utils.LOG_INFO("Tank Contains "+internalStorageTank.amount+"L of "+internalStorageTank.getFluid().getName()+".");
		}
		//this.getBaseMetaTileEntity().(tFluids[0].amount, true);        
		Utils.LOG_INFO("Tank");
		return false;
	}

	@Override
	public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
		int xDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetX;
		int zDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetZ;
		if (!aBaseMetaTileEntity.getAirOffset(xDir, 0, zDir)) {
			Utils.LOG_INFO("Must be hollow.");
			return false;
		}
		int tAmount = 0;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				for (int h = -1; h < 19; h++) {
					if ((h != 0) || (((xDir + i != 0) || (zDir + j != 0)) && ((i != 0) || (j != 0)))) {
						IGregTechTileEntity tTileEntity = aBaseMetaTileEntity.getIGregTechTileEntityOffset(xDir + i, h, zDir + j);
						if ((!addMaintenanceToMachineList(tTileEntity, 68)) && (!addInputToMachineList(tTileEntity, 68)) && (!addOutputToMachineList(tTileEntity, 68)) && (!addEnergyInputToMachineList(tTileEntity, 68))) {
							if (aBaseMetaTileEntity.getBlockOffset(xDir + i, h, zDir + j) != ModBlocks.blockCasingsMisc) {
								if (h < 3){
									Utils.LOG_INFO("Casing Expected.");
									return false;
								}
								else if (h >= 3){
									//Utils.LOG_WARNING("Your Multitank can be 20 blocks tall.");
								}
							}
							if (aBaseMetaTileEntity.getMetaIDOffset(xDir + i, h, zDir + j) != 11) {
								if (h < 3){
									Utils.LOG_INFO("Wrong Meta.");
									return false;
								}
								else if (h >= 3){
									//Utils.LOG_WARNING("Your Multitank can be 20 blocks tall.");
								}
							}
							if (h < 3){
								tAmount++;
							}
							else if (h >= 3){
								if (aBaseMetaTileEntity.getBlockOffset(xDir + i, h, zDir + j) == Blocks.air || aBaseMetaTileEntity.getBlockOffset(xDir + i, h, zDir + j).getUnlocalizedName().contains("residual")){
									Utils.LOG_INFO("Found air");
								}
								else {
									Utils.LOG_INFO("Layer "+(h+2)+" is complete. Adding "+(64000*9)+"L storage to the tank.");
									tAmount++;
								}
							}
						}
					}
				}
			}
		}
		multiblockCasingCount = (short) tAmount;
		Utils.LOG_INFO("Your Multitank can be 20 blocks tall.");
		Utils.LOG_INFO("Casings Count: "+tAmount+" Valid Multiblock: "+(tAmount >= 16)+" Tank Storage Capacity:"+getMaximumTankStorage()+"L");
		return tAmount >= 16;
	}

	@Override
	public int getMaxEfficiency(ItemStack aStack) {
		return 10000;
	}

	@Override
	public int getPollutionPerTick(ItemStack aStack) {
		return 0;
	}

	@Override
	public int getAmountOfOutputs() {
		return 1;
	}

	@Override
	public boolean explodesOnComponentBreak(ItemStack aStack) {
		return false;
	}
}