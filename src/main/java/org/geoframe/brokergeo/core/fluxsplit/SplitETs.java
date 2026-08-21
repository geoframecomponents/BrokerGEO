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

import org.geoframe.brokergeo.core.state.CurrentStepInput;
import org.geoframe.brokergeo.core.state.ProblemQuantities;

/**
 * The stressedETs abstract class.
 *
 * @author Concetta D'Amato
 */

public abstract class SplitETs {

	/**
	 * This method compute the evaporation and transpiration in each control volumes
	 * of the whole column of soil given the total evaporation and transpiration
	 * from the Evapotranspiration Component
	 *
	 * @param variables, @param input, @param Gn, @param fluxRef, @param zRef
	 *
	 * @return fluxRefs
	 */
	public abstract double[] computeStressedETs(ProblemQuantities variables, CurrentStepInput input, double[] Gn,
			double fluxRef, double zRef);

}
