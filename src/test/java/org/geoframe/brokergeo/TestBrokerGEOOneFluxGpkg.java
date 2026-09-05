/*
 * GNU GPL v3 License
 *
 * Copyright 2016 Marialaura Bancheri
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
package org.geoframe.brokergeo;

import org.geoframe.brokergeo.core.fluxsplit.FluxSplitMethod;
import org.geoframe.brokergeo.core.state.CurrentStepInput;
import org.geoframe.brokergeo.core.state.ProblemQuantities;
import org.geoframe.brokergeo.io.BrokerGeoInputsHandler;
import org.geoframe.brokergeo.io.BrokerGeoOutputsHandler;
import org.geoframe.brokergeo.solvers.ETsBrokerOneFluxSolverMain;

/**
 * Same scenario as {@link TestBrokerGEOOneFlux}, but reading every input from
 * a gpkg (via {@link BrokerGeoInputsHandler}) instead of Java
 * literals, and writing the result through {@link BrokerGeoOutputsHandler}.
 *
 * @author Concetta D'Amato
 * @author Andrea Antonello
 */
public class TestBrokerGEOOneFluxGpkg extends BrokerGeoTestCase {

	public void testOneFlux() throws Exception {

		String inputGpkg = getRes("/Input/gpkg/BrokerGEOOneFlux.gpkg");
		String outputGpkg = getTmpPath("BrokerGEOOneFluxOutput", ".gpkg");

		CurrentStepInput inputData = new CurrentStepInput();
		ProblemQuantities problemQuantities = new ProblemQuantities();

		ETsBrokerOneFluxSolverMain etsBrokerSolver = new ETsBrokerOneFluxSolverMain();
		etsBrokerSolver.input = inputData;
		etsBrokerSolver.variables = problemQuantities;

		try (BrokerGeoInputsHandler inputsHandler = new BrokerGeoInputsHandler(inputGpkg)) {
			inputsHandler.read();

			inputData.z = inputsHandler.z;
			inputData.deltaZ = inputsHandler.deltaZ;
			inputData.rootDensity = inputsHandler.rootDensity;
			inputData.g = inputsHandler.g;
			inputData.etaR = inputsHandler.getParameterDouble(BrokerGeoInputsHandler.PARAM_ETA_R);
			inputData.transpiration = inputsHandler.getParameterDouble(BrokerGeoInputsHandler.PARAM_TRANSPIRATION);
			inputData.GnT = new double[] { inputsHandler.getParameterDouble(BrokerGeoInputsHandler.PARAM_GNT_G),
					inputsHandler.getParameterDouble(BrokerGeoInputsHandler.PARAM_GNT_N) };

			etsBrokerSolver.representativeTsModel = FluxSplitMethod
					.valueOf(inputsHandler.getParameterString(BrokerGeoInputsHandler.PARAM_REPRESENTATIVE_TS_MODEL));
			etsBrokerSolver.useWaterStress = inputsHandler.getParameterInt(BrokerGeoInputsHandler.PARAM_USE_WATER_STRESS) != 0;

			etsBrokerSolver.solve();

			try (BrokerGeoOutputsHandler outputsHandler = new BrokerGeoOutputsHandler(outputGpkg)) {
				outputsHandler.stressedETs = etsBrokerSolver.stressedETs;
				outputsHandler.parameters = inputsHandler.getParameters();
				outputsHandler.write();
			}
		}

		assertGoldenArray("StressedETs", etsBrokerSolver.stressedETs);
	}
}
