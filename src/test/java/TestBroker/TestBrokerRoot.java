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
package TestBroker;
//import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;
import BrokerSolver.*;
import it.geoframe.blogpsot.netcdf.monodimensionalproblemtimedependent.ReadNetCDFRichardsLysimeterGrid1D;
/**
 * Test the Broker module.
 * @author Concetta D'Amato
 */

public class TestBrokerRoot {

	@Test
	public void Test() throws Exception {

		String pathGrid =  "resources\\Input\\Grid_NetCDF\\GridLysRoot.nc";
		ReadNetCDFRichardsLysimeterGrid1D readNetCDF = new ReadNetCDFRichardsLysimeterGrid1D();		
		double[] z= {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
		double[] theta = {0.21, 0.21, 0.28, 0.32, 0.3, 0.40, 0.40, 0.40, 0.40, 0.40};// theta vector values from the bottom to the top
		double[] deltaZ = {0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1};
		double evaporation = 10;
		double transpiration = 65;
		
		
		StressFactorBroker StressFactorBrokerSolver = new StressFactorBroker();
		
		ETsBroker ETsBrokerSolver = new ETsBroker();  
		
		readNetCDF.richardsGridFilename = pathGrid;
		
		readNetCDF.read();

		StressFactorBrokerSolver.thetaWp = readNetCDF.thetaWP;
		StressFactorBrokerSolver.thetaFc = readNetCDF.thetaFC;
		StressFactorBrokerSolver.ID = readNetCDF.parameterID;
		StressFactorBrokerSolver.z  = z; 
		StressFactorBrokerSolver.etaR = -1.0;
		StressFactorBrokerSolver.etaE = -0.8;
		StressFactorBrokerSolver.theta = theta;
		StressFactorBrokerSolver.deltaZ = deltaZ;
		StressFactorBrokerSolver.stressFactorModel = "LinearStressFactor";
		StressFactorBrokerSolver.representativeStressFactorModel = "AverageMetod"; //SizeWightedMetod, AverageMetod
		
		
		ETsBrokerSolver.z = z;
		ETsBrokerSolver.rootIC = readNetCDF.rootIC; 
		ETsBrokerSolver.etaR = -1.0;
		ETsBrokerSolver.etaE = -0.8;
		ETsBrokerSolver.deltaZ = deltaZ;
		ETsBrokerSolver.transpiration = transpiration;
		ETsBrokerSolver.evaporation = evaporation;
		ETsBrokerSolver.representativeEsModel = "AverageMetod"; 	//SizeWightedMetod, AverageMetod
		ETsBrokerSolver.representativeTsModel = "RootWightedMetod"; //SizeWightedMetod, AverageMetod, RootWightedMetod
		
		
		StressFactorBrokerSolver.solve();
		
		ETsBrokerSolver.g = StressFactorBrokerSolver.g;
		ETsBrokerSolver.GnT = StressFactorBrokerSolver.GnT;
		ETsBrokerSolver.GnE = StressFactorBrokerSolver.GnE;
		
		ETsBrokerSolver.solve();
	}
}

