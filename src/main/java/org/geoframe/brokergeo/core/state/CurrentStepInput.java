package org.geoframe.brokergeo.core.state;


import oms3.annotations.Description;
import oms3.annotations.Unit;

public class CurrentStepInput {
	
	@Description("Depth of the root.")
	@Unit("m")
	public double etaR;
	
	@Description("Depth of the Evaporation layer.")
	@Unit("m")
	public double etaE;
	
	@Description("z coordinate read from grid NetCDF file.")
	@Unit("m")
	public double[] z;
	
	@Description("The stressed Transpiration from Prospero model.")
	@Unit("mm/s")
	public double transpiration;
	
	@Description("The stressed Evaporation from Prospero model.")
	@Unit("mm/s")
	public double evaporation;
	
	@Description("The stressed flux from a generic model.")
	@Unit("mm/s")
	public double flux;
	
	@Description("Vector containing the length of each control volume")
	@Unit("m")
	public double[] deltaZ;
	
	@Description("Evaporation from each control volume can be evaluated in different way"
			+ " AverageWaterWeightedMethod, AverageWeightedMethod"
		    + " SizeWaterWeightedMetod, SizeWeightedMethod")
	public String representativeEsModel;
	
	@Description("Transpiration from each control volume can be evaluated in different way"
			+ " AverageWaterWeightedMethod, AverageWeightedMethod"
		    + " SizeWaterWeightedMetod, SizeWeightedMethod"
			+ " RootWaterWeightedMethod, RootWeightedMethod")
	public String representativeTsModel;
	
	@Description("EvapoTranspiration from each control volume can be evaluated in different way"
			+ " AverageWaterWeightedMethod, AverageWeightedMethod"
		    + " SizeWaterWeightedMetod, SizeWeightedMethod"
			+ " RootWaterWeightedMethod, RootWeightedMethod")
	public String representativeETsModel;

	@Description("The generic flux from each control volume can be evaluated in different way"
			+ " AverageWaterWeightedMethod, AverageWeightedMethod"
		    + " SizeWaterWeightedMetod, SizeWeightedMethod"
			+ " RootWaterWeightedMethod, RootWeightedMethod")
	public String representativeModel;
	
	
	@Description("Vector of root density")
	@Unit("-")
	public double [] rootDensity;
	
	
	@Description("The stress factor for each control volume")
	@Unit("-")
	public double[] g;
	
	@Description("Vector of G and n, for transpiration")
	@Unit("-")
	public double[] GnT;
	
	
	@Description("Vector of G and n, for evaporation")
	@Unit("-")
	public double[] GnE;
	
	@Description("Vector of G and n, for a generic flux")
	@Unit("-")
	public double[] Gn;
	
	@Description(" ")
	@Unit("-")
	public double etaRef;
	
	@Description("The stressed EvapoTranspiration from a general model.")
	@Unit("mm/s")
	public double evapotranspiration;
	
}
