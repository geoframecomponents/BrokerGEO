package org.geoframe.brokergeo.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hortonmachine.dbs.compat.ADb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.dbs.compat.IHMResultSet;
import org.hortonmachine.dbs.utils.SqlName;

/**
 * Reads a BrokerGEO test's input GeoPackage: one {@code parameters} row
 * (scalar config - root/evaporation depth, water-stress toggle, which
 * {@link org.geoframe.brokergeo.core.fluxsplit.FluxSplitMethod} to use, the
 * scalar flux(es) to split, the {@code GnT}/{@code GnE} pairs) and one
 * {@code grid} table (one row per control volume: {@code z}, {@code deltaZ},
 * and the optional per-cell {@code rootDensity}/{@code g}/{@code gE}
 * arrays) - the same {@code ADb}/{@code SqlName}-based convention as
 * WHETGEO-1D's {@code Whetgeo1DInputsHandler} and GEOET's
 * {@code GeoetInputsHandler}.
 *
 * <p>
 * BrokerGEO's solvers are per-instant functions (no time loop of their own -
 * they're called once per timestep by an upstream driver), so unlike its
 * siblings this handler has no {@code timeseries_*} tables: one gpkg holds
 * one snapshot of inputs.
 *
 * <p>
 * {@code z}/{@code deltaZ} may have one more row than
 * {@code rootDensity}/{@code g}/{@code gE} (the solvers only ever loop cell
 * indices {@code 0..z.length-2}, but need {@code z[z.length-1]} for the
 * domain's total depth) - the trailing row(s) simply leave those columns
 * {@code NULL}.
 */
public class BrokerGeoInputsHandler implements AutoCloseable {

	public static final String TABLE_PARAMETERS = "parameters";
	public static final String TABLE_GRID = "grid";
	public static final String COL_ID = "id";

	public static final String PARAM_ETA_R = "etaR";
	public static final String PARAM_ETA_E = "etaE";
	public static final String PARAM_USE_WATER_STRESS = "useWaterStress";
	public static final String PARAM_REPRESENTATIVE_TS_MODEL = "representativeTsModel";
	public static final String PARAM_REPRESENTATIVE_ES_MODEL = "representativeEsModel";
	public static final String PARAM_TRANSPIRATION = "transpiration";
	public static final String PARAM_EVAPORATION = "evaporation";
	public static final String PARAM_GNT_G = "gnTG";
	public static final String PARAM_GNT_N = "gnTN";
	public static final String PARAM_GNE_G = "gnEG";
	public static final String PARAM_GNE_N = "gnEN";

	public static final String COL_Z = "z";
	public static final String COL_DELTA_Z = "deltaZ";
	public static final String COL_ROOT_DENSITY = "rootDensity";
	public static final String COL_G = "g";
	public static final String COL_GE = "gE";

	private final ADb db;
	private final boolean ownsDb;
	private final Map<String, Object> parameters = new HashMap<>();

	public double[] z;
	public double[] deltaZ;
	public double[] rootDensity;
	public double[] g;
	public double[] gE;

	public BrokerGeoInputsHandler(ADb db) {
		this.db = db;
		this.ownsDb = false;
	}

	public BrokerGeoInputsHandler(String gpkgPath) throws Exception {
		this.db = EDb.GEOPACKAGE.getDb();
		this.db.open(gpkgPath);
		this.ownsDb = true;
	}

	/** Closes the underlying connection, but only if this handler opened it itself. */
	@Override
	public void close() throws Exception {
		if (ownsDb) {
			db.close();
		}
	}

	/** Reads both the {@code parameters} row and the {@code grid} table. */
	public void read() throws Exception {
		readParameters();
		readGrid();
	}

	private void readParameters() throws Exception {
		SqlName table = SqlName.m(TABLE_PARAMETERS);
		Set<String> cols = new HashSet<>();
		for (String[] col : db.getTableColumns(table)) {
			cols.add(col[0]);
		}
		cols.remove(COL_ID);

		db.execOnResultSet("SELECT * FROM " + table.fixedDoubleName + " LIMIT 1", rs -> {
			if (rs.next()) {
				for (String col : cols) {
					parameters.put(col, rs.getObject(columnIndex(rs, col)));
				}
			}
			return null;
		});
	}

	private void readGrid() throws Exception {
		SqlName table = SqlName.m(TABLE_GRID);
		Set<String> cols = new HashSet<>();
		for (String[] col : db.getTableColumns(table)) {
			cols.add(col[0]);
		}
		boolean hasRootDensity = cols.contains(COL_ROOT_DENSITY);
		boolean hasG = cols.contains(COL_G);
		boolean hasGE = cols.contains(COL_GE);

		List<Double> zList = new ArrayList<>();
		List<Double> dzList = new ArrayList<>();
		List<Double> rdList = new ArrayList<>();
		List<Double> gList = new ArrayList<>();
		List<Double> geList = new ArrayList<>();

		db.execOnResultSet("SELECT * FROM " + table.fixedDoubleName + " ORDER BY " + COL_ID, rs -> {
			int zIdx = columnIndex(rs, COL_Z);
			int dzIdx = columnIndex(rs, COL_DELTA_Z);
			Integer rdIdx = hasRootDensity ? columnIndex(rs, COL_ROOT_DENSITY) : null;
			Integer gIdx = hasG ? columnIndex(rs, COL_G) : null;
			Integer geIdx = hasGE ? columnIndex(rs, COL_GE) : null;
			while (rs.next()) {
				zList.add(rs.getDouble(zIdx));
				dzList.add(rs.getDouble(dzIdx));
				if (rdIdx != null) {
					Object v = rs.getObject(rdIdx);
					if (v != null)
						rdList.add(((Number) v).doubleValue());
				}
				if (gIdx != null) {
					Object v = rs.getObject(gIdx);
					if (v != null)
						gList.add(((Number) v).doubleValue());
				}
				if (geIdx != null) {
					Object v = rs.getObject(geIdx);
					if (v != null)
						geList.add(((Number) v).doubleValue());
				}
			}
			return null;
		});

		z = toArray(zList);
		deltaZ = toArray(dzList);
		rootDensity = rdList.isEmpty() ? null : toArray(rdList);
		g = gList.isEmpty() ? null : toArray(gList);
		gE = geList.isEmpty() ? null : toArray(geList);
	}

	private static double[] toArray(List<Double> list) {
		double[] arr = new double[list.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = list.get(i);
		}
		return arr;
	}

	private static int columnIndex(IHMResultSet rs, String colName) throws Exception {
		int count = rs.getMetaData().getColumnCount();
		for (int i = 1; i <= count; i++) {
			if (rs.getMetaData().getColumnName(i).equalsIgnoreCase(colName)) {
				return i;
			}
		}
		throw new IllegalArgumentException("Column not found: " + colName);
	}

	/** Returns a parameter as a double, throwing if absent. */
	public double getParameterDouble(String name) {
		return ((Number) requireParam(name)).doubleValue();
	}

	/** Returns a parameter as an int, throwing if absent. */
	public int getParameterInt(String name) {
		return ((Number) requireParam(name)).intValue();
	}

	/** Returns a parameter as a String, throwing if absent. */
	public String getParameterString(String name) {
		return String.valueOf(requireParam(name));
	}

	/** Whether the {@code parameters} row carries a value for this key. */
	public boolean hasParameter(String name) {
		return parameters.containsKey(name);
	}

	private Object requireParam(String name) {
		if (!parameters.containsKey(name)) {
			throw new IllegalArgumentException("Parameter not found in gpkg 'parameters' table: " + name);
		}
		return parameters.get(name);
	}

	/**
	 * The full set of parameters read from the {@code parameters} table, e.g. to
	 * snapshot them into an output gpkg via {@link BrokerGeoOutputsHandler#parameters}
	 * so the output file is self-contained even though the parameters themselves
	 * were originally read from a separate input gpkg.
	 */
	public Map<String, Object> getParameters() {
		return new HashMap<>(parameters);
	}
}
