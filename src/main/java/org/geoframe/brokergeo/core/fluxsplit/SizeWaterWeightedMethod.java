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
package org.geoframe.brokergeo.core.fluxsplit;

import static java.lang.Math.pow;

import org.geoframe.brokergeo.core.state.CurrentStepInput;
import org.geoframe.brokergeo.core.state.ProblemQuantities;

/**
 * Computation of the Transpirations and Evapotranspirations by using stress
 * factor size wighted metohd
 * 
 * @author Concetta D'Amato
 */
public class SizeWaterWeightedMethod extends SplitETs {

	public SizeWaterWeightedMethod(ProblemQuantities variables, CurrentStepInput input) {
		super(variables, input);
	}

	public double[] computeStressedETs(double[] Gn, double fluxRef, double zRef) {

		// variables = ProblemQuantities.getInstance();
		// input = CurrentStepInput.getInstance();
		variables.control = 0;
		variables.etaRef = 0;
		variables.etaRef = Math.floor((variables.totalDepth - zRef) * 100) / 100;

		for (int i = 0; i <= variables.NUM_CONTROL_VOLUMES - 2; i++) {

			if (Gn[0] == 0) {
				variables.fluxRefs[i] = 0;
			}
			if (variables.etaRef == 0) {
				variables.fluxRefs[i] = 0;
			}

			else {
				if (input.z[i] >= zRef) {
					variables.fluxRefs[i] = ((fluxRef * (input.g[i] * input.deltaZ[i])) / (variables.etaRef)) / Gn[0];
				} else {
					variables.fluxRefs[i] = 0;
				}
			}

			variables.control = variables.control + variables.fluxRefs[i];
		}
		// if (variables.control == fluxRef) { System.out.println("\n\nControllo su
		// fluxs Size corretto");}
		// if (variables.control<fluxRef + 1 * pow(10,-8) || variables.control > fluxRef
		// - 1 * pow(10,-8)) { System.out.println("\n\nControllo su fluxs Size
		// corretto");}
		if ((variables.control >= fluxRef - 1 * pow(10, -8)) && (variables.control <= fluxRef + 1 * pow(10, -8))) {
			System.out.println("\n\nControllo su fluxs Size corretto\"");
		} else {
			System.out.println("\n\nERROR in splitting ET.\nSimulation ended");
		}

		return variables.fluxRefs.clone();
	}

}
