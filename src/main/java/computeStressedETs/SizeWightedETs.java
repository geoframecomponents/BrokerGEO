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
 * Computation of the Transpirations and Evapotranspirations by using stress factor size wighted metohd 
 * @author Concetta D'Amato
 */
public class SizeWightedETs extends SplittedETs {

	/** General constructor used to pass the value of variables */
	public SizeWightedETs (double[] z, double[] deltaZ, int NUM_CONTROL_VOLUMES, double totalDepth) {
		super(z, deltaZ, NUM_CONTROL_VOLUMES, totalDepth);}


	
	public double [] computeStressedETs (double[]g,double[] Gn,double fluxRef, double zRef, double [] rootIC, double sumRootIC) {
		
		for (int i = 0; i <= NUM_CONTROL_VOLUMES-2; i++) {
			
			if (Gn[0]== 0 ) {fluxRefs[i] = 0;}
			else {		
				if (z[i] >= zRef) {					
					fluxRefs[i]=((fluxRef*(g[i]*deltaZ[i]))/(-etaR))/Gn[0];}
				else{fluxRefs[i] = 0;}}
		}
			
		return fluxRefs.clone();	
	}
}
