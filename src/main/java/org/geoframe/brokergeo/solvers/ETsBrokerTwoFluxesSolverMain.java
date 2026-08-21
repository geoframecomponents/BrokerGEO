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
package org.geoframe.brokergeo.solvers;

import org.geoframe.brokergeo.core.fluxsplit.*;
import org.hortonmachine.gears.libs.modules.HMModel;

import java.util.ArrayList;
import java.util.Arrays;

import org.geoframe.brokergeo.core.state.*;
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
public class ETsBrokerTwoFluxesSolverMain extends HMModel {

	@Description("It is needed to iterate on the date")
	int step;

	@Description("The stressed Evapotranspiration for each control volume")
	@Out
	@Unit("mm/s")
	public double[] StressedETs;

	@Description("The stressed Transpiration for each control volume")
	@Out
	@Unit("mm/s")
	public double[] transpirations;

	@Description("The stressed Evaporation for each control volume within the Evaporation layer")
	@Out
	@Unit("mm/s")
	public double[] evaporations;

	@In
	public boolean useWaterStress = true;

	@Description("Evaporation from each control volume can be evaluated in different way")
	@In
	public FluxSplitMethod representativeEsModel;

	@Description("Transpiration from each control volume can be evaluated in different way")
	@In
	public FluxSplitMethod representativeTsModel;

	@Description("ArrayList of variable to be stored in the buffer writer")
	@Out
	public ArrayList<double[]> outputToBuffer;

	@Description("Object dealing with transpiration from each control volume of the domain")
	private SplitETs computedTs;

	@Description("Object dealing with evaporation from each control volume of the domain")
	private SplitETs computedEs;

	public ProblemQuantities variables;
	public CurrentStepInput input;

	@Execute
	public void solve() {
		checkNull(variables, input);

		if (step == 0) {
			input.representativeEsModel = representativeEsModel;
			input.representativeTsModel = representativeTsModel;
			variables.NUM_CONTROL_VOLUMES = input.z.length;
			variables.totalDepth = input.z[variables.NUM_CONTROL_VOLUMES - 1];
			variables.StressedETs = new double[variables.NUM_CONTROL_VOLUMES - 1];
			variables.fluxRefs = new double[variables.NUM_CONTROL_VOLUMES - 1];

			computedEs = input.representativeEsModel.newInstance();

			computedTs = input.representativeTsModel.newInstance();

			variables.zE = variables.totalDepth + input.etaE;

			outputToBuffer = new ArrayList<double[]>();

		}

		variables.zR = variables.totalDepth + input.etaR;

		if (!useWaterStress) {
			if (input.representativeTsModel.isWaterWeighted()) {
				pm.errorMessage(
						"WARNING: the flux is splitted according the water stress factor, but transpiration is not water stressed");
			}
			if (input.representativeEsModel.isWaterWeighted()) {
				pm.errorMessage(
						"WARNING: the flux is splitted according the water stress factor, but evaporation is not water stressed");
			}
		}

		outputToBuffer.clear();

		variables.transpirations = computedTs.computeStressedETs(variables, input, input.GnT, input.transpiration,
				variables.zR);
		variables.evaporations = computedEs.computeStressedETs(variables, input, input.GnE, input.evaporation,
				variables.zE);

		for (int i = 0; i <= variables.transpirations.length - 1; i = i + 1) {
			variables.StressedETs[i] = variables.evaporations[i] + variables.transpirations[i];
		}

		StressedETs = variables.StressedETs;
		transpirations = variables.transpirations;
		evaporations = variables.evaporations;

		outputToBuffer.add(variables.StressedETs);
		outputToBuffer.add(input.rootDensity);
		outputToBuffer.add(variables.transpirations);
		outputToBuffer.add(variables.evaporations);

		step++;
		variables.step = step;

	}
}
