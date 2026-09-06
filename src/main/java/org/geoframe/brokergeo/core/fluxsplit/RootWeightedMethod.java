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

import org.geoframe.brokergeo.core.state.BGCurrentStepInput;
import org.geoframe.brokergeo.core.state.BGProblemQuantities;

/**
 * Computation of the Transpiration from each control volumes as function of
 * root distribution
 * 
 * @author Concetta D'Amato
 */
public class RootWeightedMethod extends SplitETs {

	public double[] computeStressedETs(BGProblemQuantities variables, BGCurrentStepInput input, double[] Gn,
			double fluxRef, double zRef) {

		// variables = ProblemQuantities.getInstance();
		// input = CurrentStepInput.getInstance();
		variables.control = 0;
		variables.sumRootDensity = 0;

		for (int i = 0; i <= variables.NUM_CONTROL_VOLUMES - 2; i++) {
			if (input.z[i] >= zRef) {
				variables.sumRootDensity = variables.sumRootDensity + input.rootDensity[i];
			}
			// variables.sumRootWaterStress = variables.sumRootWaterStress +
			// (input.rootIC[i]*input.g[i]);
		}

		if (input.evapotranspiration != 0 && input.etaR == 0.0) {
			System.out.println("\n\nError: Please enter the root depth.\nSimulation ended");
			System.exit(0);
		}

		for (int i = 0; i <= variables.NUM_CONTROL_VOLUMES - 2; i++) {

			if (input.z[i] >= zRef) {
				variables.fluxRefs[i] = (fluxRef * (input.rootDensity[i] / variables.sumRootDensity));
			} else {
				variables.fluxRefs[i] = 0;
			}
			variables.control = variables.control + variables.fluxRefs[i];

		}

		// if (variables.control == fluxRef) { System.out.println("\n\nControllo su
		// fluxs Root corretto");}
		// if (variables.control < fluxRef + 1 * pow(10,-8) || variables.control >
		// fluxRef - 1 * pow(10,-8)) { System.out.println("\n\nControllo su fluxs Root
		// corretto");}
		if ((variables.control < fluxRef - 1 * pow(10, -8)) || (variables.control > fluxRef + 1 * pow(10, -8))) {
			throw new RuntimeException("RootWeightedMethod: ERROR in splitting ET.");
		}

		return variables.fluxRefs.clone();
	}
}
