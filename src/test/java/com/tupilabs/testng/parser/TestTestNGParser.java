/*
 * The MIT License
 *
 * Copyright (c) 2010 Bruno P. Kinoshita <http://www.kinoshita.eti.br>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.tupilabs.testng.parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @since 0.1
 */
public class TestTestNGParser extends TestCase {

	/**
	 * The TestNG parser.
	 */
	private TestNGParser parser;

	/**
	 * Initializes the TestNG parser.
	 */
	public void setUp() {
		this.parser = new TestNGParser();
	}

	public void testTestNGParser() {
		ClassLoader cl = TestTestNGParser.class.getClassLoader();
		URL url = cl.getResource("com/tupilabs/testng/parser/testng-results.xml");
		File file = new File(url.getFile());

		Suite suite = null;
		List<Suite> suites = null;

		try {
			suites = this.parser.parse(file);
			suite = suites.get(0);
		} catch (ParserException e) {
			fail("Failed to parse testng file '" + file + "': " + e.getMessage());
		}

		assertNotNull(suites);

		assertTrue(suite.getName().equals("Command line suite"));
		assertTrue(suite.getDurationMs().equals("0"));
		assertTrue(suite.getStartedAt().equals("2010-11-17T13:31:41Z"));
		assertTrue(suite.getFinishedAt().equals("2010-11-17T13:31:41Z"));

		List<Test> tests = suite.getTests();
		assertEquals(tests.size(), 1);

		Test test = tests.get(0);
		assertTrue(test.getDurationMs().equals("0"));
		assertTrue(test.getStartedAt().equals("2010-11-17T13:31:41Z"));
		assertTrue(test.getFinishedAt().equals("2010-11-17T13:31:41Z"));
		assertTrue(test.getName().equals("Command line test"));

		List<Class> classes = test.getClasses();
		assertTrue(classes.size() == 1);

		Class clazz = classes.get(0);
		assertNotNull(clazz);

		assertTrue(clazz.getName().equals("br.eti.kinoshita.Test1"));

		Set<TestMethod> testMethods = clazz.getTestMethods();

		assertTrue(testMethods.size() == 1);

		TestMethod testMethod = testMethods.iterator().next();

		assertNotNull(testMethod);

		assertTrue(testMethod.getStatus().equals("PASS"));
		assertTrue(testMethod.getSignature().equals("testVoid()"));
		assertTrue(testMethod.getName().equals("testVoid"));
		assertTrue(testMethod.getDurationMs().equals("0"));
		assertTrue(testMethod.getStartedAt().equals("2010-11-17T13:31:41Z"));
		assertTrue(testMethod.getFinishedAt().equals("2010-11-17T13:31:41Z"));

	}

	public void testInvalidTestNGFile() {
		ClassLoader cl = TestTestNGParser.class.getClassLoader();
		URL url = cl
				.getResource("com/tupilabs/testng/parser/testng-invalid-results.xml");
		File file = new File(url.getFile());

		try {
			this.parser.parse(file);
		} catch (Throwable t) {
			assertNotNull(t);
		}

	}

	private List<Suite> parseResourceSuite(String name) {
		File file = new File(TestTestNGParser.class.getResource(name).getFile());
		//Suite suite = null;
		List<Suite> suites = null;
		try {
			suites = this.parser.parse(file);
		} catch (ParserException e) {
			fail("Failed to parse testng file '" + file + "': " + e.getMessage());
		}
		return suites;
	}

	public void testMethodIterationOrder() {

		List<Suite> suites = parseResourceSuite("testng-results-ordered.xml");

		String last = null;
		for (Suite suite : suites) {
			for (TestMethod method : suite.getTests().get(0).getClasses().get(0).getTestMethods()) {
				String testName = method.getName();
				if (last != null) {
					assertTrue("test not in correct order:" + testName,
							last.compareTo(method.getName()) < 0);
				}
				last = testName;
			}
		}
		assertNotNull("did not find any testMethods..", last);
	}

	public void testParseNonExistentFileThrowsParserException() {
		// Parsing a file that does not exist should wrap IOException in ParserException
		File nonExistent = new File("/tmp/does_not_exist_testng_results_" + System.nanoTime() + ".xml");
		try {
			parser.parse(nonExistent);
			fail("Expected ParserException for non-existent file");
		} catch (ParserException e) {
			assertNotNull(e.getCause());
			assertTrue("Cause should be IOException but was " + e.getCause().getClass().getName(),
					e.getCause() instanceof java.io.FileNotFoundException);
		}
	}

	public void testParseMalformedXmlThrowsParserException() throws IOException {
		// Malformed XML triggers SAXException which should be wrapped in ParserException
		File tempFile = File.createTempFile("testng-malformed-", ".xml");
		tempFile.deleteOnExit();
		FileWriter writer = new FileWriter(tempFile);
		writer.write("<testng-results><suite name=\"Broken\" duration-ms=\"0\"");
		writer.close();

		try {
			parser.parse(tempFile);
			fail("Expected ParserException for malformed XML");
		} catch (ParserException e) {
			assertNotNull(e.getCause());
			assertTrue("Cause should be SAXException but was " + e.getCause().getClass().getName(),
					e.getCause() instanceof org.xml.sax.SAXException);
		}
	}

	public void testParseEmptyFileThrowsParserException() throws IOException {
		// An empty file is not valid XML, so SAXException should be wrapped in ParserException
		File tempFile = File.createTempFile("testng-empty-", ".xml");
		tempFile.deleteOnExit();

		try {
			parser.parse(tempFile);
			fail("Expected ParserException for empty file");
		} catch (ParserException e) {
			assertNotNull(e.getCause());
			assertTrue("Cause should be SAXException but was " + e.getCause().getClass().getName(),
					e.getCause() instanceof org.xml.sax.SAXException);
		}
	}

	public void testParseMultipleSuites() {
		// The issue1 resource has two suites; verify both are parsed and file path is set
		ClassLoader cl = TestTestNGParser.class.getClassLoader();
		URL url = cl.getResource("com/tupilabs/testng/parser/issue1/testng-results.xml");
		File file = new File(url.getFile());

		List<Suite> suites = null;
		try {
			suites = parser.parse(file);
		} catch (ParserException e) {
			fail("Failed to parse multi-suite file: " + e.getMessage());
		}

		assertNotNull(suites);
		assertEquals("Expected 2 suites in issue1 resource", 2, suites.size());

		assertEquals("Default suite", suites.get(0).getName());
		assertEquals("suite2", suites.get(1).getName());
	}

	public void testParseSetsSuiteFilePath() {
		// After parsing, each suite should have its file path set to the absolute path of the input
		ClassLoader cl = TestTestNGParser.class.getClassLoader();
		URL url = cl.getResource("com/tupilabs/testng/parser/testng-results.xml");
		File file = new File(url.getFile());

		List<Suite> suites = null;
		try {
			suites = parser.parse(file);
		} catch (ParserException e) {
			fail("Failed to parse file: " + e.getMessage());
		}

		assertNotNull(suites);
		assertEquals(1, suites.size());
		assertEquals("Suite file path should match the input file's absolute path",
				file.getAbsolutePath(), suites.get(0).getFile());
	}

	public void testParseMultipleSuitesAllHaveFilePath() {
		// Every suite in a multi-suite file should have the file path set
		ClassLoader cl = TestTestNGParser.class.getClassLoader();
		URL url = cl.getResource("com/tupilabs/testng/parser/issue1/testng-results.xml");
		File file = new File(url.getFile());

		List<Suite> suites = null;
		try {
			suites = parser.parse(file);
		} catch (ParserException e) {
			fail("Failed to parse multi-suite file: " + e.getMessage());
		}

		assertNotNull(suites);
		for (Suite suite : suites) {
			assertEquals("Each suite should have the file path set",
					file.getAbsolutePath(), suite.getFile());
		}
	}

	public void testParseValidXmlWithNoSuitesReturnsEmptyList() throws IOException {
		// Valid XML that has no <suite> elements should return an empty list
		File tempFile = File.createTempFile("testng-nosuite-", ".xml");
		tempFile.deleteOnExit();
		FileWriter writer = new FileWriter(tempFile);
		writer.write("<testng-results></testng-results>");
		writer.close();

		List<Suite> suites = null;
		try {
			suites = parser.parse(tempFile);
		} catch (ParserException e) {
			fail("Should not throw for valid XML with no suites: " + e.getMessage());
		}

		assertNotNull(suites);
		assertEquals("Expected empty list when no suites in XML", 0, suites.size());
	}

	public void testParseNullFileThrowsException() {
		// Passing null should throw an exception (NullPointerException or ParserException)
		try {
			parser.parse(null);
			fail("Expected an exception for null file input");
		} catch (ParserException e) {
			// Acceptable: wrapped in ParserException
		} catch (NullPointerException e) {
			// Acceptable: null dereference before try block
		}
	}

	public void testParseDataProviderAttribute() {
		// The issue1 resource has test methods with data-provider; verify it is parsed
		ClassLoader cl = TestTestNGParser.class.getClassLoader();
		URL url = cl.getResource("com/tupilabs/testng/parser/issue1/testng-results.xml");
		File file = new File(url.getFile());

		List<Suite> suites = null;
		try {
			suites = parser.parse(file);
		} catch (ParserException e) {
			fail("Failed to parse file: " + e.getMessage());
		}

		assertNotNull(suites);
		Suite suite = suites.get(0);
		List<Test> tests = suite.getTests();
		assertEquals(1, tests.size());

		List<Class> classes = tests.get(0).getClasses();
		assertEquals(1, classes.size());

		Set<TestMethod> methods = classes.get(0).getTestMethods();
		assertFalse("Expected at least one test method", methods.isEmpty());

		TestMethod firstMethod = methods.iterator().next();
		assertEquals("validateFieldBase", firstMethod.getDataProvider());
	}

	public void testParseNotWellFormedXmlWithExtraContent() throws IOException {
		// XML with content after the root closing tag should trigger SAXException
		File tempFile = File.createTempFile("testng-extraxml-", ".xml");
		tempFile.deleteOnExit();
		FileWriter writer = new FileWriter(tempFile);
		writer.write("<testng-results></testng-results><extra>bad</extra>");
		writer.close();

		try {
			parser.parse(tempFile);
			fail("Expected ParserException for XML with content after root element");
		} catch (ParserException e) {
			assertNotNull(e.getCause());
			assertTrue("Cause should be SAXException but was " + e.getCause().getClass().getName(),
					e.getCause() instanceof org.xml.sax.SAXException);
		}
	}

	public void testParserIsSerializable() throws IOException, ClassNotFoundException {
		// TestNGParser implements Serializable; verify it can be serialized and deserialized
		java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
		java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
		oos.writeObject(parser);
		oos.close();

		java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
		java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
		Object deserialized = ois.readObject();
		ois.close();

		assertNotNull(deserialized);
		assertTrue("Deserialized object should be a TestNGParser",
				deserialized instanceof TestNGParser);
	}

	public void testParseDirectoryInsteadOfFileThrowsParserException() throws IOException {
		// Passing a directory instead of a file should trigger an IOException wrapped in ParserException
		File tempDir = File.createTempFile("testng-dir-", "");
		tempDir.delete();
		tempDir.mkdir();
		tempDir.deleteOnExit();

		try {
			parser.parse(tempDir);
			fail("Expected ParserException when parsing a directory");
		} catch (ParserException e) {
			assertNotNull(e.getCause());
		}
	}

	public void testParseSuiteWithMultipleTestsAndClasses() throws IOException {
		// Verify parsing of a suite with multiple <test> elements each with multiple <class> elements
		File tempFile = File.createTempFile("testng-multi-", ".xml");
		tempFile.deleteOnExit();
		FileWriter writer = new FileWriter(tempFile);
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		writer.write("<testng-results>\n");
		writer.write("  <suite name=\"MultiSuite\" duration-ms=\"100\" started-at=\"2024-01-01T00:00:00Z\" finished-at=\"2024-01-01T00:01:00Z\">\n");
		writer.write("    <test name=\"Test1\" duration-ms=\"50\" started-at=\"2024-01-01T00:00:00Z\" finished-at=\"2024-01-01T00:00:30Z\">\n");
		writer.write("      <class name=\"com.example.ClassA\">\n");
		writer.write("        <test-method status=\"PASS\" signature=\"methodA()\" name=\"methodA\" duration-ms=\"10\" started-at=\"2024-01-01T00:00:00Z\" finished-at=\"2024-01-01T00:00:10Z\"></test-method>\n");
		writer.write("      </class>\n");
		writer.write("      <class name=\"com.example.ClassB\">\n");
		writer.write("        <test-method status=\"FAIL\" signature=\"methodB()\" name=\"methodB\" duration-ms=\"20\" started-at=\"2024-01-01T00:00:10Z\" finished-at=\"2024-01-01T00:00:30Z\"></test-method>\n");
		writer.write("      </class>\n");
		writer.write("    </test>\n");
		writer.write("    <test name=\"Test2\" duration-ms=\"50\" started-at=\"2024-01-01T00:00:30Z\" finished-at=\"2024-01-01T00:01:00Z\">\n");
		writer.write("      <class name=\"com.example.ClassC\">\n");
		writer.write("        <test-method status=\"SKIP\" signature=\"methodC()\" name=\"methodC\" duration-ms=\"5\" started-at=\"2024-01-01T00:00:30Z\" finished-at=\"2024-01-01T00:00:35Z\"></test-method>\n");
		writer.write("      </class>\n");
		writer.write("    </test>\n");
		writer.write("  </suite>\n");
		writer.write("</testng-results>\n");
		writer.close();

		List<Suite> suites = parser.parse(tempFile);
		assertNotNull(suites);
		assertEquals(1, suites.size());

		Suite suite = suites.get(0);
		assertEquals("MultiSuite", suite.getName());
		assertEquals("100", suite.getDurationMs());
		assertEquals(tempFile.getAbsolutePath(), suite.getFile());

		List<Test> tests = suite.getTests();
		assertEquals(2, tests.size());

		assertEquals("Test1", tests.get(0).getName());
		assertEquals(2, tests.get(0).getClasses().size());
		assertEquals("com.example.ClassA", tests.get(0).getClasses().get(0).getName());
		assertEquals("com.example.ClassB", tests.get(0).getClasses().get(1).getName());

		assertEquals("Test2", tests.get(1).getName());
		assertEquals(1, tests.get(1).getClasses().size());

		TestMethod skipMethod = tests.get(1).getClasses().get(0).getTestMethods().iterator().next();
		assertEquals("SKIP", skipMethod.getStatus());
		assertEquals("methodC", skipMethod.getName());
	}

	public void testParseIsConfigAttribute() throws IOException {
		// Verify that the is-config attribute on test-method is parsed correctly
		File tempFile = File.createTempFile("testng-config-", ".xml");
		tempFile.deleteOnExit();
		FileWriter writer = new FileWriter(tempFile);
		writer.write("<testng-results>\n");
		writer.write("  <suite name=\"ConfigSuite\" duration-ms=\"0\" started-at=\"2024-01-01T00:00:00Z\" finished-at=\"2024-01-01T00:00:00Z\">\n");
		writer.write("    <test name=\"ConfigTest\" duration-ms=\"0\" started-at=\"2024-01-01T00:00:00Z\" finished-at=\"2024-01-01T00:00:00Z\">\n");
		writer.write("      <class name=\"com.example.Setup\">\n");
		writer.write("        <test-method status=\"PASS\" signature=\"setUp()\" name=\"setUp\" is-config=\"true\" duration-ms=\"0\" started-at=\"2024-01-01T00:00:00Z\" finished-at=\"2024-01-01T00:00:00Z\"></test-method>\n");
		writer.write("      </class>\n");
		writer.write("    </test>\n");
		writer.write("  </suite>\n");
		writer.write("</testng-results>\n");
		writer.close();

		List<Suite> suites = parser.parse(tempFile);
		assertNotNull(suites);
		assertEquals(1, suites.size());

		TestMethod configMethod = suites.get(0).getTests().get(0).getClasses().get(0)
				.getTestMethods().iterator().next();
		assertEquals("true", configMethod.getIsConfig());
		assertEquals("setUp", configMethod.getName());
	}
}
