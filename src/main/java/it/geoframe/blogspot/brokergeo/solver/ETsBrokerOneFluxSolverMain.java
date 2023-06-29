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
package it.geoframe.blogspot.brokergeo.solver;
import it.geoframe.blogspot.brokergeo.methods.*;
import java.util.ArrayList;
import it.geoframe.blogspot.brokergeo.data.*;
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


public class ETsBrokerOneFluxSolverMain {
	

	
	@Description("It is needed to iterate on the date")
	int step;
	
	@Description("The stressed Evapotranspiration for each control volume")
	@Out
	@Unit("mm/s")
	public double[] StressedETs;
	
	@In
	public boolean useWaterStress = true;
	
	@Description("Transpiration from each control volume can be evaluated in different way"
			+ " AverageWaterWeightedMethod, AverageWeightedMethod"
		    + " SizeWaterWeightedMetod, SizeWeightedMethod"
			+ " RootWaterWeightedMethod, RootWeightedMethod")
	@In
	public String representativeTsModel;
	
	@Description("ArrayList of variable to be stored in the buffer writer")
	@Out
	public ArrayList<double[]> outputToBuffer;
	
	@In
	public boolean  doProcess7;
	@Out
	public boolean  doProcess8;
	
	/////////////////////////////////////////////////////////////////////////////
	

	@Description("Object dealing with transpiration from each control volume of the domain")
	SplittedETs computedTs;
	
	
	private ProblemQuantities variables;
	private InputData input;

	@Execute
	public void solve() {
		System.out.print("\n\nStart ETsBrokerSolverMain");
		
		variables = ProblemQuantities.getInstance();
		input = InputData.getInstance();
		
		if(step==0){
			input.representativeTsModel = representativeTsModel;
			variables.NUM_CONTROL_VOLUMES = input.z.length;
			variables.totalDepth = input.z[variables.NUM_CONTROL_VOLUMES -1];
			variables.StressedETs = new double [variables.NUM_CONTROL_VOLUMES -1];
			variables.fluxRefs = new double [variables.NUM_CONTROL_VOLUMES -1];
		
			SplittedETsFactory representativeETsFactory= new SplittedETsFactory();
			
			computedTs = representativeETsFactory.createEvapoTranspirations(input.representativeTsModel);
			
			
			outputToBuffer = new ArrayList<double[]>();
			
		}
		
		variables.zR = variables.totalDepth + input.etaR;
		
		if (input.representativeTsModel.equalsIgnoreCase("AverageWaterWeightedMethod")  && useWaterStress == false) {
			System.out.print("\nWARNING: the flux is splitted according the water stress factor, but evapotranspiration is not water stressed");}
		if (input.representativeTsModel.equalsIgnoreCase("SizeWaterWeightedMethod")  && useWaterStress == false) {
			System.out.print("\nWARNING: the flux is splitted according the water stress factor, but evapotranspiration is not water stressed");}
		if (input.representativeTsModel.equalsIgnoreCase("RootWaterWeightedMethod")  && useWaterStress == false) {
			System.out.print("\nWARNING: the flux is splitted according the water stress factor, but evapotranspiration is not water stressed");}
			
		outputToBuffer.clear();
		
		variables.StressedETs = computedTs.computeStressedETs(input.GnT,input.transpiration,variables.zR);

		StressedETs = variables.StressedETs;
		outputToBuffer.add(variables.StressedETs);
		//System.out.println("\n\nEvaporations = "+Arrays.toString(evaporations));
		//System.out.println("\n\nTranspirations = "+Arrays.toString(transpirations));
		//System.out.println("\n\nStressedETs = "+Arrays.toString(StressedETs));
		System.out.print("\nEnd ETsBrokerSolverMain");
		
		step++;
		variables.step=step;
		
	}
}
