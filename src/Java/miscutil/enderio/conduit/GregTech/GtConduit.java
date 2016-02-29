package miscutil.enderio.conduit.GregTech;

import java.util.HashMap;
import java.util.Map;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.EnderIO;
import crazypants.enderio.conduit.AbstractConduitNetwork;
import crazypants.enderio.conduit.ConnectionMode;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.geom.CollidableComponent;
import crazypants.enderio.config.Config;
import crazypants.render.IconUtil;
import crazypants.util.BlockCoord;

public class GtConduit extends AbstractGtTankConduit {

  public static final int CONDUIT_VOLUME = 1000;

  public static final String ICON_KEY = "enderio:gasConduit";
  public static final String ICON_CORE_KEY = "enderio:gasConduitCore";
  public static final String ICON_EXTRACT_KEY = "enderio:gasConduitInput";
  public static final String ICON_INSERT_KEY = "enderio:gasConduitOutput";
  public static final String ICON_EMPTY_EDGE = "enderio:gasConduitEdge";

  static final Map<String, IIcon> ICONS = new HashMap<String, IIcon>();

  @SideOnly(Side.CLIENT)
  public static void initIcons() {
    IconUtil.addIconProvider(new IconUtil.IIconProvider() {

      @Override
      public void registerIcons(IIconRegister register) {
        ICONS.put(ICON_KEY, register.registerIcon(ICON_KEY));
        ICONS.put(ICON_CORE_KEY, register.registerIcon(ICON_CORE_KEY));
        ICONS.put(ICON_EXTRACT_KEY, register.registerIcon(ICON_EXTRACT_KEY));
        ICONS.put(ICON_INSERT_KEY, register.registerIcon(ICON_INSERT_KEY));
        ICONS.put(ICON_EMPTY_EDGE, register.registerIcon(ICON_EMPTY_EDGE));
      }

      @Override
      public int getTextureType() {
        return 0;
      }

    });
  }

  private GtConduitNetwork network;

  private long ticksSinceFailedExtract = 0;

  public static final int MAX_EXTRACT_PER_TICK = Config.gasConduitExtractRate;

  public static final int MAX_IO_PER_TICK = Config.gasConduitMaxIoRate;

  public GtConduit() {
    updateTank();
  }

  @Override
  public void updateEntity(World world) {
    super.updateEntity(world);
    if(world.isRemote) {
      return;
    }
    doExtract();
    if(stateDirty) {
      getBundle().dirty();
      stateDirty = false;
    }
  }

  private void doExtract() {
    BlockCoord loc = getLocation();
    if(!hasConnectionMode(ConnectionMode.INPUT)) {
      return;
    }
    if(network == null) {
      return;
    }

    // assume failure, reset to 0 if we do extract
    ticksSinceFailedExtract++;
    if(ticksSinceFailedExtract > 25 && ticksSinceFailedExtract % 10 != 0) {
      // after 25 ticks of failing, only check every 10 ticks
      return;
    }

    Gas f = tank.getGas() == null ? null : tank.getGas().getGas();
    for (ForgeDirection dir : externalConnections) {
      if(autoExtractForDir(dir)) {
        if(network.extractFrom(this, dir, MAX_EXTRACT_PER_TICK)) {
          ticksSinceFailedExtract = 0;
        }
      }
    }

  }

  @Override
  protected void updateTank() {
    tank.setCapacity(CONDUIT_VOLUME);
    if(network != null) {
      network.updateConduitVolumes();
    }
  }

  @Override
  public ItemStack createItem() {
    return new ItemStack(EnderIO.itemGasConduit);
  }

  @Override
  public AbstractConduitNetwork<?, ?> getNetwork() {
    return network;
  }

  @Override
  public boolean setNetwork(AbstractConduitNetwork<?, ?> network) {
    if(network == null) {
      this.network = null;
      return true;
    }
    if(!(network instanceof GtConduitNetwork)) {
      return false;
    }

    GtConduitNetwork n = (GtConduitNetwork) network;
    if(tank.getGas() == null) {
      tank.setGas(n.getGasType() == null ? null : n.getGasType().copy());
    } else if(n.getGasType() == null) {
      n.setGasType(tank.getGas());
    } else if(!tank.getGas().isGasEqual(n.getGasType())) {
      return false;
    }
    this.network = n;
    return true;

  }

  @Override
  public boolean canConnectToConduit(ForgeDirection direction, IConduit con) {
    if(!super.canConnectToConduit(direction, con)) {
      return false;
    }
    if(!(con instanceof GtConduit)) {
      return false;
    }
    if(getGasType() != null && ((GtConduit) con).getGasType() == null) {
      return false;
    }
    return GtConduitNetwork.areGassCompatable(getGasType(), ((GtConduit) con).getGasType());
  }

  @Override
  public void setConnectionMode(ForgeDirection dir, ConnectionMode mode) {
    super.setConnectionMode(dir, mode);
    refreshInputs(dir);
  }

  private void refreshInputs(ForgeDirection dir) {
    if(network == null) {
      return;
    }
    GtOutput lo = new GtOutput(getLocation().getLocation(dir), dir.getOpposite());
    network.removeInput(lo);
    if(getConnectionMode(dir).acceptsOutput() && containsExternalConnection(dir)) {
      network.addInput(lo);
    }
  }

  @Override
  public void externalConnectionAdded(ForgeDirection fromDirection) {
    super.externalConnectionAdded(fromDirection);
    refreshInputs(fromDirection);
  }

  @Override
  public void externalConnectionRemoved(ForgeDirection fromDirection) {
    super.externalConnectionRemoved(fromDirection);
    refreshInputs(fromDirection);
  }

  @Override
  public IIcon getTextureForState(CollidableComponent component) {
    if(component.dir == ForgeDirection.UNKNOWN) {
      return ICONS.get(ICON_CORE_KEY);
    }
    return ICONS.get(ICON_KEY);
  }

  public IIcon getTextureForInputMode() {
    return ICONS.get(ICON_EXTRACT_KEY);
  }

  public IIcon getTextureForOutputMode() {
    return ICONS.get(ICON_INSERT_KEY);
  }

  public IIcon getNotSetEdgeTexture() {
    return ICONS.get(ICON_EMPTY_EDGE);
  }

  @Override
  public IIcon getTransmitionTextureForState(CollidableComponent component) {
    if(isActive() && tank.containsValidGas()) {
      return tank.getGas().getGas().getIcon();
    }
    return null;
  }

  // ------------------------------------------- Gas API

  @Override
  public int receiveGas(ForgeDirection from, GasStack resource) {
    if(network == null || !getConnectionMode(from).acceptsInput()) {
      return 0;
    }
    return network.fill(from, resource, true);
  }

  @Override
  public GasStack drawGas(ForgeDirection from, int maxDrain) {
    if(network == null || !getConnectionMode(from).acceptsOutput()) {
      return null;
    }
    return network.drain(from, maxDrain, true);
  }

  @Override
  public boolean canReceiveGas(ForgeDirection from, Gas gas) {
    if(network == null) {
      return false;
    }
    return getConnectionMode(from).acceptsInput() && GtConduitNetwork.areGassCompatable(getGasType(), new GasStack(gas, 0));
  }

  @Override
  public boolean canDrawGas(ForgeDirection from, Gas gas) {
    if(network == null) {
      return false;
    }
    return getConnectionMode(from).acceptsOutput() && GtConduitNetwork.areGassCompatable(getGasType(), new GasStack(gas, 0));
  }

  @Override
  protected boolean canJoinNeighbour(IGtConduit n) {
    return n instanceof GtConduit;
  }

  @Override
  public AbstractGtTankConduitNetwork<? extends AbstractGtTankConduit> getTankNetwork() {
    return network;
  }

}