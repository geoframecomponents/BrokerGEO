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
package org.geoframe.brokergeo.solver;
import org.geoframe.brokergeo.methods.*;
import org.geoframe.brokergeo.data.*;
import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Documentation;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;
import oms3.annotations.Unit;

@Description("This class is used to connect the Richard model with the evapotranspiration model, calculating the evapotranspiration for each control volume.")
@Documentation("")
@Author(name = "Concetta D'Amato", contact = "concetta.damato@unitn.it")


public class ETsBrokerSimpleSolverMain {

	@Description("It is needed to iterate on the date")
	int step;
	
	@Description("z coordinate read from grid NetCDF file.")
	@In
	@Unit("m")
	public double[] z;
	
	@Description("Vector containing the length of each control volume")
	@In
	@Unit("m")
	public double[] deltaZ;
	
	@Description("The stressed EvapoTranspiration from a general model.")
	@In
	@Unit("mm/s")
	public double evapotranspiration;
	
	@Description("Reference depth of evapotranspiration flux")
	@In
	@Unit("m")
	public double etaRef;
	
	@Description("The stressed Evapotranspiration for each control volume")
	@Out
	@Unit("mm/s")
	public double[] evapotranspirations;
	
	@Description("EvapoTranspiration from each control volume can be evaluated in different way"
			+ " AverageWaterWeightedMethod, AverageWeightedMethod"
		    + " SizeWaterWeightedMetod, SizeWeightedMethod"
			+ " RootWaterWeightedMethod, RootWeightedMethod")
	@In
	public String representativeETsModel;
	
	public double [] Gn = {0,0};
	

	/////////////////////////////////////////////////////////////////////////////
	

	@Description("Object dealing with transpiration from each control volume of the domain")
	SplittedETs computedFluxs;
	

	private ProblemQuantities variables;
	private InputData input;

	@Execute
	public void solve() {
		System.out.print("\n\nStart ETsBrokerSimpleSolverMain");
		
		variables = ProblemQuantities.getInstance();
		input = InputData.getInstance();
		
		
		input.z = z;
		input.deltaZ = deltaZ;
		input.evapotranspiration = evapotranspiration;
		input.etaRef = etaRef;
		
		
		if(step==0){
			input.representativeETsModel = representativeETsModel;
			variables.NUM_CONTROL_VOLUMES = input.z.length;
			variables.totalDepth = input.z[variables.NUM_CONTROL_VOLUMES -1];
			variables.evapotranspirations = new double [variables.NUM_CONTROL_VOLUMES -1];
			variables.fluxRefs = new double [variables.NUM_CONTROL_VOLUMES -1];
			
		
			SplittedETsFactory representativeETsFactory= new SplittedETsFactory();
			computedFluxs = representativeETsFactory.createEvapoTranspirations(input.representativeETsModel);
			
			if (input.etaRef == 0.0) {variables.zRef = 0;}
			else
				variables.zRef = variables.totalDepth + input.etaRef;
			
		}	
		
		
		variables.evapotranspirations = computedFluxs.computeStressedETs(Gn,input.evapotranspiration,variables.zRef);
	
		evapotranspirations = variables.evapotranspirations;
		
	
		//System.out.println("\n\nEvaporations = "+Arrays.toString(evaporations));
		//System.out.println("\n\nTranspirations = "+Arrays.toString(transpirations));
		//System.out.println("\n\nStressedETs = "+Arrays.toString(StressedETs));
		//System.out.println("z = "+Arrays.toString(z));
		//System.out.println("\n\nStressedET  = "+ StressedET);
		
		System.out.print("\nEnd ETsBrokerSimpleSolverMain");
		variables.step=step;
		step++;
	}
}
