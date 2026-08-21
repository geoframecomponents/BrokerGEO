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

public class ETsBrokerOneGenericFluxSolverMain extends HMModel {

	@Description("It is needed to iterate on the date")
	int step;

	@Description("The stressed Evapotranspiration for each control volume")
	@Out
	@Unit("mm/s")
	public double[] StressedETs;

	@In
	public boolean useWaterStress = true;

	@Description("The generic flux from each control volume can be evaluated in different way")
	@In
	public FluxSplitMethod representativeModel;

	@Description("ArrayList of variable to be stored in the buffer writer")
	@Out
	public ArrayList<double[]> outputToBuffer;

	@Description("Object dealing with a generic flux from each control volume of the domain")
	private SplitETs computedFluxs;
	public ProblemQuantities variables;
	public CurrentStepInput input;

	@Execute
	public void solve() {
		checkNull(variables, input);

		if (step == 0) {
			input.representativeModel = representativeModel;
			variables.NUM_CONTROL_VOLUMES = input.z.length;
			variables.totalDepth = input.z[variables.NUM_CONTROL_VOLUMES - 1];
			variables.StressedETs = new double[variables.NUM_CONTROL_VOLUMES - 1];
			variables.fluxRefs = new double[variables.NUM_CONTROL_VOLUMES - 1];

			computedFluxs = input.representativeModel.newInstance();

			outputToBuffer = new ArrayList<double[]>();

		}

		variables.zRef = variables.totalDepth + input.etaRef;

		if (!useWaterStress && input.representativeModel.isWaterWeighted()) {
			pm.errorMessage(
					"WARNING: the flux is split according the water stress factor, but evapotranspiration is not water stressed");
		}

		outputToBuffer.clear();

		variables.StressedETs = computedFluxs.computeStressedETs(variables, input, input.Gn, input.flux,
				variables.zRef);

		StressedETs = variables.StressedETs;

		outputToBuffer.add(variables.StressedETs);
		outputToBuffer.add(input.rootDensity);
		outputToBuffer.add(variables.StressedETs);
		outputToBuffer.add(variables.StressedETs);

		step++;
		variables.step = step;

	}
}
