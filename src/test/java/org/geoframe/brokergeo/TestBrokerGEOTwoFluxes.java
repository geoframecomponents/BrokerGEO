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
import org.geoframe.brokergeo.solver.*;
import org.geoframe.brokergeo.data.*;
import org.hortonmachine.gears.io.geoframe.ReadNetCDFRichardsLysimeterGrid1D;
/**
 * Test the Broker module.
 * @author Concetta D'Amato
 */

public class TestBrokerGEOTwoFluxes extends BrokerGeoTestCase {

	public void testTwoFluxes() throws Exception {

		String pathGrid = getRes("/Input/Grid_NetCDF/GridLysRoot.nc");
		ReadNetCDFRichardsLysimeterGrid1D readNetCDF = new ReadNetCDFRichardsLysimeterGrid1D();
		
		double evaporation = 10;
		double transpiration = 65;
		double[] g = {0.49853778058804915, 0.49853778058804915, 0.7751738855516915, 0.9332516598166297, 0.8542127726841605, 1.0, 1.0, 1.0, 1.0, 1.0};
		double[] GnT = {0.9453297897565602,8};
		double[] GnE = {0.9453297897565602,8};
		double [] rootDensity= {0.3, 0.4, 0.6, 0.7, 0.8, 1.0, 1.0, 1.0, 1.0, 1.0};
		
		ETsBrokerTwoFluxesSolverMain ETsBrokerSolver = new ETsBrokerTwoFluxesSolverMain();  
		InputDataMain Input = new InputDataMain();
		
		readNetCDF.richardsGridFilename = pathGrid;
		
		readNetCDF.read();

		Input.z = readNetCDF.z;
		Input.etaR = -0.8;
		Input.etaE = -0.8;
		Input.deltaZ = readNetCDF.spaceDelta;
		Input.transpiration = transpiration;
		Input.evaporation = evaporation;
		ETsBrokerSolver.representativeEsModel = "AverageWeightedMethod"; //SizeWaterWeightedMethod, AverageWaterWeightedMethod //AverageWeightedMethod, SizeWeightedMetod
		ETsBrokerSolver.representativeTsModel = "RootWeightedMethod"; //SizeWaterWeightedMethod, AverageWaterWeightedMethod, RootWaterWeightedMethod //AverageWeightedMethod, SizeWeightedMetod, RootWeightedMethod
		Input.rootDensity = rootDensity;
		ETsBrokerSolver.useWaterStress = false; //false, when you use simple methods
		Input.g = g;
		
		Input.GnT = GnT;
		Input.GnE = GnE;

		Input.process();
		ETsBrokerSolver.solve();
	}
}

