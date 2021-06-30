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
package computeStressedETs;


/**
 * Computation of Transpirations and Evapotranspirations from control volumes by using stress factor avarage method
 * 
 * @author Concetta D'Amato
 */

public class AverageETs extends SplittedETs{
	

	/** General constructor used to pass the value of variables */
	public AverageETs (double[] z, double[] deltaZ, int NUM_CONTROL_VOLUMES, double totalDepth) {
		super(z,deltaZ, NUM_CONTROL_VOLUMES, totalDepth);}

	
	
	public double [] computeStressedETs (double[]g,double[] Gn, double fluxRef, double zRef,double [] rootIC, double sumRootIC) {
		
			for (int i = 0; i <= NUM_CONTROL_VOLUMES-2; i++) {
				
				if (Gn[0] == 0) {fluxRefs[i] = 0;}
				else {
					if (z[i] >= zRef){
						fluxRefs[i]=(fluxRef*(g[i]/Gn[1]))/Gn[0];}
					else {fluxRefs[i] = 0;}}
			}	
				
			return fluxRefs.clone();			
	}
}