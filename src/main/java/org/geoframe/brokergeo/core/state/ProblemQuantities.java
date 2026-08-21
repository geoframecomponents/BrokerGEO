package org.geoframe.brokergeo.core.state;

import oms3.annotations.Description;
import oms3.annotations.Unit;

public class ProblemQuantities {

	@Description("Sum of Initial Condition for root density")
	@Unit("-")
	public double sumRootDensity;

	// @Description("Vector of root density")
	// @Unit("-")
	// public double [] rootDensity;

	@Description("Number of control volume for domain discetrization")
	@Unit("-")
	public int NUM_CONTROL_VOLUMES;

	@Description("Depth of the domain")
	@Unit("m")
	public double totalDepth;

	@Description("The stressed Evapotranspiration for each control volume")
	@Unit("mm/s")
	public double[] StressedETs;

	@Description("Depth of the root from the bottom.")
	@Unit("m")
	public double zR;

	@Description("Depth of the evaporation layer from the bottom")
	@Unit("m")
	public double zE;

	@Description("The stressed Transpiration for each control volume")
	@Unit("mm/s")
	public double[] transpirations;

	@Description("The stressed Evaporation for each control volume within the Evaporation layer")
	@Unit("mm/s")
	public double[] evaporations;

	public double[] fluxRefs;

	public double control;

	@Description("Depth of the layer for SizeWaterWeightedMethod and SizeWeightedMethod  and for simplest Broker method")
	@Unit("m")
	public double etaRef;

	public double sumRootWaterStress;

	public double step;

	public int N;

	@Description("The stressed Evapotranspiration for each control volume")
	@Unit("mm/s")
	public double[] evapotranspirations;

	@Description("Depth of the layer from the bottom for simplest Broker method")
	@Unit("m")
	public double zRef;

}
