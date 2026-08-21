package org.geoframe.brokergeo.data;

import java.util.Arrays;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Status;
import oms3.annotations.Unit;

@Description("")

@Author(name = "Concetta D'Amato and Riccardo Rigon", contact = "concetta.damato@unitn.it")
@Keywords("Evapotranspiration")
@Label("")
@Name("")
@Status(Status.CERTIFIED)
@License("General Public License Version 3 (GPLv3)")
public class InputDataMain {

	/////////////////////////////////////////////
	// VARIABLES - INPUT
	/////////////////////////////////////////////

	@Description("Depth of the root.")
	@In
	@Unit("m")
	public double etaR;

	@Description("Depth of the Evaporation layer.")
	@In
	@Unit("m")
	public double etaE;

	@Description("Depth of a generic layer.")
	@In
	@Unit("m")
	public double etaRef;

	@Description("z coordinate read from grid NetCDF file.")
	@In
	@Unit("m")
	public double[] z;

	@Description("The stressed Transpiration from Prospero model.")
	@In
	@Unit("mm/s")
	public double transpiration;

	@Description("The stressed Evaporation from Prospero model.")
	@In
	@Unit("mm/s")
	public double evaporation;

	@Description("The stressed flux from a generic model.")
	@In
	@Unit("mm/s")
	public double flux;

	@Description("Vector containing the length of each control volume")
	@In
	@Unit("m")
	public double[] deltaZ;

	/*
	 * @Description("Evaporation from each control volume can be evaluated in different way"
	 * + " Average method --> AverageMetod" +
	 * " Weighted average method --> SizeWightedMetod")
	 * 
	 * @In public String representativeEsModel;
	 * 
	 * @Description("Transpiration from each control volume can be evaluated in different way"
	 * + " Average method --> AverageMetod" +
	 * " Weighted average method --> SizeWightedMetod" +
	 * " Root weighted medod --> RootWightedMetod")
	 * 
	 * @In public String representativeTsModel;
	 * 
	 * @In public String representativeModel;
	 */

	@Description("The stress factor for each control volume")
	@In
	@Unit("-")
	public double[] g;

	@Description("Vector of G and n, for transpiration")
	@In
	@Unit("-")
	public double[] GnT;

	@Description("Vector of G and n, for evaporation")
	@In
	@Unit("-")
	public double[] GnE;

	@Description("Vector of G and n, for a generic flux")
	@In
	@Unit("-")
	public double[] Gn;

	@Description("Vector of root density")
	@In
	@Unit("-")
	public double[] rootDensity;

	public InputData input;

	@Execute
	public void process() throws Exception {
		if (input == null) {
			throw new Exception("InputData object is null. Please initialize it before calling process().");
		}

		input.etaR = etaR;
		input.etaE = etaE;
		input.etaRef = etaRef;
		input.z = z;
		input.transpiration = transpiration;
		input.evaporation = evaporation;
		input.flux = flux;
		input.deltaZ = deltaZ;

		input.g = g;
		input.GnT = GnT;
		input.GnE = GnE;
		input.Gn = Gn;
		// input.rootDensityIC = rootIC;
		input.rootDensity = rootDensity;
		// input.rootDensityModel = rootDensityModel;
		// input.growthRateRoot = growthRateRoot;

		// System.out.println("\ninput.rootDensity =
		// "+Arrays.toString(input.rootDensity));
		// System.out.println("\ninput.etaR = "+ input.etaR);
	}

}
