package org.geoframe.brokergeo.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geoframe.brokergeo.io.BrokerGeoInputsHandler;
import org.hortonmachine.dbs.compat.ADb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.dbs.compat.IHMPreparedStatement;
import org.hortonmachine.dbs.utils.SqlName;

/**
 * Reusable tool that builds a BrokerGEO test input GeoPackage (one
 * {@code parameters} row + one {@code grid} table, see
 * {@link BrokerGeoInputsHandler}) from the same scalar literals and arrays a
 * original test currently hardcodes. Used to create the fixture files under
 * {@code src/test/resources/Input/gpkg/}
 */
public class GpkgFixtureBuilder {

	/**
	 * @param outputGpkgPath path to write; overwritten if it already exists
	 * @param parameters     scalar config, one row in the {@code parameters}
	 *                       table; values must be {@link String},
	 *                       {@link Integer} or a {@link Number} (stored as REAL)
	 * @param z              per-cell depth, one row per entry in the
	 *                       {@code grid} table (may be one longer than
	 *                       {@code rootDensity}/{@code g}/{@code gE} - the
	 *                       trailing row(s) just leave those columns NULL)
	 * @param deltaZ         per-cell control-volume length, same length as
	 *                       {@code z}
	 * @param rootDensity    per-cell root density, or {@code null} if unused
	 * @param g              per-cell transpiration water-stress factor, or
	 *                       {@code null} if unused
	 * @param gE             per-cell evaporation water-stress factor, or
	 *                       {@code null} if unused (two-flux scenarios only)
	 */
	public static void build(String outputGpkgPath, Map<String, Object> parameters, double[] z, double[] deltaZ,
			double[] rootDensity, double[] g, double[] gE) throws Exception {
		java.io.File out = new java.io.File(outputGpkgPath);
		if (out.exists()) {
			out.delete();
		}

		try (ADb db = EDb.GEOPACKAGE.getDb()) {
			db.open(outputGpkgPath);
			writeParameters(db, parameters);
			writeGrid(db, z, deltaZ, rootDensity, g, gE);
		}
	}

	private static void writeParameters(ADb db, Map<String, Object> parameters) throws Exception {
		SqlName table = SqlName.m(BrokerGeoInputsHandler.TABLE_PARAMETERS);

		List<String> fieldDefs = new ArrayList<>();
		fieldDefs.add(BrokerGeoInputsHandler.COL_ID + " INTEGER PRIMARY KEY");
		for (Map.Entry<String, Object> e : parameters.entrySet()) {
			String sqlType = (e.getValue() instanceof String) ? "TEXT"
					: (e.getValue() instanceof Integer) ? "INTEGER" : "REAL";
			fieldDefs.add(e.getKey() + " " + sqlType);
		}
		db.createTable(table, fieldDefs.toArray(new String[0]));

		String colsCsv = BrokerGeoInputsHandler.COL_ID + ", " + String.join(", ", parameters.keySet());
		StringBuilder placeholders = new StringBuilder("?");
		for (int i = 0; i < parameters.size(); i++) {
			placeholders.append(", ?");
		}
		String sql = "INSERT INTO " + table.fixedDoubleName + " (" + colsCsv + ") VALUES (" + placeholders + ")";

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

	private static void writeGrid(ADb db, double[] z, double[] deltaZ, double[] rootDensity, double[] g, double[] gE)
			throws Exception {
		SqlName table = SqlName.m(BrokerGeoInputsHandler.TABLE_GRID);

		List<String> cols = new ArrayList<>(List.of(BrokerGeoInputsHandler.COL_Z, BrokerGeoInputsHandler.COL_DELTA_Z));
		if (rootDensity != null)
			cols.add(BrokerGeoInputsHandler.COL_ROOT_DENSITY);
		if (g != null)
			cols.add(BrokerGeoInputsHandler.COL_G);
		if (gE != null)
			cols.add(BrokerGeoInputsHandler.COL_GE);

		List<String> fieldDefs = new ArrayList<>();
		fieldDefs.add(BrokerGeoInputsHandler.COL_ID + " INTEGER PRIMARY KEY");
		for (String c : cols) {
			fieldDefs.add(c + " REAL");
		}
		db.createTable(table, fieldDefs.toArray(new String[0]));

		String sql = "INSERT INTO " + table.fixedDoubleName + " (" + BrokerGeoInputsHandler.COL_ID + ", "
				+ String.join(", ", cols) + ") VALUES (" + placeholders(cols.size() + 1) + ")";

		db.execOnConnection(conn -> {
			boolean autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			try (IHMPreparedStatement ps = conn.prepareStatement(sql)) {
				for (int i = 0; i < z.length; i++) {
					int pos = 1;
					ps.setInt(pos++, i);
					ps.setDouble(pos++, z[i]);
					ps.setDouble(pos++, deltaZ[i]);
					if (rootDensity != null)
						setNullableDouble(ps, pos++, rootDensity, i);
					if (g != null)
						setNullableDouble(ps, pos++, g, i);
					if (gE != null)
						setNullableDouble(ps, pos++, gE, i);
					ps.addBatch();
				}
				ps.executeBatch();
				conn.commit();
				conn.setAutoCommit(autoCommit);
			}
			return null;
		});
	}

	private static void setNullableDouble(IHMPreparedStatement ps, int pos, double[] arr, int i) throws Exception {
		if (i < arr.length) {
			ps.setDouble(pos, arr[i]);
		} else {
			ps.setObject(pos, null);
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
}
