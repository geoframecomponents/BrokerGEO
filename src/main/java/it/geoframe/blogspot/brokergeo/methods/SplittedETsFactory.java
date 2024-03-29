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
package it.geoframe.blogspot.brokergeo.methods;
import it.geoframe.blogspot.brokergeo.data.InputData;
import it.geoframe.blogspot.brokergeo.data.ProblemQuantities;


/**
 * A simple design factory for creating a StressedETs objects
 * @author Concetta D'Amato
 */

public class SplittedETsFactory {
	/**
	 * Creates a new StressedETs object.
	 * @param type name of the Evaporation or Transpiration splitting model
	 * @param z 
	 * @param zR depth of the root
	 * @param dx vector containing the length of each control volume
	 * @param NUM_CONTROL_VOLUMES number of control volume for domain discetrization
	 * @return stressFactor G
	 */
	
	private ProblemQuantities variables;
	private InputData input;

	public SplittedETs createEvapoTranspirations (String type) 
	{
		this.input = InputData.getInstance();
		this.variables = ProblemQuantities.getInstance();
		
		SplittedETs splitETs = null;
		if(type.equalsIgnoreCase("AverageWaterWeightedMethod") || type.equalsIgnoreCase("AverageWaterWeightedMethod")){
			//splitETs = new AverageETs(input.z,input.deltaZ, variables.NUM_CONTROL_VOLUMES, variables.totalDepth);}
			splitETs = new AverageWaterWeightedMethod();}
		else if(type.equalsIgnoreCase("SizeWaterWeightedMethod") || type.equalsIgnoreCase("SizeWaterWeightedMethod")){
			//splitETs = new SizeWaterWeightedMethod(input.z, input.deltaZ, variables.NUM_CONTROL_VOLUMES, variables.totalDepth);}
		    splitETs = new SizeWaterWeightedMethod();}
		else if(type.equalsIgnoreCase("RootWaterWeightedMethod") || type.equalsIgnoreCase("RootWaterWeightedMethod")){
			//splitETs = new RootWaterWeightedMethod(input.z, input.deltaZ, variables.NUM_CONTROL_VOLUMES, variables.totalDepth);}
			splitETs = new RootWaterWeightedMethod();}
		
		else if(type.equalsIgnoreCase("AverageWeightedMethod") || type.equalsIgnoreCase("AverageWeightedMethod")){
			splitETs = new AverageWeightedMethod();}
		else if(type.equalsIgnoreCase("SizeWeightedMethod") || type.equalsIgnoreCase("SizeWeightedMethod")){
		    splitETs = new SizeWeightedMethod();}
		else if(type.equalsIgnoreCase("RootWeightedMethod") || type.equalsIgnoreCase("RootWeightedMethod")){
			splitETs = new RootWeightedMethod();}
		
		return splitETs;
	}	
}





