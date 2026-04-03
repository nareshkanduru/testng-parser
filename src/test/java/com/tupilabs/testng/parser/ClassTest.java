package com.tupilabs.testng.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

public class ClassTest extends TestCase {

    private Class clazz;

    public void setUp() {
        clazz = new Class();
    }

    public void testDefaultConstructorInitializesEmptyTestMethods() {
        // A freshly constructed Class should have a non-null, empty set of test methods
        Set<TestMethod> methods = clazz.getTestMethods();
        assertNotNull(methods);
        assertEquals(0, methods.size());
    }

    public void testGetNameReturnsNullByDefault() {
        // Name is not set in the constructor, so it should be null initially
        assertNull(clazz.getName());
    }

    public void testSetAndGetName() {
        // Setting a name should be retrievable via getName
        clazz.setName("com.example.MyClass");
        assertEquals("com.example.MyClass", clazz.getName());
    }

    public void testSetNameToNull() {
        // Setting name to null should be allowed and retrievable
        clazz.setName("com.example.First");
        clazz.setName(null);
        assertNull(clazz.getName());
    }

    public void testSetNameToEmptyString() {
        // Empty string is a valid name value
        clazz.setName("");
        assertEquals("", clazz.getName());
    }

    public void testSetNameWithUnicodeCharacters() {
        // Unicode class names should be stored and retrieved correctly
        String unicodeName = "com.example.\u00e9\u00e8\u00ea.\u4e2d\u6587Class";
        clazz.setName(unicodeName);
        assertEquals(unicodeName, clazz.getName());
    }

    public void testAddTestMethodReturnsTrue() {
        // Adding a new unique test method to an empty class should return true
        TestMethod method = createTestMethod("testA", "testA()", "PASS", null);
        assertTrue(clazz.addTestMethod(method));
        assertEquals(1, clazz.getTestMethods().size());
    }

    public void testAddMultipleDistinctMethods() {
        // Adding multiple methods with different signatures should all succeed
        TestMethod m1 = createTestMethod("testA", "testA()", "PASS", null);
        TestMethod m2 = createTestMethod("testB", "testB()", "FAIL", null);
        TestMethod m3 = createTestMethod("testC", "testC()", "SKIP", null);
        assertTrue(clazz.addTestMethod(m1));
        assertTrue(clazz.addTestMethod(m2));
        assertTrue(clazz.addTestMethod(m3));
        assertEquals(3, clazz.getTestMethods().size());
    }

    public void testAddDuplicateMethodReturnsFalse() {
        // Adding an equal method (same name, signature, dataProvider) should return false since it's a Set
        TestMethod m1 = createTestMethod("testA", "testA()", "PASS", "dp1");
        TestMethod m2 = createTestMethod("testA", "testA()", "PASS", "dp1");
        assertTrue(clazz.addTestMethod(m1));
        assertFalse(clazz.addTestMethod(m2));
        assertEquals(1, clazz.getTestMethods().size());
    }

    public void testAddDuplicateMethodWithFailStatusUpdatesExisting() {
        // When adding a duplicate with FAIL status, the existing method's status should be updated to FAIL
        TestMethod original = createTestMethod("testA", "testA()", "PASS", "dp1");
        clazz.addTestMethod(original);

        TestMethod failDuplicate = createTestMethod("testA", "testA()", "FAIL", "dp1");
        clazz.addTestMethod(failDuplicate);

        assertEquals(1, clazz.getTestMethods().size());
        TestMethod stored = clazz.getTestMethods().iterator().next();
        assertEquals("FAIL", stored.getStatus());
    }

    public void testAddDuplicateMethodWithSkipStatusUpdatesExisting() {
        // When adding a duplicate with SKIP status, the existing method's status should be updated to SKIP
        TestMethod original = createTestMethod("testA", "testA()", "PASS", "dp1");
        clazz.addTestMethod(original);

        TestMethod skipDuplicate = createTestMethod("testA", "testA()", "SKIP", "dp1");
        clazz.addTestMethod(skipDuplicate);

        assertEquals(1, clazz.getTestMethods().size());
        TestMethod stored = clazz.getTestMethods().iterator().next();
        assertEquals("SKIP", stored.getStatus());
    }

    public void testAddDuplicateMethodWithPassStatusDoesNotUpdateExisting() {
        // When adding a duplicate with PASS status, the existing FAIL status should NOT be overwritten
        TestMethod original = createTestMethod("testA", "testA()", "FAIL", "dp1");
        clazz.addTestMethod(original);

        TestMethod passDuplicate = createTestMethod("testA", "testA()", "PASS", "dp1");
        clazz.addTestMethod(passDuplicate);

        assertEquals(1, clazz.getTestMethods().size());
        TestMethod stored = clazz.getTestMethods().iterator().next();
        assertEquals("FAIL", stored.getStatus());
    }

    public void testAddDuplicateWithUnknownStatusDoesNotUpdateExisting() {
        // An unknown status string (not pass/fail/skip) should not update the existing method's status
        TestMethod original = createTestMethod("testA", "testA()", "PASS", "dp1");
        clazz.addTestMethod(original);

        TestMethod unknownDuplicate = createTestMethod("testA", "testA()", "UNKNOWN", "dp1");
        clazz.addTestMethod(unknownDuplicate);

        assertEquals(1, clazz.getTestMethods().size());
        TestMethod stored = clazz.getTestMethods().iterator().next();
        assertEquals("PASS", stored.getStatus());
    }

    public void testAddDuplicateWithNullStatusDoesNotUpdateExisting() {
        // A null status should not update the existing method's status (Statuses.get(null) returns null)
        TestMethod original = createTestMethod("testA", "testA()", "PASS", "dp1");
        clazz.addTestMethod(original);

        TestMethod nullStatusDuplicate = createTestMethod("testA", "testA()", null, "dp1");
        clazz.addTestMethod(nullStatusDuplicate);

        assertEquals(1, clazz.getTestMethods().size());
        TestMethod stored = clazz.getTestMethods().iterator().next();
        assertEquals("PASS", stored.getStatus());
    }

    public void testAddMethodWithFailStatusCaseInsensitive() {
        // Statuses.get uses equalsIgnoreCase, so "fail" (lowercase) should also trigger the status update
        TestMethod original = createTestMethod("testA", "testA()", "PASS", "dp1");
        clazz.addTestMethod(original);

        TestMethod failLower = createTestMethod("testA", "testA()", "fail", "dp1");
        clazz.addTestMethod(failLower);

        TestMethod stored = clazz.getTestMethods().iterator().next();
        assertEquals("fail", stored.getStatus());
    }

    public void testAddMethodWithSkipStatusCaseInsensitive() {
        // "Skip" (mixed case) should also trigger the status update
        TestMethod original = createTestMethod("testA", "testA()", "PASS", "dp1");
        clazz.addTestMethod(original);

        TestMethod skipMixed = createTestMethod("testA", "testA()", "Skip", "dp1");
        clazz.addTestMethod(skipMixed);

        TestMethod stored = clazz.getTestMethods().iterator().next();
        assertEquals("Skip", stored.getStatus());
    }

    public void testAddMethodsDifferentDataProviderAreNotDuplicates() {
        // Methods with same name/signature but different dataProvider are distinct per TestMethod.equals
        TestMethod m1 = createTestMethod("testA", "testA()", "PASS", "dp1");
        TestMethod m2 = createTestMethod("testA", "testA()", "FAIL", "dp2");
        assertTrue(clazz.addTestMethod(m1));
        assertTrue(clazz.addTestMethod(m2));
        assertEquals(2, clazz.getTestMethods().size());
    }

    public void testRemoveTestMethodReturnsTrue() {
        // Removing an existing method should return true and reduce the set size
        TestMethod method = createTestMethod("testA", "testA()", "PASS", "dp1");
        clazz.addTestMethod(method);
        assertTrue(clazz.removeTestMethod(method));
        assertEquals(0, clazz.getTestMethods().size());
    }

    public void testRemoveTestMethodReturnsFalseWhenNotPresent() {
        // Removing a method that was never added should return false
        TestMethod method = createTestMethod("testA", "testA()", "PASS", "dp1");
        assertFalse(clazz.removeTestMethod(method));
    }

    public void testRemoveFromEmptySet() {
        // Removing from an empty set should return false without error
        TestMethod method = createTestMethod("testA", "testA()", "PASS", "dp1");
        assertFalse(clazz.removeTestMethod(method));
        assertEquals(0, clazz.getTestMethods().size());
    }

    public void testInsertionOrderIsPreserved() {
        // LinkedHashSet preserves insertion order; verify methods come back in order added
        TestMethod m1 = createTestMethod("alpha", "alpha()", "PASS", "dp1");
        TestMethod m2 = createTestMethod("beta", "beta()", "PASS", "dp2");
        TestMethod m3 = createTestMethod("gamma", "gamma()", "PASS", "dp3");
        clazz.addTestMethod(m1);
        clazz.addTestMethod(m2);
        clazz.addTestMethod(m3);

        Iterator<TestMethod> it = clazz.getTestMethods().iterator();
        assertEquals("alpha", it.next().getName());
        assertEquals("beta", it.next().getName());
        assertEquals("gamma", it.next().getName());
    }

    public void testAddRemoveAddSameMethod() {
        // Adding, removing, then re-adding the same method should work correctly
        TestMethod method = createTestMethod("testA", "testA()", "PASS", "dp1");
        assertTrue(clazz.addTestMethod(method));
        assertTrue(clazz.removeTestMethod(method));
        assertEquals(0, clazz.getTestMethods().size());
        assertTrue(clazz.addTestMethod(method));
        assertEquals(1, clazz.getTestMethods().size());
    }

    public void testSerializationRoundTrip() throws IOException, ClassNotFoundException {
        // Class implements Serializable; verify it survives serialization/deserialization with data intact
        clazz.setName("com.example.SerializableClass");
        TestMethod m1 = createTestMethod("testSer", "testSer()", "PASS", "dp1");
        clazz.addTestMethod(m1);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(clazz);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof Class);
        Class restored = (Class) deserialized;
        assertEquals("com.example.SerializableClass", restored.getName());
        assertEquals(1, restored.getTestMethods().size());
        assertEquals("testSer", restored.getTestMethods().iterator().next().getName());
    }

    public void testSerializationWithEmptyState() throws IOException, ClassNotFoundException {
        // Serializing a default-constructed Class (null name, empty methods) should work
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(clazz);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        Class restored = (Class) ois.readObject();
        ois.close();

        assertNull(restored.getName());
        assertEquals(0, restored.getTestMethods().size());
    }

    public void testAddMethodWithNullDataProviderNotEqualToMethodWithDataProvider() {
        // TestMethod.equals requires non-null dataProvider; a method with null dataProvider won't equal one with non-null
        TestMethod withDp = createTestMethod("testA", "testA()", "PASS", "dp1");
        TestMethod withoutDp = createTestMethod("testA", "testA()", "FAIL", null);
        assertTrue(clazz.addTestMethod(withDp));
        assertTrue(clazz.addTestMethod(withoutDp));
        assertEquals(2, clazz.getTestMethods().size());
    }

    public void testAddManyMethods() {
        // Verify the set handles a larger number of distinct methods correctly
        for (int i = 0; i < 100; i++) {
            TestMethod m = createTestMethod("test" + i, "test" + i + "()", "PASS", "dp" + i);
            assertTrue(clazz.addTestMethod(m));
        }
        assertEquals(100, clazz.getTestMethods().size());
    }

    public void testStatusUpdateFromFailToSkip() {
        // Adding a duplicate with SKIP when existing is FAIL should update to SKIP
        TestMethod original = createTestMethod("testA", "testA()", "FAIL", "dp1");
        clazz.addTestMethod(original);

        TestMethod skipDuplicate = createTestMethod("testA", "testA()", "SKIP", "dp1");
        clazz.addTestMethod(skipDuplicate);

        TestMethod stored = clazz.getTestMethods().iterator().next();
        assertEquals("SKIP", stored.getStatus());
    }

    public void testStatusUpdateFromSkipToFail() {
        // Adding a duplicate with FAIL when existing is SKIP should update to FAIL
        TestMethod original = createTestMethod("testA", "testA()", "SKIP", "dp1");
        clazz.addTestMethod(original);

        TestMethod failDuplicate = createTestMethod("testA", "testA()", "FAIL", "dp1");
        clazz.addTestMethod(failDuplicate);

        TestMethod stored = clazz.getTestMethods().iterator().next();
        assertEquals("FAIL", stored.getStatus());
    }

    public void testSetNameOverwritesPreviousValue() {
        // Calling setName multiple times should always reflect the latest value
        clazz.setName("first");
        clazz.setName("second");
        clazz.setName("third");
        assertEquals("third", clazz.getName());
    }

    public void testGetTestMethodsReturnsSameReferenceAcrossCalls() {
        // getTestMethods should return the same set instance (not a copy)
        Set<TestMethod> first = clazz.getTestMethods();
        Set<TestMethod> second = clazz.getTestMethods();
        assertSame(first, second);
    }

    private TestMethod createTestMethod(String name, String signature, String status, String dataProvider) {
        TestMethod method = new TestMethod();
        method.setName(name);
        method.setSignature(signature);
        method.setStatus(status);
        method.setDataProvider(dataProvider);
        return method;
    }
}
