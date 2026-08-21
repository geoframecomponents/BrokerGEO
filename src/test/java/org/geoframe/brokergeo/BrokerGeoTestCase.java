package org.geoframe.brokergeo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import junit.framework.TestCase;

public abstract class BrokerGeoTestCase extends TestCase {
	protected String getRes(String name) throws Exception {
		URL url = this.getClass().getResource(name);
		if (url == null) {
			throw new Exception("Resource not found: " + name);
		}
		return Paths.get(url.toURI()).toString();
	}

	/**
	 * Compares {@code actual} against a frozen baseline checked in at
	 * src/test/resources/golden/&lt;this test's simple class name&gt;/&lt;arrayName&gt;.csv
	 * (one value per line). This freezes today's (pre-refactor) computed values so a
	 * later refactor of the solver/methods classes can be checked against them.
	 * <p>
	 * To (re)capture the baseline after an intentional behavior change, delete the
	 * golden file and write the new actual values in its place, one per line.
	 */
	protected void assertGoldenArray( String arrayName, double[] actual ) throws IOException, URISyntaxException {
		String className = this.getClass().getSimpleName();
		String resourcePath = "/golden/" + className + "/" + arrayName + ".csv";
		URL goldenUrl = this.getClass().getResource(resourcePath);
		if (goldenUrl == null) {
			fail("No golden baseline for " + className + "/" + arrayName + " at src/test/resources/golden/"
					+ className + "/" + arrayName + ".csv -- capture one from a known-good run before relying on this assertion");
			return;
		}
		List<String> lines = Files.readAllLines(Paths.get(goldenUrl.toURI()));
		double[] golden = lines.stream().filter(l -> !l.isBlank()).mapToDouble(Double::parseDouble).toArray();

		assertEquals("Length mismatch for " + arrayName + " in " + className, golden.length, actual.length);
		for (int i = 0; i < golden.length; i++) {
			assertEquals("Value mismatch at index " + i + " for " + arrayName + " in " + className, golden[i],
					actual[i], 1e-9);
		}
	}
}
