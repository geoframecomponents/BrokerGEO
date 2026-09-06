package org.geoframe.brokergeo.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hortonmachine.dbs.compat.ADb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.dbs.compat.IHMPreparedStatement;
import org.hortonmachine.dbs.utils.SqlName;
import org.hortonmachine.gears.io.geoframe.whetgeo.Whetgeo1DOutputSchema;

/**
 * DB-based output handler for BrokerGEO test results.
 * <p>
 * Table name is prefixed {@value #PREFIX}, the same convention
 * {@code Whetgeo1DOutputsHandler}/{@code GeoetOutputsHandler} use.
 *
 * <p>
 * {@link #transpirations} and {@link #evaporations} are independently
 * optional: leaving either {@code null} omits its column - {@code
 * ETsBrokerOneFluxSolverMain} only produces {@link #stressedETs}, while
 * {@code ETsBrokerTwoFluxesSolverMain} produces all three.
 *
 * <p>
 * This class has two independent write modes, coexisting side by side:
 * <ul>
 * <li>the original one-shot mode above ({@link #write()} into {@link
 * #TABLE_OUTPUT_RESULTS}) - BrokerGEO's own solvers are per-instant snapshot
 * calculators with no time-looping concept, so a native BrokerGEO test calls
 * {@code solve()} and {@link #write()} exactly once (see {@code
 * TestBrokerGEOOneFluxGpkg}/{@code TestBrokerGEOTwoFluxesGpkg});
 * <li>a buffered, per-timestep mode ({@link #writeStep()} into {@link
 * #TABLE_OUTPUT_UPTAKE}, timestamp x eta long format matching WHETGEO-1D's
 * own {@code output_state} join-key convention) for a caller that itself
 * loops over time and calls this solver once per step - e.g. GEOSPACE-1D's
 * coupled stack, where {@code stressedETs} genuinely is BrokerGEO's own
 * output even though the time-loop driving it lives in another project.
 * </ul>
 */
public class BrokerGeoOutputsHandler implements AutoCloseable {

	public static final String PREFIX = "geoframe_brokergeo";
	public static final String TABLE_OUTPUT_RESULTS = PREFIX + "_output_results";
	/** Buffered, per-timestep counterpart to {@link #TABLE_OUTPUT_RESULTS} - see {@link #writeStep()}. */
	public static final String TABLE_OUTPUT_UPTAKE = PREFIX + "_output_uptake";
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
	 * Optional per-cell input context (depth, root density, stress factor),
	 * denormalized into the results table so the output gpkg alone can be
	 * charted (e.g. stressedETs vs z, or vs rootDensity/g) without joining back
	 * to the input fixture. {@code g} is the single stress array
	 * {@code CurrentStepInput} has - shared between transpiration and
	 * evaporation, see {@code BrokerGeoInputsHandler.g}'s javadoc.
	 */
	public double[] z;
	public double[] rootDensity;
	public double[] g;

	/**
	 * Input parameter snapshot, written once if non-null and non-empty. Values
	 * must be {@link String}, {@link Integer} or a {@link Number} (stored as
	 * REAL) - the same convention {@code GpkgFixtureBuilder} uses for the input
	 * {@code parameters} table.
	 */
	public Map<String, Object> parameters;

	// --- timestepped mode (see writeStep()) ---
	/** Set once before the first {@link #writeStep()}: the real cells' eta coordinates. */
	public double[] eta;
	/** Set before each {@link #writeStep()} call. */
	public long timestamp;

	private final ADb db;
	private final boolean ownsDb;
	private final int bufferSize;

	private boolean stepInitialized = false;
	private String sqlInsertStep;
	private final List<Long> stepTsBuf = new ArrayList<>();
	private final List<double[]> stepUptakeBuf = new ArrayList<>();

	public BrokerGeoOutputsHandler(String dbPath) throws Exception {
		// each test run produces a fresh output gpkg; a stale file from a previous
		// run would otherwise collide with this run's rows
		File existing = new File(dbPath);
		if (existing.exists()) {
			existing.delete();
		}
		this.db = EDb.GEOPACKAGE.getDb();
		this.db.open(dbPath);
		this.ownsDb = true;
		this.bufferSize = 0;
	}

	/**
	 * Timestepped mode: shares an already-open {@link ADb} (not closed by this
	 * instance, and not deleted/recreated - other writers may already hold it
	 * open, see {@code GeospaceOutputsHandler} in GEOSPACE-1D for the same
	 * reasoning) and buffers {@link #writeStep()} calls, flushing every {@code
	 * bufferSize} steps.
	 */
	public BrokerGeoOutputsHandler(ADb db, int bufferSize) {
		this.db = db;
		this.ownsDb = false;
		this.bufferSize = bufferSize;
	}

	/** Writes the current snapshot (and the parameter snapshot, if set). Call once. */
	public void write() throws Exception {
		writeResults();
		writeParametersIfPresent();
	}

	/**
	 * Accumulates the current step ({@link #timestamp}, {@link #stressedETs})
	 * into {@link #TABLE_OUTPUT_UPTAKE}, flushing to the DB every {@code
	 * bufferSize} steps - see the class javadoc's timestepped-mode description.
	 */
	public void writeStep() throws Exception {
		if (!stepInitialized) {
			initializeStep();
		}
		stepTsBuf.add(timestamp);
		stepUptakeBuf.add(stressedETs.clone());
		if (stepTsBuf.size() >= bufferSize) {
			flushStep();
		}
	}

	@Override
	public void close() throws Exception {
		flushStep();
		if (ownsDb) {
			db.close();
		}
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
		boolean withTranspiration = (transpirations != null);
		boolean withEvaporation = (evaporations != null);

		List<String> cols = new ArrayList<>(List.of(COL_ID));
		if (withZ)
			cols.add(COL_Z);
		if (withRootDensity)
			cols.add(COL_ROOT_DENSITY);
		if (withG)
			cols.add(COL_G);
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

	private void initializeStep() throws Exception {
		SqlName uptakeTable = SqlName.m(TABLE_OUTPUT_UPTAKE);
		if (!db.hasTable(uptakeTable)) {
			db.createTable(uptakeTable, COL_ID + " INTEGER PRIMARY KEY", Whetgeo1DOutputSchema.COL_TIMESTAMP + " INTEGER",
					Whetgeo1DOutputSchema.COL_ETA + " REAL", COL_STRESSED_ETS + " REAL");
			db.createIndex(uptakeTable, Whetgeo1DOutputSchema.COL_TIMESTAMP, false);
			db.createIndex(uptakeTable, Whetgeo1DOutputSchema.COL_ETA, false);
		}
		sqlInsertStep = String.format("""
				INSERT INTO %s (%s, %s, %s)
				VALUES (?, ?, ?)
				""", TABLE_OUTPUT_UPTAKE, Whetgeo1DOutputSchema.COL_TIMESTAMP, Whetgeo1DOutputSchema.COL_ETA,
				COL_STRESSED_ETS);
		stepInitialized = true;
	}

	private void flushStep() throws Exception {
		if (stepTsBuf.isEmpty()) {
			return;
		}
		int n = stepTsBuf.size();
		int kmax = eta.length;
		db.execOnConnection(conn -> {
			boolean autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			try (IHMPreparedStatement ps = conn.prepareStatement(sqlInsertStep)) {
				for (int r = 0; r < n; r++) {
					long ts = stepTsBuf.get(r);
					double[] uptake = stepUptakeBuf.get(r);
					for (int k = 0; k < kmax; k++) {
						ps.setLong(1, ts);
						ps.setDouble(2, eta[k]);
						ps.setDouble(3, uptake[k]);
						ps.addBatch();
					}
				}
				ps.executeBatch();
				conn.commit();
				conn.setAutoCommit(autoCommit);
			}
			return null;
		});
		stepTsBuf.clear();
		stepUptakeBuf.clear();
	}
}
