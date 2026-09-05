package org.geoframe.brokergeo.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hortonmachine.dbs.compat.ADb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.dbs.compat.IHMPreparedStatement;
import org.hortonmachine.dbs.utils.SqlName;

/**
 * DB-based output handler for BrokerGEO test results, modeled on
 * {@code org.geoframe.geoet.io.GeoetOutputsHandler} /
 * {@code Whetgeo1DOutputsHandler}. Unlike those two, BrokerGEO's solvers are
 * per-instant functions (called once per timestep by an upstream driver, no
 * time loop of their own), so this handler writes one snapshot - a row per
 * control volume - rather than buffering rows across steps: set the
 * per-control-volume arrays and call {@link #write()} once.
 *
 * <p>
 * Table name is prefixed {@value #PREFIX}, the same convention
 * {@code Whetgeo1DOutputsHandler}/{@code GeoetOutputsHandler} use.
 *
 * <p>
 * {@link #transpirations} and {@link #evaporations} are independently
 * optional: leaving either {@code null} omits its column - {@code
 * ETsBrokerOneFluxSolverMain} only produces {@link #stressedETs}, while
 * {@code ETsBrokerTwoFluxesSolverMain} produces all three.
 */
public class BrokerGeoOutputsHandler implements AutoCloseable {

	public static final String PREFIX = "geoframe_brokergeo";
	public static final String TABLE_OUTPUT_RESULTS = PREFIX + "_output_results";
	/**
	 * Optional table, one row, written once: a snapshot of the input parameters
	 * this run was configured with - see {@code BrokerGeoInputsHandler.getParameters()}.
	 * Written so the output file is self-contained even though the parameters
	 * themselves were originally read from a separate input gpkg. Columns are
	 * dynamic, one per parameter key actually set.
	 */
	public static final String TABLE_OUTPUT_PARAMETERS = PREFIX + "_output_parameters";

	public static final String COL_ID = "id";
	public static final String COL_Z = "z";
	public static final String COL_ROOT_DENSITY = "rootDensity";
	public static final String COL_G = "g";
	public static final String COL_GE = "gE";
	public static final String COL_STRESSED_ETS = "stressed_ets";
	public static final String COL_TRANSPIRATION = "transpiration";
	public static final String COL_EVAPORATION = "evaporation";

	/** Mandatory: the stressed ET for each control volume - every Broker solver produces this. */
	public double[] stressedETs;
	/** Optional: only {@code ETsBrokerTwoFluxesSolverMain} produces this. */
	public double[] transpirations;
	/** Optional: only {@code ETsBrokerTwoFluxesSolverMain} produces this. */
	public double[] evaporations;

	/**
	 * Optional per-cell input context (depth, root density, stress factors),
	 * denormalized into the results table so the output gpkg alone can be
	 * charted (e.g. stressedETs vs z, or vs rootDensity/g) without joining back
	 * to the input fixture.
	 */
	public double[] z;
	public double[] rootDensity;
	public double[] g;
	public double[] gE;

	/**
	 * Input parameter snapshot, written once if non-null and non-empty. Values
	 * must be {@link String}, {@link Integer} or a {@link Number} (stored as
	 * REAL) - the same convention {@code GpkgFixtureBuilder} uses for the input
	 * {@code parameters} table.
	 */
	public Map<String, Object> parameters;

	private final ADb db;

	public BrokerGeoOutputsHandler(String dbPath) throws Exception {
		// each test run produces a fresh output gpkg; a stale file from a previous
		// run would otherwise collide with this run's rows
		File existing = new File(dbPath);
		if (existing.exists()) {
			existing.delete();
		}
		this.db = EDb.GEOPACKAGE.getDb();
		this.db.open(dbPath);
	}

	/** Writes the current snapshot (and the parameter snapshot, if set). Call once. */
	public void write() throws Exception {
		writeResults();
		writeParametersIfPresent();
	}

	@Override
	public void close() throws Exception {
		db.close();
	}

	private static String placeholders(int n) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			if (i > 0)
				sb.append(", ");
			sb.append("?");
		}
		return sb.toString();
	}

	private void writeResults() throws Exception {
		boolean withZ = (z != null);
		boolean withRootDensity = (rootDensity != null);
		boolean withG = (g != null);
		boolean withGE = (gE != null);
		boolean withTranspiration = (transpirations != null);
		boolean withEvaporation = (evaporations != null);

		List<String> cols = new ArrayList<>(List.of(COL_ID));
		if (withZ)
			cols.add(COL_Z);
		if (withRootDensity)
			cols.add(COL_ROOT_DENSITY);
		if (withG)
			cols.add(COL_G);
		if (withGE)
			cols.add(COL_GE);
		cols.add(COL_STRESSED_ETS);
		if (withTranspiration)
			cols.add(COL_TRANSPIRATION);
		if (withEvaporation)
			cols.add(COL_EVAPORATION);

		SqlName resultsTable = SqlName.m(TABLE_OUTPUT_RESULTS);
		List<String> fieldDefs = new ArrayList<>();
		for (String c : cols) {
			fieldDefs.add(c + (c.equals(COL_ID) ? " INTEGER PRIMARY KEY" : " REAL"));
		}
		db.createTable(resultsTable, fieldDefs.toArray(new String[0]));

		String sql = "INSERT INTO " + TABLE_OUTPUT_RESULTS + " (" + String.join(", ", cols) + ") VALUES ("
				+ placeholders(cols.size()) + ")";

		db.execOnConnection(conn -> {
			boolean autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			try (IHMPreparedStatement ps = conn.prepareStatement(sql)) {
				for (int i = 0; i < stressedETs.length; i++) {
					int pos = 1;
					ps.setInt(pos++, i);
					if (withZ)
						ps.setDouble(pos++, z[i]);
					if (withRootDensity)
						ps.setDouble(pos++, rootDensity[i]);
					if (withG)
						ps.setDouble(pos++, g[i]);
					if (withGE)
						ps.setDouble(pos++, gE[i]);
					ps.setDouble(pos++, stressedETs[i]);
					if (withTranspiration)
						ps.setDouble(pos++, transpirations[i]);
					if (withEvaporation)
						ps.setDouble(pos++, evaporations[i]);
					ps.addBatch();
				}
				ps.executeBatch();
				conn.commit();
				conn.setAutoCommit(autoCommit);
			}
			return null;
		});
	}

	private void writeParametersIfPresent() throws Exception {
		if (parameters == null || parameters.isEmpty()) {
			return;
		}
		SqlName parametersTable = SqlName.m(TABLE_OUTPUT_PARAMETERS);
		List<String> paramFieldDefs = new ArrayList<>();
		paramFieldDefs.add(COL_ID + " INTEGER PRIMARY KEY");
		for (Map.Entry<String, Object> e : parameters.entrySet()) {
			String sqlType = (e.getValue() instanceof String) ? "TEXT"
					: (e.getValue() instanceof Integer) ? "INTEGER" : "REAL";
			paramFieldDefs.add(e.getKey() + " " + sqlType);
		}
		db.createTable(parametersTable, paramFieldDefs.toArray(new String[0]));

		String paramColsCsv = "id, " + String.join(", ", parameters.keySet());
		String sql = "INSERT INTO " + TABLE_OUTPUT_PARAMETERS + " (" + paramColsCsv + ") VALUES ("
				+ placeholders(parameters.size() + 1) + ")";

		db.execOnConnection(conn -> {
			try (IHMPreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setInt(1, 1);
				int pos = 2;
				for (Object v : parameters.values()) {
					if (v instanceof String s) {
						ps.setString(pos++, s);
					} else if (v instanceof Integer i) {
						ps.setInt(pos++, i);
					} else {
						ps.setDouble(pos++, ((Number) v).doubleValue());
					}
				}
				ps.addBatch();
				ps.executeBatch();
			}
			return null;
		});
	}
}
