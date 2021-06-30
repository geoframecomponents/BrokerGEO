/*
* GNU GPL v3 License
 *
 * Copyright 2019 Concetta D'Amato
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package BrokerSolver;
import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Documentation;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;
import oms3.annotations.Unit;
//import java.util.Arrays;

//import java.util.Arrays;

import computeStressedETs.*;
@Description("This class is used to connect the Richard model with the evapotranspiration model, calculating the evapotranspiration for each control volume.")
@Documentation("")
@Author(name = "Concetta D'Amato", contact = "concetta.damato@unitn.it")


public class ETsBroker {
	
	@Description("Depth of the root.")
	@In
	@Unit("m")
	public double etaR;
	
	@Description("Depth of the Evaporation layer.")
	@In 
	@Unit("m")
	public double etaE;
	
	@Description("z coordinate read from grid NetCDF file.")
	@In
	@Unit("m")
	public double[] z;
	
	@Description("Depth of the root from the bottom.")
	@In
	@Unit("m")
	public double zR;
	
	@Description("Depth of the evaporation layer from the bottom")
	@In
	@Unit("m")
	public double zE;
	
	@Description("Depth of the domain")
	@In
	@Unit("m")
	public double totalDepth;
	
	/*@Description("The stressed Evapotranspiration from Prospero model.")
	@In
	@Unit("mm/s")
	public double StressedET;*/
	
	@Description("The stressed Transpiration from Prospero model.")
	@In
	@Unit("mm/s")
	public double transpiration;
	
	@Description("The stressed Evaporation from Prospero model.")
	@In
	@Unit("mm/s")
	public double evaporation;
	
	@Description("Vector containing the length of each control volume")
	@In
	@Unit("m")
	public double[] deltaZ;
	
	@Description("Number of control volume for domain discetrization")
	@In
	@Unit ("-")
	public int NUM_CONTROL_VOLUMES;
	
	@Description("Evaporation from each control volume can be evaluated in different way"
			    + " Average method --> AverageMetod"
			    + " Weighted average method --> SizeWightedMetod")
	@In
	public String representativeEsModel;
	
	@Description("Transpiration from each control volume can be evaluated in different way"
			+ " Average method --> AverageMetod"
		    + " Weighted average method --> SizeWightedMetod"
			+ " Root weighted medod --> RootWightedMetod")
	@In
	public String representativeTsModel;
	
	@Description("The stress factor for each control volume")
	@In
	@Unit("-")
	public double[] g;
	
	
	@Description("Vector of G and n, for transpiration and evaporation")
	@In
	@Unit("-")
	public double[] GnT;
	
	@Description("Vector of Initial Condition for root density")
	@In
	@Unit("-")
	public double[] rootIC;
	
	@Description("Sum of Initial Condition for root density")
	@In
	@Unit("-")
	public double sumRootIC;
	
	@Description("Vector of G and n, for evaporation")
	@In
	@Unit("-")
	public double[] GnE;
	
	@Description("The stressed Evapotranspiration for each control volume")
	@Out
	@Unit("mm/s")
	public double[] StressedETs;
	
	@Description("The stressed Transpiration for each control volume")
	@Unit("mm/s") 
	public double[] transpirations;
	
	@Description("The stressed Evaporation for each control volume within the Evaporation layer")
	@Unit("mm/s") 
	public double[] evaporations;
	
	@Description("It is needed to iterate on the date")
	int step;
	
	/////////////////////////////////////////////////////////////////////////////
	

	@Description("Object dealing with transpiration from each control volume of the domain")
	SplittedETs computedTs;
	
	@Description("Object dealing with evaporation from each control volume of the domain")
	SplittedETs computedEs;
	

	@Execute
	public void solve() {
		
		if(step==0){
			NUM_CONTROL_VOLUMES = z.length;
			totalDepth = z[NUM_CONTROL_VOLUMES -1];
			StressedETs = new double [NUM_CONTROL_VOLUMES -1];
		
			FactoryETs representativeETsFactory= new FactoryETs();
			computedEs = representativeETsFactory.createEvapoTranspirations(representativeEsModel, z, deltaZ, NUM_CONTROL_VOLUMES, totalDepth);
			
			computedTs = representativeETsFactory.createEvapoTranspirations(representativeTsModel, z, deltaZ, NUM_CONTROL_VOLUMES, totalDepth);
			
			zR = totalDepth + etaR;
			zE = totalDepth + etaE;
			
			for (int i = 0; i <= rootIC.length-1; i++) {
				sumRootIC= sumRootIC + rootIC[i];
			}
		}	
		
		
		transpirations = computedTs.computeStressedETs(g,GnT,transpiration,zR,rootIC,sumRootIC);
		evaporations = computedEs.computeStressedETs(g,GnE,evaporation,zE,rootIC,sumRootIC);
		
		for(int i = 0; i<=transpirations.length-1; i=i+1) {
	    	StressedETs [i] = evaporations[i] + transpirations[i];}
		
		//System.out.println("\n\nEvaporations = "+Arrays.toString(evaporations));
		//System.out.println("\n\nTranspirations = "+Arrays.toString(transpirations));
		//System.out.println("\n\nStressedETs = "+Arrays.toString(StressedETs));
		System.out.println("\n\nETsBroker Finished");
		//System.out.println("z = "+Arrays.toString(z));
		//System.out.println("\n\nStressedET  = "+ StressedET);
		
		step++;
	}
}
