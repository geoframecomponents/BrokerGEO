package org.geoframe.brokergeo.tools;

import java.util.LinkedHashMap;
import java.util.Map;

import org.geoframe.brokergeo.core.fluxsplit.FluxSplitMethod;
import org.geoframe.brokergeo.io.BrokerGeoInputsHandler;

/**
 * Generator for the {@code BrokerGEOTwoFluxes.gpkg} fixture.
 */
public class BuildBrokerGEOTwoFluxesFixture {

	public static void main(String[] args) throws Exception {
		String outPath = "src/test/resources/Input/gpkg/BrokerGEOTwoFluxes.gpkg";

		double[] z = { 0.050000000000000044, 0.15000000000000002, 0.25, 0.3500000000000001, 0.44999999999999996,
				0.55, 0.65, 0.75, 0.85, 0.95, 1.0 };
		double[] deltaZ = { 0.050000000000000044, 0.09999999999999998, 0.09999999999999998, 0.10000000000000009,
				0.09999999999999987, 0.10000000000000003, 0.10000000000000003, 0.09999999999999998,
				0.09999999999999998, 0.10000000000000002, 0.05 };

		double[] rootDensity = { 0.3, 0.4, 0.6, 0.7, 0.8, 1.0, 1.0, 1.0, 1.0, 1.0 };
		double[] g = { 0.49853778058804915, 0.49853778058804915, 0.7751738855516915, 0.9332516598166297,
				0.8542127726841605, 1.0, 1.0, 1.0, 1.0, 1.0 };

		Map<String, Object> parameters = new LinkedHashMap<>();
		parameters.put(BrokerGeoInputsHandler.PARAM_ETA_R, -0.8);
		parameters.put(BrokerGeoInputsHandler.PARAM_ETA_E, -0.8);
		parameters.put(BrokerGeoInputsHandler.PARAM_TRANSPIRATION, 65.0);
		parameters.put(BrokerGeoInputsHandler.PARAM_EVAPORATION, 10.0);
		parameters.put(BrokerGeoInputsHandler.PARAM_USE_WATER_STRESS, 0); // false, when you use simple methods
		parameters.put(BrokerGeoInputsHandler.PARAM_REPRESENTATIVE_TS_MODEL, FluxSplitMethod.ROOT_WEIGHTED.name());
		parameters.put(BrokerGeoInputsHandler.PARAM_REPRESENTATIVE_ES_MODEL, FluxSplitMethod.AVERAGE_WEIGHTED.name());
		parameters.put(BrokerGeoInputsHandler.PARAM_GNT_G, 0.9453297897565602);
		parameters.put(BrokerGeoInputsHandler.PARAM_GNT_N, 8.0);
		parameters.put(BrokerGeoInputsHandler.PARAM_GNE_G, 0.9453297897565602);
		parameters.put(BrokerGeoInputsHandler.PARAM_GNE_N, 8.0);

		GpkgFixtureBuilder.build(outPath, parameters, z, deltaZ, rootDensity, g);
		System.out.println("Wrote " + outPath);
	}
}
