package org.geoframe.brokergeo;

import java.net.URL;
import java.nio.file.Paths;

import junit.framework.TestCase;

public abstract class BrokerGeoTestCase extends TestCase {
	protected String getRes(String name) throws Exception {
		URL url = this.getClass().getResource(name);
		if (url == null) {
			throw new Exception("Resource not found: " + name);
		}
		return Paths.get(url.toURI()).toString();
	}
}
