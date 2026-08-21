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
 * A simple design factory for creating a StressedETs objects
 * 
 * @author Concetta D'Amato
 */
public class SplitETsFactory {
	/**
	 * Creates a new StressedETs object.
	 * 
	 * @param type                name of the Evaporation or Transpiration splitting
	 * @param variables           the ProblemQuantities object
	 * @param input               the CurrentStepInput object
	 */
	public static SplitETs createEvapoTranspirations(String type, ProblemQuantities variables, CurrentStepInput input) {

		SplitETs splitETs = null;
		if (type.equalsIgnoreCase("AverageWaterWeightedMethod")) {
			splitETs = new AverageWaterWeightedMethod(variables, input);
		} else if (type.equalsIgnoreCase("SizeWaterWeightedMethod")) {
			splitETs = new SizeWaterWeightedMethod(variables, input);
		} else if (type.equalsIgnoreCase("RootWaterWeightedMethod")) {
			splitETs = new RootWaterWeightedMethod(variables, input);
		} else if (type.equalsIgnoreCase("AverageWeightedMethod")) {
			splitETs = new AverageWeightedMethod(variables, input);
		} else if (type.equalsIgnoreCase("SizeWeightedMethod")) {
			splitETs = new SizeWeightedMethod(variables, input);
		} else if (type.equalsIgnoreCase("RootWeightedMethod")) {
			splitETs = new RootWeightedMethod(variables, input);
		} else {
			throw new IllegalArgumentException("Invalid type: " + type);
		}

		return splitETs;
	}
}
