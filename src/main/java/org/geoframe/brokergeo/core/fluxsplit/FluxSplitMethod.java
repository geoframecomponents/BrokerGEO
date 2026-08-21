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

import java.util.function.Supplier;

/**
 * The available strategies for splitting a flux (evaporation, transpiration, ...)
 * across control volumes, and how to instantiate the {@link SplitETs} implementing
 * each one.
 *
 * @author Concetta D'Amato
 */
public enum FluxSplitMethod {

	AVERAGE_WEIGHTED(AverageWeightedMethod::new, false),
	AVERAGE_WATER_WEIGHTED(AverageWaterWeightedMethod::new, true),
	SIZE_WEIGHTED(SizeWeightedMethod::new, false),
	SIZE_WATER_WEIGHTED(SizeWaterWeightedMethod::new, true),
	ROOT_WEIGHTED(RootWeightedMethod::new, false),
	ROOT_WATER_WEIGHTED(RootWaterWeightedMethod::new, true);

	private final Supplier<SplitETs> factory;
	private final boolean waterWeighted;

	FluxSplitMethod( Supplier<SplitETs> factory, boolean waterWeighted ) {
		this.factory = factory;
		this.waterWeighted = waterWeighted;
	}

	public SplitETs newInstance() {
		return factory.get();
	}

	/** Whether this method splits the flux according to the water stress factor. */
	public boolean isWaterWeighted() {
		return waterWeighted;
	}
}
