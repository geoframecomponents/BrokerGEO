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

import static java.lang.Math.pow;

import it.geoframe.blogspot.brokergeo.data.InputData;
import it.geoframe.blogspot.brokergeo.data.ProblemQuantities;

/**
 * Computation of the Transpiration from each control volumes as function of root distribution
 * 
 * @author Concetta D'Amato
 */


public class RootWaterWeightedMethod extends SplittedETs {

	private ProblemQuantities variables;
	private InputData input;
	
	/** General constructor used to pass the value of variables */
	//public RootWaterWeightedMethod (double[] z, double[] deltaZ, int NUM_CONTROL_VOLUMES, double totalDepth) {
		//super(z, deltaZ, NUM_CONTROL_VOLUMES, totalDepth);}


	
	public double [] computeStressedETs (double[] Gn,double fluxRef, double zRef) {
		
		variables = ProblemQuantities.getInstance();
		input = InputData.getInstance();
		variables.control=0;
		
		if(variables.step==0){
		for (int i = 0; i <= input.rootIC.length-2; i++) {
			variables.sumRootIC = variables.sumRootIC + input.rootIC[i];
			variables.sumRootWaterStress = variables.sumRootWaterStress + (input.rootIC[i]*input.g[i]);	
		}}
			
		for (int i = 0; i <= variables.NUM_CONTROL_VOLUMES-2; i++) {
			
			if (Gn[0]== 0 ) {variables.fluxRefs[i] = 0;}
			else {		
				if (input.z[i] > zRef) {					
					variables.fluxRefs[i]=(fluxRef*((input.rootIC[i]*input.g[i])/variables.sumRootWaterStress));}
				else{variables.fluxRefs[i] = 0;}}
			variables.control = variables.control + variables.fluxRefs[i];
			
		}
		
		if (variables.control == fluxRef) { System.out.println("\n\nControllo su fluxs Root corretto");}
		if (variables.control<fluxRef + 1 * pow(10,-8) || variables.control > fluxRef - 1 * pow(10,-8)) { System.out.println("\n\nControllo su fluxs Root corretto");}
		
		return variables.fluxRefs.clone();	
	}



	/*public double [] computeETs() {
		return null;
	}*/

}
 