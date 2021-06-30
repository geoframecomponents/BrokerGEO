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
 * A simple design factory for creating a StressedETs objects
 * @author Concetta D'Amato
 */

public class FactoryETs {
	/**
	 * Creates a new StressedETs object.
	 * @param type name of the Evaporation or Transpiration splitting model
	 * @param z 
	 * @param zR depth of the root
	 * @param dx vector containing the length of each control volume
	 * @param NUM_CONTROL_VOLUMES number of control volume for domain discetrization
	 * @return stressFactor G
	 */

	public SplittedETs createEvapoTranspirations (String type, double[] z, double[] deltaZ, int NUM_CONTROL_VOLUMES, double totalDepth) 
	{
		SplittedETs splitETs = null;
		if(type.equalsIgnoreCase("AverageMetod") || type.equalsIgnoreCase("AverageMetod")){
			splitETs = new AverageETs(z,deltaZ, NUM_CONTROL_VOLUMES, totalDepth);}
		else if(type.equalsIgnoreCase("SizeWightedMetod") || type.equalsIgnoreCase("SizeWightedMetod")){
			splitETs = new SizeWightedETs(z, deltaZ, NUM_CONTROL_VOLUMES, totalDepth);}
		else if(type.equalsIgnoreCase("RootWightedMetod") || type.equalsIgnoreCase("RootWightedMetod")){
			splitETs = new RootWightedTs(z, deltaZ, NUM_CONTROL_VOLUMES, totalDepth);}
		return splitETs;
	}	
}





