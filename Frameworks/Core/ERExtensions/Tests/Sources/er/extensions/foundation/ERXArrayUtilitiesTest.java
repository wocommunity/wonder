
package er.extensions.foundation;

import com.webobjects.eocontrol.EOSortOrdering;

import com.webobjects.foundation.NSSelector;

import er.extensions.eof.ERXConstant;

import junit.framework.Assert;
import junit.framework.TestCase;

/** Tests of the public API of the ERXArrayUtilities class.
 *
 * This source file is automatically generated. The method names may be improved, and re-naming tests has no ill effect.
 * Feel free to add tests or change tests to demonstrate what should be the "contracted" behavior of the class.
 *
 * @author ray@ganymede.org, Ray Kiddy
 */
public class ERXArrayUtilitiesTest extends TestCase {

    public void testSetFromArray() {
        // public static com.webobjects.foundation.NSSet setFromArray(com.webobjects.foundation.NSArray);
    }

    /* Tests that this method does create an NSSelector from a number of valid keys, and also tests that when there is a
     * "special" NSSelector instance to be found, that that NSSelector is returned.
     *
     * @see er.extensions.foundation.ERXArrayUtilities
     */
    public void testSortSelectorWithKey() {

        Assert.assertEquals(new NSSelector("random", ERXConstant.ObjectClassArray), ERXArrayUtilities.sortSelectorWithKey("random"));

        Assert.assertNull(ERXArrayUtilities.sortSelectorWithKey(""));

        Assert.assertNull(ERXArrayUtilities.sortSelectorWithKey(null));

        Assert.assertEquals(EOSortOrdering.CompareAscending, ERXArrayUtilities.sortSelectorWithKey("compareAscending"));

        // intended to be same as Assert.assertNotEquals of the above condition. Note that "equals" says the two selectors are equal
        // even when they are of different classes. The equals method was obviously overriden to do this. -rrk
        //
        Assert.assertEquals(new NSSelector("compareAscending", ERXConstant.ObjectClassArray), EOSortOrdering.CompareAscending);
        Assert.assertTrue(! (new NSSelector("compareAscending", ERXConstant.ObjectClassArray)).getClass().equals(EOSortOrdering.CompareAscending.getClass()));

        Assert.assertEquals(new NSSelector("compareCaseInsensitiveAscending", ERXConstant.ObjectClassArray), EOSortOrdering.CompareCaseInsensitiveAscending);
        Assert.assertTrue(! (new NSSelector("compareCaseInsensitiveAscending", ERXConstant.ObjectClassArray)).getClass().equals(EOSortOrdering.CompareCaseInsensitiveAscending.getClass()));

        Assert.assertEquals(new NSSelector("compareCaseInsensitiveDescending", ERXConstant.ObjectClassArray), EOSortOrdering.CompareCaseInsensitiveDescending);
        Assert.assertTrue(! (new NSSelector("compareCaseInsensitiveDescending", ERXConstant.ObjectClassArray)).getClass().equals(EOSortOrdering.CompareCaseInsensitiveDescending.getClass()));

        Assert.assertEquals(new NSSelector("compareDescending", ERXConstant.ObjectClassArray), EOSortOrdering.CompareDescending);
        Assert.assertTrue(! (new NSSelector("compareDescending", ERXConstant.ObjectClassArray)).getClass().equals(EOSortOrdering.CompareDescending.getClass()));
    }

    public void testArrayGroupedByKeyPathERXKey() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByKeyPath(com.webobjects.foundation.NSArray, er.extensions.eof.ERXKey);
    }

    public void testArrayGroupedByKeyPathString() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByKeyPath(com.webobjects.foundation.NSArray, java.lang.String);
    }

    public void testArrayGroupedByKeyPathERXKeyboolean() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByKeyPath(com.webobjects.foundation.NSArray, er.extensions.eof.ERXKey, boolean, er.extensions.eof.ERXKey);
    }

    public void testArrayGroupedByKeyPathStringboolean() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByKeyPath(com.webobjects.foundation.NSArray, java.lang.String, boolean, java.lang.String);
    }

    public void testArrayGroupedByKeyPathERXKeyObject() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByKeyPath(com.webobjects.foundation.NSArray, er.extensions.eof.ERXKey, java.lang.Object, er.extensions.eof.ERXKey);
    }

    public void testArrayGroupedByKeyPathStringObject() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByKeyPath(com.webobjects.foundation.NSArray, java.lang.String, java.lang.Object, java.lang.String);
    }

    public void testArrayGroupedByToManyKeyPathERXKey() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByToManyKeyPath(com.webobjects.foundation.NSArray, er.extensions.eof.ERXKey, boolean);
    }

    public void testArrayGroupedByToManyKeyPathString() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByToManyKeyPath(com.webobjects.foundation.NSArray, java.lang.String, boolean);
    }

    public void testArrayGroupedByToManyKeyPathERXKeyObject() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByToManyKeyPath(com.webobjects.foundation.NSArray, er.extensions.eof.ERXKey, java.lang.Object);
    }

    public void testArrayGroupedByToManyKeyPathStringObject() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByToManyKeyPath(com.webobjects.foundation.NSArray, java.lang.String, java.lang.Object);
    }

    public void testArrayGroupedByToManyKeyPathERXKeyObjectERXKey() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByToManyKeyPath(com.webobjects.foundation.NSArray, er.extensions.eof.ERXKey, java.lang.Object, er.extensions.eof.ERXKey);
    }

    public void testArrayGroupedByToManyKeyPathStringObjectString() {
        // public static com.webobjects.foundation.NSDictionary arrayGroupedByToManyKeyPath(com.webobjects.foundation.NSArray, java.lang.String, java.lang.Object, java.lang.String);
    }

    public void testArraysAreIdenticalSets() {
        // public static boolean arraysAreIdenticalSets(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray);
    }

    public void testfilteredArrayWithQualifierEvaluationNSArray() {
        // public static com.webobjects.foundation.NSArray filteredArrayWithQualifierEvaluation(com.webobjects.foundation.NSArray, com.webobjects.eocontrol.EOQualifierEvaluation);
    }

    public void testfilteredArrayWithQualifierEvaluationEnumeration() {
        // public static com.webobjects.foundation.NSArray filteredArrayWithQualifierEvaluation(java.util.Enumeration, com.webobjects.eocontrol.EOQualifierEvaluation);
    }

    public void testenumerationHasMatchWithQualifierEvaluation() {
        // public static boolean enumerationHasMatchWithQualifierEvaluation(java.util.Enumeration, com.webobjects.eocontrol.EOQualifierEvaluation);
    }

    public void testiteratorHasMatchWithQualifierEvaluation() {
        // public static boolean iteratorHasMatchWithQualifierEvaluation(java.util.Iterator, com.webobjects.eocontrol.EOQualifierEvaluation);
    }

    public void testfilteredArrayWithQualifierEvaluation() {
        // public static com.webobjects.foundation.NSArray filteredArrayWithQualifierEvaluation(java.util.Iterator, com.webobjects.eocontrol.EOQualifierEvaluation);
    }

    public void testArrayWithoutDuplicateKeyValue() {
        // public static com.webobjects.foundation.NSArray arrayWithoutDuplicateKeyValue(com.webobjects.foundation.NSArray, java.lang.String);
    }

    public void testArrayMinusArray() {
        // public static com.webobjects.foundation.NSArray arrayMinusArray(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray);
    }

    public void testArrayMinusObject() {
        // public static com.webobjects.foundation.NSArray arrayMinusObject(com.webobjects.foundation.NSArray, java.lang.Object);
    }

    public void testArrayByAddingObjectsFromArrayWithoutDuplicates() {
        // public static com.webobjects.foundation.NSArray arrayByAddingObjectsFromArrayWithoutDuplicates(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray);
    }

    public void testArrayByRemovingFirstObject() {
        // public static com.webobjects.foundation.NSArray arrayByRemovingFirstObject(com.webobjects.foundation.NSArray);
    }

    public void testSafeAddObject() {
        // public static void safeAddObject(com.webobjects.foundation.NSMutableArray, java.lang.Object);
    }

    public void testAddObjectsFromArrayWithoutDuplicates() {
        // public static void addObjectsFromArrayWithoutDuplicates(com.webobjects.foundation.NSMutableArray, com.webobjects.foundation.NSArray);
    }

    public void testflattenboolean() {
        // public static com.webobjects.foundation.NSArray flatten(com.webobjects.foundation.NSArray, boolean);
    }

    public void testflatten() {
        // public static com.webobjects.foundation.NSArray flatten(com.webobjects.foundation.NSArray);
    }

    public void testArrayFromPropertyList() {
        // public static com.webobjects.foundation.NSArray arrayFromPropertyList(java.lang.String, com.webobjects.foundation.NSBundle);
    }

    public void testvaluesForKeyPaths() {
        // public static com.webobjects.foundation.NSArray valuesForKeyPaths(java.lang.Object, com.webobjects.foundation.NSArray);
    }

    public void testfirstObject() {
        // public static java.lang.Object firstObject(com.webobjects.foundation.NSArray);
    }

    public void testindexOfFirstObjectWithValueForKeyPath() {
        // public static int indexOfFirstObjectWithValueForKeyPath(com.webobjects.foundation.NSArray, java.lang.Object, java.lang.String);
    }

    public void testfirstObjectWithValueForKeyPath() {
        // public static java.lang.Object firstObjectWithValueForKeyPath(com.webobjects.foundation.NSArray, java.lang.Object, java.lang.String);
    }

    public void testobjectsWithValueForKeyPath() {
        // public static com.webobjects.foundation.NSArray objectsWithValueForKeyPath(com.webobjects.foundation.NSArray, java.lang.Object, java.lang.String);
    }

    public void testSortedMutableArraySortedWithKey() {
        // public static com.webobjects.foundation.NSMutableArray sortedMutableArraySortedWithKey(com.webobjects.foundation.NSArray, java.lang.String);
    }

    public void testSortedArraySortedWithKey() {
        // public static com.webobjects.foundation.NSArray sortedArraySortedWithKey(com.webobjects.foundation.NSArray, java.lang.String);
    }

    public void testSortedArraySortedWithKeyNSSelector() {
        // public static com.webobjects.foundation.NSArray sortedArraySortedWithKey(com.webobjects.foundation.NSArray, java.lang.String, com.webobjects.foundation.NSSelector);
    }

    public void testSortedArraySortedWithKeys() {
        // public static com.webobjects.foundation.NSArray sortedArraySortedWithKeys(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray, com.webobjects.foundation.NSSelector);
    }

    public void testSortArrayWithKey() {
        // public static void sortArrayWithKey(com.webobjects.foundation.NSMutableArray, java.lang.String);
    }

    public void testSortArrayWithKeyNSSelector() {
        // public static void sortArrayWithKey(com.webobjects.foundation.NSMutableArray, java.lang.String, com.webobjects.foundation.NSSelector);
    }

    public void testinitialize() {
        // public static void initialize();
    }

    public void testmedian() {
        // public static java.lang.Number median(com.webobjects.foundation.NSArray, java.lang.String);
    }

    public void testdistinct() {
        // public static com.webobjects.foundation.NSArray distinct(com.webobjects.foundation.NSArray);
    }

    public void testArrayWithoutDuplicates() {
        // public static com.webobjects.foundation.NSArray arrayWithoutDuplicates(com.webobjects.foundation.NSArray);
    }

    public void testbatchedArrayWithSize() {
        // public static com.webobjects.foundation.NSArray batchedArrayWithSize(com.webobjects.foundation.NSArray, int);
    }

    public void testfilteredArrayWithEntityFetchSpecificationWithNSDictionary() {
        // public static com.webobjects.foundation.NSArray filteredArrayWithEntityFetchSpecification(com.webobjects.foundation.NSArray, java.lang.String, java.lang.String, com.webobjects.foundation.NSDictionary);
    }

    public void testfilteredArrayWithFetchSpecificationNamedEntityNamedBindings() {
        // public static com.webobjects.foundation.NSArray filteredArrayWithFetchSpecificationNamedEntityNamedBindings(com.webobjects.foundation.NSArray, java.lang.String, java.lang.String, com.webobjects.foundation.NSDictionary);
    }

    public void testfilteredArrayWithFetchSpecificationNamedEntityNamed() {
        // public static com.webobjects.foundation.NSArray filteredArrayWithFetchSpecificationNamedEntityNamed(com.webobjects.foundation.NSArray, java.lang.String, java.lang.String);
    }

    public void testfilteredArrayWithEntityFetchSpecification() {
        // public static com.webobjects.foundation.NSArray filteredArrayWithEntityFetchSpecification(com.webobjects.foundation.NSArray, java.lang.String, java.lang.String);
    }

    public void testShiftObjectLeft() {
        // public static void shiftObjectLeft(com.webobjects.foundation.NSMutableArray, java.lang.Object);
    }

    public void testShiftObjectRight() {
        // public static void shiftObjectRight(com.webobjects.foundation.NSMutableArray, java.lang.Object);
    }

    public void testArrayContainsAnyObjectFromArray() {
        // public static boolean arrayContainsAnyObjectFromArray(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray);
    }

    public void testArrayContainsArray() {
        // public static boolean arrayContainsArray(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray);
    }

    public void testintersectingElements() {
        // public static com.webobjects.foundation.NSArray intersectingElements(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray);
    }

    public void testreverse() {
        // public static com.webobjects.foundation.NSArray reverse(com.webobjects.foundation.NSArray);
    }

    public void testfriendlyDisplayForKeyPath() {
        // public static java.lang.String friendlyDisplayForKeyPath(com.webobjects.foundation.NSArray, java.lang.String, java.lang.String, java.lang.String, java.lang.String);
    }

    public void testArrayForKeysPath() {
        // public static com.webobjects.foundation.NSArray arrayForKeysPath(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSArray);
    }

    public void testremoveNullValues() {
        // public static com.webobjects.foundation.NSArray removeNullValues(com.webobjects.foundation.NSArray);
    }

    public void testobjectArrayCastToStringArray() {
        // public static java.lang.String[] objectArrayCastToStringArray(java.lang.Object[]);
    }

    public void testobjectiLangArrayToString() {
        // public static java.lang.String objectArrayToString(java.lang.Object[]);
    }

    public void testobjectLangArrayOfLangArrayToString() {
        // public static java.lang.String objectArrayToString(java.lang.Object[][]);
    }

    public void testobjectArraysOfNSArraysToString() {
        // public static java.lang.String objectArraysToString(com.webobjects.foundation.NSArray);
    }

    public void testremoveNullValuesFromEnd() {
        // public static com.webobjects.foundation.NSArray removeNullValuesFromEnd(com.webobjects.foundation.NSArray);
    }

    public void testtoStringArray() {
        // public static java.lang.String[] toStringArray(com.webobjects.foundation.NSArray);
    }

    public void testdictionaryOfObjectsIndexedByKeyPath() {
        // public static com.webobjects.foundation.NSDictionary dictionaryOfObjectsIndexedByKeyPath(com.webobjects.foundation.NSArray, java.lang.String);
    }

    public void testdictionaryOfObjectsIndexedByKeyPathThrowOnCollision() {
        // public static com.webobjects.foundation.NSDictionary dictionaryOfObjectsIndexedByKeyPathThrowOnCollision(com.webobjects.foundation.NSArray, java.lang.String, boolean);
    }

    public void testArrayBySelectingInstancesOfClass() {
        // public static com.webobjects.foundation.NSArray arrayBySelectingInstancesOfClass(com.webobjects.foundation.NSArray, java.lang.Class);
    }

    public void testSortedArrayUsingComparator() {
        // public static com.webobjects.foundation.NSArray sortedArrayUsingComparator(com.webobjects.foundation.NSArray, com.webobjects.foundation.NSComparator);
    }

    public void testArrayWithObjectsSwapped() {
        // public static com.webobjects.foundation.NSArray arrayWithObjectsSwapped(com.webobjects.foundation.NSArray, java.lang.Object, java.lang.Object);
    }

    public void testArrayWithObjectsAtIndexesSwapped() {
        // public static com.webobjects.foundation.NSArray arrayWithObjectsAtIndexesSwapped(com.webobjects.foundation.NSArray, int, int);
    }

    public void testSwapObjectsInArray() {
        // public static void swapObjectsInArray(com.webobjects.foundation.NSMutableArray, java.lang.Object, java.lang.Object);
    }

    public void testSwapObjectsAtIndexesInArray() {
        // public static void swapObjectsAtIndexesInArray(com.webobjects.foundation.NSMutableArray, int, int);
    }

    public void testSwapObjectWithObjectAtIndexInArray() {
        // public static void swapObjectWithObjectAtIndexInArray(com.webobjects.foundation.NSMutableArray, java.lang.Object, int);
    }

    public void testdeepCloneNSArray() {
        // public static com.webobjects.foundation.NSArray deepClone(com.webobjects.foundation.NSArray, boolean);
    }

    public void testdeepCloneNSSet() {
        // public static com.webobjects.foundation.NSSet deepClone(com.webobjects.foundation.NSSet, boolean);
    }
}
